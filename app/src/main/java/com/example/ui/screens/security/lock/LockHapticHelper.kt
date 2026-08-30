/**
 * =====================================================================
 * ملف: LockHapticHelper.kt
 * الحزمة: com.example.ui.screens.security.lock
 * 
 * [الوصف والمسؤولية المعمارية]:
 * يمثل هذا الكائن البرمجي المساعد (Utility Object) المسؤول عن تقديم التغذية
 * الراجعة اللمسية والاهتزازية (Haptic Feedback) أثناء تفاعل المستخدم مع شاشة القفل.
 * يعزز هذا المكون تجربة المستخدم (UX) عبر تقديم إشارات حسية دقيقة عند الضغط على
 * أرقام لوحة المفاتيح، أو عند نجاح فتح القفل، أو عند حدوث خطأ في الرمز السري.
 * 
 * [التوافق مع إصدارات أندرويد (OS Compatibility)]:
 * - يدعم الأجهزة الحديثة التي تعمل بنظام Android 12 (API 31+) عبر `VibratorManager`.
 * - يدعم التأثيرات الجاهزة المحددة مسبقاً (Predefined Effects) لنظام Android 10 (API 29+).
 * - يدعم الموجات المخصصة `VibrationEffect.createWaveform` لنظام Android 8 (API 26+).
 * - يوفر تراجعاً آمناً (Graceful Fallback) للإصدارات الأقدم دون التسبب في أي انهيار.
 * =====================================================================
 */
package com.example.ui.screens.security.lock

// ---------------------------------------------------------------------
// استيراد أدوات النظام وإدارة الاهتزاز في منصة أندرويد
// ---------------------------------------------------------------------
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

/**
 * أنواع التغذية اللمسية والاهتزازية لشاشة القفل:
 * - [KEYPRESS]: نقرة اهتزازية خفيفة جداً ولطيفة عند لمس أي رقم في لوحة المفاتيح.
 * - [SUCCESS]: نبضة اهتزازية واضحة ومطمئنة عند التحقق الناجح من الرمز وفتح التطبيق.
 * - [ERROR]: اهتزاز مزدوج تحذيري متزامن مع حركة الاهتزاز البصرية عند إدخال رمز خاطئ.
 */
enum class LockHapticType {
    KEYPRESS, 
    SUCCESS, 
    ERROR
}

/**
 * =====================================================================
 * [المساعد اللمسي لشاشة القفل - LockHapticHelper]:
 * 
 * كائن أحادي (Singleton Object) يوفر وظائف تشغيل الاهتزازات المتوافقة برمجياً.
 * =====================================================================
 */
object LockHapticHelper {

    /**
     * الحصول الآمن على كائن الاهتزاز [Vibrator] المناسب لإصدار نظام التشغيل الحالي.
     * 
     * @param context سياق التطبيق للوصول إلى خدمات النظام.
     * @return كائن [Vibrator] الفعال، أو null في حال تعذر الوصول إليه.
     */
    fun getVibrator(context: Context): Vibrator? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                // في نظام Android 12 (API 31+) يتم جلب الهزاز الافتراضي عبر مدير الاهتزاز
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator
            } else {
                // في الإصدارات الأقدم من API 31 يتم جلب خدمة الهزاز المباشرة
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
        } catch (_: Exception) {
            null
        }
    }

    /**
     * تنفيذ نمط الاهتزاز المناسب لنوع الحدث مع مراعاة قدرات العتاد وإصدار أندرويد.
     * 
     * @param vibrator كائن الهزاز المكتشف.
     * @param type نوع التغذية الراجعة المطلوب تنفيذها (ضغط زر، نجاح، أو خطأ).
     */
    fun performLockHaptic(vibrator: Vibrator?, type: LockHapticType) {
        val vib = vibrator ?: return
        try {
            // التحقق من وجود عتاد اهتزاز في الجهاز قبل بدء التشغيل
            if (!vib.hasVibrator()) return

            // ---------------------------------------------------------
            // أجهزة Android 10 (API 29) فما فوق: استخدام التأثيرات المعيارية المدمجة
            // ---------------------------------------------------------
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                when (type) {
                    LockHapticType.KEYPRESS -> {
                        vib.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK))
                    }
                    LockHapticType.SUCCESS -> {
                        vib.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
                    }
                    LockHapticType.ERROR -> {
                        // نمط اهتزاز مزدوج: (صمت 0ms، اهتزاز 45ms، صمت 60ms، اهتزاز 45ms)
                        vib.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 45, 60, 45), intArrayOf(0, 255, 0, 255), -1))
                    }
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // -----------------------------------------------------
                // أجهزة Android 8.0 (API 26) إلى Android 9.0 (API 28)
                // -----------------------------------------------------
                when (type) {
                    LockHapticType.KEYPRESS -> vib.vibrate(VibrationEffect.createOneShot(10, 90))
                    LockHapticType.SUCCESS -> vib.vibrate(VibrationEffect.createOneShot(35, 180))
                    LockHapticType.ERROR -> vib.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 45, 60, 45), -1))
                }
            } else {
                // -----------------------------------------------------
                // الإصدارات الكلاسيكية السابقة لنظام أندرويد 8.0
                // -----------------------------------------------------
                @Suppress("DEPRECATION")
                when (type) {
                    LockHapticType.KEYPRESS -> vib.vibrate(10)
                    LockHapticType.SUCCESS -> vib.vibrate(35)
                    LockHapticType.ERROR -> vib.vibrate(longArrayOf(0, 45, 60, 45), -1)
                }
            }
        } catch (_: Exception) {
            // معالجة صامتة وآمنة في حال عدم توفر الصلاحية أو غياب الدعم العتادي
        }
    }
}

