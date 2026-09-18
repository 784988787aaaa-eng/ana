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
import kotlinx.coroutines.flow.Flow

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

    @Query("""
        SELECT
            customerId,
            CASE
                WHEN is_rate_calculated = 1 THEN
                    CASE
                        WHEN base_currency_code IS NULL OR base_currency_code = '' OR base_currency_code = 'DEFAULT'
                            THEN :defaultCurrencySymbol
                        ELSE base_currency_code
                    END
                WHEN currency_code IS NULL OR currency_code = '' OR currency_code = 'DEFAULT'
                    THEN :defaultCurrencySymbol
                ELSE currency_code
            END AS currencyCode,
            SUM(
                CASE
                    WHEN type = 'OWED_BY_THEM' OR type = 'PAYMENT_TO_THEM' THEN
                        CASE
                            WHEN is_rate_calculated = 1 THEN equivalent_amount
                            WHEN currency_code IS NULL OR currency_code = '' OR currency_code = 'DEFAULT'
                                THEN amount
                            WHEN foreign_amount > 0 THEN foreign_amount
                            ELSE amount
                        END
                    WHEN type = 'OWED_TO_THEM' OR type = 'PAYMENT_BY_THEM' THEN
                        -CASE
                            WHEN is_rate_calculated = 1 THEN equivalent_amount
                            WHEN currency_code IS NULL OR currency_code = '' OR currency_code = 'DEFAULT'
                                THEN amount
                            WHEN foreign_amount > 0 THEN foreign_amount
                            ELSE amount
                        END
                    ELSE 0
                END
            ) AS netAmount,
            SUM(
                CASE
                    WHEN type = 'OWED_BY_THEM' OR type = 'PAYMENT_TO_THEM' THEN equivalent_amount
                    WHEN type = 'OWED_TO_THEM' OR type = 'PAYMENT_BY_THEM' THEN -equivalent_amount
                    ELSE 0
                END
            ) AS netEquivalentAmount,
            MAX(timestamp) AS lastTimestamp,
            COUNT(*) AS txCount
        FROM habayeb_transactions
        GROUP BY customerId,
            CASE
                WHEN is_rate_calculated = 1 THEN
                    CASE
                        WHEN base_currency_code IS NULL OR base_currency_code = '' OR base_currency_code = 'DEFAULT'
                            THEN :defaultCurrencySymbol
                        ELSE base_currency_code
                    END
                WHEN currency_code IS NULL OR currency_code = '' OR currency_code = 'DEFAULT'
                    THEN :defaultCurrencySymbol
                ELSE currency_code
            END
    """)
    fun getAllCustomerBalancesFlow(defaultCurrencySymbol: String): Flow<List<CustomerCurrencyBalance>>

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
            WHEN 'OWED_TO_THEM' THEN 'OWED_BY_THEM' 
            WHEN 'PAYMENT_TO_THEM' THEN 'PAYMENT_BY_THEM' 
            ELSE type 
        END 
        WHERE customerId = :customerId AND type IN ('OWED_TO_THEM', 'PAYMENT_TO_THEM')
    """)
    suspend fun adaptTransactionsToOwedByThem(customerId: String)

    @Query("""
        UPDATE habayeb_transactions 
        SET type = CASE type 
            WHEN 'OWED_BY_THEM' THEN 'OWED_TO_THEM' 
            WHEN 'PAYMENT_BY_THEM' THEN 'PAYMENT_TO_THEM' 
            ELSE type 
        END 
        WHERE customerId = :customerId AND type IN ('OWED_BY_THEM', 'PAYMENT_BY_THEM')
    """)
    suspend fun adaptTransactionsToOwedToThem(customerId: String)

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

