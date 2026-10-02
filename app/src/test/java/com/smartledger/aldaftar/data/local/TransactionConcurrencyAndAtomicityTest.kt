package com.smartledger.aldaftar.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.smartledger.aldaftar.data.local.entities.AppSettings
import com.smartledger.aldaftar.data.local.entities.HabayebCustomer
import com.smartledger.aldaftar.data.local.entities.HabayebTransaction
import com.smartledger.aldaftar.data.license.LicenseRepository
import com.smartledger.aldaftar.data.repository.HabayebMutationRepository
import com.smartledger.aldaftar.data.repository.HabayebRepository
import com.smartledger.aldaftar.data.repository.RecurringRepository
import com.smartledger.aldaftar.domain.model.RecurringConfig
import com.smartledger.aldaftar.domain.model.TransactionType
import java.math.BigDecimal
import java.util.UUID
import java.util.concurrent.Executors
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.withLock
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class TransactionConcurrencyAndAtomicityTest {

    private lateinit var db: AppDatabase
    private lateinit var licenseRepo: LicenseRepository
    private lateinit var habayebRepo: HabayebRepository
    private lateinit var mutationRepo: HabayebMutationRepository
    private lateinit var recurringRepo: RecurringRepository
    private val testDispatcher = Executors.newFixedThreadPool(8).asCoroutineDispatcher()

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        habayebRepo = HabayebRepository(db, db.habayebDao(), null)
        mutationRepo = HabayebMutationRepository(db)
        recurringRepo = RecurringRepository(db, db.recurringConfigDao(), null)
    }

    @After
    fun tearDown() {
        db.close()
        testDispatcher.close()
    }

    @Test
    fun concurrentTransactionInserts_preserveEveryTransactionAndBalanceIntegrity() = runBlocking {
        val customer = HabayebCustomer(
            id = "concurrent-cust-1",
            name = "Concurrent Test Customer",
            phone = "777000111",
            notes = "",
            createdAt = System.currentTimeMillis()
        )
        assertTrue(habayebRepo.insertCustomer(customer))

        val count = 20
        val deferredList = (1..count).map { i ->
            CoroutineScope(testDispatcher).async {
                val tx = HabayebTransaction(
                    id = "tx-$i",
                    customerId = customer.id,
                    type = TransactionType.OWED_BY_THEM.value,
                    amount = BigDecimal("10.00"),
                    timestamp = 1000L + i,
                    description = "Concurrent Tx $i",
                    isForeign = false,
                    currencyCode = "ر.ي",
                    foreignAmount = BigDecimal.ZERO,
                    exchangeRate = BigDecimal.ZERO,
                    isRateCalculated = false,
                    equivalentAmount = BigDecimal.ZERO,
                    baseCurrencyCode = "ر.ي"
                )
                habayebRepo.insertHabayebTransaction(tx)
            }
        }

        val results = deferredList.awaitAll()
        assertTrue(results.all { it })

        val savedTxs = db.habayebDao().getTransactionsForCustomerDirect(customer.id)
        assertEquals(count, savedTxs.size)

        val totalAmount = savedTxs.map { it.amount }.reduce { acc, bigDecimal -> acc.add(bigDecimal) }
        assertEquals(BigDecimal("200.0000"), totalAmount)
    }

    @Test
    fun trialQuota_underMassiveConcurrentRequests_neverExceedsLimit() = runBlocking {
        val limit = 100
        val creationMutex = kotlinx.coroutines.sync.Mutex()
        var trialUsed = 0

        suspend fun <T> runAuthorizedCreation(block: suspend () -> T): T? = creationMutex.withLock {
            val used = habayebRepo.getHabayebTransactionsCountDirect()
            if (used >= limit) return@withLock null
            trialUsed = used + 1
            val res = block()
            trialUsed = habayebRepo.getHabayebTransactionsCountDirect()
            res
        }

        val customer = HabayebCustomer(
            id = "quota-race-cust",
            name = "Quota Customer",
            phone = "",
            notes = "",
            createdAt = System.currentTimeMillis()
        )
        val custCreated = runAuthorizedCreation { habayebRepo.insertCustomer(customer) }
        assertTrue(custCreated == true)

        val totalAttempts = 150
        val results = withContext(testDispatcher) {
            (1..totalAttempts).map { i ->
                async {
                    val tx = HabayebTransaction(
                        id = "quota-tx-$i",
                        customerId = customer.id,
                        type = TransactionType.OWED_BY_THEM.value,
                        amount = BigDecimal("5.00"),
                        timestamp = 2000L + i,
                        description = "Tx $i",
                        baseCurrencyCode = "ر.ي"
                    )
                    runAuthorizedCreation { habayebRepo.insertHabayebTransaction(tx) }
                }
            }.awaitAll()
        }

        val successes = results.count { it == true }
        val failures = results.count { it == null }

        assertEquals(99, successes)
        assertEquals(51, failures)

        val totalOps = habayebRepo.getHabayebTransactionsCountDirect()
        assertEquals(100, totalOps)
        assertEquals(100, limit)
    }

    @Test
    fun atomicCustomerDeletionToTrash_andAtomicBundleRestore_maintainsZeroOrphans() = runBlocking {
        val customer = HabayebCustomer(
            id = "atomic-cust",
            name = "Atomic Customer",
            phone = "12345",
            notes = "Test notes",
            createdAt = 100L
        )
        assertTrue(habayebRepo.insertCustomer(customer))

        for (i in 1..5) {
            habayebRepo.insertHabayebTransaction(
                HabayebTransaction(
                    id = "atomic-tx-$i",
                    customerId = customer.id,
                    type = TransactionType.OWED_BY_THEM.value,
                    amount = BigDecimal("50.00"),
                    timestamp = 200L + i,
                    description = "Tx $i",
                    baseCurrencyCode = "ر.ي"
                )
            )
        }

        // Delete customer to trash atomically
        mutationRepo.deleteCustomerToTrash(customer.id)

        // Customer & transactions must be completely removed from active tables
        assertNull(db.habayebDao().getCustomerByIdDirect(customer.id))
        assertTrue(db.habayebDao().getTransactionsForCustomerDirect(customer.id).isEmpty())

        // Deleted items must contain exactly one bundle item
        val deleted = db.trashDao().getAllDeletedItemsDirect()
        assertEquals(1, deleted.size)
        assertEquals("bundle_${customer.id}", deleted.first().id)
        assertEquals("habayeb_bundle", deleted.first().originalTableName)

        // Atomic restore of the bundle
        db.trashDao().restoreDeletedItem(deleted.first())

        // Customer and all 5 transactions must be restored completely
        val restoredCust = db.habayebDao().getCustomerByIdDirect(customer.id)
        assertNotNull(restoredCust)
        assertEquals("Atomic Customer", restoredCust?.name)

        val restoredTxs = db.habayebDao().getTransactionsForCustomerDirect(customer.id)
        assertEquals(5, restoredTxs.size)

        // Trash table must now be empty
        assertTrue(db.trashDao().getAllDeletedItemsDirect().isEmpty())
    }

    @Test
    fun recurringTransactionExecution_isStrictlyIdempotentUnderParallelTriggers() = runBlocking {
        val customer = HabayebCustomer(
            id = "recurring-cust",
            name = "Recurring Customer",
            phone = "",
            notes = "",
            createdAt = 10L
        )
        assertTrue(habayebRepo.insertCustomer(customer))

        val now = System.currentTimeMillis()
        val config = RecurringConfig(
            id = "rec-cfg-1",
            originalTxId = "orig-tx-1",
            customerId = customer.id,
            customerName = customer.name,
            amount = BigDecimal("100.00"),
            type = TransactionType.OWED_BY_THEM.value,
            description = "Monthly Fee",
            frequency = "DAILY",
            daysOfWeek = emptyList(),
            daysOfMonth = emptyList(),
            timeHour = 10,
            timeMinute = 0,
            startDateMillis = now - 3 * 86400 * 1000L,
            endDateMillis = now + 10 * 86400 * 1000L,
            lastExecutedTimestamp = 0L,
            baseCurrencyCode = "ر.ي"
        )
        recurringRepo.save(config)

        // Trigger execution 5 times in parallel
        val parallelExecutions = withContext(testDispatcher) {
            (1..5).map {
                async { recurringRepo.executeDue(now) }
            }.awaitAll()
        }

        // Only one of the parallel executions should generate the due occurrences, others must generate 0 duplicates
        val generatedOccurrences = db.habayebDao().getTransactionsForCustomerDirect(customer.id)
        val distinctTimestamps = generatedOccurrences.map { it.timestamp }.distinct()
        assertEquals(generatedOccurrences.size, distinctTimestamps.size)
    }
}
