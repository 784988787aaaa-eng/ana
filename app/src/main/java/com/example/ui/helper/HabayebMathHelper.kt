package com.example.ui.helper

/*
 * =====================================================================================
 * حزمة العمليات الحسابية والمالية (Mathematical & Financial Utilities Package)
 * -------------------------------------------------------------------------------------
 * تحتوي هذه الفئة على دوال المعالجة الحسابية المتقدمة، التنسيق المالي المخصص،
 * وضبط دقة الأرقام والعملات لتفادي مشاكل الفاصلة العائمة (Floating-Point Precision).
 * =====================================================================================
 */

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat
import java.util.Locale

/*
 * =====================================================================================
 * كائن مساعد العمليات الحسابية والتنسيق المالي لحسابات الحبايب (HabayebMathHelper)
 * -------------------------------------------------------------------------------------
 * [المسؤوليات والأهداف المعمارية]:
 * 1. الدقة الحسابية الإلزامية: استخدام BigDecimal حصراً مع التقريب المصرفي (HALF_EVEN)
 *    لمنع تراكم أخطاء الفاصلة العائمة في العمليات المحاسبية.
 * 2. أمان التزامن وخيوط المعالجة: استخدام ThreadLocal لـ NumberFormat لتفادي إعادة الإنشاء
 *    المتكرر للكائنات ومشاكل التزامن عند تعدد خيوط العرض والرسم (UI Threads).
 * 3. التنسيق الذكي: إزالة الأصفار الزائدة على يمين الفاصلة العشرية مع الحفاظ على وضوح الأرقام الكبيرة.
 * 4. الحماية من القسمة على صفر والقيم غير الرقمية (NaN / Infinite).
 * =====================================================================================
 */
object HabayebMathHelper {
    /*
     * مخزن محلي لكل خيط معالجة لتنسيق الأرقام القياسية بالإنجليزية بدون مشاكل أمان خيوط (Thread Safety)
     */
    private val numberFormatThreadLocal = ThreadLocal.withInitial {
        NumberFormat.getNumberInstance(Locale.US)
    }

    /*
     * ---------------------------------------------------------------------------------
     * دالة تحويل القيمة العشرية Double إلى BigDecimal بأمان
     * ---------------------------------------------------------------------------------
     * تعالج حالات القيم غير المحددة (NaN) واللانهاية (Infinite) وتعيد صفراً في حال الخطأ.
     * ---------------------------------------------------------------------------------
     */
    fun toBigDecimal(value: Double): BigDecimal {
        return if (value.isNaN() || value.isInfinite()) {
            BigDecimal.ZERO
        } else {
            try {
                BigDecimal.valueOf(value)
            } catch (e: Exception) {
                BigDecimal.ZERO
            }
        }
    }

    /*
     * ---------------------------------------------------------------------------------
     * دالة تحويل النص String إلى BigDecimal بأمان
     * ---------------------------------------------------------------------------------
     * تنظف الفراغات وتعالج النصوص الفارغة أو قيم "null" لتجنب رمي استثناءات NumberFormatException.
     * ---------------------------------------------------------------------------------
     */
    fun toBigDecimal(value: String): BigDecimal {
        return try {
            val clean = value.trim()
            if (clean.isBlank() || clean.equals("null", ignoreCase = true)) BigDecimal.ZERO else BigDecimal(clean)
        } catch (e: Exception) {
            BigDecimal.ZERO
        }
    }

    /*
     * ---------------------------------------------------------------------------------
     * العمليات الحسابية الأساسية الآمنة (الجمع، الطرح، الضرب، القسمة)
     * ---------------------------------------------------------------------------------
     */
    fun add(a: BigDecimal, b: BigDecimal): BigDecimal = a.add(b)
    fun subtract(a: BigDecimal, b: BigDecimal): BigDecimal = a.subtract(b)
    fun multiply(a: BigDecimal, b: BigDecimal): BigDecimal = a.multiply(b)

    /*
     * دالة القسمة الآمنة مع تحديد دقة 10 منازل عشرية وتقريب نصف زوجي (HALF_EVEN) والحماية من القسمة على صفر
     */
    fun divide(a: BigDecimal, b: BigDecimal): BigDecimal {
        if (b.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO
        return a.divide(b, 10, RoundingMode.HALF_EVEN)
    }

    /*
     * ---------------------------------------------------------------------------------
     * دالة التنسيق المالي الذكي (formatSmart)
     * ---------------------------------------------------------------------------------
     * [الهدف والآلية]:
     * 1. تقريب القيمة إلى منزلتين عشريتين بنظام التقريب المصرفي (HALF_EVEN).
     * 2. إزالة الأصفار غير الضرورية على اليمين (مثال: 125.00 تصبح 125، بينما 125.50 تصبح 125.5).
     * 3. إضافة فواصل الآلاف لتسهيل قراءة المبالغ المالية الضخمة.
     * ---------------------------------------------------------------------------------
     */
    fun formatSmart(value: BigDecimal): String {
        if (value.compareTo(BigDecimal.ZERO) == 0) return "0"
        val rounded = value.setScale(2, RoundingMode.HALF_EVEN)
        val stripped = rounded.stripTrailingZeros()
        val formatter = numberFormatThreadLocal.get()
        if (stripped.scale() <= 0) {
            formatter.minimumFractionDigits = 0
            formatter.maximumFractionDigits = 0
        } else {
            formatter.minimumFractionDigits = 0
            formatter.maximumFractionDigits = 2
        }
        return formatter.format(stripped)
    }

    /*
     * ---------------------------------------------------------------------------------
     * دالة تنسيق أسعار الصرف من مدخلات Double (formatRate)
     * ---------------------------------------------------------------------------------
     */
    fun formatRate(value: Double): String {
        if (value <= 0.0) return "0"
        return try {
            val bd = BigDecimal(value.toString())
                .setScale(2, RoundingMode.HALF_EVEN)
                .stripTrailingZeros()
            bd.toPlainString()
        } catch (e: Exception) {
            value.toString()
        }
    }

    /*
     * ---------------------------------------------------------------------------------
     * دالة تنسيق أسعار الصرف من مدخلات BigDecimal (formatRate)
     * ---------------------------------------------------------------------------------
     */
    fun formatRate(value: BigDecimal): String {
        return try {
            value.setScale(2, RoundingMode.HALF_EVEN)
                .stripTrailingZeros()
                .toPlainString()
        } catch (e: Exception) {
            value.toString()
        }
    }
}



