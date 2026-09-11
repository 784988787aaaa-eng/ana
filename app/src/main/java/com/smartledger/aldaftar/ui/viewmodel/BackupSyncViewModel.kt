package com.smartledger.aldaftar.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.smartledger.aldaftar.data.account.UnifiedAccountSession
import com.smartledger.aldaftar.data.account.UnifiedAccountSessionRepository
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
    private val engine: BackupEngine,
    private val unifiedAccountRepository: UnifiedAccountSessionRepository,
    private val cloud: CloudArchiveStore = CloudArchiveStore(application.applicationContext)
) : AndroidViewModel(application) {

    val session: StateFlow<UnifiedAccountSession> = unifiedAccountRepository.session

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
        viewModelScope.launch {
            unifiedAccountRepository.session.collect { session ->
                _cloudConnected.value = session.isSignedIn && session.isCloudConnected
                _cloudEmail.value = session.email
                if (session.isSignedIn) {
                    refreshCloud()
                } else {
                    _cloudBackups.value = emptyList()
                }
            }
        }
    }

    fun saveConnectedAccount(email: String) {
        cloud.saveEmail(email)
        _cloudConnected.value = true
        _cloudEmail.value = email.trim().lowercase()
        viewModelScope.launch(Dispatchers.IO) {
            unifiedAccountRepository.refreshSession()
            refreshCloud()
        }
    }

    fun signInWithGoogle(
        account: GoogleSignInAccount,
        serverAuthCode: String? = null,
        onDone: (Boolean) -> Unit = {}
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            _busy.value = true
            _busyMessage.value = "جارٍ الاتصال بحساب Google Drive..."
            _error.value = null
            try {
                unifiedAccountRepository.signInWithGoogle(account, serverAuthCode)
                refreshCloud()
                withContext(Dispatchers.Main) { onDone(true) }
            } catch (e: Exception) {
                _error.value = e.message ?: "تعذر ربط حساب Google"
                withContext(Dispatchers.Main) { onDone(false) }
            } finally {
                _busy.value = false
                _busyMessage.value = null
            }
        }
    }

    fun connectCloud(onComplete: (Boolean) -> Unit = {}) {
        if (connectJob?.isActive == true) return
        connectJob = viewModelScope.launch(Dispatchers.IO) {
            _busy.value = true
            _busyMessage.value = "جارٍ الاتصال بحساب Google..."
            _error.value = null
            val ok = runCatching {
                val success = cloud.connect()
                if (success) {
                    unifiedAccountRepository.refreshSession()
                }
                success
            }.getOrElse {
                _error.value = it.message ?: "تعذر ربط Google Drive"
                false
            }
            _busy.value = false
            _busyMessage.value = null
            if (ok) {
                refreshCloud()
            }
            withContext(Dispatchers.Main) { onComplete(ok) }
        }
    }

    fun connectCloudWithServerAuthCode(code: String, email: String?, onComplete: (Boolean) -> Unit = {}) {
        if (connectJob?.isActive == true) return
        connectJob = viewModelScope.launch(Dispatchers.IO) {
            _busy.value = true
            _busyMessage.value = "جارٍ تأكيد حساب Google..."
            _error.value = null
            val ok = runCatching {
                val success = cloud.connectWithServerAuthCode(code)
                if (success && !email.isNullOrBlank()) {
                    cloud.saveEmail(email)
                    unifiedAccountRepository.refreshSession()
                }
                success
            }.getOrElse {
                _error.value = it.message ?: "تعذر تأكيد ربط Google Drive"
                false
            }
            if (ok) {
                refreshCloud()
            }
            _busy.value = false
            _busyMessage.value = null
            withContext(Dispatchers.Main) { onComplete(ok) }
        }
    }

    fun cancelConnectCloud() {
        connectJob?.cancel()
        connectJob = null
        _busy.value = false
        _busyMessage.value = null
    }

    fun disconnectCloud(onComplete: (Boolean) -> Unit = {}) {
        viewModelScope.launch(Dispatchers.IO) {
            _busy.value = true
            val ok = runCatching {
                unifiedAccountRepository.signOutUnified()
                true
            }.getOrDefault(false)
            _cloudBackups.value = emptyList()
            _busy.value = false
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
        if (!_cloudConnected.value && !cloud.connected()) {
            _cloudBackups.value = emptyList()
            return
        }
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
            if (!_cloudConnected.value && !cloud.connected()) return@launchBusy null to file
            val remote = cloud.upload(file.readBytes(), file.name)
            refreshCloud()
            remote to file
        }
    }

    fun createLocalBackup(onComplete: (File?) -> Unit = {}) = launchBusy(onComplete) {
        engine.createManual()
    }

    fun exportBackupBytes(onComplete: (ByteArray?) -> Unit = {}) = launchBusy(onComplete) {
        engine.createManual().readBytes()
    }

    fun restoreFromBytes(
        bytes: ByteArray,
        recoveryCode: String? = null,
        onComplete: (Boolean, AppSettings?, String?) -> Unit = { _, _, _ -> }
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            _busy.value = true
            _error.value = null
            val result = runCatching { engine.restoreBytes(bytes, recoveryCode) }
            _busy.value = false
            withContext(Dispatchers.Main) {
                onComplete(result.isSuccess, result.getOrNull(), result.exceptionOrNull()?.message)
            }
        }
    }

    fun restoreFromLocalFile(
        file: File,
        recoveryCode: String? = null,
        onComplete: (Boolean, AppSettings?, String?) -> Unit = { _, _, _ -> }
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            _busy.value = true
            _error.value = null
            val result = runCatching { engine.restoreBytes(file.readBytes(), recoveryCode) }
            _busy.value = false
            withContext(Dispatchers.Main) {
                onComplete(result.isSuccess, result.getOrNull(), result.exceptionOrNull()?.message)
            }
        }
    }

    fun restoreCloud(
        item: CloudBackupFile,
        recoveryCode: String? = null,
        onComplete: (Boolean, AppSettings?, String?) -> Unit = { _, _, _ -> }
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            _busy.value = true
            _busyMessage.value = "جاري تنزيل النسخة وفك التشفير واستعادة السجلات..."
            _error.value = null
            val result = runCatching { engine.restoreBytes(cloud.download(item.id), recoveryCode) }
            _busy.value = false
            _busyMessage.value = null
            withContext(Dispatchers.Main) {
                onComplete(result.isSuccess, result.getOrNull(), result.exceptionOrNull()?.message)
            }
        }
    }

    fun deleteCloudBackups(ids: Set<String>, onComplete: (Int) -> Unit = {}) = launchBusy({ onComplete(it ?: 0) }) {
        ids.chunked(100).sumOf { cloud.delete(it.toSet()) }.also { refreshCloud() }
    }

    fun clearLocalCopyAndWipeMemory(onComplete: (Boolean) -> Unit = {}) = launchBusy({ onComplete(it == true) }) {
        maintenanceRepository.deleteAllData()
        true
    }

    fun recoveryCode(): String = engine.recoveryCode()

    fun googleClientId(onComplete: (String?) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val id = runCatching { cloud.googleClientId() }.getOrNull()
            withContext(Dispatchers.Main) { onComplete(id) }
        }
    }

    private fun <T> launchBusy(onComplete: (T?) -> Unit, block: suspend () -> T) {
        viewModelScope.launch(Dispatchers.IO) {
            _busy.value = true
            _error.value = null
            val result = runCatching { block() }.onFailure { _error.value = it.message ?: "تعذر تنفيذ العملية" }.getOrNull()
            _busy.value = false
            withContext(Dispatchers.Main) { onComplete(result) }
        }
    }
}
