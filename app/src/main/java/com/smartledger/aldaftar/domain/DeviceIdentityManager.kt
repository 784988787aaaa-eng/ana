package com.smartledger.aldaftar.domain

import android.content.Context
import java.util.UUID

/**
 * Stable application device identity used by backup/file workflows.
 * This identity has no entitlement or activation semantics.
 */
object DeviceIdentityManager {
    fun getOrGenerate(context: Context): String {
        val security = AppSecurityManager.getInstance(context.applicationContext)
        var deviceId = security.getUnifiedDeviceId()
        if (deviceId.isNotBlank()) return deviceId

        val randomPart = UUID.randomUUID().toString().replace("-", "").take(16).uppercase()
        deviceId = "MZ-$randomPart"
        security.setUnifiedDeviceId(deviceId)
        return deviceId
    }
}
