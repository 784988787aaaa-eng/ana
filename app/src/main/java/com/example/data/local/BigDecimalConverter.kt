/**
 * =====================================================================
 * ملف: محول الأنواع الرقمية المالية لقاعدة البيانات (BigDecimalConverter.kt)
 * =====================================================================
 * 
 * [الغرض العام والتعليمي من الملف]:
 * يمثل هذا الملف محول أنواع Room مخصص (Room TypeConverter) مسؤول عن التحويل
 * التبادلي بين كائنات الحسابات المالية الدقيقة [BigDecimal] وأنواع التخزين
 * الأولية المدعومة في محرك SQLite (وهي النصوص `TEXT` والأرقام العشرية `REAL`).
 * 
 * [لماذا نحتاج BigDecimal في التطبيقات المالية؟]:
 * الأرقام العشرية التقليدية (مثل `Double` و `Float`) تعتمد معيار IEEE 754 لتمثيل
 * الفاصلة العائمة في الذاكرة الثنائية، مما يؤدي إلى أخطاء تقريب صغيرة تتراكم
 * مع المعاملات المتكررة (مثل 0.1 + 0.2 = 0.30000000000000004).
 * استخدام [BigDecimal] يضمن دقة حسابية لا نهائية ومطلقة لكل هللة وقرش ودينار.
 * 
 * [المسؤوليات المعمارية والتقنية]:
 * 1. تحويل النصوص المخزنة في SQLite بأمان إلى كائنات [BigDecimal] مع تطهير المدخلات.
 * 2. حفظ كائنات [BigDecimal] كنصوص واضحة غير مختصرة علمياً عبر `toPlainString()`.
 * 3. دعم التحويل التبادلي مع `Double` لدعم التوافق العكسي مع الإصدارات والجداول القديمة.
 * 4. رفض القيم غير الرقمية واللانهايات (`NaN`, `Positive/Negative Infinity`).
 * 5. المعالجة الذكية للأرقام المشرقية (٠-٩) والفارسية (۰-۹) والفواصل العربية (٫ , .).
 * 6. منع إخفاء البيانات التالفة: إرجاع `null` عند تلف القيمة بدلاً من تصفيرها المحاسبي الصامت.
 */
package com.example.data.local

// ---------------------------------------------------------------------
// استيراد حزم محولات مكتبة Room وفئة العمليات الحسابية الكبيرة BigDecimal
// ---------------------------------------------------------------------
import androidx.room.TypeConverter
import java.math.BigDecimal

/**
 * [فئة محول الأرقام المالية - BigDecimalConverter]:
 * تسجل في قاعدة بيانات Room عبر `@TypeConverters(BigDecimalConverter::class)`
 * لتتعرف Room تلقائياً على كيفية قراءة وكتابة أعمدة [BigDecimal].
 */
class BigDecimalConverter {

    /**
     * [دالة التحويل من نص إلى BigDecimal - fromString]:
     * تُستدعى تلقائياً من قبل Room عند قراءة قيمة عمود نصي من قاعدة البيانات وتحويله لكائن مالي.
     * 
     * [خطوات المعالجة]:
     * 1. التحقق من عدم فراغ النص أو كونه "null" حرفياً.
     * 2. تنظيف وتطهير النص من المحارف الغريبة وتوحيد الأرقام عبر [cleanNumberString].
     * 3. محاولة بناء كائن [BigDecimal] من النص النظيف.
     * 4. عند حدوث أي خطأ في البنية، يتم إرجاع `null` لتنبيه طبقات التحقق بعدم صحة السجل.
     */
    @TypeConverter
    fun fromString(value: String?): BigDecimal? {
        if (value.isNullOrBlank() || value.equals("null", ignoreCase = true)) return null
        val cleaned = cleanNumberString(value)
        if (cleaned.isEmpty()) return null
        return try {
            BigDecimal(cleaned)
        } catch (_: Exception) {
            // إرجاع null عند الفشل لتفادي تحويل السجلات التالفة إلى صفر محاسبي مضلل
            null
        }
    }

    /**
     * [دالة التحويل من BigDecimal إلى نص - toString]:
     * تُستدعى تلقائياً من قبل Room عند كتابة كائن مالي لحفظه في عمود نصي بجدول SQLite.
     * تستخدم `toPlainString()` لضمان عدم استخدام الترميز العلمي (مثل 1E+5) وحفظ الرقم بالكامل.
     */
    @TypeConverter
    fun toString(value: BigDecimal?): String? = value?.toPlainString()

    /**
     * [دالة التحويل من Double إلى BigDecimal - fromDouble]:
     * تفيد في دعم الجداول والبيانات القديمة التي كانت تخزن المبالغ كأرقام عشرية Double.
     * تستبعد القيم غير الحقيقية مثل `NaN` و `Infinite` ثم تنشئ الكائن المالي عبر `BigDecimal.valueOf`.
     */
    @TypeConverter
    fun fromDouble(value: Double?): BigDecimal? {
        if (value == null || value.isNaN() || value.isInfinite()) return null
        return try {
            BigDecimal.valueOf(value)
        } catch (_: Exception) {
            null
        }
    }

    /**
     * [دالة التحويل من BigDecimal إلى Double - toDouble]:
     * تُستدعى عند الحاجة المؤقتة لتمثيل القيمة كـ Double للمخططات البيانية أو التوافق.
     */
    @TypeConverter
    fun toDouble(value: BigDecimal?): Double? = value?.toDouble()

    /**
     * [الكائن المرافق - Companion Object]:
     * يوفر دوال التنظيف والمعالجة اللغوية الموحدة للأرقام.
     */
    companion object {
        /**
         * [دالة تطهير السلاسل الرقمية - cleanNumberString]:
         * تتولى توحيد الأرقام بمختلف الصيغ المكتوبة من لوحات المفاتيح المتنوعة:
         * 
         * [القواعد والمعالجات]:
         * 1. تحويل الأرقام المشرقية العربية (٠، ١، ٢، ...) إلى أرقام لاتينية قياسية (0, 1, 2, ...).
         * 2. تحويل الأرقام الفارسية/الأوردية (۰، ۱، ۲، ...) إلى أرقام قياسية.
         * 3. توحيد علامات الفاصلة العشرية المتنوعة (النقطة `.`, الفاصلة `,`, الفاصلة العربية `٫`) إلى نقطة عشرية قياسية واحدة.
         * 4. السماح بإشارة السالب `-` فقط إذا كانت في بداية الرقم لتمثيل الأرصدة المدينة أو السالبة.
         * 5. تصفية أي نتائج غير مكتملة تتكون فقط من علامات (مثل "-" أو "." أو "-.").
         */
        fun cleanNumberString(input: String): String {
            val trimmed = input.trim()
            if (trimmed.isEmpty()) return ""

            val len = trimmed.length
            val sb = StringBuilder(len)
            var seenDot = false

            for (i in 0 until len) {
                val ch = trimmed[i]
                when {
                    ch in '0'..'9' -> sb.append(ch)
                    ch in '٠'..'٩' -> sb.append((ch - '٠' + '0'.code).toChar())
                    ch in '۰'..'۹' -> sb.append((ch - '۰' + '0'.code).toChar())
                    ch == '.' || ch == ',' || ch == '٫' -> {
                        if (!seenDot) {
                            sb.append('.')
                            seenDot = true
                        }
                    }
                    ch == '-' && sb.isEmpty() -> sb.append('-')
                }
            }
            val result = sb.toString()
            if (result == "-" || result == "." || result == "-.") return ""
            return result
        }
    }
}


