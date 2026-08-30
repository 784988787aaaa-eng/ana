/**
 * =====================================================================
 * ملف: مشغل النوايا وموجه مشاركة وعرض تقارير PDF (PdfIntentLauncher.kt)
 * =====================================================================
 * 
 * [الغرض العام والتعليمي من الملف]:
 * يوفر هذا الكائن وسيطاً آمناً وموحداً للتعامل مع ملفات تقارير PDF المولدة محلياً،
 * حيث يتولى تحويل مسارات الملفات إلى مسارات محتوى آمنة [Uri] باستخدام [FileProvider]،
 * وتوجيه أوامر المشاركة العامة [ACTION_SEND] أو العرض المباشر [ACTION_VIEW]،
 * بالإضافة إلى وظيفة مساعدة لتحرير وتدوير الصور النقطية [Bitmap] بأمان.
 * 
 * [المسؤوليات المعمارية والتقنية]:
 * 1. حماية وتأمين مشاركة الملفات (FileProvider Security):
 *    - منح أذونات القراءة المؤقتة لتطبيقات الطرف الثالث دون الكشف عن المسارات الحقيقية.
 * 2. توجيه نوايا النظام (Intent Launching):
 *    - إطلاق عارض ملفات PDF مع إضافة علم [FLAG_ACTIVITY_NEW_TASK].
 *    - إطلاق حوار المشاركة الشامل [Intent.createChooser].
 * 3. التحرير الآمن للذاكرة (Safe Memory Recycling):
 *    - تفريغ صور الشعار والبيتماب لتجنب تراكمها في الذاكرة.
 */
package com.example.data.serialization.pdf

// ---------------------------------------------------------------------
// استيراد حزم سياق أندرويد والنوايا والرسومات ومزود الملفات والرسائل
// ---------------------------------------------------------------------
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.util.Log
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.R
import java.io.File

/**
 * [الكائن الأحادي لموجه نوايا تقارير PDF - PdfIntentLauncher]:
 * يدير إرسال النوايا وعرض التقارير وتفريغ كائنات الصور.
 */
object PdfIntentLauncher {

    /** وسم السجلات التشخيصية */
    private const val TAG = "PdfIntentLauncher"
    /** نوع محتوى ملفات PDF المعتمد في نظام أندرويد */
    private const val MIME_TYPE_PDF = "application/pdf"
    /** اللاحقة المعتمدة لمزود الملفات في البيان */
    private const val FILE_PROVIDER_SUFFIX = ".fileprovider"

    /**
     * [توجيه نية المشاركة أو العرض للتقرير - triggerShareOrViewIntent]:
     * يولد مسار المحتوى الآمن ويطلق النية المناسبة وفق خيار [PdfAction].
     *
     * @param context سياق التطبيق لإطلاق الأنشطة وعرض التنبيهات.
     * @param file كائن الملف المولد في وحدة التخزين المؤقتة أو المحلية.
     * @param action نوع الإجراء المطلوب (مشاركة أو معاينة).
     */
    fun triggerShareOrViewIntent(context: Context, file: File?, action: PdfAction) {
        if (file == null) {
            Toast.makeText(
                context,
                context.getString(R.string.habayeb_toast_pdf_export_failed, context.getString(R.string.csv_error_creating_file)),
                Toast.LENGTH_LONG
            ).show()
            return
        }
        try {
            val authority = "${context.packageName}$FILE_PROVIDER_SUFFIX"
            val uri = FileProvider.getUriForFile(context, authority, file)

            when (action) {
                PdfAction.SHARE -> {
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = MIME_TYPE_PDF
                        putExtra(Intent.EXTRA_STREAM, uri)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    context.startActivity(Intent.createChooser(shareIntent, context.getString(R.string.pdf_chooser_title)))
                }
                PdfAction.VIEW -> {
                    val viewIntent = Intent(Intent.ACTION_VIEW).apply {
                        setDataAndType(uri, MIME_TYPE_PDF)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(viewIntent)
                }
                else -> {}
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error sharing or viewing PDF file", e)
            Toast.makeText(context, context.getString(R.string.toast_operation_failed), Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * [تدوير وتحرير صور البيتماب بأمان - recycleBitmapsSafely]:
     * يحرر الذاكرة المستهلكة في صور الشعارات الأصلية والمصغرة دون التسبب في أخطاء.
     *
     * @param rawBitmap الصورة النقطية الأصلية.
     * @param scaledLogo الصورة النقطية المصغرة.
     */
    fun recycleBitmapsSafely(rawBitmap: Bitmap?, scaledLogo: Bitmap?) {
        try {
            if (rawBitmap != null && !rawBitmap.isRecycled) {
                if (scaledLogo != null && scaledLogo != rawBitmap && !scaledLogo.isRecycled) {
                    scaledLogo.recycle()
                }
                rawBitmap.recycle()
            } else if (scaledLogo != null && !scaledLogo.isRecycled) {
                scaledLogo.recycle()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error recycling bitmaps", e)
        }
    }
}

