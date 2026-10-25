package com.smartledger.aldaftar.ui.root

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.smartledger.aldaftar.R
import com.smartledger.aldaftar.data.cloud.GoogleDriveInternalAuth
import com.smartledger.aldaftar.ui.components.WelcomeOnboardingDialog
import com.smartledger.aldaftar.ui.main.MainAppLayout
import com.smartledger.aldaftar.ui.screens.AppLockScreen
import com.smartledger.aldaftar.ui.screens.license.DeviceReplacedDialog
import com.smartledger.aldaftar.ui.theme.AppTheme
import com.smartledger.aldaftar.ui.viewmodel.BackupSyncViewModel
import com.smartledger.aldaftar.ui.viewmodel.FinanceViewModel
import com.smartledger.aldaftar.ui.viewmodel.HabayebFinanceViewModel
import com.smartledger.aldaftar.ui.viewmodel.SecurityViewModel
import com.smartledger.aldaftar.ui.viewmodel.UiEvent
import com.smartledger.aldaftar.ui.viewmodel.LicenseViewModel

@androidx.compose.runtime.Composable
fun SmartLedgerApp(
    financeViewModel: FinanceViewModel,
    viewModelFactory: ViewModelProvider.Factory,
    onExit: () -> Unit,
    habayebViewModel: HabayebFinanceViewModel = viewModel(factory = viewModelFactory)
) {
    val context = LocalContext.current
    val securityViewModel: SecurityViewModel = viewModel(factory = viewModelFactory)
    val backupSyncViewModel: BackupSyncViewModel = viewModel(factory = viewModelFactory)
    val businessProfileViewModel: com.smartledger.aldaftar.ui.viewmodel.BusinessProfileViewModel = viewModel(factory = viewModelFactory)
    val licenseViewModel: LicenseViewModel = viewModel(factory = viewModelFactory)

    val settings by financeViewModel.settingsState.collectAsStateWithLifecycle()
    val settingsLoaded by financeViewModel.isSettingsLoaded.collectAsStateWithLifecycle()
    val themeMode by financeViewModel.themeModeState.collectAsStateWithLifecycle()
    val deviceReplacedNotice by licenseViewModel.deviceReplacedNotice.collectAsStateWithLifecycle()

    val googleSignInLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode != Activity.RESULT_OK) return@rememberLauncherForActivityResult
        runCatching {
            GoogleSignIn.getSignedInAccountFromIntent(result.data)
                .getResult(com.google.android.gms.common.api.ApiException::class.java)
        }.onSuccess { account ->
            licenseViewModel.signInWithGoogle(account, account.serverAuthCode)
        }.onFailure { _ ->
            Toast.makeText(context, context.getString(R.string.backup_toast_connect_failed), Toast.LENGTH_LONG).show()
        }
    }
    val systemDark = isSystemInDarkTheme()
    val darkTheme = when (themeMode) {
        1 -> false
        2 -> true
        else -> systemDark
    }

    var hasInitializedLockState by rememberSaveable { mutableStateOf(false) }
    var isUnlocked by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(settingsLoaded, settings.isPasscodeEnabled) {
        if (settingsLoaded) {
            if (!hasInitializedLockState) {
                isUnlocked = !settings.isPasscodeEnabled
                hasInitializedLockState = true
            } else if (!settings.isPasscodeEnabled) {
                // When lock is disabled (via settings or recovery), session is unlocked.
                isUnlocked = true
            }
            // Note: When settings.isPasscodeEnabled transitions from false -> true while
            // the user is already inside an active unlocked session (activating PIN in
            // SecurityScreen/SecurityDialog), isUnlocked remains true so MainAppLayout
            // is not torn down mid-session.
        }
    }

    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    androidx.compose.runtime.DisposableEffect(lifecycleOwner, settingsLoaded, settings.isPasscodeEnabled) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_STOP) {
                com.smartledger.aldaftar.platform.security.BiometricAuthHelper.cancelActiveAuthentication()
                val activity = context as? Activity
                val isConfigChange = activity?.isChangingConfigurations == true
                if (!isConfigChange && settingsLoaded && settings.isPasscodeEnabled) {
                    isUnlocked = false
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
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
            if (!deviceReplacedNotice.isNullOrBlank()) {
                DeviceReplacedDialog(
                    message = deviceReplacedNotice!!,
                    onDismiss = { licenseViewModel.dismissDeviceReplacedNotice() },
                    onReSignIn = {
                        licenseViewModel.dismissDeviceReplacedNotice()
                        val client = GoogleDriveInternalAuth(context).client()
                        googleSignInLauncher.launch(client.signInIntent)
                    }
                )
            }

            if (firstLaunch && showOnboarding) {
                WelcomeOnboardingDialog(
                    onDismiss = {
                        financeViewModel.completeOnboarding()
                        showOnboarding = false
                    }
                )
            }

            if (!settingsLoaded) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {}
            } else if (settings.isPasscodeEnabled && (!hasInitializedLockState || !isUnlocked)) {
                AppLockScreen(
                    viewModel = securityViewModel,
                    onUnlockSuccess = {
                        hasInitializedLockState = true
                        isUnlocked = true
                    },
                    onUnlockBypassedAndDisabled = {
                        hasInitializedLockState = true
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
                    licenseViewModel = licenseViewModel,
                    settings = settings,
                    onExit = onExit
                )
            }
        }
    }
}

