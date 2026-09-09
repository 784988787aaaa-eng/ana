package com.smartledger.aldaftar.data.backup

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BackupPathManagerTest {
    @Test
    fun automaticNameUsesShortProfessionalDateFormat() {
        val date = SimpleDateFormat("yyyy-MM-dd", Locale.US).parse("2026-09-09")!!
        assertEquals("SMN_2026-09-09.slb", BackupPathManager().automaticFile(date).name)
        assertTrue(BackupPathManager().automaticFile(date).parentFile!!.path.contains("2026-09"))
    }
}
