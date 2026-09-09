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
    companion object { private const val TRANSFORMATION = "AES/GCM/NoPadding"; private const val TAG_BITS = 128; private const val SECRET_BYTES = 20; private const val PREFS = "smartledger_backup_key_v1" }
    private val secure = SecureRandom()
    private val prefs = EncryptedSharedPreferences.create(
        context.applicationContext, PREFS,
        MasterKey.Builder(context.applicationContext).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun localRecoveryCode(): String {
        val stored = prefs.getString("recovery", null)
        if (stored != null) return stored
        val raw = ByteArray(SECRET_BYTES).also(secure::nextBytes)
        val code = encode(raw)
        prefs.edit().putString("recovery", code).apply()
        return code
    }

    fun encrypt(plain: ByteArray): Triple<ByteArray, ByteArray, ByteArray> {
        val recovery = localRecoveryCode()
        val salt = ByteArray(16).also(secure::nextBytes)
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, deriveKey(recovery, salt))
        return Triple(salt, cipher.iv, cipher.doFinal(plain))
    }

    fun decrypt(salt: ByteArray, iv: ByteArray, cipherText: ByteArray, recoveryCode: String? = null): ByteArray {
        val code = recoveryCode?.trim()?.uppercase() ?: localRecoveryCode()
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, deriveKey(code, salt), GCMParameterSpec(TAG_BITS, iv))
        return cipher.doFinal(cipherText)
    }

    fun isLocalRecoveryCode(code: String): Boolean = runCatching { decode(code); true }.getOrDefault(false)

    private fun deriveKey(code: String, salt: ByteArray): SecretKey {
        val spec = PBEKeySpec(code.toCharArray(), salt, 120_000, 256)
        val bytes = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded
        return SecretKeySpec(bytes, "AES")
    }

    private fun encode(raw: ByteArray): String = Base64.encodeToString(raw, Base64.NO_WRAP or Base64.URL_SAFE).replace("=", "")

    private fun decode(code: String): ByteArray {
        val normalized = code.filter { it.isLetterOrDigit() || it == '-' || it == '_' }
        require(normalized.length >= 24) { "مفتاح الاستعادة غير صالح" }
        val padded = normalized.padEnd((normalized.length + 3) / 4 * 4, '=')
        return Base64.decode(padded, Base64.URL_SAFE or Base64.NO_WRAP)
    }
}
