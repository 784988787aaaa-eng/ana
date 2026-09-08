package com.smartledger.aldaftar.domain

/**
 * Compatibility state model retained for the existing security UI.
 * No state in this model controls application entitlement in this build.
 */
sealed class LicenseState {
    data class Valid(
        val email: String,
        val deviceId: String,
        val isOfflineVerified: Boolean = false
    ) : LicenseState()

    data class Invalid(val message: String) : LicenseState()
    data class Revoked(val reason: String) : LicenseState()
    data class NetworkUnavailable(
        val lastKnownEmail: String?,
        val fallbackValid: Boolean
    ) : LicenseState()
    object AuthRequired : LicenseState()
    object Unknown : LicenseState()
}
