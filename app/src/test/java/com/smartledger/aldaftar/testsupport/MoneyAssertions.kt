package com.smartledger.aldaftar.testsupport

import java.math.BigDecimal
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue

object MoneyAssertions {
    fun exact(expected: String, actual: BigDecimal) {
        assertEquals("numeric value", 0, BigDecimal(expected).compareTo(actual))
        assertEquals("scale", BigDecimal(expected).scale(), actual.scale())
    }

    fun numeric(expected: String, actual: BigDecimal) {
        assertEquals(0, BigDecimal(expected).compareTo(actual))
    }

    fun positive(value: BigDecimal) {
        assertTrue("Expected positive value, got $value", value > BigDecimal.ZERO)
    }
}
