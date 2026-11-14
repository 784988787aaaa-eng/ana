package com.smartledger.aldaftar

import com.smartledger.aldaftar.presentation.formatters.AppDateTimeFormatter
import com.smartledger.aldaftar.presentation.formatters.WesternDigits
import com.smartledger.aldaftar.ui.helper.HabayebMathHelper
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File
import java.math.BigDecimal
import java.util.Date
import java.util.Locale

class WesternDigitsContractTest {

    private var previousDefaultLocale: Locale? = null

    @Before
    fun captureLocale() {
        previousDefaultLocale = Locale.getDefault()
    }

    @After
    fun restoreLocale() {
        previousDefaultLocale?.let { Locale.setDefault(it) }
    }

    private fun projectRoot(): File {
        val cwd = File(System.getProperty("user.dir") ?: ".")
        return if (File(cwd, "app/src/main").exists()) cwd else cwd.parentFile ?: cwd
    }

    private fun containsEasternOrPersianDigits(text: String): Boolean =
        text.any { it in '\u0660'..'\u0669' || it in '\u06F0'..'\u06F9' }

    @Test
    fun resourcesAndSourcesContainNoHardcodedEasternArabicOrPersianDigits() {
        val root = projectRoot()
        val allowedNormalizerFiles = setOf(
            "WesternDigits.kt",
            "HabayebMathHelper.kt",
            "StringUtils.kt",
            "BigDecimalConverter.kt",
            "MathEvaluator.kt"
        )

        val resFiles = File(root, "app/src/main/res")
            .walkTopDown()
            .filter { it.isFile && it.extension.equals("xml", ignoreCase = true) }
            .toList()
        assertTrue("Expected resource XML files", resFiles.isNotEmpty())
        resFiles.forEach { file ->
            assertFalse(
                "Eastern Arabic/Persian digit found in resource ${file.path}",
                containsEasternOrPersianDigits(file.readText())
            )
        }

        val ktFiles = File(root, "app/src/main/java")
            .walkTopDown()
            .filter { it.isFile && it.extension == "kt" && it.name !in allowedNormalizerFiles }
            .toList()
        assertTrue("Expected Kotlin source files", ktFiles.isNotEmpty())
        ktFiles.forEach { file ->
            assertFalse(
                "Eastern Arabic/Persian digit found in source ${file.path}",
                containsEasternOrPersianDigits(file.readText())
            )
        }
    }

    @Test
    fun arabicEgyptLocaleStillOutputsWesternDigitsForDatesNumbersAndTemplates() {
        Locale.setDefault(Locale.forLanguageTag("ar-EG-u-nu-arab"))
        WesternDigits.applyAppWesternLocale()

        val sampleDate = Date(1700000000000L)
        val formattedDate = AppDateTimeFormatter.formatDateArabic(sampleDate)
        val formattedTime = AppDateTimeFormatter.formatTime12h(sampleDate)
        val formattedFull = AppDateTimeFormatter.formatFullDateTime(sampleDate)
        val formattedTemplate = WesternDigits.format("متبقي %1\$d يوماً (%2\$s)", 123, "45.67")
        val formattedAmount = HabayebMathHelper.formatSmart(BigDecimal("123456.78"))

        listOf(formattedDate, formattedTime, formattedFull, formattedTemplate, formattedAmount).forEach { value ->
            assertFalse("Expected only Western 0-9 digits in: $value", containsEasternOrPersianDigits(value))
            assertTrue("Expected at least one Western digit in: $value", value.any { it in '0'..'9' })
        }
        assertTrue("Expected Arabic formatted count", formattedTemplate.contains("123"))
        assertEquals("123456.78", formattedAmount.replace(",", ""))
    }

    @Test
    fun licenseDialogDefaultsToLocalLicensingFirst() {
        val root = projectRoot()
        val licenseDialogFile = File(
            root,
            "app/src/main/java/com/smartledger/aldaftar/ui/screens/license/LicenseDialog.kt"
        )
        assertTrue("Missing LicenseDialog.kt", licenseDialogFile.exists())
        val content = licenseDialogFile.readText()

        assertTrue(content.contains("private const val MODE_LOCAL = 0"))
        assertTrue(content.contains("private const val MODE_CLOUD = 1"))
        assertTrue(content.contains("mutableIntStateOf(MODE_LOCAL)"))

        val localTabIdx = content.indexOf("license_tab_local_btn")
        val cloudTabIdx = content.indexOf("license_tab_cloud_btn")
        assertTrue("Expected both mode tab testTags", localTabIdx in 0 until cloudTabIdx)
    }
}
