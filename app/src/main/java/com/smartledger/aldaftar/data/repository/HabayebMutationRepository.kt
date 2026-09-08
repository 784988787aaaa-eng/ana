package com.smartledger.aldaftar.data.repository

import androidx.room.withTransaction
import com.smartledger.aldaftar.data.local.AppDatabase
import com.smartledger.aldaftar.data.local.entities.DeletedItemEntity

/** عمليات حذف الحبايب الذرية. */
class HabayebMutationRepository(private val database: AppDatabase) {
    private val habayeb = database.habayebDao()
    private val ledger = database.transactionDao()
    private val trash = database.trashDao()
    private val recurring = database.recurringConfigDao()

    suspend fun deleteCustomerToTrash(customerId: String) = database.withTransaction {
        val customer = habayeb.getCustomerByIdDirect(customerId) ?: return@withTransaction
        val txs = habayeb.getTransactionsForCustomerDirect(customerId)
        val pins = habayeb.getPinScopeCategoryIdsForCustomer(customerId).toSet()
        trash.insertDeletedItem(DeletedItemEntity("bundle_${customer.id}", "الحبايب", "habayeb_bundle", TrashJsonSerializer.serializeHabayebBundle(customer, txs, null, pins)))
        recurring.deleteForCustomer(customerId)
        habayeb.deleteTransactionsByCustomer(customerId)
        habayeb.deletePinsForCustomer(customerId)
        habayeb.deleteCustomerById(customerId)
    }

    suspend fun deleteTransactionToTrash(transactionId: String, saveToTrash: Boolean) = database.withTransaction {
        val tx = habayeb.getTransactionById(transactionId) ?: return@withTransaction
        if (saveToTrash) trash.insertDeletedItem(DeletedItemEntity(tx.id, "الحبايب", "habayeb_transactions", TrashJsonSerializer.serializeHabayebTransaction(tx)))
        recurring.deleteForTransaction(transactionId)
        tx.linkedMainTxId?.let { ledger.deleteTransactionById(it) }
        habayeb.deleteTransactionById(transactionId)
    }
}
