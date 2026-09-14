package com.smartledger.aldaftar.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartledger.aldaftar.data.local.entities.BusinessProfile
import com.smartledger.aldaftar.data.repository.BusinessProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BusinessProfileViewModel(private val repository: BusinessProfileRepository) : ViewModel() {
    private val _profile = MutableStateFlow(BusinessProfile())
    val profile: StateFlow<BusinessProfile> = _profile.asStateFlow()
    init { viewModelScope.launch { _profile.value = repository.get() } }
    suspend fun save(profile: BusinessProfile) { repository.save(profile); _profile.value = profile }
    suspend fun resetProfile() { repository.clearProfile(); _profile.value = BusinessProfile() }
}
