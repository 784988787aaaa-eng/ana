package com.example.ui.screens

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import com.example.R
import com.example.data.local.entities.AppSettings
import com.example.ui.screens.habayeb.components.ExchangeRateSetupDialog
import com.example.ui.screens.habayeb.utils.ExchangeRateHelper
import com.example.ui.screens.settings.components.BackupPermissionExplanationDialog
import com.example.ui.screens.settings.components.DangerDeleteButton
import com.example.ui.screens.settings.components.GeneralSettingsCard
import com.example.ui.screens.settings.components.QuadBackupCard
import com.example.ui.screens.settings.components.ResetTrapDialog
import com.example.ui.screens.settings.components.RevalueConfirmDialog
import com.example.ui.screens.settings.components.SettingsAutoBackupCard
import com.example.ui.screens.settings.components.SettingsDeveloperFooter
import com.example.ui.screens.settings.components.SignatureCard
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.SoftRed
import com.example.ui.viewmodel.BackupSyncViewModel
import com.example.ui.viewmodel.FinanceViewModel
import com.example.ui.viewmodel.HabayebFinanceViewModel

sealed interface SettingsDialogState {
    object None : SettingsDialogState
    object PermissionExplanation : SettingsDialogState
    object ResetDataTrap : SettingsDialogState
    object CurrencySetup : SettingsDialogState
    data class RevalueConfirm(val targetCurrency: String, val newRate: Double) : SettingsDialogState
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

    val systemDark = androidx.compose.foundation.isSystemInDarkTheme()
    val isDark = remember(settings.themeMode, systemDark) {
        when (settings.themeMode) {
            1 -> false
            2 -> true
            else -> systemDark
        }
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        LaunchedEffect(isDark) {
            val window = (context as Activity).window
            window.statusBarColor = android.graphics.Color.TRANSPARENT
            window.navigationBarColor = android.graphics.Color.TRANSPARENT
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = !isDark
            insetsController.isAppearanceLightNavigationBars = !isDark
        }
    }

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

    val checkBackupPermissionsGranted = remember(context) {
        {
            val hasWrite = if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.R) {
                androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == android.content.pm.PackageManager.PERMISSION_GRANTED
            } else true

            val hasRead = androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_EXTERNAL_STORAGE) == android.content.pm.PackageManager.PERMISSION_GRANTED

            val hasNotification = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) == android.content.pm.PackageManager.PERMISSION_GRANTED
            } else true

            val hasManage = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                android.os.Environment.isExternalStorageManager()
            } else true

            hasWrite && hasRead && hasNotification && hasManage
        }
    }

    val multiplePermissionsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        val writeGranted = if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.R) {
            results[android.Manifest.permission.WRITE_EXTERNAL_STORAGE] ?: false
        } else true

        val readGranted = results[android.Manifest.permission.READ_EXTERNAL_STORAGE] ?: false

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            if (!android.os.Environment.isExternalStorageManager()) {
                Toast.makeText(context, context.getString(R.string.settings_toast_permission_manage_files), Toast.LENGTH_LONG).show()
                try {
                    val intent = Intent(android.provider.Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                        data = Uri.parse("package:${context.packageName}")
                    }
                    context.startActivity(intent)
                } catch (e: Exception) {
                    val intent = Intent(android.provider.Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                    context.startActivity(intent)
                }
            } else {
                onPermissionGrantedCallback?.invoke()
            }
        } else {
            if (writeGranted && readGranted) {
                onPermissionGrantedCallback?.invoke()
            } else {
                Toast.makeText(context, context.getString(R.string.settings_toast_permission_denied_err), Toast.LENGTH_LONG).show()
            }
        }
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
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(EmeraldPrimary)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.settings_title),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.settings_subtitle),
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.82f),
                        textAlign = TextAlign.Center
                    )
                }
            }
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
            OutlinedButton(
                onClick = onNavigateToSecurity,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBackIosNew,
                        contentDescription = stringResource(id = R.string.desc_security),
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = stringResource(R.string.drawer_security_label),
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = stringResource(id = R.string.desc_security),
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
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
                            com.example.AutoBackupWorker.scheduleDailyBackupWorker(context)
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
                        com.example.AutoBackupWorker.cancelDailyBackupWorker(context)
                        Toast.makeText(context, context.getString(R.string.settings_toast_auto_backup_disabled), Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }

        item(key = "danger_zone_wipe_card") {
            ElevatedCard(
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = SoftRed.copy(alpha = 0.03f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    DangerDeleteButton {
                        activeDialogState = SettingsDialogState.ResetDataTrap
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = stringResource(R.string.settings_danger_desc),
                        fontSize = 11.sp,
                        color = SoftRed.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center,
                        lineHeight = 15.sp
                    )
                }
            }
        }

        item(key = "developer_seal_footer_item") {
            SettingsDeveloperFooter(context = context)
        }
    }

    if (activeDialogState is SettingsDialogState.PermissionExplanation) {
        BackupPermissionExplanationDialog(
            onDismiss = { activeDialogState = SettingsDialogState.None },
            onGrantPermissions = {
                val permissions = mutableListOf<String>()
                if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.R) {
                    permissions.add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
                permissions.add(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                    permissions.add(android.Manifest.permission.POST_NOTIFICATIONS)
                }
                multiplePermissionsLauncher.launch(permissions.toTypedArray())
            },
            onUseInternalStorage = {
                onPermissionGrantedCallback?.invoke()
            }
        )
    }

    if (activeDialogState is SettingsDialogState.ResetDataTrap) {
        ResetTrapDialog(
            onDismiss = { activeDialogState = SettingsDialogState.None },
            onConfirmDelete = {
                viewModel.deleteAllData()
                activeDialogState = SettingsDialogState.None
            }
        )
    }

    if (activeDialogState is SettingsDialogState.CurrencySetup && currentSetupIndex < currenciesToSetup.size) {
        val targetCurrency = currenciesToSetup[currentSetupIndex]
        ExchangeRateSetupDialog(
            selectedCurrency = targetCurrency,
            initialRateStr = "",
            activeThemeColor = MaterialTheme.colorScheme.primary,
            onDismiss = {
                if (currentSetupIndex + 1 < currenciesToSetup.size) {
                    currentSetupIndex++
                } else {
                    activeDialogState = SettingsDialogState.None
                    currenciesToSetup = emptyList()
                }
            },
            onConfirm = { newRate ->
                val migratedOriginalJson = ExchangeRateHelper.migrateRates(
                    settings.exchangeRatesJson,
                    settings.currencySymbol,
                    currencySymbol
                )
                val alreadyHasRate = ExchangeRateHelper.hasRate(
                    migratedOriginalJson,
                    currencySymbol,
                    targetCurrency
                )
                val existingRate = ExchangeRateHelper.getRate(
                    migratedOriginalJson,
                    currencySymbol,
                    targetCurrency
                )
                val oldRateBD = java.math.BigDecimal.valueOf(existingRate)
                val newRateBD = java.math.BigDecimal.valueOf(newRate)
                val rateChanged = existingRate > 0.0 && oldRateBD.compareTo(newRateBD) != 0

                if (alreadyHasRate && rateChanged) {
                    activeDialogState = SettingsDialogState.RevalueConfirm(targetCurrency, newRate)
                } else {
                    val currentSettings = settings
                    val updatedSettings = currentSettings.copy(
                        exchangeRatesJson = ExchangeRateHelper.setRate(
                            currentSettings.exchangeRatesJson,
                            currencySymbol,
                            targetCurrency,
                            newRate
                        )
                    )
                    viewModel.saveSettings(updatedSettings)

                    if (currentSetupIndex + 1 < currenciesToSetup.size) {
                        currentSetupIndex++
                    } else {
                        activeDialogState = SettingsDialogState.None
                        currenciesToSetup = emptyList()
                    }
                }
            }
        )
    }

    val revalueState = activeDialogState as? SettingsDialogState.RevalueConfirm
    if (revalueState != null) {
        val targetCurrency = revalueState.targetCurrency
        val newRate = revalueState.newRate

        RevalueConfirmDialog(
            targetCurrency = targetCurrency,
            onConfirmAll = {
                habayebViewModel.revalueHistoricalTransactions(currencySymbol, targetCurrency, java.math.BigDecimal.valueOf(newRate))
                val currentSettings = settings
                val updatedSettings = currentSettings.copy(
                    exchangeRatesJson = ExchangeRateHelper.setRate(
                        currentSettings.exchangeRatesJson,
                        currencySymbol,
                        targetCurrency,
                        newRate
                    )
                )
                viewModel.saveSettings(updatedSettings)

                if (currentSetupIndex + 1 < currenciesToSetup.size) {
                    currentSetupIndex++
                    activeDialogState = SettingsDialogState.CurrencySetup
                } else {
                    activeDialogState = SettingsDialogState.None
                    currenciesToSetup = emptyList()
                }
            },
            onConfirmFutureOnly = {
                val currentSettings = settings
                val updatedSettings = currentSettings.copy(
                    exchangeRatesJson = ExchangeRateHelper.setRate(
                        currentSettings.exchangeRatesJson,
                        currencySymbol,
                        targetCurrency,
                        newRate
                    )
                )
                viewModel.saveSettings(updatedSettings)

                if (currentSetupIndex + 1 < currenciesToSetup.size) {
                    currentSetupIndex++
                    activeDialogState = SettingsDialogState.CurrencySetup
                } else {
                    activeDialogState = SettingsDialogState.None
                    currenciesToSetup = emptyList()
                }
            },
            onDismiss = {
                activeDialogState = SettingsDialogState.None
            }
        )
    }
}
