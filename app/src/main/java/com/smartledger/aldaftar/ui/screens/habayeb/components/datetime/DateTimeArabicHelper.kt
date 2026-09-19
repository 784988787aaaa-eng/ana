package com.smartledger.aldaftar.ui.screens.habayeb.components.datetime

import com.smartledger.aldaftar.presentation.formatters.WesternDigits
import java.util.Calendar

/**
 * مساعد معالجة وتنسيق التواريخ والمدد الزمنية باللغة العربية الفصحى بصياغة احترافية ودقيقة نحوياً.
 */
object DateTimeArabicHelper {

    private val ARABIC_MONTH_NAMES = arrayOf(
        "يناير", "فبراير", "مارس", "أبريل", "مايو", "يونيو",
        "يوليو", "أغسطس", "سبتمبر", "أكتوبر", "نوفمبر", "ديسمبر"
    )

    fun getMonthNameArabic(monthZeroIndexed: Int): String {
        return ARABIC_MONTH_NAMES.getOrElse(monthZeroIndexed) { "${monthZeroIndexed + 1}" }
    }

    fun getDayOfWeekArabic(dayOfWeek: Int): String {
        return when (dayOfWeek) {
            Calendar.SATURDAY -> "السبت"
            Calendar.SUNDAY -> "الأحد"
            Calendar.MONDAY -> "الاثنين"
            Calendar.TUESDAY -> "الثلاثاء"
            Calendar.WEDNESDAY -> "الأربعاء"
            Calendar.THURSDAY -> "الخميس"
            Calendar.FRIDAY -> "الجمعة"
            else -> ""
        }
    }

    fun formatArabicDateFull(calendar: Calendar): String {
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val monthName = getMonthNameArabic(calendar.get(Calendar.MONTH))
        val year = calendar.get(Calendar.YEAR)
        return WesternDigits.normalize("$day $monthName $year")
    }

    fun formatArabicDateWithDayName(calendar: Calendar): String {
        val dayName = getDayOfWeekArabic(calendar.get(Calendar.DAY_OF_WEEK))
        val dateFull = formatArabicDateFull(calendar)
        return if (dayName.isNotBlank()) "$dayName، $dateFull" else dateFull
    }

    fun calculateDaysBetween(startCal: Calendar, endCal: Calendar): Long {
        val s = Calendar.getInstance().apply {
            timeInMillis = startCal.timeInMillis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val e = Calendar.getInstance().apply {
            timeInMillis = endCal.timeInMillis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val diffMillis = e.timeInMillis - s.timeInMillis
        return if (diffMillis < 0L) 0L else (diffMillis / (24L * 60 * 60 * 1000))
    }

    /**
     * حساب وصياغة عدد الأيام وفقاً لقواعد العدد والمعدود في اللغة العربية الفصحى دون أي ركاكة.
     */
    fun formatDaysCountArabic(days: Long): String {
        val count = kotlin.math.abs(days)
        val remainder100 = count % 100L

        val formattedDigits = WesternDigits.normalize(count.toString())

        return when {
            count == 0L -> "يوم واحد (نفس اليوم)"
            count == 1L -> "يوم واحد"
            count == 2L -> "يومان"
            remainder100 in 3L..10L -> "$formattedDigits أيام"
            remainder100 in 11L..99L -> "$formattedDigits يوماً"
            count % 100L == 0L -> "$formattedDigits يوم"
            count % 100L == 1L -> "$formattedDigits يوم"
            count % 100L == 2L -> "$formattedDigits يومان"
            else -> "$formattedDigits يوماً"
        }
    }

    fun formatDurationLabel(days: Long): String {
        return "المدة: ${formatDaysCountArabic(days)}"
    }
}
