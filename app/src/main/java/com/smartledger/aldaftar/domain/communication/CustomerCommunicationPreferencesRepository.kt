package com.smartledger.aldaftar.domain.communication

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class CustomerCommunicationConfig(
    val autoWhatsApp: Boolean = false,
    val autoSms: Boolean = false
) {
    val hasAnyEnabled: Boolean get() = autoWhatsApp || autoSms
}

/**
 * مستودع مستقل لحفظ إعدادات التواصل الفوري لكل حساب (Per-Account Communication Preferences).
 * يدعم الاستمرار الدائم عبر إغلاق التطبيق وإعادة التشغيل، ويوفر تدفقاً تفاعلياً لمراقبة الحالة.
 */
class CustomerCommunicationPreferencesRepository(context: Context) {

    private val prefs: SharedPreferences = context.applicationContext
        .getSharedPreferences("customer_comm_prefs", Context.MODE_PRIVATE)

    private val _configsState = MutableStateFlow<Map<String, CustomerCommunicationConfig>>(loadAllConfigs())
    val configsState: StateFlow<Map<String, CustomerCommunicationConfig>> = _configsState.asStateFlow()

    private fun loadAllConfigs(): Map<String, CustomerCommunicationConfig> {
        val map = mutableMapOf<String, CustomerCommunicationConfig>()
        val all = prefs.all
        for ((key, value) in all) {
            if (key.endsWith("_wa") && value is Boolean) {
                val customerId = key.removeSuffix("_wa")
                val sms = prefs.getBoolean("${customerId}_sms", false)
                map[customerId] = CustomerCommunicationConfig(autoWhatsApp = value, autoSms = sms)
            } else if (key.endsWith("_sms") && value is Boolean) {
                val customerId = key.removeSuffix("_sms")
                if (!map.containsKey(customerId)) {
                    val wa = prefs.getBoolean("${customerId}_wa", false)
                    map[customerId] = CustomerCommunicationConfig(autoWhatsApp = wa, autoSms = value)
                }
            }
        }
        return map
    }

    fun getPreferences(customerId: String): CustomerCommunicationConfig {
        return _configsState.value[customerId] ?: run {
            val wa = prefs.getBoolean("${customerId}_wa", false)
            val sms = prefs.getBoolean("${customerId}_sms", false)
            CustomerCommunicationConfig(autoWhatsApp = wa, autoSms = sms)
        }
    }

    fun setPreferences(customerId: String, autoWhatsApp: Boolean, autoSms: Boolean) {
        prefs.edit()
            .putBoolean("${customerId}_wa", autoWhatsApp)
            .putBoolean("${customerId}_sms", autoSms)
            .apply()
        val current = _configsState.value.toMutableMap()
        current[customerId] = CustomerCommunicationConfig(autoWhatsApp = autoWhatsApp, autoSms = autoSms)
        _configsState.value = current
    }

    fun setWhatsAppEnabled(customerId: String, enabled: Boolean) {
        val current = getPreferences(customerId)
        setPreferences(customerId, autoWhatsApp = enabled, autoSms = current.autoSms)
    }

    fun setSmsEnabled(customerId: String, enabled: Boolean) {
        val current = getPreferences(customerId)
        setPreferences(customerId, autoWhatsApp = current.autoWhatsApp, autoSms = enabled)
    }

    fun clearCustomer(customerId: String) {
        prefs.edit()
            .remove("${customerId}_wa")
            .remove("${customerId}_sms")
            .apply()
        val current = _configsState.value.toMutableMap()
        current.remove(customerId)
        _configsState.value = current
    }
}
