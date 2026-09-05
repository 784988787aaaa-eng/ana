/**
 * =====================================================================
 * ملف: العامل الخلفي لتنظيف سلة المهملات والملفات المؤقتة (TrashCleanupWorker.kt)
 * =====================================================================
 * 
 * [الغرض العام والتعليمي من الملف]:
 * يتولى هذا العامل الخلفي مهمة الصيانة الذاتية الدورية لنظام الملفات وقاعدة البيانات.
 * يقوم بمهمتين أساسيتين:
 * 1. الحذف النهائي للعناصر الموجودة في سلة المهملات التي تجاوزت مدة الاحتفاظ المحددة
 *    من قبل المستخدم (مثل: أسبوع، شهر، 3 أشهر، سنة).
 * 2. تنظيف ملفات التصدير والمشاركة المؤقتة القديمة (.pdf, .xlsx, .csv) من مجلد الـ Cache.
 * 
 * [قواعد الأمان المعماري والحماية المطلقة]:
 * - الحماية الصارمة لملفات النسخ الاحتياطي: يُحظر نهائياً حذف أي ملف يحمل امتداد .mzd أو بادئة النسخ.
 * - عزل الأخطاء: فشل حذف سجل أو ملف لا يوقف العملية عن متابعة تنظيف باقي العناصر.
 * - احترام الموارد: يتم تشغيل الجدولة فقط عند عدم انخفاض شحن البطارية.
 */
package com.smartledger.aldaftar

// ---------------------------------------------------------------------
// استيراد حزم إدارة المهام الخلفية WorkManager وقاعدة بيانات الغرفة Room
// ---------------------------------------------------------------------
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

/**
 * [فئة عامل التنظيف - TrashCleanupWorker]:
 * فئة غير متزامنة (CoroutineWorker) تنفذ عمليات فحص وحذف السجلات والملفات في مسار IO.
 */
class TrashCleanupWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    /**
     * [الكائن المرافق - Companion Object]:
     * يحتوي على ثوابت سلة المهملات ودوال تحويل الفترات الزمنية وجدولة التنظيف الدوري.
     */
    companion object {
        private const val TAG = "TrashCleanupWorker"
        const val WORK_NAME = "MizanTrashCleanup"
        private const val PREFS_TRASH = "trash_prefs"
        private const val KEY_AUTO_CLEANUP_PERIOD = "trash_auto_cleanup_period"
        private const val ONE_DAY_MS = 24 * 60 * 60 * 1000L

        /**
         * [دالة حساب المدة الزمنية]:
         * تحول الخيار النصي (week, month, 3months, etc.) إلى قيمة ميلي ثانية مكافئة.
         */
        fun getPeriodDurationMillis(period: String): Long {
            return when (period) {
                "week" -> 7 * ONE_DAY_MS
                "month" -> 30 * ONE_DAY_MS
                "3months" -> 90L * ONE_DAY_MS
                "6months" -> 180L * ONE_DAY_MS
                "year" -> 365L * ONE_DAY_MS
                else -> 0L
            }
        }

        /**
         * [دالة جدولة التنظيف الدوري]:
         * تضبط مهمة يومية عبر WorkManager لفحص سلة المهملات، أو تلغي المهمة إذا اختار المستخدم "never".
         */
        fun schedulePeriodicCleanup(context: Context, period: String) {
            val workManager = WorkManager.getInstance(context)
            if (period == "never") {
                workManager.cancelUniqueWork(WORK_NAME)
                Log.d(TAG, "تم إيقاف الجدولة التلقائية لتنظيف سلة المهملات (never).")
                return
            }

            // اشتراط عدم انخفاض البطارية
            val constraints = Constraints.Builder()
                .setRequiresBatteryNotLow(true)
                .build()

            val periodicRequest = PeriodicWorkRequestBuilder<TrashCleanupWorker>(
                1, TimeUnit.DAYS
            )
                .setConstraints(constraints)
                .build()

            workManager.enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                periodicRequest
            )
            Log.d(TAG, "تمت جدولة التنظيف الدوري اليومي لسلة المهملات بنجاح للفترة: $period")
        }
    }

    /**
     * [الدالة التنفيذية - doWork]:
     * تنسق عملية تنظيف قاعدة البيانات وملفات الكاش المؤقتة.
     */
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val context = applicationContext
        Log.d(TAG, "بدء عملية التنظيف الدوري في الخلفية...")

        try {
            // 1. تنظيف عناصر سلة المهملات منتهية الصلاحية في قاعدة البيانات
            cleanupDatabaseTrashItems(context)

            // 2. تنظيف الملفات المؤقتة القديمة في ذاكرة التخزين المؤقت (Cache) بأمان
            cleanupTemporaryCacheFiles(context)

            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "خطأ أثناء تنفيذ عامل التنظيف الدوري", e)
            Result.retry()
        }
    }

    /**
     * [دالة تنظيف سجلات سلة المهملات من قاعدة البيانات]:
     * تستعلم عن السجلات المحذوفة التي مضى على حذفها وقت أطول من الفترة المحددة، ثم تحذفها نهائياً.
     */
    private suspend fun cleanupDatabaseTrashItems(context: Context) {
        try {
            val sharedPrefs = context.getSharedPreferences(PREFS_TRASH, Context.MODE_PRIVATE)
            val period = sharedPrefs.getString(KEY_AUTO_CLEANUP_PERIOD, "never") ?: "never"
            if (period == "never") {
                Log.d(TAG, "تنظيف سلة المهملات معطل في التفضيلات.")
                return
            }

            val durationMs = getPeriodDurationMillis(period)
            if (durationMs <= 0L) return

            val thresholdTime = System.currentTimeMillis() - durationMs
            val db = AppDatabase.getDatabase(context)
            val trashDao = db.trashDao()

            val allItems = trashDao.getAllDeletedItemsDirect()
            val expiredItems = allItems.filter { it.deletedAt < thresholdTime }

            Log.d(TAG, "عثر على ${expiredItems.size} عنصر منتهي الصلاحية للحذف من أصل ${allItems.size}")

            for (item in expiredItems) {
                try {
                    trashDao.deleteItem(item)
                } catch (itemEx: Exception) {
                    Log.e(TAG, "تعذر حذف العنصر ${item.id} من سلة المهملات، الاستمرار في باقي العناصر", itemEx)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "فشل أثناء تنظيف سلة المهملات من قاعدة البيانات", e)
        }
    }

    /**
     * [دالة تنظيف ملفات الكاش المؤقتة]:
     * تفحص مجلد Cache وتحذف الملفات المؤقتة وملفات التقارير المصدرة التي مر عليها أكثر من 24 ساعة
     * مع الاستثناء والحماية الصارمة لأي ملف يخص النسخ الاحتياطي.
     */
    private fun cleanupTemporaryCacheFiles(context: Context) {
        try {
            val cacheDir = context.cacheDir ?: return
            val thresholdTime = System.currentTimeMillis() - ONE_DAY_MS

            cacheDir.walkTopDown().forEach { file ->
                try {
                    // حظر مطلق: منع حذف ملفات النسخ الاحتياطي أو المجلدات الأساسية
                    if (isProtectedFile(file)) {
                        return@forEach
                    }

                    // تنظيف الملفات المؤقتة فقط التي مضى عليها أكثر من 24 ساعة
                    if (file.isFile && file.lastModified() < thresholdTime) {
                        val isTempOrExport = file.name.startsWith(BackupConstants.BACKUP_TEMP_PREFIX) ||
                                file.name.endsWith(BackupConstants.BACKUP_TEMP_SUFFIX) ||
                                file.name.endsWith(".pdf") ||
                                file.name.endsWith(".xlsx") ||
                                file.name.endsWith(".csv")

                        if (isTempOrExport) {
                            file.delete()
                        }
                    }
                } catch (fileEx: Exception) {
                    Log.w(TAG, "تعذر حذف الملف المؤقت: ${file.absolutePath}", fileEx)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "فشل أثناء تنظيف ملفات الكاش المؤقتة", e)
        }
    }

    /**
     * [دالة فحص الملفات المحمية]:
     * تتأكد من أن الملف ليس نسخة احتياطية (.mzd) أو مجلداً رئيسياً لمنع حذفه بالخطأ.
     */
    private fun isProtectedFile(file: File): Boolean {
        val name = file.name
        if (name.endsWith(BackupConstants.BACKUP_FILE_EXTENSION, ignoreCase = true)) return true
        if (name.startsWith(BackupConstants.BACKUP_FILE_PREFIX, ignoreCase = true)) return true
        if (name.startsWith(BackupConstants.BACKUP_CLOUD_FILE_PREFIX, ignoreCase = true)) return true
        if (file.isDirectory) return true
        return false
    }
}
