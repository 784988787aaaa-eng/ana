package com.smartledger.aldaftar.ui.state

import androidx.compose.runtime.Immutable
import com.smartledger.aldaftar.data.local.entities.TransactionDb
import java.math.BigDecimal

/**
 * نموذج حالة دفتر العمليات العام (Main Ledger UI State).
 * تمثل فيه المبالغ المالية مثل totalCash بدقة باستخدام BigDecimal لمنع أخطاء التقريب.
 */
@Immutable
data class MainLedgerUiState(
    val transactions: List<TransactionDb> = emptyList(),
    val totalCash: BigDecimal = BigDecimal.ZERO,
    val isSearching: Boolean = false,
    val isLoading: Boolean = false
)
