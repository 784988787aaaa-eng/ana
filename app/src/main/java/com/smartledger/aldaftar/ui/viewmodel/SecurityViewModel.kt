package com.smartledger.aldaftar.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.smartledger.aldaftar.data.local.AppDatabase
import com.smartledger.aldaftar.data.local.entities.AppSettings
import com.smartledger.aldaftar.data.repository.FinanceRepository
import com.smartledger.aldaftar.domain.AppSecurityManager
import com.smartledger.aldaftar.domain.BiometricAuthHelper
import com.smartledger.aldaftar.domain.DatabaseSecurityGuard
import com.smartledger.aldaftar.domain.HashUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Coordinates local application security (passcode, biometrics and privacy mode). */
class SecurityViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = FinanceRepository(AppDatabase.getDatabase(application), application)
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
        viewModelScope.launch(Dispatchers.IO) {
            try { repository.saveSettings(settings) }
            catch (t: Throwable) { Log.e("SecurityViewModel", "Error saving settings", t) }
        }
    }

    fun verifyCredentials(input: String): Boolean {
        val inputChars = input.trim().toCharArray()
        return try {
            val hashed = HashUtils.hashString(String(inputChars))
            val settings = settingsState.value
            (settings.passcodeHash != null && DatabaseSecurityGuard.secureEqual(hashed, settings.passcodeHash)) ||
                (settings.recoveryPhraseHash != null && DatabaseSecurityGuard.secureEqual(hashed, settings.recoveryPhraseHash))
        } catch (t: Throwable) {
            Log.e("SecurityViewModel", "Error verifying credentials", t)
            false
        } finally { HashUtils.wipeCharArray(inputChars) }
    }
}
