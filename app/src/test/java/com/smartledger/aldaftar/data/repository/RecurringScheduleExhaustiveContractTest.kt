package com.smartledger.aldaftar.data.repository

import com.smartledger.aldaftar.domain.model.RecurringConfig
import java.math.BigDecimal
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class RecurringScheduleExhaustiveContractTest {
    private fun millis(y: Int, m: Int, d: Int, h: Int = 10, min: Int = 0): Long =
        Calendar.getInstance().apply {
            clear()
            set(y, m - 1, d, h, min, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

    private fun config(
        frequency: String,
        start: Long,
        end: Long,
        last: Long = 0,
        dow: List<Int> = emptyList(),
        dom: List<Int> = emptyList()
    ): RecurringConfig = RecurringConfig(
        id = "r",
        originalTxId = "tx",
        customerId = "c",
        customerName = "C",
        amount = BigDecimal("100"),
        type = "OWED_BY_THEM",
        description = "x",
        frequency = frequency,
        daysOfWeek = dow,
        daysOfMonth = dom,
        timeHour = 10,
        timeMinute = 0,
        startDateMillis = start,
        endDateMillis = end,
        lastExecutedTimestamp = last
    )

    @Test
    fun dailyBoundariesAreInclusiveAndReturnedOnce() {
        val start = millis(2026, 1, 1)
        val end = millis(2026, 1, 3)
        val out = RecurringScheduleCalculator.dueOccurrences(config("DAILY", start, end), end)
        assertEquals(listOf(start / 1000, (start + 86400000L) / 1000, (start + 2 * 86400000L) / 1000), out)
    }

    @Test
    fun lastExecutedOccurrenceIsNeverRepeated() {
        val start = millis(2026, 1, 1)
        val last = start / 1000
        val end = millis(2026, 1, 3)
        val out = RecurringScheduleCalculator.dueOccurrences(config("DAILY", start, end, last), end)
        assertEquals(listOf((start + 86400000L) / 1000, (start + 2 * 86400000L) / 1000), out)
    }

    @Test
    fun maxOccurrencesIsAHardOutputBound() {
        val start = millis(2020, 1, 1)
        val end = millis(2026, 1, 1)
        val out = RecurringScheduleCalculator.dueOccurrences(config("DAILY", start, end), end, 7)
        assertEquals(7, out.size)
        assertTrue(out.zipWithNext().all { it.second > it.first })
    }

    @Test
    fun weeklySelectorUsesOnlyConfiguredWeekdays() {
        val start = millis(2026, 1, 4)
        val end = millis(2026, 1, 11)
        val sunday = Calendar.SUNDAY
        val out = RecurringScheduleCalculator.dueOccurrences(config("WEEKLY", start, end, dow = listOf(sunday)), end)
        assertTrue(out.isNotEmpty())
        assertTrue(out.all {
            Calendar.getInstance().apply { timeInMillis = it * 1000 }.get(Calendar.DAY_OF_WEEK) == sunday
        })
    }

    @Test
    fun monthlySelectorUsesOnlyConfiguredDays() {
        val start = millis(2026, 1, 1)
        val end = millis(2026, 2, 5)
        val out = RecurringScheduleCalculator.dueOccurrences(config("MONTHLY", start, end, dom = listOf(5)), end)
        assertTrue(out.isNotEmpty())
        assertTrue(out.all {
            Calendar.getInstance().apply { timeInMillis = it * 1000 }.get(Calendar.DAY_OF_MONTH) == 5
        })
    }

    @Test
    fun unsupportedFrequencyProducesNoOccurrences() {
        val start = millis(2026, 1, 1)
        val end = millis(2026, 1, 10)
        assertTrue(RecurringScheduleCalculator.dueOccurrences(config("YEARLY", start, end), end).isEmpty())
    }
}
