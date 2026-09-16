package com.smartledger.aldaftar.ui.screens.habayeb

import com.smartledger.aldaftar.data.local.entities.HabayebCustomer
import com.smartledger.aldaftar.data.local.entities.HabayebTransaction
import com.smartledger.aldaftar.domain.model.TransactionType
import com.smartledger.aldaftar.ui.state.CustomerUiState

sealed interface HabayebDialogState {
    object None : HabayebDialogState
    object AddCustomer : HabayebDialogState
    data class AddTransaction(
        val customer: HabayebCustomer,
        val defaultType: String = TransactionType.OWED_BY_THEM.value,
        val editingTx: HabayebTransaction? = null
    ) : HabayebDialogState
    data class EditCustomer(val customer: HabayebCustomer) : HabayebDialogState
    object DeleteConfirm : HabayebDialogState
    object AddCategory : HabayebDialogState
    object BulkAssignCategory : HabayebDialogState
    data class ContextMenu(val customer: CustomerUiState) : HabayebDialogState
}
