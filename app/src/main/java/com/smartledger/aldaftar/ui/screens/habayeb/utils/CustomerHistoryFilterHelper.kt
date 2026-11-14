package com.smartledger.aldaftar.ui.screens.habayeb.utils

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import com.smartledger.aldaftar.R
import com.smartledger.aldaftar.data.local.entities.HabayebTransaction
import com.smartledger.aldaftar.domain.model.TransactionType
import com.smartledger.aldaftar.platform.contacts.StringUtils
import com.smartledger.aldaftar.platform.contacts.StringUtils.toWesternDigits
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.math.BigDecimal

private class IndexedTransactionEntry(
    val tx: HabayebTransaction,
    val normalizedDescription: String,
    val amountPlain: String,
    val foreignAmountPlain: String
)

private fun isDescendingByTimestamp(list: List<HabayebTransaction>): Boolean {
    for (i in 1 until list.size) {
        if (list[i - 1].timestamp < list[i].timestamp) return false
    }
    return true
}

private fun isAscendingByTimestamp(list: List<HabayebTransaction>): Boolean {
    for (i in 1 until list.size) {
        if (list[i - 1].timestamp > list[i].timestamp) return false
    }
    return true
}

@Composable
fun rememberFilteredCustomerTransactions(
    context: Context,
    allCustomerTxs: List<HabayebTransaction>,
    txSearchQuery: String,
    dateFilterMode: Int,
    customStartDate: Long?,
    customEndDate: Long?,
    typeFilterMode: Int,
    selectedCurrencyFilter: String?,
    currencySymbol: String,
    exchangeRatesJson: String?
): State<List<HabayebTransaction>> {
    val dateBoundaries = remember(dateFilterMode) {
        if (dateFilterMode == 0) return@remember longArrayOf(0, 0, 0, 0)
        val cal = java.util.Calendar.getInstance()
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
        cal.set(java.util.Calendar.MINUTE, 0)
        cal.set(java.util.Calendar.SECOND, 0)
        cal.set(java.util.Calendar.MILLISECOND, 0)
        val todayStart = cal.timeInMillis / 1000
        val todayEnd = todayStart + 86400

        cal.set(java.util.Calendar.DAY_OF_MONTH, 1)
        val monthStart = cal.timeInMillis / 1000
        cal.add(java.util.Calendar.MONTH, 1)
        val monthEnd = cal.timeInMillis / 1000

        longArrayOf(todayStart, todayEnd, monthStart, monthEnd)
    }

    val isSearchBlank = txSearchQuery.isBlank()
    val hasNoFilters = isSearchBlank && dateFilterMode == 0 && typeFilterMode == 0 && selectedCurrencyFilter == null

    val sortedDescending = remember(allCustomerTxs) {
        when {
            allCustomerTxs.isEmpty() -> emptyList()
            isDescendingByTimestamp(allCustomerTxs) -> allCustomerTxs
            isAscendingByTimestamp(allCustomerTxs) -> allCustomerTxs.asReversed()
            else -> allCustomerTxs.sortedByDescending { it.timestamp }
        }
    }

    if (hasNoFilters) {
        return rememberUpdatedState(sortedDescending)
    }

    val searchDebtStr = context.getString(R.string.customer_history_search_debt)
    val searchPaymentStr = context.getString(R.string.customer_history_search_payment)

    val precomputedSearchIndex = remember(sortedDescending) {
        ArrayList<IndexedTransactionEntry>(sortedDescending.size).apply {
            for (i in sortedDescending.indices) {
                val tx = sortedDescending[i]
                add(
                    IndexedTransactionEntry(
                        tx = tx,
                        normalizedDescription = StringUtils.normalizeArabic(tx.description),
                        amountPlain = tx.amount.stripTrailingZeros().toPlainString(),
                        foreignAmountPlain = if (tx.foreignAmount.compareTo(BigDecimal.ZERO) != 0) {
                            tx.foreignAmount.stripTrailingZeros().toPlainString()
                        } else {
                            ""
                        }
                    )
                )
            }
        }
    }

    return produceState<List<HabayebTransaction>>(
        initialValue = sortedDescending,
        sortedDescending,
        precomputedSearchIndex,
        txSearchQuery,
        dateFilterMode,
        customStartDate,
        customEndDate,
        typeFilterMode,
        selectedCurrencyFilter,
        currencySymbol,
        exchangeRatesJson,
        dateBoundaries
    ) {
        val filtered = withContext(Dispatchers.Default) {
            val todayStart = dateBoundaries[0]
            val todayEnd = dateBoundaries[1]
            val monthStart = dateBoundaries[2]
            val monthEnd = dateBoundaries[3]

            val normalizedQuery = if (!isSearchBlank) StringUtils.normalizeArabic(txSearchQuery) else ""
            val westernQuery = if (!isSearchBlank) txSearchQuery.toWesternDigits().trim() else ""
            val normalizedDebtStr = if (!isSearchBlank) StringUtils.normalizeArabic(searchDebtStr) else ""
            val normalizedPaymentStr = if (!isSearchBlank) StringUtils.normalizeArabic(searchPaymentStr) else ""

            val safeRatesJson = exchangeRatesJson ?: ""
            val customStartSec = (customStartDate ?: 0L) / 1000
            val customEndSec = if (customEndDate != null) (customEndDate / 1000) + 86400 else Long.MAX_VALUE

            val out = ArrayList<HabayebTransaction>(precomputedSearchIndex.size)
            for (i in precomputedSearchIndex.indices) {
                val entry = precomputedSearchIndex[i]
                val tx = entry.tx

                val matchesDate = when (dateFilterMode) {
                    1 -> tx.timestamp in todayStart..todayEnd
                    2 -> tx.timestamp in monthStart..monthEnd
                    3 -> tx.timestamp in customStartSec..customEndSec
                    else -> true
                }
                if (!matchesDate) continue

                val matchesType = when (typeFilterMode) {
                    1 -> tx.type == TransactionType.OWED_BY_THEM.value || tx.type == TransactionType.OWED_TO_THEM.value
                    2 -> tx.type == TransactionType.PAYMENT_BY_THEM.value || tx.type == TransactionType.PAYMENT_TO_THEM.value
                    else -> true
                }
                if (!matchesType) continue

                val matchesCurrency = if (selectedCurrencyFilter != null) {
                    val (txCurrency, _) = CurrencyConfig.getTransactionCurrencyAndAmountBigDecimal(tx, currencySymbol, safeRatesJson)
                    txCurrency == selectedCurrencyFilter
                } else {
                    true
                }
                if (!matchesCurrency) continue

                val matchesSearch = if (isSearchBlank) {
                    true
                } else {
                    val typeText = if (tx.type == TransactionType.OWED_BY_THEM.value) normalizedDebtStr else normalizedPaymentStr
                    entry.normalizedDescription.contains(normalizedQuery, ignoreCase = true) ||
                        (westernQuery.isNotEmpty() && entry.amountPlain.contains(westernQuery)) ||
                        (westernQuery.isNotEmpty() && entry.foreignAmountPlain.isNotEmpty() && entry.foreignAmountPlain.contains(westernQuery)) ||
                        typeText.contains(normalizedQuery, ignoreCase = true)
                }

                if (matchesSearch) {
                    out.add(tx)
                }
            }
            out
        }
        value = filtered
    }
}
