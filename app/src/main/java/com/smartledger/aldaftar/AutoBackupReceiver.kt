/**
 * مستقبل نظامي يعيد جدولة مهام النسخ الاحتياطي بعد إقلاع الجهاز أو تغير وقت النظام.
 * لا يقرأ بيانات مالية ولا ينفذ النسخ داخل خيط الواجهة.
 */
package com.smartledger.aldaftar

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * يعيد تسجيل مهام النسخ الدورية بعد الأحداث النظامية التي قد تؤثر في مواعيد الجدولة.
 */
class AutoBackupReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        if (action != Intent.ACTION_BOOT_COMPLETED &&
            action != Intent.ACTION_TIME_CHANGED &&
            action != Intent.ACTION_TIMEZONE_CHANGED
        ) {
            return
        }

        val safeContext = context.applicationContext
        AutoBackupWorker.scheduleDailyBackupWorker(safeContext)
        BackupReminderWorker.scheduleReminder(safeContext)

        val preferences = safeContext.getSharedPreferences(
            AutoBackupWorker.PREFS_NAME,
            Context.MODE_PRIVATE
        )
        if (preferences.getBoolean(com.smartledger.aldaftar.data.backup.BackupConstants.KEY_PENDING_CLOUD_UPLOAD, false)) {
            CloudUploadWorker.enqueueUploadLatest(safeContext)
        }
    }
}
