/**
 * =====================================================================
 * ملف: محدد ومحلل مسارات النسخ الاحتياطي المركزي (BackupPathResolver.kt)
 * =====================================================================
 * 
 * [الغرض والمسؤولية المركزية]:
 * يمثل هذا الكائن المرجع المعماري الموحد والوحيد لتحديد مسارات تخزين
 * النسخ الاحتياطية المحلية في تطبيق "الدفتر الذكي".
 * 
 * [المسار المعتمد الرسمي الوحيد]:
 * /storage/emulated/0/Documents/الدفتر الذكي/[yyyy-MM]/
 * 
 * [قواعد التحقق الصارم والأمان]:
 * 1. جذر ثابت وموحد: Documents/الدفتر الذكي
 * 2. تقسيم شهري ديناميكي: بصيغة yyyy-MM
 * 3. حظر كامل لثغرات Path Traversal (مثل ../)
 * 4. حظر توجيه النسخ إلى المجلدات الخاصة بالتطبيق كوجهة نهائية
 * 5. حظر تسجيل أي بيانات مالية في السجلات
 */
package com.example.data.backup

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
    // [توثيق المتغير/الخاصية: PUBLIC_BACKUP_FOLDER_NAME]: متغير/خاصية تحمل قيمة تشغيلية ضمن هذا النطاق، ويُحدد معناها من سياق العملية التي تستخدمها.
    const val PUBLIC_BACKUP_FOLDER_NAME = "الدفتر الذكي"

    /**
     * [جلب المجلد الجذري العام للنسخ الاحتياطي - getPublicBackupRoot]:
     * يرجع المجلد المركزي: /storage/emulated/0/Documents/الدفتر الذكي
     */
    fun getPublicBackupRoot(): File {
        // [توثيق المتغير/الخاصية: publicDocs]: متغير/خاصية تحمل قيمة تشغيلية ضمن هذا النطاق، ويُحدد معناها من سياق العملية التي تستخدمها.
        val publicDocs = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
            ?: File("/storage/emulated/0/Documents")
        // [توثيق المتغير/الخاصية: rootDir]: متغير/خاصية تحمل قيمة تشغيلية ضمن هذا النطاق، ويُحدد معناها من سياق العملية التي تستخدمها.
        val rootDir = File(publicDocs, PUBLIC_BACKUP_FOLDER_NAME)
        return rootDir
    }

    /**
     * [جلب مجلد الشهر الحالي - getCurrentMonthlyDirectory]:
     * يرجع المجلد الشهري للنسخ بناءً على تاريخ اللحظة الحالية:
     * /storage/emulated/0/Documents/الدفتر الذكي/[yyyy-MM]/
     */
    fun getCurrentMonthlyDirectory(now: Date = Date()): File {
        // [توثيق المتغير/الخاصية: sdf]: متغير/خاصية تحمل قيمة تشغيلية ضمن هذا النطاق، ويُحدد معناها من سياق العملية التي تستخدمها.
        val sdf = SimpleDateFormat(BackupConstants.MONTH_DATE_PATTERN, Locale.US)
        // [توثيق المتغير/الخاصية: monthStr]: متغير/خاصية تحمل قيمة تشغيلية ضمن هذا النطاق، ويُحدد معناها من سياق العملية التي تستخدمها.
        val monthStr = sdf.format(now)
        return getMonthlyDirectory(monthStr)
    }

    /**
     * [جلب مجلد شهر محدد - getMonthlyDirectory]:
     * يرجع مجلد الشهر بالصيغة الممررة (مثل "2026-08"):
     * /storage/emulated/0/Documents/الدفتر الذكي/[yearMonth]/
     */
    fun getMonthlyDirectory(yearMonth: String): File {
        validateYearMonthString(yearMonth)
        // [توثيق المتغير/الخاصية: root]: متغير/خاصية تحمل قيمة تشغيلية ضمن هذا النطاق، ويُحدد معناها من سياق العملية التي تستخدمها.
        val root = getPublicBackupRoot()
        return File(root, yearMonth)
    }

    /**
     * [التأكد من وجود وصلاحية المجلد - ensureDirectory]:
     * ينشئ المجلد إذا لم يكن موجوداً، ويفحص أنه مجلد فعلي وقابل للكتابة.
     */
    fun ensureDirectory(directory: File): Result<File> {
        return try {
            // [توثيق المتغير/الخاصية: root]: متغير/خاصية تحمل قيمة تشغيلية ضمن هذا النطاق، ويُحدد معناها من سياق العملية التي تستخدمها.
            val root = getPublicBackupRoot()
            // تدقيق الأمان: التأكد من أن المجلد يقع تحت المجلد الجذري الرسمي
            // [توثيق المتغير/الخاصية: rootCanonical]: متغير/خاصية تحمل قيمة تشغيلية ضمن هذا النطاق، ويُحدد معناها من سياق العملية التي تستخدمها.
            val rootCanonical = root.canonicalPath
            // [توثيق المتغير/الخاصية: dirCanonical]: متغير/خاصية تحمل قيمة تشغيلية ضمن هذا النطاق، ويُحدد معناها من سياق العملية التي تستخدمها.
            val dirCanonical = directory.canonicalPath
            if (!dirCanonical.startsWith(rootCanonical)) {
                return Result.failure(
                    SecurityException("محاولة إنشاء أو استخدام مجلد خارج المسار العام الرسمي المعتمد: $dirCanonical")
                )
            }

            if (!directory.exists()) {
                // [توثيق المتغير/الخاصية: created]: متغير/خاصية تحمل قيمة تشغيلية ضمن هذا النطاق، ويُحدد معناها من سياق العملية التي تستخدمها.
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
     * [التحقق من صحة صيغة السنة والشهر - validateYearMonthString]:
     * يمنع أي محاولات للهروب من المجلد (Path Traversal) أو إدخال أسماء غير قانونية.
     */
    fun validateYearMonthString(yearMonth: String) {
        require(yearMonth.isNotBlank()) { "اسم الشهر لا يمكن أن يكون فارغاً" }
        require(!yearMonth.contains("..") && !yearMonth.contains("/") && !yearMonth.contains("\\")) {
            "اسم الشهر يحتوي على رموز غير مسموح بها تسبب ثغرة اختراق المسار: $yearMonth"
        }
    }

    /**
     * [التحقق من سلامة اسم الملف المستهدف - validateFileName]:
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

// --- ملاحظات وتوصيات المعمارية البرمجية ---
// - يجب إبقاء التحقق من year-month واسم الملف صارماً لمنع مسارات غير متوقعة.
// - يفضل مستقبلاً جعل مصدر الجذر العام قابلاً للاختبار عبر abstraction بدلاً من ربط الاختبارات مباشرة بنظام الملفات.
// - أي تعديل في بنية المجلدات يجب أن يراعي النسخ الموجودة مسبقاً.
// - هذه الملاحظات توصيات مستقبلية فقط ولا تغيّر التنفيذ الحالي أو عقده البرمجي.
