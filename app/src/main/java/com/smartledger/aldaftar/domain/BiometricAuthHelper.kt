/**
 * =====================================================================
 * ملف: مساعد المصادقة الحيوية وبصمة الإصبع والوجه (.)
 * =====================================================================
 * 
 * [الغرض العام والتعليمي من الملف]:
 * يوفر هذا الكائن وسيطاً آمناً ومباشراً للتعامل مع واجهة المصادقة الحيوية
 * المدمجة في نظام أندرويد [  ].
 * يتيح للتطبيق قفل وفتح الشاشات والعمليات الحساسة باستخدام بصمة الإصبع أو
 * التعرف على الوجه المعتمدة على عتاد الجهاز، مع ضمان استجابة واجهة المستخدم
 * والتأكد المسبق من توفر مستشعرات البصمة وتسجيل أصابع المستخدم قبل إطلاق نافذة التحقق.
 * 
 * [المسؤوليات المعمارية والتقنية للملف]:
 * 1. التحقق الاستباقي من توفر العتاد الحيوي ( &  ):
 *    - فحص وجود قارئ البصمة/الوجه وتسجيل بيانات حيوية صالحة عبر [].
 * 2. إطلاق نافذة المصادقة النظامية الآمنة (   ):
 *    - بناء وتخصيص نافذة التحقق القياسية التابعة للنظام دون إمكانية اعتراضها أو تزويرها.
 * 3. معالجة أحداث الاستجابة الحيوية ( ):
 *    - التعامل السلس مع حالات: النجاح []، والخطأ الحرج أو الإلغاء []،
 *      وفشل مطابقة البصمة مع المحاولة مجدداً [].
 * 4. التنفيذ الآمن على مسار الواجهة الرئيسي (  ):
 *    - ضمان عودة النتائج مباشرة على المسار الرئيسي لتحديث واجهات  فوراً.
 */
package com.smartledger.aldaftar.domain

// ---------------------------------------------------------------------
// استيراد حزم سياق أندرويد، ومدير وموجه المصادقة الحيوية ومكونات التوافق
// ---------------------------------------------------------------------
import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

/**
 * [الكائن الأحادي لمساعد المصادقة الحيوية - ]:
 * يجمع دوال التحقق من توفر البصمة وإطلاق نافذة المصادقة.
 */
object BiometricAuthHelper {

    /**
     * تحديد مستويات المصادقة الحيوية المقبولة:
     * يشمل البصمات القوية عتادياً [_] والبصمات الموثوقة القياسية [_].
     */
    private const val AUTHENTICATORS = BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.BIOMETRIC_WEAK

    /**
     * [التحقق من جاهزية وتوفر المصادقة الحيوية - ]:
     * يفحص ما إذا كان الجهاز يحتوي على مستشعر حيوي نشط، وأن المستخدم قام بتسجيل بصمته بالفعل.
     *
     * @  سياق التطبيق للوصول إلى مدير القياسات الحيوية.
     * @  إذا كانت البصمة جاهزة للاستخدام الفوري، وإلا .
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
     * [إطلاق نافذة المصادقة الحيوية - ]:
     * يفتح حوار المصادقة الرسمي التابع لنظام أندرويد مع ربط المستمعين للنتائج.
     *
     * @  النشاط الحاضن [] المطلوب لربط حوار .
     * @  عنوان نافذة التحقق (مثال: "تأكيد الهوية").
     * @  العنوان الفرعي التوضيحي (مثال: "المس مستشعر البصمة للمتابعة").
     * @  نص زر الإلغاء أو استخدام رمز المرور.
     * @  الدالة التي يتم استدعاؤها عند نجاح مطابقة البصمة.
     * @  الدالة التي تستقبل رمز الخطأ ورسالته عند الإلغاء أو استنفاد المحاولات.
     * @  الدالة التي تستدعى عند عدم تطابق البصمة مع إتاحة المحاولة مجدداً.
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
        // التحقق الاستباقي من جاهزية المصادقة قبل إنشاء النافذة لتجنب مسار واجهة غير صالح.
        if (!isBiometricAvailable(activity)) {
            onError(BiometricPrompt.ERROR_HW_UNAVAILABLE, "المصادقة الحيوية غير متاحة")
            return
        }

        try {
            // الحصول على منفذ المسار الرئيسي لضمان تحديث الواجهة من ردود النداء في الخيط الصحيح.
            val executor = ContextCompat.getMainExecutor(activity)

            // بناء كائن ردود النداء لمعالجة النجاح والخطأ والفشل دون الاحتفاظ ببيانات حساسة.
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

            // إعداد النافذة الرسمية بالنصوص المعروضة ومستويات المصادقة المسموحة.
            val promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle(title)
                .setSubtitle(subtitle)
                .setNegativeButtonText(negativeButtonText)
                .setAllowedAuthenticators(AUTHENTICATORS)
                .build()

            // إنشاء موجه المصادقة الرسمي وإطلاقه من النشاط الحاضن.
            val biometricPrompt = BiometricPrompt(activity, executor, callback)
            biometricPrompt.authenticate(promptInfo)
        } catch (t: Throwable) {
            onError(BiometricPrompt.ERROR_UNABLE_TO_PROCESS, "تعذر معالجة المصادقة الحيوية")
        }
    }
}

