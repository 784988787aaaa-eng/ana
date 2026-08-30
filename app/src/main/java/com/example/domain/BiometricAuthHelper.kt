/**
 * =====================================================================
 * ملف: مساعد المصادقة الحيوية وبصمة الإصبع والوجه (BiometricAuthHelper.kt)
 * =====================================================================
 * 
 * [الغرض العام والتعليمي من الملف]:
 * يوفر هذا الكائن وسيطاً آمناً ومباشراً للتعامل مع واجهة المصادقة الحيوية
 * المدمجة في نظام أندرويد [AndroidX BiometricPrompt API].
 * يتيح للتطبيق قفل وفتح الشاشات والعمليات الحساسة باستخدام بصمة الإصبع أو
 * التعرف على الوجه المعتمدة على عتاد الجهاز، مع ضمان استجابة واجهة المستخدم
 * والتأكد المسبق من توفر مستشعرات البصمة وتسجيل أصابع المستخدم قبل إطلاق نافذة التحقق.
 * 
 * [المسؤوليات المعمارية والتقنية للملف]:
 * 1. التحقق الاستباقي من توفر العتاد الحيوي (Hardware & Enrollment Check):
 *    - فحص وجود قارئ البصمة/الوجه وتسجيل بيانات حيوية صالحة عبر [BiometricManager].
 * 2. إطلاق نافذة المصادقة النظامية الآمنة (Secure Biometric Prompt Dialog):
 *    - بناء وتخصيص نافذة التحقق القياسية التابعة للنظام دون إمكانية اعتراضها أو تزويرها.
 * 3. معالجة أحداث الاستجابة الحيوية (Authentication Callbacks):
 *    - التعامل السلس مع حالات: النجاح [onSuccess]، والخطأ الحرج أو الإلغاء [onError]،
 *      وفشل مطابقة البصمة مع المحاولة مجدداً [onFailed].
 * 4. التنفيذ الآمن على مسار الواجهة الرئيسي (Main Thread Execution):
 *    - ضمان عودة النتائج مباشرة على المسار الرئيسي لتحديث واجهات Compose فوراً.
 */
package com.example.domain

// ---------------------------------------------------------------------
// استيراد حزم سياق أندرويد، ومدير وموجه المصادقة الحيوية ومكونات التوافق
// ---------------------------------------------------------------------
import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

/**
 * [الكائن الأحادي لمساعد المصادقة الحيوية - BiometricAuthHelper]:
 * يجمع دوال التحقق من توفر البصمة وإطلاق نافذة المصادقة.
 */
object BiometricAuthHelper {

    /**
     * تحديد مستويات المصادقة الحيوية المقبولة:
     * يشمل البصمات القوية عتادياً [BIOMETRIC_STRONG] والبصمات الموثوقة القياسية [BIOMETRIC_WEAK].
     */
    private const val AUTHENTICATORS = BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.BIOMETRIC_WEAK

    /**
     * [التحقق من جاهزية وتوفر المصادقة الحيوية - isBiometricAvailable]:
     * يفحص ما إذا كان الجهاز يحتوي على مستشعر حيوي نشط، وأن المستخدم قام بتسجيل بصمته بالفعل.
     *
     * @param context سياق التطبيق للوصول إلى مدير القياسات الحيوية.
     * @return true إذا كانت البصمة جاهزة للاستخدام الفوري، وإلا false.
     */
    fun isBiometricAvailable(context: Context): Boolean {
        return try {
            val biometricManager = BiometricManager.from(context)
            biometricManager.canAuthenticate(AUTHENTICATORS) == BiometricManager.BIOMETRIC_SUCCESS
        } catch (_: Throwable) {
            false
        }
    }

    /**
     * [إطلاق نافذة المصادقة الحيوية - authenticate]:
     * يفتح حوار المصادقة الرسمي التابع لنظام أندرويد مع ربط المستمعين للنتائج.
     *
     * @param activity النشاط الحاضن [FragmentActivity] المطلوب لربط حوار BiometricPrompt.
     * @param title عنوان نافذة التحقق (مثال: "تأكيد الهوية").
     * @param subtitle العنوان الفرعي التوضيحي (مثال: "المس مستشعر البصمة للمتابعة").
     * @param negativeButtonText نص زر الإلغاء أو استخدام رمز المرور.
     * @param onSuccess الدالة التي يتم استدعاؤها عند نجاح مطابقة البصمة.
     * @param onError الدالة التي تستقبل رمز الخطأ ورسالته عند الإلغاء أو استنفاد المحاولات.
     * @param onFailed الدالة التي تستدعى عند عدم تطابق البصمة مع إتاحة المحاولة مجدداً.
     */
    fun authenticate(
        activity: FragmentActivity,
        title: String,
        subtitle: String,
        negativeButtonText: String,
        onSuccess: () -> Unit,
        onError: (errorCode: Int, errString: String) -> Unit = { _, _ -> },
        onFailed: () -> Unit = {}
    ) {
        // التحقق الاستباقي: إذا كان العتاد غير متوفر أو معطل نبلغ بالخطأ فوراً دون إظهار نافذة فارغة
        if (!isBiometricAvailable(activity)) {
            onError(BiometricPrompt.ERROR_HW_UNAVAILABLE, "Biometrics unavailable")
            return
        }

        try {
            // جلب منفذ المسار الرئيسي لضمان تشغيل ردود النداء على خيط الواجهة
            val executor = ContextCompat.getMainExecutor(activity)

            // بناء كائن ردود النداء لمعالجة نتائج المصادقة
            val callback = object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    onSuccess()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    onError(errorCode, errString.toString())
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    onFailed()
                }
            }

            // إعداد معلومات النافذة ونصوصها ومستويات التشفير المسموحة
            val promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle(title)
                .setSubtitle(subtitle)
                .setNegativeButtonText(negativeButtonText)
                .setAllowedAuthenticators(AUTHENTICATORS)
                .build()

            // إنشاء موجه المصادقة وإطلاقه للمستخدم
            val biometricPrompt = BiometricPrompt(activity, executor, callback)
            biometricPrompt.authenticate(promptInfo)
        } catch (t: Throwable) {
            onError(BiometricPrompt.ERROR_UNABLE_TO_PROCESS, t.message ?: "Authentication error")
        }
    }
}

