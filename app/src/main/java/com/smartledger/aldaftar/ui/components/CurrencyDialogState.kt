package com.smartledger.aldaftar.ui.components

import java.math.BigDecimal

sealed interface CurrencyDialogState {

    object None : CurrencyDialogState

    data class RevalueConfirm(
        val targetCurrency: String,
        val newRate: BigDecimal = BigDecimal.ZERO
    ) : CurrencyDialogState
}

