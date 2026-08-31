/**
 * التوثيق المعماري العربي — مشروع «الدفتر الذكي / ميزان الدار».
 * الملف: app/src/main/java/com/example/ui/helper/VibrationHelper.kt
 * المسؤولية: مساعد مركزي لتشغيل أنماط الاهتزاز اللمسي المستخدمة في تفاعلات الواجهة.
 *
 * القراءة التعليمية: يوضح هذا الملف كيف تنتقل حالة التطبيق من الطبقة المشتركة
 * إلى المشهد المرئي على الهاتف، مع تفسير العقود والحالة والتوابع والتفاعلات.
 * الكتلة التنفيذية الأصلية أدناه محفوظة حرفياً؛ الإضافات التوثيقية لا تعدّل
 * أي رمز تنفيذي وفق قاعدة Zero Code Alteration.
 */

// --- فهرس التوثيق التعليمي للسطر التنفيذي ---
// توثيق السطر 1: التوجيه الحزمي يحدد الموضع المنطقي للملف داخل طبقة الواجهة.
// توثيق السطر 3: الاستيراد التالي اعتماد أصلي يحتاجه الملف؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 4: الاستيراد التالي اعتماد أصلي يحتاجه الملف؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 5: الاستيراد التالي اعتماد أصلي يحتاجه الملف؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 6: الاستيراد التالي اعتماد أصلي يحتاجه الملف؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 7: الاستيراد التالي اعتماد أصلي يحتاجه الملف؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 8: الاستيراد التالي اعتماد أصلي يحتاجه الملف؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 17: التعريف التالي يحدد عقداً أو نوعاً أصلياً؛ يحتفظ بالاسم والبنية كما وردا في المصدر.
// توثيق السطر 36: التعليمة الوصفية التالية جزء من العقد الأصلي للواجهة أو التجميع، ولم يُغيّر سلوكها.
// توثيق السطر 44: الشرط التالي يحافظ على قرار التنفيذ الأصلي.
// توثيق السطر 45: الشرط التالي يحافظ على قرار التنفيذ الأصلي.
// توثيق السطر 48: التعليمة الوصفية التالية جزء من العقد الأصلي للواجهة أو التجميع، ولم يُغيّر سلوكها.
// توثيق السطر 60: الشرط التالي يحافظ على قرار التنفيذ الأصلي.
// توثيق السطر 61: الشرط التالي يحافظ على قرار التنفيذ الأصلي.
// توثيق السطر 64: التعليمة الوصفية التالية جزء من العقد الأصلي للواجهة أو التجميع، ولم يُغيّر سلوكها.

package com.example.ui.helper

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log

/**
 * مساعد ردود الفعل اللمسية والاهتزاز (Haptic & Vibration Feedback Helper)
 *
 * المسؤوليات المعمارية:
 * 1. دعم التوافقية العكسية لأنظمة Android القديمة والحديثة (Vibrator vs VibratorManager).
 * 2. عزل استدعاءات عتاد الاهتزاز عن واجهة المستخدم لضمان تجربة سلسة وتفادي انهيار التطبيق حال غياب العتاد أو رفض الإذن.
 */
object VibrationHelper {
    private const val TAG = "VibrationHelper"

    /**
     * أنماط الاهتزاز (Haptic Feedback Tokens):
     * تمثل مؤشرات تصميمية وتجربة مستخدم حسية (UI Sensory Tokens) للتأكيد والتنبيه اللمسي
     * وليست منطق أعمال أو حسابات مالية.
     */
    // نمط تأكيد النجاح والعمليات الإيجابية
    private val SUCCESS_PATTERN = longArrayOf(0, 40, 80, 80)

    // نمط التحذير وحذف العناصر
    private val DELETE_PATTERN = longArrayOf(0, 100, 60, 100)

    private fun getVibrator(context: Context): Vibrator? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

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
    
    // Custom beautiful patterns for a world-class premium experience
    fun triggerSuccessVibration(context: Context) {
        // Double tap pattern: wait 0, vibrate 40, wait 80, vibrate 80
        vibratePattern(context, SUCCESS_PATTERN)
    }

    fun triggerDeleteVibration(context: Context) {
        // Warning pattern: wait 0, vibrate 100, wait 60, vibrate 100
        vibratePattern(context, DELETE_PATTERN)
    }

    fun triggerClickVibration(context: Context) {
        // Quick subtle tick
        vibrate(context, 25)
    }
}


// --- ملاحظات وتوصيات المعمارية البرمجية ---
// 1) تم الحفاظ على الكتلة التنفيذية الأصلية دون تعديل أو حذف أو إعادة ترتيب.
// 2) يوصى مستقبلاً بإبقاء مكونات Compose المشتركة صغيرة ومحددة المسؤولية لتقليل إعادة التركيب غير الضرورية.
// 3) ينبغي إبقاء حالات الحوار والتنقل قابلة للتتبع من مصدر واحد عند اتساع عدد المسارات، مع الحفاظ على السلوك الحالي.
// 4) يوصى بعزل أدوات Intent وحفظ الملفات والاهتزاز عن واجهات Compose قدر الإمكان عبر عقود واضحة، دون تعديل النسخة الحالية.
// 5) في نظام Theme والألوان والطباعة، يوصى بالحفاظ على مصدر تصميم مركزي ومنع القيم المتناثرة مستقبلاً.
// 6) أي تحسين لاحق يجب أن يسبقه اختبار تكافؤ سلوكي/مرئي مناسب قبل الدمج.
