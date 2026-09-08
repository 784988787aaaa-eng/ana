package com.smartledger.aldaftar.domain

import com.smartledger.aldaftar.domain.model.RecurringConfig
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal

class RecurringConfigTest {
 @Test fun keepsRecurringBusinessDataWithoutAndroidDependency(){
  val config=RecurringConfig("1","tx","customer","عميل",BigDecimal("12.50"),"OWED_BY_THEM","وصف","WEEKLY",listOf(2,4),emptyList(),9,30,1,2,0)
  assertEquals(listOf(2,4),config.daysOfWeek); assertTrue(config.customerId.isNotBlank())
 }
 @Test fun listValuesAreIndependentFromStorageRepresentation(){
  val values=listOf(1,15,31)
  assertEquals(setOf(1,15,31),values.toSet())
 }
}
