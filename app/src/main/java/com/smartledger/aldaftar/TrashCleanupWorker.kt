/**
 * عامل خلفي لتنظيف السجلات المنتهية وملفات التصدير المؤقتة بأمان.
 * يحافظ على ملفات النسخ الاحتياطي ويعمل خارج خيط الواجهة.
 */
package com.smartledger.aldaftar

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.smartledger.aldaftar.data.backup.BackupConstants
import com.smartledger.aldaftar.data.local.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.TimeUnit

/** عامل صيانة دوري لسلة المهملات والملفات المؤقتة. */
class TrashCleanupWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    companion object {
        private const val TAG = "TrashCleanupWorker"
        const val WORK_NAME = "MizanTrashCleanup"
        private const val PREFS_TRASH = "trash_prefs"
        private const val KEY_AUTO_CLEANUP_PERIOD = "trash_auto_cleanup_period"
        private const val ONE_DAY_MS = 24L * 60L * 60L * 1000L

        /** يحول فترة الاحتفاظ المعروفة إلى مدة زمنية بالميلي ثانية. */
        fun getPeriodDurationMillis(period: String): Long = when (period) {
            "week" -> 7L * ONE_DAY_MS
            "month" -> 30L * ONE_DAY_MS
            "3months" -> 90L * ONE_DAY_MS
            "6months" -> 180L * ONE_DAY_MS
            "year" -> 365L * ONE_DAY_MS
            else -> 0L
        }

        /** يجدول مهمة صيانة يومية أو يلغيها عند تعطيل التنظيف التلقائي. */
        fun schedulePeriodicCleanup(context: Context, period: String) {
            val workManager = WorkManager.getInstance(context.applicationContext)
            if (period == "never") {
                workManager.cancelUniqueWork(WORK_NAME)
                Log.d(TAG, "تم إيقاف التنظيف التلقائي لسلة المهملات")
                return
            }

            if (getPeriodDurationMillis(period) <= 0L) {
                Log.w(TAG, "تم تجاهل فترة تنظيف غير معروفة")
                return
            }

            val constraints = Constraints.Builder()
                .setRequiresBatteryNotLow(true)
                .build()

            val periodicRequest = PeriodicWorkRequestBuilder<TrashCleanupWorker>(1, TimeUnit.DAYS)
                .setConstraints(constraints)
                .build()

            workManager.enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                periodicRequest
            )
            Log.d(TAG, "تمت جدولة التنظيف الدوري لسلة المهملات")
        }
    }

    /** ينفذ الصيانة كاملة على خيط إدخال وإخراج مستقل. */
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            cleanupDatabaseTrashItems(applicationContext)
            cleanupTemporaryCacheFiles(applicationContext)
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "فشل عامل التنظيف الدوري: ${e.javaClass.simpleName}")
            Result.retry()
        }
    }

    /** يحذف السجلات التي تجاوزت فترة الاحتفاظ مع استمرار العملية عند فشل سجل منفرد. */
    private suspend fun cleanupDatabaseTrashItems(context: Context) {
        val sharedPrefs = context.getSharedPreferences(PREFS_TRASH, Context.MODE_PRIVATE)
        val period = sharedPrefs.getString(KEY_AUTO_CLEANUP_PERIOD, "never") ?: "never"
        if (period == "never") return

        val durationMs = getPeriodDurationMillis(period)
        if (durationMs <= 0L) return

        try {
            val thresholdTime = System.currentTimeMillis() - durationMs
            val trashDao = AppDatabase.getDatabase(context).trashDao()
            val expiredItems = trashDao.getAllDeletedItemsDirect().filter { it.deletedAt < thresholdTime }

            for (item in expiredItems) {
                try {
                    trashDao.deleteItem(item)
                } catch (itemEx: Exception) {
                    Log.w(TAG, "تعذر حذف عنصر من سلة المهملات: ${itemEx.javaClass.simpleName}")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "فشل تنظيف سلة المهملات: ${e.javaClass.simpleName}")
        }
    }

    /** يحذف ملفات التصدير المؤقتة القديمة فقط مع حماية ملفات النسخ الاحتياطي. */
    private fun cleanupTemporaryCacheFiles(context: Context) {
        val cacheDir = context.cacheDir ?: return
        val thresholdTime = System.currentTimeMillis() - ONE_DAY_MS

        try {
            cacheDir.walkTopDown().forEach { file ->
                try {
                    if (isProtectedFile(file)) return@forEach
                    if (!file.isFile || file.lastModified() >= thresholdTime) return@forEach

                    val isTempOrExport = file.name.startsWith(BackupConstants.BACKUP_TEMP_PREFIX) ||
                        file.name.endsWith(BackupConstants.BACKUP_TEMP_SUFFIX) ||
                        file.name.endsWith(".pdf", ignoreCase = true) ||
                        file.name.endsWith(".xlsx", ignoreCase = true) ||
                        file.name.endsWith(".csv", ignoreCase = true)

                    if (isTempOrExport && !file.delete()) {
                        Log.w(TAG, "تعذر حذف ملف مؤقت قديم")
                    }
                } catch (fileEx: Exception) {
                    Log.w(TAG, "تعذر معالجة ملف مؤقت: ${fileEx.javaClass.simpleName}")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "فشل تنظيف ملفات التخزين المؤقت: ${e.javaClass.simpleName}")
        }
    }

    /** يمنع حذف ملفات النسخ الاحتياطي والمجلدات التي قد تحتوي عليها. */
    private fun isProtectedFile(file: File): Boolean {
        val name = file.name
        return file.isDirectory ||
            name.endsWith(BackupConstants.BACKUP_FILE_EXTENSION, ignoreCase = true) ||
            name.startsWith(BackupConstants.BACKUP_FILE_PREFIX, ignoreCase = true) ||
            name.startsWith(BackupConstants.BACKUP_CLOUD_FILE_PREFIX, ignoreCase = true)
    }
}
