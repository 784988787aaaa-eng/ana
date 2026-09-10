package com.smartledger.aldaftar.ui.viewmodel

import android.app.Application
import com.smartledger.aldaftar.data.license.LicenseRepository
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.smartledger.aldaftar.data.local.entities.*
import com.smartledger.aldaftar.domain.usecase.habayeb.*
import com.smartledger.aldaftar.ui.state.CustomerUiState
import com.smartledger.aldaftar.ui.state.CustomersUiState
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.math.BigDecimal

sealed interface HabayebUiEvent {
    data class ScrollToAccount(val accountId: String) : HabayebUiEvent
    object ResetScrollToTop : HabayebUiEvent
}

data class HabayebUiState(
    val customers: List<CustomerUiState> = emptyList(),
    val filteredCustomers: List<CustomerUiState> = emptyList(),
    val totalOwedByThem: BigDecimal = BigDecimal.ZERO,
    val totalOwedToThem: BigDecimal = BigDecimal.ZERO,
    val customCategories: List<CustomCategory> = emptyList(),
    val orderedCategories: List<String> = emptyList(),
    val categoryCounts: Map<String, Int> = emptyMap(),
    val closedCategoryName: String = "",
    val searchQuery: String = "",
    val selectedFilterTab: Int = 0,
    val financialSortMode: Int = 0,
    val historicalSortMode: Int = 0,
    val pinnedCustomerIds: Set<String> = emptySet(),
    val selectedCategory: String? = null,
    val selectedCustomerIds: List<String> = emptyList(),
    val activeCustomersCount: Int = 0
)

@OptIn(FlowPreview::class)
class HabayebFinanceViewModel(
    application: Application,
    private val licenseRepository: LicenseRepository,
    private val categoryUseCase: HabayebCategoryUseCase,
    private val habayebRepository: com.smartledger.aldaftar.data.repository.HabayebRepository,
    private val transactionsRepository: com.smartledger.aldaftar.data.repository.TransactionRepository,
    private val categoriesRepository: com.smartledger.aldaftar.data.repository.CategoryRepository,
    private val settingsRepository: com.smartledger.aldaftar.data.repository.SettingsRepository,
    private val recurringRepository: com.smartledger.aldaftar.data.repository.RecurringRepository,
    mutationRepository: com.smartledger.aldaftar.data.repository.HabayebMutationRepository,
    private val floatingUiRepository: com.smartledger.aldaftar.data.repository.FloatingUiPreferencesRepository
) : AndroidViewModel(application) {

    fun floatingAddState() = floatingUiRepository.add()
    fun saveFloatingAddState(state: com.smartledger.aldaftar.data.repository.FloatingAddState) = floatingUiRepository.saveAdd(state)

    fun isEligibleToCreate(): Boolean = licenseRepository.isEligibleToCreate()
    fun triggerLicensePrompt() = licenseRepository.triggerLicenseRequired()

    private val transactionUseCase = HabayebTransactionUseCase(habayebRepository, transactionsRepository, mutationRepository)

    private val _uiEventChannel = Channel<HabayebUiEvent>(Channel.BUFFERED)
    val uiEventFlow: Flow<HabayebUiEvent> = _uiEventChannel.receiveAsFlow()

    fun emitScrollToAccount(accountId: String) {
        _uiEventChannel.trySend(HabayebUiEvent.ScrollToAccount(accountId))
    }

    fun emitResetScrollToTop() {
        _uiEventChannel.trySend(HabayebUiEvent.ResetScrollToTop)
    }

    val searchQuery = MutableStateFlow("")
    val selectedFilterTab = MutableStateFlow(0)
    val financialSortMode = MutableStateFlow(0)
    val historicalSortMode = MutableStateFlow(1)
    val temporarilyHiddenCustomerIds = MutableStateFlow<Set<String>>(emptySet())
    val selectedCategoryFilter = MutableStateFlow<String?>(null)
    val selectedCustomerIdsState = MutableStateFlow<List<String>>(emptyList())

    val customerCategoryMapState: StateFlow<Map<String, String>> = categoryUseCase.categoryMapFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    private val selectedCategoryIdFlow: Flow<Int?> = combine(selectedCategoryFilter, categoriesRepository.customCategoriesFlow) { name, categories ->
        name?.let { selected -> categories.firstOrNull { it.name == selected }?.id }
    }

    val pinnedCustomerIds: StateFlow<Set<String>> = selectedCategoryIdFlow
        .flatMapLatest { categoryUseCase.pinnedCustomerIdsFlow(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    suspend fun recurringByOriginalTransaction(id: String) = recurringRepository.byOriginalTransaction(id)
    suspend fun saveRecurring(config: com.smartledger.aldaftar.domain.model.RecurringConfig) = recurringRepository.save(config)
    suspend fun deleteRecurring(id: String) = recurringRepository.delete(id)
    suspend fun deleteRecurringForTransaction(id: String) = recurringRepository.deleteForTransaction(id)
    suspend fun activeRecurringOriginalIds(customerId: String, existingIds: Set<String>): Set<String> = recurringRepository.all().filter { it.isActive && it.customerId == customerId && it.originalTxId in existingIds }.map { it.originalTxId }.toSet()

    fun processRecurringTransactions(onExecuted: (Int) -> Unit = {}) {
        viewModelScope.launch {
            val count = recurringRepository.executeDue()
            if (count > 0) onExecuted(count)
        }
    }
    init {
        viewModelScope.launch(Dispatchers.IO) {
            categoryUseCase.ensureClosedCategoryExists()
        }
    }

    val settingsState: StateFlow<AppSettings> = settingsRepository.settingsFlow
        .map { it ?: AppSettings() }
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppSettings())

    val habayebCustomersState: StateFlow<List<HabayebCustomer>> = habayebRepository.customersFlow
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val habayebTransactionsState: StateFlow<List<HabayebTransaction>> = habayebRepository.transactionsFlow
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _linkHabayebDebtsState = MutableStateFlow(false)
    val linkHabayebDebtsState = _linkHabayebDebtsState.asStateFlow()

    fun toggleLinkHabayebDebts(enabled: Boolean) {
        _linkHabayebDebtsState.value = enabled
        
    }

    val totalTransactionsCount: StateFlow<Int> = combine(
        transactionsRepository.getTransactionsCountFlow(), habayebRepository.getHabayebTransactionsCountFlow()
    ) { m, h -> m + h }
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)



    fun getTransactionsForCustomerFlow(customerId: String): Flow<List<HabayebTransaction>> =
        habayebRepository.getTransactionsForCustomerFlow(customerId)

    fun getInitialTransactionsForCustomer(customerId: String): List<HabayebTransaction> {
        val all = habayebTransactionsState.value
        if (all.isEmpty()) return emptyList()
        return all.filter { it.customerId == customerId }
    }

    fun getTransactionsForCustomerWithLimitFlow(customerId: String, limit: Int): Flow<List<HabayebTransaction>> =
        habayebRepository.getTransactionsForCustomerWithLimitFlow(customerId, limit)

    suspend fun getTransactionsForCustomerPaged(customerId: String, limit: Int, offset: Int): List<HabayebTransaction> =
        habayebRepository.getTransactionsForCustomerPaged(customerId, limit, offset)

    fun resetFiltersToDefault(resetCategory: Boolean = true) {
        searchQuery.value = ""
        selectedFilterTab.value = 0
        financialSortMode.value = 0
        historicalSortMode.value = 1
        if (resetCategory) {
            selectedCategoryFilter.value = null
        }
    }

    fun updateSelectedCustomerIds(ids: List<String>) { selectedCustomerIdsState.value = ids }
    fun updateSearchQuery(query: String) {
        searchQuery.value = query
        emitResetScrollToTop()
    }
    fun updateSelectedFilterTab(tab: Int) {
        selectedFilterTab.value = tab
        emitResetScrollToTop()
    }
    fun updateFinancialSortMode(mode: Int) {
        financialSortMode.value = mode
        emitResetScrollToTop()
    }
    fun updateHistoricalSortMode(mode: Int) {
        historicalSortMode.value = mode
        emitResetScrollToTop()
    }

    fun updateSelectedCategoryFilter(category: String?) {
        selectedCategoryFilter.value = category
        emitResetScrollToTop()
    }

    fun togglePinCustomer(customerId: String) {
        viewModelScope.launch {
            val isCurrentlyPinned = pinnedCustomerIds.value.contains(customerId)
            val categoryId = selectedCategoryFilter.value?.let { selected -> categoriesRepository.getAllCustomCategoriesDirect().firstOrNull { it.name == selected }?.id }
            val success = categoryUseCase.togglePinCustomer(customerId, categoryId)
            if (success && !isCurrentlyPinned) {
                emitScrollToAccount(customerId)
            }
        }
    }

    fun assignCategoryToCustomers(customerIds: List<String>, category: String?) {
        viewModelScope.launch { val categoryId = category?.let { selected -> categoriesRepository.getAllCustomCategoriesDirect().firstOrNull { it.name == selected }?.id }; categoryUseCase.assignCategoryToCustomers(customerIds, categoryId) }
    }

    fun getCustomerCategory(customerId: String): String? = customerCategoryMapState.value[customerId]

    val customCategoriesState: StateFlow<List<CustomCategory>> = categoriesRepository.customCategoriesFlow
        .map { list -> list.filter { !it.isSystemClosed } }
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val orderedCategoriesState: StateFlow<List<String>> = categoriesRepository.customCategoriesFlow
        .map { all -> all.sortedBy { it.displayOrder }.map { if (it.isSystemClosed) "CLOSED" else it.name } }
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), listOf("CLOSED"))

    val closedCategoryNameState: StateFlow<String> = categoriesRepository.customCategoriesFlow
        .map { all -> 
            all.find { it.isSystemClosed }?.name 
                ?: getApplication<Application>().getString(com.smartledger.aldaftar.R.string.category_system_closed)
        }
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 
            getApplication<Application>().getString(com.smartledger.aldaftar.R.string.category_system_closed))

    fun renameClosedCategory(newName: String) { viewModelScope.launch { categoryUseCase.renameClosedCategory(newName) } }
    fun saveCustomCategory(name: String) { viewModelScope.launch { categoryUseCase.saveCustomCategory(name) } }
    fun renameCustomCategory(category: CustomCategory, newName: String) { viewModelScope.launch { categoryUseCase.renameCustomCategory(category, newName) } }
    fun deleteCustomCategoryWithChoice(category: CustomCategory, deleteLinked: Boolean) { viewModelScope.launch { categoryUseCase.deleteCustomCategoryWithChoice(category, deleteLinked) } }
    fun moveCategoryLeft(categoryName: String) { viewModelScope.launch { val list = categoriesRepository.getAllCustomCategoriesDirect().sortedBy { it.displayOrder }; val id = list.firstOrNull { it.name == categoryName }?.id ?: return@launch; categoryUseCase.moveCategoryLeft(list.map { it.id }, id) } }
    fun moveCategoryRight(categoryName: String) { viewModelScope.launch { val list = categoriesRepository.getAllCustomCategoriesDirect().sortedBy { it.displayOrder }; val id = list.firstOrNull { it.name == categoryName }?.id ?: return@launch; categoryUseCase.moveCategoryRight(list.map { it.id }, id) } }
    fun reorderCategories(newList: List<String>) { viewModelScope.launch { val all = categoriesRepository.getAllCustomCategoriesDirect(); val ids = newList.mapNotNull { name -> all.firstOrNull { it.name == name }?.id }; categoryUseCase.reorderCategories(ids) } }

    val customersUiState: StateFlow<CustomersUiState> = combine(
        habayebRepository.customersFlow, habayebRepository.transactionsFlow, settingsState
    ) { customers, transactions, settings -> HabayebFinancialCalculator.calculateCustomersUiState(customers, transactions, settings) }
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CustomersUiState())

    private val filterGroup1Flow = combine(searchQuery, selectedFilterTab, financialSortMode, historicalSortMode) { q, t, f, h -> HabayebFilterGroup1(q, t, f, h) }
    private val filterGroup2Flow = combine(temporarilyHiddenCustomerIds, selectedCategoryFilter, pinnedCustomerIds) { hid, cat, pin -> HabayebFilterGroup2(hid, cat, pin) }
    private val filterParametersFlow = combine(filterGroup1Flow, filterGroup2Flow) { g1, g2 ->
        HabayebFilterParameters(g1.query, g1.tab, g1.finSort, g1.histSort, g2.hiddenIds, g2.selectedCat, g2.pinnedIds)
    }

    private val filteredResultFlow: Flow<FilteredResult> = combine(
        customersUiState,
        filterParametersFlow,
        categoryUseCase.categoryMapFlow
    ) { ui, params, categoryMap ->
        HabayebFinancialCalculator.calculateFilteredResult(ui, params, categoryMap)
    }.flowOn(Dispatchers.Default)

    private data class CategoryUiData(
        val customCategories: List<CustomCategory>,
        val orderedCategories: List<String>,
        val closedCategoryName: String
    )

    private val categoryUiDataFlow = combine(
        customCategoriesState,
        orderedCategoriesState,
        closedCategoryNameState
    ) { customCategories, orderedCategories, closedCategoryName ->
        CategoryUiData(customCategories, orderedCategories, closedCategoryName)
    }

    val uiState: StateFlow<HabayebUiState> = combine(
        filteredResultFlow,
        categoryUiDataFlow,
        filterParametersFlow,
        selectedCustomerIdsState
    ) { filteredRes, categoryData, filterParams, selectedIds ->
        HabayebUiState(
            customers = customersUiState.value.customers,
            filteredCustomers = filteredRes.filteredCustomers,
            totalOwedByThem = filteredRes.totalOwedByThem,
            totalOwedToThem = filteredRes.totalOwedToThem,
            customCategories = categoryData.customCategories,
            orderedCategories = categoryData.orderedCategories,
            categoryCounts = filteredRes.categoryCounts,
            closedCategoryName = categoryData.closedCategoryName,
            searchQuery = filterParams.query,
            selectedFilterTab = filterParams.tab,
            financialSortMode = filterParams.finSort,
            historicalSortMode = filterParams.histSort,
            pinnedCustomerIds = filterParams.pinnedIds,
            selectedCategory = filterParams.selectedCat,
            selectedCustomerIds = selectedIds,
            activeCustomersCount = filteredRes.activeCustomersCount
        )
    }
        .distinctUntilChanged()
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HabayebUiState())

    val filteredCustomersState: StateFlow<List<CustomerUiState>> = uiState
        .map { it.filteredCustomers }
        .distinctUntilChanged()
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val habayebOwedByThemTotalState: StateFlow<BigDecimal> = uiState
        .map { it.totalOwedByThem }
        .distinctUntilChanged()
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), BigDecimal.ZERO)

    val habayebOwedToThemTotalState: StateFlow<BigDecimal> = uiState
        .map { it.totalOwedToThem }
        .distinctUntilChanged()
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), BigDecimal.ZERO)

    val categoryCountsState: StateFlow<Map<String, Int>> = uiState
        .map { it.categoryCounts }
        .distinctUntilChanged()
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    suspend fun saveHabayebCustomer(
        customer: HabayebCustomer, initialAmount: BigDecimal, initialType: String,
        customTimestamp: Long = System.currentTimeMillis() / 1000, initialDetails: String = "",
        isForeign: Boolean = false, currencyCode: String = "DEFAULT", foreignAmount: BigDecimal = BigDecimal.ZERO,
        exchangeRate: BigDecimal = BigDecimal.ONE, isRateCalculated: Boolean = false, equivalentAmount: BigDecimal = BigDecimal.ZERO
    ) = withContext(Dispatchers.IO) {
        resetFiltersToDefault(resetCategory = true)

        val consumesTrial = initialAmount.compareTo(BigDecimal.ZERO) > 0
        if (!consumesTrial) {
            transactionUseCase.saveHabayebCustomer(
                customer, initialAmount, initialType, customTimestamp, initialDetails, isForeign, currencyCode,
                foreignAmount, exchangeRate, isRateCalculated, equivalentAmount, null, settingsState.value
            )
            return@withContext
        }
        val created = licenseRepository.runAuthorizedCreation {
            transactionUseCase.saveHabayebCustomer(
                customer, initialAmount, initialType, customTimestamp, initialDetails, isForeign, currencyCode,
                foreignAmount, exchangeRate, isRateCalculated, equivalentAmount, null, settingsState.value
            )
            true
        }
        if (created != true) return@withContext
        emitScrollToAccount(customer.id)
    }

    fun addHabayebTransaction(
        customerId: String, type: String, amount: BigDecimal, desc: String,
        timestamp: Long = System.currentTimeMillis() / 1000, editingTxId: String? = null, linkedMainTxId: String? = null,
        isForeign: Boolean = false, currencyCode: String = "DEFAULT", foreignAmount: BigDecimal = BigDecimal.ZERO,
        exchangeRate: BigDecimal = BigDecimal.ONE, isRateCalculated: Boolean = false, equivalentAmount: BigDecimal = BigDecimal.ZERO
    ) {
        viewModelScope.launch {
            resetFiltersToDefault(resetCategory = true)

            val consumesTrial = editingTxId == null
            if (!consumesTrial) {
                transactionUseCase.addHabayebTransaction(
                    customerId, type, amount, desc, timestamp, editingTxId, linkedMainTxId, isForeign, currencyCode,
                    foreignAmount, exchangeRate, isRateCalculated, equivalentAmount, settingsState.value.currencySymbol
                )
                emitScrollToAccount(customerId)
                return@launch
            }
            val created = licenseRepository.runAuthorizedCreation {
                transactionUseCase.addHabayebTransaction(
                    customerId, type, amount, desc, timestamp, editingTxId, linkedMainTxId, isForeign, currencyCode,
                    foreignAmount, exchangeRate, isRateCalculated, equivalentAmount, settingsState.value.currencySymbol
                )
                true
            }
            if (created != true) return@launch
            emitScrollToAccount(customerId)
        }
    }

    fun updateTransactionExchangeRate(txId: String, newRate: BigDecimal, calculateRate: Boolean) {
        viewModelScope.launch { transactionUseCase.updateTransactionExchangeRate(txId, newRate, calculateRate, settingsState.value.currencySymbol) }
    }

    fun revalueHistoricalTransactions(baseCurrencyCode: String, targetCurrencyCode: String, newRate: BigDecimal) {
        viewModelScope.launch { transactionUseCase.revalueHistoricalTransactions(baseCurrencyCode, targetCurrencyCode, newRate) }
    }

    fun updateHabayebCustomerName(customerId: String, newName: String) { viewModelScope.launch { transactionUseCase.updateCustomerName(customerId, newName) } }
    fun updateHabayebCustomer(customer: HabayebCustomer) { viewModelScope.launch { transactionUseCase.updateCustomer(customer) } }
    fun deleteHabayebCustomer(customerId: String) { viewModelScope.launch { transactionUseCase.deleteCustomer(customerId) } }
    fun deleteMultipleHabayebCustomers(customerIds: List<String>) { viewModelScope.launch { transactionUseCase.deleteMultipleCustomers(customerIds) } }
    fun deleteHabayebTransaction(txId: String, isEdit: Boolean = false) { viewModelScope.launch { transactionUseCase.deleteTransaction(txId, isEdit) } }
    fun deleteMultipleHabayebTransactions(txIds: List<String>) { viewModelScope.launch { transactionUseCase.deleteMultipleTransactions(txIds) } }
    fun saveSettings(settings: AppSettings) { viewModelScope.launch(Dispatchers.IO) { settingsRepository.saveSettings(settings) } }
    suspend fun transactionsForReport(customerIds: Collection<String>): Map<String, List<com.smartledger.aldaftar.data.local.entities.HabayebTransaction>> =
        customerIds.associateWith { id -> habayebRepository.getTransactionsForCustomerDirect(id) }

}
