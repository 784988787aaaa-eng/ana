/**
 * عامل خلفي لرفع النسخ الاحتياطية المعلقة إلى التخزين السحابي عند توفر الشبكة.
 * يتحقق من سلامة الملف قبل الرفع ويحافظ على استمرار العمل المحلي دون الشبكة.
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
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.smartledger.aldaftar.data.GoogleDriveSyncHelper
import com.smartledger.aldaftar.data.backup.BackupConstants
import com.smartledger.aldaftar.data.backup.BackupFileManager
import com.smartledger.aldaftar.data.serialization.BackupIntegrityManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.TimeUnit

class CloudUploadWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    companion object {
        private const val TAG = "CloudUploadWorker"
        const val WORK_NAME = "MizanDelayedCloudUpload"
        const val KEY_FILE_PATH = "backup_file_path"
        const val KEY_FILE_NAME = "backup_file_name"
        private const val NOTIFICATION_ID = 1004

        fun enqueueUpload(context: Context, filePath: String, fileName: String) {
            val sharedPrefs = context.getSharedPreferences(BackupConstants.PREFS_BACKUP, Context.MODE_PRIVATE)
            sharedPrefs.edit()
                .putBoolean(BackupConstants.KEY_PENDING_CLOUD_UPLOAD, true)
                .putString("pending_cloud_file_path", filePath)
                .putString("pending_cloud_file_name", fileName)
                .apply()

            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val data = Data.Builder()
                .putString(KEY_FILE_PATH, filePath)
                .putString(KEY_FILE_NAME, fileName)
                .build()

            val uploadWorkRequest = OneTimeWorkRequestBuilder<CloudUploadWorker>()
                .setConstraints(constraints)
                .setInputData(data)
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
            Log.d(TAG, "تم إدراج طلب الرفع السحابي للملف $fileName بانتظار توفر الإنترنت")
        }

        fun enqueueUploadLatest(context: Context) {
            val sharedPrefs = context.getSharedPreferences(BackupConstants.PREFS_BACKUP, Context.MODE_PRIVATE)
            val path = sharedPrefs.getString("pending_cloud_file_path", null)
            val name = sharedPrefs.getString("pending_cloud_file_name", null)

            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val dataBuilder = Data.Builder()
            if (!path.isNullOrEmpty()) dataBuilder.putString(KEY_FILE_PATH, path)
            if (!name.isNullOrEmpty()) dataBuilder.putString(KEY_FILE_NAME, name)

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

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val context = applicationContext
        val sharedPrefs = context.getSharedPreferences(BackupConstants.PREFS_BACKUP, Context.MODE_PRIVATE)

        try {

            val syncHelper = GoogleDriveSyncHelper(context)
            val isLinked = !syncHelper.getStoredRefreshToken().isNullOrEmpty()
            if (!isLinked) {
                Log.d(TAG, "Google Drive غير مربوط. تم تخطي الرفع السحابي وإلغاء التعليق.")
                sharedPrefs.edit().putBoolean(BackupConstants.KEY_PENDING_CLOUD_UPLOAD, false).apply()
                return@withContext Result.success()
            }

            val targetFile = resolveTargetBackupFile(context)
            if (targetFile == null || !targetFile.exists() || targetFile.length() == 0L) {
                Log.w(TAG, "لم يتم العثور على ملف نسخة احتياطية صالح للرفع السحابي.")
                sharedPrefs.edit().putBoolean(BackupConstants.KEY_PENDING_CLOUD_UPLOAD, false).apply()
                return@withContext Result.failure()
            }

            val integrityResult = BackupIntegrityManager.validateBackupFileIntegrity(targetFile)
            if (integrityResult !is BackupIntegrityManager.IntegrityCheckResult.Valid) {
                Log.e(TAG, "ملف النسخة الاحتياطية تالف وغير صالح للرفع السحابي.")
                sharedPrefs.edit().putBoolean(BackupConstants.KEY_PENDING_CLOUD_UPLOAD, false).apply()
                return@withContext Result.failure()
            }

            val jsonStr = targetFile.readText(Charsets.UTF_8)
            val uploadName = targetFile.name
            val success = syncHelper.uploadBackupToDriveWithFilename(uploadName, jsonStr)

            if (success) {
                Log.d(TAG, "تم رفع النسخة السحابية بنجاح: $uploadName")
                sharedPrefs.edit()
                    .putBoolean(BackupConstants.KEY_PENDING_CLOUD_UPLOAD, false)
                    .putLong("last_successful_cloud_backup_timestamp", System.currentTimeMillis())
                    .apply()

                com.smartledger.aldaftar.ui.helper.VibrationHelper.triggerSuccessVibration(context)
                sendDelayedUploadNotification(context, uploadName)
                Result.success()
            } else {
                Log.w(TAG, "تعذر إتمام الرفع السحابي، ستتم إعادة المحاولة تلقائياً.")
                Result.retry()
            }
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "تعذر إكمال الرفع السحابي: ${e.javaClass.simpleName}")
            Result.retry()
        }
    }

    private fun resolveTargetBackupFile(context: Context): File? {
        val sharedPrefs = context.getSharedPreferences(BackupConstants.PREFS_BACKUP, Context.MODE_PRIVATE)
        val pathFromInput = inputData.getString(KEY_FILE_PATH)
            ?: sharedPrefs.getString("pending_cloud_file_path", null)

        if (!pathFromInput.isNullOrBlank()) {
            val file = File(pathFromInput)
            if (file.exists() && file.isFile && file.length() > 0L) {
                return file
            }
        }

        val fileManager = BackupFileManager(context)
        val backups = fileManager.getAllBackupFiles()
        return backups.firstOrNull()
    }

    private fun sendDelayedUploadNotification(context: Context, fileName: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            ?: return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
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

        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = context.getString(R.string.autobackup_notification_title_cloud_delayed)
        val text = context.getString(R.string.autobackup_notification_text_cloud_delayed)

        val notification = NotificationCompat.Builder(context, AutoBackupWorker.CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_upload_done)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        try {
            notificationManager.cancel(1001)
        } catch (t: Throwable) {
            Log.w(TAG, "تعذر تنظيف إشعار التقدم: ${t.javaClass.simpleName}")
        }
        notificationManager.notify(NOTIFICATION_ID, notification)
    }
}
