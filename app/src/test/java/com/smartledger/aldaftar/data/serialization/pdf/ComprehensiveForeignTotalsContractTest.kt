package com.smartledger.aldaftar.data.serialization.pdf

import com.smartledger.aldaftar.ui.state.CustomerUiState
import com.smartledger.aldaftar.data.local.entities.HabayebCustomer
import java.math.BigDecimal
import org.junit.Assert.assertEquals
import org.junit.Test

class ComprehensiveForeignTotalsContractTest {
    private fun customer(id: String, foreign: BigDecimal): CustomerUiState =
        CustomerUiState(
            id = id,
            name = id,
            phone = "",
            defaultCurrencyTotal = BigDecimal.ZERO,
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
