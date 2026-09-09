package com.smartledger.aldaftar.platform.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import com.smartledger.aldaftar.R

class BackupNotificationManager(private val context: Context) {
    companion object { private const val CHANNEL = "backup"; private const val ID = 70413 }
    fun show(title: String, text: String) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) manager.createNotificationChannel(NotificationChannel(CHANNEL, "النسخ الاحتياطي", NotificationManager.IMPORTANCE_LOW))
        manager.notify(ID, NotificationCompat.Builder(context, CHANNEL).setSmallIcon(R.drawable.img_app_icon).setContentTitle(title).setContentText(text).setAutoCancel(true).build())
    }
}
