package com.smartledger.aldaftar.domain

import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/** اشتقاق وحفظ بصمة رمز PIN دون أسرار ثابتة داخل التطبيق. */
object HashUtils {
    private const val ALGORITHM = "PBKDF2WithHmacSHA256"
    private const val ITERATIONS = 210_000
    private const val SALT_SIZE = 16
    private const val KEY_SIZE_BITS = 256
    private const val FORMAT_VERSION = "pbkdf2-v1"
    private val HEX_CHARS = "0123456789abcdef".toCharArray()
    private val random = SecureRandom()

    private val keyFactoryThreadLocal = object : ThreadLocal<SecretKeyFactory>() {
        override fun initialValue(): SecretKeyFactory = SecretKeyFactory.getInstance(ALGORITHM)
    }

    /**
     * Pre-warms the JCA SecretKeyFactory and SecureRandom off the UI thread
     * so the first PIN entry or activation does not pay provider initialization latency.
     */
    fun warmup() {
        runCatching {
            keyFactoryThreadLocal.get()
            val seed = ByteArray(1)
            random.nextBytes(seed)
            seed.fill(0)
        }
    }

    fun hashString(input: String): String = createPinHash(input)

    fun createPinHash(input: String): String {
        val salt = ByteArray(SALT_SIZE).also(random::nextBytes)
        val key = derive(input.toCharArray(), salt)
        return try {
            "$FORMAT_VERSION:$ITERATIONS:${salt.toHex()}:${key.toHex()}"
        } finally {
            salt.fill(0)
            key.fill(0)
        }
    }

    fun verifyPin(input: String, encoded: String?): Boolean {
        if (encoded.isNullOrBlank()) return false
        val parts = encoded.split(':')
        if (parts.size != 4 || parts[0] != FORMAT_VERSION) return false
        val iterations = parts[1].toIntOrNull() ?: return false
        if (iterations <= 0) return false
        val salt = parts[2].hexToBytes() ?: return false
        val expected = parts[3].hexToBytes() ?: return false
        val actual = derive(input.toCharArray(), salt, iterations)
        return try {
            MessageDigest.isEqual(actual, expected)
        } finally {
            salt.fill(0)
            expected.fill(0)
            actual.fill(0)
        }
    }

    fun secureEquals(a: String?, b: String?): Boolean =
        a != null && b != null && MessageDigest.isEqual(a.toByteArray(), b.toByteArray())

    fun wipeCharArray(array: CharArray) = array.fill('\u0000')
    fun wipeByteArray(array: ByteArray) = array.fill(0)

    private fun derive(chars: CharArray, salt: ByteArray, iterations: Int = ITERATIONS): ByteArray {
        val spec = PBEKeySpec(chars, salt, iterations, KEY_SIZE_BITS)
        return try {
            val factory = keyFactoryThreadLocal.get() ?: SecretKeyFactory.getInstance(ALGORITHM)
            factory.generateSecret(spec).encoded
        } finally {
            spec.clearPassword()
            chars.fill('\u0000')
        }
    }

    private fun ByteArray.toHex(): String {
        val out = CharArray(size * 2)
        for (i in indices) {
            val v = this[i].toInt() and 0xFF
            out[i * 2] = HEX_CHARS[v ushr 4]
            out[i * 2 + 1] = HEX_CHARS[v and 0x0F]
        }
        return String(out)
    }

    private fun String.hexToBytes(): ByteArray? {
        if (length % 2 != 0) return null
        val out = ByteArray(length / 2)
        var i = 0
        while (i < length) {
            val hi = Character.digit(this[i], 16)
            val lo = Character.digit(this[i + 1], 16)
            if (hi == -1 || lo == -1) return null
            out[i ushr 1] = ((hi shl 4) or lo).toByte()
            i += 2
        }
        return out
    }
}

