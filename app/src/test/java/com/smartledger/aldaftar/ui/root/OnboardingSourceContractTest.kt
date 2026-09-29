package com.smartledger.aldaftar.ui.root

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class OnboardingSourceContractTest {
    private fun source(path: String): String = File("src/main/java/$path").readText()

    @Test fun dismissalUsesSingleCompletionOperation() {
        val src = source("com/smartledger/aldaftar/ui/root/SmartLedgerApp.kt")
        assertTrue(src.contains("financeViewModel.completeOnboarding()"))
        assertTrue(!src.contains("financeViewModel.markOnboardingShown()"))
    }

    @Test fun restorePreservesInstallationLocalOnboardingFlags() {
        val src = source("com/smartledger/aldaftar/data/backup/BackupEngine.kt")
        assertTrue(src.contains("isFirstLaunch = currentSettings.isFirstLaunch"))
        assertTrue(src.contains("onboardingShown = currentSettings.onboardingShown"))
    }
}
