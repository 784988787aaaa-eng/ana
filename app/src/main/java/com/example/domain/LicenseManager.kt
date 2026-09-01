/**
 * =====================================================================
 * ملف: واجهة مدير التراخيص وبصمة الأجهزة بطبقة النطاق (LicenseManager.kt)
 * =====================================================================
 * 
 * [الغرض العام والتعليمي من الملف]:
 * يمثل هذا الكائن واجهة التراخيص المركزية داخل طبقة النطاق والمنطق التجاري (Domain Layer).
 * يعمل كبوابة وسيطة تفوض عمليات التحقق من أكواد التفعيل وتوليد البصمة العتادية الموحدة
 * للأجهزة إلى مستودع التراخيص الموحد [LicenseAndTrialManager].
 * 
 * [المسؤوليات المعمارية والتقنية للملف]:
 * 1. عزل المنطق التجاري للتراخيص (Domain Isolation):
 *    - فصل طبقة واجهات المستخدم والـ ViewModels عن تفاصيل التخزين المشفر منخفض المستوى.
 * 2. إعادة تصدير الثوابت الأمنية (Security Constants Export):
 *    - توفير البادئات الرسمية للتفعيل المؤقت والدائم وحدود العمليات المجانية.
 * 3. تفويض التحقق من الأكواد وتوليد البصمات (Delegated Verification & Fingerprinting).
 */
package com.example.domain

// ---------------------------------------------------------------------
// استيراد حزم سياق أندرويد ومستودع إدارة التراخيص والفترات التجريبية
// ---------------------------------------------------------------------
import android.content.Context
import com.example.data.repository.LicenseAndTrialManager

/**
 * [الكائن الأحادي لمدير التراخيص في طبقة النطاق - LicenseManager]:
 * يوفر نقاط اتصال معيارية للتحقق من كود التفعيل وجلب بصمة الجهاز الموحدة.
 */
object LicenseManager {

    /** بادئة كود التفعيل المؤقت */
    const val PREFIX_TEMP = LicenseAndTrialManager.PREFIX_TEMP

    /** بادئة كود التفعيل الدائم */
    const val PREFIX_PERM = LicenseAndTrialManager.PREFIX_PERM

    /** الحد الأقصى الآمن للعمليات في النسخة المجانية قبل التفعيل */
    const val SECURE_LIMIT_VAL: Int = LicenseAndTrialManager.SECURE_LIMIT_VAL

    /**
     * [التحقق من صحة كود التفعيل المدخل - verifyActivationCode]:
     * يفوض التحقق إلى [LicenseAndTrialManager] لمقارنة الكود بالبصمة العتادية للجهاز.
     *
     * @param deviceId البصمة الموحدة للجهاز.
     * @param enteredCode كود التفعيل المدخل من قبل المستخدم.
     * @return true إذا كان الكود صحيحاً ومطابقاً للبصمة.
     */
    fun verifyActivationCode(deviceId: String, enteredCode: String): Boolean {
        return LicenseAndTrialManager.verifyActivationCode(deviceId, enteredCode)
    }

    /**
     * [جلب أو توليد بصمة الجهاز الموحدة - getOrGenerateUnifiedDeviceId]:
     * يستخرج المعرف الرقمي المشفر والمشتق من عتاد الهاتف.
     *
     * @param context سياق التطبيق.
     * @return البصمة الموحدة للجهاز.
     */
    fun getOrGenerateUnifiedDeviceId(context: Context): String {
        return LicenseAndTrialManager.getOrGenerateUnifiedDeviceId(context)
    }
}

