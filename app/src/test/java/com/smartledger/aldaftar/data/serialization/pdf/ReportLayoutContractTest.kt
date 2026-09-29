package com.smartledger.aldaftar.data.serialization.pdf

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ReportLayoutContractTest {
    @Test
    fun contentStartsImmediatelyAfterActualHeaderWithOnlyDesignedBreathingRoom() {
        assertEquals(86f, PdfReportLayoutSpec.contentStartY(70f, 6f, 10f), 0.001f)
        assertEquals(118f, PdfReportLayoutSpec.contentStartY(102f, 6f, 10f), 0.001f)
    }

    @Test
    fun foreignCurrencyCardHeightScalesPerCurrencyWithoutHugeEmptyBox() {
        assertTrue(PdfReportLayoutSpec.foreignCurrencySectionHeight(1) < 120f)
        assertTrue(PdfReportLayoutSpec.foreignCurrencySectionHeight(3) > PdfReportLayoutSpec.foreignCurrencySectionHeight(1))
        assertEquals(136f, PdfReportLayoutSpec.comprehensiveSummaryCardHeight(1), 0.001f)
        assertEquals(194f, PdfReportLayoutSpec.comprehensiveSummaryCardHeight(3), 0.001f)
    }
    @Test
    fun foreignSectionHeightHandlesColumnBoundariesWithoutNegativeOrExcessiveGrowth() {
        val h0 = PdfReportLayoutSpec.foreignCurrencySectionHeight(0)
        val h1 = PdfReportLayoutSpec.foreignCurrencySectionHeight(1)
        val h2 = PdfReportLayoutSpec.foreignCurrencySectionHeight(2)
        val h3 = PdfReportLayoutSpec.foreignCurrencySectionHeight(3)

        assertEquals(0f, h0, 0.001f)
        assertEquals(h1, h2, 0.001f)
        assertTrue(h3 > h2)
        assertTrue(h1 > 0f)
    }

    @Test
    fun comprehensiveSummaryUsesContentDrivenHeightAtColumnBoundaries() {
        val h0 = PdfReportLayoutSpec.comprehensiveSummaryCardHeight(0)
        val h1 = PdfReportLayoutSpec.comprehensiveSummaryCardHeight(1)
        val h2 = PdfReportLayoutSpec.comprehensiveSummaryCardHeight(2)
        val h3 = PdfReportLayoutSpec.comprehensiveSummaryCardHeight(3)

        assertTrue(h0 > 0f)
        assertEquals(h1, h2, 0.001f)
        assertTrue(h3 > h2)
        assertTrue(h1 >= h0)
    }

}
