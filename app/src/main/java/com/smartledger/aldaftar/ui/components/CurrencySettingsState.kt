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
        if (localDefaultCurrency == currencyYer) currencySar else currencyYer
    )

    var isEquationInverted by mutableStateOf(false)

    val displayPair: Pair<String, String>
        get() {
            val (base, target) = ExchangeRateHelper.getCanonicalPairOrder(selectedTargetCurrency, localDefaultCurrency, localExchangeRatesJson)
            return if (!isEquationInverted) Pair(base, target) else Pair(target, base)
        }

    val currentRateValue: BigDecimal
        get() = ExchangeRateHelper.getRate(localExchangeRatesJson, displayPair.first, displayPair.second)

    var rateInputStr by mutableStateOf(
        if (currentRateValue.compareTo(BigDecimal.ZERO) > 0) HabayebMathHelper.formatActiveRateBadge(currentRateValue) else ""
    )

    var activeDialogState by mutableStateOf<CurrencyDialogState>(CurrencyDialogState.None)

    fun toggleEquationDirection() {
        isEquationInverted = !isEquationInverted
        refreshRateInput()
    }

    fun onDefaultCurrencyChange(newDefault: String) {
        val oldDefault = localDefaultCurrency
        localDefaultCurrency = newDefault
        if (selectedTargetCurrency == newDefault) {
            selectedTargetCurrency = if (newDefault == currencyYer) currencySar else currencyYer
        }
        isEquationInverted = false
        localExchangeRatesJson = ExchangeRateHelper.migrateRates(
            localExchangeRatesJson,
            oldDefault,
            newDefault
        )
        refreshRateInput()
    }

    fun onTargetCurrencyChange(newTarget: String) {
        selectedTargetCurrency = newTarget
        isEquationInverted = false
        refreshRateInput()
    }

    fun onRateInputChange(newInput: String) {
        val cleaned = CurrencyConfig.normalizeDigits(newInput).trim()
        rateInputStr = cleaned
        if (cleaned.isBlank()) {
            localExchangeRatesJson = ExchangeRateHelper.clearRate(
                localExchangeRatesJson, displayPair.first, displayPair.second
            )
            return
        }
        val parsed = cleaned.toBigDecimalOrNull() ?: return
        if (parsed > BigDecimal.ZERO) {
            localExchangeRatesJson = ExchangeRateHelper.setRate(
                localExchangeRatesJson, displayPair.first, displayPair.second, parsed
            )
        }
    }

    private fun refreshRateInput() {
        val rate = currentRateValue
        rateInputStr = if (rate.compareTo(BigDecimal.ZERO) > 0) HabayebMathHelper.formatActiveRateBadge(rate) else ""
    }

    fun handleSave(
        settings: AppSettings,
        onSaveSettings: (AppSettings, String, BigDecimal, Boolean) -> Unit,
        onDismiss: () -> Unit
    ) {
        val finalRate = rateInputStr.trim().toBigDecimalOrNull() ?: currentRateValue
        if (finalRate.compareTo(BigDecimal.ZERO) > 0) {
            val updatedExchangeRatesJson = ExchangeRateHelper.setRate(
                localExchangeRatesJson,
                displayPair.first,
                displayPair.second,
                finalRate
            )
            val effectiveTargetToDefaultRate = ExchangeRateHelper.getRate(
                updatedExchangeRatesJson,
                selectedTargetCurrency,
                localDefaultCurrency
            )

            val migratedOriginalJson = ExchangeRateHelper.migrateRates(
                settings.exchangeRatesJson,
                settings.currencySymbol,
                localDefaultCurrency
            )
            val alreadyHasRate = ExchangeRateHelper.hasRate(
                migratedOriginalJson,
                selectedTargetCurrency,
                localDefaultCurrency
            )
            val existingRate = ExchangeRateHelper.getRate(
                migratedOriginalJson,
                selectedTargetCurrency,
                localDefaultCurrency
            )
            val rateChanged = existingRate.compareTo(BigDecimal.ZERO) > 0 && existingRate.compareTo(effectiveTargetToDefaultRate) != 0

            if (alreadyHasRate && rateChanged) {
                activeDialogState = CurrencyDialogState.RevalueConfirm(selectedTargetCurrency, effectiveTargetToDefaultRate)
            } else {
                val updatedSettings = settings.copy(
                    currencySymbol = localDefaultCurrency,
                    exchangeRatesJson = updatedExchangeRatesJson
                )
                onSaveSettings(updatedSettings, selectedTargetCurrency, effectiveTargetToDefaultRate, false)
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
                targetCurrency,
                localDefaultCurrency,
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
                targetCurrency,
                localDefaultCurrency,
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
