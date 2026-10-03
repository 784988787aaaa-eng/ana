package com.smartledger.aldaftar.data.repository

import com.smartledger.aldaftar.data.local.BusinessProfileDao
import com.smartledger.aldaftar.data.local.entities.BusinessProfile
import kotlinx.coroutines.flow.Flow

class BusinessProfileRepository(private val dao: BusinessProfileDao) {

    val profileFlow: Flow<BusinessProfile?> = dao.profileFlow()

    suspend fun get(): BusinessProfile = dao.get() ?: BusinessProfile()

    fun getDirect(): BusinessProfile = dao.getDirect() ?: BusinessProfile()

    suspend fun save(profile: BusinessProfile) {
        val sanitizedPhones = profile.phones
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .distinct()

        val sanitizedProfile = profile.copy(
            id = 1,
            name = profile.name.trim(),
            description = profile.description.trim(),
            logoPath = profile.logoPath,
            phones = sanitizedPhones
        )
        dao.save(sanitizedProfile)
    }

    suspend fun clearProfile() {
        dao.deleteProfile()
    }
}
