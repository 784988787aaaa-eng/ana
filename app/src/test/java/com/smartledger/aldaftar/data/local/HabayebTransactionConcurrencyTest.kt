package com.smartledger.aldaftar.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.smartledger.aldaftar.data.local.entities.HabayebCustomer
import com.smartledger.aldaftar.data.local.entities.HabayebTransaction
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.math.BigDecimal

@RunWith(RobolectricTestRunner::class)
class HabayebTransactionConcurrencyTest {

    @Test
    fun updateAfterDeleteMustNotResurrectTransaction() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        try {
            val dao = db.habayebDao()
            val customer = HabayebCustomer("c1", "عميل", "1", "", 1L, "OWED_TO_THEM", null)
            val original = HabayebTransaction(
                id = "t1",
                customerId = customer.id,
                type = "OWED_BY_THEM",
                amount = BigDecimal("10.00"),
                timestamp = 1L,
                description = "original"
            )
            dao.insertCustomer(customer)
            dao.insertTransaction(original)

            val staleEdit = original.copy(amount = BigDecimal("99.00"), description = "stale edit")
            dao.deleteTransactionById(original.id)

            assertEquals(0, dao.updateTransaction(staleEdit))
            assertNull(dao.getTransactionById(original.id))
        } finally {
            db.close()
        }
    }

    @Test
    fun updateExistingTransactionReportsOneAffectedRow() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        try {
            val dao = db.habayebDao()
            val customer = HabayebCustomer("c1", "عميل", "1", "", 1L, "OWED_TO_THEM", null)
            val original = HabayebTransaction("t1", customer.id, "OWED_BY_THEM", BigDecimal("10.00"), 1L, "original")
            dao.insertCustomer(customer)
            dao.insertTransaction(original)

            val updated = original.copy(amount = BigDecimal("11.00"))
            assertEquals(1, dao.updateTransaction(updated))
            assertEquals(updated, dao.getTransactionById(original.id))
        } finally {
            db.close()
        }
    }
}
