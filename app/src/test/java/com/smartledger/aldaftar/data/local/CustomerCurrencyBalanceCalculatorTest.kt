package com.smartledger.aldaftar.data.local

import com.smartledger.aldaftar.data.local.entities.HabayebTransaction
import org.junit.Assert.assertEquals
import org.junit.Test
import java.math.BigDecimal

class CustomerCurrencyBalanceCalculatorTest {

    @Test
    fun aggregatesDecimalValuesWithoutFloatingPointCoercion() {
        val transactions = listOf(
            tx(id = "1", amount = "9007199254740.1234", type = "OWED_BY_THEM"),
            tx(id = "2", amount = "0.0001", type = "OWED_BY_THEM"),
            tx(id = "3", amount = "1.0000", type = "OWED_TO_THEM")
        )

        val result = aggregateCustomerCurrencyBalances(transactions, "YER")
            .single()

        assertEquals(BigDecimal("9007199254739.1235"), result.netAmount)
        assertEquals(3, result.txCount)
        assertEquals(1003L, result.lastTimestamp)
    }

    @Test
    fun preservesForeignAndEquivalentSemantics() {
        val transactions = listOf(
            tx(
                id = "1",
                amount = "10.00",
                type = "OWED_BY_THEM",
                isForeign = true,
                currencyCode = "USD",
                foreignAmount = "1000000000000000.0001",
                isRateCalculated = false,
                equivalentAmount = "2500000.0000"
            ),
            tx(
                id = "2",
                amount = "1.00",
                type = "OWED_TO_THEM",
                isForeign = false,
                currencyCode = "USD",
                equivalentAmount = "2.0000"
            )
        )

        val result = aggregateCustomerCurrencyBalances(transactions, "YER")
            .single()

        assertEquals(BigDecimal("999999999999999.0001"), result.netAmount)
        assertEquals(BigDecimal("2499998.0000"), result.netEquivalentAmount)
        assertEquals("USD", result.currencyCode)
    }

    private fun tx(
        id: String,
        amount: String,
        type: String,
        timestamp: Long = 1000L + id.toLong(),
        isForeign: Boolean = false,
        currencyCode: String = "YER",
        foreignAmount: String = "0",
        isRateCalculated: Boolean = false,
        equivalentAmount: String = amount,
        baseCurrencyCode: String = "YER"
    ) = HabayebTransaction(
        id = id,
        customerId = "customer-1",
        type = type,
        amount = BigDecimal(amount),
        timestamp = timestamp,
        description = "test",
        isForeign = isForeign,
        currencyCode = currencyCode,
        foreignAmount = BigDecimal(foreignAmount),
        exchangeRate = BigDecimal.ZERO,
        isRateCalculated = isRateCalculated,
        equivalentAmount = BigDecimal(equivalentAmount),
        baseCurrencyCode = baseCurrencyCode
    )
}
