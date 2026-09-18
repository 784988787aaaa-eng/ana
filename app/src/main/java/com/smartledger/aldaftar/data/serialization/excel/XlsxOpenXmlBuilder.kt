package com.smartledger.aldaftar.data.serialization.excel

import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/** Small dependency-free XLSX/OpenXML writer used by the app's export layer. */
object XlsxOpenXmlBuilder {
    // Stable style contracts used by report exporters. Input styles are unlocked; formula styles remain locked.
    const val STYLE_HEADER = 1
    const val STYLE_EDITABLE_TEXT = 20
    const val STYLE_EDITABLE_NUMBER = 21
    const val STYLE_EDITABLE_RATE = 22
    const val STYLE_FORMULA_NUMBER = 4
    class SheetColumn(val min: Int, val max: Int, val width: Double)
    class MergeRange(val ref: String)
    data class Formula(val expression: String)

    class Cell(val col: Int, val value: Any?, val styleId: Int) {
        fun toXml(row: Int): String {
            val ref = getCellRef(col, row)
            if (value == null) return "<c r=\"$ref\" s=\"$styleId\"/>"
            return when (value) {
                is Formula -> "<c r=\"$ref\" s=\"$styleId\"><f>${value.expression.xmlEscape()}</f><v></v></c>"
                is Number -> "<c r=\"$ref\" s=\"$styleId\"><v>${value}</v></c>"
                is Boolean -> "<c r=\"$ref\" s=\"$styleId\" t=\"b\"><v>${if (value) 1 else 0}</v></c>"
                else -> {
                    val esc = value.toString().xmlEscape()
                    "<c r=\"$ref\" s=\"$styleId\" t=\"inlineStr\"><is><t>$esc</t></is></c>"
                }
            }
        }
    }

    class Row(val r: Int, val ht: Int = 24) {
        val cells = mutableListOf<Cell>()
        fun cell(col: Int, value: Any?, styleId: Int) { cells.add(Cell(col, value, styleId)) }
        fun toXml(): String = buildString {
            append("<row r=\"$r\" ht=\"$ht\" customHeight=\"1\">")
            cells.sortedBy { it.col }.forEach { append(it.toXml(r)) }
            append("</row>")
        }
    }

    data class SheetSpec(
        val name: String,
        val columns: List<SheetColumn> = emptyList(),
        val rows: List<Row> = emptyList(),
        val merges: List<MergeRange> = emptyList(),
        val freezeRows: Int = 0,
        val autoFilterRef: String? = null,
        val table: TableSpec? = null,
        val protected: Boolean = false
    )

    data class TableSpec(
        val name: String,
        val displayName: String,
        val ref: String,
        val headers: List<String>
    )

    data class WorkbookSpec(val sheets: List<SheetSpec>)

    fun getCellRef(colIndex: Int, rowIndex: Int): String {
        var temp = colIndex
        val colName = StringBuilder()
        while (temp >= 0) {
            colName.insert(0, ('A'.code + temp % 26).toChar())
            temp = temp / 26 - 1
        }
        return "$colName$rowIndex"
    }

    fun String.xmlEscape(): String = replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
        .replace("'", "&apos;")

    private fun getStylesXml(): String = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<styleSheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">
  <numFmts count="1"><numFmt numFmtId="164" formatCode="#,##0.00"/></numFmts>
  <fonts count="6">
    <font><sz val="10.5"/><name val="Segoe UI"/><color rgb="FF1E293B"/></font>
    <font><b/><sz val="10.5"/><name val="Segoe UI"/><color rgb="FFFFFFFF"/></font>
    <font><b/><sz val="10.5"/><name val="Segoe UI"/><color rgb="FF0F172A"/></font>
    <font><b/><sz val="10"/><name val="Segoe UI"/><color rgb="FFB91C1C"/></font>
    <font><b/><sz val="10"/><name val="Segoe UI"/><color rgb="FF156534"/></font>
    <font><b/><sz val="15"/><name val="Segoe UI"/><color rgb="FF0F4C43"/></font>
  </fonts>
  <fills count="9">
    <fill><patternFill patternType="none"/></fill><fill><patternFill patternType="gray125"/></fill>
    <fill><patternFill patternType="solid"><fgColor rgb="FF2C3E50"/><bgColor rgb="FF2C3E50"/></patternFill></fill>
    <fill><patternFill patternType="solid"><fgColor rgb="FFF0FDF4"/><bgColor rgb="FFF0FDF4"/></patternFill></fill>
    <fill><patternFill patternType="solid"><fgColor rgb="FFFEF2F2"/><bgColor rgb="FFFEF2F2"/></patternFill></fill>
    <fill><patternFill patternType="solid"><fgColor rgb="FFF8FAFC"/><bgColor rgb="FFF8FAFC"/></patternFill></fill>
    <fill><patternFill patternType="solid"><fgColor rgb="FF0F4C43"/><bgColor rgb="FF0F4C43"/></patternFill></fill>
    <fill><patternFill patternType="solid"><fgColor rgb="FFEFF6FF"/><bgColor rgb="FFEFF6FF"/></patternFill></fill>
    <fill><patternFill patternType="solid"><fgColor rgb="FFFEF3C7"/><bgColor rgb="FFFEF3C7"/></patternFill></fill>
  </fills>
  <borders count="3">
    <border><left/><right/><top/><bottom/></border>
    <border><left style="thin"><color rgb="FFCBD5E1"/></left><right style="thin"><color rgb="FFCBD5E1"/></right><top style="thin"><color rgb="FFCBD5E1"/></top><bottom style="thin"><color rgb="FFCBD5E1"/></bottom></border>
    <border><left style="thin"><color rgb="FFCBD5E1"/></left><right style="thin"><color rgb="FFCBD5E1"/></right><top style="thin"><color rgb="FF2C3E50"/></top><bottom style="double"><color rgb="FF2C3E50"/></bottom></border>
  </borders>
  <cellStyleXfs count="1"><xf numFmtId="0" fontId="0" fillId="0" borderId="0"/></cellStyleXfs>
  <cellXfs count="23">
    <xf numFmtId="0" fontId="0" fillId="0" borderId="1" applyFont="1" applyBorder="1"><alignment vertical="center" wrapText="1"/></xf>
    <xf numFmtId="0" fontId="1" fillId="2" borderId="1" applyFont="1" applyFill="1" applyBorder="1"><alignment horizontal="center" vertical="center" wrapText="1"/></xf>
    <xf numFmtId="164" fontId="3" fillId="4" borderId="1" applyFont="1" applyFill="1" applyBorder="1"><alignment horizontal="center" vertical="center" wrapText="1"/></xf>
    <xf numFmtId="164" fontId="4" fillId="3" borderId="1" applyFont="1" applyFill="1" applyBorder="1"><alignment horizontal="center" vertical="center" wrapText="1"/></xf>
    <xf numFmtId="164" fontId="2" fillId="0" borderId="1" applyFont="1" applyBorder="1"><alignment horizontal="center" vertical="center" wrapText="1"/></xf>
    <xf numFmtId="0" fontId="2" fillId="0" borderId="1" applyFont="1" applyBorder="1"><alignment horizontal="right" vertical="center" wrapText="1"/></xf>
    <xf numFmtId="0" fontId="0" fillId="0" borderId="1" applyFont="1" applyBorder="1"><alignment horizontal="center" vertical="center" wrapText="1"/></xf>
    <xf numFmtId="0" fontId="2" fillId="5" borderId="1" applyFont="1" applyFill="1" applyBorder="1"><alignment horizontal="center" vertical="center" wrapText="1"/></xf>
    <xf numFmtId="164" fontId="3" fillId="5" borderId="1" applyFont="1" applyFill="1" applyBorder="1"><alignment horizontal="center" vertical="center"/></xf>
    <xf numFmtId="164" fontId="4" fillId="5" borderId="1" applyFont="1" applyFill="1" applyBorder="1"><alignment horizontal="center" vertical="center"/></xf>
    <xf numFmtId="164" fontId="2" fillId="5" borderId="1" applyFont="1" applyFill="1" applyBorder="1"><alignment horizontal="center" vertical="center"/></xf>
    <xf numFmtId="0" fontId="2" fillId="5" borderId="2" applyFont="1" applyFill="1" applyBorder="1"><alignment horizontal="center" vertical="center" wrapText="1"/></xf>
    <xf numFmtId="164" fontId="3" fillId="4" borderId="2" applyFont="1" applyFill="1" applyBorder="1"><alignment horizontal="center" vertical="center"/></xf>
    <xf numFmtId="164" fontId="4" fillId="3" borderId="2" applyFont="1" applyFill="1" applyBorder="1"><alignment horizontal="center" vertical="center"/></xf>
    <xf numFmtId="0" fontId="2" fillId="5" borderId="2" applyFont="1" applyFill="1" applyBorder="1"><alignment horizontal="center" vertical="center"/></xf>
    <xf numFmtId="0" fontId="5" fillId="0" borderId="0" applyFont="1"><alignment horizontal="center" vertical="center"/></xf>
    <xf numFmtId="0" fontId="2" fillId="0" borderId="0" applyFont="1"><alignment horizontal="right" vertical="center"/></xf>
    <xf numFmtId="0" fontId="0" fillId="0" borderId="0" applyFont="1"><alignment horizontal="left" vertical="center"/></xf>
    <xf numFmtId="0" fontId="0" fillId="7" borderId="1" applyFont="1" applyFill="1" applyBorder="1"><alignment horizontal="center" vertical="center" wrapText="1"/></xf>
    <xf numFmtId="0" fontId="0" fillId="8" borderId="1" applyFont="1" applyFill="1" applyBorder="1"><alignment horizontal="center" vertical="center" wrapText="1"/></xf>
    <xf numFmtId="0" fontId="0" fillId="0" borderId="1" applyFont="1" applyBorder="1"><protection locked="0"/><alignment horizontal="right" vertical="center" wrapText="1"/></xf>
    <xf numFmtId="164" fontId="0" fillId="0" borderId="1" applyFont="1" applyBorder="1"><protection locked="0"/><alignment horizontal="center" vertical="center"/></xf>
    <xf numFmtId="164" fontId="0" fillId="8" borderId="1" applyFont="1" applyFill="1" applyBorder="1"><protection locked="0"/><alignment horizontal="center" vertical="center"/></xf>
  </cellXfs>
</styleSheet>"""

    fun buildXlsxFile(
        workbook: WorkbookSpec,
        file: File
    ) {
        require(workbook.sheets.isNotEmpty()) { "Workbook must contain at least one sheet" }
        FileOutputStream(file).use { fos ->
            ZipOutputStream(fos).use { zos ->
                fun add(path: String, content: String) {
                    zos.putNextEntry(ZipEntry(path))
                    zos.write(content.toByteArray(Charsets.UTF_8))
                    zos.closeEntry()
                }

                val contentTypes = buildString {
                    append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>")
                    append("<Types xmlns=\"http://schemas.openxmlformats.org/package/2006/content-types\">")
                    append("<Default Extension=\"rels\" ContentType=\"application/vnd.openxmlformats-package.relationships+xml\"/>")
                    append("<Default Extension=\"xml\" ContentType=\"application/xml\"/>")
                    append("<Override PartName=\"/xl/workbook.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml\"/>")
                    append("<Override PartName=\"/xl/styles.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml\"/>")
                    workbook.sheets.forEachIndexed { index, sheet ->
                        append("<Override PartName=\"/xl/worksheets/sheet${index + 1}.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml\"/>")
                        if (sheet.table != null) append("<Override PartName=\"/xl/tables/table${index + 1}.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.table+xml\"/>")
                    }
                    append("</Types>")
                }

                val globalRels = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?><Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships"><Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="xl/workbook.xml"/></Relationships>"""
                val workbookRels = buildString {
                    append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">")
                    workbook.sheets.forEachIndexed { index, _ ->
                        val n = index + 1
                        append("<Relationship Id=\"rId${n}\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet\" Target=\"worksheets/sheet$n.xml\"/>")
                    }
                    val styleId = workbook.sheets.size + 1
                    append("<Relationship Id=\"rId$styleId\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles\" Target=\"styles.xml\"/>")
                    append("</Relationships>")
                }
                val workbookXml = buildString {
                    append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><workbook xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\" xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\">")
                    append("<fileVersion appName=\"xl\" lastEdited=\"7\"/>")
                    append("<calcPr calcId=\"191029\" calcMode=\"auto\" fullCalcOnLoad=\"1\" forceFullCalc=\"1\"/>")
                    append("<sheets>")
                    workbook.sheets.forEachIndexed { index, sheet ->
                        append("<sheet name=\"${sheet.name.xmlEscape()}\" sheetId=\"${index + 1}\" r:id=\"rId${index + 1}\"/>")
                    }
                    append("</sheets></workbook>")
                }

                add("[Content_Types].xml", contentTypes)
                add("_rels/.rels", globalRels)
                add("xl/workbook.xml", workbookXml)
                add("xl/_rels/workbook.xml.rels", workbookRels)
                add("xl/styles.xml", getStylesXml())

                workbook.sheets.forEachIndexed { index, sheet ->
                    val n = index + 1
                    add("xl/worksheets/sheet$n.xml", sheetXml(sheet))
                    if (sheet.table != null) {
                        add("xl/worksheets/_rels/sheet$n.xml.rels", sheetRels(n))
                        add("xl/tables/table$n.xml", tableXml(sheet.table, n))
                    }
                }
            }
        }
    }

    /** Backward-compatible one-sheet entry point. */
    fun buildXlsxFile(
        sheetName: String,
        columns: List<SheetColumn>,
        rows: List<Row>,
        merges: List<MergeRange>,
        file: File
    ) = buildXlsxFile(WorkbookSpec(listOf(SheetSpec(sheetName, columns, rows, merges))), file)

    private fun sheetXml(sheet: SheetSpec): String = buildString {
        append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><worksheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\" xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\">")
        append("<sheetViews><sheetView rightToLeft=\"1\" tabSelected=\"1\" workbookViewId=\"0\">")
        if (sheet.freezeRows > 0) append("<pane ySplit=\"${sheet.freezeRows}\" topLeftCell=\"A${sheet.freezeRows + 1}\" activePane=\"bottomRight\" state=\"frozen\"/>")
        append("</sheetView></sheetViews>")
        if (sheet.protected) {
            // No password: this is intentional light protection against accidental edits.
            // Excel users can unprotect the sheet normally and edit everything if desired.
            append("<sheetProtection sheet=\"1\" objects=\"1\" scenarios=\"1\" selectLockedCells=\"1\" selectUnlockedCells=\"1\" formatCells=\"0\" formatColumns=\"0\" formatRows=\"0\" insertColumns=\"0\" insertRows=\"0\" deleteColumns=\"0\" deleteRows=\"0\" sort=\"1\" autoFilter=\"1\"/>")
        }
        if (sheet.columns.isNotEmpty()) {
            append("<cols>")
            sheet.columns.forEach { append("<col min=\"${it.min}\" max=\"${it.max}\" width=\"${it.width}\" customWidth=\"1\"/>") }
            append("</cols>")
        }
        append("<sheetData>")
        sheet.rows.forEach { append(it.toXml()) }
        append("</sheetData>")
        if (sheet.merges.isNotEmpty()) {
            append("<mergeCells count=\"${sheet.merges.size}\">")
            sheet.merges.forEach { append("<mergeCell ref=\"${it.ref}\"/>") }
            append("</mergeCells>")
        }
        sheet.autoFilterRef?.let { append("<autoFilter ref=\"$it\"/>") }
        if (sheet.table != null) append("<tableParts count=\"1\"><tablePart r:id=\"rId1\"/></tableParts>")
        append("</worksheet>")
    }

    private fun sheetRels(sheetIndex: Int): String =
        "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\"><Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/table\" Target=\"../tables/table$sheetIndex.xml\"/></Relationships>"

    private fun tableXml(table: TableSpec, tableIndex: Int): String = buildString {
        append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>")
        append("<table xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\" id=\"$tableIndex\" name=\"${table.name.xmlEscape()}\" displayName=\"${table.displayName.xmlEscape()}\" ref=\"${table.ref}\" headerRowCount=\"1\">")
        append("<autoFilter ref=\"${table.ref}\"/>")
        append("<tableColumns count=\"${table.headers.size}\">")
        table.headers.forEachIndexed { i, h -> append("<tableColumn id=\"${i + 1}\" name=\"${h.xmlEscape()}\"/>") }
        append("</tableColumns><tableStyleInfo name=\"TableStyleMedium2\" showFirstColumn=\"0\" showLastColumn=\"0\" showRowStripes=\"1\" showColumnStripes=\"0\"/></table>")
    }
}
