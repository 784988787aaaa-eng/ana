package com.smartledger.aldaftar.data.local

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.math.BigDecimal

/**
 * اختبارات التحقق من دقة وسلوك محول BigDecimalConverter وفق متطلبات الدفعة رقم 3
 */
class BigDecimalConverterTest {

    private val converter = BigDecimalConverter()

    @Test
    fun testMandatoryBatch3Values() {
        // 100.0000
        val v1 = converter.fromString("100.0000")
        assertEquals(BigDecimal("100.0000"), v1)
        assertEquals("100.0000", converter.toString(v1))

        // 0.0001
        val v2 = converter.fromString("0.0001")
        assertEquals(BigDecimal("0.0001"), v2)
        assertEquals("0.0001", converter.toString(v2))

        // -50.2500
        val v3 = converter.fromString("-50.2500")
        assertEquals(BigDecimal("-50.2500"), v3)
        assertEquals("-50.2500", converter.toString(v3))

        // 999999999999.9999
        val v4 = converter.fromString("999999999999.9999")
        assertEquals(BigDecimal("999999999999.9999"), v4)
        assertEquals("999999999999.9999", converter.toString(v4))

        // null
        assertNull(converter.fromString(null))
        assertNull(converter.toString(null))
    }

    @Test
    fun testFromString_integerNumber() {
        val result = converter.fromString("1500")
        assertEquals(BigDecimal("1500"), result)

        val arabicResult = converter.fromString("١٥٠٠")
        assertEquals(BigDecimal("1500"), arabicResult)
    }

    @Test
    fun testFromString_decimalNumber() {
        val result = converter.fromString("123.45")
        assertEquals(BigDecimal("123.45"), result)

        val arabicResult = converter.fromString("١٢٣٫٤٥")
        assertEquals(BigDecimal("123.45"), arabicResult)
    }

    @Test
    fun testFromString_emptyAndBlank() {
        assertNull(converter.fromString(""))
        assertNull(converter.fromString("   "))
    }

    @Test
    fun testFromString_nullAndSpecialStrings() {
        assertNull(converter.fromString(null))
        assertNull(converter.fromString("null"))
        assertNull(converter.fromString("NULL"))
    }

    @Test
    fun testFromString_malformedValue() {
        // السلاسل التالفة يجب ألا تتحول بصمت إلى صفر بل ترجع null
        assertNull(converter.fromString("invalid_text"))
        assertNull(converter.fromString("!@#$%^"))
        assertNull(converter.fromString("-"))
        assertNull(converter.fromString("."))
    }

    @Test
    fun testFromDouble_validAndInvalidDoubles() {
        val valid = converter.fromDouble(45.5)
        assertEquals(BigDecimal.valueOf(45.5), valid)

        // رفض NaN و Infinity
        assertNull(converter.fromDouble(Double.NaN))
        assertNull(converter.fromDouble(Double.POSITIVE_INFINITY))
        assertNull(converter.fromDouble(Double.NEGATIVE_INFINITY))
        assertNull(converter.fromDouble(null))
    }

    @Test
    fun testToString_conversion() {
        assertEquals("1500.75", converter.toString(BigDecimal("1500.75")))
        assertNull(converter.toString(null))
    }
}
