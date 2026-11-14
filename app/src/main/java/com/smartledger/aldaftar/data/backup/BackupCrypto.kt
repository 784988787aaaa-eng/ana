package com.smartledger.aldaftar.data.backup

import android.content.Context
import android.util.Base64
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

class BackupCrypto(context: Context) {
    companion object {
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val TAG_BITS = 128
        private const val SECRET_BYTES = 20
        private const val PREFS = "smartledger_backup_key_v1"
        private const val FAST_ITERATIONS = 24_000
        private const val LEGACY_ITERATIONS = 120_000
        private const val VAULT_SEED = "SMARTLEDGER_UNIFIED_PORTABLE_VAULT_KEY_V2"
        private const val WRAP_SEED = "SMARTLEDGER_ENVELOPE_KEY_WRAP_MASTER_V2"

        private val keyCache = java.util.concurrent.ConcurrentHashMap<String, SecretKey>()

        private val portableVaultCode: String by lazy {
            val digest = java.security.MessageDigest.getInstance("SHA-256")
                .digest(VAULT_SEED.toByteArray(Charsets.UTF_8))
            val raw = digest.copyOf(SECRET_BYTES)
            Base64.encodeToString(raw, Base64.NO_WRAP or Base64.URL_SAFE).replace("=", "")
        }
    }

    private val appContext = context.applicationContext
    private val secure = SecureRandom()
    private val prefs by lazy {
        runCatching {
            EncryptedSharedPreferences.create(
                appContext, PREFS,
                MasterKey.Builder(appContext).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        }.getOrElse {
            appContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        }
    }

    fun localRecoveryCode(): String {
        val existing = prefs.getString("recovery", null)
        if (existing != null && existing != portableVaultCode) {
            // Preserve pre-existing installation key as legacy fallback so older local/cloud backups on this device still decrypt
            prefs.edit()
                .putString("legacy_recovery", existing)
                .putString("recovery", portableVaultCode)
                .apply()
        } else if (existing == null) {
            prefs.edit().putString("recovery", portableVaultCode).apply()
        }
        return portableVaultCode
    }

    private fun candidateRecoveryCodes(unwrappedKey: String?): List<String> {
        val candidates = LinkedHashSet<String>()
        if (!unwrappedKey.isNullOrBlank()) candidates.add(unwrappedKey.trim())
        candidates.add(localRecoveryCode())
        prefs.getString("legacy_recovery", null)?.takeIf { it.isNotBlank() }?.let { candidates.add(it.trim()) }
        prefs.getString("recovery", null)?.takeIf { it.isNotBlank() }?.let { candidates.add(it.trim()) }
        return candidates.toList()
    }

    fun wrapRecoveryKey(recoveryCode: String, salt: ByteArray, iv: ByteArray): String {
        return runCatching {
            val cipher = Cipher.getInstance(TRANSFORMATION)
            val wrapKey = deriveEnvelopeWrapKey(salt, iv)
            val wrapIv = java.security.MessageDigest.getInstance("SHA-256")
                .digest(iv + salt)
                .copyOf(12)
            cipher.init(Cipher.ENCRYPT_MODE, wrapKey, GCMParameterSpec(TAG_BITS, wrapIv))
            val encrypted = cipher.doFinal(recoveryCode.toByteArray(Charsets.UTF_8))
            Base64.encodeToString(encrypted, Base64.NO_WRAP)
        }.getOrDefault("")
    }

    fun unwrapRecoveryKey(keyWrapBase64: String?, salt: ByteArray, iv: ByteArray): String? {
        if (keyWrapBase64.isNullOrBlank()) return null
        return runCatching {
            val cipher = Cipher.getInstance(TRANSFORMATION)
            val wrapKey = deriveEnvelopeWrapKey(salt, iv)
            val wrapIv = java.security.MessageDigest.getInstance("SHA-256")
                .digest(iv + salt)
                .copyOf(12)
            cipher.init(Cipher.DECRYPT_MODE, wrapKey, GCMParameterSpec(TAG_BITS, wrapIv))
            val plain = cipher.doFinal(Base64.decode(keyWrapBase64, Base64.DEFAULT))
            String(plain, Charsets.UTF_8).takeIf { it.isNotBlank() }
        }.getOrNull()
    }

    private fun deriveEnvelopeWrapKey(salt: ByteArray, iv: ByteArray): SecretKey {
        val digest = java.security.MessageDigest.getInstance("SHA-256")
        digest.update(WRAP_SEED.toByteArray(Charsets.UTF_8))
        digest.update(salt)
        digest.update(iv)
        return SecretKeySpec(digest.digest(), "AES")
    }

    fun encrypt(plain: ByteArray): Triple<ByteArray, ByteArray, ByteArray> {
        val recovery = localRecoveryCode()
        val salt = ByteArray(16).also(secure::nextBytes)
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, deriveKey(recovery, salt, FAST_ITERATIONS))
        return Triple(salt, cipher.iv, cipher.doFinal(plain))
    }

    fun decrypt(
        salt: ByteArray,
        iv: ByteArray,
        cipherText: ByteArray,
        recoveryCode: String? = null,
        keyWrap: String? = null
    ): ByteArray {
        val explicitCode = recoveryCode?.trim()?.takeIf { it.isNotEmpty() }
        val codesToTry = if (explicitCode != null) {
            listOf(explicitCode)
        } else {
            val unwrapped = unwrapRecoveryKey(keyWrap, salt, iv)
            candidateRecoveryCodes(unwrapped)
        }

        val iterationsToTry = intArrayOf(FAST_ITERATIONS, LEGACY_ITERATIONS)
        var lastError: Throwable? = null

        for (code in codesToTry) {
            for (iterations in iterationsToTry) {
                try {
                    val cipher = Cipher.getInstance(TRANSFORMATION)
                    cipher.init(
                        Cipher.DECRYPT_MODE,
                        deriveKey(code, salt, iterations),
                        GCMParameterSpec(TAG_BITS, iv)
                    )
                    return cipher.doFinal(cipherText)
                } catch (e: Throwable) {
                    lastError = e
                }
            }
        }

        throw javax.crypto.AEADBadTagException(
            "تعذر فك تشفير النسخة الاحتياطية لأنها أُنشئت بمفتاح محلي قديم أو أن الملف تالف. يرجى إنشاء نسخة احتياطية جديدة لتوحيد المفتاح."
        ).apply {
            if (lastError != null) initCause(lastError)
        }
    }

    fun isLocalRecoveryCode(code: String): Boolean = runCatching { decode(code); true }.getOrDefault(false)

    private fun deriveKey(code: String, salt: ByteArray, iterations: Int = FAST_ITERATIONS): SecretKey {
        val cacheKey = "$iterations:$code:${Base64.encodeToString(salt, Base64.NO_WRAP)}"
        keyCache[cacheKey]?.let { return it }
        val spec = PBEKeySpec(code.toCharArray(), salt, iterations, 256)
        val bytes = try {
            SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded
        } finally {
            spec.clearPassword()
        }
        val secretKey = SecretKeySpec(bytes, "AES")
        if (keyCache.size > 64) keyCache.clear()
        keyCache[cacheKey] = secretKey
        return secretKey
    }

    private fun encode(raw: ByteArray): String =
        Base64.encodeToString(raw, Base64.NO_WRAP or Base64.URL_SAFE).replace("=", "")

    private fun decode(code: String): ByteArray {
        val normalized = code.filter { it.isLetterOrDigit() || it == '-' || it == '_' }
        require(normalized.length >= 24) { "مفتاح الاستعادة غير صالح" }
        val padded = normalized.padEnd((normalized.length + 3) / 4 * 4, '=')
        return Base64.decode(padded, Base64.URL_SAFE or Base64.NO_WRAP)
    }
}
