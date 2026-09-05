/**
 * =====================================================================
 * ملف: مدير مجلدات ومسارات النسخ الاحتياطي (BackupDirectoryManager.kt)
 * =====================================================================
 * 
 * [الغرض العام والتعليمي من الملف]:
 * يتولى هذا الملف إدارة مسارات التخزين والتقسيم الشهري التلقائي لملفات
 * النسخ الاحتياطي في المسار العام المعتمد:
 * /storage/emulated/0/Documents/الدفتر الذكي/[yyyy-MM]/
 * 
 * [المسؤوليات المعمارية]:
 * 1. تفويض كامل إلى [BackupFileManager] و [BackupPathResolver].
 * 2. الحفاظ على التوافقية العكسية مع المستودع المالي المركزي.
 */
package com.smartledger.aldaftar.data.repository

// ---------------------------------------------------------------------
// استيراد حزم سياق أندرويد ومدير ملفات النسخ الموحد
// ---------------------------------------------------------------------
import android.content.Context
import com.smartledger.aldaftar.data.backup.BackupConstants
import com.smartledger.aldaftar.data.backup.BackupFileManager
import java.io.File

/**
 * [فئة مدير مجلدات النسخ الاحتياطي - BackupDirectoryManager]:
 * واجهة تفويض وتوافقية تفوض عمليات إدارة المسارات
 * والمجلدات الشهرية والبحث الشجري إلى [BackupFileManager] الموحد.
 */
class BackupDirectoryManager(private val context: Context) {

    private val fileManager = BackupFileManager(context)

    companion object {
        private const val TAG = "BackupDirManager"
        const val MZD_EXTENSION = BackupConstants.BACKUP_FILE_EXTENSION
        const val MONTH_DATE_PATTERN = BackupConstants.MONTH_DATE_PATTERN
    }

    /**
     * [دالة جلب المجلد الرئيسي للنسخ الاحتياطي - getBaseBackupDirectory]:
     * تفوض جلب المجلد الأساسي إلى [BackupFileManager].
     */
    fun getBaseBackupDirectory(): File {
        return fileManager.getBaseBackupDirectory()
    }

    /**
     * [دالة جلب المجلد الشهري للنسخ الحالي - getBackupDirectory]:
     * تفوض جلب المجلد الشهري إلى [BackupFileManager].
     */
    fun getBackupDirectory(): File {
        return fileManager.getMonthlyBackupDirectory()
    }

    /**
     * [دالة البحث الشجري العودي عن ملفات النسخ - getAllMzdFilesRecursively]:
     * تفحص كافة المجلدات الفرعية داخل [rootDir] وتعيد قائمة بجميع ملفات النسخ المنتهية بـ `.mzd`.
     */
    fun getAllMzdFilesRecursively(rootDir: File): List<File> {
        return fileManager.getAllBackupFiles()
    }
}
