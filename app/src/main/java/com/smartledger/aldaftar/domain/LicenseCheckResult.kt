package com.smartledger.aldaftar.domain

/**
 * UI compatibility result retained solely so the existing activation window
 * can remain unchanged while entitlement enforcement is disabled.
 */
sealed class LicenseCheckResult {
    data class Success(
        val email: String,
        val deviceId: String,
        val isTransferred: Boolean = false
    ) : LicenseCheckResult()

    data class NotLicensed(val email: String, val message: String) : LicenseCheckResult()
    data class NetworkOutage(val message: String) : LicenseCheckResult()
    data class Error(val message: String) : LicenseCheckResult()
}
