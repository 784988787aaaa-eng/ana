package com.smartledger.aldaftar.data.backup

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.smartledger.aldaftar.data.local.AppDatabase
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class BackupPayloadIntegrityTest {

    @Test
    fun rejectsTransactionReferencingMissingCustomer() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        try {
            val engine = BackupEngine(context, db)
            val payload = JSONObject()
                .put("settings", JSONObject())
                .put("categories", JSONArray())
                .put("trash", JSONArray())
                .put("customers", JSONArray())
                .put("habayebTransactions", JSONArray().put(
                    JSONObject()
                        .put("id", "tx-1")
                        .put("customerId", "missing-customer")
                        .put("type", "OWED_BY_THEM")
                        .put("amount", "10")
                        .put("timestamp", 1L)
                        .put("description", "")
                        .put("isForeign", false)
                        .put("currencyCode", "DEFAULT")
                        .put("foreignAmount", "0")
                        .put("exchangeRate", "0")
                        .put("isRateCalculated", false)
                        .put("equivalentAmount", "10")
                        .put("baseCurrencyCode", "DEFAULT")
                ))
                .put("pins", JSONArray())
                .put("businessProfile", JSONObject())
                .put("recurring", JSONArray())

            val method = BackupEngine::class.java.getDeclaredMethod("validatePayload", JSONObject::class.java)
            method.isAccessible = true

            val thrown = runCatching { method.invoke(engine, payload) }.exceptionOrNull()
            assertTrue(thrown?.cause is IllegalArgumentException)
        } finally {
            db.close()
        }
    }
}
