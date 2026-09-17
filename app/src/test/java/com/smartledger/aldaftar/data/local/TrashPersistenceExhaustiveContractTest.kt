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
}
