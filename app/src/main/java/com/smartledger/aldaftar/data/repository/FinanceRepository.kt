package com.smartledger.aldaftar.data.repository

import android.content.Context
import android.content.SharedPreferences
import androidx.paging.PagingSource
import androidx.room.withTransaction
import com.smartledger.aldaftar.data.local.AppDatabase
import com.smartledger.aldaftar.data.local.entities.AppSettings
import com.smartledger.aldaftar.data.local.entities.CustomCategory
import com.smartledger.aldaftar.data.local.entities.DeletedItemEntity
import com.smartledger.aldaftar.data.local.entities.FixedCommitment
import com.smartledger.aldaftar.data.local.entities.HabayebCustomer
import com.smartledger.aldaftar.data.local.entities.HabayebTransaction
import com.smartledger.aldaftar.data.local.entities.TransactionDb
import com.smartledger.aldaftar.domain.model.TransactionType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.UUID

class FinanceRepository(
    internal val database: AppDatabase,
    private val context: Context,
    private val preferenceManager: PreferenceManager = PreferenceManager(context)
) {

    companion object {
        private const val TABLE_TRANSACTIONS = "transactions"
        private const val TABLE_FIXED_COMMITMENTS = "fixed_commitments"
        private const val TABLE_HABAYEB_CUSTOMERS = "habayeb_customers"
        private const val TABLE_HABAYEB_TRANSACTIONS = "habayeb_transactions"
        private const val BUNDLE_HABAYEB = "habayeb_bundle"
        private const val BUNDLE_DAR = "dar_bundle"

        const val FINANCIAL_SCALE = 4
        val FINANCIAL_ROUNDING: RoundingMode = RoundingMode.HALF_EVEN
    }

    private val settingsDao = database.settingsDao()
    private val commitmentDao = database.commitmentDao()
    private val transactionDao = database.transactionDao()
    private val customCategoryDao = database.customCategoryDao()
    private val trashDao = database.trashDao()
    private val habayebDao = database.habayebDao()

    private val sourceDar: String by lazy {
        context.getString(com.smartledger.aldaftar.R.string.source_system_dar)
    }
    private val sourceHabayeb: String by lazy {
        context.getString(com.smartledger.aldaftar.R.string.source_system_habayeb)
    }

    /** Returns the legacy shared preference store used by trash metadata. */
    fun getSecurityPreferences(): SharedPreferences = preferenceManager.getSecurityPreferences()
    /** Writes the existing dual-preference contract to both stores. */
    fun writeDualPreference(action: (SharedPreferences.Editor, SharedPreferences.Editor) -> Unit) {
        preferenceManager.writeDualPreference(action)
    }
    private fun BigDecimal.normalized(): BigDecimal =
        setScale(FINANCIAL_SCALE, FINANCIAL_ROUNDING)

    private fun FixedCommitment.normalized(): FixedCommitment = copy(
        targetAmount = targetAmount.normalized(),
        currentProgress = currentProgress.normalized()
    )

    private fun HabayebTransaction.normalized(): HabayebTransaction = copy(
        amount = amount.normalized(),
        foreignAmount = foreignAmount.normalized(),
        exchangeRate = exchangeRate.normalized(),
        equivalentAmount = equivalentAmount.normalized()
    )

    val settingsFlow: Flow<AppSettings?> = settingsDao.getSettingsFlow()
    val commitmentsFlow: Flow<List<FixedCommitment>> = commitmentDao.getAllCommitmentsFlow()
    val transactionsFlow: Flow<List<TransactionDb>> = transactionDao.getAllTransactionsFlow()
    val customCategoriesFlow: Flow<List<CustomCategory>> = customCategoryDao.getAllCustomCategoriesFlow()
    val deletedItemsFlow: Flow<List<DeletedItemEntity>> = trashDao.getAllDeletedItemsFlow()
    val habayebCustomersFlow: Flow<List<HabayebCustomer>> = habayebDao.getAllCustomersFlow()
    val habayebTransactionsFlow: Flow<List<HabayebTransaction>> = habayebDao.getAllTransactionsFlow()
    fun getTransactionsForCustomerFlow(customerId: String): Flow<List<HabayebTransaction>> = 
        habayebDao.getTransactionsForCustomerFlow(customerId)
    fun getTransactionsPagingSourceForCustomer(customerId: String): PagingSource<Int, HabayebTransaction> =
        habayebDao.getTransactionsPagingSourceForCustomer(customerId)
    fun getForeignTransactionsFlow(): Flow<List<HabayebTransaction>> = habayebDao.getForeignTransactionsFlow()
    fun getTransactionsForCustomerWithLimitFlow(customerId: String, limit: Int): Flow<List<HabayebTransaction>> = 
        habayebDao.getTransactionsForCustomerWithLimitFlow(customerId, limit)
    fun getHabayebTransactionsCountFlow(): Flow<Int> = habayebDao.getHabayebTransactionsCountFlow()
    fun getTotalCashFlow(): Flow<BigDecimal> = transactionDao.getTotalCashFlow()
    fun getTransactionsCountFlow(): Flow<Int> = transactionDao.getTransactionsCountFlow()
    suspend fun getSettingsDirect(): AppSettings? = withContext(Dispatchers.IO) {
        settingsDao.getSettingsDirect()
    }
    suspend fun saveSettings(settings: AppSettings) = withContext(Dispatchers.IO) {
        settingsDao.insertOrUpdateSettings(settings)
    }
    suspend fun saveCommitment(commitment: FixedCommitment) = withContext(Dispatchers.IO) {
        val normalized = commitment.normalized()
        commitmentDao.insertCommitment(normalized)
    }
    suspend fun updateCommitments(commitments: List<FixedCommitment>) = withContext(Dispatchers.IO) {
        val normalizedList = commitments.map { it.normalized() }
        commitmentDao.updateCommitments(normalizedList)
    }
    suspend fun deleteCommitment(name: String) = withContext(Dispatchers.IO) {
        commitmentDao.deleteCommitment(name)
    }
    suspend fun clearCommitments() = withContext(Dispatchers.IO) {
        commitmentDao.clearAllCommitments()
    }
    suspend fun getTransactionById(id: String): TransactionDb? = withContext(Dispatchers.IO) {
        transactionDao.getTransactionById(id)
    }
    suspend fun saveTransaction(transaction: TransactionDb) = withContext(Dispatchers.IO) {
        val normalized = transaction.copy(amount = transaction.amount.normalized())
        transactionDao.insertTransaction(normalized)
    }
    suspend fun deleteTransaction(transaction: TransactionDb) = withContext(Dispatchers.IO) {
        transactionDao.deleteTransaction(transaction)
    }
    suspend fun deleteTransactionById(id: String) = withContext(Dispatchers.IO) {
        transactionDao.deleteTransactionById(id)
    }
    suspend fun clearTransactions() = withContext(Dispatchers.IO) {
        transactionDao.clearAllTransactions()
    }
    suspend fun saveCustomCategory(category: CustomCategory) = withContext(Dispatchers.IO) {
        customCategoryDao.insertCategory(category)
    }
    suspend fun deleteCustomCategory(category: CustomCategory) = withContext(Dispatchers.IO) {
        customCategoryDao.deleteCategory(category)
    }
    suspend fun clearCustomCategories() = withContext(Dispatchers.IO) {
        customCategoryDao.clearAllCustomCategories()
    }
    suspend fun updateCustomCategoriesOrder(orderedNames: List<String>) = withContext(Dispatchers.IO) {
        val currentCategories = customCategoryDao.getAllCustomCategoriesFlow().first()
        val orderMap = orderedNames.withIndex().associate { it.value to it.index }
        val updatedCategories = currentCategories.map { category ->
            val key = if (category.isSystemClosed) "CLOSED" else category.name
            val index = orderMap[key] ?: 999
            category.copy(displayOrder = index)
        }
        customCategoryDao.updateCategories(updatedCategories)
    }
    suspend fun getPagedTransactionsDirect(limit: Int, offset: Int): List<TransactionDb> = withContext(Dispatchers.IO) {
        transactionDao.getPagedTransactionsDirect(limit, offset)
    }
    suspend fun getExpensesSumForPeriod(startTimestamp: Long, endTimestamp: Long): BigDecimal = withContext(Dispatchers.IO) {
        transactionDao.getExpensesSumForPeriod(startTimestamp, endTimestamp)
    }
    suspend fun getTransactionsCountDirect(): Int = withContext(Dispatchers.IO) {
        transactionDao.getTransactionsCountDirect()
    }
    suspend fun insertCustomer(customer: HabayebCustomer) = withContext(Dispatchers.IO) {
        habayebDao.insertCustomer(customer)
    }
    // Keep customer and transaction-direction updates atomic.
    suspend fun updateCustomer(customer: HabayebCustomer) = withContext(Dispatchers.IO) {
        database.withTransaction {
            val oldCustomer = habayebDao.getCustomerByIdDirect(customer.id)
            habayebDao.updateCustomer(customer)
            if (oldCustomer != null && oldCustomer.initialType != customer.initialType) {
                when (customer.initialType) {
                    TransactionType.OWED_BY_THEM.value -> habayebDao.adaptTransactionsToOwedByThem(customer.id)
                    TransactionType.OWED_TO_THEM.value -> habayebDao.adaptTransactionsToOwedToThem(customer.id)
                }
            }
        }
    }
    suspend fun insertCustomerWithOpeningTransaction(customer: HabayebCustomer, transaction: HabayebTransaction?) = withContext(Dispatchers.IO) {
        val normalizedTx = transaction?.normalized()
        habayebDao.insertCustomerWithOpeningTransaction(customer, normalizedTx)
    }
    suspend fun deleteCustomerAndTransactions(customerId: String) = withContext(Dispatchers.IO) {
        habayebDao.deleteCustomerAndTransactions(customerId)
    }
    suspend fun updateCustomerName(id: String, newName: String) = withContext(Dispatchers.IO) {
        habayebDao.updateCustomerName(id, newName)
    }
    suspend fun insertHabayebTransaction(transaction: HabayebTransaction) = withContext(Dispatchers.IO) {
        val normalized = transaction.normalized()
        habayebDao.insertTransaction(normalized)
    }
    suspend fun deleteHabayebTransaction(transaction: HabayebTransaction) = withContext(Dispatchers.IO) {
        habayebDao.deleteTransaction(transaction)
    }
    suspend fun deleteHabayebTransactionById(id: String) = withContext(Dispatchers.IO) {
        habayebDao.deleteTransactionById(id)
    }
    suspend fun getHabayebTransactionById(id: String): HabayebTransaction? = withContext(Dispatchers.IO) {
        habayebDao.getTransactionById(id)
    }
    suspend fun getCustomerByIdDirect(id: String): HabayebCustomer? = withContext(Dispatchers.IO) {
        habayebDao.getCustomerByIdDirect(id)
    }
    suspend fun getAllCustomersDirect(): List<HabayebCustomer> = withContext(Dispatchers.IO) {
        habayebDao.getAllCustomersDirect()
    }
    suspend fun getAllTransactionsDirect(): List<HabayebTransaction> = withContext(Dispatchers.IO) {
        habayebDao.getAllTransactionsDirect()
    }
    suspend fun getTransactionsForCustomerDirect(customerId: String): List<HabayebTransaction> = withContext(Dispatchers.IO) {
        habayebDao.getTransactionsForCustomerDirect(customerId)
    }
    suspend fun clearAllCustomers() = withContext(Dispatchers.IO) {
        habayebDao.clearAllCustomers()
    }
    suspend fun clearAllTransactions() = withContext(Dispatchers.IO) {
        habayebDao.clearAllTransactions()
    }
    suspend fun getTransactionsForCustomerPaged(customerId: String, limit: Int, offset: Int): List<HabayebTransaction> = withContext(Dispatchers.IO) {
        habayebDao.getTransactionsForCustomerPaged(customerId, limit, offset)
    }
    suspend fun getHabayebTransactionsCountDirect(): Int = withContext(Dispatchers.IO) {
        habayebDao.getHabayebTransactionsCountDirect()
    }
    suspend fun getAllDeletedItemsDirect(): List<DeletedItemEntity> = withContext(Dispatchers.IO) {
        trashDao.getAllDeletedItemsDirect()
    }
    suspend fun saveDeletedItem(item: DeletedItemEntity) = withContext(Dispatchers.IO) {
        trashDao.insertDeletedItem(item)
    }
    suspend fun removeDeletedItem(item: DeletedItemEntity) = withContext(Dispatchers.IO) {
        trashDao.deleteItem(item)
    }
    suspend fun removeDeletedItemById(id: String) = withContext(Dispatchers.IO) {
        trashDao.deleteItemById(id)
    }
    suspend fun clearDeletedItems() = withContext(Dispatchers.IO) {
        trashDao.clearAllDeletedItems()
    }
    suspend fun softDeleteCommitmentToTrash(fc: FixedCommitment) = withContext(Dispatchers.IO) {
        val jsonData = TrashJsonSerializer.serializeCommitment(fc)
        val trashItem = DeletedItemEntity(
            id = "fc_${fc.name}",
            sourceSystem = sourceDar,
            originalTableName = TABLE_FIXED_COMMITMENTS,
            jsonData = jsonData
        )
        saveDeletedItem(trashItem)
    }
    suspend fun softDeleteHabayebBundleToTrash(customer: HabayebCustomer, transactions: List<HabayebTransaction>) = withContext(Dispatchers.IO) {
        val sharedPrefs = getSecurityPreferences()
        val jsonData = TrashJsonSerializer.serializeHabayebBundle(customer, transactions, sharedPrefs)
        val trashItem = DeletedItemEntity(
            id = "bundle_${customer.id}",
            sourceSystem = sourceHabayeb,
            originalTableName = BUNDLE_HABAYEB,
            jsonData = jsonData
        )
        saveDeletedItem(trashItem)
    }
    suspend fun softDeleteHabayebCustomerToTrash(customer: HabayebCustomer) = withContext(Dispatchers.IO) {
        val jsonData = TrashJsonSerializer.serializeHabayebCustomer(customer)
        val trashItem = DeletedItemEntity(
            id = "cust_${customer.id}",
            sourceSystem = sourceHabayeb,
            originalTableName = TABLE_HABAYEB_CUSTOMERS,
            jsonData = jsonData
        )
        saveDeletedItem(trashItem)
    }
    suspend fun softDeleteTransactionToTrash(tx: TransactionDb) = withContext(Dispatchers.IO) {
        val jsonData = TrashJsonSerializer.serializeTransaction(tx)
        val trashItem = DeletedItemEntity(
            id = tx.id,
            sourceSystem = sourceDar,
            originalTableName = TABLE_TRANSACTIONS,
            jsonData = jsonData
        )
        saveDeletedItem(trashItem)
    }
    suspend fun softDeleteTransactionBundleToTrash(transactions: List<TransactionDb>, title: String) = withContext(Dispatchers.IO) {
        val jsonData = TrashJsonSerializer.serializeTransactionBundle(transactions, title)
        val id = "dar_bundle_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(4)}"
        val trashItem = DeletedItemEntity(
            id = id,
            sourceSystem = sourceDar,
            originalTableName = BUNDLE_DAR,
            jsonData = jsonData
        )
        saveDeletedItem(trashItem)
    }
    suspend fun softDeleteHabayebTransactionToTrash(tx: HabayebTransaction) = withContext(Dispatchers.IO) {
        val jsonData = TrashJsonSerializer.serializeHabayebTransaction(tx)
        val trashItem = DeletedItemEntity(
            id = tx.id,
            sourceSystem = sourceHabayeb,
            originalTableName = TABLE_HABAYEB_TRANSACTIONS,
            jsonData = jsonData
        )
        saveDeletedItem(trashItem)
    }
    // All local data deletion remains one Room transaction.
    suspend fun deleteAllData(): Unit = withContext(Dispatchers.IO) {
        database.withTransaction {
            transactionDao.clearAllTransactions()
            commitmentDao.clearAllCommitments()
            customCategoryDao.clearAllCustomCategories()
            trashDao.clearAllDeletedItems()
            habayebDao.clearAllCustomers()
            habayebDao.clearAllTransactions()
            settingsDao.insertOrUpdateSettings(AppSettings(isFirstLaunch = false))
        }
    }

    suspend fun restoreDeletedItem(item: DeletedItemEntity) = withContext(Dispatchers.IO) {
        trashDao.restoreDeletedItem(item)
    }
    suspend fun restoreSingleTransactionFromBundle(itemId: String, txId: String) = withContext(Dispatchers.IO) {
        trashDao.restoreSingleTransactionFromBundle(itemId, txId)
    }
}

