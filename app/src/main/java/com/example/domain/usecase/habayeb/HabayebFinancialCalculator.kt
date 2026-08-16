package com.example.domain.usecase.habayeb

import android.content.SharedPreferences
import com.example.data.local.entities.AppSettings
import com.example.data.local.entities.HabayebCustomer
import com.example.data.local.entities.HabayebTransaction
import com.example.domain.StringUtils
import com.example.ui.screens.habayeb.utils.CustomerHistoryCalculator
import com.example.ui.state.CustomerUiState
import com.example.ui.state.CustomersUiState
import java.math.BigDecimal
import java.math.RoundingMode

private const val PREFIX_CAT_LINK = "CAT_LINK_"
private const val CATEGORY_CLOSED = "CLOSED"

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
    val categoryCounts: Map<String, Int>
)

object HabayebFinancialCalculator {

    fun calculateCustomersUiState(
        customers: List<HabayebCustomer>,
        allTransactions: List<HabayebTransaction>,
        settings: AppSettings
    ): CustomersUiState {
        val defaultCurrency = settings.currencySymbol
        val normDefaultCurrency = com.example.ui.screens.habayeb.utils.CurrencyConfig.getBySymbol(defaultCurrency)?.symbol ?: defaultCurrency
        val transactionsByCustomer = allTransactions.groupBy { it.customerId }

        var globalTotalOwedByThem = BigDecimal.ZERO
        var globalTotalOwedToThem = BigDecimal.ZERO

        val customerStates = ArrayList<CustomerUiState>(customers.size)
        for (customer in customers) {
            val custTxs = transactionsByCustomer[customer.id] ?: emptyList()
            val calcResult = CustomerHistoryCalculator.calculate(
                custTxs,
                defaultCurrency,
                settings.exchangeRatesJson
            )

            val defaultCurrencyTotal = calcResult.netDebtBigDecimalMap[normDefaultCurrency] ?: BigDecimal.ZERO
            val activeForeignDebts = if (calcResult.netDebtBigDecimalMap.size > 1) {
                calcResult.netDebtBigDecimalMap
                    .filterKeys { it != normDefaultCurrency }
                    .filterValues { bd -> bd.setScale(4, RoundingMode.HALF_EVEN).compareTo(BigDecimal.ZERO) != 0 }
            } else {
                emptyMap()
            }

            val displayCurrency = calcResult.primaryDisplayCurrency
            val displayNetDebt = calcResult.netDebt
            val lastTxTime = custTxs.maxOfOrNull { it.timestamp } ?: customer.createdAt

            val state = CustomerUiState(
                id = customer.id,
                name = customer.name,
                phone = customer.phone,
                notes = customer.notes,
                createdAt = customer.createdAt,
                totalTransactions = custTxs.size,
                netDebt = defaultCurrencyTotal.toDouble(),
                displayNetDebt = displayNetDebt,
                displayCurrencySymbol = displayCurrency,
                lastTransactionTimestamp = lastTxTime,
                originalCustomer = customer,
                foreignDebts = activeForeignDebts,
                defaultCurrencyTotal = defaultCurrencyTotal
            )
            customerStates.add(state)

            if (!state.isClosed) {
                val bdVal = state.defaultCurrencyTotal
                val cmp = bdVal.compareTo(BigDecimal.ZERO)
                if (cmp > 0) {
                    globalTotalOwedByThem = globalTotalOwedByThem.add(bdVal)
                } else if (cmp < 0) {
                    globalTotalOwedToThem = globalTotalOwedToThem.add(bdVal.abs())
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

    fun extractCategoryMap(sharedPrefs: SharedPreferences): Map<String, String> {
        val map = mutableMapOf<String, String>()
        sharedPrefs.all.forEach { (key, value) ->
            if (key.startsWith(PREFIX_CAT_LINK) && value is String) {
                map[key.removePrefix(PREFIX_CAT_LINK)] = value
            }
        }
        return map
    }

    fun calculateFilteredResult(
        uiState: CustomersUiState,
        params: HabayebFilterParameters,
        sharedPrefs: SharedPreferences
    ): FilteredResult {
        return calculateFilteredResult(uiState, params, extractCategoryMap(sharedPrefs))
    }

    fun calculateFilteredResult(
        uiState: CustomersUiState,
        params: HabayebFilterParameters,
        categoryMap: Map<String, String>
    ): FilteredResult {
        val normalizedQuery = StringUtils.normalizeArabic(params.query)
        val counts = mutableMapOf<String, Int>()
        var closedCount = 0

        var owedByTotal = BigDecimal.ZERO
        var owedToTotal = BigDecimal.ZERO

        val baseFilteredList = ArrayList<CustomerUiState>()

        for (customerUi in uiState.customers) {
            val isClosed = customerUi.isClosed
            val linkedCat = categoryMap[customerUi.id]

            if (isClosed) {
                closedCount++
            } else if (linkedCat != null) {
                counts[linkedCat] = (counts[linkedCat] ?: 0) + 1
            }

            val matchesSelectedCatForTotals = when (params.selectedCat) {
                null -> !isClosed
                CATEGORY_CLOSED -> isClosed
                else -> !isClosed && linkedCat == params.selectedCat
            }

            if (matchesSelectedCatForTotals) {
                val bdVal = customerUi.defaultCurrencyTotal
                if (bdVal.compareTo(BigDecimal.ZERO) > 0) {
                    owedByTotal = owedByTotal.add(bdVal)
                } else if (bdVal.compareTo(BigDecimal.ZERO) < 0) {
                    owedToTotal = owedToTotal.add(bdVal.abs())
                }
            }

            if (params.hiddenIds.contains(customerUi.id)) continue

            val matchesTab = when (params.tab) {
                1 -> customerUi.defaultCurrencyTotal.compareTo(BigDecimal.ZERO) > 0
                2 -> customerUi.defaultCurrencyTotal.compareTo(BigDecimal.ZERO) < 0
                else -> true
            }
            if (!matchesTab) continue

            val matchesCategory = when (params.selectedCat) {
                null -> !isClosed
                CATEGORY_CLOSED -> isClosed
                else -> !isClosed && linkedCat == params.selectedCat
            }
            if (!matchesCategory) continue

            val normalizedName = StringUtils.normalizeArabic(customerUi.name)
            val matchesSearch = params.query.isEmpty() ||
                    normalizedName.contains(normalizedQuery, ignoreCase = true) ||
                    customerUi.phone.contains(params.query, ignoreCase = true)
            if (!matchesSearch) continue

            baseFilteredList.add(customerUi)
        }

        counts[CATEGORY_CLOSED] = closedCount

        val (pinnedList, unpinnedList) = baseFilteredList.partition { params.pinnedIds.contains(it.id) }

        val sortedUnpinned = when {
            params.finSort != 0 -> {
                if (params.finSort == 1) unpinnedList.sortedByDescending { it.defaultCurrencyTotal.abs() }
                else unpinnedList.sortedBy { it.defaultCurrencyTotal.abs() }
            }
            params.histSort != 0 -> {
                if (params.histSort == 1) unpinnedList.sortedByDescending { it.lastTransactionTimestamp }
                else unpinnedList.sortedBy { it.lastTransactionTimestamp }
            }
            else -> unpinnedList.sortedByDescending { it.lastTransactionTimestamp }
        }

        val finalFilteredList = pinnedList.sortedByDescending { it.lastTransactionTimestamp } + sortedUnpinned

        return FilteredResult(
            filteredCustomers = finalFilteredList,
            totalOwedByThem = owedByTotal,
            totalOwedToThem = owedToTotal,
            categoryCounts = counts
        )
    }
}

