package com.smartledger.aldaftar.ui.root

import android.widget.Toast
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.ViewModelProvider
import com.smartledger.aldaftar.R
import com.smartledger.aldaftar.ui.components.WelcomeOnboardingDialog
import com.smartledger.aldaftar.ui.main.MainAppLayout
import com.smartledger.aldaftar.ui.screens.AppLockScreen
import com.smartledger.aldaftar.ui.theme.AppTheme
import com.smartledger.aldaftar.ui.viewmodel.BackupSyncViewModel
import com.smartledger.aldaftar.ui.viewmodel.FinanceViewModel
import com.smartledger.aldaftar.ui.viewmodel.HabayebFinanceViewModel
import com.smartledger.aldaftar.ui.viewmodel.SecurityViewModel
import com.smartledger.aldaftar.ui.viewmodel.UiEvent

@androidx.compose.runtime.Composable
fun SmartLedgerApp(
    financeViewModel: FinanceViewModel,
    viewModelFactory: ViewModelProvider.Factory,
    onExit: () -> Unit
) {
    val context = LocalContext.current
    val securityViewModel: SecurityViewModel = viewModel(factory = viewModelFactory)
    val habayebViewModel: HabayebFinanceViewModel = viewModel(factory = viewModelFactory)
    val backupSyncViewModel: BackupSyncViewModel = viewModel(factory = viewModelFactory)
    val businessProfileViewModel: com.smartledger.aldaftar.ui.viewmodel.BusinessProfileViewModel = viewModel(factory = viewModelFactory)

    val settings by financeViewModel.settingsState.collectAsStateWithLifecycle()
    val settingsLoaded by financeViewModel.isSettingsLoaded.collectAsStateWithLifecycle()
    val themeMode by financeViewModel.themeModeState.collectAsStateWithLifecycle()
    val systemDark = isSystemInDarkTheme()
    val darkTheme = when (themeMode) {
        1 -> false
        2 -> true
        else -> systemDark
    }

    var isUnlocked by rememberSaveable(settings.isPasscodeEnabled) {
        mutableStateOf(!settings.isPasscodeEnabled)
    }
    var showOnboarding by rememberSaveable { mutableStateOf(false) }
    val firstLaunch = settingsLoaded && settings.isFirstLaunch && !financeViewModel.hasShownOnboarding()

    LaunchedEffect(Unit) {
        financeViewModel.uiEventFlow.collect { event ->
            when (event) {
                is UiEvent.ShowToast -> Toast.makeText(
                    context,
                    context.getString(event.messageRes),
                    if (event.isLong) Toast.LENGTH_LONG else Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    LaunchedEffect(Unit) {
        habayebViewModel.processRecurringTransactions { count ->
            if (count > 0) {
                Toast.makeText(
                    context,
                    context.getString(R.string.toast_recurring_txs_success, count),
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    LaunchedEffect(firstLaunch) {
        if (firstLaunch) showOnboarding = true
    }

    AppTheme(darkTheme = darkTheme) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            if (firstLaunch && showOnboarding) {
                WelcomeOnboardingDialog(
                    onDismiss = {
                        financeViewModel.markOnboardingShown()
                        financeViewModel.saveSettings(settings.copy(isFirstLaunch = false))
                        showOnboarding = false
                    }
                )
            }

            if (settings.isPasscodeEnabled && !isUnlocked) {
                AppLockScreen(
                    viewModel = securityViewModel,
                    onUnlockSuccess = { isUnlocked = true },
                    onUnlockBypassedAndDisabled = {
                        securityViewModel.saveSettings(
                            settings.copy(
                                isPasscodeEnabled = false,
                                passcodeHash = null,
                                recoveryPhraseHash = null
                            )
                        )
                        isUnlocked = true
                    }
                )
            } else {
                MainAppLayout(
                    viewModel = financeViewModel,
                    habayebViewModel = habayebViewModel,
                    securityViewModel = securityViewModel,
                    backupSyncViewModel = backupSyncViewModel,
                businessProfileViewModel = businessProfileViewModel,
                    settings = settings,
                    onExit = onExit
                )
            }
        }
    }
}
