package com.smartledger.aldaftar.data.local

import com.smartledger.aldaftar.ui.screens.habayeb.utils.CurrencyConfig
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.math.BigDecimal

class FinancialParsingIntegrityTest {

    @Test
    fun groupedCommaDoesNotChangeThousandIntoDecimal() {
        assertEquals(BigDecimal("1500"), CurrencyConfig.parseBigDecimalOrNull("1,500"))
        assertEquals(BigDecimal("1500000"), CurrencyConfig.parseBigDecimalOrNull("1,500,000"))
    }

    @Test
    fun arabicDecimalSeparatorRemainsDecimal() {
        assertEquals(BigDecimal("123.45"), CurrencyConfig.parseBigDecimalOrNull("١٢٣٫٤٥"))
    }

    @Test
    fun persistedDecimalParserDoesNotSilentlyStripUnexpectedCharacters() {
        val converter = BigDecimalConverter()
        assertNull(converter.fromString("12abc34"))
        assertEquals(BigDecimal("1500"), converter.fromString("1,500"))
    }

    @Test
    fun persistedDecimalRoundTripPreservesScaleAndValue() {
        val converter = BigDecimalConverter()
        val original = BigDecimal("100.0000")
        val restored = converter.fromString(converter.toString(original))
        assertEquals(original, restored)
    }
}
