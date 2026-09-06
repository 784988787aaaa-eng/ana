package com.smartledger.aldaftar.data.repository

import android.content.Context
import com.smartledger.aldaftar.domain.AppSecurityManager
import com.smartledger.aldaftar.domain.LicenseLeaseVerifier
import java.util.UUID

/** Local license state is only a cache of a server-signed lease. */
class LicenseAndTrialManager(context: Context) {
    private val appContext = context.applicationContext
    private val securityManager = AppSecurityManager.getInstance(appContext)

    companion object {
        const val SECURE_LIMIT_VAL = 100

        fun getOrGenerateUnifiedDeviceId(context: Context): String {
            val security = AppSecurityManager.getInstance(context.applicationContext)
            var deviceId = security.getUnifiedDeviceId()
            if (deviceId.isNotBlank()) return deviceId

            val randomPart = UUID.randomUUID().toString().replace("-", "").take(16).uppercase()
            deviceId = "MZ-$randomPart"
            security.setUnifiedDeviceId(deviceId)
            return deviceId
        }
    }

    fun isAppActivated(): Boolean {
        val email = securityManager.getActivatedEmail()
        val deviceId = getOrGenerateUnifiedDeviceId(appContext)
        val leaseJson = securityManager.getLicenseLeaseJson()
        val signature = securityManager.getLicenseLeaseSignature()
        if (email.isBlank() || leaseJson.isBlank() || signature.isBlank()) return false

        val valid = LicenseLeaseVerifier.isValidForDevice(
            payloadJson = leaseJson,
            signatureBase64 = signature,
            expectedEmail = email,
            expectedDeviceId = deviceId
        )
        if (!valid) securityManager.clearActivationData()
        return valid
    }

    fun isTrialExpiredDirect(realTotalTransactionsCount: Int): Boolean =
        !isAppActivated() && realTotalTransactionsCount >= SECURE_LIMIT_VAL

    fun saveEmailActivation(
        email: String,
        deviceId: String,
        sessionId: String,
        leaseJson: String,
        signature: String
    ) {
        securityManager.saveLicenseLease(email, deviceId, sessionId, leaseJson, signature)
    }

    fun clearLocalActivation() = securityManager.clearActivationData()

    fun getActivatedEmail(): String = securityManager.getActivatedEmail()
    fun getDeviceId(): String = getOrGenerateUnifiedDeviceId(appContext)
    fun getLicenseSessionId(): String = securityManager.getLicenseSessionId()
}
