/**
 * =====================================================================
 * ملف: نموذج وحالات الترخيص دون اتصال (.)
 * =====================================================================
 * 
 * [الغرض العام والتعليمي من الملف]:
 * يوفر نموذج حالات الترخيص الصريح (    )
 * لضمان الفصل التام بين حالات الترخيص الفعلية وحالات انقطاع الشبكة.
 * 
 * [القاعدة الأمنية والمحاسبية الإلزامية]:
 * انقطاع الشبكة أو تعذر الوصول إلى الخادم لا يعني إلغاء الترخيص
 * (  !=  ).
 * لا يتم مسح أو حذف بيانات المستخدم أو إلغاء تفعيل الكاش المحلي عند فقدان الاتصال.
 */
package com.smartledger.aldaftar.domain

/**
 * [حالات الترخيص الصريحة - ]:
 */
sealed class LicenseState {
    /** الترخيص سارٍ ومفعل إما عبر السحابة أو الكاش المحلي المشفر */
    data class Valid(
        val email: String,
        val deviceId: String,
        val isOfflineVerified: Boolean = false
    ) : LicenseState()

    /** الترخيص غير مسجل أو البيانات المدخلة غير صالحة */
    data class Invalid(val message: String) : LicenseState()

    /** الترخيص ملغى بشكل صريح ومؤكد من قبل الإدارة السحابية */
    data class Revoked(val reason: String) : LicenseState()

    /** انقطاع الشبكة أو تعذر الاتصال مع الحفاظ التام على حالة الكاش المشفر المحلي */
    data class NetworkUnavailable(
        val lastKnownEmail: String?,
        val fallbackValid: Boolean
    ) : LicenseState()

    /** يتطلب تسجيل الدخول أو ربط الحساب */
    object AuthRequired : LicenseState()

    /** الحالة غير محددة بعد (قيد الفحص) */
    object Unknown : LicenseState()
}
