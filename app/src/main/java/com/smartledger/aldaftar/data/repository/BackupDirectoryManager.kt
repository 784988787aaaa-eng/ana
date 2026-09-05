package com.smartledger.aldaftar.data.repository

import android.content.Context
import com.smartledger.aldaftar.data.backup.BackupConstants
import com.smartledger.aldaftar.data.backup.BackupFileManager
import java.io.File

class BackupDirectoryManager(context: Context) {
    private val fileManager = BackupFileManager(context)

    companion object {
        const val MZD_EXTENSION = BackupConstants.BACKUP_FILE_EXTENSION
        const val MONTH_DATE_PATTERN = BackupConstants.MONTH_DATE_PATTERN
    }

    fun getBaseBackupDirectory(): File = fileManager.getBaseBackupDirectory()

    fun getBackupDirectory(): File = fileManager.getMonthlyBackupDirectory()

    fun getAllMzdFilesRecursively(rootDir: File): List<File> = fileManager.getAllBackupFiles(rootDir)
}
