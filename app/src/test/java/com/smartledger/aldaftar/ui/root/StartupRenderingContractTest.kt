package com.smartledger.aldaftar.ui.root

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class StartupRenderingContractTest {
    private fun source(path: String): String = File("src/main/java/$path").readText()
    @Test fun splashMustStayUntilSettingsAreLoaded() {
        val src = source("com/smartledger/aldaftar/MainActivity.kt")
        assertTrue(src.contains("installSplashScreen()"))
        assertTrue(src.contains("setKeepOnScreenCondition { !financeViewModel.isSettingsLoaded.value }"))
    }
    @Test fun rootMustNotRenderFirstRunUiBeforeSettingsLoad() {
        val src = source("com/smartledger/aldaftar/ui/root/SmartLedgerApp.kt")
        assertTrue(src.contains("settingsLoaded && settings.isFirstLaunch && !financeViewModel.hasShownOnboarding()"))
    }
}
