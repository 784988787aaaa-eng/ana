/**
 * التوثيق المعماري العربي — مشروع «الدفتر الذكي / ميزان الدار».
 * الملف: app/src/main/java/com/example/ui/helper/HabayebMathHelper.kt
 * المسؤولية: أدوات حسابية مساندة لطبقة واجهة الحبايب دون استبدال المحركات المالية الأساسية.
 *
 * القراءة التعليمية: يوضح هذا الملف كيف تنتقل حالة التطبيق من الطبقة المشتركة
 * إلى المشهد المرئي على الهاتف، مع تفسير العقود والحالة والتوابع والتفاعلات.
 * الكتلة التنفيذية الأصلية أدناه محفوظة حرفياً؛ الإضافات التوثيقية لا تعدّل
 * أي رمز تنفيذي وفق قاعدة Zero Code Alteration.
 */

// --- فهرس التوثيق التعليمي للسطر التنفيذي ---
// توثيق السطر 1: التوجيه الحزمي يحدد الموضع المنطقي للملف داخل طبقة الواجهة.
// توثيق السطر 3: الاستيراد التالي اعتماد أصلي يحتاجه الملف؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 4: الاستيراد التالي اعتماد أصلي يحتاجه الملف؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 5: الاستيراد التالي اعتماد أصلي يحتاجه الملف؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 6: الاستيراد التالي اعتماد أصلي يحتاجه الملف؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 16: التعريف التالي يحدد عقداً أو نوعاً أصلياً؛ يحتفظ بالاسم والبنية كما وردا في المصدر.
// توثيق السطر 36: الشرط التالي يحافظ على قرار التنفيذ الأصلي.
// توثيق السطر 46: الشرط التالي يحافظ على قرار التنفيذ الأصلي.
// توثيق السطر 52: الشرط التالي يحافظ على قرار التنفيذ الأصلي.
// توثيق السطر 56: الشرط التالي يحافظ على قرار التنفيذ الأصلي.
// توثيق السطر 67: الشرط التالي يحافظ على قرار التنفيذ الأصلي.

package com.example.ui.helper

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat
import java.util.Locale

/**
 * مساعد العمليات الحسابية والتقريب المالي لحبايب (Habayeb Mathematical & Formatting Helper)
 *
 * المسؤوليات المعمارية:
 * 1. الدقة الحسابية الإلزامية: استخدام BigDecimal حصراً مع التقريب المصرفي (HALF_EVEN) لمنع تراكم أخطاء الفاصلة العائمة.
 * 2. أمان الخيوط: استخدام ThreadLocal لـ NumberFormat لتفادي تسريب الذاكرة ومشاكل التزامن عند تعدد خيوط العرض.
 * 3. التنسيق الذكي: إزالة الأصفار الزائدة على يمين الفاصلة مع الحفاظ على وضوح وقراءة الأرقام الكبيرة.
 */
object HabayebMathHelper {
    private val numberFormatThreadLocal = ThreadLocal.withInitial {
        NumberFormat.getNumberInstance(Locale.US)
    }

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

    fun toBigDecimal(value: String): BigDecimal {
        return try {
            val clean = value.trim()
            if (clean.isBlank() || clean.equals("null", ignoreCase = true)) BigDecimal.ZERO else BigDecimal(clean)
        } catch (e: Exception) {
            BigDecimal.ZERO
        }
    }

    fun add(a: BigDecimal, b: BigDecimal): BigDecimal = a.add(b)
    fun subtract(a: BigDecimal, b: BigDecimal): BigDecimal = a.subtract(b)
    fun multiply(a: BigDecimal, b: BigDecimal): BigDecimal = a.multiply(b)
    fun divide(a: BigDecimal, b: BigDecimal): BigDecimal {
        if (b.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO
        return a.divide(b, 10, RoundingMode.HALF_EVEN)
    }

    // دالة التنسيق الذكي لجمالية الواجهات والدقة المطلقة
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



// --- ملاحظات وتوصيات المعمارية البرمجية ---
// 1) تم الحفاظ على الكتلة التنفيذية الأصلية دون تعديل أو حذف أو إعادة ترتيب.
// 2) يوصى مستقبلاً بإبقاء مكونات Compose المشتركة صغيرة ومحددة المسؤولية لتقليل إعادة التركيب غير الضرورية.
// 3) ينبغي إبقاء حالات الحوار والتنقل قابلة للتتبع من مصدر واحد عند اتساع عدد المسارات، مع الحفاظ على السلوك الحالي.
// 4) يوصى بعزل أدوات Intent وحفظ الملفات والاهتزاز عن واجهات Compose قدر الإمكان عبر عقود واضحة، دون تعديل النسخة الحالية.
// 5) في نظام Theme والألوان والطباعة، يوصى بالحفاظ على مصدر تصميم مركزي ومنع القيم المتناثرة مستقبلاً.
// 6) أي تحسين لاحق يجب أن يسبقه اختبار تكافؤ سلوكي/مرئي مناسب قبل الدمج.
