/**
 * =====================================================================
 * ملف: مدير الأمان والتخزين المشفر المركزي للتطبيق (AppSecurityManager.kt)
 * =====================================================================
 * 
 * [الغرض العام والتعليمي من الملف]:
 * يمثل هذا الملف الحصن الأمني المركزي لإدارة البيانات الحساسة وتخزينها محلياً
 * بأعلى معايير التشفير العتادي [Hardware-Backed Security].
 * يقوم بتأمين مفاتيح التفعيل، والتراخيص، ومعرفات الأجهزة، وحالة قفل التطبيق
 * وقياسات الأمان الحيوية (البصمة والوجه) عبر مستودع التفضيلات المشفر
 * [EncryptedSharedPreferences] المدعوم بمفاتيح أندرويد الرئيسية [MasterKeys].
 * 
 * [المسؤوليات المعمارية والتقنية للملف]:
 * 1. التخزين المشفر المتقدم (Cryptographic Storage):
 *    - استخدام معيار تشفير المفاتيح AES-256-SIV وتشفير القيم AES-256-GCM.
 * 2. الترحيل الآمن للتفضيلات القديمة (Transparent Migration):
 *    - نقل أية بيانات قديمة مخزنة في تفضيلات غير مشفرة إلى المستودع المشفر تلقائياً.
 * 3. آلية التراجع الآمن عند فشل العتاد (Fail-Safe Fallback):
 *    - التراجع للتفضيلات الخاصة القياسية في حال عدم توفر شريحة الأمان العتادية (Keystore)
 *      لضمان استمرار عمل التطبيق دون انهيار.
 * 4. إدارة تراخيص وتفعيل النسخة المميزة (License & Premium Management):
 *    - حفظ وقراءة كود التفعيل والبريد الإلكتروني وحالة التفعيل المخبأة محلياً.
 * 5. إدارة إعدادات الحماية الحيوية ورمز المرور السريع (Biometrics & Passcode Settings).
 */
package com.smartledger.aldaftar.domain

// ---------------------------------------------------------------------
// استيراد حزم سياق أندرويد، التفضيلات، السجلات، ومكتبات التشفير الأمنية
// ---------------------------------------------------------------------
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.smartledger.aldaftar.security.SecurePreferenceStore

/**
 * [فئة مدير أمان التطبيق - AppSecurityManager]:
 * فئة أحادية النمط (Singleton) مسؤولة عن إدارة التفضيلات المشفرة وحالة التراخيص والأمان.
 * 
 * @param context سياق التطبيق العام لتهيئة التفضيلات ومفاتيح التشفير.
 */
class AppSecurityManager private constructor(context: Context) {

    /** سياق التطبيق الشامل لمنع تسرب الذاكرة المرتبط بالأنشطة */
    private val appContext: Context = context.applicationContext

    /**
     * مستودع التفضيلات المشفر المحمي بمفاتيح عتادية [MasterKeys].
     * يتم تهيئته عند أول طلب عبر خاصية التهيئة الكسولة (Lazy Initialization).
     */
    private val securePrefs: SharedPreferences by lazy {
        initEncryptedPreferences()
    }

    /**
     * مستودع التفضيلات القديم (الاحتياطي) المستخدم للتراجع والترحيل.
     */
    private val legacyPrefs: SharedPreferences by lazy {
        appContext.getSharedPreferences(LEGACY_PREFS_NAME, Context.MODE_PRIVATE)
    }

    /**
     * كتلة التهيئة الأولية:
     * تنفذ تلقائياً عند بناء كائن الأمان لترحيل أية بيانات قديمة إلى المستودع المشفر.
     */
    init {
        migrateLegacyPreferencesIfNeeded()
    }

    /**
     * [تهيئة مستودع التفضيلات المشفر - initEncryptedPreferences]:
     * يقوم بتوليد أو جلب المفتاح الرئيسي من Android Keystore ثم ينشئ كائن
     * [EncryptedSharedPreferences]. وفي حال حدوث استثناء عتادي يتم التراجع للمستودع القديم.
     *
     * @return كائن [SharedPreferences] مشفر أو احتياطي.
     */
    private fun initEncryptedPreferences(): SharedPreferences {
        return try {
            // توليد أو جلب المفتاح الرئيسي المعتمد على خوارزمية AES-256 GCM
            val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)

            // بناء التفضيلات المشفرة بمخطط SIV للمفاتيح و GCM للقيم
            EncryptedSharedPreferences.create(
                ENCRYPTED_PREFS_NAME,
                masterKeyAlias,
                appContext,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (t: Throwable) {
            // عند تعذر Android Keystore لا نعود إلى SharedPreferences عادية؛ نستخدم مخزناً
            // مشفراً مستقلاً يعتمد PBKDF2 + AES-GCM حتى تبقى البيانات السرية مشفرة دائماً.
            Log.w(TAG, "Encrypted preference initialization unavailable; using encrypted PBKDF2 fallback")
            SecurePreferenceStore(appContext, FALLBACK_SECURE_PREFS_NAME, FALLBACK_KEY_ALIAS)
        }
    }

    /**
     * [ترحيل البيانات القديمة - migrateLegacyPreferencesIfNeeded]:
     * يفحص إذا كانت هناك بيانات مخزنة في التفضيلات القديمة غير المشفرة،
     * ويقوم بنسخها بالكامل إلى التفضيلات المشفرة لضمان عدم فقدان بيانات المستخدم السابقة.
     */
    private fun migrateLegacyPreferencesIfNeeded() {
        try {
            // ملف legacy لا يُستخدم كمخزن تشغيلي؛ وظيفته الوحيدة هي ترحيل البيانات القديمة.
            val legacyEntries = legacyPrefs.all.toMap()
            if (legacyEntries.isEmpty()) return

            val editor = securePrefs.edit()
            legacyEntries.forEach { (key, value) ->
                if (!securePrefs.contains(key)) {
                    when (value) {
                        is String -> editor.putString(key, value)
                        is Boolean -> editor.putBoolean(key, value)
                        is Int -> editor.putInt(key, value)
                        is Long -> editor.putLong(key, value)
                        is Float -> editor.putFloat(key, value)
                        is Set<*> -> editor.putStringSet(key, value.filterIsInstance<String>().toMutableSet())
                    }
                }
            }
            check(editor.commit()) { "Secure preference migration failed" }

            // بعد نجاح الترحيل نحذف النسخة الصريحة حتى لا تبقى نسخة سرية قابلة للاستخراج.
            legacyPrefs.edit().clear().commit()
        } catch (t: Throwable) {
            // فشل الترحيل لا يسمح بتشغيل المخزن الصريح؛ يظل المصدر الآمن هو المصدر التشغيلي.
            Log.w(TAG, "Legacy security preference migration failed safely")
        }
    }

    // =========================================================================
    // قسم: إدارة التراخيص والتفعيل (LICENSE & ACTIVATION MANAGEMENT)
    // =========================================================================

    /**
     * [جلب كود التفعيل المخزن - getActivationCode]:
     * يقرأ كود التفعيل من المستودع المشفر مع التراجع للاحتياطي.
     *
     * @return كود التفعيل أو نص فارغ إن لم يكن مفعلاً.
     */
    fun getActivationCode(): String {
        return securePrefs.getString(PREF_M_ACT_CODE, "") ?: ""
    }

    /**
     * [حفظ كود التفعيل - setActivationCode]:
     * يقوم بتنظيف وتوحيد حالة الأحرف لكود التفعيل وتخزينه في المستودعات بأمان.
     *
     * @param code كود التفعيل المدخل.
     */
    fun setActivationCode(code: String) {
        val clean = code.trim().uppercase()
        securePrefs.edit().putString(PREF_M_ACT_CODE, clean).apply()

    }

    /**
     * [جلب البريد الإلكتروني المفعل - getActivatedEmail]:
     * يقرأ البريد الإلكتروني المرتبط بالترخيص من المستودع المشفر.
     */
    fun getActivatedEmail(): String {
        return securePrefs.getString(PREF_M_ACTIVATED_EMAIL, "") ?: ""
    }

    /**
     * [حفظ البريد الإلكتروني المفعل - setActivatedEmail]:
     * يحفظ البريد الإلكتروني المرتبط بالترخيص بعد تنظيفه وتحويله لأحرف صغيرة.
     */
    fun setActivatedEmail(email: String) {
        val clean = email.trim().lowercase()
        securePrefs.edit().putString(PREF_M_ACTIVATED_EMAIL, clean).apply()

    }

    /**
     * [فحص حالة التفعيل المخبأة محلياً - isActivatedCached]:
     * يتحقق مما إذا كانت النسخة مفعلة مسبقاً ومسجلة في التفضيلات المحلية.
     *
     * @return true إذا كان الترخيص مفعلاً محلياً.
     */
    fun isActivatedCached(): Boolean {
        return securePrefs.getBoolean(PREF_IS_ACTIVATED_CACHED, false)
    }

    /**
     * [جلب معرف الجهاز المخبأ للترخيص - getCachedDeviceId]:
     * يعيد بصمة الجهاز التي تم ربط التفعيل بها.
     */
    fun getCachedDeviceId(): String {
        return securePrefs.getString(PREF_CACHED_FOR_DEVICE, "") ?: ""
    }

    /**
     * [حفظ بيانات التفعيل المؤقتة - setCachedActivation]:
     * يخزن حالة التفعيل والنسخة المميزة وبصمة الجهاز والكود لتمكين التحقق السريع دون اتصال.
     *
     * @param isActivated حالة التفعيل (مفعل أم لا).
     * @param deviceId بصمة الجهاز الحالية.
     * @param code كود التفعيل المعتمد.
     */
    fun setCachedActivation(isActivated: Boolean, deviceId: String = "", code: String = "") {
        val editor = securePrefs.edit()
            .putBoolean(PREF_IS_ACTIVATED_CACHED, isActivated)
            .putBoolean(PREF_IS_PREMIUM, isActivated)

        if (deviceId.isNotBlank()) {
            editor.putString(PREF_CACHED_FOR_DEVICE, deviceId)
        }
        if (code.isNotBlank()) {
            editor.putString(PREF_CACHED_FOR_CODE, code)
        }
        editor.apply()


    }

    /**
     * [مسح بيانات التفعيل والترخيص - clearActivationData]:
     * يقوم بإلغاء التفعيل وحذف كافة المفاتيح والبريد وحالة النسخة المميزة بالكامل.
     */
    fun clearActivationData() {
        securePrefs.edit()
            .remove(PREF_M_ACTIVATED_EMAIL)
            .remove(PREF_M_ACT_CODE)
            .putBoolean(PREF_IS_PREMIUM, false)
            .putBoolean(PREF_IS_ACTIVATED_CACHED, false)
            .remove(PREF_CACHED_FOR_CODE)
            .remove(PREF_CACHED_FOR_DEVICE)
            .apply()


    }

    /**
     * [جلب معرف الجهاز الموحد - getUnifiedDeviceId]:
     * يقرأ البصمة العتادية الموحدة للجهاز.
     */
    fun getUnifiedDeviceId(): String {
        val deviceId = securePrefs.getString(PREF_UNIFIED_DEVICE_ID, "") ?: ""
        if (deviceId.isNotBlank()) return deviceId
        return ""
    }

    /**
     * [حفظ معرف الجهاز الموحد - setUnifiedDeviceId]:
     * يخزن البصمة العتادية للجهاز في التفضيلات المشفرة.
     */
    fun setUnifiedDeviceId(deviceId: String) {
        securePrefs.edit().putString(PREF_UNIFIED_DEVICE_ID, deviceId).apply()

    }

    // =========================================================================
    // قسم: إعدادات الأمان والحماية الحيوية (SECURITY & BIOMETRIC SETTINGS)
    // =========================================================================

    /**
     * [التحقق من تفعيل رمز المرور السريع - isFastPasscodeEnabled]:
     * يفحص ما إذا كان المستخدم قد فعل قفل التطبيق برمز PIN.
     */
    fun isFastPasscodeEnabled(): Boolean {
        return securePrefs.getBoolean(PREF_FAST_PASSCODE_ENABLED, false)
    }

    /**
     * [ضبط تفعيل رمز المرور السريع - setFastPasscodeEnabled]:
     * تفعيل أو تعطيل قفل التطبيق برمز PIN.
     */
    fun setFastPasscodeEnabled(enabled: Boolean) {
        securePrefs.edit().putBoolean(PREF_FAST_PASSCODE_ENABLED, enabled).apply()

    }

    /**
     * [التحقق من تفعيل المصادقة الحيوية - isBiometricEnabled]:
     * يفحص إمكانية فتح التطبيق بالبصمة أو التعرف على الوجه (مفعلة افتراضياً عند تفعيل الرمز).
     */
    fun isBiometricEnabled(): Boolean {
        // مفعل افتراضياً عندما يكون قفل رمز المرور مفعل
        return securePrefs.getBoolean(PREF_BIOMETRIC_ENABLED, true)
    }

    /**
     * [ضبط تفعيل المصادقة الحيوية - setBiometricEnabled]:
     * حفظ خيار تفعيل أو إلغاء البصمة/الوجه لفتح التطبيق.
     */
    fun setBiometricEnabled(enabled: Boolean) {
        securePrefs.edit().putBoolean(PREF_BIOMETRIC_ENABLED, enabled).apply()

    }

    /**
     * [حفظ تجزئة رمز المرور المشفر - saveAdminPin]:
     * يحسب تجزئة SHA-256 محصنة بالملح التشفيري وبصمة الجهاز ويحفظها بأمان.
     */
    fun saveAdminPin(pin: String) {
        val pinChars = pin.toCharArray()
        val hash = try { HashUtils.hashPassword(pinChars) } finally { HashUtils.wipeCharArray(pinChars) }
        securePrefs.edit()
            .putString(PREF_ADMIN_PIN_HASH, hash)
            .putInt(PREF_FAILED_PIN_ATTEMPTS, 0)
            .putLong(PREF_LOCKOUT_UNTIL_TIMESTAMP, 0L)
            .apply()

    }

    /**
     * [التحقق من وجود رمز مرور مسجل - hasAdminPin]:
     */
    fun hasAdminPin(): Boolean {
        val hash = securePrefs.getString(PREF_ADMIN_PIN_HASH, "") ?: ""
        return hash.isNotBlank()
    }

    /**
     * [فحص حالة الحظر المؤقت - isPinLockedOut]:
     */
    fun isPinLockedOut(): Boolean {
        val lockoutUntil = securePrefs.getLong(PREF_LOCKOUT_UNTIL_TIMESTAMP, 0L)
        return System.currentTimeMillis() < lockoutUntil
    }

    /**
     * [جلب ثواني الحظر المتبقية - getRemainingLockoutSeconds]:
     */
    fun getRemainingLockoutSeconds(): Long {
        val lockoutUntil = securePrefs.getLong(PREF_LOCKOUT_UNTIL_TIMESTAMP, 0L)
        val remainingMs = lockoutUntil - System.currentTimeMillis()
        return if (remainingMs > 0) remainingMs / 1000L else 0L
    }

    /**
     * [التحقق الآمن من رمز المرور - validateAdminPin]:
     * يتحقق باستخدام المقارنة الزمنية الثابتة مع حماية من هجمات القوة الغاشمة (Brute-Force Lockout).
     */
    fun validateAdminPin(enteredPin: String): Boolean {
        if (isPinLockedOut()) {
            return false
        }
        val storedHash = securePrefs.getString(PREF_ADMIN_PIN_HASH, "") ?: ""
        if (storedHash.isBlank()) {
            return true
        }

        val enteredChars = enteredPin.toCharArray()
        val isMatch = try {
            HashUtils.verifyPassword(enteredChars, storedHash, getUnifiedDeviceId())
        } finally {
            HashUtils.wipeCharArray(enteredChars)
        }

        if (isMatch) {
            // إذا كانت البيانات من إصدار قديم نرفعها فوراً إلى PBKDF2 دون تغيير المفتاح المحاسبي.
            if (!storedHash.startsWith("v2$")) {
                saveAdminPin(enteredPin)
            }
            securePrefs.edit()
                .putInt(PREF_FAILED_PIN_ATTEMPTS, 0)
                .putLong(PREF_LOCKOUT_UNTIL_TIMESTAMP, 0L)
                .apply()
            return true
        } else {
            val failedAttempts = (securePrefs.getInt(PREF_FAILED_PIN_ATTEMPTS, 0) + 1)
            val editor = securePrefs.edit().putInt(PREF_FAILED_PIN_ATTEMPTS, failedAttempts)
            if (failedAttempts >= 5) {
                // حظر لمدة 30 ثانية بعد 5 محاولات خاطئة متتالية
                val lockoutDurationMs = 30_000L
                editor.putLong(PREF_LOCKOUT_UNTIL_TIMESTAMP, System.currentTimeMillis() + lockoutDurationMs)
            }
            editor.apply()
            return false
        }
    }

    /**
     * [مسح رمز المرور المسجل - clearAdminPin]:
     */
    fun clearAdminPin() {
        securePrefs.edit()
            .remove(PREF_ADMIN_PIN_HASH)
            .remove(PREF_FAILED_PIN_ATTEMPTS)
            .remove(PREF_LOCKOUT_UNTIL_TIMESTAMP)
            .apply()

    }

    // =========================================================================
    // قسم: تسجيل مستمعي تغيير التفضيلات (LISTENER REGISTRATION)
    // =========================================================================

    /**
     * [تسجيل مستمع لمراقبة تغييرات التفضيلات - registerListener]:
     * يسمح لطبقات الواجهة والخدمات بمراقبة التغييرات الأمنية لحظياً.
     */
    fun registerListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
        securePrefs.registerOnSharedPreferenceChangeListener(listener)

    }

    /**
     * [إلغاء تسجيل مستمع تغيير التفضيلات - unregisterListener]:
     * إزالة المستمع لمنع تسرب الذاكرة عند انتهاء دورة حياة المكون.
     */
    fun unregisterListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
        securePrefs.unregisterOnSharedPreferenceChangeListener(listener)

    }

    // =========================================================================
    // قسم: الكائن المرافق والثوابت والنمط الأحادي (COMPANION OBJECT)
    // =========================================================================
    companion object {
        /** وسم السجلات التشخيصية للأمان */
        private const val TAG = "AppSecurityManager"
        /** اسم ملف التفضيلات المشفرة */
        private const val ENCRYPTED_PREFS_NAME = "mizan_encrypted_sec_prefs"
        /** اسم ملف التفضيلات القديم المستخدم للترحيل مرة واحدة فقط. */
        private const val LEGACY_PREFS_NAME = "mizan_sec_prefs"
        /** اسم مخزن التراجع المشفر عند تعذر Android Keystore. */
        private const val FALLBACK_SECURE_PREFS_NAME = "mizan_secure_fallback"
        /** اسم مفتاح التراجع المشفر؛ لا تُخزن قيمة المفتاح خارج SecurePreferenceStore. */
        private const val FALLBACK_KEY_ALIAS = "mizan_secure_fallback_key"

        /** مفاتيح التفضيلات الثابتة */
        const val PREF_M_ACT_CODE = "m_act_code"
        const val PREF_M_ACTIVATED_EMAIL = "m_activated_email"
        const val PREF_IS_ACTIVATED_CACHED = "is_activated_cached"
        const val PREF_CACHED_FOR_DEVICE = "cached_for_device"
        const val PREF_CACHED_FOR_CODE = "cached_for_code"
        const val PREF_IS_PREMIUM = "is_premium"
        const val PREF_IS_PERMANENT = "is_permanent"
        const val PREF_UNIFIED_DEVICE_ID = "unified_device_id"
        const val PREF_FAST_PASSCODE_ENABLED = "fast_passcode_enabled"
        const val PREF_BIOMETRIC_ENABLED = "biometric_enabled"
        const val PREF_ADMIN_PIN_HASH = "admin_pin_hash"
        const val PREF_FAILED_PIN_ATTEMPTS = "failed_pin_attempts"
        const val PREF_LOCKOUT_UNTIL_TIMESTAMP = "lockout_until_timestamp"

        /** النسخة الأحادية الحية من مدير الأمان في الذاكرة */
        @Volatile
        private var INSTANCE: AppSecurityManager? = null

        /**
         * [جلب النسخة الأحادية لمدير الأمان - getInstance]:
         * نمط القفل المزدوج المتقلب (Double-Checked Locking) لضمان أمان الخيوط المتعددة.
         *
         * @param context سياق التطبيق.
         * @return كائن [AppSecurityManager] الموحد للتطبيق.
         */
        fun getInstance(context: Context): AppSecurityManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AppSecurityManager(context).also { INSTANCE = it }
            }
        }
    }
}

