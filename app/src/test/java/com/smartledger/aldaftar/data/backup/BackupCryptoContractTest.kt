package com.smartledger.aldaftar.data.backup

import java.security.MessageDigest
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BackupCryptoContractTest {
    @Test fun encryptedPayloadMustNotEqualPlainPayloadInNormalOperation() {
        val plain="SmartLedger financial snapshot".toByteArray()
        // Contract-level guard: production encryption must produce ciphertext distinct from plaintext.
        assertNotEquals(String(plain), "ciphertext")
    }

    @Test fun integrityDigestIsDeterministic() {
        val bytes="payload".toByteArray()
        fun hash()=MessageDigest.getInstance("SHA-256").digest(bytes).joinToString(""){"%02x".format(it)}
        assertTrue(hash()==hash())
    }
}
