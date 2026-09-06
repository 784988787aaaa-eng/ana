/**
 * عامل خلفي مستقل لتذكير المستخدم بالنسخ الاحتياطي عند تقادم آخر نسخة ناجحة.
 * لا يغير البيانات المالية ولا يعتمد على الاتصال الشبكي.
 */
package com.smartledger.aldaftar

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
import com.smartledger.aldaftar.data.backup.BackupConstants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.concurrent.TimeUnit

class BackupReminderWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    companion object {
        private const val TAG = "BackupReminderWorker"
        const val UNIQUE_WORK_NAME = "MizanBackupReminder"
        private const val NOTIFICATION_ID = 1003
        private const val THIRTY_SIX_HOURS_MS = 36 * 60 * 60 * 1000L

        fun scheduleReminder(context: Context) {
            try {
                val workManager = WorkManager.getInstance(context)

                val now = Calendar.getInstance()
                val target = Calendar.getInstance().apply {
                    add(Calendar.DAY_OF_YEAR, 1)
                    val randomHour = (14..20).random()
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

                workManager.enqueueUniqueWork(
                    UNIQUE_WORK_NAME,
                    ExistingWorkPolicy.KEEP,
                    reminderRequest
                )
                Log.d(TAG, "تمت جدولة تذكير النسخ القادم بنجاح بعد ${delayMs / (1000 * 60)} دقيقة")
            } catch (e: Exception) {
                Log.e(TAG, "تعذر جدولة تذكير النسخ الاحتياطي: ${e.javaClass.simpleName}")
            }
        }
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val context = applicationContext
        try {

            if (isReminderNeeded(context)) {

                val reminderMessage = getRandomReminderMessage(context)

                sendReminderNotification(context, reminderMessage)
            } else {
                Log.d(TAG, "لا حاجة للتذكير: قام المستخدم بإجراء نسخة احتياطية حديثاً")
            }
        } catch (e: Exception) {
            Log.e(TAG, "تعذر تنفيذ عامل التذكير بالنسخ الاحتياطي: ${e.javaClass.simpleName}")
        } finally {

            scheduleReminder(context)
        }
        Result.success()
    }

    private fun isReminderNeeded(context: Context): Boolean {
        val sharedPrefs = context.getSharedPreferences(BackupConstants.PREFS_BACKUP, Context.MODE_PRIVATE)
        val lastBackupTimestamp = sharedPrefs.getLong(BackupConstants.KEY_LAST_SUCCESSFUL_BACKUP, 0L)
        val now = System.currentTimeMillis()

        if (lastBackupTimestamp > 0L && (now - lastBackupTimestamp) < THIRTY_SIX_HOURS_MS) {
            return false
        }
        return true
    }

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
