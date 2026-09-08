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
import com.smartledger.aldaftar.domain.DeviceIdentityManager
import com.smartledger.aldaftar.domain.AppSecurityManager
import com.smartledger.aldaftar.domain.BiometricAuthHelper
import com.smartledger.aldaftar.domain.DatabaseSecurityGuard
import com.smartledger.aldaftar.domain.GoogleAuthSessionManager
import com.smartledger.aldaftar.domain.HashUtils
import com.smartledger.aldaftar.domain.LicenseCheckResult
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
    private val supportIdentityRepo: com.smartledger.aldaftar.domain.SupportIdentityRepository =
        com.smartledger.aldaftar.domain.SupportIdentityRepositoryImpl(application)

    private val _supportIdentityState = MutableStateFlow<com.smartledger.aldaftar.domain.SupportIdentityState>(
        com.smartledger.aldaftar.domain.SupportIdentityState.Idle
    )
    val supportIdentityState: StateFlow<com.smartledger.aldaftar.domain.SupportIdentityState> = _supportIdentityState.asStateFlow()

    fun loadSupportIdentity() {
        val cached = supportIdentityRepo.getCachedSupportId()
        if (!cached.isNullOrBlank()) {
            _supportIdentityState.value = com.smartledger.aldaftar.domain.SupportIdentityState.Available(cached)
            return
        }
        viewModelScope.launch {
            _supportIdentityState.value = com.smartledger.aldaftar.domain.SupportIdentityState.Loading
            val state = supportIdentityRepo.getSupportIdentity()
            _supportIdentityState.value = state
        }
    }

    private val _activationTrigger = MutableStateFlow(0)
    private val preferenceListener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        if (key == AppSecurityManager.PREF_SUPPORT_ID ||
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

    /** Licensing monitoring is intentionally disabled in this build. */
    fun startRealtimeMonitoring(context: Context) = Unit

    fun stopRealtimeMonitoring() = Unit

    init {
        val database = AppDatabase.getDatabase(application)
        repository = FinanceRepository(database, application)

        securityManager.registerListener(preferenceListener)

        viewModelScope.launch {
            GoogleAuthSessionManager.currentEmail.collect {
                _activationTrigger.value += 1
            }
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
        DeviceIdentityManager.getOrGenerate(context)

    private val _isLicenseLoading = MutableStateFlow(false)
    val isLicenseLoading: StateFlow<Boolean> = _isLicenseLoading.asStateFlow()

    val deviceIdState: StateFlow<String> = flow {
        emit(DeviceIdentityManager.getOrGenerate(getApplication()))
    }
    .flowOn(Dispatchers.IO)
    .stateIn(viewModelScope, SharingStarted.Eagerly, "")

    val activatedEmailState: StateFlow<String> =
        GoogleAuthSessionManager.currentEmail
            .map { it.orEmpty() }
            .stateIn(viewModelScope, SharingStarted.Eagerly, "")

    /** Compatibility state: entitlement enforcement is disabled. */
    val isActivatedState: StateFlow<Boolean> =
        flowOf(true)
            .stateIn(viewModelScope, SharingStarted.Eagerly, true)

    val licenseState: StateFlow<LicenseState> =
        combine(isActivatedState, activatedEmailState, deviceIdState) { _, email, deviceId ->
            LicenseState.Valid(email = email, deviceId = deviceId)
        }.stateIn(viewModelScope, SharingStarted.Eagerly, LicenseState.Unknown)

    /**
     * The activation window remains available for a future licensing release,
     * but this build performs no local or cloud activation.
     */
    fun activateWithFirebaseEmail(email: String, onResult: (LicenseCheckResult) -> Unit) {
        onResult(
            LicenseCheckResult.Error(
                getApplication<Application>().getString(R.string.licensing_error_connection)
            )
        )
    }

    fun unlinkCurrentDevice(onResult: (Boolean) -> Unit) {
        clearLocalActivationData()
        onResult(true)
    }

    fun clearLocalActivationData() {
        try {
            supportIdentityRepo.clearSupportIdentity()
            _supportIdentityState.value = com.smartledger.aldaftar.domain.SupportIdentityState.Idle
            _activationTrigger.value += 1
        } catch (t: Throwable) {
            Log.e(TAG, "Error clearing support identity", t)
        }
    }

    val totalTransactionsCount: StateFlow<Int> = combine(
        repository.getTransactionsCountFlow(),
        repository.getHabayebTransactionsCountFlow()
    ) { mainCount, habayebCount ->
        mainCount + habayebCount
    }.stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    fun isTrialExpired(): Boolean = false

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
