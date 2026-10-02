package com.smartledger.aldaftar.data.cloud

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.smartledger.aldaftar.data.account.UnifiedAccountSessionRepository
import com.smartledger.aldaftar.data.license.LicenseRepository
import com.smartledger.aldaftar.domain.model.CloudBackupFile
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class CloudArchiveSyncComprehensiveSuiteTest {

    private lateinit var context: Context
    private lateinit var connectionStore: CloudConnectionStore
    private lateinit var cloudStore: CloudArchiveStore
    private lateinit var sessionRepository: UnifiedAccountSessionRepository

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        connectionStore = CloudConnectionStore(context)
        cloudStore = CloudArchiveStore(context)
        sessionRepository = UnifiedAccountSessionRepository(
            context = context,
            licenseRepository = LicenseRepository(context),
            cloudArchiveStore = cloudStore
        )
    }

    @After
    fun tearDown() {
        connectionStore.clear()
    }

    @Test
    fun cloudConnectionStore_savesAndClearsCredentialsOnAccountBoundary() {
        connectionStore.save("test_token_123")
        connectionStore.saveEmail("user1@example.com")
        connectionStore.saveFolderId("folder_abc_123")

        assertEquals("test_token_123", connectionStore.token())
        assertEquals("user1@example.com", connectionStore.email())
        assertEquals("folder_abc_123", connectionStore.folderId())

        connectionStore.clear()

        assertNull(connectionStore.token())
        assertNull(connectionStore.email())
        assertNull(connectionStore.folderId())
    }

    @Test
    fun cloudOperationException_mapsStatusCodesAndLocalizedMessages() {
        val ex401 = CloudOperationException(401, "unauthorized", "يرجى إعادة تسجل الدخول")
        assertEquals(401, ex401.statusCode)
        assertEquals("unauthorized", ex401.errorCode)
        assertEquals("يرجى إعادة تسجل الدخول", ex401.userMessage)

        val ex429 = CloudOperationException(429, "rate_limited", "تم تجاوز حد الطلبات")
        assertEquals(429, ex429.statusCode)
    }

    @Test
    fun cloudBackupFile_modelsRemoteMetadataCorrectly() {
        val file = CloudBackupFile(
            id = "drive_file_999",
            name = "SNA_2026-10-02_10-00.sna",
            size = 2048L,
            modifiedTime = 1700000000000L,
            month = "2026-10"
        )

        assertEquals("drive_file_999", file.id)
        assertEquals("SNA_2026-10-02_10-00.sna", file.name)
        assertEquals(2048L, file.size)
        assertEquals(1700000000000L, file.modifiedTime)
        assertEquals("2026-10", file.month)
    }

    @Test
    fun unifiedAccountSessionRepository_signOut_clearsSessionAndCloudStore() = runTest {
        connectionStore.saveEmail("active_user@example.com")
        connectionStore.save("active_token")

        val initialSession = sessionRepository.refreshSession()
        assertTrue(initialSession.isSignedIn)

        sessionRepository.signOutUnified()

        val postSignOutSession = sessionRepository.session.value
        assertFalse(postSignOutSession.isSignedIn)
        assertNull(postSignOutSession.email)
        assertFalse(postSignOutSession.isCloudConnected)
        assertNull(connectionStore.token())
        assertNull(connectionStore.email())
    }
}
