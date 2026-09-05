/**
 * =====================================================================
 * ملف: مساعد وموجه مشاركة ملفات إكسل (ExcelShareHelper.kt)
 * =====================================================================
 * 
 * [الغرض العام والتعليمي من الملف]:
 * يقدم هذا الكائن واجهة مساعدة مركزية لإدارة العمليات اللاحقة لتوليد ملفات إكسل
 * (.xlsx)، مثل الحفظ في الذاكرة التخزينية للهاتف، أو الإرسال الفوري لجهة الاتصال
 * عبر واتساب، أو فتح نافذة مشاركة النظام العامة (Android Share Intent).
 * 
 * [المسؤوليات المعمارية والتقنية]:
 * 1. حماية وتأمين مسارات الملفات عبر [FileProvider]:
 *    - توليد روابط موثوقة (Content URIs) ومنح إذن القراءة المؤقت [FLAG_GRANT_READ_URI_PERMISSION].
 * 2. تحديد نوع الوسائط الدقيق لمصنفات إكسل الحديثة:
 *    - استخدام `application/vnd.openxmlformats-officedocument.spreadsheetml.sheet` لضمان تعرف التطبيقات المستقبلة عليه.
 * 3. التكامل المرن مع قنوات التوزيع:
 *    - التوجيه الذكي إما للحفظ المحلي عبر [LocalFileSaver] أو الإرسال المباشر لواتساب عبر [CustomerShareHelper].
 * 4. إدارة تجربة المستخدم والأخطاء:
 *    - عرض إشعارات نجاح أو فشل موحدة، وتسجيل الاستثناءات في سجلات التطبيق التشخيصية.
 */
package com.smartledger.aldaftar.data.serialization.excel

// ---------------------------------------------------------------------
// استيراد حزم سياق أندرويد والنوايا ومزود الملفات والكيانات والمساعدات
// ---------------------------------------------------------------------
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.core.content.FileProvider
import com.smartledger.aldaftar.R
import com.smartledger.aldaftar.data.local.entities.HabayebCustomer
import com.smartledger.aldaftar.data.serialization.CsvReportGenerator
import com.smartledger.aldaftar.ui.helper.LocalFileSaver
import com.smartledger.aldaftar.ui.screens.habayeb.utils.CustomerShareHelper
import java.io.File

/**
 * [الكائن الأحادي لمساعد مشاركة ملفات إكسل - ExcelShareHelper]:
 * يدير خيارات الحفظ والمشاركة والإرسال لتقارير إكسل المولدة.
 */
object ExcelShareHelper {

    /** وسم السجلات التشخيصية */
    private const val TAG = "ExcelShareHelper"
    /** نوع الوسائط المعياري لمصنفات إكسل OpenXML (.xlsx) */
    const val MIME_TYPE_EXCEL = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
    /** لاحقة مزود الملفات المعرف في حزمة التطبيق */
    private const val FILE_PROVIDER_SUFFIX = ".fileprovider"

    /**
     * [معالجة إجراء التقرير وتوجيهه - handleReportAction]:
     * يوجه ملف الإكسل المولد نحو القناة المطلوبة (حفظ، واتساب، أو مشاركة عامة).
     *
     * @param context سياق التطبيق.
     * @param file ملف الإكسل المولد في التخزين المؤقت.
     * @param action نوع الإجراء المطلوب (حفظ محلي، واتساب، مشاركة).
     * @param customer بيانات العميل المستهدف (إن وجدت).
     * @param shareSubject عنوان أو موضوع المشاركة المخصص.
     */
    fun handleReportAction(
        context: Context,
        file: File,
        action: CsvReportGenerator.CsvAction,
        customer: HabayebCustomer? = null,
        shareSubject: String? = null
    ) {
        when (action) {
            CsvReportGenerator.CsvAction.SAVE_LOCAL -> {
                LocalFileSaver.saveAndShowToast(
                    context = context,
                    cachedFile = file,
                    mimeType = MIME_TYPE_EXCEL,
                    displayName = file.name
                )
            }
            CsvReportGenerator.CsvAction.WHATSAPP_DIRECT -> {
                if (customer != null) {
                    CustomerShareHelper.triggerWhatsAppDirectFile(
                        context = context,
                        customer = customer,
                        file = file,
                        mimeType = MIME_TYPE_EXCEL
                    )
                } else {
                    triggerShareIntent(context, file, shareSubject ?: file.name)
                }
            }
            CsvReportGenerator.CsvAction.SHARE -> {
                triggerShareIntent(context, file, shareSubject ?: (customer?.name ?: file.name))
            }
        }
    }

    /**
     * [إطلاق نية المشاركة العامة للنظام - triggerShareIntent]:
     * ينشئ نية مشاركة موجهة ويفتح نافذة اختيار التطبيقات في أندرويد مع منح أذونات القراءة.
     *
     * @param context سياق التطبيق.
     * @param file الملف المطلوب مشاركته.
     * @param titleOrSubject موضوع أو اسم الحساب لمشاركته في نص الرسالة.
     */
    fun triggerShareIntent(context: Context, file: File, titleOrSubject: String) {
        try {
            val authority = "${context.packageName}$FILE_PROVIDER_SUFFIX"
            val uri = FileProvider.getUriForFile(context, authority, file)

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = MIME_TYPE_EXCEL
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.csv_share_subject, titleOrSubject))
                putExtra(Intent.EXTRA_TEXT, context.getString(R.string.csv_share_text, titleOrSubject))
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            context.startActivity(Intent.createChooser(shareIntent, context.getString(R.string.csv_share_chooser_title)))
            Toast.makeText(context, context.getString(R.string.habayeb_export_csv_success), Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            // معالجة الأخطاء ومنع عرض الاستثناءات البرمجية للمستخدم مع الاحتفاظ بالرسالة التفصيلية في السجلات
            Log.e(TAG, "Failed to share Excel statement", e)
            Toast.makeText(context, context.getString(R.string.toast_operation_failed), Toast.LENGTH_SHORT).show()
        }
    }
}

