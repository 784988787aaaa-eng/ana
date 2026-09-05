/**
 * =====================================================================
 * ملف: محرك الحسابات المالية الدقيقة (FinancialMath.kt)
 * =====================================================================
 * هذا الملف هو البوابة المركزية للعمليات الحسابية المالية داخل التطبيق.
 * يمنع هذا التجميع استعمال Double/Float في الحسابات النقدية الحديثة، مع
 * الإبقاء على التحويل إلى Double محصوراً في حدود التوافق مع مخطط Room القديم.
 */
package com.smartledger.aldaftar.domain.finance

import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode

/**
 * ثوابت الحساب المالي الموحدة حتى لا تختلف سياسة التقريب بين أجزاء التطبيق.
 */
object FinancialMath {
    /** عدد المنازل المستخدمة داخلياً للأرصدة والمبالغ المحاسبية الحالية. */
    const val MONEY_SCALE: Int = 4

    /** عدد المنازل المستخدمة لأسعار الصرف قبل الحفظ أو العرض. */
    const val RATE_SCALE: Int = 4

    /** سياق حساب عشري عالي الدقة قبل التقريب المحاسبي النهائي. */
    val DECIMAL_CONTEXT: MathContext = MathContext.DECIMAL128

    /** تقريب مصرفي يقلل الانحياز التراكمي في مجموعات المعاملات. */
    val ROUNDING: RoundingMode = RoundingMode.HALF_EVEN

    /** يحول قيمة مالية إلى مقياس محاسبي موحد دون استعمال الفاصلة العائمة. */
    fun money(value: BigDecimal): BigDecimal = value.setScale(MONEY_SCALE, ROUNDING)

    /** يحول سعر صرف إلى مقياس ثابت قبل إجراء أي عملية تحويل. */
    fun rate(value: BigDecimal): BigDecimal = value.setScale(RATE_SCALE, ROUNDING)

    /** يجمع مبلغين ثم يثبت المقياس المحاسبي لمنع اختلاف التمثيل بين السجلات. */
    fun add(a: BigDecimal, b: BigDecimal): BigDecimal = money(a.add(b, DECIMAL_CONTEXT))

    /** يطرح مبلغين مع الحفاظ على سياسة التقريب المصرفي الموحدة. */
    fun subtract(a: BigDecimal, b: BigDecimal): BigDecimal = money(a.subtract(b, DECIMAL_CONTEXT))

    /** يضرب مبلغاً في معامل مالي مع استخدام سياق عشري مرتفع الدقة. */
    fun multiply(a: BigDecimal, b: BigDecimal): BigDecimal = money(a.multiply(b, DECIMAL_CONTEXT))

    /** يقسم مبلغاً مع منع القسمة على صفر وتطبيق التقريب المصرفي. */
    fun divide(a: BigDecimal, b: BigDecimal): BigDecimal {
        if (b.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO.setScale(MONEY_SCALE, ROUNDING)
        return money(a.divide(b, DECIMAL_CONTEXT))
    }

    /**
     * يحول النص القادم من واجهة الإدخال إلى BigDecimal دون المرور بـ Double.
     * القيم غير الصالحة لا تتحول إلى صفر بصمت؛ بل ترفض حتى لا تختفي أخطاء مالية.
     */
    fun parse(value: String): BigDecimal? {
        return try {
            value.trim().takeIf { it.isNotEmpty() }?.let { BigDecimal(it) }
        } catch (_: NumberFormatException) {
            null
        }
    }
}
