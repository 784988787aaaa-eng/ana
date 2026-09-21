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

    @Test
    fun testCanonicalPairOrderPrioritizesStandardMarketOrder() {
        val pair1 = ExchangeRateHelper.getCanonicalPairOrder("ر.ي", "$")
        assertEquals("$", pair1.first)
        assertEquals("ر.ي", pair1.second)

        val pair2 = ExchangeRateHelper.getCanonicalPairOrder("ر.ي", "ر.س")
        assertEquals("ر.س", pair2.first)
        assertEquals("ر.ي", pair2.second)

        val pair3 = ExchangeRateHelper.getCanonicalPairOrder("ر.س", "$")
        assertEquals("$", pair3.first)
        assertEquals("ر.س", pair3.second)
    }

    @Test
    fun testFormatCompactRateBadgeProducesSingleLineText() {
        val json = ExchangeRateHelper.setRate("{}", "$", "ر.ي", BigDecimal("550"))
        val badge = ExchangeRateHelper.formatCompactRateBadge(json, "ر.ي", "$")
        assertEquals("1 $ = 550 ر.ي", badge)

        val badgeDirect = ExchangeRateHelper.formatCompactRateBadge(json, "$", "ر.ي")
        assertEquals("1 $ = 550 ر.ي", badgeDirect)
    }

    @Test
    fun testFormatActiveRateBadgeFormatsCleanWholeAndDecimalNumbers() {
        assertEquals("550", com.smartledger.aldaftar.ui.helper.HabayebMathHelper.formatActiveRateBadge(BigDecimal("550.000000000000")))
        assertEquals("140", com.smartledger.aldaftar.ui.helper.HabayebMathHelper.formatActiveRateBadge(BigDecimal("140")))
        // Reciprocal fraction is inverted into the market integer quote
        assertEquals("550", com.smartledger.aldaftar.ui.helper.HabayebMathHelper.formatActiveRateBadge(BigDecimal("0.001818181818")))
        assertEquals("140", com.smartledger.aldaftar.ui.helper.HabayebMathHelper.formatActiveRateBadge(BigDecimal("0.007142857143")))
        // Clean decimal rate
        assertEquals("3.75", com.smartledger.aldaftar.ui.helper.HabayebMathHelper.formatActiveRateBadge(BigDecimal("3.7500")))
    }

}

