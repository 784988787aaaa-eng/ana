/**
 * التوثيق المعماري العربي — مشروع «الدفتر الذكي / ميزان الدار».
 * الملف: app/src/main/java/com/example/ui/helper/LocalFileSaver.kt
 * المسؤولية: أداة مساعدة لحفظ الملفات محلياً باستخدام آليات النظام المناسبة.
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
// توثيق السطر 9: الاستيراد التالي اعتماد أصلي يحتاجه الملف؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 10: الاستيراد التالي اعتماد أصلي يحتاجه الملف؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 11: الاستيراد التالي اعتماد أصلي يحتاجه الملف؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 21: التعريف التالي يحدد عقداً أو نوعاً أصلياً؛ يحتفظ بالاسم والبنية كما وردا في المصدر.
// توثيق السطر 41: الشرط التالي يحافظ على قرار التنفيذ الأصلي.
// توثيق السطر 46: الشرط التالي يحافظ على قرار التنفيذ الأصلي.
// توثيق السطر 58: الشرط التالي يحافظ على قرار التنفيذ الأصلي.
// توثيق السطر 60: الشرط التالي يحافظ على قرار التنفيذ الأصلي.
// توثيق السطر 76: الشرط التالي يحافظ على قرار التنفيذ الأصلي.
// توثيق السطر 103: الشرط التالي يحافظ على قرار التنفيذ الأصلي.

package com.example.ui.helper

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import com.example.R
import java.io.File

/**
 * مساعد حفظ الملفات المصدرة في مجلد التنزيلات العام (Local File Saver to Public Downloads)
 *
 * المسؤوليات والمعايير الأمنية:
 * 1. التوافق التام مع التخزين المحدود (Scoped Storage): استخدام واجهة MediaStore على Android 10+ (API 29+) مع إدارة علم IS_PENDING لمنع قراءة الملفات غير المكتملة.
 * 2. العزل الآمن: نسخ الملفات المؤقتة من cacheDir إلى مجلد التنزيلات العام بطريقة حتمية وإغلاق مسارات التدفق بأمان (use blocks).
 * 3. عدم تسريب مسارات النظام الداخلية للمستخدمين أو التطبيقات الأخرى.
 */
object LocalFileSaver {
    private const val TAG = "LocalFileSaver"

    /**
     * Saves a cached file to the device's public Downloads directory.
     * Uses MediaStore API on Android 10+ (API 29+) to avoid requiring runtime storage permissions.
     * Uses standard file copy on Android 9 and below.
     *
     * @param context Android context
     * @param cachedFile The temporary file in cacheDir
     * @param mimeType MIME type of the file (e.g. "application/pdf", "text/csv")
     * @param displayName Desired file name (e.g. "statement_John_123.pdf")
     * @return Boolean indicating success
     */
    fun saveFileToPublicDownloads(
        context: Context,
        cachedFile: File,
        mimeType: String,
        displayName: String
    ): Boolean {
        if (!cachedFile.exists() || cachedFile.length() == 0L) {
            return false
        }
        return try {
            val resolver = context.contentResolver
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, displayName)
                    put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                    put(MediaStore.MediaColumns.IS_PENDING, 1)
                }

                // Query existing file with the same name to overwrite or resolve uniqueness
                val collectionUri = MediaStore.Downloads.EXTERNAL_CONTENT_URI
                val uri = resolver.insert(collectionUri, contentValues)

                if (uri != null) {
                    resolver.openOutputStream(uri).use { outputStream ->
                        if (outputStream != null) {
                            cachedFile.inputStream().use { inputStream ->
                                inputStream.copyTo(outputStream)
                            }
                        }
                    }
                    contentValues.clear()
                    contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                    resolver.update(uri, contentValues, null, null)
                    true
                } else {
                    false
                }
            } else {
                // Fallback for Android 9 and below (requires WRITE_EXTERNAL_STORAGE permission, which is declared)
                val targetDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                if (!targetDir.exists()) {
                    targetDir.mkdirs()
                }
                val targetFile = File(targetDir, displayName)
                cachedFile.inputStream().use { inputStream ->
                    targetFile.outputStream().use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
                true
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save file to public downloads", e)
            false
        }
    }

    /**
     * Helper to show toast messages upon saving.
     */
    fun saveAndShowToast(
        context: Context,
        cachedFile: File,
        mimeType: String,
        displayName: String
    ) {
        val success = saveFileToPublicDownloads(context, cachedFile, mimeType, displayName)
        if (success) {
            Toast.makeText(
                context,
                context.getString(R.string.autobackup_notification_title_local) + "\n" + displayName,
                Toast.LENGTH_LONG
            ).show()
        } else {
            Toast.makeText(
                context,
                context.getString(R.string.toast_save_failed),
                Toast.LENGTH_LONG
            ).show()
        }
    }
}

// --- ملاحظات وتوصيات المعمارية البرمجية ---
// 1) تم الحفاظ على الكتلة التنفيذية الأصلية دون تعديل أو حذف أو إعادة ترتيب.
// 2) يوصى مستقبلاً بإبقاء مكونات Compose المشتركة صغيرة ومحددة المسؤولية لتقليل إعادة التركيب غير الضرورية.
// 3) ينبغي إبقاء حالات الحوار والتنقل قابلة للتتبع من مصدر واحد عند اتساع عدد المسارات، مع الحفاظ على السلوك الحالي.
// 4) يوصى بعزل أدوات Intent وحفظ الملفات والاهتزاز عن واجهات Compose قدر الإمكان عبر عقود واضحة، دون تعديل النسخة الحالية.
// 5) في نظام Theme والألوان والطباعة، يوصى بالحفاظ على مصدر تصميم مركزي ومنع القيم المتناثرة مستقبلاً.
// 6) أي تحسين لاحق يجب أن يسبقه اختبار تكافؤ سلوكي/مرئي مناسب قبل الدمج.
