/**
 * =====================================================================
 * ملف: العامل الخلفي للرفع السحابي المؤجل (CloudUploadWorker.kt)
 * =====================================================================
 * 
 * [الغرض العام والتعليمي من الملف]:
 * يمثل هذا الملف صمام الأمان للمزامنة السحابية غير المتزامنة مع Google Drive.
 * عندما يتعذر رفع النسخة الاحتياطية فور إنشائها محلياً (بسبب غياب الاتصال بالإنترنت
 * أو ضعف الشبكة)، يتولى هذا العامل إدراج المهمة بانتظار عودة الاتصال، ثم رفعها
 * تلقائياً بمجرد توفر الإنترنت دون أي تدخل من المستخدم.
 * 
 * [آلية وسير تدفق العمليات (Workflow Flow)]:
 * 1. إدراج طلب الرفع مع وضع قيد صارم: `NetworkType.CONNECTED` (اشتراط توفر اتصال بالشبكة).
 * 2. عند تشغيل العامل بعد عودة الإنترنت، يتحقق أولاً من ربط المستخدم بحساب Google Drive.
 * 3. تحديد ملف النسخة المستهدف وقراءة محتواه وفحص سلامته عبر `BackupIntegrityManager`.
 * 4. نقل البيانات المشفرة إلى مجلد التطبيق في Google Drive عبر `GoogleDriveSyncHelper`.
 * 5. في حال نجاح الرفع، يتم تحديث توقيت المزامنة وإلغاء حالة التعليق وتنبيه المستخدم بنجاح العملية.
 * 6. في حال حدوث خطأ شبكي مؤقت، يتم تفعيل سياسة التراجع الأسي (Exponential Backoff) لإعادة المحاولة.
 */
package com.example

// ---------------------------------------------------------------------
// استيراد أدوات الإشعارات، مكتبة WorkManager لقيود الشبكة، وأدوات المزامنة السحابية
// ---------------------------------------------------------------------
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.data.GoogleDriveSyncHelper
import com.example.data.backup.BackupConstants
import com.example.data.backup.BackupFileManager
import com.example.data.serialization.BackupIntegrityManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * [فئة العامل الخلفي للرفع السحابي - CloudUploadWorker]:
 * فئة مشتقة من `CoroutineWorker` تضمن تنفيذ عملية النقل السحابي على مسار خلفي مخصص (IO).
 */
class CloudUploadWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    /**
     * [الكائن المرافق - Companion Object]:
     * يوفر الدوال العامة لإدراج طلبات الرفع المؤجلة في قائمة انتظار WorkManager.
     */
    companion object {
        private const val TAG = "CloudUploadWorker"
        // [توثيق المتغير/الخاصية: WORK_NAME]: متغير/خاصية تحمل قيمة تشغيلية ضمن هذا النطاق، ويُحدد معناها من سياق العملية التي تستخدمها.
        const val WORK_NAME = "MizanDelayedCloudUpload"
        // [توثيق المتغير/الخاصية: KEY_FILE_PATH]: متغير/خاصية تحمل قيمة تشغيلية ضمن هذا النطاق، ويُحدد معناها من سياق العملية التي تستخدمها.
        const val KEY_FILE_PATH = "backup_file_path"
        // [توثيق المتغير/الخاصية: KEY_FILE_NAME]: متغير/خاصية تحمل قيمة تشغيلية ضمن هذا النطاق، ويُحدد معناها من سياق العملية التي تستخدمها.
        const val KEY_FILE_NAME = "backup_file_name"
        private const val NOTIFICATION_ID = 1004

        /**
         * [دالة إدراج طلب رفع لملف محدد]:
         * تحفظ معلومات الملف المعلق وتنشئ طلباً لمرة واحدة (OneTimeWorkRequest) مقترناً بشرط الاتصال بالشبكة.
         */
        fun enqueueUpload(context: Context, filePath: String, fileName: String) {
            // [توثيق المتغير/الخاصية: sharedPrefs]: واجهة تخزين التفضيلات المحلية لحالات النسخ والمزامنة.
            val sharedPrefs = context.getSharedPreferences(BackupConstants.PREFS_BACKUP, Context.MODE_PRIVATE)
            sharedPrefs.edit()
                .putBoolean(BackupConstants.KEY_PENDING_CLOUD_UPLOAD, true)
                .putString("pending_cloud_file_path", filePath)
                .putString("pending_cloud_file_name", fileName)
                .apply()

            // اشتراط توفر اتصال بالإنترنت لبدء المهمة
            // [توثيق المتغير/الخاصية: constraints]: قيود WorkManager التي تحدد شروط تشغيل المهمة.
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            // تمرير مسار واسم الملف كبيانات دخل للعامل
            // [توثيق المتغير/الخاصية: data]: بيانات الإدخال التي تُمرر إلى Worker.
            val data = Data.Builder()
                .putString(KEY_FILE_PATH, filePath)
                .putString(KEY_FILE_NAME, fileName)
                .build()

            // [توثيق المتغير/الخاصية: uploadWorkRequest]: متغير/خاصية تحمل قيمة تشغيلية ضمن هذا النطاق، ويُحدد معناها من سياق العملية التي تستخدمها.
            val uploadWorkRequest = OneTimeWorkRequestBuilder<CloudUploadWorker>()
                .setConstraints(constraints)
                .setInputData(data)
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    5,
                    TimeUnit.MINUTES
                )
                .build()

            // استبدال أي مهمة معلقة سابقة بالمهمة الأحدث لضمان عدم تكرار الرفع
            WorkManager.getInstance(context).enqueueUniqueWork(
                WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                uploadWorkRequest
            )
            Log.d(TAG, "تم إدراج طلب الرفع السحابي للملف $fileName بانتظار توفر الإنترنت")
        }

        /**
         * [دالة إدراج رفع أحدث نسخة معلقة]:
         * تستخدم عند استدراك النسخ المعلقة تلقائياً عند فتح التطبيق.
         */
        fun enqueueUploadLatest(context: Context) {
            // [توثيق المتغير/الخاصية: sharedPrefs]: واجهة تخزين التفضيلات المحلية لحالات النسخ والمزامنة.
            val sharedPrefs = context.getSharedPreferences(BackupConstants.PREFS_BACKUP, Context.MODE_PRIVATE)
            // [توثيق المتغير/الخاصية: path]: متغير/خاصية تحمل قيمة تشغيلية ضمن هذا النطاق، ويُحدد معناها من سياق العملية التي تستخدمها.
            val path = sharedPrefs.getString("pending_cloud_file_path", null)
            // [توثيق المتغير/الخاصية: name]: متغير/خاصية تحمل قيمة تشغيلية ضمن هذا النطاق، ويُحدد معناها من سياق العملية التي تستخدمها.
            val name = sharedPrefs.getString("pending_cloud_file_name", null)

            // [توثيق المتغير/الخاصية: constraints]: قيود WorkManager التي تحدد شروط تشغيل المهمة.
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            // [توثيق المتغير/الخاصية: dataBuilder]: متغير/خاصية تحمل قيمة تشغيلية ضمن هذا النطاق، ويُحدد معناها من سياق العملية التي تستخدمها.
            val dataBuilder = Data.Builder()
            if (!path.isNullOrEmpty()) dataBuilder.putString(KEY_FILE_PATH, path)
            if (!name.isNullOrEmpty()) dataBuilder.putString(KEY_FILE_NAME, name)

            // [توثيق المتغير/الخاصية: uploadWorkRequest]: متغير/خاصية تحمل قيمة تشغيلية ضمن هذا النطاق، ويُحدد معناها من سياق العملية التي تستخدمها.
            val uploadWorkRequest = OneTimeWorkRequestBuilder<CloudUploadWorker>()
                .setConstraints(constraints)
                .setInputData(dataBuilder.build())
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    5,
                    TimeUnit.MINUTES
                )
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                uploadWorkRequest
            )
            Log.d(TAG, "تم إدراج رفع أحدث نسخة سحابية معلقة بانتظار توفر الإنترنت")
        }
    }

    /**
     * [الدالة التنفيذية المركزية - doWork]:
     * تنفذ خطوات الفحص، وقراءة الملف، والرفع الفعلي لـ Google Drive.
     */
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        // [توثيق المتغير/الخاصية: context]: سياق أندرويد المستخدم للوصول إلى الموارد والخدمات اللازمة.
        val context = applicationContext
        // [توثيق المتغير/الخاصية: sharedPrefs]: واجهة تخزين التفضيلات المحلية لحالات النسخ والمزامنة.
        val sharedPrefs = context.getSharedPreferences(BackupConstants.PREFS_BACKUP, Context.MODE_PRIVATE)

        try {
            // 1. التحقق من أن المستخدم قام بربط حسابه في Google Drive مسبقاً
            // [توثيق المتغير/الخاصية: syncHelper]: مساعد تنسيق المصادقة والمزامنة مع Google Drive.
            val syncHelper = GoogleDriveSyncHelper(context)
            // [توثيق المتغير/الخاصية: isLinked]: متغير/خاصية تحمل قيمة تشغيلية ضمن هذا النطاق، ويُحدد معناها من سياق العملية التي تستخدمها.
            val isLinked = !syncHelper.getStoredRefreshToken().isNullOrEmpty()
            if (!isLinked) {
                Log.d(TAG, "Google Drive غير مربوط. تم تخطي الرفع السحابي وإلغاء التعليق.")
                sharedPrefs.edit().putBoolean(BackupConstants.KEY_PENDING_CLOUD_UPLOAD, false).apply()
                return@withContext Result.success()
            }

            // 2. تحديد وتجهيز الملف المستهدف للرفع من التخزين المحلي
            // [توثيق المتغير/الخاصية: targetFile]: متغير/خاصية تحمل قيمة تشغيلية ضمن هذا النطاق، ويُحدد معناها من سياق العملية التي تستخدمها.
            val targetFile = resolveTargetBackupFile(context)
            if (targetFile == null || !targetFile.exists() || targetFile.length() == 0L) {
                Log.w(TAG, "لم يتم العثور على ملف نسخة احتياطية صالح للرفع السحابي.")
                sharedPrefs.edit().putBoolean(BackupConstants.KEY_PENDING_CLOUD_UPLOAD, false).apply()
                return@withContext Result.failure()
            }

            // 3. التحقق الاستباقي من سلامة وصحة بنية الملف وتشفيره قبل بدء استهلاك الإنترنت
            // [توثيق المتغير/الخاصية: integrityResult]: نتيجة وسيطة أو نهائية للعملية الحالية.
            val integrityResult = BackupIntegrityManager.validateBackupFileIntegrity(targetFile)
            if (integrityResult !is BackupIntegrityManager.IntegrityCheckResult.Valid) {
                Log.e(TAG, "ملف النسخة الاحتياطية تالف وغير صالح للرفع السحابي.")
                sharedPrefs.edit().putBoolean(BackupConstants.KEY_PENDING_CLOUD_UPLOAD, false).apply()
                return@withContext Result.failure()
            }

            // 4. قراءة البيانات المشفرة وإرسالها لخوادم Google Drive عبر REST API
            // [توثيق المتغير/الخاصية: jsonStr]: متغير/خاصية تحمل قيمة تشغيلية ضمن هذا النطاق، ويُحدد معناها من سياق العملية التي تستخدمها.
            val jsonStr = targetFile.readText(Charsets.UTF_8)
            // [توثيق المتغير/الخاصية: uploadName]: متغير/خاصية تحمل قيمة تشغيلية ضمن هذا النطاق، ويُحدد معناها من سياق العملية التي تستخدمها.
            val uploadName = targetFile.name
            // [توثيق المتغير/الخاصية: success]: متغير/خاصية تحمل قيمة تشغيلية ضمن هذا النطاق، ويُحدد معناها من سياق العملية التي تستخدمها.
            val success = syncHelper.uploadBackupToDriveWithFilename(uploadName, jsonStr)

            // 5. معالجة نتيجة الرفع وتحديث سجلات النظام
            if (success) {
                Log.d(TAG, "تم رفع النسخة السحابية بنجاح: $uploadName")
                sharedPrefs.edit()
                    .putBoolean(BackupConstants.KEY_PENDING_CLOUD_UPLOAD, false)
                    .putLong("last_successful_cloud_backup_timestamp", System.currentTimeMillis())
                    .apply()

                // إطلاق اهتزاز التأكيد وإرسال إشعار النجاح للمستخدم
                com.example.ui.helper.VibrationHelper.triggerSuccessVibration(context)
                sendDelayedUploadNotification(context, uploadName)
                Result.success()
            } else {
                Log.w(TAG, "تعذر إتمام الرفع السحابي، ستتم إعادة المحاولة تلقائياً.")
                Result.retry()
            }
        } catch (e: Exception) {
            Log.e(TAG, "استثناء غير متوقع أثناء الرفع السحابي", e)
            Result.retry()
        }
    }

    /**
     * [دالة تحديد الملف المستهدف للرفع]:
     * تبحث عن المسار الممرر في دخل المهمة، أو المسجل في التفضيلات، أو أحدث ملف محلي صالح.
     */
    private fun resolveTargetBackupFile(context: Context): File? {
        // [توثيق المتغير/الخاصية: sharedPrefs]: واجهة تخزين التفضيلات المحلية لحالات النسخ والمزامنة.
        val sharedPrefs = context.getSharedPreferences(BackupConstants.PREFS_BACKUP, Context.MODE_PRIVATE)
        // [توثيق المتغير/الخاصية: pathFromInput]: متغير/خاصية تحمل قيمة تشغيلية ضمن هذا النطاق، ويُحدد معناها من سياق العملية التي تستخدمها.
        val pathFromInput = inputData.getString(KEY_FILE_PATH)
            ?: sharedPrefs.getString("pending_cloud_file_path", null)

        if (!pathFromInput.isNullOrBlank()) {
            // [توثيق المتغير/الخاصية: file]: مرجع ملف النسخة الاحتياطية على التخزين.
            val file = File(pathFromInput)
            if (file.exists() && file.isFile && file.length() > 0L) {
                return file
            }
        }

        // في حال عدم توفر مسار صالح، البحث عن أحدث ملف .mzd متاح محلياً
        // [توثيق المتغير/الخاصية: fileManager]: مدير الملفات المسؤول عن عمليات النسخ على التخزين.
        val fileManager = BackupFileManager(context)
        // [توثيق المتغير/الخاصية: backups]: متغير/خاصية تحمل قيمة تشغيلية ضمن هذا النطاق، ويُحدد معناها من سياق العملية التي تستخدمها.
        val backups = fileManager.getAllBackupFiles()
        return backups.firstOrNull()
    }

    /**
     * [دالة إرسال إشعار نجاح الرفع السحابي المؤجل]:
     * تخطر المستخدم بأن النسخة المعلقة قد تم رفعها وحفظها بنجاح على Google Drive.
     */
    private fun sendDelayedUploadNotification(context: Context, fileName: String) {
        // [توثيق المتغير/الخاصية: notificationManager]: متغير/خاصية تحمل قيمة تشغيلية ضمن هذا النطاق، ويُحدد معناها من سياق العملية التي تستخدمها.
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            ?: return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // [توثيق المتغير/الخاصية: channel]: متغير/خاصية تحمل قيمة تشغيلية ضمن هذا النطاق، ويُحدد معناها من سياق العملية التي تستخدمها.
            val channel = NotificationChannel(
                AutoBackupWorker.CHANNEL_ID,
                context.getString(R.string.autobackup_channel_name),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = context.getString(R.string.autobackup_channel_desc)
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 100, 50, 150)
            }
            notificationManager.createNotificationChannel(channel)
        }

        // [توثيق المتغير/الخاصية: openIntent]: متغير/خاصية تحمل قيمة تشغيلية ضمن هذا النطاق، ويُحدد معناها من سياق العملية التي تستخدمها.
        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        // [توثيق المتغير/الخاصية: pendingIntent]: متغير/خاصية تحمل قيمة تشغيلية ضمن هذا النطاق، ويُحدد معناها من سياق العملية التي تستخدمها.
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // [توثيق المتغير/الخاصية: title]: متغير/خاصية تحمل قيمة تشغيلية ضمن هذا النطاق، ويُحدد معناها من سياق العملية التي تستخدمها.
        val title = context.getString(R.string.autobackup_notification_title_cloud_delayed)
        // [توثيق المتغير/الخاصية: text]: متغير/خاصية تحمل قيمة تشغيلية ضمن هذا النطاق، ويُحدد معناها من سياق العملية التي تستخدمها.
        val text = context.getString(R.string.autobackup_notification_text_cloud_delayed)

        // [توثيق المتغير/الخاصية: notification]: متغير/خاصية تحمل قيمة تشغيلية ضمن هذا النطاق، ويُحدد معناها من سياق العملية التي تستخدمها.
        val notification = NotificationCompat.Builder(context, AutoBackupWorker.CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_upload_done)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }
}

// --- ملاحظات وتوصيات المعمارية البرمجية ---
// - يفضل مستقبلاً توحيد مفاتيح حالة الرفع المعلق بين Worker وGoogleDriveSyncHelper في عقد واحد.
// - سياسة Exponential Backoff مناسبة للأخطاء المؤقتة، ويجب عدم تطبيقها على أخطاء المصادقة الدائمة دون تمييز.
// - ينبغي استمرار التحقق من سلامة الملف قبل الرفع وعدم تسجيل محتوى النسخة في السجلات.
// - هذه الملاحظات توصيات مستقبلية فقط ولا تغيّر التنفيذ الحالي أو عقده البرمجي.
