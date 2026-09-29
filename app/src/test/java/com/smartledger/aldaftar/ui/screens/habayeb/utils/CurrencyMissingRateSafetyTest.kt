package com.smartledger.aldaftar.ui.screens.habayeb.utils

import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import java.math.BigDecimal
import org.junit.Test

class CurrencyMissingRateSafetyTest {
    @Test fun emptyMatrixHasNoForeignRate() {
        assertFalse(ExchangeRateHelper.hasRate("{}", "ر.ي", "ر.س"))
        assertFalse(ExchangeRateHelper.hasRate("{}", "ر.س", "$"))
    }

    @Test fun missingPairCannotSilentlyBehaveAsOne() {
        assertNotEquals("1.0000",ExchangeRateHelper.getRateBigDecimal("{}", "ر.س", "$").toPlainString())
    }

    @Test fun invalidStoredRateIsNotAValidRate() {
        val bad="""{"ر.س":{"$":"-4"}}"""
        assertFalse(ExchangeRateHelper.hasRate(bad,"ر.س","$"))
    }

    @Test fun conversionWithMissingRateFailsWithoutFinancialMutation() {
        org.junit.Assert.assertThrows(IllegalArgumentException::class.java) {
            CurrencyConfig.convertDirectedAmount(BigDecimal("100"), "ر.س", "ر.ي", BigDecimal.ZERO, "ر.س", "ر.ي")
        }
    }
}
