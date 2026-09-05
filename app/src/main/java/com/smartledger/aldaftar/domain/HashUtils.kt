/**
 * أدوات التشفير والتجزئة ومسح الذاكرة الحساسة.
 * hashString محفوظ للتوافق العكسي مع نسخ قاعدة البيانات والنسخ الاحتياطية القديمة،
 * بينما hashPassword/verifyPassword هما المسار الجديد لرموز PIN وكلمات المرور.
 */
package com.smartledger.aldaftar.domain

import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

object HashUtils {
    // هذا المسار قديم ومطلوب فقط لقراءة بيانات المستخدمين القدامى؛ لا يُستخدم لإنشاء أسرار جديدة.
    private const val LEGACY_APP_PEPPER = "SmartMakhzanSecurityGuard_2026_#!"
    private const val PASSWORD_VERSION = "v2"
    private const val PBKDF2_ITERATIONS = 210_000
    private const val PBKDF2_KEY_BITS = 256
    private const val SALT_BYTES = 16

    private val secureRandom = SecureRandom()

    /** تجزئة SHA-256 القديمة المحفوظة للتوافق مع بيانات الإصدارات السابقة فقط. */
    fun hashString(input: String, deviceSalt: String = ""): String {
        val dynamicSalt = if (deviceSalt.isNotEmpty()) deviceSalt.reversed() else "DefaultDeviceSalt2026#$"
        val bytes = (input + LEGACY_APP_PEPPER + dynamicSalt).toByteArray(Charsets.UTF_8)
        val digest = MessageDigest.getInstance("SHA-256").digest(bytes)
        return digest.toHex()
    }

    /**
     * إنشاء تجزئة جديدة مقاومة للقوة الغاشمة باستخدام PBKDF2-HMAC-SHA256 وsalt عشوائي.
     * لا تُخزن كلمة المرور نفسها ولا أي pepper ثابت داخل APK.
     */
    fun hashPassword(password: CharArray): String {
        val salt = ByteArray(SALT_BYTES).also(secureRandom::nextBytes)
        val chars = password.copyOf()
        return try {
            val spec = PBEKeySpec(chars, salt, PBKDF2_ITERATIONS, PBKDF2_KEY_BITS)
            try {
                val derived = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded
                "${PASSWORD_VERSION}\$${PBKDF2_ITERATIONS}\$${salt.toHex()}\$${derived.toHex()}"
            } finally {
                spec.clearPassword()
            }
        } finally {
            chars.fill('\u0000')
        }
    }

    /** التحقق من hash v2، مع دعم hashString القديم للترقية التدريجية دون كسر البيانات. */
    fun verifyPassword(password: CharArray, stored: String?, legacySalt: String = ""): Boolean {
        if (stored.isNullOrBlank()) return false
        if (!stored.startsWith("$PASSWORD_VERSION$")) {
            val legacy = hashString(String(password), legacySalt)
            return secureEquals(legacy, stored)
        }

        val parts = stored.split('$')
        if (parts.size != 4 || parts[0] != PASSWORD_VERSION) return false
        val iterations = parts[1].toIntOrNull() ?: return false
        val salt = parts[2].hexToBytesOrNull() ?: return false
        val expected = parts[3].hexToBytesOrNull() ?: return false
        if (iterations < 100_000 || iterations > 1_000_000 || salt.size < 16 || expected.isEmpty()) return false

        val chars = password.copyOf()
        return try {
            val spec = PBEKeySpec(chars, salt, iterations, expected.size * 8)
            try {
                val actual = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded
                MessageDigest.isEqual(actual, expected)
            } finally {
                spec.clearPassword()
            }
        } finally {
            chars.fill('\u0000')
        }
    }

    /** مقارنة ثابتة الزمن على مستوى البايتات دون early-exit للمحتوى. */
    fun secureEquals(a: String?, b: String?): Boolean {
        if (a == null || b == null) return false
        return MessageDigest.isEqual(a.toByteArray(Charsets.UTF_8), b.toByteArray(Charsets.UTF_8))
    }

    fun wipeCharArray(array: CharArray) { array.fill('\u0000') }
    fun wipeByteArray(array: ByteArray) { array.fill(0) }

    private fun ByteArray.toHex(): String = buildString(size * 2) {
        for (byte in this@toHex) append("%02x".format(byte.toInt() and 0xff))
    }

    private fun String.hexToBytesOrNull(): ByteArray? {
        if (length == 0 || length % 2 != 0) return null
        return try {
            ByteArray(length / 2) { index -> substring(index * 2, index * 2 + 2).toInt(16).toByte() }
        } catch (_: NumberFormatException) {
            null
        }
    }
}
