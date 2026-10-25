package com.smartledger.aldaftar

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Automated verification contract for the Root-to-Leaf UI/UX Refactoring
 * documented in `/UI_REFACTOR_MASTER_PLAN.md`.
 */
class UiRefactorMasterPlanContractTest {

    private fun resolveRepoRoot(): File {
        val cwd = File(System.getProperty("user.dir") ?: ".")
        return if (File(cwd, "UI_REFACTOR_MASTER_PLAN.md").exists()) {
            cwd
        } else {
            cwd.parentFile ?: cwd
        }
    }

    @Test
    fun masterPlanFileExistsInProjectRootWithAllRequiredSections() {
        val root = resolveRepoRoot()
        val planFile = File(root, "UI_REFACTOR_MASTER_PLAN.md")
        assertTrue("UI_REFACTOR_MASTER_PLAN.md must exist in project root", planFile.exists())

        val content = planFile.readText()
        assertTrue(content.contains("1. هدف إعادة الهيكلة"))
        assertTrue(content.contains("2. المبادئ التصميمية الملزمة"))
        assertTrue(content.contains("3. خريطة كاملة لبنية المشروع"))
        assertTrue(content.contains("4. الجرد الكامل لجميع الشاشات والنوافذ والمكونات"))
        assertTrue(content.contains("7. تدقيق وإعادة هيكلة التقويم ومنتقيات التاريخ والوقت"))
        assertTrue(content.contains("8. تدقيق وإعادة هيكلة ميزة التكرار التلقائي للمعاملات"))
        assertTrue(content.contains("10. خطة التنفيذ المرحلية (الدفعات التنفيذية 1 إلى 11)"))
        assertTrue(content.contains("12. سجل التحقق التفصيلي"))
        assertTrue(content.contains("13. المشكلات المتبقية أو العوائق"))
        assertTrue(content.contains("14. توجيه تنفيذي إضافي — تحسين الحسابات المثبتة والإنشاء الذكي للحسابات"))
        assertFalse("All checklist items in UI_REFACTOR_MASTER_PLAN.md must be completed", content.contains("- [ ]"))
        assertTrue(content.contains("VERIFIED"))
    }

    @Test
    fun zeroRawAlertDialogsExistInUiPackage() {
        val root = resolveRepoRoot()
        val uiDir = File(root, "app/src/main/java/com/smartledger/aldaftar/ui")
        val offenders = uiDir.walkTopDown()
            .filter { it.isFile && it.extension == "kt" }
            .filter { it.readText().contains("AlertDialog(") }
            .map { it.relativeTo(uiDir).path }
            .toList()

        assertTrue(
            "All dialogs in ui/ must use MizanAnimatedDialog / MizanDialogCard / MizanDeleteConfirmationDialog instead of raw AlertDialog: $offenders",
            offenders.isEmpty()
        )
    }

    @Test
    fun navigationDrawerUsesBusinessProfileAndStorefrontIcon() {
        val root = resolveRepoRoot()
        val drawerFile = File(
            root,
            "app/src/main/java/com/smartledger/aldaftar/ui/components/AppNavigationDrawer.kt"
        )
        val source = drawerFile.readText()
        assertTrue(
            "AppNavigationDrawer must observe businessProfileViewModel.profile",
            source.contains("businessProfileViewModel.profile.collectAsStateWithLifecycle()")
        )
        assertTrue(
            "AppNavigationDrawer must use Icons.Default.Storefront for Business Profile",
            source.contains("Icons.Default.Storefront")
        )
        assertFalse(
            "AppNavigationDrawer must not use Icons.Default.People for Business Profile",
            source.contains("Icons.Default.People")
        )
    }

    @Test
    fun businessProfileSectionsEliminateHeaderAndSupportingTextDuplication() {
        val root = resolveRepoRoot()
        val infoSection = File(
            root,
            "app/src/main/java/com/smartledger/aldaftar/ui/screens/business/BusinessProfileInfoSection.kt"
        ).readText()
        val logoSection = File(
            root,
            "app/src/main/java/com/smartledger/aldaftar/ui/screens/business/BusinessProfileLogoSection.kt"
        ).readText()
        val phonesSection = File(
            root,
            "app/src/main/java/com/smartledger/aldaftar/ui/screens/business/BusinessProfilePhonesSection.kt"
        ).readText()

        assertFalse(
            "BusinessProfileInfoSection must not use tall supportingText blocks for char counters",
            infoSection.contains("supportingText =")
        )
        assertTrue(
            "BusinessProfileInfoSection must hide inner section header when isDialog is true",
            infoSection.contains("if (!isDialog)")
        )
        assertFalse(
            "BusinessProfileLogoSection must not repeat biz_logo_subtitle",
            logoSection.contains("biz_logo_subtitle")
        )
        assertFalse(
            "BusinessProfilePhonesSection must not repeat biz_phones_subtitle",
            phonesSection.contains("biz_phones_subtitle")
        )
    }

    @Test
    fun calculatorAndCurrencyAndPrivacyDialogsEnforceRefactoredInvariants() {
        val root = resolveRepoRoot()
        val calculator = File(
            root,
            "app/src/main/java/com/smartledger/aldaftar/ui/screens/CalculatorDialog.kt"
        ).readText()
        val currency = File(
            root,
            "app/src/main/java/com/smartledger/aldaftar/ui/components/CurrencySettingsDialog.kt"
        ).readText()
        val privacy = File(
            root,
            "app/src/main/java/com/smartledger/aldaftar/ui/components/PrivacyPolicyDialog.kt"
        ).readText()

        assertTrue(
            "CalculatorDialog must enforce LayoutDirection.Ltr on expression display",
            calculator.contains("LayoutDirection.Ltr")
        )
        assertTrue(
            "CurrencySettingsDialog must use heightIn(min = 56.dp) to prevent Arabic clipping",
            currency.contains("heightIn(min = 56.dp)")
        )
        assertTrue(
            "PrivacyPolicyDialog must include a Close button in its header",
            privacy.contains("Icons.Default.Close")
        )
    }

    @Test
    fun calendarAndDateTimePickersEnforceMizanDialogStandardsAndRtl() {
        val root = resolveRepoRoot()
        val singlePicker = File(
            root,
            "app/src/main/java/com/smartledger/aldaftar/ui/screens/habayeb/components/CustomDateTimePickerDialog.kt"
        ).readText()
        val rangePicker = File(
            root,
            "app/src/main/java/com/smartledger/aldaftar/ui/screens/habayeb/components/datetime/CustomDateRangePickerContent.kt"
        ).readText()
        val dialPicker = File(
            root,
            "app/src/main/java/com/smartledger/aldaftar/ui/screens/habayeb/components/datetime/RollingDialPicker.kt"
        ).readText()

        assertTrue(
            "CustomDateTimePickerDialog must enforce LayoutDirection.Rtl and MizanDialogHeader",
            singlePicker.contains("LayoutDirection.Rtl") && singlePicker.contains("MizanDialogHeader")
        )
        assertTrue(
            "CustomDateTimePickerDialog must provide quick Today action",
            singlePicker.contains("datetime_picker_quick_today")
        )
        assertTrue(
            "CustomDateRangePickerContent must use MizanDialogCard and MizanDialogHeader",
            rangePicker.contains("MizanDialogCard") && rangePicker.contains("MizanDialogHeader")
        )
        assertTrue(
            "RollingDialPicker must use 24.dp arrow targets and 32.dp value box height",
            dialPicker.contains(".size(24.dp)") && dialPicker.contains(".height(32.dp)")
        )
    }

    @Test
    fun autoRecurringTransactionsEnforceMizanDialogCardAndNonTruncatingDateTimeLayout() {
        val root = resolveRepoRoot()
        val recurringPopup = File(
            root,
            "app/src/main/java/com/smartledger/aldaftar/ui/screens/habayeb/components/RecurringTransactionPopup.kt"
        ).readText()
        val recurringDateTime = File(
            root,
            "app/src/main/java/com/smartledger/aldaftar/ui/screens/habayeb/components/RecurringDateTimeSection.kt"
        ).readText()
        val recurringFreq = File(
            root,
            "app/src/main/java/com/smartledger/aldaftar/ui/screens/habayeb/components/RecurringFrequencySelector.kt"
        ).readText()

        assertTrue(
            "RecurringTransactionPopup must use MizanDialogCard, MizanDialogHeader, and MizanDialogInnerCard",
            recurringPopup.contains("MizanDialogCard") &&
                recurringPopup.contains("MizanDialogHeader") &&
                recurringPopup.contains("MizanDialogInnerCard")
        )
        assertTrue(
            "RecurringTransactionPopup must show explicit status badge and fallback warning when no next occurrence exists",
            recurringPopup.contains("تكرار مفعّل") &&
                recurringPopup.contains("لا توجد دفعة قادمة ضمن النطاق المحدد")
        )
        assertTrue(
            "RecurringDateTimeSection must use a 2-tier Column layout with Locale.ENGLISH Western digits",
            recurringDateTime.contains("Column(") && recurringDateTime.contains("Locale.ENGLISH")
        )
        assertTrue(
            "RecurringFrequencySelector monthly grid must format day numbers with Locale.ENGLISH",
            recurringFreq.contains("Locale.ENGLISH")
        )
    }
}
