package com.smartledger.aldaftar.presentation.formatters

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Date

/**
 * اختبارات منسق التواريخ والأوقات
 */
class AppDateTimeFormatterTest {

    @Test
    fun testDateFormattingNotBlanks() {
        val now = Date()
        val formattedArabic = AppDateTimeFormatter.formatDateArabic(now)
        val formattedDefault = AppDateTimeFormatter.formatDateDefault(now)
        val formattedIso = AppDateTimeFormatter.formatDateIso(now)

        assertTrue(formattedArabic.isNotBlank())
        assertTrue(formattedDefault.isNotBlank())
        assertTrue(formattedIso.isNotBlank())
    }

    @Test
    fun testTimestampNormalization() {
        // فحص التعامل مع الثواني مقابل المللي ثانية
        val seconds = 1700000000L
        val formattedFromSec = AppDateTimeFormatter.formatDateIso(seconds)
        assertTrue(formattedFromSec.contains("2023"))
    }
}
