/**
 * =====================================================================
 * ملف: حارس أمان وسلامة قاعدة البيانات (DatabaseSecurityGuard.kt)
 * =====================================================================
 * 
 * [الغرض العام والتعليمي من الملف]:
 * يمثل هذا الكائن صمام الأمان الدفاعي لحماية وتفتيش قاعدة بيانات التطبيق المحلية.
 * يوفر طبقة حماية متعددة المستويات (Defense-in-Depth) تركز على:
 * 1. منع هجمات قياس التوقيت (Timing Attacks) أثناء مقارنة كلمات المرور والتجزئات.
 * 2. التحقق من سلامة وبنية ملفات SQLite ومنع التلاعب اليدوي بها أو تلفها.
 * 3. فحص بيئة العزل الأمني للتطبيق (Sandbox Health Check) للتأكد من صلاحيات القراءة والكتابة.
 * 
 * [المسؤوليات المعمارية والتقنية للملف]:
 * 1. المقارنة الزمنية الثابتة (Constant-Time String Comparison):
 *    - تنفيذ مقارنة بين النصوص المشفرة ورموز PIN بزمن ثابت كلياً لحرمان المهاجم
 *      من قياس الفروق الميكروثانية لمعرفة الأحرف الصحيحة.
 * 2. الفحص البنيوي لترويسة SQLite (SQLite Magic Header Validation):
 *    - قراءة البايتات الـ 16 الأولى من ملف قاعدة البيانات والتأكد من مطابقتها لمعيار
 *      التنسيق الرسمي "SQLite format 3".
 * 3. فحص عزل البيئة المحلية ومجلد قواعد البيانات (Local Sandbox Assessment):
 *    - التحقق من سلامة مجلد /databases وعدم وجود قيود أو تلف في نظام الملفات.
 */
package com.example.domain

// ---------------------------------------------------------------------
// استيراد حزم سياق أندرويد وإدارة الملفات في نظام التشغيل
// ---------------------------------------------------------------------
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import java.io.File

/**
 * [الكائن الأحادي لحارس أمان قاعدة البيانات - DatabaseSecurityGuard]:
 * يوفر دوال فحص السلامة والتفتيش الأمني على مستوى ملفات التخزين.
 */
object DatabaseSecurityGuard {

    /** البايتات القياسية لترويسة ملف قاعدة بيانات SQLite بصيغة ASCII */
    private val SQLITE_HEADER_BYTES = "SQLite format 3".toByteArray(Charsets.US_ASCII)

    /**
     * [المقارنة الآمنة زمنياً للنصوص ورموز المرور - secureEqual]:
     * تقارن سلسلتين نصيتين دون إنهاء الحلقة مبكراً عند أول حرف غير متطابق،
     * مما يبطل تماماً هجمات القنوات الجانبية المعتمدة على قياس زمن المعالجة (Side-Channel Timing Attacks).
     *
     * @param a النص الأول (مثل الرمز المدخل).
     * @param b النص الثاني (مثل الرمز المخزن أو التجزئة).
     * @return true إذا كانت السلسلتان متطابقتين تماماً وبنفس الطول، وإلا false.
     */
    fun secureEqual(a: String?, b: String?): Boolean {
        if (a == null || b == null) return false
        if (a.length != b.length) return false

        var result = 0
        // تنفيذ عملية XOR على جميع المحارف مع تجميع الفروق بـ OR لضمان ثبات زمن التنفيذ
        for (i in 0 until a.length) {
            result = result or (a[i].code xor b[i].code)
        }
        return result == 0
    }

    /**
     * [التحقق من سلامة وبنية ملف قاعدة البيانات - verifyDatabaseIntegrity]:
     * يفتح ملف قاعدة البيانات ويفحص ترويسته الأصلية للتأكد من أنه ملف SQLite حقيقي وسليم.
     *
     * @param context سياق التطبيق للوصول لمسار قاعدة البيانات.
     * @param databaseName اسم ملف قاعدة البيانات (مثال: "habayeb_database.db").
     * @return true إذا كان الملف سليماً أو لم ينشأ بعد، وfalse إذا كان تالفاً أو تم التلاعب به.
     */
    fun verifyDatabaseIntegrity(context: Context, databaseName: String): Boolean {
        val dbFile = context.getDatabasePath(databaseName)
        // إذا لم يكن الملف موجوداً بعد (تشغيل أول مرة)، نعتبر الحالة سليمة
        if (!dbFile.exists()) return true

        // حجم ترويسة SQLite يجب ألا يقل عن 16 بايت
        if (dbFile.length() < 16) return false
        return try {
            val isHeaderValid = dbFile.inputStream().use { input ->
                val header = ByteArray(16)
                val read = input.read(header)
                if (read == 16) {
                    var matches = true
                    for (i in SQLITE_HEADER_BYTES.indices) {
                        if (header[i] != SQLITE_HEADER_BYTES[i]) {
                            matches = false
                            break
                        }
                    }
                    matches
                } else false
            }

            if (!isHeaderValid) return false

            // فحص PRAGMA quick_check للتأكد من سلامة الجداول الداخلية
            var quickCheckPassed = true
            try {
                val db = SQLiteDatabase.openDatabase(dbFile.path, null, SQLiteDatabase.OPEN_READONLY)
                db.use { database ->
                    database.rawQuery("PRAGMA quick_check", null).use { cursor ->
                        if (cursor.moveToFirst()) {
                            val result = cursor.getString(0)
                            quickCheckPassed = result.equals("ok", ignoreCase = true)
                        }
                    }
                }
            } catch (_: Exception) {
                // إذا تعذر فتحها بالقراءة فقط نكتفي بفحص الترويسة
            }
            quickCheckPassed
        } catch (e: Exception) {
            false
        }
    }

    /**
     * [التحقق من تفويض تصدير البيانات - preventUnauthorizedExport]:
     * يتحقق من رمز المرور إذا كان القفل مفعلاً لمنع التصدير غير المصرح به للبيانات.
     */
    fun preventUnauthorizedExport(context: Context, enteredPin: String?): Boolean {
        val secManager = AppSecurityManager.getInstance(context)
        if (!secManager.isFastPasscodeEnabled() || !secManager.hasAdminPin()) {
            return true
        }
        if (enteredPin.isNullOrBlank()) {
            return false
        }
        return secManager.validateAdminPin(enteredPin)
    }

    /**
     * [فحص صحة البيئة المعزولة لقواعد البيانات - performLocalSandboxHealthCheck]:
     * يتأكد من أن مجلد قواعد البيانات الخاص بالتطبيق يمتلك أذونات القراءة والكتابة الطبيعية.
     *
     * @param context سياق التطبيق للوصول لمجلد البيانات.
     * @return قائمة برموز الأخطاء المكتشفة، أو قائمة فارغة عند سلامة البيئة تماماً.
     */
    fun performLocalSandboxHealthCheck(context: Context): List<String> {
        val dbDir = File(context.applicationInfo.dataDir, "databases")
        if (dbDir.exists() && (!dbDir.canRead() || !dbDir.canWrite())) {
            return listOf("SANDBOX_IO_FAILURE")
        }
        return emptyList()
    }
}

// --- ملاحظات وتوصيات المعمارية البرمجية ---
// هذا القسم توثيقي فقط؛ لا يغيّر أي تعليمة تنفيذية في الملف الأصلي.
// - اعتبار هذا المكون نقطة سياسة وليس بديلاً عن تشفير قاعدة البيانات نفسه.
// - توثيق مصدر كل قرار أمني ومتى يُستدعى الحارس ضمن دورة حياة التطبيق.
// - اختبار حالات تلف/غياب بيانات الحماية مع ضمان الفشل الآمن.
// - أي تنفيذ فعلي لهذه التوصيات يُرحّل إلى مهمة هندسية مستقلة ولا يُجرى داخل هذا الملف أثناء التوثيق.
