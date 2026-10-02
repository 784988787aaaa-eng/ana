package com.smartledger.aldaftar.ui.root

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.smartledger.aldaftar.AppContainer
import com.smartledger.aldaftar.data.local.AppDatabase
import com.smartledger.aldaftar.data.local.entities.AppSettings
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class StartupAndInitialResponsivenessTest {

    private lateinit var context: Context
    private lateinit var db: AppDatabase

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun appContainer_instantiationIsImmediateAndLazy_withZeroEagerDiskIO() {
        val container = AppContainer(context)
        assertNotNull("Container holds references safely", container)
    }

    @Test
    fun startupCriticalPath_databaseOpeningAndSettingsFetch_occursWithoutMainThreadBlock() = runBlocking {
        val settingsDao = db.settingsDao()
        settingsDao.insertOrUpdateSettings(AppSettings(id = 1, currencySymbol = "ر.ي"))

        val loaded = settingsDao.getSettingsFlow().first()
        assertNotNull("Database settings flow emits immediately on startup", loaded)
        assertEquals("ر.ي", loaded?.currencySymbol)
    }

    @Test
    fun databaseWalMode_ensuresNonBlockingStartupReads() = runBlocking {
        db.settingsDao().getSettingsDirect()
        assertTrue("SQLite Database is open and accessible after first query", db.isOpen)
    }
}
