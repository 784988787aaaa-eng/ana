package com.smartledger.aldaftar.ui.security

import com.smartledger.aldaftar.domain.HashUtils
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SecurityPerformanceAndLockContractTest {

    private fun findSourceFile(relative: String): File {
        val path1 = File("src/main/java/com/smartledger/aldaftar/$relative")
        val path2 = File("app/src/main/java/com/smartledger/aldaftar/$relative")
        return when {
            path1.exists() -> path1
            path2.exists() -> path2
            else -> error("Source file not found: $relative")
        }
    }

    @Test
    fun testAppSecurityManagerProvidesIOStorageWarmup() {
        val fileContent = findSourceFile("platform/security/AppSecurityManager.kt").readText()
        assertTrue("AppSecurityManager must offer warmupStorage() for IO dispatcher", fileContent.contains("suspend fun warmupStorage()"))
        assertTrue("warmupStorage must run on Dispatchers.IO", fileContent.contains("withContext(Dispatchers.IO)"))
    }

    @Test
    fun testSecurityViewModelPrewarmsStorageAndOffloadsPreferencesSave() {
        val fileContent = findSourceFile("ui/viewmodel/SecurityViewModel.kt").readText()
        assertTrue("SecurityViewModel must pre-warm storage in init on IO", fileContent.contains("securityManager.warmupStorage()"))
        assertTrue("saveSettingsSync must run on Dispatchers.IO", fileContent.contains("withContext(Dispatchers.IO)"))
    }

    @Test
    fun testPasscodeKeypadUsesXmlStringResourceForLockout() {
        val keypadContent = findSourceFile("ui/screens/security/lock/PasscodeKeypadContent.kt").readText()
        assertTrue("Lockout prompt must use stringResource lock_lockout_active", keypadContent.contains("lock_lockout_active"))
        assertFalse("Lockout prompt must not contain hardcoded Arabic string", keypadContent.contains("\"تم قفل المحاولات مؤقتاً"))
    }

    @Test
    fun testPbkdf2HashInvariantsMaintained() {
        val hashContent = findSourceFile("domain/HashUtils.kt").readText()
        assertTrue("PBKDF2 must maintain 210,000 iterations for security", hashContent.contains("210_000") || hashContent.contains("210000"))
        assertTrue("PBKDF2 algorithm must remain PBKDF2WithHmacSHA256", hashContent.contains("PBKDF2WithHmacSHA256"))

        val pin = "1234"
        val hashed = HashUtils.hashString(pin)
        assertTrue("HashUtils verifyPin must succeed for correct PIN", HashUtils.verifyPin(pin, hashed))
        assertFalse("HashUtils verifyPin must fail for wrong PIN", HashUtils.verifyPin("9999", hashed))
    }
}
