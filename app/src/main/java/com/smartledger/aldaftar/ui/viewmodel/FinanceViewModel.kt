package com.smartledger.aldaftar.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.smartledger.aldaftar.R
import com.smartledger.aldaftar.data.local.entities.AppSettings
import com.smartledger.aldaftar.data.local.entities.CustomCategory
import com.smartledger.aldaftar.data.local.entities.DeletedItemEntity
import com.smartledger.aldaftar.data.local.entities.FixedCommitment
import com.smartledger.aldaftar.data.local.entities.TransactionDb
import com.smartledger.aldaftar.presentation.utils.DateUtils
import com.smartledger.aldaftar.platform.contacts.StringUtils
import com.smartledger.aldaftar.domain.model.TransactionType
import com.smartledger.aldaftar.ui.state.MainLedgerUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.UUID
import com.smartledger.aldaftar.ui.viewmodel.ledger.MonthLedger


class FinanceViewModel(
    application: Application,
    private val settingsRepository: com.smartledger.aldaftar.data.repository.SettingsRepository,
    private val commitmentsRepository: com.smartledger.aldaftar.data.repository.CommitmentRepository,
    private val transactionsRepository: com.smartledger.aldaftar.data.repository.TransactionRepository,
    private val categoriesRepository: com.smartledger.aldaftar.data.repository.CategoryRepository,
    private val habayebRepository: com.smartledger.aldaftar.data.repository.HabayebRepository,
    private val trashRepository: com.smartledger.aldaftar.data.repository.TrashRepository,
    private val maintenanceRepository: com.smartledger.aldaftar.data.repository.DataMaintenanceRepository,
    private val floatingUiRepository: com.smartledger.aldaftar.data.repository.FloatingUiPreferencesRepository
) : AndroidViewModel(application) {

    companion object {
        private const val CLEANUP_PERIOD_NEVER = "never"
        private const val TRANSACTION_TYPE_EXPENSE = "EXPENSE"
        private const val PREFIX_HABAYEB = "habayeb_"
    }

    private val app = application


    fun floatingSearchState() = floatingUiRepository.search()
    fun saveFloatingSearchState(state: com.smartledger.aldaftar.data.repository.FloatingSearchState) = floatingUiRepository.saveSearch(state)
    fun floatingAddState() = floatingUiRepository.add()
    fun saveFloatingAddState(state: com.smartledger.aldaftar.data.repository.FloatingAddState) = floatingUiRepository.saveAdd(state)
    fun isFloatingSearchActive() = floatingUiRepository.floatingSearchActive()
    fun setFloatingSearchActive(active: Boolean) = floatingUiRepository.setFloatingSearchActive(active)

    private val _autoCleanupPeriod = MutableStateFlow(CLEANUP_PERIOD_NEVER)
    val autoCleanupPeriod: StateFlow<String> = _autoCleanupPeriod.asStateFlow()

    private val _uiEventChannel = Channel<UiEvent>(Channel.BUFFERED)
    val uiEventFlow = _uiEventChannel.receiveAsFlow()

    private fun sendUiEvent(event: UiEvent) {
        _uiEventChannel.trySend(event)
    }


    private val _themeModeState = MutableStateFlow(0)
    val themeModeState: StateFlow<Int> = _themeModeState.asStateFlow()

    val isSettingsLoaded = MutableStateFlow(false)

    val settingsState: StateFlow<AppSettings> = settingsRepository.settingsFlow
        .onEach {
            isSettingsLoaded.value = true
            if (it != null && _themeModeState.value != it.themeMode) {
                _themeModeState.value = it.themeMode
            }
        }
        .map { it ?: AppSettings() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppSettings())

    val commitmentsState: StateFlow<List<FixedCommitment>> = commitmentsRepository.commitmentsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val transactionsState: StateFlow<List<TransactionDb>> = transactionsRepository.transactionsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val customCategoriesState: StateFlow<List<CustomCategory>> = categoriesRepository.customCategoriesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val deletedItemsFlow: Flow<List<DeletedItemEntity>> = trashRepository.deletedItemsFlow

    val totalTransactionsCount: StateFlow<Int> = combine(
        transactionsRepository.getTransactionsCountFlow(),
        habayebRepository.getHabayebTransactionsCountFlow()
    ) { mainCount, habayebCount ->
        mainCount + habayebCount
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun hasShownOnboarding(): Boolean = settingsState.value.onboardingShown

    fun markOnboardingShown() {
        viewModelScope.launch { settingsRepository.saveSettings(settingsState.value.copy(onboardingShown = true)) }
    }

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    val searchResultsState: StateFlow<List<TransactionDb>> = combine(transactionsState, _searchQuery) { transactions, query ->
        if (query.isBlank()) emptyList()
        else {
            val normalizedQuery = StringUtils.normalizeArabic(query, app)
            transactions.filter { tx ->
                StringUtils.normalizeArabic(tx.description, app).contains(normalizedQuery, ignoreCase = true)
            }.sortedByDescending { it.timestamp }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalCashState: StateFlow<BigDecimal> = transactionsRepository.getTotalCashFlow()
        .map { it.setScale(2, RoundingMode.HALF_EVEN) }
        .flowOn(Dispatchers.IO)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), BigDecimal.ZERO)

    val dailyExpenseComparisonState: StateFlow<Pair<BigDecimal, BigDecimal>> = transactionsState
        .map { txList ->
            val nowSec = System.currentTimeMillis() / 1000
            val todayKey = DateUtils.formatDateFull(nowSec)
            val yesterdayKey = DateUtils.formatDateFull(nowSec - 86400L)

            var todayExpenses = BigDecimal.ZERO
            var yesterdayExpenses = BigDecimal.ZERO

            for (tx in txList) {
                val txType = TransactionType.fromValue(tx.type)
                if (txType == TransactionType.EXPENSE || tx.type.equals(TRANSACTION_TYPE_EXPENSE, ignoreCase = true)) {
                    val txDate = DateUtils.formatDateFull(tx.timestamp)
                    if (txDate == todayKey) {
                        todayExpenses = todayExpenses.add(tx.amount)
                    } else if (txDate == yesterdayKey) {
                        yesterdayExpenses = yesterdayExpenses.add(tx.amount)
                    }
                }
            }
            Pair(todayExpenses, yesterdayExpenses)
        }
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Pair(BigDecimal.ZERO, BigDecimal.ZERO))

    val ledgerUiState: StateFlow<MainLedgerUiState> = combine(
        searchResultsState,
        totalCashState,
        _searchQuery
    ) { txList, totalCash, query ->
        MainLedgerUiState(
            transactions = txList,
            totalCash = totalCash,
            isSearching = query.isNotBlank(),
            isLoading = false
        )
    }.flowOn(Dispatchers.Default)
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MainLedgerUiState()
    )

    val monthlyLedgerState: StateFlow<List<MonthLedger>> = transactionsState
        .map { txList -> LedgerCalculator.computeMonthlyLedger(txList) }
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun saveSettings(settings: AppSettings) {
        if (_themeModeState.value != settings.themeMode) {
            _themeModeState.value = settings.themeMode
        }
        viewModelScope.launch(Dispatchers.IO) {
            settingsRepository.saveSettings(settings)
        }
    }

    fun addTransaction(type: String, category: String, amount: BigDecimal, description: String, timestamp: Long = System.currentTimeMillis() / 1000, presetId: String? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            val id = presetId ?: "tx_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}"
            val tx = TransactionDb(
                id = id,
                timestamp = timestamp,
                type = type,
                category = category,
                amount = amount,
                description = description
            )
            transactionsRepository.saveTransaction(tx)
        }
    }

    fun permanentlyDeleteDeletedItem(item: DeletedItemEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                trashRepository.removeDeletedItem(item)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                android.util.Log.e("FinanceViewModel", "تعذر إكمال العملية")
            }
        }
    }

    fun permanentlyDeleteMultipleItems(items: List<DeletedItemEntity>) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                items.forEach { trashRepository.removeDeletedItem(it) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                android.util.Log.e("FinanceViewModel", "تعذر إكمال العملية")
            }
        }
    }

    fun restoreMultipleItems(items: List<DeletedItemEntity>) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val context = app
                items.forEach { trashRepository.restoreDeletedItem(it) }
                sendUiEvent(UiEvent.ShowToast(R.string.toast_restore_success))
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                android.util.Log.e("FinanceViewModel", "تعذر إكمال العملية")
                sendUiEvent(UiEvent.ShowToast(R.string.toast_operation_failed))
            }
        }
    }

    fun restoreDeletedItem(item: DeletedItemEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val context = app
                trashRepository.restoreDeletedItem(item)
                sendUiEvent(UiEvent.ShowToast(R.string.toast_restore_success))
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                android.util.Log.e("FinanceViewModel", "تعذر إكمال العملية")
                sendUiEvent(UiEvent.ShowToast(R.string.toast_operation_failed))
            }
        }
    }

    fun restoreSingleTransactionFromBundle(itemId: String, txId: String, item: DeletedItemEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val context = app
                trashRepository.restoreSingleTransactionFromBundle(itemId, txId)
                sendUiEvent(UiEvent.ShowToast(R.string.toast_restore_success))
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                android.util.Log.e("FinanceViewModel", "تعذر إكمال العملية")
                sendUiEvent(UiEvent.ShowToast(R.string.toast_operation_failed))
            }
        }
    }

    fun updateAutoCleanupPeriod(period: String) {
        _autoCleanupPeriod.value = period
        viewModelScope.launch(Dispatchers.IO) {
            val context = app
            settingsRepository.saveSettings(settingsState.value.copy(trashAutoCleanupPeriod = period))

            com.smartledger.aldaftar.TrashCleanupWorker.schedulePeriodicCleanup(context, period)

            if (period != CLEANUP_PERIOD_NEVER) {
                try {
                    val ageInMillis = com.smartledger.aldaftar.TrashCleanupWorker.getPeriodDurationMillis(period)
                    if (ageInMillis > 0L) {
                        val thresholdTime = System.currentTimeMillis() - ageInMillis
                        val items = trashRepository.getAllDeletedItemsDirect()
                        val expiredItems = items.filter { it.deletedAt < thresholdTime }
                        expiredItems.forEach { item ->
                            trashRepository.removeDeletedItem(item)
                        }
                    }
                } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                    android.util.Log.e("FinanceViewModel", "تعذر إكمال العملية")
                }
            }
        }
    }

    fun cleanLedgerTrashItems() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val systemHabayeb = app.getString(R.string.source_system_habayeb)
                val allItems = trashRepository.getAllDeletedItemsDirect()
                val nonHabayebItems = allItems.filter {
                    it.sourceSystem != systemHabayeb && !it.originalTableName.startsWith(PREFIX_HABAYEB)
                }
                nonHabayebItems.forEach {
                    trashRepository.removeDeletedItem(it)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                android.util.Log.e("FinanceViewModel", "تعذر إكمال العملية")
            }
        }
    }

    fun emptyTrash() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val systemHabayeb = app.getString(R.string.source_system_habayeb)
                val allItems = trashRepository.getAllDeletedItemsDirect()
                val habayebItems = allItems.filter {
                    it.sourceSystem == systemHabayeb || it.originalTableName.startsWith(PREFIX_HABAYEB)
                }
                habayebItems.forEach {
                    trashRepository.removeDeletedItem(it)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                android.util.Log.e("FinanceViewModel", "تعذر إكمال العملية")
            }
        }
    }

    fun deleteTransaction(tx: TransactionDb) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                trashRepository.softDeleteTransactionToTrash(tx)
                transactionsRepository.deleteTransaction(tx)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                android.util.Log.e("FinanceViewModel", "تعذر إكمال العملية")
                sendUiEvent(UiEvent.ShowToast(R.string.toast_delete_failed))
            }
        }
    }

    fun deleteTransactionById(id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val tx = transactionsState.value.find { it.id == id }
                if (tx != null) {
                    trashRepository.softDeleteTransactionToTrash(tx)
                }
                transactionsRepository.deleteTransactionById(id)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                android.util.Log.e("FinanceViewModel", "تعذر إكمال العملية")
                sendUiEvent(UiEvent.ShowToast(R.string.toast_delete_failed))
            }
        }
    }

    fun deleteTransactionsBulk(ids: List<String>, bundleTitle: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val allTxs = transactionsState.value
                val idSet = ids.toSet()
                val toDelete = allTxs.filter { idSet.contains(it.id) }
                if (toDelete.isNotEmpty()) {
                    trashRepository.softDeleteTransactionBundleToTrash(toDelete, bundleTitle)
                    toDelete.forEach { transactionsRepository.deleteTransactionById(it.id) }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                android.util.Log.e("FinanceViewModel", "تعذر إكمال العملية")
                sendUiEvent(UiEvent.ShowToast(R.string.toast_delete_failed))
            }
        }
    }

    fun updateTransaction(tx: TransactionDb) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                transactionsRepository.saveTransaction(tx)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                android.util.Log.e("FinanceViewModel", "تعذر إكمال العملية")
                sendUiEvent(UiEvent.ShowToast(R.string.toast_operation_failed))
            }
        }
    }

    fun saveCommitment(name: String, targetAmount: BigDecimal, currentProgress: BigDecimal) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val count = commitmentsState.value.size
                val fc = FixedCommitment(name, targetAmount, currentProgress, count)
                commitmentsRepository.saveCommitment(fc)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                android.util.Log.e("FinanceViewModel", "تعذر إكمال العملية")
                sendUiEvent(UiEvent.ShowToast(R.string.toast_save_failed))
            }
        }
    }

    fun reorderCommitment(commitment: FixedCommitment, toPosition: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val currentList = commitmentsState.value.toMutableList()
                currentList.sortBy { it.orderIndex }

                val targetIndex = (toPosition - 1).coerceIn(0, currentList.size - 1)
                val currentIndex = currentList.indexOfFirst { it.name == commitment.name }

                if (currentIndex != -1 && currentIndex != targetIndex) {
                    val item = currentList.removeAt(currentIndex)
                    currentList.add(targetIndex, item)

                    val updatedList = currentList.mapIndexed { index, fc ->
                        fc.copy(orderIndex = index)
                    }

                    commitmentsRepository.updateCommitments(updatedList)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                android.util.Log.e("FinanceViewModel", "تعذر إكمال العملية")
            }
        }
    }

    fun deleteCommitment(name: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val oldFc = commitmentsState.value.find { it.name == name }
                if (oldFc != null) {
                    trashRepository.softDeleteCommitmentToTrash(oldFc)
                }
                commitmentsRepository.deleteCommitment(name)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                android.util.Log.e("FinanceViewModel", "تعذر إكمال العملية")
                sendUiEvent(UiEvent.ShowToast(R.string.toast_delete_failed))
            }
        }
    }

    fun saveCustomCategory(name: String, tabType: String, emoji: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                categoriesRepository.saveCustomCategory(CustomCategory(name = name, tabType = tabType, iconEmoji = emoji))
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                android.util.Log.e("FinanceViewModel", "تعذر إكمال العملية")
                sendUiEvent(UiEvent.ShowToast(R.string.toast_save_failed))
            }
        }
    }

    fun deleteCustomCategory(customCategory: CustomCategory) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                categoriesRepository.deleteCustomCategory(customCategory)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                android.util.Log.e("FinanceViewModel", "تعذر إكمال العملية")
                sendUiEvent(UiEvent.ShowToast(R.string.toast_delete_failed))
            }
        }
    }

    fun deleteAllData() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                maintenanceRepository.deleteAllData()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                android.util.Log.e("FinanceViewModel", "تعذر إكمال العملية")
            }
        }
    }
}
