package com.smartledger.aldaftar.domain

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.smartledger.aldaftar.R
import com.smartledger.aldaftar.data.local.AppDatabase
import com.smartledger.aldaftar.data.local.entities.AppSettings
import com.smartledger.aldaftar.data.local.entities.DeletedItemEntity
import com.smartledger.aldaftar.data.local.entities.HabayebCustomer
import com.smartledger.aldaftar.data.local.entities.HabayebTransaction
import com.smartledger.aldaftar.data.repository.HabayebRepository
import com.smartledger.aldaftar.domain.model.FinancialPolicy
import com.smartledger.aldaftar.domain.model.TransactionType
import com.smartledger.aldaftar.domain.usecase.habayeb.HabayebFinancialCalculator
import com.smartledger.aldaftar.platform.contacts.FormatUtils
import com.smartledger.aldaftar.testsupport.MoneyAssertions
import com.smartledger.aldaftar.ui.components.CurrencyDialogState
import com.smartledger.aldaftar.ui.components.CurrencySettingsState
import com.smartledger.aldaftar.ui.helper.HabayebMathHelper
import com.smartledger.aldaftar.ui.screens.habayeb.components.ExchangeRateInputHelper
import com.smartledger.aldaftar.ui.screens.habayeb.components.row.CustomerTransactionRowStateCalculator
import com.smartledger.aldaftar.ui.screens.habayeb.utils.CurrencyConfig
import com.smartledger.aldaftar.ui.screens.habayeb.utils.CustomerHistoryCalculator
import com.smartledger.aldaftar.ui.screens.habayeb.utils.CustomerShareHelper
import com.smartledger.aldaftar.ui.screens.habayeb.utils.ExchangeRateHelper
import com.smartledger.aldaftar.ui.screens.trash.utils.TrashItemParser
import com.smartledger.aldaftar.ui.screens.trash.utils.TrashStrings
import java.math.BigDecimal
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class FinancialExactPrecisionContractTest {

    private lateinit var context: Context
    private lateinit var db: AppDatabase
    private lateinit var repository: HabayebRepository

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
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
    fun largeSubtractionAndMultiTransactionHalalasMatchAcrossDaoCalculatorAndMainList() = runBlocking {
        val custA = HabayebCustomer(
            id = "cust-large",
            name = "Large Precision",
            phone = "",
            notes = "",
            createdAt = 1L,
            initialType = TransactionType.OWED_BY_THEM.value
        )
        val custB = HabayebCustomer(
            id = "cust-halala",
            name = "Halala Sum",
            phone = "",
            notes = "",
            createdAt = 2L,
            initialType = TransactionType.OWED_BY_THEM.value
        )
        repository.insertCustomer(custA)
        repository.insertCustomer(custB)

        // 10000000.34 - 10000000.00 = 0.3400 (catches IEEE-754 Double/SQLite REAL precision loss)
        repository.insertHabayebTransaction(
            HabayebTransaction(
                id = "large-1",
                customerId = custA.id,
                type = TransactionType.OWED_BY_THEM.value,
                amount = BigDecimal("10000000.34"),
                timestamp = 100L,
                description = "large debit",
                currencyCode = "ر.س",
                baseCurrencyCode = "ر.س"
            )
        )
        repository.insertHabayebTransaction(
            HabayebTransaction(
                id = "large-2",
                customerId = custA.id,
                type = TransactionType.PAYMENT_BY_THEM.value,
                amount = BigDecimal("10000000.00"),
                timestamp = 101L,
                description = "large credit",
                currencyCode = "ر.س",
                baseCurrencyCode = "ر.س"
            )
        )

        // 40 micro halala transactions summing to an exact BigDecimal figure with 4 decimal places
        val pattern = listOf("0.01", "0.03", "0.07", "0.19", "0.0025", "1.1234", "0.8766", "2.3455")
        val halalaTxs = mutableListOf<HabayebTransaction>()
        var idx = 0
        repeat(5) {
            for (amt in pattern) {
                idx++
                val tx = HabayebTransaction(
                    id = "hal-$idx",
                    customerId = custB.id,
                    type = if (idx % 4 == 0) TransactionType.PAYMENT_BY_THEM.value else TransactionType.OWED_BY_THEM.value,
                    amount = BigDecimal(amt),
                    timestamp = 200L + idx,
                    description = "halala $idx",
                    currencyCode = "ر.س",
                    baseCurrencyCode = "ر.س"
                )
                halalaTxs.add(tx)
                repository.insertHabayebTransaction(tx)
            }
        }

        val balances = db.habayebDao().getAllCustomerBalancesFlow("ر.س").first()
        val uiState = HabayebFinancialCalculator.calculateCustomersUiState(
            listOf(custA, custB),
            balances,
            AppSettings(currencySymbol = "ر.س")
        )

        val custATxs = db.habayebDao().getTransactionsForCustomerDirect(custA.id)
        val custAHistory = CustomerHistoryCalculator.calculate(custATxs, "ر.س", null)
        val custAUi = uiState.customers.first { it.id == custA.id }
        MoneyAssertions.exact("0.3400", custAHistory.netDebt)
        MoneyAssertions.exact("0.3400", custAUi.defaultCurrencyTotal)

        val custBTxs = db.habayebDao().getTransactionsForCustomerDirect(custB.id)
        val custBHistory = CustomerHistoryCalculator.calculate(custBTxs, "ر.س", null)
        val custBUi = uiState.customers.first { it.id == custB.id }
        MoneyAssertions.exact(custBHistory.netDebt.toPlainString(), custBUi.defaultCurrencyTotal)
        assertEquals(0, custBHistory.netDebt.compareTo(custBUi.netDebt))
    }

    @Test
    fun updateCustomerFlipsMixedTransactionsSymmetricallyWithoutBalanceCorruption() = runBlocking {
        val cust = HabayebCustomer(
            id = "cust-flip",
            name = "Symmetric Flip",
            phone = "",
            notes = "",
            createdAt = 1L,
            initialType = TransactionType.OWED_BY_THEM.value
        )
        repository.insertCustomer(cust)
        val txs = listOf(
            HabayebTransaction(id = "f1", customerId = cust.id, type = TransactionType.OWED_BY_THEM.value, amount = BigDecimal("100.2500"), timestamp = 1L, description = "a", currencyCode = "ر.س", baseCurrencyCode = "ر.س"),
            HabayebTransaction(id = "f2", customerId = cust.id, type = TransactionType.PAYMENT_BY_THEM.value, amount = BigDecimal("30.1250"), timestamp = 2L, description = "b", currencyCode = "ر.س", baseCurrencyCode = "ر.س"),
            HabayebTransaction(id = "f3", customerId = cust.id, type = TransactionType.OWED_TO_THEM.value, amount = BigDecimal("10.0000"), timestamp = 3L, description = "c", currencyCode = "ر.س", baseCurrencyCode = "ر.س"),
            HabayebTransaction(id = "f4", customerId = cust.id, type = TransactionType.PAYMENT_TO_THEM.value, amount = BigDecimal("5.0000"), timestamp = 4L, description = "d", currencyCode = "ر.س", baseCurrencyCode = "ر.س")
        )
        txs.forEach { repository.insertHabayebTransaction(it) }

        val before = CustomerHistoryCalculator.calculate(db.habayebDao().getTransactionsForCustomerDirect(cust.id), "ر.س", null)
        MoneyAssertions.exact("65.1250", before.netDebt)

        repository.updateCustomer(cust.copy(initialType = TransactionType.OWED_TO_THEM.value))
        val flippedTxs = db.habayebDao().getTransactionsForCustomerDirect(cust.id)
        val after = CustomerHistoryCalculator.calculate(flippedTxs, "ر.س", null)
        MoneyAssertions.exact("-65.1250", after.netDebt)

        val typesById = flippedTxs.associate { it.id to it.type }
        assertEquals(TransactionType.OWED_TO_THEM.value, typesById["f1"])
        assertEquals(TransactionType.PAYMENT_TO_THEM.value, typesById["f2"])
        assertEquals(TransactionType.OWED_BY_THEM.value, typesById["f3"])
        assertEquals(TransactionType.PAYMENT_BY_THEM.value, typesById["f4"])

        // Flip back restores exact original balance
        repository.updateCustomer(cust.copy(initialType = TransactionType.OWED_BY_THEM.value))
        val restored = CustomerHistoryCalculator.calculate(db.habayebDao().getTransactionsForCustomerDirect(cust.id), "ر.س", null)
        MoneyAssertions.exact("65.1250", restored.netDebt)
    }

    @Test
    fun exchangeRateSettingsAndSetupInputNeverTruncate3755OrReciprocal() {
        val ratesJson = ExchangeRateHelper.setRate("{}", "$", "ر.س", BigDecimal("3.755"))
        val state = CurrencySettingsState(
            initialSettings = AppSettings(currencySymbol = "ر.س", exchangeRatesJson = ratesJson),
            currencyYer = "ر.ي",
            currencySar = "ر.س",
            currencyUsd = "$"
        )
        state.onTargetCurrencyChange("$")
        assertEquals("3.755", state.rateInputStr)

        state.toggleEquationDirection()
        assertEquals("0.266311584554", state.rateInputStr)

        state.onRateInputChange("0.266666666667")
        assertEquals("0.266666666667", state.rateInputStr)
        var savedRate: BigDecimal? = null
        val initialSettings = AppSettings(currencySymbol = "ر.س", exchangeRatesJson = ratesJson)
        state.handleSave(
            settings = initialSettings,
            onSaveSettings = { _, _, effectiveRate, _ -> savedRate = effectiveRate },
            onDismiss = {}
        )
        // Changing an existing rate triggers RevalueConfirm with full-precision reciprocal (no 2-decimal truncation)
        val confirmDialog = state.activeDialogState as CurrencyDialogState.RevalueConfirm
        assertTrue(confirmDialog.newRate > BigDecimal("3.74"))
        state.handleConfirmFutureOnly(
            settings = initialSettings,
            targetCurrency = confirmDialog.targetCurrency,
            newRate = confirmDialog.newRate,
            onSaveSettings = { _, _, effectiveRate, _ -> savedRate = effectiveRate },
            onDismiss = {}
        )
        assertTrue((savedRate ?: BigDecimal.ZERO) > BigDecimal("3.74"))

        // Saving a fresh rate 3.755 directly without prior rate preserves exact 3.755
        val freshState = CurrencySettingsState(
            initialSettings = AppSettings(currencySymbol = "ر.س", exchangeRatesJson = "{}"),
            currencyYer = "ر.ي",
            currencySar = "ر.س",
            currencyUsd = "$"
        )
        freshState.onTargetCurrencyChange("$")
        freshState.onRateInputChange("3.755")
        var freshSavedRate: BigDecimal? = null
        freshState.handleSave(
            settings = AppSettings(currencySymbol = "ر.س", exchangeRatesJson = "{}"),
            onSaveSettings = { _, _, effectiveRate, _ -> freshSavedRate = effectiveRate },
            onDismiss = {}
        )
        MoneyAssertions.numeric("3.755", freshSavedRate!!)

        // Setup dialog helpers preserve exact fractions & reciprocals
        assertEquals(
            "0.266666666667",
            ExchangeRateInputHelper.computeInitialRateInput("0.266666666667", "$", "$")
        )
        assertEquals("3.755", ExchangeRateInputHelper.computeInitialRateInput("3.755", "$", "$"))
        assertEquals("0.266311584554", ExchangeRateInputHelper.flipRateInput("3.755"))
    }

    @Test
    fun pasteSanitizationFormattingAndHalfEvenMathEvaluatorWorkWithoutDrift() {
        // Thousands comma in paste must strip cleanly, not become a decimal dot!
        assertEquals("1034.50", HabayebMathHelper.sanitizeAmountInput("1,034.50"))
        MoneyAssertions.numeric("1034.50", CurrencyConfig.parseBigDecimalOrNull("1,034.50")!!)
        // Arabic/Persian digits and Arabic decimal mark
        assertEquals("500.75", HabayebMathHelper.sanitizeAmountInput("٥٠٠٫٧٥"))
        assertEquals("100.1234", HabayebMathHelper.sanitizeAmountInput("100.1234"))
        assertNull(HabayebMathHelper.sanitizeAmountInput("100.12345"))

        // Formatters must never hide digits 3 & 4
        assertEquals("12.3456", HabayebMathHelper.formatSmart(BigDecimal("12.3456")))
        assertEquals("100", HabayebMathHelper.formatSmart(BigDecimal("100.0000")))
        assertEquals("1,000.1234", FormatUtils.formatBigDecimal(BigDecimal("1000.1234"), "").trim())

        // MathEvaluator uses HALF_EVEN (banker's rounding) instead of HALF_UP:
        // 1 / 20000000000 = 0.00000000005 -> HALF_EVEN rounds 10th digit to even 0; HALF_UP would produce 1
        assertEquals(BigDecimal("0.0000000000"), evaluateSimpleExpression("1 / 20000000000"))
        // 3 / 20000000000 = 0.00000000015 -> HALF_EVEN rounds 10th digit to even 2
        assertEquals(BigDecimal("0.0000000002"), evaluateSimpleExpression("3 / 20000000000"))
    }

    @Test
    fun runningBalanceRowStateShowsAccurateDebtBalancedAndFlippedCredit() {
        val tx1 = HabayebTransaction(
            id = "r1", customerId = "c", type = TransactionType.OWED_BY_THEM.value,
            amount = BigDecimal("100.25"), timestamp = 10L, description = "tx1",
            currencyCode = "ر.س", baseCurrencyCode = "ر.س"
        )
        val tx2 = HabayebTransaction(
            id = "r2", customerId = "c", type = TransactionType.PAYMENT_BY_THEM.value,
            amount = BigDecimal("100.25"), timestamp = 20L, description = "tx2",
            currencyCode = "ر.س", baseCurrencyCode = "ر.س"
        )
        val tx3 = HabayebTransaction(
            id = "r3", customerId = "c", type = TransactionType.PAYMENT_BY_THEM.value,
            amount = BigDecimal("25.50"), timestamp = 30L, description = "tx3",
            currencyCode = "ر.س", baseCurrencyCode = "ر.س"
        )

        val history = CustomerHistoryCalculator.calculate(listOf(tx1, tx2, tx3), "ر.س", null)
        MoneyAssertions.exact("100.2500", history.runningBalances["r1"]!!)
        MoneyAssertions.exact("0.0000", history.runningBalances["r2"]!!)
        MoneyAssertions.exact("-25.5000", history.runningBalances["r3"]!!)

        val state1 = CustomerTransactionRowStateCalculator.calculate(
            tx1, "ر.س", TransactionType.OWED_BY_THEM.value, Color.Red, Color.Green, history.runningBalances["r1"]
        )
        val state2 = CustomerTransactionRowStateCalculator.calculate(
            tx2, "ر.س", TransactionType.OWED_BY_THEM.value, Color.Red, Color.Green, history.runningBalances["r2"]
        )
        val state3 = CustomerTransactionRowStateCalculator.calculate(
            tx3, "ر.س", TransactionType.OWED_BY_THEM.value, Color.Red, Color.Green, history.runningBalances["r3"]
        )

        assertEquals(1, state1.runningBalanceSign)
        assertEquals(R.string.habayeb_owed, state1.runningBalanceStatusResId)
        assertEquals("100.25 ر.س", state1.runningBalanceText)

        assertEquals(0, state2.runningBalanceSign)
        assertEquals(R.string.habayeb_status_balanced_short, state2.runningBalanceStatusResId)
        assertEquals("0 ر.س", state2.runningBalanceText)

        assertEquals(-1, state3.runningBalanceSign)
        assertEquals(R.string.habayeb_to_them, state3.runningBalanceStatusResId)
        assertEquals("25.5 ر.س", state3.runningBalanceText)
    }

    @Test
    fun shareHelperAndTrashParserPreserveExactFourDecimalAmountsAndFullRates() {
        val cust = HabayebCustomer(
            id = "cust-share",
            name = "Share Customer",
            phone = "",
            notes = "",
            createdAt = 1L,
            initialType = TransactionType.OWED_BY_THEM.value
        )
        val txs = listOf(
            HabayebTransaction(
                id = "s1", customerId = cust.id, type = TransactionType.OWED_BY_THEM.value,
                amount = BigDecimal("100.1234"), timestamp = 1L, description = "local",
                currencyCode = "ر.ي", baseCurrencyCode = "ر.ي"
            ),
            HabayebTransaction(
                id = "s2", customerId = cust.id, type = TransactionType.OWED_BY_THEM.value,
                amount = BigDecimal("25.5678"), foreignAmount = BigDecimal("25.5678"),
                isForeign = true, isRateCalculated = false,
                timestamp = 2L, description = "foreign",
                currencyCode = "ر.س", baseCurrencyCode = "ر.ي"
            )
        )

        val shareBody = CustomerShareHelper.buildStatementShareBody(
            context = context,
            customer = cust,
            netDebt = BigDecimal("100.1234"),
            currencySymbol = "ر.ي",
            allCustomerTxs = txs
        )
        assertTrue("Expected 4-decimal primary total in share body: $shareBody", shareBody.contains("100.1234"))
        assertTrue("Expected 4-decimal foreign total in share body: $shareBody", shareBody.contains("25.5678"))

        val deletedTxJson = """
            {
              "id": "dt1",
              "customerId": "cust-share",
              "type": "OWED_BY_THEM",
              "amount": "375.50",
              "timestamp": 1700000000,
              "description": "rate check",
              "is_foreign": true,
              "currency_code": "$",
              "foreign_amount": "100",
              "is_rate_calculated": true,
              "exchange_rate": "3.755",
              "equivalent_amount": "375.50",
              "base_currency_code": "ر.س"
            }
        """.trimIndent()
        val deletedItem = DeletedItemEntity(
            id = "trash-1",
            sourceSystem = "الحبايب",
            originalTableName = "habayeb_transactions",
            jsonData = deletedTxJson
        )
        val strings = TrashStrings(
            systemHabayeb = "الحبايب",
            unknownText = "unknown",
            noPhoneText = "",
            noNotesText = "",
            debtTxText = "",
            owedByThemText = "عليه",
            paymentByThemText = "سدد",
            owedToThemText = "له",
            paymentToThemText = "سلمنا",
            categoryLabelText = "%s",
            customerLabelText = "%s",
            equivalentInfoTemplate = "%1\$s (%2\$s)",
            progressTextTemplate = "%s",
            customerBundleDescTemplate = "%d",
            ledgerBundleDescTemplate = "%d",
            exchangeRateLabelText = ""
        )
        val parsed = TrashItemParser.parse(
            item = deletedItem,
            customersList = listOf(cust),
            currencySymbol = "ر.س",
            strings = strings,
            primaryColor = Color.Blue,
            secondaryColor = Color.Green,
            errorColor = Color.Red,
            outlineColor = Color.Gray
        )
        assertEquals("3.755", parsed.exchangeRateVal)

        val bundleJson = """
            {
              "customer": {"id": "c1", "name": "Share Customer", "phone": ""},
              "transactions": [
                {
                  "id": "bt1",
                  "type": "OWED_BY_THEM",
                  "amount": "26.6667",
                  "timestamp": 1700000000,
                  "description": "sar to usd",
                  "is_foreign": true,
                  "currency_code": "ر.س",
                  "foreign_amount": "100",
                  "is_rate_calculated": true,
                  "exchange_rate": "0.266666666667",
                  "equivalent_amount": "26.6667",
                  "base_currency_code": "$"
                }
              ]
            }
        """.trimIndent()
        val parsedBundle = TrashItemParser.parse(
            item = DeletedItemEntity(
                id = "trash-bundle",
                sourceSystem = "الحبايب",
                originalTableName = "habayeb_bundle",
                jsonData = bundleJson
            ),
            customersList = listOf(cust),
            currencySymbol = "$",
            strings = strings,
            primaryColor = Color.Blue,
            secondaryColor = Color.Green,
            errorColor = Color.Red,
            outlineColor = Color.Gray
        )
        assertEquals("0.266666666667", parsedBundle.bundleTransactions.single().exchangeRateText)
    }
}
