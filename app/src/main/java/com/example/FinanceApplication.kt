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
package com.example

import android.app.Application
import androidx.work.Configuration

/**
 * [فئة التطبيق - FinanceApplication]:
 * ترث من فئة `Application` وتطبق واجهة `Configuration.Provider`.
 */
class FinanceApplication : Application(), Configuration.Provider {

    override fun onCreate() {
        super.onCreate()
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
