package com.smartledger.aldaftar.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartledger.aldaftar.data.license.LicenseRepository
import com.smartledger.aldaftar.domain.admin.AdminAuthValidator
import com.smartledger.aldaftar.domain.admin.AdminIssuedLicense
import com.smartledger.aldaftar.domain.admin.AdminLicenseRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class AdminLicenseViewModel(
    private val adminRepository: AdminLicenseRepository,
    private val licenseRepository: LicenseRepository
) : ViewModel() {

    private val _isAuthorized = MutableStateFlow(false)
    val isAuthorized: StateFlow<Boolean> = _isAuthorized.asStateFlow()

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    // Form inputs
    val accountOrDeviceCode = MutableStateFlow("")
    val customerEmail = MutableStateFlow("")
    val customerPhone = MutableStateFlow("")
    val customerName = MutableStateFlow("")
    val selectedLicenseType = MutableStateFlow("LOCAL") // LOCAL, ACCOUNT
    val selectedPlan = MutableStateFlow("LIFETIME")    // LIFETIME, TRIAL
    val trialDays = MutableStateFlow("30")
    val maxDevices = MutableStateFlow("1")
    val notes = MutableStateFlow("")

    private val _lastIssuedLicense = MutableStateFlow<AdminIssuedLicense?>(null)
    val lastIssuedLicense: StateFlow<AdminIssuedLicense?> = _lastIssuedLicense.asStateFlow()

    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    val searchQuery = MutableStateFlow("")

    val allLicenses: StateFlow<List<AdminIssuedLicense>> = combine(
        adminRepository.getAllLicenses(),
        searchQuery
    ) { list, query ->
        val q = query.trim().lowercase()
        if (q.isBlank()) {
            list
        } else {
            list.filter {
                it.customerName.lowercase().contains(q) ||
                it.customerEmail.lowercase().contains(q) ||
                it.customerPhone.lowercase().contains(q) ||
                it.accountCode.lowercase().contains(q) ||
                it.licenseId.lowercase().contains(q) ||
                it.shortActivationCode.lowercase().contains(q) ||
                it.notes.lowercase().contains(q)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun authenticate(code: String): Boolean {
        _authError.value = null
        val ok = AdminAuthValidator.isAuthorized(code)
        if (ok) {
            _isAuthorized.value = true
            _authError.value = null
        } else {
            _isAuthorized.value = false
            _authError.value = "رمز الإدارة غير صحيح، الدخول مقيد فقط للإدارة المصرح لها"
        }
        return ok
    }

    fun lock() {
        _isAuthorized.value = false
        _authError.value = null
    }

    fun clearMessages() {
        _errorMessage.value = null
        _successMessage.value = null
        _authError.value = null
    }

    fun getCurrentDeviceCode(): String = licenseRepository.deviceCode()

    fun fillCurrentDeviceCode() {
        accountOrDeviceCode.value = getCurrentDeviceCode()
    }

    fun generateLicense() {
        val code = accountOrDeviceCode.value.trim()
        val email = customerEmail.value.trim()
        val phone = customerPhone.value.trim()
        val name = customerName.value.trim()
        val type = selectedLicenseType.value
        val plan = selectedPlan.value
        val days = trialDays.value.toIntOrNull() ?: 0
        val maxDev = maxDevices.value.toIntOrNull() ?: 1

        if (code.isBlank()) {
            _errorMessage.value = "يرجى إدخال كود الحساب أو معرف الجهاز"
            return
        }

        viewModelScope.launch {
            _isProcessing.value = true
            _errorMessage.value = null
            _successMessage.value = null
            try {
                val result = adminRepository.issueLicense(
                    deviceOrAccountCode = code,
                    email = email,
                    customerPhone = phone,
                    customerName = if (name.isNotBlank()) name else "مشترك جديد",
                    licenseType = type,
                    plan = plan,
                    maxDevices = maxDev,
                    trialDays = if (plan == "TRIAL") days else 0,
                    notes = notes.value.trim()
                )
                _lastIssuedLicense.value = result
                _successMessage.value = "تم توليد وتوقيع الترخيص بنجاح وحفظه في السجل"
            } catch (e: Exception) {
                _errorMessage.value = "فشل توليد الترخيص: ${e.message}"
            } finally {
                _isProcessing.value = false
            }
        }
    }

    fun activateCurrentDeviceWithIssued(token: String, onResult: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            try {
                licenseRepository.applySignedToken(token)
                _successMessage.value = "تم تفعيل الترخيص بنجاح على هذا الجهاز"
                onResult(true)
            } catch (e: Exception) {
                _errorMessage.value = "تعذر تفعيل هذا الجهاز: ${e.message}"
                onResult(false)
            }
        }
    }

    fun toggleLicenseStatus(license: AdminIssuedLicense) {
        viewModelScope.launch {
            try {
                adminRepository.toggleStatus(license)
            } catch (e: Exception) {
                _errorMessage.value = "تعذر تعديل حالة الترخيص: ${e.message}"
            }
        }
    }

    fun deleteLicense(license: AdminIssuedLicense) {
        viewModelScope.launch {
            try {
                adminRepository.deleteLicense(license)
                if (_lastIssuedLicense.value?.licenseId == license.licenseId) {
                    _lastIssuedLicense.value = null
                }
            } catch (e: Exception) {
                _errorMessage.value = "تعذر حذف الترخيص: ${e.message}"
            }
        }
    }
}
