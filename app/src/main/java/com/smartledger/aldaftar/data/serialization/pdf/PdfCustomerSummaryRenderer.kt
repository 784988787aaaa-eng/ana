package com.smartledger.aldaftar.data.serialization.pdf

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.text.Layout
import com.smartledger.aldaftar.R
import com.smartledger.aldaftar.data.serialization.pdf.PdfDrawingUtils.drawArabicText
import com.smartledger.aldaftar.ui.helper.HabayebMathHelper
import com.smartledger.aldaftar.ui.state.CustomerUiState
import java.math.BigDecimal
import kotlin.math.roundToInt

object PdfCustomerSummaryRenderer {

    private val paintCardBg = Paint().apply {
        color = Color.parseColor(PdfColors.CARD_BG)
        style = Paint.Style.FILL
    }
    private val paintCardBorder = Paint().apply {
        color = Color.parseColor(PdfColors.HEADER_BORDER)
        strokeWidth = 0.5f
        style = Paint.Style.STROKE
    }

    fun calculateCustomerSummaryRowHeight(
        context: Context,
        c: CustomerUiState,
        nameWidth: Int = 175,
        foreignWidth: Int = 125
    ): Float {
        val nameHeight = PdfDrawingUtils.measureTextHeight(c.name, PdfPaints.paintAccountNameBold, nameWidth)
        val phoneHeight = if (c.phone.isNotBlank()) 14 else 0
        val colNameTotal = nameHeight + phoneHeight

        val foreignList = c.foreignDebts.filter { it.value.compareTo(BigDecimal.ZERO) != 0 }
        val foreignStr = if (foreignList.isEmpty()) "-" else foreignList.entries.joinToString("\n") { (curr, bd) ->
            val formatted = HabayebMathHelper.formatSmart(bd.abs())
            val prefix = if (bd.compareTo(BigDecimal.ZERO) > 0) "+" else "-"
            "$prefix$formatted $curr"
        }
        val foreignHeight = PdfDrawingUtils.measureTextHeight(foreignStr, PdfPaints.paintCellNormal, foreignWidth)

        return maxOf(colNameTotal + 14f, foreignHeight + 14f, 34f)
    }

    fun drawCustomerSummaryRow(
        canvas: Canvas,
        context: Context,
        index: Int,
        c: CustomerUiState,
        currentY: Float,
        rowHeight: Float,
        currencySymbol: String
    ) {
        val hasForeign = c.foreignDebts.any { it.value.compareTo(BigDecimal.ZERO) != 0 }
        if (hasForeign) {
            canvas.drawRect(25f, currentY, 570f, currentY + rowHeight, PdfPaints.paintForeignBg)
        }

        canvas.drawLine(25f, currentY + rowHeight, 570f, currentY + rowHeight, PdfPaints.paintRowDivider)

        canvas.drawLine(535f, currentY, 535f, currentY + rowHeight, PdfPaints.paintRowDivider)
        canvas.drawLine(360f, currentY, 360f, currentY + rowHeight, PdfPaints.paintRowDivider)
        canvas.drawLine(230f, currentY, 230f, currentY + rowHeight, PdfPaints.paintRowDivider)
        canvas.drawLine(105f, currentY, 105f, currentY + rowHeight, PdfPaints.paintRowDivider)

        val textYOffset = (rowHeight - 12f) / 2f

        drawArabicText(canvas, (index + 1).toString(), 535f, currentY + textYOffset, 35, PdfPaints.paintCellNormal, Layout.Alignment.ALIGN_CENTER)

        val nameLayout = PdfDrawingUtils.createStaticLayout(c.name, PdfPaints.paintAccountNameBold, 170, Layout.Alignment.ALIGN_NORMAL)
        val nameTotalH = nameLayout.height + if (c.phone.isNotBlank()) 14f else 0f
        val nameYOffset = ((rowHeight - nameTotalH) / 2f).coerceAtLeast(3f)

        PdfDrawingUtils.drawStaticLayout(canvas, nameLayout, 365f, currentY + nameYOffset)
        if (c.phone.isNotBlank()) {
            drawArabicText(canvas, c.phone, 365f, currentY + nameYOffset + nameLayout.height + 1f, 170, PdfPaints.paintMutedText, Layout.Alignment.ALIGN_NORMAL)
        }

        val totalBd = c.defaultCurrencyTotal
        val isPositive = totalBd.compareTo(BigDecimal.ZERO) > 0
        val isNegative = totalBd.compareTo(BigDecimal.ZERO) < 0
        val formattedPrimary = HabayebMathHelper.formatSmart(totalBd.abs()) + " " + currencySymbol
        val balancePaint = if (isPositive) PdfPaints.paintOwedText else if (isNegative) PdfPaints.paintPaymentText else PdfPaints.paintCellNormal
        drawArabicText(canvas, formattedPrimary, 230f, currentY + textYOffset, 130, balancePaint, Layout.Alignment.ALIGN_CENTER)

        val foreignList = c.foreignDebts.filter { it.value.compareTo(BigDecimal.ZERO) != 0 }
        val foreignStr = if (foreignList.isEmpty()) {
            "-"
        } else {
            foreignList.entries.joinToString("\n") { (curr, bd) ->
                val formatted = HabayebMathHelper.formatSmart(bd.abs())
                val prefix = if (bd.compareTo(BigDecimal.ZERO) > 0) "+" else "-"
                "$prefix$formatted $curr"
            }
        }
        val foreignLayout = PdfDrawingUtils.createStaticLayout(foreignStr, PdfPaints.paintCellNormal, 120, Layout.Alignment.ALIGN_CENTER)
        val foreignYOffset = ((rowHeight - foreignLayout.height) / 2f).coerceAtLeast(3f)
        PdfDrawingUtils.drawStaticLayout(canvas, foreignLayout, 105f, currentY + foreignYOffset)

        val statusStr = if (isPositive) {
            context.getString(R.string.pdf_status_owed_word)
        } else if (isNegative) {
            context.getString(R.string.pdf_status_to_him_word)
        } else {
            context.getString(R.string.pdf_status_balanced_word)
        }
        val statusPaint = if (isPositive) PdfPaints.paintOwedText else if (isNegative) PdfPaints.paintPaymentText else PdfPaints.paintMutedText
        drawArabicText(canvas, statusStr, 25f, currentY + textYOffset, 80, statusPaint, Layout.Alignment.ALIGN_CENTER)
    }

    fun drawBookletIndexHeader(canvas: Canvas, y: Float, context: Context) {
        val paintHeaderBg = Paint().apply {
            color = Color.parseColor(PdfColors.HEADER_BG)
            style = Paint.Style.FILL
        }
        val paintHeaderBorder = Paint().apply {
            color = Color.parseColor(PdfColors.HEADER_BORDER)
            strokeWidth = 0.5f
            style = Paint.Style.STROKE
        }
        canvas.drawRect(25f, y, 570f, y + 24f, paintHeaderBg)
        canvas.drawLine(25f, y, 570f, y, paintHeaderBorder)
        canvas.drawLine(25f, y + 24f, 570f, y + 24f, paintHeaderBorder)

        canvas.drawLine(535f, y, 535f, y + 24f, paintHeaderBorder)
        canvas.drawLine(305f, y, 305f, y + 24f, paintHeaderBorder)
        canvas.drawLine(205f, y, 205f, y + 24f, paintHeaderBorder)
        canvas.drawLine(105f, y, 105f, y + 24f, paintHeaderBorder)

        val paintHeaderText = Paint().apply {
            color = Color.parseColor(PdfColors.HEADER_TEXT)
            textSize = 9f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        drawArabicText(canvas, context.getString(R.string.pdf_col_m), 535f, y + 6f, 35, paintHeaderText, Layout.Alignment.ALIGN_CENTER)
        drawArabicText(canvas, context.getString(R.string.pdf_index_col_name), 305f, y + 6f, 230, paintHeaderText, Layout.Alignment.ALIGN_NORMAL)
        drawArabicText(canvas, context.getString(R.string.pdf_index_col_phone), 205f, y + 6f, 100, paintHeaderText, Layout.Alignment.ALIGN_NORMAL)
        drawArabicText(canvas, context.getString(R.string.pdf_index_col_balance), 105f, y + 6f, 100, paintHeaderText, Layout.Alignment.ALIGN_CENTER)
        drawArabicText(canvas, context.getString(R.string.pdf_index_col_status), 25f, y + 6f, 80, paintHeaderText, Layout.Alignment.ALIGN_CENTER)
    }

    fun calculateBookletIndexRowHeight(customer: CustomerUiState, availableWidth: Int = 225): Float {
        val nameHeight = PdfDrawingUtils.measureTextHeight(customer.name, PdfPaints.paintCellBold, availableWidth)
        return (nameHeight + 10f).coerceAtLeast(24f)
    }

    fun drawBookletIndexRow(
        canvas: Canvas,
        context: Context,
        index: Int,
        customer: CustomerUiState,
        currentY: Float,
        rowHeight: Float,
        currencySymbol: String
    ) {
        canvas.drawLine(25f, currentY + rowHeight, 570f, currentY + rowHeight, PdfPaints.paintRowDivider)

        canvas.drawLine(535f, currentY, 535f, currentY + rowHeight, PdfPaints.paintRowDivider)
        canvas.drawLine(305f, currentY, 305f, currentY + rowHeight, PdfPaints.paintRowDivider)
        canvas.drawLine(205f, currentY, 205f, currentY + rowHeight, PdfPaints.paintRowDivider)
        canvas.drawLine(105f, currentY, 105f, currentY + rowHeight, PdfPaints.paintRowDivider)

        val textYOffset = (rowHeight - 12f) / 2f

        drawArabicText(canvas, (index + 1).toString(), 535f, currentY + textYOffset, 35, PdfPaints.paintCellNormal, Layout.Alignment.ALIGN_CENTER)

        val nameLayout = PdfDrawingUtils.createStaticLayout(customer.name, PdfPaints.paintCellBold, 225, Layout.Alignment.ALIGN_NORMAL)
        val nameYOffset = ((rowHeight - nameLayout.height) / 2f).coerceAtLeast(2f)
        PdfDrawingUtils.drawStaticLayout(canvas, nameLayout, 310f, currentY + nameYOffset)

        drawArabicText(canvas, customer.phone.ifEmpty { "-" }, 205f, currentY + textYOffset, 100, PdfPaints.paintCellNormal, Layout.Alignment.ALIGN_NORMAL)

        val balText = "${HabayebMathHelper.formatSmart(customer.defaultCurrencyTotal.abs())} $currencySymbol"
        drawArabicText(canvas, balText, 105f, currentY + textYOffset, 100, PdfPaints.paintCellBold, Layout.Alignment.ALIGN_CENTER)

        val statusStr = when {
            customer.defaultCurrencyTotal > BigDecimal.ZERO -> context.getString(R.string.pdf_status_for_us)
            customer.defaultCurrencyTotal < BigDecimal.ZERO -> context.getString(R.string.pdf_status_on_us)
            else -> context.getString(R.string.pdf_status_balanced)
        }
        val statusColor = when {
            customer.defaultCurrencyTotal > BigDecimal.ZERO -> PdfColors.PAYMENT_TEXT
            customer.defaultCurrencyTotal < BigDecimal.ZERO -> PdfColors.OWED_TEXT
            else -> PdfColors.TEXT_LIGHT
        }
        val paintStatus = Paint().apply {
            color = Color.parseColor(statusColor)
            textSize = 8.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        drawArabicText(canvas, statusStr, 25f, currentY + textYOffset, 80, paintStatus, Layout.Alignment.ALIGN_CENTER)
    }

    fun drawComprehensiveSummaryCard(
        canvas: Canvas,
        context: Context,
        primaryColorHex: String,
        summary: ComprehensivePdfSummary,
        totalItems: Int,
        currencySymbol: String,
        startY: Float = 98f
    ): Float {
        val nonZeroForeign = summary.foreignBalances.entries
            .filter { it.value.owedByThem.compareTo(BigDecimal.ZERO) != 0 || it.value.owedToThem.compareTo(BigDecimal.ZERO) != 0 }
            .sortedBy { it.key }
        val cardHeight = PdfReportLayoutSpec.comprehensiveSummaryCardHeight(nonZeroForeign.size)
        val endY = startY + cardHeight

        canvas.drawRoundRect(25f, startY, 570f, endY, 7f, 7f, paintCardBg)
        canvas.drawRoundRect(25f, startY, 570f, endY, 7f, 7f, paintCardBorder)

        val netPrimary = summary.netPrimary
        val netStatus = when {
            netPrimary > BigDecimal.ZERO -> context.getString(R.string.pdf_status_for_us)
            netPrimary < BigDecimal.ZERO -> context.getString(R.string.pdf_status_on_us)
            else -> context.getString(R.string.pdf_status_balanced_word)
        }
        val netColor = when {
            netPrimary > BigDecimal.ZERO -> PdfColors.OWED_TEXT
            netPrimary < BigDecimal.ZERO -> PdfColors.PAYMENT_TEXT
            else -> PdfColors.TEXT_DARK
        }

        val labelPaint = Paint().apply {
            color = Color.parseColor(PdfColors.TEXT_MUTED_GREY)
            textSize = 7.2f
            typeface = Typeface.DEFAULT
            isAntiAlias = true
        }
        val valuePaint = Paint().apply {
            color = Color.parseColor(PdfColors.TEXT_DARK)
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        val netPaint = Paint().apply {
            color = Color.parseColor(netColor)
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        val accentPaint = Paint().apply {
            color = Color.parseColor(primaryColorHex)
            style = Paint.Style.FILL
        }

        // Three compact KPIs replace the old crowded one-line summary.
        val tileTop = startY + 7f
        val tileHeight = 54f
        val gap = 6f
        val tileW = (525f - gap * 2f) / 3f
        val tiles = listOf(
            Triple(context.getString(R.string.pdf_kpi_accounts), totalItems.toString(), PdfColors.TEXT_DARK),
            Triple(context.getString(R.string.pdf_kpi_owed_to_us), "${HabayebMathHelper.formatSmart(summary.totalOwedByThem)} $currencySymbol", PdfColors.OWED_TEXT),
            Triple(context.getString(R.string.pdf_kpi_owed_by_us), "${HabayebMathHelper.formatSmart(summary.totalOwedToThem)} $currencySymbol", PdfColors.PAYMENT_TEXT)
        )
        tiles.forEachIndexed { index, (_, value, valueColorHex) ->
            val left = 35f + index * (tileW + gap)
            val tilePaint = Paint().apply { color = Color.WHITE; style = Paint.Style.FILL }
            val tileBorder = Paint().apply { color = Color.parseColor(PdfColors.HEADER_BORDER); strokeWidth = 0.5f; style = Paint.Style.STROKE }
            val valueP = Paint(valuePaint).apply { color = Color.parseColor(valueColorHex) }
            canvas.drawRoundRect(left, tileTop, left + tileW, tileTop + tileHeight, 5f, 5f, tilePaint)
            canvas.drawRoundRect(left, tileTop, left + tileW, tileTop + tileHeight, 5f, 5f, tileBorder)
            drawArabicText(canvas, tiles[index].first, left + 5f, tileTop + 7f, (tileW - 10f).roundToInt(), labelPaint, Layout.Alignment.ALIGN_CENTER)
            drawArabicText(canvas, value, left + 5f, tileTop + 25f, (tileW - 10f).roundToInt(), valueP, Layout.Alignment.ALIGN_CENTER)
        }

        val netLineY = tileTop + tileHeight + 5f
        drawArabicText(
            canvas,
            context.getString(R.string.pdf_kpi_net_position, "${HabayebMathHelper.formatSmart(netPrimary.abs())} $currencySymbol", netStatus),
            35f, netLineY, 525, netPaint, Layout.Alignment.ALIGN_CENTER
        )

        if (nonZeroForeign.isNotEmpty()) {
            val titlePaint = Paint().apply {
                color = Color.parseColor(primaryColorHex)
                textSize = 8.5f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }
            drawArabicText(canvas, context.getString(R.string.pdf_other_currencies_balances), 35f, startY + 68f, 525, titlePaint, Layout.Alignment.ALIGN_NORMAL)

            val foreignGap = 6f
            val cardW = (525f - foreignGap) / 2f
            nonZeroForeign.forEachIndexed { index, entry ->
                val col = index % 2
                val row = index / 2
                val left = 35f + col * (cardW + foreignGap)
                val top = startY + 82f + row * PdfReportLayoutSpec.comprehensiveForeignCardRowHeight()
                val right = left + cardW
                val bottom = top + PdfReportLayoutSpec.comprehensiveForeignCardRowHeight() - 4f
                val bg = Paint().apply { color = Color.parseColor(PdfColors.FOREIGN_ROW_BG); style = Paint.Style.FILL }
                val border = Paint().apply { color = Color.parseColor(PdfColors.HEADER_BORDER); strokeWidth = 0.6f; style = Paint.Style.STROKE }
                val accentColor = if (entry.value.net >= BigDecimal.ZERO) PdfColors.OWED_TEXT else PdfColors.PAYMENT_TEXT
                val accent = Paint().apply { color = Color.parseColor(accentColor); style = Paint.Style.FILL }
                canvas.drawRoundRect(left, top, right, bottom, 5f, 5f, bg)
                canvas.drawRoundRect(left, top, right, bottom, 5f, 5f, border)
                canvas.drawRoundRect(left, top, left + 3f, bottom, 3f, 3f, accent)

                val currency = com.smartledger.aldaftar.ui.screens.habayeb.utils.CurrencyConfig.getBySymbol(entry.key)
                val code = currency?.code ?: entry.key
                val symbol = currency?.symbol ?: entry.key
                val amountPaint = Paint().apply {
                    color = Color.parseColor(PdfColors.TEXT_DARK)
                    textSize = 9.5f
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    isAntiAlias = true
                }
                val statusPaint = Paint().apply {
                    color = Color.parseColor(accentColor)
                    textSize = 7.4f
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    isAntiAlias = true
                }
                val status = when {
                    entry.value.net > BigDecimal.ZERO -> context.getString(R.string.pdf_status_for_us)
                    entry.value.net < BigDecimal.ZERO -> context.getString(R.string.pdf_status_on_us)
                    else -> context.getString(R.string.pdf_status_balanced_word)
                }
                val grossText = "له: ${HabayebMathHelper.formatSmart(entry.value.owedByThem)} $symbol   •   عليه: ${HabayebMathHelper.formatSmart(entry.value.owedToThem)} $symbol"
                val netText = "الصافي: ${HabayebMathHelper.formatSmart(entry.value.net.abs())} $symbol • $status"
                drawArabicText(canvas, "$code — $symbol", left + 7f, top + 6f, (cardW - 14f).roundToInt(), amountPaint, Layout.Alignment.ALIGN_NORMAL)
                drawArabicText(canvas, grossText, left + 7f, top + 23f, (cardW - 14f).roundToInt(), statusPaint, Layout.Alignment.ALIGN_CENTER)
                drawArabicText(canvas, netText, left + 7f, top + 39f, (cardW - 14f).roundToInt(), statusPaint, Layout.Alignment.ALIGN_CENTER)
            }
        }
        return endY
    }
}

