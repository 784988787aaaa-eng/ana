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
    companion object {
        const val TRIAL_LIMIT = 100
        private const val VERIFY_DAYS = 30L * 24 * 60 * 60 * 1000
    }

    private val store = LicenseStore(context)
    private val crypto = LicenseCrypto(context)
    private val device = DeviceIdentity()
    private val creationMutex = Mutex()

    private val _onLicenseRequired = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val onLicenseRequired: SharedFlow<Unit> = _onLicenseRequired.asSharedFlow()

    private val _onDeviceReplaced = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val onDeviceReplaced: SharedFlow<String> = _onDeviceReplaced.asSharedFlow()

    fun triggerLicenseRequired() {
        _onLicenseRequired.tryEmit(Unit)
    }

    fun syncTrialUsedWithCount(dbCount: Int) {
        if (store.trialUsed != dbCount) {
            store.trialUsed = dbCount
        }
    }

    fun isEligibleToCreate(currentUsed: Int? = null): Boolean {
        val state = snapshot()
        if (state.status == LicenseStatus.REVOKED ||
            state.status == LicenseStatus.VERIFICATION_REQUIRED ||
            state.status == LicenseStatus.TRIAL_EXPIRED
        ) return false
        if (state.isPaid) return true
        val used = currentUsed ?: store.trialUsed
        return used < TRIAL_LIMIT
    }

    fun snapshot(now: Long = System.currentTimeMillis()): LicenseSnapshot {
        val planEnum = when (store.plan?.uppercase()) {
            "TRIAL" -> LicensePlan.TRIAL
            else -> LicensePlan.LIFETIME
        }

        if (store.serverRevoked) {
            val revReason = when (store.revocationReason) {
                "device_replaced" -> RevocationReason.DEVICE_REPLACED
                "admin_revoked" -> RevocationReason.ADMIN_REVOKED
                "trial_expired" -> RevocationReason.TRIAL_EXPIRED
                else -> RevocationReason.NONE
            }
            val status = if (revReason == RevocationReason.TRIAL_EXPIRED) {
                LicenseStatus.TRIAL_EXPIRED
            } else {
                LicenseStatus.REVOKED
            }
            return LicenseSnapshot(
                type = LicenseType.ACCOUNT,
                plan = planEnum,
                status = status,
                accountCode = store.accountCode,
                email = store.email,
                deviceCode = device.deviceCode(),
                lastVerifiedAt = store.lastVerifiedAt,
                maxDevices = store.maxDevices,
                trialEndsAt = if (store.trialEndsAt > 0) store.trialEndsAt else null,
                active = false,
                revocationReason = revReason,
                revocationMessage = store.revocationMessage,
                activationRequired = store.activationRequired,
                trialUsed = store.trialUsed
            )
        }

        val token = store.token ?: return LicenseSnapshot(
            plan = planEnum,
            status = if (store.activationRequired) LicenseStatus.NOT_ACTIVATED else LicenseStatus.TRIAL,
            accountCode = store.accountCode,
            email = store.email,
            deviceCode = device.deviceCode(),
            activationRequired = store.activationRequired,
            trialUsed = store.trialUsed
        )

        return runCatching {
            parse(crypto.verify(token), now)
        }.getOrElse {
            LicenseSnapshot(
                status = LicenseStatus.REVOKED,
                accountCode = store.accountCode,
                email = store.email,
                deviceCode = device.deviceCode(),
                trialUsed = store.trialUsed
            )
        }
    }

    fun deviceCode(): String = device.deviceCode()

    fun deviceFingerprint(): String = device.fingerprint()

    fun applySignedToken(token: String): LicenseSnapshot {
        val body = crypto.verify(token)
        require(body.optString("product") == "SMARTLEDGER") { "الترخيص يخص منتجاً آخر" }
        val type = LicenseType.valueOf(body.getString("type"))
        if (type == LicenseType.LOCAL) {
            require(body.getString("deviceCode") == device.deviceCode()) { "الترخيص غير مرتبط بهذا الجهاز" }
        }
        if (type == LicenseType.ACCOUNT) {
            require(body.optString("accountCode").isNotBlank()) { "ترخيص الحساب ناقص" }
            require(body.optString("deviceFingerprint") == device.fingerprint()) { "جلسة الترخيص لا تخص هذا الجهاز" }
        }

        val planStr = body.optString("licenseType", body.optString("plan", "LIFETIME")).uppercase()
        val trialEndsAt = body.optLong("trialEndsAt", 0L).takeIf { it > 0 }
        val maxDev = body.optInt("maxDevices", 1).coerceAtLeast(1)

        store.token = token
        store.serverRevoked = false
        store.revocationReason = null
        store.revocationMessage = null
        store.activationRequired = false
        store.plan = planStr
        store.maxDevices = maxDev
        if (trialEndsAt != null) {
            store.trialEndsAt = trialEndsAt
        }
        store.accountCode = body.optString("accountCode").takeIf { it.isNotBlank() }
        store.email = body.optString("email").takeIf { it.isNotBlank() }
        store.lastVerifiedAt = System.currentTimeMillis()

        return parse(body, System.currentTimeMillis())
    }

    /**
     * Authorizes a database creation against the authoritative live DB count.
     * `trialUsed` is only a UI/cache mirror and is never used to grant capacity
     * when a live count is supplied. The mutex closes the check/insert race for
     * concurrent creation requests in the app process.
     */
    suspend fun <T> runAuthorizedCreation(
        currentUsed: suspend () -> Int,
        slots: Int = 1,
        block: suspend () -> T
    ): T? = creationMutex.withLock {
        require(slots > 0) { "slots must be positive" }
        val state = snapshot()
        if (state.status == LicenseStatus.REVOKED ||
            state.status == LicenseStatus.VERIFICATION_REQUIRED ||
            state.status == LicenseStatus.TRIAL_EXPIRED
        ) {
            _onLicenseRequired.tryEmit(Unit)
            return@withLock null
        }
        if (state.isPaid) return@withLock block()

        val used = currentUsed()
        if (used + slots > TRIAL_LIMIT) {
            store.trialUsed = used.coerceAtMost(TRIAL_LIMIT)
            _onLicenseRequired.tryEmit(Unit)
            return@withLock null
        }

        store.trialUsed = used + slots
        try {
            val result = block()
            // Reconcile from the DB immediately after a successful mutation.
            store.trialUsed = currentUsed().coerceAtMost(TRIAL_LIMIT)
            result
        } catch (t: Throwable) {
            store.trialUsed = currentUsed().coerceAtMost(TRIAL_LIMIT)
            throw t
        }
    }


    fun supportCodes(): Pair<String, String> {
        val account = store.accountCode ?: "غير مرتبط"
        val request = "RQ-" + UUID.randomUUID().toString().replace("-", "").take(4).uppercase() + "-" +
                UUID.randomUUID().toString().replace("-", "").take(2).uppercase()
        return account to request
    }

    fun signOutAccount() {
        val state = snapshot()
        if (state.type == LicenseType.ACCOUNT) {
            store.clearToken()
        }
        store.clearAccountSession()
    }

    /**
     * Automatic Seamless Activation by Email for already activated accounts.
     */
    suspend fun checkAndAutoActivateCloudAccount(email: String): LicenseSnapshot? = withContext(Dispatchers.IO) {
        val cleanEmail = email.trim().lowercase()
        if (cleanEmail.isBlank()) return@withContext null
        val payload = JSONObject()
            .put("email", cleanEmail)
            .put("devicePublicKey", device.publicKeyBase64())

        val response = runCatching {
            post(resolveUrl("/license/auto-activate"), payload)
        }.getOrNull() ?: return@withContext null

        if (response.optBoolean("licensed", false) && response.has("token")) {
            val token = response.getString("token")
            return@withContext applySignedToken(token)
        }

        // Account is registered but requires first-time activation code
        if (response.optBoolean("activationRequired", false)) {
            store.activationRequired = true
            store.accountCode = response.optString("accountCode").takeIf { it.isNotBlank() }
            store.email = cleanEmail
            return@withContext snapshot()
        }

        // Check if trial is expired
        if (response.optBoolean("expired", false)) {
            store.serverRevoked = true
            store.revocationReason = "trial_expired"
            store.revocationMessage = response.optString("message", "انتهت الفترة التجريبية لهذا الترخيص.")
            return@withContext snapshot()
        }

        // Check if account is revoked or disabled
        if (response.optBoolean("revoked", false)) {
            store.serverRevoked = true
            store.revocationReason = "admin_revoked"
            store.revocationMessage = response.optString("message", "تم إيقاف هذا الحساب.")
            return@withContext snapshot()
        }

        null
    }

    /**
     * First-Time Activation using Account Code or Email + Activation Code.
     */
    suspend fun activateAccountOnline(
        accountCode: String,
        activationCode: String,
        email: String? = null
    ): LicenseSnapshot = withContext(Dispatchers.IO) {
        val clean = accountCode.trim().uppercase()
        require(Regex("SL-[A-Z0-9]{4}-[A-Z0-9]{4}").matches(clean)) { "كود الحساب غير صالح" }
        val payload = JSONObject()
            .put("accountCode", clean)
            .put("activationCode", activationCode.trim().uppercase())
            .put("devicePublicKey", device.publicKeyBase64())
        if (!email.isNullOrBlank()) {
            payload.put("email", email.trim().lowercase())
        }

        val response = post(resolveUrl("/license/activate"), payload)
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
            return@withContext activateAccountOnline(resolvedAccountCode, codeToUse, email)
        }

        val payload = JSONObject()
            .put("activationCode", trimmed.uppercase())
            .put("devicePublicKey", device.publicKeyBase64())
        if (!email.isNullOrBlank()) {
            payload.put("email", email.trim().lowercase())
        }
        val response = post(resolveUrl("/license/activate"), payload)
        applySignedToken(response.getString("token"))
    }

    suspend fun reconnectAccountOnline(accountCode: String? = null): LicenseSnapshot = withContext(Dispatchers.IO) {
        val clean = (accountCode ?: store.accountCode)?.trim()?.uppercase()
            ?: throw IllegalArgumentException("لا يوجد حساب مسجل لإعادة الربط")
        require(Regex("SL-[A-Z0-9]{4}-[A-Z0-9]{4}").matches(clean)) { "كود الحساب غير صالح" }
        val challenge = "${System.currentTimeMillis()}:${UUID.randomUUID()}"
        val response = post(resolveUrl("/license/verify"), JSONObject()
            .put("accountCode", clean)
            .put("deviceFingerprint", device.fingerprint())
            .put("challenge", challenge)
            .put("signature", device.sign(challenge)))
        applySignedToken(response.getString("token"))
    }

    /**
     * Periodic and On-demand verification of device authorization.
     * Handles device eviction ("device_replaced") and trial expiration gracefully.
     */
    suspend fun verifyAccountOnline(): LicenseSnapshot = withContext(Dispatchers.IO) {
        if (snapshot().type != LicenseType.ACCOUNT) return@withContext snapshot()
        val account = store.accountCode ?: return@withContext snapshot()
        val challenge = "${System.currentTimeMillis()}:${UUID.randomUUID()}"
        val response = runCatching {
            post(resolveUrl("/license/verify"), JSONObject()
                .put("accountCode", account)
                .put("deviceFingerprint", device.fingerprint())
                .put("challenge", challenge)
                .put("signature", device.sign(challenge)))
        }.getOrElse { return@withContext snapshot() }

        if (response.optBoolean("revoked", false)) {
            store.serverRevoked = true
            val reasonStr = response.optString("reason", response.optString("status", ""))
            val msg = response.optString("message", "تم إلغاء ترخيص هذا الجهاز.")
            store.revocationReason = reasonStr
            store.revocationMessage = msg
            store.clearToken()

            if (reasonStr == "device_replaced" || reasonStr == "device_revoked") {
                _onDeviceReplaced.tryEmit(msg)
            }

            return@withContext snapshot()
        }

        if (response.has("token")) {
            applySignedToken(response.getString("token"))
        } else {
            snapshot()
        }
    }

    suspend fun checkStatusOnline(accountCode: String? = null, email: String? = null): JSONObject? = withContext(Dispatchers.IO) {
        val payload = JSONObject()
        val acc = accountCode ?: store.accountCode
        val em = email ?: store.email
        if (!acc.isNullOrBlank()) payload.put("accountCode", acc.trim().uppercase())
        if (!em.isNullOrBlank()) payload.put("email", em.trim().lowercase())
        if (payload.length() == 0) return@withContext null

        runCatching {
            post(resolveUrl("/license/check-status"), payload)
        }.getOrNull()
    }

    private fun resolveUrl(path: String): String {
        val base = endpoint()
        val cleanBase = base.removeSuffix("/").removeSuffix("/license")
        val cleanPath = "/" + path.trimStart('/')
        return cleanBase + cleanPath
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
                val errCode = json.optString("error")
                val errMsg = json.optString("message")
                throw IllegalStateException(when {
                    errMsg.isNotBlank() -> errMsg
                    errCode == "revoked" -> "الترخيص ملغى"
                    errCode == "rate_limited" -> "تم تجاوز محاولات التحقق مؤقتاً"
                    errCode == "activation_invalid" -> "رمز التفعيل غير صحيح"
                    errCode == "trial_expired" -> "انتهت الفترة التجريبية لهذا الترخيص"
                    errCode == "account_disabled" -> "تم إيقاف هذا الحساب من قبل الإدارة"
                    errCode in listOf("installation_not_authorized", "proof_invalid", "challenge_expired") -> "جلسة الترخيص غير صالحة"
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
        val planStr = body.optString("licenseType", body.optString("plan", "LIFETIME")).uppercase()
        val planEnum = if (planStr == "TRIAL") LicensePlan.TRIAL else LicensePlan.LIFETIME
        val active = body.optBoolean("active", true)
        val maxDevices = body.optInt("maxDevices", 1).coerceAtLeast(1)
        val trialEndsAt = body.optLong("trialEndsAt", 0L).takeIf { it > 0 }
        val offlineUntil = body.optLong("offlineUntil", if (type == LicenseType.LOCAL) Long.MAX_VALUE else now + VERIFY_DAYS)
        val revoked = body.optBoolean("revoked", false)
        val isExpired = planEnum == LicensePlan.TRIAL && trialEndsAt != null && now >= trialEndsAt

        val clockRolledBack = type == LicenseType.ACCOUNT && store.lastSeenAt > 0L && now + 5 * 60 * 1000L < store.lastSeenAt
        if (now >= store.lastSeenAt) store.lastSeenAt = now

        val status = when {
            revoked || !active -> LicenseStatus.REVOKED
            isExpired -> LicenseStatus.TRIAL_EXPIRED
            clockRolledBack -> LicenseStatus.VERIFICATION_REQUIRED
            type == LicenseType.ACCOUNT && now > offlineUntil -> LicenseStatus.VERIFICATION_REQUIRED
            else -> LicenseStatus.ACTIVE
        }

        return LicenseSnapshot(
            type = type,
            plan = planEnum,
            status = status,
            licenseId = body.optString("licenseId").takeIf { it.isNotBlank() },
            accountCode = body.optString("accountCode").takeIf { it.isNotBlank() },
            email = body.optString("email").takeIf { it.isNotBlank() },
            deviceCode = body.optString("deviceCode").takeIf { it.isNotBlank() } ?: device.deviceCode(),
            lastVerifiedAt = store.lastVerifiedAt,
            offlineUntil = offlineUntil,
            trialEndsAt = trialEndsAt,
            remainingDays = if (planEnum == LicensePlan.TRIAL && trialEndsAt != null) {
                Math.max(0, Math.ceil((trialEndsAt - now).toDouble() / (24.0 * 60 * 60 * 1000)).toInt())
            } else null,
            maxDevices = maxDevices,
            active = active,
            revocationReason = if (revoked) RevocationReason.ADMIN_REVOKED else RevocationReason.NONE,
            trialUsed = store.trialUsed
        )
    }
}
