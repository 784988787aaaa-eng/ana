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
// استيراد حزم سياق أندرويد وبيئة التخزين وإدارة الملفات والتنسيق الزمني
// ---------------------------------------------------------------------
import android.content.Context
import android.os.Environment
import android.util.Log
import com.example.R
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * [فئة مدير مجلدات النسخ الاحتياطي - BackupDirectoryManager]:
 * مسؤولة عن إنشاء المسارات، تنظيم المجلدات الشهرية، والبحث عن ملفات النسخ المحلية.
 */
class BackupDirectoryManager(private val context: Context) {

    /**
     * [الكائن المرافق للثوابت والإعدادات]:
     */
    companion object {
        /** وسم السجلات التشخيصية */
        private const val TAG = "BackupDirManager"
        /** امتداد ملفات النسخ الاحتياطي المشفرة لتطبيق الميزان */
        private const val MZD_EXTENSION = ".mzd"
        /** نمط تسمية المجلدات الشهرية */
        private const val MONTH_DATE_PATTERN = "yyyy-MM"
    }

    /**
     * [دالة جلب المجلد الرئيسي للنسخ الاحتياطي - getBaseBackupDirectory]:
     * تنشئ المجلد العام للتطبيق داخل مجلد المستندات مع معالجة الاستثناءات والتراجع للتخزين الداخلي.
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
            Log.e(TAG, "Error creating backup directory", e)
        }
        return safeDocsDir
    }

    /**
     * [دالة جلب المجلد الشهري للنسخ الحالي - getBackupDirectory]:
     * تنشئ وتعيد مسار المجلد الشهري للنسخ الاحتياطي استناداً إلى تاريخ اليوم الحالي (`yyyy-MM`).
     */
    fun getBackupDirectory(): File {
        val baseDir = getBaseBackupDirectory()
        val sdf = SimpleDateFormat(MONTH_DATE_PATTERN, Locale.US)
        val monthStr = sdf.format(Date())
        val targetDir = File(baseDir, monthStr)
        if (!targetDir.exists()) {
            targetDir.mkdirs()
        }
        return targetDir
    }

    /**
     * [دالة البحث الشجري العودي عن ملفات النسخ - getAllMzdFilesRecursively]:
     * تفحص كافة المجلدات الفرعية داخل [rootDir] وتعيد قائمة بجميع ملفات النسخ المنتهية بـ `.mzd`.
     */
    fun getAllMzdFilesRecursively(rootDir: File): List<File> {
        if (!rootDir.exists()) return emptyList()
        return rootDir.walkTopDown().filter { it.isFile && it.name.endsWith(MZD_EXTENSION) }.toList()
    }
}

