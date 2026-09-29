package com.smartledger.aldaftar.data.serialization.pdf

import com.smartledger.aldaftar.ui.state.CustomerUiState
import com.smartledger.aldaftar.data.local.entities.HabayebCustomer
import java.math.BigDecimal
import org.junit.Assert.assertEquals
import org.junit.Test

class ComprehensiveForeignTotalsContractTest {
    @Test
    fun accountCountsAreExposedForProfessionalSummary() {
        val customers = listOf(
            customer("a", BigDecimal("100"), BigDecimal("100")),
            customer("b", BigDecimal("-40"), BigDecimal("-40")),
            customer("c", BigDecimal.ZERO, BigDecimal.ZERO)
        )
        val summary = PdfReportCalculator.calculateComprehensiveReport(customers)
        assertEquals(1, summary.owedAccounts)
        assertEquals(1, summary.toAccounts)
        assertEquals(1, summary.balancedAccounts)
    }

    private fun customer(id: String, foreign: BigDecimal, balance: BigDecimal = BigDecimal.ZERO): CustomerUiState =
        CustomerUiState(
            id = id,
            name = id,
            phone = "",
            defaultCurrencyTotal = balance,
            originalCustomer = HabayebCustomer(id, id, "", "", 0L),
            foreignDebts = mapOf("ر.س" to foreign)
        )

    @Test
    fun generalReportDoesNotNetDifferentAccountsIntoOneForeignTotal() {
        val summary = PdfReportCalculator.calculateComprehensiveReport(
            listOf(customer("A", BigDecimal("100")), customer("B", BigDecimal("-80")))
        )
        val sar = summary.foreignBalances.getValue("ر.س")
        assertEquals("100", sar.owedByThem.stripTrailingZeros().toPlainString())
        assertEquals("80", sar.owedToThem.stripTrailingZeros().toPlainString())
        assertEquals("20", sar.net.stripTrailingZeros().toPlainString())
    }
}
