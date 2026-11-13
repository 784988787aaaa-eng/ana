package com.smartledger.aldaftar.platform.license

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.security.KeyFactory
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.MessageDigest
import java.security.PrivateKey
import java.security.Signature
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import java.util.UUID

class DeviceIdentity(private val context: Context) {
    companion object {
        private const val ALIAS = "smartledger_license_device_v1"
        private const val PREFS_NAME = "smartledger_device_identity_v1"
        private const val KEY_FALLBACK_SEED = "device_seed_v1"
        private const val KEY_FALLBACK_RSA_PUBLIC = "device_rsa_public_v1"
        private const val KEY_FALLBACK_RSA_PRIVATE = "device_rsa_private_v1"
        private const val KNOWN_EMULATOR_ANDROID_ID = "9774d56d682e549c"
        const val DEVICE_CODE_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
        private val DEVICE_CODE_REGEX = Regex("^SLD-[A-HJ-NP-Z2-9]{4}-[A-HJ-NP-Z2-9]{4}-[A-HJ-NP-Z2-9]{4}$")
        private val INVISIBLE_CHARS_REGEX = Regex("[\\u200B-\\u200F\\u202A-\\u202E\\u2060\\u2066-\\u2069\\uFEFF]")

        fun normalizeDeviceCode(raw: String): String {
            return raw
                .replace(INVISIBLE_CHARS_REGEX, "")
                .trim()
                .replace(Regex("\\s+"), "")
                .uppercase()
        }

        fun computeCheckCharacter(elevenChars: String): Char {
            require(elevenChars.length == 11) { "Device code payload must be 11 characters" }
            val modulus = DEVICE_CODE_ALPHABET.length // 32
            var factor = 2
            var sum = 0
            for (i in elevenChars.length - 1 downTo 0) {
                val codePoint = DEVICE_CODE_ALPHABET.indexOf(elevenChars[i])
                require(codePoint >= 0) { "Invalid character in device code: ${elevenChars[i]}" }
                var addend = factor * codePoint
                factor = if (factor == 2) 1 else 2
                addend = (addend / modulus) + (addend % modulus)
                sum += addend
            }
            val remainder = sum % modulus
            val checkCodePoint = (modulus - remainder) % modulus
            return DEVICE_CODE_ALPHABET[checkCodePoint]
        }

        fun isValidDeviceCode(raw: String): Boolean {
            val normalized = normalizeDeviceCode(raw)
            if (!DEVICE_CODE_REGEX.matches(normalized)) return false
            val body = normalized.removePrefix("SLD-").replace("-", "")
            if (body.length != 12) return false
            val elevenChars = body.substring(0, 11)
            val expectedCheck = runCatching { computeCheckCharacter(elevenChars) }.getOrNull() ?: return false
            return body[11] == expectedCheck
        }

        fun formatDeviceCodeFromDigest(digest: ByteArray): String {
            require(digest.size >= 7) { "Digest must be at least 7 bytes" }
            val chars = StringBuilder(11)
            var bitBuffer = 0
            var bitsInBuffer = 0
            var byteIndex = 0
            while (chars.length < 11) {
                while (bitsInBuffer < 5) {
                    val nextByte = digest[byteIndex++].toInt() and 0xFF
                    bitBuffer = (bitBuffer shl 8) or nextByte
                    bitsInBuffer += 8
                }
                val shift = bitsInBuffer - 5
                val index = (bitBuffer ushr shift) and 0x1F
                bitsInBuffer -= 5
                bitBuffer = bitBuffer and ((1 shl bitsInBuffer) - 1)
                chars.append(DEVICE_CODE_ALPHABET[index])
            }
            val eleven = chars.toString()
            val full = eleven + computeCheckCharacter(eleven)
            return "SLD-${full.substring(0, 4)}-${full.substring(4, 8)}-${full.substring(8, 12)}"
        }
    }

    private val prefs: SharedPreferences by lazy {
        val appContext = context.applicationContext ?: context
        runCatching {
            EncryptedSharedPreferences.create(
                appContext,
                PREFS_NAME,
                MasterKey.Builder(appContext).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        }.getOrElse {
            appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        }
    }

    private fun getOrCreatePersistedRsaKeyPair(): KeyPair {
        val pubB64 = prefs.getString(KEY_FALLBACK_RSA_PUBLIC, null)
        val privB64 = prefs.getString(KEY_FALLBACK_RSA_PRIVATE, null)
        if (!pubB64.isNullOrBlank() && !privB64.isNullOrBlank()) {
            val loaded = runCatching {
                val kf = KeyFactory.getInstance("RSA")
                val pub = kf.generatePublic(X509EncodedKeySpec(Base64.decode(pubB64, Base64.NO_WRAP)))
                val priv = kf.generatePrivate(PKCS8EncodedKeySpec(Base64.decode(privB64, Base64.NO_WRAP)))
                KeyPair(pub, priv)
            }.getOrNull()
            if (loaded != null) return loaded
        }

        val generator = KeyPairGenerator.getInstance("RSA")
        generator.initialize(2048)
        val pair = generator.generateKeyPair()
        prefs.edit()
            .putString(KEY_FALLBACK_RSA_PUBLIC, Base64.encodeToString(pair.public.encoded, Base64.NO_WRAP))
            .putString(KEY_FALLBACK_RSA_PRIVATE, Base64.encodeToString(pair.private.encoded, Base64.NO_WRAP))
            .commit()
        return pair
    }

    private fun publicKeyBytes(): ByteArray {
        return runCatching {
            val store = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
            val existing = store.getCertificate(ALIAS)?.publicKey?.encoded
            if (existing != null) return@runCatching existing
            val generator = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_RSA, "AndroidKeyStore")
            generator.initialize(
                KeyGenParameterSpec.Builder(ALIAS, KeyProperties.PURPOSE_SIGN or KeyProperties.PURPOSE_VERIFY)
                    .setKeySize(2048)
                    .setDigests(KeyProperties.DIGEST_SHA256)
                    .build()
            )
            generator.generateKeyPair().public.encoded
        }.getOrElse {
            getOrCreatePersistedRsaKeyPair().public.encoded
        }
    }

    fun fingerprint(): String = MessageDigest.getInstance("SHA-256").digest(publicKeyBytes())
        .joinToString("") { "%02X".format(it) }

    fun publicKeyBase64(): String = Base64.encodeToString(publicKeyBytes(), Base64.NO_WRAP)

    fun sign(message: String): String {
        val messageBytes = message.toByteArray(Charsets.UTF_8)
        return runCatching {
            val store = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
            val privateKey = store.getKey(ALIAS, null) as PrivateKey
            val signer = Signature.getInstance("SHA256withRSA")
            signer.initSign(privateKey)
            signer.update(messageBytes)
            Base64.encodeToString(signer.sign(), Base64.NO_WRAP)
        }.getOrElse {
            val fallbackPrivate = getOrCreatePersistedRsaKeyPair().private
            val signer = Signature.getInstance("SHA256withRSA")
            signer.initSign(fallbackPrivate)
            signer.update(messageBytes)
            Base64.encodeToString(signer.sign(), Base64.NO_WRAP)
        }
    }

    @SuppressLint("HardwareIds")
    private fun stableDeviceSeed(): String {
        val appContext = context.applicationContext ?: context
        val androidId = runCatching {
            Settings.Secure.getString(appContext.contentResolver, Settings.Secure.ANDROID_ID)
        }.getOrNull()?.trim()

        if (!androidId.isNullOrBlank() && !androidId.equals(KNOWN_EMULATOR_ANDROID_ID, ignoreCase = true)) {
            return "aid:$androidId"
        }

        val existing = prefs.getString(KEY_FALLBACK_SEED, null)
        if (!existing.isNullOrBlank()) {
            return "seed:$existing"
        }

        val generated = UUID.randomUUID().toString()
        prefs.edit().putString(KEY_FALLBACK_SEED, generated).commit()
        return "seed:$generated"
    }

    @Suppress("DEPRECATION")
    private fun appSigningCertificateSha256(): String {
        val appContext = context.applicationContext ?: context
        return runCatching {
            val pm = appContext.packageManager
            val pkgName = appContext.packageName
            val certBytes: ByteArray? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val info = pm.getPackageInfo(pkgName, PackageManager.GET_SIGNING_CERTIFICATES)
                val signingInfo = info.signingInfo
                val sigs = if (signingInfo?.hasMultipleSigners() == true) {
                    signingInfo.apkContentsSigners
                } else {
                    signingInfo?.signingCertificateHistory
                }
                sigs?.firstOrNull()?.toByteArray()
            } else {
                val info = pm.getPackageInfo(pkgName, PackageManager.GET_SIGNATURES)
                info.signatures?.firstOrNull()?.toByteArray()
            }
            if (certBytes == null) {
                ""
            } else {
                MessageDigest.getInstance("SHA-256").digest(certBytes).joinToString("") { "%02X".format(it) }
            }
        }.getOrDefault("")
    }

    fun deviceCode(): String {
        val appContext = context.applicationContext ?: context
        val seed = stableDeviceSeed()
        val pkg = appContext.packageName.orEmpty()
        val certSha = appSigningCertificateSha256()
        val material = "$seed|$pkg|$certSha".toByteArray(Charsets.UTF_8)
        val digest = MessageDigest.getInstance("SHA-256").digest(material)
        return formatDeviceCodeFromDigest(digest)
    }
}
