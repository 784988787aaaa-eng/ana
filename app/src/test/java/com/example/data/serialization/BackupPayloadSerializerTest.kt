/*
 * التوثيق الهندسي العربي — الدفعة 13 / Automated Tests
 * الملف: BackupPayloadSerializerTest.kt
 *
 * هذا الملف هو اختبار آلي من منظومة مشروع «الدفتر الذكي / ميزان الدار».
 * الغرض من التوثيق المضاف هنا هو شرح ما يختبره الملف وكيف يحمي العقود
 * الحسابية والبيانية أثناء التطوير، دون تغيير أي سطر تنفيذي أصلي.
 *
 * قاعدة الثبات: الكتلة البرمجية الأصلية أدناه محفوظة حرفياً؛ الإضافة الوحيدة
 * هي التعليقات التوثيقية والمعمارية.
 */

package com.example.data.serialization

import com.example.data.local.entities.AppSettings
import com.example.data.local.entities.FixedCommitment
import com.example.data.local.entities.HabayebCustomer
import com.example.data.local.entities.TransactionDb
import com.example.domain.model.TransactionType
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.StringWriter
import java.math.BigDecimal

/**
 * اختبارات الحالات الطرفية لمنظومة النسخ الاحتياطي وحساب البصمة المشفرة (BackupPayloadSerializer)
 *
 * التوثيق المعماري:
 * يختبر هذا الملف ثبات البصمة المنطقية (Deterministic Hash)، وضمان عدم فقدان دقة الكسور العشرية،
 * وسلامة تصدير واستيراد البيانات التراكمية، ومقاومة التلف في السجلات الفارغة.
 */
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class BackupPayloadSerializerTest {

    @Test
    fun testDeterministicIntegrityHash() {
        val settings = AppSettings(
            id = 1,
            currencySymbol = "ر.ي",
            schoolExpensesEnabled = true
        )
        val commitment = FixedCommitment(
            name = "إيجار الشقة",
            targetAmount = BigDecimal("150000.00"),
            currentProgress = BigDecimal("50000.00"),
            orderIndex = 0
        )
        val customer = HabayebCustomer(
            id = "c1",
            name = "علي محمد",
            phone = "777123456",
            notes = "",
            createdAt = 1000L,
            initialType = TransactionType.OWED_BY_THEM.value
        )

        val payload1 = BackupPayloadData(
            settings = settings,
            commitments = listOf(commitment),
            transactions = emptyList(),
            habayebCustomers = listOf(customer),
            habayebTransactions = emptyList()
        )

        val payload2 = BackupPayloadData(
            settings = settings,
            commitments = listOf(commitment),
            transactions = emptyList(),
            habayebCustomers = listOf(customer),
            habayebTransactions = emptyList()
        )

        val hash1 = BackupPayloadSerializer.calculateIntegrityHash(payload1)
        val hash2 = BackupPayloadSerializer.calculateIntegrityHash(payload2)

        assertNotNull(hash1)
        assertEquals(hash1, hash2)
    }

    @Test
    fun testExportAndParsePayloadStream() = runBlocking {
        val settings = AppSettings(
            id = 1,
            currencySymbol = "ر.ي"
        )
        val tx = TransactionDb(
            id = "tx100",
            timestamp = 1600000000000L,
            type = "EXPENSE",
            category = "طعام",
            amount = BigDecimal("12345.67"),
            description = "وجبة غداء"
        )

        val payload = BackupPayloadData(
            settings = settings,
            commitments = emptyList(),
            transactions = listOf(tx),
            habayebCustomers = emptyList(),
            habayebTransactions = emptyList()
        )

        val writer = StringWriter()
        BackupPayloadSerializer.exportBackupToWriter(payload, writer)
        val exportedJson = writer.toString()

        assertTrue(exportedJson.contains("Mizan Al-Dar"))
        assertTrue(exportedJson.contains("12345.67"))
        assertTrue(exportedJson.contains("وجبة غداء"))

        // استيراد السلسلة
        val (restoredSettings, restoredCommitments, restoredTransactions) = BackupPayloadSerializer.importBackupFromJson(exportedJson)

        assertNotNull(restoredSettings)
        assertEquals(1, restoredTransactions.size)
        assertEquals("tx100", restoredTransactions[0].id)
        assertEquals(BigDecimal("12345.67"), restoredTransactions[0].amount)
        assertEquals("وجبة غداء", restoredTransactions[0].description)
    }

    @Test
    fun testEmptyPayloadIntegrity() = runBlocking {
        val emptyPayload = BackupPayloadData(
            settings = AppSettings(id = 1),
            commitments = emptyList(),
            transactions = emptyList()
        )

        val hash = BackupPayloadSerializer.calculateIntegrityHash(emptyPayload)
        assertNotNull(hash)
        assertTrue(hash.isNotEmpty())

        val writer = StringWriter()
        BackupPayloadSerializer.exportBackupToWriter(emptyPayload, writer)
        val exported = writer.toString()

        val (_, restoredCommitments, restoredTransactions) = BackupPayloadSerializer.importBackupFromJson(exported)
        assertEquals(0, restoredTransactions.size)
        assertEquals(0, restoredCommitments.size)
    }
}


// --- ملاحظات وتوصيات المعمارية البرمجية ---
// 1) يُحافظ على استقلال الاختبار عن تفاصيل التنفيذ غير الضرورية قدر الإمكان.
// 2) يجب أن يبقى كل اختبار حامياً لعقد سلوكي قابل للملاحظة، لا لتفاصيل داخلية قابلة لإعادة الهيكلة.
// 3) عند إضافة حالات حدية جديدة، يُفضّل تغطية القيم الصفرية، السالبة، العشرية،
//    وفشل التحويل أو الترحيل بحسب طبيعة الوحدة التي يختبرها الملف.
// 4) أي تحسين مستقبلي يجب أن يتم في نسخة تطوير مستقلة، مع إبقاء هذا الملف
//    دون تعديل وظيفي أثناء مرحلة التوثيق الحالية.
