package com.smartledger.aldaftar.data.backup

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.smartledger.aldaftar.data.local.AppDatabase
import com.smartledger.aldaftar.data.local.entities.*
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.json.JSONObject
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.File
import java.math.BigDecimal
import java.nio.charset.StandardCharsets

@RunWith(RobolectricTestRunner::class)
class BackupComprehensiveSuiteTest {

    private lateinit var context: Context
    private lateinit var db: AppDatabase
    private lateinit var engine: BackupEngine
    private lateinit var tempFile: File

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        engine = BackupEngine(context, db)
        tempFile = File.createTempFile("backup_test_", ".sna", context.cacheDir)
    }

    @After
    fun tearDown() {
        tempFile.delete()
        db.close()
    }

    @Test
    fun backupAndRestore_emptyDatabase_createsValidEnvelopeAndRestores() = runTest {
        val created = engine.create(tempFile)
        assertTrue(created.exists() && created.length() > 0)

        val bytes = created.readBytes()
        engine.validateEnvelope(bytes)

        val settings = engine.restore(created)
        assertNotNull(settings)
    }

    @Test
    fun backupAndRestore_goldenDatasetWithArabicAndMultiCurrency_preservesExactSemantics() = runTest {
        val customer = HabayebCustomer(
            id = "c_arabic_1",
            name = "محل البركة لتجارة المواد الغذائية",
            phone = "+967770000000",
            notes = "عميل مميز - حساب بالريال السعودي والدولار",
            createdAt = 1700000000000L
        )
        val transaction1 = HabayebTransaction(
            id = "tx_arabic_1",
            customerId = customer.id,
            type = "OWED_BY_THEM",
            amount = BigDecimal("140000.50"),
            timestamp = 1700001000000L,
            description = "بضاعة تجارية مقومة بالريال السعودي",
            isForeign = true,
            currencyCode = "ر.س",
            foreignAmount = BigDecimal("1000.00"),
            exchangeRate = BigDecimal("140.0005"),
            isRateCalculated = true,
            equivalentAmount = BigDecimal("140000.50"),
            baseCurrencyCode = "ر.ي"
        )
        val recurring = RecurringConfigEntity(
            id = "rec_arabic_1",
            originalTxId = transaction1.id,
            customerId = customer.id,
            customerName = customer.name,
            amount = transaction1.amount,
            type = transaction1.type,
            description = "قسط شهري متكرر",
            frequency = "MONTHLY",
            daysOfWeek = emptyList(),
            daysOfMonth = listOf(1),
            timeHour = 10,
            timeMinute = 0,
            startDateMillis = 1700000000000L,
            endDateMillis = 1800000000000L,
            lastExecutedTimestamp = 0L,
            isActive = true,
            isForeign = true,
            currencyCode = "ر.س",
            foreignAmount = BigDecimal("1000.00"),
            exchangeRate = BigDecimal("140.0005"),
            isRateCalculated = true,
            equivalentAmount = BigDecimal("140000.50"),
            baseCurrencyCode = "ر.ي"
        )

        db.habayebDao().insertCustomer(customer)
        db.habayebDao().insertTransaction(transaction1)
        db.recurringConfigDao().save(recurring)

        engine.create(tempFile)

        // Wipe database completely to simulate new device restore
        db.habayebDao().clearAllTransactions()
        db.habayebDao().clearAllCustomers()
        db.recurringConfigDao().clear()

        assertEquals(0, db.habayebDao().getAllCustomersDirect().size)
        assertEquals(0, db.habayebDao().getAllTransactionsDirect().size)

        engine.restore(tempFile)

        val restoredCustomer = db.habayebDao().getCustomerByIdDirect("c_arabic_1")
        assertNotNull(restoredCustomer)
        assertEquals("محل البركة لتجارة المواد الغذائية", restoredCustomer?.name)

        val restoredTx = db.habayebDao().getTransactionById("tx_arabic_1")
        assertNotNull(restoredTx)
        assertEquals(0, BigDecimal("140000.50").compareTo(restoredTx?.amount))
        assertEquals(0, BigDecimal("140.0005").compareTo(restoredTx?.exchangeRate))
        assertEquals("ر.س", restoredTx?.currencyCode)

        val restoredRec = db.recurringConfigDao().byOriginalTransaction("tx_arabic_1")
        assertNotNull(restoredRec)
        assertEquals("MONTHLY", restoredRec?.frequency)
        assertEquals(0, BigDecimal("140000.50").compareTo(restoredRec?.equivalentAmount))
    }

    @Test
    fun restore_corruptedPayloadSha256_failsBeforeModifyingDatabase() = runTest {
        db.habayebDao().insertCustomer(HabayebCustomer("c1", "العميل الأصلي", "", "", 100L))

        engine.create(tempFile)

        val rawText = tempFile.readText(StandardCharsets.UTF_8)
        val envelope = JSONObject(rawText)
        envelope.put("payloadSha256", "0".repeat(64))

        val tamperedFile = File.createTempFile("tampered_", ".sna", context.cacheDir)
        tamperedFile.writeText(envelope.toString(), StandardCharsets.UTF_8)

        val originalCustomerCount = db.habayebDao().getAllCustomersDirect().size

        val ex = assertThrows(IllegalArgumentException::class.java) {
            runBlocking { engine.restore(tamperedFile) }
        }
        assertTrue(ex.message?.contains("سلامة النسخة") == true)

        // Verify the original database content is 100% untouched
        assertEquals(originalCustomerCount, db.habayebDao().getAllCustomersDirect().size)
        assertEquals("العميل الأصلي", db.habayebDao().getCustomerByIdDirect("c1")?.name)

        tamperedFile.delete()
    }

    @Test
    fun restore_invalidAppId_throwsIllegalArgumentException() = runTest {
        engine.create(tempFile)
        val envelope = JSONObject(tempFile.readText(StandardCharsets.UTF_8))
        envelope.put("appId", "WRONG_APP")

        val invalidFile = File.createTempFile("invalid_app_", ".sna", context.cacheDir)
        invalidFile.writeText(envelope.toString(), StandardCharsets.UTF_8)

        assertThrows(IllegalArgumentException::class.java) {
            runBlocking { engine.restore(invalidFile) }
        }

        invalidFile.delete()
    }

    @Test
    fun restore_preservesLocalPasscodeAndOnboardingState() = runTest {
        val currentSettings = AppSettings(
            id = 1,
            currencySymbol = "ر.ي",
            isFirstLaunch = false,
            onboardingShown = true,
            isPasscodeEnabled = true,
            passcodeHash = "LOCAL_PASSCODE_HASH"
        )
        db.settingsDao().insertOrUpdateSettings(currentSettings)

        engine.create(tempFile)

        val newSettings = currentSettings.copy(currencySymbol = "$")
        db.settingsDao().insertOrUpdateSettings(newSettings)

        engine.restore(tempFile)

        val restoredSettings = db.settingsDao().getSettingsDirect()
        assertNotNull(restoredSettings)
        assertEquals("ر.ي", restoredSettings?.currencySymbol)
        assertTrue(restoredSettings?.isPasscodeEnabled == true)
        assertEquals("LOCAL_PASSCODE_HASH", restoredSettings?.passcodeHash)
    }
}
