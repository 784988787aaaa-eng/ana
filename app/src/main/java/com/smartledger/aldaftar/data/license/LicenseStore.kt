package com.smartledger.aldaftar.data.license

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class LicenseStore(context: Context) {
    private val prefs = EncryptedSharedPreferences.create(
        context.applicationContext,
        "smartledger_license_v1",
        MasterKey.Builder(context.applicationContext).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    var token: String?
        get() = prefs.getString("token", null)
        set(value) { prefs.edit().putString("token", value).apply() }

    var accountCode: String?
        get() = prefs.getString("account_code", null)
        set(value) { prefs.edit().putString("account_code", value).apply() }

    var email: String?
        get() = prefs.getString("email", null)
        set(value) { prefs.edit().putString("email", value).apply() }

    var plan: String?
        get() = prefs.getString("plan", "LIFETIME")
        set(value) { prefs.edit().putString("plan", value).apply() }

    var trialEndsAt: Long
        get() = prefs.getLong("trial_ends_at", 0L)
        set(value) { prefs.edit().putLong("trial_ends_at", value).apply() }

    var maxDevices: Int
        get() = prefs.getInt("max_devices", 1)
        set(value) { prefs.edit().putInt("max_devices", value).apply() }

    var trialUsed: Int
        get() = prefs.getInt("trial_used", 0)
        set(value) { prefs.edit().putInt("trial_used", value).apply() }

    var lastVerifiedAt: Long
        get() = prefs.getLong("last_verified_at", 0L)
        set(value) { prefs.edit().putLong("last_verified_at", value).apply() }

    var serverRevoked: Boolean
        get() = prefs.getBoolean("server_revoked", false)
        set(value) { prefs.edit().putBoolean("server_revoked", value).apply() }

    var revocationReason: String?
        get() = prefs.getString("revocation_reason", null)
        set(value) { prefs.edit().putString("revocation_reason", value).apply() }

    var revocationMessage: String?
        get() = prefs.getString("revocation_message", null)
        set(value) { prefs.edit().putString("revocation_message", value).apply() }

    var activationRequired: Boolean
        get() = prefs.getBoolean("activation_required", false)
        set(value) { prefs.edit().putBoolean("activation_required", value).apply() }

    var lastSeenAt: Long
        get() = prefs.getLong("last_seen_at", 0L)
        set(value) { prefs.edit().putLong("last_seen_at", value).apply() }

    fun clearAccountSession() {
        accountCode = null
        email = null
        plan = "LIFETIME"
        trialEndsAt = 0L
        maxDevices = 1
        lastVerifiedAt = 0L
        lastSeenAt = 0L
        serverRevoked = false
        revocationReason = null
        revocationMessage = null
        activationRequired = false
    }

    fun clearToken() {
        token = null
    }
}
