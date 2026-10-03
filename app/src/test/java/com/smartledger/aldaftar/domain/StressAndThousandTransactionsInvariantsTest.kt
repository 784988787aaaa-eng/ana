package com.smartledger.aldaftar.domain

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.smartledger.aldaftar.data.backup.BackupEngine
import com.smartledger.aldaftar.data.local.AppDatabase
import com.smartledger.aldaftar.data.local.entities.AppSettings
import com.smartledger.aldaftar.data.local.entities.HabayebCustomer
import com.smartledger.aldaftar.data.local.entities.HabayebTransaction
import com.smartledger.aldaftar.domain.model.FinancialPolicy
import com.smartledger.aldaftar.domain.model.RecurringConfig
import com.smartledger.aldaftar.domain.model.TransactionType
import com.smartledger.aldaftar.domain.usecase.habayeb.HabayebFinancialCalculator
import com.smartledger.aldaftar.ui.screens.habayeb.utils.ExchangeRateHelper
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Random

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class StressAndThousandTransactionsInvariantsTest {

    private lateinit var context: Context
    private lateinit var db: AppDatabase

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun tearDown() {
        db.close()
    }

    data class ExpectedSnapshot(
        val id: String,
        val currency: String,
        val originalAmount: BigDecimal,
        val exchangeRate: BigDecimal,
        val isRateCalculated: Boolean,
        val equivalentAmount: BigDecimal,
        val baseCurrency: String
    )

    @Test
    fun testThousandTransactions_withHundredsOfCurrencyAndRateChangesAndRestarts(): Unit = runBlocking {
        // Setup initial rates and settings
        var ratesJson = "{}"
        ratesJson = ExchangeRateHelper.setRate(ratesJson, "ر.س", "ر.ي", BigDecimal("140.0000"))
        ratesJson = ExchangeRateHelper.setRate(ratesJson, "$", "ر.ي", BigDecimal("550.0000"))
        ratesJson = ExchangeRateHelper.setRate(ratesJson, "$", "ر.س", BigDecimal("3.7500"))

        db.settingsDao().insertOrUpdateSettings(
            AppSettings(currencySymbol = "ر.ي", exchangeRatesJson = ratesJson)
        )

        // Create 10 customers
        val customers = (1..10).map { i ->
            HabayebCustomer(
                id = "cust_$i",
                name = "Customer $i",
                phone = "777$i",
                notes = "Customer note",
                createdAt = 1000L + i,
                initialType = if (i % 2 == 0) TransactionType.OWED_BY_THEM.value else TransactionType.OWED_TO_THEM.value
            )
        }
        for (c in customers) {
            db.habayebDao().insertCustomer(c)
        }

        val currencies = listOf("ر.ي", "ر.س", "$")
        val types = listOf(
            TransactionType.OWED_BY_THEM.value,
            TransactionType.PAYMENT_BY_THEM.value,
            TransactionType.OWED_TO_THEM.value,
            TransactionType.PAYMENT_TO_THEM.value
        )

        val rng = Random(42)
        val expectedSnapshots = mutableMapOf<String, ExpectedSnapshot>()
        val allTxList = ArrayList<HabayebTransaction>(1000)

        // Generate 1000 transactions across 3 currencies, converted & unconverted, local & foreign
        for (i in 1..1000) {
            val txId = "tx_stress_$i"
            val customer = customers[i % customers.size]
            val txCurrency = currencies[i % currencies.size]
            val txType = types[i % types.size]
            val amountNum = BigDecimal((i % 500) + 1).setScale(4, RoundingMode.HALF_EVEN)

            val isForeign = txCurrency != "ر.ي"
            val isConverted = isForeign && (i % 2 == 0)

            val baseCurrency = "ر.ي"
            val (rate, eqAmount) = if (isConverted) {
                val r = if (txCurrency == "ر.س") BigDecimal("140.0000") else BigDecimal("550.0000")
                val eq = amountNum.multiply(r).setScale(4, RoundingMode.HALF_EVEN)
                Pair(r, eq)
            } else {
                Pair(BigDecimal.ZERO, BigDecimal.ZERO)
            }

            val tx = HabayebTransaction(
                id = txId,
                customerId = customer.id,
                type = txType,
                amount = if (isConverted) eqAmount else amountNum,
                timestamp = 10000L + i,
                description = "Tx stress #$i",
                isForeign = isForeign,
                currencyCode = txCurrency,
                foreignAmount = amountNum,
                exchangeRate = rate,
                isRateCalculated = isConverted,
                equivalentAmount = eqAmount,
                baseCurrencyCode = baseCurrency
            )
            allTxList.add(tx)
            expectedSnapshots[txId] = ExpectedSnapshot(
                id = txId,
                currency = txCurrency,
                originalAmount = amountNum,
                exchangeRate = rate,
                isRateCalculated = isConverted,
                equivalentAmount = eqAmount,
                baseCurrency = baseCurrency
            )
        }

        // Insert batch of 1000 transactions
        db.habayebDao().insertTransactionsBatch(allTxList)
        assertEquals(1000, db.habayebDao().getAllTransactionsDirect().size)

        // Perform 100+ default currency changes: YER -> SAR -> USD -> YER ...
        val currencyCycle = listOf("ر.ي", "ر.س", "$")
        for (step in 1..120) {
            val newDefault = currencyCycle[step % currencyCycle.size]
            val currSettings = db.settingsDao().getSettingsDirect()!!
            db.settingsDao().insertOrUpdateSettings(currSettings.copy(currencySymbol = newDefault))

            // Query balances under the new default
            val balances = db.habayebDao().getAllCustomerBalancesFlow(newDefault).first()
            assertTrue(balances.isNotEmpty())
        }

        // Perform 100+ rate changes in settings
        for (step in 1..100) {
            val currSettings = db.settingsDao().getSettingsDirect()!!
            val newSarRate = BigDecimal(130 + (step % 40))
            val newUsdRate = BigDecimal(500 + (step % 100))
            var json = currSettings.exchangeRatesJson
            json = ExchangeRateHelper.setRate(json, "ر.س", "ر.ي", newSarRate)
            json = ExchangeRateHelper.setRate(json, "$", "ر.ي", newUsdRate)
            db.settingsDao().insertOrUpdateSettings(currSettings.copy(exchangeRatesJson = json))
        }

        // Create backup of current state
        val backupFile = File(context.cacheDir, "stress_backup.backup")
        val backupEngine = BackupEngine(context, db)
        backupEngine.create(backupFile)
        assertTrue(backupFile.exists())

        // Simulate app restart with completely fresh DB
        db.close()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        val restartBackupEngine = BackupEngine(context, db)

        // Restore backup into the fresh DB
        restartBackupEngine.restore(backupFile)

        // Verify every single one of the 1,000 transactions in the restored DB
        val restoredTransactions = db.habayebDao().getAllTransactionsDirect()
        assertEquals(1000, restoredTransactions.size)

        for (tx in restoredTransactions) {
            val expected = expectedSnapshots[tx.id]
            assertNotNull("Transaction ${tx.id} must exist in snapshots", expected)
            assertEquals("Transaction ${tx.id} currencyCode must remain immutable", expected!!.currency, tx.currencyCode)
            assertEquals("Transaction ${tx.id} foreignAmount must remain immutable", 0, expected.originalAmount.compareTo(tx.foreignAmount))
            assertEquals("Transaction ${tx.id} exchangeRate must remain immutable", 0, expected.exchangeRate.compareTo(tx.exchangeRate))
            assertEquals("Transaction ${tx.id} isRateCalculated must match", expected.isRateCalculated, tx.isRateCalculated)
            assertEquals("Transaction ${tx.id} equivalentAmount must remain immutable", 0, expected.equivalentAmount.compareTo(tx.equivalentAmount))
            assertEquals("Transaction ${tx.id} baseCurrencyCode must remain immutable", expected.baseCurrency, tx.baseCurrencyCode)
        }

        backupFile.delete()
    }
}
