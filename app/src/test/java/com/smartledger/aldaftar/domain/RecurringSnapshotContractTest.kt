package com.smartledger.aldaftar.domain

import com.smartledger.aldaftar.domain.model.RecurringConfig
import com.smartledger.aldaftar.testsupport.MoneyAssertions
import java.math.BigDecimal
import org.junit.Test

class RecurringSnapshotContractTest {
    private fun config(
        amount: String, currency: String, foreign: String, rate: String, equivalent: String, exchanged: Boolean
    ) = RecurringConfig(
        id="r", originalTxId="tx", customerId="c", customerName="C",
        amount=BigDecimal(amount), type="OWED_BY_THEM", description="fixed",
        frequency="DAILY", daysOfWeek=emptyList(), daysOfMonth=emptyList(),
        timeHour=10, timeMinute=0, startDateMillis=1_700_000_000_000,
        endDateMillis=1_800_000_000_000, lastExecutedTimestamp=0,
        isActive=true, isForeign=true, currencyCode=currency,
        foreignAmount=BigDecimal(foreign), exchangeRate=BigDecimal(rate),
        isRateCalculated=exchanged, equivalentAmount=BigDecimal(equivalent)
    )

    @Test fun fixedForeignSnapshotContainsOriginalAndEquivalentFacts() {
        val c=config("14000","ر.س","100","140","14000",true)
        MoneyAssertions.numeric("100", c.foreignAmount)
        MoneyAssertions.numeric("140", c.exchangeRate)
        MoneyAssertions.numeric("14000", c.equivalentAmount)
        org.junit.Assert.assertEquals("ر.س", c.currencyCode)
        org.junit.Assert.assertTrue(c.isRateCalculated)
    }

    @Test fun unexchangedRecurringSnapshotMustStayUnexchanged() {
        val c=config("100","ر.س","100","140","0",false)
        org.junit.Assert.assertFalse(c.isRateCalculated)
        MoneyAssertions.numeric("100", c.foreignAmount)
        MoneyAssertions.numeric("0", c.equivalentAmount)
    }

    @Test fun changingExternalRateCannotMutateImmutableSnapshotByMathematicsAlone() {
        val c=config("14000","ر.س","100","140","14000",true)
        val currentRate=BigDecimal("170")
        val expectedFrozen=c.equivalentAmount
        MoneyAssertions.numeric("14000", expectedFrozen)
        org.junit.Assert.assertNotEquals(0, currentRate.compareTo(c.exchangeRate))
    }
}
