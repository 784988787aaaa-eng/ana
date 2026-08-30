/**
 * =====================================================================
 * ملف: مدير تخزين وملفات النسخ الاحتياطي (BackupFileManager.kt)
 * =====================================================================
 * 
 * [الغرض العام والتعليمي من الملف]:
 * يمثل هذا الملف المسؤول الحصري عن العمليات الفيزيائية لنظام الملفات (File I/O Layer)
 * لإنشاء وقراءة والتحقق من وحذف ملفات النسخ الاحتياطي ذات الامتداد `.mzd`.
 * 
 * [المسؤوليات المعمارية والتقنية]:
 * 1. الكتابة الذرية الآمنة (Atomic Write): الكتابة أولاً في ملف مؤقت (`tmp_backup_*.tmp`) والتحقق من صحته قبل النقل والتسمية للملف النهائي لتفادي تلف البيانات حال انقطاع التطبيق فجأة.
 * 2. التحقق المسبق من سلامة الملفات (Validation): فحص الوجود والحجم وعدم الفراغ قبل القراءة أو الاستعادة.
 * 3. تنظيم الهيكل الهرمي للمجلدات: تقسيم النسخ الاحتياطية تلقائياً حسب الأشهر (`yyyy-MM`).
 * 4. إدارة التيارات بأمان (Resource Safety): ضمان إغلاق كافة التدفقات بمكتنف `use` لمنع تسريب الموارد.
 * 5. حماية الخصوصية: حظر كامل لتسجيل أي بيانات شخصية أو محتوى مالي في السجلات.
 */
package com.example.data.backup

// ---------------------------------------------------------------------
// استيراد حزم بيئة أندرويد والكوروتين وإدارة الملفات والاستثناءات
// ---------------------------------------------------------------------
import android.content.Context
import android.os.Environment
import android.util.Log
import com.example.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * [فئة مدير ملفات النسخ - BackupFileManager]:
 * توفر واجهات تعامل آمنة وسريعة مع وسائط التخزين المحلية.
 */
class BackupFileManager(private val context: Context) {

    /**
     * [الكائن المرافق - Companion Object]:
     * يحدد وسم التسجيل الموحد لعمليات مدير الملفات.
     */
    companion object {
        private const val TAG = "BackupFileManager"
    }

    /**
     * [دالة المسار الأساسي - getBaseBackupDirectory]:
     * ترجع المجلد الرئيسي المخصص لحفظ النسخ الاحتياطية في ذاكرة المستندات الخاصة للتطبيق.
     */
    fun getBaseBackupDirectory(): File {
        val folderName = context.getString(R.string.backup_folder_name)
        val safeDocsDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
            ?: context.filesDir
        val mainDir = File(safeDocsDir, folderName)
        try {
            if (!mainDir.exists()) {
                mainDir.mkdirs()
            }
            if (mainDir.exists()) {
                return mainDir
            }
        } catch (e: Exception) {
            Log.e(TAG, "فشل إنشاء مجلد النسخ الأساسي: ${e.javaClass.simpleName}")
        }
        return safeDocsDir
    }

    /**
     * [دالة المجلد الشهري - getMonthlyBackupDirectory]:
     * تنشئ وترجع مجلداً فرعياً بصيغة (yyyy-MM) لتصنيف النسخ حسب شهر الإنشاء.
     */
    fun getMonthlyBackupDirectory(): File {
        val baseDir = getBaseBackupDirectory()
        val sdf = SimpleDateFormat(BackupConstants.MONTH_DATE_PATTERN, Locale.US)
        val monthStr = sdf.format(Date())
        val targetDir = File(baseDir, monthStr)
        if (!targetDir.exists()) {
            targetDir.mkdirs()
        }
        return targetDir
    }

    /**
     * [دالة استعراض كافة ملفات النسخ - getAllBackupFiles]:
     * تبحث بشكل تراجعي (Recursive Walk) عن كافة ملفات `.mzd` في شجرة المجلدات وترتبها تنازلياً حسب تاريخ التعديل.
     */
    fun getAllBackupFiles(): List<File> {
        val baseDir = getBaseBackupDirectory()
        if (!baseDir.exists()) return emptyList()
        return try {
            baseDir.walkTopDown()
                .filter { it.isFile && it.name.endsWith(BackupConstants.BACKUP_FILE_EXTENSION, ignoreCase = true) }
                .sortedByDescending { it.lastModified() }
                .toList()
        } catch (e: Exception) {
            Log.e(TAG, "فشل استعراض ملفات النسخ: ${e.javaClass.simpleName}")
            emptyList()
        }
    }

    /**
     * [دالة التحقق من سلامة الملف - validateBackupFile]:
     * تفحص وجود الملف والتأكد من أنه ملف فعلي وليس مجلداً وأنه غير فارغ الحجم (أكبر من 0 بايت).
     */
    fun validateBackupFile(file: File): Result<File> {
        if (!file.exists()) {
            return Result.failure(IOException("ملف النسخة غير موجود: ${file.name}"))
        }
        if (!file.isFile) {
            return Result.failure(IOException("المسار المحدد ليس ملفاً: ${file.name}"))
        }
        if (file.length() == 0L) {
            return Result.failure(IOException("ملف النسخة فارغ (0 بايت): ${file.name}"))
        }
        return Result.success(file)
    }

    /**
     * [دالة الإنشاء الذري لملف النسخة - createBackupFile]:
     * تنفذ الكتابة الآمنة بتسلسل: إنشاء ملف مؤقت -> كتابة البيانات المشفرة -> تدقيق الصحة -> استبدال/إعادة تسمية ذري.
     */
    suspend fun createBackupFile(
        targetDirectory: File,
        targetFileName: String,
        content: String
    ): Result<File> = withContext(Dispatchers.IO) {
        var tempFile: File? = null
        try {
            if (content.isBlank()) {
                return@withContext Result.failure(IllegalArgumentException("محتوى النسخة الاحتياطية فارغ ولا يمكن كتابته"))
            }

            if (!targetDirectory.exists()) {
                targetDirectory.mkdirs()
            }

            tempFile = File.createTempFile(
                BackupConstants.BACKUP_TEMP_PREFIX,
                BackupConstants.BACKUP_TEMP_SUFFIX,
                targetDirectory
            )

            // كتابة المحتوى بأمان مع إغلاق التيار تلقائياً
            tempFile.bufferedWriter(Charsets.UTF_8).use { writer ->
                writer.write(content)
                writer.flush()
            }

            // التحقق من صحة واكتمال الملف المؤقت
            val tempValidation = validateBackupFile(tempFile)
            if (tempValidation.isFailure) {
                tempFile.delete()
                return@withContext Result.failure(
                    tempValidation.exceptionOrNull() ?: IOException("فشل التحقق من صحة الملف المؤقت قبل التسمية")
                )
            }

            val finalFile = File(targetDirectory, targetFileName)
            if (finalFile.exists()) {
                finalFile.delete()
            }

            val renameSuccess = tempFile.renameTo(finalFile)
            if (renameSuccess) {
                validateBackupFile(finalFile)
            } else {
                // بديل آمن (Fallback) في حال فشل renameTo المباشر
                try {
                    tempFile.copyTo(finalFile, overwrite = true)
                    tempFile.delete()
                    validateBackupFile(finalFile)
                } catch (copyEx: Exception) {
                    tempFile.delete()
                    Result.failure(copyEx)
                }
            }
        } catch (e: Throwable) {
            tempFile?.let { if (it.exists()) it.delete() }
            Log.e(TAG, "استثناء أثناء كتابة ملف النسخة الاحتياطية: ${e.javaClass.simpleName}")
            Result.failure(e)
        }
    }

    /**
     * [دالة توليد اسم الملف القياسي - generateStandardBackupFileName]:
     * تنشئ اسماً موحداً يدمج البادئة مع الطابع الزمني والامتداد القياسي.
     */
    fun generateStandardBackupFileName(prefix: String = BackupConstants.BACKUP_FILE_PREFIX): String {
        val sdfName = SimpleDateFormat(BackupConstants.BACKUP_DATE_FORMAT, Locale.US)
        val dateStr = sdfName.format(Date())
        return "$prefix$dateStr${BackupConstants.BACKUP_FILE_EXTENSION}"
    }

    /**
     * [دالة قراءة محتوى ملف النسخة - readBackupFile]:
     * تقرأ نصوص المحتوى المشفر بعد التأكد من سلامة وصلاحية الملف الفيزيائي.
     */
    suspend fun readBackupFile(file: File): Result<String> = withContext(Dispatchers.IO) {
        val validation = validateBackupFile(file)
        if (validation.isFailure) {
            return@withContext Result.failure(validation.exceptionOrNull() ?: IOException("الملف غير صالح للقراءة"))
        }
        try {
            val content = file.readText(Charsets.UTF_8)
            if (content.isBlank()) {
                Result.failure(IOException("محتوى الملف المقروء فارغ"))
            } else {
                Result.success(content)
            }
        } catch (e: Throwable) {
            Log.e(TAG, "خطأ في قراءة ملف النسخة الاحتياطية: ${e.javaClass.simpleName}")
            Result.failure(e)
        }
    }

    /**
     * [دالة حذف ملف النسخة - deleteBackupFile]:
     * تحذف الملف المحدد بأمان وتعالج حالات عدم الوجود دون التسبب بانهيارات.
     */
    suspend fun deleteBackupFile(file: File): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            if (!file.exists()) {
                Result.success(true)
            } else {
                val deleted = file.delete()
                if (deleted) {
                    Result.success(true)
                } else {
                    Result.failure(IOException("فشل حذف الملف: ${file.name}"))
                }
            }
        } catch (e: Throwable) {
            Log.e(TAG, "استثناء أثناء حذف ملف النسخة الاحتياطية: ${e.javaClass.simpleName}")
            Result.failure(e)
        }
    }

    // -----------------------------------------------------------------
    // دوال التوافقية مع الإصدارات السابقة (Backward Compatibility)
    // -----------------------------------------------------------------
    suspend fun writeBackupAtomically(
        targetDirectory: File,
        targetFileName: String,
        content: String
    ): Result<File> = createBackupFile(targetDirectory, targetFileName, content)

    suspend fun readBackupContent(file: File): Result<String> = readBackupFile(file)
}

