/**
 * =====================================================================
 * ملف: واجهة توليد تقارير إكسل وجداول البيانات (CsvReportGenerator.kt)
 * =====================================================================
 * 
 * [الغرض العام والتعليمي من الملف]:
 * يمثل هذا الكائن واجهة أمامية عالية المستوى (Facade Pattern) لإنشاء وتصدير
 * ومشاركة كشوفات الحسابات وجداول الديون بصيغة جداول مايكروسوفت إكسل المفتوحة (.xlsx)
 * المتوافقة مع مواصفات OpenXML. تتميز التقارير بالاتجاه العربي الأصيل (RTL)،
 * والتنسيق اللوني للخلايا الدائنة والمدينة، والحقول الرقمية الحقيقية القابلة للجمع التلقائي.
 * 
 * [المسؤوليات المعمارية والتقنية]:
 * 1. تفويض بناء ملفات إكسل للعميل الفردي إلى [SingleCustomerExcelEngine].
 * 2. تفويض بناء الدليل الشامل لجميع العملاء إلى [AllCustomersExcelEngine].
 * 3. إدارة عمليات المشاركة، الحفظ المحلي، أو الإرسال المباشر عبر واتساب عبر [ExcelShareHelper].
 * 4. إدارة المهام اللاتزامنية عبر Coroutines وتوجيه نتائج العرض إلى الخيط الرئيسي (Main Thread).
 */
package com.smartledger.aldaftar.data.serialization

// ---------------------------------------------------------------------
// استيراد حزم سياق أندرويد والرسائل ونماذج البيانات ومحركات إكسل وتزامن كوتلن
// ---------------------------------------------------------------------
import android.content.Context
import android.util.Log
import android.widget.Toast
import com.smartledger.aldaftar.R
import com.smartledger.aldaftar.data.local.entities.HabayebCustomer
import com.smartledger.aldaftar.data.local.entities.HabayebTransaction
import com.smartledger.aldaftar.data.serialization.excel.AllCustomersExcelEngine
import com.smartledger.aldaftar.data.serialization.excel.ExcelShareHelper
import com.smartledger.aldaftar.data.serialization.excel.SingleCustomerExcelEngine
import com.smartledger.aldaftar.data.serialization.excel.XlsxOpenXmlBuilder
import com.smartledger.aldaftar.ui.state.CustomerUiState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * [الكائن الأحادي لواجهة تقارير إكسل - CsvReportGenerator]:
 * يوفر نقاط دخول مبسطة لإنشاء وتداول ملفات إكسل لكشوف الحسابات.
 */
object CsvReportGenerator {

    /** وسم السجلات التشخيصية */
    private const val TAG = "CsvReportGenerator"
    /** أسعار الصرف الافتراضية */
    private const val DEFAULT_EXCHANGE_RATES_JSON = "{}"
    /** نوع الوسائط المعياري لملفات إكسل */
    const val MIME_TYPE_EXCEL = ExcelShareHelper.MIME_TYPE_EXCEL

    /**
     * [تعداد إجراءات التقرير - CsvAction]:
     * يحدد الغرض من توليد ملف التقرير.
     */
    enum class CsvAction {
        /** إظهار قائمة المشاركة العامة للنظام */
        SHARE,
        /** الحفظ في مجلد التنزيلات بالجهاز */
        SAVE_LOCAL,
        /** الإرسال الفوري والمباشر عبر تطبيق واتساب */
        WHATSAPP_DIRECT;

        companion object {
            /** استخراج الإجراء من النص مع افتراض المشاركة كخيار افتراضي */
            fun from(action: String): CsvAction {
                return values().find { it.name.equals(action, ignoreCase = true) } ?: SHARE
            }
        }
    }

    /**
     * [كائن مساعد التوافق مع بناء OpenXML - XlsxHelper]:
     * يوفر صياغات مساعدة لأبعاد الأعمدة ودمج الخلايا.
     */
    object XlsxHelper {
        class SheetColumn(val min: Int, val max: Int, val width: Double)
        class MergeRange(val ref: String)

        /** استخراج مرجع الخلية بالحروف والأرقام (مثل A1 أو C5) */
        fun getCellRef(colIndex: Int, rowIndex: Int): String =
            XlsxOpenXmlBuilder.getCellRef(colIndex, rowIndex)
    }

    /**
     * [توليد ومشاركة تقرير كشف حساب العميل - generateAndShareCsvReport]:
     * دالة ملائمة لإنشاء تقرير إكسل ومشاركته مباشرة عبر قائمة مشاركة النظام.
     *
     * @param context سياق التطبيق.
     * @param scope نطاق الكوروتين لتشغيل المعالجة في الخلفية.
     * @param customer بيانات العميل المستهدف.
     * @param transactions قائمة معاملات العميل.
     * @param currencySymbol رمز العملة المحلية.
     * @param exchangeRatesJson نص أسعار الصرف بالعملات الأجنبية.
     * @param onFinished رد نداء يتم استدعاؤه عند انتهاء العملية.
     */
    fun generateAndShareCsvReport(
        context: Context,
        scope: CoroutineScope,
        customer: HabayebCustomer,
        transactions: List<HabayebTransaction>,
        currencySymbol: String,
        exchangeRatesJson: String = DEFAULT_EXCHANGE_RATES_JSON,
        onFinished: () -> Unit = {}
    ) {
        generateAndHandleCsvReportAsync(
            context = context,
            scope = scope,
            customer = customer,
            transactions = transactions,
            currencySymbol = currencySymbol,
            exchangeRatesJson = exchangeRatesJson,
            action = CsvAction.SHARE,
            onFinished = onFinished
        )
    }

    /**
     * [توليد ومعالجة تقرير العميل اللاتزامني - generateAndHandleCsvReportAsync]:
     * يبني كشف حساب إكسل للعميل في خيوط IO، ثم ينفذ الإجراء المطلوب (مشاركة/حفظ/واتساب).
     *
     * @param context سياق التطبيق.
     * @param scope نطاق تشغيل الكوروتين.
     * @param customer بيانات العميل.
     * @param transactions قائمة المعاملات.
     * @param currencySymbol رمز العملة.
     * @param exchangeRatesJson أسعار الصرف.
     * @param action الإجراء المطلوب تنفيذه على الملف المولد.
     * @param onFinished رد نداء عند الانتهاء.
     */
    fun generateAndHandleCsvReportAsync(
        context: Context,
        scope: CoroutineScope,
        customer: HabayebCustomer,
        transactions: List<HabayebTransaction>,
        currencySymbol: String,
        exchangeRatesJson: String = DEFAULT_EXCHANGE_RATES_JSON,
        action: CsvAction,
        onFinished: () -> Unit = {}
    ) {
        scope.launch(Dispatchers.IO) {
            try {
                val file = SingleCustomerExcelEngine.generate(
                    context = context,
                    customer = customer,
                    transactions = transactions,
                    currencySymbol = currencySymbol,
                    exchangeRatesJson = exchangeRatesJson
                )
                withContext(Dispatchers.Main) {
                    if (file != null) {
                        ExcelShareHelper.handleReportAction(
                            context = context,
                            file = file,
                            action = action,
                            customer = customer
                        )
                    } else {
                        Toast.makeText(
                            context,
                            context.getString(R.string.habayeb_export_csv_failed, context.getString(R.string.csv_error_creating_file)),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error generating Excel statement", e)
            } finally {
                withContext(Dispatchers.Main) {
                    onFinished()
                }
            }
        }
    }

    /**
     * [توليد ومعالجة تقرير كافة العملاء اللاتزامني - generateAndHandleAllCustomersExcelReportAsync]:
     * يبني جدول إكسل شامل يتضمن أرصدة وحالات جميع العملاء في المنظومة.
     *
     * @param context سياق التطبيق.
     * @param scope نطاق تشغيل الكوروتين.
     * @param customers قائمة حالات واجهة المستخدم لكافة العملاء.
     * @param currencySymbol رمز العملة الرئيسية.
     * @param action الإجراء المستهدف بعد التوليد (افتراضياً: مشاركة).
     * @param onFinished رد نداء عند الانتهاء.
     */
    fun generateAndHandleAllCustomersExcelReportAsync(
        context: Context,
        scope: CoroutineScope,
        customers: List<CustomerUiState>,
        currencySymbol: String,
        action: CsvAction = CsvAction.SHARE,
        onFinished: () -> Unit = {}
    ) {
        scope.launch(Dispatchers.IO) {
            try {
                val file = AllCustomersExcelEngine.generate(
                    context = context,
                    customers = customers,
                    currencySymbol = currencySymbol
                )
                withContext(Dispatchers.Main) {
                    if (file != null) {
                        ExcelShareHelper.handleReportAction(
                            context = context,
                            file = file,
                            action = action,
                            shareSubject = context.getString(R.string.pdf_comprehensive_report_title)
                        )
                    } else {
                        Toast.makeText(
                            context,
                            context.getString(R.string.habayeb_export_csv_failed, context.getString(R.string.csv_error_creating_file)),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error generating All Customers Excel", e)
            } finally {
                withContext(Dispatchers.Main) {
                    onFinished()
                }
            }
        }
    }
}

