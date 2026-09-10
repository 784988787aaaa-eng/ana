package com.smartledger.aldaftar.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.smartledger.aldaftar.data.backup.BackupEngine
import com.smartledger.aldaftar.data.cloud.CloudArchiveStore
import com.smartledger.aldaftar.data.local.entities.AppSettings
import com.smartledger.aldaftar.data.repository.DataMaintenanceRepository
import com.smartledger.aldaftar.domain.model.CloudBackupFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class BackupSyncViewModel(
    application: Application,
    private val maintenanceRepository: DataMaintenanceRepository,
    private val engine: BackupEngine
) : AndroidViewModel(application) {
    private val cloud = CloudArchiveStore(application.applicationContext)

    private val _cloudBackups = MutableStateFlow<List<CloudBackupFile>>(emptyList())
    val cloudBackups: StateFlow<List<CloudBackupFile>> = _cloudBackups.asStateFlow()
    private val _cloudSearch = MutableStateFlow("")
    val cloudSearch: StateFlow<String> = _cloudSearch.asStateFlow()
    private val _cloudConnected = MutableStateFlow(false)
    val cloudConnected: StateFlow<Boolean> = _cloudConnected.asStateFlow()
    private val _cloudEmail = MutableStateFlow<String?>(null)
    val cloudEmail: StateFlow<String?> = _cloudEmail.asStateFlow()
    private val _busy = MutableStateFlow(false)
    val isBusy: StateFlow<Boolean> = _busy.asStateFlow()
    private val _busyMessage = MutableStateFlow<String?>(null)
    val busyMessage: StateFlow<String?> = _busyMessage.asStateFlow()
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    private var cloudSearchJob: Job? = null
    private var connectJob: Job? = null

    init {
        viewModelScope.launch(Dispatchers.IO) {
            _cloudConnected.value = cloud.connected()
            _cloudEmail.value = cloud.email()
        }
    }

    fun connectCloud(onComplete: (Boolean) -> Unit = {}) {
        if (connectJob?.isActive == true) return
        connectJob = viewModelScope.launch(Dispatchers.IO) {
            _busy.value = true
            _busyMessage.value = "جارٍ الاتصال بحساب Google..."
            _error.value = null
            val ok = runCatching { cloud.connect() }.getOrElse {
                _error.value = it.message ?: "تعذر ربط Google Drive"
                false
            }
            _cloudConnected.value = ok
            _cloudEmail.value = cloud.email()
            _busy.value = false
            _busyMessage.value = null
            withContext(Dispatchers.Main) { onComplete(ok) }
        }
    }

    fun connectCloudWithServerAuthCode(code: String, email: String?, onComplete: (Boolean) -> Unit = {}) {
        if (connectJob?.isActive == true) return
        connectJob = viewModelScope.launch(Dispatchers.IO) {
            _busy.value = true
            _busyMessage.value = "جارٍ تأكيد حساب Google..."
            _error.value = null
            val ok = runCatching { cloud.connectWithServerAuthCode(code) }.getOrElse {
                _error.value = it.message ?: "تعذر تأكيد ربط Google Drive"
                false
            }
            if (ok) {
                cloud.saveEmail(email)
                _cloudEmail.value = email?.trim()?.lowercase()
            }
            _cloudConnected.value = ok
            _busy.value = false
            _busyMessage.value = null
            withContext(Dispatchers.Main) { onComplete(ok) }
        }
    }

    fun cancelConnectCloud() {
        connectJob?.cancel(); connectJob = null
        _busy.value = false; _busyMessage.value = null
    }

    fun disconnectCloud(onComplete: (Boolean) -> Unit = {}) {
        viewModelScope.launch(Dispatchers.IO) {
            val ok = runCatching { cloud.disconnect(); true }.getOrDefault(false)
            _cloudConnected.value = false; _cloudEmail.value = null; _cloudBackups.value = emptyList()
            withContext(Dispatchers.Main) { onComplete(ok) }
        }
    }

    fun setCloudSearch(value: String) {
        _cloudSearch.value = value
        cloudSearchJob?.cancel()
        cloudSearchJob = viewModelScope.launch(Dispatchers.IO) {
            delay(300)
            refreshCloud()
        }
    }

    fun refreshCloud() {
        if (!_cloudConnected.value) { _cloudBackups.value = emptyList(); return }
        viewModelScope.launch(Dispatchers.IO) {
            _busyMessage.value = "جارٍ تحميل قائمة النسخ من السحابة..."
            val result = runCatching { cloud.list(_cloudSearch.value) }
            result.exceptionOrNull()?.let { _error.value = it.message ?: "تعذر تحميل النسخ السحابية" }
            _cloudBackups.value = result.getOrDefault(emptyList())
            _busyMessage.value = null
        }
    }

    fun createCloudBackup(onComplete: (CloudBackupFile?, File?) -> Unit = { _, _ -> }) {
        launchBusy({ pair -> onComplete(pair?.first, pair?.second) }) {
            val file = engine.createManual()
            if (!_cloudConnected.value) return@launchBusy null to file
            val remote = cloud.upload(file.readBytes(), file.name)
            refreshCloud()
            remote to file
        }
    }

    fun createLocalBackup(onComplete: (File?) -> Unit = {}) = launchBusy(onComplete) {
        engine.createManual()
    }

    fun exportBackupBytes(onComplete: (ByteArray?) -> Unit = {}) = launchBusy(onComplete) { engine.createManual().readBytes() }

    fun restoreFromBytes(bytes: ByteArray, recoveryCode: String? = null, onComplete: (Boolean, AppSettings?, String?) -> Unit = { _, _, _ -> }) {
        viewModelScope.launch(Dispatchers.IO) {
            _busy.value = true; _error.value = null
            val result = runCatching { engine.restoreBytes(bytes, recoveryCode) }
            _busy.value = false
            withContext(Dispatchers.Main) { onComplete(result.isSuccess, result.getOrNull(), result.exceptionOrNull()?.message) }
        }
    }

    fun restoreFromLocalFile(file: File, recoveryCode: String? = null, onComplete: (Boolean, AppSettings?, String?) -> Unit = { _, _, _ -> }) {
        viewModelScope.launch(Dispatchers.IO) {
            _busy.value = true; _error.value = null
            val result = runCatching { engine.restoreBytes(file.readBytes(), recoveryCode) }
            _busy.value = false
            withContext(Dispatchers.Main) { onComplete(result.isSuccess, result.getOrNull(), result.exceptionOrNull()?.message) }
        }
    }

    fun restoreCloud(item: CloudBackupFile, recoveryCode: String? = null, onComplete: (Boolean, AppSettings?, String?) -> Unit = { _, _, _ -> }) {
        viewModelScope.launch(Dispatchers.IO) {
            _busy.value = true; _busyMessage.value = "جاري تنزيل النسخة وفك التشفير واستعادة السجلات..."; _error.value = null
            val result = runCatching { engine.restoreBytes(cloud.download(item.id), recoveryCode) }
            _busy.value = false; _busyMessage.value = null
            withContext(Dispatchers.Main) { onComplete(result.isSuccess, result.getOrNull(), result.exceptionOrNull()?.message) }
        }
    }

    fun deleteCloudBackups(ids: Set<String>, onComplete: (Int) -> Unit = {}) = launchBusy({ onComplete(it ?: 0) }) {
        ids.chunked(100).sumOf { cloud.delete(it.toSet()) }.also { refreshCloud() }
    }

    fun clearLocalCopyAndWipeMemory(onComplete: (Boolean) -> Unit = {}) = launchBusy({ onComplete(it == true) }) {
        maintenanceRepository.deleteAllData(); true
    }

    fun recoveryCode(): String = engine.recoveryCode()
    fun googleClientId(onComplete: (String?) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) { val id = runCatching { cloud.googleClientId() }.getOrNull(); withContext(Dispatchers.Main) { onComplete(id) } }
    }

    private fun <T> launchBusy(onComplete: (T?) -> Unit, block: suspend () -> T) {
        viewModelScope.launch(Dispatchers.IO) {
            _busy.value = true; _error.value = null
            val result = runCatching { block() }.onFailure { _error.value = it.message ?: "تعذر تنفيذ العملية" }.getOrNull()
            _busy.value = false
            withContext(Dispatchers.Main) { onComplete(result) }
        }
    }
}
