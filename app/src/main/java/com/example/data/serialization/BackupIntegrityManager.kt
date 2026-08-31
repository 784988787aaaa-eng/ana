/*
 * ╔══════════════════════════════════════════════════════════════════════╗
 * ║ التوثيق العربي الهندسي — MASTERCLASS DOCUMENTATION                 ║
 * ╚══════════════════════════════════════════════════════════════════════╝
 *
 * الملف: app/src/main/java/com/example/data/serialization/BackupIntegrityManager.kt
 * الدور المعماري: طبقة Serialization / Export.
 *
 * الرؤية التشغيلية:
 * هذا الملف يمثل جزءاً من المسار الذي يحول البيانات الداخلية في التطبيق
 * إلى مخرجات يمكن حفظها أو مشاركتها أو طباعتها خارج التطبيق. أثناء التشغيل
 * تبدأ الرحلة من بيانات Room/Domain، ثم تمر عبر هذا المكوّن، ثم تنتهي
 * بملف أو بنية قابلة للاستهلاك خارج التطبيق. لذلك يجب اعتبار هذا الملف
 * عقداً حساساً بين نموذج البيانات الداخلي وشكل البيانات الخارجي.
 *
 * الوصف المعماري:
 * طبقة سلامة النسخ الاحتياطي المسؤولة عن التحقق من التكامل والبصمات ومطابقة المحتوى قبل الاعتماد عليه.
 *
 * قاعدة الثبات البرمجي:
 * الكود الأصلي يبدأ بعد هذا الرأس مباشرة، وقد تم الحفاظ عليه حرفياً دون
 * تعديل أسماء أو أنواع أو قيم أو منطق تنفيذي. الإضافات في هذه النسخة
 * توثيقية فقط.
 *
 * قراءة تعليمية:
 * تخيل شاشة التطبيق بعد ضغط المستخدم على «تصدير»؛ البيانات التي تظهر
 * أمامه لا تُنسخ عشوائياً، بل تمر بسلسلة تحويل منظمة. هذا الملف هو إحدى
 * حلقات تلك السلسلة: يستقبل البنية المتوقعة، يطبق قواعد التنسيق/التسلسل
 * الخاصة به، ثم يسلم النتيجة للمرحلة التالية.
 */

// --- فهرس العناصر البرمجية في الملف ---
// السطر 86: object BackupIntegrityManager — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 89: private const val ALGORITHM_SHA_256 — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 91: private val HEX_CHARS — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 97: sealed class IntegrityCheckResult — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 99: object Valid — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 101: data class Invalid — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 110: fun calculateSha256Hash — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 111: val digest — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 112: val hashBytes — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 113: val hexChars — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 115: val v — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 129: fun calculateIntegrityHash — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 130: val sb — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 203: fun verifyIntegrity — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 205: val calculated — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 216: fun validateBackupFileIntegrity — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 227: val content — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 237: val root — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 243: val hasValidSchema — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// --- نهاية الفهرس التوثيقي ---

/*
 * ╔══════════════════════════════════════════════════════════════════════╗
 * ║ التوثيق العربي الهندسي — MASTERCLASS DOCUMENTATION                 ║
 * ╚══════════════════════════════════════════════════════════════════════╝
 *
 * الملف: /mnt/data/source_full/app/src/main/java/com/example/data/serialization/BackupIntegrityManager.kt
 * الدور المعماري: طبقة Serialization / Export.
 *
 * الرؤية التشغيلية:
 * هذا الملف يمثل جزءاً من المسار الذي يحول البيانات الداخلية في التطبيق
 * إلى مخرجات يمكن حفظها أو مشاركتها أو طباعتها خارج التطبيق. أثناء التشغيل
 * تبدأ الرحلة من بيانات Room/Domain، ثم تمر عبر هذا المكوّن، ثم تنتهي
 * بملف أو بنية قابلة للاستهلاك خارج التطبيق. لذلك يجب اعتبار هذا الملف
 * عقداً حساساً بين نموذج البيانات الداخلي وشكل البيانات الخارجي.
 *
 * الوصف المعماري:
 * طبقة سلامة النسخ الاحتياطي المسؤولة عن التحقق من التكامل والبصمات ومطابقة المحتوى قبل الاعتماد عليه.
 *
 * قاعدة الثبات البرمجي:
 * الكود الأصلي يبدأ بعد هذا الرأس مباشرة، وقد تم الحفاظ عليه حرفياً دون
 * تعديل أسماء أو أنواع أو قيم أو منطق تنفيذي. الإضافات في هذه النسخة
 * توثيقية فقط.
 *
 * قراءة تعليمية:
 * تخيل شاشة التطبيق بعد ضغط المستخدم على «تصدير»؛ البيانات التي تظهر
 * أمامه لا تُنسخ عشوائياً، بل تمر بسلسلة تحويل منظمة. هذا الملف هو إحدى
 * حلقات تلك السلسلة: يستقبل البنية المتوقعة، يطبق قواعد التنسيق/التسلسل
 * الخاصة به، ثم يسلم النتيجة للمرحلة التالية.
 */

// --- فهرس العناصر البرمجية في الملف ---
// السطر 34: object BackupIntegrityManager — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 37: private const val ALGORITHM_SHA_256 — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 39: private val HEX_CHARS — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 45: sealed class IntegrityCheckResult — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 47: object Valid — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 49: data class Invalid — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 58: fun calculateSha256Hash — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 59: val digest — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 60: val hashBytes — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 61: val hexChars — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 63: val v — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 77: fun calculateIntegrityHash — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 78: val sb — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 151: fun verifyIntegrity — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 153: val calculated — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 164: fun validateBackupFileIntegrity — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 175: val content — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 185: val root — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 191: val hasValidSchema — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// --- نهاية الفهرس التوثيقي ---

/**
 * =====================================================================
 * ملف: مدير سلامة وبصمة النسخ الاحتياطية (BackupIntegrityManager.kt)
 * =====================================================================
 * 
 * [الغرض العام والتعليمي من الملف]:
 * يوفر هذا الكائن الأحادي طبقة التحقق التشفيري الصارم لملفات وحزم النسخ الاحتياطي.
 * يعتمد خوارزمية SHA-256 لحساب البصمة التشفيرية، ويبني "البصمة المنطقية الحتمية"
 * (Deterministic Logical Integrity Hash) التي تضمن تطابق البصمة لنفس البيانات بنسبة 100%
 * بغض النظر عن ترتيب السجلات في الذاكرة أو أنظمة التشغيل المختلفة.
 * 
 * [المسؤوليات المعمارية والتقنية]:
 * 1. حساب تجزئة SHA-256 القياسية للنصوص والمصفوفات البايتية.
 * 2. بناء التجزئة المنطقية الحتمية عبر فرز الكيانات ترتيباً تصاعدياً بالمعرفات والتواريخ:
 *    - الإعدادات والالتزامات المالية مرتبة حسب الفهرس والاسم.
 *    - قيود اليومية ومعاملات الحبايب مرتبة زمنياً ثم بالمعرف.
 *    - العملاء، سلة المهملات، الفئات المخصصة، والروابط المثبتة مرتبة أبجدياً ومعرفياً.
 * 3. التحقق الاستباقي الشامل من ملف النسخة (Validation Guard) قبل الاستعادة لمنع انهيار التطبيق.
 */
package com.example.data.serialization

// ---------------------------------------------------------------------
// استيراد حزم الإدخال والإخراج والتشفير ومعالجة بنية JSON
// ---------------------------------------------------------------------
import java.io.File
import java.io.IOException
import java.security.MessageDigest
import org.json.JSONObject

/**
 * [الكائن الأحادي لمدير سلامة النسخ - BackupIntegrityManager]:
 * يتولى حساب وتدقيق البصمات التشفيرية والتأكد من سلامة ملفات النسخ قبل استيرادها.
 */
object BackupIntegrityManager {

    /** خوارزمية التجزئة التشفيرية المستخدمة */
    private const val ALGORITHM_SHA_256 = "SHA-256"
    /** جدول الحروف الست عشرية لتحويل البايتات */
    private val HEX_CHARS = "0123456789abcdef".toCharArray()

    /**
     * [النوع المغلق لنتيجة فحص السلامة - IntegrityCheckResult]:
     * يمثل الحالات الممكنة لفحص سلامة وصحة ملف النسخة الاحتياطية.
     */
    sealed class IntegrityCheckResult {
        /** النسخة سليمة ومكتملة وصالحة للاستعادة */
        object Valid : IntegrityCheckResult()
        /** النسخة تالفة أو غير متوافقة مع توضيح السبب */
        data class Invalid(val reason: String, val cause: Throwable? = null) : IntegrityCheckResult()
    }

    /**
     * [حساب بصمة SHA-256 المعيارية لسلسلة نصية - calculateSha256Hash]:
     *
     * @param input النص المراد تجزئته.
     * @return البصمة الناتجة بصيغة ست عشرية (Hexadecimal String).
     */
    fun calculateSha256Hash(input: String): String {
        val digest = MessageDigest.getInstance(ALGORITHM_SHA_256)
        val hashBytes = digest.digest(input.toByteArray(Charsets.UTF_8))
        val hexChars = CharArray(hashBytes.size * 2)
        for (i in hashBytes.indices) {
            val v = hashBytes[i].toInt() and 0xFF
            hexChars[i * 2] = HEX_CHARS[v ushr 4]
            hexChars[i * 2 + 1] = HEX_CHARS[v and 0x0F]
        }
        return String(hexChars)
    }

    /**
     * [حساب بصمة التكامل المنطقية الحتمية - calculateIntegrityHash]:
     * ترتب الكيانات منطقياً بأسبقية محددة وتبني نصاً تسلسلياً موحداً لتوليد البصمة الثابتة.
     *
     * @param data كائن حمولة النسخة الاحتياطية الكامل.
     * @return بصمة SHA-256 الحتمية للحمولة.
     */
    fun calculateIntegrityHash(data: BackupPayloadData): String {
        val sb = StringBuilder()
        sb.append("settings:").append(data.settings.currencySymbol).append("|")
            .append(data.settings.schoolExpensesEnabled).append("|")
            .append(data.settings.exchangeRatesJson).append(";")

        sb.append("commitments:")
        data.commitments.sortedWith(compareBy({ it.orderIndex }, { it.name })).forEach {
            sb.append(it.name).append(",").append(it.targetAmount.toPlainString()).append(",")
                .append(it.currentProgress.toPlainString()).append(",")
                .append(it.orderIndex).append("|")
        }
        sb.append(";")

        sb.append("transactions:")
        data.transactions.sortedWith(compareBy({ it.timestamp }, { it.id })).forEach {
            sb.append(it.id).append(",").append(it.timestamp).append(",")
                .append(it.type).append(",").append(it.category).append(",")
                .append(it.amount.toPlainString()).append(",")
                .append(it.description).append("|")
        }
        sb.append(";")

        sb.append("customers:")
        data.habayebCustomers.sortedBy { it.id }.forEach {
            sb.append(it.id).append(",").append(it.name).append(",")
                .append(it.phone).append(",").append(it.initialType).append("|")
        }
        sb.append(";")

        sb.append("habayebTx:")
        data.habayebTransactions.sortedWith(compareBy({ it.timestamp }, { it.id })).forEach {
            sb.append(it.id).append(",").append(it.customerId).append(",")
                .append(it.type).append(",").append(it.amount.toPlainString()).append(",")
                .append(it.timestamp).append(",").append(it.currencyCode).append(",")
                .append(it.foreignAmount.toPlainString()).append(",")
                .append(it.linkedMainTxId ?: "").append("|")
        }
        sb.append(";")

        sb.append("deletedItems:")
        data.deletedItems.sortedBy { it.id }.forEach {
            sb.append(it.id).append(",").append(it.sourceSystem).append(",")
                .append(it.originalTableName).append(",").append(it.deletedAt).append("|")
        }
        sb.append(";")

        sb.append("customCategories:")
        data.customCategories.sortedWith(compareBy({ it.displayOrder }, { it.name })).forEach {
            sb.append(it.name).append(",").append(it.tabType).append(",")
                .append(it.displayOrder).append(",").append(it.isSystemClosed).append("|")
        }
        sb.append(";")

        data.categoryLinks.toSortedMap().forEach { (k, v) ->
            sb.append("catLink:").append(k).append("=").append(v).append(";")
        }

        data.pinnedCustomerIdsByCategory.toSortedMap().forEach { (k, set) ->
            sb.append("pinned:").append(k).append("=")
                .append(set.sorted().joinToString(",")).append(";")
        }

        return calculateSha256Hash(sb.toString())
    }

    /**
     * [التحقق من مطابقة البصمة - verifyIntegrity]:
     * يقارن البصمة المحسوبة للحمولة مع البصمة المتوقعة المسجلة في الترويسة.
     *
     * @param data بيانات حمولة النسخة.
     * @param expectedHash البصمة المرفقة في ملف النسخة.
     * @return true إذا كانت البصمتان متطابقتين تماماً.
     */
    fun verifyIntegrity(data: BackupPayloadData, expectedHash: String): Boolean {
        if (expectedHash.isBlank()) return false
        val calculated = calculateIntegrityHash(data)
        return calculated.equals(expectedHash.trim(), ignoreCase = true)
    }

    /**
     * [التحقق الشامل من سلامة ملف النسخة قبل الاستعادة - validateBackupFileIntegrity]:
     * يفحص وجود الملف وحجمه وقابليته للقراءة وتوافق بنية JSON قبل محاولة فتح قاعدة البيانات.
     *
     * @param file ملف النسخة الاحتياطية على القرص.
     * @return [IntegrityCheckResult.Valid] عند سلامة الملف أو [IntegrityCheckResult.Invalid] مع السبب.
     */
    fun validateBackupFileIntegrity(file: File): IntegrityCheckResult {
        if (!file.exists()) {
            return IntegrityCheckResult.Invalid("ملف النسخة غير موجود: ${file.absolutePath}")
        }
        if (!file.isFile) {
            return IntegrityCheckResult.Invalid("المسار المحدد ليس ملفاً: ${file.absolutePath}")
        }
        if (file.length() == 0L) {
            return IntegrityCheckResult.Invalid("ملف النسخة الاحتياطية فارغ (0 بايت)")
        }

        val content = try {
            file.readText(Charsets.UTF_8)
        } catch (e: Exception) {
            return IntegrityCheckResult.Invalid("فشل قراءة محتوى الملف: ${e.message}", e)
        }

        if (content.isBlank()) {
            return IntegrityCheckResult.Invalid("محتوى النسخة الاحتياطية فارغ تماماً")
        }

        val root = try {
            JSONObject(content)
        } catch (e: Exception) {
            return IntegrityCheckResult.Invalid("صيغة الملف تالفة وليست بصيغة JSON صالحة", e)
        }

        val hasValidSchema = root.has("metadata") ||
                root.has("settings") ||
                root.has("transactions") ||
                root.has("mizan_al_dar_db") ||
                root.has("habayeb_debts_db")

        if (!hasValidSchema) {
            return IntegrityCheckResult.Invalid("بنية النسخة الاحتياطية مفقودة أو غير متوافقة مع النظام")
        }

        return IntegrityCheckResult.Valid
    }
}



/*
 * --- ملاحظات وتوصيات المعمارية البرمجية ---
 * 1) هذا الملف جزء من حدود التحويل بين نموذج التطبيق والمخرج الخارجي؛
 *    أي تغيير مستقبلي يجب أن يسبقه اختبار توافق مع المستهلكين الحاليين.
 * 2) يجب الحفاظ على دقة القيم المالية وعدم إجراء تحويلات تقريبية غير مقصودة.
 * 3) يفضّل مستقبلاً فصل مسؤولية بناء البيانات عن مسؤولية I/O عندما يسمح
 *    التصميم بذلك، مع إبقاء السلوك الحالي ثابتاً أثناء أي Refactoring.
 * 4) أي تعديل في صيغة المخرج يجب أن يرافقه اختبار Regression يثبت أن
 *    الملفات القديمة والجديدة قابلة للقراءة وفق متطلبات المشروع.
 * 5) عند التعامل مع بيانات المستخدم، ينبغي استمرار تطبيق سياسات الخصوصية
 *    والصلاحيات والمشاركة الآمنة قبل إرسال الملفات إلى تطبيقات خارجية.
 * 6) لا تمثل هذه الملاحظات تغييراً في التنفيذ الحالي؛ هي نقاط هندسية
 *    مرجعية لأي مرحلة تطوير مستقبلية.
 */


/*
 * --- ملاحظات وتوصيات المعمارية البرمجية ---
 * 1) هذا الملف جزء من حدود التحويل بين نموذج التطبيق والمخرج الخارجي؛
 *    أي تغيير مستقبلي يجب أن يسبقه اختبار توافق مع المستهلكين الحاليين.
 * 2) يجب الحفاظ على دقة القيم المالية وعدم إجراء تحويلات تقريبية غير مقصودة.
 * 3) يفضّل مستقبلاً فصل مسؤولية بناء البيانات عن مسؤولية I/O عندما يسمح
 *    التصميم بذلك، مع إبقاء السلوك الحالي ثابتاً أثناء أي Refactoring.
 * 4) أي تعديل في صيغة المخرج يجب أن يرافقه اختبار Regression يثبت أن
 *    الملفات القديمة والجديدة قابلة للقراءة وفق متطلبات المشروع.
 * 5) عند التعامل مع بيانات المستخدم، ينبغي استمرار تطبيق سياسات الخصوصية
 *    والصلاحيات والمشاركة الآمنة قبل إرسال الملفات إلى تطبيقات خارجية.
 * 6) لا تمثل هذه الملاحظات تغييراً في التنفيذ الحالي؛ هي نقاط هندسية
 *    مرجعية لأي مرحلة تطوير مستقبلية.
 */
