package com.smartledger.aldaftar.data.backup

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.smartledger.aldaftar.data.local.AppDatabase
import com.smartledger.aldaftar.data.local.entities.HabayebCustomer
import com.smartledger.aldaftar.data.local.entities.HabayebTransaction
import com.smartledger.aldaftar.data.local.entities.RecurringConfigEntity
import com.smartledger.aldaftar.data.local.entities.AppSettings
import java.io.File
import java.math.BigDecimal
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class BackupRestoreIntegrationTest {
    @Test fun restoreRoundTripPreservesCurrencyRecurringAndTrashSnapshot() = runTest {
        val context=ApplicationProvider.getApplicationContext<Context>()
        val db=Room.inMemoryDatabaseBuilder(context,AppDatabase::class.java).allowMainThreadQueries().build()
        val file=File.createTempFile("smartledger-contract-",".sna",context.cacheDir)
        try {
            db.settingsDao().insertOrUpdateSettings(
                AppSettings(id=1,currencySymbol="ر.ي",exchangeRatesJson="""{"ر.ي":{"ر.س":"140.0000"}}""")
            )
            db.habayebDao().insertCustomer(HabayebCustomer("c","C","","",1L))
            val tx=HabayebTransaction("t","c","OWED_BY_THEM",BigDecimal("14000"),2L,"",
                linkedMainTxId="r",isForeign=true,currencyCode="ر.س",foreignAmount=BigDecimal("100"),
                exchangeRate=BigDecimal("140"),isRateCalculated=true,equivalentAmount=BigDecimal("14000"),baseCurrencyCode="ر.ي")
            db.habayebDao().insertTransaction(tx)
            db.recurringConfigDao().save(
                RecurringConfigEntity("r","t","c","C",tx.amount,tx.type,tx.description,"DAILY",emptyList(),emptyList(),
                    10,0,1,4_000_000_000_000L,0,true,tx.isForeign,tx.currencyCode,tx.foreignAmount,
                    tx.exchangeRate,tx.isRateCalculated,tx.equivalentAmount)
            )
            val engine=BackupEngine(context,db)
            engine.create(file)

            // Mutate the live database to prove restore is a snapshot operation.
            db.habayebDao().clearAllTransactions()
            db.recurringConfigDao().clear()
            db.settingsDao().insertOrUpdateSettings(AppSettings(id=1,currencySymbol="$",exchangeRatesJson="{}"))

            engine.restore(file)

            assertEquals(tx,db.habayebDao().getTransactionById("t"))
            val r=db.recurringConfigDao().byOriginalTransaction("t")!!
            assertEquals("ر.س",r.currencyCode)
            assertEquals(0,BigDecimal("140").compareTo(r.exchangeRate))
            assertEquals(0,BigDecimal("14000").compareTo(r.equivalentAmount))
            assertEquals("ر.ي",db.settingsDao().getSettingsDirect()!!.currencySymbol)
        } finally {
            file.delete()
            db.close()
        }
    }
}
