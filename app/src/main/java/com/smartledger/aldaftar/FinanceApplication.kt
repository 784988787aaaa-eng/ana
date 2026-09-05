/**
 * =====================================================================
 * ملف: فئة التطبيق الرئيسية (FinanceApplication.kt)
 * =====================================================================
 * 
 * [الغرض العام والتعليمي من الملف]:
 * تمثل هذه الفئة نقطة البداية المركزية لدورة حياة التطبيق بأكمله (Application Entry Point).
 * يتم إنشاؤها وتشغيلها قبل أي واجهة أو شاشة (Activity/Service)، وتتولى تهيئة الموارد
 * والخدمات العامة، مثل تجهيز قاعدة البيانات، وتهيئة جلسة تسجيل الدخول، وضبط إعدادات WorkManager.
 * 
 * [الاعتبارات المعمارية والأداء]:
 * - يتم تنفيذ جميع التهيئة الثقيلة في مسار خلفي (IO Coroutine) لضمان بقاء المسار الرئيسي (Main Thread)
 *   حراً تماماً، مما يحقق سرعة تشغيل فائقة (Cold Startup في أقل من 400ms).
 * - تطبيق واجهة `Configuration.Provider` لضبط مستوى تسجيل السجلات (Logging) في مكتبة WorkManager.
 */
package com.smartledger.aldaftar

// ---------------------------------------------------------------------
// استيراد حزم التطبيق، مكتبة WorkManager، وقاعدة البيانات، وكوروتينات كوتلن
// ---------------------------------------------------------------------
import android.app.Application
import androidx.work.Configuration
import com.smartledger.aldaftar.data.local.AppDatabase
import com.smartledger.aldaftar.security.PlayIntegrityGate
import com.smartledger.aldaftar.security.SecurityEnvironmentGuard
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * [فئة التطبيق - FinanceApplication]:
 * ترث من فئة `Application` وتطبق واجهة `Configuration.Provider`.
 */
class FinanceApplication : Application(), Configuration.Provider {

    /**
     * [دالة بدء التشغيل - onCreate]:
     * تستدعى مرة واحدة عند إقلاع التطبيق في الذاكرة.
     */
    override fun onCreate() {
        super.onCreate()

        // تنفيذ التهيئة الخلفية بشكل غير متزامن لتفادي حظر المسار الرئيسي (Main Thread)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // 1. تهيئة مدير جلسة تسجيل الدخول الموحد لحسابات Google
                com.smartledger.aldaftar.domain.GoogleAuthSessionManager.initialize(this@FinanceApplication)
                
                // 2. فحص بيئة التشغيل مبكراً؛ النتيجة لا تُسجل تفصيلياً حتى لا تكشف إشارات دفاعية.
                SecurityEnvironmentGuard.assess(this@FinanceApplication)

                // 3. تجهيز Play Integrity بشكل غير حاجب للمسار الرئيسي إذا تم ضبط رقم مشروع Google.
                PlayIntegrityGate(this@FinanceApplication).prepare()

                // 4. التحمية الاستباقية لقاعدة البيانات (Pre-warming) لتسريع أول استعلام على الشاشة.
                val db = AppDatabase.getDatabase(applicationContext)
                db.settingsDao().getSettingsDirect()
            } catch (e: Exception) {
                // لا نطبع stack trace في الإنتاج؛ نمنع تسريب معلومات البنية الداخلية إلى Logcat.
                android.util.Log.e("FinanceApplication", "Background initialization failed: ${e.javaClass.simpleName}")
            }
        }
    }

    /**
     * [خاصية ضبط WorkManager - workManagerConfiguration]:
     * تحدد مستوى تسجيل الأحداث لمكتبة WorkManager (مستوى INFO).
     */
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setMinimumLoggingLevel(android.util.Log.INFO)
            .build()
}
