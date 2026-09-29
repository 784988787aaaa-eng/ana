package com.smartledger.aldaftar.ui.screens.security.lock

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

enum class LockHapticType {
    KEYPRESS, SUCCESS, ERROR
}

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
                        vib.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_DOUBLE_CLICK))
                    }
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                when (type) {
                    LockHapticType.KEYPRESS -> vib.vibrate(VibrationEffect.createOneShot(10, 90))
                    LockHapticType.SUCCESS -> vib.vibrate(VibrationEffect.createOneShot(35, 180))
                    LockHapticType.ERROR -> vib.vibrate(VibrationEffect.createOneShot(25, 120))
                }
            } else {
                @Suppress("DEPRECATION")
                when (type) {
                    LockHapticType.KEYPRESS -> vib.vibrate(10)
                    LockHapticType.SUCCESS -> vib.vibrate(35)
                    LockHapticType.ERROR -> vib.vibrate(25)
                }
            }
        } catch (_: Exception) {
        }
    }
}
