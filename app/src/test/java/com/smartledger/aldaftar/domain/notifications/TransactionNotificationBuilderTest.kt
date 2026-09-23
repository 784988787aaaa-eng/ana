package com.smartledger.aldaftar.domain.notifications

import com.smartledger.aldaftar.data.local.entities.HabayebCustomer
import com.smartledger.aldaftar.data.local.entities.HabayebTransaction
import com.smartledger.aldaftar.domain.model.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal

class TransactionNotificationBuilderTest {

    private val sampleCustomer = HabayebCustomer(
        id = "c1",
        name = "محمد علي",
        phone = "777123456",
        notes = "",
        createdAt = System.currentTimeMillis(),
        initialType = TransactionType.OWED_BY_THEM.value
    )

    @Test
    fun testCase1_PaymentToThem_RemainsForThem() {
        val tx = HabayebTransaction(
            id = "tx1",
            customerId = "c1",
            type = TransactionType.PAYMENT_TO_THEM.value,
            amount = BigDecimal("10000"),
            timestamp = 1000L,
            description = ""
        )
        // net debt -15000 (meaning we owe them 15,000)
        val msg = TransactionNotificationBuilder.buildWhatsAppNotification(
            tx = tx,
            customer = sampleCustomer,
            netDebt = BigDecimal("-15000"),
            currencySymbol = "ر.ي"
        )

        val expected = """
            🟢 **سداد لكم**
            💰 10,000 ر.ي
            ◀ **المتبقي لكم:** 15,000 ر.ي
        """.trimIndent()

        assertEquals(expected, msg)
    }

    @Test
    fun testCase2_PaymentByThem_RemainsAgainstThem() {
        val tx = HabayebTransaction(
            id = "tx2",
            customerId = "c1",
            type = TransactionType.PAYMENT_BY_THEM.value,
            amount = BigDecimal("1000"),
            timestamp = 1000L,
            description = "دفعة نقدية"
        )
        // net debt 500 (meaning they owe us 500)
        val msg = TransactionNotificationBuilder.buildWhatsAppNotification(
            tx = tx,
            customer = sampleCustomer,
            netDebt = BigDecimal("500"),
            currencySymbol = "ر.ي"
        )

        val expected = """
            🟢 **استلام منكم**
            💰 1,000 ر.ي
            📝 دفعة نقدية
            ◀ **المتبقي عليكم:** 500 ر.ي
        """.trimIndent()

        assertEquals(expected, msg)
    }

    @Test
    fun testCase3_DebtAgainstThem_SAR() {
        val tx = HabayebTransaction(
            id = "tx3",
            customerId = "c1",
            type = TransactionType.OWED_BY_THEM.value,
            amount = BigDecimal("1500"),
            timestamp = 1000L,
            description = "قيمة المشتريات",
            currencyCode = "ر.س"
        )
        // net debt 4500 SAR
        val msg = TransactionNotificationBuilder.buildWhatsAppNotification(
            tx = tx,
            customer = sampleCustomer,
            netDebt = BigDecimal("4500"),
            currencySymbol = "ر.س"
        )

        val expected = """
            🔴 **دين عليكم**
            💰 1,500 ر.س
            📝 قيمة المشتريات
            ◀ **الإجمالي عليكم:** 4,500 ر.س
        """.trimIndent()

        assertEquals(expected, msg)
    }

    @Test
    fun testCase4_DebtForThem_USD() {
        val tx = HabayebTransaction(
            id = "tx4",
            customerId = "c1",
            type = TransactionType.OWED_TO_THEM.value,
            amount = BigDecimal("500"),
            timestamp = 1000L,
            description = "",
            currencyCode = "$"
        )
        // net debt -1200 USD (we owe them 1200)
        val msg = TransactionNotificationBuilder.buildWhatsAppNotification(
            tx = tx,
            customer = sampleCustomer,
            netDebt = BigDecimal("-1200"),
            currencySymbol = "$"
        )

        val expected = """
            🔴 **دين لكم**
            💰 $500
            ◀ **الإجمالي لكم:** $1,200
        """.trimIndent()

        assertEquals(expected, msg)
    }

    @Test
    fun testCase5_SettledToZero() {
        val tx = HabayebTransaction(
            id = "tx5",
            customerId = "c1",
            type = TransactionType.PAYMENT_BY_THEM.value,
            amount = BigDecimal("5000"),
            timestamp = 1000L,
            description = ""
        )
        val msg = TransactionNotificationBuilder.buildWhatsAppNotification(
            tx = tx,
            customer = sampleCustomer,
            netDebt = BigDecimal.ZERO,
            currencySymbol = "ر.ي"
        )

        val expected = """
            🟢 **استلام منكم**
            💰 5,000 ر.ي
            ◀ **الرصيد: 0 ر.ي**
        """.trimIndent()

        assertEquals(expected, msg)
    }

    @Test
    fun testCase6_CrossZero_FromForToAgainst() {
        // Was for them, now crosses to debt against them
        val tx = HabayebTransaction(
            id = "tx6",
            customerId = "c1",
            type = TransactionType.PAYMENT_TO_THEM.value,
            amount = BigDecimal("10000"),
            timestamp = 1000L,
            description = ""
        )
        // Resulting net debt is +5000 (against them)
        val msg = TransactionNotificationBuilder.buildWhatsAppNotification(
            tx = tx,
            customer = sampleCustomer,
            netDebt = BigDecimal("5000"),
            currencySymbol = "ر.ي"
        )

        val expected = """
            🟢 **سداد لكم**
            💰 10,000 ر.ي
            ◀ **المتبقي عليكم:** 5,000 ر.ي
        """.trimIndent()

        assertEquals(expected, msg)
    }

    @Test
    fun testCase7_ExchangeUSDToYER() {
        val tx = HabayebTransaction(
            id = "tx7",
            customerId = "c1",
            type = TransactionType.PAYMENT_TO_THEM.value,
            amount = BigDecimal("100"),
            foreignAmount = BigDecimal("100"),
            currencyCode = "$",
            baseCurrencyCode = "ر.ي",
            exchangeRate = BigDecimal("550"),
            isForeign = true,
            isRateCalculated = true,
            equivalentAmount = BigDecimal("55000"),
            timestamp = 1000L,
            description = ""
        )
        val msg = TransactionNotificationBuilder.buildWhatsAppNotification(
            tx = tx,
            customer = sampleCustomer,
            netDebt = BigDecimal("-55000"),
            currencySymbol = "ر.ي"
        )

        val expected = """
            🟢 **سداد لكم**
            💰 $100
            💱 **سعر الصرف:** 1$ = 550 ر.ي
            💰 **ما يعادل:** 55,000 ر.ي
            ◀ **المتبقي لكم:** 55,000 ر.ي
        """.trimIndent()

        assertEquals(expected, msg)
    }

    @Test
    fun testCase8_ExchangeSARToYER_WithDescription() {
        val tx = HabayebTransaction(
            id = "tx8",
            customerId = "c1",
            type = TransactionType.OWED_BY_THEM.value,
            amount = BigDecimal("1000"),
            foreignAmount = BigDecimal("1000"),
            currencyCode = "ر.س",
            baseCurrencyCode = "ر.ي",
            exchangeRate = BigDecimal("140"),
            isForeign = true,
            isRateCalculated = true,
            equivalentAmount = BigDecimal("140000"),
            timestamp = 1000L,
            description = "قيمة المشتريات"
        )
        val msg = TransactionNotificationBuilder.buildWhatsAppNotification(
            tx = tx,
            customer = sampleCustomer,
            netDebt = BigDecimal("140000"),
            currencySymbol = "ر.ي"
        )

        val expected = """
            🔴 **دين عليكم**
            💰 1,000 ر.س
            💱 **سعر الصرف:** 1 ر.س = 140 ر.ي
            💰 **ما يعادل:** 140,000 ر.ي
            📝 قيمة المشتريات
            ◀ **الإجمالي عليكم:** 140,000 ر.ي
        """.trimIndent()

        assertEquals(expected, msg)
    }

    @Test
    fun testCase9_SmsPlainFormat() {
        val tx = HabayebTransaction(
            id = "tx9",
            customerId = "c1",
            type = TransactionType.PAYMENT_TO_THEM.value,
            amount = BigDecimal("1000"),
            timestamp = 1000L,
            description = "قيمة المشتريات"
        )
        val msg = TransactionNotificationBuilder.buildSmsNotification(
            tx = tx,
            customer = sampleCustomer,
            netDebt = BigDecimal("-500"),
            currencySymbol = "ر.ي"
        )

        val expected = """
            🟢 سداد لكم
            💰 1,000 ر.ي
            📝 قيمة المشتريات
            ◀ المتبقي لكم: 500 ر.ي
        """.trimIndent()

        assertEquals(expected, msg)
        assertFalse(msg.contains("**"))
    }

    @Test
    fun testCase10_NoDescription_DoesNotContainNoteLine() {
        val tx = HabayebTransaction(
            id = "tx10",
            customerId = "c1",
            type = TransactionType.OWED_BY_THEM.value,
            amount = BigDecimal("1000"),
            timestamp = 1000L,
            description = "   "
        )
        val msg = TransactionNotificationBuilder.buildWhatsAppNotification(
            tx = tx,
            customer = sampleCustomer,
            netDebt = BigDecimal("1000"),
            currencySymbol = "ر.ي"
        )

        assertFalse(msg.contains("📝"))
        assertFalse(msg.contains("\n\n"))
    }
}
