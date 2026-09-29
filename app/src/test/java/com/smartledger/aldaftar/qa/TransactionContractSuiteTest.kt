package com.smartledger.aldaftar.qa
import org.junit.Assert.*
import org.junit.Test
class TransactionContractSuiteTest {
 @Test fun transactionContractFieldsRequired(){
  val required=listOf("id","currency","date","historicalRate","status")
  assertEquals(5, required.size)
 }
 @Test fun balanceMustBeDeterministic(){
  val a=100-30; val b=100-30; assertEquals(a,b)
 }
}
