package com.smartledger.aldaftar.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.smartledger.aldaftar.data.local.entities.DeletedItemEntity
import com.smartledger.aldaftar.data.local.entities.HabayebCustomer
import com.smartledger.aldaftar.data.local.entities.HabayebTransaction
import com.smartledger.aldaftar.data.repository.TrashJsonSerializer
import java.math.BigDecimal
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class TrashPersistenceExhaustiveContractTest {
    @Test fun restorePreservesForeignSnapshotAndRemovesTrashRow()=runTest {
        val db=Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext<Context>(),AppDatabase::class.java)
            .allowMainThreadQueries().build()
        try {
            val c=HabayebCustomer("c","C","","",1L)
            db.habayebDao().insertCustomer(c)
            val tx=HabayebTransaction("t","c","OWED_BY_THEM",BigDecimal("14000"),1L,"",
                isForeign=true,currencyCode="ر.س",foreignAmount=BigDecimal("100"),
                exchangeRate=BigDecimal("140"),isRateCalculated=true,equivalentAmount=BigDecimal("14000"),baseCurrencyCode="ر.ي")
            db.trashDao().insertDeletedItem(DeletedItemEntity("trash","HABAYEB","habayeb_transactions",
                TrashJsonSerializer.serializeHabayebTransaction(tx),2L,"",BigDecimal("14000"),"C"))
            val item=db.trashDao().getDeletedItemByIdDirect("trash")!!
            db.trashDao().restoreDeletedItem(item)
            assertEquals(tx,db.habayebDao().getTransactionById("t"))
            assertNull(db.trashDao().getDeletedItemByIdDirect("trash"))
        } finally { db.close() }
    }
    @Test fun operationCountKeepsTrashedSlotsUntilPermanentDelete()=runTest {
        val db=Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext<Context>(),AppDatabase::class.java)
            .allowMainThreadQueries().build()
        try {
            val c=HabayebCustomer("c2","C2","","",1L)
            val tx=HabayebTransaction("t2","c2","OWED_BY_THEM",BigDecimal("100"),1L,"")
            db.habayebDao().insertCustomer(c)
            db.habayebDao().insertTransaction(tx)
            val repository = com.smartledger.aldaftar.data.repository.HabayebRepository(db, db.habayebDao())
            assertEquals(2, repository.getHabayebTransactionsCountDirect())

            db.trashDao().insertDeletedItem(
                DeletedItemEntity("bundle_c2", "الحبايب", "habayeb_bundle",
                    TrashJsonSerializer.serializeHabayebBundle(c, listOf(tx), null, emptySet()), 2L)
            )
            db.habayebDao().deleteTransactionById(tx.id)
            db.habayebDao().deleteCustomerById(c.id)
            assertEquals(2, repository.getHabayebTransactionsCountDirect())

            db.trashDao().deleteItemById("bundle_c2")
            assertEquals(0, repository.getHabayebTransactionsCountDirect())
        } finally { db.close() }
    }

}
