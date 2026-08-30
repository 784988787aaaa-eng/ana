package com.example.ui.helper

/*
 * =====================================================================================
 * حزمة إدارة حفظ الملفات محلياً (Local Storage & File Saver Package)
 * -------------------------------------------------------------------------------------
 * تحتوي هذه الفئة على دوال تصدير وحفظ التقارير وكشوفات الحساب وقواعد البيانات الاحتياطية
 * في مجلد التنزيلات العام للجهاز (Public Downloads Directory) وفق أحدث معايير أندرويد الأمنية.
 * =====================================================================================
 */

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import com.example.R
import java.io.File

/*
 * =====================================================================================
 * كائن مساعد حفظ الملفات في مجلد التنزيلات العام (LocalFileSaver)
 * -------------------------------------------------------------------------------------
 * [المسؤوليات والمعايير الأمنية]:
 * 1. التوافق التام مع التخزين المحدود (Scoped Storage):
 *    استخدام واجهة MediaStore على إصدارات Android 10+ (API 29+) بدون الحاجة لطلب أذونات
 *    التخزين الخطرة (WRITE_EXTERNAL_STORAGE).
 * 2. التحكم في حالة الملف المعلق (IS_PENDING):
 *    تعيين الراية `IS_PENDING = 1` أثناء الكتابة لمنع التطبيقات الأخرى أو النظام من قراءة
 *    الملف قبل اكتمال نسخه بالكامل، ثم تعديلها إلى `0` فور إتمام النسخ.
 * 3. المعالجة التراجعية للإصدارات القديمة (Backward Compatibility):
 *    استخدام الدليل العام المباشر لنظام Android 9 فما دون.
 * 4. إدارة الموارد الآمنة: فتح وإغلاق تدفقات القراءة والكتابة داخل كتل `use` لضمان تحريرها دائماً.
 * =====================================================================================
 */
object LocalFileSaver {
    // وسم السجلات لتتبع العمليات
    private const val TAG = "LocalFileSaver"

    /*
     * ---------------------------------------------------------------------------------
     * دالة حفظ الملف في مجلد التنزيلات العام (saveFileToPublicDownloads)
     * ---------------------------------------------------------------------------------
     * [المُدخلات]:
     * - context: سياق التطبيق للوصول إلى ContentResolver.
     * - cachedFile: الملف المؤقت الموجود في الذاكرة المؤقتة للتطبيق (cacheDir).
     * - mimeType: نوع الوسائط للملف (مثل "application/pdf" أو "text/csv").
     * - displayName: الاسم الظاهر للملف عند حفظه (مثل "كشف_حساب_محمد.pdf").
     *
     * [المُخرجات]:
     * - Boolean: قيمة منطقية تعبر عن نجاح أو فشل عملية الحفظ.
     * ---------------------------------------------------------------------------------
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
                /*
                 * ---------------------------------------------------------------------
                 * الحفظ عبر MediaStore لنظام Android 10+ (API 29+)
                 * ---------------------------------------------------------------------
                 */
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, displayName)
                    put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                    put(MediaStore.MediaColumns.IS_PENDING, 1)
                }

                // إدراج سجل جديد في جدول التنزيلات الخارجي
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
                    // إلغاء راية الانتظار للإشارة إلى اكتمال كتابة الملف بنجاح
                    contentValues.clear()
                    contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                    resolver.update(uri, contentValues, null, null)
                    true
                } else {
                    false
                }
            } else {
                /*
                 * ---------------------------------------------------------------------
                 * الحفظ المباشر في الدليل العام لنظام Android 9 فما دون
                 * ---------------------------------------------------------------------
                 */
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

    /*
     * ---------------------------------------------------------------------------------
     * دالة مساعدة لحفظ الملف وعرض رسالة تنبيهية للمستخدم (saveAndShowToast)
     * ---------------------------------------------------------------------------------
     * تنفذ الحفظ وتُظهر إشعاراً مرئياً منبثقاً (Toast) يوضح نجاح أو فشل العملية وموقع الحفظ.
     * ---------------------------------------------------------------------------------
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

