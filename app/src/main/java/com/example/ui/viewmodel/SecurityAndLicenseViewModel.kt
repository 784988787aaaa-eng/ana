/*
 * ╔══════════════════════════════════════════════════════════════════════╗
 * ║ التوثيق العربي الهندسي — MASTERCLASS DOCUMENTATION                 ║
 * ╚══════════════════════════════════════════════════════════════════════╝
 *
 * الملف: app/src/main/java/com/example/ui/viewmodel/SecurityAndLicenseViewModel.kt
 * القطاع المعماري: ViewModels & UI State.
 *
 * الوصف المعماري:
 * مدير حالة الأمان والترخيص؛ ينسق نتائج المصادقة وحالة الترخيص/التجربة مع واجهة المستخدم.
 *
 * الرؤية التعليمية والبصرية:
 * تخيل شاشة الهاتف أثناء تفاعل المستخدم: يضغط على زر أو يغيّر قيمة،
 * فتتولد إشارة، ثم تُعالج في طبقة الحالة، ثم تتغير الحالة التي تقرأها
 * Compose لإعادة رسم الشاشة. هذا الملف يقع في تلك السلسلة ويجب قراءته
 * باعتباره عقداً بين «ما فعله المستخدم» و«ما تراه الشاشة».
 *
 * قاعدة الثبات البرمجي:
 * النص التنفيذي الأصلي محفوظ حرفياً بعد هذا الرأس. الإضافات هنا توثيقية
 * فقط ولا تستبدل أي تعليمة أو اسماً أو قيمة أو منطقاً تنفيذياً.
 */

// --- الفهرس التوثيقي للعناصر البرمجية ---
// السطر 26: class SecurityAndLicenseViewModel — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 29: private const val TAG — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 32: private val repository — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 33: private val securityManager — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 34: private val licenseAndTrialManager — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 36: private val _activationTrigger — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 37: private val preferenceListener — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 48: private val _kickoutEvent — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 49: val kickoutEvent — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 51: val isBiometricSupported — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 53: private val _isBiometricEnabled — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 54: val isBiometricEnabled — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 56: fun toggleBiometric — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 61: fun startRealtimeMonitoring — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 62: val activatedEmail — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 63: val googleEmail — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 64: val emailToMonitor — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 65: val deviceId — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 81: fun stopRealtimeMonitoring — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 86: val database — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 102: fun checkFirebaseLicenseStatus — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 103: val activatedEmail — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 104: val googleEmail — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 105: val emailToCheck — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 122: val settingsState — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 126: val showActivationRequired — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 127: fun resetActivationRequired — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 131: val isPrivacyModeEnabled — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 132: fun togglePrivacyMode — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 136: fun getOrGenerateUnifiedDeviceId — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 139: private val _isLicenseLoading — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 140: val isLicenseLoading — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 142: val deviceIdState — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 148: val activatedEmailState — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 152: val isActivatedState — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 158: fun activateLicense — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 160: val isValid — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 171: fun isNetworkAvailable — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 173: val cm — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 175: val activeNetwork — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 176: val capabilities — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 184: fun activateWithFirebaseEmail — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 193: val deviceId — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 197: val result — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 215: fun unlinkCurrentDevice — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 216: val email — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 219: var success — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 239: fun clearLocalActivationData — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 248: private fun saveEmailActivationLocally — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 253: val totalTransactionsCount — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 260: fun isTrialExpired — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 261: val count — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 265: fun saveSettings — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 279: fun verifyCredentials — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 280: val inputChars — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 282: val hashed — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 283: val settings — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 294: override fun onCleared — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// --- نهاية الفهرس التوثيقي ---

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
                }
                _activationTrigger.value += 1
            }
        }
    }

    fun checkFirebaseLicenseStatus() {
        val activatedEmail = securityManager.getActivatedEmail()
        val googleEmail = GoogleAuthSessionManager.currentEmail.value
        val emailToCheck = if (activatedEmail.isNotBlank()) activatedEmail else (googleEmail ?: "")

        if (emailToCheck.isNotBlank()) {
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

    fun isNetworkAvailable(): Boolean {
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


/*
 * --- ملاحظات وتوصيات المعمارية البرمجية ---
 * 1) يجب أن تبقى ViewModel طبقة تنسيق للحالة والأحداث، لا مستودعاً لقواعد
 *    المجال المالية التي ينبغي أن تعيش في طبقاتها المتخصصة.
 * 2) يوصى مستقبلاً بمراجعة دورة حياة كل Coroutine/Flow والتأكد من ارتباطها
 *    بـ viewModelScope أو نطاقها المقصود لمنع التسرب أو العمل بعد زوال الشاشة.
 * 3) عند تعديل UiState يجب الحفاظ على دلالة الحالات الانتقالية مثل التحميل،
 *    النجاح، الخطأ، والفراغ حتى لا تظهر واجهة مضللة للمستخدم.
 * 4) أي تغيير في الأحداث أو العقود العامة يجب أن يرافقه Regression Test
 *    يثبت أن التفاعل الحالي في Compose لم يتغير.
 * 5) الحسابات المالية والـ BigDecimal يجب أن تبقى في مسارها الدقيق، وألا
 *    تتحول إلى Double/Float داخل طبقة العرض إلا بقرار موثق وصريح.
 * 6) هذه التوصيات مرجعية مستقبلية فقط ولا تمثل أي تغيير في التنفيذ الحالي.
 */
