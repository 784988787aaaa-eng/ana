/**
 * =====================================================================
 * ملف: أدوات التلوين وأنماط الخطوط في PDF (PdfPaints.kt)
 * =====================================================================
 * 
 * [الغرض العام والتعليمي من الملف]:
 * يمثل هذا الكائن مستودعاً مركزياً لكائنات التلوين والفرش [Paint] ومحركات
 * خطوط النصوص [TextPaint] المستخدمة في رسم مستندات PDF.
 * من خلال تجميع كائنات [Paint] وإعادة استخدامها، نتفادى إنشاء كائنات جديدة
 * أثناء تكرار رسم الصفوف، مما يحسن الأداء ويمنع استنزاف الذاكرة أثناء توليد الدفاتر الكبيرة.
 * 
 * [المسؤوليات المعمارية والتقنية]:
 * 1. إعادة استخدام الكائنات وتخفيف العبء على جامع القمامة (GC Allocation Prevention):
 *    - تهيئة وتثبيت كائنات [Paint] مسبقاً لاستخدامها في كل خلايا الجداول.
 * 2. التوحيد البصري للألوان والخطوط (Visual Consistency):
 *    - تطبيق ثوابت [PdfColors] على أنماط الخطوط العادية والعريضة بدقة نقطية (Point Sizes).
 * 3. دعم تلوين المعاملات المالية (Semantic Financial Styling):
 *    - خلفيات ونصوص المديونيات [paintOwedBg, paintOwedText].
 *    - خلفيات ونصوص المقبوضات والمدفوعات [paintPaymentBg, paintPaymentText].
 */
package com.smartledger.aldaftar.data.serialization.pdf

// ---------------------------------------------------------------------
// استيراد حزم الرسومات والألوان والخطوط وتخطيط النصوص في أندرويد
// ---------------------------------------------------------------------
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.text.TextPaint

/**
 * [الكائن الأحادي لأدوات التلوين والخطوط - PdfPaints]:
 * يحتوي على كافة كائنات الرسم والتنسيق الجاهزة لمحرك PDF.
 */
object PdfPaints {

    /** نمط الخط القياسي العادي */
    private val TYPEFACE_NORMAL = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
    /** نمط الخط العريض للعناوين والأرقام البارزة */
    private val TYPEFACE_BOLD = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)

    /**
     * [إنشاء وتجهيز أداة تلوين نصية - createTextPaint]:
     * دالة مساعدة داخلية لإنشاء كائن [Paint] مع تفعيل تنعيم الحواف [isAntiAlias].
     */
    private fun createTextPaint(
        colorHex: String,
        textSizePt: Float,
        typeface: Typeface = TYPEFACE_NORMAL
    ): Paint = Paint().apply {
        color = Color.parseColor(colorHex)
        textSize = textSizePt
        this.typeface = typeface
        isAntiAlias = true
    }

    /** أداة تلوين النصوص العادية في خلايا الجدول */
    val paintCellNormal = createTextPaint(PdfColors.TEXT_CHARCOAL, 9.5f, TYPEFACE_NORMAL)
    /** أداة تلوين النصوص العريضة والأرقام الهامة */
    val paintCellBold = createTextPaint(PdfColors.TEXT_DARK, 9.5f, TYPEFACE_BOLD)
    /** أداة تلوين النصوص الخافتة والملاحظات الفرعية */
    val paintMutedText = createTextPaint(PdfColors.TEXT_MUTED_GREY, 8.5f, TYPEFACE_NORMAL)
    /** أداة تلوين الشرطة التوضيحية للخلايا الفارغة */
    val paintEmptyDash = createTextPaint(PdfColors.TEXT_MUTED_GREY, 10f, TYPEFACE_NORMAL)

    /** فرشاة تعبئة خلفية خلية المبلغ في المعاملات المدينة (لنا) */
    val paintOwedBg = Paint().apply {
        color = Color.parseColor(PdfColors.OWED_BG)
        style = Paint.Style.FILL
    }
    /** خط تلوين نص المبلغ المدين */
    val paintOwedText = createTextPaint(PdfColors.OWED_TEXT, 9.5f, TYPEFACE_BOLD)

    /** فرشاة تعبئة خلفية خلية المبلغ في المعاملات الدائنة (علينا / دفعة) */
    val paintPaymentBg = Paint().apply {
        color = Color.parseColor(PdfColors.PAYMENT_BG)
        style = Paint.Style.FILL
    }
    /** خط تلوين نص المبلغ الدائن */
    val paintPaymentText = createTextPaint(PdfColors.PAYMENT_TEXT, 9.5f, TYPEFACE_BOLD)

    /** خط تلوين اسم اليوم باللغة العربية */
    val paintDayText = createTextPaint(PdfColors.TEXT_MEDIUM, 8.5f, TYPEFACE_NORMAL)
    /** خط تلوين التاريخ الميلادي */
    val paintDateText = createTextPaint(PdfColors.TEXT_DARK, 9f, TYPEFACE_NORMAL)

    /** فرشاة تعبئة خلفية صفوف العملات الأجنبية */
    val paintForeignBg = Paint().apply {
        color = Color.parseColor(PdfColors.FOREIGN_ROW_BG)
        style = Paint.Style.FILL
    }
    /** أداة رسم الخطوط الفاصلة بين صفوف الجداول */
    val paintRowDivider = Paint().apply {
        color = Color.parseColor(PdfColors.ROW_DIVIDER)
        strokeWidth = 0.5f
        style = Paint.Style.STROKE
    }
    /** أداة رسم النصوص لوصف المعاملة متعدد الأسطر */
    val textPaintDesc = TextPaint(paintCellNormal)
}


