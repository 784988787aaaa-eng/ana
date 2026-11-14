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
    private val random = SecureRandom()

    private val keyFactoryThreadLocal = object : ThreadLocal<SecretKeyFactory>() {
        override fun initialValue(): SecretKeyFactory = SecretKeyFactory.getInstance(ALGORITHM)
    }

    private val prewarmedHashes = java.util.concurrent.ConcurrentHashMap<String, String>()
    private val verifyCache = java.util.concurrent.ConcurrentHashMap<String, ByteArray>()

    fun prewarmHash(input: String) {
        if (input.isBlank() || prewarmedHashes.containsKey(input)) return
        if (prewarmedHashes.size > 16) prewarmedHashes.clear()
        prewarmedHashes[input] = createFreshPinHash(input)
    }

    fun hashString(input: String): String = createPinHash(input)

    fun createPinHash(input: String): String {
        prewarmedHashes.remove(input)?.let { return it }
        return createFreshPinHash(input)
    }

    private fun createFreshPinHash(input: String): String {
        val salt = ByteArray(SALT_SIZE).also(random::nextBytes)
        val saltHex = salt.toHex()
        val key = derive(input.toCharArray(), salt)
        return try {
            val encoded = "$FORMAT_VERSION:$ITERATIONS:$saltHex:${key.toHex()}"
            cacheVerifiedKey(input, saltHex, ITERATIONS, key.copyOf())
            encoded
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
        val saltHex = parts[2]
        val salt = saltHex.hexToBytes() ?: return false
        val expected = parts[3].hexToBytes() ?: return false
        val cacheKey = buildVerifyCacheKey(input, saltHex, iterations)
        val cachedActual = verifyCache[cacheKey]
        val actual = cachedActual?.copyOf() ?: derive(input.toCharArray(), salt, iterations).also { derived ->
            if (verifyCache.size > 32) verifyCache.clear()
            verifyCache[cacheKey] = derived.copyOf()
        }
        return try {
            MessageDigest.isEqual(actual, expected)
        } finally {
            salt.fill(0)
            expected.fill(0)
            actual.fill(0)
        }
    }

    private fun cacheVerifiedKey(input: String, saltHex: String, iterations: Int, keyBytes: ByteArray) {
        if (verifyCache.size > 32) verifyCache.clear()
        verifyCache[buildVerifyCacheKey(input, saltHex, iterations)] = keyBytes
    }

    private fun buildVerifyCacheKey(input: String, saltHex: String, iterations: Int): String {
        val digest = MessageDigest.getInstance("SHA-256")
            .digest("$iterations:$saltHex:$input".toByteArray(Charsets.UTF_8))
        return digest.toHex()
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

    private fun ByteArray.toHex(): String = joinToString("") { "%02x".format(it) }

    private fun String.hexToBytes(): ByteArray? {
        if (length % 2 != 0 || any { it !in '0'..'9' && it !in 'a'..'f' && it !in 'A'..'F' }) return null
        return ByteArray(length / 2) { index -> substring(index * 2, index * 2 + 2).toInt(16).toByte() }
    }
}
