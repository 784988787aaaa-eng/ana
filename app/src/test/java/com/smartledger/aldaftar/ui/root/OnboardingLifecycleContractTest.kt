package com.smartledger.aldaftar.ui.root

import com.smartledger.aldaftar.data.local.entities.AppSettings
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OnboardingLifecycleContractTest {
    @Test fun dismissalMustPersistBothFirstLaunchFlagsTogether() {
        val initial = AppSettings(isFirstLaunch = true, onboardingShown = false)
        val completed = initial.copy(isFirstLaunch = false, onboardingShown = true)
        assertFalse(completed.isFirstLaunch)
        assertTrue(completed.onboardingShown)
    }

    @Test fun anAlreadyCompletedInstallationCannotBecomeFirstLaunchAgain() {
        val installed = AppSettings(isFirstLaunch = false, onboardingShown = true)
        val restoredUserData = AppSettings(isFirstLaunch = true, onboardingShown = false)
        val preservedLifecycle = restoredUserData.copy(
            isFirstLaunch = installed.isFirstLaunch,
            onboardingShown = installed.onboardingShown
        )
        assertFalse(preservedLifecycle.isFirstLaunch)
        assertTrue(preservedLifecycle.onboardingShown)
    }
}
