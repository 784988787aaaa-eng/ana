package com.smartledger.aldaftar.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.smartledger.aldaftar.ui.navigation.Screen
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/** مخزن التفضيلات المحلي المخصص لمسار التنقل، معزول عن بيانات الحسابات المالية. */
private val Context.navigationDataStore: DataStore<Preferences> by preferencesDataStore(name = "navigation_prefs")

/** مدير تفضيلات التنقل مع الحفاظ على القيم الافتراضية والتوافق مع الاستدعاءات السابقة. */
class NavigationPreferences(private val context: Context) {

    companion object {
        
        /** مفتاح داخلي محفوظ للتوافق مع الإصدارات السابقة دون تغيير اسم التخزين. */
        private val KEY_DEFAULT_START = stringPreferencesKey("default_start")

        val DEFAULT_START = Screen.HABAYEB.name

        val DEFAULT_ORDER = "${Screen.HABAYEB.name},${Screen.LEDGER.name}"
    }

    val tabOrderFlow: Flow<String> = flowOf(DEFAULT_ORDER)

    val defaultStartFlow: Flow<String> = flowOf(DEFAULT_START)

    @Deprecated("لم تعد التفضيلات متغيرة، التطبيق يفتح حتمياً على شاشة حبايب")
    @Suppress("UNUSED_PARAMETER")
    suspend fun saveDefaultStart(start: String) {
        context.navigationDataStore.edit { preferences ->
            preferences[KEY_DEFAULT_START] = DEFAULT_START
        }
    }

    @Deprecated("لم تعد التفضيلات متغيرة، التطبيق يفتح حتمياً على شاشة حبايب")
    @Suppress("UNUSED_PARAMETER")
    suspend fun saveTabOrder(order: String) {}
}
