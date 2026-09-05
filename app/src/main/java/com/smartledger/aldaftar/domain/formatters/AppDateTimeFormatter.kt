/**
 * =====================================================================
 * ملف: المنسق الموحد للتواريخ والأوقات والمدد الزمنية (.)
 * =====================================================================
 * 
 * [الغرض العام والتعليمي من الملف]:
 * يمثل هذا الكائن المحرك المركزي والمصدر الموحد للحقيقة (   ) لكافة
 * عمليات تنسيق التواريخ، والأوقات، وأسماء الأيام والشهور، وحساب الفروق الزمنية النسبية
 * في التطبيق باللغة العربية والإنجليزية.
 * يتميز بأنه مصمم ليكون آمناً تماماً لتعدد الخيوط (-) باستخدام [].
 * 
 * [المسؤوليات المعمارية والتقنية للملف]:
 * 1. الأمان التام للخيوط في منسقات التواريخ (-  ):
 *    - عزل كافة كائنات [] و [] داخل [] لمنع أخطاء التزامن ( ).
 * 2. المعالجة والتوحيد التلقائي للطوابع الزمنية (  ):
 *    - التمييز التلقائي بين الطوابع بالثواني () والمللي ثانية () وتحويلها بدقة.
 * 3. التوطين الكامل للتواريخ العربية ( ):
 *    - دعم صيغ الوقت 12 ساعة مع اللاحقة العربية (ص/م)، وتنسيق أسماء الأيام والشهور العربية.
 * 4. حساب الفروق الزمنية النسبية (  ):
 *    - تحويل الفرق بين تاريخين إلى عبارات نسبية معربة ودقيقة (منذ يوم، منذ شهر، قريب جداً).
 */
package com.smartledger.aldaftar.domain.formatters

// ---------------------------------------------------------------------
// استيراد حزم سياق أندرويد، وموارد النصوص، ومكتبات التواريخ والتقويم
// ---------------------------------------------------------------------
import android.content.Context
import com.smartledger.aldaftar.R
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * [الكائن الأحادي لمنسق التواريخ والأوقات - ]:
 * يوفر دوال تنسيق متقدمة ومعزولة للخيوط لكافة أجزاء التطبيق.
 */
object AppDateTimeFormatter {

    /** البيئة المحلية للغة العربية */
    private val arabicLocale = Locale("ar")

    // =========================================================================
    // قسم: منسقات التواريخ والأوقات المعزولة لكل خيط (- )
    // =========================================================================

    /** منسق التاريخ العربي القياسي (//) */
    private val dateArabicFormatter = ThreadLocal.withInitial { SimpleDateFormat("yyyy/MM/dd", arabicLocale) }
    /** منسق التاريخ بحسب اللغة الافتراضية للنظام */
    private val dateDefaultFormatter = ThreadLocal.withInitial { SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()) }
    /** منسق التاريخ المختصر (//) */
    private val dateShortFormatter = ThreadLocal.withInitial { SimpleDateFormat("yy/MM/dd", Locale.ENGLISH) }
    /** منسق التاريخ القياسي الدولي  (--) */
    private val dateIsoFormatter = ThreadLocal.withInitial { SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH) }
    /** منسق الوقت بنظام 12 ساعة بالعربية (: ) */
    private val time12hFormatter = ThreadLocal.withInitial { SimpleDateFormat("hh:mm a", arabicLocale) }
    /** منسق الوقت بنظام 12 ساعة باللغة الافتراضية */
    private val time12hDefaultFormatter = ThreadLocal.withInitial { SimpleDateFormat("hh:mm a", Locale.getDefault()) }
    /** منسق التاريخ والوقت الكامل بالعربية (// : ) */
    private val fullDateTimeArabicFormatter = ThreadLocal.withInitial { SimpleDateFormat("yyyy/MM/dd hh:mm a", arabicLocale) }
    /** منسق استخراج اسم يوم الأسبوع كاملاً () */
    private val dayOfWeekFormatter = ThreadLocal.withInitial { SimpleDateFormat("EEEE", arabicLocale) }
    /** منسق استخراج اسم الشهر كاملاً () */
    private val monthNameFormatter = ThreadLocal.withInitial { SimpleDateFormat("MMMM", arabicLocale) }
    /** منسق استخراج السنة بالأرقام اللاتينية () */
    private val yearOnlyFormatter = ThreadLocal.withInitial { SimpleDateFormat("yyyy", Locale.ENGLISH) }
    /** منسق مفتاح التجميع الشهري (-) */
    private val yearMonthFormatter = ThreadLocal.withInitial { SimpleDateFormat("yyyy-MM", Locale.ENGLISH) }
    /** كائن التقويم المعزول لكل خيط للحسابات التقويمية */
    private val calendarLocal = ThreadLocal.withInitial { Calendar.getInstance() }

    // =========================================================================
    // قسم: دوال تنسيق التواريخ (  )
    // =========================================================================

    /**
     * [تنسيق كائن  بالصيغة العربية القياسية]:
     * @  كائن التاريخ.
     * @ التاريخ المنسق (//).
     */
    fun formatDateArabic(date: Date): String {
        return dateArabicFormatter.get().format(date)
    }

    /**
     * [تنسيق الطابع الزمني بالصيغة العربية القياسية]:
     * @  الطابع الزمني بالثواني أو المللي ثانية.
     */
    fun formatDateArabic(timestampSeconds: Long): String {
        return formatDateArabic(Date(normalizeToMillis(timestampSeconds)))
    }

    /**
     * [تنسيق كائن  باللغة الافتراضية للجهاز]:
     */
    fun formatDateDefault(date: Date): String {
        return dateDefaultFormatter.get().format(date)
    }

    /**
     * [تنسيق الطابع الزمني باللغة الافتراضية للجهاز]:
     */
    fun formatDateDefault(timestampSeconds: Long): String {
        return formatDateDefault(Date(normalizeToMillis(timestampSeconds)))
    }

    /**
     * [تنسيق التاريخ بصيغة السنة المختصرة (//)]:
     */
    fun formatShortDate(date: Date): String {
        return dateShortFormatter.get().format(date)
    }

    /**
     * [تنسيق الطابع الزمني بصيغة السنة المختصرة]:
     */
    fun formatShortDate(timestampSeconds: Long): String {
        return formatShortDate(Date(normalizeToMillis(timestampSeconds)))
    }

    /**
     * [تنسيق كائن التاريخ بالصيغة الدولية  (--)]:
     */
    fun formatDateIso(date: Date): String {
        return dateIsoFormatter.get().format(date)
    }

    /**
     * [تنسيق الطابع الزمني بالصيغة الدولية ]:
     */
    fun formatDateIso(timestampSeconds: Long): String {
        return formatDateIso(Date(normalizeToMillis(timestampSeconds)))
    }

    // =========================================================================
    // قسم: دوال تنسيق الأوقات (  )
    // =========================================================================

    /**
     * [تنسيق الوقت بنظام 12 ساعة باللغة الافتراضية]:
     */
    fun formatTime12h(date: Date): String {
        return time12hDefaultFormatter.get().format(date)
    }

    /**
     * [تنسيق الطابع الزمني للوقت بنظام 12 ساعة]:
     */
    fun formatTime12h(timestampSeconds: Long): String {
        return formatTime12h(Date(normalizeToMillis(timestampSeconds)))
    }

    /**
     * [تنسيق الوقت بنظام 12 ساعة باللغة العربية (ص/م)]:
     */
    fun formatTime12hArabic(date: Date): String {
        return time12hFormatter.get().format(date)
    }

    /**
     * [تنسيق التاريخ والوقت الكامل بالعربية]:
     */
    fun formatFullDateTime(date: Date): String {
        return fullDateTimeArabicFormatter.get().format(date)
    }

    /**
     * [تنسيق الطابع الزمني للتاريخ والوقت الكامل بالعربية]:
     */
    fun formatFullDateTime(timestampMillisOrSeconds: Long): String {
        if (timestampMillisOrSeconds <= 0L) return ""
        val ms = normalizeToMillis(timestampMillisOrSeconds)
        return try {
            formatFullDateTime(Date(ms))
        } catch (e: Exception) {
            ""
        }
    }

    // =========================================================================
    // قسم: دوال استخراج مكونات التاريخ وأسماء الأيام والشهور
    // =========================================================================

    /**
     * [جلب اسم يوم الأسبوع كاملاً بالعربية - ]:
     * @  الطابع الزمني بالثواني.
     * @ اسم اليوم (مثال: الجمعة، السبت).
     */
    fun getDayOfWeekArabic(timestampSeconds: Long): String =
        dayOfWeekFormatter.get().format(Date(normalizeToMillis(timestampSeconds)))

    /**
     * [استخراج معرف المورد النصي ليوم الأسبوع - ]:
     * يعيد معرف المورد (  ) لليوم للوصول للترجمات المخصصة.
     */
    fun getDayOfWeekResId(timestampSeconds: Long): Int {
        val cal = calendarLocal.get().apply {
            timeInMillis = normalizeToMillis(timestampSeconds)
        }
        return when (cal.get(Calendar.DAY_OF_WEEK)) {
            Calendar.SATURDAY -> R.string.day_saturday
            Calendar.SUNDAY -> R.string.day_sunday
            Calendar.MONDAY -> R.string.day_monday
            Calendar.TUESDAY -> R.string.day_tuesday
            Calendar.WEDNESDAY -> R.string.day_wednesday
            Calendar.THURSDAY -> R.string.day_thursday
            Calendar.FRIDAY -> R.string.day_friday
            else -> R.string.day_sunday
        }
    }

    /**
     * [استخراج رقم اليوم في الشهر - ]:
     * @  الطابع الزمني بالثواني.
     * @ رقم اليوم (1-31).
     */
    fun getDayOfMonth(timestampSeconds: Long): Int =
        calendarLocal.get().apply {
            timeInMillis = normalizeToMillis(timestampSeconds)
        }.get(Calendar.DAY_OF_MONTH)

    /**
     * [جلب اسم الشهر والسنة بالعربية - ]:
     * يعيد اسم الشهر مع السنة (مثال: أغسطس 2026).
     */
    fun getMonthNameArabic(timestampSeconds: Long): String {
        val date = Date(normalizeToMillis(timestampSeconds))
        return "${monthNameFormatter.get().format(date)} ${yearOnlyFormatter.get().format(date)}"
    }

    /**
     * [توليد مفتاح التجميع الشهري - ]:
     * يولد مفتاحاً بصيغة (-) لتصنيف وتجميع المعاملات الشهرية.
     */
    fun getYearMonthKey(timestampSeconds: Long): String =
        yearMonthFormatter.get().format(Date(normalizeToMillis(timestampSeconds)))

    /**
     * [حساب وصياغة الفرق الزمني النسبي بين تاريخين - ]:
     * يحسب الفارق بالثواني ويعيد نصاً تعبيرياً معرباً (مثال: منذ يوم، منذ 5 أيام...).
     *
     * @  الطابع الزمني الأحدث بالثواني.
     * @  الطابع الزمني الأقدم بالثواني.
     * @  سياق التطبيق للوصول لموارد النصوص.
     * @ نص المدة النسبية المنسق.
     */
    fun formatDurationBetween(newerSec: Long, olderSec: Long, context: Context? = null): String {
        val diffSec = (newerSec - olderSec).coerceAtLeast(0)
        val days = diffSec / 86400
        val hours = (diffSec % 86400) / 3600

        return when {
            days > 30 -> context?.getString(R.string.date_diff_over_month).orEmpty()
            days > 1 -> context?.getString(R.string.date_diff_days_pattern, days).orEmpty()
            days == 1L -> context?.getString(R.string.date_diff_one_day).orEmpty()
            hours > 1 -> context?.getString(R.string.date_diff_hours_pattern, hours).orEmpty()
            else -> context?.getString(R.string.date_diff_very_close).orEmpty()
        }
    }

    /**
     * [توحيد الطوابع الزمنية إلى مللي ثانية - ]:
     * إذا كان الطابع بالثواني يحوله لمللي ثانية، وإلا يبقيه كما هو.
     */
    private fun normalizeToMillis(timestamp: Long): Long {
        return if (timestamp in 1..999999999999L) timestamp * 1000L else timestamp
    }
}

