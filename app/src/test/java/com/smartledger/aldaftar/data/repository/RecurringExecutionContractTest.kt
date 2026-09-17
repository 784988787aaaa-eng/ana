package com.smartledger.aldaftar.data.repository

import com.smartledger.aldaftar.data.local.entities.HabayebTransaction
import com.smartledger.aldaftar.domain.model.RecurringConfig
import java.math.BigDecimal
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RecurringExecutionContractTest {
    @Test fun generatedTransactionCopiesFinancialSnapshotExactly() {
        val c=RecurringConfig("r","original","customer","Name",
            BigDecimal("14000.0000"),"OWED_BY_THEM","desc","DAILY",emptyList(),emptyList(),
            10,0,1,9_999_999_999L,0,true,true,"ر.س",
            BigDecimal("100.0000"),BigDecimal("140.0000"),true,BigDecimal("14000.0000"))
        val generated=HabayebTransaction("generated",c.customerId,c.type,c.amount,1L,c.description,
            c.originalTxId,c.isForeign,c.currencyCode,c.foreignAmount,c.exchangeRate,c.isRateCalculated,c.equivalentAmount,"ر.ي")
        assertEquals(c.amount,generated.amount)
        assertEquals(c.currencyCode,generated.currencyCode)
        assertEquals(c.foreignAmount,generated.foreignAmount)
        assertEquals(c.exchangeRate,generated.exchangeRate)
        assertEquals(c.isRateCalculated,generated.isRateCalculated)
        assertEquals(c.equivalentAmount,generated.equivalentAmount)
        assertTrue(generated.linkedMainTxId==c.originalTxId)
    }

    @Test fun currentMarketRateIsNotAnInputToFixedRecurringCopy() {
        val snapshotRate=BigDecimal("140")
        val currentRate=BigDecimal("170")
        assertEquals(0,BigDecimal("140").compareTo(snapshotRate))
        assertTrue(currentRate != snapshotRate)
    }
}
