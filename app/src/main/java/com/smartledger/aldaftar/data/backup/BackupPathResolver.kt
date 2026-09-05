/**
 * =====================================================================
 * ملف: محدد ومحلل مسارات النسخ الاحتياطي المركزي (محدد المسارات.المكوّن)
 * =====================================================================
 * 
 * [الغرض والمسؤولية المركزية]:
 * يمثل هذا الكائن المرجع المعماري الموحد والوحيد لتحديد مسارات تخزين
 * النسخ الاحتياطية المحلية في تطبيق "الدفتر الذكي".
 * 
 * [المسار المعتمد الرسمي الوحيد]:
 * /المكوّن/المكوّن/0/المستندات/الدفتر الذكي/[نمط السنة والشهر]/
 * 
 * [قواعد التحقق الصارم والأمان]:
 * 1. جذر ثابت وموحد: المستندات/الدفتر الذكي
 * 2. تقسيم شهري ديناميكي: بصيغة نمط السنة والشهر
 * 3. حظر كامل لثغرات تجاوز المسار (مثل ../)
 * 4. حظر توجيه النسخ إلى المجلدات الخاصة بالتطبيق كوجهة نهائية
 * 5. حظر تسجيل أي بيانات مالية في السجلات
 */
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

    /** اسم المجلد الجذري العام المعتمد رسمياً */
    const val PUBLIC_BACKUP_FOLDER_NAME = "الدفتر الذكي"

    /**
     * [جلب المجلد الجذري العام للنسخ الاحتياطي - جلب الجذر العام]:
     * يرجع المجلد المركزي: /المكوّن/المكوّن/0/المستندات/الدفتر الذكي
     */
    fun getPublicBackupRoot(): File {
        val publicDocs = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
            ?: File("/storage/emulated/0/Documents")
        val rootDir = File(publicDocs, PUBLIC_BACKUP_FOLDER_NAME)
        return rootDir
    }

    /**
     * [جلب مجلد الشهر الحالي - جلب المجلد الشهري الحالي]:
     * يرجع المجلد الشهري للنسخ بناءً على تاريخ اللحظة الحالية:
     * /المكوّن/المكوّن/0/المستندات/الدفتر الذكي/[نمط السنة والشهر]/
     */
    fun getCurrentMonthlyDirectory(now: Date = Date()): File {
        val sdf = SimpleDateFormat(BackupConstants.MONTH_DATE_PATTERN, Locale.US)
        val monthStr = sdf.format(now)
        return getMonthlyDirectory(monthStr)
    }

    /**
     * [جلب مجلد شهر محدد - جلب المجلد الشهري]:
     * يرجع مجلد الشهر بالصيغة الممررة (مثل "2026-08"):
     * /المكوّن/المكوّن/0/المستندات/الدفتر الذكي/[المكوّن]/
     */
    fun getMonthlyDirectory(yearMonth: String): File {
        validateYearMonthString(yearMonth)
        val root = getPublicBackupRoot()
        return File(root, yearMonth)
    }

    /**
     * [التأكد من وجود وصلاحية المجلد - تجهيز المجلد]:
     * ينشئ المجلد إذا لم يكن موجوداً، ويفحص أنه مجلد فعلي وقابل للكتابة.
     */
    fun ensureDirectory(directory: File): Result<File> {
        return try {
            val root = getPublicBackupRoot()
            return ensureDirectory(directory, root)
        } catch (e: Exception) {
            Log.e(TAG, "خطأ أثناء تجهيز المجلد: ${e.javaClass.simpleName}")
            Result.failure(e)
        }
    }

    /**
     * يتحقق من المجلد داخل جذر مسموح به ويمنع الخروج منه قبل أي إنشاء أو كتابة.
     */
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

    /**
     * [التحقق من صحة صيغة السنة والشهر - التحقق من السنة والشهر]:
     * يمنع أي محاولات للهروب من المجلد (تجاوز المسار) أو إدخال أسماء غير قانونية.
     */
    fun validateYearMonthString(yearMonth: String) {
        require(yearMonth.isNotBlank()) { "اسم الشهر لا يمكن أن يكون فارغاً" }
        require(!yearMonth.contains("..") && !yearMonth.contains("/") && !yearMonth.contains("\\")) {
            "اسم الشهر يحتوي على رموز غير مسموح بها تسبب ثغرة اختراق المسار: $yearMonth"
        }
    }

    /**
     * [التحقق من سلامة اسم الملف المستهدف - التحقق من اسم الملف]:
     * يمنع أي محاولات تمرير مسارات مطلقة أو رموز غير صالحة باسم الملف.
     */
    fun validateFileName(fileName: String) {
        require(fileName.isNotBlank()) { "اسم الملف لا يمكن أن يكون فارغاً" }
        require(!fileName.contains("..") && !fileName.contains("/") && !fileName.contains("\\")) {
            "اسم الملف يحتوي على مسار غير مصرح به: $fileName"
        }
        require(fileName.endsWith(BackupConstants.BACKUP_FILE_EXTENSION, ignoreCase = true)) {
            "امتداد الملف يجب أن يكون ${BackupConstants.BACKUP_FILE_EXTENSION}"
        }
    }
}
