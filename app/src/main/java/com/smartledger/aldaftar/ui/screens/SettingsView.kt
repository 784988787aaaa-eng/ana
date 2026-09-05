package com.smartledger.aldaftar.ui.screens

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import com.smartledger.aldaftar.R
import com.smartledger.aldaftar.data.local.entities.AppSettings
import com.smartledger.aldaftar.ui.screens.habayeb.utils.ExchangeRateHelper
import com.smartledger.aldaftar.ui.screens.settings.components.GeneralSettingsCard
import com.smartledger.aldaftar.ui.screens.settings.components.QuadBackupCard
import com.smartledger.aldaftar.ui.screens.settings.components.SettingsAutoBackupCard
import com.smartledger.aldaftar.ui.screens.settings.components.SettingsDangerZoneCard
import com.smartledger.aldaftar.ui.screens.settings.components.SettingsDeveloperFooter
import com.smartledger.aldaftar.ui.screens.settings.components.SettingsDialogHost
import com.smartledger.aldaftar.ui.screens.settings.components.SettingsHeaderCard
import com.smartledger.aldaftar.ui.screens.settings.components.SettingsSecurityCard
import com.smartledger.aldaftar.ui.screens.settings.components.SignatureCard
import com.smartledger.aldaftar.ui.viewmodel.BackupSyncViewModel
import com.smartledger.aldaftar.ui.viewmodel.FinanceViewModel
import com.smartledger.aldaftar.ui.viewmodel.HabayebFinanceViewModel
import java.math.BigDecimal

sealed interface SettingsDialogState {
    object None : SettingsDialogState
    object PermissionExplanation : SettingsDialogState
    object ResetDataTrap : SettingsDialogState
    object CurrencySetup : SettingsDialogState
    data class RevalueConfirm(val targetCurrency: String, val newRate: BigDecimal = BigDecimal.ZERO) : SettingsDialogState
}

@Composable
fun SettingsView(
    viewModel: FinanceViewModel,
    habayebViewModel: HabayebFinanceViewModel,
    backupSyncViewModel: BackupSyncViewModel,
    settings: AppSettings,
    onNavigateToSecurity: () -> Unit,
    contentPadding: PaddingValues = PaddingValues()
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    var activeDialogState by remember { mutableStateOf<SettingsDialogState>(SettingsDialogState.None) }
    var onPermissionGrantedCallback by remember { mutableStateOf<(() -> Unit)?>(null) }

    var currencySymbol by remember { mutableStateOf(settings.currencySymbol) }
    var currenciesToSetup by remember { mutableStateOf<List<String>>(emptyList()) }
    var currentSetupIndex by remember { mutableStateOf(0) }
    var schoolExpenses by remember { mutableStateOf(settings.schoolExpensesEnabled) }
    var isAutoBackupEnabled by remember { mutableStateOf(settings.isAutoBackupEnabled) }

    LaunchedEffect(settings) {
        currencySymbol = settings.currencySymbol
        schoolExpenses = settings.schoolExpensesEnabled
        isAutoBackupEnabled = settings.isAutoBackupEnabled
    }

    val coroutineScope = rememberCoroutineScope()

    // النسخ التلقائي يعمل داخل مساحة التطبيق المعزولة؛ التصدير اليدوي يتم عبر SAF.
    val checkBackupPermissionsGranted = remember { { true } }

    val multiplePermissionsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        // لا توجد صلاحيات تخزين مطلوبة؛ نستدعي الإجراء مباشرة بعد إغلاق شاشة الشرح القديمة.
        onPermissionGrantedCallback?.invoke()
    }

    val saveAllSettings = remember(settings, currencySymbol, schoolExpenses, isAutoBackupEnabled) {
        {
            var finalJson = settings.exchangeRatesJson
            if (settings.currencySymbol != currencySymbol) {
                finalJson = ExchangeRateHelper.migrateRates(
                    settings.exchangeRatesJson,
                    settings.currencySymbol,
                    currencySymbol
                )
            }
            val updated = settings.copy(
                currencySymbol = currencySymbol,
                schoolExpensesEnabled = schoolExpenses,
                isAutoBackupEnabled = isAutoBackupEnabled,
                exchangeRatesJson = finalJson
            )
            viewModel.saveSettings(updated)
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = contentPadding.calculateBottomPadding() + 40.dp)
    ) {
        item(key = "settings_header_card") {
            SettingsHeaderCard()
        }

        item(key = "general_preferences_card") {
            GeneralSettingsCard(
                currencySymbol = currencySymbol,
                onCurrencySymbolChange = { newSymbol ->
                    val oldSymbol = settings.currencySymbol
                    currencySymbol = newSymbol

                    val migratedJson = if (oldSymbol != newSymbol) {
                        ExchangeRateHelper.migrateRates(
                            settings.exchangeRatesJson,
                            oldSymbol,
                            newSymbol
                        )
                    } else {
                        settings.exchangeRatesJson
                    }

                    val updated = settings.copy(
                        currencySymbol = newSymbol,
                        schoolExpensesEnabled = schoolExpenses,
                        isAutoBackupEnabled = isAutoBackupEnabled,
                        exchangeRatesJson = migratedJson
                    )
                    viewModel.saveSettings(updated)

                    val otherCurrencies = listOf(
                        context.getString(R.string.currency_yer),
                        context.getString(R.string.currency_usd),
                        context.getString(R.string.currency_sar)
                    ).filter { it != newSymbol }
                    val missingRates = otherCurrencies.filter { other ->
                        !ExchangeRateHelper.hasRate(migratedJson, newSymbol, other)
                    }
                    if (missingRates.isNotEmpty()) {
                        currenciesToSetup = missingRates
                        currentSetupIndex = 0
                        activeDialogState = SettingsDialogState.CurrencySetup
                    }
                }
            )
        }

        item(key = "business_signature_card") {
            SignatureCard()
        }

        item(key = "security_portal_button") {
            SettingsSecurityCard(onNavigateToSecurity = onNavigateToSecurity)
        }

        item(key = "quad_backup_central_card") {
            QuadBackupCard(
                backupSyncViewModel = backupSyncViewModel,
                settings = settings,
                onRestoreSuccess = { restoredSettings ->
                    currencySymbol = restoredSettings.currencySymbol
                    schoolExpenses = restoredSettings.schoolExpensesEnabled
                }
            )
        }

        item(key = "auto_backup_schedule_card") {
            SettingsAutoBackupCard(
                isAutoBackupEnabled = isAutoBackupEnabled,
                onCheckedChange = { checked ->
                    if (checked) {
                        val enableAutoBackup = {
                            isAutoBackupEnabled = true
                            saveAllSettings()
                            com.smartledger.aldaftar.AutoBackupWorker.scheduleDailyBackupWorker(context)
                            Toast.makeText(context, context.getString(R.string.settings_toast_auto_backup_enabled), Toast.LENGTH_SHORT).show()
                        }
                        if (checkBackupPermissionsGranted()) {
                            enableAutoBackup()
                        } else {
                            onPermissionGrantedCallback = enableAutoBackup
                            activeDialogState = SettingsDialogState.PermissionExplanation
                        }
                    } else {
                        isAutoBackupEnabled = false
                        saveAllSettings()
                        com.smartledger.aldaftar.AutoBackupWorker.cancelDailyBackupWorker(context)
                        Toast.makeText(context, context.getString(R.string.settings_toast_auto_backup_disabled), Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }

        item(key = "danger_zone_wipe_card") {
            SettingsDangerZoneCard(
                onTriggerResetTrap = {
                    activeDialogState = SettingsDialogState.ResetDataTrap
                }
            )
        }

        item(key = "developer_seal_footer_item") {
            SettingsDeveloperFooter(context = context)
        }
    }

    SettingsDialogHost(
        activeDialogState = activeDialogState,
        onDismissDialog = { activeDialogState = SettingsDialogState.None },
        onStateChange = { activeDialogState = it },
        settings = settings,
        currencySymbol = currencySymbol,
        currenciesToSetup = currenciesToSetup,
        currentSetupIndex = currentSetupIndex,
        onSetupIndexChange = { currentSetupIndex = it },
        onCurrenciesToSetupChange = { currenciesToSetup = it },
        viewModel = viewModel,
        habayebViewModel = habayebViewModel,
        onLaunchPermissions = {
            // لا نطلب صلاحيات تخزين؛ المسار الداخلي لا يحتاج Permission وSAF يطلب التفويض للملف المحدد فقط.
            onPermissionGrantedCallback?.invoke()
        },
        onPermissionGrantedCallback = onPermissionGrantedCallback
    )
}
