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

    @Test fun habayebListMustDecoupleLoadingFromEmptyState() {
        val src = source("com/smartledger/aldaftar/ui/screens/habayeb/components/HabayebListSection.kt")
        assertTrue(src.contains("if (!isInitialized || isLoading)"))
        assertTrue(src.contains("else if (filteredCustomers.isEmpty())"))
    }

    @Test fun financeViewModelMustAwaitCustomersOnStartup() {
        val src = source("com/smartledger/aldaftar/ui/viewmodel/FinanceViewModel.kt")
        assertTrue(src.contains("habayebRepository.customersFlow"))
        assertTrue(src.contains("_isSettingsLoaded.value = true"))
    }

    @Test fun habayebUiStateMustTrackInitializationAndLoading() {
        val src = source("com/smartledger/aldaftar/ui/viewmodel/HabayebFinanceViewModel.kt")
        assertTrue(src.contains("isInitialized: Boolean = false"))
        assertTrue(src.contains("isLoading: Boolean = false"))
        assertTrue(src.contains("CustomersUiState(isLoading = true, isInitialized = false)"))
    }
}
