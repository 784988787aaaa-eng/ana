package com.smartledger.aldaftar.ui.screens.habayeb.utils

import com.smartledger.aldaftar.data.local.entities.HabayebTransaction
import java.math.BigDecimal
import org.junit.Assert.assertEquals
import org.junit.Test

class DefaultCurrencyInvarianceTest {
    @Test fun foreignTransactionStaysForeignRelativeToEachNewDefaultUntilItMatches() {
        val tx=HabayebTransaction("t","c","OWED_BY_THEM",BigDecimal("100"),1L,"",
            isForeign=true,currencyCode="ر.س",foreignAmount=BigDecimal("100"),
            isRateCalculated=false,equivalentAmount=BigDecimal.ZERO,baseCurrencyCode="ر.ي")
        listOf("ر.ي","$").forEach { d ->
            assertEquals("ر.س",CurrencyConfig.getTransactionCurrencyAndAmountBigDecimal(tx,d).first)
        }
        assertEquals("ر.س",CurrencyConfig.getTransactionCurrencyAndAmountBigDecimal(tx,"ر.س").first)
    }

    @Test fun exchangedTransactionKeepsItsFrozenBaseCurrencyAcrossDisplayDefaults() {
        val tx=HabayebTransaction("t","c","OWED_BY_THEM",BigDecimal("14000"),1L,"",
            isForeign=true,currencyCode="ر.س",foreignAmount=BigDecimal("100"),
            exchangeRate=BigDecimal("140"),isRateCalculated=true,equivalentAmount=BigDecimal("14000"),baseCurrencyCode="ر.ي")
        listOf("ر.ي","ر.س","$").forEach { d ->
            val shown=CurrencyConfig.getTransactionCurrencyAndAmountBigDecimal(tx,d)
            assertEquals("ر.ي",shown.first)
            assertEquals(0,BigDecimal("14000.0000").compareTo(shown.second))
        }
    }
}
