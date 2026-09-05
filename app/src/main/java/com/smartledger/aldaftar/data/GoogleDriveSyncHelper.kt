/**
 * =====================================================================
 * ملف: منسق وواجهة المزامنة السحابية (GoogleDriveSyncHelper.kt)
 * =====================================================================
 * 
 * [الغرض العام والتعليمي من الملف]:
 * يمثل هذا الملف المايسترو ومنسق العمليات الرئيسي (Sync Orchestrator / Facade)
 * لكافة وظائف المزامنة والنسخ الاحتياطي السحابي مع Google Drive.
 * 
 * [المسؤوليات المعمارية والوظيفية]:
 * 1. تنسيق التدفق الكامل للمزامنة: التحقق من الصلاحيات -> فحص الشبكة -> تجهيز البيانات -> الرفع/التنزيل -> تحديث حالة واجهة المستخدم.
 * 2. التحكم في التزامن عبر [syncMutex] لمنع تشغيل عمليتي رفع أو تنزيل متزامنتين.
 * 3. بث حالات المزامنة الصريحة عبر [StateFlow] الموجه لشاشات Jetpack Compose.
 * 4. إدارة النسخة الاحتياطية المتطابقة محلياً (Mirror Cache) للحفظ المؤقت عند فقد الاتصال.
 * 5. تفويض المهام الدقيقة للوحدات المتخصصة:
 *    - إدارة المصادقة والجلسة -> [GoogleDriveAuthManager].
 *    - عمليات الرفع والتنزيل وتدقيق البصمات -> [GoogleDriveNetworkUploader].
 *    - استعراض المجلدات والبحث في Drive -> [GoogleDriveFolderNavigator].
 */
package com.smartledger.aldaftar.data

// ---------------------------------------------------------------------
// استيراد حزم الاتصال والتدفقات والحسابات وقواعد البيانات المحلية
// ---------------------------------------------------------------------
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

/**
 * [فئات حالة المزامنة السحابية - CloudSyncState]:
 * تمثل جميع الحالات التي تمر بها عملية المزامنة للتفاعل الفوري مع واجهات المستخدم:
 * - Idle: في وضع الخمول وجاهز للعمل.
 * - Preparing: تجهيز حزمة البيانات والتحقق من الرموز المميزة.
 * - Authenticating: قيد التحقق وتجديد جلسة OAuth 2.0.
 * - Authenticated: تم تسجيل الدخول والجلسة نشطة وتحمل بريد المستخدم.
 * - Syncing: قيد نقل البيانات سحابياً (رفع أو تنزيل).
 * - Success: اكتملت عملية المزامنة بنجاح تام.
 * - Skipped: تم تخطي الرفع لعدم وجود أي تعديلات على البيانات (Zero-Diff).
 * - Error: حدث خطأ أثناء المزامنة مع رسالة توضيحية.
 * - SessionExpired: انتهت صلاحية الجلسة وتتطلب إعادة تسجيل الدخول.
 */
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

/**
 * [نموذج بيانات ملف النسخة السحابية - CloudBackupFile]:
 * يحتوي على البيانات الوصفية للملفات المخزنة في مجلد التطبيق السحابي.
 */
data class CloudBackupFile(
    val id: String,
    val name: String,
    val size: Long,
    val createdTime: String
)

/**
 * [فئة مساعد المزامنة السحابية - GoogleDriveSyncHelper]:
 * الواجهة المركزية لكافة وظائف المزامنة مع Google Drive.
 */
class GoogleDriveSyncHelper(private val context: Context) {

    /**
     * [الكائن المرافق - Companion Object]:
     * يحتوي على ثوابت أسماء الملفات وتنسيق التواريخ ودالة تسجيل الخروج الشامل.
     */
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

        /**
         * [دالة قطع الاتصال وتسجيل الخروج الشامل]:
         * تعطل المزامنة السحابية في الإعدادات وتمسح الرموز وتسجل الخروج من حساب Google.
         */
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

    // تدفق حالة المزامنة السحابية للمراقبة في واجهات Compose
    private val _syncState = MutableStateFlow<CloudSyncState>(CloudSyncState.Idle)
    val syncState: StateFlow<CloudSyncState> = _syncState.asStateFlow()

    // المدراء والوحدات المتخصصة التابعة
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

    /**
     * [كتلة التهيئة - init]:
     * تستعيد حالة الجلسة المحفوظة وتتحقق من صحة الحساب مع قاعدة البيانات المحلية عند بدء التشغيل.
     */
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

    /**
     * [دالة معالجة انتهاء الجلسة]:
     * تحدث الحالة إلى SessionExpired وتعطل المزامنة السحابية في الإعدادات وتنظف الرموز.
     */
    private suspend fun handleSessionExpired() = withContext(Dispatchers.IO) {
        _syncState.value = CloudSyncState.SessionExpired
        authManager.disableCloudSyncInSettings()
        authManager.clearAuthData()
    }

    fun updateSyncState(state: CloudSyncState) {
        _syncState.value = state
    }

    /**
     * [دالة التحقق من رمز الوصول الصالح]:
     * تجدد الرمز إذا لزم الأمر، وتتعامل مع انتهاء الجلسة تلقائياً إذا فشل التجديد.
     */
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

    /**
     * [دالة حفظ المرآة المحلية - writeLocalMirrorCache]:
     * تحفظ نسخة احتياطية محلية متطابقة في ملفات التطبيق الخاصة لضمان الأمان الإضافي.
     */
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

    /**
     * [تدفق الرفع التلقائي للنسخة الاحتياطية - uploadBackupToDrive]:
     * ينفذ خطوات الرفع المنسقة مع تدقيق عدم التغيير والتحديث الذكي لأحدث نسخة.
     */
    suspend fun uploadBackupToDrive(backupJsonContent: String): Boolean = syncMutex.withLock {
        withContext(Dispatchers.IO) {
            _syncState.value = CloudSyncState.Preparing
            val accessToken = getValidAccessTokenOrExpired() ?: return@withContext false
            val email = authManager.getStoredEmail() ?: DEFAULT_ACCOUNT_EMAIL

            // 1. كتابة المرآة المحلية
            writeLocalMirrorCache(backupJsonContent)

            // 2. التحقق من تطابق البصمة قبل استهلاك الشبكة (Zero-Diff)
            if (networkUploader.isPayloadIdentical(backupJsonContent)) {
                _syncState.value = CloudSyncState.Skipped
                delay(800)
                _syncState.value = CloudSyncState.Authenticated(email)
                return@withContext true
            }

            _syncState.value = CloudSyncState.Syncing

            // 3. البحث عن الملف الأخير للتحديث أو الإنشاء
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

            // 4. تنفيذ الرفع عبر NetworkUploader
            val uploadResult = networkUploader.uploadBackupSafe(
                filename = fileName,
                backupJsonContent = backupJsonContent,
                accessToken = accessToken,
                existingFileId = existingFileId
            )

            folderNavigator.clearCache()

            // 5. تأكيد النتيجة وتحديث الحالة
            return@withContext processUploadResult(uploadResult, email)
        }
    }

    /**
     * [تدفق الرفع المخصص باسم ملف محدد - uploadBackupToDriveWithFilename]:
     * يتيح رفع ملف نسخة احتياطية باسم محدد يختاره المستخدم أو النظام.
     */
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

    /**
     * [دالة معالجة نتيجة الرفع - processUploadResult]:
     * تحول نتيجة الرفع إلى حالة StateFlow مناسبة للواجهة (Success, Skipped, Error, AuthError).
     */
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

    /**
     * [تدفق تنزيل أحدث نسخة احتياطية - downloadBackupFromDrive]:
     * يبحث عن أحدث ملف .mzd في السحابة وينزله ويفحص سلامة بياناته.
     */
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

    /**
     * [تدفق تنزيل نسخة محددة عبر معرف الملف - downloadBackupFromDriveById]:
     * ينزل محتوى ملف محدد بناءً على اختيار المستخدم من قائمة النسخ المتوفرة.
     */
    suspend fun downloadBackupFromDriveById(fileId: String): String? = syncMutex.withLock {
        withContext(Dispatchers.IO) {
            _syncState.value = CloudSyncState.Preparing
            val accessToken = getValidAccessTokenOrExpired() ?: return@withContext null
            val email = authManager.getStoredEmail() ?: DEFAULT_ACCOUNT_EMAIL

            _syncState.value = CloudSyncState.Syncing
            return@withContext downloadBackupFromDriveByIdInternal(fileId, accessToken, email)
        }
    }

    /**
     * [الدالة الداخلية لتنزيل النسخة وفحص النتيجة - downloadBackupFromDriveByIdInternal]:
     * تتولى استدعاء النقل الشبكي وتحديث حالة الواجهة بناءً على النتيجة.
     */
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

    /**
     * [دالة استعراض قائمة النسخ السحابية - listCloudBackups]:
     * تستعلم عن كافة ملفات النسخ السحابية المتاحة في حساب المستخدم.
     */
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

    /**
     * [دالة حذف نسخة سحابية - deleteBackupFromDriveById]:
     * تحذف ملف نسخة محدد من Google Drive وتصفر الكاش المؤقت.
     */
    suspend fun deleteBackupFromDriveById(fileId: String): Boolean = withContext(Dispatchers.IO) {
        val accessToken = getValidAccessTokenOrExpired() ?: return@withContext false
        folderNavigator.clearCache()
        return@withContext networkUploader.deleteFileById(fileId, accessToken)
    }
}

/**
 * [كائن واجهة التوافق - GoogleDriveHelper]:
 * كائن مساعد لتوفير توافق استدعاء دالة تسجيل الخروج من أي مكان في المشروع.
 */
object GoogleDriveHelper {
    suspend fun disconnectAndSignOut(context: Context) {
        GoogleDriveSyncHelper.disconnectAndSignOut(context)
    }
}
