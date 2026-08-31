/**
 * =====================================================================
 * ملف: مدير الفترة التجريبية والتراخيص المباشر (TrialManager.kt)
 * =====================================================================
 * 
 * [الغرض العام والتعليمي من الملف]:
 * يعمل هذا الصنف كواجهة تفويض مبسطة (Facade Adapter Pattern) لمدير التراخيص
 * المركزي [LicenseAndTrialManager]، حيث يوفر دوالاً ملائمة ومباشرة لطبقات المستودع
 * ونماذج العرض (ViewModels) لفحص صلاحية الاستخدام والتفعيل وحدود العمليات المجانية.
 * 
 * [المسؤوليات المعمارية والتقنية]:
 * 1. تفويض فحص التفعيل إلى المرجع المركزي الموحد.
 * 2. تطبيق سقف العمليات التجريبية [SECURE_LIMIT_VAL] (100 عملية كحد أقصى).
 * 3. توفير دوال التحقق التشفيري الثابتة وتوليد بصمة الجهاز الموحدة.
 */
package com.example.data.repository

// ---------------------------------------------------------------------
// استيراد حزم سياق أندرويد
// ---------------------------------------------------------------------
import android.content.Context

/**
 * [فئة مدير الفترة التجريبية - TrialManager]:
 * تفوض عمليات الترخيص والتحقق من انتهاء التجربة إلى [LicenseAndTrialManager].
 *
 * @param context سياق التطبيق للوصول للتفضيلات المشفرة وموارد النظام.
 */
class TrialManager(private val context: Context) {

    /** المرجع المركزي الموحد لمنظومة التراخيص */
    private val licenseAndTrialManager = LicenseAndTrialManager(context)

    /**
     * [الكائن المرافق للثوابت والدوال الثابتة]:
     */
    companion object {
        /** سقف عدد العمليات المسموح بها في الفترة التجريبية */
        const val SECURE_LIMIT_VAL: Int = LicenseAndTrialManager.SECURE_LIMIT_VAL
        /** بادئة كود التفعيل المؤقت */
        const val PREFIX_TEMP = LicenseAndTrialManager.PREFIX_TEMP
        /** بادئة كود التفعيل الدائم */
        const val PREFIX_PERM = LicenseAndTrialManager.PREFIX_PERM

        /**
         * التحقق التشفيري المباشر من كود التفعيل عبر تجزئة SHA-256
         */
        fun verifyActivationCode(deviceId: String, enteredCode: String): Boolean =
            LicenseAndTrialManager.verifyActivationCode(deviceId, enteredCode)

        /**
         * توليد أو جلب معرف الجهاز الموحد المشفر
         */
        fun getOrGenerateUnifiedDeviceId(context: Context): String =
            LicenseAndTrialManager.getOrGenerateUnifiedDeviceId(context)
    }

    /**
     * [التحقق من حالة تفعيل التطبيق - isAppActivated]:
     * يفحص ما إذا كان التطبيق مفعلًا برمز تنشيط صالح أو بريد سحابي مرخص.
     */
    fun isAppActivated(): Boolean = licenseAndTrialManager.isAppActivated()

    /**
     * [التحقق من انتهاء الفترة التجريبية - isTrialExpiredDirect]:
     * يقارن إجمالي العمليات المسجلة بسقف العمليات التجريبية المسموح.
     *
     * @param realTotalTransactionsCount العدد الفعلي الكلي للعمليات في قاعدة البيانات.
     * @return true إذا تجاوز السقف وكان التطبيق غير مفعل.
     */
    fun isTrialExpiredDirect(realTotalTransactionsCount: Int): Boolean =
        licenseAndTrialManager.isTrialExpiredDirect(realTotalTransactionsCount)

    /**
     * [تفعيل الترخيص بواسطة كود التفعيل - activateLicenseWithCode]:
     *
     * @param code كود التفعيل المدخل.
     * @return true إذا كان الكود صحيحاً ومطابقاً لبصمة الجهاز.
     */
    fun activateLicenseWithCode(code: String): Boolean =
        licenseAndTrialManager.activateLicenseWithCode(code)

    /**
     * [حفظ تفعيل البريد الإلكتروني السحابي - saveEmailActivation]:
     *
     * @param email البريد المرخص من السحابة.
     * @param deviceId معرف الجهاز المعتمد.
     */
    fun saveEmailActivation(email: String, deviceId: String) {
        licenseAndTrialManager.saveEmailActivation(email, deviceId)
    }

    /**
     * [مسح بيانات التفعيل المحلية بأمان - clearLocalActivation]:
     * يعيد ضبط حالة الجهاز إلى الوضع التجريبي الافتراضي.
     */
    fun clearLocalActivation() {
        licenseAndTrialManager.clearLocalActivation()
    }

    /** استرجاع البريد الإلكتروني المرخص المسجل محلياً */
    fun getActivatedEmail(): String = licenseAndTrialManager.getActivatedEmail()

    /** استرجاع بصمة ومعرف الجهاز الموحد */
    fun getDeviceId(): String = licenseAndTrialManager.getDeviceId()
}

