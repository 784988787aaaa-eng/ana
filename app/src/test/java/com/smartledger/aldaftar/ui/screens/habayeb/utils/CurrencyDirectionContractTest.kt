package com.smartledger.aldaftar.ui.screens.habayeb.utils

import com.smartledger.aldaftar.testsupport.MoneyAssertions
import java.math.BigDecimal
import org.junit.Test

/**
 * الاتجاه هو مصدر معنى السعر:
 * 1 SAR = 140 YER => SAR→YER = 140, YER→SAR = 1/140.
 * لا يجوز اختيار الضرب/القسمة حسب ترتيب العملة.
 */
class CurrencyDirectionContractTest {
    @Test fun directPairMultipliesByDirectionalRate() {
        MoneyAssertions.exact("14000.0000",
            CurrencyConfig.convertAmountBigDecimal(BigDecimal("100"), "ر.س", "ر.ي", BigDecimal("140")))
    }

    @Test fun reversePairDividesWhenRequestedDirectionIsReverseOfRate() {
        // 1 SAR = 140 YER. The amount is in YER and the requested direction is YER -> SAR.
        MoneyAssertions.exact("100.0000",
            CurrencyConfig.convertDirectedAmount(BigDecimal("14000"), "ر.ي", "ر.س", BigDecimal("140"), "ر.س", "ر.ي"))
    }

    @Test fun explicitDirectionalPairMustNotBeInterpretedByCurrencyRank() {
        // 1 USD = 2500 YER => 100 USD = 250000 YER.
        MoneyAssertions.exact("250000.0000",
            CurrencyConfig.convertAmountBigDecimal(BigDecimal("100"), "$", "ر.ي", BigDecimal("2500")))
    }

    @Test fun sameCurrencyIsIdentity() {
        MoneyAssertions.exact("100.0000",
            CurrencyConfig.convertAmountBigDecimal(BigDecimal("100"), "ر.ي", "ر.ي", BigDecimal("999")))
    }

    @Test fun storedBaseToForeignRateConvertsForeignTransactionBackToBaseByDivision() {
        // Stored rate: 1 YER = 0.007142857... SAR. A 100 SAR transaction
        // therefore equals 14,000 YER.
        MoneyAssertions.exact("14000.0000", CurrencyConfig.convertDirectedAmount(
            BigDecimal("100"), "ر.س", "ر.ي", BigDecimal("0.007142857"), "ر.ي", "ر.س"
        ))
    }

    @Test fun explicitDirectionWorksRegardlessOfCurrencyRank() {
        MoneyAssertions.exact("2500.0000", CurrencyConfig.convertDirectedAmount(
            BigDecimal("1"), "$", "ر.ي", BigDecimal("2500"), "$", "ر.ي"
        ))
        MoneyAssertions.exact("1.0000", CurrencyConfig.convertDirectedAmount(
            BigDecimal("2500"), "ر.ي", "$", BigDecimal("2500"), "$", "ر.ي"
        ))
    }

    @Test fun invalidRateIsNotAValidConversionRate() {
        // This test intentionally protects the zero/negative-rate invariant.
        val result = CurrencyConfig.convertAmountBigDecimal(BigDecimal("100"), "ر.ي", "ر.س", BigDecimal.ZERO)
        org.junit.Assert.assertNotEquals("100.0000", result.toPlainString())
    }
}
