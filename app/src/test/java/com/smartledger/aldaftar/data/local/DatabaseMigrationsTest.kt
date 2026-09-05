package com.smartledger.aldaftar.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.smartledger.aldaftar.data.local.entities.AppSettings
import com.smartledger.aldaftar.data.local.entities.FixedCommitment
import com.smartledger.aldaftar.data.local.entities.TransactionDb
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
import java.math.BigDecimal

/**
 * اختبارات سلامة قاعدة البيانات والترحيلات (Database & Migrations Integrity Test)
 */
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class DatabaseMigrationsTest {

    private lateinit var database: AppDatabase

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        )
        .addMigrations(*DatabaseMigrations.ALL_MIGRATIONS)
        .allowMainThreadQueries()
        .build()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun testDatabaseInitializationAndAllMigrations() = runBlocking {
        assertNotNull(database)
        val settingsDao = database.settingsDao()
        val commitmentDao = database.commitmentDao()
        val transactionDao = database.transactionDao()
        val habayebDao = database.habayebDao()
        val customCategoryDao = database.customCategoryDao()
        val trashDao = database.trashDao()

        // 1. Settings Insertion & Retrieval
        val settings = AppSettings(
            id = 1,
            currencySymbol = "ر.ي",
            schoolExpensesEnabled = true
        )
        settingsDao.insertOrUpdateSettings(settings)
        val retrievedSettings = settingsDao.getSettingsDirect()
        assertNotNull(retrievedSettings)
        assertEquals("ر.ي", retrievedSettings?.currencySymbol)

        // 2. FixedCommitment Insertion
        val commitment = FixedCommitment(
            name = "إيجار",
            targetAmount = BigDecimal("50000.50"),
            currentProgress = BigDecimal("25000.00"),
            orderIndex = 1
        )
        commitmentDao.insertCommitment(commitment)
        val commitments = commitmentDao.getAllCommitmentsFlow().first()
        val retrievedCommitment = commitments.find { it.name == "إيجار" }
        assertNotNull(retrievedCommitment)
        assertEquals(BigDecimal("50000.50"), retrievedCommitment?.targetAmount)

        // 3. Transactions with exact BigDecimal precision
        val tx = TransactionDb(
            id = "tx_test_1",
            timestamp = 1700000000000L,
            type = "EXPENSE",
            category = "طعام",
            amount = BigDecimal("12345.6789"),
            description = "وجبة عائلية"
        )
        transactionDao.insertTransaction(tx)
        val retrievedTx = transactionDao.getTransactionById("tx_test_1")
        assertNotNull(retrievedTx)
        assertEquals(BigDecimal("12345.6789"), retrievedTx?.amount)
    }

    @Test
    fun testAllMigrationsCountAndHistory() {
        assertEquals(30, DatabaseMigrations.ALL_MIGRATIONS.size)
        // Ensure starting and ending versions align sequentially from 1 to 31
        for (i in 0 until DatabaseMigrations.ALL_MIGRATIONS.size) {
            val migration = DatabaseMigrations.ALL_MIGRATIONS[i]
            assertEquals(i + 1, migration.startVersion)
            assertEquals(i + 2, migration.endVersion)
        }
    }
}
