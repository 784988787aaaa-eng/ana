
/**
 * مدير التفضيلات المشتركة؛ يحافظ على التوافق مع مفاتيح الإصدارات السابقة ويمنع التغيير غير المقصود في بيانات الحسابات والتصنيفات.
 * التوثيق هنا يوضح أثر الدوال على الأمان والتوافق والدقة المالية دون تغيير واجهات الاستدعاء.
 */
package com.smartledger.aldaftar.data.repository

import android.content.Context
import android.content.SharedPreferences

class PreferenceManager(private val context: Context) {

    companion object {
        
        const val PREFS_MIZAN_SEC = "mizan_sec_prefs"
        
        const val PREFS_MIZAN_FINANCE = "mizan_finance_prefs"

        const val PREF_CAT_LINK_PREFIX = "CAT_LINK_"
        
        const val PREF_KEY_PINNED_PREFIX = "KEY_PINNED_IN_"
        
        const val PREF_CATEGORY_ORDER_LIST_KEY = "CATEGORY_ORDER_LIST_KEY"
        
        const val PREF_CLOSED_CUSTOM_NAME_KEY = "CLOSED_CUSTOM_NAME_KEY"
    }

    fun getSecurityPreferences(): SharedPreferences =
        context.getSharedPreferences(PREFS_MIZAN_SEC, Context.MODE_PRIVATE)

    fun getFinancePreferences(): SharedPreferences =
        context.getSharedPreferences(PREFS_MIZAN_FINANCE, Context.MODE_PRIVATE)

    /**
     * ينفذ الكتابة إلى مخزني التفضيلات معاً عبر تطبيق غير حاجز لتقليل زمن انتظار الواجهة.
     */
    fun writeDualPreference(action: (SharedPreferences.Editor, SharedPreferences.Editor) -> Unit) {
        val sharedPrefs = getSecurityPreferences()
        val financePrefs = getFinancePreferences()
        val sharedEdit = sharedPrefs.edit()
        val financeEdit = financePrefs.edit()
        action(sharedEdit, financeEdit)
        sharedEdit.apply()
        financeEdit.apply()
    }

    /**
     * يسترجع رابط التصنيف مع الحفاظ على مسار التوافق التاريخي عند غياب القيمة الحديثة.
     */
    fun getCategoryLinkForCustomer(customerId: String): String? {
        val financePrefs = getFinancePreferences()
        val secPrefs = getSecurityPreferences()
        return financePrefs.getString("$PREF_CAT_LINK_PREFIX$customerId", null)
            ?: secPrefs.getString("$PREF_CAT_LINK_PREFIX$customerId", null)
    }

    /**
     * يحفظ رابط التصنيف في المسارين الحالي والتاريخي حتى لا تضيع البيانات عند الانتقال بين الإصدارات.
     */
    fun saveCategoryLinkForCustomer(customerId: String, categoryName: String) {
        writeDualPreference { sharedEdit, financeEdit ->
            sharedEdit.putString("$PREF_CAT_LINK_PREFIX$customerId", categoryName)
            financeEdit.putString("$PREF_CAT_LINK_PREFIX$customerId", categoryName)
        }
    }

    /**
     * يحذف رابط التصنيف من المسارين لضمان عدم بقاء نسخة قديمة متعارضة.
     */
    fun removeCategoryLinkForCustomer(customerId: String) {
        writeDualPreference { sharedEdit, financeEdit ->
            sharedEdit.remove("$PREF_CAT_LINK_PREFIX$customerId")
            financeEdit.remove("$PREF_CAT_LINK_PREFIX$customerId")
        }
    }

    /**
     * يسترجع ترتيب التصنيفات من المسار الحديث ثم المسار التاريخي عند الحاجة.
     */
    fun getCategoryOrderList(): String? {
        val financePrefs = getFinancePreferences()
        val secPrefs = getSecurityPreferences()
        return financePrefs.getString(PREF_CATEGORY_ORDER_LIST_KEY, null)
            ?: secPrefs.getString(PREF_CATEGORY_ORDER_LIST_KEY, null)
    }

    /**
     * يحفظ ترتيب التصنيفات في المسارين المتوافقين دون تغيير المفتاح التاريخي.
     */
    fun saveCategoryOrderList(orderListJson: String) {
        writeDualPreference { sharedEdit, financeEdit ->
            sharedEdit.putString(PREF_CATEGORY_ORDER_LIST_KEY, orderListJson)
            financeEdit.putString(PREF_CATEGORY_ORDER_LIST_KEY, orderListJson)
        }
    }

    /**
     * يسترجع الاسم المخصص للحسابات المقفلة مع الحفاظ على التوافق التاريخي.
     */
    fun getClosedCustomName(): String? {
        val financePrefs = getFinancePreferences()
        val secPrefs = getSecurityPreferences()
        return financePrefs.getString(PREF_CLOSED_CUSTOM_NAME_KEY, null)
            ?: secPrefs.getString(PREF_CLOSED_CUSTOM_NAME_KEY, null)
    }

    /**
     * يحفظ الاسم المخصص في المسارين المتوافقين مع واجهة التطبيق الحالية.
     */
    fun saveClosedCustomName(name: String) {
        writeDualPreference { sharedEdit, financeEdit ->
            sharedEdit.putString(PREF_CLOSED_CUSTOM_NAME_KEY, name)
            financeEdit.putString(PREF_CLOSED_CUSTOM_NAME_KEY, name)
        }
    }
}

