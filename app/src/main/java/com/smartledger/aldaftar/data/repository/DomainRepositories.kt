package com.smartledger.aldaftar.data.repository

import androidx.paging.PagingSource
import androidx.room.withTransaction
import com.smartledger.aldaftar.data.local.*
import com.smartledger.aldaftar.data.local.entities.*
import com.smartledger.aldaftar.domain.model.FinancialPolicy
import com.smartledger.aldaftar.domain.model.TransactionType
import kotlinx.coroutines.flow.Flow
import java.math.BigDecimal

private fun BigDecimal.money() = FinancialPolicy.normalize(this)

class SettingsRepository(private val dao: SettingsDao) {
    val settingsFlow = dao.getSettingsFlow()
    suspend fun getSettingsDirect() = dao.getSettingsDirect()
    suspend fun saveSettings(settings: AppSettings) = dao.insertOrUpdateSettings(settings)
}
class CategoryRepository(private val dao: CustomCategoryDao) {
    val customCategoriesFlow = dao.getAllCustomCategoriesFlow()
    suspend fun saveCustomCategory(v: CustomCategory) = dao.insertCategory(v)
    suspend fun deleteCustomCategory(v: CustomCategory) = dao.deleteCategory(v)
    suspend fun getAllCustomCategoriesDirect() = dao.getAllCustomCategoriesDirect()
    suspend fun clearCustomCategories() = dao.clearAllCustomCategories()
    suspend fun updateCustomCategoriesOrder(categoryIds: List<Int>) {
        val current = dao.getAllCustomCategoriesDirect()
        val order = categoryIds.withIndex().associate { it.value to it.index }
        dao.updateCategories(current.map { c -> c.copy(displayOrder = order[c.id] ?: c.displayOrder) })
    }
}
class HabayebRepository(private val database:AppDatabase, private val dao:HabayebDao) {
    val customersFlow=dao.getAllCustomersFlow(); fun customerBalancesFlow(defaultCurrencySymbol: String) = dao.getAllCustomerBalancesFlow(defaultCurrencySymbol)
    fun getTransactionsForCustomerFlow(id: String) = dao.getTransactionsForCustomerFlow(id)
    fun getTransactionsPagingSourceForCustomer(id: String): PagingSource<Int, HabayebTransaction> {
        return dao.getTransactionsPagingSourceForCustomer(id)
    }
    fun getForeignTransactionsFlow() = dao.getForeignTransactionsFlow(); fun getTransactionsForCustomerWithLimitFlow(id:String,l:Int)=dao.getTransactionsForCustomerWithLimitFlow(id,l)
    fun getHabayebTransactionsCountFlow(): kotlinx.coroutines.flow.Flow<Int> {
        return kotlinx.coroutines.flow.combine(
            dao.getHabayebCustomersCountFlow(),
            dao.getHabayebTransactionsCountFlow(),
            database.trashDao().getAllDeletedItemsFlow()
        ) { customersCount, txsCount, deletedItems ->
            var count = customersCount + txsCount
            for (item in deletedItems) {
                when (item.originalTableName) {
                    "habayeb_customers" -> count += 1
                    "habayeb_transactions" -> count += 1
                    "habayeb_bundle" -> {
                        try {
                            val json = org.json.JSONObject(item.jsonData)
                            val txCount = json.optInt("totalTransactions", json.optJSONArray("transactions")?.length() ?: 0)
                            count += 1 + txCount
                        } catch (e: Exception) {
                            count += 1
                        }
                    }
                }
            }
            count
        }
    }
    suspend fun insertCustomer(v:HabayebCustomer)=dao.insertCustomer(v)
    suspend fun updateCustomer(v:HabayebCustomer)=database.withTransaction { val old=dao.getCustomerByIdDirect(v.id); dao.updateCustomer(v); if(old!=null&&old.initialType!=v.initialType) when(v.initialType){TransactionType.OWED_BY_THEM.value->dao.adaptTransactionsToOwedByThem(v.id);TransactionType.OWED_TO_THEM.value->dao.adaptTransactionsToOwedToThem(v.id)} }
    suspend fun insertCustomerWithOpeningTransaction(c:HabayebCustomer,t:HabayebTransaction?)=dao.insertCustomerWithOpeningTransaction(c,t?.let{it.copy(amount=it.amount.money(),foreignAmount=it.foreignAmount.money(),exchangeRate=it.exchangeRate.money(),equivalentAmount=it.equivalentAmount.money())})
    suspend fun deleteCustomerAndTransactions(id:String)=database.withTransaction { database.recurringConfigDao().deleteForCustomer(id); dao.deleteCustomerAndTransactions(id) }
    suspend fun updateCustomerName(id:String,n:String)=dao.updateCustomerName(id,n)
    suspend fun insertHabayebTransaction(v:HabayebTransaction)=dao.insertTransaction(v.copy(amount=v.amount.money(),foreignAmount=v.foreignAmount.money(),exchangeRate=v.exchangeRate.money(),equivalentAmount=v.equivalentAmount.money()))
    suspend fun deleteHabayebTransaction(v:HabayebTransaction)=dao.deleteTransaction(v); suspend fun deleteHabayebTransactionById(id:String)=dao.deleteTransactionById(id)
    suspend fun getHabayebTransactionById(id:String)=dao.getTransactionById(id); suspend fun getCustomerByIdDirect(id:String)=dao.getCustomerByIdDirect(id)
    suspend fun getAllCustomersDirect()=dao.getAllCustomersDirect(); suspend fun getAllTransactionsDirect()=dao.getAllTransactionsDirect(); suspend fun getTransactionsForCustomerDirect(id:String)=dao.getTransactionsForCustomerDirect(id)
    suspend fun clearAllCustomers()=database.withTransaction { database.recurringConfigDao().clear(); dao.clearAllTransactions(); dao.clearAllPins(); dao.clearAllCustomers() }; suspend fun clearAllTransactions()=dao.clearAllTransactions(); suspend fun getTransactionsForCustomerPaged(id:String,l:Int,o:Int)=dao.getTransactionsForCustomerPaged(id,l,o)
    suspend fun getHabayebTransactionsCountDirect(): Int {
        val customersCount = dao.getHabayebCustomersCountDirect()
        val txsCount = dao.getHabayebTransactionsCountDirect()
        val deletedItems = database.trashDao().getAllDeletedItemsDirect()
        var count = customersCount + txsCount
        for (item in deletedItems) {
            when (item.originalTableName) {
                "habayeb_customers" -> count += 1
                "habayeb_transactions" -> count += 1
                "habayeb_bundle" -> {
                    try {
                        val json = org.json.JSONObject(item.jsonData)
                        val txCount = json.optInt("totalTransactions", json.optJSONArray("transactions")?.length() ?: 0)
                        count += 1 + txCount
                    } catch (e: Exception) {
                        count += 1
                    }
                }
            }
        }
        return count
    }
}
class TrashRepository(private val dao:TrashDao) {
    val deletedItemsFlow=dao.getAllDeletedItemsFlow(); suspend fun getAllDeletedItemsDirect()=dao.getAllDeletedItemsDirect(); suspend fun saveDeletedItem(v:DeletedItemEntity)=dao.insertDeletedItem(v); suspend fun removeDeletedItem(v:DeletedItemEntity)=dao.deleteItem(v); suspend fun removeDeletedItemById(id:String)=dao.deleteItemById(id); suspend fun clearDeletedItems()=dao.clearAllDeletedItems(); suspend fun restoreDeletedItem(v:DeletedItemEntity)=dao.restoreDeletedItem(v); suspend fun restoreSingleTransactionFromBundle(i:String,t:String)=dao.restoreSingleTransactionFromBundle(i,t)
    fun getPagedTrashItems(query: String, tableFilter: String) = dao.getDeletedItemsPagingSource(query, tableFilter)
    suspend fun removeExpiredBefore(threshold: Long): Int { return dao.removeExpiredBefore(threshold) }
    suspend fun softDeleteHabayebCustomerToTrash(v:HabayebCustomer)=saveDeletedItem(DeletedItemEntity("cust_${v.id}", "الحبايب", "habayeb_customers", TrashJsonSerializer.serializeHabayebCustomer(v), searchableText = "${v.name} ${v.phone} ${v.notes}", displayName = v.name, amount = java.math.BigDecimal.ZERO))
    suspend fun softDeleteHabayebTransactionToTrash(v:HabayebTransaction)=saveDeletedItem(DeletedItemEntity(v.id, "الحبايب", "habayeb_transactions", TrashJsonSerializer.serializeHabayebTransaction(v), searchableText = v.description, displayName = v.description, amount = v.amount))
    suspend fun softDeleteHabayebBundleToTrash(c:HabayebCustomer, tx:List<HabayebTransaction>) {
        saveDeletedItem(DeletedItemEntity("bundle_${c.id}","الحبايب","habayeb_bundle",TrashJsonSerializer.serializeHabayebBundle(c,tx,null,emptySet<Int>()), searchableText = "${c.name} ${c.phone} ${c.notes}", displayName = c.name, amount = java.math.BigDecimal.ZERO))
    }

}
class DataMaintenanceRepository(private val database:AppDatabase, private val settings:SettingsDao, private val categories:CustomCategoryDao, private val trash:TrashDao, private val habayeb:HabayebDao) {
    suspend fun deleteAllData()=database.withTransaction { database.recurringConfigDao().clear();habayeb.clearAllTransactions();habayeb.clearAllPins();habayeb.clearAllCustomers();trash.clearAllDeletedItems();categories.clearAllCustomCategories();settings.insertOrUpdateSettings(AppSettings(isFirstLaunch=false)) }
}
