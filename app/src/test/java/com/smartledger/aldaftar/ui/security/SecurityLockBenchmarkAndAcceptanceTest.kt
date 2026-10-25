package com.smartledger.aldaftar.ui.security

import android.app.Application
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.smartledger.aldaftar.data.local.AppDatabase
import com.smartledger.aldaftar.data.local.entities.AppSettings
import com.smartledger.aldaftar.data.repository.SettingsRepository
import com.smartledger.aldaftar.domain.HashUtils
import com.smartledger.aldaftar.platform.security.AppSecurityManager
import com.smartledger.aldaftar.platform.security.BiometricAuthHelper
import com.smartledger.aldaftar.ui.viewmodel.SecurityViewModel
import java.io.File
import kotlin.system.measureNanoTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class SecurityLockBenchmarkAndAcceptanceTest {

    private lateinit var app: Application
    private lateinit var db: AppDatabase
    private lateinit var securityManager: AppSecurityManager
    private lateinit var settingsRepository: SettingsRepository
    private lateinit var viewModel: SecurityViewModel

    private fun findSourceFile(relative: String): File {
        val path1 = File("src/main/java/com/smartledger/aldaftar/$relative")
        val path2 = File("app/src/main/java/com/smartledger/aldaftar/$relative")
        return when {
            path1.exists() -> path1
            path2.exists() -> path2
            else -> error("Source file not found: $relative")
        }
    }

    @Before
    fun setUp() {
        app = ApplicationProvider.getApplicationContext()
        securityManager = AppSecurityManager.getInstance(app)
        runBlocking {
            securityManager.warmupStorage()
        }
        securityManager.resetFailedAttempts()

        db = Room.inMemoryDatabaseBuilder(app, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()

        val dao = db.settingsDao()
        runBlocking {
            dao.insertOrUpdateSettings(AppSettings(id = 1))
        }

        settingsRepository = SettingsRepository(dao)
        viewModel = SecurityViewModel(app, settingsRepository)
    }

    @After
    fun tearDown() {
        BiometricAuthHelper.cancelActiveAuthentication()
        securityManager.resetFailedAttempts()
        db.close()
    }

    @Test
    fun benchmarkAndVerifyPinActivationAndVerificationStages() = runBlocking {
        // Stage 0: Warmup JCA PBKDF2 engine
        val warmupMs = measureNanoTime {
            HashUtils.warmup()
        } / 1_000_000.0

        // Stage 1: Parallel PIN + Recovery Phrase Hashing on Dispatchers.Default (Activation flow)
        var pinHash = ""
        var recoveryHash = ""
        val parallelActivationHashMs = measureNanoTime {
            coroutineScope {
                val pinDeferred = async(Dispatchers.Default) { HashUtils.hashString("2580") }
                val recDeferred = async(Dispatchers.Default) { HashUtils.hashString("مفتاح الاسترداد") }
                pinHash = pinDeferred.await()
                recoveryHash = recDeferred.await()
            }
        } / 1_000_000.0

        // Stage 2: Persistence to Room + Non-blocking SecurityManager cache update
        val persistActivationMs = measureNanoTime {
            val activated = AppSettings(
                id = 1,
                isPasscodeEnabled = true,
                passcodeHash = pinHash,
                recoveryPhraseHash = recoveryHash,
                recoveryHint = "تلميح سري"
            )
            viewModel.toggleBiometric(true)
            viewModel.saveSettingsSync(activated)
        } / 1_000_000.0

        // Stage 3: Single-derivation valid PIN verification off Main Thread
        var validResult = false
        val validPinVerifyMs = measureNanoTime {
            validResult = viewModel.verifyPinOffMainThread("2580")
        } / 1_000_000.0
        assertTrue("Valid PIN must verify true", validResult)

        // Stage 4: Single-derivation invalid PIN verification off Main Thread (no second PBKDF2 on recoveryPhrase!)
        var invalidResult = true
        val invalidPinVerifyMs = measureNanoTime {
            invalidResult = viewModel.verifyPinOffMainThread("9999")
        } / 1_000_000.0
        assertFalse("Invalid PIN must verify false", invalidResult)

        // Stage 5: Compare with legacy double-derivation on invalid PIN (PIN + Recovery Phrase)
        var legacyInvalidResult = true
        val legacyDoubleDerivationMs = measureNanoTime {
            legacyInvalidResult = HashUtils.verifyPin("9999", pinHash) || HashUtils.verifyPin("9999", recoveryHash)
        } / 1_000_000.0
        assertFalse(legacyInvalidResult)

        // Stage 6: Atomic in-memory failed-attempt & lockout read/write latency
        val atomicStateOpsMs = measureNanoTime {
            viewModel.handleFailedAttempt()
            val attempts = viewModel.failedAttemptsState.value
            val remaining = viewModel.getLockoutTimeRemainingMs()
            assertEquals(1, attempts)
            assertEquals(0L, remaining)
            viewModel.resetFailedAttempts()
        } / 1_000_000.0

        println(
            "SECURITY_BENCHMARK_RESULTS: " +
                "warmupMs=${"%.2f".format(warmupMs)}, " +
                "parallelActivationHashMs=${"%.2f".format(parallelActivationHashMs)}, " +
                "persistActivationMs=${"%.2f".format(persistActivationMs)}, " +
                "validPinVerifyMs=${"%.2f".format(validPinVerifyMs)}, " +
                "invalidPinVerifyMs=${"%.2f".format(invalidPinVerifyMs)}, " +
                "legacyDoubleDerivationMs=${"%.2f".format(legacyDoubleDerivationMs)}, " +
                "atomicStateOpsMs=${"%.4f".format(atomicStateOpsMs)}"
        )

        // Assert that atomic state operations are sub-millisecond (< 5ms)
        assertTrue("Atomic state operations must be non-blocking (< 5ms)", atomicStateOpsMs < 5.0)
        // Assert single-derivation invalid PIN check is faster than double derivation
        assertTrue("Single derivation must be faster than double derivation", invalidPinVerifyMs < legacyDoubleDerivationMs * 0.95)
    }

    @Test
    fun scenario1_activateChangeAndDisablePasscodeFlow() = runBlocking {
        val initialPinHash = HashUtils.hashString("1234")
        val initialRecoveryHash = HashUtils.hashString("كلمة استرداد")
        val enabledSettings = AppSettings(
            id = 1,
            isPasscodeEnabled = true,
            passcodeHash = initialPinHash,
            recoveryPhraseHash = initialRecoveryHash,
            recoveryHint = "تلميح ١"
        )
        viewModel.toggleBiometric(false)
        viewModel.saveSettingsSync(enabledSettings)

        // Verify initial PIN & recovery phrase
        assertTrue(viewModel.verifyPinOffMainThread("1234"))
        assertFalse(viewModel.verifyPinOffMainThread("0000"))
        assertTrue(viewModel.verifyRecoveryPhraseOffMainThread("كلمة استرداد"))
        assertFalse(viewModel.verifyRecoveryPhraseOffMainThread("خاطئة"))

        // Change PIN to 5678
        val updatedPinHash = HashUtils.hashString("5678")
        val updatedSettings = enabledSettings.copy(
            passcodeHash = updatedPinHash
        )
        viewModel.toggleBiometric(true)
        viewModel.saveSettingsSync(updatedSettings)

        assertFalse("Old PIN must no longer match", viewModel.verifyPinOffMainThread("1234"))
        assertTrue("New PIN must match", viewModel.verifyPinOffMainThread("5678"))
        assertTrue(securityManager.isBiometricEnabled())

        // Disable passcode via Recovery Phrase flow
        viewModel.handleFailedAttempt()
        assertEquals(1, securityManager.getFailedAttempts())
        viewModel.disablePasscodeAfterRecovery(updatedSettings)

        val afterDisable = settingsRepository.getSettingsDirect()
        assertNotNull(afterDisable)
        assertFalse("Passcode must be disabled after recovery", afterDisable!!.isPasscodeEnabled)
        assertNull("Passcode hash must be cleared", afterDisable.passcodeHash)
        assertEquals("Failed attempts must be reset to 0", 0, securityManager.getFailedAttempts())
        assertFalse("Biometric flag must be disabled when passcode is disabled", securityManager.isBiometricEnabled())
    }

    @Test
    fun scenario2_incompleteAndCorruptedInputsRejectedSafely() = runBlocking {
        val validHash = HashUtils.hashString("4321")
        viewModel.saveSettingsSync(
            AppSettings(
                id = 1,
                isPasscodeEnabled = true,
                passcodeHash = validHash,
                recoveryPhraseHash = HashUtils.hashString("استرداد آمن")
            )
        )

        // Incomplete inputs must return false immediately without throwing
        assertFalse(viewModel.verifyPinOffMainThread(""))
        assertFalse(viewModel.verifyPinOffMainThread("   "))
        assertFalse(viewModel.verifyPinOffMainThread("4"))
        assertFalse(viewModel.verifyPinOffMainThread("43"))
        assertFalse(viewModel.verifyPinOffMainThread("432"))
        assertFalse(viewModel.verifyRecoveryPhraseOffMainThread(""))
        assertFalse(viewModel.verifyRecoveryPhraseOffMainThread("   "))

        // Corrupted / malformed stored hashes must never crash HashUtils.verifyPin
        assertFalse(HashUtils.verifyPin("4321", ""))
        assertFalse(HashUtils.verifyPin("4321", "pbkdf2-v1:invalid"))
        assertFalse(HashUtils.verifyPin("4321", "pbkdf2-v1:210000:zzzz:zzzz"))
        assertFalse(HashUtils.verifyPin("4321", "corrupted-random-string"))
    }

    @Test
    fun scenario3_reactiveFailedAttemptsAndLockoutStateFlowsUpdateImmediately() {
        assertEquals(0, viewModel.failedAttemptsState.value)
        assertEquals(0L, viewModel.lockoutUntilState.value)

        // Attempts 1 to 4: counter increments immediately in StateFlow, lockout remains 0
        for (expected in 1..4) {
            viewModel.handleFailedAttempt()
            assertEquals(expected, viewModel.failedAttemptsState.value)
            assertEquals(0L, viewModel.getLockoutTimeRemainingMs())
        }

        // Attempt 5: 30s lockout reflected immediately in StateFlow
        viewModel.handleFailedAttempt()
        assertEquals(5, viewModel.failedAttemptsState.value)
        assertTrue(viewModel.getLockoutTimeRemainingMs() in 25_000L..30_000L)

        // Reset clears both StateFlows immediately
        viewModel.resetFailedAttempts()
        assertEquals(0, viewModel.failedAttemptsState.value)
        assertEquals(0L, viewModel.lockoutUntilState.value)
        assertEquals(0L, viewModel.getLockoutTimeRemainingMs())
    }

    @Test
    fun scenario4_biometricHelperGuardAndLifecycleContracts() {
        assertFalse(BiometricAuthHelper.isAuthenticationInProgress())
        BiometricAuthHelper.cancelActiveAuthentication()
        assertFalse(BiometricAuthHelper.isAuthenticationInProgress())

        // Verify SmartLedgerApp lifecycle & in-session PIN activation fix
        val rootAppCode = findSourceFile("ui/root/SmartLedgerApp.kt").readText()
        assertFalse(
            "SmartLedgerApp must not reset isUnlocked when settings.isPasscodeEnabled changes in-session",
            rootAppCode.contains("rememberSaveable(settingsLoaded, settings.isPasscodeEnabled)")
        )
        assertTrue(
            "SmartLedgerApp must lock on Lifecycle.Event.ON_STOP when backgrounded",
            rootAppCode.contains("Lifecycle.Event.ON_STOP")
        )
        assertTrue(
            "SmartLedgerApp must cancel active biometric prompts on background",
            rootAppCode.contains("BiometricAuthHelper.cancelActiveAuthentication()")
        )

        // Verify FinanceViewModel reads directly from settingsRepository.settingsFlow instead of settingsState default
        val financeVmCode = findSourceFile("ui/viewmodel/FinanceViewModel.kt").readText()
        assertTrue(
            "FinanceViewModel must read directly from settingsRepository.settingsFlow in init",
            financeVmCode.contains("settingsRepository.settingsFlow,")
        )

        // Verify VerifyOldPinDialog uses off-main-thread verification
        val activePanelCode = findSourceFile("ui/screens/security/components/SecurityActivePanel.kt").readText()
        assertTrue(
            "SecurityActivePanel must use verifyPinOffMainThread",
            activePanelCode.contains("verifyPinOffMainThread")
        )
        assertTrue(
            "SecurityActivePanel must use verifyRecoveryPhraseOffMainThread",
            activePanelCode.contains("verifyRecoveryPhraseOffMainThread")
        )
    }
}
