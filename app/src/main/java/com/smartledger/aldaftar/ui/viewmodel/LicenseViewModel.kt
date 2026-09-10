package com.smartledger.aldaftar.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.smartledger.aldaftar.data.license.LicenseRepository
import com.smartledger.aldaftar.domain.license.LicenseSnapshot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LicenseViewModel(application: Application, private val repository: LicenseRepository) : AndroidViewModel(application) {
    private val _snapshot = MutableStateFlow(repository.snapshot())
    val snapshot: StateFlow<LicenseSnapshot> = _snapshot.asStateFlow()
    private val _message = MutableStateFlow<String?>(null)
    private val _busy = MutableStateFlow(false)
    val isBusy: StateFlow<Boolean> = _busy.asStateFlow()
    val message: StateFlow<String?> = _message.asStateFlow()

    val licenseRequiredEvent: SharedFlow<Unit> = repository.onLicenseRequired

    fun isEligibleToCreate(): Boolean = repository.isEligibleToCreate()

    fun triggerLicensePrompt() {
        repository.triggerLicenseRequired()
    }

    fun refresh() { _snapshot.value = repository.snapshot() }
    fun deviceCode(): String = repository.deviceCode()
    fun activateAccount(accountCode: String, activationCode: String, onDone: (Boolean) -> Unit = {}) {
        viewModelScope.launch(Dispatchers.IO) {
            _busy.value = true
            try {
                runCatching { repository.activateAccountOnline(accountCode, activationCode) }
                    .onSuccess {
                        _snapshot.value = it
                        _message.value = "تم تفعيل حساب الترخيص"
                        onDone(true)
                    }
                    .onFailure {
                        _message.value = it.message ?: "تعذر تفعيل الحساب"
                        onDone(false)
                    }
            } finally { _busy.value = false }
        }
    }

    fun applyToken(token: String, onDone: (Boolean) -> Unit = {}) {
        viewModelScope.launch(Dispatchers.IO) {
            _busy.value = true
            try {
                runCatching { repository.applySignedToken(token) }
                    .onSuccess {
                        _snapshot.value = it
                        _message.value = "تم تفعيل الترخيص بنجاح"
                        onDone(true)
                    }
                    .onFailure {
                        _message.value = it.message ?: "رمز الترخيص غير صالح"
                        onDone(false)
                    }
            } finally { _busy.value = false }
        }
    }
    fun supportCodes() = repository.supportCodes()
    fun reconnectAccount(accountCode: String, onDone: (Boolean) -> Unit = {}) {
        viewModelScope.launch(Dispatchers.IO) {
            _busy.value = true
            try {
                runCatching { repository.reconnectAccountOnline(accountCode) }
                    .onSuccess {
                        _snapshot.value = it
                        _message.value = "تمت إعادة ربط الترخيص بنجاح"
                        onDone(true)
                    }
                    .onFailure {
                        _message.value = it.message ?: "تعذر إعادة ربط الترخيص"
                        onDone(false)
                    }
            } finally { _busy.value = false }
        }
    }

    fun signOutAccount() { repository.signOutAccount(); refresh() }
    fun clearMessage() { _message.value = null }
}
