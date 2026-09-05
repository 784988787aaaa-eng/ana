package com.smartledger.aldaftar.domain.finance

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.math.BigDecimal

class FinancialMathTest {
    @Test
    fun decimalAdditionDoesNotUseBinaryFloatingPoint() {
        assertEquals(BigDecimal("0.3000"), FinancialMath.add(BigDecimal("0.1"), BigDecimal("0.2")))
    }

    @Test
    fun halfEvenRoundingIsDeterministic() {
        assertEquals(BigDecimal("2.0000"), FinancialMath.money(BigDecimal("2.00005")))
        assertEquals(BigDecimal("2.0000"), FinancialMath.money(BigDecimal("2.00004")))
    }

    @Test
    fun divisionUsesFinancialScale() {
        assertEquals(BigDecimal("0.3333"), FinancialMath.divide(BigDecimal.ONE, BigDecimal("3")))
    }

    @Test
    fun invalidInputIsRejected() {
        assertNull(FinancialMath.parse("not-a-number"))
    }

    @Test
    fun legacyExchangeRateBoundaryPreservesDecimalValue() {
        val original = BigDecimal("140.1250")
        val stored = LegacyExchangeRateBridge.toLegacyDouble(original)
        val restored = LegacyExchangeRateBridge.fromLegacyDouble(stored)
        assertEquals(original, restored)
    }
}
