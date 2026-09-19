package com.smartledger.aldaftar.data.serialization.excel

import android.content.Context
import android.util.Log
import com.smartledger.aldaftar.R
import com.smartledger.aldaftar.data.local.entities.BusinessProfile
import com.smartledger.aldaftar.data.local.entities.HabayebCustomer
import com.smartledger.aldaftar.data.local.entities.HabayebTransaction
import com.smartledger.aldaftar.data.serialization.pdf.BusinessProfileLoader
import com.smartledger.aldaftar.data.serialization.pdf.PdfReportCalculator
import com.smartledger.aldaftar.domain.model.TransactionType
import com.smartledger.aldaftar.ui.helper.HabayebMathHelper
import com.smartledger.aldaftar.ui.screens.habayeb.utils.CurrencyConfig
import java.io.File
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.Date
import java.util.LinkedHashMap
import java.util.LinkedHashSet
import java.util.Locale

/**
 * مولّد كشف الحساب بصيغة XLSX.
 *
 * التصميم المعتمد في هذه النسخة:
 * - ورقة واحدة فقط باسم «الحركات».
 * - إزالة ورقة «الملخص» المنفصلة حتى لا تتكرر المعلومات.
 * - إزالة الأعمدة المشتقة «حالة التحويل» و«عملة الأساس» و«الأثر» نهائياً.
 * - إبقاء «العملة» و«المبلغ الأصلي» و«سعر الصرف» و«المعادل بالعملة الأساسية» و«له/عليه/الرصيد».
 * - وضع «ملخص العملات» أسفل جدول الحركات في الورقة نفسها.
 * - استخدام صيغ Excel مرتبطة بصفوف الحركات الفعلية، بدلاً من نطاق الصفوف الكامل 1,048,576،
 *   حتى يبقى الملف أخف وأوضح ولا تتضخم الصيغ بلا داعٍ.
 * - عدم جمع أرصدة العملات المختلفة في رقم واحد؛ لكل عملة صافي مستقل،
 *   والمعادل المحول يظهر فقط عندما يكون التحويل متاحاً.
 */
object SingleCustomerExcelEngine {

    private const val TAG = "SingleCustomerExcel"
    private const val LOCALE_AR = "ar"
    private const val LOCALE_EN = "en"
    private const val FILE_PREFIX = "statement_"

    private val DATE_FORMATTER_EN = ThreadLocal.withInitial { SimpleDateFormat("yyyy/MM/dd", Locale(LOCALE_EN)) }
    private val DAY_FORMATTER_AR = ThreadLocal.withInitial { SimpleDateFormat("EEEE", Locale(LOCALE_AR)) }

    /**
     * ينشئ كشف الحساب اعتماداً على آخر بيانات التطبيق كما هي، مع الحفاظ على قابلية
     * تعديل المبلغ وسعر الصرف داخل Excel وإعادة الحساب تلقائياً عند فتح الملف.
     */
    fun generate(
        context: Context,
        customer: HabayebCustomer,
        transactions: List<HabayebTransaction>,
        businessProfile: BusinessProfile,
        currencySymbol: String,
        exchangeRatesJson: String = "{}"
    ): File? {
        val sanitizedName = customer.name.replace("[\\\\/:*?\"<>|]".toRegex(), "_")
        val fileName = "${FILE_PREFIX}${sanitizedName}_${System.currentTimeMillis() % 100000}.xlsx"
        val file = File(context.cacheDir, fileName)

        try {
            val bizHeader = BusinessProfileLoader.load(context, businessProfile)
            val now = Date()
            val dayName = try { DAY_FORMATTER_AR.get().format(now) } catch (_: Exception) { "" }
            val dateFormatted = try { DATE_FORMATTER_EN.get().format(now) } catch (_: Exception) { "" }
            val docDateText = "$dayName $dateFormatted"

            val isOwedToThemAccount = customer.initialType == TransactionType.OWED_TO_THEM.value
            val accountTypeDesc = if (isOwedToThemAccount) {
                context.getString(R.string.excel_type_supplier)
            } else {
                context.getString(R.string.excel_type_customer)
            }

            val summary = PdfReportCalculator.calculateSingleCustomerReport(
                transactions,
                currencySymbol,
                exchangeRatesJson
            )

            val txSheetName = "الحركات"

            // الأعمدة التي بقيت في كشف الحركات بعد التنظيف النهائي.
            val txHeaders = listOf(
                context.getString(R.string.excel_col_seq),
                context.getString(R.string.pdf_col_date),
                context.getString(R.string.pdf_col_description),
                "العملة",
                "المبلغ الأصلي",
                "سعر الصرف",
                "المعادل بالعملة الأساسية",
                "له",
                "عليه",
                "الرصيد"
            )
            val txColumns = listOf(
                XlsxOpenXmlBuilder.SheetColumn(1, 1, 7.0),
                XlsxOpenXmlBuilder.SheetColumn(2, 2, 16.0),
                XlsxOpenXmlBuilder.SheetColumn(3, 3, 42.0),
                XlsxOpenXmlBuilder.SheetColumn(4, 4, 13.0),
                XlsxOpenXmlBuilder.SheetColumn(5, 5, 17.0),
                XlsxOpenXmlBuilder.SheetColumn(6, 6, 16.0),
                XlsxOpenXmlBuilder.SheetColumn(7, 7, 21.0),
                XlsxOpenXmlBuilder.SheetColumn(8, 8, 17.0),
                XlsxOpenXmlBuilder.SheetColumn(9, 9, 17.0),
                XlsxOpenXmlBuilder.SheetColumn(10, 10, 17.0)
            )

            val txRows = mutableListOf<XlsxOpenXmlBuilder.Row>()
            txRows.add(XlsxOpenXmlBuilder.Row(1, 32).apply {
                cell(0, context.getString(R.string.excel_single_title), 15)
            })
            txRows.add(XlsxOpenXmlBuilder.Row(2, 22).apply {
                cell(0, bizHeader.displayedName + " - " + bizHeader.displayedDesc, 16)
                cell(3, context.getString(R.string.excel_date_format, docDateText), 17)
            })
            txRows.add(XlsxOpenXmlBuilder.Row(3, 22).apply {
                cell(0, context.getString(R.string.excel_phone_format, bizHeader.phonesStr), 16)
                cell(3, "", 17)
            })
            txRows.add(XlsxOpenXmlBuilder.Row(4, 6))
            txRows.add(XlsxOpenXmlBuilder.Row(5, 28).apply {
                val phoneText = customer.phone.ifEmpty { context.getString(R.string.csv_not_registered) }
                cell(
                    0,
                    context.getString(
                        R.string.excel_account_card_format,
                        customer.name,
                        phoneText,
                        accountTypeDesc
                    ),
                    7
                )
            })
            txRows.add(XlsxOpenXmlBuilder.Row(6, 6))
            txRows.add(XlsxOpenXmlBuilder.Row(7, 30).apply {
                txHeaders.forEachIndexed { i, h -> cell(i, h, XlsxOpenXmlBuilder.STYLE_HEADER) }
            })

            val sortedTxs = summary.sortedProcessedTxs
            var txRow = 8

            // نحتفظ فقط بأرقام الصفوف، لا ببيانات إضافية داخل الملف.
            // هذه الخرائط تُستخدم لصناعة ملخص العملات بصيغ Excel ديناميكية دون إعادة إضافة
            // عمود «الأثر» الذي تم حذفه نهائياً من جدول الحركات.
            val currencyCodesInOrder = LinkedHashSet<String>()
            val owedRowsByCurrency = LinkedHashMap<String, MutableList<Int>>()
            val dueRowsByCurrency = LinkedHashMap<String, MutableList<Int>>()

            val baseCurrencyCode = CurrencyConfig.getBySymbol(currencySymbol)?.code ?: currencySymbol

            sortedTxs.forEachIndexed { index, pt ->
                val tx = pt.tx
                val txType = TransactionType.fromValue(tx.type)
                val isCol4 = if (isOwedToThemAccount) {
                    txType == TransactionType.OWED_TO_THEM || txType == TransactionType.PAYMENT_BY_THEM
                } else {
                    txType == TransactionType.OWED_BY_THEM || txType == TransactionType.PAYMENT_TO_THEM
                }

                val originalCurrency = CurrencyConfig.getBySymbol(pt.resolvedCurrency)
                val currencyCode = originalCurrency?.code ?: pt.resolvedCurrency
                currencyCodesInOrder.add(currencyCode)

                val sourceAmount = if (tx.foreignAmount.compareTo(BigDecimal.ZERO) > 0) {
                    tx.foreignAmount
                } else {
                    tx.amount
                }
                val hasConversion = tx.isRateCalculated &&
                    tx.exchangeRate > BigDecimal.ZERO &&
                    tx.equivalentAmount > BigDecimal.ZERO
                val cleanDetails = CurrencyConfig.getCleanDetails(tx.description)

                val typeName = when (txType) {
                    TransactionType.OWED_BY_THEM -> context.getString(R.string.pdf_tx_type_owed_by_them)
                    TransactionType.PAYMENT_BY_THEM -> if (isOwedToThemAccount) {
                        context.getString(R.string.pdf_tx_type_payment_to_them)
                    } else {
                        context.getString(R.string.pdf_tx_type_payment_by_them)
                    }
                    TransactionType.OWED_TO_THEM -> context.getString(R.string.pdf_tx_type_owed_to_them)
                    TransactionType.PAYMENT_TO_THEM -> context.getString(R.string.pdf_tx_type_payment_to_them)
                    else -> context.getString(R.string.pdf_tx_type_new)
                }

                val descText = buildString {
                    append(typeName)
                    if (cleanDetails.isNotBlank()) append(" - ").append(cleanDetails)
                    if (hasConversion) {
                        append(" — ")
                            .append(HabayebMathHelper.formatSmart(sourceAmount))
                            .append(" ")
                            .append(currencyCode)
                            .append(" × ")
                            .append(HabayebMathHelper.formatRate(tx.exchangeRate))
                    }
                }

                val date = Date(if (tx.timestamp > 1_000_000_000_000L) tx.timestamp else tx.timestamp * 1000)
                val dateText = "${DAY_FORMATTER_AR.get().format(date)} ${DATE_FORMATTER_EN.get().format(date)}"
                val row = XlsxOpenXmlBuilder.Row(txRow, 24)

                row.cell(0, index + 1, 6)
                row.cell(1, dateText, XlsxOpenXmlBuilder.STYLE_EDITABLE_TEXT)
                row.cell(2, descText, XlsxOpenXmlBuilder.STYLE_EDITABLE_TEXT)
                row.cell(3, currencyCode, 6)
                row.cell(4, sourceAmount, XlsxOpenXmlBuilder.STYLE_EDITABLE_NUMBER)
                row.cell(
                    5,
                    if (hasConversion) tx.exchangeRate else null,
                    XlsxOpenXmlBuilder.STYLE_EDITABLE_RATE
                )
                row.cell(
                    6,
                    if (hasConversion) {
                        XlsxOpenXmlBuilder.Formula(
                            "IF(AND(E$txRow<>\"\",F$txRow>0),E$txRow*F$txRow,\"\")"
                        )
                    } else {
                        null
                    },
                    XlsxOpenXmlBuilder.STYLE_FORMULA_NUMBER
                )

                // بعد حذف «عملة الأساس»، نستخدم كود العملة الأساسي داخل الصيغة نفسها.
                // لا نعيد إنشاء عمود مساعد مخفي، وبذلك تبقى الورقة نظيفة فعلياً.
                val directionFormula =
                    "IF(D$txRow=\"$baseCurrencyCode\",E$txRow,IF(G$txRow>0,G$txRow,\"\"))"
                row.cell(
                    7,
                    if (isCol4) XlsxOpenXmlBuilder.Formula(directionFormula) else null,
                    2
                )
                row.cell(
                    8,
                    if (!isCol4) XlsxOpenXmlBuilder.Formula(directionFormula) else null,
                    3
                )
                row.cell(
                    9,
                    XlsxOpenXmlBuilder.Formula("SUM(H$8:H$txRow)-SUM(I$8:I$txRow)"),
                    XlsxOpenXmlBuilder.STYLE_FORMULA_NUMBER
                )

                if (isCol4) {
                    owedRowsByCurrency.getOrPut(currencyCode) { mutableListOf() }.add(txRow)
                } else {
                    dueRowsByCurrency.getOrPut(currencyCode) { mutableListOf() }.add(txRow)
                }

                txRows.add(row)
                txRow++
            }

            if (sortedTxs.isEmpty()) {
                txRows.add(XlsxOpenXmlBuilder.Row(txRow, 28).apply {
                    cell(0, context.getString(R.string.pdf_no_transactions), 6)
                })
                txRow++
            }

            val txLastDataRow = (txRow - 1).coerceAtLeast(8)
            txRows.add(XlsxOpenXmlBuilder.Row(txRow, 28).apply {
                cell(0, "الإجماليات", 11)
                cell(7, XlsxOpenXmlBuilder.Formula("SUM(H8:H$txLastDataRow)"), 12)
                cell(8, XlsxOpenXmlBuilder.Formula("SUM(I8:I$txLastDataRow)"), 13)
                cell(9, XlsxOpenXmlBuilder.Formula("H$txLastDataRow-I$txLastDataRow"), 14)
            })
            val txTotalsRow = txRow

            // -----------------------------
            // ملخص العملات — داخل ورقة الحركات نفسها
            // -----------------------------
            // لا نجمع العملات المختلفة في إجمالي واحد، لأن ذلك سيخلط وحدات نقدية مختلفة.
            // «له/عليه/الصافي» هنا بالمبلغ الأصلي لكل عملة، بينما «المعادل المحول»
            // يعتمد على عمودي «له/عليه» في جدول الحركات عندما يتوفر التحويل.
            val summaryTitleRow = txTotalsRow + 2
            val summaryHeaderRow = summaryTitleRow + 1
            val summaryDataStartRow = summaryHeaderRow + 1

            // العملة الأساسية تظهر دائماً حتى لو لم توجد أي حركة بعد.
            currencyCodesInOrder.remove(baseCurrencyCode)
            val summaryCurrencyCodes = mutableListOf<String>().apply {
                add(baseCurrencyCode)
                addAll(currencyCodesInOrder)
            }

            val currencyHeaders = listOf(
                "العملة",
                "اسم العملة",
                "له",
                "عليه",
                "الصافي",
                "المعادل المحول",
                "عدد الحركات"
            )

            txRows.add(XlsxOpenXmlBuilder.Row(summaryTitleRow, 30).apply {
                cell(0, "ملخص العملات", 7)
            })
            txRows.add(XlsxOpenXmlBuilder.Row(summaryHeaderRow, 28).apply {
                currencyHeaders.forEachIndexed { i, h -> cell(i, h, XlsxOpenXmlBuilder.STYLE_HEADER) }
            })

            summaryCurrencyCodes.forEachIndexed { index, code ->
                val rowNo = summaryDataStartRow + index
                val currency = CurrencyConfig.getByCode(code) ?: CurrencyConfig.getBySymbol(code)
                val currencyName = currency?.arabicName ?: code
                val owedRows = owedRowsByCurrency[code].orEmpty()
                val dueRows = dueRowsByCurrency[code].orEmpty()

                val owedFormula = sumCellRefsFormula(owedRows, "E")
                val dueFormula = sumCellRefsFormula(dueRows, "E")
                val netFormula = "C$rowNo-D$rowNo"
                val convertedFormula = if (code == baseCurrencyCode) {
                    "E$rowNo"
                } else {
                    "IF(COUNTIFS(D$8:D$txLastDataRow,\"$code\",G$8:G$txLastDataRow,\">0\")=0,\"\",SUMIFS(H$8:H$txLastDataRow,D$8:D$txLastDataRow,\"$code\")-SUMIFS(I$8:I$txLastDataRow,D$8:D$txLastDataRow,\"$code\"))"
                }
                val countFormula = "COUNTIF(D$8:D$txLastDataRow,\"$code\")"

                txRows.add(XlsxOpenXmlBuilder.Row(rowNo, 24).apply {
                    cell(0, code, 6)
                    cell(1, currencyName, 6)
                    cell(2, XlsxOpenXmlBuilder.Formula(owedFormula), 4)
                    cell(3, XlsxOpenXmlBuilder.Formula(dueFormula), 4)
                    cell(4, XlsxOpenXmlBuilder.Formula(netFormula), 10)
                    cell(5, XlsxOpenXmlBuilder.Formula(convertedFormula), 4)
                    cell(6, XlsxOpenXmlBuilder.Formula(countFormula), 6)
                })
            }

            val summaryDataLastRow = summaryDataStartRow + summaryCurrencyCodes.lastIndex
            val summaryNoteRow = summaryDataLastRow + 2
            txRows.add(XlsxOpenXmlBuilder.Row(summaryNoteRow, 30).apply {
                cell(
                    0,
                    "ملاحظة: لا تُجمع أرصدة العملات المختلفة معاً. المعادل المحول يظهر فقط للحركات التي تحتوي على تحويل صالح، وتبقى العملات غير المحولة مستقلة.",
                    17
                )
            })

            // اعتماد التقرير يأتي في نهاية المحتوى، بعد الحركات وملخص العملات.
            val footerRow = summaryNoteRow + 2
            txRows.add(XlsxOpenXmlBuilder.Row(footerRow, 24).apply {
                cell(
                    0,
                    context.getString(
                        R.string.excel_footer_certified_icon,
                        context.getString(R.string.pdf_footer_certified)
                    ),
                    17
                )
                cell(7, context.getString(R.string.excel_footer_signature), 16)
            })

            val merges = listOf(
                XlsxOpenXmlBuilder.MergeRange("A1:J1"),
                XlsxOpenXmlBuilder.MergeRange("A2:C2"),
                XlsxOpenXmlBuilder.MergeRange("D2:J2"),
                XlsxOpenXmlBuilder.MergeRange("A3:C3"),
                XlsxOpenXmlBuilder.MergeRange("D3:J3"),
                XlsxOpenXmlBuilder.MergeRange("A5:J5"),
                XlsxOpenXmlBuilder.MergeRange("A$txTotalsRow:G$txTotalsRow"),
                XlsxOpenXmlBuilder.MergeRange("A$summaryTitleRow:G$summaryTitleRow"),
                XlsxOpenXmlBuilder.MergeRange("A$summaryNoteRow:G$summaryNoteRow"),
                XlsxOpenXmlBuilder.MergeRange("A$footerRow:G$footerRow"),
                XlsxOpenXmlBuilder.MergeRange("H$footerRow:J$footerRow")
            )

            XlsxOpenXmlBuilder.buildXlsxFile(
                workbook = XlsxOpenXmlBuilder.WorkbookSpec(
                    // الورقة الوحيدة المقصودة في هذا التصدير هي «الحركات».
                    sheets = listOf(
                        XlsxOpenXmlBuilder.SheetSpec(
                            name = txSheetName,
                            columns = txColumns,
                            rows = txRows,
                            merges = merges,
                            freezeRows = 7,
                            autoFilterRef = "A7:J$txLastDataRow",
                            table = if (sortedTxs.isNotEmpty()) {
                                XlsxOpenXmlBuilder.TableSpec(
                                    "TransactionsTable",
                                    "TransactionsTable",
                                    "A7:J$txLastDataRow",
                                    txHeaders
                                )
                            } else {
                                null
                            },
                            protected = true
                        )
                    )
                ),
                file = file
            )
            return file
        } catch (e: Exception) {
            Log.e(TAG, "Error writing XLSX statement file", e)
            return null
        }
    }

    /**
     * يبني SUM من مراجع الخلايا الفعلية للحركات.
     * استخدام مراجع الصفوف يمنع الحاجة إلى عمود «الأثر» المحذوف،
     * وفي الوقت نفسه يبقي الملخص قابلاً لإعادة الحساب إذا عدّل المستخدم المبالغ في Excel.
     */
    private fun sumCellRefsFormula(rows: List<Int>, column: String): String {
        if (rows.isEmpty()) return "0"
        return "SUM(${rows.joinToString(",") { "$column$it" }})"
    }
}
