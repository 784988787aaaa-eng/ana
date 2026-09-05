package com.smartledger.aldaftar.data.serialization

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.smartledger.aldaftar.data.backup.BackupFileManager
import com.smartledger.aldaftar.data.backup.BackupOperationResult
import com.smartledger.aldaftar.data.backup.BackupService
import com.smartledger.aldaftar.data.local.AppDatabase
import com.smartledger.aldaftar.data.local.DatabaseMigrations
import com.smartledger.aldaftar.data.local.entities.AppSettings
import com.smartledger.aldaftar.data.local.entities.FixedCommitment
import com.smartledger.aldaftar.data.local.entities.HabayebCustomer
import com.smartledger.aldaftar.data.local.entities.HabayebTransaction
import com.smartledger.aldaftar.data.local.entities.TransactionDb
import com.smartledger.aldaftar.data.repository.BackupRepository
import com.smartledger.aldaftar.data.repository.FinanceRestoreService
import com.smartledger.aldaftar.domain.model.TransactionType
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
import java.io.File
import java.math.BigDecimal

/**
 * اختبارات استعادة وسلامة النسخ الاحتياطية والتكامل المعماري (Batch 4 Tests)
 */
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class BackupRestoreServiceTest {

    private lateinit var context: Context
    private lateinit var database: AppDatabase
    private lateinit var restoreService: FinanceRestoreService
    private lateinit var backupService: BackupService
    private lateinit var backupRepository: BackupRepository
    private lateinit var fileManager: BackupFileManager

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .addMigrations(*DatabaseMigrations.ALL_MIGRATIONS)
            .allowMainThreadQueries()
            .build()
        restoreService = FinanceRestoreService(database, context)
        fileManager = BackupFileManager(context)
        backupService = BackupService(context, database, fileManager)
        backupRepository = BackupRepository(context, database, backupService, fileManager, restoreService)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun testExportAndRestoreWithPrecision() = runBlocking {
        val settings = AppSettings(id = 1, currencySymbol = "$")
        val commitment = FixedCommitment(
            name = "Rent",
            targetAmount = BigDecimal("1500.75"),
            currentProgress = BigDecimal("500.25"),
            orderIndex = 0
        )
        val tx = TransactionDb(
            id = "tx-1",
            timestamp = 1700000000000L,
            type = "EXPENSE",
            category = "Utilities",
            amount = BigDecimal("99.99"),
            description = "Electricity"
        )
        val customer = HabayebCustomer(
            id = "cust-1",
            name = "John Doe",
            phone = "123456789",
            notes = "VIP",
            createdAt = 1600000000L,
            initialType = TransactionType.OWED_BY_THEM.value
        )
        val hTx = HabayebTransaction(
            id = "htx-1",
            customerId = "cust-1",
            type = TransactionType.OWED_BY_THEM.value,
            amount = BigDecimal("250.50"),
            timestamp = 1700000001000L,
            description = "Loan",
            currencyCode = "DEFAULT",
            foreignAmount = BigDecimal.ZERO,
            exchangeRate = BigDecimal.ONE,
            isRateCalculated = false,
            equivalentAmount = BigDecimal("250.50"),
            baseCurrencyCode = "DEFAULT"
        )

        val payload = BackupPayloadData(
            settings = settings,
            commitments = listOf(commitment),
            transactions = listOf(tx),
            habayebCustomers = listOf(customer),
            habayebTransactions = listOf(hTx)
        )

        val exportedJson = BackupPayloadSerializer.exportBackupToJson(payload)
        assertNotNull(exportedJson)
        assertTrue(exportedJson.contains("Rent"))
        assertTrue(exportedJson.contains("1500.75"))
        assertTrue(exportedJson.contains("99.99"))

        // Execute Master Restore
        val result = restoreService.executeMasterRestore(exportedJson)
        assertNotNull(result)
        assertEquals("$", result.settings.currencySymbol)

        // Verify Database Records
        val commitments = database.commitmentDao().getAllCommitmentsFlow().first()
        val restoredCommitment = commitments.find { it.name == "Rent" }
        assertNotNull(restoredCommitment)
        assertEquals(BigDecimal("1500.7500"), restoredCommitment?.targetAmount)

        val restoredTx = database.transactionDao().getTransactionById("tx-1")
        assertNotNull(restoredTx)
        assertEquals(BigDecimal("99.9900"), restoredTx?.amount)

        val restoredCust = database.habayebDao().getCustomerByIdDirect("cust-1")
        assertNotNull(restoredCust)
        assertEquals("John Doe", restoredCust?.name)

        val restoredHTx = database.habayebDao().getTransactionById("htx-1")
        assertNotNull(restoredHTx)
        assertEquals(BigDecimal("250.5000"), restoredHTx?.amount)
    }

    @Test
    fun testFallbackCustomerCreationForOrphanTransactions() = runBlocking {
        val hTxOrphan = HabayebTransaction(
            id = "orphan-tx-1",
            customerId = "missing-cust-99",
            type = TransactionType.OWED_BY_THEM.value,
            amount = BigDecimal("100.00"),
            timestamp = 1700000000000L,
            description = "Orphan Transaction",
            currencyCode = "DEFAULT",
            foreignAmount = BigDecimal.ZERO,
            exchangeRate = BigDecimal.ONE,
            isRateCalculated = false,
            equivalentAmount = BigDecimal("100.00"),
            baseCurrencyCode = "DEFAULT"
        )

        val payload = BackupPayloadData(
            settings = AppSettings(id = 1),
            commitments = emptyList(),
            transactions = emptyList(),
            habayebCustomers = emptyList(), // No customer provided!
            habayebTransactions = listOf(hTxOrphan)
        )

        val exportedJson = BackupPayloadSerializer.exportBackupToJson(payload)

        // Execute Master Restore - should create fallback customer automatically to avoid FK violation
        val result = restoreService.executeMasterRestore(exportedJson)
        assertNotNull(result)

        val createdCustomer = database.habayebDao().getCustomerByIdDirect("missing-cust-99")
        assertNotNull(createdCustomer)
        assertTrue(createdCustomer!!.name.contains("missing-cust-99"))

        val tx = database.habayebDao().getTransactionById("orphan-tx-1")
        assertNotNull(tx)
        assertEquals("missing-cust-99", tx?.customerId)
    }

    @Test
    fun testBackupServiceAndAtomicFileOperations() = runBlocking {
        // Prepare data in Database
        database.settingsDao().insertOrUpdateSettings(AppSettings(id = 1, currencySymbol = "SAR"))
        database.transactionDao().insertTransaction(
            TransactionDb(
                id = "tx-local-1",
                timestamp = 1700000000000L,
                type = "INCOME",
                category = "Salary",
                amount = BigDecimal("5000.00"),
                description = "Monthly Salary"
            )
        )

        // Create local backup
        val backupResult = backupRepository.createLocalBackup("Test_Mizan_Backup.mzd")
        assertTrue(backupResult is BackupOperationResult.Success)

        val file = (backupResult as BackupOperationResult.Success).file
        assertTrue(file.exists())
        assertTrue(file.length() > 0)

        // Integrity verification
        val integrityResult = BackupIntegrityManager.validateBackupFileIntegrity(file)
        assertTrue(integrityResult is BackupIntegrityManager.IntegrityCheckResult.Valid)

        // Restore from file
        val restoreFileResult = backupRepository.restoreFromFile(file)
        assertTrue(restoreFileResult.isSuccess)
        val restoredData = restoreFileResult.getOrThrow()
        assertEquals("SAR", restoredData.settings.currencySymbol)

        // Clean up
        val deleteResult = fileManager.deleteBackupFile(file)
        assertTrue(deleteResult.isSuccess)
    }

    @Test
    fun testIntegrityCheckRejectionOnCorruptedOrEmptyFile() {
        val tempDir = fileManager.getMonthlyBackupDirectory()
        val corruptedFile = File(tempDir, "corrupted_backup.mzd")
        corruptedFile.writeText("{ this is invalid json content !!!")

        val integrityResult = BackupIntegrityManager.validateBackupFileIntegrity(corruptedFile)
        assertTrue(integrityResult is BackupIntegrityManager.IntegrityCheckResult.Invalid)

        val emptyFile = File(tempDir, "empty_backup.mzd")
        emptyFile.writeText("")
        val emptyIntegrityResult = BackupIntegrityManager.validateBackupFileIntegrity(emptyFile)
        assertTrue(emptyIntegrityResult is BackupIntegrityManager.IntegrityCheckResult.Invalid)

        corruptedFile.delete()
        emptyFile.delete()
    }

    @Test
    fun testLegacyRestoreSupport() = runBlocking {
        val legacyJson = """
        {
            "mizan_al_dar_db": {
                "settings": {
                    "currency_symbol": "ر.ي",
                    "school_expenses_enabled": true
                },
                "transactions": [
                    {
                        "id": "legacy-tx-1",
                        "timestamp": 1690000000000,
                        "type": "EXPENSE",
                        "category": "مشتريات",
                        "amount": "3500.50",
                        "description": "فاتورة قديمة"
                    }
                ],
                "fixed_commitments": []
            }
        }
        """.trimIndent()

        val restoreResult = restoreService.executeMasterRestore(legacyJson)
        assertTrue(restoreResult.isLegacy)
        assertEquals("ر.ي", restoreResult.settings.currencySymbol)

        val tx = database.transactionDao().getTransactionById("legacy-tx-1")
        assertNotNull(tx)
        assertEquals(BigDecimal("3500.5000"), tx?.amount)
    }

    @Test
    fun testLargeDatasetRestoreAndPrecision() = runBlocking {
        val count = 200
        val txList = mutableListOf<TransactionDb>()
        for (i in 1..count) {
            txList.add(
                TransactionDb(
                    id = "large-tx-$i",
                    timestamp = 1700000000000L + i,
                    type = "EXPENSE",
                    category = "Cat-$i",
                    amount = BigDecimal("100.1234"),
                    description = "Description $i"
                )
            )
        }

        val payload = BackupPayloadData(
            settings = AppSettings(id = 1, currencySymbol = "USD"),
            commitments = emptyList(),
            transactions = txList
        )

        val json = BackupPayloadSerializer.exportBackupToJson(payload)
        val restoreResult = restoreService.executeMasterRestore(json)
        assertEquals("USD", restoreResult.settings.currencySymbol)

        val retrievedCount = database.transactionDao().getTransactionsCountDirect()
        assertEquals(count, retrievedCount)
    }
}
