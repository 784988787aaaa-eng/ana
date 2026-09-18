package com.smartledger.aldaftar.data.serialization

import com.smartledger.aldaftar.data.local.entities.HabayebTransaction
import com.smartledger.aldaftar.data.serialization.pdf.PdfReportCalculator
import java.math.BigDecimal
import org.junit.Assert.assertEquals
import org.junit.Test

class ReportCurrencyInvariantTest {
    private fun tx(id:String,currency:String,amount:String,foreign:String,exchanged:Boolean=false,equivalent:String="0",base:String="ر.ي",type:String="OWED_BY_THEM") =
        HabayebTransaction(id,"c",type,BigDecimal(amount),id.hashCode().toLong(),"",
            isForeign=currency!=base,currencyCode=currency,foreignAmount=BigDecimal(foreign),
            exchangeRate=BigDecimal("140"),isRateCalculated=exchanged,
            equivalentAmount=BigDecimal(equivalent),baseCurrencyCode=base)

    @Test fun unexchangedForeignDoesNotEnterPrimaryCurrencyTotals() {
        val s=PdfReportCalculator.calculateSingleCustomerReport(listOf(tx("1","ر.س","100","100")), "ر.ي")
        assertEquals(0,BigDecimal.ZERO.compareTo(s.totalDebtsBase))
        assertEquals(0,BigDecimal.ZERO.compareTo(s.calculatedNetDebt))
        assertEquals("100",s.uncalculatedForeignSums["ر.س"]?.stripTrailingZeros()?.toPlainString())
    }

    @Test fun exchangedForeignEntersOnlyItsFrozenBaseCurrency() {
        val s=PdfReportCalculator.calculateSingleCustomerReport(
            listOf(tx("1","ر.س","14000","100",true,"14000","ر.ي")),"ر.ي"
        )
        assertEquals("14000",s.totalDebtsBase.stripTrailingZeros().toPlainString())
        assertEquals("14000",s.calculatedNetDebt.stripTrailingZeros().toPlainString())
    }

    @Test fun mixedCurrenciesNeverCollapseIntoOneUnsupportedTotal() {
        val s=PdfReportCalculator.calculateSingleCustomerReport(
            listOf(
                tx("1","ر.ي","100","100"),
                tx("2","ر.س","200","200"),
                tx("3","$","3","3")
            ),"ر.ي"
        )
        assertEquals("100",s.calculatedNetDebt.stripTrailingZeros().toPlainString())
        assertEquals("200",s.uncalculatedForeignSums["ر.س"]?.stripTrailingZeros()?.toPlainString())
        assertEquals("3",s.uncalculatedForeignSums["$"]?.stripTrailingZeros()?.toPlainString())
    }
}

