/**
 * =====================================================================
 * ملف: كائن زوج العملات وسعر الصرف (CurrencyPair.kt)
 * =====================================================================
 * 
 * [الغرض العام والتعليمي من الملف]:
 * يمثل هذا الملف كائن قيمة آمن النمط (Type-Safe Value Object) لتمثيل زوج العملات
 * (مثل: USD/YER أو SAR/YER) وسعر التحويل أو الصرف المالي بينهما في طبقة النطاق.
 * يغلف قيود التحقق من صحة السعر وقواعد التقريب المحاسبي البنكي (Banker's Rounding).
 * 
 * [المسؤوليات المعمارية والتقنية للملف]:
 * 1. نمذجة أسعار الصرف (Exchange Rate Modeling):
 *    - تمثيل العملة الأساسية [baseCurrency]، والعملة المستهدفة [targetCurrency]، وسعر الصرف كـ [BigDecimal].
 * 2. التحقق من صحة وقوة السعر (Rate Validity & Safety):
 *    - فحص كون السعر موجباً وأكبر من الصفر عبر [isValid].
 *    - توفير سعر آمن [safeRate] بدقة 4 خانات عشرية باستخدام نمط تقريب الصيرفة [RoundingMode.HALF_EVEN].
 * 3. تمييز التحويل الذاتي (Self-Pair Detection):
 *    - معرفة ما إذا كان التحويل لنفس العملة عبر [isSelfPair] دون الحاجة لإجراء عمليات ضرب معقدة.
 */
package com.smartledger.aldaftar.domain.model

// ---------------------------------------------------------------------
// استيراد حزم الأرقام العشرية وأنماط التقريب المحاسبي
// ---------------------------------------------------------------------
import java.math.BigDecimal
import java.math.RoundingMode

/**
 * [فئة بيانات زوج العملات - CurrencyPair]:
 * @property baseCurrency رمز العملة الأساسية (مثل: USD).
 * @property targetCurrency رمز العملة المستهدفة للتحويل إليها (مثل: YER).
 * @property rate معامل أو سعر الصرف بين العملتين كقيمة [BigDecimal] فائقة الدقة.
 */
data class CurrencyPair(
    val baseCurrency: String,
    val targetCurrency: String,
    val rate: BigDecimal = BigDecimal.ONE
) {
    /**
     * خاصية للتحقق من صلاحية سعر الصرف:
     * يكون السعر صالحاً إذا كان أكبر تماماً من الصفر.
     */
    val isValid: Boolean
        get() = rate.compareTo(BigDecimal.ZERO) > 0

    /**
     * خاصية السعر الآمن والمقرب محاسبياً:
     * تعيد سعر الصرف بدقة 4 خانات عشرية مع تقريب بنكي، أو ترجع 1.0000 كقيمة افتراضية آمنة في حال كان السعر سالباً أو صفراً.
     */
    val safeRate: BigDecimal
        get() = if (rate.compareTo(BigDecimal.ZERO) > 0) {
            rate.setScale(4, RoundingMode.HALF_EVEN)
        } else {
            BigDecimal.ONE.setScale(4, RoundingMode.HALF_EVEN)
        }

    /**
     * خاصية التحقق من تطابق العملتين (التحويل لنفس العملة):
     * تفحص ما إذا كانت العملة الأساسية هي ذاتها العملة المستهدفة بعد تجاهل الفراغات وحالة الأحرف.
     */
    val isSelfPair: Boolean
        get() = baseCurrency.trim().equals(targetCurrency.trim(), ignoreCase = true)
}

