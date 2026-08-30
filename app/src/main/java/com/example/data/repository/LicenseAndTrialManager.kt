/**
 * =====================================================================
 * ملف: مدير التراخيص والفترة التجريبية (LicenseAndTrialManager.kt)
 * =====================================================================
 * 
 * [الغرض العام والتعليمي من الملف]:
 * يمثل هذا المدير المرجع المركزي الموحد (Single Source of Truth) لكافة عمليات التراخيص،
 * والتحقق من رموز التفعيل المشفرة، وإدارة حصص الفترة التجريبية (Trial Quotas)،
 * وبصمة الجهاز الموحدة (Device Fingerprinting).
 * 
 * [المسؤوليات المعمارية والتقنية لمنظومة التراخيص]:
 * 1. توليد بصمة الجهاز الموحدة (Unified Device Fingerprint):
 *    - تركيب معرف جهاز بصيغة `MZ-XXXXXXXX-YYYYYYYY` يجمع بين جزء عشوائي دائم ومعرف النظام `ANDROID_ID`.
 * 2. التحقق التشفيري دون اتصال (Offline Cryptographic Verification):
 *    - حساب تجزئة SHA-256 مع ملح تشفيري مموه بعملية XOR (Obfuscated Salt) لمنع الهندسة العكسية.
 *    - استخدام مقارنة الرموز في وقت زمني ثابت لمنع هجمات التوقيت (Timing Attacks).
 * 3. إدارة كوتة العمليات المجانية (Trial Limit):
 *    - قفل العمليات بعد الوصول إلى الحد الأقصى للمعاملات [SECURE_LIMIT_VAL] (100 معاملة) ما لم يتم التفعيل.
 * 4. التفعيل السحابي واليدوي:
 *    - دعم التفعيل بكود التنشيط الفوري بدون إنترنت أو عبر الحساب السحابي المرخص.
 */
package com.example.data.repository

// ---------------------------------------------------------------------
// استيراد حزم سياق أندرويد ومديري الأمان والجلسات والتجزئة التشفيرية
// ---------------------------------------------------------------------
import android.content.Context
import com.example.domain.AppSecurityManager
import com.example.domain.GoogleAuthSessionManager
import com.example.domain.HashUtils
import java.security.MessageDigest
import java.util.UUID

/**
 * [فئة مدير التراخيص والفترة التجريبية - LicenseAndTrialManager]:
 * تفحص حالة التفعيل، وتدقق الأكواد، وتدير حدود الفترة التجريبية.
 *
 * @param context سياق التطبيق للوصول إلى تفضيلات الأمان وإعدادات الجهاز.
 */
class LicenseAndTrialManager(context: Context) {

    /** سياق التطبيق العام لتجنب تسريب الذاكرة */
    private val appContext: Context = context.applicationContext

    /** مدير أمان التطبيق والتخزين المشفر */
    private val securityManager: AppSecurityManager = AppSecurityManager.getInstance(appContext)

    /**
     * [الكائن المرافق للثوابت والتحقق التشفيري]:
     */
    companion object {
        /** بادئة كود التنشيط المؤقت */
        const val PREFIX_TEMP = "ACT-T-"
        /** بادئة كود التنشيط الدائم */
        const val PREFIX_PERM = "ACT-P-"
        /** سقف عدد المعاملات المسموح بها في النسخة التجريبية */
        const val SECURE_LIMIT_VAL: Int = 100

        /** حروف التمثيل الست عشري لتحويل البايتات */
        private val HEX_CHARS = "0123456789ABCDEF".toCharArray()

        /**
         * الملح التشفيري المشفر بعملية قناع XOR:
         * يتم فك تمويهه برمجياً في الذاكرة عند الحاجة لحمايته من الفحص الثابت (Static Analysis).
         */
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
            String(decrypted, Charsets.UTF_8)
        }

        /**
         * [التحقق التشفيري من كود التفعيل دون اتصال - verifyActivationCode]:
         * يطابق الكود المدخل مع البصمة المحسوبة لجزء معرف الجهاز المناسب باستخدام SHA-256.
         *
         * @param deviceId معرف الجهاز الموحد.
         * @param enteredCode الكود المدخل من المستخدم.
         * @return true إذا كان الكود صحيحاً ومطابقاً لجزء الجهاز المطلوب.
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

            val combined = targetPart + cachedSalt
            val md = MessageDigest.getInstance("SHA-256")
            val bytes = md.digest(combined.toByteArray(Charsets.UTF_8))

            // تحويل أول 4 بايتات (8 أحرف ست عشرية)
            val hexChars = CharArray(8)
            for (i in 0 until 4) {
                val v = bytes[i].toInt() and 0xFF
                hexChars[i * 2] = HEX_CHARS[v ushr 4]
                hexChars[i * 2 + 1] = HEX_CHARS[v and 0x0F]
            }
            val shaPrefix = String(hexChars)
            val isMatch = HashUtils.secureEquals(enteredPayload, shaPrefix)
            HashUtils.wipeCharArray(hexChars)
            return isMatch
        }

        /**
         * [توليد أو جلب معرف الجهاز الموحد - getOrGenerateUnifiedDeviceId]:
         * يولد بصمة فريدة للجهاز بصيغة `MZ-XXXXXXXX-YYYYYYYY` ويحفظها بأمان.
         *
         * @param context سياق النظام للوصول للمعرفات الفريدة.
         * @return معرف الجهاز الموحد.
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
     * [فحص حالة تفعيل التطبيق - isAppActivated]:
     * يتحقق مما إذا كان التطبيق مفعلاً عبر التخزين المؤقت أو الكود المشفر أو البريد المرخص.
     *
     * @return true إذا كان التطبيق مرخصاً ونشطاً.
     */
    fun isAppActivated(): Boolean {
        val deviceId = getOrGenerateUnifiedDeviceId(appContext)

        val cachedIsActivated = securityManager.isActivatedCached()
        val cachedForDevice = securityManager.getCachedDeviceId()

        val activatedEmail = securityManager.getActivatedEmail()
        val enteredCode = securityManager.getActivationCode()
        val isCodeValid = enteredCode.isNotBlank() && verifyActivationCode(deviceId, enteredCode)

        val isEmailActivated = activatedEmail.isNotBlank()

        if (cachedIsActivated && (cachedForDevice == deviceId || cachedForDevice.isBlank()) && (isCodeValid || isEmailActivated)) {
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
     * [التحقق من انتهاء الفترة التجريبية - isTrialExpiredDirect]:
     * يفحص إذا تجاوز إجمالي عدد القيود المحاسبية الحد الأقصى المسموح [SECURE_LIMIT_VAL].
     *
     * @param realTotalTransactionsCount العدد الإجمالي الفعلي لمعاملات اليومية والحبايب.
     * @return true إذا انتهت التجربة والتطبيق غير مفعل.
     */
    fun isTrialExpiredDirect(realTotalTransactionsCount: Int): Boolean {
        if (isAppActivated()) {
            return false
        }
        return realTotalTransactionsCount >= SECURE_LIMIT_VAL
    }

    /**
     * [تفعيل التطبيق بواسطة كود تنشيط - activateLicenseWithCode]:
     * يتحقق من الكود ويحفظه في التفضيلات المشفرة عند صحته.
     *
     * @param code كود التفعيل المدخل.
     * @return true إذا تم التفعيل بنجاح.
     */
    fun activateLicenseWithCode(code: String): Boolean {
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
     * [حفظ التفعيل بالبريد الإلكتروني - saveEmailActivation]:
     * يسجل بيانات التفعيل السحابي المعتمد ويربطه بالجهاز.
     *
     * @param email البريد المرخص.
     * @param deviceId معرف الجهاز.
     */
    fun saveEmailActivation(email: String, deviceId: String) {
        securityManager.setActivatedEmail(email)
        securityManager.setCachedActivation(true, deviceId)
    }

    /**
     * [إلغاء ومسح بيانات التفعيل المحلية - clearLocalActivation]:
     * يعيد ضبط حالة الترخيص ومسح الأكواد والبريد من الذاكرة المشفرة.
     */
    fun clearLocalActivation() {
        securityManager.clearActivationData()
    }

    /** جلب البريد الإلكتروني المفعل */
    fun getActivatedEmail(): String = securityManager.getActivatedEmail()

    /** جلب معرف الجهاز الحالي الموحد */
    fun getDeviceId(): String = getOrGenerateUnifiedDeviceId(appContext)
}

