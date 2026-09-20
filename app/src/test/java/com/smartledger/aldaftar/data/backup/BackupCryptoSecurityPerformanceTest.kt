package com.smartledger.aldaftar.data.backup

import androidx.test.core.app.ApplicationProvider
import java.util.concurrent.TimeUnit
import kotlin.system.measureNanoTime
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class BackupCryptoSecurityPerformanceTest {
    private val context = ApplicationProvider.getApplicationContext<android.content.Context>()

    @Test
    fun recoveryCodeIsStableAndEncryptionUsesFreshParameters() {
        val crypto = BackupCrypto(context)
        val first = crypto.localRecoveryCode()
        val second = crypto.localRecoveryCode()

        assertTrue(first.length >= 24)
        assertTrue(crypto.isLocalRecoveryCode(first))
        assertTrue(first == second)

        val payload = "SMARTLEDGER-backup-test".toByteArray()
        val encryptedA = crypto.encrypt(payload)
        val encryptedB = crypto.encrypt(payload)

        assertNotEquals(encryptedA.first.contentToString(), encryptedB.first.contentToString())
        assertNotEquals(encryptedA.second.contentToString(), encryptedB.second.contentToString())

        val restored = crypto.decrypt(encryptedA.first, encryptedA.second, encryptedA.third, first)
        assertArrayEquals(payload, restored)
    }

    @Test
    fun wrongRecoveryCodeAndTamperedCiphertextAreRejected() {
        val crypto = BackupCrypto(context)
        val recovery = crypto.localRecoveryCode()
        val encrypted = crypto.encrypt("confidential".toByteArray())

        runCatching {
            crypto.decrypt(
                encrypted.first,
                encrypted.second,
                encrypted.third,
                recovery.dropLast(1) + if (recovery.last() == 'A') 'B' else 'A'
            )
        }.onSuccess {
            throw AssertionError("A wrong recovery code must never decrypt the backup")
        }

        val tampered = encrypted.third.copyOf().also { it[it.lastIndex] = (it.last().toInt() xor 1).toByte() }
        runCatching {
            crypto.decrypt(encrypted.first, encrypted.second, tampered, recovery)
        }.onSuccess {
            throw AssertionError("Tampered ciphertext must never decrypt successfully")
        }
    }

    @Test
    fun encryptionAndDecryptionStayWithinGenerousCiBudget() {
        val crypto = BackupCrypto(context)
        val recovery = crypto.localRecoveryCode()
        val payload = ByteArray(64 * 1024) { (it and 0xFF).toByte() }

        val elapsedNanos = measureNanoTime {
            repeat(2) {
                val encrypted = crypto.encrypt(payload)
                val restored = crypto.decrypt(encrypted.first, encrypted.second, encrypted.third, recovery)
                assertArrayEquals(payload, restored)
            }
        }

        val elapsedMs = TimeUnit.NANOSECONDS.toMillis(elapsedNanos)
        println("BackupCrypto 2x 64KiB encrypt/decrypt elapsedMs=$elapsedMs")
        assertTrue(
            "Backup encryption/decryption exceeded the CI safety budget: ${elapsedMs}ms",
            elapsedMs < 10_000
        )
    }
}
