package com.smartledger.aldaftar.domain.license

enum class LicenseType { ACCOUNT, LOCAL }

enum class LicenseStatus { TRIAL, ACTIVE, VERIFICATION_REQUIRED, REVOKED, NOT_ACTIVATED }

data class LicenseSnapshot(
    val type: LicenseType? = null,
    val status: LicenseStatus = LicenseStatus.TRIAL,
    val licenseId: String? = null,
    val accountCode: String? = null,
    val deviceCode: String? = null,
    val lastVerifiedAt: Long? = null,
    val offlineUntil: Long? = null,
    val trialUsed: Int = 0,
    val trialLimit: Int = 100
) {
    val isPaid: Boolean get() = status == LicenseStatus.ACTIVE && type != null
    val isTrialExpired: Boolean get() = status == LicenseStatus.TRIAL && trialUsed >= trialLimit
    val requiresActivation: Boolean get() = !isPaid && (isTrialExpired || status == LicenseStatus.VERIFICATION_REQUIRED || status == LicenseStatus.REVOKED || status == LicenseStatus.NOT_ACTIVATED)
    val canCreate: Boolean get() = isPaid || (status == LicenseStatus.TRIAL && trialUsed < trialLimit)
}

sealed interface LicenseResult {
    data class Success(val snapshot: LicenseSnapshot) : LicenseResult
    data class Failure(val message: String) : LicenseResult
}
