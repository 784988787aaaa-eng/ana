package com.smartledger.aldaftar.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index

@Entity(tableName = "custom_categories", indices = [Index(value = ["name", "tabType"], unique = true)])
data class CustomCategory(
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") val id: Int = 0,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "tabType") val tabType: String,
    @ColumnInfo(name = "iconEmoji") val iconEmoji: String,
    @ColumnInfo(name = "displayOrder") val displayOrder: Int = 0,
    @ColumnInfo(name = "isSystemClosed") val isSystemClosed: Boolean = false
)

