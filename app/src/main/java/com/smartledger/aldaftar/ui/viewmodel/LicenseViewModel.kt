package com.smartledger.aldaftar.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.smartledger.aldaftar.data.license.LicenseRepository
import com.smartledger.aldaftar.domain.license.LicenseSnapshot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
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

    fun refresh() { _snapshot.value = repository.snapshot() }
    fun deviceCode(): String = repository.deviceCode()
    fun activateAccount(accountCode: String, activationCode: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _busy.value = true
            try {
                runCatching { repository.activateAccountOnline(accountCode, activationCode) }
                    .onSuccess { _snapshot.value = it; _message.value = "تم تفعيل حساب الترخيص" }
                    .onFailure { _message.value = it.message ?: "تعذر تفعيل الحساب" }
            } finally { _busy.value = false }
        }
    }

    fun applyToken(token: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _busy.value = true
            try {
                runCatching { repository.applySignedToken(token) }
                    .onSuccess { _snapshot.value = it; _message.value = "تم تفعيل الترخيص" }
                    .onFailure { _message.value = it.message ?: "رمز الترخيص غير صالح" }
            } finally { _busy.value = false }
        }
    }
    fun supportCodes() = repository.supportCodes()
    fun reconnectAccount(accountCode: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _busy.value = true
            try {
                runCatching { repository.reconnectAccountOnline(accountCode) }
                    .onSuccess { _snapshot.value = it; _message.value = "تمت إعادة ربط الترخيص" }
                    .onFailure { _message.value = it.message ?: "تعذر إعادة ربط الترخيص" }
            } finally { _busy.value = false }
        }
    }

    fun signOutAccount() { repository.signOutAccount(); refresh() }
    fun clearMessage() { _message.value = null }
}
