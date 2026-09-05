package com.smartledger.aldaftar.data.backup

import android.content.Context
import android.util.Log
import com.smartledger.aldaftar.data.local.AppDatabase
import com.smartledger.aldaftar.data.local.entities.AppSettings
import com.smartledger.aldaftar.data.serialization.BackupExtraDataProvider
import com.smartledger.aldaftar.data.serialization.BackupPayloadData
import com.smartledger.aldaftar.data.serialization.BackupPayloadSerializer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File

sealed class BackupOperationResult {
    data class Success(val file: File, val timestamp: Long) : BackupOperationResult()
    data class Failure(val userMessage: String, val cause: Throwable? = null) : BackupOperationResult()
}

sealed class BackupExecutionState {
    object Idle : BackupExecutionState()
    object Running : BackupExecutionState()
    data class Success(val file: File, val timestamp: Long) : BackupExecutionState()
    data class Failed(val reason: String) : BackupExecutionState()
}

class BackupService(
    private val context: Context,
    private val database: AppDatabase,
    private val fileManager: BackupFileManager = BackupFileManager(context)
) {

    companion object {
        private const val TAG = "BackupService"
    }

    private val backupMutex = Mutex()

    suspend fun buildBackupPayload(): BackupPayloadData = withContext(Dispatchers.IO) {
        val settings = database.settingsDao().getSettingsDirect() ?: AppSettings()
        val commitments = database.commitmentDao().getAllCommitmentsFlow().first()
        val transactions = database.transactionDao().getAllTransactionsFlow().first()
        val customers = database.habayebDao().getAllCustomersDirect()
        val habayebTransactions = database.habayebDao().getAllTransactionsDirect()
        val deletedItems = database.trashDao().getAllDeletedItemsDirect()
        val extraData = BackupExtraDataProvider.fetchExtraBackupData(context, customers)

        BackupPayloadData(
            settings = settings,
            commitments = commitments,
            transactions = transactions,
            habayebCustomers = customers,
            habayebTransactions = habayebTransactions,
            deletedItems = deletedItems,
            customCategories = extraData.customCategories,
            categoryLinks = extraData.categoryLinks,
            pinnedCustomerIdsByCategory = extraData.pinnedMap,
            categoryOrderList = extraData.categoryOrderList,
            closedCustomName = extraData.closedCustomName
        )
    }

    suspend fun generateBackupJson(): String = withContext(Dispatchers.IO) {
        val payload = buildBackupPayload()
        BackupPayloadSerializer.exportBackupToJson(payload)
    }

    suspend fun performLocalBackup(
        customFileName: String? = null,
        targetDir: File? = null
    ): BackupOperationResult = backupMutex.withLock {
        withContext(Dispatchers.IO + NonCancellable) {
            try {

                val jsonString = generateBackupJson()
                if (jsonString.isBlank()) {
                    return@withContext BackupOperationResult.Failure(
                        userMessage = "فشل إنشاء حزمة النسخ الاحتياطي: البيانات فارغة",
                        cause = IllegalStateException("حزمة النسخ الاحتياطية فارغة")
                    )
                }

                val directory = targetDir ?: fileManager.getMonthlyBackupDirectory()
                val fileName = customFileName ?: fileManager.generateStandardBackupFileName()

                val writeResult = fileManager.createBackupFile(directory, fileName, jsonString)
                if (writeResult.isSuccess) {
                    val file = writeResult.getOrThrow()

                    val validationResult = fileManager.validateBackupFile(file)
                    if (validationResult.isFailure) {
                        return@withContext BackupOperationResult.Failure(
                            userMessage = "فشل التحقق من سلامة ملف النسخة بعد الإنشاء",
                            cause = validationResult.exceptionOrNull()
                        )
                    }

                    val now = System.currentTimeMillis()

                    val prefs = context.getSharedPreferences(BackupConstants.PREFS_BACKUP, Context.MODE_PRIVATE)
                    prefs.edit().putLong(BackupConstants.KEY_LAST_SUCCESSFUL_BACKUP, now).apply()

                    Log.d(TAG, "اكتملت دورة النسخ الاحتياطي المحلي بنجاح في المسار العام.")
                    BackupOperationResult.Success(file, now)
                } else {
                    val err = writeResult.exceptionOrNull()
                    Log.e(TAG, "فشل إنشاء ملف النسخة الاحتياطية: ${err?.javaClass?.simpleName}")
                    BackupOperationResult.Failure(
                        userMessage = "فشل إنشاء ملف النسخة الاحتياطية المحلية",
                        cause = err
                    )
                }
            } catch (e: kotlinx.coroutines.CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "استثناء أثناء دورة النسخ الاحتياطي: ${e.javaClass.simpleName}")
                BackupOperationResult.Failure(
                    userMessage = "حدث خطأ أثناء النسخ الاحتياطي: ${e.localizedMessage ?: ""}",
                    cause = e
                )
            }
        }
    }

    suspend fun performSilentBackup(): BackupOperationResult = withContext(Dispatchers.IO) {
        performLocalBackup(
            customFileName = BackupConstants.BACKUP_SILENT_FILE_NAME,
            targetDir = fileManager.getMonthlyBackupDirectory()
        )
    }
}
