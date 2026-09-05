
/**
 * مدير سلامة النسخ؛ يحسب البصمات الحتمية ويتحقق من الملفات قبل الاستعادة مع مقارنة ثابتة الزمن وحدود حجم آمنة.
 * التوثيق هنا يوضح أثر الدوال على الأمان والتوافق والدقة المالية دون تغيير واجهات الاستدعاء.
 */
package com.smartledger.aldaftar.data.serialization

import java.io.File
import java.io.IOException
import java.security.MessageDigest
import org.json.JSONObject

object BackupIntegrityManager {

    private const val ALGORITHM_SHA_256 = "SHA-256"
    
    private val HEX_CHARS = "0123456789abcdef".toCharArray()

    sealed class IntegrityCheckResult {
        
        object Valid : IntegrityCheckResult()
        
        data class Invalid(val reason: String, val cause: Throwable? = null) : IntegrityCheckResult()
    }

    /**
     * يحسب بصمة تجزئة تشفيرية ويحوّلها إلى تمثيل سداسي ثابت.
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
        val result = String(hexChars)
        java.util.Arrays.fill(hashBytes, 0)
        java.util.Arrays.fill(hexChars, '\u0000')
        return result
    }

    /**
     * يبني تمثيلاً حتمياً للحمولة ثم يحسب بصمتها دون تغيير القيم المالية.
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
     * يقارن البصمة المتوقعة والمحسوبة بمقارنة ثابتة الزمن.
     */
    fun verifyIntegrity(data: BackupPayloadData, expectedHash: String): Boolean {
        if (expectedHash.isBlank()) return false
        val calculated = calculateIntegrityHash(data)
        val calculatedBytes = calculated.lowercase(java.util.Locale.ROOT).toByteArray(Charsets.UTF_8)
        val expectedBytes = expectedHash.trim().lowercase(java.util.Locale.ROOT).toByteArray(Charsets.UTF_8)
        val result = MessageDigest.isEqual(calculatedBytes, expectedBytes)
        java.util.Arrays.fill(calculatedBytes, 0)
        java.util.Arrays.fill(expectedBytes, 0)
        return result
    }

    /**
     * يتحقق من وجود الملف وحجمه وبنيته قبل تمرير محتواه إلى طبقة الاستعادة.
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
        if (file.length() > 64L * 1024L * 1024L) {
            return IntegrityCheckResult.Invalid("حجم ملف النسخة الاحتياطية يتجاوز الحد المسموح")
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

