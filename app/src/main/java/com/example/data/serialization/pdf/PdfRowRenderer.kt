package com.example.data.serialization.pdf

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.text.Layout
import com.example.R
import com.example.data.serialization.pdf.PdfDrawingUtils.drawArabicText
import com.example.domain.model.TransactionType
import com.example.ui.helper.HabayebMathHelper
import com.example.ui.screens.habayeb.utils.CurrencyConfig
import com.example.ui.state.CustomerUiState
import java.math.BigDecimal
import java.util.Date

object PdfRowRenderer {

    private val paintCardBg = Paint().apply {
        color = Color.parseColor(PdfColors.CARD_BG)
        style = Paint.Style.FILL
    }
    private val paintCardBorder = Paint().apply {
        color = Color.parseColor(PdfColors.HEADER_BORDER)
        strokeWidth = 0.5f
        style = Paint.Style.STROKE
    }
    private val paintSummaryTitle = Paint().apply {
        textSize = 10f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        isAntiAlias = true
    }
    private val paintSummaryText = Paint().apply {
        color = Color.parseColor(PdfColors.TEXT_MEDIUM)
        textSize = 9f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        isAntiAlias = true
    }

    // -------------------------------------------------------------------------
    // 1. Single Customer Transaction Rows & Details
    // -------------------------------------------------------------------------

    fun buildTransactionDescriptionText(
        context: Context,
        pt: ProcessedTransaction,
        initialType: String = TransactionType.OWED_BY_THEM.value
    ): String {
        val tx = pt.tx
        val isTxForeign = pt.isTxForeign
        val isOwedToThemAccount = initialType == TransactionType.OWED_TO_THEM.value

        val txTypeStr = when (tx.type) {
            TransactionType.OWED_BY_THEM.value -> context.getString(R.string.pdf_tx_type_owed_by_them)
            TransactionType.PAYMENT_BY_THEM.value -> if (isOwedToThemAccount) context.getString(R.string.pdf_tx_type_payment_to_them) else context.getString(R.string.pdf_tx_type_payment_by_them)
            TransactionType.OWED_TO_THEM.value -> context.getString(R.string.pdf_tx_type_owed_to_them)
            TransactionType.PAYMENT_TO_THEM.value -> context.getString(R.string.pdf_tx_type_payment_to_them)
            else -> context.getString(R.string.pdf_tx_type_new)
        }
        val cleanDetails = CurrencyConfig.getCleanDetails(tx.description)

        return buildString {
            append(txTypeStr)
            if (cleanDetails.isNotEmpty()) {
                append(" - ")
                append(cleanDetails)
            }
            if (isTxForeign) {
                append("\n[${tx.foreignAmount.toPlainString()} ${pt.resolvedCurrency}")
                if (tx.isRateCalculated) {
                    val formattedRate = HabayebMathHelper.formatRate(tx.exchangeRate)
                    append(context.getString(R.string.pdf_rate_suffix, formattedRate))
                } else {
                    append(context.getString(R.string.pdf_uncalculated_suffix))
                }
            }
        }
    }

    fun calculateTransactionRowHeight(
        context: Context,
        pt: ProcessedTransaction,
        initialType: String = TransactionType.OWED_BY_THEM.value,
        availableWidth: Int = 190
    ): Float {
        val descText = buildTransactionDescriptionText(context, pt, initialType)
        val textHeight = PdfDrawingUtils.measureTextHeight(descText, PdfPaints.textPaintDesc, availableWidth)
        return (textHeight + 14f).coerceAtLeast(32f)
    }

    fun drawSingleTransactionRow(
        canvas: Canvas,
        context: Context,
        index: Int,
        pt: ProcessedTransaction,
        currentY: Float,
        rowHeight: Float,
        runningBal: BigDecimal,
        initialType: String = TransactionType.OWED_BY_THEM.value
    ) {
        val tx = pt.tx
        val isTxForeign = pt.isTxForeign
        val resolvedAmount = pt.resolvedAmount

        if (isTxForeign) {
            canvas.drawRect(25f, currentY, 570f, currentY + rowHeight, PdfPaints.paintForeignBg)
        }

        // Horizontal bottom divider
        canvas.drawLine(25f, currentY + rowHeight, 570f, currentY + rowHeight, PdfPaints.paintRowDivider)

        // Vertical grid lines between columns
        canvas.drawLine(545f, currentY, 545f, currentY + rowHeight, PdfPaints.paintRowDivider)
        canvas.drawLine(455f, currentY, 455f, currentY + rowHeight, PdfPaints.paintRowDivider)
        canvas.drawLine(260f, currentY, 260f, currentY + rowHeight, PdfPaints.paintRowDivider)
        canvas.drawLine(180f, currentY, 180f, currentY + rowHeight, PdfPaints.paintRowDivider)
        canvas.drawLine(100f, currentY, 100f, currentY + rowHeight, PdfPaints.paintRowDivider)

        val textYOffset = (rowHeight - 12f) / 2f

        // Col 1: Sequence Number (#)
        val seqNo = (index + 1).toString()
        drawArabicText(canvas, seqNo, 545f, currentY + textYOffset, 25, PdfPaints.paintCellNormal, Layout.Alignment.ALIGN_CENTER)

        // Col 2: Date
        val txDate = Date(tx.timestamp * 1000)
        val dayName = try { PdfPageRenderer.formatDayAr(txDate) } catch (e: Exception) { "" }
        val formattedDate = try { PdfPageRenderer.formatDateEn(txDate) } catch (e: Exception) { "" }
        if (rowHeight > 32f) {
            val dateYOffset = (rowHeight - 24f) / 2f
            drawArabicText(canvas, dayName, 455f, currentY + dateYOffset, 90, PdfPaints.paintDayText, Layout.Alignment.ALIGN_CENTER)
            drawArabicText(canvas, formattedDate, 455f, currentY + dateYOffset + 12f, 90, PdfPaints.paintDateText, Layout.Alignment.ALIGN_CENTER)
        } else {
            drawArabicText(canvas, formattedDate, 455f, currentY + textYOffset, 90, PdfPaints.paintDateText, Layout.Alignment.ALIGN_CENTER)
        }

        // Col 3: Details with Dynamic StaticLayout
        val txLabel = buildTransactionDescriptionText(context, pt, initialType)
        val layoutDesc = PdfDrawingUtils.createStaticLayout(txLabel, PdfPaints.textPaintDesc, 190, Layout.Alignment.ALIGN_NORMAL)
        val descYOffset = ((rowHeight - layoutDesc.height) / 2f).coerceAtLeast(2f)
        PdfDrawingUtils.drawStaticLayout(canvas, layoutDesc, 262f, currentY + descYOffset)

        // Amounts
        val formattedAmount = if (isTxForeign) {
            if (tx.isRateCalculated) {
                HabayebMathHelper.formatSmart(resolvedAmount)
            } else "-"
        } else {
            HabayebMathHelper.formatSmart(resolvedAmount)
        }

        // Col 4 & Col 5
        val isOwedToThemAccount = initialType == TransactionType.OWED_TO_THEM.value
        val isCol4 = if (isOwedToThemAccount) {
            tx.type == TransactionType.OWED_TO_THEM.value || tx.type == TransactionType.PAYMENT_BY_THEM.value
        } else {
            tx.type == TransactionType.OWED_BY_THEM.value || tx.type == TransactionType.PAYMENT_TO_THEM.value
        }

        if (isCol4) {
            // Col 4 Badge & Text (180f)
            val badgeLeft = 184f
            val badgeTop = currentY + ((rowHeight - 18f) / 2f)
            val badgeRight = 256f
            val badgeBottom = badgeTop + 18f
            val badgePaint = if (isOwedToThemAccount) PdfPaints.paintPaymentBg else PdfPaints.paintOwedBg
            val textPaint = if (isOwedToThemAccount) PdfPaints.paintPaymentText else PdfPaints.paintOwedText

            canvas.drawRoundRect(badgeLeft, badgeTop, badgeRight, badgeBottom, 3f, 3f, badgePaint)
            drawArabicText(canvas, formattedAmount, 180f, currentY + textYOffset, 80, textPaint, Layout.Alignment.ALIGN_CENTER)

            // Col 5: empty dash (-)
            drawArabicText(canvas, "-", 100f, currentY + textYOffset, 80, PdfPaints.paintEmptyDash, Layout.Alignment.ALIGN_CENTER)
        } else {
            // Col 4: empty dash (-)
            drawArabicText(canvas, "-", 180f, currentY + textYOffset, 80, PdfPaints.paintEmptyDash, Layout.Alignment.ALIGN_CENTER)

            // Col 5 Badge & Text (100f)
            val badgeLeft = 104f
            val badgeTop = currentY + ((rowHeight - 18f) / 2f)
            val badgeRight = 176f
            val badgeBottom = badgeTop + 18f
            val badgePaint = if (isOwedToThemAccount) PdfPaints.paintOwedBg else PdfPaints.paintPaymentBg
            val textPaint = if (isOwedToThemAccount) PdfPaints.paintOwedText else PdfPaints.paintPaymentText

            canvas.drawRoundRect(badgeLeft, badgeTop, badgeRight, badgeBottom, 3f, 3f, badgePaint)
            drawArabicText(canvas, formattedAmount, 100f, currentY + textYOffset, 80, textPaint, Layout.Alignment.ALIGN_CENTER)
        }

        // Col 6: Running Balance (الرصيد)
        val formattedRunning = HabayebMathHelper.formatSmart(runningBal.abs())
        val isBalanced = runningBal.compareTo(BigDecimal.ZERO) == 0
        val isPositive = runningBal.compareTo(BigDecimal.ZERO) > 0
        val runningBalColor = when {
            isBalanced -> PdfColors.TEXT_DARK
            isOwedToThemAccount -> if (isPositive) PdfColors.PAYMENT_TEXT else PdfColors.OWED_TEXT
            else -> if (isPositive) PdfColors.OWED_TEXT else PdfColors.PAYMENT_TEXT
        }
        val paintRunning = Paint(PdfPaints.paintCellBold).apply { color = Color.parseColor(runningBalColor) }
        val balText = if (isBalanced) "-" else formattedRunning
        drawArabicText(canvas, balText, 25f, currentY + textYOffset, 75, paintRunning, Layout.Alignment.ALIGN_CENTER)
    }

    // -------------------------------------------------------------------------
    // 2. Statement Totals, Net Banner & Foreign Currencies
    // -------------------------------------------------------------------------

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

        // Draw vertical column dividers
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
            color = Color.parseColor(if (isOwedByThemStatus) "#FCA5A5" else if (isOwedToThemStatus) "#86EFAC" else PdfColors.HEADER_BORDER)
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

        var y = currentY + 4f
        val itemHeight = 20f
        val boxHeight = 24f + (uncalculatedForeignSums.size * itemHeight)

        val paintBg = Paint().apply {
            color = Color.parseColor("#F8FAFC")
            style = Paint.Style.FILL
        }
        val paintBorder = Paint().apply {
            color = Color.parseColor("#CBD5E1")
            strokeWidth = 1f
            style = Paint.Style.STROKE
        }
        val paintTitle = Paint().apply {
            color = Color.parseColor(PdfColors.PRIMARY_EMERALD)
            textSize = 9.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        val paintItem = Paint().apply {
            color = Color.parseColor(PdfColors.TEXT_CHARCOAL)
            textSize = 9f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        canvas.drawRoundRect(25f, y, 570f, y + boxHeight, 4f, 4f, paintBg)
        canvas.drawRoundRect(25f, y, 570f, y + boxHeight, 4f, 4f, paintBorder)

        drawArabicText(canvas, context.getString(R.string.pdf_independent_totals_uncalculated), 35f, y + 6f, 520, paintTitle, Layout.Alignment.ALIGN_NORMAL)

        var itemY = y + 24f
        for ((curr, amount) in uncalculatedForeignSums) {
            val isPositive = amount.compareTo(BigDecimal.ZERO) > 0
            val isNegative = amount.compareTo(BigDecimal.ZERO) < 0
            val statusText = if (isPositive) context.getString(R.string.pdf_status_owed_word) else if (isNegative) context.getString(R.string.pdf_status_to_him_word) else context.getString(R.string.pdf_status_balanced_word)
            val foreignTag = context.getString(R.string.pdf_foreign_currency_tag)
            val lineStr = "• ${context.getString(R.string.pdf_total_currency_prefix, curr)}: ${HabayebMathHelper.formatSmart(amount.abs())} $curr ($statusText - $foreignTag)"
            drawArabicText(canvas, lineStr, 40f, itemY, 510, paintItem, Layout.Alignment.ALIGN_NORMAL)
            itemY += itemHeight
        }

        return y + boxHeight + 6f
    }

    // -------------------------------------------------------------------------
    // 3. Customer Summary Rows (Comprehensive & All-Customers Report)
    // -------------------------------------------------------------------------

    fun calculateCustomerSummaryRowHeight(
        context: Context,
        c: CustomerUiState,
        nameWidth: Int = 175,
        foreignWidth: Int = 125
    ): Float {
        val nameHeight = PdfDrawingUtils.measureTextHeight(c.name, PdfPaints.paintCellBold, nameWidth)
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

        // Vertical dividers
        canvas.drawLine(535f, currentY, 535f, currentY + rowHeight, PdfPaints.paintRowDivider)
        canvas.drawLine(360f, currentY, 360f, currentY + rowHeight, PdfPaints.paintRowDivider)
        canvas.drawLine(230f, currentY, 230f, currentY + rowHeight, PdfPaints.paintRowDivider)
        canvas.drawLine(105f, currentY, 105f, currentY + rowHeight, PdfPaints.paintRowDivider)

        val textYOffset = (rowHeight - 12f) / 2f

        // Col 1: Index
        drawArabicText(canvas, (index + 1).toString(), 535f, currentY + textYOffset, 35, PdfPaints.paintCellNormal, Layout.Alignment.ALIGN_CENTER)

        // Col 2: Name & Phone with Dynamic Layout
        val nameLayout = PdfDrawingUtils.createStaticLayout(c.name, PdfPaints.paintCellBold, 170, Layout.Alignment.ALIGN_NORMAL)
        val nameTotalH = nameLayout.height + if (c.phone.isNotBlank()) 14f else 0f
        val nameYOffset = ((rowHeight - nameTotalH) / 2f).coerceAtLeast(3f)

        PdfDrawingUtils.drawStaticLayout(canvas, nameLayout, 365f, currentY + nameYOffset)
        if (c.phone.isNotBlank()) {
            drawArabicText(canvas, c.phone, 365f, currentY + nameYOffset + nameLayout.height + 1f, 170, PdfPaints.paintMutedText, Layout.Alignment.ALIGN_NORMAL)
        }

        // Col 3: Primary Balance
        val totalBd = c.defaultCurrencyTotal
        val isPositive = totalBd.compareTo(BigDecimal.ZERO) > 0
        val isNegative = totalBd.compareTo(BigDecimal.ZERO) < 0
        val formattedPrimary = HabayebMathHelper.formatSmart(totalBd.abs()) + " " + currencySymbol
        val balancePaint = if (isPositive) PdfPaints.paintOwedText else if (isNegative) PdfPaints.paintPaymentText else PdfPaints.paintCellNormal
        drawArabicText(canvas, formattedPrimary, 230f, currentY + textYOffset, 130, balancePaint, Layout.Alignment.ALIGN_CENTER)

        // Col 4: Foreign Currencies with Dynamic Layout
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

        // Col 5: Status
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

    // -------------------------------------------------------------------------
    // 4. Booklet Index Table Header & Rows
    // -------------------------------------------------------------------------

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

        // Vertical dividers
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

        // Vertical dividers
        canvas.drawLine(535f, currentY, 535f, currentY + rowHeight, PdfPaints.paintRowDivider)
        canvas.drawLine(305f, currentY, 305f, currentY + rowHeight, PdfPaints.paintRowDivider)
        canvas.drawLine(205f, currentY, 205f, currentY + rowHeight, PdfPaints.paintRowDivider)
        canvas.drawLine(105f, currentY, 105f, currentY + rowHeight, PdfPaints.paintRowDivider)

        val textYOffset = (rowHeight - 12f) / 2f

        // Column: No (م)
        drawArabicText(canvas, (index + 1).toString(), 535f, currentY + textYOffset, 35, PdfPaints.paintCellNormal, Layout.Alignment.ALIGN_CENTER)

        // Column: Name with Dynamic Layout
        val nameLayout = PdfDrawingUtils.createStaticLayout(customer.name, PdfPaints.paintCellBold, 225, Layout.Alignment.ALIGN_NORMAL)
        val nameYOffset = ((rowHeight - nameLayout.height) / 2f).coerceAtLeast(2f)
        PdfDrawingUtils.drawStaticLayout(canvas, nameLayout, 310f, currentY + nameYOffset)

        // Column: Phone
        drawArabicText(canvas, customer.phone.ifEmpty { "-" }, 205f, currentY + textYOffset, 100, PdfPaints.paintCellNormal, Layout.Alignment.ALIGN_NORMAL)

        // Column: Final Balance
        val balText = "${HabayebMathHelper.formatSmart(customer.defaultCurrencyTotal.abs())} $currencySymbol"
        drawArabicText(canvas, balText, 105f, currentY + textYOffset, 100, PdfPaints.paintCellBold, Layout.Alignment.ALIGN_CENTER)

        // Column: Status
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

    // -------------------------------------------------------------------------
    // 5. Comprehensive Summary Card
    // -------------------------------------------------------------------------

    fun drawComprehensiveSummaryCard(
        canvas: Canvas,
        context: Context,
        primaryColorHex: String,
        summary: ComprehensivePdfSummary,
        totalItems: Int,
        currencySymbol: String
    ) {
        canvas.drawRoundRect(25f, 155f, 570f, 235f, 6f, 6f, paintCardBg)
        canvas.drawRoundRect(25f, 155f, 570f, 235f, 6f, 6f, paintCardBorder)

        paintSummaryTitle.color = Color.parseColor(primaryColorHex)

        drawArabicText(canvas, context.getString(R.string.pdf_general_summary_title), 35f, 163f, 520, paintSummaryTitle, Layout.Alignment.ALIGN_NORMAL)

        val netPrimary = summary.netPrimary
        val netPrimaryFormatted = HabayebMathHelper.formatSmart(netPrimary.abs()) + " " + currencySymbol
        val netPrimaryStatus = if (netPrimary.compareTo(BigDecimal.ZERO) > 0) {
            context.getString(R.string.pdf_status_for_us)
        } else if (netPrimary.compareTo(BigDecimal.ZERO) < 0) {
            context.getString(R.string.pdf_status_on_us)
        } else {
            context.getString(R.string.pdf_status_balanced_word)
        }

        val primarySummary = context.getString(
            R.string.pdf_comprehensive_accounts_summary,
            totalItems,
            currencySymbol,
            HabayebMathHelper.formatSmart(summary.totalOwedByThem),
            HabayebMathHelper.formatSmart(summary.totalOwedToThem),
            netPrimaryFormatted,
            netPrimaryStatus
        )

        drawArabicText(canvas, primarySummary, 35f, 180f, 520, paintSummaryText, Layout.Alignment.ALIGN_NORMAL)

        val nonZeroForeign = summary.foreignTotalsMap.filter { it.value.compareTo(BigDecimal.ZERO) != 0 }
        val foreignSummary = if (nonZeroForeign.isEmpty()) {
            context.getString(R.string.pdf_no_pending_balances_foreign)
        } else {
            context.getString(R.string.pdf_other_currencies_balances) + nonZeroForeign.entries.joinToString("  |  ") { (curr, bd) ->
                val status = if (bd.compareTo(BigDecimal.ZERO) > 0) {
                    context.getString(R.string.pdf_status_for_us)
                } else {
                    context.getString(R.string.pdf_status_on_us)
                }
                "$curr: " + HabayebMathHelper.formatSmart(bd.abs()) + " ($status)"
            }
        }
        drawArabicText(canvas, foreignSummary, 35f, 198f, 520, paintSummaryText, Layout.Alignment.ALIGN_NORMAL)
    }
}
