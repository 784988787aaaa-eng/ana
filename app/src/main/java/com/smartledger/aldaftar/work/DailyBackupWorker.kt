package com.smartledger.aldaftar.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.smartledger.aldaftar.data.backup.AutomaticBackupCoordinator

/** تنفيذ النسخة الاحتياطية اليومية دون إعداد ظاهر للمستخدم. */
class DailyBackupWorker(
    appContext: Context,
    params: WorkerParameters,
    private val coordinator: AutomaticBackupCoordinator
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result = when (coordinator.runDaily()) {
        is AutomaticBackupCoordinator.Result.LocalAndCloud,
        is AutomaticBackupCoordinator.Result.LocalOnly,
        AutomaticBackupCoordinator.Result.AlreadyRunning -> Result.success()
    }
}
