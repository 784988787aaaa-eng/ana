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
import java.util.Locale

object SingleCustomerExcelEngine {

    private const val TAG = "SingleCustomerExcel"
    private const val LOCALE_AR = "ar"
    private const val LOCALE_EN = "en"
    private const val FILE_PREFIX = "statement_"

    private val DATE_FORMATTER_EN = ThreadLocal.withInitial { SimpleDateFormat("yyyy/MM/dd", Locale(LOCALE_EN)) }
    private val TIME_FORMATTER_AR = ThreadLocal.withInitial { SimpleDateFormat("hh:mm a", Locale(LOCALE_AR)) }
    private val DAY_FORMATTER_AR = ThreadLocal.withInitial { SimpleDateFormat("EEEE", Locale(LOCALE_AR)) }

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
            val dayName = try { DAY_FORMATTER_AR.get().format(now) } catch (e: Exception) { "" }
            val dateFormatted = try { DATE_FORMATTER_EN.get().format(now) } catch (e: Exception) { "" }
            val timeFormatted = try { TIME_FORMATTER_AR.get().format(now) } catch (e: Exception) { "" }
            val docDateText = "$dayName $dateFormatted"

            val isOwedToThemAccount = customer.initialType == TransactionType.OWED_TO_THEM.value
            val col4HeaderText = if (isOwedToThemAccount) context.getString(R.string.pdf_col_owed_to) else context.getString(R.string.pdf_col_owed_by)
            val col5HeaderText = if (isOwedToThemAccount) context.getString(R.string.pdf_col_paid) else context.getString(R.string.pdf_col_received)
            val accountTypeDesc = if (isOwedToThemAccount) context.getString(R.string.excel_type_supplier) else context.getString(R.string.excel_type_customer)

            val summary = PdfReportCalculator.calculateSingleCustomerReport(transactions, currencySymbol, exchangeRatesJson)

            val txSheetName = "الحركات"
            val summarySheetName = "الملخص"
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
                "الرصيد",
                "حالة التحويل",
                "عملة الأساس",
                "الأثر"
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
                XlsxOpenXmlBuilder.SheetColumn(10, 10, 17.0),
                XlsxOpenXmlBuilder.SheetColumn(11, 11, 18.0),
                XlsxOpenXmlBuilder.SheetColumn(12, 12, 16.0),
                XlsxOpenXmlBuilder.SheetColumn(13, 13, 14.0)
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
            txRows.add(XlsxOpenXmlBuilder.Row(4, 10))
            txRows.add(XlsxOpenXmlBuilder.Row(5, 28).apply {
                val phoneText = customer.phone.ifEmpty { context.getString(R.string.csv_not_registered) }
                cell(0, context.getString(R.string.excel_account_card_format, customer.name, phoneText, accountTypeDesc), 7)
            })
            txRows.add(XlsxOpenXmlBuilder.Row(6, 10))
            txRows.add(XlsxOpenXmlBuilder.Row(7, 30).apply { txHeaders.forEachIndexed { i, h -> cell(i, h, 1) } })

            val sortedTxs = summary.sortedProcessedTxs
            var txRow = 8
            sortedTxs.forEachIndexed { index, pt ->
                val tx = pt.tx
                val txType = TransactionType.fromValue(tx.type)
                val isCol4 = if (isOwedToThemAccount) {
                    txType == TransactionType.OWED_TO_THEM || txType == TransactionType.PAYMENT_BY_THEM
                } else {
                    txType == TransactionType.OWED_BY_THEM || txType == TransactionType.PAYMENT_TO_THEM
                }
                val originalCurrency = com.smartledger.aldaftar.ui.screens.habayeb.utils.CurrencyConfig.getBySymbol(pt.resolvedCurrency)
                val currencyCode = originalCurrency?.code ?: pt.resolvedCurrency
                val sourceAmount = if (tx.foreignAmount.compareTo(BigDecimal.ZERO) > 0) tx.foreignAmount else tx.amount
                val hasConversion = tx.isRateCalculated && tx.exchangeRate > BigDecimal.ZERO && tx.equivalentAmount > BigDecimal.ZERO
                val cleanDetails = CurrencyConfig.getCleanDetails(tx.description)
                val typeName = when (txType) {
                    TransactionType.OWED_BY_THEM -> context.getString(R.string.pdf_tx_type_owed_by_them)
                    TransactionType.PAYMENT_BY_THEM -> if (isOwedToThemAccount) context.getString(R.string.pdf_tx_type_payment_to_them) else context.getString(R.string.pdf_tx_type_payment_by_them)
                    TransactionType.OWED_TO_THEM -> context.getString(R.string.pdf_tx_type_owed_to_them)
                    TransactionType.PAYMENT_TO_THEM -> context.getString(R.string.pdf_tx_type_payment_to_them)
                    else -> context.getString(R.string.pdf_tx_type_new)
                }
                val descText = buildString {
                    append(typeName)
                    if (cleanDetails.isNotBlank()) append(" - ").append(cleanDetails)
                    if (hasConversion) append(" — ").append(HabayebMathHelper.formatSmart(sourceAmount)).append(" ").append(currencyCode).append(" × ").append(HabayebMathHelper.formatRate(tx.exchangeRate))
                }
                val date = Date(if (tx.timestamp > 1_000_000_000_000L) tx.timestamp else tx.timestamp * 1000)
                val dateText = "${DAY_FORMATTER_AR.get().format(date)} ${DATE_FORMATTER_EN.get().format(date)}"
                val baseAmount = pt.baseCurrencyAmount
                val hasBase = baseAmount.compareTo(BigDecimal.ZERO) > 0
                val row = XlsxOpenXmlBuilder.Row(txRow, 24)
                row.cell(0, index + 1, 6)
                row.cell(1, dateText, XlsxOpenXmlBuilder.STYLE_EDITABLE_TEXT)
                row.cell(2, descText, XlsxOpenXmlBuilder.STYLE_EDITABLE_TEXT)
                row.cell(3, currencyCode, 6)
                row.cell(4, sourceAmount, XlsxOpenXmlBuilder.STYLE_EDITABLE_NUMBER)
                row.cell(5, if (hasConversion) tx.exchangeRate else null, XlsxOpenXmlBuilder.STYLE_EDITABLE_RATE)
                row.cell(6, if (hasConversion) XlsxOpenXmlBuilder.Formula("IF(AND(E$txRow<>\"\",F$txRow>0),E$txRow*F$txRow,\"\")") else null, 4)
                row.cell(7, if (isCol4) XlsxOpenXmlBuilder.Formula("IF(D$txRow=L$txRow,E$txRow,IF(G$txRow>0,G$txRow,\"\"))") else null, 2)
                row.cell(8, if (!isCol4) XlsxOpenXmlBuilder.Formula("IF(D$txRow=L$txRow,E$txRow,IF(G$txRow>0,G$txRow,\"\"))") else null, 3)
                row.cell(9, XlsxOpenXmlBuilder.Formula("SUM(H$8:H$txRow)-SUM(I$8:I$txRow)"), 4)
                row.cell(10, if (hasConversion) "محسوب بسعر صرف تاريخي" else if (pt.isTxForeign) "عملة أجنبية — غير محوّل" else "العملة الأساسية", 6)
                row.cell(11, CurrencyConfig.getBySymbol(tx.baseCurrencyCode)?.code ?: tx.baseCurrencyCode.ifBlank { currencySymbol }, 6)
                row.cell(12, if (isCol4) "له" else "عليه", 6)
                txRows.add(row)
                txRow++
            }
            if (sortedTxs.isEmpty()) {
                txRows.add(XlsxOpenXmlBuilder.Row(txRow, 28).apply { cell(0, context.getString(R.string.pdf_no_transactions), 6) })
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
            txRows.add(XlsxOpenXmlBuilder.Row(txRow + 1, 12))
            txRows.add(XlsxOpenXmlBuilder.Row(txRow + 2, 24).apply {
                cell(0, context.getString(R.string.excel_footer_certified_icon, context.getString(R.string.pdf_footer_certified)), 17)
                cell(7, context.getString(R.string.excel_footer_signature), 16)
            })

            val summaryRows = mutableListOf<XlsxOpenXmlBuilder.Row>()
            summaryRows.add(XlsxOpenXmlBuilder.Row(1, 34).apply { cell(0, "ملخص تقرير الحساب", 15) })
            summaryRows.add(XlsxOpenXmlBuilder.Row(2, 22).apply { cell(0, bizHeader.displayedName, 16); cell(4, context.getString(R.string.excel_date_format, docDateText), 17) })
            summaryRows.add(XlsxOpenXmlBuilder.Row(3, 28).apply { cell(0, "اسم الحساب: ${customer.name}", 7) })
            summaryRows.add(XlsxOpenXmlBuilder.Row(4, 24).apply { cell(0, "العملة الأساسية", 1); cell(1, currencySymbol, 6); cell(2, "إجمالي له", 1); cell(3, XlsxOpenXmlBuilder.Formula("'الحركات'!H$txTotalsRow"), 12); cell(4, "إجمالي عليه", 1); cell(5, XlsxOpenXmlBuilder.Formula("'الحركات'!I$txTotalsRow"), 13) })
            summaryRows.add(XlsxOpenXmlBuilder.Row(5, 30).apply { cell(0, "الرصيد النهائي", 7); cell(1, XlsxOpenXmlBuilder.Formula("'الحركات'!J$txTotalsRow"), 10) })
            summaryRows.add(XlsxOpenXmlBuilder.Row(6, 14))
            summaryRows.add(XlsxOpenXmlBuilder.Row(7, 24).apply { cell(0, "طريقة الاستخدام", 7); cell(1, "عدّل المبالغ أو أسعار الصرف في ورقة الحركات؛ ستُعاد الحسابات تلقائيًا عند فتح الملف في Excel.", 0) })
            summaryRows.add(XlsxOpenXmlBuilder.Row(8, 24).apply { cell(0, "ملاحظة العملات", 7); cell(1, "العملات الأجنبية غير المحوّلة تبقى مستقلة ولا تدخل في رصيد العملة الأساسية.", 0) })

            val summaryColumns = listOf(
                XlsxOpenXmlBuilder.SheetColumn(1, 1, 20.0), XlsxOpenXmlBuilder.SheetColumn(2, 2, 28.0),
                XlsxOpenXmlBuilder.SheetColumn(3, 3, 18.0), XlsxOpenXmlBuilder.SheetColumn(4, 4, 20.0),
                XlsxOpenXmlBuilder.SheetColumn(5, 5, 18.0), XlsxOpenXmlBuilder.SheetColumn(6, 6, 18.0)
            )

            val currencyRows = mutableListOf<XlsxOpenXmlBuilder.Row>()
            val currencyHeaders = listOf("العملة", "الاسم", "إجمالي المبلغ الأصلي", "المعادل المحول", "ملاحظة")
            currencyRows.add(XlsxOpenXmlBuilder.Row(1, 34).apply { cell(0, "ملخص العملات", 15) })
            currencyRows.add(XlsxOpenXmlBuilder.Row(2, 28).apply { currencyHeaders.forEachIndexed { i, h -> cell(i, h, 1) } })
            val supportedCurrencies = CurrencyConfig.currencies
            supportedCurrencies.forEachIndexed { index, currency ->
                val rowNo = index + 3
                currencyRows.add(XlsxOpenXmlBuilder.Row(rowNo, 24).apply {
                    cell(0, currency.code, 6)
                    cell(1, currency.arabicName, 6)
                    cell(2, XlsxOpenXmlBuilder.Formula("SUMIFS('الحركات'!E$8:E$1048576,'الحركات'!D$8:D$1048576,\"${currency.code}\",'الحركات'!M$8:M$1048576,\"له\")-SUMIFS('الحركات'!E$8:E$1048576,'الحركات'!D$8:D$1048576,\"${currency.code}\",'الحركات'!M$8:M$1048576,\"عليه\")"), 4)
                    cell(3, XlsxOpenXmlBuilder.Formula("SUMIF('الحركات'!D$8:D$1048576,\"${currency.code}\",'الحركات'!G$8:G$1048576)"), 4)
                    cell(4, if (currency.symbol == currencySymbol) "العملة الأساسية" else "رصيد مستقل؛ التحويل يظهر فقط عند وجود سعر صرف", 6)
                })
            }

            XlsxOpenXmlBuilder.buildXlsxFile(
                workbook = XlsxOpenXmlBuilder.WorkbookSpec(
                    sheets = listOf(
                        XlsxOpenXmlBuilder.SheetSpec(
                            name = summarySheetName, columns = summaryColumns, rows = summaryRows,
                            merges = listOf(
                                XlsxOpenXmlBuilder.MergeRange("A1:F1"),
                                XlsxOpenXmlBuilder.MergeRange("A2:D2"),
                                XlsxOpenXmlBuilder.MergeRange("A3:F3"),
                                XlsxOpenXmlBuilder.MergeRange("B7:F7"),
                                XlsxOpenXmlBuilder.MergeRange("B8:F8")
                            ), freezeRows = 3,
                            protected = true
                        ),
                        XlsxOpenXmlBuilder.SheetSpec(
                            name = txSheetName, columns = txColumns, rows = txRows,
                            merges = listOf(
                                XlsxOpenXmlBuilder.MergeRange("A1:M1"),
                                XlsxOpenXmlBuilder.MergeRange("A2:C2"),
                                XlsxOpenXmlBuilder.MergeRange("D2:M2"),
                                XlsxOpenXmlBuilder.MergeRange("A3:C3"),
                                XlsxOpenXmlBuilder.MergeRange("D3:M3"),
                                XlsxOpenXmlBuilder.MergeRange("A5:M5"),
                                XlsxOpenXmlBuilder.MergeRange("A$txTotalsRow:G$txTotalsRow"),
                                XlsxOpenXmlBuilder.MergeRange("A${txRow + 2}:G${txRow + 2}"),
                                XlsxOpenXmlBuilder.MergeRange("H${txRow + 2}:M${txRow + 2}")
                            ),
                            freezeRows = 7,
                            autoFilterRef = "A7:M$txLastDataRow",
                            table = if (sortedTxs.isNotEmpty()) XlsxOpenXmlBuilder.TableSpec("TransactionsTable", "TransactionsTable", "A7:M$txLastDataRow", txHeaders) else null,
                            protected = true
                        ),
                        XlsxOpenXmlBuilder.SheetSpec(
                            name = "العملات",
                            columns = listOf(
                                XlsxOpenXmlBuilder.SheetColumn(1, 1, 14.0),
                                XlsxOpenXmlBuilder.SheetColumn(2, 2, 22.0),
                                XlsxOpenXmlBuilder.SheetColumn(3, 3, 22.0),
                                XlsxOpenXmlBuilder.SheetColumn(4, 4, 20.0),
                                XlsxOpenXmlBuilder.SheetColumn(5, 5, 40.0)
                            ),
                            rows = currencyRows,
                            merges = listOf(XlsxOpenXmlBuilder.MergeRange("A1:E1")),
                            freezeRows = 2,
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
}

