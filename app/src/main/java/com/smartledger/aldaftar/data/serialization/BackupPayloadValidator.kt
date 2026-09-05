
/**
 * مدقق حمولة النسخ؛ يرفض البنية التالفة والأرقام غير الصالحة والمعرفات المتكررة ويطبق تحقق التكامل قبل الاستعادة.
 * التوثيق هنا يوضح أثر الدوال على الأمان والتوافق والدقة المالية دون تغيير واجهات الاستدعاء.
 */
package com.smartledger.aldaftar.data.serialization

import com.smartledger.aldaftar.data.backup.BackupConstants
import com.smartledger.aldaftar.data.local.entities.AppSettings
import com.smartledger.aldaftar.data.local.entities.CustomCategory
import com.smartledger.aldaftar.data.local.entities.DeletedItemEntity
import com.smartledger.aldaftar.data.local.entities.FixedCommitment
import com.smartledger.aldaftar.data.local.entities.HabayebCustomer
import com.smartledger.aldaftar.data.local.entities.HabayebTransaction
import com.smartledger.aldaftar.data.local.entities.TransactionDb
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.math.BigDecimal

sealed class BackupValidationResult {
    data class Valid(
        val payloadData: BackupPayloadData,
        val formatVersion: String,
        val isLegacy: Boolean
    ) : BackupValidationResult()

    data class Invalid(
        val reason: String,
        val errorCode: ValidationErrorCode,
        val cause: Throwable? = null
    ) : BackupValidationResult()
}

enum class ValidationErrorCode {
    EMPTY_OR_UNREADABLE_FILE,
    MALFORMED_JSON_SYNTAX,
    UNKNOWN_OR_INCOMPATIBLE_VERSION,
    MISSING_REQUIRED_SECTIONS,
    INTEGRITY_HASH_MISMATCH,
    DUPLICATE_IDENTIFIER,
    DANGLING_FOREIGN_KEY,
    MALFORMED_DECIMAL_NUMBER,
    INVALID_CURRENCY_OR_SETTINGS,
    CORRUPTED_PAYLOAD_STRUCTURE
}

object BackupPayloadValidator {

    const val CURRENT_BACKUP_VERSION = "1.1.0"
    val SUPPORTED_VERSIONS = setOf("1.0.0", "1.0", "1.1.0", "1.1", "1.0-legacy", "legacy")

    /**
     * يتحقق من أن القيمة النصية رقم عشري صالح وقابل للحساب.
     */
    fun isValidDecimalString(raw: Any?): Boolean {
        if (raw == null) return false
        val str = raw.toString().trim()
        if (str.isEmpty() || str.equals("null", ignoreCase = true)) return false
        if (str.equals("NaN", ignoreCase = true) || 
            str.contains("Infinity", ignoreCase = true) ||
            str.contains("E+", ignoreCase = true) || 
            str.contains("E-", ignoreCase = true)) {
            return false
        }
        return try {
            val cleaned = com.smartledger.aldaftar.data.local.BigDecimalConverter.cleanNumberString(str)
            if (cleaned.isEmpty()) false else {
                BigDecimal(cleaned)
                true
            }
        } catch (_: Exception) {
            false
        }
    }

    /**
     * يقرأ الحقل المالي مع رفض القيم الفارغة والتالفة واللانهاية.
     */
    fun parseStrictBigDecimal(obj: JSONObject, key: String, isRequired: Boolean = true, defaultVal: String = "0"): BigDecimal {
        if (!obj.has(key) || obj.isNull(key)) {
            if (isRequired) {
                throw IllegalArgumentException("الحقل المالي الإلزامي مفقود: $key")
            }
            return BigDecimal(defaultVal)
        }
        val raw = obj.opt(key)
        val str = raw?.toString()?.trim() ?: ""
        if (str.isEmpty() || str.equals("null", ignoreCase = true)) {
            if (isRequired) {
                throw IllegalArgumentException("الحقل المالي الإلزامي فارغ أو null: $key")
            }
            return BigDecimal(defaultVal)
        }
        if (str.equals("NaN", ignoreCase = true) || 
            str.contains("Infinity", ignoreCase = true)) {
            throw IllegalArgumentException("تم اكتشاف قيمة عددية تالفة (NaN / Infinity) في الحقل: $key")
        }
        val cleaned = com.smartledger.aldaftar.data.local.BigDecimalConverter.cleanNumberString(str)
        if (cleaned.isEmpty()) {
            throw IllegalArgumentException("فشل تنظيف القيمة الرقمية للحقل: $key (القيمة الأصلية: $str)")
        }
        return try {
            BigDecimal(cleaned)
        } catch (e: Exception) {
            throw IllegalArgumentException("صيغة رقمية غير صالحة للحقل $key: $str", e)
        }
    }

    /**
     * يفحص البنية والمعرفات والمفاتيح والأرقام والبصمة قبل السماح بالاستعادة.
     */
    fun validateBackupPayload(rawJsonString: String, verifyHashStrictly: Boolean = true): BackupValidationResult {
        if (rawJsonString.isBlank()) {
            return BackupValidationResult.Invalid(
                "نص النسخة الاحتياطية فارغ تماماً",
                ValidationErrorCode.EMPTY_OR_UNREADABLE_FILE
            )
        }

        val root = try {
            JSONObject(rawJsonString)
        } catch (e: Exception) {
            return BackupValidationResult.Invalid(
                "فشل تفكيك بنية JSON: ${e.message}",
                ValidationErrorCode.MALFORMED_JSON_SYNTAX,
                e
            )
        }

        val isLegacyContainer = root.has(BackupConstants.JSON_KEY_MIZAN_AL_DAR_DB) || 
                                root.has(BackupConstants.JSON_KEY_HABAYEB_DEBTS_DB)

        val sourceObj = if (root.has(BackupConstants.JSON_KEY_MIZAN_AL_DAR_DB)) {
            root.getJSONObject(BackupConstants.JSON_KEY_MIZAN_AL_DAR_DB)
        } else {
            root
        }

        var formatVersion = "legacy"
        var embeddedHash: String? = null
        if (root.has("metadata") && !root.isNull("metadata")) {
            val metaObj = root.optJSONObject("metadata")
            if (metaObj != null) {
                formatVersion = metaObj.optString("app_version", CURRENT_BACKUP_VERSION)
                embeddedHash = metaObj.optString("security_hash", "").takeIf { it.isNotBlank() }
            }
        }

        val settingsObj = sourceObj.optJSONObject(BackupConstants.JSON_KEY_SETTINGS)
        val settings = if (settingsObj != null) {
            val currency = settingsObj.optString("currency_symbol", "").trim()
            if (currency.isBlank()) {
                return BackupValidationResult.Invalid(
                    "رمز العملة في إعدادات النسخة غير صالح أو فارغ",
                    ValidationErrorCode.INVALID_CURRENCY_OR_SETTINGS
                )
            }
            AppSettings(
                currencySymbol = currency,
                schoolExpensesEnabled = settingsObj.optBoolean("school_expenses_enabled", true),
                themeMode = 0,
                exchangeRatesJson = settingsObj.optString("exchange_rates_json", "{}")
            )
        } else {
            AppSettings()
        }

        val commitments = mutableListOf<FixedCommitment>()
        val commitmentsArr = sourceObj.optJSONArray(BackupConstants.JSON_KEY_FIXED_COMMITMENTS)
            ?: sourceObj.optJSONArray(BackupConstants.JSON_KEY_COMMITMENTS)
        if (commitmentsArr != null) {
            for (i in 0 until commitmentsArr.length()) {
                val obj = commitmentsArr.optJSONObject(i)
                    ?: return BackupValidationResult.Invalid(
                        "عنصر تالف في مصفوفة الالتزامات المالية عند الفهرس $i",
                        ValidationErrorCode.CORRUPTED_PAYLOAD_STRUCTURE
                    )
                val name = obj.optString("name", "").trim()
                if (name.isBlank()) {
                    return BackupValidationResult.Invalid(
                        "اسم الالتزام المالي فارغ في الفهرس $i",
                        ValidationErrorCode.CORRUPTED_PAYLOAD_STRUCTURE
                    )
                }
                val targetAmount = try {
                    parseStrictBigDecimal(obj, "target_amount", isRequired = true)
                } catch (e: Exception) {
                    return BackupValidationResult.Invalid(
                        "قيمة المبلغ المستهدف غير صالحة في الالتزام '$name': ${e.message}",
                        ValidationErrorCode.MALFORMED_DECIMAL_NUMBER,
                        e
                    )
                }
                val currentProgress = try {
                    parseStrictBigDecimal(obj, "current_progress", isRequired = false, defaultVal = "0")
                } catch (e: Exception) {
                    return BackupValidationResult.Invalid(
                        "قيمة التقدم المالي غير صالحة في الالتزام '$name': ${e.message}",
                        ValidationErrorCode.MALFORMED_DECIMAL_NUMBER,
                        e
                    )
                }
                commitments.add(
                    FixedCommitment(
                        name = name,
                        targetAmount = targetAmount,
                        currentProgress = currentProgress,
                        orderIndex = obj.optInt("order_index", i)
                    )
                )
            }
        }

        val transactions = mutableListOf<TransactionDb>()
        val transactionIds = mutableSetOf<String>()
        val txArr = sourceObj.optJSONArray(BackupConstants.JSON_KEY_TRANSACTIONS)
        if (txArr != null) {
            for (i in 0 until txArr.length()) {
                val obj = txArr.optJSONObject(i)
                    ?: return BackupValidationResult.Invalid(
                        "عنصر تالف في مصفوفة قيود اليومية عند الفهرس $i",
                        ValidationErrorCode.CORRUPTED_PAYLOAD_STRUCTURE
                    )
                val id = obj.optString("id", "").trim()
                if (id.isBlank()) {
                    return BackupValidationResult.Invalid(
                        "معرف المعاملة فارغ عند الفهرس $i",
                        ValidationErrorCode.CORRUPTED_PAYLOAD_STRUCTURE
                    )
                }
                if (!transactionIds.add(id)) {
                    return BackupValidationResult.Invalid(
                        "تم اكتشاف معرف معاملة مكرر في اليومية العامة: $id",
                        ValidationErrorCode.DUPLICATE_IDENTIFIER
                    )
                }
                val amount = try {
                    parseStrictBigDecimal(obj, "amount", isRequired = true)
                } catch (e: Exception) {
                    return BackupValidationResult.Invalid(
                        "مبلغ المعاملة غير صالح في القيد رقم $id: ${e.message}",
                        ValidationErrorCode.MALFORMED_DECIMAL_NUMBER,
                        e
                    )
                }
                val timestamp = obj.optLong("timestamp", 0L)
                if (timestamp <= 0L) {
                    return BackupValidationResult.Invalid(
                        "الطابع الزمني غير صالح للمعاملة رقم $id",
                        ValidationErrorCode.CORRUPTED_PAYLOAD_STRUCTURE
                    )
                }
                transactions.add(
                    TransactionDb(
                        id = id,
                        timestamp = timestamp,
                        type = obj.optString("type", "EXPENSE"),
                        category = obj.optString("category", "عام"),
                        amount = amount,
                        description = obj.optString("description", "")
                    )
                )
            }
        }

        val customerDataList = MzdBackupSerializer.parseHabayebCustomers(root)
        val customers = customerDataList.map { it.customer }
        val customerIds = mutableSetOf<String>()
        for (c in customers) {
            if (c.id.isBlank()) {
                return BackupValidationResult.Invalid(
                    "تم العثور على سجل عميل بدون معرف فريد (ID)",
                    ValidationErrorCode.CORRUPTED_PAYLOAD_STRUCTURE
                )
            }
            if (!customerIds.add(c.id)) {
                return BackupValidationResult.Invalid(
                    "تم اكتشاف معرف عميل مكرر في ديون الحبايب: ${c.id}",
                    ValidationErrorCode.DUPLICATE_IDENTIFIER
                )
            }
        }

        val habayebTxRaw = MzdBackupSerializer.parseHabayebTransactions(root, settings.currencySymbol)
        val habayebTxIds = mutableSetOf<String>()
        val habayebTransactions = mutableListOf<HabayebTransaction>()

        for (htx in habayebTxRaw) {
            if (htx.id.isBlank()) {
                return BackupValidationResult.Invalid(
                    "تم العثور على معاملة دين بدون معرف فريد (ID)",
                    ValidationErrorCode.CORRUPTED_PAYLOAD_STRUCTURE
                )
            }
            if (!habayebTxIds.add(htx.id)) {
                return BackupValidationResult.Invalid(
                    "تم اكتشاف معرف معاملة دين مكرر: ${htx.id}",
                    ValidationErrorCode.DUPLICATE_IDENTIFIER
                )
            }
            if (htx.customerId.isBlank()) {
                return BackupValidationResult.Invalid(
                    "معاملة الدين رقم ${htx.id} غير مربوطة بأي عميل (customerId مفقود)",
                    ValidationErrorCode.DANGLING_FOREIGN_KEY
                )
            }
            habayebTransactions.add(htx)
        }

        val customCategories = MzdBackupSerializer.parseCustomCategories(root)
        val deletedItems = MzdBackupSerializer.parseDeletedItems(root)
        val categoryLinks = customerDataList.mapNotNull { data ->
            data.categoryLink?.let { data.customer.id to it }
        }.toMap()

        val pinnedMap = mutableMapOf<String, Set<String>>()
        if (root.has(BackupConstants.JSON_KEY_PINNED_CUSTOMERS) && !root.isNull(BackupConstants.JSON_KEY_PINNED_CUSTOMERS)) {
            val pinnedObj = root.optJSONObject(BackupConstants.JSON_KEY_PINNED_CUSTOMERS)
            if (pinnedObj != null) {
                val keys = pinnedObj.keys()
                while (keys.hasNext()) {
                    val k = keys.next()
                    val arr = pinnedObj.optJSONArray(k)
                    if (arr != null) {
                        val set = mutableSetOf<String>()
                        for (idx in 0 until arr.length()) {
                            set.add(arr.getString(idx))
                        }
                        pinnedMap[k] = set
                    }
                }
            }
        }

        val payloadData = BackupPayloadData(
            settings = settings,
            commitments = commitments,
            transactions = transactions,
            habayebCustomers = customers,
            habayebTransactions = habayebTransactions,
            deletedItems = deletedItems,
            customCategories = customCategories,
            categoryLinks = categoryLinks,
            pinnedCustomerIdsByCategory = pinnedMap,
            categoryOrderList = root.optString(BackupConstants.JSON_KEY_CATEGORY_ORDER_LIST, null),
            closedCustomName = root.optString(BackupConstants.JSON_KEY_CLOSED_CUSTOM_NAME, null)
        )

        if (verifyHashStrictly && !isLegacyContainer && formatVersion in setOf("1.1.0", "1.1")) {
            if (embeddedHash.isNullOrBlank()) {
                return BackupValidationResult.Invalid(
                    "بصمة التكامل المنطقية مفقودة من النسخة الحديثة",
                    ValidationErrorCode.INTEGRITY_HASH_MISMATCH
                )
            }
            val isMatch = BackupIntegrityManager.verifyIntegrity(payloadData, embeddedHash)
            if (!isMatch) {
                return BackupValidationResult.Invalid(
                    "بصمة التكامل المنطقية غير متطابقة (Security Hash Mismatch)؛ قد يكون الملف قد عُدّل خارجياً",
                    ValidationErrorCode.INTEGRITY_HASH_MISMATCH
                )
            }
        }

        return BackupValidationResult.Valid(
            payloadData = payloadData,
            formatVersion = formatVersion,
            isLegacy = isLegacyContainer
        )
    }

    /**
     * يقرأ الملف بعد فحص حدوده ثم يمرره إلى التحقق الشامل.
     */
    fun validateBackupFile(file: File, verifyHashStrictly: Boolean = true): BackupValidationResult {
        if (!file.exists() || !file.isFile || file.length() == 0L) {
            return BackupValidationResult.Invalid(
                "ملف النسخة الاحتياطية غير موجود أو فارغ: ${file.name}",
                ValidationErrorCode.EMPTY_OR_UNREADABLE_FILE
            )
        }
        if (file.length() > 64L * 1024L * 1024L) {
            return BackupValidationResult.Invalid(
                "حجم ملف النسخة الاحتياطية يتجاوز الحد المسموح",
                ValidationErrorCode.EMPTY_OR_UNREADABLE_FILE
            )
        }
        val content = try {
            file.readText(Charsets.UTF_8)
        } catch (e: Exception) {
            return BackupValidationResult.Invalid(
                "فشل قراءة محتوى الملف: ${e.message}",
                ValidationErrorCode.EMPTY_OR_UNREADABLE_FILE,
                e
            )
        }
        return validateBackupPayload(content, verifyHashStrictly)
    }
}
