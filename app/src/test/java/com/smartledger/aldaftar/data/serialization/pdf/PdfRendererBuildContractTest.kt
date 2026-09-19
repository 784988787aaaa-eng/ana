package com.smartledger.aldaftar.data.serialization.pdf

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Source-level guard for renderer regressions that can break release compilation.
 * In particular, Paint.apply exposes a `color: Int` receiver property, so using a
 * lambda variable also named `color` can silently turn Color.parseColor(color)
 * into an Int/String type error.
 */
class PdfRendererBuildContractTest {
    private fun source(name: String): String =
        listOf(
            File("src/main/java/com/smartledger/aldaftar/data/serialization/pdf/$name"),
            File("app/src/main/java/com/smartledger/aldaftar/data/serialization/pdf/$name")
        ).firstOrNull(File::exists)?.readText()
            ?: error("PDF renderer source not found: $name")

    @Test
    fun comprehensiveKpiPaintDoesNotShadowPaintColor() {
        val source = source("PdfCustomerSummaryRenderer.kt")
        assertTrue(source.contains("valueColorHex"))
        assertFalse(source.contains("Color.parseColor(color)"))
    }

    @Test
    fun pageRendererImportsMathHelperUsedByCustomerIntro() {
        val source = source("PdfPageRenderer.kt")
        assertTrue(source.contains("import com.smartledger.aldaftar.ui.helper.HabayebMathHelper"))
        assertTrue(source.contains("HabayebMathHelper.formatSmart(net.abs())"))
    }

    @Test
    fun bookletCustomerTitleMatchesSingleReportAndAvoidsTableOverlap() {
        val rendererSource = source("PdfPageRenderer.kt")
        // Verify title format matches single report
        assertTrue(rendererSource.contains("R.string.pdf_statement_title"))
        // Verify text size is 17.5f matching single report
        assertTrue(rendererSource.contains("textSize = 17.5f"))
        // Verify title bottom margin is included so title does not hide behind table header
        assertTrue(rendererSource.contains("titleBottomMargin = 12f"))
        assertTrue(rendererSource.contains("workingY = textStartY + actualBannerHeight + titleBottomMargin"))

        val engineSource = source("MasterBookletPdfEngine.kt")
        // Verify inter-customer spacing / pagination check is in place
        assertTrue(engineSource.contains("ctx.currentY + 200f > 780f"))
    }
}
