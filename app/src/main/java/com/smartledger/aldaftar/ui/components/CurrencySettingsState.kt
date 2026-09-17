package com.smartledger.aldaftar.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.smartledger.aldaftar.data.local.entities.AppSettings
import com.smartledger.aldaftar.ui.helper.HabayebMathHelper
import com.smartledger.aldaftar.ui.screens.habayeb.utils.CurrencyConfig
import com.smartledger.aldaftar.ui.screens.habayeb.utils.ExchangeRateHelper
import java.math.BigDecimal


class CurrencySettingsState(
    initialSettings: AppSettings,
    val currencyYer: String,
    val currencySar: String,
    val currencyUsd: String
) {
    var localDefaultCurrency by mutableStateOf(initialSettings.currencySymbol)
    var localExchangeRatesJson by mutableStateOf(initialSettings.exchangeRatesJson)

    val currenciesToDisplay = listOf(currencyYer, currencySar, currencyUsd)

    var selectedTargetCurrency by mutableStateOf(
        if (localDefaultCurrency == currencyYer) currencyUsd else currencyYer
    )

    val currentRateValue: BigDecimal
        get() = ExchangeRateHelper.getRate(localExchangeRatesJson, localDefaultCurrency, selectedTargetCurrency)

    var rateInputStr by mutableStateOf(
        if (currentRateValue.compareTo(BigDecimal.ZERO) > 0) HabayebMathHelper.formatRate(currentRateValue) else ""
    )

    var activeDialogState by mutableStateOf<CurrencyDialogState>(CurrencyDialogState.None)

    fun onDefaultCurrencyChange(newDefault: String) {
        val oldDefault = localDefaultCurrency
        localDefaultCurrency = newDefault
        if (selectedTargetCurrency == newDefault) {
            selectedTargetCurrency = if (newDefault == currencyYer) currencyUsd else currencyYer
        }
        localExchangeRatesJson = ExchangeRateHelper.migrateRates(
            localExchangeRatesJson,
            oldDefault,
            newDefault
        )
        refreshRateInput()
    }

    fun onTargetCurrencyChange(newTarget: String) {
        selectedTargetCurrency = newTarget
        refreshRateInput()
    }

    fun onRateInputChange(newInput: String) {
        val cleaned = CurrencyConfig.normalizeDigits(newInput)
        rateInputStr = cleaned
        val parsed = cleaned.toBigDecimalOrNull() ?: BigDecimal.ZERO
        localExchangeRatesJson = ExchangeRateHelper.setRate(
            localExchangeRatesJson,
            localDefaultCurrency,
            selectedTargetCurrency,
            parsed
        )
    }

    private fun refreshRateInput() {
        val rate = currentRateValue
        rateInputStr = if (rate.compareTo(BigDecimal.ZERO) > 0) HabayebMathHelper.formatRate(rate) else ""
    }

    fun handleSave(
        settings: AppSettings,
        onSaveSettings: (AppSettings, String, BigDecimal, Boolean) -> Unit,
        onDismiss: () -> Unit
    ) {
        val finalRate = rateInputStr.trim().toBigDecimalOrNull() ?: currentRateValue
        if (finalRate.compareTo(BigDecimal.ZERO) > 0) {
            val migratedOriginalJson = ExchangeRateHelper.migrateRates(
                settings.exchangeRatesJson,
                settings.currencySymbol,
                localDefaultCurrency
            )
            val alreadyHasRate = ExchangeRateHelper.hasRate(
                migratedOriginalJson,
                localDefaultCurrency,
                selectedTargetCurrency
            )
            val existingRate = ExchangeRateHelper.getRate(
                migratedOriginalJson,
                localDefaultCurrency,
                selectedTargetCurrency
            )
            val rateChanged = existingRate.compareTo(BigDecimal.ZERO) > 0 && existingRate.compareTo(finalRate) != 0

            if (alreadyHasRate && rateChanged) {
                activeDialogState = CurrencyDialogState.RevalueConfirm(selectedTargetCurrency, finalRate)
            } else {
                val updatedExchangeRatesJson = ExchangeRateHelper.setRate(
                    localExchangeRatesJson,
                    localDefaultCurrency,
                    selectedTargetCurrency,
                    finalRate
                )
                val updatedSettings = settings.copy(
                    currencySymbol = localDefaultCurrency,
                    exchangeRatesJson = updatedExchangeRatesJson
                )
                onSaveSettings(updatedSettings, selectedTargetCurrency, finalRate, false)
                onDismiss()
            }
        } else {
            val updatedSettings = settings.copy(
                currencySymbol = localDefaultCurrency,
                exchangeRatesJson = localExchangeRatesJson
            )
            onSaveSettings(updatedSettings, "", BigDecimal.ZERO, false)
            onDismiss()
        }
    }

    fun handleConfirmHistoricalAndFuture(
        settings: AppSettings,
        targetCurrency: String,
        newRate: BigDecimal,
        onSaveSettings: (AppSettings, String, BigDecimal, Boolean) -> Unit,
        onDismiss: () -> Unit
    ) {
        val updatedSettings = settings.copy(
            currencySymbol = localDefaultCurrency,
            exchangeRatesJson = ExchangeRateHelper.setRate(
                localExchangeRatesJson,
                localDefaultCurrency,
                targetCurrency,
                newRate
            )
        )
        onSaveSettings(updatedSettings, targetCurrency, newRate, true)
        activeDialogState = CurrencyDialogState.None
        onDismiss()
    }

    fun handleConfirmFutureOnly(
        settings: AppSettings,
        targetCurrency: String,
        newRate: BigDecimal,
        onSaveSettings: (AppSettings, String, BigDecimal, Boolean) -> Unit,
        onDismiss: () -> Unit
    ) {
        val updatedSettings = settings.copy(
            currencySymbol = localDefaultCurrency,
            exchangeRatesJson = ExchangeRateHelper.setRate(
                localExchangeRatesJson,
                localDefaultCurrency,
                targetCurrency,
                newRate
            )
        )
        onSaveSettings(updatedSettings, targetCurrency, newRate, false)
        activeDialogState = CurrencyDialogState.None
        onDismiss()
    }
}

@Composable
fun rememberCurrencySettingsState(
    settings: AppSettings,
    currencyYer: String,
    currencySar: String,
    currencyUsd: String
): CurrencySettingsState {
    return remember(settings, currencyYer, currencySar, currencyUsd) {
        CurrencySettingsState(
            initialSettings = settings,
            currencyYer = currencyYer,
            currencySar = currencySar,
            currencyUsd = currencyUsd
        )
    }
}
