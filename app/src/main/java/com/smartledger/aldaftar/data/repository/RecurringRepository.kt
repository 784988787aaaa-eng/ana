package com.smartledger.aldaftar.data.repository

import androidx.room.withTransaction
import com.smartledger.aldaftar.data.local.AppDatabase
import com.smartledger.aldaftar.data.local.RecurringConfigDao
import com.smartledger.aldaftar.data.local.entities.HabayebTransaction
import com.smartledger.aldaftar.data.local.entities.RecurringConfigEntity
import com.smartledger.aldaftar.domain.model.FinancialPolicy
import com.smartledger.aldaftar.domain.model.RecurringConfig
import com.smartledger.aldaftar.data.license.LicenseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

class RecurringRepository(private val database:AppDatabase, private val dao:RecurringConfigDao, private val licenseRepository: LicenseRepository? = null) {
 val configsFlow:Flow<List<RecurringConfig>> = dao.allFlow().map { it.map(::toModel) }
 suspend fun all():List<RecurringConfig> = dao.all().map(::toModel)
 suspend fun byOriginalTransaction(id:String):RecurringConfig? = dao.byOriginalTransaction(id)?.let(::toModel)
 suspend fun save(config:RecurringConfig) { validate(config); dao.save(toEntity(config)) }
 suspend fun delete(id:String)=dao.delete(id)
 suspend fun deleteForTransaction(id:String)=dao.deleteForTransaction(id)
 suspend fun deleteForCustomer(id:String)=dao.deleteForCustomer(id)
 suspend fun clear()=dao.clear()

 suspend fun executeDue(nowMillis:Long=System.currentTimeMillis()):Int = database.withTransaction {
  data class Pending(val entity: RecurringConfigEntity, val config: RecurringConfig, val timestamps: List<Long>)
  val pending = mutableListOf<Pending>()

  dao.all().filter { it.isActive }.forEach { entity ->
    val config=toModel(entity)
    val due=RecurringScheduleCalculator.dueOccurrences(config, nowMillis, maxOccurrences = 50)
      .filterNot { ts -> dao.occurrenceAlreadyGenerated(config.originalTxId, ts) }
    if (due.isNotEmpty()) pending += Pending(entity, config, due)
  }

  val total = pending.sumOf { it.timestamps.size }
  if (total == 0) return@withTransaction 0

  val authorized = licenseRepository?.runAuthorizedCreation(
    currentUsed = { currentOperationsCount() },
    slots = total
  ) {
    pending.forEach { item ->
      item.timestamps.forEach { ts ->
        database.habayebDao().insertTransaction(
          HabayebTransaction(id=UUID.randomUUID().toString(), customerId=item.config.customerId, type=item.config.type,
            amount=FinancialPolicy.normalize(item.config.amount), timestamp=ts, description=item.config.description,
            linkedMainTxId=item.config.originalTxId, isForeign=item.config.isForeign, currencyCode=item.config.currencyCode,
            foreignAmount=FinancialPolicy.normalize(item.config.foreignAmount), exchangeRate=FinancialPolicy.normalizeRate(item.config.exchangeRate),
            isRateCalculated=item.config.isRateCalculated, equivalentAmount=FinancialPolicy.normalize(item.config.equivalentAmount),
            baseCurrencyCode=item.config.baseCurrencyCode, snapshotVersion=item.config.snapshotVersion, rateContext=item.config.rateContext)
        )
      }
      dao.save(item.entity.copy(lastExecutedTimestamp=item.timestamps.maxOrNull() ?: item.entity.lastExecutedTimestamp))
    }
    total
  }
  if (licenseRepository == null) {
    pending.forEach { item ->
      item.timestamps.forEach { ts ->
        database.habayebDao().insertTransaction(
          HabayebTransaction(id=UUID.randomUUID().toString(), customerId=item.config.customerId, type=item.config.type,
            amount=FinancialPolicy.normalize(item.config.amount), timestamp=ts, description=item.config.description,
            linkedMainTxId=item.config.originalTxId, isForeign=item.config.isForeign, currencyCode=item.config.currencyCode,
            foreignAmount=FinancialPolicy.normalize(item.config.foreignAmount), exchangeRate=FinancialPolicy.normalizeRate(item.config.exchangeRate),
            isRateCalculated=item.config.isRateCalculated, equivalentAmount=FinancialPolicy.normalize(item.config.equivalentAmount),
            baseCurrencyCode=item.config.baseCurrencyCode, snapshotVersion=item.config.snapshotVersion, rateContext=item.config.rateContext)
        )
      }
      dao.save(item.entity.copy(lastExecutedTimestamp=item.timestamps.maxOrNull() ?: item.entity.lastExecutedTimestamp))
    }
    total
  } else authorized ?: 0
 }

 private suspend fun currentOperationsCount(): Int {
  val daoCount = database.habayebDao().getBaseOperationsCountDirect()
  val trashItems = database.trashDao().getAllDeletedItemsDirect()
  var count = daoCount
  trashItems.forEach { item ->
    when (item.originalTableName) {
      "habayeb_customers", "habayeb_transactions" -> count++
      "habayeb_bundle" -> {
        val json = runCatching { org.json.JSONObject(item.jsonData) }.getOrNull()
        count += 1 + (json?.optInt("totalTransactions", json.optJSONArray("transactions")?.length() ?: 0) ?: 0)
      }
    }
  }
  return count
 }

 private fun validate(c:RecurringConfig){ require(c.id.isNotBlank()&&c.originalTxId.isNotBlank()&&c.customerId.isNotBlank()); require(c.endDateMillis>=c.startDateMillis); require(c.timeHour in 0..23&&c.timeMinute in 0..59) }
 private fun toModel(e:RecurringConfigEntity)=RecurringConfig(e.id,e.originalTxId,e.customerId,e.customerName,e.amount,e.type,e.description,e.frequency,e.daysOfWeek,e.daysOfMonth,e.timeHour,e.timeMinute,e.startDateMillis,e.endDateMillis,e.lastExecutedTimestamp,e.isActive,e.isForeign,e.currencyCode,e.foreignAmount,e.exchangeRate,e.isRateCalculated,e.equivalentAmount,e.baseCurrencyCode,e.snapshotVersion,e.rateContext)
 private fun toEntity(c:RecurringConfig)=RecurringConfigEntity(c.id,c.originalTxId,c.customerId,c.customerName,FinancialPolicy.normalize(c.amount),c.type,c.description,c.frequency,c.daysOfWeek.distinct(),c.daysOfMonth.distinct(),c.timeHour,c.timeMinute,c.startDateMillis,c.endDateMillis,c.lastExecutedTimestamp,c.isActive,c.isForeign,c.currencyCode,FinancialPolicy.normalize(c.foreignAmount),FinancialPolicy.normalizeRate(c.exchangeRate),c.isRateCalculated,FinancialPolicy.normalize(c.equivalentAmount),c.baseCurrencyCode,c.snapshotVersion,c.rateContext)
}
