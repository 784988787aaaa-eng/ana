/**
 * =====================================================================
 * ملف: مدير مجلدات ومسارات النسخ الاحتياطي (BackupDirectoryManager.kt)
 * =====================================================================
 * 
 * [الغرض العام والتعليمي من الملف]:
 * يتولى هذا الملف إدارة مسارات التخزين والتقسيم الشهري التلقائي لملفات
 * النسخ الاحتياطي المشفرة ذات الامتداد الخاص بالميزان (`.mzd`).
 * 
 * [المسؤوليات المعمارية وقواعد نظام أندرويد]:
 * 1. الامتثال للتخزين المحدود (Scoped Storage Compliance):
 *    - استخدام `getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)` لحفظ الملفات في مساحات التطبيق
 *      الآمنة دون طلب أذونات التخزين الشاملة المعقدة في الإصدارات الحديثة من أندرويد.
 *    - التراجع الذكي (Fallback) إلى التخزين الداخلي المباشر `context.filesDir` عند تعذر الوصول للتخزين الخارجي.
 * 2. التنظيم الزمني والمجلدات الشهرية:
 *    - تقسيم النسخ الاحتياطية تلقائياً في مجلدات فرعية حسب السنة والشهر (`yyyy-MM`).
 * 3. الفحص الشجري العودي (Recursive Search):
 *    - البحث العودي في كافة المجلدات الفرعية لجمع كافة ملفات `.mzd` لعرضها في شاشات الاستعادة.
 */
package com.example.data.repository

// ---------------------------------------------------------------------
// استيراد حزم سياق أندرويد ومدير ملفات النسخ الموحد
// ---------------------------------------------------------------------
import android.content.Context
import com.example.data.backup.BackupConstants
import com.example.data.backup.BackupFileManager
import java.io.File

/**
 * [فئة مدير مجلدات النسخ الاحتياطي - BackupDirectoryManager]:
 * واجهة تفويض وتوافقية (Backward-compatible Facade) تفوض عمليات إدارة المسارات
 * والمجلدات الشهرية والبحث الشجري إلى [BackupFileManager] الموحد.
 */
class BackupDirectoryManager(private val context: Context) {

    private val fileManager = BackupFileManager(context)

    /**
     * [الكائن المرافق للثوابت والإعدادات]:
     */
    companion object {
        /** وسم السجلات التشخيصية */
        private const val TAG = "BackupDirManager"
        /** امتداد ملفات النسخ الاحتياطي المشفرة لتطبيق الميزان */
        const val MZD_EXTENSION = BackupConstants.BACKUP_FILE_EXTENSION
        /** نمط تسمية المجلدات الشهرية */
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
        if (!rootDir.exists()) return emptyList()
        return try {
            rootDir.walkTopDown()
                .filter { it.isFile && it.name.endsWith(MZD_EXTENSION, ignoreCase = true) }
                .toList()
        } catch (_: Exception) {
            emptyList()
        }
    }
}


