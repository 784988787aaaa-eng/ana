/**
 * =====================================================================
 * ملف: ثوابت ألوان وسمات تقارير PDF (PdfColors.kt)
 * =====================================================================
 * 
 * [الغرض العام والتعليمي من الملف]:
 * يحتوي هذا الكائن على لوحة الألوان المعيارية والاحترافية (Color Palette) المستخدمة
 * في رسم مستندات PDF وكشوفات الحسابات وجداول الأستاذ العام بواسطة Android Canvas،
 * مع مراعاة التباين اللوني العالي وقابلية القراءة عند الطباعة الورقية بالألوان أو الرمادي.
 * 
 * [المسؤوليات المعمارية والتقنية]:
 * 1. المركزية اللونية للمستندات المطبوعة:
 *    - توحيد درجات الألوان للنصوص والترويسات والخلفيات وحدود الجداول.
 * 2. الدلالات المحاسبية للألوان (Semantic Accounting Colors):
 *    - تمييز الديون والالتزامات بدرجات الأحمر الهادئ، والمقبوضات والدفعات بدرجات الأخضر الزمردي.
 */
package com.example.data.serialization.pdf

import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.ui.graphics.toArgb
import com.example.ui.theme.MizanDocumentColors

/**
 * [الكائن الأحادي لألوان تقارير PDF - PdfColors]:
 * يجمع رموز الألوان الست عشرية (Hex) المخصصة لمحركات رسم التقارير المطبوعة.
 * تم تحويله بالكامل ليعمل كـ Adapter/Mapping Layer يعتمد على MizanDocumentColors كمصدر للحقيقة.
 */
object PdfColors {
    private fun ComposeColor.toHex(): String {
        return String.format(java.util.Locale.US, "#%06X", 0xFFFFFF and this.toArgb())
    }

    /** اللون الزمردي الأساسي المعتمد لهوية التقارير */
    val PRIMARY_EMERALD = MizanDocumentColors.brandPrimary.toHex()
    /** لون خلفية الترويسات الرئيسية للجداول */
    val HEADER_BG = MizanDocumentColors.headerBackground.toHex()
    /** لون النصوص داخل الترويسات الداكنة (أبيض ناصع) */
    val HEADER_TEXT = MizanDocumentColors.headerText.toHex()
    /** لون إطارات وحدود الترويسات */
    val HEADER_BORDER = MizanDocumentColors.borderStrong.toHex()
    /** لون النص الفحمي عالي التباين */
    val TEXT_CHARCOAL = MizanDocumentColors.contentPrimary.toHex()
    /** لون النص الرمادي الهادئ للبيانات الثانوية */
    val TEXT_MUTED_GREY = MizanDocumentColors.contentSecondary.toHex()
    /** لون النص الداكن الأساسي */
    val TEXT_DARK = MizanDocumentColors.contentPrimary.toHex()
    /** لون النص المتوسط للملاحظات */
    val TEXT_MEDIUM = MizanDocumentColors.contentSecondary.toHex()
    /** لون النص الفاتح للتفاصيل الدقيقة */
    val TEXT_LIGHT = MizanDocumentColors.contentTertiary.toHex()
    /** لون صافي الدين الأزرق الكحلي */
    val NET_DEBT_BLUE = MizanDocumentColors.netDebtBlue.toHex()
    /** خلفية معاملات الدين (أحمر خافت جداً) */
    val OWED_BG = MizanDocumentColors.debtContainer.toHex()
    /** نص مبالغ الدين والالتزام (أحمر داكن) */
    val OWED_TEXT = MizanDocumentColors.debt.toHex()
    /** حد خلايا الدين والالتزام (أحمر فاتح) */
    val DEBT_BORDER = MizanDocumentColors.debtBorder.toHex()
    /** خلفية معاملات السداد والدفع (أخضر خافت جداً) */
    val PAYMENT_BG = MizanDocumentColors.creditContainer.toHex()
    /** نص مبالغ السداد والمقبوضات (أخضر داكن) */
    val PAYMENT_TEXT = MizanDocumentColors.credit.toHex()
    /** حد خلايا السداد والمقبوضات (أخضر فاتح) */
    val CREDIT_BORDER = MizanDocumentColors.creditBorder.toHex()
    /** خلفية صفوف المعاملات بالعملات الأجنبية */
    val FOREIGN_ROW_BG = MizanDocumentColors.surfaceVariant.toHex()
    /** خلفية بطاقات الملخصات */
    val CARD_BG = MizanDocumentColors.surfaceContainer.toHex()
    /** لون الخط الفاصل بين صفوف الجداول */
    val ROW_DIVIDER = MizanDocumentColors.borderVariant.toHex()
    /** خلفية الصفوف التبادلية (Zebra Striping) */
    val ALT_ROW_BG = MizanDocumentColors.altRowBackground.toHex()
    /** خلفية صف الإجماليات الختامي */
    val TOTALS_ROW_BG = MizanDocumentColors.totalsRowBackground.toHex()
    /** خلفية شريط تنبيه الديون */
    val BANNER_OWED_BG = MizanDocumentColors.debtContainer.toHex()
    /** خلفية شريط تنبيه السداد */
    val BANNER_PAYMENT_BG = MizanDocumentColors.creditContainer.toHex()
}


