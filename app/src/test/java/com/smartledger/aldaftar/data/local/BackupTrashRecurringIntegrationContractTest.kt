package com.smartledger.aldaftar.data.local

import com.smartledger.aldaftar.data.local.entities.HabayebTransaction
import com.smartledger.aldaftar.data.local.entities.RecurringConfigEntity
import java.math.BigDecimal
import org.junit.Assert.assertEquals
import org.junit.Test

class BackupTrashRecurringIntegrationContractTest {
    @Test fun sameFinancialSnapshotMustSurviveThreeBoundaries() {
        val tx=HabayebTransaction("t","c","OWED_BY_THEM",BigDecimal("14000"),1L,"",
            linkedMainTxId="r",isForeign=true,currencyCode="ر.س",foreignAmount=BigDecimal("100"),
            exchangeRate=BigDecimal("140"),isRateCalculated=true,equivalentAmount=BigDecimal("14000"),baseCurrencyCode="ر.ي")
        val recurring=RecurringConfigEntity("r","t","c","C",tx.amount,tx.type,tx.description,"DAILY",
            emptyList(),emptyList(),10,0,1,2,0,true,tx.isForeign,tx.currencyCode,tx.foreignAmount,
            tx.exchangeRate,tx.isRateCalculated,tx.equivalentAmount)
        val trash=mapOf("amount" to tx.amount,"foreign" to tx.foreignAmount,"rate" to tx.exchangeRate,"eq" to tx.equivalentAmount)
        assertEquals(recurring.amount,trash["amount"])
        assertEquals(recurring.foreignAmount,trash["foreign"])
        assertEquals(recurring.exchangeRate,trash["rate"])
        assertEquals(recurring.equivalentAmount,trash["eq"])
    }
}
