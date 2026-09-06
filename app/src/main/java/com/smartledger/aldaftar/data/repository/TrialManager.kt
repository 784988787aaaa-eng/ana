package com.smartledger.aldaftar.data.repository

import android.content.Context

/** Compatibility facade for the free transaction quota and device identity. */
class TrialManager(context: Context) {
    private val licenseManager = LicenseAndTrialManager(context)

    companion object {
        const val SECURE_LIMIT_VAL = LicenseAndTrialManager.SECURE_LIMIT_VAL

        fun getOrGenerateUnifiedDeviceId(context: Context): String =
            LicenseAndTrialManager.getOrGenerateUnifiedDeviceId(context)
    }

    fun isAppActivated(): Boolean = licenseManager.isAppActivated()

    fun isTrialExpiredDirect(realTotalTransactionsCount: Int): Boolean =
        licenseManager.isTrialExpiredDirect(realTotalTransactionsCount)

    fun clearLocalActivation() = licenseManager.clearLocalActivation()
    fun getActivatedEmail(): String = licenseManager.getActivatedEmail()
    fun getDeviceId(): String = licenseManager.getDeviceId()
}
