package com.smartledger.aldaftar.ui.state

import androidx.compose.runtime.Immutable
import com.smartledger.aldaftar.data.local.entities.TransactionDb
import java.math.BigDecimal

@Immutable
data class MainLedgerUiState(
    val transactions: List<TransactionDb> = emptyList(),
    val totalCash: BigDecimal = BigDecimal.ZERO,
    val isSearching: Boolean = false,
    val isLoading: Boolean = false
)
