package com.smartledger.aldaftar.domain.model
import java.math.BigDecimal

data class RecurringConfig(
 val id:String, val originalTxId:String, val customerId:String, val customerName:String,
 val amount:BigDecimal, val type:String, val description:String, val frequency:String,
 val daysOfWeek:List<Int>, val daysOfMonth:List<Int>, val timeHour:Int, val timeMinute:Int,
 val startDateMillis:Long, val endDateMillis:Long, val lastExecutedTimestamp:Long,
 val isActive:Boolean=true, val isForeign:Boolean=false, val currencyCode:String="DEFAULT",
 val foreignAmount:BigDecimal=BigDecimal.ZERO, val exchangeRate:BigDecimal=BigDecimal.ONE,
 val isRateCalculated:Boolean=false, val equivalentAmount:BigDecimal=BigDecimal.ZERO
)
