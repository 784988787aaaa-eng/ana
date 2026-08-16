package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.R
import com.example.data.local.AppDatabase
import com.example.data.local.entities.AppSettings
import com.example.data.repository.FinanceRepository
import com.example.data.repository.LicenseAndTrialManager
import com.example.domain.AppSecurityManager
import com.example.domain.BiometricAuthHelper
import com.example.domain.DatabaseSecurityGuard
import com.example.domain.FirebaseLicenseManager
import com.example.domain.GoogleAuthSessionManager
import com.example.domain.HashUtils
import com.example.domain.LicenseCheckResult
import com.example.domain.LicenseManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SecurityAndLicenseViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: FinanceRepository
    private val securityManager: AppSecurityManager = AppSecurityManager.getInstance(application)
    private val licenseAndTrialManager: LicenseAndTrialManager = LicenseAndTrialManager(application)

    private val _activationTrigger = MutableStateFlow(0)
    private val preferenceListener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        if (key == AppSecurityManager.PREF_M_ACT_CODE ||
            key == AppSecurityManager.PREF_M_ACTIVATED_EMAIL ||
            key == AppSecurityManager.PREF_IS_ACTIVATED_CACHED ||
            key == AppSecurityManager.PREF_BIOMETRIC_ENABLED ||
            key == AppSecurityManager.PREF_FAST_PASSCODE_ENABLED
        ) {
            _activationTrigger.value += 1
        }
    }

    private val _kickoutEvent = MutableSharedFlow<String>()
    val kickoutEvent: SharedFlow<String> = _kickoutEvent.asSharedFlow()

    val isBiometricSupported: Boolean = BiometricAuthHelper.isBiometricAvailable(application)

    private val _isBiometricEnabled = MutableStateFlow(securityManager.isBiometricEnabled())
    val isBiometricEnabled: StateFlow<Boolean> = _isBiometricEnabled.asStateFlow()

    fun toggleBiometric(enabled: Boolean) {
        securityManager.setBiometricEnabled(enabled)
        _isBiometricEnabled.value = enabled
    }

    fun startRealtimeMonitoring(context: Context) {
        val activatedEmail = securityManager.getActivatedEmail()
        val googleEmail = GoogleAuthSessionManager.currentEmail.value
        val emailToMonitor = if (activatedEmail.isNotBlank()) activatedEmail else (googleEmail ?: "")
        val deviceId = LicenseAndTrialManager.getOrGenerateUnifiedDeviceId(context)

        if (emailToMonitor.isNotBlank()) {
            FirebaseLicenseManager.startRealtimeLicenseMonitoring(
                context = context,
                email = emailToMonitor,
                currentDeviceId = deviceId
            ) { reason ->
                viewModelScope.launch {
                    clearLocalActivationData()
                    _kickoutEvent.emit(reason)
                }
            }
        }
    }

    fun stopRealtimeMonitoring() {
        FirebaseLicenseManager.stopRealtimeLicenseMonitoring()
    }

    init {
        val database = AppDatabase.getDatabase(application)
        repository = FinanceRepository(database, application)

        securityManager.registerListener(preferenceListener)

        // Observe unified Google email to trigger license verification dynamically
        viewModelScope.launch {
            GoogleAuthSessionManager.currentEmail.collect { email ->
                if (email != null) {
                    checkFirebaseLicenseStatus()
                } else {
                    val activatedEmail = securityManager.getActivatedEmail()
                    if (activatedEmail.isNotBlank()) {
                        clearLocalActivationData()
                    }
                }
                _activationTrigger.value += 1
            }
        }
    }

    fun checkFirebaseLicenseStatus() {
        val googleEmail = GoogleAuthSessionManager.currentEmail.value
        if (googleEmail != null) {
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    val deviceId = LicenseAndTrialManager.getOrGenerateUnifiedDeviceId(getApplication())
                    val result = FirebaseLicenseManager.verifyAndActivateEmail(getApplication(), googleEmail, deviceId)
                    if (result is LicenseCheckResult.Success) {
                        saveEmailActivationLocally(result.email, result.deviceId)
                    } else {
                        val localEmail = securityManager.getActivatedEmail()
                        if (localEmail.isNotBlank() && localEmail.trim().lowercase() == googleEmail.trim().lowercase()) {
                            securityManager.clearActivationData()
                        }
                    }
                } catch (t: Throwable) {
                    Log.e("SecurityAndLicenseVM", "Error checking Firebase license status", t)
                } finally {
                    _activationTrigger.value += 1
                }
            }
        } else {
            val localEmail = securityManager.getActivatedEmail()
            if (localEmail.isNotBlank()) {
                viewModelScope.launch(Dispatchers.IO) {
                    try {
                        FirebaseLicenseManager.syncAndVerifyLocalEmailLicense(getApplication())
                    } catch (t: Throwable) {
                        Log.e("SecurityAndLicenseVM", "Error verifying local email license", t)
                    } finally {
                        _activationTrigger.value += 1
                    }
                }
            } else {
                _activationTrigger.value += 1
            }
        }
    }

    val settingsState: StateFlow<AppSettings> = repository.settingsFlow
        .map { it ?: AppSettings() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, AppSettings())

    val showActivationRequired = MutableStateFlow(false)

    val isPrivacyModeEnabled = MutableStateFlow(true)
    fun togglePrivacyMode() {
        isPrivacyModeEnabled.value = !isPrivacyModeEnabled.value
    }

    fun getOrGenerateUnifiedDeviceId(context: Context): String =
        LicenseAndTrialManager.getOrGenerateUnifiedDeviceId(context)

    private val _isLicenseLoading = MutableStateFlow(false)
    val isLicenseLoading: StateFlow<Boolean> = _isLicenseLoading.asStateFlow()

    val deviceIdState: StateFlow<String> = flow {
        emit(LicenseAndTrialManager.getOrGenerateUnifiedDeviceId(getApplication()))
    }
    .flowOn(Dispatchers.IO)
    .stateIn(viewModelScope, SharingStarted.Eagerly, "")

    val activatedEmailState: StateFlow<String> = combine(deviceIdState, _activationTrigger) { _, _ ->
        securityManager.getActivatedEmail()
    }.stateIn(viewModelScope, SharingStarted.Eagerly, "")

    val isActivatedState: StateFlow<Boolean> = combine(deviceIdState, _activationTrigger) { _, _ ->
        licenseAndTrialManager.isAppActivated()
    }
    .flowOn(Dispatchers.IO)
    .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    fun activateLicense(code: String): Boolean {
        return try {
            val isValid = licenseAndTrialManager.activateLicenseWithCode(code)
            if (isValid) {
                _activationTrigger.value += 1
            }
            isValid
        } catch (t: Throwable) {
            Log.e("SecurityAndLicenseVM", "Error activating license code", t)
            false
        }
    }

    fun activateWithFirebaseEmail(email: String, onResult: (LicenseCheckResult) -> Unit) {
        val deviceId = LicenseAndTrialManager.getOrGenerateUnifiedDeviceId(getApplication())
        viewModelScope.launch {
            _isLicenseLoading.value = true
            try {
                val result = FirebaseLicenseManager.verifyAndActivateEmail(getApplication(), email, deviceId)
                if (result is LicenseCheckResult.Success) {
                    saveEmailActivationLocally(result.email, result.deviceId)
                }
                onResult(result)
            } catch (t: Throwable) {
                Log.e("SecurityAndLicenseVM", "Error activating with Firebase email", t)
                onResult(
                    LicenseCheckResult.Error(
                        getApplication<Application>().getString(R.string.licensing_error_connection)
                    )
                )
            } finally {
                _isLicenseLoading.value = false
            }
        }
    }

    fun sendTransferOtp(email: String, onResult: (LicenseCheckResult) -> Unit) {
        val deviceId = LicenseAndTrialManager.getOrGenerateUnifiedDeviceId(getApplication())
        viewModelScope.launch {
            _isLicenseLoading.value = true
            try {
                val result = FirebaseLicenseManager.sendTransferOtp(getApplication(), email, deviceId)
                onResult(result)
            } catch (t: Throwable) {
                Log.e("SecurityAndLicenseVM", "Error sending transfer OTP", t)
                onResult(
                    LicenseCheckResult.Error(
                        getApplication<Application>().getString(R.string.licensing_error_otp_send_failed)
                    )
                )
            } finally {
                _isLicenseLoading.value = false
            }
        }
    }

    fun verifyOtpAndTransfer(email: String, otpInput: String, onResult: (LicenseCheckResult) -> Unit) {
        val deviceId = LicenseAndTrialManager.getOrGenerateUnifiedDeviceId(getApplication())
        viewModelScope.launch {
            _isLicenseLoading.value = true
            try {
                val result = FirebaseLicenseManager.verifyOtpAndTransfer(getApplication(), email, otpInput, deviceId)
                if (result is LicenseCheckResult.Success) {
                    saveEmailActivationLocally(result.email, result.deviceId)
                }
                onResult(result)
            } catch (t: Throwable) {
                Log.e("SecurityAndLicenseVM", "Error verifying OTP", t)
                onResult(
                    LicenseCheckResult.Error(
                        getApplication<Application>().getString(R.string.licensing_error_otp_verify_failed)
                    )
                )
            } finally {
                _isLicenseLoading.value = false
            }
        }
    }

    fun unlinkCurrentDevice(onResult: (Boolean) -> Unit) {
        val email = securityManager.getActivatedEmail()
        viewModelScope.launch {
            _isLicenseLoading.value = true
            var success = false
            try {
                success = if (email.isNotBlank()) {
                    FirebaseLicenseManager.unlinkDevice(email)
                } else true

                if (success) {
                    clearLocalActivationData()
                }
            } catch (t: Throwable) {
                Log.e("SecurityAndLicenseVM", "Error unlinking current device", t)
                clearLocalActivationData()
                success = true
            } finally {
                _isLicenseLoading.value = false
                onResult(success)
            }
        }
    }

    fun clearLocalActivationData() {
        try {
            licenseAndTrialManager.clearLocalActivation()
            _activationTrigger.value += 1
        } catch (t: Throwable) {
            Log.e("SecurityAndLicenseVM", "Error clearing local activation data", t)
        }
    }

    private fun saveEmailActivationLocally(email: String, deviceId: String) {
        licenseAndTrialManager.saveEmailActivation(email, deviceId)
        _activationTrigger.value += 1
    }

    val totalTransactionsCount: StateFlow<Int> = combine(
        repository.getTransactionsCountFlow(),
        repository.getHabayebTransactionsCountFlow()
    ) { mainCount, habayebCount ->
        mainCount + habayebCount
    }.stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    fun isTrialExpired(): Boolean {
        val count = totalTransactionsCount.value
        return licenseAndTrialManager.isTrialExpiredDirect(count)
    }

    fun saveSettings(settings: AppSettings) {
        securityManager.setFastPasscodeEnabled(settings.isPasscodeEnabled)
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.saveSettings(settings)
            } catch (t: Throwable) {
                Log.e("SecurityAndLicenseVM", "Error saving settings", t)
            }
        }
    }

    /**
     * Verifies PIN or recovery phrase credentials with memory scrubbing.
     */
    fun verifyCredentials(input: String): Boolean {
        val inputChars = input.trim().toCharArray()
        return try {
            val hashed = HashUtils.hashString(String(inputChars))
            val settings = settingsState.value
            (settings.passcodeHash != null && DatabaseSecurityGuard.secureEqual(hashed, settings.passcodeHash)) ||
                    (settings.recoveryPhraseHash != null && DatabaseSecurityGuard.secureEqual(hashed, settings.recoveryPhraseHash))
        } catch (t: Throwable) {
            Log.e("SecurityAndLicenseVM", "Error verifying credentials", t)
            false
        } finally {
            HashUtils.wipeCharArray(inputChars)
        }
    }

    override fun onCleared() {
        super.onCleared()
        try {
            stopRealtimeMonitoring()
            securityManager.unregisterListener(preferenceListener)
        } catch (t: Throwable) {
            Log.e("SecurityAndLicenseVM", "Error unregistering listener", t)
        }
    }
}
