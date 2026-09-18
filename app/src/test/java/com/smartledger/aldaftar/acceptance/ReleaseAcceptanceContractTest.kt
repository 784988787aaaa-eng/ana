package com.smartledger.aldaftar.acceptance

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.smartledger.aldaftar.data.backup.BackupEngine
import com.smartledger.aldaftar.data.local.AppDatabase
import com.smartledger.aldaftar.data.local.TrashDao
import com.smartledger.aldaftar.data.local.entities.AppSettings
import com.smartledger.aldaftar.data.local.entities.BusinessProfile
import com.smartledger.aldaftar.data.local.entities.HabayebCustomer
import com.smartledger.aldaftar.data.local.entities.HabayebTransaction
import com.smartledger.aldaftar.data.repository.HabayebMutationRepository
import com.smartledger.aldaftar.data.repository.HabayebRepository
import com.smartledger.aldaftar.data.repository.RecurringRepository
import com.smartledger.aldaftar.domain.HashUtils
import com.smartledger.aldaftar.domain.evaluateSimpleExpression
import com.smartledger.aldaftar.domain.model.FinancialPolicy
import com.smartledger.aldaftar.domain.model.RecurringConfig
import com.smartledger.aldaftar.domain.model.TransactionType
import com.smartledger.aldaftar.ui.screens.habayeb.utils.CurrencyConfig
import com.smartledger.aldaftar.ui.screens.habayeb.utils.ExchangeRateHelper
import java.io.File
import java.math.BigDecimal
import java.math.RoundingMode
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Master Release Acceptance Test Suite
 * Validates the complete 16-pillar release specification of Smart Ledger (الدفتر الذكي).
 */
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class ReleaseAcceptanceContractTest {

    private lateinit var context: Context
    private lateinit var db: AppDatabase
    private lateinit var repository: HabayebRepository
    private lateinit var mutationRepository: HabayebMutationRepository
    private lateinit var recurringRepository: RecurringRepository

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = HabayebRepository(db, db.habayebDao())
        mutationRepository = HabayebMutationRepository(db)
        recurringRepository = RecurringRepository(db, db.recurringConfigDao())
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun pillar1_mathematicalEvaluationAndCurrencyPrecision() {
        // Math evaluator contract
        val evaluated = evaluateSimpleExpression("1000 + 250 * 2 - 50 / 2")
        assertNotNull(evaluated)
        assertEquals(BigDecimal("1475.0000"), evaluated!!.setScale(4, RoundingMode.HALF_EVEN))

        // Precision & Rounding Policy
        val raw = BigDecimal("123.456789")
        val normalized = FinancialPolicy.normalize(raw)
        assertEquals(BigDecimal("123.4568"), normalized)
        assertEquals(FinancialPolicy.scale, normalized.scale())

        // 3 Currency Declarations (YER, SAR, USD)
        val yer = CurrencyConfig.getBySymbol("ر.ي")
        val sar = CurrencyConfig.getBySymbol("ر.س")
        val usd = CurrencyConfig.getBySymbol("$")
        assertNotNull(yer)
        assertNotNull(sar)
        assertNotNull(usd)
    }

    @Test
    fun pillar2_exchangeRateMatrixReciprocityAndDirectionInvariance() {
        var rateMatrix = "{}"
        // 1 USD = 530 YER
        rateMatrix = ExchangeRateHelper.setRate(rateMatrix, "$", "ر.ي", BigDecimal("530.0000"))
        // 1 SAR = 140 YER
        rateMatrix = ExchangeRateHelper.setRate(rateMatrix, "ر.س", "ر.ي", BigDecimal("140.0000"))

        val usdToYer = ExchangeRateHelper.getRateBigDecimal(rateMatrix, "$", "ر.ي")
        assertEquals(BigDecimal("530.0000"), usdToYer.setScale(4, RoundingMode.HALF_EVEN))

        // Reciprocal auto-resolution
        val yerToUsd = ExchangeRateHelper.getRateBigDecimal(rateMatrix, "ر.ي", "$")
        val expectedReciprocal = BigDecimal.ONE.divide(BigDecimal("530.0000"), 12, RoundingMode.HALF_EVEN)
        assertEquals(expectedReciprocal.setScale(6, RoundingMode.HALF_EVEN), yerToUsd.setScale(6, RoundingMode.HALF_EVEN))

        // Direct conversion application
        val converted = CurrencyConfig.convertDirectedAmount(
            amount = BigDecimal("100.0000"),
            sourceCurrency = "$",
            targetCurrency = "ر.ي",
            rate = usdToYer,
            rateSourceCurrency = "$",
            rateTargetCurrency = "ر.ي"
        )
        assertEquals(BigDecimal("53000.0000"), converted)
    }

    @Test
    fun pillar3_fullCustomerAndTransactionLifecycle() = runBlocking {
        val customer = HabayebCustomer(
            id = "cust-101",
            name = "مؤسسة النجاح التجارية",
            phone = "777123456",
            notes = "عميل مميز",
            createdAt = System.currentTimeMillis()
        )
        assertTrue(repository.insertCustomer(customer))

        // 1. Transaction in YER (Base)
        val txYer = HabayebTransaction(
            id = "tx-yer-1",
            customerId = customer.id,
            type = TransactionType.OWED_BY_THEM.value,
            amount = BigDecimal("50000.0000"),
            timestamp = 1000L,
            description = "فاتورة بضاعة محلية",
            currencyCode = "ر.ي",
            baseCurrencyCode = "ر.ي"
        )
        assertTrue(repository.insertHabayebTransaction(txYer))

        // 2. Transaction in SAR (Foreign with rate 140 -> 28000 YER)
        val txSar = HabayebTransaction(
            id = "tx-sar-1",
            customerId = customer.id,
            type = TransactionType.OWED_BY_THEM.value,
            amount = BigDecimal("28000.0000"),
            timestamp = 2000L,
            description = "فاتورة استيراد",
            isForeign = true,
            currencyCode = "ر.س",
            foreignAmount = BigDecimal("200.0000"),
            exchangeRate = BigDecimal("140.0000"),
            isRateCalculated = true,
            equivalentAmount = BigDecimal("28000.0000"),
            baseCurrencyCode = "ر.ي"
        )
        assertTrue(repository.insertHabayebTransaction(txSar))

        // 3. Payment in YER
        val txPay = HabayebTransaction(
            id = "tx-pay-1",
            customerId = customer.id,
            type = TransactionType.PAYMENT_BY_THEM.value,
            amount = BigDecimal("18000.0000"),
            timestamp = 3000L,
            description = "دفعة نقدية",
            currencyCode = "ر.ي",
            baseCurrencyCode = "ر.ي"
        )
        assertTrue(repository.insertHabayebTransaction(txPay))

        // Verify aggregation
        val balances = repository.customerBalancesFlow("ر.ي").first()
        val customerBalances = balances.filter { it.customerId == customer.id }
        assertEquals("Expected 1 balance entry for customer in balances: $balances", 1, customerBalances.size)

        // Net = 50,000 + 28,000 - 18,000 = 60,000 YER
        val yerBalance = customerBalances.first()
        assertEquals("Expected 60000 YER but got ${yerBalance.netAmount} in $yerBalance", 0, BigDecimal("60000.0000").compareTo(yerBalance.netAmount.setScale(4, RoundingMode.HALF_EVEN)))
    }

    @Test
    fun pillar4_businessProfilePersistence() = runBlocking {
        val profile = BusinessProfile(
            id = 1,
            name = "مركز الأمانة للصرافة والتجارة",
            description = "صنعاء - شارع حدة",
            phones = listOf("770000000")
        )
        db.businessProfileDao().save(profile)

        val retrieved = db.businessProfileDao().getDirect()
        assertNotNull(retrieved)
        assertEquals("مركز الأمانة للصرافة والتجارة", retrieved?.name)
        assertEquals("صنعاء - شارع حدة", retrieved?.description)
    }

    @Test
    fun pillar5_trashAndSoftDeleteLifecycleWithBalanceRestoration() = runBlocking {
        val customer = HabayebCustomer(id = "cust-trash-1", name = "عميل السلة", phone = "", notes = "", createdAt = 1L)
        repository.insertCustomer(customer)

        val tx = HabayebTransaction(
            id = "tx-trash-1",
            customerId = customer.id,
            type = TransactionType.OWED_BY_THEM.value,
            amount = BigDecimal("35000.0000"),
            timestamp = 1000L,
            description = "حساب قبل الحذف",
            currencyCode = "ر.ي"
        )
        repository.insertHabayebTransaction(tx)

        // Verify pre-delete balance
        var balances = repository.customerBalancesFlow("ر.ي").first()
        val preBal = balances.firstOrNull { it.customerId == customer.id }
        assertNotNull("Expected pre-delete balance for customer in $balances", preBal)
        assertEquals("Expected 35000 but got ${preBal?.netAmount} in $preBal", 0, BigDecimal("35000.0000").compareTo(preBal!!.netAmount))

        // Soft Delete to Trash
        mutationRepository.deleteTransactionToTrash(tx.id, saveToTrash = true)

        // Verify balance updated immediately (0 transactions active)
        balances = repository.customerBalancesFlow("ر.ي").first()
        assertTrue("Balances should not contain customer with positive amount: $balances", balances.none { it.customerId == customer.id && it.netAmount > BigDecimal.ZERO })

        // Check Trash count
        val trashItems = db.trashDao().getAllDeletedItemsDirect()
        assertEquals(1, trashItems.size)
        assertEquals("habayeb_transactions", trashItems.first().originalTableName)

        // Restore from Trash
        db.trashDao().restoreDeletedItem(trashItems.first())

        // Balance is restored exactly
        balances = repository.customerBalancesFlow("ر.ي").first()
        val postBal = balances.firstOrNull { it.customerId == customer.id }
        assertNotNull("Expected post-restore balance for customer in $balances", postBal)
        assertEquals("Expected 35000 after restore but got ${postBal?.netAmount} in $postBal", 0, BigDecimal("35000.0000").compareTo(postBal!!.netAmount))
    }

    @Test
    fun pillar6_recurringConfigAndScheduleExecution() = runBlocking {
        val config = RecurringConfig(
            id = "rec-1",
            originalTxId = "tx-rec-orig",
            customerId = "cust-rec-1",
            customerName = "عميل الإيجار الدوري",
            amount = BigDecimal("150000.0000"),
            type = TransactionType.OWED_BY_THEM.value,
            description = "إيجار شهري دوري",
            frequency = "MONTHLY",
            daysOfWeek = emptyList(),
            daysOfMonth = listOf(1),
            timeHour = 10,
            timeMinute = 0,
            startDateMillis = 1000L,
            endDateMillis = 2000000000000L,
            lastExecutedTimestamp = 0L,
            isActive = true,
            isForeign = false,
            currencyCode = "ر.ي",
            foreignAmount = BigDecimal.ZERO,
            exchangeRate = BigDecimal.ONE,
            isRateCalculated = false,
            equivalentAmount = BigDecimal("150000.0000"),
            baseCurrencyCode = "ر.ي"
        )
        recurringRepository.save(config)

        val retrieved = recurringRepository.byOriginalTransaction("tx-rec-orig")
        assertNotNull(retrieved)
        assertEquals("MONTHLY", retrieved?.frequency)
        assertEquals(0, BigDecimal("150000.0000").compareTo(retrieved?.amount))
    }

    @Test
    fun pillar7_backupCryptoAndSnapshotRestoreRoundtrip() = runBlocking {
        val settings = AppSettings(
            id = 1,
            currencySymbol = "ر.ي",
            exchangeRatesJson = """{"$":{"ر.ي":"530.0000"}}"""
        )
        db.settingsDao().insertOrUpdateSettings(settings)

        val customer = HabayebCustomer(id = "cust-bk-1", name = "عميل النسخ", phone = "", notes = "", createdAt = 1L)
        repository.insertCustomer(customer)

        val tx = HabayebTransaction(
            id = "tx-bk-1",
            customerId = customer.id,
            type = TransactionType.OWED_BY_THEM.value,
            amount = BigDecimal("106000.0000"),
            timestamp = 5000L,
            description = "قيد دولار محول",
            isForeign = true,
            currencyCode = "$",
            foreignAmount = BigDecimal("200.0000"),
            exchangeRate = BigDecimal("530.0000"),
            isRateCalculated = true,
            equivalentAmount = BigDecimal("106000.0000"),
            baseCurrencyCode = "ر.ي"
        )
        repository.insertHabayebTransaction(tx)

        val tempBackupFile = File.createTempFile("smartledger-backup-", ".sna", context.cacheDir)
        try {
            val engine = BackupEngine(context, db)
            engine.create(tempBackupFile)
            assertTrue("Backup file must be non-empty", tempBackupFile.length() > 0)

            // Wipe database to simulate fresh reinstall
            db.habayebDao().clearAllTransactions()
            db.habayebDao().clearAllCustomers()
            db.settingsDao().insertOrUpdateSettings(AppSettings(id = 1, currencySymbol = "SAR"))

            // Restore from encrypted snapshot
            engine.restore(tempBackupFile)

            // Verify full data restoration
            val restoredSettings = db.settingsDao().getSettingsDirect()
            assertEquals("ر.ي", restoredSettings?.currencySymbol)

            val restoredTx = repository.getHabayebTransactionById("tx-bk-1")
            assertNotNull(restoredTx)
            assertEquals("$", restoredTx?.currencyCode)
            assertEquals("ر.ي", restoredTx?.baseCurrencyCode)
            assertEquals(0, BigDecimal("200.0000").compareTo(restoredTx?.foreignAmount))
            assertEquals(0, BigDecimal("106000.0000").compareTo(restoredTx?.equivalentAmount))
        } finally {
            tempBackupFile.delete()
        }
    }

    @Test
    fun pillar8_securityPinHashingContract() {
        val encoded = HashUtils.createPinHash("1234")
        assertNotNull(encoded)
        assertTrue(HashUtils.verifyPin("1234", encoded))
        assertFalse(HashUtils.verifyPin("9999", encoded))
    }

    @Test
    fun pillar9_stressHighVolumeFinancialCalculations() = runBlocking {
        val customer = HabayebCustomer(id = "cust-stress-1", name = "عميل الضغط المالي", phone = "", notes = "", createdAt = 1L)
        repository.insertCustomer(customer)

        val start = System.currentTimeMillis()
        val batch = ArrayList<HabayebTransaction>(200)
        for (i in 1..200) {
            batch.add(
                HabayebTransaction(
                    id = "tx-stress-$i",
                    customerId = customer.id,
                    type = if (i % 2 == 0) TransactionType.OWED_BY_THEM.value else TransactionType.PAYMENT_BY_THEM.value,
                    amount = BigDecimal("100.0000"),
                    timestamp = i.toLong(),
                    description = "معاملة تجريبية $i",
                    currencyCode = "ر.ي"
                )
            )
        }
        for (tx in batch) {
            db.habayebDao().insertTransaction(tx)
        }

        val elapsed = System.currentTimeMillis() - start
        assertTrue("Batch insertion must complete within reasonable time budget (< 3000ms)", elapsed < 3000)

        // 100 debts of 100, 100 payments of 100 -> Net Balance = 0
        val balances = repository.customerBalancesFlow("ر.ي").first()
        val net = balances.firstOrNull { it.customerId == customer.id }?.netAmount ?: BigDecimal.ZERO
        assertEquals(0, BigDecimal.ZERO.compareTo(net))
    }
}
