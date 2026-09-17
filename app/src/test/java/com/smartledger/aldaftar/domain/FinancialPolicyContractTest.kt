package com.smartledger.aldaftar.domain

import com.smartledger.aldaftar.domain.model.FinancialPolicy
import com.smartledger.aldaftar.testsupport.MoneyAssertions
import java.math.BigDecimal
import org.junit.Test

class FinancialPolicyContractTest {
    @Test fun normalizationUsesFourDecimalPlacesAndHalfEven() {
        MoneyAssertions.exact("1.2346", FinancialPolicy.normalize(BigDecimal("1.23455")))
        MoneyAssertions.exact("1.2344", FinancialPolicy.normalize(BigDecimal("1.23445")))
        MoneyAssertions.exact("0.0000", FinancialPolicy.normalize(BigDecimal.ZERO))
    }

    @Test fun normalizationPreservesVeryLargeValuesWithoutScientificNotationSemantics() {
        val value = BigDecimal("999999999999999999999999.99995")
        MoneyAssertions.exact("1000000000000000000000000.0000", FinancialPolicy.normalize(value))
    }

    @Test fun rateNormalizationUsesHigherPrecisionThanMoney() {
        org.junit.Assert.assertEquals(12, FinancialPolicy.normalizeRate(BigDecimal("0.0071428571429")).scale())
        org.junit.Assert.assertEquals("0.007142857143", FinancialPolicy.normalizeRate(BigDecimal("0.0071428571429")).toPlainString())
    }

    @Test fun numericEqualityIgnoresScaleButNotValue() {
        org.junit.Assert.assertTrue(FinancialPolicy.numericallyEquals(BigDecimal("10"), BigDecimal("10.0000")))
        org.junit.Assert.assertFalse(FinancialPolicy.numericallyEquals(BigDecimal("10.0001"), BigDecimal("10.0000")))
    }
}
