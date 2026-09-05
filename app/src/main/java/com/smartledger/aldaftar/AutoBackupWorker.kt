/**
 * عامل الخلفية المسؤول عن إنشاء النسخة الاحتياطية المحلية الدورية والتحقق من سلامتها،
 * ثم محاولة المزامنة السحابية دون التأثير في عمل المحاسبة المحلية عند غياب الشبكة.
 */
package com.smartledger.aldaftar

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
import com.smartledger.aldaftar.data.GoogleDriveSyncHelper
import com.smartledger.aldaftar.data.backup.BackupConstants
import com.smartledger.aldaftar.data.backup.BackupFileManager
import com.smartledger.aldaftar.data.backup.BackupOperationResult
import com.smartledger.aldaftar.data.backup.BackupService
import com.smartledger.aldaftar.data.local.AppDatabase
import com.smartledger.aldaftar.data.local.entities.AppSettings
import com.smartledger.aldaftar.data.serialization.BackupIntegrityManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class AutoBackupWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    companion object {
        private const val TAG = "AutoBackupWorker"
        const val WORK_NAME = "MizanDailyBackup"
        const val PREFS_NAME = BackupConstants.PREFS_BACKUP
        const val CHANNEL_ID = "mizan_backup_channel"
        private const val NOTIFICATION_PROGRESS_ID = 1001
        private const val NOTIFICATION_RESULT_ID = 1002

        fun scheduleDailyBackupWorker(context: Context) {
            val workManager = WorkManager.getInstance(context)

            val currentDate = Calendar.getInstance()
            val dueDate = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            if (dueDate.before(currentDate)) {
                dueDate.add(Calendar.DAY_OF_YEAR, 1)
            }
            val initialDelay = (dueDate.timeInMillis - currentDate.timeInMillis).coerceAtLeast(0L)

            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                .setRequiresBatteryNotLow(true)
                .build()

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

            workManager.enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                dailyWorkRequest
            )
            Log.d(TAG, "تمت جدولة النسخ الاحتياطي اليومي بنجاح بعد ${initialDelay / (1000 * 60)} دقيقة")
        }

        fun cancelDailyBackupWorker(context: Context) {
            val workManager = WorkManager.getInstance(context)
            workManager.cancelUniqueWork(WORK_NAME)
            Log.d(TAG, "تم إلغاء مهمة النسخ الاحتياطي اليومي.")
        }

        suspend fun checkAndTriggerBackupIfMissed(context: Context) = withContext(Dispatchers.IO) {
            try {
                val db = AppDatabase.getDatabase(context)
                val settings = db.settingsDao().getSettingsDirect() ?: AppSettings()
                if (!settings.isAutoBackupEnabled) {
                    Log.d(TAG, "النسخ التلقائي معطل في إعدادات المستخدم.")
                    return@withContext
                }

                val sharedPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

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
                Log.e(TAG, "تعذر فحص النسخ الاحتياطي الفائت: ${e.javaClass.simpleName}")
            }
        }
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val context = applicationContext
        try {
            val db = AppDatabase.getDatabase(context)
            val settings = db.settingsDao().getSettingsDirect() ?: AppSettings()

            if (!settings.isAutoBackupEnabled) {
                Log.d(TAG, "ميزة النسخ الاحتياطي التلقائي معطلة في الإعدادات.")
                return@withContext Result.success()
            }

            sendBackupInProgressNotification(context)

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

                    val integrity = BackupIntegrityManager.validateBackupFileIntegrity(activeFile)
                    if (integrity !is BackupIntegrityManager.IntegrityCheckResult.Valid) {
                        Log.e(TAG, "فشل فحص سلامة ملف النسخة بعد إنشائه")
                        sendBackupFailureNotification(context, false)
                        return@withContext Result.retry()
                    }

                    val sharedPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                    sharedPrefs.edit()
                        .putLong("last_successful_auto_backup_timestamp", backupResult.timestamp)
                        .putLong(BackupConstants.KEY_LAST_SUCCESSFUL_BACKUP, backupResult.timestamp)
                        .apply()

                    val syncHelper = GoogleDriveSyncHelper(context)
                    val isCloudLinked = !syncHelper.getStoredRefreshToken().isNullOrEmpty()
                    var cloudSynced = false

                    if (isCloudLinked) {
                        if (isNetworkConnected(context)) {
                            val jsonContent = activeFile.readText(Charsets.UTF_8)
                            cloudSynced = syncHelper.uploadBackupToDriveWithFilename(fileName, jsonContent)
                        }

                        if (!cloudSynced) {
                            Log.w(TAG, "تعذر الرفع السحابي الفوري، إدراج CloudUploadWorker في الخلفية.")
                            sharedPrefs.edit().putBoolean(BackupConstants.KEY_PENDING_CLOUD_UPLOAD, true).apply()
                            CloudUploadWorker.enqueueUpload(context, activeFile.absolutePath, fileName)
                        } else {
                            sharedPrefs.edit().putBoolean(BackupConstants.KEY_PENDING_CLOUD_UPLOAD, false).apply()
                        }
                    }

                    com.smartledger.aldaftar.ui.helper.VibrationHelper.triggerSuccessVibration(context)
                    sendBackupSuccessNotification(context, activeFile.name, cloudSynced)
                    Result.success()
                }
                is BackupOperationResult.Failure -> {
                    Log.e(TAG, "فشلت عملية إنشاء النسخة الاحتياطية المحلية: ${backupResult.userMessage}")
                    sendBackupFailureNotification(context, false)
                    if (backupResult.cause is IOException) {
                        Result.retry()
                    } else {
                        Result.failure()
                    }
                }
            }
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "تعذر إكمال دورة النسخ الاحتياطي التلقائي: ${e.javaClass.simpleName}")
            sendBackupFailureNotification(context, false)
            if (e is IOException) Result.retry() else Result.failure()
        }
    }

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

    private fun sendBackupInProgressNotification(context: Context) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return

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

        notificationManager.cancel(NOTIFICATION_PROGRESS_ID)
        notificationManager.notify(NOTIFICATION_RESULT_ID, notification)
    }

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

        notificationManager.cancel(NOTIFICATION_PROGRESS_ID)
        notificationManager.notify(NOTIFICATION_RESULT_ID, notification)
    }
}
