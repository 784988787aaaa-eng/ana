package com.smartledger.aldaftar.data.serialization.pdf

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.test.core.app.ApplicationProvider
import com.smartledger.aldaftar.data.local.entities.HabayebCustomer
import com.smartledger.aldaftar.data.local.entities.HabayebTransaction
import com.smartledger.aldaftar.ui.state.CustomerUiState
import java.math.BigDecimal
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class PdfForeignCurrencyRenderingRegressionTest {

    private val context: Context get() = ApplicationProvider.getApplicationContext()

    @Test
    fun testDrawForeignCurrenciesSummary_handlesMultipleCurrenciesWithoutCrashOrZeroHeight() {
        val bitmap = Bitmap.createBitmap(600, 800, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val zeroHeight = PdfStatementTotalsRenderer.drawForeignCurrenciesSummary(
            canvas = canvas,
            context = context,
            currentY = 100f,
            uncalculatedForeignSums = emptyMap(),
            currencySymbol = "ر.ي"
        )
        assertEquals(100f, zeroHeight, 0.001f)

        val foreignMap = mapOf(
            "ر.س" to BigDecimal("100"),
            "$" to BigDecimal("-50"),
            "درهم" to BigDecimal("200")
        )

        val renderedY = PdfStatementTotalsRenderer.drawForeignCurrenciesSummary(
            canvas = canvas,
            context = context,
            currentY = 100f,
            uncalculatedForeignSums = foreignMap,
            currencySymbol = "ر.ي"
        )

        assertTrue("Rendered Y should be greater than start Y", renderedY > 100f)
    }

    @Test
    fun testDrawComprehensiveSummaryCard_handles10ForeignCurrenciesProperly() {
        val bitmap = Bitmap.createBitmap(600, 1000, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val customers = listOf(
            CustomerUiState(
                id = "c1",
                name = "أحمد علي محمد صالح العريقي",
                phone = "771234567",
                defaultCurrencyTotal = BigDecimal("150000"),
                originalCustomer = HabayebCustomer("c1", "أحمد", "771234567", "", 0L),
                foreignDebts = mapOf(
                    "ر.س" to BigDecimal("1000"),
                    "$" to BigDecimal("500"),
                    "AED" to BigDecimal("2000"),
                    "KWD" to BigDecimal("100"),
                    "OMR" to BigDecimal("150"),
                    "QAR" to BigDecimal("3000"),
                    "BHD" to BigDecimal("80"),
                    "EUR" to BigDecimal("400"),
                    "EGP" to BigDecimal("50000"),
                    "TRY" to BigDecimal("20000")
                )
            )
        )

        val summary = PdfReportCalculator.calculateComprehensiveReport(customers)
        assertEquals(10, summary.foreignBalances.size)

        val endY = PdfCustomerSummaryRenderer.drawComprehensiveSummaryCard(
            canvas = canvas,
            context = context,
            primaryColorHex = "#0D9488",
            summary = summary,
            totalItems = 1,
            currencySymbol = "ر.ي",
            startY = 100f
        )

        assertTrue(endY > 100f)
    }

    @Test
    fun testSingleCustomerReportCalculator_parityWithCustomerHistoryCalculator() {
        val txs = listOf(
            HabayebTransaction("1", "c1", "OWED_BY_THEM", BigDecimal("10000"), 1000L, description = "سلفة"),
            HabayebTransaction("2", "c1", "PAYMENT_BY_THEM", BigDecimal("4000"), 2000L, description = "سداد"),
            HabayebTransaction("3", "c1", "OWED_BY_THEM", BigDecimal("100"), 3000L, description = "سلفة سعودي", currencyCode = "ر.س", isRateCalculated = false)
        )

        val pdfSummary = PdfReportCalculator.calculateSingleCustomerReport(txs, "ر.ي")

        assertEquals(0, BigDecimal("10000").compareTo(pdfSummary.totalDebts))
        assertEquals(0, BigDecimal("4000").compareTo(pdfSummary.totalPayments))
        assertEquals(0, BigDecimal("6000").compareTo(pdfSummary.calculatedNetDebt))
        assertTrue(pdfSummary.uncalculatedForeignSums.containsKey("ر.س"))
        assertEquals(0, BigDecimal("100").compareTo(pdfSummary.uncalculatedForeignSums.getValue("ر.س")))
    }
}
