package com.smartledger.aldaftar.domain

import android.content.Context
import com.smartledger.aldaftar.data.repository.LicenseAndTrialManager

/** Compatibility facade for the trial limit and device identity. */
object LicenseManager {
    const val SECURE_LIMIT_VAL = LicenseAndTrialManager.SECURE_LIMIT_VAL

    fun getOrGenerateUnifiedDeviceId(context: Context): String =
        LicenseAndTrialManager.getOrGenerateUnifiedDeviceId(context)
}
