package com.smartledger.aldaftar.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.smartledger.aldaftar.data.backup.BackupEngine
import com.smartledger.aldaftar.data.backup.BackupScheduler
import com.smartledger.aldaftar.data.backup.BackupSettingsRepository
import com.smartledger.aldaftar.data.cloud.CloudArchiveStore
import com.smartledger.aldaftar.platform.notifications.BackupNotificationManager
import kotlinx.coroutines.flow.first

class DailyBackupWorker(context: Context, params: WorkerParameters, private val engine: BackupEngine) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val settings = BackupSettingsRepository(applicationContext)
        if (!settings.enabled.first()) return Result.success()
        return try {
            val file = engine.createAutomatic()
            val cloud = CloudArchiveStore(applicationContext)
            if (cloud.connected()) cloud.upload(file.readBytes(), file.name)
            BackupNotificationManager(applicationContext).show("النسخ الاحتياطي", "تم حفظ النسخة اليومية بنجاح")
            Result.success()
        } catch (_: java.io.IOException) {
            Result.retry()
        } catch (_: Exception) {
            Result.retry()
        } finally {
            if (settings.enabled.first()) BackupScheduler(applicationContext).scheduleNext()
        }
    }
}
