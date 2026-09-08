package com.smartledger.aldaftar.domain

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Representation of Customer Support Identity state.
 * Opaque customer service identifier designed strictly for support communication.
 * NEVER derived directly from email, Firebase UID, or hardware secrets.
 */
sealed class SupportIdentityState {
    data object Idle : SupportIdentityState()
    data object Loading : SupportIdentityState()
    data class Available(val supportId: String) : SupportIdentityState()
    data class Unavailable(val message: String? = null) : SupportIdentityState()
}

/**
 * Clean abstraction decoupling UI from the future cloud Support ID provider.
 */
interface SupportIdentityRepository {
    /**
     * Attempts to resolve Support ID:
     * 1. Returns securely cached Support ID if previously issued by the backend.
     * 2. Calls future GET /v1/support/identity on Cloudflare Worker.
     * 3. If unavailable (e.g. 404 or missing), cleanly returns [SupportIdentityState.Unavailable].
     * NEVER invents fake IDs, and NEVER falls back to email, UID, or device ID.
     */
    suspend fun getSupportIdentity(): SupportIdentityState

    /**
     * Returns the cached Support ID from encrypted storage, or null if not yet issued.
     */
    fun getCachedSupportId(): String?

    /**
     * Clears cached Support ID on account change or account change.
     */
    fun clearSupportIdentity()
}

/**
 * Default production implementation of [SupportIdentityRepository].
 */
class SupportIdentityRepositoryImpl(
    private val context: Context
) : SupportIdentityRepository {

    private val securityManager = AppSecurityManager.getInstance(context.applicationContext)
    private val client = SupportIdentityClient(context.applicationContext)

    override suspend fun getSupportIdentity(): SupportIdentityState = withContext(Dispatchers.IO) {
        val cached = getCachedSupportId()
        if (!cached.isNullOrBlank()) {
            return@withContext SupportIdentityState.Available(cached)
        }

        client.fetch().fold(
            onSuccess = { supportId ->
                securityManager.saveSupportId(supportId)
                SupportIdentityState.Available(supportId)
            },
            onFailure = { error ->
                SupportIdentityState.Unavailable(error.message)
            }
        )
    }

    override fun getCachedSupportId(): String? {
        val id = securityManager.getSupportId()
        return id.takeIf { it.isNotBlank() }
    }

    override fun clearSupportIdentity() {
        securityManager.clearSupportId()
    }
}
