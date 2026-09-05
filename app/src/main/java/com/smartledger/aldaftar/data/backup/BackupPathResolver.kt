package com.smartledger.aldaftar.data.backup

import android.os.Environment
import android.util.Log
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object BackupPathResolver {

    private const val TAG = "BackupPathResolver"

    const val PUBLIC_BACKUP_FOLDER_NAME = "الدفتر الذكي"

    fun getPublicBackupRoot(): File {
        val publicDocs = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
        ?: File("/storage/emulated/0/Documents")
        val rootDir = File(publicDocs, PUBLIC_BACKUP_FOLDER_NAME)
        return rootDir
    }

    fun getCurrentMonthlyDirectory(now: Date = Date()): File {
        val sdf = SimpleDateFormat(BackupConstants.MONTH_DATE_PATTERN, Locale.US)
        val monthStr = sdf.format(now)
        return getMonthlyDirectory(monthStr)
    }

    fun getMonthlyDirectory(yearMonth: String): File {
        validateYearMonthString(yearMonth)
        val root = getPublicBackupRoot()
        return File(root, yearMonth)
    }

    fun ensureDirectory(directory: File): Result<File> {
        return try {
            val root = getPublicBackupRoot()
            return ensureDirectory(directory, root)
        } catch (e: Exception) {
            Log.e(TAG, "خطأ أثناء تجهيز المجلد: ${e.javaClass.simpleName}")
            Result.failure(e)
        }
    }

    fun ensureDirectory(directory: File, allowedRoot: File): Result<File> {
        return try {
            val rootCanonical = allowedRoot.canonicalFile
            val dirCanonical = directory.canonicalFile
            val rootPath = rootCanonical.path
            val dirPath = dirCanonical.path
            val insideRoot = dirPath == rootPath || dirPath.startsWith(rootPath + File.separator)
            if (!insideRoot) {
                return Result.failure(
                    SecurityException("محاولة استخدام مجلد خارج الجذر المسموح به: $dirPath")
                )
            }

            if (!directory.exists()) {
                val created = directory.mkdirs()
                if (!created && !directory.exists()) {
                    Log.e(TAG, "فشل إنشاء المجلد الفيزيائي للمسار: ${directory.path}")
                    return Result.failure(IOException("تعذر إنشاء مجلد النسخ الاحتياطي: ${directory.name}"))
                }
            }

            if (!directory.isDirectory) {
                return Result.failure(IOException("المسار المحدد ليس مجلداً: ${directory.path}"))
            }

            Result.success(directory)
        } catch (e: Exception) {
            Log.e(TAG, "خطأ أثناء تجهيز المجلد: ${e.javaClass.simpleName}")
            Result.failure(e)
        }
    }

    fun validateYearMonthString(yearMonth: String) {
        require(yearMonth.isNotBlank()) { "اسم الشهر لا يمكن أن يكون فارغاً" }
        require(!yearMonth.contains("..") && !yearMonth.contains("/") && !yearMonth.contains("\\")) {
            "اسم الشهر يحتوي على رموز غير مسموح بها تسبب ثغرة اختراق المسار: $yearMonth"
        }
    }

    fun validateFileName(fileName: String) {
        require(fileName.isNotBlank()) { "اسم الملف لا يمكن أن يكون فارغاً" }
        require(!fileName.contains("..") && !fileName.contains("/") && !fileName.contains("\\")) {
            "اسم الملف يحتوي على مسار غير مصرح به: $fileName"
        }
        require(fileName.endsWith(BackupConstants.BACKUP_FILE_EXTENSION, ignoreCase = true)) {
            "امتداد الملف يجب أن يكون ${BackupConstants.BACKUP_FILE_EXTENSION}"
        }
    }
    fun isWithin(candidate: File, root: File): Boolean {
        val rootPath = root.canonicalFile.toPath()
        val candidatePath = candidate.canonicalFile.toPath()
        return candidatePath == rootPath || candidatePath.startsWith(rootPath)
    }

}
