package com.example.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.data.local.entities.AppSettings
import com.example.ui.helper.HabayebMathHelper
import com.example.ui.screens.habayeb.utils.CurrencyConfig
import com.example.ui.screens.habayeb.utils.ExchangeRateHelper
import java.math.BigDecimal

// تم فصل حالة العرض عن مكونات الواجهة للحفاظ على مسؤولية واحدة دون تغيير تجربة المستخدم.

/**
 * فئة إدارة وتخزين حالة إعدادات العملة وأسعار الصرف، منفصلة عن عناصر بناء الواجهة.
 */
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

    val currentRateValue: Double
        get() = ExchangeRateHelper.getRate(localExchangeRatesJson, localDefaultCurrency, selectedTargetCurrency)

    var rateInputStr by mutableStateOf(
        if (currentRateValue > 0.0 && currentRateValue != 1.0) HabayebMathHelper.formatRate(currentRateValue) else ""
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
        val parsed = cleaned.toDoubleOrNull() ?: 1.0
        localExchangeRatesJson = ExchangeRateHelper.setRate(
            localExchangeRatesJson,
            localDefaultCurrency,
            selectedTargetCurrency,
            parsed
        )
    }

    private fun refreshRateInput() {
        val rate = currentRateValue
        rateInputStr = if (rate > 0.0 && rate != 1.0) HabayebMathHelper.formatRate(rate) else ""
    }

    fun handleSave(
        settings: AppSettings,
        onSaveSettings: (AppSettings, String, Double, Boolean) -> Unit,
        onDismiss: () -> Unit
    ) {
        val finalRate = rateInputStr.trim().toDoubleOrNull() ?: currentRateValue
        if (finalRate > 0.0) {
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
            val oldRateBD = BigDecimal.valueOf(existingRate)
            val newRateBD = BigDecimal.valueOf(finalRate)
            val rateChanged = existingRate > 0.0 && oldRateBD.compareTo(newRateBD) != 0

            if (alreadyHasRate && rateChanged) {
                activeDialogState = CurrencyDialogState.RevalueConfirm(selectedTargetCurrency, newRateBD)
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
            onSaveSettings(updatedSettings, "", 0.0, false)
            onDismiss()
        }
    }

    fun handleConfirmHistoricalAndFuture(
        settings: AppSettings,
        targetCurrency: String,
        newRate: BigDecimal,
        onSaveSettings: (AppSettings, String, Double, Boolean) -> Unit,
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
        onSaveSettings(updatedSettings, targetCurrency, newRate.toDouble(), true)
        activeDialogState = CurrencyDialogState.None
        onDismiss()
    }

    fun handleConfirmFutureOnly(
        settings: AppSettings,
        targetCurrency: String,
        newRate: BigDecimal,
        onSaveSettings: (AppSettings, String, Double, Boolean) -> Unit,
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
        onSaveSettings(updatedSettings, targetCurrency, newRate.toDouble(), false)
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
