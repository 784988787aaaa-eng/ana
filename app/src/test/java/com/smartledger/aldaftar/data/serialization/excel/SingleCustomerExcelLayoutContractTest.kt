package com.smartledger.aldaftar.data.serialization.excel

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * عقد حماية لتصميم تصدير كشف الحساب.
 *
 * الهدف: منع عودة الأعمدة والصفحات التي تم حذفها عمداً أثناء أي تحديث لاحق.
 */
class SingleCustomerExcelLayoutContractTest {

    private fun source(): String =
        File("src/main/java/com/smartledger/aldaftar/data/serialization/excel/SingleCustomerExcelEngine.kt")
            .readText()

    @Test
    fun transactionsUseOnlyTenColumnsAndNoLegacyDerivedColumns() {
        val source = source()

        assertTrue(source.contains("val txHeaders = listOf("))
        assertTrue(source.contains("\"المعادل بالعملة الأساسية\",\n                \"له\",\n                \"عليه\",\n                \"الرصيد\""))

        assertFalse(source.contains("row.cell(10,"))
        assertFalse(source.contains("row.cell(11,"))
        assertFalse(source.contains("row.cell(12,"))
        assertFalse(source.contains("A7:M"))
        assertFalse(source.contains("M\$8"))
        assertFalse(source.contains("L\$txRow"))
    }

    @Test
    fun summaryAndCurrencyCatalogAreMergedIntoTransactionsSheet() {
        val source = source()

        assertTrue(source.contains("val txSheetName = \"الحركات\""))
        assertTrue(source.contains("cell(0, \"ملخص العملات\", 7)"))
        assertTrue(source.contains("val currencyHeaders = listOf("))
        assertTrue(source.contains("\"عدد الحركات\""))
        assertTrue(source.contains("XlsxOpenXmlBuilder.MergeRange(\"A\$summaryTitleRow:G\$summaryTitleRow\")"))

        assertFalse(source.contains("val summarySheetName = \"الملخص\""))
        assertFalse(source.contains("name = \"الملخص\""))
        assertFalse(source.contains("name = \"العملات\""))
    }

    @Test
    fun currencySummaryAvoidsFullExcelColumnRanges() {
        val source = source()

        assertFalse(source.contains("1048576"))
        assertTrue(source.contains("COUNTIFS(D\$8:D\$txLastDataRow"))
        assertTrue(source.contains("SUMIFS(H\$8:H\$txLastDataRow"))
        assertTrue(source.contains("SUMIFS(I\$8:I\$txLastDataRow"))
    }
}
