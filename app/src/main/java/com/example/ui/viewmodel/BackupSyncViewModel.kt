/*
 * ╔══════════════════════════════════════════════════════════════════════╗
 * ║ التوثيق العربي الهندسي — MASTERCLASS DOCUMENTATION                 ║
 * ╚══════════════════════════════════════════════════════════════════════╝
 *
 * الملف: app/src/main/java/com/example/ui/viewmodel/BackupSyncViewModel.kt
 * القطاع المعماري: ViewModels & UI State.
 *
 * الوصف المعماري:
 * منسق حالة النسخ الاحتياطي والمزامنة؛ يربط أحداث الواجهة بخدمات النسخ المحلي والسحابي ويعرض حالة العملية للمستخدم.
 *
 * الرؤية التعليمية والبصرية:
 * تخيل شاشة الهاتف أثناء تفاعل المستخدم: يضغط على زر أو يغيّر قيمة،
 * فتتولد إشارة، ثم تُعالج في طبقة الحالة، ثم تتغير الحالة التي تقرأها
 * Compose لإعادة رسم الشاشة. هذا الملف يقع في تلك السلسلة ويجب قراءته
 * باعتباره عقداً بين «ما فعله المستخدم» و«ما تراه الشاشة».
 *
 * قاعدة الثبات البرمجي:
 * النص التنفيذي الأصلي محفوظ حرفياً بعد هذا الرأس. الإضافات هنا توثيقية
 * فقط ولا تستبدل أي تعليمة أو اسماً أو قيمة أو منطقاً تنفيذياً.
 */

// --- الفهرس التوثيقي للعناصر البرمجية ---
// السطر 28: private const val TAG — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 30: class BackupSyncViewModel — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 32: private val repository — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 33: private val backupRepository — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 34: private val backupRestoreMutex — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 35: val showActivationRequired — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 36: val googleDriveSyncHelper — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 37: val googleDriveSyncState — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 38: val storedEmailState — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 40: private val _cloudBackupsList — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 41: val cloudBackupsList — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 43: private val _searchQuery — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 44: val searchQuery — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 46: fun updateSearchQuery — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 51: val filteredCloudBackups — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 70: private val _isFetchingCloudBackups — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 71: val isFetchingCloudBackups — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 73: private val _localBackups — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 74: val localBackups — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 77: val database — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 87: fun getBaseBackupDirectory — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 88: fun getBackupDirectory — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 90: fun refreshLocalBackups — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 92: val baseDir — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 93: val files — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 99: fun getClientIdOverride — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 100: fun getClientSecretOverride — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 101: fun getAppSignatureSHA1 — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 103: fun saveClientCredentialsOverride — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 107: fun updateCloudSyncState — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 111: fun handleGoogleOAuthCode — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 113: val success — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 115: val current — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 122: fun handleRawOAuthCodeOrUrl — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 123: val finalCode — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 128: fun googleDriveLogout — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 130: val current — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 139: fun fetchCloudBackupsList — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 143: val list — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 153: private val backupPrefs — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 157: private suspend fun buildBackupJson — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 162: fun getBackupJsonForClipboard — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 165: val jsonStr — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 176: fun uploadBackupToGoogleDrive — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 177: val sdfName — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 178: val dateStr — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 179: val newFileName — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 183: fun uploadBackupToGoogleDriveWithFilename — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 187: val isTrulySignedIn — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 188: val refreshToken — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 189: val isConnected — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 197: val jsonStr — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 198: val success — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 217: fun backupToGoogleDriveDirect — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 221: val isTrulySignedIn — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 222: val refreshToken — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 223: val isConnected — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 231: val jsonStr — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 232: val success — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 250: fun restoreFromGoogleDriveDirect — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 254: val isTrulySignedIn — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 255: val refreshToken — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 256: val accessToken — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 257: val isConnected — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 265: val jsonStr — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 267: val result — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 291: fun restoreFromGoogleDriveById — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 295: val isTrulySignedIn — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 296: val refreshToken — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 297: val accessToken — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 298: val isConnected — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 306: val jsonStr — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 308: val result — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 332: fun deleteCloudBackupById — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 336: val success — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 353: fun deleteMultipleCloudBackupsByIds — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 357: var allSuccess — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 359: val success — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 380: fun exportLocalBackup — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 384: val result — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 410: fun createLocalBackup — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 416: private var lastSilentBackupTime — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 418: fun triggerSilentLocalBackup — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 419: val currentTime — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 428: val result — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 441: fun clearLocalCopyAndWipeMemory — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 450: fun executeMasterRestore — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 454: val result — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 460: val successMessageRes — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 484: fun restoreFromMzdContent — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 490: fun restoreFromLocalFile — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 494: val content — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// --- نهاية الفهرس التوثيقي ---

package com.example.ui.viewmodel

import android.util.Log
import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.CloudBackupFile
import com.example.data.CloudSyncState
import com.example.data.GoogleDriveSyncHelper
import com.example.data.local.AppDatabase
import com.example.data.local.entities.AppSettings
import com.example.data.repository.FinanceRepository
import com.example.ui.viewmodel.backup.BackupPayloadBuilder
import com.example.ui.viewmodel.backup.BackupSearchMatcher
import com.example.ui.viewmodel.backup.OAuthCodeParser
import kotlinx.coroutines.Dispatchers
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

class BackupSyncViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: FinanceRepository
    private val backupRepository: com.example.data.repository.BackupRepository
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
        backupRepository = com.example.data.repository.BackupRepository(application, database)
        googleDriveSyncHelper = GoogleDriveSyncHelper(application)
        googleDriveSyncState = googleDriveSyncHelper.syncState
        storedEmailState = com.example.domain.GoogleAuthSessionManager.currentEmail
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

    // مصادقة Google Drive والمزامنة
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

    // استخراج بيانات قاعدة البيانات لتصديرها كـ JSON
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
                        com.example.ui.helper.VibrationHelper.triggerSuccessVibration(getApplication())
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
                        com.example.ui.helper.VibrationHelper.triggerSuccessVibration(getApplication())
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
                            com.example.ui.helper.VibrationHelper.triggerSuccessVibration(context)
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
                            com.example.ui.helper.VibrationHelper.triggerSuccessVibration(context)
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
                        is com.example.data.backup.BackupOperationResult.Success -> {
                            com.example.ui.helper.VibrationHelper.triggerSuccessVibration(context)
                            refreshLocalBackups()
                            launch(Dispatchers.Main) {
                                onComplete(Result.success(result.file))
                            }
                        }
                        is com.example.data.backup.BackupOperationResult.Failure -> {
                            Log.e(TAG, "فشل إنشاء النسخة المحلية: ${result.userMessage}")
                            launch(Dispatchers.Main) {
                                onComplete(Result.failure(result.cause ?: java.io.IOException(result.userMessage)))
                            }
                        }
                    }
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
        viewModelScope.launch(Dispatchers.IO) {
            if (!backupRestoreMutex.tryLock()) {
                return@launch
            }
            try {
                val result = backupRepository.createSilentBackup()
                if (result is com.example.data.backup.BackupOperationResult.Success) {
                    lastSilentBackupTime = currentTime
                    refreshLocalBackups()
                }
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

                    val successMessageRes = if (result.isLegacy) com.example.R.string.toast_restore_legacy_migrated else com.example.R.string.cloud_toast_restore_success

                    withContext(Dispatchers.Main) {
                        com.example.ui.helper.VibrationHelper.triggerSuccessVibration(context)
                        android.widget.Toast.makeText(context, successMessageRes, android.widget.Toast.LENGTH_SHORT).show()
                        onComplete(true, result.settings)
                    }
                } catch (e: org.json.JSONException) {
                    Log.e(TAG, "JSON Schema mismatch during executeMasterRestore", e)
                    withContext(Dispatchers.Main) {
                        android.widget.Toast.makeText(context, com.example.R.string.backup_schema_mismatch, android.widget.Toast.LENGTH_LONG).show()
                        onComplete(false, null)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "General failure during executeMasterRestore", e)
                    withContext(Dispatchers.Main) {
                        android.widget.Toast.makeText(context, com.example.R.string.cloud_toast_restore_failed, android.widget.Toast.LENGTH_LONG).show()
                        onComplete(false, null)
                    }
                }
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
                        android.widget.Toast.makeText(context, com.example.R.string.cloud_toast_restore_failed, android.widget.Toast.LENGTH_SHORT).show()
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


/*
 * --- ملاحظات وتوصيات المعمارية البرمجية ---
 * 1) يجب أن تبقى ViewModel طبقة تنسيق للحالة والأحداث، لا مستودعاً لقواعد
 *    المجال المالية التي ينبغي أن تعيش في طبقاتها المتخصصة.
 * 2) يوصى مستقبلاً بمراجعة دورة حياة كل Coroutine/Flow والتأكد من ارتباطها
 *    بـ viewModelScope أو نطاقها المقصود لمنع التسرب أو العمل بعد زوال الشاشة.
 * 3) عند تعديل UiState يجب الحفاظ على دلالة الحالات الانتقالية مثل التحميل،
 *    النجاح، الخطأ، والفراغ حتى لا تظهر واجهة مضللة للمستخدم.
 * 4) أي تغيير في الأحداث أو العقود العامة يجب أن يرافقه Regression Test
 *    يثبت أن التفاعل الحالي في Compose لم يتغير.
 * 5) الحسابات المالية والـ BigDecimal يجب أن تبقى في مسارها الدقيق، وألا
 *    تتحول إلى Double/Float داخل طبقة العرض إلا بقرار موثق وصريح.
 * 6) هذه التوصيات مرجعية مستقبلية فقط ولا تمثل أي تغيير في التنفيذ الحالي.
 */
