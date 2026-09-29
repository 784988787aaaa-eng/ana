package com.smartledger.aldaftar

import android.content.Context
import android.util.Log
import androidx.work.*
import com.smartledger.aldaftar.data.repository.SettingsRepository
import com.smartledger.aldaftar.data.repository.TrashRepository
import kotlinx.coroutines.CancellationException
import java.util.concurrent.TimeUnit

class TrashCleanupWorker(
    context: Context,
    params: WorkerParameters,
    private val settingsRepository: SettingsRepository,
    private val trashRepository: TrashRepository
) : CoroutineWorker(context, params) {
    companion object {
        private const val TAG = "TrashCleanupWorker"
        const val WORK_NAME = "MizanTrashCleanup"
        private const val ONE_DAY_MS = 24 * 60 * 60 * 1000L
        fun getPeriodDurationMillis(period: String): Long = when (period.lowercase()) {
            "week" -> 7 * ONE_DAY_MS; "month" -> 30 * ONE_DAY_MS; "3months" -> 90L * ONE_DAY_MS
            "6months" -> 180L * ONE_DAY_MS; "year" -> 365L * ONE_DAY_MS; else -> 0L
        }
        fun schedulePeriodicCleanup(context: Context, period: String) {
            val wm = WorkManager.getInstance(context)
            if (period.equals("never", true)) { wm.cancelUniqueWork(WORK_NAME); return }
            wm.enqueueUniquePeriodicWork(WORK_NAME, ExistingPeriodicWorkPolicy.KEEP,
                PeriodicWorkRequestBuilder<TrashCleanupWorker>(1, TimeUnit.DAYS).setConstraints(Constraints.Builder().setRequiresBatteryNotLow(true).build()).build())
        }
    }
    override suspend fun doWork(): Result {
        return try {
            val period = settingsRepository.getSettingsDirect()?.trashAutoCleanupPeriod ?: "NEVER"
            if (!period.equals("never", true)) {
                val duration = getPeriodDurationMillis(period)
                if (duration <= 0L) return Result.failure()
                trashRepository.removeExpiredBefore(System.currentTimeMillis() - duration)
            }
            cleanupTemporaryCacheFiles(applicationContext)
            Result.success()
        } catch (e: CancellationException) {
            throw e
        } catch (e: java.io.IOException) {
            Log.w(TAG, "تعذر الوصول المؤقت إلى التخزين", e); Result.retry()
        } catch (e: Exception) {
            Log.e(TAG, "فشل تنظيف سلة المهملات", e); Result.failure()
        }
    }
    private fun cleanupTemporaryCacheFiles(context: Context) {
        val threshold = System.currentTimeMillis() - ONE_DAY_MS
        context.cacheDir?.walkTopDown()?.forEach { file ->
            if (file.isFile && file.lastModified() < threshold && (file.name.endsWith(".pdf") || file.name.endsWith(".xlsx") || file.name.endsWith(".csv"))) runCatching { file.delete() }
        }
    }
}
