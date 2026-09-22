package com.smartledger.aldaftar.domain.admin

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class AdminLicenseRepository(context: Context) {
    private val database = AdminLicenseDatabase.getInstance(context)
    private val dao = database.adminLicenseDao()

    fun getAllLicenses(): Flow<List<AdminIssuedLicense>> = dao.getAllLicenses()

    suspend fun findByCode(code: String): AdminIssuedLicense? = withContext(Dispatchers.IO) {
        dao.findByCode(code.trim().uppercase())
    }

    suspend fun issueLicense(
        deviceOrAccountCode: String,
        email: String,
        customerPhone: String = "",
        customerName: String,
        licenseType: String,
        plan: String,
        maxDevices: Int = 1,
        trialDays: Int = 0,
        notes: String = ""
    ): AdminIssuedLicense = withContext(Dispatchers.IO) {
        val issued = AdminLicenseSigner.signLicense(
            deviceOrAccountCode = deviceOrAccountCode,
            email = email,
            customerPhone = customerPhone,
            customerName = customerName,
            licenseType = licenseType,
            plan = plan,
            maxDevices = maxDevices,
            trialDays = trialDays,
            notes = notes
        )
        dao.insertLicense(issued)
        issued
    }

    suspend fun toggleStatus(license: AdminIssuedLicense) = withContext(Dispatchers.IO) {
        dao.updateLicense(license.copy(isActive = !license.isActive))
    }

    suspend fun deleteLicense(license: AdminIssuedLicense) = withContext(Dispatchers.IO) {
        dao.deleteLicense(license)
    }

    suspend fun deleteById(id: String) = withContext(Dispatchers.IO) {
        dao.deleteById(id)
    }
}
