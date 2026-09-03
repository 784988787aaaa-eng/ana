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
     * [دالة التحقق من بنية وصلاحية معرف عميل الويب]:
     * تفحص نص المعرف للتأكد من أنه ليس فارغاً ولا placeholder وينتهي بالامتداد القياسي (.apps.googleusercontent.com)
     */
    fun isWebClientIdValid(clientId: String = WEB_CLIENT_ID): Boolean {
        val trimmed = clientId.trim()
        if (trimmed.isEmpty() || trimmed == "YOUR_GOOGLE_CLIENT_ID" || trimmed == "none") {
            return false
        }
        val suffix = ".apps.googleusercontent.com"
        if (!trimmed.endsWith(suffix)) {
            return false
        }
        val prefix = trimmed.substring(0, trimmed.length - suffix.length)
        return prefix.isNotEmpty() && prefix.any { it.isDigit() }
    }

    /**
     * [دالة التحقق من صحة معرف العميل - validateClientId]:
     * تفحص نص المعرف للتأكد من أنه ليس فارغاً وينتهي بالامتداد القياسي (.apps.googleusercontent.com)
     * ويحتوي على أرقام تعريفية صالحة قبل بدء عملية تسجيل الدخول.
     */
    fun validateClientId(): Boolean {
        val isValid = isWebClientIdValid(WEB_CLIENT_ID)
        if (!isValid) {
            Log.e(TAG, "❌ [GOOGLE_AUTH_ERROR] WEB_CLIENT_ID is invalid, empty or placeholder.")
            return false
        }
        
        Log.d(TAG, "✅ [GOOGLE_AUTH_SUCCESS] Google WEB_CLIENT_ID is valid and configured.")
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
