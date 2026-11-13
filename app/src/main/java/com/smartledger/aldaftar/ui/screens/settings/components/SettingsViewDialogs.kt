package com.smartledger.aldaftar.ui.screens.settings.components

import androidx.compose.runtime.Composable
import com.smartledger.aldaftar.ui.components.CurrencyRevalueConfirmDialog

@Composable
fun RevalueConfirmDialog(
    targetCurrency: String,
    onConfirmAll: () -> Unit,
    onConfirmFutureOnly: () -> Unit,
    onDismiss: () -> Unit
) {
    CurrencyRevalueConfirmDialog(
        targetCurrency = targetCurrency,
        onConfirmHistoricalAndFuture = onConfirmAll,
        onConfirmFutureOnly = onConfirmFutureOnly,
        onDismiss = onDismiss
    )
}
