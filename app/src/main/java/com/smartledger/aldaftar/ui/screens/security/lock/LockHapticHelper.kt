package com.smartledger.aldaftar.ui.screens.security.lock

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

/**
 * يحدد أنواع التنبيه اللمسي المستخدمة في أحداث شاشة القفل.
 * الفصل بين الأنواع يجعل الإحساس اللمسي متسقاً مع نتيجة العملية الأمنية.
 */
enum class LockHapticType {
    KEYPRESS, SUCCESS, ERROR
}

/**
 * يوفر اهتزازاً لمسياً قصيراً لعمليات شاشة القفل مع توافق إصدارات النظام المختلفة.
 * عند غياب العتاد أو تعذر الخدمة تستمر وظيفة القفل دون الاعتماد على الاهتزاز.
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
            // عند تعذر خدمة الاهتزاز أو غياب العتاد يستمر القفل دون أثر على الوظيفة الأمنية
        }
    }
}
