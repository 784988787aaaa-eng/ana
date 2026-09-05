/**
 * =====================================================================
 * ملف: محول بيانات سلة المهملات إلى JSON (TrashJsonSerializer.kt)
 * =====================================================================
 * 
 * [الغرض العام والتعليمي من الملف]:
 * يوفر هذا الكائن الأحادي (Singleton Object) آليات تحويل وتغليف الكيانات المحذوفة
 * من جداول قاعدة البيانات المختلفة إلى نصوص مهيكلة بصيغة JSON، لحفظها داخل جدول
 * سلة المهملات العام [DeletedItemEntity] تمهيداً لاسترجاعها لاحقاً دون فقدان أي حقل.
 * 
 * [المسؤوليات المعمارية ونمط التغليف المتعدد]:
 * 1. التغليف الفردي للكيانات (Single Entity Serialization):
 *    - تحويل القيود المحاسبية، الالتزامات المالية، أو بطاقات العملاء إلى كائنات JSON مستقلة.
 * 2. التغليف المركب كحزم ذرية (Compound Bundle Serialization):
 *    - حزمة عميل الحبايب (Habayeb Bundle): تغليف بيانات العميل وتصنيفه وحالات تثبيته مع كامل كشف حساب معاملاته في حمولة واحدة.
 *    - حزمة القيود المجمعة (Transaction Bundle): تغليف مجموعة من قيود اليومية المحذوفة دفعة واحدة مع حساب صافي السيولة المجمعة [totalNet].
 * 3. حفظ بيانات العملات الأجنبية:
 *    - الحفاظ الكامل على حقول أسعار الصرف والمبالغ المعادلة والعملات الأجنبية.
 */
package com.smartledger.aldaftar.data.repository

// ---------------------------------------------------------------------
// استيراد حزم التفضيلات والكيانات ونماذج النطاق ومكتبات JSON
// ---------------------------------------------------------------------
import android.content.SharedPreferences
import com.smartledger.aldaftar.data.local.entities.FixedCommitment
import com.smartledger.aldaftar.data.local.entities.HabayebCustomer
import com.smartledger.aldaftar.data.local.entities.HabayebTransaction
import com.smartledger.aldaftar.data.local.entities.TransactionDb
import com.smartledger.aldaftar.domain.model.TransactionType
import org.json.JSONArray
import org.json.JSONObject
import java.math.BigDecimal

/**
 * [الكائن الأحادي لمحول سلة المهملات - TrashJsonSerializer]:
 * يحول الكائنات إلى تمثيلات JSON آمنة ومكتملة للتخزين في سلة المهملات.
 */
object TrashJsonSerializer {

    /** بادئة مفتاح ربط العميل بالتصنيف في التفضيلات */
    private const val PREF_CAT_LINK_PREFIX = "CAT_LINK_"
    /** بادئة مفتاح العملاء المثبتين في التصنيفات */
    private const val PREF_KEY_PINNED_PREFIX = "KEY_PINNED_IN_"

    /**
     * [تحويل الالتزام المالي الثابت إلى JSON - serializeCommitment]:
     *
     * @param fc كائن الالتزام المالي.
     * @return نص JSON يحمل حقول الالتزام والهدف ونسبة الإنجاز.
     */
    fun serializeCommitment(fc: FixedCommitment): String {
        return JSONObject().apply {
            put("name", fc.name)
            put("targetAmount", fc.targetAmount)
            put("currentProgress", fc.currentProgress)
            put("orderIndex", fc.orderIndex)
        }.toString()
    }

    /**
     * [تحويل حزمة عميل كاملة مع كشف حسابه إلى JSON - serializeHabayebBundle]:
     * يجمع بيانات العميل والتصنيف المربوط وقوائم التثبيت وجميع المعاملات في حزمة واحدة.
     *
     * @param customer بطاقة العميل.
     * @param transactions قائمة معاملات العميل المرتبطة.
     * @param sharedPrefs كائن التفضيلات لاستخراج الروابط وحالات التثبيت.
     * @return نص JSON شامل لحزمة العميل.
     */
    fun serializeHabayebBundle(
        customer: HabayebCustomer,
        transactions: List<HabayebTransaction>,
        sharedPrefs: SharedPreferences
    ): String {
        val categoryLink = sharedPrefs.getString("$PREF_CAT_LINK_PREFIX${customer.id}", null)
        val pinnedCats = JSONArray()
        for ((key, value) in sharedPrefs.all) {
            if (key.startsWith(PREF_KEY_PINNED_PREFIX) && value is Set<*> && value.contains(customer.id)) {
                pinnedCats.put(key.removePrefix(PREF_KEY_PINNED_PREFIX))
            }
        }

        return JSONObject().apply {
            put("customer", JSONObject().apply {
                put("id", customer.id)
                put("name", customer.name)
                put("phone", customer.phone)
                put("notes", customer.notes)
                put("createdAt", customer.createdAt)
                if (categoryLink != null) {
                    put("categoryLink", categoryLink)
                }
                if (pinnedCats.length() > 0) {
                    put("pinnedCategories", pinnedCats)
                }
            })
            val txsArray = JSONArray()
            transactions.forEach { tx ->
                txsArray.put(serializeHabayebTransactionJsonObject(tx))
            }
            put("transactions", txsArray)
            put("totalTransactions", transactions.size)
            put("name", customer.name)
        }.toString()
    }

    /**
     * [تحويل بطاقة عميل فردية إلى JSON - serializeHabayebCustomer]:
     *
     * @param customer كائن العميل.
     * @return نص JSON للعميل.
     */
    fun serializeHabayebCustomer(customer: HabayebCustomer): String {
        return JSONObject().apply {
            put("id", customer.id)
            put("name", customer.name)
            put("phone", customer.phone)
            put("notes", customer.notes)
            put("createdAt", customer.createdAt)
        }.toString()
    }

    /**
     * [تحويل قيد يومية فردي إلى JSON - serializeTransaction]:
     *
     * @param tx قيد اليومية العام.
     * @return نص JSON للقيد.
     */
    fun serializeTransaction(tx: TransactionDb): String {
        return serializeTransactionJsonObject(tx).toString()
    }

    /**
     * [تحويل حزمة قيود يومية مجمعة إلى JSON - serializeTransactionBundle]:
     * يجمع عدة قيود محذوفة دفعة واحدة مع احتساب الصافي الإجمالي.
     *
     * @param transactions قائمة القيود المحذوفة.
     * @param title عنوان الحزمة المعروض للمستخدم.
     * @return نص JSON لحزمة القيود.
     */
    fun serializeTransactionBundle(transactions: List<TransactionDb>, title: String): String {
        return JSONObject().apply {
            val txsArray = JSONArray()
            transactions.forEach { tx ->
                txsArray.put(serializeTransactionJsonObject(tx))
            }
            put("transactions", txsArray)
            put("totalTransactions", transactions.size)
            val totalNet = transactions.fold(BigDecimal.ZERO) { acc, tx ->
                if (tx.type == TransactionType.INCOME.value) acc.add(tx.amount) else acc.subtract(tx.amount)
            }
            put("totalNet", totalNet)
            put("name", title)
        }.toString()
    }

    /**
     * [تحويل قيد ديون حبايب فردي إلى JSON - serializeHabayebTransaction]:
     *
     * @param tx قيد معاملة العميل.
     * @return نص JSON للمعاملة.
     */
    fun serializeHabayebTransaction(tx: HabayebTransaction): String {
        return serializeHabayebTransactionJsonObject(tx).toString()
    }

    /**
     * بناء كائن JSONObject لقيد اليومية العام الداخلي
     */
    private fun serializeTransactionJsonObject(tx: TransactionDb): JSONObject {
        return JSONObject().apply {
            put("id", tx.id)
            put("timestamp", tx.timestamp)
            put("type", tx.type)
            put("category", tx.category)
            put("amount", tx.amount)
            put("description", tx.description)
        }
    }

    /**
     * بناء كائن JSONObject لقيد الحبايب والعملات الأجنبية الداخلي
     */
    private fun serializeHabayebTransactionJsonObject(tx: HabayebTransaction): JSONObject {
        return JSONObject().apply {
            put("id", tx.id)
            put("customerId", tx.customerId)
            put("type", tx.type)
            put("amount", tx.amount)
            put("timestamp", tx.timestamp)
            put("description", tx.description)
            put("linkedMainTxId", tx.linkedMainTxId ?: JSONObject.NULL)
            put("is_foreign", tx.isForeign)
            put("currency_code", tx.currencyCode)
            put("foreign_amount", tx.foreignAmount)
            put("exchange_rate", tx.exchangeRate)
            put("is_rate_calculated", tx.isRateCalculated)
            put("equivalent_amount", tx.equivalentAmount)
            put("base_currency_code", tx.baseCurrencyCode)
        }
    }
}

