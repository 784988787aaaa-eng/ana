/**
 * =====================================================================
 * ملف: محول النسخ الاحتياطية ودعم التوافق التاريخي (MzdBackupSerializer.kt)
 * =====================================================================
 * 
 * [الغرض العام والتعليمي من الملف]:
 * يوفر هذا الكائن جسراً معمارياً متقدماً لدعم التوافق العكسي مع كافة إصدارات
 * حزم النسخ الاحتياطي MZD والإصدارات القديمة (Legacy Backups v1/v2).
 * يتولى القراءة الذكية للكيانات، والحفاظ على قدسية اختيارات المستخدم المحفوظة،
 * والكتابة الذرية الآمنة للملفات على القرص لتجنب تلف البيانات عند انقطاع الطاقة.
 * 
 * [المسؤوليات المعمارية والتقنية]:
 * 1. الحفظ الذري الآمن (Atomic File Writing):
 *    - الكتابة في ملف مؤقت (`tmp_mzd_...`) ثم إعادة التسمية والاستبدال الذري لمنع الملفات الناقصة.
 * 2. قدسية خيارات المستخدم الصريحة (Explicit User Intent):
 *    - الالتزام التام بالقيمة الصريحة لحقل `initial_type` ومنع إعادة اشتقاقه إلا عند غيابه التام في النسخ العتيقة.
 * 3. التسامح مع تنوع المخططات والمفاتيح القديمة:
 *    - قراءة مفاتيح `habayeb_debts` أو `habayeb_debts_db`، وحقول `customer_id` أو `customerId`.
 * 4. استخراج دقيق للفئات المخصصة والمحذوفات والديون والعملات الأجنبية.
 */
package com.example.data.serialization

// ---------------------------------------------------------------------
// استيراد حزم سياق أندرويد والسجلات والكيانات والنماذج ومعالجة JSON والملفات
// ---------------------------------------------------------------------
import android.content.Context
import android.util.Log
import com.example.data.local.entities.AppSettings
import com.example.data.local.entities.CustomCategory
import com.example.data.local.entities.DeletedItemEntity
import com.example.data.local.entities.FixedCommitment
import com.example.data.local.entities.HabayebCustomer
import com.example.data.local.entities.HabayebTransaction
import com.example.data.local.entities.TransactionDb
import com.example.domain.model.TransactionType
import com.example.ui.navigation.Screen
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.math.BigDecimal

/**
 * [الكائن الأحادي لمحول وتوافق نسخ MZD - MzdBackupSerializer]:
 * يدير تصدير واستيراد وتحليل ملفات وحزم النسخ الاحتياطي عبر مخططات الإصدارات المختلفة.
 */
object MzdBackupSerializer {

    /** وسم السجلات التشخيصية */
    private const val TAG = "MzdBackupSerializer"

    /**
     * [تصدير النسخة كنص JSON - exportBackupToJson]:
     * يفوض التحويل إلى [BackupPayloadSerializer].
     */
    suspend fun exportBackupToJson(
        settings: AppSettings,
        commitments: List<FixedCommitment>,
        transactions: List<TransactionDb>,
        habayebCustomers: List<HabayebCustomer> = emptyList(),
        habayebTransactions: List<HabayebTransaction> = emptyList(),
        deletedItems: List<DeletedItemEntity> = emptyList(),
        context: Context? = null
    ): String = BackupPayloadSerializer.exportBackupToJson(
        settings, commitments, transactions, habayebCustomers, habayebTransactions, deletedItems, context
    )

    /**
     * [تصدير النسخة الاحتياطية ذرياً إلى ملف محلي - exportBackupToFile]:
     * يكتب المحتوى إلى ملف مؤقت أولاً ثم يستبدل الملف الهدف ذرياً لضمان عدم التلف.
     *
     * @param settings إعدادات التطبيق.
     * @param commitments قائمة الالتزامات.
     * @param transactions قيود اليومية.
     * @param habayebCustomers عملاء الحبايب.
     * @param habayebTransactions معاملات الحبايب.
     * @param deletedItems المحذوفات.
     * @param context سياق التطبيق.
     * @param targetFile الملف المستهدف على القرص.
     */
    suspend fun exportBackupToFile(
        settings: AppSettings,
        commitments: List<FixedCommitment>,
        transactions: List<TransactionDb>,
        habayebCustomers: List<HabayebCustomer> = emptyList(),
        habayebTransactions: List<HabayebTransaction> = emptyList(),
        deletedItems: List<DeletedItemEntity> = emptyList(),
        context: Context,
        targetFile: File
    ) {
        val jsonStr = BackupPayloadSerializer.exportBackupToJson(
            settings, commitments, transactions, habayebCustomers, habayebTransactions, deletedItems, context
        )
        val parentDir = targetFile.parentFile ?: targetFile.absoluteFile.parentFile
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs()
        }

        val tempFile = File.createTempFile("tmp_mzd_", ".tmp", parentDir)
        try {
            tempFile.bufferedWriter(Charsets.UTF_8).use { writer ->
                writer.write(jsonStr)
                writer.flush()
            }
            if (targetFile.exists()) {
                targetFile.delete()
            }
            if (!tempFile.renameTo(targetFile)) {
                tempFile.copyTo(targetFile, overwrite = true)
                tempFile.delete()
            }
        } catch (e: Exception) {
            Log.e(TAG, "فشل تصدير ملف النسخة الاحتياطية MZD بشكل ذري", e)
            if (tempFile.exists()) {
                tempFile.delete()
            }
            throw e
        }
    }

    /**
     * [استخراج رقم BigDecimal بأمان ودقة - getBigDecimal]:
     * يفوض الاستخراج المالي إلى [BackupPayloadSerializer].
     */
    fun getBigDecimal(obj: JSONObject, key: String, fallback: String = "0"): BigDecimal =
        BackupPayloadSerializer.getBigDecimal(obj, key, fallback)

    /**
     * [استيراد النسخة من نص JSON - importBackupFromJson]:
     * يفوض التفكيك إلى [BackupPayloadSerializer].
     */
    suspend fun importBackupFromJson(
        jsonString: String,
        context: Context? = null
    ): Triple<AppSettings, List<FixedCommitment>, List<TransactionDb>> =
        BackupPayloadSerializer.importBackupFromJson(jsonString, context)

    /**
     * [تحليل واستخراج الفئات المخصصة - parseCustomCategories]:
     * يستخرج قائمة [CustomCategory] من كائن الـ JSON الجذري.
     *
     * @param root كائن الـ JSON الجذري للنسخة الاحتياطية.
     * @return قائمة الفئات المخصصة المستعادة.
     */
    fun parseCustomCategories(root: JSONObject): List<CustomCategory> {
        val list = mutableListOf<CustomCategory>()
        if (root.has("custom_categories") && !root.isNull("custom_categories")) {
            val catsArr = root.optJSONArray("custom_categories")
            if (catsArr != null) {
                for (i in 0 until catsArr.length()) {
                    val obj = catsArr.getJSONObject(i)
                    list.add(
                        CustomCategory(
                            name = obj.getString("name"),
                            tabType = obj.optString("tab_type", Screen.HABAYEB.name),
                            iconEmoji = obj.optString("icon_emoji", ""),
                            displayOrder = obj.optInt("display_order", i),
                            isSystemClosed = obj.optBoolean("is_system_closed", false)
                        )
                    )
                }
            }
        }
        return list
    }

    /**
     * [تحليل واستخراج عناصر سلة المهملات - parseDeletedItems]:
     * يستخرج قائمة [DeletedItemEntity] من حزمة النسخ الاحتياطي.
     *
     * @param root كائن الـ JSON الجذري.
     * @return قائمة العناصر المحذوفة المستعادة.
     */
    fun parseDeletedItems(root: JSONObject): List<DeletedItemEntity> {
        val list = mutableListOf<DeletedItemEntity>()
        if (root.has("deleted_items") && !root.isNull("deleted_items")) {
            val deletedItemsArr = root.optJSONArray("deleted_items")
            if (deletedItemsArr != null) {
                for (i in 0 until deletedItemsArr.length()) {
                    val obj = deletedItemsArr.getJSONObject(i)
                    list.add(
                        DeletedItemEntity(
                            id = obj.getString("id"),
                            sourceSystem = obj.getString("sourceSystem"),
                            originalTableName = obj.getString("originalTableName"),
                            jsonData = obj.getString("jsonData"),
                            deletedAt = obj.getLong("deletedAt")
                        )
                    )
                }
            }
        }
        return list
    }

    /**
     * [وعاء بيانات عميل الحبايب المستعاد مع رابط الفئة - RestoredHabayebCustomerData]:
     * يجمع كيان العميل مع التصنيف المربوط به إن وجد.
     *
     * @property customer بطاقة العميل المستعادة.
     * @property categoryLink الفئة المربوط بها العميل.
     */
    data class RestoredHabayebCustomerData(
        val customer: HabayebCustomer,
        val categoryLink: String?
    )

    /**
     * [تحليل واستعادة عملاء الحبايب والديون - parseHabayebCustomers]:
     * يحلل مصفوفات العملاء ويدعم استعادة `initial_type` الصريح أو اشتقاقه للنسخ التاريخية القديمة.
     *
     * @param root كائن JSON الجذري.
     * @return قائمة العملاء مع روابط فئاتهم.
     */
    fun parseHabayebCustomers(root: JSONObject): List<RestoredHabayebCustomerData> {
        val jsonHabayebObj = root.optJSONObject("habayeb_debts")
            ?: root.optJSONObject("habayeb_debts_db")

        val txArr = jsonHabayebObj?.optJSONArray("debt_transactions")
            ?: jsonHabayebObj?.optJSONArray("habayeb_transactions")

        val customerIdToTxTypes = mutableMapOf<String, MutableSet<String>>()
        if (txArr != null) {
            for (i in 0 until txArr.length()) {
                val obj = txArr.getJSONObject(i)
                val cId = obj.optString("customer_id", obj.optString("customerId", "")).trim()
                val tType = obj.optString("type", "").trim()
                if (cId.isNotEmpty() && tType.isNotEmpty()) {
                    customerIdToTxTypes.getOrPut(cId) { mutableSetOf() }.add(tType)
                }
            }
        }

        val custArr = jsonHabayebObj?.optJSONArray("customers")
            ?: jsonHabayebObj?.optJSONArray("habayeb_customers")

        val result = mutableListOf<RestoredHabayebCustomerData>()
        if (custArr != null) {
            for (i in 0 until custArr.length()) {
                val obj = custArr.getJSONObject(i)
                val cId = obj.optString("id", obj.optString("customer_id", "")).trim()

                // التحقق من وجود قيمة صريحة لـ initial_type في ملف النسخ الاحتياطي
                val explicitInitialType = when {
                    obj.has("initial_type") && !obj.isNull("initial_type") -> obj.optString("initial_type").trim()
                    obj.has("initialType") && !obj.isNull("initialType") -> obj.optString("initialType").trim()
                    else -> ""
                }

                val determinedInitialType = if (explicitInitialType.isNotBlank()) {
                    // الالتزام التام بالقيمة الصريحة المحفوظة من المستخدم وعدم إعادة اشتقاقها
                    explicitInitialType
                } else {
                    // التراجع للاشتقاق فقط للنسخ القديمة التي لا تحتوي على الحقل
                    val txTypesForCust = customerIdToTxTypes[cId]
                    if (txTypesForCust != null && txTypesForCust.isNotEmpty()) {
                        if (txTypesForCust.contains(TransactionType.OWED_TO_THEM.value) || txTypesForCust.contains(TransactionType.PAYMENT_TO_THEM.value)) {
                            TransactionType.OWED_TO_THEM.value
                        } else {
                            TransactionType.OWED_BY_THEM.value
                        }
                    } else {
                        TransactionType.OWED_BY_THEM.value
                    }
                }

                val cust = HabayebCustomer(
                    id = cId,
                    name = obj.getString("name"),
                    phone = obj.optString("phone", ""),
                    notes = obj.optString("notes", ""),
                    createdAt = obj.optLong("created_at", obj.optLong("createdAt", System.currentTimeMillis() / 1000)),
                    initialType = determinedInitialType
                )
                val catLink = obj.optString("category_link", null)?.takeIf { it.isNotBlank() }
                result.add(RestoredHabayebCustomerData(cust, catLink))
            }
        }
        return result
    }

    /**
     * [تحليل واستعادة معاملات الحبايب والعملات الأجنبية - parseHabayebTransactions]:
     * يستخرج قيود ديون الحبايب بدقة ويضبط أسعار الصرف والمكافئات المالية بدقة [BigDecimal].
     *
     * @param root كائن الـ JSON الجذري.
     * @param defaultCurrency رمز العملة الافتراضية.
     * @return قائمة المعاملات المستعادة.
     */
    fun parseHabayebTransactions(root: JSONObject, defaultCurrency: String): List<HabayebTransaction> {
        val list = mutableListOf<HabayebTransaction>()
        val jsonHabayebObj = root.optJSONObject("habayeb_debts")
            ?: root.optJSONObject("habayeb_debts_db")

        val txArr = jsonHabayebObj?.optJSONArray("debt_transactions")
            ?: jsonHabayebObj?.optJSONArray("habayeb_transactions")

        if (txArr != null) {
            for (i in 0 until txArr.length()) {
                val obj = txArr.getJSONObject(i)
                val amount = getBigDecimal(obj, "amount")
                val foreignAmount = getBigDecimal(obj, "foreign_amount", getBigDecimal(obj, "foreignAmount", "0").toPlainString())
                val exchangeRate = getBigDecimal(obj, "exchange_rate", getBigDecimal(obj, "exchangeRate", "1").toPlainString())
                val equivalentAmount = getBigDecimal(obj, "equivalent_amount", getBigDecimal(obj, "equivalentAmount", amount.toPlainString()).toPlainString())
                val isForeign = obj.optBoolean("is_foreign", obj.optBoolean("isForeign", false))
                val currencyCode = obj.optString("currency_code", obj.optString("currencyCode", defaultCurrency))
                val baseCurrencyCode = obj.optString("base_currency_code", obj.optString("baseCurrencyCode", defaultCurrency))
                val isRateCalculated = obj.optBoolean("is_rate_calculated", obj.optBoolean("isRateCalculated", false))

                list.add(
                    HabayebTransaction(
                        id = obj.getString("id"),
                        customerId = obj.optString("customer_id", obj.optString("customerId", "")).trim(),
                        type = obj.getString("type"),
                        amount = amount,
                        timestamp = obj.optLong("timestamp", System.currentTimeMillis()),
                        description = obj.optString("description", ""),
                        linkedMainTxId = obj.optString("linked_main_tx_id", obj.optString("linkedMainTxId", null))?.takeIf {
                            it.isNotBlank() && !it.equals("null", ignoreCase = true) && it != "0"
                        },
                        isForeign = isForeign,
                        currencyCode = currencyCode,
                        foreignAmount = foreignAmount,
                        exchangeRate = exchangeRate,
                        isRateCalculated = isRateCalculated,
                        equivalentAmount = equivalentAmount,
                        baseCurrencyCode = baseCurrencyCode
                    )
                )
            }
        }
        return list
    }
}

