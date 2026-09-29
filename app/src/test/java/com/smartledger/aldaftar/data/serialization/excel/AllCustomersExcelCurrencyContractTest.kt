package com.smartledger.aldaftar.data.serialization.excel

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AllCustomersExcelCurrencyContractTest {
    @Test
    fun foreignTotalsMustSeparateOwedAndOwedToBeforeNetting() {
        val source = File("src/main/java/com/smartledger/aldaftar/data/serialization/excel/AllCustomersExcelEngine.kt").readText()
        assertTrue(source.contains("val foreignHeaders = listOf(\"الحساب\", \"العملة\", \"له\", \"عليه\", \"الصافي\")"))
        assertTrue(source.contains("SUMIFS(C5:C\$foreignLastRow,B5:B\$foreignLastRow,\\\"\$code\\\")"))
        assertTrue(source.contains("SUMIFS(D5:D\$foreignLastRow,B5:B\$foreignLastRow,\\\"\$code\\\")"))
        assertTrue(source.contains("B\$rowNo-C\$rowNo"))
        assertFalse(source.contains("SUMIF(B5:B1048576,\\\"\$code\\\",C5:C1048576)"))
    }
}
