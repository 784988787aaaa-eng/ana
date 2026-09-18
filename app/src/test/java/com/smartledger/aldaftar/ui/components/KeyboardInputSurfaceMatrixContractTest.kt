package com.smartledger.aldaftar.ui.components

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Release contract for every intentional text-input surface in the app.
 *
 * This is deliberately source-level: it prevents a new input from silently
 * shipping without an explicit IME action/focus contract. Runtime keyboard
 * visibility still belongs to device/instrumentation verification.
 */
class KeyboardInputSurfaceMatrixContractTest {
    private val inputFiles = setOf(
        "ui/components/CurrencySettingsDialog.kt",
        "ui/screens/BackupRestoreBottomSheet.kt",
        "ui/screens/business/BusinessProfileInfoSection.kt",
        "ui/screens/business/BusinessProfilePhonesSection.kt",
        "ui/screens/habayeb/components/AddCustomerFormFields.kt",
        "ui/screens/habayeb/components/AddTransactionFormFields.kt",
        "ui/screens/habayeb/components/CustomerDeleteAndEditDialogs.kt",
        "ui/screens/habayeb/components/CustomerHistoryTopBar.kt",
        "ui/screens/habayeb/components/ExchangeRateSetupDialog.kt",
        "ui/screens/habayeb/components/MicroAddCategoryDialog.kt",
        "ui/screens/habayeb/components/MicroRenameCategoryDialog.kt",
        "ui/screens/habayeb/components/datetime/RollingDialPicker.kt",
        "ui/screens/habayeb/components/header/HabayebHeaderSearchBar.kt",
        "ui/screens/license/LicenseDialog.kt",
        "ui/screens/security/components/SecurityActivePanel.kt",
        "ui/screens/security/components/SecuritySetupForm.kt",
        "ui/screens/security/lock/RecoveryPhraseContent.kt",
        "ui/screens/settings/components/GeneralSettingsCard.kt",
        "ui/screens/trash/components/TrashTopBarSection.kt"
    )

    private fun mainRoot(): File = listOf(File("src/main/java"), File("app/src/main/java"))
        .firstOrNull(File::exists) ?: error("main source root not found")

    private fun source(relative: String): String = File(mainRoot(), "com/smartledger/aldaftar/$relative")
        .takeIf(File::exists)
        ?.readText()
        ?: error("source not found: $relative")

    private fun fieldCount(source: String): Int =
        Regex("(?<![A-Za-z])(?:BasicTextField|OutlinedTextField|TextField)\\s*\\(").findAll(source).count()

    @Test
    fun inputSurfaceInventoryIsExplicitAndComplete() {
        val root = mainRoot()
        val discovered = root.resolve("com/smartledger/aldaftar/ui").walkTopDown()
            .filter { it.isFile && it.extension == "kt" }
            .filter { fieldCount(it.readText()) > 0 }
            .map { it.relativeTo(root.resolve("com/smartledger/aldaftar")).path.replace(File.separatorChar, '/') }
            .toSet()

        assertEquals("Input surface inventory changed; review the matrix deliberately", inputFiles, discovered)
    }

    @Test
    fun everyInputFieldDeclaresImeOptionsAndActions() {
        inputFiles.forEach { relative ->
            val source = source(relative)
            val fields = fieldCount(source)
            assertTrue("$relative must contain at least one input", fields > 0)
            assertEquals("$relative: every input needs keyboardOptions", fields, source.count("keyboardOptions ="))
            assertEquals("$relative: every input needs keyboardActions", fields, source.count("keyboardActions ="))
        }
    }

    @Test
    fun everyAutomaticKeyboardCallsiteIsExplicitlyOptedIn() {
        val root = mainRoot()
        val offenders = root.resolve("com/smartledger/aldaftar/ui").walkTopDown()
            .filter { it.isFile && it.extension == "kt" && it.name != "KeyboardFocus.kt" }
            .flatMap { file ->
                file.readLines().asSequence().filter { line ->
                    line.contains("RequestFocusAndShowKeyboard(") && !line.contains("autoShow =")
                }.map { "${file.name}:$it" }
            }.toList()

        assertTrue("Every automatic keyboard callsite must explicitly declare autoShow=true/false: $offenders", offenders.isEmpty())
    }

    @Test
    fun transactionCreationUsesOneSharedInputFormFromBothEntryPoints() {
        val popup = source("ui/screens/habayeb/components/AddTransactionPopup.kt")
        val host = source("ui/screens/habayeb/HabayebDialogHost.kt")
        val history = source("ui/screens/habayeb/components/CustomerHistoryDialogsManager.kt")

        assertEquals("AddTransactionPopup owns exactly one shared transaction form", 1, popup.count("AddTransactionFormFields("))
        assertEquals("main entry point must use the shared popup", 1, host.count("AddTransactionPopup("))
        assertEquals("customer-details entry point must use the same popup", 1, history.count("AddTransactionPopup("))
    }

    @Test
    fun transactionFormHasStableTwoStepImeFlow() {
        val source = source("ui/screens/habayeb/components/AddTransactionFormFields.kt")
        assertTrue(source.contains("keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next"))
        assertTrue(source.contains("KeyboardActions(onNext = { descFocusRequester.requestFocus() })"))
        assertTrue(source.contains("keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)"))
        assertTrue(source.contains("focusManager.clearFocus()"))
        assertTrue(source.contains("onDone?.invoke()"))
    }

    @Test
    fun customerFormHasStableFourStepImeFlow() {
        val source = source("ui/screens/habayeb/components/AddCustomerFormFields.kt")
        assertTrue(source.contains("KeyboardActions(onNext = { initialAmountFocusRequester.requestFocus() })"))
        assertTrue(source.contains("KeyboardActions(onNext = { notesFocusRequester.requestFocus() })"))
        assertTrue(source.contains("KeyboardActions(onNext = { phoneFocusRequester.requestFocus() })"))
        assertTrue(source.contains("KeyboardType.Phone, imeAction = ImeAction.Done"))
        assertTrue(source.contains("KeyboardActions(onDone = { onDone() })"))
    }

    @Test
    fun noInputSurfaceUsesGlobalAlwaysVisibleIme() {
        val root = mainRoot().resolve("com/smartledger/aldaftar/ui")
        val offenders = root.walkTopDown()
            .filter { it.isFile && it.extension == "kt" }
            .filter { it.readText().contains("SOFT_INPUT_STATE_ALWAYS_" + "VISIBLE") }
            .map { it.path }
            .toList()
        assertTrue("Global ALWAYS_VISIBLE IME is forbidden: $offenders", offenders.isEmpty())
    }
}
