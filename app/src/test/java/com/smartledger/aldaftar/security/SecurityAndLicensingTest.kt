package com.smartledger.aldaftar.security

import com.smartledger.aldaftar.data.cloud.CloudNetworkEngine
import com.smartledger.aldaftar.domain.DatabaseSecurityGuard
import com.smartledger.aldaftar.domain.HashUtils
import com.smartledger.aldaftar.domain.LicenseCheckResult
import com.smartledger.aldaftar.domain.LicenseState
import okhttp3.Request
import org.junit.Assert.*
import org.junit.Test
import java.io.IOException

/**
 * اختبارات أمان Phase 3 — Security & Licensing.
 */
class SecurityAndLicensingTest {

    @Test
    fun testSecureEqualsConstantTime() {
        val hash1 = HashUtils.hashString("1234")
        val hash2 = HashUtils.hashString("1234")
        val hash3 = HashUtils.hashString("5678")

        assertTrue(HashUtils.secureEquals(hash1, hash2))
        assertFalse(HashUtils.secureEquals(hash1, hash3))
        assertFalse(HashUtils.secureEquals(hash1, null))
        assertFalse(HashUtils.secureEquals(null, hash2))
    }

    @Test
    fun testDatabaseSecurityGuardConstantTime() {
        val hash1 = HashUtils.hashString("secret_passcode")
        val hash2 = HashUtils.hashString("secret_passcode")
        val hash3 = HashUtils.hashString("wrong_passcode")

        assertTrue(DatabaseSecurityGuard.secureEqual(hash1, hash2))
        assertFalse(DatabaseSecurityGuard.secureEqual(hash1, hash3))
        assertFalse(DatabaseSecurityGuard.secureEqual(null, hash1))
    }

    @Test
    fun testMemoryScrubbingWipeCharArray() {
        val sensitiveChars = "TopSecret123!".toCharArray()
        assertEquals('T', sensitiveChars[0])
        HashUtils.wipeCharArray(sensitiveChars)
        for (c in sensitiveChars) {
            assertEquals('\u0000', c)
        }
    }

    @Test
    fun testMemoryScrubbingWipeByteArray() {
        val sensitiveBytes = byteArrayOf(1, 2, 3, 4, 5)
        assertEquals(1.toByte(), sensitiveBytes[0])
        HashUtils.wipeByteArray(sensitiveBytes)
        for (b in sensitiveBytes) {
            assertEquals(0.toByte(), b)
        }
    }

    @Test
    fun testTokenRedaction() {
        val sensitiveToken = "ya29.a0AfH6SMB_sample_bearer_token_12345"
        val redacted = CloudNetworkEngine.redactSensitiveString(sensitiveToken)
        assertEquals("[REDACTED]", redacted)
        assertEquals("", CloudNetworkEngine.redactSensitiveString(null))
        assertEquals("", CloudNetworkEngine.redactSensitiveString(""))
    }

    @Test
    fun testRetryStatusCodeClassification() {
        // Retryable
        assertTrue(CloudNetworkEngine.isRetryableStatusCode(429))
        assertTrue(CloudNetworkEngine.isRetryableStatusCode(500))
        assertTrue(CloudNetworkEngine.isRetryableStatusCode(503))

        // Non-Retryable
        assertFalse(CloudNetworkEngine.isRetryableStatusCode(200))
        assertFalse(CloudNetworkEngine.isRetryableStatusCode(400))
        assertFalse(CloudNetworkEngine.isRetryableStatusCode(401))
        assertFalse(CloudNetworkEngine.isRetryableStatusCode(403))
        assertFalse(CloudNetworkEngine.isRetryableStatusCode(404))
    }

    @Test
    fun testLicenseStateHierarchy() {
        val validState: LicenseState = LicenseState.Valid("user@example.com", "device_xyz_123")
        assertTrue(validState is LicenseState.Valid)
        assertEquals("user@example.com", (validState as LicenseState.Valid).email)

        val outageState: LicenseState = LicenseState.NetworkUnavailable("user@example.com", true)
        assertTrue(outageState is LicenseState.NetworkUnavailable)
        assertTrue((outageState as LicenseState.NetworkUnavailable).fallbackValid)

        val revokedState: LicenseState = LicenseState.Revoked("Admin disabled account")
        assertTrue(revokedState is LicenseState.Revoked)
    }

    @Test
    fun testLicenseCheckResultOutageSafety() {
        val networkOutage = LicenseCheckResult.NetworkOutage("No internet connection")
        assertTrue(networkOutage is LicenseCheckResult.NetworkOutage)
        assertEquals("No internet connection", networkOutage.message)
    }
}
