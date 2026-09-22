package com.smartledger.aldaftar.domain.admin

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AdminLicenseDao {
    @Query("SELECT * FROM admin_issued_licenses ORDER BY issuedAt DESC")
    fun getAllLicenses(): Flow<List<AdminIssuedLicense>>

    @Query("SELECT * FROM admin_issued_licenses WHERE licenseId = :id LIMIT 1")
    suspend fun getLicenseById(id: String): AdminIssuedLicense?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLicense(license: AdminIssuedLicense)

    @Update
    suspend fun updateLicense(license: AdminIssuedLicense)

    @Delete
    suspend fun deleteLicense(license: AdminIssuedLicense)

    @Query("DELETE FROM admin_issued_licenses WHERE licenseId = :id")
    suspend fun deleteById(id: String)
}
