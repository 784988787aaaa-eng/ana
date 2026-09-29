package com.smartledger.aldaftar.data.serialization.pdf

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.text.Layout
import com.smartledger.aldaftar.R
import com.smartledger.aldaftar.data.serialization.pdf.PdfDrawingUtils.drawArabicText
import com.smartledger.aldaftar.domain.model.TransactionType
import com.smartledger.aldaftar.ui.helper.HabayebMathHelper
import com.smartledger.aldaftar.ui.screens.habayeb.utils.CurrencyConfig
import java.math.BigDecimal
import kotlin.math.roundToInt

object PdfStatementTotalsRenderer {

    fun drawTotalsRow(
        canvas: Canvas,
        context: Context,
        currentY: Float,
        totalDebts: BigDecimal,
        totalPayments: BigDecimal,
        currencySymbol: String,
        initialType: String = TransactionType.OWED_BY_THEM.value
    ): Float {
        val rowHeight = 24f
        val paintBg = Paint().apply {
            color = Color.parseColor(PdfColors.TOTALS_ROW_BG)
            style = Paint.Style.FILL
        }
        canvas.drawRect(25f, currentY, 570f, currentY + rowHeight, paintBg)
        canvas.drawLine(25f, currentY, 570f, currentY, PdfPaints.paintRowDivider)
        canvas.drawLine(25f, currentY + rowHeight, 570f, currentY + rowHeight, PdfPaints.paintRowDivider)

        canvas.drawLine(545f, currentY, 545f, currentY + rowHeight, PdfPaints.paintRowDivider)
        canvas.drawLine(455f, currentY, 455f, currentY + rowHeight, PdfPaints.paintRowDivider)
        canvas.drawLine(260f, currentY, 260f, currentY + rowHeight, PdfPaints.paintRowDivider)
        canvas.drawLine(180f, currentY, 180f, currentY + rowHeight, PdfPaints.paintRowDivider)
        canvas.drawLine(100f, currentY, 100f, currentY + rowHeight, PdfPaints.paintRowDivider)

        val paintTitle = Paint().apply {
            color = Color.parseColor(PdfColors.TEXT_DARK)
            textSize = 9.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        drawArabicText(canvas, "-", 545f, currentY + 5f, 25, PdfPaints.paintEmptyDash, Layout.Alignment.ALIGN_CENTER)
        drawArabicText(canvas, "-", 455f, currentY + 5f, 90, PdfPaints.paintEmptyDash, Layout.Alignment.ALIGN_CENTER)
        drawArabicText(canvas, context.getString(R.string.pdf_totals_operations_title), 260f, currentY + 5f, 195, paintTitle, Layout.Alignment.ALIGN_NORMAL)

        val isOwedToThemAccount = initialType == TransactionType.OWED_TO_THEM.value
        val col4Color = if (isOwedToThemAccount) PdfColors.PAYMENT_TEXT else PdfColors.OWED_TEXT
        val col5Color = if (isOwedToThemAccount) PdfColors.OWED_TEXT else PdfColors.PAYMENT_TEXT

        val formattedDebts = HabayebMathHelper.formatSmart(totalDebts)
        val paintDebts = Paint(PdfPaints.paintCellBold).apply {
            color = Color.parseColor(col4Color)
        }
        drawArabicText(canvas, formattedDebts, 180f, currentY + 5f, 80, paintDebts, Layout.Alignment.ALIGN_CENTER)

        val formattedPayments = HabayebMathHelper.formatSmart(totalPayments)
        val paintPayments = Paint(PdfPaints.paintCellBold).apply {
            color = Color.parseColor(col5Color)
        }
        drawArabicText(canvas, formattedPayments, 100f, currentY + 5f, 80, paintPayments, Layout.Alignment.ALIGN_CENTER)

        drawArabicText(canvas, "-", 25f, currentY + 5f, 75, PdfPaints.paintEmptyDash, Layout.Alignment.ALIGN_CENTER)

        return currentY + rowHeight
    }

    fun drawFinalNetBanner(
        canvas: Canvas,
        context: Context,
        currentY: Float,
        netBalance: BigDecimal,
        currencySymbol: String,
        initialType: String = TransactionType.OWED_BY_THEM.value
    ): Float {
        val bannerHeight = 30f
        val isOwedToThemAccount = initialType == TransactionType.OWED_TO_THEM.value
        val rawPositive = netBalance.compareTo(BigDecimal.ZERO) > 0
        val rawNegative = netBalance.compareTo(BigDecimal.ZERO) < 0

        val isOwedToThemStatus = if (isOwedToThemAccount) rawPositive else rawNegative
        val isOwedByThemStatus = if (isOwedToThemAccount) rawNegative else rawPositive

        val bannerBgColor = when {
            isOwedByThemStatus -> PdfColors.BANNER_OWED_BG
            isOwedToThemStatus -> PdfColors.BANNER_PAYMENT_BG
            else -> PdfColors.TOTALS_ROW_BG
        }

        val paintBannerBg = Paint().apply {
            color = Color.parseColor(bannerBgColor)
            style = Paint.Style.FILL
        }
        val paintBannerBorder = Paint().apply {
            color = Color.parseColor(if (isOwedByThemStatus) PdfColors.DEBT_BORDER else if (isOwedToThemStatus) PdfColors.CREDIT_BORDER else PdfColors.HEADER_BORDER)
            strokeWidth = 1f
            style = Paint.Style.STROKE
        }

        canvas.drawRoundRect(25f, currentY, 570f, currentY + bannerHeight, 4f, 4f, paintBannerBg)
        canvas.drawRoundRect(25f, currentY, 570f, currentY + bannerHeight, 4f, 4f, paintBannerBorder)

        val statusText = when {
            isOwedByThemStatus -> context.getString(R.string.pdf_net_banner_owed_by)
            isOwedToThemStatus -> context.getString(R.string.pdf_net_banner_owed_to)
            else -> context.getString(R.string.pdf_net_banner_balanced)
        }

        val textColor = when {
            isOwedByThemStatus -> PdfColors.OWED_TEXT
            isOwedToThemStatus -> PdfColors.PAYMENT_TEXT
            else -> PdfColors.TEXT_DARK
        }

        val paintTextLabel = Paint().apply {
            color = Color.parseColor(textColor)
            textSize = 10.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val formattedAmount = "${HabayebMathHelper.formatSmart(netBalance.abs())} $currencySymbol"

        drawArabicText(canvas, statusText, 250f, currentY + 7f, 310, paintTextLabel, Layout.Alignment.ALIGN_NORMAL)
        drawArabicText(canvas, formattedAmount, 35f, currentY + 7f, 200, paintTextLabel, Layout.Alignment.ALIGN_OPPOSITE)

        return currentY + bannerHeight + 8f
    }

    fun drawForeignCurrenciesSummary(
        canvas: Canvas,
        context: Context,
        currentY: Float,
        uncalculatedForeignSums: Map<String, BigDecimal>,
        currencySymbol: String
    ): Float {
        if (uncalculatedForeignSums.isEmpty()) return currentY

        val entries = uncalculatedForeignSums.entries
            .filter { it.value.compareTo(BigDecimal.ZERO) != 0 }
            .sortedBy { it.key }
        if (entries.isEmpty()) return currentY

        val sectionTop = currentY + 6f
        val cardGap = 8f
        val cardWidth = (545f - cardGap) / 2f
        val cardHeight = 36f
        val rows = kotlin.math.ceil(entries.size / 2.0).toInt()
        val sectionHeight = PdfReportLayoutSpec.foreignCurrencySectionHeight(entries.size)

        val paintTitle = Paint().apply {
            color = Color.parseColor(PdfColors.PRIMARY_EMERALD)
            textSize = 9.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        drawArabicText(
            canvas,
            context.getString(R.string.pdf_independent_totals_uncalculated),
            25f, sectionTop + 6f, 545, paintTitle, Layout.Alignment.ALIGN_NORMAL
        )

        entries.forEachIndexed { index, entry ->
            val column = index % 2
            val row = index / 2
            val left = if (column == 0) 25f else 25f + cardWidth + cardGap
            val top = sectionTop + PdfReportLayoutSpec.foreignCurrencyCardItemHeight() * row + 28f
            val right = left + cardWidth
            val bottom = top + cardHeight

            val cardPaint = Paint().apply {
                color = Color.parseColor(PdfColors.FOREIGN_ROW_BG)
                style = Paint.Style.FILL
            }
            val statusColor = if (entry.value > BigDecimal.ZERO) PdfColors.OWED_TEXT else PdfColors.PAYMENT_TEXT
            val borderPaint = Paint().apply {
                color = Color.parseColor(PdfColors.HEADER_BORDER)
                strokeWidth = 0.7f
                style = Paint.Style.STROKE
            }
            val accentPaint = Paint().apply {
                color = Color.parseColor(statusColor)
                style = Paint.Style.FILL
            }
            canvas.drawRoundRect(left, top, right, bottom, 6f, 6f, cardPaint)
            canvas.drawRoundRect(left, top, right, bottom, 6f, 6f, borderPaint)
            canvas.drawRoundRect(left, top, left + 3.5f, bottom, 3f, 3f, accentPaint)

            val currency = CurrencyConfig.getBySymbol(entry.key)
            val code = currency?.code ?: entry.key
            val symbol = currency?.symbol ?: entry.key
            val name = currency?.arabicName.orEmpty()
            val amount = entry.value.abs()
            val statusText = if (entry.value > BigDecimal.ZERO) {
                context.getString(R.string.pdf_status_owed_word)
            } else {
                context.getString(R.string.pdf_status_to_him_word)
            }
            val statusPaint = Paint().apply {
                color = Color.parseColor(if (entry.value > BigDecimal.ZERO) PdfColors.OWED_TEXT else PdfColors.PAYMENT_TEXT)
                textSize = 8f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }
            val codePaint = Paint().apply {
                color = Color.parseColor(PdfColors.TEXT_DARK)
                textSize = 9.5f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }
            val amountPaint = Paint().apply {
                color = Color.parseColor(PdfColors.TEXT_CHARCOAL)
                textSize = 12f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }
            val namePaint = Paint().apply {
                color = Color.parseColor(PdfColors.TEXT_MEDIUM)
                textSize = 7.5f
                typeface = Typeface.DEFAULT
                isAntiAlias = true
            }

            drawArabicText(canvas, "$code — $symbol", left + 8f, top + 5f, (cardWidth - 16f).roundToInt(), codePaint, Layout.Alignment.ALIGN_NORMAL)
            if (name.isNotBlank()) {
                drawArabicText(canvas, name, left + 8f, top + 5f, (cardWidth - 16f).roundToInt(), namePaint, Layout.Alignment.ALIGN_NORMAL)
            }
            drawArabicText(canvas, "${HabayebMathHelper.formatSmart(amount)} $symbol", left + 8f, top + 18f, (cardWidth - 16f).roundToInt(), amountPaint, Layout.Alignment.ALIGN_CENTER)
            drawArabicText(canvas, "$statusText • ${context.getString(R.string.pdf_foreign_currency_tag)}", left + 8f, top + 5f, (cardWidth - 16f).roundToInt(), statusPaint, Layout.Alignment.ALIGN_CENTER)
        }

        return sectionTop + sectionHeight
    }
}

