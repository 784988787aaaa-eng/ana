package com.smartledger.aldaftar.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "business_profile")
data class BusinessProfile(
    @PrimaryKey val id: Int = 1,
    val name: String = "",
    val description: String = "",
    val logoPath: String = "",
    val phones: List<String> = emptyList()
)
