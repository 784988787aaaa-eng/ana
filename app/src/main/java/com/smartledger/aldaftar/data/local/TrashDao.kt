package com.smartledger.aldaftar.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.smartledger.aldaftar.data.local.entities.DeletedItemEntity
import com.smartledger.aldaftar.data.local.entities.HabayebCustomer
import com.smartledger.aldaftar.data.local.entities.HabayebTransaction
import com.smartledger.aldaftar.data.local.entities.RecurringConfigEntity
import com.smartledger.aldaftar.ui.screens.trash.utils.TrashItemParser
import kotlinx.coroutines.flow.Flow
import org.json.JSONArray
import org.json.JSONObject
import java.math.BigDecimal

@Dao
abstract class TrashDao {

    companion object {
        const val TABLE_HABAYEB_TRANSACTIONS = "habayeb_transactions"
        const val TABLE_HABAYEB_CUSTOMERS = "habayeb_customers"
        const val BUNDLE_HABAYEB = "habayeb_bundle"
    }

    @Query("SELECT * FROM deleted_items WHERE (:query = '' OR searchableText LIKE '%' || :query || '%') AND (:tableFilter = '' OR originalTableName = :tableFilter OR (:tableFilter = 'habayeb_transactions' AND originalTableName = 'habayeb_bundle')) ORDER BY deletedAt DESC")
    abstract fun getDeletedItemsPagingSource(query: String, tableFilter: String): androidx.paging.PagingSource<Int, DeletedItemEntity>

    @Query("SELECT * FROM deleted_items ORDER BY deletedAt DESC")
    abstract fun getAllDeletedItemsFlow(): Flow<List<DeletedItemEntity>>

    @Query("SELECT * FROM deleted_items ORDER BY deletedAt DESC")
    abstract suspend fun getAllDeletedItemsDirect(): List<DeletedItemEntity>

    @Query("SELECT * FROM deleted_items WHERE id = :id LIMIT 1")
    abstract suspend fun getDeletedItemByIdDirect(id: String): DeletedItemEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertDeletedItem(item: DeletedItemEntity)

    @Delete
    abstract suspend fun deleteItem(item: DeletedItemEntity)

    @Query("DELETE FROM deleted_items WHERE id = :id")
    abstract suspend fun deleteItemById(id: String)

    @Query("DELETE FROM deleted_items WHERE deletedAt < :threshold")
    abstract suspend fun removeExpiredBefore(threshold: Long): Int

    @Query("DELETE FROM deleted_items")
    abstract suspend fun clearAllDeletedItems()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertHabayebTransaction(tx: HabayebTransaction)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertHabayebCustomer(customer: HabayebCustomer)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertRecurringConfig(config: RecurringConfigEntity)

    @Query("SELECT COUNT(*) FROM habayeb_customers WHERE id = :customerId")
    abstract suspend fun checkCustomerExists(customerId: String): Int

    @Query("SELECT COUNT(*) FROM custom_categories WHERE id = :categoryId")
    abstract suspend fun checkCategoryExists(categoryId: Int): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract suspend fun insertPinnedCustomer(pin: com.smartledger.aldaftar.data.local.entities.PinnedCustomer)

    @Transaction
    open suspend fun restoreSingleTransactionFromBundle(itemId: String, txId: String) {
        val item = getDeletedItemByIdDirect(itemId) ?: return
        if (item.originalTableName != BUNDLE_HABAYEB) return

        val root = JSONObject(item.jsonData)
        val custData = root.getJSONObject("customer")
        val customerId = custData.getString("id")

        if (checkCustomerExists(customerId) == 0) {
            insertHabayebCustomer(TrashItemParser.parseHabayebCustomer(custData))
        }

        val txsArray = root.getJSONArray("transactions")
        var targetTxObj: JSONObject? = null
        val remainingTxs = JSONArray()

        for (i in 0 until txsArray.length()) {
            val txObj = txsArray.getJSONObject(i)
            if (txObj.getString("id") == txId) targetTxObj = txObj else remainingTxs.put(txObj)
        }

        if (targetTxObj != null) {
            insertHabayebTransaction(TrashItemParser.parseHabayebTransaction(targetTxObj))
            restoreRecurringForTransaction(root, txId)
            if (remainingTxs.length() == 0) {
                deleteItem(item)
            } else {
                root.put("transactions", remainingTxs)
                val updatedItem = item.copy(jsonData = root.toString())
                insertDeletedItem(updatedItem)
            }
        }
    }

    @Transaction
    open suspend fun restoreDeletedItem(item: DeletedItemEntity) {
        val root = JSONObject(item.jsonData)
        when (item.originalTableName) {
            TABLE_HABAYEB_TRANSACTIONS -> {
                val txRoot = root.optJSONObject("transaction") ?: root
                val tx = TrashItemParser.parseHabayebTransaction(txRoot)
                insertHabayebTransaction(tx)
                root.optJSONObject("recurringConfig")?.let { insertRecurringConfig(parseRecurringConfig(it)) }
            }
            TABLE_HABAYEB_CUSTOMERS -> {
                val customer = TrashItemParser.parseHabayebCustomer(root)
                insertHabayebCustomer(customer)
            }
            BUNDLE_HABAYEB -> {
                val custData = root.getJSONObject("customer")
                val parsed = TrashItemParser.parseHabayebCustomer(custData)
                val customer = parsed.copy(categoryId = parsed.categoryId?.takeIf { checkCategoryExists(it) > 0 })
                insertHabayebCustomer(customer)
                if (custData.has("pinnedScopeCategoryIds")) {
                    val scopes = custData.getJSONArray("pinnedScopeCategoryIds")
                    for (i in 0 until scopes.length()) {
                        val scope = scopes.optInt(i)
                        if (scope == 0 || checkCategoryExists(scope) > 0) {
                            insertPinnedCustomer(com.smartledger.aldaftar.data.local.entities.PinnedCustomer(scope, customer.id))
                        }
                    }
                }

                val txsArray = root.getJSONArray("transactions")
                for (i in 0 until txsArray.length()) {
                    insertHabayebTransaction(TrashItemParser.parseHabayebTransaction(txsArray.getJSONObject(i)))
                }
                val recurringArray = root.optJSONArray("recurringConfigs")
                if (recurringArray != null) {
                    for (i in 0 until recurringArray.length()) {
                        insertRecurringConfig(parseRecurringConfig(recurringArray.getJSONObject(i)))
                    }
                }
            }
        }
        deleteItem(item)
    }

    private suspend fun restoreRecurringForTransaction(root: JSONObject, txId: String) {
        val recurringArray = root.optJSONArray("recurringConfigs") ?: return
        for (i in 0 until recurringArray.length()) {
            val config = parseRecurringConfig(recurringArray.getJSONObject(i))
            if (config.originalTxId == txId) insertRecurringConfig(config)
        }
    }

    private fun parseRecurringConfig(obj: JSONObject): RecurringConfigEntity {
        fun intList(key: String): List<Int> {
            val array = obj.optJSONArray(key) ?: return emptyList()
            return buildList { for (i in 0 until array.length()) add(array.optInt(i)) }
        }
        return RecurringConfigEntity(
            id = obj.getString("id"),
            originalTxId = obj.getString("originalTxId"),
            customerId = obj.getString("customerId"),
            customerName = obj.getString("customerName"),
            amount = BigDecimal(obj.getString("amount")),
            type = obj.getString("type"),
            description = obj.getString("description"),
            frequency = obj.getString("frequency"),
            daysOfWeek = intList("daysOfWeek"),
            daysOfMonth = intList("daysOfMonth"),
            timeHour = obj.getInt("timeHour"),
            timeMinute = obj.getInt("timeMinute"),
            startDateMillis = obj.getLong("startDateMillis"),
            endDateMillis = obj.getLong("endDateMillis"),
            lastExecutedTimestamp = obj.getLong("lastExecutedTimestamp"),
            isActive = obj.optBoolean("isActive", true),
            isForeign = obj.optBoolean("isForeign", false),
            currencyCode = obj.optString("currencyCode", "DEFAULT"),
            foreignAmount = BigDecimal(obj.optString("foreignAmount", "0")),
            exchangeRate = BigDecimal(obj.optString("exchangeRate", "0")),
            isRateCalculated = obj.optBoolean("isRateCalculated", false),
            equivalentAmount = BigDecimal(obj.optString("equivalentAmount", "0")),
            baseCurrencyCode = obj.optString("baseCurrencyCode", "DEFAULT"),
            snapshotVersion = obj.optInt("snapshotVersion", 1),
            rateContext = obj.optString("rateContext", "FIXED_SNAPSHOT")
        )
    }
}
