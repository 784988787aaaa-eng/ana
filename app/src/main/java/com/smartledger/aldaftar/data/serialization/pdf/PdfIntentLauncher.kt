/**
 * =====================================================================
 * ملف: مشغل النوايا وموجه مشاركة وعرض تقارير  (.)
 * =====================================================================
 * 
 * [الغرض العام والتعليمي من الملف]:
 * يوفر هذا الكائن وسيطاً آمناً وموحداً للتعامل مع ملفات تقارير  المولدة محلياً،
 * حيث يتولى تحويل مسارات الملفات إلى مسارات محتوى آمنة [] باستخدام []،
 * وتوجيه أوامر المشاركة العامة [_] أو العرض المباشر [_]،
 * بالإضافة إلى وظيفة مساعدة لتحرير وتدوير الصور النقطية [الصورة النقطية] بأمان.
 * 
 * [المسؤوليات المعمارية والتقنية]:
 * 1. حماية وتأمين مشاركة الملفات (أمان مزود الملفات):
 *    - منح أذونات القراءة المؤقتة لتطبيقات الطرف الثالث دون الكشف عن المسارات الحقيقية.
 * 2. توجيه نوايا النظام (إطلاق النوايا):
 *    - إطلاق عارض ملفات  مع إضافة علم [___].
 *    - إطلاق حوار المشاركة الشامل [.].
 * 3. التحرير الآمن للذاكرة (تحرير الذاكرة بأمان):
 *    - تفريغ صور الشعار والبيتماب لتجنب تراكمها في الذاكرة.
 */
package com.smartledger.aldaftar.data.serialization.pdf

// ---------------------------------------------------------------------
// استيراد حزم سياق أندرويد والنوايا والرسومات ومزود الملفات والرسائل
// ---------------------------------------------------------------------
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.widget.Toast
import androidx.core.content.FileProvider
import com.smartledger.aldaftar.R
import java.io.File

/**
 * [الكائن الأحادي لموجه نوايا تقارير  - ]:
 * يدير إرسال النوايا وعرض التقارير وتفريغ كائنات الصور.
 */
object PdfIntentLauncher {

    /** وسم السجلات التشخيصية */
    /** نوع محتوى ملفات  المعتمد في نظام أندرويد */
    private const val MIME_TYPE_PDF = "application/pdf"
    /** اللاحقة المعتمدة لمزود الملفات في البيان */
    private const val FILE_PROVIDER_SUFFIX = ".fileprovider"

    /**
     * [توجيه نية المشاركة أو العرض للتقرير - ]:
     * يولد مسار المحتوى الآمن ويطلق النية المناسبة وفق خيار [].
     *
     * @  سياق التطبيق لإطلاق الأنشطة وعرض التنبيهات.
     * @  كائن الملف المولد في وحدة التخزين المؤقتة أو المحلية.
     * @  نوع الإجراء المطلوب (مشاركة أو معاينة).
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
            Toast.makeText(context, context.getString(R.string.toast_operation_failed), Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * [تدوير وتحرير صور البيتماب بأمان - الصورة النقطية]:
     * يحرر الذاكرة المستهلكة في صور الشعارات الأصلية والمصغرة دون التسبب في أخطاء.
     *
     * @ الصورة النقطية الصورة النقطية الأصلية.
     * @  الصورة النقطية المصغرة.
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
        }
    }
}

