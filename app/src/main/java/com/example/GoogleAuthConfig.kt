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
package com.example

// ---------------------------------------------------------------------
// استيراد حزم إدارة الحزم والتوقيعات الرقمية واستخراج بصمات SHA-1
// ---------------------------------------------------------------------
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import java.security.MessageDigest

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

    /**
     * [دالة فحص وطباعة توقيع التطبيق - logAppSignatureAndPackage]:
     * تستخرج بصمة SHA-1 لشهادة توقيع الـ APK واسم الحزمة وتطبعها في سجلات Logcat.
     * مفيدة جداً للمطورين لنسخ البصمة بدقة إلى Google Cloud Console.
     */
    fun logAppSignatureAndPackage(context: Context) {
        val packageName = context.packageName
        Log.d(TAG, "===============================================")
        Log.d(TAG, "📱 [GOOGLE_AUTH_COMPATIBILITY_CHECK] Checking Configuration:")
        Log.d(TAG, "📱 App Package Name: $packageName")
        
        try {
            // استخراج التوقيعات وفق إصدار نظام أندرويد (دعم Android 9 Pie وما فوق مع الإصدارات الأقدم)
            val signatures = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val info = context.packageManager.getPackageInfo(
                    packageName,
                    PackageManager.GET_SIGNING_CERTIFICATES
                )
                val signingInfo = info.signingInfo
                if (signingInfo != null) {
                    if (signingInfo.hasMultipleSigners()) {
                        signingInfo.apkContentsSigners
                    } else {
                        signingInfo.signingCertificateHistory
                    }
                } else null
            } else {
                @Suppress("DEPRECATION")
                val info = context.packageManager.getPackageInfo(
                    packageName,
                    PackageManager.GET_SIGNATURES
                )
                @Suppress("DEPRECATION")
                info.signatures
            }

            // تحويل مصفوفة البايتات الخام للشهادة إلى بصمة SHA-1 بصيغة Hexadecimal مقروءة
            if (signatures != null) {
                for ((index, sig) in signatures.withIndex()) {
                    val rawCert = sig.toByteArray()
                    val md = MessageDigest.getInstance("SHA-1")
                    val publicKey = md.digest(rawCert)
                    val hexString = publicKey.joinToString(":") { 
                        String.format("%02X", it) 
                    }
                    Log.d(TAG, "🔑 SHA-1 Fingerprint [#$index]: $hexString")
                }
            } else {
                Log.e(TAG, "⚠️ No signing signatures found for this app!")
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error fetching signatures", e)
        }
        
        Log.d(TAG, "===============================================")
    }
}
