package com.smartledger.aldaftar.data.local.entities

import androidx.room.Entity
import androidx.room.Index

/** تثبيت عميل ضمن نطاق محدد. */
@Entity(tableName = "pinned_habayeb_customers", primaryKeys = ["scopeCategoryId", "customerId"], indices = [Index("customerId"), Index("scopeCategoryId")])
data class PinnedCustomer(
    val scopeCategoryId: Int,
    val customerId: String
)
