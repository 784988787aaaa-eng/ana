package com.smartledger.aldaftar.ui.screens.habayeb.utils

import com.smartledger.aldaftar.testsupport.MoneyAssertions
import java.math.BigDecimal
import org.junit.Test

class CurrencyPrecisionBoundaryTest {
    @Test fun tinyRateRemainsRepresentableToFourDecimalsWhenStoredByPolicy() {
        val json=ExchangeRateHelper.setRate("{}", "$", "ر.ي", BigDecimal("0.0004"))
        MoneyAssertions.numeric("0.0004",ExchangeRateHelper.getRateBigDecimal(json,"$","ر.ي"))
    }

    @Test fun highRatePreservesFourDecimalPrecision() {
        val json=ExchangeRateHelper.setRate("{}", "$", "ر.ي", BigDecimal("2500.12345"))
        MoneyAssertions.numeric("2500.12345",ExchangeRateHelper.getRateBigDecimal(json,"$","ر.ي"))
    }

    @Test fun largeAmountDoesNotOverflowIntoDouble() {
        val amount=BigDecimal("999999999999999999.9999")
        val result=CurrencyConfig.convertAmountBigDecimal(amount,"$","ر.ي",BigDecimal("2500"))
        MoneyAssertions.numeric("2499999999999999999997.5",result)
    }
}
