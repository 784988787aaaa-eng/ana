package com.smartledger.aldaftar.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.smartledger.aldaftar.data.local.AppDatabase
import com.smartledger.aldaftar.domain.model.RecurringConfig
import java.math.BigDecimal
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class RecurringExecutionIntegrationTest {
    @Test fun executeDueCopiesFrozenForeignSnapshotEvenWhenCurrentWorldChanges() = runTest {
        val db=Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext<Context>(), AppDatabase::class.java
        ).allowMainThreadQueries().build()
        try {
            db.habayebDao().insertCustomer(
                com.smartledger.aldaftar.data.local.entities.HabayebCustomer("c","C","","",1L)
            )
            val start=java.util.Calendar.getInstance().apply {
                clear(); set(2026,0,1,0,0,0)
            }.timeInMillis
            val end=start+86_400_000L
            val config=RecurringConfig(
                id="r",originalTxId="origin",customerId="c",customerName="C",
                amount=BigDecimal("14000.0000"),type="OWED_BY_THEM",description="fixed",
                frequency="DAILY",daysOfWeek=emptyList(),daysOfMonth=emptyList(),
                timeHour=0,timeMinute=0,startDateMillis=start,endDateMillis=end,
                lastExecutedTimestamp=0,isActive=true,isForeign=true,currencyCode="ر.س",
                foreignAmount=BigDecimal("100.0000"),exchangeRate=BigDecimal("140.0000"),
                isRateCalculated=true,equivalentAmount=BigDecimal("14000.0000")
            )
            val repo=RecurringRepository(db,db.recurringConfigDao())
            repo.save(config)

            // No current exchange-rate lookup is passed to execution. The persisted snapshot is authoritative.
            val count=repo.executeDue(end)
            assertEquals(2,count)
            val secondRun=repo.executeDue(end)
            assertEquals(0,secondRun)
            // Even if the scheduler state is replayed/reset, the occurrence
            // identity must prevent duplicate financial rows.
            db.recurringConfigDao().save(db.recurringConfigDao().all().single().copy(lastExecutedTimestamp=0))
            val replayRun=repo.executeDue(end)
            assertEquals(0,replayRun)
            val rows=db.habayebDao().getAllTransactionsDirect()
            assertEquals(2,rows.size)
            rows.forEach {
                assertTrue(it.isForeign)
                assertEquals("ر.س",it.currencyCode)
                assertEquals(0,BigDecimal("100.0000").compareTo(it.foreignAmount))
                assertEquals(0,BigDecimal("140.0000").compareTo(it.exchangeRate))
                assertEquals(0,BigDecimal("14000.0000").compareTo(it.equivalentAmount))
            }
        } finally { db.close() }
    }
}
