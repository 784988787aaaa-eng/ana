/**
 * =====================================================================
 * ملف: مدير جلسة مصادقة حساب جوجل الموحد (GoogleAuthSessionManager.kt)
 * =====================================================================
 * 
 * [الغرض العام والتعليمي من الملف]:
 * يمثل هذا الكائن المصدر المركزي الوحيد للحقيقة (Single Source of Truth) لحالة
 * جلسة حساب Google للمستخدم في جميع أنحاء التطبيق.
 * يضمن مزامنة لحظية وفورية لحالة تسجيل الدخول أو الخروج بين مختلف الشاشات
 * (شاشة التفعيل والترخيص، شاشة إعدادات النسخ الاحتياطي السحابي، وشاشة إدارة الحساب)
 * عبر تدفقات الحالة التفاعلية [StateFlow].
 * 
 * [المسؤوليات المعمارية والتقنية للملف]:
 * 1. إدارة تدفق حالة البريد الإلكتروني المتفاعل (Reactive Session StateFlow):
 *    - بث البريد الإلكتروني المسجل لحظياً لكافة واجهات المستخدم ومراقبي الحالة.
 * 2. التهيئة التلقائية من مستودع المصادقة (Auth Persistence Sync):
 *    - استرجاع البريد الإلكتروني المحفوظ مسبقاً من [GoogleDriveAuthManager] عند إقلاع التطبيق.
 * 3. التحديث الموحد للجلسة (Unified Session Mutation):
 *    - تنظيف وتوحيد البريد عند تسجيل الدخول، ومسح الجلسة فوراً عند تسجيل الخروج.
 */
package com.example.domain

// ---------------------------------------------------------------------
// استيراد حزم سياق أندرويد، والسجلات، ومدير جوجل درايف، وتدفقات Coroutines
// ---------------------------------------------------------------------
import android.content.Context
import android.util.Log
import com.example.data.GoogleDriveAuthManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * [الكائن الأحادي لمدير جلسة حساب جوجل - GoogleAuthSessionManager]:
 * يوفر حالة تفاعلية عامة تعكس البريد الإلكتروني النشط لحساب Google.
 */
object GoogleAuthSessionManager {

    /** وسم السجلات التشخيصية لجلسة المصادقة */
    private const val TAG = "GoogleAuthSession"

    /** تدفق الحالة الداخلي القابل للتعديل للبريد الإلكتروني الحالي */
    private val _currentEmail = MutableStateFlow<String?>(null)

    /**
     * تدفق الحالة العام المتاح للقراءة فقط (Read-Only StateFlow):
     * تراقبه واجهات Jetpack Compose لإعادة رسم المكونات فور تغير حساب المستخدم.
     */
    val currentEmail: StateFlow<String?> = _currentEmail.asStateFlow()

    /**
     * [تهيئة جلسة المصادقة عند بدء التطبيق - initialize]:
     * يسترجع البريد المحفوظ في تفضيلات أمان جوجل درايف ويحدث الحالة الموحدة.
     *
     * @param context سياق التطبيق للوصول للتخزين المحلي.
     */
    fun initialize(context: Context) {
        try {
            val authManager = GoogleDriveAuthManager(context)
            val email = authManager.getStoredEmail()
            _currentEmail.value = email.takeIf { !it.isNullOrBlank() }
            Log.d(TAG, "Initialized unified Google Auth Session with email: ${_currentEmail.value}")
        } catch (t: Throwable) {
            Log.e(TAG, "Error initializing GoogleAuthSessionManager", t)
        }
    }

    /**
     * [تحديث البريد الإلكتروني للجلسة - updateEmail]:
     * يقوم بتنظيف وتوحيد حالة الأحرف للبريد وتحديث التدفق التفاعلي.
     *
     * @param email البريد الإلكتروني الجديد لحساب Google أو null.
     */
    fun updateEmail(email: String?) {
        val cleanEmail = email?.trim()?.lowercase()
        _currentEmail.value = cleanEmail.takeIf { !it.isNullOrBlank() }
        Log.d(TAG, "Unified Google Auth Session email updated to: ${_currentEmail.value}")
    }

    /**
     * [مسح جلسة المصادقة بالكامل - clearSession]:
     * يعيد البريد الإلكتروني إلى null عند تسجيل الخروج لإشعار كافة شاشات التطبيق فوراً.
     */
    fun clearSession() {
        _currentEmail.value = null
        Log.d(TAG, "Unified Google Auth Session cleared.")
    }
}

// --- ملاحظات وتوصيات المعمارية البرمجية ---
// هذا القسم توثيقي فقط؛ لا يغيّر أي تعليمة تنفيذية في الملف الأصلي.
// - حصر رموز OAuth في الذاكرة أو التخزين الآمن وعدم تسجيلها في Logcat.
// - اختبار انتهاء الجلسة، وإلغاء الاعتماد، وتبديل الحساب، وإعادة المصادقة.
// - أي تنفيذ فعلي لهذه التوصيات يُرحّل إلى مهمة هندسية مستقلة ولا يُجرى داخل هذا الملف أثناء التوثيق.
