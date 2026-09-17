package com.smartledger.aldaftar.ui.screens.habayeb.utils

import com.smartledger.aldaftar.testsupport.MoneyAssertions
import java.math.BigDecimal
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * End-to-end financial contract at the rate boundary:
 * the user-facing meaning "1 USD = 550 YER" must survive storage/retrieval
 * and then produce 55,000 YER for a 100 USD transaction.
 */
class RateEntryToConversionRegressionTest {
    @Test fun userFacingRateMeaningSurvivesStorageAndConversion() {
        val stored = ExchangeRateHelper.setRate("{}", "$", "ر.ي", BigDecimal("550"))
        val retrieved = ExchangeRateHelper.getRate(stored, "$", "ر.ي")
        MoneyAssertions.exact("550.000000000000", retrieved)

        val equivalent = CurrencyConfig.convertDirectedAmount(
            amount = BigDecimal("100"),
            sourceCurrency = "$",
            targetCurrency = "ر.ي",
            rate = retrieved,
            rateSourceCurrency = "$",
            rateTargetCurrency = "ر.ي"
        )
        MoneyAssertions.exact("55000.0000", equivalent)
    }

    @Test fun reverseReadIsReciprocalNotAnotherIndependentMeaning() {
        val stored = ExchangeRateHelper.setRate("{}", "$", "ر.ي", BigDecimal("550"))
        val reverse = ExchangeRateHelper.getRate(stored, "ر.ي", "$")
        assertEquals(BigDecimal("1"), reverse.multiply(BigDecimal("550")).setScale(0))
    }
}
