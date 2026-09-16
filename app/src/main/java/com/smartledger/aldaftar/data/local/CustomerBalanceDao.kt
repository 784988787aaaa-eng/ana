package com.smartledger.aldaftar.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.smartledger.aldaftar.data.local.entities.CustomerBalance
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomerBalanceDao {
    @Query("SELECT * FROM customer_balances")
    fun getAllBalancesFlow(): Flow<List<CustomerBalance>>

    @Query("SELECT * FROM customer_balances WHERE customerId = :customerId")
    suspend fun getBalancesForCustomer(customerId: String): List<CustomerBalance>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBalances(balances: List<CustomerBalance>)

    @Query("DELETE FROM customer_balances WHERE customerId = :customerId")
    suspend fun deleteBalancesForCustomer(customerId: String)

    @Query("DELETE FROM customer_balances")
    suspend fun clearAllBalances()
}
