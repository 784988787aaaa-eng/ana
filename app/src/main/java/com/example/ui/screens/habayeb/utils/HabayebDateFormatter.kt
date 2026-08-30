package com.example.ui.screens.habayeb.utils

/*
 * =====================================================================================
 * مُنسق التواريخ والأوقات لقسم الحسابات وحبايب (Habayeb Date Formatter Delegate)
 * -------------------------------------------------------------------------------------
 * [الوصف والهدف]:
 * كائن وسيط وواجهة تفويض (Delegate) للمنسق الزمني الموحد للتطبيق (AppDateTimeFormatter):
 * 1. يوفر دوال تنسيق التواريخ باللغة العربية والإنجليزية.
 * 2. يدعم صيغ التواريخ القصيرة والمطولة وتنسيق الوقت بنظام 12 ساعة (صباحاً/مساءً).
 * 3. يدعم استقبال المدخلات ككائنات Date أو طوابع زمنية بالثواني أو الميلي ثانية.
 * 4. يحدد معرّفات الموارد النصية لأسماء أيام الأسبوع.
 * =====================================================================================
 */

import com.example.domain.formatters.AppDateTimeFormatter
import java.util.Date

/*
 * =====================================================================================
 * كائن منسق التواريخ (HabayebDateFormatter Object)
 * -------------------------------------------------------------------------------------
 * يفوض كافة عمليات التنسيق إلى AppDateTimeFormatter لضمان الاتساق عبر كامل التطبيق.
 * =====================================================================================
 */
object HabayebDateFormatter {
    /*
     * تنسيق التاريخ باللغة العربية (اليوم والشهر والسنة)
     */
    fun formatDateArabic(date: Date): String = AppDateTimeFormatter.formatDateArabic(date)
    fun formatDateArabic(timestampSeconds: Long): String = AppDateTimeFormatter.formatDateArabic(timestampSeconds)

    /*
     * تنسيق التاريخ بالصيغة الافتراضية
     */
    fun formatDateDefault(date: Date): String = AppDateTimeFormatter.formatDateDefault(date)

    /*
     * تنسيق التاريخ بالصيغة المختصرة
     */
    fun formatShortDate(date: Date): String = AppDateTimeFormatter.formatShortDate(date)
    fun formatShortDate(timestampSeconds: Long): String = AppDateTimeFormatter.formatShortDate(timestampSeconds)

    /*
     * تنسيق الوقت بنظام 12 ساعة (مع إشارة ص/م)
     */
    fun formatTime12h(date: Date): String = AppDateTimeFormatter.formatTime12h(date)
    fun formatTime12h(timestampSeconds: Long): String = AppDateTimeFormatter.formatTime12h(timestampSeconds)

    /*
     * تنسيق التاريخ والوقت كاملاً
     */
    fun formatFullDateTime(date: Date): String = AppDateTimeFormatter.formatFullDateTime(date)
    fun formatFullDateTime(timestampMillis: Long): String = AppDateTimeFormatter.formatFullDateTime(timestampMillis)

    /*
     * استخراج معرف مورد اسم اليوم في الأسبوع
     */
    fun getDayOfWeekResId(timestampSeconds: Long): Int = AppDateTimeFormatter.getDayOfWeekResId(timestampSeconds)
}

