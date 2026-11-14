package com.smartledger.aldaftar.data.backup

import com.smartledger.aldaftar.data.cloud.CloudArchiveStore
import android.net.Uri
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean

/** دورة النسخ اليومية: نسخة محلية مؤكدة أولاً، ثم رفع النسخة نفسها إلى السحابة. */
class AutomaticBackupCoordinator(
    private val engine: BackupEngine,
    private val cloud: CloudArchiveStore,
    private val publicStore: PublicBackupStore
) {
    private val running = AtomicBoolean(false)

    companion object {
        private const val PREFS_NAME = "smartledger_auto_backup_meta"
        private const val KEY_LAST_DAILY_TS = "last_daily_backup_ts"
        private const val MIN_DAILY_INTERVAL_MS = 20L * 60L * 60L * 1000L // 20 hours
    }

    suspend fun runDaily(): Result {
        if (!running.compareAndSet(false, true)) return Result.AlreadyRunning
        return try {
            val local = engine.createAutomatic()
            val publicUri = runCatching { publicStore.publish(local) }.getOrNull()
            if (!cloud.connected()) return Result.LocalOnly(local, null, publicUri)

            runCatching { cloud.upload(local, local.name) }
                .fold(
                    onSuccess = { Result.LocalAndCloud(local, publicUri) },
                    onFailure = { Result.LocalOnly(local, it, publicUri) }
                )
        } finally {
            running.set(false)
        }
    }

    suspend fun runDailyIfDue(context: android.content.Context): Result? {
        val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, android.content.Context.MODE_PRIVATE)
        val now = System.currentTimeMillis()
        val lastRun = prefs.getLong(KEY_LAST_DAILY_TS, 0L)
        if (now - lastRun < MIN_DAILY_INTERVAL_MS) return null
        val result = runDaily()
        if (result is Result.LocalAndCloud || result is Result.LocalOnly) {
            prefs.edit().putLong(KEY_LAST_DAILY_TS, now).apply()
        }
        return result
    }

    sealed interface Result {
        data class LocalAndCloud(val file: File, val publicUri: Uri? = null) : Result
        data class LocalOnly(val file: File, val cloudError: Throwable? = null, val publicUri: Uri? = null) : Result
        data object AlreadyRunning : Result
    }
}
