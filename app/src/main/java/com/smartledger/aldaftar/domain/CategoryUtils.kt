/**
 * =====================================================================
 * ملف: أدوات تصنيف المعاملات وتنسيق سجلات التدقيق (.)
 * =====================================================================
 * 
 * [الغرض العام والتعليمي من الملف]:
 * يوفر هذا الملف مجموعة من الدوال المساعدة الذكية الخاصة بتصنيف المعاملات المالية،
 * واستخراج الرموز التعبيرية () الملائمة للسلع والخدمات تلقائياً بناءً على
 * الكلمات المفتاحية في البيان، وتحديد درجات الألوان المتناسقة مع كل تصنيف للواجهات
 * الداكنة والفاتحة، بالإضافة إلى تنسيق وتجميع تواريخ سجلات التدقيق والأمان بالعربية.
 * 
 * [المسؤوليات المعمارية والتقنية للملف]:
 * 1. استخراج الرموز التعبيرية الذكي (  ):
 *    - التعرف على الكلمات المفتاحية الشائعة (دقيق، غاز، كهرباء، ماء، شاي، إنترنت...)
 *      أو استخلاص الرمز التعبيري المضمن باستخدام التعبيرات النمطية [].
 * 2. إدارة لوحة ألوان التصنيفات (   ):
 *    - اختيار ألوان الخلفية المناسبة لكل رمز تعبيري متوافقة مع الوضعين الفاتح والداكن.
 * 3. تجميع وتنسيق تواريخ سجلات الرقابة (   ):
 *    - تحويل الطوابع الزمنية إلى عبارات عربية نسبية ("اليوم"، "أمس"، "أول أمس")
 *      أو اسم اليوم وتاريخه لسهولة القراءة في شاشات السجلات.
 * 4. أمان الخيوط في تنسيق التواريخ (-  ):
 *    - استخدام [] لكائنات [] و [] لمنع التضارب
 *      بين الخيوط المتعددة أثناء المعالجة المتزامنة.
 */
package com.smartledger.aldaftar.domain

// ---------------------------------------------------------------------
// استيراد حزم سياق أندرويد، ورسومات ، وموارد التطبيق وتنسيق التواريخ
// ---------------------------------------------------------------------
import android.content.Context
import androidx.compose.ui.graphics.Color
import com.smartledger.aldaftar.R
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

import com.smartledger.aldaftar.ui.theme.CategoryPalette

// ---------------------------------------------------------------------
// المتغيرات والثوابت الخاصة بالتعبيرات النمطية ومحليات اللغة وتنسيق التواريخ.
// ---------------------------------------------------------------------

/** تعبير نمطي للبحث عن الرموز التعبيرية في النصوص. */
private val EMOJI_REGEX = "[\\uD83C-\\uDBFF\\uDC00-\\uDFFF]+".toRegex()

/** إعدادات اللغة العربية المستخدمة في التنسيق المحلي. */
private val ARABIC_LOCALE = Locale("ar")

/** منسق آمن للخيوط لجلب اسم اليوم بالعربية. */
private val DAY_NAME_FORMATTER = ThreadLocal.withInitial { SimpleDateFormat("EEEE", ARABIC_LOCALE) }

/** منسق آمن للخيوط للتاريخ الرقمي القياسي. */
private val DATE_NUMBERS_FORMATTER = ThreadLocal.withInitial { SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH) }

/** منسق آمن للخيوط للتاريخ والوقت مجتمعين. */
private val DATE_TIME_FORMATTER = ThreadLocal.withInitial { SimpleDateFormat("dd-MM-yyyy | hh:mm", Locale.ENGLISH) }

/** منسق آمن للخيوط لعرض الفترة الصباحية أو المسائية بالعربية. */
private val AM_PM_FORMATTER = ThreadLocal.withInitial { SimpleDateFormat("a", ARABIC_LOCALE) }

// =========================================================================
// قسم استخراج وتصنيف الرموز التعبيرية.
// =========================================================================

/**
 * [استخراج الرمز التعبيري المناسب للتصنيف - ]:
 * يفحص نص الفئة أو البيان المالي ويطابق الكلمات المفتاحية الشهيرة لإرجاع
 * الرمز التعبيري الأنسب، أو يستخرج الإيموجي المدخل يدوياً، أو يعيد الإيموجي الافتراضي.
 *
 * @  نص الفئة أو وصف السلعة.
 * @  الرمز التعبيري الافتراضي في حال عدم المطابقة.
 * @ الرمز التعبيري النصي المختار.
 */
fun extractEmoji(category: String, defaultEmoji: String): String {
    if (category.isEmpty()) return defaultEmoji
    val cat = category.lowercase()

    return when {
        cat.contains("دقيق") || cat.contains("🌾") -> "🌾"
        cat.contains("غاز") || cat.contains("🔥") -> "🔥"
        cat.contains("كهرباء") || cat.contains("⚡") -> "⚡"
        cat.contains("ماء") || cat.contains("💧") -> "💧"
        cat.contains("حليب") || cat.contains("🍼") -> "🍼"
        cat.contains("حفاظ") || cat.contains("👶") -> "👶"
        cat.contains("سكر") || cat.contains("🍬") -> "🍬"
        cat.contains("شاي") || cat.contains("☕") -> "☕"
        cat.contains("نت") || cat.contains("رصيد") || cat.contains("🌐") || cat.contains("إنترنت") -> "🌐"
        cat.contains("مدرس") || cat.contains("🎒") -> "🎒"
        cat.contains("ادخار") || cat.contains("🏦") -> "🏦"
        else -> EMOJI_REGEX.find(category)?.value ?: defaultEmoji
    }
}

/**
 * دالة مساعدة سريعة لاختيار اللون المناسب وفق حالة السمة (داكنة أم فاتحة).
 */
private inline fun selectColor(isDark: Boolean, dark: Color, light: Color) = if (isDark) dark else light

// =========================================================================
// قسم تعيين ألوان خلفيات التصنيفات.
// =========================================================================

/**
 * [جلب لون الخلفية المتناسق مع الرمز التعبيري - ]:
 * يحدد درجة اللون المناسبة لبطاقة الفئة بناءً على طبيعة الرمز التعبيري ونوع المظهر.
 *
 * @  الرمز التعبيري للسلعة أو التصنيف.
 * @  هل واجهة التطبيق في الوضع الليلي/الداكن.
 * @ كائن [] المتناسق بصرياً من لوحة [].
 */
fun getEmojiBgColor(emoji: String, isDark: Boolean = false): Color {
    return when (emoji) {
        "🌾" -> selectColor(isDark, CategoryPalette.AMBER_DARK, CategoryPalette.AMBER_LIGHT)
        "🍬", "🍭" -> selectColor(isDark, CategoryPalette.PINK_DARK, CategoryPalette.PINK_LIGHT)
        "☕" -> selectColor(isDark, CategoryPalette.GRAY_LIGHT_DARK, CategoryPalette.GRAY_LIGHT_LIGHT)
        "🔥" -> selectColor(isDark, CategoryPalette.RED_SOFT_DARK, CategoryPalette.RED_SOFT_LIGHT)
        "⚡" -> selectColor(isDark, CategoryPalette.YELLOW_DARK, CategoryPalette.YELLOW_LIGHT)
        "💧" -> selectColor(isDark, CategoryPalette.BLUE_SOFT_DARK, CategoryPalette.BLUE_SOFT_LIGHT)
        "🚀", "🌐" -> selectColor(isDark, CategoryPalette.SKY_DARK, CategoryPalette.SKY_LIGHT)
        "🍼", "👶" -> selectColor(isDark, CategoryPalette.PURPLE_DARK, CategoryPalette.PURPLE_LIGHT)
        "🎒" -> selectColor(isDark, CategoryPalette.SKY_DARK, CategoryPalette.SKY_LIGHT)
        "🏦" -> selectColor(isDark, CategoryPalette.EMERALD_SOFT_DARK, CategoryPalette.EMERALD_SOFT_LIGHT)
        "💰" -> selectColor(isDark, CategoryPalette.GREEN_FIFTY_DARK, CategoryPalette.GREEN_FIFTY_LIGHT)
        else -> selectColor(isDark, CategoryPalette.SLATE_DEFAULT_DARK, CategoryPalette.SLATE_DEFAULT_LIGHT)
    }
}

// =========================================================================
// قسم تنسيق تواريخ سجلات التدقيق والأمان.
// =========================================================================

/** تقويم آمن للخيوط لحساب الفروق الزمنية بين الأيام */
private val CALENDAR_THREAD_LOCAL = ThreadLocal.withInitial { Calendar.getInstance() }

/**
 * [تجميع وتنسيق عنوان تاريخ سجل التدقيق - ]:
 * يقارن تاريخ العملية مع الوقت الحالي ويعيد عنواناً نسبياً مبسطاً ("اليوم"، "أمس"، "أول أمس")
 * أو التاريخ المفصل مع اسم اليوم بالعربية.
 *
 * @  الطابع الزمني للعملية بالمللي ثانية.
 * @  سياق التطبيق للوصول لموارد النصوص المترجمة.
 * @ نص العنوان المناسب لرأس مجموعة السجلات.
 */
fun getAuditLogGroupDate(timestampMs: Long, context: Context? = null): String {
    val cal = CALENDAR_THREAD_LOCAL.get()
    val now = System.currentTimeMillis()

    cal.timeInMillis = now
    val currentYear = cal.get(Calendar.YEAR)
    val currentDayOfYear = cal.get(Calendar.DAY_OF_YEAR)

    cal.timeInMillis = timestampMs
    val logYear = cal.get(Calendar.YEAR)
    val logDayOfYear = cal.get(Calendar.DAY_OF_YEAR)

    val isSameYear = logYear == currentYear
    val dayDiff = if (isSameYear) currentDayOfYear - logDayOfYear else -1

    return when {
        isSameYear && dayDiff == 0 -> context?.getString(R.string.ledger_day_today).orEmpty()
        isSameYear && dayDiff == 1 -> context?.getString(R.string.ledger_day_yesterday).orEmpty()
        isSameYear && dayDiff == 2 -> context?.getString(R.string.ledger_day_before_yesterday).orEmpty()
        else -> {
            val date = Date(timestampMs)
            val dayName = DAY_NAME_FORMATTER.get()?.format(date).orEmpty()
            val dateNumbers = DATE_NUMBERS_FORMATTER.get()?.format(date).orEmpty()
            "$dayName، $dateNumbers"
        }
    }
}

/**
 * [تنسيق وقت سجل التدقيق - ]:
 * يحول الطابع الزمني إلى صيغة مقروءة تتضمن التاريخ والوقت مع مؤشر (ص/م) بالعربية.
 *
 * @  الطابع الزمني بالمللي ثانية.
 * @ النص المنسق (مثال: "25-08-2026 | 02:30 م").
 */
fun formatAuditLogTime(timestampMs: Long): String {
    val date = Date(timestampMs)
    val datePart = DATE_TIME_FORMATTER.get()?.format(date).orEmpty()
    val amPmPart = AM_PM_FORMATTER.get()?.format(date).orEmpty()
    return "$datePart $amPmPart"
}

