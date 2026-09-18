package com.smartledger.aldaftar.data.serialization

import com.smartledger.aldaftar.data.serialization.excel.XlsxOpenXmlBuilder
import com.smartledger.aldaftar.ui.screens.habayeb.utils.CurrencyConfig
import java.io.File
import java.util.zip.ZipFile
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReportCurrencyAndExcelIntegrityContractTest {
    @Test
    fun supportedCurrenciesAreExactlyYER_SAR_USD() {
        val codes = CurrencyConfig.currencies.map { it.code }
        assertTrue(codes.containsAll(listOf("YER", "SAR", "USD")))
        assertTrue(codes.size == 3)
        assertFalse(codes.contains("EUR"))
    }

    @Test
    fun protectedSheetKeepsInputsEditableAndPreventsFormulaAccidentalEdits() {
        val file = File.createTempFile("xlsx-protection-contract", ".xlsx")
        try {
            XlsxOpenXmlBuilder.buildXlsxFile(
                XlsxOpenXmlBuilder.WorkbookSpec(
                    listOf(
                        XlsxOpenXmlBuilder.SheetSpec(
                            name = "الحركات",
                            rows = listOf(
                                XlsxOpenXmlBuilder.Row(1).apply {
                                    cell(0, "المبلغ", XlsxOpenXmlBuilder.STYLE_HEADER)
                                    cell(1, "السعر", XlsxOpenXmlBuilder.STYLE_HEADER)
                                    cell(2, "المعادل", XlsxOpenXmlBuilder.STYLE_HEADER)
                                },
                                XlsxOpenXmlBuilder.Row(2).apply {
                                    cell(0, 100, XlsxOpenXmlBuilder.STYLE_EDITABLE_NUMBER)
                                    cell(1, 530, XlsxOpenXmlBuilder.STYLE_EDITABLE_RATE)
                                    cell(2, XlsxOpenXmlBuilder.Formula("A2*B2"), XlsxOpenXmlBuilder.STYLE_FORMULA_NUMBER)
                                }
                            ),
                            protected = true
                        )
                    )
                ),
                file
            )
            ZipFile(file).use { zip ->
                val styles = zip.getInputStream(zip.getEntry("xl/styles.xml")).bufferedReader().readText()
                val sheet = zip.getInputStream(zip.getEntry("xl/worksheets/sheet1.xml")).bufferedReader().readText()
                assertTrue(sheet.contains("<sheetProtection"))
                assertTrue(styles.contains("<protection locked=\"0\""))
                assertTrue(sheet.contains("<f>A2*B2</f>"))
            }
        } finally {
            file.delete()
        }
    }

    @Test
    fun workbookRequestsAutomaticRecalculationAfterExternalEdits() {
        val file = File.createTempFile("xlsx-recalc-contract", ".xlsx")
        try {
            XlsxOpenXmlBuilder.buildXlsxFile(
                XlsxOpenXmlBuilder.WorkbookSpec(
                    listOf(XlsxOpenXmlBuilder.SheetSpec("الحركات"))
                ),
                file
            )
            ZipFile(file).use { zip ->
                val workbook = zip.getInputStream(zip.getEntry("xl/workbook.xml")).bufferedReader().readText()
                assertTrue(workbook.contains("calcMode=\"auto\""))
                assertTrue(workbook.contains("fullCalcOnLoad=\"1\""))
                assertTrue(workbook.contains("forceFullCalc=\"1\""))
            }
        } finally {
            file.delete()
        }
    }
}
