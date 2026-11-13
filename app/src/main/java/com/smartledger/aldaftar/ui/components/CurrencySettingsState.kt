package com.smartledger.aldaftar.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.smartledger.aldaftar.data.local.entities.AppSettings
import com.smartledger.aldaftar.ui.helper.HabayebMathHelper
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
        if (currentRateValue.compareTo(BigDecimal.ZERO) > 0) HabayebMathHelper.formatRate(currentRateValue) else ""
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
        val cleaned = HabayebMathHelper.normalizeAmountDigits(newInput).trim()
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
        rateInputStr = if (rate.compareTo(BigDecimal.ZERO) > 0) HabayebMathHelper.formatRate(rate) else ""
    }

    fun handleSave(
        settings: AppSettings,
        onSaveSettings: (AppSettings, String, BigDecimal, Boolean) -> Unit,
        onDismiss: () -> Unit
    ) {
        val trimmedInput = HabayebMathHelper.normalizeAmountDigits(rateInputStr).trim()
        val parsedInput = trimmedInput.toBigDecimalOrNull()
        val finalRate = parsedInput ?: currentRateValue

        val workingJson = when {
            trimmedInput.isBlank() -> ExchangeRateHelper.clearRate(
                localExchangeRatesJson,
                displayPair.first,
                displayPair.second
            )
            finalRate.compareTo(BigDecimal.ZERO) > 0 -> ExchangeRateHelper.setRate(
                localExchangeRatesJson,
                displayPair.first,
                displayPair.second,
                finalRate
            )
            else -> localExchangeRatesJson
        }
        localExchangeRatesJson = workingJson

        val migratedOriginalJson = ExchangeRateHelper.migrateRates(
            settings.exchangeRatesJson,
            settings.currencySymbol,
            localDefaultCurrency
        )

        val changedExistingPairs = currenciesToDisplay
            .filter { it != localDefaultCurrency }
            .mapNotNull { candidateTarget ->
                val newPairRate = ExchangeRateHelper.getRate(workingJson, candidateTarget, localDefaultCurrency)
                val alreadyHasRate = ExchangeRateHelper.hasRate(migratedOriginalJson, candidateTarget, localDefaultCurrency)
                val existingRate = ExchangeRateHelper.getRate(migratedOriginalJson, candidateTarget, localDefaultCurrency)
                val rateChanged = existingRate.compareTo(BigDecimal.ZERO) > 0 &&
                    newPairRate.compareTo(BigDecimal.ZERO) > 0 &&
                    existingRate.compareTo(newPairRate) != 0
                if (alreadyHasRate && rateChanged) candidateTarget to newPairRate else null
            }

        if (changedExistingPairs.isNotEmpty()) {
            val primary = changedExistingPairs.find { it.first == selectedTargetCurrency }
                ?: changedExistingPairs.first()
            activeDialogState = CurrencyDialogState.RevalueConfirm(
                targetCurrency = primary.first,
                newRate = primary.second,
                modifiedPairs = changedExistingPairs
            )
        } else {
            val selectedRate = if (selectedTargetCurrency != localDefaultCurrency) {
                ExchangeRateHelper.getRate(workingJson, selectedTargetCurrency, localDefaultCurrency)
            } else {
                BigDecimal.ZERO
            }
            val updatedSettings = settings.copy(
                currencySymbol = localDefaultCurrency,
                exchangeRatesJson = workingJson
            )
            val saveTarget = if (selectedRate.compareTo(BigDecimal.ZERO) > 0) selectedTargetCurrency else ""
            val saveRate = if (selectedRate.compareTo(BigDecimal.ZERO) > 0) selectedRate else BigDecimal.ZERO
            onSaveSettings(updatedSettings, saveTarget, saveRate, false)
            onDismiss()
        }
    }

    private fun applyModifiedPairsToRatesJson(
        baseJson: String,
        targetCurrency: String,
        newRate: BigDecimal,
        modifiedPairs: List<Pair<String, BigDecimal>>
    ): String {
        val pairsToApply = modifiedPairs.ifEmpty {
            if (targetCurrency.isNotBlank() && newRate.compareTo(BigDecimal.ZERO) > 0) {
                listOf(targetCurrency to newRate)
            } else {
                emptyList()
            }
        }
        return pairsToApply.fold(baseJson) { acc, (curr, rate) ->
            if (rate.compareTo(BigDecimal.ZERO) > 0) {
                ExchangeRateHelper.setRate(acc, curr, localDefaultCurrency, rate)
            } else {
                acc
            }
        }
    }

    fun handleConfirmHistoricalAndFuture(
        settings: AppSettings,
        targetCurrency: String,
        newRate: BigDecimal,
        onSaveSettings: (AppSettings, String, BigDecimal, Boolean) -> Unit,
        onDismiss: () -> Unit,
        modifiedPairs: List<Pair<String, BigDecimal>> = listOf(targetCurrency to newRate),
        onRevalueAllPairs: ((List<Pair<String, BigDecimal>>) -> Unit)? = null
    ) {
        val pairsToApply = modifiedPairs.ifEmpty {
            if (targetCurrency.isNotBlank() && newRate.compareTo(BigDecimal.ZERO) > 0) {
                listOf(targetCurrency to newRate)
            } else {
                emptyList()
            }
        }
        val finalJson = applyModifiedPairsToRatesJson(
            localExchangeRatesJson,
            targetCurrency,
            newRate,
            pairsToApply
        )
        localExchangeRatesJson = finalJson
        val updatedSettings = settings.copy(
            currencySymbol = localDefaultCurrency,
            exchangeRatesJson = finalJson
        )
        if (onRevalueAllPairs != null) {
            onSaveSettings(updatedSettings, targetCurrency, newRate, false)
            onRevalueAllPairs(pairsToApply)
        } else {
            onSaveSettings(updatedSettings, targetCurrency, newRate, true)
        }
        activeDialogState = CurrencyDialogState.None
        onDismiss()
    }

    fun handleConfirmFutureOnly(
        settings: AppSettings,
        targetCurrency: String,
        newRate: BigDecimal,
        onSaveSettings: (AppSettings, String, BigDecimal, Boolean) -> Unit,
        onDismiss: () -> Unit,
        modifiedPairs: List<Pair<String, BigDecimal>> = listOf(targetCurrency to newRate)
    ) {
        val finalJson = applyModifiedPairsToRatesJson(
            localExchangeRatesJson,
            targetCurrency,
            newRate,
            modifiedPairs
        )
        localExchangeRatesJson = finalJson
        val updatedSettings = settings.copy(
            currencySymbol = localDefaultCurrency,
            exchangeRatesJson = finalJson
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
