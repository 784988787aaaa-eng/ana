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
        File("app/src/main/java/com/smartledger/aldaftar/data/serialization/pdf/$name")
            .takeIf(File::exists)
            ?.readText()
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
}
