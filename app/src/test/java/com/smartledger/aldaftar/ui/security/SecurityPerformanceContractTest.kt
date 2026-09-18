package com.smartledger.aldaftar.ui.security

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SecurityPerformanceContractTest {
    private val root = File("src/main/java/com/smartledger/aldaftar")

    @Test
    fun lockScreenDoesNotUseArtificialWaitBeforeInteraction() {
        val file = File(root, "ui/screens/AppLockScreen.kt").readText()
        assertFalse("The lock screen must not add a fixed 200ms wait before biometric prompt", file.contains("delay(200)"))
        assertFalse("Security UI must never block the UI thread", file.contains("Thread.sleep"))
    }

    @Test
    fun lockKeypadUsesImmediateVisualFeedbackAndNoHeavySpringHeaderAnimation() {
        val file = File(root, "ui/screens/security/lock/PasscodeKeypadContent.kt").readText()
        assertFalse("The lock header must not use a heavy bouncy spring on every digit", file.contains("DampingRatioMediumBouncy"))
        val buttons = File(root, "ui/screens/security/lock/LockKeypadViews.kt").readText()
        assertTrue("Keypad buttons need immediate pressed-state feedback", buttons.contains("collectIsPressedAsState"))
        assertTrue("Keypad buttons need a visible touch indication", buttons.contains("ripple(bounded = true)"))
    }

    @Test
    fun lockInputHasTactileFeedbackPath() {
        val helper = File(root, "ui/screens/security/lock/LockHapticHelper.kt").readText()
        val lock = File(root, "ui/screens/AppLockScreen.kt").readText()
        assertTrue(helper.contains("LockHapticType.KEYPRESS"))
        assertTrue(helper.contains("EFFECT_TICK"))
        assertTrue(lock.contains("performLockHaptic(vibrator, LockHapticType.KEYPRESS)"))
    }

    @Test
    fun securitySaveKeepsStrongHashingOffUiThreadAndDoesNotDoubleToggleEditMode() {
        val screen = File(root, "ui/screens/SecurityScreen.kt").readText()
        assertTrue("PIN/recovery hashing must remain off the UI thread", screen.contains("async(Dispatchers.Default)"))
        assertFalse(screen.contains("isEditingPasscode = true\n                        isEditingPasscode = false"))
    }

    @Test
    fun securityActionsDoNotFallBackToOpaqueWhiteButtons() {
        val active = File(root, "ui/screens/security/components/SecurityActivePanel.kt").readText()
        val setup = File(root, "ui/screens/security/components/SecuritySetupForm.kt").readText()
        assertTrue(active.contains("containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.28f)"))
        assertTrue(setup.contains("disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.70f)"))
    }
}
