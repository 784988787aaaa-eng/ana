package com.smartledger.aldaftar.data.backup

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.time.Duration
import java.time.ZonedDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit
import com.smartledger.aldaftar.work.DailyBackupWorker

class BackupScheduler(private val context: Context) {
    companion object { private const val WORK_NAME = "smartledger_daily_backup" }
    fun scheduleNext() {
        val now = ZonedDateTime.now(ZoneId.systemDefault())
        val next = now.toLocalDate().plusDays(1).atStartOfDay(now.zone).plusMinutes(2)
        val delay = Duration.between(now, next).toMillis().coerceAtLeast(1_000L)
        val request = OneTimeWorkRequestBuilder<DailyBackupWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setConstraints(Constraints.Builder().setRequiresBatteryNotLow(true).build())
            .build()
        WorkManager.getInstance(context).enqueueUniqueWork(WORK_NAME, ExistingWorkPolicy.REPLACE, request)
    }
    fun cancel() { WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME) }
}
