package com.smartledger.aldaftar.ui.screens.habayeb.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.math.BigDecimal
import java.math.RoundingMode

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class ExchangeRateHelperTest {

    @Test
    fun testSameCurrencyAlwaysReturnsOne() {
        val json = "{}"
        val rate = ExchangeRateHelper.getRateBigDecimal(json, "ر.ي", "ر.ي")
        assertEquals(0, BigDecimal.ONE.compareTo(rate))

        assertTrue(ExchangeRateHelper.hasRate(json, "ر.ي", "ر.ي"))
    }

    @Test
    fun testSetRateAndGetRate() {
        val base = "ر.ي"
        val foreign = "ر.س"
        val originalJson = "{}"
        val updatedJson = ExchangeRateHelper.setRate(originalJson, base, foreign, BigDecimal("140.0"))

        val retrievedRate = ExchangeRateHelper.getRateBigDecimal(updatedJson, base, foreign)
        assertEquals(0, BigDecimal("140.0").compareTo(retrievedRate))
        assertTrue(ExchangeRateHelper.hasRate(updatedJson, base, foreign))
    }

    @Test
    fun testRejectZeroOrNegativeRate() {
        val originalJson = "{}"
        val updatedJsonZero = ExchangeRateHelper.setRate(originalJson, "ر.ي", "ر.س", BigDecimal.ZERO)
        val updatedJsonNeg = ExchangeRateHelper.setRate(originalJson, "ر.ي", "ر.س", BigDecimal("-5"))

        assertEquals(originalJson, updatedJsonZero)
        assertEquals(originalJson, updatedJsonNeg)
    }

    @Test
    fun testReverseRateIsReciprocal() {
        val json = ExchangeRateHelper.setRate("{}", "ر.ي", "ر.س", BigDecimal("140"))
        val reverse = ExchangeRateHelper.getRateBigDecimal(json, "ر.س", "ر.ي")
        assertEquals(0, BigDecimal("0.007142857143").compareTo(reverse))
    }

}

