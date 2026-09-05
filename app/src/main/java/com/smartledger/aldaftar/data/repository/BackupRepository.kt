package com.smartledger.aldaftar.data.repository

import android.content.Context
import com.smartledger.aldaftar.data.backup.BackupFileManager
import com.smartledger.aldaftar.data.backup.BackupOperationResult
import com.smartledger.aldaftar.data.backup.BackupService
import com.smartledger.aldaftar.data.local.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException

class BackupRepository(
    private val context: Context,
    private val database: AppDatabase,
    private val backupService: BackupService = BackupService(context, database),
    private val backupFileManager: BackupFileManager = BackupFileManager(context),
    private val restoreService: FinanceRestoreService = FinanceRestoreService(database, context)
) {

    fun getBaseBackupDirectory(): File = backupFileManager.getBaseBackupDirectory()

    fun getBackupDirectory(): File = backupFileManager.getMonthlyBackupDirectory()

    fun getAllLocalBackupFiles(): List<File> = backupFileManager.getAllBackupFiles()

    suspend fun createLocalBackup(customFileName: String? = null): BackupOperationResult =
    backupService.performLocalBackup(customFileName)

    suspend fun createSilentBackup(): BackupOperationResult =
    backupService.performSilentBackup()

    suspend fun getBackupJson(): String =
    backupService.generateBackupJson()

    suspend fun restoreFromJson(rawJson: String): FinanceRestoreResult =
    restoreService.executeMasterRestore(rawJson)

    suspend fun restoreFromFile(file: File): Result<FinanceRestoreResult> = withContext(Dispatchers.IO) {
        val readResult = backupFileManager.readBackupFile(file)
        if (readResult.isSuccess) {
            try {
                val content = readResult.getOrThrow()
                val restoreResult = restoreService.executeMasterRestore(content)
                Result.success(restoreResult)
            } catch (e: Exception) {
                Result.failure(e)
            }
        } else {
            val originalException = readResult.exceptionOrNull() ?: IOException("فشل قراءة ملف النسخة الاحتياطية: ${file.name}")
            Result.failure(originalException)
        }
    }

    suspend fun clearAllData(): Unit =
    restoreService.deleteAllData()
}
