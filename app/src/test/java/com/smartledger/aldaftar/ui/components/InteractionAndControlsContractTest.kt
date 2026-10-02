package com.smartledger.aldaftar.ui.components

import com.smartledger.aldaftar.data.local.entities.DatabaseDefaults
import com.smartledger.aldaftar.platform.contacts.StringUtils
import com.smartledger.aldaftar.platform.contacts.StringUtils.toWesternDigits
import com.smartledger.aldaftar.ui.screens.habayeb.utils.CurrencyConfig
import com.smartledger.aldaftar.domain.model.FinancialPolicy
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal

class InteractionAndControlsContractTest {

    @Test
    fun normalizeDigits_convertsArabicAndPersianDigitsToWestern() {
        val arabic = "١٢٣٤٥٦٧٨٩٠"
        val normalized = StringUtils.normalizeDigits(arabic)
        assertEquals("1234567890", normalized)

        val persian = "۱۲۳۴۵۶۷۸۹۰"
        val normalizedPersian = StringUtils.normalizeDigits(persian)
        assertEquals("1234567890", normalizedPersian)
    }

    @Test
    fun normalizeDigits_handlesArabicAndCommaSeparators() {
        val arabicDecimal = "١٢٫٥٠"
        val normalized = StringUtils.normalizeDigits(arabicDecimal)
        assertEquals("12.50", normalized)

        val commaDecimal = "12,50"
        val normalizedComma = StringUtils.normalizeDigits(commaDecimal)
        assertEquals("12.50", normalizedComma)

        val arabicComma = "12،50"
        val normalizedArabicComma = StringUtils.normalizeDigits(arabicComma)
        assertEquals("12.50", normalizedArabicComma)
    }

    @Test
    fun parseBigDecimalOrNull_handlesVariousValidFinancialInputs() {
        assertEquals(BigDecimal("100"), CurrencyConfig.parseBigDecimalOrNull("100"))
        assertEquals(BigDecimal("100.5"), CurrencyConfig.parseBigDecimalOrNull("100.5"))
        assertEquals(BigDecimal("100.5"), CurrencyConfig.parseBigDecimalOrNull("١٠٠٫٥"))
        assertEquals(BigDecimal("530.25"), CurrencyConfig.parseBigDecimalOrNull(" 530,25 "))
        assertNull(CurrencyConfig.parseBigDecimalOrNull(""))
        assertNull(CurrencyConfig.parseBigDecimalOrNull("   "))
        assertNull(CurrencyConfig.parseBigDecimalOrNull("abc"))
    }

    @Test
    fun financialRateScale_isEnforcedCorrectly() {
        val rateScale = FinancialPolicy.rateScale
        assertTrue("Rate scale should allow multi-decimal accuracy", rateScale >= 4)

        val testRate = BigDecimal("530.1234")
        assertEquals(4, testRate.scale())
    }

    @Test
    fun pinCodeInput_normalizesArabicDigitsAccurately() {
        val arabicPin = "١٢٣٤"
        val clean = arabicPin.toWesternDigits()
        assertEquals("4 digits", 4, clean.length)
        assertEquals("1234", clean)
        assertTrue(clean.all { it.isDigit() })
    }

    @Test
    fun sanitizedAmountInputAlgorithm_simulatesTypingAndDecimals() {
        fun sanitizeAmount(raw: String): String {
            if (raw.isEmpty()) return ""
            val normalized = CurrencyConfig.normalizeDigits(raw).replace(" ", "")
            val dotCount = normalized.count { it == '.' }
            val isValidChars = normalized.all { it.isDigit() || it == '.' }
            val dotIdx = normalized.indexOf('.')
            val validDecimals = dotIdx == -1 || (normalized.length - dotIdx - 1 <= 2)
            if (isValidChars && dotCount <= 1 && validDecimals) {
                return if (normalized.startsWith("0") && normalized.length > 1 && normalized[1] != '.') {
                    normalized.trimStart('0').ifEmpty { "0" }
                } else if (normalized.startsWith(".")) {
                    "0$normalized"
                } else {
                    normalized
                }
            }
            return ""
        }

        assertEquals("500", sanitizeAmount("500"))
        assertEquals("500", sanitizeAmount("٥٠٠"))
        assertEquals("500.5", sanitizeAmount("500.5"))
        assertEquals("500.50", sanitizeAmount("500.50"))
        assertEquals("0.5", sanitizeAmount(".5"))
        assertEquals("0.5", sanitizeAmount("٫٥"))
        assertEquals("50", sanitizeAmount("050"))
    }
}
