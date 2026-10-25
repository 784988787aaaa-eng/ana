package com.smartledger.aldaftar.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.smartledger.aldaftar.data.local.entities.AppSettings
import com.smartledger.aldaftar.domain.HashUtils
import com.smartledger.aldaftar.platform.security.AppSecurityManager
import com.smartledger.aldaftar.platform.security.BiometricAuthHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SecurityViewModel(
    application: Application,
    private val repository: com.smartledger.aldaftar.data.repository.SettingsRepository
) : AndroidViewModel(application) {

    private val securityManager = AppSecurityManager.getInstance(application)

    val settingsState: StateFlow<AppSettings> = repository.settingsFlow
        .map { it ?: AppSettings() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, AppSettings())

    val isBiometricSupported: Boolean = BiometricAuthHelper.isBiometricAvailable(application)

    private val _isBiometricEnabled = MutableStateFlow(false)
    val isBiometricEnabled: StateFlow<Boolean> = _isBiometricEnabled.asStateFlow()

    val failedAttemptsState: StateFlow<Int> = securityManager.failedAttemptsFlow
    val lockoutUntilState: StateFlow<Long> = securityManager.lockoutUntilFlow

    val isPrivacyModeEnabled = MutableStateFlow(true)

    init {
        // Pre-warm security storage on IO thread and JCA PBKDF2 provider on Default thread
        viewModelScope.launch(Dispatchers.IO) {
            securityManager.warmupStorage()
            _isBiometricEnabled.value = securityManager.isBiometricEnabled()
        }
        viewModelScope.launch(Dispatchers.Default) {
            HashUtils.warmup()
        }
    }

    fun toggleBiometric(enabled: Boolean) {
        _isBiometricEnabled.value = enabled
        viewModelScope.launch(Dispatchers.IO) {
            securityManager.setBiometricEnabled(enabled)
        }
    }

    fun togglePrivacyMode() {
        isPrivacyModeEnabled.value = !isPrivacyModeEnabled.value
    }

    fun saveSettings(settings: AppSettings) {
        viewModelScope.launch(Dispatchers.IO) {
            securityManager.setFastPasscodeEnabled(settings.isPasscodeEnabled)
            if (settings.isPasscodeEnabled) {
                securityManager.resetFailedAttempts()
            }
            repository.saveSettings(settings)
        }
    }

    suspend fun saveSettingsSync(settings: AppSettings) {
        withContext(Dispatchers.IO) {
            securityManager.setFastPasscodeEnabled(settings.isPasscodeEnabled)
            if (settings.isPasscodeEnabled) {
                securityManager.resetFailedAttempts()
            }
            repository.saveSettings(settings)
        }
    }

    fun getFailedAttemptsCount(): Int = securityManager.getFailedAttempts()

    fun getLockoutTimeRemainingMs(): Long {
        val until = securityManager.getLockoutUntil()
        val now = System.currentTimeMillis()
        return if (until > now) until - now else 0L
    }

    fun handleFailedAttempt() {
        securityManager.incrementFailedAttempts()
        val attempts = securityManager.getFailedAttempts()
        if (attempts >= 5) {
            val lockoutDurationMs = when (attempts) {
                5 -> 30_000L
                6 -> 60_000L
                else -> 300_000L
            }
            securityManager.setLockoutUntil(System.currentTimeMillis() + lockoutDurationMs)
        }
    }

    fun resetFailedAttempts() {
        securityManager.resetFailedAttempts()
    }

    /**
     * Verifies a 4-digit PIN off the Main UI thread with a single PBKDF2 derivation.
     */
    suspend fun verifyPinOffMainThread(pin: String): Boolean {
        val cleanPin = pin.trim()
        if (cleanPin.length != 4) return false
        val passChars = cleanPin.toCharArray()
        val targetHash = settingsState.value.passcodeHash?.takeIf { it.isNotBlank() }
            ?: withContext(Dispatchers.IO) { repository.getSettingsDirect()?.passcodeHash }
        return withContext(Dispatchers.Default) {
            try {
                HashUtils.verifyPin(String(passChars), targetHash)
            } finally {
                HashUtils.wipeCharArray(passChars)
            }
        }
    }

    /**
     * Verifies a recovery phrase off the Main UI thread with a single PBKDF2 derivation.
     */
    suspend fun verifyRecoveryPhraseOffMainThread(phrase: String): Boolean {
        val cleanPhrase = phrase.trim()
        if (cleanPhrase.isEmpty()) return false
        val recoveryChars = cleanPhrase.toCharArray()
        val targetHash = settingsState.value.recoveryPhraseHash?.takeIf { it.isNotBlank() }
            ?: withContext(Dispatchers.IO) { repository.getSettingsDirect()?.recoveryPhraseHash }
        return withContext(Dispatchers.Default) {
            try {
                HashUtils.verifyPin(String(recoveryChars), targetHash)
            } finally {
                HashUtils.wipeCharArray(recoveryChars)
            }
        }
    }

    /**
     * Atomically disables the passcode lock after a valid recovery phrase verification.
     */
    suspend fun disablePasscodeAfterRecovery(fallbackSettings: AppSettings) {
        _isBiometricEnabled.value = false
        withContext(Dispatchers.IO) {
            val latest = repository.getSettingsDirect() ?: settingsState.value.takeIf { it != AppSettings() } ?: fallbackSettings
            val updated = latest.copy(
                isPasscodeEnabled = false,
                passcodeHash = null,
                recoveryPhraseHash = null,
                recoveryHint = null
            )
            securityManager.resetFailedAttempts()
            securityManager.setFastPasscodeEnabled(false)
            securityManager.setBiometricEnabled(false)
            repository.saveSettings(updated)
        }
    }

    fun verifyCredentials(input: String): Boolean {
        val inputChars = input.trim().toCharArray()
        return try {
            val value = String(inputChars)
            val settings = settingsState.value
            HashUtils.verifyPin(value, settings.passcodeHash) ||
                HashUtils.verifyPin(value, settings.recoveryPhraseHash)
        } finally {
            HashUtils.wipeCharArray(inputChars)
        }
    }
}

