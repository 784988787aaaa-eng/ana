package com.smartledger.aldaftar.ui.root

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.smartledger.aldaftar.data.cloud.GoogleDriveInternalAuth
import com.smartledger.aldaftar.ui.main.MainAppLayout
import com.smartledger.aldaftar.ui.screens.AppLockScreen
import com.smartledger.aldaftar.ui.screens.license.DeviceReplacedDialog
import com.smartledger.aldaftar.ui.screens.license.LicenseDialog
import com.smartledger.aldaftar.ui.theme.AppTheme
import com.smartledger.aldaftar.ui.viewmodel.BusinessProfileViewModel
import com.smartledger.aldaftar.ui.viewmodel.FinanceViewModel
import com.smartledger.aldaftar.ui.viewmodel.HabayebFinanceViewModel
import com.smartledger.aldaftar.ui.viewmodel.LicenseViewModel
import com.smartledger.aldaftar.ui.viewmodel.SecurityViewModel

/** Application root. The normal workspace is Habayeb; security/licensing remain infrastructure gates. */
@androidx.compose.runtime.Composable
fun SmartLedgerApp(
    financeViewModel: FinanceViewModel,
    viewModelFactory: ViewModelProvider.Factory,
    onExit: () -> Unit
) {
    val context = LocalContext.current
    val securityViewModel: SecurityViewModel = viewModel(factory = viewModelFactory)
    val habayebViewModel: HabayebFinanceViewModel = viewModel(factory = viewModelFactory)
    val businessProfileViewModel: BusinessProfileViewModel = viewModel(factory = viewModelFactory)
    val licenseViewModel: LicenseViewModel = viewModel(factory = viewModelFactory)

    val settings by financeViewModel.settingsState.collectAsStateWithLifecycle()
    val themeMode by financeViewModel.themeModeState.collectAsStateWithLifecycle()
    val deviceReplacedNotice by licenseViewModel.deviceReplacedNotice.collectAsStateWithLifecycle()
    var showLicenseDialog by rememberSaveable { mutableStateOf(false) }

    val googleSignInLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode != Activity.RESULT_OK) return@rememberLauncherForActivityResult
        runCatching {
            GoogleSignIn.getSignedInAccountFromIntent(result.data)
                .getResult(com.google.android.gms.common.api.ApiException::class.java)
        }.onSuccess { account ->
            licenseViewModel.signInWithGoogle(account, account.serverAuthCode)
        }.onFailure { ex ->
            Toast.makeText(
                context,
                "تعذر تسجيل الدخول بحساب Google: ${ex.localizedMessage ?: ex.message}",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    val darkTheme = when (themeMode) {
        1 -> false
        2 -> true
        else -> isSystemInDarkTheme()
    }

    var isUnlocked by rememberSaveable(settings.isPasscodeEnabled) {
        mutableStateOf(!settings.isPasscodeEnabled)
    }


    LaunchedEffect(Unit) {
        habayebViewModel.processRecurringTransactions { count ->
            if (count > 0) {
                Toast.makeText(
                    context,
                    context.getString(com.smartledger.aldaftar.R.string.toast_recurring_txs_success, count),
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    LaunchedEffect(Unit) {
        licenseViewModel.licenseRequiredEvent.collect { showLicenseDialog = true }
    }

    AppTheme(darkTheme = darkTheme) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            if (!deviceReplacedNotice.isNullOrBlank()) {
                DeviceReplacedDialog(
                    message = deviceReplacedNotice!!,
                    onDismiss = { licenseViewModel.dismissDeviceReplacedNotice() },
                    onReSignIn = {
                        licenseViewModel.dismissDeviceReplacedNotice()
                        googleSignInLauncher.launch(GoogleDriveInternalAuth(context).client().signInIntent)
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
                    businessProfileViewModel = businessProfileViewModel,
                    settings = settings,
                    onExit = onExit
                )
            }

            if (showLicenseDialog) {
                LicenseDialog(
                    viewModel = licenseViewModel,
                    onDismiss = { showLicenseDialog = false }
                )
            }
        }
    }
}
