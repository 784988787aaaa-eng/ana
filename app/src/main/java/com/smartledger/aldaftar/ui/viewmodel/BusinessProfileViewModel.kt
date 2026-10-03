package com.smartledger.aldaftar.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartledger.aldaftar.data.local.entities.BusinessProfile
import com.smartledger.aldaftar.data.repository.BusinessProfileRepository
import com.smartledger.aldaftar.domain.business.BusinessPhoneFormatter
import com.smartledger.aldaftar.domain.business.PhoneValidationResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class PhoneInputData(
    val id: Int,
    val countryCode: String = BusinessPhoneFormatter.DEFAULT_COUNTRY_CODE,
    val nationalNumber: String = ""
)

data class BusinessProfileUiState(
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val errorMessage: String? = null,
    val nameError: String? = null,
    val phoneErrors: Map<Int, String> = emptyMap()
)

class BusinessProfileViewModel(private val repository: BusinessProfileRepository) : ViewModel() {

    val profile: StateFlow<BusinessProfile> = repository.profileFlow
        .map { it ?: BusinessProfile() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = BusinessProfile()
        )

    private val _uiState = MutableStateFlow(BusinessProfileUiState())
    val uiState: StateFlow<BusinessProfileUiState> = _uiState.asStateFlow()

    fun clearSaveState() {
        _uiState.value = _uiState.value.copy(saveSuccess = false, errorMessage = null)
    }

    fun saveProfile(
        name: String,
        description: String,
        logoPath: String,
        phoneInputs: List<PhoneInputData>,
        onSuccess: () -> Unit = {}
    ) {
        val trimmedName = name.trim()
        if (trimmedName.isBlank()) {
            _uiState.value = _uiState.value.copy(
                nameError = "اسم النشاط التجاري مطلوب",
                errorMessage = "يرجى كتابة اسم النشاط التجاري"
            )
            return
        }

        // التحقق من أرقام الهواتف
        val phoneErrors = mutableMapOf<Int, String>()
        val formattedPhones = mutableListOf<String>()

        phoneInputs.forEachIndexed { index, phoneData ->
            val validation = BusinessPhoneFormatter.validateNationalNumber(phoneData.nationalNumber)
            when (validation) {
                is PhoneValidationResult.Invalid -> {
                    phoneErrors[index] = validation.message
                }
                is PhoneValidationResult.Valid -> {
                    val combined = BusinessPhoneFormatter.formatCombinedPhone(phoneData.countryCode, phoneData.nationalNumber)
                    if (combined != null) {
                        formattedPhones.add(combined)
                    }
                }
                is PhoneValidationResult.Empty -> {
                    // إهمال الرقم الفارغ بحرية دون إجبار ودون تخزين قيمة مشوهة
                }
            }
        }

        if (phoneErrors.isNotEmpty()) {
            _uiState.value = _uiState.value.copy(
                phoneErrors = phoneErrors,
                errorMessage = "يرجى تصحيح أرقام الهواتف المدخلة"
            )
            return
        }

        _uiState.value = _uiState.value.copy(isSaving = true, errorMessage = null, nameError = null, phoneErrors = emptyMap())

        viewModelScope.launch {
            try {
                val updatedProfile = BusinessProfile(
                    id = 1,
                    name = trimmedName,
                    description = description.trim(),
                    logoPath = logoPath,
                    phones = formattedPhones.distinct()
                )
                repository.save(updatedProfile)
                _uiState.value = _uiState.value.copy(isSaving = false, saveSuccess = true, errorMessage = null)
                onSuccess()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    saveSuccess = false,
                    errorMessage = "حدث خطأ أثناء الحفظ: ${e.localizedMessage ?: "فشلت العملية"}"
                )
            }
        }
    }

    suspend fun resetProfile() {
        try {
            repository.clearProfile()
            _uiState.value = BusinessProfileUiState()
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(errorMessage = "تعذر إعادة الضبط")
        }
    }
}
