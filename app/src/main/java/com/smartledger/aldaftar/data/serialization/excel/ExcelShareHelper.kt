/**
 * =====================================================================
 * ملف: مساعد وموجه مشاركة ملفات إكسل (.)
 * =====================================================================
 * 
 * [الغرض العام والتعليمي من الملف]:
 * يقدم هذا الكائن واجهة مساعدة مركزية لإدارة العمليات اللاحقة لتوليد ملفات إكسل
 * (.)، مثل الحفظ في الذاكرة التخزينية للهاتف، أو الإرسال الفوري لجهة الاتصال
 * عبر واتساب، أو فتح نافذة مشاركة النظام العامة (  ).
 * 
 * [المسؤوليات المعمارية والتقنية]:
 * 1. حماية وتأمين مسارات الملفات عبر []:
 *    - توليد روابط موثوقة ( ) ومنح إذن القراءة المؤقت [____].
 * 2. تحديد نوع الوسائط الدقيق لمصنفات إكسل الحديثة:
 *    - استخدام `/.-..` لضمان تعرف التطبيقات المستقبلة عليه.
 * 3. التكامل المرن مع قنوات التوزيع:
 *    - التوجيه الذكي إما للحفظ المحلي عبر [] أو الإرسال المباشر لواتساب عبر [].
 * 4. إدارة تجربة المستخدم والأخطاء:
 *    - عرض إشعارات نجاح أو فشل موحدة، وتسجيل الاستثناءات في سجلات التطبيق التشخيصية.
 */
package com.smartledger.aldaftar.data.serialization.excel

// ---------------------------------------------------------------------
// استيراد حزم سياق أندرويد والنوايا ومزود الملفات والكيانات والمساعدات
// ---------------------------------------------------------------------
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.content.FileProvider
import com.smartledger.aldaftar.R
import com.smartledger.aldaftar.data.local.entities.HabayebCustomer
import com.smartledger.aldaftar.data.serialization.CsvReportGenerator
import com.smartledger.aldaftar.ui.helper.LocalFileSaver
import com.smartledger.aldaftar.ui.screens.habayeb.utils.CustomerShareHelper
import java.io.File

/**
 * [الكائن الأحادي لمساعد مشاركة ملفات إكسل - ]:
 * يدير خيارات الحفظ والمشاركة والإرسال لتقارير إكسل المولدة.
 */
object ExcelShareHelper {

    /** وسم السجلات التشخيصية */
    /** نوع الوسائط المعياري لمصنفات إكسل  (.) */
    const val MIME_TYPE_EXCEL = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
    /** لاحقة مزود الملفات المعرف في حزمة التطبيق */
    private const val FILE_PROVIDER_SUFFIX = ".fileprovider"

    /**
     * [معالجة إجراء التقرير وتوجيهه - ]:
     * يوجه ملف الإكسل المولد نحو القناة المطلوبة (حفظ، واتساب، أو مشاركة عامة).
     *
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
     * [إطلاق نية المشاركة العامة للنظام - ]:
     * ينشئ نية مشاركة موجهة ويفتح نافذة اختيار التطبيقات في أندرويد مع منح أذونات القراءة.
     *
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
            // معالجة الفشل داخلياً دون تسجيل تفاصيل التنفيذ أو الاستثناءات الحساسة.
            Toast.makeText(context, context.getString(R.string.toast_operation_failed), Toast.LENGTH_SHORT).show()
        }
    }
}

