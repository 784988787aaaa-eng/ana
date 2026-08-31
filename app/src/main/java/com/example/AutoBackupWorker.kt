/**
 * =====================================================================
 * ملف: العامل الخلفي للنسخ الاحتياطي التلقائي (AutoBackupWorker.kt)
 * =====================================================================
 * 
 * [الغرض العام والتعليمي من الملف]:
 * يمثل هذا الملف عصب منظومة النسخ الاحتياطي الذاتي اليومي في الخلفية. يعتمد على
 * مكتبة Android WorkManager لجدولة وتنفيذ عمليات النسخ الاحتياطي لقاعدة بيانات التطبيق
 * في وقت محدد (نهاية اليوم 11:59 مساءً) دون الحاجة لفتح المستخدم للتطبيق.
 * 
 * [آلية وسير تدفق العمليات (Workflow Flow)]:
 * 1. استيقاظ الـ Worker في الوقت المحدد وفق قيود النظام (مثل توفر شحن كافٍ في البطارية).
 * 2. التحقق من تمكين ميزة النسخ التلقائي في إعدادات التطبيق.
 * 3. إنشاء نسخة احتياطية محلية بصيغة مشفرة وآمنة والتحقق من سلامتها عبر فحص الشيكسم (Checksum).
 * 4. إذا كان المستخدم قد ربط حسابه بـ Google Drive وتوفرت شبكة الإنترنت، يتم رفع النسخة تلقائياً.
 * 5. في حال عدم توفر شبكة، يتم تسجيل طلب مزامنة معلق لتقوم مهمة لاحقة برفعه فور عودة الاتصال.
 * 6. إرسال إشعارات واضحة للمستخدم بحالة النسخ (جاري التنفيذ، نجاح، أو فشل).
 */
package com.example

// ---------------------------------------------------------------------
// استيراد المكتبات وحزم أندرويد الأساسية
// تشمل: نظام الإشعارات، مكتبة WorkManager للمهام الخلفية، وأدوات الشبكة والتوقيت
// ---------------------------------------------------------------------
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.data.GoogleDriveSyncHelper
import com.example.data.backup.BackupConstants
import com.example.data.backup.BackupFileManager
import com.example.data.backup.BackupOperationResult
import com.example.data.backup.BackupService
import com.example.data.local.AppDatabase
import com.example.data.local.entities.AppSettings
import com.example.data.serialization.BackupIntegrityManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * [فئة العامل الخلفي - AutoBackupWorker]:
 * ترث من `CoroutineWorker` مما يتيح تنفيذ العمليات الطويلة (مثل قراءة قاعدة البيانات،
 * وضغط الملفات، والرفع للإنترنت) بشكل غير متزامن تماماً على مسار خلفي (IO Thread)
 * دون التسبب في أي تجميد لواجهة المستخدم.
 */
class AutoBackupWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    /**
     * [الكائن المرافق - Companion Object]:
     * يحتوي على الثوابت المشتركة ودوال الجدولة والإلغاء والفحص الاستدراكي للنسخ الفائتة.
     * يمكن استدعاء هذه الدوال من أي مكان في التطبيق دون الحاجة لإنشاء كائن جديد.
     */
    companion object {
        private const val TAG = "AutoBackupWorker"
        const val WORK_NAME = "MizanDailyBackup"
        const val PREFS_NAME = BackupConstants.PREFS_BACKUP
        const val CHANNEL_ID = "mizan_backup_channel"
        private const val NOTIFICATION_PROGRESS_ID = 1001
        private const val NOTIFICATION_RESULT_ID = 1002

        /**
         * [دالة جدولة النسخ الاحتياطي اليومي]:
         * تقوم بحساب الوقت المتبقي حتى الساعة 11:59 مساءً من اليوم الحالي (أو اليوم التالي
         * إذا كان الوقت قد مضى)، ثم تنشئ طلباً دورياً يتكرر كل 24 ساعة عبر WorkManager
         * مع اشتراط عدم انخفاض مستوى البطارية لضمان استقرار النظام.
         */
        fun scheduleDailyBackupWorker(context: Context) {
            val workManager = WorkManager.getInstance(context)

            // حساب التوقيت المستهدف: 11:59:00 مساءً
            val currentDate = Calendar.getInstance()
            val dueDate = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            // إذا كان الوقت الحالي بعد 11:59 مساءً، نجدول لليوم التالي
            if (dueDate.before(currentDate)) {
                dueDate.add(Calendar.DAY_OF_YEAR, 1)
            }
            val initialDelay = (dueDate.timeInMillis - currentDate.timeInMillis).coerceAtLeast(0L)

            // ضبط قيود التشغيل: الحفاظ على طاقة الجهاز
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                .setRequiresBatteryNotLow(true)
                .build()

            // بناء طلب العمل الدوري مع استراتيجية التراجع التدريجي عند حدوث خطأ
            val dailyWorkRequest = PeriodicWorkRequestBuilder<AutoBackupWorker>(
                1, TimeUnit.DAYS
            )
                .setConstraints(constraints)
                .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    30,
                    TimeUnit.MINUTES
                )
                .build()

            // تسجيل المهمة في نظام أندرويد مع الحفاظ على أي مهمة قائمة سابقاً
            workManager.enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                dailyWorkRequest
            )
            Log.d(TAG, "تمت جدولة النسخ الاحتياطي اليومي بنجاح بعد ${initialDelay / (1000 * 60)} دقيقة")
        }

        /**
         * [دالة إلغاء الجدولة اليومية]:
         * تستخدم عند قيام المستخدم بتعطيل ميزة النسخ الاحتياطي التلقائي من شاشة الإعدادات.
         */
        fun cancelDailyBackupWorker(context: Context) {
            val workManager = WorkManager.getInstance(context)
            workManager.cancelUniqueWork(WORK_NAME)
            Log.d(TAG, "تم إلغاء مهمة النسخ الاحتياطي اليومي.")
        }

        /**
         * [دالة التحقق الذاتي واستدراك النسخ الفائتة]:
         * تُستدعى عند فتح التطبيق. تتحقق مما إذا كان الجهاز مغلقاً وقت الجدولة الليلية السابقة.
         * إذا وُجد أن موعد النسخ السابق قد فات دون إنشاء نسخة، تقوم فوراً بتشغيل نسخة تعويضية لمرة واحدة.
         */
        suspend fun checkAndTriggerBackupIfMissed(context: Context) = withContext(Dispatchers.IO) {
            try {
                val db = AppDatabase.getDatabase(context)
                val settings = db.settingsDao().getSettingsDirect() ?: AppSettings()
                if (!settings.isAutoBackupEnabled) {
                    Log.d(TAG, "النسخ التلقائي معطل في إعدادات المستخدم.")
                    return@withContext
                }

                val sharedPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

                // إذا كان هذا هو التشغيل الأول للتطبيق بعد التثبيت، نهيئ التوقيت لتفادي عمل نسخة فورية غير ضرورية
                val isFirstLaunch = sharedPrefs.getBoolean("is_first_backup_initialized", true)
                if (isFirstLaunch) {
                    sharedPrefs.edit()
                        .putLong("last_successful_auto_backup_timestamp", System.currentTimeMillis())
                        .putLong(BackupConstants.KEY_LAST_SUCCESSFUL_BACKUP, System.currentTimeMillis())
                        .putBoolean("is_first_backup_initialized", false)
                        .apply()
                    Log.d(TAG, "تمت تهيئة مؤشر النسخ الأول عند التثبيت.")
                    return@withContext
                }

                // حساب توقيت آخر موعد نسخ مفترض
                val now = Calendar.getInstance()
                val lastDueBackup = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 23)
                    set(Calendar.MINUTE, 59)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                if (now.before(lastDueBackup)) {
                    lastDueBackup.add(Calendar.DAY_OF_YEAR, -1)
                }

                val lastBackupTimestamp = sharedPrefs.getLong(BackupConstants.KEY_LAST_SUCCESSFUL_BACKUP, 0L)
                val isPendingCloud = sharedPrefs.getBoolean(BackupConstants.KEY_PENDING_CLOUD_UPLOAD, false)

                val syncHelper = GoogleDriveSyncHelper(context)
                val isCloudLinked = !syncHelper.getStoredRefreshToken().isNullOrEmpty()

                // المقارنة: هل آخر نسخة تم إنشاؤها أقدم من آخر موعد استحقاق؟
                if (lastBackupTimestamp < lastDueBackup.timeInMillis) {
                    Log.d(TAG, "فات موعد النسخ اليومي السابق، جاري تشغيل نسخة تعويضية فورية.")
                    val immediateWorkRequest = OneTimeWorkRequestBuilder<AutoBackupWorker>()
                        .setBackoffCriteria(
                            BackoffPolicy.EXPONENTIAL,
                            10,
                            TimeUnit.MINUTES
                        )
                        .build()
                    WorkManager.getInstance(context).enqueue(immediateWorkRequest)

                    if (isCloudLinked) {
                        CloudUploadWorker.enqueueUploadLatest(context)
                    }
                } else if (isCloudLinked && isPendingCloud) {
                    Log.d(TAG, "النسخة المحلية محدثة، لكن هناك رفع سحابي معلق.")
                    CloudUploadWorker.enqueueUploadLatest(context)
                }
            } catch (e: Exception) {
                Log.e(TAG, "خطأ أثناء فحص النسخ الاحتياطي الفائت", e)
            }
        }
    }

    /**
     * [الدالة التنفيذية المركزية - doWork]:
     * يتم تشغيل هذه الدالة بواسطة نظام أندرويد في الخلفية.
     * تقوم بتنفيذ الخطوات المتسلسلة لإنشاء الملف وفحصه وحفظه ومزامنته.
     */
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val context = applicationContext
        try {
            val db = AppDatabase.getDatabase(context)
            val settings = db.settingsDao().getSettingsDirect() ?: AppSettings()

            // 1. التحقق من تمكين الميزة من قبل المستخدم
            if (!settings.isAutoBackupEnabled) {
                Log.d(TAG, "ميزة النسخ الاحتياطي التلقائي معطلة في الإعدادات.")
                return@withContext Result.success()
            }

            // 2. إرسال إشعار قيد التنفيذ لإعلام المستخدم في شريط الإشعارات
            sendBackupInProgressNotification(context)

            // 3. تنسيق إنشاء وحفظ النسخة الاحتياطية محلياً عبر BackupService و BackupFileManager
            val fileManager = BackupFileManager(context)
            val backupService = BackupService(context, db, fileManager)

            val sdfName = SimpleDateFormat(BackupConstants.BACKUP_DATE_FORMAT, Locale.US)
            val dateStr = sdfName.format(Date())
            val fileName = "${BackupConstants.BACKUP_CLOUD_FILE_PREFIX}$dateStr${BackupConstants.BACKUP_FILE_EXTENSION}"

            val backupResult = backupService.performLocalBackup(
                customFileName = fileName,
                targetDir = fileManager.getMonthlyBackupDirectory()
            )

            when (backupResult) {
                is BackupOperationResult.Success -> {
                    val activeFile = backupResult.file

                    // 4. التحقق الاستباقي من صحة وسلامة الملف المكتوب واكتمال بنيته
                    val integrity = BackupIntegrityManager.validateBackupFileIntegrity(activeFile)
                    if (integrity !is BackupIntegrityManager.IntegrityCheckResult.Valid) {
                        Log.e(TAG, "فشل فحص سلامة ملف النسخة بعد إنشائه")
                        sendBackupFailureNotification(context, false)
                        return@withContext Result.retry()
                    }

                    // 5. تحديث التفضيلات بتوقيت آخر نسخة ناجحة
                    val sharedPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                    sharedPrefs.edit()
                        .putLong("last_successful_auto_backup_timestamp", backupResult.timestamp)
                        .putLong(BackupConstants.KEY_LAST_SUCCESSFUL_BACKUP, backupResult.timestamp)
                        .apply()

                    // 6. المزامنة السحابية إذا كان الحساب مربوطاً بحساب Google Drive
                    val syncHelper = GoogleDriveSyncHelper(context)
                    val isCloudLinked = !syncHelper.getStoredRefreshToken().isNullOrEmpty()
                    var cloudSynced = false

                    if (isCloudLinked) {
                        if (isNetworkConnected(context)) {
                            val jsonContent = activeFile.readText(Charsets.UTF_8)
                            cloudSynced = syncHelper.uploadBackupToDriveWithFilename(fileName, jsonContent)
                        }

                        // إذا تعذر الرفع لعدم توفر شبكة، نجدول مهمة رفع منفصلة لاحقاً
                        if (!cloudSynced) {
                            Log.w(TAG, "تعذر الرفع السحابي الفوري، إدراج CloudUploadWorker في الخلفية.")
                            sharedPrefs.edit().putBoolean(BackupConstants.KEY_PENDING_CLOUD_UPLOAD, true).apply()
                            CloudUploadWorker.enqueueUpload(context, activeFile.absolutePath, fileName)
                        } else {
                            sharedPrefs.edit().putBoolean(BackupConstants.KEY_PENDING_CLOUD_UPLOAD, false).apply()
                        }
                    }

                    // 7. إشعار النجاح والاهتزاز لتأكيد اكتمال العملية للمستخدم
                    com.example.ui.helper.VibrationHelper.triggerSuccessVibration(context)
                    sendBackupSuccessNotification(context, activeFile.name, cloudSynced)
                    Result.success()
                }
                is BackupOperationResult.Failure -> {
                    Log.e(TAG, "فشلت عملية إنشاء النسخة الاحتياطية المحلية: ${backupResult.userMessage}", backupResult.cause)
                    sendBackupFailureNotification(context, false)
                    if (backupResult.cause is IOException) {
                        Result.retry()
                    } else {
                        Result.failure()
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "استثناء غير متوقع أثناء دورة النسخ الاحتياطي التلقائي", e)
            sendBackupFailureNotification(context, false)
            if (e is IOException) Result.retry() else Result.failure()
        }
    }

    /**
     * [دالة فحص الاتصال بالإنترنت]:
     * تتحقق مما إذا كان الجهاز متصلاً حالياً بشبكة إنترنت نشطة وصالحة لنقل البيانات.
     */
    private fun isNetworkConnected(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = cm.activeNetwork ?: return false
            val capabilities = cm.getNetworkCapabilities(network) ?: return false
            return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) ||
                    capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        } else {
            @Suppress("DEPRECATION")
            val networkInfo = cm.activeNetworkInfo ?: return false
            @Suppress("DEPRECATION")
            return networkInfo.isConnected
        }
    }

    /**
     * [دالة إرسال إشعار بدء العمل]:
     * تظهر إشعاراً ثابتاً غير قابل للإلغاء يدوياً يفيد بأن النسخ جاري حالياً.
     */
    private fun sendBackupInProgressNotification(context: Context) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return

        // إنشاء قناة الإشعارات لنظام أندرويد 8.0 فأعلى (Android Oreo+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.autobackup_channel_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = context.getString(R.string.autobackup_channel_desc)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val title = context.getString(R.string.autobackup_notification_title_inprogress)
        val text = context.getString(R.string.autobackup_notification_text_inprogress)

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_upload)
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()

        notificationManager.notify(NOTIFICATION_PROGRESS_ID, notification)
    }

    /**
     * [دالة إرسال إشعار النجاح]:
     * تظهر إشعاراً قابلاً للنقر يفتح التطبيق مباشرة، مع نمط اهتزاز احتفالي بنجاح العملية.
     */
    private fun sendBackupSuccessNotification(context: Context, fileName: String, cloudSynced: Boolean) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.autobackup_channel_name),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = context.getString(R.string.autobackup_channel_desc)
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 100, 50, 150)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = context.getString(R.string.autobackup_notification_title_success)
        val text = context.getString(R.string.autobackup_notification_text_success)

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_upload_done)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setOngoing(false)
            .setAutoCancel(true)
            .build()

        // إلغاء إشعار التقدم الثابت أولاً لضمان عدم بقائه في شريط الإشعارات
        notificationManager.cancel(NOTIFICATION_PROGRESS_ID)
        notificationManager.notify(NOTIFICATION_RESULT_ID, notification)
    }

    /**
     * [دالة إرسال إشعار الفشل أو التنبيه]:
     * تنبه المستخدم في حال تعذر إكمال النسخ (بسبب نقص الصلاحيات أو أخطاء التخزين) ليتخذ إجراءً يدوياً.
     */
    private fun sendBackupFailureNotification(context: Context, isPermissionIssue: Boolean) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.autobackup_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = context.getString(R.string.autobackup_channel_desc)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = context.getString(R.string.autobackup_notification_title_failure)
        val text = if (isPermissionIssue) {
            context.getString(R.string.autobackup_notification_text_permission)
        } else {
            context.getString(R.string.autobackup_notification_text_failure)
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_warning)
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setOngoing(false)
            .setAutoCancel(true)
            .build()

        // إلغاء إشعار التقدم الثابت لضمان تنظيف شريط الإشعارات
        notificationManager.cancel(NOTIFICATION_PROGRESS_ID)
        notificationManager.notify(NOTIFICATION_RESULT_ID, notification)
    }
}
