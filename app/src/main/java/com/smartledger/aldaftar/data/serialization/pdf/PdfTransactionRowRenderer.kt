/**
 * =====================================================================
 * ملف: رسام صفوف المعاملات الفردية في  (.)
 * =====================================================================
 * 
 * [الغرض العام والتعليمي من الملف]:
 * يختص هذا الكائن بالرسم التفصيلي الدقيق لكل معاملة مالية داخل جدول كشف حساب العميل.
 * يتولى مسؤولية صياغة الوصف المالي المفصل (نوع العملية، البيان، ملاحظات سعر الصرف أو النقد الأجنبي)،
 * وقياس الارتفاع الرأسي الديناميكي للصف بناءً على طول الوصف، ورسم الخلايا الست:
 * 1. رقم التسلسل (#).
 * 2. التاريخ واسم اليوم بالعربية.
 * 3. البيان وتفاصيل الصرف بـ [].
 * 4. المبلغ المدين (لنا) مع شارة ملونة.
 * 5. المبلغ الدائن (علينا / دفعة) مع شارة ملونة.
 * 6. الرصيد التراكمي بعد العملية بتلوين دلالي.
 * 
 * [المسؤوليات المعمارية والتقنية]:
 * 1. الصياغة الوصفية المتقدمة للمعاملات (  ):
 *    - إظهار عمليات تحويل العملات مع سعر الصرف المعتمد والمبلغ الأصلي.
 * 2. الحساب الديناميكي لارتفاع الصف (   ):
 *    - قياس أسطر البيان لتفادي تداخل النصوص أو اقتطاعها.
 * 3. رسم الشارات الملونة للأرقام (  ):
 *    - رسم مستطيلات ذات حواف منحنية خلف المبالغ للتمييز السريع بين المقبوضات والمديونيات.
 */
package com.smartledger.aldaftar.data.serialization.pdf

// ---------------------------------------------------------------------
// استيراد حزم سياق أندرويد والرسومات وتخطيط النصوص والرياضيات والتواريخ
// ---------------------------------------------------------------------
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.text.Layout
import com.smartledger.aldaftar.R
import com.smartledger.aldaftar.data.serialization.pdf.PdfDrawingUtils.drawArabicText
import com.smartledger.aldaftar.domain.model.TransactionType
import com.smartledger.aldaftar.ui.helper.HabayebMathHelper
import com.smartledger.aldaftar.ui.screens.habayeb.utils.CurrencyConfig
import java.math.BigDecimal
import java.util.Date

/**
 * [الكائن الأحادي لرسم صفوف المعاملات - ]:
 * يقدم وظائف بناء البيان وحساب الارتفاع ورسم صفوف المعاملات الفردية.
 */
object PdfTransactionRowRenderer {

    /**
     * [بناء النص التوضيحي المفصل للمعاملة - ]:
     * يجمع نوع الحركة مع البيان المخصص ومعلومات سعر الصرف أو النقد الأجنبي.
     *
     * @  سياق التطبيق لجلب مسميات أنواع المعاملات.
     * @  كائن المعاملة المعالجة.
     * @  طبيعة الحساب الأصلية (لنا أم علينا).
     * @ النص التوضيحي المكتمل للطباعة.
     */
    fun buildTransactionDescriptionText(
        context: Context,
        pt: ProcessedTransaction,
        initialType: String = TransactionType.OWED_BY_THEM.value
    ): String {
        val tx = pt.tx
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
            if (tx.isRateCalculated) {
                val origCurrency = CurrencyConfig.getBySymbol(tx.currencyCode)?.symbol ?: tx.currencyCode
                val origAmount = if (tx.foreignAmount.compareTo(BigDecimal.ZERO) > 0) tx.foreignAmount else tx.amount
                val formattedAmount = HabayebMathHelper.formatSmart(origAmount)
                val formattedRate = HabayebMathHelper.formatRate(tx.exchangeRate)
                append("\n[ صرف: $formattedAmount $origCurrency × $formattedRate ]")
            } else if (pt.isTxForeign) {
                val origCurrency = CurrencyConfig.getBySymbol(tx.currencyCode)?.symbol ?: tx.currencyCode
                val origAmount = if (tx.foreignAmount.compareTo(BigDecimal.ZERO) > 0) tx.foreignAmount else tx.amount
                val formattedAmount = HabayebMathHelper.formatSmart(origAmount)
                append("\n[ $formattedAmount $origCurrency - نقد أجنبي ]")
            }
        }
    }

    /**
     * [حساب الارتفاع الرأسي لصف المعاملة - ]:
     * يقيس ارتفاع النص المتولد ضمن العرض المتاح مع إضافة الهوامش القياسية.
     *
     * @  سياق التطبيق.
     * @  كائن المعاملة المعالجة.
     * @  طبيعة الحساب.
     * @  العرض المخصص لعمود البيان بالنقاط (افتراضياً 190).
     * @ الارتفاع الرأسي المحسوب بالنقاط (بحد أدنى 32 نقطة).
     */
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

    /**
     * [رسم صف معاملة فردية في كشف الحساب - ]:
     * يرسم خلايا الصف الست وخطوط الشبكة والشارات التوضيحية للأرصدة.
     *
     * @  لوحة الرسم.
     * @  سياق التطبيق.
     * @  ترتيب المعاملة التسلسلي (يبدأ من 0).
     * @  كائن المعاملة المعالجة.
     * @  الإحداثي الرأسي للرسم.
     * @  الارتفاع المحسوب للصف.
     * @  الرصيد التراكمي المحسوب بعد هذه المعاملة.
     * @  طبيعة الحساب.
     */
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
        val hasBaseAmount = pt.baseCurrencyAmount.compareTo(BigDecimal.ZERO) > 0

        if (isTxForeign) {
            canvas.drawRect(25f, currentY, 570f, currentY + rowHeight, PdfPaints.paintForeignBg)
        }

        // رسم الفاصل الأفقي السفلي للصف.
        canvas.drawLine(25f, currentY + rowHeight, 570f, currentY + rowHeight, PdfPaints.paintRowDivider)

        // رسم الفواصل الرأسية الفاصلة بين الأعمدة.
        canvas.drawLine(545f, currentY, 545f, currentY + rowHeight, PdfPaints.paintRowDivider)
        canvas.drawLine(455f, currentY, 455f, currentY + rowHeight, PdfPaints.paintRowDivider)
        canvas.drawLine(260f, currentY, 260f, currentY + rowHeight, PdfPaints.paintRowDivider)
        canvas.drawLine(180f, currentY, 180f, currentY + rowHeight, PdfPaints.paintRowDivider)
        canvas.drawLine(100f, currentY, 100f, currentY + rowHeight, PdfPaints.paintRowDivider)

        val textYOffset = (rowHeight - 12f) / 2f

        // رسم العمود الأول الخاص برقم التسلسل.
        val seqNo = (index + 1).toString()
        drawArabicText(canvas, seqNo, 545f, currentY + textYOffset, 25, PdfPaints.paintCellNormal, Layout.Alignment.ALIGN_CENTER)

        // رسم العمود الثاني الذي يجمع التاريخ واسم اليوم في صف واحد.
        val txTimestampMs = if (tx.timestamp > 1000000000000L) tx.timestamp else tx.timestamp * 1000
        val txDate = Date(txTimestampMs)
        val dayName = try { PdfPageRenderer.formatDayAr(txDate) } catch (e: Exception) { "" }
        val formattedDate = try { PdfPageRenderer.formatDateEn(txDate) } catch (e: Exception) { "" }
        val fullDateStr = if (dayName.isNotBlank()) "$dayName $formattedDate" else formattedDate

        val layoutDate = PdfDrawingUtils.createStaticLayout(fullDateStr, PdfPaints.paintDateText, 90, Layout.Alignment.ALIGN_CENTER)
        val dateYOffset = ((rowHeight - layoutDate.height) / 2f).coerceAtLeast(2f)
        PdfDrawingUtils.drawStaticLayout(canvas, layoutDate, 455f, currentY + dateYOffset)

        // رسم العمود الثالث بتخطيط نصي ديناميكي لتفادي اقتطاع البيان.
        val txLabel = buildTransactionDescriptionText(context, pt, initialType)
        val layoutDesc = PdfDrawingUtils.createStaticLayout(txLabel, PdfPaints.textPaintDesc, 190, Layout.Alignment.ALIGN_NORMAL)
        val descYOffset = ((rowHeight - layoutDesc.height) / 2f).coerceAtLeast(2f)
        PdfDrawingUtils.drawStaticLayout(canvas, layoutDesc, 262f, currentY + descYOffset)

        // تجهيز القيم المالية للعرض دون تغيير قيمها الحسابية الأصلية.
        val formattedAmount = if (hasBaseAmount) {
            HabayebMathHelper.formatSmart(pt.baseCurrencyAmount)
        } else "-"

        // توزيع المبلغ بين عمودي المدين والدائن وفق نوع الحركة.
        val isOwedToThemAccount = initialType == TransactionType.OWED_TO_THEM.value
        val isCol4 = if (isOwedToThemAccount) {
            tx.type == TransactionType.OWED_TO_THEM.value || tx.type == TransactionType.PAYMENT_BY_THEM.value
        } else {
            tx.type == TransactionType.OWED_BY_THEM.value || tx.type == TransactionType.PAYMENT_TO_THEM.value
        }

        if (hasBaseAmount) {
            if (isCol4) {
                // رسم شارة المبلغ في العمود الرابع مع إبقاء القيمة العشرية كما هي.
                val badgeLeft = 184f
                val badgeTop = currentY + ((rowHeight - 18f) / 2f)
                val badgeRight = 256f
                val badgeBottom = badgeTop + 18f
                val badgePaint = if (isOwedToThemAccount) PdfPaints.paintPaymentBg else PdfPaints.paintOwedBg
                val textPaint = if (isOwedToThemAccount) PdfPaints.paintPaymentText else PdfPaints.paintOwedText

                canvas.drawRoundRect(badgeLeft, badgeTop, badgeRight, badgeBottom, 3f, 3f, badgePaint)
                drawArabicText(canvas, formattedAmount, 180f, currentY + textYOffset, 80, textPaint, Layout.Alignment.ALIGN_CENTER)

                // إظهار علامة عدم وجود مبلغ في العمود الخامس.
                drawArabicText(canvas, "-", 100f, currentY + textYOffset, 80, PdfPaints.paintEmptyDash, Layout.Alignment.ALIGN_CENTER)
            } else {
                // إظهار علامة عدم وجود مبلغ في العمود الرابع.
                drawArabicText(canvas, "-", 180f, currentY + textYOffset, 80, PdfPaints.paintEmptyDash, Layout.Alignment.ALIGN_CENTER)

                // رسم شارة المبلغ في العمود الخامس مع إبقاء القيمة العشرية كما هي.
                val badgeLeft = 104f
                val badgeTop = currentY + ((rowHeight - 18f) / 2f)
                val badgeRight = 176f
                val badgeBottom = badgeTop + 18f
                val badgePaint = if (isOwedToThemAccount) PdfPaints.paintOwedBg else PdfPaints.paintPaymentBg
                val textPaint = if (isOwedToThemAccount) PdfPaints.paintOwedText else PdfPaints.paintPaymentText

                canvas.drawRoundRect(badgeLeft, badgeTop, badgeRight, badgeBottom, 3f, 3f, badgePaint)
                drawArabicText(canvas, formattedAmount, 100f, currentY + textYOffset, 80, textPaint, Layout.Alignment.ALIGN_CENTER)
            }

            // رسم العمود السادس للرصيد التراكمي المحسوب مسبقاً.
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
        } else {
            // الحركة تخص دفتر عملة أخرى، لذلك لا تدخل في مبالغ هذا الدفتر.
            drawArabicText(canvas, "-", 180f, currentY + textYOffset, 80, PdfPaints.paintEmptyDash, Layout.Alignment.ALIGN_CENTER)
            drawArabicText(canvas, "-", 100f, currentY + textYOffset, 80, PdfPaints.paintEmptyDash, Layout.Alignment.ALIGN_CENTER)
            drawArabicText(canvas, "-", 25f, currentY + textYOffset, 75, PdfPaints.paintEmptyDash, Layout.Alignment.ALIGN_CENTER)
        }
    }
}

