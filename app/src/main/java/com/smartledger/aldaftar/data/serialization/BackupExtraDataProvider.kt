
/**
 * مزود البيانات الإضافية للنسخ؛ يجمع التفضيلات والتصنيفات خارج الجداول الرئيسية على خيوط الإدخال والإخراج.
 * التوثيق هنا يوضح أثر الدوال على الأمان والتوافق والدقة المالية دون تغيير واجهات الاستدعاء.
 */
package com.smartledger.aldaftar.data.serialization

import android.content.Context
import android.content.SharedPreferences
import com.smartledger.aldaftar.data.local.AppDatabase
import com.smartledger.aldaftar.data.local.entities.CustomCategory
import com.smartledger.aldaftar.data.local.entities.HabayebCustomer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

data class BackupExtraData(
    val categoryLinks: Map<String, String> = emptyMap(),
    val pinnedMap: Map<String, Set<String>> = emptyMap(),
    val categoryOrderList: String? = null,
    val closedCustomName: String? = null,
    val customCategories: List<CustomCategory> = emptyList()
)

object BackupExtraDataProvider {

    
    private const val PREF_MIZAN_SEC = "mizan_sec_prefs"
    private const val PREF_MIZAN_FINANCE = "mizan_finance_prefs"

    private const val PREFIX_CAT_LINK = "CAT_LINK_"
    private const val PREFIX_KEY_PINNED_IN = "KEY_PINNED_IN_"
    private const val KEY_CATEGORY_ORDER_LIST_PREF = "CATEGORY_ORDER_LIST_KEY"
    private const val KEY_CLOSED_CUSTOM_NAME_PREF = "CLOSED_CUSTOM_NAME_KEY"

    /**
     * يجمع روابط التصنيفات من المسارين مع أولوية القيمة الحديثة.
     */
    fun getCategoryLinks(
        financePrefs: SharedPreferences?,
        sharedPrefs: SharedPreferences?,
        habayebCustomers: List<HabayebCustomer>
    ): Map<String, String> {
        val categoryLinks = mutableMapOf<String, String>()
        for (c in habayebCustomers) {
            val catLink = financePrefs?.getString("$PREFIX_CAT_LINK${c.id}", null)
                ?: sharedPrefs?.getString("$PREFIX_CAT_LINK${c.id}", null)
            if (catLink != null) {
                categoryLinks[c.id] = catLink
            }
        }
        return categoryLinks
    }

    /**
     * يجمع قوائم التثبيت ويمنع إسقاطها أثناء إنشاء النسخة الاحتياطية.
     */
    fun getPinnedCategoriesMap(
        financePrefs: SharedPreferences?,
        sharedPrefs: SharedPreferences?
    ): Map<String, Set<String>> {
        val pinnedMap = mutableMapOf<String, Set<String>>()
        val combinedPrefs = mutableMapOf<String, Any?>()
        sharedPrefs?.all?.let { combinedPrefs.putAll(it) }
        financePrefs?.all?.let { combinedPrefs.putAll(it) }

        for ((key, value) in combinedPrefs) {
            if (key.startsWith(PREFIX_KEY_PINNED_IN)) {
                val catKey = key.removePrefix(PREFIX_KEY_PINNED_IN)
                if (value is Set<*>) {
                    @Suppress("UNCHECKED_CAST")
                    pinnedMap[catKey] = value as Set<String>
                }
            }
        }
        return pinnedMap
    }

    /**
     * يجمع ترتيب التصنيفات والاسم المخصص من مسار التخزين المتاح.
     */
    fun getUserPreferences(
        financePrefs: SharedPreferences?,
        sharedPrefs: SharedPreferences?
    ): Pair<String?, String?> {
        val catOrder = financePrefs?.getString(KEY_CATEGORY_ORDER_LIST_PREF, null)
            ?: sharedPrefs?.getString(KEY_CATEGORY_ORDER_LIST_PREF, null)
        val closedCustomName = financePrefs?.getString(KEY_CLOSED_CUSTOM_NAME_PREF, null)
            ?: sharedPrefs?.getString(KEY_CLOSED_CUSTOM_NAME_PREF, null)
        return Pair(catOrder, closedCustomName)
    }

    /**
     * يقرأ التصنيفات المخصصة على خيط الإدخال والإخراج لتجنب حجب الواجهة.
     */
    suspend fun getCustomCategoriesData(context: Context): List<CustomCategory> = withContext(Dispatchers.IO) {
        try {
            val db = AppDatabase.getDatabase(context)
            db.customCategoryDao().getAllCustomCategoriesFlow().first()
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * يجمع البيانات الإضافية للنسخ على خيط الإدخال والإخراج ويعيد وعاءً موحداً.
     */
    suspend fun fetchExtraBackupData(
        context: Context,
        habayebCustomers: List<HabayebCustomer>
    ): BackupExtraData = withContext(Dispatchers.IO) {
        val sharedPrefs = context.getSharedPreferences(PREF_MIZAN_SEC, Context.MODE_PRIVATE)
        val financePrefs = context.getSharedPreferences(PREF_MIZAN_FINANCE, Context.MODE_PRIVATE)

        val categoryLinks = getCategoryLinks(financePrefs, sharedPrefs, habayebCustomers)
        val pinnedMap = getPinnedCategoriesMap(financePrefs, sharedPrefs)
        val (catOrder, closedCustomName) = getUserPreferences(financePrefs, sharedPrefs)
        val customCategories = getCustomCategoriesData(context)

        BackupExtraData(
            categoryLinks = categoryLinks,
            pinnedMap = pinnedMap,
            categoryOrderList = catOrder,
            closedCustomName = closedCustomName,
            customCategories = customCategories
        )
    }
}

