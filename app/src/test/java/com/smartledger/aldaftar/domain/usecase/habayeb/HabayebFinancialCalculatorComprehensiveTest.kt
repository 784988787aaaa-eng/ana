package com.smartledger.aldaftar.domain.usecase.habayeb

import com.smartledger.aldaftar.data.local.CustomerCurrencyBalance
import com.smartledger.aldaftar.data.local.entities.AppSettings
import com.smartledger.aldaftar.data.local.entities.HabayebCustomer
import java.math.BigDecimal
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HabayebFinancialCalculatorComprehensiveTest {
    private val settings = AppSettings(currencySymbol = "ر.ي")

    private fun customer(id: String, name: String, createdAt: Long = 1L) =
        HabayebCustomer(id = id, name = name, phone = id, notes = "", createdAt = createdAt)

    @Test
    fun localBalances_areAggregated_andGlobalTotalsAreSeparatedByDirection() {
        val customers = listOf(customer("owed", "مدين"), customer("to", "دائن"))
        val balances = listOf(
            CustomerCurrencyBalance("owed", "ر.ي", BigDecimal("100.125"), BigDecimal.ZERO, 10L, 2),
            CustomerCurrencyBalance("owed", "ر.ي", BigDecimal("50.375"), BigDecimal.ZERO, 20L, 3),
            CustomerCurrencyBalance("to", "ر.ي", BigDecimal("-40.50"), BigDecimal.ZERO, 30L, 1)
        )

        val result = HabayebFinancialCalculator.calculateCustomersUiState(customers, balances, settings)

        assertEquals(0, BigDecimal("150.5000").compareTo(result.totalOwedByThem))
        assertEquals(0, BigDecimal("40.5000").compareTo(result.totalOwedToThem))
        assertEquals(5, result.customers.first { it.id == "owed" }.totalTransactions)
        assertEquals(20L, result.customers.first { it.id == "owed" }.lastTransactionTimestamp)
    }

    @Test
    fun zeroLocalBalance_withMultipleForeignCurrencies_usesLargestOutstandingCurrencyDeterministically() {
        val balances = listOf(
            CustomerCurrencyBalance("c", "ر.س", BigDecimal("120"), BigDecimal.ZERO, 10L, 1),
            CustomerCurrencyBalance("c", "$", BigDecimal("-900"), BigDecimal.ZERO, 20L, 1),
            CustomerCurrencyBalance("c", "€", BigDecimal("300"), BigDecimal.ZERO, 30L, 1)
        )

        val state = HabayebFinancialCalculator.calculateCustomersUiState(
            listOf(customer("c", "عميل")), balances, settings
        ).customers.single()

        assertEquals("$", state.displayCurrencySymbol)
        assertEquals(0, BigDecimal("-900").compareTo(state.displayNetDebt))
        assertEquals(3, state.foreignDebts.size)
    }

    @Test
    fun filtering_excludesHiddenCustomers_andSearchesNormalizedNameAndPhone() {
        val customers = listOf(
            customer("1", "أحمد", 10L),
            customer("2", "محمد", 20L),
            customer("3", "خالد", 30L)
        )
        val balances = listOf(
            CustomerCurrencyBalance("1", "ر.ي", BigDecimal("100"), BigDecimal.ZERO, 10L, 1),
            CustomerCurrencyBalance("2", "ر.ي", BigDecimal("-50"), BigDecimal.ZERO, 20L, 1),
            CustomerCurrencyBalance("3", "ر.ي", BigDecimal("25"), BigDecimal.ZERO, 30L, 1)
        )
        val ui = HabayebFinancialCalculator.calculateCustomersUiState(customers, balances, settings)

        val byPhone = HabayebFinancialCalculator.calculateFilteredResult(
            ui,
            HabayebFilterParameters("", 0, 0, 0, emptySet(), null, emptySet()),
            emptyMap()
        )
        assertEquals(listOf("3", "2", "1"), byPhone.filteredCustomers.map { it.id })

        val hidden = HabayebFinancialCalculator.calculateFilteredResult(
            ui,
            HabayebFilterParameters("", 0, 0, 0, setOf("3"), null, emptySet()),
            emptyMap()
        )
        assertEquals(listOf("2", "1"), hidden.filteredCustomers.map { it.id })

        val search = HabayebFinancialCalculator.calculateFilteredResult(
            ui,
            HabayebFilterParameters("", 1, 0, 0, emptySet(), null, emptySet()),
            emptyMap()
        )
        assertEquals(listOf("3", "1"), search.filteredCustomers.map { it.id })
    }

    @Test
    fun financialTabs_keepPositiveAndNegativeCustomersSeparate() {
        val customers = listOf(customer("p", "موجب"), customer("n", "سالب"))
        val balances = listOf(
            CustomerCurrencyBalance("p", "ر.ي", BigDecimal("10"), BigDecimal.ZERO, 1L, 1),
            CustomerCurrencyBalance("n", "ر.ي", BigDecimal("-20"), BigDecimal.ZERO, 2L, 1)
        )
        val ui = HabayebFinancialCalculator.calculateCustomersUiState(customers, balances, settings)

        val positive = HabayebFinancialCalculator.calculateFilteredResult(
            ui, HabayebFilterParameters("", 1, 0, 0, emptySet(), null, emptySet()), emptyMap()
        )
        val negative = HabayebFinancialCalculator.calculateFilteredResult(
            ui, HabayebFilterParameters("", 2, 0, 0, emptySet(), null, emptySet()), emptyMap()
        )

        assertEquals(listOf("p"), positive.filteredCustomers.map { it.id })
        assertEquals(listOf("n"), negative.filteredCustomers.map { it.id })
        assertTrue(positive.totalOwedByThem > BigDecimal.ZERO)
        assertTrue(negative.totalOwedToThem > BigDecimal.ZERO)
    }

    @Test
    fun categoryFilter_andPinnedOrdering_preserveFinancialSemantics() {
        val customers = listOf(
            customer("a", "أ", 10L),
            customer("b", "ب", 30L),
            customer("c", "ج", 20L)
        )
        val balances = listOf(
            CustomerCurrencyBalance("a", "ر.ي", BigDecimal("100"), BigDecimal.ZERO, 10L, 1),
            CustomerCurrencyBalance("b", "ر.ي", BigDecimal("50"), BigDecimal.ZERO, 30L, 1),
            CustomerCurrencyBalance("c", "ر.ي", BigDecimal("-25"), BigDecimal.ZERO, 20L, 1)
        )
        val ui = HabayebFinancialCalculator.calculateCustomersUiState(customers, balances, settings)

        val result = HabayebFinancialCalculator.calculateFilteredResult(
            ui,
            HabayebFilterParameters("", 0, 1, 0, emptySet(), "vip", setOf("b")),
            mapOf("a" to "other", "b" to "vip", "c" to "other")
        )

        assertEquals(listOf("b"), result.filteredCustomers.map { it.id })
        assertEquals(1, result.activeCustomersCount)
        assertEquals(1, result.categoryCounts["vip"])
    }
}
