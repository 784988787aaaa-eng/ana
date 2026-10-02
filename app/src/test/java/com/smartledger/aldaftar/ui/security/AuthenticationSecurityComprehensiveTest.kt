package com.smartledger.aldaftar.ui.security

import android.app.Application
import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.smartledger.aldaftar.data.local.AppDatabase
import com.smartledger.aldaftar.data.local.entities.AppSettings
import com.smartledger.aldaftar.data.repository.SettingsRepository
import com.smartledger.aldaftar.platform.security.AppSecurityManager
import com.smartledger.aldaftar.ui.viewmodel.SecurityViewModel
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AuthenticationSecurityComprehensiveTest {

    private lateinit var app: Application
    private lateinit var db: AppDatabase
    private lateinit var securityManager: AppSecurityManager
    private lateinit var settingsRepository: SettingsRepository
    private lateinit var viewModel: SecurityViewModel

    @Before
    fun setUp() {
        app = ApplicationProvider.getApplicationContext()
        securityManager = AppSecurityManager.getInstance(app)
        securityManager.resetFailedAttempts()

        db = Room.inMemoryDatabaseBuilder(app, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()

        val dao = db.settingsDao()
        kotlinx.coroutines.runBlocking {
            dao.insertOrUpdateSettings(AppSettings(id = 1))
        }

        settingsRepository = SettingsRepository(dao)
        viewModel = SecurityViewModel(app, settingsRepository)
    }

    @After
    fun tearDown() {
        securityManager.resetFailedAttempts()
        db.close()
    }

    @Test
    fun failedAttempts_incrementAndProgressiveLockout_escalatesCorrectly() {
        assertEquals(0, securityManager.getFailedAttempts())
        assertEquals(0L, viewModel.getLockoutTimeRemainingMs())

        // Simulate 4 failed attempts (no lockout yet)
        repeat(4) { viewModel.handleFailedAttempt() }
        assertEquals(4, securityManager.getFailedAttempts())
        assertEquals(0L, viewModel.getLockoutTimeRemainingMs())

        // 5th failed attempt triggers 30 seconds lockout
        viewModel.handleFailedAttempt()
        assertEquals(5, securityManager.getFailedAttempts())
        val remaining5 = viewModel.getLockoutTimeRemainingMs()
        assertTrue(remaining5 in 25_000L..30_000L)

        // 6th failed attempt triggers 60 seconds lockout
        viewModel.handleFailedAttempt()
        assertEquals(6, securityManager.getFailedAttempts())
        val remaining6 = viewModel.getLockoutTimeRemainingMs()
        assertTrue(remaining6 in 55_000L..60_000L)

        // 7th failed attempt triggers 300 seconds lockout
        viewModel.handleFailedAttempt()
        assertEquals(7, securityManager.getFailedAttempts())
        val remaining7 = viewModel.getLockoutTimeRemainingMs()
        assertTrue(remaining7 in 290_000L..300_000L)
    }

    @Test
    fun successfulUnlock_resetsFailedAttemptsAndLockout() {
        repeat(5) { viewModel.handleFailedAttempt() }
        assertEquals(5, securityManager.getFailedAttempts())
        assertTrue(viewModel.getLockoutTimeRemainingMs() > 0)

        // Simulating successful unlock or reset
        viewModel.resetFailedAttempts()

        assertEquals(0, securityManager.getFailedAttempts())
        assertEquals(0L, viewModel.getLockoutTimeRemainingMs())
    }
}
