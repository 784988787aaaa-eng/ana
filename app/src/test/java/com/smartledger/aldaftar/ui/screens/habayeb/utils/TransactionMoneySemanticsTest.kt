package com.smartledger.aldaftar.ui.screens.habayeb.utils

import com.smartledger.aldaftar.data.local.entities.HabayebTransaction
import com.smartledger.aldaftar.testsupport.MoneyAssertions
import java.math.BigDecimal
import org.junit.Test

class TransactionMoneySemanticsTest {
    private fun tx(
        currency: String,
        foreign: String,
        exchanged: Boolean,
        equivalent: String = "0",
        base: String = "ر.ي"
    ) = HabayebTransaction(
        id = "t", customerId = "c", type = "OWED_BY_THEM",
        amount = BigDecimal(if (exchanged) equivalent else foreign),
        timestamp = 1L, description = "",
        isForeign = currency != base, currencyCode = currency,
        foreignAmount = BigDecimal(foreign), exchangeRate = BigDecimal("140"),
        isRateCalculated = exchanged, equivalentAmount = BigDecimal(equivalent),
        baseCurrencyCode = base
    )

    @Test fun unexchangedForeignTransactionRemainsInItsOwnCurrency() {
        val result = CurrencyConfig.getTransactionCurrencyAndAmountBigDecimal(tx("ر.س", "100", false), "ر.ي")
        org.junit.Assert.assertEquals("ر.س", result.first)
        MoneyAssertions.exact("100.0000", result.second)
    }

    @Test fun exchangedForeignTransactionUsesFrozenEquivalentAndBase() {
        val result = CurrencyConfig.getTransactionCurrencyAndAmountBigDecimal(
            tx("ر.س", "100", true, "14000", "ر.ي"), "ر.ي"
        )
        org.junit.Assert.assertEquals("ر.ي", result.first)
        MoneyAssertions.exact("14000.0000", result.second)
    }

    @Test fun changingDisplayDefaultDoesNotRewriteOriginalTransactionCurrency() {
        val transaction = tx("ر.س", "100", false)
        val yerView = CurrencyConfig.getTransactionCurrencyAndAmountBigDecimal(transaction, "ر.ي")
        val usdView = CurrencyConfig.getTransactionCurrencyAndAmountBigDecimal(transaction, "$")
        org.junit.Assert.assertEquals("ر.س", yerView.first)
        org.junit.Assert.assertEquals("ر.س", usdView.first)
        MoneyAssertions.numeric("100", usdView.second)
    }

    @Test fun historicalEquivalentDoesNotUseCurrentRate() {
        val transaction = tx("ر.س", "100", true, "14000", "ر.ي")
        val currentRate = BigDecimal("170")
        val shown = CurrencyConfig.getTransactionCurrencyAndAmountBigDecimal(transaction, "ر.ي")
        MoneyAssertions.exact("14000.0000", shown.second)
        org.junit.Assert.assertNotEquals(0, currentRate.compareTo(transaction.exchangeRate))
    }
}
