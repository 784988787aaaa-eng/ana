package com.smartledger.aldaftar.data.serialization.pdf

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ReportHeaderSpacingContractTest {
    @Test
    fun customerTitleGapIsSmallAndIndependentOfHeaderHeight() {
        assertEquals(6f, PdfReportLayoutSpec.customerTitleGap(), 0.001f)
        assertEquals(10f, PdfReportLayoutSpec.tableHeaderGap(), 0.001f)
        assertTrue(PdfReportLayoutSpec.customerBannerAdvance(14f) < 25f)
        assertTrue(PdfReportLayoutSpec.customerBannerAdvance(28f) > PdfReportLayoutSpec.customerBannerAdvance(14f))
        assertTrue(PdfReportLayoutSpec.comprehensiveSummaryCardHeight(3) > PdfReportLayoutSpec.comprehensiveSummaryCardHeight(1))
    }
}

