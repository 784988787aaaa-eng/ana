package com.smartledger.aldaftar.domain.business

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BusinessIdentityAndPhoneContractTest {

    @Test
    fun testCleanDigits_convertsArabicDigitsToWestern() {
        val arabicInput = "٠٥٠١٢٣٤٥٦٧"
        val cleaned = BusinessPhoneFormatter.cleanDigits(arabicInput)
        assertEquals("0501234567", cleaned)
    }

    @Test
    fun testParseRawPhone_withYemenCode() {
        val raw = "+967771234567"
        val parsed = BusinessPhoneFormatter.parseRawPhone(raw)
        assertEquals("+967", parsed.countryCode)
        assertEquals("771234567", parsed.nationalNumber)
    }

    @Test
    fun testParseRawPhone_withSaudiCode() {
        val raw = "+966501234567"
        val parsed = BusinessPhoneFormatter.parseRawPhone(raw)
        assertEquals("+966", parsed.countryCode)
        assertEquals("501234567", parsed.nationalNumber)
    }

    @Test
    fun testParseRawPhone_withDoubleZero() {
        val raw = "00967771234567"
        val parsed = BusinessPhoneFormatter.parseRawPhone(raw)
        assertEquals("+967", parsed.countryCode)
        assertEquals("771234567", parsed.nationalNumber)
    }

    @Test
    fun testParseRawPhone_withLocalYemenNumber() {
        val raw = "771234567"
        val parsed = BusinessPhoneFormatter.parseRawPhone(raw)
        assertEquals("+967", parsed.countryCode)
        assertEquals("771234567", parsed.nationalNumber)
    }

    @Test
    fun testParseRawPhone_nullOrBlank() {
        val parsedNull = BusinessPhoneFormatter.parseRawPhone(null)
        assertEquals("+967", parsedNull.countryCode)
        assertEquals("", parsedNull.nationalNumber)

        val parsedBlank = BusinessPhoneFormatter.parseRawPhone("   ")
        assertEquals("+967", parsedBlank.countryCode)
        assertEquals("", parsedBlank.nationalNumber)
    }

    @Test
    fun testFormatCombinedPhone_returnsNormalizedOrNull() {
        val combined = BusinessPhoneFormatter.formatCombinedPhone("+967", "771234567")
        assertEquals("+967771234567", combined)

        val combinedBlank = BusinessPhoneFormatter.formatCombinedPhone("+967", "")
        assertNull(combinedBlank)
    }

    @Test
    fun testValidateNationalNumber() {
        assertTrue(BusinessPhoneFormatter.validateNationalNumber("771234567") is PhoneValidationResult.Valid)
        assertTrue(BusinessPhoneFormatter.validateNationalNumber("") is PhoneValidationResult.Empty)
        assertTrue(BusinessPhoneFormatter.validateNationalNumber("12") is PhoneValidationResult.Invalid)
    }
}
