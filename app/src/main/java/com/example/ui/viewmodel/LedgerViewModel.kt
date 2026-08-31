/*
 * ╔══════════════════════════════════════════════════════════════════════╗
 * ║ التوثيق العربي الهندسي — MASTERCLASS DOCUMENTATION                 ║
 * ╚══════════════════════════════════════════════════════════════════════╝
 *
 * الملف: app/src/main/java/com/example/ui/viewmodel/LedgerViewModel.kt
 * القطاع المعماري: ViewModels & UI State.
 *
 * الوصف المعماري:
 * مدير حالة دفتر الحسابات؛ يحول أحداث المستخدم ونتائج طبقة البيانات إلى حالة قابلة للرسم في Compose.
 *
 * الرؤية التعليمية والبصرية:
 * تخيل شاشة الهاتف أثناء تفاعل المستخدم: يضغط على زر أو يغيّر قيمة،
 * فتتولد إشارة، ثم تُعالج في طبقة الحالة، ثم تتغير الحالة التي تقرأها
 * Compose لإعادة رسم الشاشة. هذا الملف يقع في تلك السلسلة ويجب قراءته
 * باعتباره عقداً بين «ما فعله المستخدم» و«ما تراه الشاشة».
 *
 * قاعدة الثبات البرمجي:
 * النص التنفيذي الأصلي محفوظ حرفياً بعد هذا الرأس. الإضافات هنا توثيقية
 * فقط ولا تستبدل أي تعليمة أو اسماً أو قيمة أو منطقاً تنفيذياً.
 */

// --- الفهرس التوثيقي للعناصر البرمجية ---
// السطر 23: sealed interface LedgerUiEvent — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 24: object ScrollToTop — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 25: data class ScrollToRecord — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 40: class LedgerViewModel — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 42: private val database — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 43: private val transactionDao — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 44: private val customCategoryDao — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 45: private val settingsDao — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 46: private val repository — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 49: private val _uiEventChannel — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 50: val uiEventFlow — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 52: fun emitScrollToTop — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 57: val settingsState — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 61: val transactionsState — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 64: val customCategoriesState — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 68: val searchQuery — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 70: val searchResultsState — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 77: val normalizedQuery — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 85: val selectedYear — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 86: val selectedMonth — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 87: val selectedCategory — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 90: val filteredTransactionsState — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 96: val calendar — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 99: val txYear — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 100: val txMonth — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 102: val yearMatches — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 103: val monthMatches — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 104: val categoryMatches — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 111: val totalIncomeState — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 113: var sum — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 115: val tx — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 116: val txType — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 125: val totalExpenseState — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 127: var sum — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 129: val tx — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 130: val txType — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 139: val netBalanceState — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 145: fun addTransaction — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 165: val id — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 166: val tx — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 190: fun updateTransaction — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 209: fun deleteTransaction — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 228: fun deleteTransactionById — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 231: val tx — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 250: fun deleteTransactionsBulk — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 253: val allTxs — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 254: val toDelete — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 275: fun saveCustomCategory — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 293: fun deleteCustomCategory — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 313: fun selectYear — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 317: fun selectMonth — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 321: fun selectCategory — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 325: fun clearFilters — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// --- نهاية الفهرس التوثيقي ---

package com.example.ui.viewmodel

import android.app.Application
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.R
import com.example.data.local.AppDatabase
import com.example.data.local.entities.AppSettings
import com.example.data.local.entities.CustomCategory
import com.example.data.local.entities.TransactionDb
import com.example.data.repository.FinanceRepository
import com.example.domain.StringUtils
import com.example.domain.model.TransactionType
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.math.BigDecimal

sealed interface LedgerUiEvent {
    object ScrollToTop : LedgerUiEvent
    data class ScrollToRecord(val recordId: String) : LedgerUiEvent
}

/**
 * LedgerViewModel handles all of the Daily Ledger and General Accounting logic,
 * cleanly isolated from the monolithic FinanceViewModel.
 *
 * It manages:
 * - Dynamic year/month/category filtered StateFlows of transactions.
 * - Reactive and leak-free balance calculations (Total Income, Total Expense, Net Balance).
 * - Full Arabic character search normalization via centralized StringUtils.
 * - Thread-safe insert, update, delete, and soft delete transactions via FinanceRepository.
 * - Custom category creation and removal.
 * - Dynamic mapping of error states to localized resource IDs with zero hardcoded strings.
 */
class LedgerViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val transactionDao = database.transactionDao()
    private val customCategoryDao = database.customCategoryDao()
    private val settingsDao = database.settingsDao()
    private val repository = FinanceRepository(database, application)

    // --- UI Event Channel ---
    private val _uiEventChannel = Channel<LedgerUiEvent>(Channel.BUFFERED)
    val uiEventFlow = _uiEventChannel.receiveAsFlow()

    fun emitScrollToTop() {
        viewModelScope.launch { _uiEventChannel.send(LedgerUiEvent.ScrollToTop) }
    }

    // --- Core Database Flows ---
    val settingsState: StateFlow<AppSettings> = settingsDao.getSettingsFlow()
        .map { it ?: AppSettings() }
        .stateIn(viewModelScope, SharingStarted.Lazily, AppSettings())

    val transactionsState: StateFlow<List<TransactionDb>> = transactionDao.getAllTransactionsFlow()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val customCategoriesState: StateFlow<List<CustomCategory>> = customCategoryDao.getAllCustomCategoriesFlow()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // --- Search Query and Normalized Character Processing ---
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

    // --- Interactive Filtering States ---
    val selectedYear = MutableStateFlow<Int?>(null)
    val selectedMonth = MutableStateFlow<Int?>(null)
    val selectedCategory = MutableStateFlow<String?>(null)

    // Combined filtered transactions stream
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

    // --- General Balance and Dynamic Accounting Calculations ---
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

    // --- Thread-Safe Core Ledger Mutations (IO-Bound) ---

    fun addTransaction(
        type: String,
        category: String,
        amount: Double,
        description: String,
        timestamp: Long = System.currentTimeMillis() / 1000,
        presetId: String? = null
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (repository.isTrialExpiredDirect()) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            getApplication(),
                            getApplication<Application>().getString(R.string.licensing_dialog_desc),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    return@launch
                }
                val id = presetId ?: "tx_${System.currentTimeMillis()}_${java.util.UUID.randomUUID().toString().take(6)}"
                val tx = TransactionDb(
                    id = id,
                    timestamp = timestamp,
                    type = type,
                    category = category,
                    amount = BigDecimal(amount.toString()),
                    description = description
                )
                repository.saveTransaction(tx)
                _uiEventChannel.send(LedgerUiEvent.ScrollToTop)
                com.example.ui.helper.VibrationHelper.triggerSuccessVibration(getApplication())
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
                transactionDao.insertTransaction(tx)
                _uiEventChannel.send(LedgerUiEvent.ScrollToTop)
                com.example.ui.helper.VibrationHelper.triggerSuccessVibration(getApplication())
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
                repository.softDeleteTransactionToTrash(tx)
                transactionDao.deleteTransaction(tx)
                com.example.ui.helper.VibrationHelper.triggerDeleteVibration(getApplication())
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
                    repository.softDeleteTransactionToTrash(tx)
                }
                transactionDao.deleteTransactionById(id)
                com.example.ui.helper.VibrationHelper.triggerDeleteVibration(getApplication())
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
                    repository.softDeleteTransactionBundleToTrash(toDelete, bundleTitle)
                    toDelete.forEach { transactionDao.deleteTransactionById(it.id) }
                    com.example.ui.helper.VibrationHelper.triggerDeleteVibration(getApplication())
                }
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

    // --- Category Actions ---

    fun saveCustomCategory(name: String, tabType: String, emoji: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                customCategoryDao.insertCategory(CustomCategory(name = name, tabType = tabType, iconEmoji = emoji))
                com.example.ui.helper.VibrationHelper.triggerSuccessVibration(getApplication())
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
                customCategoryDao.deleteCategory(customCategory)
                com.example.ui.helper.VibrationHelper.triggerDeleteVibration(getApplication())
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

    // --- Interactive Filtering Actions ---

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


/*
 * --- ملاحظات وتوصيات المعمارية البرمجية ---
 * 1) يجب أن تبقى ViewModel طبقة تنسيق للحالة والأحداث، لا مستودعاً لقواعد
 *    المجال المالية التي ينبغي أن تعيش في طبقاتها المتخصصة.
 * 2) يوصى مستقبلاً بمراجعة دورة حياة كل Coroutine/Flow والتأكد من ارتباطها
 *    بـ viewModelScope أو نطاقها المقصود لمنع التسرب أو العمل بعد زوال الشاشة.
 * 3) عند تعديل UiState يجب الحفاظ على دلالة الحالات الانتقالية مثل التحميل،
 *    النجاح، الخطأ، والفراغ حتى لا تظهر واجهة مضللة للمستخدم.
 * 4) أي تغيير في الأحداث أو العقود العامة يجب أن يرافقه Regression Test
 *    يثبت أن التفاعل الحالي في Compose لم يتغير.
 * 5) الحسابات المالية والـ BigDecimal يجب أن تبقى في مسارها الدقيق، وألا
 *    تتحول إلى Double/Float داخل طبقة العرض إلا بقرار موثق وصريح.
 * 6) هذه التوصيات مرجعية مستقبلية فقط ولا تمثل أي تغيير في التنفيذ الحالي.
 */
