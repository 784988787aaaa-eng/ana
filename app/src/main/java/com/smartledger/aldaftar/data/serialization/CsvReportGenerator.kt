/**
 * واجهة تقارير الجداول: تنسق نقاط الدخول للتوليد والمشاركة دون تغيير المحركات الحالية.
 * المعالجة الثقيلة تبقى خارج الخيط الرئيسي، ولا تُسجل بيانات مالية أو أخطاء داخلية.
 * التوافق مع صيغ التصدير القائمة محفوظ، والقيم المالية تمر إلى المحركات دون تحويل عائم.
 */
package com.smartledger.aldaftar.data.serialization


import android.content.Context
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


/** واجهة موحدة لتوليد تقارير الجداول والتعامل مع نتائج المشاركة والحفظ. */
object CsvReportGenerator {
    
    private const val DEFAULT_EXCHANGE_RATES_JSON = "{}"
    
    const val MIME_TYPE_EXCEL = ExcelShareHelper.MIME_TYPE_EXCEL

    
    /** يحدد الإجراء النهائي على التقرير بعد اكتمال التوليد. */
    enum class CsvAction {
        
        SHARE,
        
        SAVE_LOCAL,
        
        WHATSAPP_DIRECT;

        companion object {
            
            /** يحول القيمة النصية إلى إجراء معروف مع اختيار المشاركة عند الغموض. */
            fun from(action: String): CsvAction {
                return values().find { it.name.equals(action, ignoreCase = true) } ?: SHARE
            }
        }
    }

    
    /** أدوات صغيرة تحفظ توافق مراجع الخلايا ونطاقات الدمج مع المحرك الحالي. */
    object XlsxHelper {
        class SheetColumn(val min: Int, val max: Int, val width: Double)
        class MergeRange(val ref: String)

        
        fun getCellRef(colIndex: Int, rowIndex: Int): String =
            XlsxOpenXmlBuilder.getCellRef(colIndex, rowIndex)
    }

    
    /** يبدأ توليد تقرير العميل خارج الخيط الرئيسي ثم يمرره إلى مسار المشاركة الحالي. */
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

    
    /** ينفذ توليد تقرير العميل في الخلفية ويعيد التحكم للواجهة بعد انتهاء العملية. */
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
            } finally {
                withContext(Dispatchers.Main) {
                    onFinished()
                }
            }
        }
    }

    
    /** ينفذ توليد التقرير الشامل في الخلفية ويحافظ على مسار المشاركة القائم. */
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
            } finally {
                withContext(Dispatchers.Main) {
                    onFinished()
                }
            }
        }
    }
}

