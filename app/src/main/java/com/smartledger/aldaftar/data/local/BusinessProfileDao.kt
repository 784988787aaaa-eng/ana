package com.smartledger.aldaftar.data.local
import androidx.room.*
import com.smartledger.aldaftar.data.local.entities.BusinessProfile
import kotlinx.coroutines.flow.Flow
@Dao interface BusinessProfileDao {
 @Query("SELECT * FROM business_profile WHERE id=1") fun profileFlow(): Flow<BusinessProfile?>
 @Query("SELECT * FROM business_profile WHERE id=1") suspend fun get(): BusinessProfile?
 @Query("SELECT * FROM business_profile WHERE id=1") fun getDirect(): BusinessProfile?
 @Insert(onConflict=OnConflictStrategy.REPLACE) suspend fun save(profile: BusinessProfile)
}
