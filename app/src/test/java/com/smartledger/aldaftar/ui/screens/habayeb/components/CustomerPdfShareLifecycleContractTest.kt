package com.smartledger.aldaftar.ui.screens.habayeb.components

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class CustomerPdfShareLifecycleContractTest {

    @Test
    fun shareBottomSheetDelegatesPdfExportToOwnerInsteadOfOwningCoroutineScope() {
        val source = locateSourceFile()

        assertTrue(source.contains("onPdfAction: (PdfAction) -> Unit"))
        assertFalse(source.contains("PdfReportGenerator.generateAndHandleCustomerPdfReportAsync"))
        assertFalse(source.contains("val appScope = rememberCoroutineScope()"))
    }

    private fun locateSourceFile(): String {
        val relativePath = "app/src/main/java/com/smartledger/aldaftar/ui/screens/habayeb/components/CustomerHistoryShareBottomSheet.kt"
        var directory = File(System.getProperty("user.dir")).absoluteFile
        repeat(5) {
            val candidate = File(directory, relativePath)
            if (candidate.isFile) return candidate.readText()
            directory = directory.parentFile ?: return@repeat
        }
        error("Production source file not found: $relativePath")
    }
}
