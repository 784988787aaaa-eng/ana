package com.smartledger.aldaftar.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.math.BigDecimal

@Entity(tableName = "recurring_configs", indices = [Index(value=["originalTxId"], unique=true), Index(value=["customerId"]), Index(value=["isActive"])])
data class RecurringConfigEntity(
    @PrimaryKey val id: String,
    val originalTxId: String,
    val customerId: String,
    val customerName: String,
    val amount: BigDecimal,
    val type: String,
    val description: String,
    val frequency: String,
    val daysOfWeek: List<Int>,
    val daysOfMonth: List<Int>,
    val timeHour: Int,
    val timeMinute: Int,
    val startDateMillis: Long,
    val endDateMillis: Long,
    val lastExecutedTimestamp: Long,
    val isActive: Boolean = true,
    val isForeign: Boolean = false,
    val currencyCode: String = "DEFAULT",
    val foreignAmount: BigDecimal = BigDecimal.ZERO,
    val exchangeRate: BigDecimal = BigDecimal.ONE,
    val isRateCalculated: Boolean = false,
    val equivalentAmount: BigDecimal = BigDecimal.ZERO
)
