package com.smartledger.aldaftar.domain.model

data class CloudBackupFile(
    val id: String,
    val name: String,
    val size: Long,
    val modifiedTime: Long,
    val month: String = ""
)
