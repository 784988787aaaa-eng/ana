/** توثيق تنفيذي عربي: يوضح هذا الجزء الغرض التشغيلي وأثره على سلامة المزامنة والبيانات. */
package com.smartledger.aldaftar.data

// توثيق تنفيذي: يوضح هذا الموضع الغرض التشغيلي وأثره على سلامة المزامنة والبيانات.
// توثيق تنفيذي: يوضح هذا الموضع الغرض التشغيلي وأثره على سلامة المزامنة والبيانات.
// توثيق تنفيذي: يوضح هذا الموضع الغرض التشغيلي وأثره على سلامة المزامنة والبيانات.
import android.content.Context
import android.util.Log
import com.smartledger.aldaftar.data.cloud.CloudNetworkEngine
import com.smartledger.aldaftar.data.local.AppDatabase
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** توثيق تنفيذي عربي: يوضح هذا الجزء الغرض التشغيلي وأثره على سلامة المزامنة والبيانات. */
sealed class CloudSyncState {
    object Idle : CloudSyncState()
    object Preparing : CloudSyncState()
    object Authenticating : CloudSyncState()
    data class Authenticated(val email: String) : CloudSyncState()
    object Syncing : CloudSyncState()
    object Success : CloudSyncState()
    object Skipped : CloudSyncState()
    data class Error(val message: String) : CloudSyncState()
    object SessionExpired : CloudSyncState()
}

/** توثيق تنفيذي عربي: يوضح هذا الجزء الغرض التشغيلي وأثره على سلامة المزامنة والبيانات. */
data class CloudBackupFile(
    val id: String,
    val name: String,
    val size: Long,
    val createdTime: String
)

/** توثيق تنفيذي عربي: يوضح هذا الجزء الغرض التشغيلي وأثره على سلامة المزامنة والبيانات. */
class GoogleDriveSyncHelper(private val context: Context) {

    /** توثيق تنفيذي عربي: يوضح هذا الجزء الغرض التشغيلي وأثره على سلامة المزامنة والبيانات. */
    companion object {
        private const val TAG = "GoogleDriveSyncHelper"
        private const val MIRROR_FILE_NAME = "google_drive_mirror.mzd"
        private const val DEFAULT_ACCOUNT_EMAIL = "account@google.com"

        private val DATE_FORMATTER = ThreadLocal.withInitial {
            SimpleDateFormat("yyyy-MM-dd_HH-mm", Locale.US)
        }

        private fun formatDate(date: Date): String {
            return DATE_FORMATTER.get()?.format(date) ?: ""
        }

        /** توثيق تنفيذي عربي: يوضح هذا الجزء الغرض التشغيلي وأثره على سلامة المزامنة والبيانات. */
        suspend fun disconnectAndSignOut(context: Context) = withContext(Dispatchers.IO) {
            try {
                val syncHelper = GoogleDriveSyncHelper(context.applicationContext)
                syncHelper.authManager.disableCloudSyncInSettings()
                syncHelper.authManager.clearAuthData()
                syncHelper.authManager.logout()
                Log.d(TAG, "تم قطع الاتصال مع Google Drive ومسح رموز الجلسة السحابية بنجاح.")
            } catch (e: Exception) {
                Log.e(TAG, "خطأ أثناء تسجيل الخروج الشامل من Google Drive: ${e.javaClass.simpleName}")
            }
        }
    }

    private val helperScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val syncMutex = Mutex()

    // توثيق تنفيذي: يوضح هذا الموضع الغرض التشغيلي وأثره على سلامة المزامنة والبيانات.
    private val _syncState = MutableStateFlow<CloudSyncState>(CloudSyncState.Idle)
    val syncState: StateFlow<CloudSyncState> = _syncState.asStateFlow()

    // توثيق تنفيذي: يوضح هذا الموضع الغرض التشغيلي وأثره على سلامة المزامنة والبيانات.
    private val authManager = GoogleDriveAuthManager(context) { state ->
        _syncState.value = state
    }

    private val folderNavigator = GoogleDriveFolderNavigator(CloudNetworkEngine.getInstance(context).client)
    private val networkUploader = GoogleDriveNetworkUploader(context)

    val clientId: String
        get() = authManager.clientId

    val clientSecret: String
        get() = authManager.clientSecret

    val scope: String
        get() = authManager.scope

    fun getClientIdOverride(): String = authManager.getClientIdOverride()
    fun getClientSecretOverride(): String = authManager.getClientSecretOverride()

    fun saveClientCredentialsOverride(clientIdStr: String?, clientSecretStr: String?) {
        authManager.saveClientCredentialsOverride(clientIdStr, clientSecretStr)
    }

    fun getAppSignatureSHA1(): String = authManager.getAppSignatureSHA1()

    /** توثيق تنفيذي عربي: يوضح هذا الجزء الغرض التشغيلي وأثره على سلامة المزامنة والبيانات. */
    init {
        val email = getStoredEmail()
        val refreshToken = getStoredRefreshToken()
        val accessToken = getStoredAccessToken()
        if (!email.isNullOrEmpty() && (!refreshToken.isNullOrEmpty() || (!accessToken.isNullOrEmpty() && !authManager.isTokenExpired()))) {
            _syncState.value = CloudSyncState.Authenticated(email)
        }

        try {
            val account = GoogleSignIn.getLastSignedInAccount(context)
            if (account != null) {
                helperScope.launch {
                    try {
                        val db = AppDatabase.getDatabase(context)
                        val settings = db.settingsDao().getSettingsDirect()
                        if (getStoredRefreshToken().isNullOrEmpty() && getStoredAccessToken().isNullOrEmpty() && (settings == null || !settings.isCloudSyncEnabled)) {
                            Log.d(TAG, "اكتشاف إعادة تثبيت دون وجود جلسة سارية، جاري تسجيل الخروج الصامت.")
                            getGoogleSignInClient().signOut()
                            authManager.clearAuthData()
                            _syncState.value = CloudSyncState.Idle
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "خطأ أثناء فحص حالة الجلسة عند التهيئة: ${e.javaClass.simpleName}")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "خطأ أثناء استعلام الحساب المسجل: ${e.javaClass.simpleName}")
        }
    }

    fun isUserTrulySignedIn(): Boolean = authManager.isUserTrulySignedIn()
    fun isUserTrulySignedIn(context: Context): Boolean = isUserTrulySignedIn()

    fun getGoogleSignInClient(): GoogleSignInClient = authManager.getGoogleSignInClient()
    fun getGoogleSignInClient(context: Context): GoogleSignInClient = getGoogleSignInClient()

    fun getAuthUrl(): String = authManager.getAuthUrl()

    fun logout() {
        authManager.logout()
    }

    fun logoutAsync(onComplete: () -> Unit) {
        authManager.logoutAsync(onComplete)
    }

    fun getStoredAccessToken(): String? = authManager.getStoredAccessToken()
    fun getStoredRefreshToken(): String? = authManager.getStoredRefreshToken()
    fun getStoredEmail(): String? = authManager.getStoredEmail()

    fun storeEmail(email: String) {
        authManager.storeEmail(email)
    }

    /** توثيق تنفيذي عربي: يوضح هذا الجزء الغرض التشغيلي وأثره على سلامة المزامنة والبيانات. */
    private suspend fun handleSessionExpired() = withContext(Dispatchers.IO) {
        _syncState.value = CloudSyncState.SessionExpired
        authManager.disableCloudSyncInSettings()
        authManager.clearAuthData()
    }

    fun updateSyncState(state: CloudSyncState) {
        _syncState.value = state
    }

    /** توثيق تنفيذي عربي: يوضح هذا الجزء الغرض التشغيلي وأثره على سلامة المزامنة والبيانات. */
    private suspend fun getValidAccessTokenOrExpired(): String? {
        val token = authManager.refreshAccessTokenIfNeeded()
        if (token == null) {
            handleSessionExpired()
        }
        return token
    }

    suspend fun handleAuthorizationCode(code: String, inputEmail: String? = null, redirectUri: String = ""): Boolean {
        return authManager.handleAuthorizationCode(code, inputEmail, redirectUri)
    }

    /** توثيق تنفيذي عربي: يوضح هذا الجزء الغرض التشغيلي وأثره على سلامة المزامنة والبيانات. */
    private fun writeLocalMirrorCache(jsonContent: String) {
        try {
            val mirrorFile = File(context.filesDir, MIRROR_FILE_NAME)
            mirrorFile.bufferedWriter().use { writer ->
                writer.write(jsonContent)
            }
        } catch (e: Exception) {
            Log.w(TAG, "تعذر كتابة ملف المرآة المحلي: ${e.javaClass.simpleName}")
        }
    }

    /** توثيق تنفيذي عربي: يوضح هذا الجزء الغرض التشغيلي وأثره على سلامة المزامنة والبيانات. */
    suspend fun uploadBackupToDrive(backupJsonContent: String): Boolean = syncMutex.withLock {
        withContext(Dispatchers.IO) {
            _syncState.value = CloudSyncState.Preparing
            val accessToken = getValidAccessTokenOrExpired() ?: return@withContext false
            val email = authManager.getStoredEmail() ?: DEFAULT_ACCOUNT_EMAIL

            // توثيق تنفيذي: يوضح هذا الموضع الغرض التشغيلي وأثره على سلامة المزامنة والبيانات.
            writeLocalMirrorCache(backupJsonContent)

            // توثيق تنفيذي: يوضح هذا الموضع الغرض التشغيلي وأثره على سلامة المزامنة والبيانات.
            if (networkUploader.isPayloadIdentical(backupJsonContent)) {
                _syncState.value = CloudSyncState.Skipped
                delay(800)
                _syncState.value = CloudSyncState.Authenticated(email)
                return@withContext true
            }

            _syncState.value = CloudSyncState.Syncing

            // توثيق تنفيذي: يوضح هذا الموضع الغرض التشغيلي وأثره على سلامة المزامنة والبيانات.
            val searchResult = folderNavigator.findLatestBackupFileId(accessToken, forceRefresh = true)
            val existingFileId = when (searchResult) {
                is GoogleDriveFolderNavigator.FileSearchResult.Success -> searchResult.fileId
                is GoogleDriveFolderNavigator.FileSearchResult.Error -> {
                    if (searchResult.isAuthError) {
                        handleSessionExpired()
                    } else {
                        _syncState.value = CloudSyncState.Error(context.getString(com.smartledger.aldaftar.R.string.gdrive_error_server_failed))
                    }
                    return@withContext false
                }
            }

            val dateStr = formatDate(Date())
            val fileName = "Mzd_$dateStr.mzd"

            // توثيق تنفيذي: يوضح هذا الموضع الغرض التشغيلي وأثره على سلامة المزامنة والبيانات.
            val uploadResult = networkUploader.uploadBackupSafe(
                filename = fileName,
                backupJsonContent = backupJsonContent,
                accessToken = accessToken,
                existingFileId = existingFileId
            )

            folderNavigator.clearCache()

            // توثيق تنفيذي: يوضح هذا الموضع الغرض التشغيلي وأثره على سلامة المزامنة والبيانات.
            return@withContext processUploadResult(uploadResult, email)
        }
    }

    /** توثيق تنفيذي عربي: يوضح هذا الجزء الغرض التشغيلي وأثره على سلامة المزامنة والبيانات. */
    suspend fun uploadBackupToDriveWithFilename(filename: String, backupJsonContent: String): Boolean = syncMutex.withLock {
        withContext(Dispatchers.IO) {
            _syncState.value = CloudSyncState.Preparing
            val accessToken = getValidAccessTokenOrExpired() ?: return@withContext false
            val email = authManager.getStoredEmail() ?: DEFAULT_ACCOUNT_EMAIL

            writeLocalMirrorCache(backupJsonContent)

            if (networkUploader.isPayloadIdentical(backupJsonContent)) {
                _syncState.value = CloudSyncState.Skipped
                delay(800)
                _syncState.value = CloudSyncState.Authenticated(email)
                return@withContext true
            }

            _syncState.value = CloudSyncState.Syncing

            val uploadResult = networkUploader.createAndUploadNewFile(
                filename = filename,
                backupJsonContent = backupJsonContent,
                accessToken = accessToken
            )

            folderNavigator.clearCache()

            return@withContext processUploadResult(uploadResult, email)
        }
    }

    /** توثيق تنفيذي عربي: يوضح هذا الجزء الغرض التشغيلي وأثره على سلامة المزامنة والبيانات. */
    private suspend fun processUploadResult(result: GoogleDriveNetworkUploader.UploadResult, email: String): Boolean {
        return when (result) {
            is GoogleDriveNetworkUploader.UploadResult.Success -> {
                _syncState.value = CloudSyncState.Success
                delay(1200)
                _syncState.value = CloudSyncState.Authenticated(email)
                true
            }
            is GoogleDriveNetworkUploader.UploadResult.SkippedUnchanged -> {
                _syncState.value = CloudSyncState.Skipped
                delay(800)
                _syncState.value = CloudSyncState.Authenticated(email)
                true
            }
            is GoogleDriveNetworkUploader.UploadResult.AuthError -> {
                handleSessionExpired()
                false
            }
            is GoogleDriveNetworkUploader.UploadResult.Failure -> {
                _syncState.value = CloudSyncState.Error(context.getString(com.smartledger.aldaftar.R.string.gdrive_error_server_failed))
                false
            }
        }
    }

    /** توثيق تنفيذي عربي: يوضح هذا الجزء الغرض التشغيلي وأثره على سلامة المزامنة والبيانات. */
    suspend fun downloadBackupFromDrive(): String? = syncMutex.withLock {
        withContext(Dispatchers.IO) {
            _syncState.value = CloudSyncState.Preparing
            val accessToken = getValidAccessTokenOrExpired() ?: return@withContext null
            val email = authManager.getStoredEmail() ?: DEFAULT_ACCOUNT_EMAIL

            val searchResult = folderNavigator.findLatestBackupFileId(accessToken, forceRefresh = true)
            val fileId = when (searchResult) {
                is GoogleDriveFolderNavigator.FileSearchResult.Success -> searchResult.fileId
                is GoogleDriveFolderNavigator.FileSearchResult.Error -> {
                    if (searchResult.isAuthError) {
                        handleSessionExpired()
                    } else {
                        _syncState.value = CloudSyncState.Error(context.getString(com.smartledger.aldaftar.R.string.gdrive_error_server_failed))
                    }
                    return@withContext null
                }
            }

            if (fileId == null) {
                _syncState.value = CloudSyncState.Error(context.getString(com.smartledger.aldaftar.R.string.gdrive_error_backups_not_found))
                return@withContext null
            }

            _syncState.value = CloudSyncState.Syncing
            return@withContext downloadBackupFromDriveByIdInternal(fileId, accessToken, email)
        }
    }

    /** توثيق تنفيذي عربي: يوضح هذا الجزء الغرض التشغيلي وأثره على سلامة المزامنة والبيانات. */
    suspend fun downloadBackupFromDriveById(fileId: String): String? = syncMutex.withLock {
        withContext(Dispatchers.IO) {
            _syncState.value = CloudSyncState.Preparing
            val accessToken = getValidAccessTokenOrExpired() ?: return@withContext null
            val email = authManager.getStoredEmail() ?: DEFAULT_ACCOUNT_EMAIL

            _syncState.value = CloudSyncState.Syncing
            return@withContext downloadBackupFromDriveByIdInternal(fileId, accessToken, email)
        }
    }

    /** توثيق تنفيذي عربي: يوضح هذا الجزء الغرض التشغيلي وأثره على سلامة المزامنة والبيانات. */
    private suspend fun downloadBackupFromDriveByIdInternal(
        fileId: String,
        accessToken: String,
        email: String
    ): String? {
        val downloadResult = networkUploader.downloadFileById(fileId, accessToken)
        return when (downloadResult) {
            is GoogleDriveNetworkUploader.DownloadResult.Success -> {
                _syncState.value = CloudSyncState.Authenticated(email)
                downloadResult.content
            }
            is GoogleDriveNetworkUploader.DownloadResult.FileNotFound -> {
                _syncState.value = CloudSyncState.Error(context.getString(com.smartledger.aldaftar.R.string.gdrive_error_backups_not_found))
                null
            }
            is GoogleDriveNetworkUploader.DownloadResult.InvalidPayload -> {
                _syncState.value = CloudSyncState.Error(context.getString(com.smartledger.aldaftar.R.string.backup_schema_mismatch))
                null
            }
            is GoogleDriveNetworkUploader.DownloadResult.AuthError -> {
                handleSessionExpired()
                null
            }
            is GoogleDriveNetworkUploader.DownloadResult.Failure -> {
                _syncState.value = CloudSyncState.Error(context.getString(com.smartledger.aldaftar.R.string.gdrive_error_server_failed))
                null
            }
        }
    }

    /** توثيق تنفيذي عربي: يوضح هذا الجزء الغرض التشغيلي وأثره على سلامة المزامنة والبيانات. */
    suspend fun listCloudBackups(): List<CloudBackupFile> = withContext(Dispatchers.IO) {
        val accessToken = getValidAccessTokenOrExpired() ?: return@withContext emptyList()
        val result = folderNavigator.listCloudBackups(accessToken)
        return@withContext when (result) {
            is GoogleDriveFolderNavigator.ListBackupsResult.Success -> result.backups
            is GoogleDriveFolderNavigator.ListBackupsResult.Error -> {
                if (result.isAuthError) {
                    handleSessionExpired()
                }
                emptyList()
            }
        }
    }

    /** توثيق تنفيذي عربي: يوضح هذا الجزء الغرض التشغيلي وأثره على سلامة المزامنة والبيانات. */
    suspend fun deleteBackupFromDriveById(fileId: String): Boolean = withContext(Dispatchers.IO) {
        val accessToken = getValidAccessTokenOrExpired() ?: return@withContext false
        folderNavigator.clearCache()
        return@withContext networkUploader.deleteFileById(fileId, accessToken)
    }
}

/** توثيق تنفيذي عربي: يوضح هذا الجزء الغرض التشغيلي وأثره على سلامة المزامنة والبيانات. */
object GoogleDriveHelper {
    suspend fun disconnectAndSignOut(context: Context) {
        GoogleDriveSyncHelper.disconnectAndSignOut(context)
    }
}
