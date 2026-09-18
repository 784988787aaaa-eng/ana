package com.smartledger.aldaftar.data.serialization.excel

import java.io.File
import java.util.zip.ZipFile
import org.junit.Assert.assertTrue
import org.junit.Test

class XlsxProfessionalContractTest {
    @Test
    fun generatedWorkbookSupportsRealFormulasAndAutomaticRecalculation() {
        val file = File.createTempFile("professional-xlsx-contract", ".xlsx")
        try {
            XlsxOpenXmlBuilder.buildXlsxFile(
                workbook = XlsxOpenXmlBuilder.WorkbookSpec(
                    sheets = listOf(
                        XlsxOpenXmlBuilder.SheetSpec(
                            name = "الحركات",
                            columns = listOf(XlsxOpenXmlBuilder.SheetColumn(1, 3, 18.0)),
                            rows = listOf(
                                XlsxOpenXmlBuilder.Row(1).apply {
                                    cell(0, "المبلغ", 1)
                                    cell(1, "سعر الصرف", 1)
                                    cell(2, XlsxOpenXmlBuilder.Formula("A2*B2"), 4)
                                },
                                XlsxOpenXmlBuilder.Row(2).apply {
                                    cell(0, 100, 4)
                                    cell(1, 530, 4)
                                }
                            )
                        )
                    )
                ),
                file = file
            )

            ZipFile(file).use { zip ->
                val sheetXml = zip.getInputStream(zip.getEntry("xl/worksheets/sheet1.xml"))
                    .bufferedReader().readText()
                val workbookXml = zip.getInputStream(zip.getEntry("xl/workbook.xml"))
                    .bufferedReader().readText()

                assertTrue("formula must be stored as an Excel formula", sheetXml.contains("<f>A2*B2</f>"))
                assertTrue("workbook must request recalculation", workbookXml.contains("calcPr"))
            }
        } finally {
            file.delete()
        }
    }
}
