package com.smartledger.aldaftar.ui.viewmodel

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.smartledger.aldaftar.R
import com.smartledger.aldaftar.data.local.AppDatabase
import com.smartledger.aldaftar.data.local.entities.AppSettings
import com.smartledger.aldaftar.data.repository.FinanceRepository
import com.smartledger.aldaftar.data.repository.LicenseAndTrialManager
import com.smartledger.aldaftar.domain.AppSecurityManager
import com.smartledger.aldaftar.domain.BiometricAuthHelper
import com.smartledger.aldaftar.domain.DatabaseSecurityGuard
import com.smartledger.aldaftar.domain.FirebaseLicenseManager
import com.smartledger.aldaftar.domain.GoogleAuthSessionManager
import com.smartledger.aldaftar.domain.HashUtils
import com.smartledger.aldaftar.domain.LicenseCheckResult
import com.smartledger.aldaftar.domain.LicenseManager
import com.smartledger.aldaftar.domain.LicenseState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SecurityAndLicenseViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "SecurityAndLicenseVM"
    }

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
        if (activatedEmail.isBlank() || !securityManager.isActivatedCached()) {
            // لا يتم تشغيل المراقبة اللحظية لطرد الأجهزة إلا إذا كان التفعيل محلياً ومسجلاً مسبقاً
            return
        }
        val deviceId = LicenseAndTrialManager.getOrGenerateUnifiedDeviceId(context)

        FirebaseLicenseManager.startRealtimeLicenseMonitoring(
            context = context,
            email = activatedEmail,
            currentDeviceId = deviceId
        ) { reason ->
            viewModelScope.launch {
                clearLocalActivationData()
                _kickoutEvent.emit(reason)
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
                }
                _activationTrigger.value += 1
            }
        }
    }

    fun checkFirebaseLicenseStatus() {
        val activatedEmail = securityManager.getActivatedEmail()

        if (activatedEmail.isNotBlank() && securityManager.isActivatedCached()) {
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    FirebaseLicenseManager.syncAndVerifyLocalEmailLicense(getApplication())
                } catch (t: Throwable) {
                    Log.w(TAG, "Offline or error syncing license safely: ${t.message}")
                } finally {
                    _activationTrigger.value += 1
                }
            }
        } else {
            _activationTrigger.value += 1
        }
    }

    val settingsState: StateFlow<AppSettings> = repository.settingsFlow
        .map { it ?: AppSettings() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, AppSettings())

    val showActivationRequired = MutableStateFlow(false)
    fun resetActivationRequired() {
        showActivationRequired.value = false
    }

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

    /**
     * حالة الترخيص الصريحة والمهيكلة المعتمدة على [LicenseState].
     */
    val licenseState: StateFlow<LicenseState> = combine(isActivatedState, activatedEmailState, deviceIdState) { isActivated, email, deviceId ->
        when {
            isActivated && email.isNotBlank() -> LicenseState.Valid(email = email, deviceId = deviceId)
            isActivated -> LicenseState.Valid(email = "", deviceId = deviceId)
            else -> LicenseState.Invalid(message = "App is not activated")
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, LicenseState.Unknown)

    fun activateLicense(code: String): Boolean {
        return try {
            val isValid = licenseAndTrialManager.activateLicenseWithCode(code)
            if (isValid) {
                _activationTrigger.value += 1
            }
            isValid
        } catch (t: Throwable) {
            Log.e(TAG, "Error activating license code", t)
            false
        }
    }

    private fun isNetworkAvailable(): Boolean {
        return try {
            val cm = getApplication<Application>().getSystemService(Context.CONNECTIVITY_SERVICE) as? android.net.ConnectivityManager
            if (cm != null) {
                val activeNetwork = cm.activeNetwork ?: return false
                val capabilities = cm.getNetworkCapabilities(activeNetwork) ?: return false
                capabilities.hasCapability(android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET)
            } else false
        } catch (e: Exception) {
            false
        }
    }

    fun activateWithFirebaseEmail(email: String, onResult: (LicenseCheckResult) -> Unit) {
        if (!isNetworkAvailable()) {
            onResult(
                LicenseCheckResult.Error(
                    getApplication<Application>().getString(R.string.licensing_error_no_internet)
                )
            )
            return
        }
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
                Log.e(TAG, "Error activating with Firebase email", t)
                onResult(
                    LicenseCheckResult.Error(
                        getApplication<Application>().getString(R.string.licensing_error_no_internet)
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
                Log.e(TAG, "Error unlinking current device", t)
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
            Log.e(TAG, "Error clearing local activation data", t)
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
                Log.e(TAG, "Error saving settings", t)
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
            Log.e(TAG, "Error verifying credentials", t)
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
            Log.e(TAG, "Error unregistering listener", t)
        }
    }
}
