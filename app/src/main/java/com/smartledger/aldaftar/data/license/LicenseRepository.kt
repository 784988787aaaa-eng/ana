package com.smartledger.aldaftar.data.license

import android.content.Context
import com.smartledger.aldaftar.domain.license.*
import com.smartledger.aldaftar.platform.license.DeviceIdentity
import com.smartledger.aldaftar.platform.license.LicenseCrypto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID

class LicenseRepository(private val context: Context) {
    companion object { const val TRIAL_LIMIT = 100; private const val VERIFY_DAYS = 30L * 24 * 60 * 60 * 1000 }
    private val store = LicenseStore(context)
    private val crypto = LicenseCrypto(context)
    private val device = DeviceIdentity()
    private val creationMutex = Mutex()
    private val _onLicenseRequired = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val onLicenseRequired: SharedFlow<Unit> = _onLicenseRequired.asSharedFlow()

    fun triggerLicenseRequired() {
        _onLicenseRequired.tryEmit(Unit)
    }

    fun isEligibleToCreate(): Boolean {
        val state = snapshot()
        if (state.status == LicenseStatus.REVOKED || state.status == LicenseStatus.VERIFICATION_REQUIRED) return false
        if (state.isPaid) return true
        return store.trialUsed < TRIAL_LIMIT
    }

    fun snapshot(now: Long = System.currentTimeMillis()): LicenseSnapshot {
        if (store.serverRevoked) return LicenseSnapshot(status = LicenseStatus.REVOKED, accountCode = store.accountCode, trialUsed = store.trialUsed)
        val token = store.token ?: return LicenseSnapshot(trialUsed = store.trialUsed)
        return runCatching { parse(crypto.verify(token), now) }.getOrElse { LicenseSnapshot(status = LicenseStatus.REVOKED, trialUsed = store.trialUsed) }
    }

    fun deviceCode(): String = device.deviceCode()

    fun applySignedToken(token: String): LicenseSnapshot {
        val body = crypto.verify(token)
        require(body.optString("product") == "SMARTLEDGER") { "الترخيص يخص منتجاً آخر" }
        val type = LicenseType.valueOf(body.getString("type"))
        if (type == LicenseType.LOCAL) require(body.getString("deviceCode") == device.deviceCode()) { "الترخيص غير مرتبط بهذا الجهاز" }
        if (type == LicenseType.ACCOUNT) {
            require(body.optString("accountCode").isNotBlank()) { "ترخيص الحساب ناقص" }
            require(body.optString("deviceFingerprint") == device.fingerprint()) { "جلسة الترخيص لا تخص هذا الجهاز" }
        }
        store.token = token
        store.serverRevoked = false
        store.accountCode = body.optString("accountCode").takeIf { it.isNotBlank() }
        store.lastVerifiedAt = System.currentTimeMillis()
        return parse(body, System.currentTimeMillis())
    }

    suspend fun <T> runAuthorizedCreation(block: suspend () -> T): T? = creationMutex.withLock {
        val state = snapshot()
        if (state.status == LicenseStatus.REVOKED || state.status == LicenseStatus.VERIFICATION_REQUIRED) {
            _onLicenseRequired.tryEmit(Unit)
            return@withLock null
        }
        if (state.isPaid) return@withLock block()
        if (store.trialUsed >= TRIAL_LIMIT) {
            _onLicenseRequired.tryEmit(Unit)
            return@withLock null
        }
        store.trialUsed += 1
        try {
            block()
        } catch (t: Throwable) {
            store.trialUsed = (store.trialUsed - 1).coerceAtLeast(0)
            throw t
        }
    }

    fun supportCodes(): Pair<String, String> {
        val account = store.accountCode ?: "غير مرتبط"
        val request = "RQ-" + UUID.randomUUID().toString().replace("-", "").take(4).uppercase() + "-" + UUID.randomUUID().toString().replace("-", "").take(2).uppercase()
        return account to request
    }
    fun signOutAccount() {
        val state = snapshot()
        if (state.type == LicenseType.ACCOUNT) {
            store.clearToken()
        }
        store.clearAccountSession()
    }

    suspend fun checkAndAutoActivateCloudAccount(email: String): LicenseSnapshot? = withContext(Dispatchers.IO) {
        val cleanEmail = email.trim().lowercase()
        if (cleanEmail.isBlank()) return@withContext null
        val endpoint = runCatching { endpoint() }.getOrNull() ?: return@withContext null
        val payload = JSONObject()
            .put("email", cleanEmail)
            .put("devicePublicKey", device.publicKeyBase64())
        val response = runCatching {
            post(endpoint + "/license/auto-activate", payload)
        }.getOrNull() ?: return@withContext null

        if (response.optBoolean("licensed", false) && response.has("token")) {
            val token = response.getString("token")
            return@withContext applySignedToken(token)
        }
        null
    }

    suspend fun activateAccountOnline(accountCode: String, activationCode: String): LicenseSnapshot = withContext(Dispatchers.IO) {
        val clean = accountCode.trim().uppercase()
        require(Regex("SL-[A-Z0-9]{4}-[A-Z0-9]{4}").matches(clean)) { "كود الحساب غير صالح" }
        val endpoint = endpoint()
        val response = post(endpoint + "/activate", JSONObject()
            .put("accountCode", clean)
            .put("activationCode", activationCode.trim())
            .put("devicePublicKey", device.publicKeyBase64()))
        applySignedToken(response.getString("token"))
    }

    suspend fun activateWithAccountOrCode(activationInput: String, email: String? = null): LicenseSnapshot = withContext(Dispatchers.IO) {
        val trimmed = activationInput.trim()
        val accountMatch = Regex("SL-[A-Z0-9]{4}-[A-Z0-9]{4}", RegexOption.IGNORE_CASE).find(trimmed)
        val extractedAccountCode = accountMatch?.value?.uppercase()
        val resolvedAccountCode = extractedAccountCode ?: store.accountCode

        val cleanActivationCode = if (extractedAccountCode != null) {
            trimmed.replace(accountMatch.value, "").trim(':', '-', ' ', '_', '/')
        } else {
            trimmed
        }

        if (!resolvedAccountCode.isNullOrBlank()) {
            val codeToUse = if (cleanActivationCode.isNotBlank()) cleanActivationCode else trimmed
            return@withContext activateAccountOnline(resolvedAccountCode, codeToUse)
        }

        val endpoint = endpoint()
        val payload = JSONObject()
            .put("activationCode", trimmed)
            .put("devicePublicKey", device.publicKeyBase64())
        if (!email.isNullOrBlank()) {
            payload.put("email", email.trim().lowercase())
        }
        val response = post(endpoint + "/activate", payload)
        applySignedToken(response.getString("token"))
    }

    suspend fun reconnectAccountOnline(accountCode: String? = null): LicenseSnapshot = withContext(Dispatchers.IO) {
        val clean = (accountCode ?: store.accountCode)?.trim()?.uppercase()
            ?: throw IllegalArgumentException("لا يوجد حساب مسجل لإعادة الربط")
        require(Regex("SL-[A-Z0-9]{4}-[A-Z0-9]{4}").matches(clean)) { "كود الحساب غير صالح" }
        val challenge = "${System.currentTimeMillis()}:${UUID.randomUUID()}"
        val response = post(endpoint() + "/verify", JSONObject()
            .put("accountCode", clean)
            .put("deviceFingerprint", device.fingerprint())
            .put("challenge", challenge)
            .put("signature", device.sign(challenge)))
        applySignedToken(response.getString("token"))
    }

    suspend fun verifyAccountOnline(): LicenseSnapshot = withContext(Dispatchers.IO) {
        if (snapshot().type != LicenseType.ACCOUNT) return@withContext snapshot()
        val account = store.accountCode ?: return@withContext snapshot()
        val challenge = "${System.currentTimeMillis()}:${UUID.randomUUID()}"
        val response = runCatching {
            post(endpoint() + "/verify", JSONObject()
                .put("accountCode", account)
                .put("deviceFingerprint", device.fingerprint())
                .put("challenge", challenge)
                .put("signature", device.sign(challenge)))
        }.getOrElse { return@withContext snapshot() }
        if (response.optBoolean("revoked", false)) {
            store.serverRevoked = true
            return@withContext LicenseSnapshot(
                type = LicenseType.ACCOUNT,
                status = LicenseStatus.REVOKED,
                accountCode = account,
                trialUsed = store.trialUsed
            )
        }
        applySignedToken(response.getString("token"))
    }

    private fun endpoint(): String =
        context.assets.open("license_endpoint.txt").bufferedReader().use { it.readText().trim().trimEnd('/') }
            .also { require(it.isNotBlank() && !it.startsWith("__")) { "خدمة الترخيص غير مهيأة" } }

    private fun post(url: String, body: JSONObject): JSONObject {
        val connection = (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            doOutput = true
            connectTimeout = 12_000
            readTimeout = 15_000
            setRequestProperty("Accept", "application/json")
            setRequestProperty("Content-Type", "application/json; charset=utf-8")
            setRequestProperty("Cache-Control", "no-store")
        }
        try {
            connection.outputStream.bufferedWriter(Charsets.UTF_8).use { it.write(body.toString()) }
            val stream = if (connection.responseCode in 200..299) connection.inputStream else connection.errorStream
            val text = stream?.bufferedReader()?.use { it.readText() }.orEmpty()
            val json = runCatching { JSONObject(text) }.getOrElse { JSONObject() }
            if (connection.responseCode !in 200..299) {
                throw IllegalStateException(when (json.optString("error")) {
                    "revoked" -> "الترخيص ملغى"
                    "rate_limited" -> "تم تجاوز محاولات التحقق مؤقتاً"
                    "activation_invalid" -> "رمز التفعيل غير صحيح"
                    "installation_not_authorized", "proof_invalid", "challenge_expired" -> "جلسة الترخيص غير صالحة"
                    else -> "تعذر التحقق من الترخيص"
                })
            }
            return json
        } finally {
            connection.disconnect()
        }
    }

    private fun parse(body: JSONObject, now: Long): LicenseSnapshot {
        val type = LicenseType.valueOf(body.getString("type"))
        val offlineUntil = body.optLong("offlineUntil", if (type == LicenseType.LOCAL) Long.MAX_VALUE else now + VERIFY_DAYS)
        val revoked = body.optBoolean("revoked", false)
        val clockRolledBack = type == LicenseType.ACCOUNT && store.lastSeenAt > 0L && now + 5 * 60 * 1000L < store.lastSeenAt
        if (now >= store.lastSeenAt) store.lastSeenAt = now
        val status = when { revoked -> LicenseStatus.REVOKED; clockRolledBack -> LicenseStatus.VERIFICATION_REQUIRED; type == LicenseType.ACCOUNT && now > offlineUntil -> LicenseStatus.VERIFICATION_REQUIRED; else -> LicenseStatus.ACTIVE }
        return LicenseSnapshot(type, status, body.optString("licenseId").takeIf { it.isNotBlank() }, body.optString("accountCode").takeIf { it.isNotBlank() }, body.optString("deviceCode").takeIf { it.isNotBlank() }, store.lastVerifiedAt, offlineUntil, store.trialUsed)
    }
}
