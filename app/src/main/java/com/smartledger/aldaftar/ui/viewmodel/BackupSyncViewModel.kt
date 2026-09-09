package com.smartledger.aldaftar.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.smartledger.aldaftar.data.backup.BackupEngine
import com.smartledger.aldaftar.data.backup.BackupPathManager
import com.smartledger.aldaftar.data.backup.BackupSettingsRepository
import com.smartledger.aldaftar.data.backup.BackupScheduler
import com.smartledger.aldaftar.data.cloud.CloudArchiveStore
import com.smartledger.aldaftar.data.local.entities.AppSettings
import com.smartledger.aldaftar.domain.model.CloudBackupFile
import com.smartledger.aldaftar.data.repository.DataMaintenanceRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.first
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption

class BackupSyncViewModel(
    application: Application,
    private val maintenanceRepository: DataMaintenanceRepository,
    private val engine: BackupEngine
) : AndroidViewModel(application) {
    private val paths = BackupPathManager()
    private val cloud = CloudArchiveStore(application.applicationContext)
    private val backupSettings = BackupSettingsRepository(application.applicationContext)
    private val scheduler = BackupScheduler(application.applicationContext)

    private val _localBackups = MutableStateFlow<List<File>>(emptyList())
    val localBackups: StateFlow<List<File>> = _localBackups.asStateFlow()
    private val _cloudBackups = MutableStateFlow<List<CloudBackupFile>>(emptyList())
    val cloudBackups: StateFlow<List<CloudBackupFile>> = _cloudBackups.asStateFlow()
    private val _cloudSearch = MutableStateFlow("")
    val cloudSearch: StateFlow<String> = _cloudSearch.asStateFlow()
    private val _cloudConnected = MutableStateFlow(false)
    val cloudConnected: StateFlow<Boolean> = _cloudConnected.asStateFlow()
    private val _busy = MutableStateFlow(false)
    val isBusy: StateFlow<Boolean> = _busy.asStateFlow()
    private val _error = MutableStateFlow<String?>(null)
    private val _automaticBackupEnabled = MutableStateFlow(false)
    private var cloudSearchJob: Job? = null
    val automaticBackupEnabled: StateFlow<Boolean> = _automaticBackupEnabled.asStateFlow()
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            _cloudConnected.value = cloud.connected()
            val enabled = backupSettings.enabled.first()
            _automaticBackupEnabled.value = enabled
            if (enabled) scheduler.scheduleNext()
        }
    }

    fun setAutomaticBackupEnabled(enabled: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            backupSettings.setEnabled(enabled)
            _automaticBackupEnabled.value = enabled
            if (enabled) scheduler.scheduleNext() else scheduler.cancel()
        }
    }

    fun recoveryCode(): String = engine.recoveryCode()

    fun refreshLocalBackups() {
        _localBackups.value = runCatching {
            val root = File(paths.documentsRoot(), BackupPathManager.ROOT_FOLDER)
            if (!root.exists()) emptyList() else root.walkTopDown().filter(paths::isSupported).sortedByDescending { it.lastModified() }.toList()
        }.getOrDefault(emptyList())
    }

    fun connectCloud(onComplete: (Boolean) -> Unit = {}) {
        viewModelScope.launch(Dispatchers.IO) {
            _busy.value = true
            _error.value = null
            val ok = runCatching { cloud.connect() }.getOrDefault(false)
            _cloudConnected.value = ok
            if (ok) _cloudBackups.value = runCatching { cloud.list(_cloudSearch.value) }.getOrDefault(emptyList())
            _busy.value = false
            withContext(Dispatchers.Main) { onComplete(ok) }
        }
    }
    fun disconnectCloud() { viewModelScope.launch(Dispatchers.IO) { cloud.disconnect(); _cloudConnected.value = false; _cloudBackups.value = emptyList() } }
    fun setCloudSearch(value: String) {
        _cloudSearch.value = value
        cloudSearchJob?.cancel()
        cloudSearchJob = viewModelScope.launch(Dispatchers.IO) {
            delay(350)
            refreshCloudNow()
        }
    }
    private suspend fun refreshCloudNow() {
        _cloudBackups.value = runCatching { cloud.list(_cloudSearch.value) }.getOrDefault(emptyList())
    }
    fun refreshCloud() { viewModelScope.launch(Dispatchers.IO) { _cloudBackups.value = runCatching { cloud.list(_cloudSearch.value) }.getOrDefault(emptyList()) } }

    fun createLocalBackup(onComplete: (File?) -> Unit = {}) = launchBusy(onComplete) { engine.createManual().also { refreshLocalBackups() } }
    fun createAutomaticBackup(onComplete: (File?) -> Unit = {}) = launchBusy(onComplete) { engine.createAutomatic().also { refreshLocalBackups() } }

    fun uploadLatestToCloud(onComplete: (CloudBackupFile?) -> Unit = {}) = launchBusy(onComplete) {
        val file = engine.createManual()
        val result = cloud.upload(file.readBytes(), file.name)
        refreshCloud()
        result
    }

    fun downloadCloudToLocal(item: CloudBackupFile, onComplete: (File?) -> Unit = {}) = launchBusy(onComplete) {
        val bytes = cloud.download(item.id)
        require(bytes.isNotEmpty()) { "النسخة السحابية فارغة" }
        engine.validateEnvelope(bytes)
        val name = item.name.takeIf { paths.isSafeBackupName(it) } ?: paths.manualFile().name
        val month = Regex("SMN_(\\d{4}-\\d{2})").find(name)?.groupValues?.getOrNull(1)
        val folder = month?.let { File(paths.documentsRoot(), "${BackupPathManager.ROOT_FOLDER}/$it") } ?: paths.monthFolder()
        val target = File(folder, name)
        target.parentFile?.mkdirs()
        writeAtomically(target, bytes)
        refreshLocalBackups()
        target
    }

    fun saveCloudCopyToLocal(bytes: ByteArray, fileName: String? = null, onComplete: (File?) -> Unit = {}) = launchBusy(onComplete) {
        require(bytes.isNotEmpty()) { "النسخة فارغة" }
        engine.validateEnvelope(bytes)
        val name = fileName?.takeIf { paths.isSafeBackupName(it) } ?: paths.manualFile().name
        val month = Regex("SMN_(\\d{4}-\\d{2})").find(name)?.groupValues?.getOrNull(1)
        val folder = month?.let { File(paths.documentsRoot(), "${BackupPathManager.ROOT_FOLDER}/$it") } ?: paths.monthFolder()
        val target = File(folder, name)
        target.parentFile?.mkdirs()
        writeAtomically(target, bytes)
        refreshLocalBackups()
        target
    }

    private fun writeAtomically(target: File, bytes: ByteArray) {
        val temp = File(target.parentFile, ".${target.name}.tmp")
        try {
            temp.writeBytes(bytes)
            runCatching {
                Files.move(temp.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE)
            }.recoverCatching {
                Files.move(temp.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING)
            }.getOrElse { throw IllegalStateException("تعذر حفظ النسخة") }
        } finally {
            if (temp.exists()) temp.delete()
        }
    }

    fun restoreFromLocalFile(file: File, recoveryCode: String? = null, onComplete: (Boolean, AppSettings?, String?) -> Unit = { _, _, _ -> }) = viewModelScope.launch(Dispatchers.IO) {
        _busy.value = true; _error.value = null
        val result = runCatching { engine.restoreBytes(file.readBytes(), recoveryCode) }
        _busy.value = false
        withContext(Dispatchers.Main) { onComplete(result.isSuccess, result.getOrNull(), result.exceptionOrNull()?.message) }
    }

    fun restoreCloud(item: CloudBackupFile, recoveryCode: String? = null, onComplete: (Boolean, AppSettings?, String?) -> Unit = { _, _, _ -> }) = viewModelScope.launch(Dispatchers.IO) {
        _busy.value = true; _error.value = null
        val result = runCatching { engine.restoreBytes(cloud.download(item.id), recoveryCode) }
        _busy.value = false
        withContext(Dispatchers.Main) { onComplete(result.isSuccess, result.getOrNull(), result.exceptionOrNull()?.message) }
    }

    fun deleteLocalBackup(file: File, onComplete: (Boolean) -> Unit = {}) = launchBusy(onComplete) {
        val root = File(paths.documentsRoot(), BackupPathManager.ROOT_FOLDER).canonicalPath
        require(file.canonicalPath.startsWith("$root${File.separator}")) { "مسار غير مسموح" }
        val ok = file.delete(); refreshLocalBackups(); ok
    }
    fun deleteCloudBackups(ids: Set<String>, onComplete: (Int) -> Unit = {}) = launchBusy(onComplete) {
        ids.toList().chunked(100).sumOf { cloud.delete(it.toSet()) }.also { refreshCloud() }
    }
    fun clearLocalCopyAndWipeMemory(context: android.content.Context, onComplete: (Boolean) -> Unit = {}) = launchBusy(onComplete) { maintenanceRepository.deleteAllData(); true }
    fun clearError() { _error.value = null }

    private fun <T> launchBusy(onComplete: (T?) -> Unit, block: suspend () -> T) {
        viewModelScope.launch(Dispatchers.IO) {
            _busy.value = true; _error.value = null
            val result = runCatching { block() }.onFailure { _error.value = it.message ?: "تعذر تنفيذ العملية" }.getOrNull()
            _busy.value = false
            withContext(Dispatchers.Main) { onComplete(result) }
        }
    }
}
