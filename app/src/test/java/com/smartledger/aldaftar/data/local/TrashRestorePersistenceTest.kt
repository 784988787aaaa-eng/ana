package com.smartledger.aldaftar.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.smartledger.aldaftar.data.local.entities.DeletedItemEntity
import com.smartledger.aldaftar.data.local.entities.HabayebTransaction
import com.smartledger.aldaftar.data.repository.TrashJsonSerializer
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.math.BigDecimal

class TrashRestorePersistenceTest {
    @Test fun persistedDeletedTransactionRestoresCriticalFieldsAndRemovesTrashItem() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).allowMainThreadQueries().build()
        try {
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
}
