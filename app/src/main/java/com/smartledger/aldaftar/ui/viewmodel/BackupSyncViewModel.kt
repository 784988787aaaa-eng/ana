package com.smartledger.aldaftar.ui.viewmodel

import android.util.Log
import android.app.Application
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.smartledger.aldaftar.data.CloudBackupFile
import com.smartledger.aldaftar.data.CloudSyncState
import com.smartledger.aldaftar.data.GoogleDriveSyncHelper
import com.smartledger.aldaftar.data.local.AppDatabase
import com.smartledger.aldaftar.data.local.entities.AppSettings
import com.smartledger.aldaftar.data.repository.FinanceRepository
import com.smartledger.aldaftar.ui.viewmodel.backup.BackupPayloadBuilder
import com.smartledger.aldaftar.ui.viewmodel.backup.BackupSearchMatcher
import com.smartledger.aldaftar.ui.viewmodel.backup.OAuthCodeParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val TAG = "BackupSyncViewModel"
private const val MAX_LOCAL_RESTORE_BYTES = 64L * 1024L * 1024L

class BackupSyncViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: FinanceRepository
    private val backupRepository: com.smartledger.aldaftar.data.repository.BackupRepository
    private val backupRestoreMutex = Mutex()
    val showActivationRequired = MutableStateFlow(false)
    val googleDriveSyncHelper: GoogleDriveSyncHelper
    val googleDriveSyncState: StateFlow<CloudSyncState>
    val storedEmailState: StateFlow<String?>

    private val _cloudBackupsList = MutableStateFlow<List<CloudBackupFile>>(emptyList())
    val cloudBackupsList: StateFlow<List<CloudBackupFile>> = _cloudBackupsList.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    @OptIn(kotlinx.coroutines.FlowPreview::class)
    val filteredCloudBackups: StateFlow<List<CloudBackupFile>> = combine(
        _cloudBackupsList,
        _searchQuery.debounce(300)
    ) { backups, query ->
        if (query.isBlank()) {
            backups
        } else {
            backups.filter { backup ->
                BackupSearchMatcher.matchesFlexibleQuery(backup.name, query)
            }
        }
    }
    .flowOn(Dispatchers.Default)
    .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _isFetchingCloudBackups = MutableStateFlow(false)
    val isFetchingCloudBackups: StateFlow<Boolean> = _isFetchingCloudBackups.asStateFlow()

    private val _localBackups = MutableStateFlow<List<File>>(emptyList())
    val localBackups: StateFlow<List<File>> = _localBackups.asStateFlow()

    init {
        val database = AppDatabase.getDatabase(application)
        repository = FinanceRepository(database, application)
        backupRepository = com.smartledger.aldaftar.data.repository.BackupRepository(application, database)
        googleDriveSyncHelper = GoogleDriveSyncHelper(application)
        googleDriveSyncState = googleDriveSyncHelper.syncState
        storedEmailState = com.smartledger.aldaftar.domain.GoogleAuthSessionManager.currentEmail
        refreshLocalBackups()
    }

    // إدارة الأدلة والملفات المحلية للنسخ الاحتياطي
    fun getBaseBackupDirectory(): File = repository.getBaseBackupDirectory()
    fun getBackupDirectory(): File = repository.getBackupDirectory()

    fun refreshLocalBackups() {
        viewModelScope.launch(Dispatchers.IO) {
            val baseDir = repository.getBaseBackupDirectory()
            val files = repository.getAllMzdFilesRecursively(baseDir)
            _localBackups.value = files.sortedByDescending { it.lastModified() }
        }
    }

    // إدارة المصادقة والمزامنة السحابية
    fun getClientIdOverride(): String = googleDriveSyncHelper.getClientIdOverride()
    fun getClientSecretOverride(): String = googleDriveSyncHelper.getClientSecretOverride()
    fun getAppSignatureSHA1(): String = googleDriveSyncHelper.getAppSignatureSHA1()

    fun saveClientCredentialsOverride(clientId: String?, clientSecret: String?) {
        googleDriveSyncHelper.saveClientCredentialsOverride(clientId, clientSecret)
    }

    fun updateCloudSyncState(state: CloudSyncState) {
        googleDriveSyncHelper.updateSyncState(state)
    }

    fun handleGoogleOAuthCode(code: String, email: String? = null, redirectUri: String = "", onComplete: ((Boolean) -> Unit)? = null) {
        viewModelScope.launch {
            val success = googleDriveSyncHelper.handleAuthorizationCode(code, email, redirectUri)
            if (success) {
                val current = repository.getSettingsDirect() ?: AppSettings()
                repository.saveSettings(current.copy(isCloudSyncEnabled = true))
            }
            onComplete?.invoke(success)
        }
    }

    fun handleRawOAuthCodeOrUrl(input: String, email: String? = null, redirectUri: String = "", onComplete: ((Boolean) -> Unit)? = null) {
        val finalCode = OAuthCodeParser.extractCodeFromInput(input)
        if (finalCode.isEmpty()) return
        handleGoogleOAuthCode(finalCode, email, redirectUri, onComplete)
    }

    fun googleDriveLogout(onComplete: (() -> Unit)? = null) {
        viewModelScope.launch {
            val current = repository.getSettingsDirect() ?: AppSettings()
            repository.saveSettings(current.copy(isCloudSyncEnabled = false))
        }
        googleDriveSyncHelper.logoutAsync {
            _cloudBackupsList.value = emptyList()
            onComplete?.invoke()
        }
    }

    fun fetchCloudBackupsList() {
        viewModelScope.launch {
            try {
                _isFetchingCloudBackups.value = true
                val list = googleDriveSyncHelper.listCloudBackups()
                _cloudBackupsList.value = list
                _isFetchingCloudBackups.value = false
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching cloud backups list", e)
                _isFetchingCloudBackups.value = false
            }
        }
    }

    private val backupPrefs by lazy {
        getApplication<Application>().getSharedPreferences(FinanceConstants.PREFS_BACKUP, Context.MODE_PRIVATE)
    }

    private suspend fun buildBackupJson(isMzd: Boolean, context: Context = getApplication()): String {
        return BackupPayloadBuilder.buildBackupJson(repository, isMzd, context)
    }

    // استخراج بيانات قاعدة البيانات لتصديرها كبيانات منظمة
    fun getBackupJsonForClipboard(onComplete: (String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val jsonStr = buildBackupJson(isMzd = false)
                withContext(Dispatchers.Main) {
                    onComplete(jsonStr)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error generating clipboard backup JSON", e)
            }
        }
    }

    // عمليات الرفع المباشر والنسخ السحابي والمحلي
    fun uploadBackupToGoogleDrive(onComplete: (Boolean) -> Unit) {
        val sdfName = SimpleDateFormat(FinanceConstants.BACKUP_DATE_FORMAT, Locale.US)
        val dateStr = sdfName.format(Date())
        val newFileName = "${FinanceConstants.BACKUP_CLOUD_FILE_PREFIX}$dateStr${FinanceConstants.BACKUP_FILE_EXTENSION}"
        uploadBackupToGoogleDriveWithFilename(newFileName, onComplete)
    }

    fun uploadBackupToGoogleDriveWithFilename(filename: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            backupRestoreMutex.withLock {
                try {
                    val isTrulySignedIn = googleDriveSyncHelper.isUserTrulySignedIn()
                    val refreshToken = googleDriveSyncHelper.getStoredRefreshToken()
                    val isConnected = isTrulySignedIn || !refreshToken.isNullOrEmpty()
                    if (!isConnected) {
                        launch(Dispatchers.Main) {
                            onComplete(false)
                        }
                        return@withLock
                    }

                    val jsonStr = buildBackupJson(isMzd = true)
                    val success = googleDriveSyncHelper.uploadBackupToDriveWithFilename(filename, jsonStr)
                    if (success) {
                        com.smartledger.aldaftar.ui.helper.VibrationHelper.triggerSuccessVibration(getApplication())
                        backupPrefs.edit().putLong(FinanceConstants.KEY_LAST_SUCCESSFUL_BACKUP, System.currentTimeMillis()).apply()
                        fetchCloudBackupsList()
                    }
                    launch(Dispatchers.Main) {
                        onComplete(success)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error in uploadBackupToGoogleDriveWithFilename", e)
                    launch(Dispatchers.Main) {
                        onComplete(false)
                    }
                }
            }
        }
    }

    fun backupToGoogleDriveDirect(onComplete: ((Boolean) -> Unit)? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            backupRestoreMutex.withLock {
                try {
                    val isTrulySignedIn = googleDriveSyncHelper.isUserTrulySignedIn()
                    val refreshToken = googleDriveSyncHelper.getStoredRefreshToken()
                    val isConnected = isTrulySignedIn || !refreshToken.isNullOrEmpty()
                    if (!isConnected) {
                        launch(Dispatchers.Main) {
                            onComplete?.invoke(false)
                        }
                        return@withLock
                    }

                    val jsonStr = buildBackupJson(isMzd = true)
                    val success = googleDriveSyncHelper.uploadBackupToDrive(jsonStr)
                    if (success) {
                        com.smartledger.aldaftar.ui.helper.VibrationHelper.triggerSuccessVibration(getApplication())
                        backupPrefs.edit().putLong(FinanceConstants.KEY_LAST_SUCCESSFUL_BACKUP, System.currentTimeMillis()).apply()
                    }
                    launch(Dispatchers.Main) {
                        onComplete?.invoke(success)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error in backupToGoogleDriveDirect", e)
                    launch(Dispatchers.Main) {
                        onComplete?.invoke(false)
                    }
                }
            }
        }
    }

    fun restoreFromGoogleDriveDirect(context: Context, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            backupRestoreMutex.withLock {
                try {
                    val isTrulySignedIn = googleDriveSyncHelper.isUserTrulySignedIn()
                    val refreshToken = googleDriveSyncHelper.getStoredRefreshToken()
                    val accessToken = googleDriveSyncHelper.getStoredAccessToken()
                    val isConnected = isTrulySignedIn || !refreshToken.isNullOrEmpty() || !accessToken.isNullOrEmpty()
                    if (!isConnected) {
                        launch(Dispatchers.Main) {
                            onComplete(false)
                        }
                        return@withLock
                    }

                    val jsonStr = googleDriveSyncHelper.downloadBackupFromDrive()
                    if (jsonStr != null) {
                        val result = repository.executeMasterRestore(jsonStr)
                        if (repository.isTrialExpiredDirect()) {
                            showActivationRequired.value = true
                        }
                        refreshLocalBackups()
                        launch(Dispatchers.Main) {
                            com.smartledger.aldaftar.ui.helper.VibrationHelper.triggerSuccessVibration(context)
                            onComplete(true)
                        }
                    } else {
                        launch(Dispatchers.Main) {
                            onComplete(false)
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error in restoreFromGoogleDriveDirect", e)
                    launch(Dispatchers.Main) {
                        onComplete(false)
                    }
                }
            }
        }
    }

    fun restoreFromGoogleDriveById(context: Context, fileId: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            backupRestoreMutex.withLock {
                try {
                    val isTrulySignedIn = googleDriveSyncHelper.isUserTrulySignedIn()
                    val refreshToken = googleDriveSyncHelper.getStoredRefreshToken()
                    val accessToken = googleDriveSyncHelper.getStoredAccessToken()
                    val isConnected = isTrulySignedIn || !refreshToken.isNullOrEmpty() || !accessToken.isNullOrEmpty()
                    if (!isConnected) {
                        launch(Dispatchers.Main) {
                            onComplete(false)
                        }
                        return@withLock
                    }

                    val jsonStr = googleDriveSyncHelper.downloadBackupFromDriveById(fileId)
                    if (jsonStr != null) {
                        val result = repository.executeMasterRestore(jsonStr)
                        if (repository.isTrialExpiredDirect()) {
                            showActivationRequired.value = true
                        }
                        refreshLocalBackups()
                        launch(Dispatchers.Main) {
                            com.smartledger.aldaftar.ui.helper.VibrationHelper.triggerSuccessVibration(context)
                            onComplete(true)
                        }
                    } else {
                        launch(Dispatchers.Main) {
                            onComplete(false)
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error in restoreFromGoogleDriveById", e)
                    launch(Dispatchers.Main) {
                        onComplete(false)
                    }
                }
            }
        }
    }

    fun deleteCloudBackupById(fileId: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            backupRestoreMutex.withLock {
                try {
                    val success = googleDriveSyncHelper.deleteBackupFromDriveById(fileId)
                    if (success) {
                        fetchCloudBackupsList()
                    }
                    launch(Dispatchers.Main) {
                        onComplete(success)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error in deleteCloudBackupById", e)
                    launch(Dispatchers.Main) {
                        onComplete(false)
                    }
                }
            }
        }
    }

    fun deleteMultipleCloudBackupsByIds(fileIds: List<String>, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            backupRestoreMutex.withLock {
                try {
                    var allSuccess = true
                    for (fileId in fileIds) {
                        val success = googleDriveSyncHelper.deleteBackupFromDriveById(fileId)
                        if (!success) {
                            allSuccess = false
                        }
                    }
                    if (fileIds.isNotEmpty()) {
                        fetchCloudBackupsList()
                    }
                    launch(Dispatchers.Main) {
                        onComplete(allSuccess)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error in deleteMultipleCloudBackupsByIds", e)
                    launch(Dispatchers.Main) {
                        onComplete(false)
                    }
                }
            }
        }
    }

    fun exportLocalBackup(context: Context, onComplete: (Result<File>) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            backupRestoreMutex.withLock {
                try {
                    val result = backupRepository.createLocalBackup()
                    when (result) {
                        is com.smartledger.aldaftar.data.backup.BackupOperationResult.Success -> {
                            com.smartledger.aldaftar.ui.helper.VibrationHelper.triggerSuccessVibration(context)
                            refreshLocalBackups()
                            launch(Dispatchers.Main) {
                                onComplete(Result.success(result.file))
                            }
                        }
                        is com.smartledger.aldaftar.data.backup.BackupOperationResult.Failure -> {
                            Log.e(TAG, "فشل إنشاء النسخة المحلية: ${result.userMessage}")
                            launch(Dispatchers.Main) {
                                onComplete(Result.failure(result.cause ?: java.io.IOException(result.userMessage)))
                            }
                        }
                    }
                } catch (e: kotlinx.coroutines.CancellationException) {
                    // Ignore normal coroutine cancellation
                } catch (e: Exception) {
                    Log.e(TAG, "استثناء في exportLocalBackup", e)
                    launch(Dispatchers.Main) {
                        onComplete(Result.failure(e))
                    }
                }
            }
        }
    }

    fun createLocalBackup(context: Context, onComplete: (File?) -> Unit) {
        exportLocalBackup(context) { result ->
            onComplete(result.getOrNull())
        }
    }

    private var lastSilentBackupTime: Long = 0L

    fun triggerSilentLocalBackup() {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastSilentBackupTime < 600000) {
            return
        }
        viewModelScope.launch(Dispatchers.IO + NonCancellable) {
            if (!backupRestoreMutex.tryLock()) {
                return@launch
            }
            try {
                val result = backupRepository.createSilentBackup()
                if (result is com.smartledger.aldaftar.data.backup.BackupOperationResult.Success) {
                    lastSilentBackupTime = currentTime
                    refreshLocalBackups()
                }
            } catch (e: kotlinx.coroutines.CancellationException) {
                // Ignore normal cancellation
            } catch (e: Exception) {
                Log.e(TAG, "استثناء في triggerSilentLocalBackup", e)
            } finally {
                backupRestoreMutex.unlock()
            }
        }
    }

    fun clearLocalCopyAndWipeMemory(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            backupRestoreMutex.withLock {
                repository.deleteAllData()
                refreshLocalBackups()
            }
        }
    }

    fun executeMasterRestore(rawJsonString: String, context: Context, onComplete: (Boolean, AppSettings?) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            backupRestoreMutex.withLock {
                try {
                    val result = repository.executeMasterRestore(rawJsonString)

                    if (repository.isTrialExpiredDirect()) {
                        showActivationRequired.value = true
                    }

                    val successMessageRes = if (result.isLegacy) com.smartledger.aldaftar.R.string.toast_restore_legacy_migrated else com.smartledger.aldaftar.R.string.cloud_toast_restore_success

                    withContext(Dispatchers.Main) {
                        com.smartledger.aldaftar.ui.helper.VibrationHelper.triggerSuccessVibration(context)
                        android.widget.Toast.makeText(context, successMessageRes, android.widget.Toast.LENGTH_SHORT).show()
                        onComplete(true, result.settings)
                    }
                } catch (e: org.json.JSONException) {
                    Log.e(TAG, "JSON Schema mismatch during executeMasterRestore", e)
                    withContext(Dispatchers.Main) {
                        android.widget.Toast.makeText(context, com.smartledger.aldaftar.R.string.backup_schema_mismatch, android.widget.Toast.LENGTH_LONG).show()
                        onComplete(false, null)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "General failure during executeMasterRestore", e)
                    withContext(Dispatchers.Main) {
                        android.widget.Toast.makeText(context, com.smartledger.aldaftar.R.string.cloud_toast_restore_failed, android.widget.Toast.LENGTH_LONG).show()
                        onComplete(false, null)
                    }
                }
            }
        }
    }

    /**
     * يقرأ النسخة من معرّف المحتوى الذي يمنحه منتقي الملفات، ثم يتحقق من الحجم
     * وامتداد الاسم وبنية المحتوى قبل تمرير النص إلى مسار الاستعادة الذرية.
     */
    fun readLocalBackupFromUri(
        context: Context,
        uri: Uri,
        onComplete: (String?) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = runCatching {
                val resolver = context.contentResolver
                val displayName = resolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE), null, null, null)
                    ?.use { cursor ->
                        if (!cursor.moveToFirst()) return@use Pair<String?, Long?>(null, null)
                        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                        Pair(
                            if (nameIndex >= 0) cursor.getString(nameIndex) else null,
                            if (sizeIndex >= 0 && !cursor.isNull(sizeIndex)) cursor.getLong(sizeIndex) else null
                        )
                    } ?: Pair(null, null)

                val name = displayName.first.orEmpty()
                val size = displayName.second
                val supportedName = name.isBlank() ||
                    name.endsWith(".mzd", ignoreCase = true) ||
                    name.endsWith(".json", ignoreCase = true)
                require(supportedName) { "امتداد ملف النسخة غير مدعوم" }
                require(size == null || size in 1L..MAX_LOCAL_RESTORE_BYTES) { "حجم ملف النسخة غير مسموح" }

                val input = resolver.openInputStream(uri) ?: error("تعذر فتح ملف النسخة")
                input.use { stream ->
                    val buffer = ByteArray(8192)
                    val output = StringBuilder()
                    var total = 0L
                    while (true) {
                        val count = stream.read(buffer)
                        if (count < 0) break
                        total += count
                        require(total <= MAX_LOCAL_RESTORE_BYTES) { "حجم ملف النسخة يتجاوز الحد المسموح" }
                        output.append(String(buffer, 0, count, Charsets.UTF_8))
                    }
                    val content = output.toString()
                    require(content.isNotBlank()) { "محتوى ملف النسخة فارغ" }
                    val validation = com.smartledger.aldaftar.data.serialization.BackupPayloadValidator
                        .validateBackupPayload(content, verifyHashStrictly = true)
                    require(validation is com.smartledger.aldaftar.data.serialization.BackupValidationResult.Valid) {
                        "فشل التحقق من سلامة وبنية النسخة"
                    }
                    content
                }
            }

            withContext(Dispatchers.Main) {
                onComplete(result.getOrNull())
            }
        }
    }

    fun restoreFromMzdContent(jsonContent: String, context: Context, onComplete: (Boolean) -> Unit) {
        executeMasterRestore(jsonContent, context) { success, _ ->
            onComplete(success)
        }
    }

    fun restoreFromLocalFile(file: File, context: Context, onComplete: (Boolean, AppSettings?) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (file.exists()) {
                    val content = file.readText()
                    executeMasterRestore(content, context) { success, restoredSettings ->
                        onComplete(success, restoredSettings)
                    }
                } else {
                    launch(Dispatchers.Main) {
                        android.widget.Toast.makeText(context, com.smartledger.aldaftar.R.string.cloud_toast_restore_failed, android.widget.Toast.LENGTH_SHORT).show()
                        onComplete(false, null)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error in restoreFromLocalFile", e)
                launch(Dispatchers.Main) {
                    onComplete(false, null)
                }
            }
        }
    }
}
