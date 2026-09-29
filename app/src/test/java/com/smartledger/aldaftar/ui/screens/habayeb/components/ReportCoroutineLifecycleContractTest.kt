package com.smartledger.aldaftar.ui.screens.habayeb.components

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class ReportCoroutineLifecycleContractTest {

    @Test
    fun reportGeneratorsPreserveCancellationAndFinishOnMain() {
        val pdf = source("app/src/main/java/com/smartledger/aldaftar/data/serialization/PdfReportGenerator.kt")
        val csv = source("app/src/main/java/com/smartledger/aldaftar/data/serialization/CsvReportGenerator.kt")
        val booklet = source("app/src/main/java/com/smartledger/aldaftar/data/serialization/pdf/MasterBookletPdfEngine.kt")

        assertTrue(pdf.contains("catch (e: CancellationException)"))
        assertTrue(pdf.contains("throw e"))
        assertTrue(pdf.contains("withContext(NonCancellable + Dispatchers.Main.immediate)"))
        assertTrue(pdf.contains("generateAndHandleCustomerPdfReportAsync"))
        assertTrue(pdf.contains("generateAndHandleAllCustomersPdfReportAsync"))

        assertTrue(csv.contains("catch (e: CancellationException)"))
        assertTrue(csv.contains("throw e"))
        assertTrue(csv.contains("withContext(NonCancellable + Dispatchers.Main.immediate)"))
        assertTrue(csv.contains("generateAndHandleCsvReportAsync"))
        assertTrue(csv.contains("generateAndHandleAllCustomersExcelReportAsync"))

        assertTrue(booklet.contains("catch (e: CancellationException)"))
        assertTrue(booklet.contains("throw e"))
        assertTrue(booklet.contains("withContext(NonCancellable + Dispatchers.Main.immediate)"))
    }

    @Test
    fun pdfGenerationInternalsDoNotSwallowCancellation() {
        val pdf = source("app/src/main/java/com/smartledger/aldaftar/data/serialization/PdfReportGenerator.kt")
        val customerBlock = pdf.substringAfter("private fun generatePdfFileInternal(")
            .substringBefore("private fun generateAllCustomersPdfFileInternal(")
        val allCustomersBlock = pdf.substringAfter("private fun generateAllCustomersPdfFileInternal(")
            .substringBefore("fun triggerShareOrViewIntent(")

        assertTrue(customerBlock.contains("catch (e: CancellationException)"))
        assertTrue(customerBlock.contains("catch (e: Exception)"))
        assertTrue(allCustomersBlock.contains("catch (e: CancellationException)"))
        assertTrue(allCustomersBlock.contains("catch (e: Exception)"))
        assertTrue(customerBlock.indexOf("catch (e: CancellationException)") < customerBlock.indexOf("catch (e: Exception)"))
        assertTrue(allCustomersBlock.indexOf("catch (e: CancellationException)") < allCustomersBlock.indexOf("catch (e: Exception)"))
    }

    @Test
    fun temporaryReportUiDoesNotOwnExportCoroutineScope() {
        val bottomSheet = source("app/src/main/java/com/smartledger/aldaftar/ui/screens/habayeb/components/CustomerHistoryShareBottomSheet.kt")
        val overlay = source("app/src/main/java/com/smartledger/aldaftar/ui/screens/habayeb/components/CustomerHistoryOverlay.kt")
        val comprehensive = source("app/src/main/java/com/smartledger/aldaftar/ui/screens/habayeb/components/ComprehensiveReportDialog.kt")
        val mainLayout = source("app/src/main/java/com/smartledger/aldaftar/ui/main/MainAppLayout.kt")

        assertFalse(bottomSheet.contains("rememberCoroutineScope"))
        assertFalse(bottomSheet.contains("PdfReportGenerator.generateAndHandleCustomerPdfReportAsync"))
        assertFalse(bottomSheet.contains("CsvReportGenerator.generateAndHandleCsvReportAsync"))

        assertFalse(overlay.contains("rememberCoroutineScope"))
        assertTrue(overlay.contains("scope = viewModel.viewModelScope"))
        assertTrue(overlay.contains("PdfReportGenerator.generateAndHandleCustomerPdfReportAsync"))
        assertTrue(overlay.contains("CsvReportGenerator.generateAndHandleCsvReportAsync"))

        assertFalse(comprehensive.contains("rememberCoroutineScope"))
        assertTrue(comprehensive.contains("reportCoroutineScope: CoroutineScope"))
        assertTrue(comprehensive.contains("val job = reportCoroutineScope.launch"))
        assertTrue(comprehensive.contains("scope = reportCoroutineScope"))

        assertTrue(mainLayout.contains("reportCoroutineScope = scope"))
    }

    @Test
    fun productionReportCodeDoesNotCreateIndependentGlobalOrBlockingScopes() {
        val productionRoot = File(findProjectRoot(), "app/src/main")
        val forbidden = listOf("GlobalScope", "runBlocking")
        productionKotlinFiles(productionRoot).forEach { file ->
            val text = file.readText()
            forbidden.forEach { token ->
                assertFalse("${file.path} contains $token", text.contains(token))
            }
        }
    }

    private fun source(relativePath: String): String =
        File(findProjectRoot(), relativePath).readText()

    private fun productionKotlinFiles(root: File): List<File> =
        root.walkTopDown().filter { it.isFile && it.extension == "kt" }.toList()

    private fun findProjectRoot(): File {
        var directory = File(System.getProperty("user.dir")).absoluteFile
        repeat(8) {
            if (File(directory, "settings.gradle.kts").isFile) return directory
            directory = directory.parentFile ?: return@repeat
        }
        error("Project root not found")
    }
}
