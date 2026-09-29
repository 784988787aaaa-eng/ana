package com.smartledger.aldaftar.data.serialization.excel

import android.content.Context
import android.util.Log
import com.smartledger.aldaftar.R
import com.smartledger.aldaftar.data.local.entities.BusinessProfile
import com.smartledger.aldaftar.data.serialization.pdf.BusinessProfileLoader
import com.smartledger.aldaftar.ui.helper.HabayebMathHelper
import com.smartledger.aldaftar.ui.state.CustomerUiState
import java.io.File
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object AllCustomersExcelEngine {

    private const val TAG = "AllCustomersExcel"
    private const val LOCALE_AR = "ar"
    private const val LOCALE_EN = "en"
    private const val FILE_PREFIX_ALL = "all_accounts_"

    private val DATE_FORMATTER_EN = ThreadLocal.withInitial { SimpleDateFormat("yyyy/MM/dd", Locale(LOCALE_EN)) }
    private val TIME_FORMATTER_AR = ThreadLocal.withInitial { SimpleDateFormat("hh:mm a", Locale(LOCALE_AR)) }
    private val DAY_FORMATTER_AR = ThreadLocal.withInitial { SimpleDateFormat("EEEE", Locale(LOCALE_AR)) }

    fun generate(
        context: Context,
        customers: List<CustomerUiState>,
        businessProfile: BusinessProfile,
        currencySymbol: String
    ): File? {
        val fileName = "${FILE_PREFIX_ALL}${System.currentTimeMillis() % 100000}.xlsx"
        val file = File(context.cacheDir, fileName)

        try {
            val bizHeader = BusinessProfileLoader.load(context, businessProfile)
            val now = Date()
            val dayName = try { DAY_FORMATTER_AR.get().format(now) } catch (e: Exception) { "" }
            val dateFormatted = try { DATE_FORMATTER_EN.get().format(now) } catch (e: Exception) { "" }
            val timeFormatted = try { TIME_FORMATTER_AR.get().format(now) } catch (e: Exception) { "" }
            val docDateText = "$dayName $dateFormatted"

            val accountHeaders = listOf(
                context.getString(R.string.excel_col_seq),
                "الحساب",
                "الهاتف",
                "العملة الأساسية",
                "الرصيد الأساسي",
                "الحالة"
            )
            val accountColumns = listOf(
                XlsxOpenXmlBuilder.SheetColumn(1, 1, 7.0),
                XlsxOpenXmlBuilder.SheetColumn(2, 2, 34.0),
                XlsxOpenXmlBuilder.SheetColumn(3, 3, 20.0),
                XlsxOpenXmlBuilder.SheetColumn(4, 4, 17.0),
                XlsxOpenXmlBuilder.SheetColumn(5, 5, 20.0),
                XlsxOpenXmlBuilder.SheetColumn(6, 6, 18.0)
            )
            val accountRows = mutableListOf<XlsxOpenXmlBuilder.Row>()
            accountRows.add(XlsxOpenXmlBuilder.Row(1, 34).apply { cell(0, context.getString(R.string.excel_all_title), 15) })
            accountRows.add(XlsxOpenXmlBuilder.Row(2, 22).apply { cell(0, bizHeader.displayedName + " - " + bizHeader.displayedDesc, 16); cell(3, context.getString(R.string.excel_date_format, docDateText), 17) })
            accountRows.add(XlsxOpenXmlBuilder.Row(3, 22).apply { cell(0, context.getString(R.string.excel_phone_format, bizHeader.phonesStr), 16) })
            accountRows.add(XlsxOpenXmlBuilder.Row(4, 6))
            accountRows.add(XlsxOpenXmlBuilder.Row(5, 30).apply {
                cell(0, "ملخص الحسابات", 7)
                cell(1, "إجمالي له", 1)
                cell(2, XlsxOpenXmlBuilder.Formula("SUMIF(E8:E${customers.size + 7},\">0\",E8:E${customers.size + 7})"), 12)
                cell(3, "إجمالي عليه", 1)
                cell(4, XlsxOpenXmlBuilder.Formula("SUMIF(E8:E${customers.size + 7},\"<0\",E8:E${customers.size + 7})*-1"), 13)
                cell(5, "عدد الحسابات: ${customers.size}", 7)
            })
            accountRows.add(XlsxOpenXmlBuilder.Row(6, 6))
            accountRows.add(XlsxOpenXmlBuilder.Row(7, 30).apply { accountHeaders.forEachIndexed { i, h -> cell(i, h, 1) } })

            customers.forEachIndexed { index, c ->
                val total = c.defaultCurrencyTotal
                val status = when {
                    total > BigDecimal.ZERO -> context.getString(R.string.pdf_status_owed_word)
                    total < BigDecimal.ZERO -> context.getString(R.string.pdf_status_to_him_word)
                    else -> context.getString(R.string.pdf_status_balanced_word)
                }
                val style = when { total > BigDecimal.ZERO -> 2; total < BigDecimal.ZERO -> 3; else -> 4 }
                accountRows.add(XlsxOpenXmlBuilder.Row(index + 8, 24).apply {
                    cell(0, index + 1, 6)
                    cell(1, c.name, 5)
                    cell(2, c.phone.ifEmpty { "-" }, 6)
                    cell(3, currencySymbol, 6)
                    cell(4, total, style)
                    cell(5, status, style)
                })
            }
            val accountLastRow = customers.size + 7
            accountRows.add(XlsxOpenXmlBuilder.Row(accountLastRow + 1, 28).apply {
                cell(0, "الرصيد الصافي", 11)
                cell(4, XlsxOpenXmlBuilder.Formula("SUM(E8:E$accountLastRow)"), 14)
            })

            val foreignHeaders = listOf("الحساب", "العملة", "له", "عليه", "الصافي")
            val foreignRows = mutableListOf<XlsxOpenXmlBuilder.Row>()
            foreignRows.add(XlsxOpenXmlBuilder.Row(1, 34).apply { cell(0, "الأرصدة بالعملات الأجنبية", 15) })
            foreignRows.add(XlsxOpenXmlBuilder.Row(2, 22).apply { cell(0, bizHeader.displayedName, 16) })
            foreignRows.add(XlsxOpenXmlBuilder.Row(3, 6))
            foreignRows.add(XlsxOpenXmlBuilder.Row(4, 28).apply { foreignHeaders.forEachIndexed { i, h -> cell(i, h, 1) } })
            var foreignRow = 5
            customers.forEach { c ->
                c.foreignDebts.filter { it.value.compareTo(BigDecimal.ZERO) != 0 }.forEach { (curr, value) ->
                    foreignRows.add(XlsxOpenXmlBuilder.Row(foreignRow, 24).apply {
                        val positiveStyle = if (value > BigDecimal.ZERO) 2 else 3
                        val negativeStyle = if (value > BigDecimal.ZERO) 3 else 2
                        cell(0, c.name, 5)
                        cell(1, curr, 6)
                        cell(2, if (value > BigDecimal.ZERO) value.abs() else null, positiveStyle)
                        cell(3, if (value < BigDecimal.ZERO) value.abs() else null, negativeStyle)
                        cell(4, XlsxOpenXmlBuilder.Formula("C$foreignRow-D$foreignRow"), 4)
                    })
                    foreignRow++
                }
            }
            if (foreignRow == 5) {
                foreignRows.add(XlsxOpenXmlBuilder.Row(foreignRow, 24).apply { cell(0, "لا توجد أرصدة أجنبية مستقلة", 6) })
                foreignRows.add(XlsxOpenXmlBuilder.Row(foreignRow + 1, 6))
            }
            val foreignLastRow = (foreignRow - 1).coerceAtLeast(5)
            val foreignSummaryStart = foreignLastRow + 3
            val supportedCurrencyCodes = listOf("YER", "SAR", "USD")
            foreignRows.add(XlsxOpenXmlBuilder.Row(foreignLastRow + 2, 26).apply { cell(0, "ملخص تلقائي حسب العملة", 7) })
            supportedCurrencyCodes.forEachIndexed { i, code ->
                val rowNo = foreignSummaryStart + i
                foreignRows.add(XlsxOpenXmlBuilder.Row(rowNo, 24).apply {
                    cell(0, code, 6)
                    cell(1, XlsxOpenXmlBuilder.Formula("SUMIFS(C5:C$foreignLastRow,B5:B$foreignLastRow,\"$code\")"), 4)
                    cell(2, XlsxOpenXmlBuilder.Formula("SUMIFS(D5:D$foreignLastRow,B5:B$foreignLastRow,\"$code\")"), 4)
                    cell(3, XlsxOpenXmlBuilder.Formula("B$rowNo-C$rowNo"), 4)
                    cell(4, if (code == currencySymbol || com.smartledger.aldaftar.ui.screens.habayeb.utils.CurrencyConfig.getBySymbol(currencySymbol)?.code == code) "العملة الأساسية" else "رصيد مستقل", 6)
                })
            }

            val summaryRows = mutableListOf<XlsxOpenXmlBuilder.Row>()
            summaryRows.add(XlsxOpenXmlBuilder.Row(1, 36).apply { cell(0, "ملخص التقرير العام", 15) })
            summaryRows.add(XlsxOpenXmlBuilder.Row(2, 24).apply { cell(0, "العملة الأساسية", 1); cell(1, currencySymbol, 6); cell(2, "إجمالي له", 1); cell(3, XlsxOpenXmlBuilder.Formula("'الحسابات'!C5"), 12); cell(4, "إجمالي عليه", 1); cell(5, XlsxOpenXmlBuilder.Formula("'الحسابات'!E5"), 13) })
            summaryRows.add(XlsxOpenXmlBuilder.Row(3, 30).apply { cell(0, "صافي الرصيد", 7); cell(1, XlsxOpenXmlBuilder.Formula("'الحسابات'!E${accountLastRow + 1}"), 10); cell(2, "عدد الحسابات", 7); cell(3, customers.size, 6) })
            summaryRows.add(XlsxOpenXmlBuilder.Row(4, 6))
            summaryRows.add(XlsxOpenXmlBuilder.Row(5, 24).apply { cell(0, "العملات الأجنبية", 7); cell(1, "راجع ورقة الأرصدة الأجنبية؛ لا تُجمع مع العملة الأساسية دون تحويل موثق.", 0) })

            XlsxOpenXmlBuilder.buildXlsxFile(
                workbook = XlsxOpenXmlBuilder.WorkbookSpec(
                    sheets = listOf(
                        XlsxOpenXmlBuilder.SheetSpec(
                            name = "الملخص",
                            columns = listOf(XlsxOpenXmlBuilder.SheetColumn(1, 1, 20.0), XlsxOpenXmlBuilder.SheetColumn(2, 2, 28.0), XlsxOpenXmlBuilder.SheetColumn(3, 3, 18.0), XlsxOpenXmlBuilder.SheetColumn(4, 4, 18.0), XlsxOpenXmlBuilder.SheetColumn(5, 5, 18.0), XlsxOpenXmlBuilder.SheetColumn(6, 6, 18.0)),
                            rows = summaryRows,
                            merges = listOf(XlsxOpenXmlBuilder.MergeRange("A1:F1"), XlsxOpenXmlBuilder.MergeRange("A5:F5")),
                            freezeRows = 2,
                            protected = true
                        ),
                        XlsxOpenXmlBuilder.SheetSpec(
                            name = "الحسابات", columns = accountColumns, rows = accountRows,
                            merges = listOf(XlsxOpenXmlBuilder.MergeRange("A1:F1"), XlsxOpenXmlBuilder.MergeRange("A2:C2"), XlsxOpenXmlBuilder.MergeRange("A3:C3")),
                            freezeRows = 7, autoFilterRef = "A7:F$accountLastRow",
                            table = if (customers.isNotEmpty()) XlsxOpenXmlBuilder.TableSpec("AccountsTable", "AccountsTable", "A7:F$accountLastRow", accountHeaders) else null,
                            protected = true
                        ),
                        XlsxOpenXmlBuilder.SheetSpec(
                            name = "الأرصدة الأجنبية",
                            columns = listOf(XlsxOpenXmlBuilder.SheetColumn(1, 1, 34.0), XlsxOpenXmlBuilder.SheetColumn(2, 2, 16.0), XlsxOpenXmlBuilder.SheetColumn(3, 3, 18.0), XlsxOpenXmlBuilder.SheetColumn(4, 4, 18.0), XlsxOpenXmlBuilder.SheetColumn(5, 5, 18.0)),
                            rows = foreignRows,
                            merges = listOf(XlsxOpenXmlBuilder.MergeRange("A1:E1"), XlsxOpenXmlBuilder.MergeRange("A2:E2")),
                            freezeRows = 4, autoFilterRef = "A4:E$foreignLastRow",
                            table = if (foreignRow > 5) XlsxOpenXmlBuilder.TableSpec("ForeignBalancesTable", "ForeignBalancesTable", "A4:E$foreignLastRow", foreignHeaders) else null,
                            protected = true
                        )
                    )
                ),
                file = file
            )
            return file
        } catch (e: Exception) {
            Log.e(TAG, "Error writing XLSX All Customers file", e)
            return null
        }
    }
}

