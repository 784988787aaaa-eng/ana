package com.smartledger.aldaftar.ui.helper

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import com.smartledger.aldaftar.R
import java.io.File

/**
 * مساعد حفظ الملفات المصدرة في مساحة التنزيل العامة أو مساحة التطبيق الآمنة.
 * يستخدم التخزين المحدود ومسار النظام المخصص للملفات دون طلب صلاحيات تخزين عامة.
 */
object LocalFileSaver {
    private const val TAG = "LocalFileSaver"

    /**
     * يحفظ الملف المؤقت في مجلد المستندات العام عبر واجهة النظام الحديثة،
     * أو في مساحة التطبيق الآمنة على الإصدارات الأقدم.
     * يمنع التنفيذ طلب صلاحيات التخزين العامة ويغلق جميع تيارات القراءة والكتابة.
     *
     * @معامل السياق سياق التطبيق المستخدم للوصول إلى واجهة التخزين.
     * @معامل الملف المؤقت الملف المؤقت المراد تصديره.
     * @معامل نوع الملف نوع الملف المستخدم عند إنشاء السجل في مساحة التخزين.
     * @معامل الاسم الظاهر اسم الملف الظاهر للمستخدم.
     * @القيمة المعادة صحيح عند اكتمال الحفظ بنجاح، وخاطئ عند الفشل.
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
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS + "/" + "الدفتر الذكي")
                    put(MediaStore.MediaColumns.IS_PENDING, 1)
                }

                // ينشئ النظام سجلاً جديداً للاسم المطلوب داخل مساحة المستندات.
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
                // قبل أندرويد 10 لا توجد واجهة التخزين النظامية المسار النسبي؛ نحفظ في مساحة التطبيق المعزولة
                // دون طلب صلاحيات تخزين عامة، ثم يمكن للمستخدم مشاركة الملف عبر مزود الملفات أو واجهة اختيار الملفات.
                val targetDir = File(
                    context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS) ?: context.filesDir,
                    "الدفتر الذكي"
                )
                if (!targetDir.exists()) targetDir.mkdirs()
                val targetFile = File(targetDir, displayName)
                cachedFile.inputStream().use { inputStream ->
                    targetFile.outputStream().use { outputStream -> inputStream.copyTo(outputStream) }
                }
                true
            }
        } catch (e: Exception) {
            Log.e(TAG, "فشل حفظ الملف في مساحة التنزيل العامة", e)
            false
        }
    }

    /**
     * يعرض رسالة مختصرة للمستخدم بعد نجاح الحفظ أو فشله.
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
