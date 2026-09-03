/**
 * =====================================================================
 * ملف: واجهة أدوات وتنسيقات التواريخ (DateUtils.kt)
 * =====================================================================
 * 
 * [الغرض العام والتعليمي من الملف]:
 * يمثل هذا الكائن واجهة توافقية وتفويضية (Facade Pattern) لكافة دوال التعامل مع
 * التواريخ والأوقات في التطبيق. يقوم بتوجيه كافة الطلبات الخاصة باستخراج أسماء
 * الأيام والشهور بالعربية، وتنسيق الأوقات بالصيغة العربية، وحساب الفروق الزمنية
 * إلى المنسق المركزي الموحد [AppDateTimeFormatter].
 * 
 * [المسؤوليات المعمارية والتقنية للملف]:
 * 1. تطبيق نمط الواجهة الموحدة (Facade Design Pattern):
 *    - الحفاظ على استقرار واستمرارية استدعاءات الوحدات القديمة دون كسر الشيفرة البرمجية.
 * 2. معالجة وتوحيد الطوابع الزمنية (Timestamp Normalization):
 *    - التحويل الذكي بين الثواني والمللي ثانية لضمان دقة تواريخ الجافا.
 * 3. التوطين الكامل للتواريخ العربية (Arabic Date Localization).
 */
package com.example.domain

// ---------------------------------------------------------------------
// استيراد حزم سياق أندرويد والمنسق المركزي الموحد للتواريخ
// ---------------------------------------------------------------------
import android.content.Context
import com.example.domain.formatters.AppDateTimeFormatter

/**
 * [الكائن الأحادي لأدوات التواريخ - DateUtils]:
 * يوفر دوال تفويضية سريعة لاستخراج وتنسيق التواريخ والأوقات باللغة العربية.
 */
object DateUtils {

    /**
     * [استخراج اسم يوم الأسبوع بالعربية - getDayOfWeekArabic]:
     * يفوض الاستخراج لـ [AppDateTimeFormatter] لجلب اسم اليوم (مثال: السبت، الأحد...).
     *
     * @param timestampSec الطابع الزمني بالثواني.
     * @return اسم اليوم بالعربية.
     */
    fun getDayOfWeekArabic(timestampSec: Long): String =
        AppDateTimeFormatter.getDayOfWeekArabic(timestampSec)

    /**
     * [تنسيق الوقت بنظام 12 ساعة بالعربية - formatTime24Or12]:
     * يحول الطابع الزمني إلى صيغة وقت بنظام 12 ساعة مع اللاحقة (ص/م).
     *
     * @param timestampSec الطابع الزمني بالثواني أو المللي ثانية.
     * @return نص الوقت المنسق (مثال: "03:45 م").
     */
    fun formatTime24Or12(timestampSec: Long): String =
        AppDateTimeFormatter.formatTime12hArabic(java.util.Date(if (timestampSec in 1..999999999999L) timestampSec * 1000L else timestampSec))

    /**
     * [تنسيق التاريخ الكامل بالصيغة القياسية - formatDateFull]:
     * يفوض التنسيق لإرجاع التاريخ بالصيغة القياسية (YYYY-MM-DD).
     *
     * @param timestampSec الطابع الزمني بالثواني.
     * @return تاريخ منسق بصيغة ISO.
     */
    fun formatDateFull(timestampSec: Long): String =
        AppDateTimeFormatter.formatDateIso(timestampSec)

    /**
     * [جلب مفتاح السنة والشهر للتجميع - getYearMonthKey]:
     * يولد مفتاحاً نصياً يجمع السنة والشهر (مثال: "2026-08") لتجميع الحركات الشهرية.
     *
     * @param timestampSec الطابع الزمني بالثواني.
     * @return مفتاح السنة والشهر.
     */
    fun getYearMonthKey(timestampSec: Long): String =
        AppDateTimeFormatter.getYearMonthKey(timestampSec)

    /**
     * [استخراج رقم اليوم من الشهر - getDayOfMonth]:
     * يعيد رقم اليوم الحالي في الشهر (1-31).
     *
     * @param timestampSec الطابع الزمني بالثواني.
     * @return رقم اليوم.
     */
    fun getDayOfMonth(timestampSec: Long): Int =
        AppDateTimeFormatter.getDayOfMonth(timestampSec)

    /**
     * [جلب اسم الشهر بالعربية - getMonthNameArabic]:
     * يعيد اسم الشهر باللغة العربية (مثال: أغسطس، سبتمبر...).
     *
     * @param timestampSec الطابع الزمني بالثواني.
     * @return اسم الشهر المعرب.
     */
    fun getMonthNameArabic(timestampSec: Long): String =
        AppDateTimeFormatter.getMonthNameArabic(timestampSec)

    /**
     * [حساب وصياغة الفرق الزمني بين تاريخين - formatDurationBetween]:
     * يحسب المدة المنقضية بين تاريخين ويصيغها بعبارات عربية دقيقة (أيام، أشهر، سنوات).
     *
     * @param newerSec الطابع الزمني الأحدث بالثواني.
     * @param olderSec الطابع الزمني الأقدم بالثواني.
     * @param context سياق التطبيق للوصول لموارد النصوص.
     * @return نص المدة الزمنية المعربة.
     */
    fun formatDurationBetween(newerSec: Long, olderSec: Long, context: Context? = null): String =
        AppDateTimeFormatter.formatDurationBetween(newerSec, olderSec, context)
}
