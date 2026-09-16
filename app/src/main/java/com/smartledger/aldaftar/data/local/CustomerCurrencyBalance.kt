package com.smartledger.aldaftar.data.local

import androidx.room.ColumnInfo
import java.math.BigDecimal

data class CustomerCurrencyBalance(
    @ColumnInfo(name = "customerId") val customerId: String,
    @ColumnInfo(name = "currencyCode") val currencyCode: String,
    @ColumnInfo(name = "netAmount") val netAmount: BigDecimal,
    @ColumnInfo(name = "netEquivalentAmount") val netEquivalentAmount: BigDecimal,
    @ColumnInfo(name = "lastTimestamp") val lastTimestamp: Long,
    @ColumnInfo(name = "txCount") val txCount: Int
)
