
package com.smartledger.aldaftar.data.local

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

/** واجهة قاعدة بيانات سلة المهملات ومسار الاستعادة الذرية للبيانات المحذوفة. */
@Dao
abstract class TrashDao {

    /** ثوابت تعريف الجداول والحزم التي تحدد نوع البيانات المحذوفة ومسار استعادتها. */
    companion object {
        
        /** اسم جدول قيود اليومية العامة. */
        const val TABLE_TRANSACTIONS = "transactions"
        
        /** اسم جدول معاملات ديون الحبايب. */
        const val TABLE_HABAYEB_TRANSACTIONS = "habayeb_transactions"
        
        /** اسم جدول الالتزامات المالية الثابتة. */
        const val TABLE_FIXED_COMMITMENTS = "fixed_commitments"
        
        /** اسم جدول عملاء ديون الحبايب. */
        const val TABLE_HABAYEB_CUSTOMERS = "habayeb_customers"
        
        /** نوع حزمة عميل الحبايب مع قيوده التابعة. */
        const val BUNDLE_HABAYEB = "habayeb_bundle"
        
        /** نوع حزمة بيانات دفتر الدار. */
        const val BUNDLE_DAR = "dar_bundle"
    }

    /** يعيد عناصر السلة بترتيب حذف ثابت لدعم العرض والاستعادة. */
    @Query("SELECT * FROM deleted_items ORDER BY deletedAt DESC, id ASC")
    abstract fun getAllDeletedItemsFlow(): Flow<List<DeletedItemEntity>>

    /** يجلب عناصر السلة مباشرة لمعالجة التنظيف دون حجب خيط الواجهة. */
    @Query("SELECT * FROM deleted_items ORDER BY deletedAt DESC, id ASC")
    abstract suspend fun getAllDeletedItemsDirect(): List<DeletedItemEntity>

    /** يسترجع عنصراً محذوفاً بواسطة معرفه للتحقق قبل الاستعادة. */
    @Query("SELECT * FROM deleted_items WHERE id = :id LIMIT 1")
    abstract suspend fun getDeletedItemByIdDirect(id: String): DeletedItemEntity?

    /** يحفظ بيانات العنصر المحذوف باستراتيجية الاستبدال المتوافقة مع السلوك السابق. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertDeletedItem(item: DeletedItemEntity)
    /** يحذف العنصر من السلة نهائياً بعد اكتمال الاستعادة أو الحذف المقصود. */
    @Delete
    abstract suspend fun deleteItem(item: DeletedItemEntity)

    /** يحذف عنصراً من السلة بواسطة معرفه الفريد. */
    @Query("DELETE FROM deleted_items WHERE id = :id")
    abstract suspend fun deleteItemById(id: String)

    /** يفرغ السلة بالكامل عند تنفيذ عملية حذف شاملة مصرح بها. */
    @Query("DELETE FROM deleted_items")
    abstract suspend fun clearAllDeletedItems()

    /** يعيد إدراج قيد يومية مستعاد مع الحفاظ على معرفه وقيمته المالية. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertTransaction(tx: TransactionDb)

    /** يعيد إدراج معاملة حبايب مستعادة دون تغيير بياناتها المالية. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertHabayebTransaction(tx: HabayebTransaction)

    /** يعيد إدراج التزام مالي مستعاد ضمن المعاملة الذرية. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertFixedCommitment(commitment: FixedCommitment)

    /** يعيد إنشاء بطاقة العميل عند الحاجة قبل استعادة معاملاته التابعة. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertHabayebCustomer(customer: HabayebCustomer)

    /** يتحقق من وجود العميل قبل ربط المعاملات المستعادة به. */
    @Query("SELECT COUNT(*) FROM habayeb_customers WHERE id = :customerId")
    abstract suspend fun checkCustomerExists(customerId: String): Int

    /** يستعيد معاملة واحدة من حزمة العميل ويحدث الحزمة داخل معاملة ذرية واحدة. */
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

    /** يستعيد العنصر المحذوف كاملاً ثم يحذفه من السلة بعد نجاح جميع عمليات الإدراج. */
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

