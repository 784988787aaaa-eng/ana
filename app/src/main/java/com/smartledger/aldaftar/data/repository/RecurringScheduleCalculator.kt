package com.smartledger.aldaftar.data.repository

import com.smartledger.aldaftar.domain.model.RecurringConfig
import java.util.Calendar

/** يحافظ على وحدة الثواني في وقت التنفيذ ووحدة المللي ثانية في حدود الجدول. */
object RecurringScheduleCalculator {
    fun dueOccurrences(config: RecurringConfig, nowMillis: Long, maxOccurrences: Int = Int.MAX_VALUE): List<Long> {
        require(maxOccurrences > 0)
        val endMillis = minOf(nowMillis, config.endDateMillis)
        val calendar = Calendar.getInstance().apply {
            timeInMillis = if (config.lastExecutedTimestamp > 0) {
                config.lastExecutedTimestamp * 1000
            } else {
                config.startDateMillis
            }
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (config.lastExecutedTimestamp > 0) add(Calendar.DAY_OF_YEAR, 1)
        }
        if (calendar.timeInMillis > endMillis) return emptyList()

        val occurrences = ArrayList<Long>()
        while (calendar.timeInMillis <= endMillis && occurrences.size < maxOccurrences) {
            val occurrence = Calendar.getInstance().apply {
                timeInMillis = calendar.timeInMillis
                set(Calendar.HOUR_OF_DAY, config.timeHour)
                set(Calendar.MINUTE, config.timeMinute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val occurrenceMillis = occurrence.timeInMillis
            val matches = when (config.frequency) {
                "DAILY" -> true
                "WEEKLY" -> occurrence.get(Calendar.DAY_OF_WEEK) in config.daysOfWeek
                "MONTHLY" -> occurrence.get(Calendar.DAY_OF_MONTH) in config.daysOfMonth
                else -> false
            }
            if (matches && occurrenceMillis in config.startDateMillis..endMillis) {
                occurrences += occurrenceMillis / 1000
            }
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }
        return occurrences.sorted()
    }
}
