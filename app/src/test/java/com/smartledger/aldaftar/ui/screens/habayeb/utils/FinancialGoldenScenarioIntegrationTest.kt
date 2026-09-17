package com.smartledger.aldaftar.ui.screens.habayeb.utils

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.smartledger.aldaftar.data.local.AppDatabase
import com.smartledger.aldaftar.data.local.CustomerCurrencyBalance
import com.smartledger.aldaftar.data.local.entities.AppSettings
import com.smartledger.aldaftar.data.local.entities.HabayebCustomer
import com.smartledger.aldaftar.data.local.entities.HabayebTransaction
import com.smartledger.aldaftar.data.repository.HabayebRepository
import com.smartledger.aldaftar.domain.model.TransactionType
import com.smartledger.aldaftar.domain.usecase.habayeb.HabayebFinancialCalculator
import com.smartledger.aldaftar.testsupport.MoneyAssertions
import java.math.BigDecimal
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Broad-stack financial scenarios using the production Room schema, DAO aggregation,
 * currency persistence helpers and customer-balance calculator.
 *
 * These are deliberately boring examples: they are executable specifications for the
 * complete currency triangle YER/SAR/USD under each possible default currency.
 */
class FinancialGoldenScenarioIntegrationTest {
    private lateinit var db: AppDatabase
    private lateinit var repository: HabayebRepository
    private val customer = HabayebCustomer(
        id = "golden-customer",
        name = "Golden Customer",
        phone = "",
        notes = "",
        createdAt = 1L,
        initialType = TransactionType.OWED_BY_THEM.value
    )

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = HabayebRepository(db, db.habayebDao())
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun defaultYER_sarAndUsdForeignTransactionsBecomeExactYERBalance() = runBlocking {
        repository.insertCustomer(customer)

        insertExchanged("usd-yer", "$", "ر.ي", "100", "550", "55000")
        insertExchanged("sar-yer", "ر.س", "ر.ي", "100", "139.5", "13950")

        val balances = db.habayebDao().getAllCustomerBalancesFlow("ر.ي").first()
        assertBalance(balances, "ر.ي", "68950")

        val state = HabayebFinancialCalculator.calculateCustomersUiState(
            listOf(customer), balances, AppSettings(currencySymbol = "ر.ي")
        ).customers.single()
        MoneyAssertions.exact("68950.0000", state.defaultCurrencyTotal)
        assertEquals("ر.ي", state.displayCurrencySymbol)
    }

    @Test
    fun defaultSAR_usdAndYerHaveExplicitDirectRates_andNoHiddenThirdCurrency() = runBlocking {
        repository.insertCustomer(customer)

        // 1 USD = 3.75 SAR; 100 USD = 375 SAR.
        insertExchanged("usd-sar", "$", "ر.س", "100", "3.75", "375")
        // 1 YER = 0.0072 SAR; 10000 YER = 72 SAR.
        insertExchanged("yer-sar", "ر.ي", "ر.س", "10000", "0.0072", "72")

        val balances = db.habayebDao().getAllCustomerBalancesFlow("ر.س").first()
        assertBalance(balances, "ر.س", "447")

        val state = HabayebFinancialCalculator.calculateCustomersUiState(
            listOf(customer), balances, AppSettings(currencySymbol = "ر.س")
        ).customers.single()
        MoneyAssertions.exact("447.0000", state.defaultCurrencyTotal)

        // USD -> YER is not allowed merely because USD -> SAR and YER -> SAR exist.
        var rates = "{}"
        rates = ExchangeRateHelper.setRate(rates, "$", "ر.س", BigDecimal("3.75"))
        rates = ExchangeRateHelper.setRate(rates, "ر.ي", "ر.س", BigDecimal("0.0072"))
        assertFalse(ExchangeRateHelper.hasRate(rates, "$", "ر.ي"))
    }

    @Test
    fun defaultUSD_sarAndYerHaveExplicitDirectRates_andBalanceUsesUSD() = runBlocking {
        repository.insertCustomer(customer)

        // 1 SAR = 0.266666666667 USD; 100 SAR -> 26.666666666700 USD.
        insertExchanged("sar-usd", "ر.س", "$", "100", "0.266666666667", "26.666666666700")
        // 1 YER = 0.001818181818 USD; 55000 YER -> 99.999999990000 USD.
        insertExchanged("yer-usd", "ر.ي", "$", "55000", "0.001818181818", "99.999999990000")

        val balances = db.habayebDao().getAllCustomerBalancesFlow("$").first()
        assertBalance(balances, "$", "126.666666656700")

        val state = HabayebFinancialCalculator.calculateCustomersUiState(
            listOf(customer), balances, AppSettings(currencySymbol = "$")
        ).customers.single()
        MoneyAssertions.exact("126.6667", state.defaultCurrencyTotal)
    }

    @Test
    fun historicalDataKeepsItsOriginalBase_whenDefaultCurrencyChanges_thenNewDataUsesNewBase() = runBlocking {
        repository.insertCustomer(customer)

        // Old record: created while YER was default.
        insertExchanged("old-sar-yer", "ر.س", "ر.ي", "100", "139.5", "13950")
        val old = repository.getHabayebTransactionById("old-sar-yer")!!
        assertEquals("ر.ي", old.baseCurrencyCode)
        MoneyAssertions.exact("13950.0000", old.equivalentAmount)

        // New record: created after the default became SAR.
        insertExchanged("new-usd-sar", "$", "ر.س", "100", "3.75", "375")
        val newer = repository.getHabayebTransactionById("new-usd-sar")!!
        assertEquals("ر.س", newer.baseCurrencyCode)
        MoneyAssertions.exact("375.0000", newer.equivalentAmount)

        // Re-reading with USD as the current display default must not rewrite either record.
        val oldAfter = repository.getHabayebTransactionById("old-sar-yer")!!
        val newAfter = repository.getHabayebTransactionById("new-usd-sar")!!
        assertEquals("ر.ي", oldAfter.baseCurrencyCode)
        assertEquals("ر.س", newAfter.baseCurrencyCode)
        MoneyAssertions.exact("13950.0000", oldAfter.equivalentAmount)
        MoneyAssertions.exact("375.0000", newAfter.equivalentAmount)
    }

    @Test
    fun unexchangedForeignOldDataRemainsForeign_andNeverEntersDefaultTotal() = runBlocking {
        repository.insertCustomer(customer)
        repository.insertHabayebTransaction(
            HabayebTransaction(
                id = "old-unexchanged-sar",
                customerId = customer.id,
                type = TransactionType.OWED_BY_THEM.value,
                amount = BigDecimal("100"),
                timestamp = 1L,
                description = "old SAR",
                isForeign = true,
                currencyCode = "ر.س",
                foreignAmount = BigDecimal("100"),
                exchangeRate = BigDecimal.ZERO,
                isRateCalculated = false,
                equivalentAmount = BigDecimal.ZERO,
                baseCurrencyCode = "ر.ي"
            )
        )

        val balances = db.habayebDao().getAllCustomerBalancesFlow("ر.ي").first()
        assertTrue(balances.any { it.currencyCode == "ر.س" && it.netAmount.compareTo(BigDecimal("100")) == 0 })
        assertFalse(balances.any { it.currencyCode == "ر.ي" && it.netAmount.compareTo(BigDecimal("100")) == 0 })

        val state = HabayebFinancialCalculator.calculateCustomersUiState(
            listOf(customer), balances, AppSettings(currencySymbol = "ر.ي")
        ).customers.single()
        MoneyAssertions.exact("0.0000", state.defaultCurrencyTotal)
        assertEquals("ر.س", state.displayCurrencySymbol)
        MoneyAssertions.exact("100.0000", state.foreignDebts["ر.س"]!!)
    }

    @Test
    fun reverseDirectionsAreReciprocal_forAllThreeCurrenciesWithoutRankRules() {
        val cases = listOf(
            Triple("$", "ر.ي", BigDecimal("550")),
            Triple("ر.س", "ر.ي", BigDecimal("139.5")),
            Triple("$", "ر.س", BigDecimal("3.75"))
        )
        for ((source, target, rate) in cases) {
            val json = ExchangeRateHelper.setRate("{}", source, target, rate)
            val direct = ExchangeRateHelper.getRateBigDecimal(json, source, target)
            val reverse = ExchangeRateHelper.getRateBigDecimal(json, target, source)
            MoneyAssertions.exact(rate.setScale(12).toPlainString(), direct)
            val error = direct.multiply(reverse).setScale(12).subtract(BigDecimal.ONE).abs()
            assertTrue("reciprocal error too large for $source->$target: $error", error <= BigDecimal("0.000000001"))
        }
    }

    private suspend fun insertExchanged(
        id: String,
        transactionCurrency: String,
        baseCurrency: String,
        foreignAmount: String,
        rate: String,
        equivalent: String
    ) {
        repository.insertHabayebTransaction(
            HabayebTransaction(
                id = id,
                customerId = customer.id,
                type = TransactionType.OWED_BY_THEM.value,
                amount = BigDecimal(equivalent),
                timestamp = id.hashCode().toLong(),
                description = id,
                isForeign = true,
                currencyCode = transactionCurrency,
                foreignAmount = BigDecimal(foreignAmount),
                exchangeRate = BigDecimal(rate),
                isRateCalculated = true,
                equivalentAmount = BigDecimal(equivalent),
                baseCurrencyCode = baseCurrency
            )
        )
    }

    private fun assertBalance(
        balances: List<CustomerCurrencyBalance>,
        currency: String,
        expected: String
    ) {
        val row = balances.single { it.customerId == customer.id && it.currencyCode == currency }
        MoneyAssertions.exact(expected, row.netAmount)
    }
}
