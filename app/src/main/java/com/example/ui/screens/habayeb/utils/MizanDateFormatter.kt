package com.example.ui.screens.habayeb.utils

/*
 * =====================================================================================
 * مُنسق ومحول التواريخ لقسم الميزان (Mizan Date Formatter Delegate)
 * -------------------------------------------------------------------------------------
 * [الوصف والهدف]:
 * كائن وسيط وواجهة تفويض (Delegate) للمنسق الزمني الموحد للتطبيق (AppDateTimeFormatter):
 * 1. يوفر دوال سريعة لتنسيق التواريخ بالصيغة المختصرة أو المفصلة باللغة العربية.
 * 2. يدعم تنسيق الأوقات بنظام 12 ساعة للمدفوعات والحركات المالية.
 * 3. يدعم استقبال المدخلات ككائنات Date أو طوابع زمنية بالثواني أو الميلي ثانية.
 * =====================================================================================
 */

import com.example.domain.formatters.AppDateTimeFormatter
import java.util.Date

/*
 * =====================================================================================
 * كائن منسق التواريخ لشاشات الميزان (MizanDateFormatter Object)
 * -------------------------------------------------------------------------------------
 * يفوض كافة عمليات التنسيق إلى AppDateTimeFormatter لتوحيد عرض التواريخ.
 * =====================================================================================
 */
object MizanDateFormatter {
    /*
     * تنسيق التاريخ بالصيغة المختصرة
     */
    fun formatShortDate(date: Date): String = AppDateTimeFormatter.formatShortDate(date)
    fun formatShortDate(timestampSeconds: Long): String = AppDateTimeFormatter.formatShortDate(timestampSeconds)

    /*
     * تنسيق التاريخ باللغة العربية
     */
    fun formatDateArabic(date: Date): String = AppDateTimeFormatter.formatDateArabic(date)
    fun formatDateArabic(timestampSeconds: Long): String = AppDateTimeFormatter.formatDateArabic(timestampSeconds)

    /*
     * تنسيق الوقت بنظام 12 ساعة (ص/م)
     */
    fun formatTime12h(date: Date): String = AppDateTimeFormatter.formatTime12h(date)
    fun formatTime12h(timestampSeconds: Long): String = AppDateTimeFormatter.formatTime12h(timestampSeconds)

    /*
     * تنسيق التاريخ والوقت كاملاً
     */
    fun formatFullDateTime(date: Date): String = AppDateTimeFormatter.formatFullDateTime(date)
    fun formatFullDateTime(timestampMillis: Long): String = AppDateTimeFormatter.formatFullDateTime(timestampMillis)
}

