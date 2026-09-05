/**
 * =====================================================================
 * ملف: كائن الوصول لبيانات واستعادة سلة المهملات (TrashDao.kt)
 * =====================================================================
 * 
 * [الغرض العام والتعليمي من الملف]:
 * يمثل هذا الملف صمام الأمان واسترجاع البيانات المحذوفة (Recycle Bin & Disaster Recovery).
 * يعتمد على نمط الحذف المرن (Soft Delete via JSON Bundling)، حيث يتم تحويل الكيانات
 * أو مجموعات الحسابات المحذوفة إلى نصوص JSON وحفظها في جدول `deleted_items`.
 * 
 * [المسؤوليات المعمارية والتقنية]:
 * 1. التخزين المرن (JSON Serialization): حفظ أي نوع من السجلات المحذوفة (معاملات، عملاء، التزامات، أو حزم كاملة)
 *    في هيكل مرن دون الحاجة لتغيير جداول قاعدة البيانات عند كل تعديل.
 * 2. الاستعادة الذرية الشاملة [@Transaction]: تضمن دالة [restoreDeletedItem] فك تشفير كائن JSON
 *    وإعادة إدراج السجلات في جداولها الأصلية وحذفها من سلة المهملات في معاملة واحدة لا تقبل التجزئة.
 * 3. استعادة حركة مفردة من حزمة عميل [restoreSingleTransactionFromBundle]: تتيح للمستخدم استعادة قيد واحد
 *    من حزمة عميل محذوف مع ضمان وجود العميل في قاعدة البيانات وتحديث ما تبقى من الحزمة داخل السلة.
 */
package com.smartledger.aldaftar.data.local

// ---------------------------------------------------------------------
// استيراد حزم قاعدة البيانات Room والكيانات ومحلل كائنات JSON والتدفقات
// ---------------------------------------------------------------------
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.smartledger.aldaftar.data.local.entities.DeletedItemEntity
import com.smartledger.aldaftar.data.local.entities.FixedCommitment
import com.smartledger.aldaftar.data.local.entities.HabayebCustomer
import com.smartledger.aldaftar.data.local.entities.HabayebTransaction
import com.smartledger.aldaftar.data.local.entities.TransactionDb
import com.smartledger.aldaftar.ui.screens.trash.utils.TrashItemParser
import kotlinx.coroutines.flow.Flow
import org.json.JSONArray
import org.json.JSONObject

/**
 * [فئة الوصول لبيانات سلة المهملات - TrashDao]:
 * فئة مجردة تجمع بين استعلامات Room المجردة والدوال التنفيذية المعقدة لفك الحزم واستعادتها.
 */
@Dao
abstract class TrashDao {

    /**
     * [ثوابت أسماء الجداول وأنواع الحزم المحذوفة]:
     * تُستخدم لتمييز نوع الكيان المحفوظ داخل نص JSON في سلة المهملات.
     */
    companion object {
        /** جدول معاملات اليومية العامة */
        const val TABLE_TRANSACTIONS = "transactions"
        /** جدول معاملات ديون الحبايب */
        const val TABLE_HABAYEB_TRANSACTIONS = "habayeb_transactions"
        /** جدول الالتزامات والأقساط */
        const val TABLE_FIXED_COMMITMENTS = "fixed_commitments"
        /** جدول عملاء ديون الحبايب */
        const val TABLE_HABAYEB_CUSTOMERS = "habayeb_customers"
        /** حزمة عميل الحبايب مع كافة كشف حسابه */
        const val BUNDLE_HABAYEB = "habayeb_bundle"
        /** حزمة ميزان الدار المجمعة */
        const val BUNDLE_DAR = "dar_bundle"
    }

    // =================================================================
    // 1. استعلامات جدول سلة المهملات (Deleted Items Queries)
    // =================================================================

    /**
     * [استعلام جلب عناصر السلة كتدفق حي - getAllDeletedItemsFlow]:
     * يستعلم عن جميع العناصر المحذوفة مرتبة تنازلياً حسب توقيت الحذف [deletedAt].
     */
    @Query("SELECT * FROM deleted_items ORDER BY deletedAt DESC")
    abstract fun getAllDeletedItemsFlow(): Flow<List<DeletedItemEntity>>

    /**
     * [استعلام جلب عناصر السلة كقائمة مباشرة - getAllDeletedItemsDirect]:
     * دالة معلقة تجلب قائمة بالعناصر المحذوفة لمعالج التنظيف التلقائي القديم.
     */
    @Query("SELECT * FROM deleted_items ORDER BY deletedAt DESC")
    abstract suspend fun getAllDeletedItemsDirect(): List<DeletedItemEntity>

    /**
     * [استعلام جلب عنصر سلة بالمعرف - getDeletedItemByIdDirect]:
     * يسترجع كائن العنصر المحذوف من السلة باستخدام معرفه الفريد [id].
     */
    @Query("SELECT * FROM deleted_items WHERE id = :id LIMIT 1")
    abstract suspend fun getDeletedItemByIdDirect(id: String): DeletedItemEntity?

    /**
     * [دالة إدراج عنصر في سلة المهملات - insertDeletedItem]:
     * تحفظ الكيان المحذوف في جدول السلة مع بياناته بصيغة JSON وتوقيت الحذف.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertDeletedItem(item: DeletedItemEntity)

    /**
     * [دالة حذف عنصر نهائياً من السلة - deleteItem]:
     * تحذف سجل المهملات بشكل دائم.
     */
    @Delete
    abstract suspend fun deleteItem(item: DeletedItemEntity)

    /**
     * [دالة حذف عنصر من السلة بواسطة المعرف - deleteItemById]:
     * تحذف العنصر المحذوف نهائياً باستخدام معرفه.
     */
    @Query("DELETE FROM deleted_items WHERE id = :id")
    abstract suspend fun deleteItemById(id: String)

    /**
     * [دالة تفريغ سلة المهملات بالكامل - clearAllDeletedItems]:
     * تحذف كافة العناصر المتواجدة في سلة المهملات نهائياً.
     */
    @Query("DELETE FROM deleted_items")
    abstract suspend fun clearAllDeletedItems()

    // =================================================================
    // 2. دوال إعادة الإدراج للجداول الأصلية (Target Restoration Inserts)
    // =================================================================

    /** دالة إدراج حركة يومية مستعادة */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertTransaction(tx: TransactionDb)

    /** دالة إدراج حركة حبايب مستعادة */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertHabayebTransaction(tx: HabayebTransaction)

    /** دالة إدراج التزام مالي مستعاد */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertFixedCommitment(commitment: FixedCommitment)

    /** دالة إدراج بطاقة عميل مستعادة */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertHabayebCustomer(customer: HabayebCustomer)

    /** استعلام التحقق من وجود العميل في قاعدة البيانات قبل إعادة ربط معاملاته */
    @Query("SELECT COUNT(*) FROM habayeb_customers WHERE id = :customerId")
    abstract suspend fun checkCustomerExists(customerId: String): Int

    // =================================================================
    // 3. الدوال التنفيذية للاستعادة الذرية (@Transaction Restoration Logic)
    // =================================================================

    /**
     * [استعادة معاملة مفردة من حزمة عميل محذوف - restoreSingleTransactionFromBundle]:
     * دالة ذرية متقدمة تتيح للمستخدم استعادة قيد مالي محدد من داخل حزمة عميل محذوف:
     * 1. تتحقق من وجود بطاقة العميل في قاعدة البيانات، وإذا لم تكن موجودة تعيد إنشاءها أولاً.
     * 2. تستخرج المعاملة المطلوبة وتدرجها في جدول المعاملات.
     * 3. إذا لم يتبق معاملات أخرى في الحزمة تحذف عنصر السلة كاملاً، وإلا تحدث نص JSON المتبقي.
     */
    @Transaction
    open suspend fun restoreSingleTransactionFromBundle(itemId: String, txId: String) {
        val item = getDeletedItemByIdDirect(itemId) ?: return
        if (item.originalTableName != BUNDLE_HABAYEB) return
        
        val root = JSONObject(item.jsonData)
        val custData = root.getJSONObject("customer")
        val customerId = custData.getString("id")
        
        val customerExists = checkCustomerExists(customerId) > 0
        if (!customerExists) {
            val customer = TrashItemParser.parseHabayebCustomer(custData)
            insertHabayebCustomer(customer)
        }
        
        val txsArray = root.getJSONArray("transactions")
        var targetTxObj: JSONObject? = null
        val remainingTxs = JSONArray()
        
        for (i in 0 until txsArray.length()) {
            val txObj = txsArray.getJSONObject(i)
            if (txObj.getString("id") == txId) {
                targetTxObj = txObj
            } else {
                remainingTxs.put(txObj)
            }
        }
        
        if (targetTxObj != null) {
            val tx = TrashItemParser.parseHabayebTransaction(targetTxObj)
            insertHabayebTransaction(tx)
            
            if (remainingTxs.length() == 0) {
                deleteItem(item)
            } else {
                root.put("transactions", remainingTxs)
                val updatedItem = item.copy(jsonData = root.toString())
                insertDeletedItem(updatedItem)
            }
        }
    }

    /**
     * [استعادة عنصر محذوف بالكامل - restoreDeletedItem]:
     * عملية ذرية [@Transaction] شاملة تحلل نص JSON للعنصر المحذوف وتعيد بناء الكائنات
     * وإدراجها في جداولها المخصصة حسب نوع الجدول الأصلي [originalTableName]، ثم تمسح عنصر السلة.
     */
    @Transaction
    open suspend fun restoreDeletedItem(item: DeletedItemEntity) {
        val root = JSONObject(item.jsonData)
        when (item.originalTableName) {
            TABLE_TRANSACTIONS -> {
                val tx = TrashItemParser.parseTransactionDb(root)
                insertTransaction(tx)
            }
            TABLE_HABAYEB_TRANSACTIONS -> {
                val tx = TrashItemParser.parseHabayebTransaction(root)
                insertHabayebTransaction(tx)
            }
            TABLE_FIXED_COMMITMENTS -> {
                val fc = TrashItemParser.parseFixedCommitment(root)
                insertFixedCommitment(fc)
            }
            TABLE_HABAYEB_CUSTOMERS -> {
                val customer = TrashItemParser.parseHabayebCustomer(root)
                insertHabayebCustomer(customer)
            }
            BUNDLE_HABAYEB -> {
                val custData = root.getJSONObject("customer")
                val customer = TrashItemParser.parseHabayebCustomer(custData)
                insertHabayebCustomer(customer)

                val txsArray = root.getJSONArray("transactions")
                for (i in 0 until txsArray.length()) {
                    val txObj = txsArray.getJSONObject(i)
                    val tx = TrashItemParser.parseHabayebTransaction(txObj)
                    insertHabayebTransaction(tx)
                }
            }
            BUNDLE_DAR -> {
                if (root.has("commitments")) {
                    val fcsArray = root.getJSONArray("commitments")
                    for (i in 0 until fcsArray.length()) {
                        val fcObj = fcsArray.getJSONObject(i)
                        val fc = TrashItemParser.parseFixedCommitment(fcObj)
                        insertFixedCommitment(fc)
                    }
                }
                
                if (root.has("transactions")) {
                    val txsArray = root.getJSONArray("transactions")
                    for (i in 0 until txsArray.length()) {
                        val txObj = txsArray.getJSONObject(i)
                        val tx = TrashItemParser.parseTransactionDb(txObj)
                        insertTransaction(tx)
                    }
                }
            }
        }
        deleteItem(item)
    }
}


