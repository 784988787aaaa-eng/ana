package com.smartledger.aldaftar.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.smartledger.aldaftar.R
import com.smartledger.aldaftar.data.account.UnifiedAccountSession
import com.smartledger.aldaftar.data.account.UnifiedAccountSessionRepository
import com.smartledger.aldaftar.data.backup.BackupEngine
import com.smartledger.aldaftar.data.backup.PublicBackupStore
import com.smartledger.aldaftar.data.cloud.CloudArchiveStore
import com.smartledger.aldaftar.data.local.entities.AppSettings
import com.smartledger.aldaftar.data.repository.DataMaintenanceRepository
import com.smartledger.aldaftar.domain.model.CloudBackupFile
import com.smartledger.aldaftar.platform.notifications.BackupNotificationManager
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class BackupOperationState {
    Idle,
    Preparing,
    Uploading,
    Restoring,
    Deleting,
    Refreshing,
    Success,
    Error
}

class BackupSyncViewModel(
    application: Application,
    private val maintenanceRepository: DataMaintenanceRepository,
    private val engine: BackupEngine,
    private val unifiedAccountRepository: UnifiedAccountSessionRepository,
    private val cloud: CloudArchiveStore = CloudArchiveStore(application.applicationContext),
    private val publicBackupStore: PublicBackupStore = PublicBackupStore(application.applicationContext)
) : AndroidViewModel(application) {

    private val appContext = application.applicationContext
    private val backupNotifications = BackupNotificationManager(appContext)

    val session: StateFlow<UnifiedAccountSession> = unifiedAccountRepository.session

    private val _cloudBackups = MutableStateFlow<List<CloudBackupFile>>(emptyList())
    val cloudBackups: StateFlow<List<CloudBackupFile>> = _cloudBackups.asStateFlow()

    private val _cloudSearch = MutableStateFlow("")
    val cloudSearch: StateFlow<String> = _cloudSearch.asStateFlow()

    private val _cloudConnected = MutableStateFlow(false)
    val cloudConnected: StateFlow<Boolean> = _cloudConnected.asStateFlow()

    private val _cloudEmail = MutableStateFlow<String?>(null)
    val cloudEmail: StateFlow<String?> = _cloudEmail.asStateFlow()

    private val _operationState = MutableStateFlow(BackupOperationState.Idle)
    val operationState: StateFlow<BackupOperationState> = _operationState.asStateFlow()
    val isBusy: StateFlow<Boolean> = operationState
        .map { it != BackupOperationState.Idle }
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

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
            _operationState.value = BackupOperationState.Preparing
            _busyMessage.value = appContext.getString(R.string.backup_busy_connecting_drive)
            _error.value = null
            try {
                unifiedAccountRepository.signInWithGoogle(account, serverAuthCode)
                refreshCloud()
                withContext(Dispatchers.Main) { onDone(true) }
            } catch (e: Exception) {
                _error.value = e.message ?: appContext.getString(R.string.backup_err_connect_google_short)
                withContext(Dispatchers.Main) { onDone(false) }
            } finally {
                _operationState.value = BackupOperationState.Idle
                _busyMessage.value = null
            }
        }
    }

    fun connectCloud(onComplete: (Boolean) -> Unit = {}) {
        if (connectJob?.isActive == true) return
        connectJob = viewModelScope.launch(Dispatchers.IO) {
            _operationState.value = BackupOperationState.Preparing
            _busyMessage.value = appContext.getString(R.string.backup_busy_connecting_google)
            _error.value = null
            val ok = runCatching {
                val success = cloud.connect()
                if (success) {
                    unifiedAccountRepository.refreshSession()
                }
                success
            }.getOrElse {
                _error.value = it.message ?: appContext.getString(R.string.backup_err_connect_drive_short)
                false
            }
            _operationState.value = BackupOperationState.Idle
            _busyMessage.value = null
            if (ok) {
                refreshCloud()
            }
            withContext(Dispatchers.Main) { onComplete(ok) }
        }
    }

    fun connectCloudWithServerAuthCode(code: String, email: String?, onComplete: (Boolean) -> Unit = {}) {
        if (connectJob?.isActive == true) return
        if (!tryBeginOperation(BackupOperationState.Preparing, appContext.getString(R.string.backup_busy_confirming_google))) return
        connectJob = viewModelScope.launch(Dispatchers.IO) {
            _error.value = null
            val ok = runCatching {
                val success = cloud.connectWithServerAuthCode(code)
                if (success && !email.isNullOrBlank()) {
                    cloud.saveEmail(email)
                    unifiedAccountRepository.refreshSession()
                }
                success
            }.getOrElse {
                _error.value = it.message ?: appContext.getString(R.string.backup_err_confirm_drive_short)
                false
            }
            if (ok) {
                refreshCloud()
            }
            completeOperation(ok)
            withContext(Dispatchers.Main) { onComplete(ok) }
        }
    }

    fun cancelConnectCloud() {
        connectJob?.cancel()
        connectJob = null
        _operationState.value = BackupOperationState.Idle
        _busyMessage.value = null
    }

    fun disconnectCloud(onComplete: (Boolean) -> Unit = {}) {
        if (!tryBeginOperation(BackupOperationState.Preparing, appContext.getString(R.string.backup_busy_disconnecting_google))) return
        viewModelScope.launch(Dispatchers.IO) {
            val ok = runCatching {
                unifiedAccountRepository.signOutUnified()
                true
            }.getOrDefault(false)
            _cloudBackups.value = emptyList()
            completeOperation(ok)
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
            _busyMessage.value = appContext.getString(R.string.backup_busy_loading_cloud_list)
            val result = runCatching { cloud.list(_cloudSearch.value) }
            result.exceptionOrNull()?.let { _error.value = it.message ?: appContext.getString(R.string.backup_err_load_cloud_list) }
            _cloudBackups.value = result.getOrDefault(emptyList())
            if (_operationState.value == BackupOperationState.Refreshing) {
                completeOperation(result.isSuccess)
            } else if (_operationState.value == BackupOperationState.Idle) {
                _busyMessage.value = null
            }
        }
    }

    fun createCloudBackup(onComplete: (CloudBackupFile?, File?) -> Unit = { _, _ -> }) {
        launchBusy({ pair -> onComplete(pair?.first, pair?.second) }) {
            _busyMessage.value = appContext.getString(R.string.backup_busy_preparing_backup)
            _operationState.value = BackupOperationState.Preparing
            val file = engine.createManual()
            val publicUri = publicBackupStore.publish(file)
            if (!cloud.connected()) {
                throw IllegalStateException(appContext.getString(R.string.backup_err_connect_drive_first))
            }
            _operationState.value = BackupOperationState.Uploading
            _busyMessage.value = appContext.getString(R.string.backup_busy_uploading_drive)
            val remote = cloud.upload(file, file.name)
            _cloudBackups.value = listOf(remote) + _cloudBackups.value.filterNot { it.id == remote.id }
            backupNotifications.show(
                appContext.getString(R.string.backup_notif_uploaded_title),
                appContext.getString(R.string.backup_notif_uploaded_body, file.name),
                publicUri
            )
            remote to file
        }
    }

    fun createLocalBackup(onComplete: (File?) -> Unit = {}) = launchBusy(
        onComplete,
        BackupOperationState.Preparing,
        appContext.getString(R.string.backup_busy_preparing_local)
    ) {
        val file = engine.createManual()
        publicBackupStore.publish(file)
        file
    }

    fun exportBackupBytes(onComplete: (ByteArray?) -> Unit = {}) = launchBusy(
        onComplete,
        BackupOperationState.Preparing,
        appContext.getString(R.string.backup_busy_preparing_archive)
    ) {
        engine.createManual().readBytes()
    }

    fun restoreFromBytes(
        bytes: ByteArray,
        recoveryCode: String? = null,
        onComplete: (Boolean, AppSettings?, String?) -> Unit = { _, _, _ -> }
    ) {
        if (!tryBeginOperation(BackupOperationState.Restoring, appContext.getString(R.string.backup_busy_restoring))) return
        viewModelScope.launch(Dispatchers.IO) {
            _error.value = null
            val result = runCatching { engine.restoreBytes(bytes, recoveryCode) }
            completeOperation(result.isSuccess)
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
        if (!tryBeginOperation(BackupOperationState.Restoring, appContext.getString(R.string.backup_busy_restoring))) return
        viewModelScope.launch(Dispatchers.IO) {
            _error.value = null
            val result = runCatching { engine.restoreBytes(file.readBytes(), recoveryCode) }
            completeOperation(result.isSuccess)
            withContext(Dispatchers.Main) {
                onComplete(result.isSuccess, result.getOrNull(), result.exceptionOrNull()?.message)
            }
        }
    }

    fun restoreLatestCloudBackup(
        onConfirmationRequired: (CloudBackupFile) -> Unit,
        onError: (Int) -> Unit
    ) {
        if (!tryBeginOperation(BackupOperationState.Restoring, appContext.getString(R.string.backup_busy_finding_latest))) return
        viewModelScope.launch(Dispatchers.IO) {
            _busyMessage.value = appContext.getString(R.string.backup_busy_finding_latest)
            _error.value = null
            val currentList = _cloudBackups.value
            val list = if (currentList.isNotEmpty()) {
                currentList
            } else if (cloud.connected()) {
                val fetched = runCatching { cloud.list("") }.getOrElse {
                    _error.value = it.message
                    emptyList()
                }
                _cloudBackups.value = fetched
                fetched
            } else {
                emptyList()
            }
            val latest = list.maxByOrNull { it.modifiedTime } ?: list.firstOrNull()
            completeOperation(latest != null)
            withContext(Dispatchers.Main) {
                if (latest != null) {
                    onConfirmationRequired(latest)
                } else {
                    onError(R.string.backup_err_no_backups_found)
                }
            }
        }
    }

    fun restoreCloud(
        item: CloudBackupFile,
        recoveryCode: String? = null,
        onComplete: (Boolean, AppSettings?, String?) -> Unit = { _, _, _ -> }
    ) {
        if (!tryBeginOperation(BackupOperationState.Restoring, appContext.getString(R.string.backup_busy_downloading_and_restoring))) return
        viewModelScope.launch(Dispatchers.IO) {
            _busyMessage.value = appContext.getString(R.string.backup_busy_downloading_and_restoring)
            _error.value = null
            val result = runCatching { engine.restoreBytes(cloud.download(item.id), recoveryCode) }
            completeOperation(result.isSuccess)
            withContext(Dispatchers.Main) {
                onComplete(result.isSuccess, result.getOrNull(), result.exceptionOrNull()?.message)
            }
        }
    }

    fun deleteCloudBackups(ids: Set<String>, onComplete: (Int) -> Unit = {}) = launchBusy(
        { onComplete(it ?: 0) },
        BackupOperationState.Deleting,
        appContext.getString(R.string.backup_busy_deleting)
    ) {
        ids.chunked(100).sumOf { cloud.delete(it.toSet()) }.also { refreshCloud() }
    }

    fun clearLocalCopyAndWipeMemory(onComplete: (Boolean) -> Unit = {}) = launchBusy(
        { onComplete(it == true) },
        BackupOperationState.Deleting,
        appContext.getString(R.string.backup_busy_wiping_data)
    ) {
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

    private fun tryBeginOperation(state: BackupOperationState, message: String? = null): Boolean {
        if (!_operationState.compareAndSet(BackupOperationState.Idle, state)) return false
        _error.value = null
        _busyMessage.value = message
        return true
    }

    private fun completeOperation(success: Boolean) {
        val terminalState = if (success) BackupOperationState.Success else BackupOperationState.Error
        _operationState.value = terminalState
        viewModelScope.launch {
            delay(if (success) 350L else 900L)
            if (_operationState.value == terminalState) {
                _operationState.value = BackupOperationState.Idle
                _busyMessage.value = null
            }
        }
    }

    private fun <T> launchBusy(
        onComplete: (T?) -> Unit,
        state: BackupOperationState = BackupOperationState.Preparing,
        message: String? = null,
        block: suspend () -> T
    ) {
        if (!tryBeginOperation(state, message)) return
        viewModelScope.launch(Dispatchers.IO) {
            _error.value = null
            val result = runCatching { block() }
                .onFailure { _error.value = it.message ?: appContext.getString(R.string.backup_err_operation_generic) }
                .getOrNull()
            completeOperation(result != null)
            withContext(Dispatchers.Main) { onComplete(result) }
        }
    }
}
