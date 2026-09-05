package com.smartledger.aldaftar.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.smartledger.aldaftar.data.local.entities.FixedCommitment
import com.smartledger.aldaftar.data.local.entities.TransactionDb

sealed interface MainLedgerDialogState {
    object None : MainLedgerDialogState
    data class AddTransaction(val type: String = "EXPENSE", val editingTx: TransactionDb? = null) : MainLedgerDialogState
    object Search : MainLedgerDialogState
    object CommitmentsList : MainLedgerDialogState
    data class AddCommitment(val editingCommitment: FixedCommitment? = null) : MainLedgerDialogState
    data class ReorderCommitment(val target: FixedCommitment) : MainLedgerDialogState
    object DeleteDaysConfirm : MainLedgerDialogState
    object DeviceActivation : MainLedgerDialogState
}

class MainLedgerUiController internal constructor(
    private val habayebActiveState: MutableState<Boolean>
) {
    var activeDialogState by mutableStateOf<MainLedgerDialogState>(MainLedgerDialogState.None)
    var expandedDayKeys by mutableStateOf(setOf<String>())
    var isSelectionMode by mutableStateOf(false)
    val selectedTxIds = mutableStateListOf<String>()
    var collapsedMonths by mutableStateOf(setOf<String>())

    var isHabayebActive: Boolean
        get() = habayebActiveState.value
        set(value) {
            habayebActiveState.value = value
        }

    var isDaySelectionMode by mutableStateOf(false)
    val selectedDayKeys = mutableStateListOf<String>()

    fun clearSelection() {
        selectedTxIds.clear()
        selectedDayKeys.clear()
        isSelectionMode = false
        isDaySelectionMode = false
    }

    fun toggleMonthCollapsed(mKey: String) {
        collapsedMonths = if (collapsedMonths.contains(mKey)) {
            collapsedMonths - mKey
        } else {
            collapsedMonths + mKey
        }
    }

    fun handleDayClick(key: String) {
        if (isDaySelectionMode) {
            if (selectedDayKeys.contains(key)) {
                selectedDayKeys.remove(key)
                if (selectedDayKeys.isEmpty()) isDaySelectionMode = false
            } else {
                selectedDayKeys.add(key)
            }
        } else {
            expandedDayKeys = if (expandedDayKeys.contains(key)) expandedDayKeys - key else expandedDayKeys + key
        }
    }

    fun handleDayLongClick(key: String) {
        if (!isDaySelectionMode && !isSelectionMode) {
            isDaySelectionMode = true
            selectedDayKeys.add(key)
        } else if (isDaySelectionMode) {
            if (selectedDayKeys.contains(key)) {
                selectedDayKeys.remove(key)
                if (selectedDayKeys.isEmpty()) isDaySelectionMode = false
            } else {
                selectedDayKeys.add(key)
            }
        }
    }

    fun handleTransactionSelectToggle(txId: String) {
        if (selectedTxIds.contains(txId)) {
            selectedTxIds.remove(txId)
            if (selectedTxIds.isEmpty()) isSelectionMode = false
        } else {
            if (!isSelectionMode) isSelectionMode = true
            selectedTxIds.add(txId)
        }
    }

    fun cancelDaySelection() {
        isDaySelectionMode = false
        selectedDayKeys.clear()
    }

    fun selectAllDays(allKeys: List<String>) {
        if (selectedDayKeys.size == allKeys.size) {
            selectedDayKeys.clear()
        } else {
            selectedDayKeys.clear()
            selectedDayKeys.addAll(allKeys)
        }
    }

    fun dismissDialog() {
        activeDialogState = MainLedgerDialogState.None
    }
}

@Composable
fun rememberMainLedgerUiController(): MainLedgerUiController {
    val habayebActiveState = rememberSaveable { mutableStateOf(false) }
    return remember {
        MainLedgerUiController(habayebActiveState = habayebActiveState)
    }
}
