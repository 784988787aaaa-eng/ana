package com.example.ui.helper

/*
 * =====================================================================================
 * حزمة معالجة النوايا والتكامل الخارجي (Android Intents Helper Package)
 * -------------------------------------------------------------------------------------
 * تحتوي هذه الفئة على دوال مساعدة لإنشاء وإطلاق نوايا أندرويد (Android Intents)،
 * مثل مشاركة ملفات النسخ الاحتياطي عبر مزود الملفات الآمن (FileProvider)،
 * الاتصال الهاتفي، فتح محادثات واتساب، وتشغيل تطبيق جوجل درايف.
 * =====================================================================================
 */

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File

/*
 * =====================================================================================
 * دالة مشاركة ملف النسخة الاحتياطية بأمان (shareBackupFile)
 * -------------------------------------------------------------------------------------
 * [الوصف والهدف]:
 * تشارك ملف قاعدة البيانات أو النسخة الاحتياطية مع التطبيقات الخارجية (تليجرام، واتساب، درايف...):
 * 1. توليد رابط URI آمن ومعزول باستخدام موفر الملفات (FileProvider).
 * 2. منح إذن القراءة المؤقت للتطبيق المستلم (FLAG_GRANT_READ_URI_PERMISSION).
 * 3. فتح قائمة مشاركة النظام القياسية (System Share Chooser).
 * =====================================================================================
 */
fun shareBackupFile(context: Context, file: File) {
    try {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/octet-stream"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, context.getString(com.example.R.string.intent_share_backup_title)))
    } catch (e: Exception) {
        // حماية تجربة المستخدم من الرسائل التقنية غير المفهومة مع تسجيل الخطأ في السجلات
        android.util.Log.e("IntentHelper", "Failed to share backup file", e)
        Toast.makeText(context, context.getString(com.example.R.string.toast_operation_failed), Toast.LENGTH_SHORT).show()
    }
}

/*
 * =====================================================================================
 * دالة فتح أو تنزيل تطبيق جوجل درايف (openGoogleDriveApp)
 * -------------------------------------------------------------------------------------
 * [الوصف والهدف]:
 * تفحص وجود تطبيق Google Drive على الجهاز لتشغيله مباشرة، أو توجيه المستخدم لمتجر Google Play.
 * =====================================================================================
 */
fun openGoogleDriveApp(context: Context) {
    try {
        val launchIntent = context.packageManager.getLaunchIntentForPackage("com.google.android.apps.docs")
        if (launchIntent != null) {
            context.startActivity(launchIntent)
        } else {
            val playIntent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.google.android.apps.docs"))
            context.startActivity(playIntent)
        }
    } catch (e: Exception) {
        try {
            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.google.android.apps.docs"))
            context.startActivity(webIntent)
        } catch (ex: Exception) {
            Toast.makeText(context, context.getString(com.example.R.string.intent_play_store_failed), Toast.LENGTH_SHORT).show()
        }
    }
}

/*
 * =====================================================================================
 * دالة إجراء اتصال هاتفي (dialPhoneNumber)
 * -------------------------------------------------------------------------------------
 * [الوصف والهدف]:
 * تفتح واجهة لوحة الاتصال في الهاتف (Dialer) برقم العميل المحدد دون الحاجة لإذن الاتصال المباشر.
 * =====================================================================================
 */
fun dialPhoneNumber(context: Context, phoneNumber: String) {
    try {
        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phoneNumber"))
        context.startActivity(intent)
    } catch (e: Exception) {
        android.util.Log.e("IntentHelper", "Failed to launch dialer", e)
        Toast.makeText(context, context.getString(com.example.R.string.toast_operation_failed), Toast.LENGTH_SHORT).show()
    }
}

/*
 * =====================================================================================
 * دالة إرسال رسالة واتساب مباشرة (openWhatsAppChat)
 * -------------------------------------------------------------------------------------
 * [الوصف والهدف]:
 * تنظف رقم الهاتف من الرموز والمسافات الزائدة، وتفتح محادثة واتساب مع نص كشف الحساب المرمز.
 * =====================================================================================
 */
fun openWhatsAppChat(context: Context, phoneNumber: String, message: String) {
    try {
        val cleanNumber = phoneNumber.replace("+", "").replace(" ", "").trim()
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("https://api.whatsapp.com/send?phone=$cleanNumber&text=${Uri.encode(message)}")
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        android.util.Log.e("IntentHelper", "Failed to launch WhatsApp", e)
        Toast.makeText(context, context.getString(com.example.R.string.toast_operation_failed), Toast.LENGTH_SHORT).show()
    }
}


