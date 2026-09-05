package com.smartledger.aldaftar.security

import android.content.Context
import com.smartledger.aldaftar.BuildConfig
import com.google.android.play.core.integrity.IntegrityManagerFactory
import com.google.android.play.core.integrity.StandardIntegrityManager
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import java.security.MessageDigest

/**
 * عميل Play Integrity Standard API.
 * يحصل التطبيق على token موقّع؛ لا يتم تفسير verdict داخل APK لأن Google تنص على أن
 * فك الرمز والتحقق من verdict يجب أن يتم على خادم موثوق. لذلك لا نضع مفتاح خدمة Google داخل APK.
 */
class PlayIntegrityGate(context: Context) {
    private val manager = IntegrityManagerFactory.createStandard(context.applicationContext)
    @Volatile private var provider: StandardIntegrityManager.StandardIntegrityTokenProvider? = null

    suspend fun prepare(): Boolean {
        val projectNumber = BuildConfig.PLAY_INTEGRITY_CLOUD_PROJECT_NUMBER
        if (projectNumber <= 0L) return false
        return try {
            provider = awaitPrepare(projectNumber)
            true
        } catch (_: Throwable) {
            false
        }
    }

    suspend fun requestToken(requestPayload: String): String? {
        val current = provider ?: return null
        val requestHash = MessageDigest.getInstance("SHA-256")
            .digest(requestPayload.toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it.toInt() and 0xff) }
        return try {
            suspendCancellableCoroutine { continuation ->
                current.request(
                    StandardIntegrityManager.StandardIntegrityTokenRequest.builder()
                        .setRequestHash(requestHash)
                        .build()
                ).addOnSuccessListener { response ->
                    if (continuation.isActive) continuation.resume(response.token())
                }.addOnFailureListener { error ->
                    if (continuation.isActive) continuation.resumeWithException(error)
                }
            }
        } catch (_: Throwable) {
            null
        }
    }

    private suspend fun awaitPrepare(projectNumber: Long): StandardIntegrityManager.StandardIntegrityTokenProvider =
        suspendCancellableCoroutine { continuation ->
            manager.prepareIntegrityToken(
                StandardIntegrityManager.PrepareIntegrityTokenRequest.builder()
                    .setCloudProjectNumber(projectNumber)
                    .build()
            ).addOnSuccessListener { result ->
                if (continuation.isActive) continuation.resume(result)
            }.addOnFailureListener { error ->
                if (continuation.isActive) continuation.resumeWithException(error)
            }
        }
}
