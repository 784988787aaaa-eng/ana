package com.smartledger.aldaftar.data.serialization

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class ReportLayoutSpacingContractTest {
    private fun source(relative: String): String =
        File("app/src/main/java/com/smartledger/aldaftar/data/serialization/excel/$relative").readText()

    @Test
    fun excelSpacerRowsStayCompactWithoutChangingRowIndices() {
        val single = source("SingleCustomerExcelEngine.kt")
        val all = source("AllCustomersExcelEngine.kt")
        assertTrue(single.contains("Row(4, 6)"))
        assertTrue(single.contains("Row(6, 6)"))
        assertTrue(single.contains("Row(txRow + 1, 6)"))
        assertTrue(all.contains("Row(4, 6)"))
        assertTrue(all.contains("Row(6, 6)"))
        assertTrue(all.contains("Row(3, 6)"))
        assertTrue(all.contains("Row(foreignRow + 1, 6)"))
    }
}
