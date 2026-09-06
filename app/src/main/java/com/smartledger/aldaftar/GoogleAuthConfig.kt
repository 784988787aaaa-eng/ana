/**
 * ملف مركزي لإعدادات مصادقة خدمات جوجل والتحقق من معرف العميل.
 * يقرأ المعرف من إعدادات البناء ولا يخزن أي سر حساس داخل المصدر.
 */
package com.smartledger.aldaftar

import android.util.Log

/**
 * كائن مركزي يتيح قراءة إعدادات المصادقة والتحقق من صيغة معرف العميل.
 */
object GoogleAuthConfig {
    private const val TAG = "GoogleAuthConfig"

    /** معرف العميل العام المستخدم لبدء جلسة مصادقة خدمات جوجل. */
    val WEB_CLIENT_ID: String = BuildConfig.GOOGLE_CLIENT_ID.trim()

    /**
     * يتحقق من أن المعرف موجود ويطابق البنية المتوقعة دون تسجيل قيمته.
     */
    fun validateClientId(): Boolean {
        if (WEB_CLIENT_ID.isBlank()) {
            Log.e(TAG, "معرف عميل جوجل غير مضبوط في إعدادات البناء")
            return false
        }

        val suffix = ".apps.googleusercontent.com"
        if (!WEB_CLIENT_ID.endsWith(suffix)) {
            Log.e(TAG, "صيغة معرف عميل جوجل غير صالحة")
            return false
        }

        val prefix = WEB_CLIENT_ID.removeSuffix(suffix)
        if (prefix.isBlank() || prefix.any { !it.isDigit() }) {
            Log.e(TAG, "مقدمة معرف عميل جوجل غير صالحة")
            return false
        }

        Log.d(TAG, "تم التحقق من إعداد معرف عميل جوجل")
        return true
    }
}
