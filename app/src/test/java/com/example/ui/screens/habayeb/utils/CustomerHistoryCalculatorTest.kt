package com.example.ui.screens.habayeb.utils

import com.example.data.local.entities.HabayebTransaction
import com.example.domain.model.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal

/**
 * اختبارات الحالات الطرفية لحاسبة تاريخ وأرصدة عملاء الحبايب (CustomerHistoryCalculator)
 *
 * التوثيق المعماري:
 * يختبر هذا الملف السلوك الحسابي للأرصدة المتعددة العملات، وحساب التسلسل الزمني،
 * والتعامل مع القوائم الفارغة، والقيم الصفرية دون أي انهيار أو أخطاء تقريب.
 */
class CustomerHistoryCalculatorTest {

    @Test
    fun testEmptyTransactionList() {
        val result = CustomerHistoryCalculator.calculate(
            allCustomerTxs = emptyList(),
            currencySymbol = "ر.ي",
            exchangeRatesJson = null
        )

        assertEquals(0, BigDecimal.ZERO.compareTo(result.netDebt))
        assertTrue(result.runningBalances.isEmpty())
        assertTrue(result.txSequenceNumbers.isEmpty())
        assertEquals(1, result.currencyKeys.size)
        assertEquals("ر.ي", result.currencyKeys[0])
    }

    @Test
    fun testSingleCreditTransaction() {
        val tx = HabayebTransaction(
            id = "tx1",
            customerId = "cust1",
            type = TransactionType.OWED_BY_THEM.value, // دين له
            amount = BigDecimal("500.00"),
            timestamp = 1000L,
            description = "دين جديد",
            currencyCode = "ر.ي"
        )

        val result = CustomerHistoryCalculator.calculate(
            allCustomerTxs = listOf(tx),
            currencySymbol = "ر.ي",
            exchangeRatesJson = null
        )

        // له = رصيد موجب للمستخدم (عليه دين لنا)
        assertEquals(0, BigDecimal("500.00").compareTo(result.owedByThemBDMap["ر.ي"]))
        assertEquals(0, BigDecimal("500.00").compareTo(result.runningBalances["tx1"]))
        assertEquals(1, result.txSequenceNumbers["tx1"])
    }

    @Test
    fun testDebitAndCreditSequence() {
        val tx1 = HabayebTransaction(
            id = "tx1",
            customerId = "cust1",
            type = TransactionType.OWED_BY_THEM.value, // له 1000
            amount = BigDecimal("1000.00"),
            timestamp = 1000L,
            description = "دين 1",
            currencyCode = "ر.ي"
        )
        val tx2 = HabayebTransaction(
            id = "tx2",
            customerId = "cust1",
            type = TransactionType.PAYMENT_BY_THEM.value, // تسديد 400
            amount = BigDecimal("400.00"),
            timestamp = 2000L,
            description = "سداد 1",
            currencyCode = "ر.ي"
        )

        val result = CustomerHistoryCalculator.calculate(
            allCustomerTxs = listOf(tx2, tx1), // غير مرتبة زمنياً للتحقق من الترتيب
            currencySymbol = "ر.ي",
            exchangeRatesJson = null
        )

        // التحقق من أن tx1 أخذت التسلسل 1، و tx2 أخذت التسلسل 2
        assertEquals(1, result.txSequenceNumbers["tx1"])
        assertEquals(2, result.txSequenceNumbers["tx2"])

        // الرصيد التراكمي: بعد الأولى 1000، بعد الثانية 600
        assertEquals(0, BigDecimal("1000.00").compareTo(result.runningBalances["tx1"]))
        assertEquals(0, BigDecimal("600.00").compareTo(result.runningBalances["tx2"]))
    }
}
