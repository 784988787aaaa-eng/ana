package com.example.domain

import android.content.Context
import androidx.compose.ui.graphics.Color
import com.example.R
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private val EMOJI_REGEX = "[\\uD83C-\\uDBFF\\uDC00-\\uDFFF]+".toRegex()
private val ARABIC_LOCALE = Locale("ar")

private val DAY_NAME_FORMATTER = ThreadLocal.withInitial { SimpleDateFormat("EEEE", ARABIC_LOCALE) }
private val DATE_NUMBERS_FORMATTER = ThreadLocal.withInitial { SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH) }
private val DATE_TIME_FORMATTER = ThreadLocal.withInitial { SimpleDateFormat("dd-MM-yyyy | hh:mm", Locale.ENGLISH) }
private val AM_PM_FORMATTER = ThreadLocal.withInitial { SimpleDateFormat("a", ARABIC_LOCALE) }

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

private object CategoryColors {
    val AMBER_DARK = Color(0xFF451A03)
    val AMBER_LIGHT = Color(0xFFFEF3C7)
    val PINK_DARK = Color(0xFF4D1222)
    val PINK_LIGHT = Color(0xFFFCE7F3)
    val GRAY_LIGHT_DARK = Color(0xFF262626)
    val GRAY_LIGHT_LIGHT = Color(0xFFEFEFEF)
    val RED_SOFT_DARK = Color(0xFF3E1F1F)
    val RED_SOFT_LIGHT = Color(0xFFFEE2E2)
    val YELLOW_DARK = Color(0xFF3F3701)
    val YELLOW_LIGHT = Color(0xFFFEF9C3)
    val BLUE_SOFT_DARK = Color(0xFF172554)
    val BLUE_SOFT_LIGHT = Color(0xFFDBEAFE)
    val SKY_DARK = Color(0xFF0C4A6E)
    val SKY_LIGHT = Color(0xFFE0F2FE)
    val PURPLE_DARK = Color(0xFF3B0764)
    val PURPLE_LIGHT = Color(0xFFF3E8FF)
    val EMERALD_SOFT_DARK = Color(0xFF064E3B)
    val EMERALD_SOFT_LIGHT = Color(0xFFD1FAE5)
    val GREEN_FIFTY_DARK = Color(0xFF022C22)
    val GREEN_FIFTY_LIGHT = Color(0xFFECFDF5)
    val SLATE_DEFAULT_DARK = Color(0xFF0F172A)
    val SLATE_DEFAULT_LIGHT = Color(0xFFF1F5F9)

    inline fun select(isDark: Boolean, dark: Color, light: Color) = if (isDark) dark else light
}

fun getEmojiBgColor(emoji: String, isDark: Boolean = false): Color {
    return when (emoji) {
        "🌾" -> CategoryColors.select(isDark, CategoryColors.AMBER_DARK, CategoryColors.AMBER_LIGHT)
        "🍬", "🍭" -> CategoryColors.select(isDark, CategoryColors.PINK_DARK, CategoryColors.PINK_LIGHT)
        "☕" -> CategoryColors.select(isDark, CategoryColors.GRAY_LIGHT_DARK, CategoryColors.GRAY_LIGHT_LIGHT)
        "🔥" -> CategoryColors.select(isDark, CategoryColors.RED_SOFT_DARK, CategoryColors.RED_SOFT_LIGHT)
        "⚡" -> CategoryColors.select(isDark, CategoryColors.YELLOW_DARK, CategoryColors.YELLOW_LIGHT)
        "💧" -> CategoryColors.select(isDark, CategoryColors.BLUE_SOFT_DARK, CategoryColors.BLUE_SOFT_LIGHT)
        "🚀", "🌐" -> CategoryColors.select(isDark, CategoryColors.SKY_DARK, CategoryColors.SKY_LIGHT)
        "🍼", "👶" -> CategoryColors.select(isDark, CategoryColors.PURPLE_DARK, CategoryColors.PURPLE_LIGHT)
        "🎒" -> CategoryColors.select(isDark, CategoryColors.SKY_DARK, CategoryColors.SKY_LIGHT)
        "🏦" -> CategoryColors.select(isDark, CategoryColors.EMERALD_SOFT_DARK, CategoryColors.EMERALD_SOFT_LIGHT)
        "💰" -> CategoryColors.select(isDark, CategoryColors.GREEN_FIFTY_DARK, CategoryColors.GREEN_FIFTY_LIGHT)
        else -> CategoryColors.select(isDark, CategoryColors.SLATE_DEFAULT_DARK, CategoryColors.SLATE_DEFAULT_LIGHT)
    }
}

private val CALENDAR_THREAD_LOCAL = ThreadLocal.withInitial { Calendar.getInstance() }

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

fun formatAuditLogTime(timestampMs: Long): String {
    val date = Date(timestampMs)
    val datePart = DATE_TIME_FORMATTER.get()?.format(date).orEmpty()
    val amPmPart = AM_PM_FORMATTER.get()?.format(date).orEmpty()
    return "$datePart $amPmPart"
}
