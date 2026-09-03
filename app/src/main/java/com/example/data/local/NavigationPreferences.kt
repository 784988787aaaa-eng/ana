/**
 * =====================================================================
 * ملف: تفضيلات وإعدادات التنقل الافتراضية (NavigationPreferences.kt)
 * =====================================================================
 * 
 * [الغرض العام والتعليمي من الملف]:
 * يدير هذا الملف تفضيلات شاشات البداية وترتيب ألسنة التبويب (Navigation Tabs)
 * بالاعتماد على مكتبة Jetpack DataStore الحديثة كبديل آمن وسريع لـ SharedPreferences.
 * 
 * [القرارات المعمارية وتطور الواجهة]:
 * 1. تثبيت شاشة الانطلاق (Deterministic Start Screen): تم توجيه التطبيق حتمياً ليبدأ بشاشة
 *    "ديون الحبايب" ([Screen.HABAYEB]) كونها الوجهة اليومية الأكثر استخداماً لدى أصحاب الأنشطة.
 * 2. التدفقات التفاعلية (Flows): توفير تدفقات سريعة وخفيفة [defaultStartFlow] و [tabOrderFlow]
 *    توفر استجابة فورية لموجه التنقل الرئيسي دون تأخير في وقت التشغيل.
 * 3. التوافق العكسي (Deprecation Grace Period): تم الاحتفاظ بالدوال القديمة [saveDefaultStart] و [saveTabOrder]
 *    مع وسمها بـ `@Deprecated` لتفادي كسر أي استدعاءات قديمة أثناء الترقية المعمارية.
 */
package com.example.data.local

// ---------------------------------------------------------------------
// استيراد حزم سياق أندرويد وتخزين التفضيلات DataStore وتدفقات الكوروتين
// ---------------------------------------------------------------------
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.ui.navigation.Screen
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/**
 * [تفويض إنشاء مخزن التفضيلات - navigationDataStore]:
 * يوفر كائن DataStore وحيد على مستوى سياق التطبيق باسم "navigation_prefs".
 */
private val Context.navigationDataStore: DataStore<Preferences> by preferencesDataStore(name = "navigation_prefs")

/**
 * [فئة إدارة تفضيلات التنقل - NavigationPreferences]:
 * مسؤولة عن تزويد محرك التنقل بالشاشة الافتراضية وترتيب تبويبات الشريط السفلي.
 */
class NavigationPreferences(private val context: Context) {

    /**
     * [الكائن المرافق للثوابت والمفاتيح]:
     * يحدد مفاتيح التخزين والقيم الافتراضية الثابتة.
     */
    companion object {
        /** مفتاح تخزين الشاشة الافتراضية في DataStore */
        private val KEY_DEFAULT_START = stringPreferencesKey("default_start")
        
        /** الشاشة الافتراضية الحتمية لبدء تشغيل التطبيق (حبايب) */
        val DEFAULT_START = Screen.HABAYEB.name

        /** الترتيب الافتراضي لألسنة التبويب في الشريط السفلي */
        val DEFAULT_ORDER = "${Screen.HABAYEB.name},${Screen.LEDGER.name}"
    }

    /**
     * [تدفق ترتيب التبويبات - tabOrderFlow]:
     * يزود شريط التنقل السفلي بالترتيب المعتمد للتبويبات.
     */
    val tabOrderFlow: Flow<String> = flowOf(DEFAULT_ORDER)

    /**
     * [تدفق الشاشة الافتراضية - defaultStartFlow]:
     * يزود محرك التنقل في Compose بالوجهة الابتدائية المباشرة عند بدء التطبيق.
     */
    val defaultStartFlow: Flow<String> = flowOf(DEFAULT_START)

    /**
     * [دالة حفظ شاشة البداية - saveDefaultStart]:
     * @deprecated تم تثبيت شاشة الانطلاق حتمياً لتكون شاشة الحبايب، وتم الإبقاء على الدالة لمنع أخطاء التوافق.
     */
    @Deprecated("لم تعد التفضيلات متغيرة، التطبيق يفتح حتمياً على شاشة حبايب")
    @Suppress("UNUSED_PARAMETER")
    suspend fun saveDefaultStart(start: String) {
        context.navigationDataStore.edit { preferences ->
            preferences[KEY_DEFAULT_START] = DEFAULT_START
        }
    }

    /**
     * [دالة حفظ ترتيب التبويبات - saveTabOrder]:
     * @deprecated الترتيب أصبح ثابتاً وموحداً، وتم الإبقاء على الدالة لتجنب كسر التوافقية البرمجية.
     */
    @Deprecated("لم تعد التفضيلات متغيرة، التطبيق يفتح حتمياً على شاشة حبايب")
    @Suppress("UNUSED_PARAMETER")
    suspend fun saveTabOrder(order: String) {}
}




