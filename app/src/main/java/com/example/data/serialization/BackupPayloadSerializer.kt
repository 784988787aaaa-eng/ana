/**
 * =====================================================================
 * ملف: محرك تسلسل وتصدير حمولة النسخ الاحتياطي (BackupPayloadSerializer.kt)
 * =====================================================================
 * 
 * [الغرض العام والتعليمي من الملف]:
 * يمثل هذا المكون العصب المركزي لعمليات تحويل البيانات المالية وقواعد بيانات التطبيق
 * الثنائية إلى صيغة نصية مهيكلة ومعيارية JSON، والعكس. يعتمد على تقنية التدفق المتسلسل
 * المباشر (Streaming Serialization) عبر [android.util.JsonWriter] لضمان استهلاك ذاكرة ثابت
 * ومنع أخطاء نفاد الذاكرة (OutOfMemoryError) أثناء تصدير مئات الآلاف من السجلات.
 * 
 * [المسؤوليات المعمارية والتقنية]:
 * 1. الحفاظ الصارم على الدقة المالية لـ [BigDecimal]:
 *    - تحويل الأرقام حصرياً عبر `toPlainString()` دون أي تحويل وسيط إلى أرقام عشرية عائمة (Float/Double).
 * 2. التوافق التاريخي العكسي (Backward Compatibility):
 *    - تثبيت مفاتيح بنية الـ JSON التاريخية لضمان استيراد النسخ القديمة دون أدنى تعارض.
 * 3. المعالجة الآمنة للتدفقات (Stream-Based I/O):
 *    - إتاحة التصدير المباشر إلى ملفات [File]، ومسارات خروج [OutputStream]، ومحررات نصوص [Writer].
 * 4. التدقيق الاستباقي للبنية والتحقق التشفيري:
 *    - فحص سلامة الحقول الإلزامية ورمز العملة وحساب التجزئة التشفيرية SHA-256 للبيانات.
 */
package com.example.data.serialization

// ---------------------------------------------------------------------
// استيراد حزم سياق أندرويد والكيانات ومحولات الأرقام ومعالجة JSON والتزامن
// ---------------------------------------------------------------------
import android.content.Context
import com.example.data.local.BigDecimalConverter
import com.example.data.local.entities.AppSettings
import com.example.data.local.entities.CustomCategory
import com.example.data.local.entities.DatabaseDefaults
import com.example.data.local.entities.DeletedItemEntity
import com.example.data.local.entities.FixedCommitment
import com.example.data.local.entities.HabayebCustomer
import com.example.data.local.entities.HabayebTransaction
import com.example.data.local.entities.TransactionDb
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.IOException
import java.math.BigDecimal

/**
 * [وعاء بيانات النسخ الاحتياطي الكامل - BackupPayloadData]:
 * يجمع كافة كيانات وتفضيلات وروابط النظام المالي في كائن موحد قبل التصدير.
 *
 * @property settings إعدادات التطبيق والعملة وأسعار الصرف.
 * @property commitments قائمة الالتزامات المالية الثابتة.
 * @property transactions قائمة قيود اليومية العامة.
 * @property habayebCustomers قائمة بطاقات عملاء الحبايب.
 * @property habayebTransactions قائمة معاملات ديون الحبايب والعملات الأجنبية.
 * @property deletedItems عناصر سلة المهملات.
 * @property customCategories التصنيفات المخصصة.
 * @property categoryLinks خريطة ربط العملاء بالتصنيفات.
 * @property pinnedCustomerIdsByCategory خريطة العملاء المثبتين حسب التصنيف.
 * @property categoryOrderList ترتيب التبويبات المخصص.
 * @property closedCustomName التسمية المخصصة للحسابات المقفلة.
 */
data class BackupPayloadData(
    val settings: AppSettings,
    val commitments: List<FixedCommitment>,
    val transactions: List<TransactionDb>,
    val habayebCustomers: List<HabayebCustomer> = emptyList(),
    val habayebTransactions: List<HabayebTransaction> = emptyList(),
    val deletedItems: List<DeletedItemEntity> = emptyList(),
    val customCategories: List<CustomCategory> = emptyList(),
    val categoryLinks: Map<String, String> = emptyMap(),
    val pinnedCustomerIdsByCategory: Map<String, Set<String>> = emptyMap(),
    val categoryOrderList: String? = null,
    val closedCustomName: String? = null
)

/**
 * [الكائن الأحادي لمحرك تسلسل النسخ الاحتياطي - BackupPayloadSerializer]:
 * يدير عمليات التحويل الثنائي وكتابة واستيراد حزم النسخ الاحتياطي.
 */
object BackupPayloadSerializer {

    // =================================================================
    // مفاتيح بنية الـ JSON المعيارية (ثابتة لحماية التوافق التاريخي)
    // =================================================================
    private const val KEY_MIZAN_AL_DAR_DB = "mizan_al_dar_db"
    private const val KEY_HABAYEB_DEBTS_DB = "habayeb_debts_db"
    private const val KEY_METADATA = "metadata"
    private const val KEY_APP_NAME = "app_name"
    private const val KEY_APP_VERSION = "app_version"
    private const val KEY_BACKUP_TIMESTAMP = "backup_timestamp"
    private const val KEY_SECURITY_HASH = "security_hash"

    private const val KEY_SETTINGS = "settings"
    private const val KEY_CURRENCY_SYMBOL = "currency_symbol"
    private const val KEY_SCHOOL_EXPENSES_ENABLED = "school_expenses_enabled"
    private const val KEY_EXCHANGE_RATES_JSON = "exchange_rates_json"

    private const val KEY_FIXED_COMMITMENTS = "fixed_commitments"
    private const val KEY_NAME = "name"
    private const val KEY_TARGET_AMOUNT = "target_amount"
    private const val KEY_CURRENT_PROGRESS = "current_progress"
    private const val KEY_ORDER_INDEX = "order_index"

    private const val KEY_TRANSACTIONS = "transactions"
    private const val KEY_ID = "id"
    private const val KEY_TIMESTAMP = "timestamp"
    private const val KEY_TYPE = "type"
    private const val KEY_CATEGORY = "category"
    private const val KEY_AMOUNT = "amount"
    private const val KEY_DESCRIPTION = "description"

    private const val KEY_HABAYEB_DEBTS = "habayeb_debts"
    private const val KEY_CUSTOMERS = "customers"
    private const val KEY_PHONE = "phone"
    private const val KEY_NOTES = "notes"
    private const val KEY_CREATED_AT = "created_at"
    private const val KEY_INITIAL_TYPE = "initial_type"
    private const val KEY_CATEGORY_LINK = "category_link"

    private const val KEY_DEBT_TRANSACTIONS = "debt_transactions"
    private const val KEY_CUSTOMER_ID = "customer_id"
    private const val KEY_LINKED_MAIN_TX_ID = "linked_main_tx_id"
    private const val KEY_IS_FOREIGN = "is_foreign"
    private const val KEY_CURRENCY_CODE = "currency_code"
    private const val KEY_FOREIGN_AMOUNT = "foreign_amount"
    private const val KEY_EXCHANGE_RATE = "exchange_rate"
    private const val KEY_IS_RATE_CALCULATED = "is_rate_calculated"
    private const val KEY_EQUIVALENT_AMOUNT = "equivalent_amount"
    private const val KEY_BASE_CURRENCY_CODE = "base_currency_code"

    private const val KEY_DELETED_ITEMS = "deleted_items"
    private const val KEY_SOURCE_SYSTEM = "sourceSystem"
    private const val KEY_ORIGINAL_TABLE_NAME = "originalTableName"
    private const val KEY_JSON_DATA = "jsonData"
    private const val KEY_DELETED_AT = "deletedAt"

    private const val KEY_PINNED_CUSTOMER_IDS_BY_CATEGORY = "pinned_customer_ids_by_category"
    private const val KEY_CATEGORY_ORDER_LIST = "category_order_list"
    private const val KEY_CLOSED_CUSTOM_NAME = "closed_custom_name"

    private const val KEY_CUSTOM_CATEGORIES = "custom_categories"
    private const val KEY_TAB_TYPE = "tab_type"
    private const val KEY_ICON_EMOJI = "icon_emoji"
    private const val KEY_DISPLAY_ORDER = "display_order"
    private const val KEY_IS_SYSTEM_CLOSED = "is_system_closed"

    /** حساب بصمة التجزئة SHA-256 للنصوص */
    fun calculateSha256Hash(input: String): String =
        BackupIntegrityManager.calculateSha256Hash(input)

    /** حساب البصمة المنطقية الحتمية للحمولة */
    fun calculateIntegrityHash(data: BackupPayloadData): String =
        BackupIntegrityManager.calculateIntegrityHash(data)

    /**
     * [التحقق من سلامة البيانات قبل التصدير - validatePayloadBeforeExport]:
     * يفحص صحة الحقول الأساسية كرمز العملة قبل الشروع في التصدير.
     *
     * @param data بيانات حمولة النسخة الاحتياطية.
     */
    fun validatePayloadBeforeExport(data: BackupPayloadData) {
        if (data.settings.currencySymbol.isBlank()) {
            throw IllegalArgumentException("رمز العملة في الإعدادات لا يمكن أن يكون فارغاً")
        }
    }

    /**
     * [التحقق من صحة بنية JSON الأساسية - validateJsonStructure]:
     * يتأكد من سلامة نص الـ JSON ووجود الأقسام والجداول الرئيسية قبل المعالجة.
     *
     * @param rawJson النص الخام لملف النسخة.
     * @return كائن [JSONObject] الجذري.
     */
    fun validateJsonStructure(rawJson: String): JSONObject {
        if (rawJson.isBlank()) {
            throw IOException("نص النسخة الاحتياطية فارغ")
        }
        val root = try {
            JSONObject(rawJson)
        } catch (e: Exception) {
            throw IOException("صيغة JSON غير صالحة للنسخة الاحتياطية: ${e.message}", e)
        }

        val hasValidSchema = root.has(KEY_METADATA) ||
                root.has(KEY_SETTINGS) ||
                root.has(KEY_TRANSACTIONS) ||
                root.has(KEY_MIZAN_AL_DAR_DB) ||
                root.has(KEY_HABAYEB_DEBTS_DB)

        if (!hasValidSchema) {
            throw IOException("بنية ملف النسخة الاحتياطية غير معروفة أو تفتقد للعناصر الأساسية")
        }

        return root
    }

    /**
     * [التصدير المتدفق المباشر إلى كاتب - exportBackupToWriter]:
     * يكتب عناصر الحمولة تباعاً عبر [android.util.JsonWriter] دون تجميعها كنص ضخم في الذاكرة.
     *
     * @param data بيانات الحمولة الشاملة.
     * @param writer كاتب الإدخال/الإخراج المستهدف.
     */
    fun exportBackupToWriter(data: BackupPayloadData, writer: java.io.Writer) {
        validatePayloadBeforeExport(data)

        val jsonWriter = android.util.JsonWriter(writer)
        jsonWriter.beginObject()

        // البيانات الوصفية (Metadata)
        jsonWriter.name(KEY_METADATA)
        jsonWriter.beginObject()
        jsonWriter.name(KEY_APP_NAME).value("Mizan Al-Dar")
        jsonWriter.name(KEY_APP_VERSION).value("1.1.0")
        jsonWriter.name(KEY_BACKUP_TIMESTAMP).value(System.currentTimeMillis() / 1000)
        jsonWriter.name(KEY_SECURITY_HASH).value(calculateIntegrityHash(data))
        jsonWriter.endObject()

        // الإعدادات العامة (Settings)
        jsonWriter.name(KEY_SETTINGS)
        jsonWriter.beginObject()
        jsonWriter.name(KEY_CURRENCY_SYMBOL).value(data.settings.currencySymbol)
        jsonWriter.name(KEY_SCHOOL_EXPENSES_ENABLED).value(data.settings.schoolExpensesEnabled)
        jsonWriter.name(KEY_EXCHANGE_RATES_JSON).value(data.settings.exchangeRatesJson)
        jsonWriter.endObject()

        // الالتزامات المالية الثابتة (Fixed Commitments)
        jsonWriter.name(KEY_FIXED_COMMITMENTS)
        jsonWriter.beginArray()
        for (fc in data.commitments) {
            jsonWriter.beginObject()
            jsonWriter.name(KEY_NAME).value(fc.name)
            jsonWriter.name(KEY_TARGET_AMOUNT).value(fc.targetAmount.toPlainString())
            jsonWriter.name(KEY_CURRENT_PROGRESS).value(fc.currentProgress.toPlainString())
            jsonWriter.name(KEY_ORDER_INDEX).value(fc.orderIndex)
            jsonWriter.endObject()
        }
        jsonWriter.endArray()

        // قيود اليومية العامة (Transactions)
        jsonWriter.name(KEY_TRANSACTIONS)
        jsonWriter.beginArray()
        for (tx in data.transactions) {
            jsonWriter.beginObject()
            jsonWriter.name(KEY_ID).value(tx.id)
            jsonWriter.name(KEY_TIMESTAMP).value(tx.timestamp)
            jsonWriter.name(KEY_TYPE).value(tx.type)
            jsonWriter.name(KEY_CATEGORY).value(tx.category)
            jsonWriter.name(KEY_AMOUNT).value(tx.amount.toPlainString())
            jsonWriter.name(KEY_DESCRIPTION).value(tx.description)
            jsonWriter.endObject()
        }
        jsonWriter.endArray()

        // ديون الحبايب والعملاء والعملات الأجنبية (Habayeb Debts)
        jsonWriter.name(KEY_HABAYEB_DEBTS)
        jsonWriter.beginObject()
        jsonWriter.name(KEY_CUSTOMERS)
        jsonWriter.beginArray()
        for (c in data.habayebCustomers) {
            jsonWriter.beginObject()
            jsonWriter.name(KEY_ID).value(c.id)
            jsonWriter.name(KEY_NAME).value(c.name)
            jsonWriter.name(KEY_PHONE).value(c.phone)
            jsonWriter.name(KEY_NOTES).value(c.notes)
            jsonWriter.name(KEY_CREATED_AT).value(c.createdAt)
            jsonWriter.name(KEY_INITIAL_TYPE).value(c.initialType)
            val catLink = data.categoryLinks[c.id]
            if (catLink != null) {
                jsonWriter.name(KEY_CATEGORY_LINK).value(catLink)
            }
            jsonWriter.endObject()
        }
        jsonWriter.endArray()

        jsonWriter.name(KEY_DEBT_TRANSACTIONS)
        jsonWriter.beginArray()
        for (t in data.habayebTransactions) {
            jsonWriter.beginObject()
            jsonWriter.name(KEY_ID).value(t.id)
            jsonWriter.name(KEY_CUSTOMER_ID).value(t.customerId)
            jsonWriter.name(KEY_TYPE).value(t.type)
            jsonWriter.name(KEY_AMOUNT).value(t.amount.toPlainString())
            jsonWriter.name(KEY_TIMESTAMP).value(t.timestamp)
            jsonWriter.name(KEY_DESCRIPTION).value(t.description)
            val cleanLinkedId = t.linkedMainTxId?.trim()?.takeIf { 
                it.isNotBlank() && !it.equals("null", ignoreCase = true) && it != "0" && it != t.id 
            }
            if (cleanLinkedId != null) {
                jsonWriter.name(KEY_LINKED_MAIN_TX_ID).value(cleanLinkedId)
            } else {
                jsonWriter.name(KEY_LINKED_MAIN_TX_ID).nullValue()
            }
            jsonWriter.name(KEY_IS_FOREIGN).value(t.isForeign)
            jsonWriter.name(KEY_CURRENCY_CODE).value(t.currencyCode)
            jsonWriter.name(KEY_FOREIGN_AMOUNT).value(t.foreignAmount.toPlainString())
            jsonWriter.name(KEY_EXCHANGE_RATE).value(t.exchangeRate.toPlainString())
            jsonWriter.name(KEY_IS_RATE_CALCULATED).value(t.isRateCalculated)
            jsonWriter.name(KEY_EQUIVALENT_AMOUNT).value(t.equivalentAmount.toPlainString())
            jsonWriter.name(KEY_BASE_CURRENCY_CODE).value(t.baseCurrencyCode)
            jsonWriter.endObject()
        }
        jsonWriter.endArray()
        jsonWriter.endObject()

        // سلة المهملات والمحذوفات (Deleted Items)
        jsonWriter.name(KEY_DELETED_ITEMS)
        jsonWriter.beginArray()
        for (di in data.deletedItems) {
            jsonWriter.beginObject()
            jsonWriter.name(KEY_ID).value(di.id)
            jsonWriter.name(KEY_SOURCE_SYSTEM).value(di.sourceSystem)
            jsonWriter.name(KEY_ORIGINAL_TABLE_NAME).value(di.originalTableName)
            jsonWriter.name(KEY_JSON_DATA).value(di.jsonData)
            jsonWriter.name(KEY_DELETED_AT).value(di.deletedAt)
            jsonWriter.endObject()
        }
        jsonWriter.endArray()

        // الحسابات المثبتة وترتيب التصنيفات (Pinned Customers & Order)
        if (data.pinnedCustomerIdsByCategory.isNotEmpty()) {
            jsonWriter.name(KEY_PINNED_CUSTOMER_IDS_BY_CATEGORY)
            jsonWriter.beginObject()
            for ((catKey, set) in data.pinnedCustomerIdsByCategory.toSortedMap()) {
                jsonWriter.name(catKey)
                jsonWriter.beginArray()
                set.sorted().forEach { jsonWriter.value(it) }
                jsonWriter.endArray()
            }
            jsonWriter.endObject()
        }

        if (data.categoryOrderList != null) {
            jsonWriter.name(KEY_CATEGORY_ORDER_LIST).value(data.categoryOrderList)
        }
        if (data.closedCustomName != null) {
            jsonWriter.name(KEY_CLOSED_CUSTOM_NAME).value(data.closedCustomName)
        }

        // التصنيفات المخصصة (Custom Categories)
        if (data.customCategories.isNotEmpty()) {
            jsonWriter.name(KEY_CUSTOM_CATEGORIES)
            jsonWriter.beginArray()
            for (cc in data.customCategories) {
                jsonWriter.beginObject()
                jsonWriter.name(KEY_NAME).value(cc.name)
                jsonWriter.name(KEY_TAB_TYPE).value(cc.tabType)
                jsonWriter.name(KEY_ICON_EMOJI).value(cc.iconEmoji)
                jsonWriter.name(KEY_DISPLAY_ORDER).value(cc.displayOrder)
                jsonWriter.name(KEY_IS_SYSTEM_CLOSED).value(cc.isSystemClosed)
                jsonWriter.endObject()
            }
            jsonWriter.endArray()
        }

        jsonWriter.endObject()
        jsonWriter.flush()
    }

    /**
     * [التصدير المتدفق المباشر إلى تيار مخرجات - exportBackupToStream]:
     * يتدفق البيانات مباشرة عبر OutputStream على خيوط Dispatchers.IO.
     *
     * @param data بيانات الحمولة.
     * @param outputStream تيار المخرجات المستهدف.
     */
    suspend fun exportBackupToStream(
        data: BackupPayloadData,
        outputStream: java.io.OutputStream
    ) = withContext(Dispatchers.IO) {
        outputStream.bufferedWriter(Charsets.UTF_8).use { writer ->
            exportBackupToWriter(data, writer)
        }
    }

    /**
     * [التصدير المباشر إلى ملف محلي - exportBackupToFile]:
     * ينشئ ملف النسخة ويكتب البيانات فيه بشكل متدفق وسريع.
     *
     * @param data بيانات الحمولة.
     * @param targetFile الملف المستهدف على القرص.
     */
    suspend fun exportBackupToFile(
        data: BackupPayloadData,
        targetFile: java.io.File
    ) = withContext(Dispatchers.IO) {
        java.io.FileOutputStream(targetFile).use { fos ->
            exportBackupToStream(data, fos)
        }
    }

    /**
     * [تصدير الحمولة كنص JSON موحد - exportBackupToJson]:
     * يحول كائن [BackupPayloadData] إلى سلسلة نصية كاملة بصيغة JSON.
     *
     * @param data كائن الحمولة.
     * @return نص الـ JSON الناتج.
     */
    suspend fun exportBackupToJson(
        data: BackupPayloadData
    ): String = withContext(Dispatchers.IO) {
        val stringWriter = java.io.StringWriter()
        stringWriter.use { sw ->
            exportBackupToWriter(data, sw)
        }
        stringWriter.toString()
    }

    /**
     * [دالة التصدير المتوافقة مع الإصدارات السابقة - exportBackupToJson]:
     * تجمع المعاملات والكيانات والتفضيلات وتصدر نص الـ JSON الشامل.
     */
    suspend fun exportBackupToJson(
        settings: AppSettings,
        commitments: List<FixedCommitment>,
        transactions: List<TransactionDb>,
        habayebCustomers: List<HabayebCustomer> = emptyList(),
        habayebTransactions: List<HabayebTransaction> = emptyList(),
        deletedItems: List<DeletedItemEntity> = emptyList(),
        context: Context? = null
    ): String = withContext(Dispatchers.IO) {
        val extraData = context?.let { BackupExtraDataProvider.fetchExtraBackupData(it, habayebCustomers) }
            ?: BackupExtraData()
        val payloadData = BackupPayloadData(
            settings = settings,
            commitments = commitments,
            transactions = transactions,
            habayebCustomers = habayebCustomers,
            habayebTransactions = habayebTransactions,
            deletedItems = deletedItems,
            customCategories = extraData.customCategories,
            categoryLinks = extraData.categoryLinks,
            pinnedCustomerIdsByCategory = extraData.pinnedMap,
            categoryOrderList = extraData.categoryOrderList,
            closedCustomName = extraData.closedCustomName
        )
        exportBackupToJson(payloadData)
    }

    /**
     * [استخراج قيمة BigDecimal بأمان ودقة - getBigDecimal]:
     * يستخرج القيمة الرقمية بدقة متناهية دون تحويل وسيط إلى أرقام عشرية عائمة لمنع فقدان الهللات.
     *
     * @param obj كائن JSON الحاوي للحقل.
     * @param key اسم الحقل الرقمي.
     * @param fallback القيمة الاحتياطية عند الغياب.
     * @return كائن [BigDecimal] المطابق.
     */
    fun getBigDecimal(obj: JSONObject, key: String, fallback: String = "0"): BigDecimal {
        if (!obj.has(key)) return BigDecimal(fallback)
        val raw = obj.opt(key) ?: return BigDecimal(fallback)
        if (raw is BigDecimal) return raw
        val valueStr = raw.toString().trim()
        if (valueStr.isEmpty() || valueStr.equals("null", ignoreCase = true)) {
            return BigDecimal(fallback)
        }
        val cleaned = BigDecimalConverter.cleanNumberString(valueStr)
        if (cleaned.isEmpty()) return BigDecimal(fallback)
        return try {
            BigDecimal(cleaned)
        } catch (_: Exception) {
            BigDecimal(fallback)
        }
    }

    /**
     * [استيراد وتفكيك حمولة النسخة من JSON - importBackupFromJson]:
     * يفكك نص الـ JSON إلى نماذج الكيانات الأساسية (الإعدادات، الالتزامات، واليومية).
     *
     * @param jsonString نص النسخة الاحتياطية.
     * @param context سياق التطبيق لجلب العملة الافتراضية.
     * @return ثلاثية تحتوي على (الإعدادات، قائمة الالتزامات، قائمة قيود اليومية).
     */
    suspend fun importBackupFromJson(
        jsonString: String,
        context: Context? = null
    ): Triple<AppSettings, List<FixedCommitment>, List<TransactionDb>> = withContext(Dispatchers.IO) {
        val root = validateJsonStructure(jsonString)
        val sourceObj = if (root.has(KEY_MIZAN_AL_DAR_DB)) root.getJSONObject(KEY_MIZAN_AL_DAR_DB) else root

        val settingsObj = sourceObj.optJSONObject(KEY_SETTINGS)
        val fallbackCurrency = context?.getString(com.example.R.string.currency_yer) ?: DatabaseDefaults.DEFAULT_CURRENCY_SYMBOL
        val settings = if (settingsObj != null) {
            AppSettings(
                currencySymbol = settingsObj.optString(KEY_CURRENCY_SYMBOL, fallbackCurrency),
                schoolExpensesEnabled = settingsObj.optBoolean(KEY_SCHOOL_EXPENSES_ENABLED, true),
                themeMode = 0,
                exchangeRatesJson = settingsObj.optString(KEY_EXCHANGE_RATES_JSON, "{}")
            )
        } else {
            AppSettings()
        }

        val commitmentsList = mutableListOf<FixedCommitment>()
        val commitmentsArr = sourceObj.optJSONArray(KEY_FIXED_COMMITMENTS)
        if (commitmentsArr != null) {
            for (i in 0 until commitmentsArr.length()) {
                val obj = commitmentsArr.getJSONObject(i)
                commitmentsList.add(
                    FixedCommitment(
                        name = obj.getString(KEY_NAME),
                        targetAmount = getBigDecimal(obj, KEY_TARGET_AMOUNT),
                        currentProgress = getBigDecimal(obj, KEY_CURRENT_PROGRESS),
                        orderIndex = obj.optInt(KEY_ORDER_INDEX, i)
                    )
                )
            }
        }

        val transactionsList = mutableListOf<TransactionDb>()
        val transactionsArr = sourceObj.optJSONArray(KEY_TRANSACTIONS)
        if (transactionsArr != null) {
            for (i in 0 until transactionsArr.length()) {
                val obj = transactionsArr.getJSONObject(i)
                transactionsList.add(
                    TransactionDb(
                        id = obj.getString(KEY_ID),
                        timestamp = obj.getLong(KEY_TIMESTAMP),
                        type = obj.getString(KEY_TYPE),
                        category = obj.getString(KEY_CATEGORY),
                        amount = getBigDecimal(obj, KEY_AMOUNT),
                        description = obj.optString(KEY_DESCRIPTION, "")
                    )
                )
            }
        }

        Triple(settings, commitmentsList, transactionsList)
    }
}

