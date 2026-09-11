package com.smartledger.aldaftar.data.account

import com.smartledger.aldaftar.domain.license.LicenseSnapshot
import com.smartledger.aldaftar.domain.license.LicenseType

enum class AccountProvider {
    NONE,
    GOOGLE
}

data class UnifiedAccountSession(
    val isSignedIn: Boolean = false,
    val email: String? = null,
    val displayName: String? = null,
    val photoUrl: String? = null,
    val provider: AccountProvider = AccountProvider.NONE,
    val accountCode: String? = null,
    val isCloudConnected: Boolean = false,
    val licenseSnapshot: LicenseSnapshot = LicenseSnapshot()
) {
    val isLicensePaid: Boolean get() = licenseSnapshot.isPaid
    val isLocalLicense: Boolean get() = licenseSnapshot.type == LicenseType.LOCAL && licenseSnapshot.isPaid
    val isAccountLicense: Boolean get() = licenseSnapshot.type == LicenseType.ACCOUNT && licenseSnapshot.isPaid
}
