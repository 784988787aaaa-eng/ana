package com.smartledger.aldaftar.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.smartledger.aldaftar.data.local.AppDatabase
import com.smartledger.aldaftar.data.local.entities.AppSettings
import com.smartledger.aldaftar.data.repository.FinanceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

/**
 * Presentation-only state holder for the existing Backup/Restore UI.
 *
 * This presentation holder contains no external-service implementation.
 * The public callbacks remain only so the preserved UI can compile while
 * the implementation is rebuilt in a later phase.
 */
class BackupSyncViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = FinanceRepository(AppDatabase.getDatabase(application), application)

    private val _cloudBackupsList = MutableStateFlow<List<CloudBackupFile>>(emptyList())
    val cloudBackupsList: StateFlow<List<CloudBackupFile>> = _cloudBackupsList.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val filteredCloudBackups: StateFlow<List<CloudBackupFile>> = cloudBackupsList

    private val _isFetchingCloudBackups = MutableStateFlow(false)
    val isFetchingCloudBackups: StateFlow<Boolean> = _isFetchingCloudBackups.asStateFlow()


    val storedEmailState: StateFlow<String?> = MutableStateFlow(null).asStateFlow()

    fun updateSearchQuery(query: String) { _searchQuery.value = query }

    fun fetchCloudBackupsList() { _cloudBackupsList.value = emptyList() }

    fun getBackupJsonForClipboard(onComplete: (String) -> Unit) { onComplete("") }
    fun exportLocalBackup(context: Context, onComplete: (Result<File>) -> Unit) {
        onComplete(Result.failure(UnsupportedOperationException("Backup implementation is disabled in Phase 1")))
    }
    fun createLocalBackup(context: Context, onComplete: (File?) -> Unit) { onComplete(null) }
    fun triggerSilentLocalBackup() { }
    fun deleteCloudBackupById(fileId: String, onComplete: (Boolean) -> Unit) { onComplete(false) }
    fun deleteMultipleCloudBackupsByIds(fileIds: List<String>, onComplete: (Boolean) -> Unit) { onComplete(false) }

    fun clearLocalCopyAndWipeMemory(context: Context) {
        viewModelScope.launch { repository.deleteAllData() }
    }

    fun executeMasterRestore(
        rawJsonString: String,
        context: Context,
        onComplete: (Boolean, AppSettings?) -> Unit
    ) { onComplete(false, null) }

    fun restoreFromBackupContent(jsonContent: String, context: Context, onComplete: (Boolean) -> Unit) {
        onComplete(false)
    }

    fun restoreFromLocalFile(file: File, context: Context, onComplete: (Boolean, AppSettings?) -> Unit) {
        onComplete(false, null)
    }



}
