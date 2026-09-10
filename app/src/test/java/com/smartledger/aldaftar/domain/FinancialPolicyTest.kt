package com.smartledger.aldaftar.domain

import com.smartledger.aldaftar.domain.model.FinancialPolicy
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal

class FinancialPolicyTest {
    @Test fun `تطبيع القيمة يوحد المقياس`() {
        assertEquals(BigDecimal("12.3457"), FinancialPolicy.normalize(BigDecimal("12.34567")))
    }

    @Test fun `المقارنة العددية تتجاهل اختلاف المقياس`() {
        assertTrue(FinancialPolicy.numericallyEquals(BigDecimal("1.0"), BigDecimal("1.0000")))
    }

    @Test fun `التقريب المالي يستخدم HALF_EVEN عند حد القرار`() {
        assertEquals(BigDecimal("12.3456"), FinancialPolicy.normalize(BigDecimal("12.34565")))
        assertEquals(BigDecimal("12.3458"), FinancialPolicy.normalize(BigDecimal("12.34575")))
        assertEquals(BigDecimal("-12.3456"), FinancialPolicy.normalize(BigDecimal("-12.34565")))
    }
}
