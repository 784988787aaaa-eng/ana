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
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID

class LicenseRepository(
    private val context: Context,
    private val crypto: LicenseCrypto = LicenseCrypto(context)
) {
    companion object {
        const val TRIAL_LIMIT = 100
        private const val VERIFY_DAYS = 30L * 24 * 60 * 60 * 1000
        private const val CLOCK_ROLLBACK_TOLERANCE_MS = 5L * 60 * 1000
    }

    private val store = LicenseStore(context)
    private val device = DeviceIdentity(context)
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
                "clock_rolled_back" -> RevocationReason.CLOCK_ROLLED_BACK
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

    fun applySignedToken(token: String, now: Long = System.currentTimeMillis()): LicenseSnapshot {
        val cleanToken = crypto.sanitizeToken(token)
        val body = crypto.verify(cleanToken)
        require(body.optString("product") == LicenseCrypto.PRODUCT_SMARTLEDGER) {
            "الترخيص يخص منتجاً آخر"
        }

        val rawType = body.optString("type").trim().uppercase()
        val type = runCatching { LicenseType.valueOf(rawType) }
            .getOrElse { throw IllegalArgumentException("نوع الترخيص غير صالح") }

        val planStr = body.optString("plan", body.optString("licenseType")).trim().uppercase()
        require(planStr == "LIFETIME" || planStr == "TRIAL") {
            "خطة الترخيص غير صالحة"
        }
        val planEnum = if (planStr == "TRIAL") LicensePlan.TRIAL else LicensePlan.LIFETIME

        val version = body.optInt("v", 0)
        val licenseId = body.optString("licenseId").trim()
        val iat = body.optLong("iat", 0L)

        if (type == LicenseType.LOCAL) {
            require(version >= 1) { "إصدار الترخيص غير مدعوم أو ناقص" }
            require(licenseId.isNotBlank()) { "معرف الترخيص مفقود" }
            require(iat > 0L) { "تاريخ إصدار الترخيص مفقود أو غير صالح" }

            val rawTokenDeviceCode = body.optString("deviceCode")
            require(rawTokenDeviceCode.isNotBlank()) { "رمز الجهاز مفقود في ملف أو كود الترخيص" }
            val normalizedTokenDevice = DeviceIdentity.normalizeDeviceCode(rawTokenDeviceCode)
            val currentDeviceCode = DeviceIdentity.normalizeDeviceCode(device.deviceCode())
            require(DeviceIdentity.isValidDeviceCode(normalizedTokenDevice)) {
                "صيغة رمز الجهاز داخل الترخيص غير صالحة"
            }
            require(normalizedTokenDevice == currentDeviceCode) {
                "الترخيص لا يخص هذا الجهاز. رمز جهازك: $currentDeviceCode"
            }
        } else {
            if (body.has("v")) {
                require(version >= 1) { "إصدار الترخيص غير مدعوم" }
            }
            require(body.optString("accountCode").isNotBlank()) { "ترخيص الحساب ناقص" }
            require(body.optString("deviceFingerprint") == device.fingerprint()) {
                "جلسة الترخيص لا تخص هذا الجهاز"
            }
        }

        val trialEndsAt = body.optLong("trialEndsAt", 0L).takeIf { it > 0L }
        if (planEnum == LicensePlan.TRIAL) {
            require(trialEndsAt != null) { "تاريخ انتهاء الفترة التجريبية مفقود" }
            if (iat > 0L) {
                require(trialEndsAt > iat) { "تاريخ انتهاء الفترة التجريبية غير صالح" }
            }
            require(now < trialEndsAt) { "انتهت مدة هذا الترخيص التجريبي ولا يمكن تفعيله" }
        }

        val clockRolledBack = (type == LicenseType.ACCOUNT || (type == LicenseType.LOCAL && planEnum == LicensePlan.TRIAL)) &&
            store.lastSeenAt > 0L && now + CLOCK_ROLLBACK_TOLERANCE_MS < store.lastSeenAt
        check(!clockRolledBack) { "تم اكتشاف تراجع في ساعة الجهاز. يرجى ضبط التاريخ والوقت الصحيحين ثم المحاولة مجدداً" }

        val active = body.optBoolean("active", true)
        val revoked = body.optBoolean("revoked", false)
        require(active && !revoked) { "هذا الترخيص موقوف أو ملغى" }

        val maxDev = body.optInt("maxDevices", 1).coerceAtLeast(1)

        // All validation passed before mutating persistent state so an invalid/tampered token
        // never overwrites an existing valid license.
        store.token = cleanToken
        store.serverRevoked = false
        store.revocationReason = null
        store.revocationMessage = null
        store.activationRequired = false
        store.plan = planStr
        store.maxDevices = maxDev
        store.trialEndsAt = trialEndsAt ?: 0L
        store.accountCode = body.optString("accountCode").takeIf { it.isNotBlank() }
        store.email = body.optString("email").takeIf { it.isNotBlank() }
        store.lastVerifiedAt = now

        return parse(body, now)
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
        if (state.type == LicenseType.LOCAL) {
            store.clearAccountMetadataOnly()
            return
        }
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
            post("/license/auto-activate", payload)
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

        val response = post("/license/activate", payload)
        applySignedToken(response.getString("token"))
    }

    suspend fun activateWithAccountOrCode(activationInput: String, email: String? = null): LicenseSnapshot = withContext(Dispatchers.IO) {
        val trimmed = activationInput.trim()

        // 1. If it's directly a signed local or JWT token
        if (trimmed.startsWith("${LicenseCrypto.SLK1_PREFIX}.") || (trimmed.startsWith("ey") && trimmed.contains("."))) {
            return@withContext applySignedToken(trimmed)
        }

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
        val response = post("/license/activate", payload)
        applySignedToken(response.getString("token"))
    }

    suspend fun reconnectAccountOnline(accountCode: String? = null): LicenseSnapshot = withContext(Dispatchers.IO) {
        val clean = (accountCode ?: store.accountCode)?.trim()?.uppercase()
            ?: throw IllegalArgumentException("لا يوجد حساب مسجل لإعادة الربط")
        require(Regex("SL-[A-Z0-9]{4}-[A-Z0-9]{4}").matches(clean)) { "كود الحساب غير صالح" }
        val challenge = "${System.currentTimeMillis()}:${UUID.randomUUID()}"
        val response = post("/license/verify", JSONObject()
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
            post("/license/verify", JSONObject()
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
            post("/license/check-status", payload)
        }.getOrNull()
    }

    private fun resolveUrls(path: String): List<String> {
        val bases = endpoints()
        val cleanPath = "/" + path.trimStart('/')
        return bases.map { base ->
            val cleanBase = base.removeSuffix("/").removeSuffix("/license")
            cleanBase + cleanPath
        }
    }

    private fun endpoints(): List<String> = runCatching {
        context.assets.open("license_endpoint.txt").bufferedReader().use { reader ->
            reader.readLines().map { it.trim().trimEnd('/') }.filter { it.isNotBlank() && !it.startsWith("__") }
        }
    }.getOrNull()?.takeIf { it.isNotEmpty() } ?: listOf("https://al-daftar-license-api.pages.dev", "https://al-daftar-license-api.mansour-ghawy.workers.dev")

    private fun post(path: String, body: JSONObject): JSONObject {
        val urls = resolveUrls(path)
        var lastException: Exception? = null

        for (url in urls) {
            try {
                val connection = (URL(url).openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    doOutput = true
                    connectTimeout = 12_000
                    readTimeout = 15_000
                    setRequestProperty("Accept", "application/json")
                    setRequestProperty("Content-Type", "application/json; charset=utf-8")
                    setRequestProperty("Cache-Control", "no-store")
                }
                return try {
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
                    json
                } finally {
                    connection.disconnect()
                }
            } catch (e: IllegalStateException) {
                throw e
            } catch (e: Exception) {
                lastException = e
            }
        }
        throw (lastException ?: IOException("تعذر الاتصال بخادم الترخيص"))
    }

    private fun parse(body: JSONObject, now: Long): LicenseSnapshot {
        require(body.optString("product") == LicenseCrypto.PRODUCT_SMARTLEDGER) {
            "الترخيص يخص منتجاً آخر"
        }
        val type = LicenseType.valueOf(body.getString("type").trim().uppercase())
        val planStr = body.optString("plan", body.optString("licenseType", "")).trim().uppercase()
        require(planStr == "LIFETIME" || planStr == "TRIAL") { "خطة الترخيص غير صالحة" }
        val planEnum = if (planStr == "TRIAL") LicensePlan.TRIAL else LicensePlan.LIFETIME

        if (type == LicenseType.LOCAL) {
            require(body.optInt("v", 0) >= 1) { "إصدار الترخيص غير مدعوم" }
            require(body.optString("licenseId").isNotBlank()) { "معرف الترخيص مفقود" }
            val iat = body.optLong("iat", 0L)
            require(iat > 0L) { "تاريخ إصدار الترخيص غير صالح" }
            val normalizedTokenDevice = DeviceIdentity.normalizeDeviceCode(body.optString("deviceCode"))
            val currentDevice = DeviceIdentity.normalizeDeviceCode(device.deviceCode())
            require(DeviceIdentity.isValidDeviceCode(normalizedTokenDevice) && normalizedTokenDevice == currentDevice) {
                "الترخيص لا يخص هذا الجهاز"
            }
        } else {
            require(body.optString("accountCode").isNotBlank()) { "ترخيص الحساب ناقص" }
            require(body.optString("deviceFingerprint") == device.fingerprint()) {
                "جلسة الترخيص لا تخص هذا الجهاز"
            }
        }

        val active = body.optBoolean("active", true)
        val maxDevices = body.optInt("maxDevices", 1).coerceAtLeast(1)
        val trialEndsAt = body.optLong("trialEndsAt", 0L).takeIf { it > 0L }
        if (planEnum == LicensePlan.TRIAL) {
            require(trialEndsAt != null) { "تاريخ انتهاء الفترة التجريبية مفقود" }
            val iat = body.optLong("iat", 0L)
            if (iat > 0L) {
                require(trialEndsAt > iat) { "تاريخ انتهاء الفترة التجريبية غير صالح" }
            }
        }

        val offlineUntil = body.optLong("offlineUntil", if (type == LicenseType.LOCAL) Long.MAX_VALUE else now + VERIFY_DAYS)
        val revoked = body.optBoolean("revoked", false)
        val isExpired = planEnum == LicensePlan.TRIAL && trialEndsAt != null && now >= trialEndsAt

        val clockRolledBack = (type == LicenseType.ACCOUNT || (type == LicenseType.LOCAL && planEnum == LicensePlan.TRIAL)) &&
            store.lastSeenAt > 0L && now + CLOCK_ROLLBACK_TOLERANCE_MS < store.lastSeenAt
        if (now >= store.lastSeenAt) store.lastSeenAt = now

        val status = when {
            revoked || !active -> LicenseStatus.REVOKED
            isExpired -> LicenseStatus.TRIAL_EXPIRED
            clockRolledBack -> LicenseStatus.VERIFICATION_REQUIRED
            type == LicenseType.ACCOUNT && now > offlineUntil -> LicenseStatus.VERIFICATION_REQUIRED
            else -> LicenseStatus.ACTIVE
        }

        val revocationReason = when {
            revoked -> RevocationReason.ADMIN_REVOKED
            isExpired -> RevocationReason.TRIAL_EXPIRED
            clockRolledBack -> RevocationReason.CLOCK_ROLLED_BACK
            else -> RevocationReason.NONE
        }

        val revocationMessage = when (revocationReason) {
            RevocationReason.CLOCK_ROLLED_BACK ->
                "تم اكتشاف تراجع في ساعة الجهاز. يرجى ضبط التاريخ والوقت الصحيحين للمتابعة."
            RevocationReason.TRIAL_EXPIRED ->
                "انتهت الفترة التجريبية لهذا الترخيص."
            else -> null
        }

        return LicenseSnapshot(
            type = type,
            plan = planEnum,
            status = status,
            licenseId = body.optString("licenseId").takeIf { it.isNotBlank() },
            accountCode = body.optString("accountCode").takeIf { it.isNotBlank() },
            email = body.optString("email").takeIf { it.isNotBlank() },
            deviceCode = body.optString("deviceCode").takeIf { it.isNotBlank() }?.let(DeviceIdentity::normalizeDeviceCode) ?: device.deviceCode(),
            lastVerifiedAt = store.lastVerifiedAt,
            offlineUntil = offlineUntil,
            trialEndsAt = trialEndsAt,
            remainingDays = if (planEnum == LicensePlan.TRIAL && trialEndsAt != null) {
                Math.max(0, Math.ceil((trialEndsAt - now).toDouble() / (24.0 * 60 * 60 * 1000)).toInt())
            } else null,
            maxDevices = maxDevices,
            active = active,
            revocationReason = revocationReason,
            revocationMessage = revocationMessage,
            trialUsed = store.trialUsed
        )
    }
}
