package com.smartledger.aldaftar.data.backup

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.backupDataStore by preferencesDataStore("smartledger_backup_settings")

class BackupSettingsRepository(private val context: Context) {
    private val enabledKey = booleanPreferencesKey("automatic_backup_enabled")
    val enabled: Flow<Boolean> = context.backupDataStore.data.map { it[enabledKey] ?: false }
    suspend fun setEnabled(value: Boolean) { context.backupDataStore.edit { it[enabledKey] = value } }
}
