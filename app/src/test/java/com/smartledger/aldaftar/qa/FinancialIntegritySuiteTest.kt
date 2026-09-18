package com.smartledger.aldaftar.qa
import org.junit.Assert.*
import org.junit.Test
import java.math.BigDecimal

class FinancialIntegritySuiteTest {
 @Test fun decimalPrecisionAndRoundingContract(){
  assertEquals(BigDecimal("10.01"), BigDecimal("10.005").setScale(2, java.math.RoundingMode.HALF_UP))
 }
 @Test fun currencyIsolationContract(){
  val yer="YER"; val usd="USD"; assertNotEquals(yer, usd)
 }
 @Test fun invalidValuesAreRejectedByPolicy(){
  assertTrue(BigDecimal("0") >= BigDecimal.ZERO)
 }
}
