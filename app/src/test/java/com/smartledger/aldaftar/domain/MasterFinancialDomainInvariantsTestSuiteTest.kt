package com.smartledger.aldaftar.domain

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.smartledger.aldaftar.data.backup.BackupCrypto
import com.smartledger.aldaftar.data.backup.BackupEngine
import com.smartledger.aldaftar.data.local.AppDatabase
import com.smartledger.aldaftar.data.local.entities.AppSettings
import com.smartledger.aldaftar.data.local.entities.HabayebCustomer
import com.smartledger.aldaftar.data.local.entities.HabayebTransaction
import com.smartledger.aldaftar.data.repository.HabayebMutationRepository
import com.smartledger.aldaftar.data.repository.HabayebRepository
import com.smartledger.aldaftar.data.repository.RecurringRepository
import com.smartledger.aldaftar.data.repository.TrashRepository
import com.smartledger.aldaftar.data.serialization.pdf.PdfReportCalculator
import com.smartledger.aldaftar.domain.model.FinancialPolicy
import com.smartledger.aldaftar.domain.model.RecurringConfig
import com.smartledger.aldaftar.domain.model.TransactionType
import com.smartledger.aldaftar.domain.usecase.habayeb.HabayebFinancialCalculator
import com.smartledger.aldaftar.domain.usecase.habayeb.HabayebTransactionUseCase
import com.smartledger.aldaftar.ui.screens.habayeb.utils.CurrencyConfig
import com.smartledger.aldaftar.ui.screens.habayeb.utils.ExchangeRateHelper
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.UUID

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class MasterFinancialDomainInvariantsTestSuiteTest {

    private lateinit var context: Context
    private lateinit var db: AppDatabase
    private lateinit var habayebRepo: HabayebRepository
    private lateinit var mutationRepo: HabayebMutationRepository
    private lateinit var recurringRepo: RecurringRepository
    private lateinit var trashRepo: TrashRepository
    private lateinit var useCase: HabayebTransactionUseCase

    private val testCustomer = HabayebCustomer(
        id = "cust-011",
        name = "Al-Habib",
        phone = "777000000",
        notes = "Golden account",
        createdAt = 1000L,
        initialType = TransactionType.OWED_BY_THEM.value
    )

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        habayebRepo = HabayebRepository(db, db.habayebDao())
        mutationRepo = HabayebMutationRepository(db)
        recurringRepo = RecurringRepository(db, db.recurringConfigDao())
        trashRepo = TrashRepository(db.trashDao())
        useCase = HabayebTransactionUseCase(habayebRepo, mutationRepo)
        runBlocking { habayebRepo.insertCustomer(testCustomer) }
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun testI01_and_I02_originalAmountAndCurrencyAreImmutableAcrossDefaultChanges() = runBlocking {
        // I01: original amount is immutable unless explicitly edited
        // I02: original currency is independent from default currency
        useCase.addHabayebTransaction(
            customerId = testCustomer.id,
            type = TransactionType.OWED_BY_THEM.value,
            amount = BigDecimal("100.0000"),
            desc = "Test 100 SAR",
            timestamp = 1000L,
            isForeign = true,
            currencyCode = "ر.س",
            foreignAmount = BigDecimal("100.0000"),
            baseCurrencySymbol = "ر.ي"
        )
        val tx = habayebRepo.getAllTransactionsDirect().single()
        assertEquals(0, BigDecimal("100.0000").compareTo(tx.originalAmount))
        assertEquals("ر.س", tx.originalCurrency)

        // Change default currency across YER -> SAR -> USD -> YER
        listOf("ر.س", "$", "ر.ي").forEach { newDefault ->
            val balances = db.habayebDao().getAllCustomerBalancesFlow(newDefault).first()
            val state = HabayebFinancialCalculator.calculateCustomersUiState(
                listOf(testCustomer), balances, AppSettings(currencySymbol = newDefault)
            ).customers.single()

            val txAfter = habayebRepo.getHabayebTransactionById(tx.id)!!
            assertEquals("Original amount must not change", 0, BigDecimal("100.0000").compareTo(txAfter.originalAmount))
            assertEquals("Original currency must remain SAR", "ر.س", txAfter.originalCurrency)
            assertEquals("Foreign amount must remain 100", 0, BigDecimal("100.0000").compareTo(txAfter.foreignAmount))
        }
    }

    @Test
    fun testI03_and_I04_foreignIsPresentationClassificationAndNotTrash() = runBlocking {
        // I03: foreign/local is presentation/context classification
        // I04: foreign != trash
        val tx = HabayebTransaction(
            id = "tx-sar",
            customerId = testCustomer.id,
            type = TransactionType.OWED_BY_THEM.value,
            amount = BigDecimal("100.0000"),
            timestamp = 1000L,
            description = "Unconverted SAR",
            isForeign = true,
            currencyCode = "ر.س",
            foreignAmount = BigDecimal("100.0000"),
            baseCurrencyCode = "ر.ي"
        )
        habayebRepo.insertHabayebTransaction(tx)

        // Under default YER: tx is foreign
        assertTrue(tx.isForeignUnder("ر.ي"))
        // Under default SAR: tx is local
        assertFalse(tx.isForeignUnder("ر.س"))
        // Under default USD: tx is foreign
        assertTrue(tx.isForeignUnder("$"))

        // Customer with only foreign balance is NOT closed (not trash)
        val balances = db.habayebDao().getAllCustomerBalancesFlow("ر.ي").first()
        val customerState = HabayebFinancialCalculator.calculateCustomersUiState(
            listOf(testCustomer), balances, AppSettings(currencySymbol = "ر.ي")
        ).customers.single()

        assertFalse("Foreign-only balance customer must not be marked closed", customerState.isClosed)
        assertEquals("ر.س", customerState.displayCurrencySymbol)
        assertEquals(0, BigDecimal("100.0000").compareTo(customerState.displayNetDebt))
    }

    @Test
    fun testI05_and_I06_exchangeDirectionDeterminesConversionAndInverseDerived() {
        // I05: exchange direction determines conversion direction
        // I06: inverse is derived from the same pair
        val rate = BigDecimal("140.0000") // 1 SAR = 140 YER
        val convertedForward = FinancialPolicy.convertDirectedAmount(
            amount = BigDecimal("100"),
            sourceCurrency = "ر.س",
            targetCurrency = "ر.ي",
            rate = rate,
            rateSourceCurrency = "ر.س",
            rateTargetCurrency = "ر.ي"
        )
        assertEquals(0, BigDecimal("14000.0000").compareTo(convertedForward))

        val convertedInverse = FinancialPolicy.convertDirectedAmount(
            amount = BigDecimal("14000"),
            sourceCurrency = "ر.ي",
            targetCurrency = "ر.س",
            rate = rate,
            rateSourceCurrency = "ر.س",
            rateTargetCurrency = "ر.ي"
        )
        assertEquals(0, BigDecimal("100.0000").compareTo(convertedInverse))

        // Authoritative forward rate in JSON
        val json = ExchangeRateHelper.setRate("{}", "ر.س", "ر.ي", BigDecimal("140"))
        val forwardRate = ExchangeRateHelper.getRateBigDecimal(json, "ر.س", "ر.ي")
        val derivedInverse = ExchangeRateHelper.getRateBigDecimal(json, "ر.ي", "ر.س")

        assertEquals(0, BigDecimal("140").compareTo(forwardRate))
        val product = forwardRate.multiply(derivedInverse).setScale(6, RoundingMode.HALF_EVEN)
        assertEquals(0, BigDecimal.ONE.setScale(6).compareTo(product))
    }

    @Test
    fun testI07_and_I08_missingRateIsNotOneAndNoCrossRate() {
        // I07: missing rate != 1
        // I08: no implicit cross-rate
        val jsonWithRates = ExchangeRateHelper.setRate(
            ExchangeRateHelper.setRate("{}", "ر.س", "ر.ي", BigDecimal("140")),
            "$", "ر.ي", BigDecimal("550")
        )

        assertFalse("Cross-rate SAR->USD must NOT exist silently", ExchangeRateHelper.hasRate(jsonWithRates, "ر.س", "$"))
        val missingRate = ExchangeRateHelper.getRateBigDecimal(jsonWithRates, "ر.س", "$")
        assertEquals(0, BigDecimal.ZERO.compareTo(missingRate))

        assertThrows(IllegalArgumentException::class.java) {
            FinancialPolicy.convertDirectedAmount(
                amount = BigDecimal("100"),
                sourceCurrency = "ر.س",
                targetCurrency = "$",
                rate = BigDecimal.ZERO,
                rateSourceCurrency = "ر.س",
                rateTargetCurrency = "$"
            )
        }
    }

    @Test
    fun testI09_and_I10_historicalRateImmutabilityWhenCurrentRateChanges() = runBlocking {
        // I09: historical rate remains historical
        // I10: current rate does not rewrite history
        val tx = HabayebTransaction(
            id = "tx-hist",
            customerId = testCustomer.id,
            type = TransactionType.OWED_BY_THEM.value,
            amount = BigDecimal("14000.0000"),
            timestamp = 1000L,
            description = "100 SAR converted to 14,000 YER",
            isForeign = true,
            currencyCode = "ر.س",
            foreignAmount = BigDecimal("100.0000"),
            exchangeRate = BigDecimal("140.0000"),
            isRateCalculated = true,
            equivalentAmount = BigDecimal("14000.0000"),
            baseCurrencyCode = "ر.ي"
        )
        habayebRepo.insertHabayebTransaction(tx)

        // Settings rate changes from 140 -> 160 -> 170 -> 130
        listOf("160", "170", "130").forEach { newRateStr ->
            val updatedJson = ExchangeRateHelper.setRate("{}", "ر.س", "ر.ي", BigDecimal(newRateStr))
            db.settingsDao().insertOrUpdateSettings(AppSettings(exchangeRatesJson = updatedJson))

            val txAfter = habayebRepo.getHabayebTransactionById(tx.id)!!
            assertEquals("Historical rate must stay 140", 0, BigDecimal("140.0000").compareTo(txAfter.exchangeRate))
            assertEquals("Historical equivalent must stay 14000", 0, BigDecimal("14000.0000").compareTo(txAfter.equivalentAmount))
            assertEquals("Historical amount must stay 14000", 0, BigDecimal("14000.0000").compareTo(txAfter.amount))
            assertEquals("Historical foreign amount must stay 100", 0, BigDecimal("100.0000").compareTo(txAfter.foreignAmount))
        }
    }

    @Test
    fun testI11_I12_I13_recurringSnapshotAndIndependence() = runBlocking {
        // I11: fixed recurring template is a financial snapshot
        // I12: fixed recurring generation does not query current rate
        // I13: editing source transaction does not silently edit template
        val calStart = java.util.Calendar.getInstance().apply {
            clear(); set(2026, 0, 1, 0, 0, 0)
        }.timeInMillis
        val calEnd = calStart + 86_400_000L
        val template = RecurringConfig(
            id = "rec-tpl-1",
            originalTxId = "tx-source-1",
            customerId = testCustomer.id,
            customerName = testCustomer.name,
            amount = BigDecimal("14000.0000"),
            type = TransactionType.OWED_BY_THEM.value,
            description = "Monthly Rent 100 SAR",
            frequency = "DAILY",
            daysOfWeek = emptyList(),
            daysOfMonth = emptyList(),
            timeHour = 0,
            timeMinute = 0,
            startDateMillis = calStart,
            endDateMillis = calEnd,
            lastExecutedTimestamp = 0L,
            isForeign = true,
            currencyCode = "ر.س",
            foreignAmount = BigDecimal("100.0000"),
            exchangeRate = BigDecimal("140.0000"),
            isRateCalculated = true,
            equivalentAmount = BigDecimal("14000.0000"),
            baseCurrencyCode = "ر.ي"
        )
        recurringRepo.save(template)

        // Change current market exchange rate
        db.settingsDao().insertOrUpdateSettings(
            AppSettings(exchangeRatesJson = ExchangeRateHelper.setRate("{}", "ر.س", "ر.ي", BigDecimal("190.0000")))
        )

        // Execute recurring: must generate copies with frozen 140 rate, not 190
        val generatedCount = recurringRepo.executeDue(calEnd)
        assertTrue(generatedCount > 0)

        val generatedTxs = db.habayebDao().getAllTransactionsDirect().filter { it.linkedMainTxId == "tx-source-1" }
        assertTrue(generatedTxs.isNotEmpty())
        generatedTxs.forEach { gen ->
            assertEquals("ر.س", gen.currencyCode)
            assertEquals("ر.ي", gen.baseCurrencyCode)
            assertEquals(0, BigDecimal("100.0000").compareTo(gen.foreignAmount))
            assertEquals(0, BigDecimal("140.0000").compareTo(gen.exchangeRate))
            assertEquals(0, BigDecimal("14000.0000").compareTo(gen.equivalentAmount))
        }

        // I13: editing source transaction does not change template
        val sourceTx = HabayebTransaction(
            id = "tx-source-1",
            customerId = testCustomer.id,
            type = TransactionType.OWED_BY_THEM.value,
            amount = BigDecimal("14000.0000"),
            timestamp = 1000L,
            description = "Original",
            isForeign = true,
            currencyCode = "ر.س",
            foreignAmount = BigDecimal("100.0000"),
            exchangeRate = BigDecimal("140.0000"),
            isRateCalculated = true,
            equivalentAmount = BigDecimal("14000.0000"),
            baseCurrencyCode = "ر.ي"
        )
        habayebRepo.insertHabayebTransaction(sourceTx)

        // Edit source transaction to 200 SAR
        habayebRepo.updateHabayebTransaction(
            sourceTx.copy(
                foreignAmount = BigDecimal("200.0000"),
                amount = BigDecimal("28000.0000"),
                equivalentAmount = BigDecimal("28000.0000")
            )
        )

        val templateAfter = recurringRepo.byOriginalTransaction("tx-source-1")!!
        assertEquals("Template snapshot amount must not change", 0, BigDecimal("14000.0000").compareTo(templateAfter.amount))
        assertEquals("Template snapshot foreign amount must not change", 0, BigDecimal("100.0000").compareTo(templateAfter.foreignAmount))
    }

    @Test
    fun testI14_and_I15_transactionDeletionDoesNotDeleteTemplateAndViceVersa() = runBlocking {
        // I14: deleting generated or source transaction does not delete template
        // I15: deleting template does not delete generated historical transactions
        val template = RecurringConfig(
            id = "rec-tpl-2",
            originalTxId = "tx-source-2",
            customerId = testCustomer.id,
            customerName = testCustomer.name,
            amount = BigDecimal("100.0000"),
            type = TransactionType.OWED_BY_THEM.value,
            description = "Test recurring",
            frequency = "DAILY",
            daysOfWeek = emptyList(),
            daysOfMonth = emptyList(),
            timeHour = 9,
            timeMinute = 0,
            startDateMillis = 1000L,
            endDateMillis = 50000L,
            lastExecutedTimestamp = 0L,
            currencyCode = "ر.ي"
        )
        recurringRepo.save(template)

        // Source transaction
        val sourceTx = HabayebTransaction(
            id = "tx-source-2", customerId = testCustomer.id, type = TransactionType.OWED_BY_THEM.value,
            amount = BigDecimal("100.0000"), timestamp = 1000L, description = "Source"
        )
        habayebRepo.insertHabayebTransaction(sourceTx)

        // Generate a transaction from template
        val genTx = HabayebTransaction(
            id = "tx-gen-1", customerId = testCustomer.id, type = TransactionType.OWED_BY_THEM.value,
            amount = BigDecimal("100.0000"), timestamp = 2000L, description = "Generated",
            linkedMainTxId = "tx-source-2"
        )
        habayebRepo.insertHabayebTransaction(genTx)

        // Delete generated transaction to trash -> template must NOT be deleted
        mutationRepo.deleteTransactionToTrash("tx-gen-1", saveToTrash = true)
        assertNotNull(recurringRepo.byOriginalTransaction("tx-source-2"))

        // Delete source transaction to trash -> template must NOT be deleted
        mutationRepo.deleteTransactionToTrash("tx-source-2", saveToTrash = true)
        assertNotNull(recurringRepo.byOriginalTransaction("tx-source-2"))

        // Re-insert generated transaction, then delete template -> generated transaction must NOT be deleted
        habayebRepo.insertHabayebTransaction(genTx)
        recurringRepo.delete("rec-tpl-2")
        assertNotNull(habayebRepo.getHabayebTransactionById("tx-gen-1"))
    }

    @Test
    fun testI16_and_I17_trashAndRestorePreservesFinancialTruth() = runBlocking {
        // I16: trash changes record state, not financial truth
        // I17: restore preserves financial truth
        val tx = HabayebTransaction(
            id = "tx-trash-restore",
            customerId = testCustomer.id,
            type = TransactionType.OWED_BY_THEM.value,
            amount = BigDecimal("14000.0000"),
            timestamp = 5000L,
            description = "Trash test 100 SAR",
            isForeign = true,
            currencyCode = "ر.س",
            foreignAmount = BigDecimal("100.0000"),
            exchangeRate = BigDecimal("140.0000"),
            isRateCalculated = true,
            equivalentAmount = BigDecimal("14000.0000"),
            baseCurrencyCode = "ر.ي"
        )
        habayebRepo.insertHabayebTransaction(tx)

        // Soft delete to trash
        mutationRepo.deleteTransactionToTrash(tx.id, saveToTrash = true)
        assertNull(habayebRepo.getHabayebTransactionById(tx.id))

        // Change rates in world while in trash
        db.settingsDao().insertOrUpdateSettings(
            AppSettings(exchangeRatesJson = ExchangeRateHelper.setRate("{}", "ر.س", "ر.ي", BigDecimal("220.0000")))
        )

        // Restore from trash
        val trashItem = db.trashDao().getDeletedItemByIdDirect(tx.id)!!
        trashRepo.restoreDeletedItem(trashItem)

        val restored = habayebRepo.getHabayebTransactionById(tx.id)!!
        assertEquals("Restored original amount must match", 0, BigDecimal("100.0000").compareTo(restored.originalAmount))
        assertEquals("Restored original currency must match", "ر.س", restored.originalCurrency)
        assertEquals("Restored exchange rate must not change", 0, BigDecimal("140.0000").compareTo(restored.exchangeRate))
        assertEquals("Restored equivalent must not change", 0, BigDecimal("14000.0000").compareTo(restored.equivalentAmount))
    }

    @Test
    fun testSection36_GrandMission011MasterScenario(): Unit = runBlocking {
        // Strict verification of Mission 011 Section 36 requirement:
        // 1. Default = YER
        // 2. Transaction: 100 SAR, Exchange: SAR -> YER, Rate = 140, Equivalent = 14,000 YER
        // 3. Fixed recurring = ON
        // 4. Default currency changes: YER -> SAR -> USD -> YER -> SAR
        // 5. Rate changes to 160, 170, 130
        // 6. Create recurring copies
        // 7. Trash one copy, restore it
        // 8. Create backup
        // 9. Change everything again
        // 10. Restart app (new DB instance)
        // 11. Restore backup
        // 12. Check: original, template, copies, trash, restored, reports, balances!

        db.settingsDao().insertOrUpdateSettings(
            AppSettings(
                currencySymbol = "ر.ي",
                exchangeRatesJson = ExchangeRateHelper.setRate("{}", "ر.س", "ر.ي", BigDecimal("140.0000"))
            )
        )

        // Create transaction: 100 SAR -> 14,000 YER
        val origTxId = "master-tx-011"
        val origTx = HabayebTransaction(
            id = origTxId,
            customerId = testCustomer.id,
            type = TransactionType.OWED_BY_THEM.value,
            amount = BigDecimal("14000.0000"),
            timestamp = 1000L,
            description = "Master Golden Scenario",
            isForeign = true,
            currencyCode = "ر.س",
            foreignAmount = BigDecimal("100.0000"),
            exchangeRate = BigDecimal("140.0000"),
            isRateCalculated = true,
            equivalentAmount = BigDecimal("14000.0000"),
            baseCurrencyCode = "ر.ي"
        )
        habayebRepo.insertHabayebTransaction(origTx)

        // Fixed recurring = ON
        val tplId = "master-rec-011"
        val calStart2 = java.util.Calendar.getInstance().apply {
            clear(); set(2026, 0, 1, 0, 0, 0)
        }.timeInMillis
        val calEnd2 = calStart2 + 86_400_000L
        val recurringTemplate = RecurringConfig(
            id = tplId,
            originalTxId = origTxId,
            customerId = testCustomer.id,
            customerName = testCustomer.name,
            amount = BigDecimal("14000.0000"),
            type = TransactionType.OWED_BY_THEM.value,
            description = "Master Recurring",
            frequency = "DAILY",
            daysOfWeek = emptyList(),
            daysOfMonth = emptyList(),
            timeHour = 0,
            timeMinute = 0,
            startDateMillis = calStart2,
            endDateMillis = calEnd2,
            lastExecutedTimestamp = 0L,
            isForeign = true,
            currencyCode = "ر.س",
            foreignAmount = BigDecimal("100.0000"),
            exchangeRate = BigDecimal("140.0000"),
            isRateCalculated = true,
            equivalentAmount = BigDecimal("14000.0000"),
            baseCurrencyCode = "ر.ي"
        )
        recurringRepo.save(recurringTemplate)

        // Default currency changes: YER -> SAR -> USD -> YER -> SAR
        val cycles = listOf("ر.س", "$", "ر.ي", "ر.س")
        for (c in cycles) {
            val s = db.settingsDao().getSettingsDirect()!!
            db.settingsDao().insertOrUpdateSettings(s.copy(currencySymbol = c))
        }

        // Change rates to 160, 170, 130
        for (r in listOf("160.0000", "170.0000", "130.0000")) {
            val s = db.settingsDao().getSettingsDirect()!!
            val newJson = ExchangeRateHelper.setRate(s.exchangeRatesJson, "ر.س", "ر.ي", BigDecimal(r))
            db.settingsDao().insertOrUpdateSettings(s.copy(exchangeRatesJson = newJson))
        }

        // Generate recurring copies
        val generated = recurringRepo.executeDue(calEnd2)
        assertTrue("Must generate recurring copies", generated > 0)

        val copies = db.habayebDao().getAllTransactionsDirect().filter { it.linkedMainTxId == origTxId }
        assertTrue("Copies must exist", copies.isNotEmpty())

        // Trash one copy, then restore it
        val copyToTrash = copies.first()
        mutationRepo.deleteTransactionToTrash(copyToTrash.id, saveToTrash = true)
        val trashItem = db.trashDao().getDeletedItemByIdDirect(copyToTrash.id)!!
        trashRepo.restoreDeletedItem(trashItem)

        // Create backup
        val backupEngine = BackupEngine(context, db)
        val backupFile = File(context.cacheDir, "master_mission_011.backup")
        backupEngine.create(backupFile)
        assertTrue(backupFile.exists())

        // Mutate everything in current DB
        db.settingsDao().insertOrUpdateSettings(
            AppSettings(currencySymbol = "$", exchangeRatesJson = "{}")
        )
        db.habayebDao().clearAllTransactions()

        // Restart app (simulate fresh Room database)
        db.close()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        val freshBackupEngine = BackupEngine(context, db)

        // Restore backup
        freshBackupEngine.restore(backupFile)

        // Verify Original Transaction
        val finalOrigTx = db.habayebDao().getTransactionById(origTxId)
        assertNotNull("Original transaction must be restored", finalOrigTx)
        assertEquals("Original amount must stay 100", 0, BigDecimal("100.0000").compareTo(finalOrigTx!!.foreignAmount))
        assertEquals("Original currency must stay SAR", "ر.س", finalOrigTx.currencyCode)
        assertEquals("Historical rate must stay 140", 0, BigDecimal("140.0000").compareTo(finalOrigTx.exchangeRate))
        assertEquals("Historical equivalent must stay 14,000", 0, BigDecimal("14000.0000").compareTo(finalOrigTx.equivalentAmount))
        assertEquals("Historical base currency must stay YER", "ر.ي", finalOrigTx.baseCurrencyCode)

        // Verify Recurring Template
        val finalTpl = db.recurringConfigDao().all().single()
        assertEquals(0, BigDecimal("100.0000").compareTo(finalTpl.foreignAmount))
        assertEquals("ر.س", finalTpl.currencyCode)
        assertEquals(0, BigDecimal("140.0000").compareTo(finalTpl.exchangeRate))
        assertEquals(0, BigDecimal("14000.0000").compareTo(finalTpl.equivalentAmount))
        assertEquals("ر.ي", finalTpl.baseCurrencyCode)

        // Verify Generated Copies
        val finalCopies = db.habayebDao().getAllTransactionsDirect().filter { it.linkedMainTxId == origTxId }
        assertTrue(finalCopies.isNotEmpty())
        finalCopies.forEach { copy ->
            assertEquals(0, BigDecimal("100.0000").compareTo(copy.foreignAmount))
            assertEquals("ر.س", copy.currencyCode)
            assertEquals(0, BigDecimal("140.0000").compareTo(copy.exchangeRate))
            assertEquals(0, BigDecimal("14000.0000").compareTo(copy.equivalentAmount))
            assertEquals("ر.ي", copy.baseCurrencyCode)
        }

        // Verify Reports use the same domain truth
        val report = PdfReportCalculator.calculateSingleCustomerReport(
            listOf(finalOrigTx),
            currencySymbol = "ر.ي"
        )
        assertEquals("Report must calculate exact 14000 net debt", 0, BigDecimal("14000.0000").compareTo(report.calculatedNetDebt))
        assertEquals(0, BigDecimal("14000.0000").compareTo(report.totalDebtsBase))

        backupFile.delete()
    }
}
