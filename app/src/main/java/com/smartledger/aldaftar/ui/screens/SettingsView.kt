package com.smartledger.aldaftar.ui.screens

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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.smartledger.aldaftar.R
import com.smartledger.aldaftar.data.local.entities.AppSettings
import com.smartledger.aldaftar.ui.screens.habayeb.utils.ExchangeRateHelper
import com.smartledger.aldaftar.ui.screens.settings.components.GeneralSettingsCard
import com.smartledger.aldaftar.ui.screens.settings.components.SettingsDangerZoneCard
import com.smartledger.aldaftar.ui.screens.settings.components.SettingsDeveloperFooter
import com.smartledger.aldaftar.ui.screens.settings.components.SettingsDialogHost
import com.smartledger.aldaftar.ui.screens.settings.components.SettingsHeaderCard
import com.smartledger.aldaftar.ui.screens.settings.components.SettingsSecurityCard
import com.smartledger.aldaftar.ui.screens.settings.components.SignatureCard
import com.smartledger.aldaftar.ui.viewmodel.FinanceViewModel
import com.smartledger.aldaftar.ui.viewmodel.HabayebFinanceViewModel
import java.math.BigDecimal

sealed interface SettingsDialogState {
    object None : SettingsDialogState
    object ResetDataTrap : SettingsDialogState
    object CurrencySetup : SettingsDialogState
    data class RevalueConfirm(val targetCurrency: String, val newRate: BigDecimal = BigDecimal.ZERO) : SettingsDialogState
}

@Composable
fun SettingsView(
    viewModel: FinanceViewModel,
    habayebViewModel: HabayebFinanceViewModel,
    settings: AppSettings,
    onNavigateToSecurity: () -> Unit,
    contentPadding: PaddingValues = PaddingValues()
) {
    val context = LocalContext.current

    var activeDialogState by remember { mutableStateOf<SettingsDialogState>(SettingsDialogState.None) }

    var currencySymbol by remember { mutableStateOf(settings.currencySymbol) }
    var currenciesToSetup by remember { mutableStateOf<List<String>>(emptyList()) }
    var currentSetupIndex by remember { mutableStateOf(0) }
    var schoolExpenses by remember { mutableStateOf(settings.schoolExpensesEnabled) }

    LaunchedEffect(settings) {
        currencySymbol = settings.currencySymbol
        schoolExpenses = settings.schoolExpensesEnabled
    }


    val saveAllSettings = remember(settings, currencySymbol, schoolExpenses) {
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
    )
}
