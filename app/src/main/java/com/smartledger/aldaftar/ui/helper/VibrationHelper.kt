package com.smartledger.aldaftar.ui.helper

import android.content.Context
import android.os.Build
import android.os.SystemClock
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log

object VibrationHelper {
    private const val TAG = "VibrationHelper"

    private const val DEBOUNCE_MS = 200L
    private var lastSuccessTime = 0L
    private var lastDeleteTime = 0L
    private var lastErrorTime = 0L
    private var lastClickTime = 0L

    private fun getVibrator(context: Context): Vibrator? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    fun vibrate(context: Context, milliseconds: Long = 40) {
        try {
            val vibrator = getVibrator(context)
            if (vibrator != null && vibrator.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(milliseconds, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(milliseconds)
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to trigger vibration: ${e.message}")
        }
    }

    fun vibratePattern(context: Context, pattern: LongArray, repeat: Int = -1) {
        try {
            val vibrator = getVibrator(context)
            if (vibrator != null && vibrator.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createWaveform(pattern, repeat))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(pattern, repeat)
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to trigger vibration pattern: ${e.message}")
        }
    }

    /**
     * Single crisp success pulse (35ms) with debounce protection.
     */
    fun triggerSuccessVibration(context: Context) {
        val now = SystemClock.uptimeMillis()
        if (now - lastSuccessTime < DEBOUNCE_MS) return
        lastSuccessTime = now
        vibrate(context, 35)
    }

    /** Dedicated manual-backup confirmation: 40ms + 30ms gap + 40ms. */
    fun triggerBackupSuccessVibration(context: Context) {
        val now = SystemClock.uptimeMillis()
        if (now - lastSuccessTime < DEBOUNCE_MS) return
        lastSuccessTime = now
        vibratePattern(context, longArrayOf(0, 40, 30, 40))
    }

    /**
     * Single distinct delete pulse (45ms) with debounce protection.
     */
    fun triggerDeleteVibration(context: Context) {
        val now = SystemClock.uptimeMillis()
        if (now - lastDeleteTime < DEBOUNCE_MS) return
        lastDeleteTime = now
        vibrate(context, 45)
    }

    /**
     * Micro pulse for subtle user interaction (15ms).
     */
    fun triggerClickVibration(context: Context) {
        val now = SystemClock.uptimeMillis()
        if (now - lastClickTime < 80L) return
        lastClickTime = now
        vibrate(context, 15)
    }

    /**
     * Error vibration feedback (double tap pattern) with debounce.
     */
    fun triggerErrorVibration(context: Context) {
        val now = SystemClock.uptimeMillis()
        if (now - lastErrorTime < DEBOUNCE_MS) return
        lastErrorTime = now
        vibratePattern(context, longArrayOf(0, 40, 60, 40))
    }

    // Semantic API aliases
    fun success(context: Context) = triggerSuccessVibration(context)
    fun delete(context: Context) = triggerDeleteVibration(context)
    fun error(context: Context) = triggerErrorVibration(context)
    fun selection(context: Context) = triggerClickVibration(context)
    fun keyPress(context: Context) = triggerClickVibration(context)
}


