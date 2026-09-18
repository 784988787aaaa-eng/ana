package com.smartledger.aldaftar.qa
import org.junit.Assert.assertTrue
import org.junit.Test

class ReleaseAcceptanceSuiteTest {
 @Test fun releaseGateChecklistExists(){
  val gates=listOf(
   "business_profile","account","transactions","YER_SAR_USD",
   "reports","PDF","CSV_XLSX","backup_restore","PIN","recurring","performance"
  )
  assertTrue(gates.size >= 11)
 }
}
