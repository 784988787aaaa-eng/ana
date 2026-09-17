package com.smartledger.aldaftar.data.backup

import com.smartledger.aldaftar.data.local.entities.HabayebTransaction
import com.smartledger.aldaftar.data.local.entities.RecurringConfigEntity
import java.math.BigDecimal
import org.junit.Assert.assertEquals
import org.junit.Test

class BackupRoundTripContractTest {
    @Test fun backupMustCarryHistoricalExchangeSnapshot() {
        val tx=HabayebTransaction("t","c","OWED_BY_THEM",BigDecimal("14000"),1L,"",
            isForeign=true,currencyCode="ر.س",foreignAmount=BigDecimal("100"),
            exchangeRate=BigDecimal("140"),isRateCalculated=true,equivalentAmount=BigDecimal("14000"),baseCurrencyCode="ر.ي")
        val snapshot=mapOf(
            "amount" to tx.amount.toPlainString(),
            "currency" to tx.currencyCode,
            "foreign" to tx.foreignAmount.toPlainString(),
            "rate" to tx.exchangeRate.toPlainString(),
            "exchanged" to tx.isRateCalculated.toString(),
            "equivalent" to tx.equivalentAmount.toPlainString(),
            "base" to tx.baseCurrencyCode
        )
        assertEquals("140",snapshot["rate"])
        assertEquals("14000",snapshot["equivalent"])
    }

    @Test fun backupMustCarryRecurringSnapshotToo() {
        val r=RecurringConfigEntity("r","t","c","C",BigDecimal("14000"),"OWED_BY_THEM","",
            "DAILY",emptyList(),emptyList(),10,0,1,2,0,true,true,"ر.س",
            BigDecimal("100"),BigDecimal("140"),true,BigDecimal("14000"))
        assertEquals("ر.س",r.currencyCode)
        assertEquals(0,BigDecimal("140").compareTo(r.exchangeRate))
        assertEquals(0,BigDecimal("14000").compareTo(r.equivalentAmount))
    }
}
