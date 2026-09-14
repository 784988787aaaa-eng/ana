package com.smartledger.aldaftar.data.repository

import androidx.room.withTransaction
import com.smartledger.aldaftar.data.local.AppDatabase
import com.smartledger.aldaftar.data.local.RecurringConfigDao
import com.smartledger.aldaftar.data.local.entities.HabayebTransaction
import com.smartledger.aldaftar.data.local.entities.RecurringConfigEntity
import com.smartledger.aldaftar.domain.model.FinancialPolicy
import com.smartledger.aldaftar.domain.model.RecurringConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

class RecurringRepository(private val database:AppDatabase, private val dao:RecurringConfigDao) {
 val configsFlow:Flow<List<RecurringConfig>> = dao.allFlow().map { it.map(::toModel) }
 suspend fun all():List<RecurringConfig> = dao.all().map(::toModel)
 suspend fun byOriginalTransaction(id:String):RecurringConfig? = dao.byOriginalTransaction(id)?.let(::toModel)
 suspend fun save(config:RecurringConfig) { validate(config); dao.save(toEntity(config)) }
 suspend fun delete(id:String)=dao.delete(id)
 suspend fun deleteForTransaction(id:String)=dao.deleteForTransaction(id)
 suspend fun deleteForCustomer(id:String)=dao.deleteForCustomer(id)
 suspend fun clear()=dao.clear()

 suspend fun executeDue(nowMillis:Long=System.currentTimeMillis()):Int = database.withTransaction {
  var count=0
  dao.all().filter { it.isActive }.forEach { entity ->
   val config=toModel(entity); val due=RecurringScheduleCalculator.dueOccurrences(config, nowMillis, maxOccurrences = 50)
   if(due.isNotEmpty()) {
    due.forEach { ts -> database.habayebDao().insertTransaction(
      HabayebTransaction(id=UUID.randomUUID().toString(), customerId=config.customerId, type=config.type,
       amount=FinancialPolicy.normalize(config.amount), timestamp=ts, description=config.description,
       linkedMainTxId=config.originalTxId, isForeign=config.isForeign, currencyCode=config.currencyCode,
       foreignAmount=FinancialPolicy.normalize(config.foreignAmount), exchangeRate=FinancialPolicy.normalize(config.exchangeRate),
       isRateCalculated=config.isRateCalculated, equivalentAmount=FinancialPolicy.normalize(config.equivalentAmount)) ) }
    dao.save(entity.copy(lastExecutedTimestamp=due.last()))
    count += due.size
   }
  }; count
 }

 private fun validate(c:RecurringConfig){ require(c.id.isNotBlank()&&c.originalTxId.isNotBlank()&&c.customerId.isNotBlank()); require(c.endDateMillis>=c.startDateMillis); require(c.timeHour in 0..23&&c.timeMinute in 0..59) }
 private fun toModel(e:RecurringConfigEntity)=RecurringConfig(e.id,e.originalTxId,e.customerId,e.customerName,e.amount,e.type,e.description,e.frequency,e.daysOfWeek,e.daysOfMonth,e.timeHour,e.timeMinute,e.startDateMillis,e.endDateMillis,e.lastExecutedTimestamp,e.isActive,e.isForeign,e.currencyCode,e.foreignAmount,e.exchangeRate,e.isRateCalculated,e.equivalentAmount)
 private fun toEntity(c:RecurringConfig)=RecurringConfigEntity(c.id,c.originalTxId,c.customerId,c.customerName,FinancialPolicy.normalize(c.amount),c.type,c.description,c.frequency,c.daysOfWeek.distinct(),c.daysOfMonth.distinct(),c.timeHour,c.timeMinute,c.startDateMillis,c.endDateMillis,c.lastExecutedTimestamp,c.isActive,c.isForeign,c.currencyCode,FinancialPolicy.normalize(c.foreignAmount),FinancialPolicy.normalize(c.exchangeRate),c.isRateCalculated,FinancialPolicy.normalize(c.equivalentAmount))
}
