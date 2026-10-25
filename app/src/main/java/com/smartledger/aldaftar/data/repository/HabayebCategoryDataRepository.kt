package com.smartledger.aldaftar.data.repository

import androidx.room.withTransaction
import com.smartledger.aldaftar.data.local.AppDatabase
import com.smartledger.aldaftar.data.local.entities.CustomCategory
import com.smartledger.aldaftar.data.local.entities.DeletedItemEntity
import com.smartledger.aldaftar.data.local.entities.HabayebCustomer
import com.smartledger.aldaftar.data.local.entities.PinnedCustomer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

/** عمليات بيانات التصنيفات المركبة والذرية. */
class HabayebCategoryDataRepository(
    private val database: AppDatabase,
    private val categories: CategoryRepository
) {
    private val dao = database.habayebDao()
    val categoryMapFlow: Flow<Map<String, String>> = combine(dao.getAllCustomersFlow(), categories.customCategoriesFlow) { customers, list ->
        val names = list.associate { it.id to it.name }
        customers.mapNotNull { c -> c.categoryId?.let { id -> names[id]?.let { c.id to it } } }.toMap()
    }

    fun pinnedCustomerIdsFlow(categoryId: Int?): Flow<Set<String>> {
        val targetScope = scopeCategoryId(categoryId)
        return if (targetScope == GLOBAL_SCOPE_CATEGORY_ID) {
            dao.getAllPinnedCustomerIdsFlow().map { it.toSet() }
        } else {
            combine(
                dao.getAllPinnedCustomerIdsFlow(),
                dao.getPinnedCustomerIdsFlow(targetScope)
            ) { allPinned, scopedPinned ->
                (allPinned + scopedPinned).toSet()
            }
        }
    }

    suspend fun getAllPinnedCustomerIdsDirect(): Set<String> =
        dao.getAllPinnedCustomerIdsDirect().toSet()


    suspend fun togglePinCustomer(customerId: String, categoryId: Int?): Boolean = database.withTransaction {
        val customer = dao.getCustomerByIdDirect(customerId) ?: return@withTransaction false
        if (categoryId != null && categories.getAllCustomCategoriesDirect().none { it.id == categoryId }) return@withTransaction false
        val existingScopes = dao.getPinScopeCategoryIdsForCustomer(customerId)
        if (existingScopes.isNotEmpty()) {
            dao.deletePinsForCustomer(customerId)
            true
        } else {
            val allPinnedIds = dao.getAllPinnedCustomerIdsDirect().toSet()
            if (allPinnedIds.size >= MAX_PINNED_COUNT) {
                false
            } else {
                val insertedGlobal = dao.insertPinnedCustomer(PinnedCustomer(GLOBAL_SCOPE_CATEGORY_ID, customerId)) != -1L
                val targetCategoryScope = categoryId ?: customer.categoryId
                if (targetCategoryScope != null && targetCategoryScope != GLOBAL_SCOPE_CATEGORY_ID) {
                    dao.insertPinnedCustomer(PinnedCustomer(scopeCategoryId(targetCategoryScope), customerId))
                }
                insertedGlobal || customerId in dao.getAllPinnedCustomerIdsDirect()
            }
        }
    }

    suspend fun assignCategory(customerIds: List<String>, categoryId: Int?) = database.withTransaction {
        if (customerIds.isEmpty()) return@withTransaction
        if (categoryId != null && categories.getAllCustomCategoriesDirect().none { it.id == categoryId }) return@withTransaction
        dao.assignCustomersToCategory(customerIds, categoryId)
        for (customerId in customerIds) {
            val existingScopes = dao.getPinScopeCategoryIdsForCustomer(customerId)
            if (existingScopes.isNotEmpty()) {
                existingScopes.filter { it != GLOBAL_SCOPE_CATEGORY_ID }.forEach { oldScope ->
                    dao.deletePinnedCustomer(oldScope, customerId)
                }
                dao.insertPinnedCustomer(PinnedCustomer(GLOBAL_SCOPE_CATEGORY_ID, customerId))
                if (categoryId != null && categoryId != GLOBAL_SCOPE_CATEGORY_ID) {
                    dao.insertPinnedCustomer(PinnedCustomer(scopeCategoryId(categoryId), customerId))
                }
            }
        }
    }

    suspend fun deleteCategory(category: CustomCategory, deleteLinkedAccounts: Boolean) = database.withTransaction {
        val linked = dao.getCustomersByCategoryDirect(category.id)
        if (deleteLinkedAccounts) {
            linked.forEach { customer ->
                val transactions = dao.getTransactionsForCustomerDirect(customer.id)
                val pins = dao.getPinScopeCategoryIdsForCustomer(customer.id).toSet()
                database.trashDao().insertDeletedItem(
                    DeletedItemEntity(
                        id = "bundle_${customer.id}",
                        sourceSystem = "الحبايب",
                        originalTableName = "habayeb_bundle",
                        jsonData = TrashJsonSerializer.serializeHabayebBundle(customer, transactions, category.name, pins)
                    )
                )
                dao.deleteTransactionsByCustomer(customer.id)
                dao.deletePinsForCustomer(customer.id)
                dao.deleteCustomerById(customer.id)
            }
        } else {
            val scopedPinnedIds = dao.getPinnedCustomerIdsDirect(scopeCategoryId(category.id))
            scopedPinnedIds.forEach { pinnedId ->
                dao.insertPinnedCustomer(PinnedCustomer(GLOBAL_SCOPE_CATEGORY_ID, pinnedId))
            }
            dao.clearCategoryFromCustomers(category.id)
        }
        dao.deletePinsForScope(scopeCategoryId(category.id))
        categories.deleteCustomCategory(category)
    }

    suspend fun saveCategory(category: CustomCategory) = database.withTransaction {
        categories.saveCustomCategory(category)
    }

    suspend fun createCategory(name: String, tabType: String, icon: String) = database.withTransaction {
        val nextOrder = (categories.getAllCustomCategoriesDirect().maxOfOrNull { it.displayOrder } ?: -1) + 1
        categories.saveCustomCategory(CustomCategory(name = name, tabType = tabType, iconEmoji = icon, displayOrder = nextOrder))
    }

    suspend fun updateOrder(categoryIds: List<Int>) = database.withTransaction {
        categories.updateCustomCategoriesOrder(categoryIds)
    }

    suspend fun customerCategoryId(customerId: String): Int? = dao.getCustomerByIdDirect(customerId)?.categoryId
    suspend fun customerCategoryName(customerId: String): String? {
        val id = customerCategoryId(customerId) ?: return null
        return categories.getAllCustomCategoriesDirect().firstOrNull { it.id == id }?.name
    }

    companion object {
        const val GLOBAL_SCOPE_CATEGORY_ID = 0
        const val MAX_PINNED_COUNT = 100
        fun scopeCategoryId(categoryId: Int?): Int = categoryId ?: GLOBAL_SCOPE_CATEGORY_ID
    }
}
