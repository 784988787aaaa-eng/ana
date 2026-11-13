package com.smartledger.aldaftar.ui.screens.habayeb.utils

import com.smartledger.aldaftar.data.local.entities.HabayebTransaction
import com.smartledger.aldaftar.domain.model.FinancialPolicy
import com.smartledger.aldaftar.domain.model.TransactionType
import java.math.BigDecimal

@androidx.compose.runtime.Immutable
data class CustomerSummaryResult(
    val netDebtBigDecimalMap: Map<String, BigDecimal>,
    val primaryDisplayCurrency: String,
    val netDebt: BigDecimal,
    val lastTimestamp: Long
)

@androidx.compose.runtime.Immutable
data class CustomerHistoryCalculationResult(
    val currencyKeys: List<String>,
    val owedByThemMap: Map<String, BigDecimal>,
    val paymentByThemMap: Map<String, BigDecimal>,
    val owedToThemMap: Map<String, BigDecimal>,
    val paymentToThemMap: Map<String, BigDecimal>,
    val netDebtMap: Map<String, BigDecimal>,
    val runningBalances: Map<String, BigDecimal>,
    val txSequenceNumbers: Map<String, Int>,
    val primaryDisplayCurrency: String,
    val netDebt: BigDecimal,
    val netDebtBigDecimalMap: Map<String, BigDecimal> = emptyMap(),
    val owedByThemBDMap: Map<String, BigDecimal> = emptyMap(),
    val paymentByThemBDMap: Map<String, BigDecimal> = emptyMap(),
    val owedToThemBDMap: Map<String, BigDecimal> = emptyMap(),
    val paymentToThemBDMap: Map<String, BigDecimal> = emptyMap()
) {
}

object CustomerHistoryCalculator {
    fun calculate(
        allCustomerTxs: List<HabayebTransaction>,
        currencySymbol: String,
        exchangeRatesJson: String?
    ): CustomerHistoryCalculationResult {
        val normDefaultCurrency = FinancialPolicy.canonicalCurrencySymbol(currencySymbol)
        val safeRatesJson = exchangeRatesJson ?: ""
        val totalCount = allCustomerTxs.size

        val chronological = if (totalCount <= 1) allCustomerTxs else allCustomerTxs.sortedWith(
            compareBy<HabayebTransaction> { it.timestamp }.thenBy { it.id }
        )

        val owedByThemBD = HashMap<String, BigDecimal>(4)
        val paymentByThemBD = HashMap<String, BigDecimal>(4)
        val owedToThemBD = HashMap<String, BigDecimal>(4)
        val paymentToThemBD = HashMap<String, BigDecimal>(4)

        val currencySet = LinkedHashSet<String>(4)
        currencySet.add(normDefaultCurrency)

        val balancesMap = HashMap<String, BigDecimal>(totalCount)
        val currentBalBDMap = HashMap<String, BigDecimal>(4)
        val txSequenceNumbers = HashMap<String, Int>(totalCount)

        var seq = 1
        for (tx in chronological) {
            val (rawTxCurrency, bdAmount) = CurrencyConfig.getTransactionCurrencyAndAmountBigDecimal(tx, normDefaultCurrency, safeRatesJson)
            val txCurrency = FinancialPolicy.canonicalCurrencySymbol(rawTxCurrency)
            currencySet.add(txCurrency)

            val safeBd = FinancialPolicy.normalize(bdAmount)
            val txType = TransactionType.fromValue(tx.type)

            when (txType) {
                TransactionType.OWED_BY_THEM -> owedByThemBD[txCurrency] = (owedByThemBD[txCurrency] ?: BigDecimal.ZERO).add(safeBd)
                TransactionType.PAYMENT_BY_THEM -> paymentByThemBD[txCurrency] = (paymentByThemBD[txCurrency] ?: BigDecimal.ZERO).add(safeBd)
                TransactionType.OWED_TO_THEM -> owedToThemBD[txCurrency] = (owedToThemBD[txCurrency] ?: BigDecimal.ZERO).add(safeBd)
                TransactionType.PAYMENT_TO_THEM -> paymentToThemBD[txCurrency] = (paymentToThemBD[txCurrency] ?: BigDecimal.ZERO).add(safeBd)
                else -> {}
            }

            var currentBalBD = currentBalBDMap[txCurrency] ?: BigDecimal.ZERO
            currentBalBD = FinancialPolicy.normalize(
                when (txType) {
                    TransactionType.OWED_BY_THEM, TransactionType.PAYMENT_TO_THEM -> currentBalBD.add(safeBd)
                    TransactionType.PAYMENT_BY_THEM, TransactionType.OWED_TO_THEM -> currentBalBD.subtract(safeBd)
                    else -> currentBalBD
                }
            )

            currentBalBDMap[txCurrency] = currentBalBD
            balancesMap[tx.id] = currentBalBD
            txSequenceNumbers[tx.id] = seq++
        }

        val currencyKeys = currencySet.toList()
        val numCurrencies = currencyKeys.size

        val owedByThemMap = HashMap<String, BigDecimal>(numCurrencies)
        val paymentByThemMap = HashMap<String, BigDecimal>(numCurrencies)
        val owedToThemMap = HashMap<String, BigDecimal>(numCurrencies)
        val paymentToThemMap = HashMap<String, BigDecimal>(numCurrencies)
        val netDebtMap = HashMap<String, BigDecimal>(numCurrencies)
        val netDebtBDMap = HashMap<String, BigDecimal>(numCurrencies)

        for (curr in currencyKeys) {
            val owedBy = FinancialPolicy.normalize(owedByThemBD[curr] ?: BigDecimal.ZERO)
            val payBy = FinancialPolicy.normalize(paymentByThemBD[curr] ?: BigDecimal.ZERO)
            val owedTo = FinancialPolicy.normalize(owedToThemBD[curr] ?: BigDecimal.ZERO)
            val payTo = FinancialPolicy.normalize(paymentToThemBD[curr] ?: BigDecimal.ZERO)

            owedByThemMap[curr] = owedBy
            paymentByThemMap[curr] = payBy
            owedToThemMap[curr] = owedTo
            paymentToThemMap[curr] = payTo

            val netDebtBD = FinancialPolicy.normalize(owedBy.subtract(payBy).subtract(owedTo).add(payTo))
            netDebtMap[curr] = netDebtBD
            netDebtBDMap[curr] = netDebtBD
        }

        val primaryDisplayCurrency: String
        val netDebt: BigDecimal

        val baseNetBd = FinancialPolicy.normalize(netDebtBDMap[normDefaultCurrency] ?: BigDecimal.ZERO)
        if (baseNetBd.compareTo(BigDecimal.ZERO) != 0) {
            primaryDisplayCurrency = normDefaultCurrency
            netDebt = baseNetBd
        } else {
            val nonZeroForeignEntry = netDebtBDMap.entries
                .filter { (k, v) -> k != normDefaultCurrency && FinancialPolicy.normalize(v).compareTo(BigDecimal.ZERO) != 0 }
                .maxWithOrNull(compareBy<Map.Entry<String, BigDecimal>> { it.value.abs() }.thenBy { it.key })
            if (nonZeroForeignEntry != null) {
                primaryDisplayCurrency = nonZeroForeignEntry.key
                netDebt = nonZeroForeignEntry.value
            } else {
                primaryDisplayCurrency = normDefaultCurrency
                netDebt = BigDecimal.ZERO
            }
        }

        return CustomerHistoryCalculationResult(
            currencyKeys = currencyKeys,
            owedByThemMap = owedByThemMap,
            paymentByThemMap = paymentByThemMap,
            owedToThemMap = owedToThemMap,
            paymentToThemMap = paymentToThemMap,
            netDebtMap = netDebtMap,
            runningBalances = balancesMap,
            txSequenceNumbers = txSequenceNumbers,
            primaryDisplayCurrency = primaryDisplayCurrency,
            netDebt = netDebt,
            netDebtBigDecimalMap = netDebtBDMap,
            owedByThemBDMap = owedByThemBD,
            paymentByThemBDMap = paymentByThemBD,
            owedToThemBDMap = owedToThemBD,
            paymentToThemBDMap = paymentToThemBD
        )
    }

    fun calculateSummary(
        allCustomerTxs: List<HabayebTransaction>,
        currencySymbol: String,
        exchangeRatesJson: String?,
        fallbackCreatedAt: Long
    ): CustomerSummaryResult {
        val normDefaultCurrency = FinancialPolicy.canonicalCurrencySymbol(currencySymbol)
        if (allCustomerTxs.isEmpty()) {
            return CustomerSummaryResult(
                netDebtBigDecimalMap = emptyMap(),
                primaryDisplayCurrency = normDefaultCurrency,
                netDebt = BigDecimal.ZERO,
                lastTimestamp = fallbackCreatedAt
            )
        }

        val safeRatesJson = exchangeRatesJson ?: ""
        val owedByThemBD = HashMap<String, BigDecimal>(2)
        val paymentByThemBD = HashMap<String, BigDecimal>(2)
        val owedToThemBD = HashMap<String, BigDecimal>(2)
        val paymentToThemBD = HashMap<String, BigDecimal>(2)
        val currencySet = LinkedHashSet<String>(2)
        currencySet.add(normDefaultCurrency)

        var maxTime = fallbackCreatedAt

        for (tx in allCustomerTxs) {
            if (tx.timestamp > maxTime) {
                maxTime = tx.timestamp
            }
            val (rawTxCurrency, bdAmount) = CurrencyConfig.getTransactionCurrencyAndAmountBigDecimal(tx, normDefaultCurrency, safeRatesJson)
            val txCurrency = FinancialPolicy.canonicalCurrencySymbol(rawTxCurrency)
            currencySet.add(txCurrency)

            val safeBd = FinancialPolicy.normalize(bdAmount)
            when (TransactionType.fromValue(tx.type)) {
                TransactionType.OWED_BY_THEM -> owedByThemBD[txCurrency] = (owedByThemBD[txCurrency] ?: BigDecimal.ZERO).add(safeBd)
                TransactionType.PAYMENT_BY_THEM -> paymentByThemBD[txCurrency] = (paymentByThemBD[txCurrency] ?: BigDecimal.ZERO).add(safeBd)
                TransactionType.OWED_TO_THEM -> owedToThemBD[txCurrency] = (owedToThemBD[txCurrency] ?: BigDecimal.ZERO).add(safeBd)
                TransactionType.PAYMENT_TO_THEM -> paymentToThemBD[txCurrency] = (paymentToThemBD[txCurrency] ?: BigDecimal.ZERO).add(safeBd)
                else -> {}
            }
        }

        val netDebtBDMap = HashMap<String, BigDecimal>(currencySet.size)
        for (curr in currencySet) {
            val owedBy = owedByThemBD[curr] ?: BigDecimal.ZERO
            val payBy = paymentByThemBD[curr] ?: BigDecimal.ZERO
            val owedTo = owedToThemBD[curr] ?: BigDecimal.ZERO
            val payTo = paymentToThemBD[curr] ?: BigDecimal.ZERO
            netDebtBDMap[curr] = FinancialPolicy.normalize(owedBy.subtract(payBy).subtract(owedTo).add(payTo))
        }

        val primaryDisplayCurrency: String
        val netDebt: BigDecimal
        val baseNetBd = FinancialPolicy.normalize(netDebtBDMap[normDefaultCurrency] ?: BigDecimal.ZERO)
        if (baseNetBd.compareTo(BigDecimal.ZERO) != 0) {
            primaryDisplayCurrency = normDefaultCurrency
            netDebt = baseNetBd
        } else {
            val nonZeroForeignEntry = netDebtBDMap.entries
                .filter { (k, v) -> k != normDefaultCurrency && FinancialPolicy.normalize(v).compareTo(BigDecimal.ZERO) != 0 }
                .maxWithOrNull(compareBy<Map.Entry<String, BigDecimal>> { it.value.abs() }.thenBy { it.key })
            if (nonZeroForeignEntry != null) {
                primaryDisplayCurrency = nonZeroForeignEntry.key
                netDebt = nonZeroForeignEntry.value
            } else {
                primaryDisplayCurrency = normDefaultCurrency
                netDebt = BigDecimal.ZERO
            }
        }

        return CustomerSummaryResult(
            netDebtBigDecimalMap = netDebtBDMap,
            primaryDisplayCurrency = primaryDisplayCurrency,
            netDebt = netDebt,
            lastTimestamp = maxTime
        )
    }
}


