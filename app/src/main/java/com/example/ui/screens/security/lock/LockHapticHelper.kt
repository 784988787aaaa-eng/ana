/*
 * توثيق هندسي عربي — LockHapticHelper.kt
 *
 * المسؤولية المعمارية:
 * مساعد اهتزاز مستقل لمسار قفل التطبيق. يعزل الوصول إلى Vibrator/VibratorManager ويحوّل أحداث النجاح أو الخطأ أو الضغط إلى استجابة لمسية.
 *
 * القراءة التعليمية:
 * هذا الملف جزء من مسار الأمان في التطبيق؛ أي تفاعل مرئي هنا يجب فهمه كحالة من حالات
 * الحماية: ما يراه المستخدم على الشاشة هو انعكاس لحالة داخلية، وليس بديلاً عن قرار
 * أمني صادر من طبقات المجال والتخزين الآمن. التوثيق التالي لا يغير التنفيذ الأصلي.
 *
 * مبدأ الثبات: الشيفرة التنفيذية الأصلية محفوظة دون حذف أو استبدال أو تعديل.
 */

package com.example.ui.screens.security.lock

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

/**
 * Haptic feedback types for application lock interactions.
 */
enum class LockHapticType {
    KEYPRESS, SUCCESS, ERROR
}

/**
 * Utility helper providing tactile and haptic vibration feedback for lock screen interactions.
 */
object LockHapticHelper {

    fun getVibrator(context: Context): Vibrator? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
        } catch (_: Exception) {
            null
        }
    }

    fun performLockHaptic(vibrator: Vibrator?, type: LockHapticType) {
        val vib = vibrator ?: return
        try {
            if (!vib.hasVibrator()) return

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                when (type) {
                    LockHapticType.KEYPRESS -> {
                        vib.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK))
                    }
                    LockHapticType.SUCCESS -> {
                        vib.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
                    }
                    LockHapticType.ERROR -> {
                        vib.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 45, 60, 45), intArrayOf(0, 255, 0, 255), -1))
                    }
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                when (type) {
                    LockHapticType.KEYPRESS -> vib.vibrate(VibrationEffect.createOneShot(10, 90))
                    LockHapticType.SUCCESS -> vib.vibrate(VibrationEffect.createOneShot(35, 180))
                    LockHapticType.ERROR -> vib.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 45, 60, 45), -1))
                }
            } else {
                @Suppress("DEPRECATION")
                when (type) {
                    LockHapticType.KEYPRESS -> vib.vibrate(10)
                    LockHapticType.SUCCESS -> vib.vibrate(35)
                    LockHapticType.ERROR -> vib.vibrate(longArrayOf(0, 45, 60, 45), -1)
                }
            }
        } catch (_: Exception) {
            // Fallback safely if device lacks vibration hardware permission
        }
    }
}


/*
 * --- ملاحظات وتوصيات المعمارية البرمجية ---
 *
 * - اختبار اختلافات API الخاصة بـVibratorManager وVibrator على إصدارات أندرويد المدعومة.
 * - الإبقاء على المساعد عديم الحالة قدر الإمكان لتسهيل الاختبار والعزل.
 */
