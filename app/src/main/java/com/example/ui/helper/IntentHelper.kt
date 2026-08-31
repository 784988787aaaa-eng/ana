/**
 * التوثيق المعماري العربي — مشروع «الدفتر الذكي / ميزان الدار».
 * الملف: app/src/main/java/com/example/ui/helper/IntentHelper.kt
 * المسؤولية: أدوات مساعدة لبناء وتنفيذ Intent للتكامل مع وظائف النظام والتطبيقات الأخرى.
 *
 * القراءة التعليمية: يوضح هذا الملف كيف تنتقل حالة التطبيق من الطبقة المشتركة
 * إلى المشهد المرئي على الهاتف، مع تفسير العقود والحالة والتوابع والتفاعلات.
 * الكتلة التنفيذية الأصلية أدناه محفوظة حرفياً؛ الإضافات التوثيقية لا تعدّل
 * أي رمز تنفيذي وفق قاعدة Zero Code Alteration.
 */

// --- فهرس التوثيق التعليمي للسطر التنفيذي ---
// توثيق السطر 1: التوجيه الحزمي يحدد الموضع المنطقي للملف داخل طبقة الواجهة.
// توثيق السطر 3: الاستيراد التالي اعتماد أصلي يحتاجه الملف؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 4: الاستيراد التالي اعتماد أصلي يحتاجه الملف؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 5: الاستيراد التالي اعتماد أصلي يحتاجه الملف؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 6: الاستيراد التالي اعتماد أصلي يحتاجه الملف؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 7: الاستيراد التالي اعتماد أصلي يحتاجه الملف؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 8: الاستيراد التالي اعتماد أصلي يحتاجه الملف؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 35: الشرط التالي يحافظ على قرار التنفيذ الأصلي.

package com.example.ui.helper

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File

// Helper function to share the backup file using FileProvider
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

// Helper to launch or download Google Drive app from store
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

// Helper to dial a phone number
fun dialPhoneNumber(context: Context, phoneNumber: String) {
    try {
        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phoneNumber"))
        context.startActivity(intent)
    } catch (e: Exception) {
        android.util.Log.e("IntentHelper", "Failed to launch dialer", e)
        Toast.makeText(context, context.getString(com.example.R.string.toast_operation_failed), Toast.LENGTH_SHORT).show()
    }
}

// Helper to open WhatsApp chat with a message
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


// --- ملاحظات وتوصيات المعمارية البرمجية ---
// 1) تم الحفاظ على الكتلة التنفيذية الأصلية دون تعديل أو حذف أو إعادة ترتيب.
// 2) يوصى مستقبلاً بإبقاء مكونات Compose المشتركة صغيرة ومحددة المسؤولية لتقليل إعادة التركيب غير الضرورية.
// 3) ينبغي إبقاء حالات الحوار والتنقل قابلة للتتبع من مصدر واحد عند اتساع عدد المسارات، مع الحفاظ على السلوك الحالي.
// 4) يوصى بعزل أدوات Intent وحفظ الملفات والاهتزاز عن واجهات Compose قدر الإمكان عبر عقود واضحة، دون تعديل النسخة الحالية.
// 5) في نظام Theme والألوان والطباعة، يوصى بالحفاظ على مصدر تصميم مركزي ومنع القيم المتناثرة مستقبلاً.
// 6) أي تحسين لاحق يجب أن يسبقه اختبار تكافؤ سلوكي/مرئي مناسب قبل الدمج.
