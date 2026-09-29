package com.smartledger.aldaftar.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.smartledger.aldaftar.domain.model.TransactionType

@Entity(
    tableName = "habayeb_customers",
    indices = [
        Index(value = ["name"]),
        Index(value = ["phone"]),
        Index(value = ["createdAt"]),
        Index(value = ["initialType"]),
        Index(value = ["categoryId"])
    ]
)
data class HabayebCustomer(
    @PrimaryKey @ColumnInfo(name = "id") val id: String,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "phone") val phone: String,
    @ColumnInfo(name = "notes") val notes: String,
    @ColumnInfo(name = "createdAt") val createdAt: Long,
    @ColumnInfo(name = "initialType", defaultValue = "OWED_BY_THEM") val initialType: String = TransactionType.OWED_BY_THEM.value,
    /** هوية التصنيف المستقرة، أو null عند عدم الإسناد. */
    @ColumnInfo(name = "categoryId") val categoryId: Int? = null
)

