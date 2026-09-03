/**
 * =====================================================================
 * ملف: العامل الخلفي للتذكير بالنسخ الاحتياطي (BackupReminderWorker.kt)
 * =====================================================================
 * 
 * [الغرض العام والتعليمي من الملف]:
 * يهدف هذا الملف إلى حماية بيانات المستخدم من الضياع عبر إرسال إشعارات تذكيرية
 * ذكية وغير مزعجة عندما يمر أكثر من 36 ساعة دون إنشاء نسخة احتياطية ناجحة.
 * 
 * [آلية وسير تدفق العمليات (Workflow Flow)]:
 * 1. تتم جدولة التذكير ليعمل في نافذة زمنية نشطة ومريحة للمستخدم (بين الساعة 2 ظهراً و 8 مساءً).
 * 2. عند استيقاظ العامل الخلفي، يتحقق من توقيت آخر نسخة احتياطية ناجحة.
 * 3. إذا كان الوقت المنقضي أقل من 36 ساعة، يتم تخطي الإشعار بهدوء لعدم إزعاج المستخدم.
 * 4. إذا تجاوزت المدة 36 ساعة، يتم اختيار نص تذكيري ودي من موارد النصوص وعرضه في شريط الإشعارات.
 * 5. عند النقر على الإشعار، يفتح التطبيق ويوجه المستخدم مباشرة إلى شاشة إدارة النسخ الاحتياطي.
 * 6. في جميع الحالات (سواء أُرسل إشعار أم لا)، يعيد العامل جدولة نفسه لليوم التالي لضمان استمرارية الحماية.
 */
package com.example

// ---------------------------------------------------------------------
// استيراد أدوات الإشعارات، وسياق أندرويد، ومكتبة المهام الخلفية WorkManager
// ---------------------------------------------------------------------
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.data.backup.BackupConstants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.concurrent.TimeUnit

/**
 * [فئة العامل الخلفي للتذكير - BackupReminderWorker]:
 * فئة غير متزامنة (CoroutineWorker) تنفذ فحص التوقيت وإرسال التنبيهات في خيط خلفي (IO).
 */
class BackupReminderWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    /**
     * [الكائن المرافق - Companion Object]:
     * يضم الثوابت المعرفة لمعرف الإشعار ونافذة الساعات (36 ساعة)، بالإضافة إلى دالة الجدولة.
     */
    companion object {
        private const val TAG = "BackupReminderWorker"
        const val UNIQUE_WORK_NAME = "MizanBackupReminder"
        private const val NOTIFICATION_ID = 1003
        private const val THIRTY_SIX_HOURS_MS = 36 * 60 * 60 * 1000L

        /**
         * [دالة جدولة التذكير القادم]:
         * تحسب موعداً عشوائياً في اليوم التالي بين الساعة 14:00 (2 ظهراً) و 20:59 (8:59 مساءً)
         * لمنع إرسال الإشعارات في أوقات النوم أو في أوقات متطابقة دائماً.
         */
        fun scheduleReminder(context: Context) {
            try {
                val workManager = WorkManager.getInstance(context)

                val now = Calendar.getInstance()
                val target = Calendar.getInstance().apply {
                    add(Calendar.DAY_OF_YEAR, 1) // ضبط اليوم ليكون غداً
                    val randomHour = (14..20).random() // اختيار ساعة نشاط مناسبة
                    val randomMinute = (0..59).random()
                    set(Calendar.HOUR_OF_DAY, randomHour)
                    set(Calendar.MINUTE, randomMinute)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                val delayMs = (target.timeInMillis - now.timeInMillis).coerceAtLeast(0L)

                val reminderRequest = OneTimeWorkRequestBuilder<BackupReminderWorker>()
                    .setInitialDelay(delayMs, TimeUnit.MILLISECONDS)
                    .build()

                // نستخدم ExistingWorkPolicy.KEEP حتى لا نعيد ضبط المؤقت إذا كانت هناك جدولة قائمة بالفعل
                workManager.enqueueUniqueWork(
                    UNIQUE_WORK_NAME,
                    ExistingWorkPolicy.KEEP,
                    reminderRequest
                )
                Log.d(TAG, "تمت جدولة تذكير النسخ القادم بنجاح بعد ${delayMs / (1000 * 60)} دقيقة")
            } catch (e: Exception) {
                Log.e(TAG, "فشل أثناء محاولة جدولة تذكير النسخ الاحتياطي", e)
            }
        }
    }

    /**
     * [الدالة التنفيذية - doWork]:
     * تستدعى في الخلفية عندما يحين وقت التنفيذ المحدد.
     */
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val context = applicationContext
        try {
            // 1. تقييم قرار الحاجة للتذكير عبر التحقق من تاريخ آخر نسخة
            if (isReminderNeeded(context)) {
                // 2. اختيار نص التذكير من الموارد المترجمة
                val reminderMessage = getRandomReminderMessage(context)

                // 3. بناء وإرسال إشعار التذكير للمستخدم
                sendReminderNotification(context, reminderMessage)
            } else {
                Log.d(TAG, "لا حاجة للتذكير: قام المستخدم بإجراء نسخة احتياطية حديثاً")
            }
        } catch (e: Exception) {
            Log.e(TAG, "خطأ غير حرج أثناء تنفيذ عامل التذكير بالنسخ الاحتياطي", e)
        } finally {
            // إعادة الجدولة دائماً لليوم التالي لضمان بقاء التذكير نشطاً دوماً
            scheduleReminder(context)
        }
        Result.success()
    }

    /**
     * [دالة فحص الحاجة للتذكير]:
     * تقرأ توقيت آخر نسخة ناجحة من SharedPreferences.
     * تعيد `false` إذا كانت النسخة أحدث من 36 ساعة، و `true` إذا كانت قديمة أو غير موجودة.
     */
    private fun isReminderNeeded(context: Context): Boolean {
        val sharedPrefs = context.getSharedPreferences(BackupConstants.PREFS_BACKUP, Context.MODE_PRIVATE)
        val lastBackupTimestamp = sharedPrefs.getLong(BackupConstants.KEY_LAST_SUCCESSFUL_BACKUP, 0L)
        val now = System.currentTimeMillis()

        // لا داعي لإزعاج المستخدم إذا تم إجراء نسخة في آخر 36 ساعة
        if (lastBackupTimestamp > 0L && (now - lastBackupTimestamp) < THIRTY_SIX_HOURS_MS) {
            return false
        }
        return true
    }

    /**
     * [دالة اختيار نص التذكير]:
     * تختار عبارة تنبيهية عشوائية من ملف النصوص `strings.xml` لتنويع أسلوب التذكير.
     */
    private fun getRandomReminderMessage(context: Context): String {
        val reminderMessages = listOf(
            context.getString(R.string.backup_reminder_msg_1),
            context.getString(R.string.backup_reminder_msg_2),
            context.getString(R.string.backup_reminder_msg_3),
            context.getString(R.string.backup_reminder_msg_4),
            context.getString(R.string.backup_reminder_msg_5)
        )
        return reminderMessages.random()
    }

    /**
     * [دالة بناء وإظهار الإشعار]:
     * تنشئ قناة الإشعارات (لإصدارات أندرويد 8+) وتجهز Intent لنقل المستخدم لشاشة النسخ الاحتياطي.
     */
    private fun sendReminderNotification(context: Context, message: String) {
        val channelId = AutoBackupWorker.CHANNEL_ID
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            ?: return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                context.getString(R.string.autobackup_channel_name),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = context.getString(R.string.autobackup_channel_desc)
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        // توجيه المستخدم مباشرة إلى تبويب النسخ الاحتياطي عند النقر
        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", "backup_settings")
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            2001,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(context.getString(R.string.backup_reminder_title))
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }
}
