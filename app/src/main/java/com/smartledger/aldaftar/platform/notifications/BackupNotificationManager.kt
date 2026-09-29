package com.smartledger.aldaftar.platform.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.app.PendingIntent
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.smartledger.aldaftar.R

class BackupNotificationManager(private val context: Context) {
    companion object {
        private const val CHANNEL = "backup_daily"
        private const val ID = 70413
    }

    fun show(title: String, text: String, fileUri: Uri? = null) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) return

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            manager.createNotificationChannel(
                NotificationChannel(CHANNEL, "النسخ الاحتياطي للدفتر الذكي", NotificationManager.IMPORTANCE_LOW).apply {
                    description = "إشعارات النسخ الاحتياطي اليومي المحلي والسحابي"
                    setShowBadge(true)
                }
            )
        }

        val contentIntent = fileUri?.let { uri ->
            Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/vnd.smartledger.backup")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }.let { intent ->
                PendingIntent.getActivity(
                    context, ID, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            }
        }

        manager.notify(
            ID,
            NotificationCompat.Builder(context, CHANNEL)
                .setSmallIcon(R.drawable.ic_splash_logo)
                .setContentTitle(title)
                .setContentText(text)
                .setStyle(NotificationCompat.BigTextStyle().bigText(text))
                .setAutoCancel(true)
                .apply { if (contentIntent != null) setContentIntent(contentIntent) }
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build()
        )
    }
}
