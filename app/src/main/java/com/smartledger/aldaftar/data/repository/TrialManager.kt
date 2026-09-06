
/**
 * واجهة تفويض لمنظومة الترخيص؛ تبقي واجهة الاستدعاء الحالية ثابتة وتمنع تكرار منطق الترخيص.
 * التوثيق هنا يوضح أثر الدوال على الأمان والتوافق والدقة المالية دون تغيير واجهات الاستدعاء.
 */
package com.smartledger.aldaftar.data.repository

import android.content.Context

class TrialManager(private val context: Context) {

    private val licenseAndTrialManager = LicenseAndTrialManager(context)

    companion object {
        
        const val SECURE_LIMIT_VAL: Int = LicenseAndTrialManager.SECURE_LIMIT_VAL
        
        const val PREFIX_TEMP = LicenseAndTrialManager.PREFIX_TEMP
        
        const val PREFIX_PERM = LicenseAndTrialManager.PREFIX_PERM

        /**
         * يتحقق من كود التفعيل بصمةً بصمةً وبمقارنة ثابتة الزمن لمنع تسريب النتيجة عبر فروق التوقيت.
         */
        fun verifyActivationCode(deviceId: String, enteredCode: String): Boolean =
            LicenseAndTrialManager.verifyActivationCode(deviceId, enteredCode)

        /**
         * يجلب معرف الجهاز الموحد أو ينشئه مرة واحدة ويحفظه عبر مخزن الأمان المعتمد لضمان ثبات الربط.
         */
        fun getOrGenerateUnifiedDeviceId(context: Context): String =
            LicenseAndTrialManager.getOrGenerateUnifiedDeviceId(context)
    }

    /**
     * يجمع إشارات الترخيص المستقلة ويفحص بيئة التشغيل قبل قبول حالة التفعيل.
     */
    fun isAppActivated(): Boolean = licenseAndTrialManager.isAppActivated()

    /**
     * يحدد انتهاء الحصة التجريبية اعتماداً على العدد الفعلي للعمليات دون تعديل البيانات المالية.
     */
    fun isTrialExpiredDirect(realTotalTransactionsCount: Int): Boolean =
        licenseAndTrialManager.isTrialExpiredDirect(realTotalTransactionsCount)

    /**
     * يتحقق من كود التفعيل قبل تخزينه ولا يسمح بالتفعيل المحلي في بيئة تشغيل غير موثوقة.
     */
    fun activateLicenseWithCode(code: String): Boolean =
        licenseAndTrialManager.activateLicenseWithCode(code)

    /**
     * يحفظ اعتماد البريد السحابي بعد ربطه بمعرف الجهاز المقدم من مسار الترخيص المعتمد.
     */
    fun saveEmailActivation(email: String, deviceId: String) {
        licenseAndTrialManager.saveEmailActivation(email, deviceId)
    }

    /**
     * يمسح حالة التفعيل المحلية من مخزن الأمان ويعيد التطبيق إلى حالة عدم التفعيل.
     */
    fun clearLocalActivation() {
        licenseAndTrialManager.clearLocalActivation()
    }

    fun getActivatedEmail(): String = licenseAndTrialManager.getActivatedEmail()

    fun getDeviceId(): String = licenseAndTrialManager.getDeviceId()
}

