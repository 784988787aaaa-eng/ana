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
    }
}
