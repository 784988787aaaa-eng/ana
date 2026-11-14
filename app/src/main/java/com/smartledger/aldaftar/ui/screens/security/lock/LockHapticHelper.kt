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

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                @Suppress("DEPRECATION")
                val audioAttrs = android.media.AudioAttributes.Builder()
                    .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(android.media.AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                    .build()

                // Use crisp, universally supported one-shot amplitude pulses so ERM and LRA motors
                // both produce unmistakable tactile feedback when pressing numbers, with predefined fallback.
                val effect = when (type) {
                    LockHapticType.KEYPRESS -> {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && false) {
                            VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK)
                        } else {
                            VibrationEffect.createOneShot(18L, VibrationEffect.DEFAULT_AMPLITUDE)
                        }
                    }
                    LockHapticType.SUCCESS -> {
                        VibrationEffect.createOneShot(35L, VibrationEffect.DEFAULT_AMPLITUDE)
                    }
                    LockHapticType.ERROR -> {
                        VibrationEffect.createWaveform(longArrayOf(0L, 35L, 55L, 35L), -1)
                    }
                }
                @Suppress("DEPRECATION")
                vib.vibrate(effect, audioAttrs)
            } else {
                @Suppress("DEPRECATION")
                when (type) {
                    LockHapticType.KEYPRESS -> vib.vibrate(18L)
                    LockHapticType.SUCCESS -> vib.vibrate(35L)
                    LockHapticType.ERROR -> vib.vibrate(longArrayOf(0L, 35L, 55L, 35L), -1)
                }
            }
        } catch (_: Exception) {
        }
    }
}
