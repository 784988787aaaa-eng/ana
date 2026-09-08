package com.smartledger.aldaftar.domain

import com.smartledger.aldaftar.domain.model.FinancialPolicy
import java.math.BigDecimal
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FinancialPolicyTest {
    @Test fun `تطبيع القيمة يوحد المقياس`() {
        assertEquals(BigDecimal("12.3457"), FinancialPolicy.normalize(BigDecimal("12.34567")))
    }

    @Test fun `المقارنة العددية تتجاهل اختلاف المقياس`() {
        assertTrue(FinancialPolicy.numericallyEquals(BigDecimal("1.0"), BigDecimal("1.0000")))
    }
}
