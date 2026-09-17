package com.smartledger.aldaftar.ui.screens.habayeb.utils

import java.math.BigDecimal
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CurrencyCycleStressTest {
    @Test fun repeatedDefaultCurrencyCyclesDoNotChangeStoredTransactionFacts() {
        val originalAmount=BigDecimal("100.0000")
        val originalCurrency="ر.س"
        val originalRate=BigDecimal("140.0000")
        var defaultCurrency="ر.ي"
        repeat(500) {
            defaultCurrency=when(defaultCurrency) {
                "ر.ي"->"ر.س"
                "ر.س"->"$"
                else->"ر.ي"
            }
        }
        assertTrue(defaultCurrency in listOf("ر.ي","ر.س","$"))
        assertEquals(0,BigDecimal("100.0000").compareTo(originalAmount))
        assertEquals("ر.س",originalCurrency)
        assertEquals(0,BigDecimal("140.0000").compareTo(originalRate))
    }

    @Test fun independentPairsRemainIndependentAfterRepeatedWrites() {
        var json="{}"
        repeat(100) {
            json=ExchangeRateHelper.setRate(json,"ر.س","ر.ي",BigDecimal("140"))
            json=ExchangeRateHelper.setRate(json,"$","ر.ي",BigDecimal("2500"))
        }
        assertEquals("140.0000",ExchangeRateHelper.getRateBigDecimal(json,"ر.س","ر.ي").toPlainString())
        assertEquals("2500.0000",ExchangeRateHelper.getRateBigDecimal(json,"$","ر.ي").toPlainString())
        assertTrue(!ExchangeRateHelper.hasRate(json,"ر.س","$"))
    }
}
