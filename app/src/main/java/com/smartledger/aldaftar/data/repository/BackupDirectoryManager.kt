/**
 * =====================================================================
 * ملف: مدير مجلدات ومسارات النسخ الاحتياطي (مدير مجلدات النسخ.المكوّن)
 * =====================================================================
 * 
 * [الغرض العام والتعليمي من الملف]:
 * يتولى هذا الملف إدارة مسارات التخزين والتقسيم الشهري التلقائي لملفات
 * النسخ الاحتياطي في المسار العام المعتمد:
 * /المكوّن/المكوّن/0/المستندات/الدفتر الذكي/[نمط السنة والشهر]/
 * 
 * [المسؤوليات المعمارية]:
 * 1. تفويض كامل إلى [مدير الملفات] و [محدد المسارات].
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
 * [فئة مدير مجلدات النسخ الاحتياطي - مدير مجلدات النسخ]:
 * واجهة تفويض وتوافقية تفوض عمليات إدارة المسارات
 * والمجلدات الشهرية والبحث الشجري إلى [مدير الملفات] الموحد.
 */
class BackupDirectoryManager(private val context: Context) {

    private val fileManager = BackupFileManager(context)

    companion object {
        private const val TAG = "BackupDirManager"
        const val MZD_EXTENSION = BackupConstants.BACKUP_FILE_EXTENSION
        const val MONTH_DATE_PATTERN = BackupConstants.MONTH_DATE_PATTERN
    }

    /**
     * [دالة جلب المجلد الرئيسي للنسخ الاحتياطي - جلب المجلد الأساسي]:
     * تفوض جلب المجلد الأساسي إلى [مدير الملفات].
     */
    fun getBaseBackupDirectory(): File {
        return fileManager.getBaseBackupDirectory()
    }

    /**
     * [دالة جلب المجلد الشهري للنسخ الحالي - جلب مجلد النسخ]:
     * تفوض جلب المجلد الشهري إلى [مدير الملفات].
     */
    fun getBackupDirectory(): File {
        return fileManager.getMonthlyBackupDirectory()
    }

    /**
     * [دالة البحث الشجري العودي عن ملفات النسخ - جلب ملفات النسخ تكرارياً]:
     * تفحص كافة المجلدات الفرعية داخل [المجلد الجذري] وتعيد قائمة بجميع ملفات النسخ المنتهية بـ .
     */
    fun getAllMzdFilesRecursively(rootDir: File): List<File> {
        return fileManager.getAllBackupFiles()
    }
}
