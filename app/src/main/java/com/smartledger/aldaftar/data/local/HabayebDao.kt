package com.smartledger.aldaftar.data.local

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.smartledger.aldaftar.data.local.entities.HabayebCustomer
import com.smartledger.aldaftar.data.local.entities.HabayebTransaction
import com.smartledger.aldaftar.data.local.entities.PinnedCustomer
import com.smartledger.aldaftar.domain.model.FinancialPolicy
import com.smartledger.aldaftar.domain.model.TransactionType
import com.smartledger.aldaftar.ui.screens.habayeb.utils.CurrencyConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.math.BigDecimal

@Dao
interface HabayebDao {

    @Query("SELECT * FROM habayeb_customers ORDER BY createdAt DESC")
    fun getAllCustomersFlow(): Flow<List<HabayebCustomer>>

    @Query("SELECT * FROM habayeb_customers")
    suspend fun getAllCustomersDirect(): List<HabayebCustomer>

    @Query("SELECT * FROM habayeb_customers WHERE id = :id")
    suspend fun getCustomerByIdDirect(id: String): HabayebCustomer?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomer(customer: HabayebCustomer)

    @Update
    suspend fun updateCustomer(customer: HabayebCustomer)

    @Query("UPDATE habayeb_customers SET name = :newName WHERE id = :id")
    suspend fun updateCustomerName(id: String, newName: String)

    @Delete
    suspend fun deleteCustomer(customer: HabayebCustomer)

    @Query("DELETE FROM habayeb_customers WHERE id = :id")
    suspend fun deleteCustomerById(id: String)

    @Query("DELETE FROM habayeb_customers")
    suspend fun clearAllCustomers()

    @Query("DELETE FROM pinned_habayeb_customers")
    suspend fun clearAllPins()

    @Query("UPDATE habayeb_customers SET categoryId = :categoryId WHERE id IN (:customerIds)")
    suspend fun assignCustomersToCategory(customerIds: List<String>, categoryId: Int?)

    @Query("SELECT * FROM habayeb_customers WHERE categoryId = :categoryId")
    suspend fun getCustomersByCategoryDirect(categoryId: Int): List<HabayebCustomer>

    @Query("UPDATE habayeb_customers SET categoryId = NULL WHERE categoryId = :categoryId")
    suspend fun clearCategoryFromCustomers(categoryId: Int)

    @Query("SELECT * FROM pinned_habayeb_customers")
    suspend fun getAllPinsDirect(): List<PinnedCustomer>

    @Query("SELECT customerId FROM pinned_habayeb_customers WHERE scopeCategoryId = :scope")
    fun getPinnedCustomerIdsFlow(scope: Int): Flow<List<String>>

    @Query("SELECT customerId FROM pinned_habayeb_customers WHERE scopeCategoryId = :scope")
    suspend fun getPinnedCustomerIdsDirect(scope: Int): List<String>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertPinnedCustomer(pin: PinnedCustomer): Long

    @Query("DELETE FROM pinned_habayeb_customers WHERE scopeCategoryId = :scope AND customerId = :customerId")
    suspend fun deletePinnedCustomer(scope: Int, customerId: String)

    @Query("DELETE FROM pinned_habayeb_customers WHERE customerId = :customerId")
    suspend fun deletePinsForCustomer(customerId: String)

    @Query("SELECT scopeCategoryId FROM pinned_habayeb_customers WHERE customerId = :customerId")
    suspend fun getPinScopeCategoryIdsForCustomer(customerId: String): List<Int>

    @Query("DELETE FROM pinned_habayeb_customers WHERE scopeCategoryId = :scope")
    suspend fun deletePinsForScope(scope: Int)

    @Query("SELECT * FROM habayeb_transactions")
    fun getAllTransactionsFlow(): Flow<List<HabayebTransaction>>

    @Query("SELECT id, customerId, type, amount, timestamp, '' AS description, linkedMainTxId, is_foreign, currency_code, foreign_amount, exchange_rate, is_rate_calculated, equivalent_amount, base_currency_code, snapshot_version, rate_context FROM habayeb_transactions")
    fun getAllTransactionsForBalancesFlow(): Flow<List<HabayebTransaction>>

    fun getAllCustomerBalancesFlow(defaultCurrencySymbol: String): Flow<List<CustomerCurrencyBalance>> {
        val normDefault = FinancialPolicy.canonicalCurrencySymbol(defaultCurrencySymbol)
        return getAllTransactionsForBalancesFlow().map { transactions ->
            aggregateCustomerBalancesBigDecimal(transactions, normDefault)
        }
    }

    private fun aggregateCustomerBalancesBigDecimal(
        transactions: List<HabayebTransaction>,
        defaultCurrencySymbol: String
    ): List<CustomerCurrencyBalance> {
        if (transactions.isEmpty()) return emptyList()

        class BalanceAccumulator(
            val customerId: String,
            val currencyCode: String,
            var netAmount: BigDecimal = BigDecimal.ZERO,
            var netEquivalentAmount: BigDecimal = BigDecimal.ZERO,
            var lastTimestamp: Long = 0L,
            var txCount: Int = 0
        )

        val byCustomer = LinkedHashMap<String, LinkedHashMap<String, BalanceAccumulator>>()
        var totalBuckets = 0

        for (i in transactions.indices) {
            val tx = transactions[i]
            val (rawCurrency, bdAmount) = CurrencyConfig.getTransactionCurrencyAndAmountBigDecimal(tx, defaultCurrencySymbol)
            val txCurrency = FinancialPolicy.canonicalCurrencySymbol(rawCurrency)
            val safeAmount = FinancialPolicy.normalize(bdAmount)
            val safeEquiv = FinancialPolicy.normalize(tx.equivalentAmount)
            val txType = TransactionType.fromValue(tx.type)

            val isPositive = txType == TransactionType.OWED_BY_THEM || txType == TransactionType.PAYMENT_TO_THEM
            val isNegative = txType == TransactionType.PAYMENT_BY_THEM || txType == TransactionType.OWED_TO_THEM

            val signedAmount = when {
                isPositive -> safeAmount
                isNegative -> safeAmount.negate()
                else -> BigDecimal.ZERO
            }
            val signedEquiv = when {
                isPositive -> safeEquiv
                isNegative -> safeEquiv.negate()
                else -> BigDecimal.ZERO
            }

            var currencyMap = byCustomer[tx.customerId]
            if (currencyMap == null) {
                currencyMap = LinkedHashMap(4)
                byCustomer[tx.customerId] = currencyMap
            }

            var acc = currencyMap[txCurrency]
            if (acc == null) {
                acc = BalanceAccumulator(
                    customerId = tx.customerId,
                    currencyCode = txCurrency,
                    lastTimestamp = tx.timestamp
                )
                currencyMap[txCurrency] = acc
                totalBuckets += 1
            }

            acc.netAmount = acc.netAmount.add(signedAmount)
            acc.netEquivalentAmount = acc.netEquivalentAmount.add(signedEquiv)
            if (tx.timestamp > acc.lastTimestamp) {
                acc.lastTimestamp = tx.timestamp
            }
            acc.txCount += 1
        }

        val result = ArrayList<CustomerCurrencyBalance>(totalBuckets)
        for (currencyMap in byCustomer.values) {
            for (acc in currencyMap.values) {
                result.add(
                    CustomerCurrencyBalance(
                        customerId = acc.customerId,
                        currencyCode = acc.currencyCode,
                        netAmount = FinancialPolicy.normalize(acc.netAmount),
                        netEquivalentAmount = FinancialPolicy.normalize(acc.netEquivalentAmount),
                        lastTimestamp = acc.lastTimestamp,
                        txCount = acc.txCount
                    )
                )
            }
        }
        return result
    }

    @Query("SELECT * FROM habayeb_transactions WHERE customerId = :customerId ORDER BY timestamp DESC")
    fun getTransactionsForCustomerFlow(customerId: String): Flow<List<HabayebTransaction>>

    @Query("SELECT * FROM habayeb_transactions WHERE customerId = :customerId ORDER BY timestamp DESC")
    suspend fun getTransactionsForCustomerDirect(customerId: String): List<HabayebTransaction>

    @Query("SELECT * FROM habayeb_transactions")
    suspend fun getAllTransactionsDirect(): List<HabayebTransaction>

    @Query("SELECT * FROM habayeb_transactions WHERE id = :id")
    suspend fun getTransactionById(id: String): HabayebTransaction?

    @Query("SELECT * FROM habayeb_transactions WHERE is_foreign = 1")
    fun getForeignTransactionsFlow(): Flow<List<HabayebTransaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: HabayebTransaction)

    @Delete
    suspend fun deleteTransaction(transaction: HabayebTransaction)

    @Query("DELETE FROM habayeb_transactions WHERE customerId = :customerId")
    suspend fun deleteTransactionsByCustomer(customerId: String)

    @Query("DELETE FROM habayeb_transactions WHERE id = :id")
    suspend fun deleteTransactionById(id: String)

    @Query("DELETE FROM habayeb_transactions")
    suspend fun clearAllTransactions()

    @Query("SELECT COUNT(*) FROM habayeb_transactions")
    fun getHabayebTransactionsCountFlow(): Flow<Int>

    @Query("SELECT COUNT(*) FROM habayeb_transactions")
    suspend fun getHabayebTransactionsCountDirect(): Int

    @Query("SELECT COUNT(*) FROM habayeb_customers")
    fun getHabayebCustomersCountFlow(): Flow<Int>

    @Query("SELECT COUNT(*) FROM habayeb_customers")
    suspend fun getHabayebCustomersCountDirect(): Int

    @Query("""
        SELECT
            (SELECT COUNT(*) FROM habayeb_customers) +
            (SELECT COUNT(*) FROM habayeb_transactions) +
            (SELECT COUNT(*) FROM deleted_items
                WHERE originalTableName IN ('habayeb_customers', 'habayeb_transactions'))
        """)
    fun getBaseOperationsCountFlow(): Flow<Int>

    @Query("""
        SELECT
            (SELECT COUNT(*) FROM habayeb_customers) +
            (SELECT COUNT(*) FROM habayeb_transactions) +
            (SELECT COUNT(*) FROM deleted_items
                WHERE originalTableName IN ('habayeb_customers', 'habayeb_transactions'))
        """)
    suspend fun getBaseOperationsCountDirect(): Int

    @Query("SELECT * FROM habayeb_transactions WHERE customerId = :customerId ORDER BY timestamp DESC")
    fun getTransactionsPagingSourceForCustomer(customerId: String): PagingSource<Int, HabayebTransaction>

    @Query("SELECT * FROM habayeb_transactions WHERE customerId = :customerId ORDER BY timestamp DESC LIMIT :limit OFFSET :offset")
    suspend fun getTransactionsForCustomerPaged(customerId: String, limit: Int, offset: Int): List<HabayebTransaction>

    @Query("SELECT * FROM habayeb_transactions WHERE customerId = :customerId ORDER BY timestamp DESC LIMIT :limit")
    fun getTransactionsForCustomerWithLimitFlow(customerId: String, limit: Int): Flow<List<HabayebTransaction>>

    @Query("""
        UPDATE habayeb_transactions 
        SET type = CASE type 
            WHEN 'OWED_BY_THEM' THEN 'OWED_TO_THEM'
            WHEN 'OWED_TO_THEM' THEN 'OWED_BY_THEM'
            WHEN 'PAYMENT_BY_THEM' THEN 'PAYMENT_TO_THEM'
            WHEN 'PAYMENT_TO_THEM' THEN 'PAYMENT_BY_THEM'
            ELSE type 
        END 
        WHERE customerId = :customerId
    """)
    suspend fun invertTransactionsForCustomer(customerId: String)

    @Transaction
    suspend fun insertCustomerWithOpeningTransaction(customer: HabayebCustomer, transaction: HabayebTransaction?) {
        insertCustomer(customer)
        if (transaction != null) {
            insertTransaction(transaction)
        }
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactionsBatch(transactions: List<HabayebTransaction>)

    @Transaction
    suspend fun deleteCustomerAndTransactions(customerId: String) {
        deleteTransactionsByCustomer(customerId)
        deletePinsForCustomer(customerId)
        deleteCustomerById(customerId)
    }

    @Transaction
    suspend fun deleteCustomersAndTransactionsBatch(customerIds: List<String>) {
        for (id in customerIds) {
            deleteTransactionsByCustomer(id)
            deletePinsForCustomer(id)
            deleteCustomerById(id)
        }
    }
}
