
/**
 * مدير التراخيص والفترة التجريبية؛ يحافظ على قرار الترخيص محلياً مع حماية التخزين وبصمة الجهاز والتحقق الزمني الثابت.
 * التوثيق هنا يوضح أثر الدوال على الأمان والتوافق والدقة المالية دون تغيير واجهات الاستدعاء.
 */
package com.smartledger.aldaftar.data.repository

import android.content.Context
import com.smartledger.aldaftar.domain.AppSecurityManager
import com.smartledger.aldaftar.domain.GoogleAuthSessionManager
import com.smartledger.aldaftar.domain.HashUtils
import com.smartledger.aldaftar.security.SecurityEnvironmentGuard
import java.security.MessageDigest
import java.util.UUID

class LicenseAndTrialManager(context: Context) {

    private val appContext: Context = context.applicationContext

    private val securityManager: AppSecurityManager = AppSecurityManager.getInstance(appContext)

    companion object {
        
        const val PREFIX_TEMP = "ACT-T-"
        
        const val PREFIX_PERM = "ACT-P-"
        
        const val SECURE_LIMIT_VAL: Int = 100

        private val HEX_CHARS = "0123456789ABCDEF".toCharArray()

        private val cachedSalt: String by lazy {
            val mask = 0x7F
            val obfuscatedSalt = byteArrayOf(
                50, 22, 5, 30, 17, 62, 19, 59, 30, 13,
                44, 26, 28, 10, 13, 26, 44, 30, 19, 11,
                77, 79, 77, 73, 32, 50, 30, 17, 12, 16,
                10, 13
            )
            val decrypted = ByteArray(obfuscatedSalt.size)
            for (i in obfuscatedSalt.indices) {
                decrypted[i] = (obfuscatedSalt[i].toInt() xor mask).toByte()
            }
            val salt = String(decrypted, Charsets.UTF_8)
            java.util.Arrays.fill(decrypted, 0)
            salt
        }

        /**
         * يتحقق من كود التفعيل بصمةً بصمةً وبمقارنة ثابتة الزمن لمنع تسريب النتيجة عبر فروق التوقيت.
         */
        fun verifyActivationCode(deviceId: String, enteredCode: String): Boolean {
            val cleanEntered = enteredCode.trim().uppercase()
            val parts = deviceId.split("-")
            val tempPart = if (parts.size >= 3) parts[1] else ""
            val permPart = if (parts.size >= 3) parts[2] else ""

            val isTemp = cleanEntered.startsWith(PREFIX_TEMP)
            val isPerm = cleanEntered.startsWith(PREFIX_PERM)

            if (!isTemp && !isPerm) return false

            val prefixLength = if (isTemp) PREFIX_TEMP.length else PREFIX_PERM.length
            val targetPart = if (isTemp) tempPart else permPart
            val enteredPayload = cleanEntered.substring(prefixLength)

            val combinedBytes = (targetPart + cachedSalt).toByteArray(Charsets.UTF_8)
            val md = MessageDigest.getInstance("SHA-256")
            val bytes = md.digest(combinedBytes)
            java.util.Arrays.fill(combinedBytes, 0)

            val hexChars = CharArray(8)
            for (i in 0 until 4) {
                val v = bytes[i].toInt() and 0xFF
                hexChars[i * 2] = HEX_CHARS[v ushr 4]
                hexChars[i * 2 + 1] = HEX_CHARS[v and 0x0F]
            }
            val shaPrefix = String(hexChars)
            val isMatch = HashUtils.secureEquals(enteredPayload, shaPrefix)
            HashUtils.wipeCharArray(hexChars)
            java.util.Arrays.fill(bytes, 0)
            return isMatch
        }

        /**
         * يجلب معرف الجهاز الموحد أو ينشئه مرة واحدة ويحفظه عبر مخزن الأمان المعتمد لضمان ثبات الربط.
         */
        fun getOrGenerateUnifiedDeviceId(context: Context): String {
            val secManager = AppSecurityManager.getInstance(context)
            var deviceId = secManager.getUnifiedDeviceId()

            if (deviceId.isBlank()) {
                val tempPart = UUID.randomUUID().toString().replace("-", "").take(8).uppercase()
                val androidId = android.provider.Settings.Secure.getString(
                    context.contentResolver,
                    android.provider.Settings.Secure.ANDROID_ID
                )
                val permPart = if (!androidId.isNullOrBlank()) {
                    androidId.take(8).uppercase()
                } else {
                    "A1B2C3D4"
                }
                deviceId = "MZ-$tempPart-$permPart"
                secManager.setUnifiedDeviceId(deviceId)
            }
            return deviceId
        }
    }

    /**
     * يجمع إشارات الترخيص المستقلة ويفحص بيئة التشغيل قبل قبول حالة التفعيل.
     */
    fun isAppActivated(): Boolean {
        
        val environment = SecurityEnvironmentGuard.assess(appContext)
        if (environment.compromised) return false

        val deviceId = getOrGenerateUnifiedDeviceId(appContext)
        val cachedIsActivated = securityManager.isActivatedCached()
        val cachedForDevice = securityManager.getCachedDeviceId()

        val activatedEmail = securityManager.getActivatedEmail()
        val enteredCode = securityManager.getActivationCode()
        val isCodeValid = enteredCode.isNotBlank() && verifyActivationCode(deviceId, enteredCode)

        val isEmailActivated = activatedEmail.isNotBlank()
        val deviceBindingValid = cachedForDevice.isBlank() || cachedForDevice == deviceId
        val credentialValid = isCodeValid || isEmailActivated
        val cachedActivationValid = cachedIsActivated && deviceBindingValid && credentialValid

        if (cachedActivationValid) {
            return true
        }

        if (isEmailActivated) {
            securityManager.setCachedActivation(true, deviceId)
            return true
        }

        if (isCodeValid) {
            securityManager.setCachedActivation(true, deviceId, enteredCode)
            return true
        }

        return false
    }

    /**
     * يحدد انتهاء الحصة التجريبية اعتماداً على العدد الفعلي للعمليات دون تعديل البيانات المالية.
     */
    fun isTrialExpiredDirect(realTotalTransactionsCount: Int): Boolean {
        if (isAppActivated()) {
            return false
        }
        return realTotalTransactionsCount >= SECURE_LIMIT_VAL
    }

    /**
     * يتحقق من كود التفعيل قبل تخزينه ولا يسمح بالتفعيل المحلي في بيئة تشغيل غير موثوقة.
     */
    fun activateLicenseWithCode(code: String): Boolean {
        
        if (SecurityEnvironmentGuard.assess(appContext).compromised) return false
        val cleanCode = code.trim().uppercase()
        val deviceId = getOrGenerateUnifiedDeviceId(appContext)
        val isValid = verifyActivationCode(deviceId, cleanCode)
        if (isValid) {
            securityManager.setActivationCode(cleanCode)
            securityManager.setCachedActivation(true, deviceId, cleanCode)
        }
        return isValid
    }

    /**
     * يحفظ اعتماد البريد السحابي بعد ربطه بمعرف الجهاز المقدم من مسار الترخيص المعتمد.
     */
    fun saveEmailActivation(email: String, deviceId: String) {
        securityManager.setActivatedEmail(email)
        securityManager.setCachedActivation(true, deviceId)
    }

    /**
     * يمسح حالة التفعيل المحلية من مخزن الأمان ويعيد التطبيق إلى حالة عدم التفعيل.
     */
    fun clearLocalActivation() {
        securityManager.clearActivationData()
    }

    fun getActivatedEmail(): String = securityManager.getActivatedEmail()

    fun getDeviceId(): String = getOrGenerateUnifiedDeviceId(appContext)
}

