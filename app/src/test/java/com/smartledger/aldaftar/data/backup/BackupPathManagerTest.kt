package com.smartledger.aldaftar.data.backup

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BackupPathManagerTest {
    @Test
    fun isSafeBackupNameValidatesCorrectFormat() {
        assertTrue("SMN_2026-09-09.slb".matches(Regex("SMN_\\d{4}-\\d{2}(?:-\\d{2}(?:_\\d{4})?)?\\.slb", RegexOption.IGNORE_CASE)))
        assertTrue("SMN_2026-09-09_1430.slb".matches(Regex("SMN_\\d{4}-\\d{2}(?:-\\d{2}(?:_\\d{4})?)?\\.slb", RegexOption.IGNORE_CASE)))
    }

    @Test
    fun fileNamingFormatting() {
        val date = SimpleDateFormat("yyyy-MM-dd", Locale.US).parse("2026-09-09")!!
        val dateName = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(date)
        val expected = "${BackupPathManager.PREFIX}$dateName${BackupPathManager.EXTENSION}"
        assertEquals("SMN_2026-09-09.slb", expected)
    }
}
