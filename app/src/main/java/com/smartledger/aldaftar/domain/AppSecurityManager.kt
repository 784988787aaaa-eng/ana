package com.smartledger.aldaftar.domain

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys

/** Secure local storage for application security settings only. */
class AppSecurityManager private constructor(context: Context) {
    private val appContext = context.applicationContext
    private val securePrefs: SharedPreferences by lazy { initEncryptedPreferences() }

    private fun initEncryptedPreferences(): SharedPreferences {
        val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        return EncryptedSharedPreferences.create(
            ENCRYPTED_PREFS_NAME,
            masterKeyAlias,
            appContext,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    fun isBiometricEnabled(): Boolean = securePrefs.getBoolean(PREF_BIOMETRIC_ENABLED, false)
    fun setBiometricEnabled(enabled: Boolean) { securePrefs.edit().putBoolean(PREF_BIOMETRIC_ENABLED, enabled).apply() }
    fun isFastPasscodeEnabled(): Boolean = securePrefs.getBoolean(PREF_FAST_PASSCODE_ENABLED, false)
    fun setFastPasscodeEnabled(enabled: Boolean) { securePrefs.edit().putBoolean(PREF_FAST_PASSCODE_ENABLED, enabled).apply() }

    fun hasAdminPin(): Boolean = !securePrefs.getString(PREF_ADMIN_PIN_HASH, null).isNullOrBlank()
    fun validateAdminPin(enteredPin: String): Boolean {
        val stored = securePrefs.getString(PREF_ADMIN_PIN_HASH, null) ?: return false
        return DatabaseSecurityGuard.secureEqual(HashUtils.hashString(enteredPin), stored)
    }

    fun registerListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) = securePrefs.registerOnSharedPreferenceChangeListener(listener)
    fun unregisterListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) = securePrefs.unregisterOnSharedPreferenceChangeListener(listener)

    companion object {
        private const val ENCRYPTED_PREFS_NAME = "mizan_encrypted_sec_prefs"
        const val PREF_FAST_PASSCODE_ENABLED = "fast_passcode_enabled"
        const val PREF_BIOMETRIC_ENABLED = "biometric_enabled"
        const val PREF_ADMIN_PIN_HASH = "admin_pin_hash"
        const val PREF_FAILED_PIN_ATTEMPTS = "failed_pin_attempts"
        const val PREF_LOCKOUT_UNTIL_TIMESTAMP = "lockout_until_timestamp"
        @Volatile private var INSTANCE: AppSecurityManager? = null
        fun getInstance(context: Context): AppSecurityManager = INSTANCE ?: synchronized(this) {
            INSTANCE ?: AppSecurityManager(context).also { INSTANCE = it }
        }
    }
}
