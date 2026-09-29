package com.smartledger.aldaftar.domain.usecase.habayeb

import com.smartledger.aldaftar.data.local.CustomerCurrencyBalance
import com.smartledger.aldaftar.data.local.entities.AppSettings
import com.smartledger.aldaftar.data.local.entities.HabayebCustomer
import java.math.BigDecimal
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class ForeignOnlyCustomerClassificationTest {
    private val customer = HabayebCustomer(id = "foreign-only", name = "عميل أجنبي", phone = "", notes = "", createdAt = 1L)

    @Test
    fun unexchangedForeignBalance_isActive_notClosed_andUsesForeignCurrency() {
        val balances = listOf(
            CustomerCurrencyBalance(customer.id, "ر.س", BigDecimal("250"), BigDecimal.ZERO, 10L, 1)
        )
        val state = HabayebFinancialCalculator.calculateCustomersUiState(
            listOf(customer), balances, AppSettings(currencySymbol = "ر.ي")
        ).customers.single()

        assertFalse("foreign-only customer must never be classified as closed", state.isClosed)
        assertEquals("ر.س", state.displayCurrencySymbol)
        assertEquals(0, BigDecimal("0").compareTo(state.defaultCurrencyTotal))
        assertEquals(0, BigDecimal("250").compareTo(state.foreignDebts["ر.س"]!!))
    }

    @Test
    fun whenLocalIsZero_primaryForeignCurrency_isLargestOutstandingBalance() {
        val balances = listOf(
            CustomerCurrencyBalance(customer.id, "ر.س", BigDecimal("120"), BigDecimal.ZERO, 10L, 1),
            CustomerCurrencyBalance(customer.id, "$", BigDecimal("850"), BigDecimal.ZERO, 20L, 1),
            CustomerCurrencyBalance(customer.id, "€", BigDecimal("300"), BigDecimal.ZERO, 30L, 1)
        )
        val state = HabayebFinancialCalculator.calculateCustomersUiState(
            listOf(customer), balances, AppSettings(currencySymbol = "ر.ي")
        ).customers.single()

        assertFalse(state.isClosed)
        assertEquals("$", state.displayCurrencySymbol)
        assertEquals(0, BigDecimal("850").compareTo(state.displayNetDebt))
        assertEquals(3, state.foreignDebts.size)
    }
}
