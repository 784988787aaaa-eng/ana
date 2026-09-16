package com.smartledger.aldaftar.domain.usecase.habayeb

import com.smartledger.aldaftar.data.local.entities.AppSettings
import com.smartledger.aldaftar.data.local.entities.HabayebCustomer
import com.smartledger.aldaftar.data.local.entities.HabayebTransaction
import com.smartledger.aldaftar.platform.contacts.StringUtils
import com.smartledger.aldaftar.ui.screens.habayeb.utils.CustomerHistoryCalculator
import com.smartledger.aldaftar.ui.screens.habayeb.utils.CurrencyConfig
import com.smartledger.aldaftar.ui.state.CustomerUiState
import com.smartledger.aldaftar.ui.state.CustomersUiState
import java.math.BigDecimal
import java.math.RoundingMode
import com.smartledger.aldaftar.ui.viewmodel.FinanceConstants


private const val CATEGORY_CLOSED = FinanceConstants.CATEGORY_CLOSED

data class HabayebFilterParameters(
    val query: String,
    val tab: Int,
    val finSort: Int,
    val histSort: Int,
    val hiddenIds: Set<String>,
    val selectedCat: String?,
    val pinnedIds: Set<String>
)

data class HabayebFilterGroup1(
    val query: String,
    val tab: Int,
    val finSort: Int,
    val histSort: Int
)

data class HabayebFilterGroup2(
    val hiddenIds: Set<String>,
    val selectedCat: String?,
    val pinnedIds: Set<String>
)

data class FilteredResult(
    val filteredCustomers: List<CustomerUiState>,
    val totalOwedByThem: BigDecimal,
    val totalOwedToThem: BigDecimal,
    val categoryCounts: Map<String, Int>,
    val activeCustomersCount: Int = 0
)

object HabayebFinancialCalculator {

    fun calculateCustomersUiState(
        customers: List<HabayebCustomer>,
        customerBalances: List<com.smartledger.aldaftar.data.local.CustomerCurrencyBalance>,
        settings: AppSettings
    ): CustomersUiState {
        val defaultCurrency = settings.currencySymbol
        val normDefaultCurrency = com.smartledger.aldaftar.ui.screens.habayeb.utils.CurrencyConfig.getBySymbol(defaultCurrency)?.symbol ?: defaultCurrency
        val balancesByCustomer = customerBalances.groupBy { it.customerId }

        var globalTotalOwedByThem = BigDecimal.ZERO
        var globalTotalOwedToThem = BigDecimal.ZERO

        val customerStates = ArrayList<CustomerUiState>(customers.size)
        for (customer in customers) {
            val custBalances = balancesByCustomer[customer.id] ?: emptyList()
            
            // Reconstruct netDebtBigDecimalMap from DB aggregations
            val netDebtBDMap = mutableMapOf<String, BigDecimal>()
            var maxTimestamp = customer.createdAt
            var totalTxs = 0
            
            for (bal in custBalances) {
                // If it's the default currency, or no exchange rates given, use netEquivalentAmount or netAmount
                // The actual logic is we use the aggregated netAmount.
                // Wait, netAmount is per currency.
                val rawCurr = bal.currencyCode.ifBlank { normDefaultCurrency }
                val curr = CurrencyConfig.getBySymbol(rawCurr)?.symbol ?: rawCurr
                val amount = bal.netAmount.setScale(4, RoundingMode.HALF_EVEN)
                netDebtBDMap[curr] = (netDebtBDMap[curr] ?: BigDecimal.ZERO).add(amount)
                
                if (bal.lastTimestamp > maxTimestamp) {
                    maxTimestamp = bal.lastTimestamp
                }
                totalTxs += bal.txCount
            }

            val defaultCurrencyTotal = netDebtBDMap[normDefaultCurrency] ?: BigDecimal.ZERO
            val defaultCurrencyTotalAbs = defaultCurrencyTotal.abs()
            
            val activeForeignDebts = if (netDebtBDMap.size > 1) {
                netDebtBDMap
                    .filterKeys { it != normDefaultCurrency }
                    .filterValues { bd -> bd.compareTo(BigDecimal.ZERO) != 0 }
            } else {
                emptyMap()
            }

            // Determine primary display currency and net debt
            val displayCurrency: String
            val displayNetDebt: BigDecimal
            
            if (defaultCurrencyTotal.compareTo(BigDecimal.ZERO) != 0) {
                displayCurrency = normDefaultCurrency
                displayNetDebt = defaultCurrencyTotal
            } else {
                val nonZeroForeignEntry = netDebtBDMap.entries.firstOrNull { it.key != normDefaultCurrency && it.value.compareTo(BigDecimal.ZERO) != 0 }
                if (nonZeroForeignEntry != null) {
                    displayCurrency = nonZeroForeignEntry.key
                    displayNetDebt = nonZeroForeignEntry.value
                } else {
                    displayCurrency = normDefaultCurrency
                    displayNetDebt = BigDecimal.ZERO
                }
            }

            val normalizedName = StringUtils.normalizeArabic(customer.name)

            val state = CustomerUiState(
                id = customer.id,
                name = customer.name,
                phone = customer.phone,
                notes = customer.notes,
                createdAt = customer.createdAt,
                totalTransactions = totalTxs,
                netDebt = defaultCurrencyTotal,
                displayNetDebt = displayNetDebt,
                displayCurrencySymbol = displayCurrency,
                lastTransactionTimestamp = maxTimestamp,
                originalCustomer = customer,
                foreignDebts = activeForeignDebts,
                defaultCurrencyTotal = defaultCurrencyTotal,
                normalizedName = normalizedName,
                defaultCurrencyTotalAbs = defaultCurrencyTotalAbs
            )
            customerStates.add(state)

            if (!state.isClosed) {
                val cmp = defaultCurrencyTotal.compareTo(BigDecimal.ZERO)
                if (cmp > 0) {
                    globalTotalOwedByThem = globalTotalOwedByThem.add(defaultCurrencyTotal)
                } else if (cmp < 0) {
                    globalTotalOwedToThem = globalTotalOwedToThem.add(defaultCurrencyTotalAbs)
                }
            }
        }

        return CustomersUiState(
            customers = customerStates,
            totalOwedByThem = globalTotalOwedByThem.setScale(4, RoundingMode.HALF_EVEN),
            totalOwedToThem = globalTotalOwedToThem.setScale(4, RoundingMode.HALF_EVEN),
            isLoading = false
        )
    }

    fun calculateFilteredResult(
        uiState: CustomersUiState,
        params: HabayebFilterParameters,
        categoryMap: Map<String, String>
    ): FilteredResult {
        val normalizedQuery = if (params.query.isNotEmpty()) StringUtils.normalizeArabic(params.query) else ""
        val counts = mutableMapOf<String, Int>()
        var closedCount = 0
        var activeCustomersCount = 0

        var owedByTotal = BigDecimal.ZERO
        var owedToTotal = BigDecimal.ZERO

        val baseFilteredList = ArrayList<CustomerUiState>(uiState.customers.size)
        val selectedCat = params.selectedCat
        val isQueryEmpty = normalizedQuery.isEmpty()

        for (customerUi in uiState.customers) {
            val isClosed = customerUi.isClosed
            val linkedCat = categoryMap[customerUi.id]

            if (isClosed) {
                closedCount++
            } else {
                activeCustomersCount++
                if (linkedCat != null) {
                    counts[linkedCat] = (counts[linkedCat] ?: 0) + 1
                }
            }

            val matchesSelectedCatForTotals = when (selectedCat) {
                null -> !isClosed
                CATEGORY_CLOSED -> isClosed
                else -> !isClosed && linkedCat == selectedCat
            }

            if (matchesSelectedCatForTotals) {
                val bdVal = customerUi.defaultCurrencyTotal
                val cmp = bdVal.compareTo(BigDecimal.ZERO)
                if (cmp > 0) {
                    owedByTotal = owedByTotal.add(bdVal)
                } else if (cmp < 0) {
                    owedToTotal = owedToTotal.add(customerUi.defaultCurrencyTotalAbs)
                }
            }

            if (params.hiddenIds.contains(customerUi.id)) continue

            val matchesTab = when (params.tab) {
                1 -> customerUi.defaultCurrencyTotal.compareTo(BigDecimal.ZERO) > 0
                2 -> customerUi.defaultCurrencyTotal.compareTo(BigDecimal.ZERO) < 0
                else -> true
            }
            if (!matchesTab) continue

            val matchesCategory = when (selectedCat) {
                null -> !isClosed
                CATEGORY_CLOSED -> isClosed
                else -> !isClosed && linkedCat == selectedCat
            }
            if (!matchesCategory) continue

            val matchesSearch = isQueryEmpty ||
                    customerUi.normalizedName.contains(normalizedQuery, ignoreCase = true) ||
                    customerUi.phone.contains(params.query, ignoreCase = true)
            if (!matchesSearch) continue

            baseFilteredList.add(customerUi)
        }

        counts[CATEGORY_CLOSED] = closedCount

        val finalFilteredList = if (params.pinnedIds.isEmpty()) {
            when {
                params.finSort == 1 -> baseFilteredList.sortedByDescending { it.defaultCurrencyTotalAbs }
                params.finSort == 2 -> baseFilteredList.sortedBy { it.defaultCurrencyTotalAbs }
                params.histSort == 2 -> baseFilteredList.sortedBy { it.lastTransactionTimestamp }
                else -> baseFilteredList.sortedByDescending { it.lastTransactionTimestamp }
            }
        } else {
            val (pinnedList, unpinnedList) = baseFilteredList.partition { params.pinnedIds.contains(it.id) }
            val sortedUnpinned = when {
                params.finSort == 1 -> unpinnedList.sortedByDescending { it.defaultCurrencyTotalAbs }
                params.finSort == 2 -> unpinnedList.sortedBy { it.defaultCurrencyTotalAbs }
                params.histSort == 2 -> unpinnedList.sortedBy { it.lastTransactionTimestamp }
                else -> unpinnedList.sortedByDescending { it.lastTransactionTimestamp }
            }
            pinnedList.sortedByDescending { it.lastTransactionTimestamp } + sortedUnpinned
        }

        return FilteredResult(
            filteredCustomers = finalFilteredList,
            totalOwedByThem = owedByTotal,
            totalOwedToThem = owedToTotal,
            categoryCounts = counts,
            activeCustomersCount = activeCustomersCount
        )
    }
}
