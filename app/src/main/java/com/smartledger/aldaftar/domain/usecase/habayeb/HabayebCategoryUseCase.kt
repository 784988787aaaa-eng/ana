package com.smartledger.aldaftar.domain.usecase.habayeb

import com.smartledger.aldaftar.data.local.entities.CustomCategory
import com.smartledger.aldaftar.data.repository.CategoryRepository
import com.smartledger.aldaftar.data.repository.HabayebCategoryDataRepository
import kotlinx.coroutines.flow.Flow

/** حالات أعمال التصنيفات وعلاقات العملاء. */
class HabayebCategoryUseCase(
    private val categories: CategoryRepository,
    private val data: HabayebCategoryDataRepository
) {
    suspend fun ensureClosedCategoryExists() {
        if (categories.getAllCustomCategoriesDirect().none { it.isSystemClosed }) {
            data.saveCategory(CustomCategory(name = "مغلق", tabType = TAB_TYPE_HABAYEB, iconEmoji = DEFAULT_EMOJI, displayOrder = 0, isSystemClosed = true))
        }
    }

    suspend fun getCustomerCategory(customerId: String): String? = data.customerCategoryName(customerId)
    val categoryMapFlow: Flow<Map<String, String>> = data.categoryMapFlow

    fun pinnedCustomerIdsFlow(categoryId: Int?): Flow<Set<String>> = data.pinnedCustomerIdsFlow(categoryId)
    suspend fun togglePinCustomer(customerId: String, categoryId: Int?): Boolean = data.togglePinCustomer(customerId, categoryId)
    suspend fun assignCategoryToCustomers(customerIds: List<String>, categoryId: Int?) = data.assignCategory(customerIds, categoryId)

    suspend fun renameClosedCategory(newName: String) {
        val current = categories.getAllCustomCategoriesDirect().find { it.isSystemClosed }
        data.saveCategory((current ?: CustomCategory(name = newName, tabType = TAB_TYPE_HABAYEB, iconEmoji = DEFAULT_EMOJI, displayOrder = 0, isSystemClosed = true)).copy(name = newName))
    }
    suspend fun saveCustomCategory(name: String) = data.createCategory(name, TAB_TYPE_HABAYEB, "")
    suspend fun renameCustomCategory(category: CustomCategory, newName: String) {
        if (category.name != newName) data.saveCategory(category.copy(name = newName))
    }
    suspend fun deleteCustomCategoryWithChoice(category: CustomCategory, deleteLinkedAccounts: Boolean) = data.deleteCategory(category, deleteLinkedAccounts)
    suspend fun moveCategoryLeft(currentOrder: List<Int>, categoryId: Int) = move(currentOrder, categoryId, -1)
    suspend fun moveCategoryRight(currentOrder: List<Int>, categoryId: Int) = move(currentOrder, categoryId, 1)
    private suspend fun move(order: List<Int>, id: Int, delta: Int) {
        val index = order.indexOf(id); val target = index + delta
        if (index !in order.indices || target !in order.indices) return
        val next = order.toMutableList(); next[index] = next[target]; next[target] = id
        data.updateOrder(next)
    }
    suspend fun reorderCategories(newList: List<Int>) = data.updateOrder(newList)
    companion object { private const val TAB_TYPE_HABAYEB = "HABAYEB"; private const val DEFAULT_EMOJI = "📁"; const val MAX_PINNED_COUNT = HabayebCategoryDataRepository.MAX_PINNED_COUNT }
}
