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
 */
package com.example

import android.util.Log

/**
 * [كائن إعدادات المصادقة - GoogleAuthConfig]:
 * كائن عام يتيح الوصول السريع لبيانات الاعتماد والتحقق من معرف العميل.
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
            Log.e(TAG, "❌ [GOOGLE_AUTH_ERROR] Invalid Web Client ID: It must end with '$suffix'. Got: '$trimmed'")
            return false
        }
        
        val prefix = trimmed.substring(0, trimmed.length - suffix.length)
        if (prefix.isEmpty() || !prefix.any { it.isDigit() }) {
            Log.e(TAG, "❌ [GOOGLE_AUTH_ERROR] Invalid Web Client ID prefix. Prefix must contain numeric digits. Got: '$trimmed'")
            return false
        }
        
        Log.d(TAG, "✅ [GOOGLE_AUTH_SUCCESS] Google WEB_CLIENT_ID is valid and configured: '$trimmed'")
        return true
    }
}

