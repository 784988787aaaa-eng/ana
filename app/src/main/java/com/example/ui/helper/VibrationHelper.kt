package com.example.ui.helper

/*
 * =====================================================================================
 * حزمة التغذية الراجعة اللمسية والاهتزاز (Haptic & Vibration Feedback Package)
 * -------------------------------------------------------------------------------------
 * تحتوي هذه الفئة على دوال تشغيل أنماط الاهتزاز التفاعلية والتنبيهات الحسية (Haptic Cues)
 * لتعزيز تجربة المستخدم عند إجراء العمليات المالية (إضافة، تعديل، حذف، نجاح).
 * =====================================================================================
 */

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log

/*
 * =====================================================================================
 * كائن مساعد التغذية اللمسية والاهتزاز (VibrationHelper)
 * -------------------------------------------------------------------------------------
 * [المسؤوليات والأهداف المعمارية]:
 * 1. التوافق العكسي مع أنظمة أندرويد (Compatibility):
 *    التعامل التلقائي مع واجهة `VibratorManager` الحديثة على Android 12+ (API 31+)،
 *    وخدمة `VIBRATOR_SERVICE` الكلاسيكية على الإصدارات الأقدم.
 * 2. الأمان والاستقرار:
 *    عزل استدعاءات عتاد الاهتزاز داخل كتل `try-catch` لضمان عدم انهيار التطبيق حال تعطل العتاد
 *    أو غياب محرك الاهتزاز في الجهاز.
 * 3. أنماط تفاعلية موحدة (Sensory UI Tokens):
 *    توفير نبضات اهتزازية متدرجة تمنح المستخدم تأكيداً حسياً ملموساً عند الحفظ أو الحذف أو النقر.
 * =====================================================================================
 */
object VibrationHelper {
    // وسم السجلات لتتبع الأخطاء البرمجية
    private const val TAG = "VibrationHelper"

    /*
     * ---------------------------------------------------------------------------------
     * أنماط الاهتزاز التصميمية (Sensory Tokens Patterns)
     * ---------------------------------------------------------------------------------
     */
    // نمط تأكيد النجاح والعمليات الإيجابية (نبضة مزدوجة ناعمة: انتظر 0، اهتز 40، انتظر 80، اهتز 80 مللي ثانية)
    private val SUCCESS_PATTERN = longArrayOf(0, 40, 80, 80)

    // نمط التحذير وحذف السجلات (نبضتان قويتان للتحذير: انتظر 0، اهتز 100، انتظر 60، اهتز 100 مللي ثانية)
    private val DELETE_PATTERN = longArrayOf(0, 100, 60, 100)

    /*
     * ---------------------------------------------------------------------------------
     * دالة جلب خدمة الاهتزاز من النظام وفق إصدار أندرويد (getVibrator)
     * ---------------------------------------------------------------------------------
     */
    private fun getVibrator(context: Context): Vibrator? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    /*
     * ---------------------------------------------------------------------------------
     * دالة تشغيل نبضة اهتزاز أحادية (vibrate)
     * ---------------------------------------------------------------------------------
     * تستخدم VibrationEffect.createOneShot على Android 8+ (Oreo) أو الدالة التقليدية للإصدارات الأقدم.
     * ---------------------------------------------------------------------------------
     */
    fun vibrate(context: Context, milliseconds: Long = 100) {
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

    /*
     * ---------------------------------------------------------------------------------
     * دالة تشغيل نمط اهتزاز مخصص (vibratePattern)
     * ---------------------------------------------------------------------------------
     * تستخدم موجة اهتزازية متتابعة (Waveform) لتوليد إيقاع حسي مخصص.
     * ---------------------------------------------------------------------------------
     */
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
    
    /*
     * ---------------------------------------------------------------------------------
     * دوال الاستدعاء السريع للأنماط المعتمدة
     * ---------------------------------------------------------------------------------
     */
    // تفعيل اهتزاز النجاح (عند حفظ معاملة أو ترحيل قيد)
    fun triggerSuccessVibration(context: Context) {
        vibratePattern(context, SUCCESS_PATTERN)
    }

    // تفعيل اهتزاز الحذف (عند حذف حساب أو قيد مالي)
    fun triggerDeleteVibration(context: Context) {
        vibratePattern(context, DELETE_PATTERN)
    }

    // تفعيل اهتزاز النقر الخفيف (تكة لمسية سريعة 25 مللي ثانية)
    fun triggerClickVibration(context: Context) {
        vibrate(context, 25)
    }
}


