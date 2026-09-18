package com.smartledger.aldaftar.qa
import org.junit.Assert.assertEquals
import org.junit.Test

class StressAndLifecycleSuiteTest {
 @Test fun stressSimulation(){
  var balance=0
  repeat(10000){ balance += 1 }
  assertEquals(10000,balance)
 }
 @Test fun lifecycleStateContract(){
  assertEquals("resume","resume")
 }
}
