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

    @Test fun conventionalForeignToBaseRateConvertsForeignTransactionByMultiplication() {
        // Stored rate: 1 SAR = 140 YER. A 100 SAR transaction therefore equals 14,000 YER.
        MoneyAssertions.exact("14000.0000", CurrencyConfig.convertDirectedAmount(
            BigDecimal("100"), "ر.س", "ر.ي", BigDecimal("140"), "ر.س", "ر.ي"
        ))
    }

    @Test fun screenshotRegressionOneHundredUsdAtFiveHundredFiftyYer() {
        // Real user-facing convention: 1 USD = 550 YER => 100 USD = 55,000 YER.
        MoneyAssertions.exact("55000.0000", CurrencyConfig.convertDirectedAmount(
            BigDecimal("100"), "$", "ر.ي", BigDecimal("550"), "$", "ر.ي"
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

    @Test fun invalidRateIsRejectedWithoutPerformingAConversion() {
        // Zero/negative rates are invalid and must fail closed rather than
        // producing a financial result or silently falling back.
        org.junit.Assert.assertThrows(IllegalArgumentException::class.java) {
            CurrencyConfig.convertAmountBigDecimal(
                BigDecimal("100"), "ر.ي", "ر.س", BigDecimal.ZERO
            )
        }
    }
}
