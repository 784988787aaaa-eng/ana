package com.smartledger.aldaftar.ui.screens.habayeb.utils

import com.smartledger.aldaftar.testsupport.MoneyAssertions
import java.math.BigDecimal
import org.json.JSONObject
import org.junit.Test

class ExchangeRateMatrixContractTest {
    @Test fun enteredForeignToBaseRateIsStoredInTheEnteredDirection() {
        val json = ExchangeRateHelper.setRate("{}", "ر.س", "ر.ي", BigDecimal("140"))
        MoneyAssertions.exact("140.000000000000", ExchangeRateHelper.getRateBigDecimal(json, "ر.س", "ر.ي"))
        MoneyAssertions.exact("0.007142857143", ExchangeRateHelper.getRateBigDecimal(json, "ر.ي", "ر.س"))
    }

    @Test fun editingReverseDirectionReplacesTheAuthoritativeDirection() {
        var json = ExchangeRateHelper.setRate("{}", "ر.س", "ر.ي", BigDecimal("140"))
        json = ExchangeRateHelper.setRate(json, "ر.ي", "ر.س", BigDecimal("0.008"))
        MoneyAssertions.exact("0.008000000000", ExchangeRateHelper.getRateBigDecimal(json, "ر.ي", "ر.س"))
        MoneyAssertions.exact("125.000000000000", ExchangeRateHelper.getRateBigDecimal(json, "ر.س", "ر.ي"))
    }

    @Test fun updatingOnePairDoesNotRewriteAnIndependentPair() {
        var json = "{}"
        json = ExchangeRateHelper.setRate(json, "ر.س", "ر.ي", BigDecimal("140"))
        json = ExchangeRateHelper.setRate(json, "ر.س", "$", BigDecimal("0.40"))

        MoneyAssertions.exact("140.000000000000", ExchangeRateHelper.getRateBigDecimal(json, "ر.س", "ر.ي"))
        MoneyAssertions.exact("0.400000000000", ExchangeRateHelper.getRateBigDecimal(json, "ر.س", "$"))
    }

    @Test fun missingCrossPairIsNotDerivedFromThirdCurrency() {
        var json = "{}"
        json = ExchangeRateHelper.setRate(json, "ر.س", "ر.ي", BigDecimal("140"))
        json = ExchangeRateHelper.setRate(json, "$", "ر.ي", BigDecimal("2500"))

        // SAR/USD is intentionally undefined. The application must not invent it.
        org.junit.Assert.assertFalse(ExchangeRateHelper.hasRate(json, "ر.س", "$"))
    }

    @Test fun malformedAndMissingRatesNeverBecomeOneToOne() {
        org.junit.Assert.assertFalse(ExchangeRateHelper.hasRate("{}", "ر.س", "$"))
        org.junit.Assert.assertFalse(ExchangeRateHelper.hasRate("""{"ر.س":{"$":"bad"}}""", "ر.س", "$"))
        org.junit.Assert.assertNotEquals("1.0000", ExchangeRateHelper.getRateBigDecimal("{}", "ر.س", "$").toPlainString())
    }

    @Test fun reciprocalIsComputedFromStoredCanonicalValue() {
        val json = JSONObject()
            .put("ر.ي", JSONObject().put("ر.س", "0.007142857142857143"))
            .toString()
        val completed = ExchangeRateHelper.completeMatrix(json)
        MoneyAssertions.exact("140.000000000000", ExchangeRateHelper.getRateBigDecimal(completed, "ر.ي", "ر.س"))
        MoneyAssertions.exact("0.007142857143", ExchangeRateHelper.getRateBigDecimal(completed, "ر.س", "ر.ي"))
    }
    @Test fun clearingRateRemovesBothDirectionsAndLeavesPairMissing() {
        var json = ExchangeRateHelper.setRate("{}", "$", "ر.ي", BigDecimal("550"))
        json = ExchangeRateHelper.clearRate(json, "$", "ر.ي")
        org.junit.Assert.assertFalse(ExchangeRateHelper.hasRate(json, "$", "ر.ي"))
        org.junit.Assert.assertFalse(ExchangeRateHelper.hasRate(json, "ر.ي", "$"))
    }

    @Test fun foreignToDefaultRateMatchesTheUserVisibleConvention() {
        val json = ExchangeRateHelper.setRate("{}", "$", "ر.ي", BigDecimal("550"))
        MoneyAssertions.exact("550.000000000000", ExchangeRateHelper.getRateBigDecimal(json, "$", "ر.ي"))
        MoneyAssertions.exact("0.001818181818", ExchangeRateHelper.getRateBigDecimal(json, "ر.ي", "$"))
    }

}
