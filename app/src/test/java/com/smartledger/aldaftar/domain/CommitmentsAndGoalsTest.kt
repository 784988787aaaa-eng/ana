package com.smartledger.aldaftar.domain

import com.smartledger.aldaftar.data.local.entities.FixedCommitment
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal
import java.math.RoundingMode

/**
 * اختبارات منطق الالتزامات والأهداف المالية
 */
class CommitmentsAndGoalsTest {

    @Test
    fun testCommitmentProgressAndRemainingCalculation() {
        val commitment = FixedCommitment(
            name = "إيجار المحل",
            targetAmount = BigDecimal("50000.00"),
            currentProgress = BigDecimal("20000.00"),
            orderIndex = 0
        )

        val remaining = commitment.targetAmount.subtract(commitment.currentProgress)
        assertEquals(0, BigDecimal("30000.00").compareTo(remaining))

        val progressFraction = commitment.currentProgress.divide(commitment.targetAmount, 2, RoundingMode.HALF_EVEN)
        assertEquals(0, BigDecimal("0.40").compareTo(progressFraction))
    }

    @Test
    fun testCommitmentCompletion() {
        val commitment = FixedCommitment(
            name = "فاتورة الكهرباء",
            targetAmount = BigDecimal("15000.00"),
            currentProgress = BigDecimal("15000.00"),
            orderIndex = 1
        )

        val remaining = commitment.targetAmount.subtract(commitment.currentProgress)
        assertEquals(0, BigDecimal.ZERO.compareTo(remaining))
        assertTrue(commitment.currentProgress >= commitment.targetAmount)
    }

    @Test
    fun testCommitmentReordering() {
        val c1 = FixedCommitment("التزام 1", BigDecimal("100"), BigDecimal.ZERO, 0)
        val c2 = FixedCommitment("التزام 2", BigDecimal("200"), BigDecimal.ZERO, 1)
        val c3 = FixedCommitment("التزام 3", BigDecimal("300"), BigDecimal.ZERO, 2)

        val list = mutableListOf(c1, c2, c3)
        // نقل العنصر 3 إلى الموقع الأول
        val item = list.removeAt(2)
        list.add(0, item)

        val reordered = list.mapIndexed { index, fc -> fc.copy(orderIndex = index) }
        assertEquals("التزام 3", reordered[0].name)
        assertEquals(0, reordered[0].orderIndex)
        assertEquals("التزام 1", reordered[1].name)
        assertEquals(1, reordered[1].orderIndex)
    }
}
