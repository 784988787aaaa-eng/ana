package com.smartledger.aldaftar.domain

import android.content.Context
import android.util.Log
import com.google.firebase.appcheck.FirebaseAppCheck
import com.smartledger.aldaftar.BuildConfig
import com.google.firebase.auth.FirebaseAuth
import com.smartledger.aldaftar.R
import com.smartledger.aldaftar.data.repository.LicenseAndTrialManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.Locale
import java.util.concurrent.TimeUnit

sealed class LicenseCheckResult {
    data class Success(
        val email: String,
        val deviceId: String,
        val isTransferred: Boolean = false
    ) : LicenseCheckResult()

    data class NotLicensed(val email: String, val message: String) : LicenseCheckResult()
    data class NetworkOutage(val message: String) : LicenseCheckResult()
    data class Error(val message: String) : LicenseCheckResult()
}

/**
 * Client facade for the Cloudflare Worker license authority.
 * Firebase Authentication remains the identity provider; Android never writes license state directly.
 */
object FirebaseLicenseManager {
    private const val TAG = "FirebaseLicenseManager"
    private const val PATH_ACTIVATE = "/v1/license/activate"
    private const val PATH_REFRESH = "/v1/license/refresh"
    private const val PATH_UNLINK = "/v1/license/unlink"
    private const val PATH_SESSION = "/v1/license/session/"
    private const val PATH_SUPPORT_IDENTITY = "/v1/support/identity"
    private const val POLL_INTERVAL_MS = 60_000L

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .writeTimeout(20, TimeUnit.SECONDS)
        .build()

    private val monitorScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var monitorJob: Job? = null

    private fun normalizeEmail(email: String): String = email.trim().lowercase(Locale.ROOT)

    private fun backendUrl(): String = BuildConfig.LICENSE_BACKEND_URL.trim().trimEnd('/')

    private fun requireBackendUrl(): String {
        val url = backendUrl()
        check(url.startsWith("https://") && url.length > "https://".length) {
            "License backend URL is not configured"
        }
        return url
    }

    private suspend fun authHeaders(): Pair<String, String> {
        val user = FirebaseAuth.getInstance().currentUser
            ?: throw LicenseClientException(401, "Authentication is required.")
        val idToken = user.getIdToken(false).await()?.token
            ?: throw LicenseClientException(401, "Authentication token is unavailable.")
        val appCheckToken = FirebaseAppCheck.getInstance().getAppCheckToken(false).await().token
        return idToken to appCheckToken
    }

    private suspend fun post(path: String, body: JSONObject): HttpResult {
        val (idToken, appCheckToken) = authHeaders()
        val request = Request.Builder()
            .url(requireBackendUrl() + path)
            .header("Authorization", "Bearer $idToken")
            .header("X-Firebase-AppCheck", appCheckToken)
            .header("Accept", "application/json")
            .post(body.toString().toRequestBody("application/json; charset=utf-8".toMediaType()))
            .build()
        return execute(request)
    }

    private suspend fun get(path: String): HttpResult {
        val (idToken, appCheckToken) = authHeaders()
        val request = Request.Builder()
            .url(requireBackendUrl() + path)
            .header("Authorization", "Bearer $idToken")
            .header("X-Firebase-AppCheck", appCheckToken)
            .header("Accept", "application/json")
            .get()
            .build()
        return execute(request)
    }

    private suspend fun execute(request: Request): HttpResult {
        return kotlinx.coroutines.withContext(Dispatchers.IO) {
            try {
                httpClient.newCall(request).execute().use { response ->
                    val raw = response.body?.string().orEmpty()
                    val json = runCatching { JSONObject(raw) }.getOrNull()
                    val message = json?.optJSONObject("error")?.optString("message")
                        ?.takeIf { it.isNotBlank() }
                        ?: json?.optString("message")?.takeIf { it.isNotBlank() }
                        ?: response.message
                    HttpResult(response.code, json, message)
                }
            } catch (t: Throwable) {
                throw LicenseClientException(0, t.message ?: "Network error")
            }
        }
    }

    suspend fun verifyAndActivateEmail(
        context: Context,
        email: String,
        currentDeviceId: String
    ): LicenseCheckResult {
        val cleanEmail = normalizeEmail(email)
        val user = FirebaseAuth.getInstance().currentUser
            ?: return LicenseCheckResult.Error(context.getString(R.string.licensing_error_connection))
        val authEmail = normalizeEmail(user.email.orEmpty())
        if (authEmail.isBlank() || authEmail != cleanEmail) {
            return LicenseCheckResult.NotLicensed(
                cleanEmail,
                context.getString(R.string.licensing_error_account_disabled)
            )
        }

        return try {
            val result = post(
                PATH_ACTIVATE,
                JSONObject()
                    .put("deviceId", currentDeviceId)
                    .put("appVersion", currentAppVersion(context))
            )
            when {
                result.code in 200..299 -> {
                    persistLease(context, result.requireJson(), cleanEmail, currentDeviceId)
                    LicenseCheckResult.Success(
                        email = cleanEmail,
                        deviceId = currentDeviceId,
                        isTransferred = result.requireJson().optBoolean("replaced", false)
                    )
                }
                result.code == 401 || result.code == 403 || result.code == 404 ->
                    LicenseCheckResult.NotLicensed(cleanEmail, result.message)
                result.code == 408 || result.code == 429 || result.code >= 500 ->
                    LicenseCheckResult.NetworkOutage(context.getString(R.string.licensing_error_no_internet))
                else -> LicenseCheckResult.Error(result.message)
            }
        } catch (e: LicenseClientException) {
            if (e.status in 401..403) LicenseCheckResult.NotLicensed(cleanEmail, e.message.orEmpty())
            else LicenseCheckResult.NetworkOutage(context.getString(R.string.licensing_error_no_internet))
        } catch (t: Throwable) {
            Log.w(TAG, "License activation failed safely: ${t.javaClass.simpleName}")
            LicenseCheckResult.NetworkOutage(context.getString(R.string.licensing_error_no_internet))
        }
    }

    suspend fun unlinkDevice(context: Context, email: String, currentDeviceId: String): Boolean {
        val user = FirebaseAuth.getInstance().currentUser ?: return false
        if (!normalizeEmail(user.email.orEmpty()).equals(normalizeEmail(email), ignoreCase = true)) return false
        return try {
            val result = post(PATH_UNLINK, JSONObject().put("deviceId", currentDeviceId))
            if (result.code in 200..299) {
                stopRealtimeLicenseMonitoring()
                true
            } else false
        } catch (t: Throwable) {
            Log.w(TAG, "Current-device license unlink failed: ${t.javaClass.simpleName}")
            false
        }
    }

    /**
     * Future-ready contract to fetch opaque customer Support ID from the license worker.
     * GET /v1/support/identity
     * Expected response: { "supportId": "SD-XXXX-XXXX" }
     * Returns failure cleanly if endpoint is not implemented or unavailable.
     */
    suspend fun fetchSupportIdentity(): Result<String> {
        return try {
            val result = get(PATH_SUPPORT_IDENTITY)
            if (result.code in 200..299) {
                val sid = result.requireJson().optString("supportId").trim()
                if (sid.isNotBlank()) {
                    Result.success(sid)
                } else {
                    Result.failure(LicenseClientException(result.code, "Empty support ID received"))
                }
            } else {
                Result.failure(LicenseClientException(result.code, result.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun syncAndVerifyLocalEmailLicense(context: Context): Boolean {
        val security = AppSecurityManager.getInstance(context.applicationContext)
        val email = security.getActivatedEmail()
        val deviceId = LicenseManager.getOrGenerateUnifiedDeviceId(context)
        val sessionId = security.getLicenseSessionId()
        if (email.isBlank() || sessionId.isBlank()) return false

        val localValid = LicenseAndTrialManager(context).isAppActivated()
        if (!localValid) return false

        val user = FirebaseAuth.getInstance().currentUser
        if (user == null || !normalizeEmail(user.email.orEmpty()).equals(normalizeEmail(email), ignoreCase = true)) {
            return localValid
        }

        return try {
            val result = post(
                PATH_REFRESH,
                JSONObject().put("deviceId", deviceId).put("sessionId", sessionId)
            )
            when {
                result.code in 200..299 -> {
                    persistLease(context, result.requireJson(), email, deviceId)
                    true
                }
                result.code == 401 || result.code == 403 || result.code == 404 -> {
                    security.clearActivationData()
                    false
                }
                else -> localValid
            }
        } catch (_: Throwable) {
            localValid
        }
    }

    fun startRealtimeLicenseMonitoring(
        context: Context,
        email: String,
        currentDeviceId: String,
        onKickedOrDisabled: (reason: String) -> Unit
    ) {
        val security = AppSecurityManager.getInstance(context.applicationContext)
        val sessionId = security.getLicenseSessionId()
        if (sessionId.isBlank()) return

        stopRealtimeLicenseMonitoring()
        monitorJob = monitorScope.launch {
            while (isActive) {
                try {
                    val result = get(PATH_SESSION + java.net.URLEncoder.encode(sessionId, Charsets.UTF_8.name()))
                    if (result.code == 403 || result.code == 404) {
                        security.clearActivationData()
                        onKickedOrDisabled(context.getString(R.string.licensing_device_kicked))
                        break
                    }
                    if (result.code in 200..299) {
                        val status = result.requireJson().optString("status")
                        if (status != "active") {
                            security.clearActivationData()
                            onKickedOrDisabled(context.getString(R.string.licensing_device_kicked))
                            break
                        }
                    }
                } catch (t: Throwable) {
                    Log.w(TAG, "License session polling failed safely: ${t.javaClass.simpleName}")
                }
                delay(POLL_INTERVAL_MS)
            }
        }
    }

    fun stopRealtimeLicenseMonitoring() {
        monitorJob?.cancel()
        monitorJob = null
    }

    private fun persistLease(
        context: Context,
        data: JSONObject,
        email: String,
        deviceId: String
    ) {
        val sessionId = data.optString("sessionId").takeIf { it.isNotBlank() }
            ?: error("Missing license session")
        val signature = data.optString("signature").takeIf { it.isNotBlank() }
            ?: error("Missing license signature")
        val lease = data.optJSONObject("lease") ?: error("Missing license lease")
        val leaseJson = lease.toString()
        val verified = LicenseLeaseVerifier.isValidForDevice(
            leaseJson,
            signature,
            email,
            deviceId
        )
        check(verified) { "Server returned an invalid license lease" }

        AppSecurityManager.getInstance(context.applicationContext).saveLicenseLease(
            email = email,
            deviceId = deviceId,
            sessionId = sessionId,
            leaseJson = leaseJson,
            signature = signature
        )
    }

    private fun currentAppVersion(context: Context): String =
        runCatching {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName.orEmpty()
        }.getOrDefault("unknown")

    private data class HttpResult(val code: Int, val json: JSONObject?, val message: String) {
        fun requireJson(): JSONObject = json ?: error("Invalid license response")
    }

    private class LicenseClientException(val status: Int, message: String) : Exception(message)
}
