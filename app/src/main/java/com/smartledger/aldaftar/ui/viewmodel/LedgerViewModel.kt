package com.smartledger.aldaftar.ui.viewmodel

import android.app.Application
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.smartledger.aldaftar.R
import com.smartledger.aldaftar.data.local.entities.AppSettings
import com.smartledger.aldaftar.data.local.entities.CustomCategory
import com.smartledger.aldaftar.data.local.entities.TransactionDb
import com.smartledger.aldaftar.platform.contacts.StringUtils
import com.smartledger.aldaftar.domain.model.TransactionType
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.math.BigDecimal

sealed interface LedgerUiEvent {
    object ScrollToTop : LedgerUiEvent
    data class ScrollToRecord(val recordId: String) : LedgerUiEvent
}

class LedgerViewModel(
    application: Application,
    private val settingsRepository: com.smartledger.aldaftar.data.repository.SettingsRepository,
    private val transactionsRepository: com.smartledger.aldaftar.data.repository.TransactionRepository,
    private val categoriesRepository: com.smartledger.aldaftar.data.repository.CategoryRepository,
    private val trashRepository: com.smartledger.aldaftar.data.repository.TrashRepository
) : AndroidViewModel(application) {

    private val _uiEventChannel = Channel<LedgerUiEvent>(Channel.BUFFERED)
    val uiEventFlow = _uiEventChannel.receiveAsFlow()

    fun emitScrollToTop() {
        viewModelScope.launch { _uiEventChannel.send(LedgerUiEvent.ScrollToTop) }
    }

    val settingsState: StateFlow<AppSettings> = settingsRepository.settingsFlow
        .map { it ?: AppSettings() }
        .stateIn(viewModelScope, SharingStarted.Lazily, AppSettings())

    val transactionsState: StateFlow<List<TransactionDb>> = transactionsRepository.transactionsFlow
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val customCategoriesState: StateFlow<List<CustomCategory>> = categoriesRepository.customCategoriesFlow
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val searchQuery = MutableStateFlow("")

    val searchResultsState: StateFlow<List<TransactionDb>> = combine(
        transactionsState,
        searchQuery
    ) { transactions, query ->
        if (query.isBlank()) {
            emptyList()
        } else {
            val normalizedQuery = StringUtils.normalizeArabic(query, getApplication<Application>())
            transactions.filter { tx ->
                StringUtils.normalizeArabic(tx.description, getApplication<Application>()).contains(normalizedQuery, ignoreCase = true)
            }.sortedByDescending { it.timestamp }
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val selectedYear = MutableStateFlow<Int?>(null)
    val selectedMonth = MutableStateFlow<Int?>(null)
    val selectedCategory = MutableStateFlow<String?>(null)

    val filteredTransactionsState: StateFlow<List<TransactionDb>> = combine(
        transactionsState,
        selectedYear,
        selectedMonth,
        selectedCategory
    ) { transactions, year, month, category ->
        val calendar = java.util.Calendar.getInstance()
        transactions.filter { tx ->
            calendar.timeInMillis = tx.timestamp * 1000
            val txYear = calendar.get(java.util.Calendar.YEAR)
            val txMonth = calendar.get(java.util.Calendar.MONTH) + 1

            val yearMatches = year == null || txYear == year
            val monthMatches = month == null || txMonth == month
            val categoryMatches = category == null || tx.category.trim() == category.trim()

            yearMatches && monthMatches && categoryMatches
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val totalIncomeState: StateFlow<BigDecimal> = filteredTransactionsState
        .map { txList ->
            var sum = BigDecimal.ZERO
            for (i in txList.indices) {
                val tx = txList[i]
                val txType = TransactionType.fromValue(tx.type)
                if (txType == TransactionType.INCOME || tx.type.equals("INCOME", ignoreCase = true)) {
                    sum = sum.add(tx.amount)
                }
            }
            sum
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, BigDecimal.ZERO)

    val totalExpenseState: StateFlow<BigDecimal> = filteredTransactionsState
        .map { txList ->
            var sum = BigDecimal.ZERO
            for (i in txList.indices) {
                val tx = txList[i]
                val txType = TransactionType.fromValue(tx.type)
                if (txType == TransactionType.EXPENSE || tx.type.equals("EXPENSE", ignoreCase = true)) {
                    sum = sum.add(tx.amount)
                }
            }
            sum
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, BigDecimal.ZERO)

    val netBalanceState: StateFlow<BigDecimal> = combine(totalIncomeState, totalExpenseState) { income, expense ->
        income.subtract(expense)
    }.stateIn(viewModelScope, SharingStarted.Lazily, BigDecimal.ZERO)


    fun addTransaction(
        type: String,
        category: String,
        amount: BigDecimal,
        description: String,
        timestamp: Long = System.currentTimeMillis() / 1000,
        presetId: String? = null
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val id = presetId ?: "tx_${System.currentTimeMillis()}_${java.util.UUID.randomUUID().toString().take(6)}"
                val tx = TransactionDb(
                    id = id,
                    timestamp = timestamp,
                    type = type,
                    category = category,
                    amount = amount,
                    description = description
                )
                transactionsRepository.saveTransaction(tx)
                _uiEventChannel.send(LedgerUiEvent.ScrollToTop)
                com.smartledger.aldaftar.ui.helper.VibrationHelper.triggerSuccessVibration(getApplication())
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e("LedgerViewModel", "Error in addTransaction: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        getApplication(),
                        getApplication<Application>().getString(R.string.toast_save_failed),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    fun updateTransaction(tx: TransactionDb) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                transactionsRepository.saveTransaction(tx)
                _uiEventChannel.send(LedgerUiEvent.ScrollToTop)
                com.smartledger.aldaftar.ui.helper.VibrationHelper.triggerSuccessVibration(getApplication())
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e("LedgerViewModel", "Error in updateTransaction: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        getApplication(),
                        getApplication<Application>().getString(R.string.toast_save_failed),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    fun deleteTransaction(tx: TransactionDb) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                trashRepository.softDeleteTransactionToTrash(tx)
                transactionsRepository.deleteTransaction(tx)
                com.smartledger.aldaftar.ui.helper.VibrationHelper.triggerDeleteVibration(getApplication())
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e("LedgerViewModel", "Error in deleteTransaction: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        getApplication(),
                        getApplication<Application>().getString(R.string.toast_delete_failed),
                        Toast.LENGTH_SHORT
                    ).show()
                }
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
                com.smartledger.aldaftar.ui.helper.VibrationHelper.triggerDeleteVibration(getApplication())
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e("LedgerViewModel", "Error in deleteTransactionById: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        getApplication(),
                        getApplication<Application>().getString(R.string.toast_delete_failed),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    fun deleteTransactionsBulk(ids: List<String>, bundleTitle: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val allTxs = transactionsState.value
                val toDelete = allTxs.filter { ids.contains(it.id) }
                if (toDelete.isNotEmpty()) {
                    trashRepository.softDeleteTransactionBundleToTrash(toDelete, bundleTitle)
                    toDelete.forEach { transactionsRepository.deleteTransactionById(it.id) }
                    com.smartledger.aldaftar.ui.helper.VibrationHelper.triggerDeleteVibration(getApplication())
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e("LedgerViewModel", "Error in deleteTransactionsBulk: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        getApplication(),
                        getApplication<Application>().getString(R.string.toast_delete_failed),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }


    fun saveCustomCategory(name: String, tabType: String, emoji: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                categoriesRepository.saveCustomCategory(CustomCategory(name = name, tabType = tabType, iconEmoji = emoji))
                com.smartledger.aldaftar.ui.helper.VibrationHelper.triggerSuccessVibration(getApplication())
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e("LedgerViewModel", "Error in saveCustomCategory: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        getApplication(),
                        getApplication<Application>().getString(R.string.toast_save_failed),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    fun deleteCustomCategory(customCategory: CustomCategory) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                categoriesRepository.deleteCustomCategory(customCategory)
                com.smartledger.aldaftar.ui.helper.VibrationHelper.triggerDeleteVibration(getApplication())
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e("LedgerViewModel", "Error in deleteCustomCategory: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        getApplication(),
                        getApplication<Application>().getString(R.string.toast_delete_failed),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }


    fun selectYear(year: Int?) {
        selectedYear.value = year
    }

    fun selectMonth(month: Int?) {
        selectedMonth.value = month
    }

    fun selectCategory(category: String?) {
        selectedCategory.value = category
    }

    fun clearFilters() {
        selectedYear.value = null
        selectedMonth.value = null
        selectedCategory.value = null
    }
}
