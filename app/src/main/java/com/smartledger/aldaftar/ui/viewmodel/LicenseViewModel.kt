package com.smartledger.aldaftar.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.smartledger.aldaftar.data.account.UnifiedAccountSession
import com.smartledger.aldaftar.data.account.UnifiedAccountSessionRepository
import com.smartledger.aldaftar.data.license.LicenseRepository
import com.smartledger.aldaftar.domain.license.LicenseSnapshot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class LicenseViewModel(
    application: Application,
    private val repository: LicenseRepository,
    private val unifiedAccountRepository: UnifiedAccountSessionRepository
) : AndroidViewModel(application) {

    val session: StateFlow<UnifiedAccountSession> = unifiedAccountRepository.session

    private val _snapshot = MutableStateFlow(repository.snapshot())
    val snapshot: StateFlow<LicenseSnapshot> = _snapshot.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    private val _deviceReplacedNotice = MutableStateFlow<String?>(null)
    val deviceReplacedNotice: StateFlow<String?> = _deviceReplacedNotice.asStateFlow()

    private val _busy = MutableStateFlow(false)
    val isBusy: StateFlow<Boolean> = _busy.asStateFlow()

    val licenseRequiredEvent: SharedFlow<Unit> = repository.onLicenseRequired

    init {
        viewModelScope.launch {
            unifiedAccountRepository.session.collect { session ->
                _snapshot.value = session.licenseSnapshot
            }
        }

        viewModelScope.launch {
            repository.onDeviceReplaced.collect { notice ->
                _deviceReplacedNotice.value = notice
                _snapshot.value = repository.snapshot()
            }
        }
    }

    fun dismissDeviceReplacedNotice() {
        _deviceReplacedNotice.value = null
    }

    fun isEligibleToCreate(): Boolean = repository.isEligibleToCreate()

    fun triggerLicensePrompt() {
        repository.triggerLicenseRequired()
    }

    fun refresh() {
        viewModelScope.launch(Dispatchers.IO) {
            val snap = repository.snapshot()
            _snapshot.value = snap
            unifiedAccountRepository.updateLicenseSnapshot(snap)
        }
    }

    fun deviceCode(): String = repository.deviceCode()
    fun deviceFingerprint(): String = repository.deviceFingerprint()

    fun signInWithGoogle(
        account: GoogleSignInAccount,
        serverAuthCode: String? = null,
        onDone: (Boolean) -> Unit = {}
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            _busy.value = true
            _message.value = null
            _deviceReplacedNotice.value = null
            try {
                val newSession = unifiedAccountRepository.signInWithGoogle(account, serverAuthCode)
                _snapshot.value = newSession.licenseSnapshot
                if (newSession.licenseSnapshot.isPaid) {
                    _message.value = "تم تسجيل الدخول وتفعيل الترخيص بنجاح"
                } else if (newSession.licenseSnapshot.activationRequired) {
                    _message.value = "تم تسجيل الدخول. الحساب بحاجة لإدخال رمز التفعيل لأول مرة."
                } else {
                    _message.value = "تم تسجيل الدخول بالحساب بنجاح"
                }
                onDone(true)
            } catch (e: Exception) {
                _message.value = userFacingError(e, "تعذر تسجيل الدخول بحساب Google")
                onDone(false)
            } finally {
                _busy.value = false
            }
        }
    }

    fun signOutUnified() {
        viewModelScope.launch(Dispatchers.IO) {
            _busy.value = true
            try {
                unifiedAccountRepository.signOutUnified()
                _snapshot.value = repository.snapshot()
                _message.value = "تم تسجيل الخروج بنجاح"
            } catch (e: Exception) {
                _message.value = e.message ?: "تعذر تسجيل الخروج"
            } finally {
                _busy.value = false
            }
        }
    }

    fun checkCloudLicense(onDone: (Boolean) -> Unit = {}) {
        viewModelScope.launch(Dispatchers.IO) {
            val email = session.value.email
            if (email.isNullOrBlank()) {
                _message.value = "يرجى تسجيل الدخول بحساب Google أولاً"
                onDone(false)
                return@launch
            }
            _busy.value = true
            _message.value = null
            try {
                val newSnap = repository.checkAndAutoActivateCloudAccount(email)
                if (newSnap != null && newSnap.isPaid) {
                    _snapshot.value = newSnap
                    unifiedAccountRepository.updateLicenseSnapshot(newSnap)
                    _message.value = "تم التحقق وتفعيل الترخيص السحابي بنجاح"
                    onDone(true)
                } else if (newSnap != null && newSnap.activationRequired) {
                    _snapshot.value = newSnap
                    unifiedAccountRepository.updateLicenseSnapshot(newSnap)
                    _message.value = "الحساب بحاجة لإدخال كود التفعيل لأول مرة"
                    onDone(false)
                } else {
                    _message.value = "هذا الحساب غير مسجل بترخيص سحابي مفعل بعد"
                    onDone(false)
                }
            } catch (e: Exception) {
                _message.value = userFacingError(e, "تعذر التحقق من الترخيص السحابي")
                onDone(false)
            } finally {
                _busy.value = false
            }
        }
    }

    fun activateWithCode(activationCode: String, onDone: (Boolean) -> Unit = {}) {
        viewModelScope.launch(Dispatchers.IO) {
            _busy.value = true
            _message.value = null
            try {
                val email = session.value.email
                val newSnap = repository.activateWithAccountOrCode(activationCode, email)
                _snapshot.value = newSnap
                unifiedAccountRepository.updateLicenseSnapshot(newSnap)
                _message.value = "تم تفعيل الترخيص بنجاح"
                onDone(true)
            } catch (e: Exception) {
                _message.value = userFacingError(e, "تعذر تفعيل الترخيص")
                onDone(false)
            } finally {
                _busy.value = false
            }
        }
    }

    fun applyToken(token: String, onDone: (Boolean) -> Unit = {}) {
        viewModelScope.launch(Dispatchers.IO) {
            _busy.value = true
            _message.value = null
            try {
                val newSnap = repository.applySignedToken(token)
                _snapshot.value = newSnap
                unifiedAccountRepository.updateLicenseSnapshot(newSnap)
                _message.value = "تم تفعيل الترخيص بنجاح"
                onDone(true)
            } catch (e: Exception) {
                _message.value = e.message ?: "رمز الترخيص غير صالح"
                onDone(false)
            } finally {
                _busy.value = false
            }
        }
    }

    fun reconnectAccount(accountCode: String? = null, onDone: (Boolean) -> Unit = {}) {
        viewModelScope.launch(Dispatchers.IO) {
            _busy.value = true
            _message.value = null
            try {
                val newSnap = repository.reconnectAccountOnline(accountCode)
                _snapshot.value = newSnap
                unifiedAccountRepository.updateLicenseSnapshot(newSnap)
                _message.value = "تمت إعادة ربط الترخيص بنجاح"
                onDone(true)
            } catch (e: Exception) {
                _message.value = userFacingError(e, "تعذر إعادة ربط الترخيص")
                onDone(false)
            } finally {
                _busy.value = false
            }
        }
    }

    fun activateAccount(accountCode: String, activationCode: String, onDone: (Boolean) -> Unit = {}) {
        viewModelScope.launch(Dispatchers.IO) {
            _busy.value = true
            _message.value = null
            try {
                val email = session.value.email
                val newSnap = repository.activateAccountOnline(accountCode, activationCode, email)
                _snapshot.value = newSnap
                unifiedAccountRepository.updateLicenseSnapshot(newSnap)
                _message.value = "تم تفعيل حساب الترخيص بنجاح"
                onDone(true)
            } catch (e: Exception) {
                _message.value = userFacingError(e, "تعذر تفعيل الحساب")
                onDone(false)
            } finally {
                _busy.value = false
            }
        }
    }

    fun supportCodes() = repository.supportCodes()
    fun clearMessage() { _message.value = null }
    private fun userFacingError(error: Throwable, fallback: String): String {
        return when (error) {
            is UnknownHostException, is SocketTimeoutException, is IOException ->
                "تعذر على الحساب الوصول إلى خدمة الترخيص. يرجى المحاولة لاحقاً أو التواصل مع الدعم."
            else -> error.message?.takeIf { it.isNotBlank() } ?: fallback
        }
    }

}
