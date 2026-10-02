package com.smartledger.aldaftar.ui.theme

import androidx.compose.ui.unit.sp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

class TypographyAndContentContractTest {

    @Test
    fun testArabicTypographyLetterSpacingInvariant() {
        val styles = listOf(
            Typography.displayLarge,
            Typography.displayMedium,
            Typography.displaySmall,
            Typography.headlineLarge,
            Typography.headlineMedium,
            Typography.headlineSmall,
            Typography.titleLarge,
            Typography.titleMedium,
            Typography.titleSmall,
            Typography.bodyLarge,
            Typography.bodyMedium,
            Typography.bodySmall,
            Typography.labelLarge,
            Typography.labelMedium,
            Typography.labelSmall,
            MizanTypographyTokens.metricHero,
            MizanTypographyTokens.metricValue,
            MizanTypographyTokens.metricLabel,
            MizanTypographyTokens.cardTitle,
            MizanTypographyTokens.cardSubtitle,
            MizanTypographyTokens.dialogTitle,
            MizanTypographyTokens.dialogSubtitle,
            MizanTypographyTokens.badge,
            MizanTypographyTokens.button
        )

        for (style in styles) {
            assertEquals(
                "Arabic typography requires letterSpacing = 0.sp to prevent disconnected cursive glyphs",
                0.sp,
                style.letterSpacing
            )
            assertTrue(
                "Line height must provide adequate clearance for Arabic ascenders and descenders",
                style.lineHeight.value >= style.fontSize.value
            )
        }
    }

    @Test
    fun testStringsResourceContentAndCanonicalGovernance() {
        val stringsFile = File("src/main/res/values/strings.xml").let {
            if (it.exists()) it else File("app/src/main/res/values/strings.xml")
        }
        assertTrue("strings.xml must exist", stringsFile.exists())

        val factory = DocumentBuilderFactory.newInstance()
        val builder = factory.newDocumentBuilder()
        val doc = builder.parse(stringsFile)
        val stringNodes = doc.getElementsByTagName("string")

        val stringsMap = mutableMapOf<String, String>()
        for (i in 0 until stringNodes.length) {
            val node = stringNodes.item(i)
            val name = node.attributes.getNamedItem("name")?.nodeValue ?: continue
            val text = node.textContent ?: ""
            stringsMap[name] = text
        }

        // 1. Check customer canonical naming
        val editNameTitle = stringsMap["habayeb_edit_name_title"]
        assertTrue(
            "Customer edit title must use canonical term 'العميل' rather than 'الزبون'",
            editNameTitle?.contains("العميل") == true && editNameTitle.contains("الزبون") == false
        )

        // 2. Check taa marboota typo fix
        val autoGenSub = stringsMap["habayeb_auto_generated_sub"]
        assertTrue(
            "Auto generated sub text must use 'للمعاملة' with taa marboota",
            autoGenSub?.contains("للمعاملة") == true
        )

        // 3. Check summary typo fix
        val indexTitle = stringsMap["pdf_index_title"]
        assertTrue(
            "Index title must use 'وملخص' (summary) not 'ومخلص'",
            indexTitle?.contains("وملخص") == true
        )

        // 4. Check trash canonical naming
        val trashEmptied = stringsMap["toast_trash_emptied"]
        assertTrue(
            "Trash emptied toast must use canonical term 'سلة المحذوفات' rather than 'سلة المهملات'",
            trashEmptied?.contains("سلة المحذوفات") == true
        )

        // 5. Check payment financial canonical term
        val paymentToThem = stringsMap["pdf_tx_type_payment_to_them"]
        assertEquals(
            "Financial payment to them term must be canonical 'سداد' rather than 'تسديد'",
            "سداد",
            paymentToThem
        )

        // 6. Check hamzat wasl grammar
        val defaultCurrencyTitle = stringsMap["settings_currency_title"]
        assertEquals(
            "Grammar rule for hamzat wasl in الافتراضية",
            "العملة الافتراضية",
            defaultCurrencyTitle
        )

        // 7. Check category placeholder grammar
        val catPlaceholder = stringsMap["habayeb_category_add_placeholder"]
        assertTrue(
            "Category placeholder must have proper alif 'الأصدقاء'",
            catPlaceholder?.startsWith("الأصدقاء") == true
        )

        // 8. Check trash status balanced
        val trashBalanced = stringsMap["trash_status_balanced"]
        assertEquals(
            "Trash status balanced must match canonical 'مصفّى'",
            "مصفّى",
            trashBalanced
        )
    }
}
