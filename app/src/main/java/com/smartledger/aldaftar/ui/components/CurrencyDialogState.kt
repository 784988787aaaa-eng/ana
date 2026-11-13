package com.smartledger.aldaftar.ui.components

import java.math.BigDecimal

sealed interface CurrencyDialogState {

    object None : CurrencyDialogState

    data class RevalueConfirm(
        val targetCurrency: String,
        val newRate: BigDecimal = BigDecimal.ZERO,
        val modifiedPairs: List<Pair<String, BigDecimal>> =
            if (targetCurrency.isNotBlank()) listOf(targetCurrency to newRate) else emptyList()
    ) : CurrencyDialogState
}

