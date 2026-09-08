package com.smartledger.aldaftar.data.repository

import com.smartledger.aldaftar.domain.model.RecurringConfig
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal
import java.util.Calendar

class RecurringScheduleCalculatorTest {
    @Test fun firstRunUsesMillisBoundsAndReturnsUnixSeconds() {
        val start = day(2026, Calendar.JANUARY, 10)
        val end = day(2026, Calendar.JANUARY, 12, 23, 59)
        val config = config(start, end)
        val due = RecurringScheduleCalculator.dueOccurrences(config, end)
        assertEquals(3, due.size)
        assertTrue(due.all { it < 10_000_000_000L })
        assertEquals(start / 1000 + 9 * 60 + 30, due.first())
    }

    @Test fun lastExecutedOccurrenceIsNotRepeated() {
        val start = day(2026, Calendar.JANUARY, 10)
        val end = day(2026, Calendar.JANUARY, 12, 23, 59)
        val first = start / 1000 + 9 * 60 + 30
        val due = RecurringScheduleCalculator.dueOccurrences(config(start, end, first), end)
        assertEquals(2, due.size)
        assertTrue(first !in due)
        assertTrue(due.all { it > first })
    }

    @Test fun startAndEndBoundariesAreInclusiveForScheduledTime() {
        val start = day(2026, Calendar.JANUARY, 10, 9, 30)
        val config = config(start, start)
        assertEquals(listOf(start / 1000), RecurringScheduleCalculator.dueOccurrences(config, start))
    }


    @Test fun catchUpIsNotSilentlyLimitedToOneYear() {
        val start = day(2024, Calendar.JANUARY, 1)
        val end = day(2025, Calendar.JANUARY, 2, 23, 59)
        val due = RecurringScheduleCalculator.dueOccurrences(config(start, end), end)
        assertEquals(368, due.size)
    }


    @Test fun boundedGenerationLimitsWorkBeforeBuildingTheFullCatchUpList() {
        val start = day(2020, Calendar.JANUARY, 1)
        val end = day(2025, Calendar.JANUARY, 1, 23, 59)
        val due = RecurringScheduleCalculator.dueOccurrences(config(start, end), end, maxOccurrences = 50)
        assertEquals(50, due.size)
        assertEquals(due.sorted(), due)
    }

    @Test fun sequentialBatchesAdvanceWithoutDuplicatesOrLoss() {
        val start = day(2026, Calendar.JANUARY, 1)
        val end = day(2026, Calendar.APRIL, 30, 23, 59)
        val first = RecurringScheduleCalculator.dueOccurrences(config(start, end), end, maxOccurrences = 50)
        val second = RecurringScheduleCalculator.dueOccurrences(
            config(start, end, first.last()), end, maxOccurrences = 50
        )
        val combined = first + second
        val expected = RecurringScheduleCalculator.dueOccurrences(config(start, end), end)
        assertEquals(100, combined.size)
        assertEquals(combined.size, combined.toSet().size)
        assertEquals(expected.take(100), combined)
    }


    @Test fun weeklyAndMonthlyFrequenciesRespectConfiguredCalendarSelectors() {
        val weeklyStart = day(2026, Calendar.JANUARY, 5)
        val weeklyEnd = day(2026, Calendar.JANUARY, 18, 23, 59)
        val weekly = RecurringScheduleCalculator.dueOccurrences(
            config(weeklyStart, weeklyEnd).copy(
                frequency = "WEEKLY",
                daysOfWeek = listOf(Calendar.MONDAY, Calendar.FRIDAY)
            ),
            weeklyEnd
        )
        assertEquals(
            listOf(
                day(2026, Calendar.JANUARY, 5, 9, 30) / 1000,
                day(2026, Calendar.JANUARY, 9, 9, 30) / 1000,
                day(2026, Calendar.JANUARY, 12, 9, 30) / 1000,
                day(2026, Calendar.JANUARY, 16, 9, 30) / 1000
            ),
            weekly
        )

        val monthlyStart = day(2026, Calendar.JANUARY, 30)
        val monthlyEnd = day(2026, Calendar.MARCH, 31, 23, 59)
        val monthly = RecurringScheduleCalculator.dueOccurrences(
            config(monthlyStart, monthlyEnd).copy(
                frequency = "MONTHLY",
                daysOfMonth = listOf(1, 31)
            ),
            monthlyEnd
        )
        assertEquals(
            listOf(
                day(2026, Calendar.JANUARY, 31, 9, 30) / 1000,
                day(2026, Calendar.FEBRUARY, 1, 9, 30) / 1000,
                day(2026, Calendar.MARCH, 1, 9, 30) / 1000,
                day(2026, Calendar.MARCH, 31, 9, 30) / 1000
            ),
            monthly
        )
    }

    private fun config(start: Long, end: Long, lastExecuted: Long = 0) = RecurringConfig(
        id = "r1", originalTxId = "tx1", customerId = "c1", customerName = "عميل",
        amount = BigDecimal.ONE, type = "OWED_BY_THEM", description = "", frequency = "DAILY",
        daysOfWeek = emptyList(), daysOfMonth = emptyList(), timeHour = 9, timeMinute = 30,
        startDateMillis = start, endDateMillis = end, lastExecutedTimestamp = lastExecuted
    )

    private fun day(year: Int, month: Int, day: Int, hour: Int = 0, minute: Int = 0): Long =
        Calendar.getInstance().apply {
            clear(); set(year, month, day, hour, minute, 0)
        }.timeInMillis
}
