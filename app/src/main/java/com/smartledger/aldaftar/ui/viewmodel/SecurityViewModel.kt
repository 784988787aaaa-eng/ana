package com.smartledger.aldaftar.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.smartledger.aldaftar.data.local.entities.AppSettings
import com.smartledger.aldaftar.platform.security.AppSecurityManager
import com.smartledger.aldaftar.platform.security.BiometricAuthHelper
import com.smartledger.aldaftar.domain.HashUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.Dispatchers
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
    private val _isBiometricEnabled = MutableStateFlow(securityManager.isBiometricEnabled())
    val isBiometricEnabled: StateFlow<Boolean> = _isBiometricEnabled.asStateFlow()

    val isPrivacyModeEnabled = MutableStateFlow(true)

    fun toggleBiometric(enabled: Boolean) {
        securityManager.setBiometricEnabled(enabled)
        _isBiometricEnabled.value = enabled
    }

    fun togglePrivacyMode() {
        isPrivacyModeEnabled.value = !isPrivacyModeEnabled.value
    }

    fun saveSettings(settings: AppSettings) {
        securityManager.setFastPasscodeEnabled(settings.isPasscodeEnabled)
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                repository.saveSettings(settings)
            }
        }
    }

    suspend fun saveSettingsSync(settings: AppSettings) {
        securityManager.setFastPasscodeEnabled(settings.isPasscodeEnabled)
        withContext(Dispatchers.IO) {
            repository.saveSettings(settings)
        }
    }

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

    fun verifyCredentials(input: String): Boolean {
        val inputChars = input.trim().toCharArray()
        return try {
            val value = String(inputChars)
            val settings = settingsState.value
            HashUtils.verifyPin(value, settings.passcodeHash) ||
                HashUtils.verifyPin(value, settings.recoveryPhraseHash)
        } finally { HashUtils.wipeCharArray(inputChars) }
    }
}
