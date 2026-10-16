package com.smartledger.aldaftar.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.smartledger.aldaftar.R
import com.smartledger.aldaftar.data.account.UnifiedAccountSession
import com.smartledger.aldaftar.data.account.UnifiedAccountSessionRepository
import com.smartledger.aldaftar.data.license.LicenseRepository
import com.smartledger.aldaftar.data.repository.HabayebRepository
import com.smartledger.aldaftar.domain.license.LicenseSnapshot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LicenseViewModel(
    application: Application,
    private val repository: LicenseRepository,
    private val unifiedAccountRepository: UnifiedAccountSessionRepository,
    private val habayebRepository: HabayebRepository
) : AndroidViewModel(application) {

    private val appContext = application.applicationContext

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
            habayebRepository.getHabayebTransactionsCountFlow().collect { count ->
                repository.syncTrialUsedWithCount(count)
                val snap = repository.snapshot()
                _snapshot.value = snap
                unifiedAccountRepository.updateLicenseSnapshot(snap)
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
                    _message.value = appContext.getString(R.string.license_vm_signin_paid)
                } else if (newSession.licenseSnapshot.activationRequired) {
                    _message.value = appContext.getString(R.string.license_vm_signin_needs_code)
                } else {
                    _message.value = appContext.getString(R.string.license_vm_signin_ok)
                }
                onDone(true)
            } catch (e: Exception) {
                _message.value = e.message ?: appContext.getString(R.string.license_vm_signin_err)
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
                _message.value = appContext.getString(R.string.license_vm_signout_ok)
            } catch (e: Exception) {
                _message.value = e.message ?: appContext.getString(R.string.license_vm_signout_err)
            } finally {
                _busy.value = false
            }
        }
    }

    fun checkCloudLicense(onDone: (Boolean) -> Unit = {}) {
        viewModelScope.launch(Dispatchers.IO) {
            val email = session.value.email
            if (email.isNullOrBlank()) {
                _message.value = appContext.getString(R.string.license_vm_signin_first)
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
                    _message.value = appContext.getString(R.string.license_vm_cloud_activated)
                    onDone(true)
                } else if (newSnap != null && newSnap.activationRequired) {
                    _snapshot.value = newSnap
                    unifiedAccountRepository.updateLicenseSnapshot(newSnap)
                    _message.value = appContext.getString(R.string.license_vm_cloud_needs_code)
                    onDone(false)
                } else {
                    _message.value = appContext.getString(R.string.license_vm_cloud_not_registered)
                    onDone(false)
                }
            } catch (e: Exception) {
                _message.value = e.message ?: appContext.getString(R.string.license_vm_cloud_check_err)
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
                _message.value = appContext.getString(R.string.license_vm_activate_ok)
                onDone(true)
            } catch (e: Exception) {
                _message.value = e.message ?: appContext.getString(R.string.license_vm_activate_err)
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
                _message.value = appContext.getString(R.string.license_vm_activate_ok)
                onDone(true)
            } catch (e: Exception) {
                _message.value = e.message ?: appContext.getString(R.string.license_vm_invalid_token)
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
                _message.value = appContext.getString(R.string.license_vm_reconnect_ok)
                onDone(true)
            } catch (e: Exception) {
                _message.value = e.message ?: appContext.getString(R.string.license_vm_reconnect_err)
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
                _message.value = appContext.getString(R.string.license_vm_account_activate_ok)
                onDone(true)
            } catch (e: Exception) {
                _message.value = e.message ?: appContext.getString(R.string.license_vm_account_activate_err)
                onDone(false)
            } finally {
                _busy.value = false
            }
        }
    }

    fun supportCodes() = repository.supportCodes()
    fun clearMessage() { _message.value = null }
}
