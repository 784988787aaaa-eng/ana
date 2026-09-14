package com.smartledger.aldaftar.data.repository
import com.smartledger.aldaftar.data.local.BusinessProfileDao
import com.smartledger.aldaftar.data.local.entities.BusinessProfile
import kotlinx.coroutines.flow.Flow
class BusinessProfileRepository(private val dao:BusinessProfileDao){
 val profileFlow:Flow<BusinessProfile?> = dao.profileFlow()
 suspend fun get():BusinessProfile = dao.get() ?: BusinessProfile()
 fun getDirect():BusinessProfile = dao.getDirect() ?: BusinessProfile()
 suspend fun save(profile:BusinessProfile)=dao.save(profile.copy(id=1, phones=profile.phones.map(String::trim).filter(String::isNotBlank)))
}
