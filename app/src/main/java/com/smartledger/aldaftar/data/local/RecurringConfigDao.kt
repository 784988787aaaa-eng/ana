package com.smartledger.aldaftar.data.local
import androidx.room.*
import com.smartledger.aldaftar.data.local.entities.RecurringConfigEntity
import kotlinx.coroutines.flow.Flow
@Dao interface RecurringConfigDao {
 @Query("SELECT * FROM recurring_configs ORDER BY startDateMillis") fun allFlow(): Flow<List<RecurringConfigEntity>>
 @Query("SELECT * FROM recurring_configs") suspend fun all(): List<RecurringConfigEntity>
 @Query("SELECT * FROM recurring_configs WHERE originalTxId=:transactionId LIMIT 1") suspend fun byOriginalTransaction(transactionId:String): RecurringConfigEntity?
 @Insert(onConflict=OnConflictStrategy.REPLACE) suspend fun save(config:RecurringConfigEntity)
 @Query("DELETE FROM recurring_configs WHERE id=:id") suspend fun delete(id:String)
 @Query("DELETE FROM recurring_configs WHERE originalTxId=:id") suspend fun deleteForTransaction(id:String)
 @Query("DELETE FROM recurring_configs WHERE customerId=:id") suspend fun deleteForCustomer(id:String)
 @Query("DELETE FROM recurring_configs") suspend fun clear()
}
