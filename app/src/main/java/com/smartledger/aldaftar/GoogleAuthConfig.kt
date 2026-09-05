/**
 * =====================================================================
 * ملف: إعدادات مصادقة جوجل (GoogleAuthConfig.kt)
 * =====================================================================
 * 
 * [الغرض العام والتعليمي من الملف]:
 * يمثل هذا الكائن الأحادي (Singleton Object) مرجع التكوين المركزي لخدمات المصادقة
 * وتسجيل الدخول بحسابات Google وخدمات السحابة (Google Drive API).
 * 
 * [أهميته في حل مشكلات الاعتماد (OAuth Resolution)]:
 * - يستخرج معرف العميل الموحد (Web Client ID) من `BuildConfig`.
 * - يوفر دالة تحقق صارمة لصيغة المعرف لتفادي الأخطاء الشائعة (مثل الخطأ 10 Developer Error).
 * - يوفر أداة برمجية لفحص واستخراج بصمة التوقيع الرقمية للشهادة (SHA-1 Fingerprint)
 *   واسم الحزمة (Package Name) ديناميكياً من نظام أندرويد لتسهيل التحقق والتكامل مع Google Cloud Console.
 */
package com.smartledger.aldaftar

// ---------------------------------------------------------------------
// استيراد حزم إدارة الحزم والتوقيعات الرقمية واستخراج بصمات SHA-1
// ---------------------------------------------------------------------
import android.util.Log

/**
 * [كائن إعدادات المصادقة - GoogleAuthConfig]:
 * كائن عام يتيح الوصول السريع لبيانات الاعتماد وأدوات فحص توقيع التطبيق.
 */
object GoogleAuthConfig {
    private const val TAG = "GoogleAuthConfig"

    // -----------------------------------------------------------------
    // قراءة معرف العميل المعتمد (Web Client ID) من متغيرات البناء BuildConfig
    // -----------------------------------------------------------------
    val WEB_CLIENT_ID = BuildConfig.GOOGLE_CLIENT_ID

    /**
     * [دالة التحقق من صحة معرف العميل - validateClientId]:
     * تفحص نص المعرف للتأكد من أنه ليس فارغاً وينتهي بالامتداد القياسي (.apps.googleusercontent.com)
     * ويحتوي على أرقام تعريفية صالحة قبل بدء عملية تسجيل الدخول.
     */
    fun validateClientId(): Boolean {
        if (WEB_CLIENT_ID.isEmpty()) {
            Log.e(TAG, "❌ [GOOGLE_AUTH_ERROR] WEB_CLIENT_ID is EMPTY! Please insert your Web Client ID in GoogleAuthConfig.kt")
            return false
        }
        
        val trimmed = WEB_CLIENT_ID.trim()
        val suffix = ".apps.googleusercontent.com"
        
        if (!trimmed.endsWith(suffix)) {
            Log.e(TAG, "Google Web Client ID format is invalid")
            return false
        }
        
        val prefix = trimmed.substring(0, trimmed.length - suffix.length)
        if (prefix.isEmpty() || !prefix.any { it.isDigit() }) {
            Log.e(TAG, "Google Web Client ID prefix is invalid")
            return false
        }
        
        Log.d(TAG, "Google Web Client ID is configured")
        return true
    }


}
