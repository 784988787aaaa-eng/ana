package com.smartledger.aldaftar.platform.security

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.smartledger.aldaftar.domain.HashUtils
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AppSecurityManager private constructor(context: Context) {
    private val appContext = context.applicationContext
    private val ioScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val securePrefs: SharedPreferences by lazy { initEncryptedPreferences() }

    private val isWarmupComplete = AtomicBoolean(false)
    private val failedAttemptsDirty = AtomicBoolean(false)
    private val lockoutUntilDirty = AtomicBoolean(false)
    private val biometricDirty = AtomicBoolean(false)
    private val fastPasscodeDirty = AtomicBoolean(false)

    private val cachedBiometricEnabled = AtomicBoolean(false)
    private val cachedFastPasscodeEnabled = AtomicBoolean(false)
    private val cachedFailedAttempts = AtomicInteger(0)
    private val cachedLockoutUntil = AtomicLong(0L)

    private val _failedAttemptsFlow = MutableStateFlow(0)
    val failedAttemptsFlow: StateFlow<Int> = _failedAttemptsFlow.asStateFlow()

    private val _lockoutUntilFlow = MutableStateFlow(0L)
    val lockoutUntilFlow: StateFlow<Long> = _lockoutUntilFlow.asStateFlow()

    private fun initEncryptedPreferences(): SharedPreferences {
        return runCatching {
            val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
            EncryptedSharedPreferences.create(
                ENCRYPTED_PREFS_NAME,
                masterKeyAlias,
                appContext,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        }.getOrElse {
            appContext.getSharedPreferences(ENCRYPTED_PREFS_NAME, Context.MODE_PRIVATE)
        }
    }

    /**
     * Pre-warms EncryptedSharedPreferences off the main thread to eliminate UI thread lag
     * and hydrates the fast in-memory atomic state.
     */
    suspend fun warmupStorage() {
        withContext(Dispatchers.IO) {
            securePrefs.contains("warmup_check")
            if (!biometricDirty.get()) {
                cachedBiometricEnabled.set(securePrefs.getBoolean(PREF_BIOMETRIC_ENABLED, false))
            }
            if (!fastPasscodeDirty.get()) {
                cachedFastPasscodeEnabled.set(securePrefs.getBoolean(PREF_FAST_PASSCODE_ENABLED, false))
            }
            if (!failedAttemptsDirty.get()) {
                val attempts = securePrefs.getInt(PREF_FAILED_PIN_ATTEMPTS, 0)
                cachedFailedAttempts.set(attempts)
                _failedAttemptsFlow.value = attempts
            }
            if (!lockoutUntilDirty.get()) {
                val until = securePrefs.getLong(PREF_LOCKOUT_UNTIL_TIMESTAMP, 0L)
                cachedLockoutUntil.set(until)
                _lockoutUntilFlow.value = until
            }
            isWarmupComplete.set(true)
        }
    }

    fun isBiometricEnabled(): Boolean {
        return if (isWarmupComplete.get() || biometricDirty.get()) {
            cachedBiometricEnabled.get()
        } else {
            val value = securePrefs.getBoolean(PREF_BIOMETRIC_ENABLED, false)
            cachedBiometricEnabled.set(value)
            value
        }
    }

    fun setBiometricEnabled(enabled: Boolean) {
        biometricDirty.set(true)
        cachedBiometricEnabled.set(enabled)
        ioScope.launch {
            securePrefs.edit().putBoolean(PREF_BIOMETRIC_ENABLED, enabled).apply()
        }
    }

    fun isFastPasscodeEnabled(): Boolean {
        return if (isWarmupComplete.get() || fastPasscodeDirty.get()) {
            cachedFastPasscodeEnabled.get()
        } else {
            val value = securePrefs.getBoolean(PREF_FAST_PASSCODE_ENABLED, false)
            cachedFastPasscodeEnabled.set(value)
            value
        }
    }

    fun setFastPasscodeEnabled(enabled: Boolean) {
        fastPasscodeDirty.set(true)
        cachedFastPasscodeEnabled.set(enabled)
        ioScope.launch {
            securePrefs.edit().putBoolean(PREF_FAST_PASSCODE_ENABLED, enabled).apply()
        }
    }

    fun getFailedAttempts(): Int = cachedFailedAttempts.get()

    fun incrementFailedAttempts() {
        failedAttemptsDirty.set(true)
        val next = cachedFailedAttempts.incrementAndGet()
        _failedAttemptsFlow.value = next
        ioScope.launch {
            securePrefs.edit().putInt(PREF_FAILED_PIN_ATTEMPTS, next).apply()
        }
    }

    fun resetFailedAttempts() {
        failedAttemptsDirty.set(true)
        lockoutUntilDirty.set(true)
        cachedFailedAttempts.set(0)
        cachedLockoutUntil.set(0L)
        _failedAttemptsFlow.value = 0
        _lockoutUntilFlow.value = 0L
        ioScope.launch {
            securePrefs.edit().remove(PREF_FAILED_PIN_ATTEMPTS).remove(PREF_LOCKOUT_UNTIL_TIMESTAMP).apply()
        }
    }

    fun getLockoutUntil(): Long = cachedLockoutUntil.get()

    fun setLockoutUntil(timestamp: Long) {
        lockoutUntilDirty.set(true)
        cachedLockoutUntil.set(timestamp)
        _lockoutUntilFlow.value = timestamp
        ioScope.launch {
            securePrefs.edit().putLong(PREF_LOCKOUT_UNTIL_TIMESTAMP, timestamp).apply()
        }
    }

    fun hasAdminPin(): Boolean = !securePrefs.getString(PREF_ADMIN_PIN_HASH, null).isNullOrBlank()

    fun validateAdminPin(enteredPin: String): Boolean {
        val stored = securePrefs.getString(PREF_ADMIN_PIN_HASH, null) ?: return false
        return HashUtils.verifyPin(enteredPin, stored)
    }

    fun registerListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) =
        securePrefs.registerOnSharedPreferenceChangeListener(listener)

    fun unregisterListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) =
        securePrefs.unregisterOnSharedPreferenceChangeListener(listener)

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

