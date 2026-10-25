package com.smartledger.aldaftar

import androidx.compose.ui.graphics.Color
import com.smartledger.aldaftar.data.local.entities.DeletedItemEntity
import com.smartledger.aldaftar.data.local.entities.HabayebCustomer
import com.smartledger.aldaftar.ui.screens.trash.utils.TrashItemParser
import com.smartledger.aldaftar.ui.screens.trash.utils.TrashStrings
import java.io.File
import java.math.BigDecimal
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class SecurityAndTrashRefactorContractTest {

    private fun resolveRepoRoot(): File {
        val cwd = File(System.getProperty("user.dir") ?: ".")
        return if (File(cwd, "UI_SECURITY_TRASH_REFACTOR_PLAN.md").exists()) {
            cwd
        } else {
            cwd.parentFile ?: cwd
        }
    }

    @Test
    fun securityAndTrashRefactorPlanExistsWithAllVerifiedChecklists() {
        val root = resolveRepoRoot()
        val planFile = File(root, "UI_SECURITY_TRASH_REFACTOR_PLAN.md")
        assertTrue("UI_SECURITY_TRASH_REFACTOR_PLAN.md must exist in project root", planFile.exists())

        val content = planFile.readText()
        assertTrue(content.contains("1. تحليل الوضع الحالي لكلتا الواجهتين والملفات المرتبطة بهما"))
        assertTrue(content.contains("2. التصميم المستهدف لكل شاشة"))
        assertTrue(content.contains("3. قائمة مهام التنفيذ وحالتها"))
        assertTrue(content.contains("4. معايير القبول النهائية"))
        assertFalse("All tasks in UI_SECURITY_TRASH_REFACTOR_PLAN.md must be completed", content.contains("- [ ]"))
    }

    @Test
    fun securityDialogAndSetupFormEliminateRedundantHeadersAndEnforceSyncSave() {
        val root = resolveRepoRoot()
        val securityScreen = File(
            root,
            "app/src/main/java/com/smartledger/aldaftar/ui/screens/SecurityScreen.kt"
        ).readText()
        val setupForm = File(
            root,
            "app/src/main/java/com/smartledger/aldaftar/ui/screens/security/components/SecuritySetupForm.kt"
        ).readText()
        val activePanel = File(
            root,
            "app/src/main/java/com/smartledger/aldaftar/ui/screens/security/components/SecurityActivePanel.kt"
        ).readText()

        // Both SecurityScreen and SecurityDialog must use saveSettingsSync inside try/catch
        val syncSaveCount = Regex("""viewModel\.saveSettingsSync\(""").findAll(securityScreen).count()
        assertTrue(
            "Both SecurityScreen and SecurityDialog must use saveSettingsSync for reliable persistence (found $syncSaveCount)",
            syncSaveCount >= 4
        )
        assertFalse(
            "SecurityScreen/Dialog must not use fire-and-forget viewModel.saveSettings(",
            securityScreen.contains("viewModel.saveSettings(")
        )

        // SecuritySetupForm must not repeat long subtitles and must provide inline error feedback
        assertFalse("SecuritySetupForm must not render redundant sec_setup_subtitle", setupForm.contains("sec_setup_subtitle"))
        assertFalse("SecuritySetupForm must not render redundant sec_recovery_subtitle", setupForm.contains("sec_recovery_subtitle"))
        assertTrue("SecuritySetupForm must provide inline PIN mismatch feedback", setupForm.contains("sec_pin_mismatch_inline"))
        assertTrue("SecuritySetupForm must provide inline incomplete PIN feedback", setupForm.contains("sec_pin_incomplete_inline"))

        // SecurityActivePanel must display recovery hint status badge
        assertTrue("SecurityActivePanel must display recovery hint status", activePanel.contains("sec_hint_configured_badge"))
    }

    @Test
    fun trashComponentsEnforceRefactoredHeaderToolbarCardAndSpecialStates() {
        val root = resolveRepoRoot()
        val topBar = File(
            root,
            "app/src/main/java/com/smartledger/aldaftar/ui/screens/trash/components/TrashTopBarSection.kt"
        ).readText()
        val toolbar = File(
            root,
            "app/src/main/java/com/smartledger/aldaftar/ui/screens/trash/components/TrashFilterToolbar.kt"
        ).readText()
        val card = File(
            root,
            "app/src/main/java/com/smartledger/aldaftar/ui/screens/trash/components/TrashItemCard.kt"
        ).readText()
        val listSection = File(
            root,
            "app/src/main/java/com/smartledger/aldaftar/ui/screens/trash/components/TrashItemListSection.kt"
        ).readText()
        val deadDialogFile = File(
            root,
            "app/src/main/java/com/smartledger/aldaftar/ui/screens/trash/components/TrashDetailDeleteConfirmDialog.kt"
        )

        assertFalse("Unused TrashDetailDeleteConfirmDialog.kt must be removed", deadDialogFile.exists())
        assertTrue("TrashTopBarSection must use AutoMirrored ArrowBack", topBar.contains("Icons.AutoMirrored.Filled.ArrowBack"))
        assertTrue("TrashTopBarSection must use guarded Empty Trash pill", topBar.contains("trash_empty_short"))
        assertTrue("TrashFilterToolbar must provide quick reset chip", toolbar.contains("trash_filter_reset_short"))

        // TrashItemCard must only render a single selection checkmark
        val checkIconCount = Regex("""Icons\.Default\.Check""").findAll(card).count()
        assertEquals("TrashItemCard must render exactly one Check icon when selected", 1, checkIconCount)
        assertTrue("TrashItemCard must support 2-line wrapping for descriptions", card.contains("maxLines = 2"))
        assertTrue("TrashItemCard must use labeled Restore pill", card.contains("trash_restore_short"))

        // TrashItemListSection must handle search empty, filter empty, and loading states
        assertTrue(listSection.contains("trash_empty_search_title"))
        assertTrue(listSection.contains("trash_empty_filter_title"))
        assertTrue(listSection.contains("trash_loading_message"))
    }

    @Test
    fun trashItemParserHandlesLongTextsEmptyNotesLargeAmountsAndCustomerBundles() {
        val strings = TrashStrings(
            systemHabayeb = "HABAYEB",
            unknownText = "بيانات غير متوفرة",
            noPhoneText = "لا يوجد هاتف",
            noNotesText = "لا توجد ملاحظات",
            debtTxText = "معاملة دين",
            owedByThemText = "دين عليه ↗️",
            paymentByThemText = "سداد منه ↙️",
            owedToThemText = "دين له ↙️",
            paymentToThemText = "سداد له ↗️",
            categoryLabelText = "التصنيف: %s",
            customerLabelText = "معاملة تابعة لحساب: %s",
            equivalentInfoTemplate = "يعادل %s (صرف: %s)",
            progressTextTemplate = "%s",
            customerBundleDescTemplate = "حزمة عميل: تشمل %d معاملات تابعة",
            ledgerBundleDescTemplate = "تشمل %d حركات",
            exchangeRateLabelText = "صرف:"
        )

        val longCustomerName = "شركة الآفاق التجارية المتقدمة للاستيراد والتصدير والتوريدات العامة المحدودة"
        val longDescription = "دفعة أولى من قيمة توريد بضاعة المواد الغذائية والتموينية حسب الفاتورة رقم 2026/998877 مع خصم فرق العملة المتفق عليه"
        val customers = listOf(
            HabayebCustomer(
                id = "cust-1",
                name = longCustomerName,
                phone = "+967777123456",
                notes = "",
                createdAt = 1760000000000L
            )
        )

        // 1. Deleted transaction with long customer name, long description, and large amount
        val txEntity = DeletedItemEntity(
            id = "del-tx-1",
            sourceSystem = "HABAYEB",
            originalTableName = "habayeb_transactions",
            jsonData = """
                {
                    "id": "tx-1",
                    "customerId": "cust-1",
                    "type": "OWED_BY_THEM",
                    "amount": "987654321.50",
                    "description": "[USD] $longDescription",
                    "timestamp": 1760000000000
                }
            """.trimIndent(),
            deletedAt = 1760000500000L
        )

        val parsedTx = TrashItemParser.parse(
            item = txEntity,
            customersList = customers,
            currencySymbol = "ر.ي",
            strings = strings,
            primaryColor = Color.Blue,
            secondaryColor = Color.Cyan,
            errorColor = Color.Red,
            outlineColor = Color.Gray
        )

        assertEquals(longCustomerName, parsedTx.customerName)
        assertEquals(longDescription, parsedTx.rawDescription)
        assertEquals("دين عليه ↗️", parsedTx.txTypeDisplay)
        assertTrue(parsedTx.isExpense)
        assertEquals(0, BigDecimal("987654321.50").compareTo(parsedTx.amount))

        // 2. Deleted transaction with empty notes
        val emptyNoteEntity = DeletedItemEntity(
            id = "del-tx-2",
            sourceSystem = "HABAYEB",
            originalTableName = "habayeb_transactions",
            jsonData = """
                {
                    "id": "tx-2",
                    "customerId": "cust-1",
                    "type": "PAYMENT_BY_THEM",
                    "amount": "5000",
                    "description": "",
                    "timestamp": 1760000000000
                }
            """.trimIndent(),
            deletedAt = 1760000500000L
        )

        val parsedEmptyNote = TrashItemParser.parse(
            item = emptyNoteEntity,
            customersList = customers,
            currencySymbol = "ر.ي",
            strings = strings,
            primaryColor = Color.Blue,
            secondaryColor = Color.Cyan,
            errorColor = Color.Red,
            outlineColor = Color.Gray
        )

        assertEquals("", parsedEmptyNote.rawDescription)
        assertEquals("لا توجد ملاحظات", parsedEmptyNote.titleText)
        assertEquals(longCustomerName, parsedEmptyNote.customerName)
    }
}
