package com.smartledger.aldaftar.data.backup

import android.content.Context
import android.os.Environment
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BackupFileManager(private val context: Context) {
    companion object {
        private const val TAG = "BackupFileManager"
    }

    fun getBaseBackupDirectory(): File {
        val externalDocuments = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
        val fallbackRoot = externalDocuments ?: context.filesDir
        val root = File(fallbackRoot, BackupPathResolver.PUBLIC_BACKUP_FOLDER_NAME)
        return BackupPathResolver.ensureDirectory(root, fallbackRoot).getOrDefault(root)
    }

    fun getMonthlyBackupDirectory(): File {
        val base = getBaseBackupDirectory()
        val month = SimpleDateFormat(BackupConstants.MONTH_DATE_PATTERN, Locale.US).format(Date())
        val directory = File(base, month)
        return BackupPathResolver.ensureDirectory(directory, base).getOrDefault(directory)
    }

    fun getAllBackupFiles(rootDir: File = getBaseBackupDirectory()): List<File> {
        val base = getBaseBackupDirectory()
        if (!rootDir.exists() || !rootDir.isDirectory) return emptyList()
        if (!BackupPathResolver.isWithin(rootDir, base)) return emptyList()

        return try {
            rootDir.walkTopDown()
            .onFail { file, exception -> Log.w(TAG, "تعذر استعراض ${file.name}: ${exception.javaClass.simpleName}") }
            .filter { it.isFile && it.name.endsWith(BackupConstants.BACKUP_FILE_EXTENSION, ignoreCase = true) }
            .sortedByDescending(File::lastModified)
            .toList()
        } catch (e: Exception) {
            Log.e(TAG, "فشل استعراض ملفات النسخ: ${e.javaClass.simpleName}")
            emptyList()
        }
    }

    fun validateBackupFile(file: File): Result<File> {
        return when {
            !file.exists() -> Result.failure(IOException("ملف النسخة غير موجود: ${file.name}"))
            !file.isFile -> Result.failure(IOException("المسار المحدد ليس ملفاً: ${file.name}"))
            file.length() <= 0L -> Result.failure(IOException("ملف النسخة فارغ: ${file.name}"))
            file.length() > BackupConstants.MAX_BACKUP_BYTES -> Result.failure(IOException("حجم ملف النسخة يتجاوز الحد المسموح: ${file.name}"))
            else -> Result.success(file)
        }
    }

    suspend fun createBackupFile(
        targetDirectory: File,
        targetFileName: String,
        content: String
    ): Result<File> = withContext(Dispatchers.IO) {
        var tempFile: File? = null
        try {
            if (content.isBlank()) return@withContext Result.failure(IllegalArgumentException("محتوى النسخة الاحتياطية فارغ"))
            BackupPathResolver.validateFileName(targetFileName)

            val base = getBaseBackupDirectory()
            val validTargetDir = BackupPathResolver.ensureDirectory(targetDirectory, base).getOrElse { return@withContext Result.failure(it) }
            val finalFile = File(validTargetDir, targetFileName)
            if (!BackupPathResolver.isWithin(finalFile, base)) {
                return@withContext Result.failure(SecurityException("مسار ملف النسخة خارج المجلد المسموح"))
            }

            tempFile = File.createTempFile(BackupConstants.BACKUP_TEMP_PREFIX, BackupConstants.BACKUP_TEMP_SUFFIX, validTargetDir)
            tempFile.outputStream().use { output ->
                output.write(content.toByteArray(Charsets.UTF_8))
                output.fd.sync()
            }

            validateBackupFile(tempFile).getOrElse {
                tempFile?.delete()
                return@withContext Result.failure(it)
            }

            if (tempFile.renameTo(finalFile)) {
                return@withContext validateBackupFile(finalFile)
            }

            val replacement = File.createTempFile(BackupConstants.BACKUP_TEMP_PREFIX, BackupConstants.BACKUP_TEMP_SUFFIX, validTargetDir)
            try {
                tempFile.copyTo(replacement, overwrite = true)
                replacement.inputStream().use { it.fd.sync() }
                if (!replacement.renameTo(finalFile)) {
                    return@withContext Result.failure(IOException("تعذر استبدال ملف النسخة بصورة آمنة"))
                }
                validateBackupFile(finalFile)
            } finally {
                tempFile?.delete()
                replacement.delete()
            }
        } catch (e: Exception) {
            tempFile?.delete()
            Log.e(TAG, "فشل إنشاء ملف النسخة: ${e.javaClass.simpleName}")
            Result.failure(e)
        }
    }

    fun generateStandardBackupFileName(prefix: String = BackupConstants.BACKUP_FILE_PREFIX): String {
        val timestamp = SimpleDateFormat(BackupConstants.BACKUP_DATE_FORMAT, Locale.US).format(Date())
        return "$prefix$timestamp${BackupConstants.BACKUP_FILE_EXTENSION}"
    }

    suspend fun readBackupFile(file: File): Result<String> = withContext(Dispatchers.IO) {
        validateBackupFile(file).fold(
            onSuccess = {
                try {
                    Result.success(it.readText(Charsets.UTF_8)).also { result ->
                        if (result.getOrThrow().isBlank()) throw IOException("محتوى الملف فارغ")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "فشل قراءة ملف النسخة: ${e.javaClass.simpleName}")
                    Result.failure(e)
                }
            },
            onFailure = { Result.failure(it) }
        )
    }

    suspend fun deleteBackupFile(file: File): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            if (!file.exists()) return@withContext Result.success(true)
            val base = getBaseBackupDirectory()
            if (!BackupPathResolver.isWithin(file, base)) {
                return@withContext Result.failure(SecurityException("لا يمكن حذف ملف خارج مجلد النسخ المسموح"))
            }
            Result.success(file.delete() || !file.exists()).also {
                if (!it.getOrDefault(false)) Log.e(TAG, "فشل حذف ملف النسخة: ${file.name}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "فشل حذف ملف النسخة: ${e.javaClass.simpleName}")
            Result.failure(e)
        }
    }

    suspend fun writeBackupAtomically(targetDirectory: File, targetFileName: String, content: String): Result<File> =
    createBackupFile(targetDirectory, targetFileName, content)

    suspend fun readBackupContent(file: File): Result<String> = readBackupFile(file)
}
