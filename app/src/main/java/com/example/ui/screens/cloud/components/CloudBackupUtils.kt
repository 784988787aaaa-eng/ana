package com.example.ui.screens.cloud.components

import android.content.Context
import android.util.Log
import com.example.R
import com.example.ui.viewmodel.FinanceConstants

private const val TAG = "CloudBackupUtils"

/**
 * دالة مساعدة لتنسيق التاريخ والوقت لملفات النسخ الاحتياطي السحابي.
 */
fun formatBackupDateTime(context: Context, filename: String, createdTimeIso: String): Pair<String, String> {
    var dateString = context.getString(R.string.cloud_date_unknown)
    var timeString = "--:--"
    
    // First, try from filename which starts with Mzd_
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
    
    // Fallback: Parse ISO timestamp createdTimeIso (e.g. 2026-06-15T22:30:15.000Z)
    if (createdTimeIso.isNotEmpty()) {
        try {
            // Split Date and Time
            val parts = createdTimeIso.split("T")
            if (parts.size >= 2) {
                // Parse Date: 2026-06-15
                val datePart = parts[0]
                val dateSplit = datePart.split("-")
                if (dateSplit.size == 3) {
                    dateString = "${dateSplit[2]}-${dateSplit[1]}-${dateSplit[0]}"
                }
                // Parse Time: 22:30:15...
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
