package com.smartledger.aldaftar.data.serialization.pdf

import com.smartledger.aldaftar.data.local.entities.HabayebTransaction
import com.smartledger.aldaftar.domain.model.TransactionType
import com.smartledger.aldaftar.ui.viewmodel.FinanceConstants
import org.junit.Assert.*
import org.junit.Test
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.UUID

class FinancialReportingComprehensiveTest {

    private fun createTx(
        amount: BigDecimal,
        type: TransactionType,
        currencyCode: String = "IQD",
        isForeign: Boolean = false,
        foreignAmount: BigDecimal = BigDecimal.ZERO,
        equivalentAmount: BigDecimal = BigDecimal.ZERO,
        isRateCalculated: Boolean = false
    ): HabayebTransaction {
        return HabayebTransaction(
            id = UUID.randomUUID().toString(),
            customerId = "cust_123",
            amount = amount,
            type = type.value,
            timestamp = System.currentTimeMillis(),
            description = "Test Note",
            isForeign = isForeign,
            foreignAmount = foreignAmount,
            exchangeRate = BigDecimal.ONE,
            equivalentAmount = equivalentAmount,
            isRateCalculated = isRateCalculated,
            currencyCode = currencyCode,
            baseCurrencyCode = "IQD"
        )
    }

    @Test
    fun calculateSingleCustomerReport_emptyTransactions_returnsZeroTotals() {
        val summary = PdfReportCalculator.calculateSingleCustomerReport(
            transactions = emptyList(),
            currencySymbol = "IQD"
        )

        assertEquals(0, summary.totalDebts.compareTo(BigDecimal.ZERO))
        assertEquals(0, summary.totalPayments.compareTo(BigDecimal.ZERO))
        assertEquals(0, summary.calculatedNetDebt.compareTo(BigDecimal.ZERO))
        assertFalse(summary.hasMultipleCurrencies)
        assertTrue(summary.sortedProcessedTxs.isEmpty())
    }

    @Test
    fun calculateSingleCustomerReport_reconcilesCalculationsAndTotalsCorrectly() {
        val tx1 = createTx(BigDecimal("150000.00"), TransactionType.OWED_BY_THEM) // Debt
        val tx2 = createTx(BigDecimal("50000.00"), TransactionType.PAYMENT_BY_THEM) // Payment

        val summary = PdfReportCalculator.calculateSingleCustomerReport(
            transactions = listOf(tx1, tx2),
            currencySymbol = "IQD"
        )

        assertEquals(0, summary.totalDebts.compareTo(BigDecimal("150000.00")))
        assertEquals(0, summary.totalPayments.compareTo(BigDecimal("50000.00")))
        assertEquals(0, summary.calculatedNetDebt.compareTo(BigDecimal("100000.00")))
        assertFalse(summary.hasMultipleCurrencies)
    }

    @Test
    fun calculateSingleCustomerReport_multiCurrency_keepsDistinctCurrenciesUnmixed() {
        val txPrimary = createTx(BigDecimal("1000.00"), TransactionType.OWED_BY_THEM, currencyCode = "IQD")
        val txForeign = createTx(
            amount = BigDecimal.ZERO,
            type = TransactionType.OWED_BY_THEM,
            currencyCode = "USD",
            isForeign = true,
            foreignAmount = BigDecimal("500.00")
        )

        val summary = PdfReportCalculator.calculateSingleCustomerReport(
            transactions = listOf(txPrimary, txForeign),
            currencySymbol = "IQD"
        )

        assertEquals(0, summary.totalDebts.compareTo(BigDecimal("1000.00")))
        assertTrue(summary.hasMultipleCurrencies)
        
        val foreignVal = summary.uncalculatedForeignSums["$"]
        assertNotNull(foreignVal)
        assertEquals(0, foreignVal?.compareTo(BigDecimal("500.00")))
    }

    @Test
    fun reportPerformance_largeDataset_processesInMilliseconds() {
        val largeList = List(1000) { i ->
            val type = if (i % 2 == 0) TransactionType.OWED_BY_THEM else TransactionType.PAYMENT_BY_THEM
            createTx(BigDecimal("10.00"), type)
        }

        val startTime = System.nanoTime()
        val summary = PdfReportCalculator.calculateSingleCustomerReport(largeList, "IQD")
        val endTime = System.nanoTime()
        val durationMs = (endTime - startTime) / 1_000_000

        assertNotNull(summary)
        assertTrue("Report calculation must complete within 250 milliseconds", durationMs < 250)
    }
}
