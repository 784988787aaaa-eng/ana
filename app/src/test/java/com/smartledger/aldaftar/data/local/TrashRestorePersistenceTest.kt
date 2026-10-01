package com.smartledger.aldaftar.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.smartledger.aldaftar.data.local.entities.DeletedItemEntity
import com.smartledger.aldaftar.data.local.entities.HabayebCustomer
import com.smartledger.aldaftar.data.local.entities.HabayebTransaction
import com.smartledger.aldaftar.data.local.entities.RecurringConfigEntity
import com.smartledger.aldaftar.data.repository.TrashJsonSerializer
import java.math.BigDecimal
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class TrashRestorePersistenceTest {
    @Test fun persistedDeletedTransactionRestoresCriticalFieldsAndRemovesTrashItem() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).allowMainThreadQueries().build()
        try {
            val customer = HabayebCustomer(id = "c1", name = "عميل", phone = "", notes = "", createdAt = 1L, initialType = "OWED_BY_THEM")
            db.habayebDao().insertCustomer(customer)
            val original = HabayebTransaction(
                id = "t1", customerId = "c1", type = "OWED_BY_THEM", amount = BigDecimal("12.50"),
                timestamp = 123L, description = "وصف", linkedMainTxId = "main", isForeign = true,
                currencyCode = "USD", foreignAmount = BigDecimal("10"), exchangeRate = BigDecimal("1.25"),
                isRateCalculated = true, equivalentAmount = BigDecimal("12.50"), baseCurrencyCode = "YER"
            )
            val item = DeletedItemEntity(
                id = "trash1", sourceSystem = "HABAYEB", originalTableName = TrashDao.TABLE_HABAYEB_TRANSACTIONS,
                jsonData = TrashJsonSerializer.serializeHabayebTransaction(original), deletedAt = 1L
            )
            db.trashDao().insertDeletedItem(item)
            val persisted = db.trashDao().getDeletedItemByIdDirect(item.id)!!
            db.trashDao().restoreDeletedItem(persisted)
            assertEquals(original, db.habayebDao().getTransactionById(original.id))
            assertNull(db.trashDao().getDeletedItemByIdDirect(item.id))
        } finally {
            db.close()
        }
    }

    @Test fun persistedDeletedTransactionRestoresItsRecurringConfiguration() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).allowMainThreadQueries().build()
        try {
            val customer = HabayebCustomer(id = "c2", name = "عميل recurring", phone = "", notes = "", createdAt = 2L, initialType = "OWED_BY_THEM")
            val tx = HabayebTransaction("t2", "c2", "OWED_BY_THEM", BigDecimal("14000"), 2L, "قسط")
            val recurring = RecurringConfigEntity(
                id = "r2", originalTxId = "t2", customerId = "c2", customerName = customer.name,
                amount = BigDecimal("14000"), type = "OWED_BY_THEM", description = "قسط", frequency = "DAILY",
                daysOfWeek = listOf(1, 3), daysOfMonth = listOf(5, 20), timeHour = 10, timeMinute = 30,
                startDateMillis = 2L, endDateMillis = 100L, lastExecutedTimestamp = 0L, isActive = true
            )
            db.habayebDao().insertCustomer(customer)
            db.habayebDao().insertTransaction(tx)
            val item = DeletedItemEntity(
                id = "trash2", sourceSystem = "HABAYEB", originalTableName = TrashDao.TABLE_HABAYEB_TRANSACTIONS,
                jsonData = TrashJsonSerializer.serializeHabayebTransaction(tx, recurring), deletedAt = 2L
            )
            db.trashDao().insertDeletedItem(item)
            db.recurringConfigDao().deleteForTransaction(tx.id)
            db.habayebDao().deleteTransactionById(tx.id)

            val persisted = db.trashDao().getDeletedItemByIdDirect(item.id)!!
            db.trashDao().restoreDeletedItem(persisted)

            assertEquals(tx, db.habayebDao().getTransactionById(tx.id))
            assertEquals(listOf(recurring), db.recurringConfigDao().byCustomer(customer.id))
            assertNull(db.trashDao().getDeletedItemByIdDirect(item.id))
        } finally {
            db.close()
        }
    }
}
