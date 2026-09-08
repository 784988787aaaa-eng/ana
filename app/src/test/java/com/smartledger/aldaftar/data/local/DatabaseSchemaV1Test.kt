package com.smartledger.aldaftar.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.smartledger.aldaftar.data.local.entities.AppSettings
import org.junit.Assert.assertEquals
import org.junit.Test

class DatabaseSchemaV1Test {

    @Test
    fun freshDatabaseUsesSchemaVersionOneAndExpectedTables() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        val cursor = db.openHelper.readableDatabase.query(
            "SELECT name FROM sqlite_master WHERE type='table' AND name NOT LIKE 'room_%' ORDER BY name"
        )
        val tables = buildList {
            cursor.use {
                while (it.moveToNext()) add(it.getString(0))
            }
        }
        assertEquals(1, db.openHelper.readableDatabase.version)
        assertEquals(
            listOf(
                "app_settings",
                "business_profile",
                "custom_categories",
                "deleted_items",
                "fixed_commitments",
                "habayeb_customers",
                "habayeb_transactions",
                "pinned_habayeb_customers",
                "recurring_configs",
                "transactions"
            ),
            tables
        )
        db.close()
    }

}
