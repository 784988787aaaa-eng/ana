package com.smartledger.aldaftar.data.serialization

import com.smartledger.aldaftar.data.local.entities.AppSettings
import com.smartledger.aldaftar.data.local.entities.FixedCommitment
import com.smartledger.aldaftar.data.local.entities.HabayebCustomer
import com.smartledger.aldaftar.data.local.entities.TransactionDb
import com.smartledger.aldaftar.domain.model.TransactionType
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.StringWriter
import java.math.BigDecimal

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
    fun integrityHashCoversPreferencesAndCustomerMetadata() {
        val settings = AppSettings(id = 1, currencySymbol = "ر.ي")
        val customer = HabayebCustomer(
            id = "c1",
            name = "علي",
            phone = "777",
            notes = "ملاحظة",
            createdAt = 100L,
            initialType = TransactionType.OWED_BY_THEM.value
        )
        val base = BackupPayloadData(
            settings = settings,
            commitments = emptyList(),
            transactions = emptyList(),
            habayebCustomers = listOf(customer),
            categoryLinks = mapOf("c1" to "food"),
            pinnedCustomerIdsByCategory = mapOf("food" to setOf("c1")),
            categoryOrderList = "food,work",
            closedCustomName = "custom"
        )

        assertNotEquals(
            BackupPayloadSerializer.calculateIntegrityHash(base),
            BackupPayloadSerializer.calculateIntegrityHash(base.copy(categoryOrderList = "work,food"))
        )
        assertNotEquals(
            BackupPayloadSerializer.calculateIntegrityHash(base),
            BackupPayloadSerializer.calculateIntegrityHash(base.copy(closedCustomName = "other"))
        )
        assertNotEquals(
            BackupPayloadSerializer.calculateIntegrityHash(base),
            BackupPayloadSerializer.calculateIntegrityHash(base.copy(habayebCustomers = listOf(customer.copy(notes = "ملاحظة أخرى"))))
        )
    }

    @Test
    fun verifyIntegrityRejectsMalformedHash() {
        val payload = BackupPayloadData(AppSettings(id = 1), emptyList(), emptyList())
        assertTrue(!BackupPayloadSerializer.calculateIntegrityHash(payload).isBlank())
        assertTrue(!BackupIntegrityManager.verifyIntegrity(payload, "invalid"))
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
