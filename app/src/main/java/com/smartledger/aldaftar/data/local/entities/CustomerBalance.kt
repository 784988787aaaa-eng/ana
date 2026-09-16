package com.smartledger.aldaftar.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.math.BigDecimal

@Entity(tableName = "customer_balances")
data class CustomerBalance(
    @PrimaryKey val id: String, // format: "customerId_currencyCode"
    val customerId: String,
    val currencyCode: String,
    val netAmount: BigDecimal,
    val netEquivalentAmount: BigDecimal,
    val lastTimestamp: Long,
    val txCount: Int
)
