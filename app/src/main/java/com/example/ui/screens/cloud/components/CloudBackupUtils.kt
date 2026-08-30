package com.example.ui.screens.cloud.components

/*
 * =====================================================================================
 * حزمة الأدوات المساعدة للنسخ الاحتياطي السحابي (Cloud Backup Utilities Package)
 * -------------------------------------------------------------------------------------
 * تحتوي هذه الفئة على دوال معالجة وتحليل وتنسيق بيانات النسخ السحابي:
 * - استخراج التاريخ والوقت من اسم الملف البنيوي (Structured Filename) مثل `Mzd_YYYY-MM-DD_HH-mm-ss.db`.
 * - التحليل البديل للطوابع الزمنية القياسية (ISO 8601 Timestamps).
 * - تحويل الوقت إلى نظام 12 ساعة مع مؤشرات صباحاً/مساءً المحلية.
 * =====================================================================================
 */

import android.content.Context
import android.util.Log
import com.example.R
import com.example.ui.viewmodel.FinanceConstants

private const val TAG = "CloudBackupUtils"

/*
 * =====================================================================================
 * دالة تنسيق تاريخ ووقت النسخة السحابية (formatBackupDateTime)
 * -------------------------------------------------------------------------------------
 * [الوصف والهدف]:
 * تقوم هذه الدالة باستخراج وتنسيق التاريخ والوقت البشري لعرضه في واجهة المستخدم:
 * 1. تحاول أولاً قراءة النمط البنيوي لاسم الملف المشفر من الثابت `Mzd_YYYY-MM-DD_HH-mm-ss`.
 * 2. إذا تعذر ذلك، تستخدم طابع الإنشاء الزمني القياسي ISO (مثل `2026-06-15T22:30:15.000Z`).
 * 3. تحويل نظام 24 ساعة إلى نظام 12 ساعة مع الرمز اللغوي المناسب (ص/م).
 *
 * [المُدخلات]:
 * - context: سياق التطبيق للوصول إلى الموارد اللغوية.
 * - filename: اسم ملف النسخة الاحتياطية.
 * - createdTimeIso: الطابع الزمني للإنشاء بصيغة ISO.
 *
 * [المُخرجات]:
 * - Pair<String, String>: زوج يحتوي على (تاريخ منسق، وقت منسق).
 * =====================================================================================
 */
fun formatBackupDateTime(context: Context, filename: String, createdTimeIso: String): Pair<String, String> {
    var dateString = context.getString(R.string.cloud_date_unknown)
    var timeString = "--:--"
    
    // أولاً: محاولة استخراج التاريخ والوقت من بادئة ونمط اسم الملف Mzd_
    if (filename.startsWith(FinanceConstants.BACKUP_CLOUD_FILE_PREFIX) && filename.length >= 18) {
        try {
            val clean = filename.replace(FinanceConstants.BACKUP_CLOUD_FILE_PREFIX, "").replace(FinanceConstants.BACKUP_FILE_EXTENSION, "")
            val segments = clean.split("_")
            if (segments.isNotEmpty()) {
                val datePart = segments[0]
                val dateSplit = datePart.split("-")
                if (dateSplit.size == 3) {
                    dateString = "${dateSplit[2]}-${dateSplit[1]}-${dateSplit[0]}"
                }
                
                if (segments.size > 1) {
                    val timePart = segments[1]
                    val timeSplit = timePart.split("-")
                    if (timeSplit.size >= 2) {
                        val hour = timeSplit[0].toIntOrNull() ?: 12
                        val min = timeSplit[1].toIntOrNull() ?: 0
                        val amPm = if (hour >= 12) context.getString(R.string.cloud_time_pm) else context.getString(R.string.cloud_time_am)
                        val hour12 = when {
                            hour == 0 -> 12
                            hour > 12 -> hour - 12
                            else -> hour
                        }
                        timeString = String.format("%d:%02d %s", hour12, min, amPm)
                        return Pair(dateString, timeString)
                    }
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error formatting date from filename: ${e.message}")
        }
    }
    
    // ثانياً: الخيار البديل (Fallback) - تحليل طابع ISO الزمني (مثال: 2026-06-15T22:30:15.000Z)
    if (createdTimeIso.isNotEmpty()) {
        try {
            val parts = createdTimeIso.split("T")
            if (parts.size >= 2) {
                val datePart = parts[0]
                val dateSplit = datePart.split("-")
                if (dateSplit.size == 3) {
                    dateString = "${dateSplit[2]}-${dateSplit[1]}-${dateSplit[0]}"
                }
                val timePart = parts[1]
                val timeSplit = timePart.split(":")
                if (timeSplit.size >= 2) {
                    val hour = timeSplit[0].toIntOrNull() ?: 12
                    val min = timeSplit[1].toIntOrNull() ?: 0
                    val amPm = if (hour >= 12) context.getString(R.string.cloud_time_pm) else context.getString(R.string.cloud_time_am)
                    val hour12 = when {
                        hour == 0 -> 12
                        hour > 12 -> hour - 12
                        else -> hour
                    }
                    timeString = String.format("%d:%02d %s", hour12, min, amPm)
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error formatting date from ISO timestamp: ${e.message}")
        }
    }
    
    return Pair(dateString, timeString)
}

