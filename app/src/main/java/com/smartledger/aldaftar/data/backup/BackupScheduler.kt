package com.smartledger.aldaftar.data.backup

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.time.Duration
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit
import com.smartledger.aldaftar.work.DailyBackupWorker

/**
 * جدولة النسخة اليومية في نهاية اليوم بحسب توقيت الجهاز.
 * WorkManager يبقي المهمة مسجلة عبر إعادة تشغيل الهاتف، مع السماح للنظام بالتأخير
 * القسري عند Doze/قيود الشركة المصنّعة.
 */
class BackupScheduler(private val context: Context) {
    companion object {
        const val WORK_NAME = "smartledger_daily_backup"
        private const val BACKUP_HOUR = 23L
        private const val BACKUP_MINUTE = 55L
    }

    fun scheduleDaily() {
        val now = ZonedDateTime.now(ZoneId.systemDefault())
        var next = now.toLocalDate().atTime(BACKUP_HOUR.toInt(), BACKUP_MINUTE.toInt()).atZone(now.zone)
        if (!next.isAfter(now)) next = next.plusDays(1)

        val delay = Duration.between(now, next).toMillis().coerceAtLeast(1_000L)
        val request = PeriodicWorkRequestBuilder<DailyBackupWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setConstraints(Constraints.Builder().build())
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }

    /** توافق خلفي مع أي استدعاء قديم في المشروع. */
    fun scheduleNext() = scheduleDaily()

    fun cancel() {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
    }
}
