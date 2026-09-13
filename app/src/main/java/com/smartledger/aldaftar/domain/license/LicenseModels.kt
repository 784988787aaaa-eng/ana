package com.smartledger.aldaftar.domain.license

enum class LicenseType { ACCOUNT, LOCAL }

enum class LicensePlan { LIFETIME, TRIAL }

enum class LicenseStatus {
    TRIAL,
    ACTIVE,
    VERIFICATION_REQUIRED,
    REVOKED,
    NOT_ACTIVATED,
    TRIAL_EXPIRED
}

enum class RevocationReason {
    NONE,
    DEVICE_REPLACED,
    ADMIN_REVOKED,
    TRIAL_EXPIRED,
    CLOCK_ROLLED_BACK
}

data class LicenseSnapshot(
    val type: LicenseType? = null,
    val plan: LicensePlan = LicensePlan.LIFETIME,
    val status: LicenseStatus = LicenseStatus.TRIAL,
    val licenseId: String? = null,
    val accountCode: String? = null,
    val email: String? = null,
    val deviceCode: String? = null,
    val lastVerifiedAt: Long? = null,
    val offlineUntil: Long? = null,
    val trialEndsAt: Long? = null,
    val remainingDays: Int? = null,
    val maxDevices: Int = 1,
    val active: Boolean = true,
    val revocationReason: RevocationReason = RevocationReason.NONE,
    val revocationMessage: String? = null,
    val activationRequired: Boolean = false,
    val trialUsed: Int = 0,
    val trialLimit: Int = 100
) {
    val isLifetime: Boolean
        get() = (plan == LicensePlan.LIFETIME || type == LicenseType.LOCAL) && status == LicenseStatus.ACTIVE && active

    val isTrialPlan: Boolean
        get() = plan == LicensePlan.TRIAL

    val isTrialActive: Boolean
        get() = isTrialPlan && status == LicenseStatus.ACTIVE && !isTrialExpired && active

    val isTrialExpired: Boolean
        get() = status == LicenseStatus.TRIAL_EXPIRED ||
                (isTrialPlan && trialEndsAt != null && System.currentTimeMillis() >= trialEndsAt) ||
                (status == LicenseStatus.TRIAL && trialUsed >= trialLimit)

    val isPaid: Boolean
        get() = (status == LicenseStatus.ACTIVE || (isTrialActive)) && active && status != LicenseStatus.REVOKED && status != LicenseStatus.TRIAL_EXPIRED

    val isDeviceReplaced: Boolean
        get() = revocationReason == RevocationReason.DEVICE_REPLACED

    val requiresActivation: Boolean
        get() = activationRequired || (!isPaid && (isTrialExpired || status == LicenseStatus.VERIFICATION_REQUIRED || status == LicenseStatus.REVOKED || status == LicenseStatus.NOT_ACTIVATED))

    val canCreate: Boolean
        get() = isPaid || (status == LicenseStatus.TRIAL && trialUsed < trialLimit && !isTrialExpired)

    fun calculateRemainingDays(): Int? {
        if (!isTrialPlan || trialEndsAt == null) return null
        val now = System.currentTimeMillis()
        if (now >= trialEndsAt) return 0
        return Math.max(0, Math.ceil((trialEndsAt - now).toDouble() / (24.0 * 60 * 60 * 1000)).toInt())
    }
}

sealed interface LicenseResult {
    data class Success(val snapshot: LicenseSnapshot) : LicenseResult
    data class Failure(val message: String) : LicenseResult
}
