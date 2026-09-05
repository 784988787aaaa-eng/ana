/**
 * =====================================================================
 * ملف: مدير تصنيفات وتثبيتات عملاء الحبايب (HabayebCategoryManager.kt)
 * =====================================================================
 * 
 * [الغرض العام والتعليمي من الملف]:
 * يمثل هذا الكائن حالة الاستخدام (Use Case / Domain Manager) المركزية المسؤولة عن
 * تنظيم وتبويب عملاء قسم "الحبايب"، وتعيين وتثبيت الحسابات المميزة في أعلى كل تصنيف،
 * وإدارة الترتيب الأفقي للأقسام بمراعاة اتجاه واجهات المستخدم العربية (RTL).
 * 
 * [المسؤوليات المعمارية والتقنية للملف]:
 * 1. التخزين المؤقت عالي السرعة (High-Performance In-Memory Caching):
 *    - استخدام [ConcurrentHashMap] لتخزين روابط العملاء بالتصنيفات والتثبيتات محلياً في الذاكرة
 *      لتجنب القراءة المستمرة والبطيئة من [SharedPreferences] أثناء التمرير في القوائم.
 * 2. التحكم في تثبيت الحسابات (Pinning Management & Limits):
 *    - تثبيت حتى 3 عملاء كحد أقصى لكل تصنيف، مع إشعار المستخدم عبر التوست والاهتزاز اللمسي.
 * 3. العمليات الشاملة للتصنيفات (Category Lifecycle & Cascade Operations):
 *    - إنشاء، وإعادة تسمية، وحذف التصنيفات إما بنقل حساباتها أو بنقلها مع المعاملات لسلة المحذوفات.
 * 4. إدارة الترتيب الأفقي للواجهة العربية (RTL Layout Reordering):
 *    - عكس اتجاهات التحريك (اليمين واليسار) لتتوافق طبيعياً مع اللغة العربية.
 */
package com.smartledger.aldaftar.domain.usecase.habayeb

// ---------------------------------------------------------------------
// استيراد حزم أندرويد الأساسية، ومستودع البيانات، والمكتبات المساعدة
// ---------------------------------------------------------------------
import android.app.Application
import android.content.SharedPreferences
import android.util.Log
import android.widget.Toast
import com.smartledger.aldaftar.R
import com.smartledger.aldaftar.data.local.entities.CustomCategory
import com.smartledger.aldaftar.data.repository.FinanceRepository
import com.smartledger.aldaftar.ui.helper.VibrationHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap

/**
 * [فئة مدير تصنيفات وتثبيتات الحبايب - HabayebCategoryManager]:
 * @property application سياق التطبيق العام لجلب الموارد النصية وإظهار الرسائل.
 * @property repository مستودع البيانات المالية لتنفيذ عمليات قاعدة البيانات.
 * @property sharedPrefs تفضيلات التطبيق لتخزين الروابط السريعة والتثبيتات.
 */
class HabayebCategoryManager(
    private val application: Application,
    private val repository: FinanceRepository,
    private val sharedPrefs: SharedPreferences
) {
    /** ذاكرة تخزين مؤقتة آمنة للخيوط لربط معرف العميل باسم التصنيف */
    private val categoryMapCache = ConcurrentHashMap<String, String>()
    /** ذاكرة تخزين مؤقتة آمنة للخيوط لمجموعات معرفات العملاء المثبتين في كل تصنيف */
    private val pinnedMapCache = ConcurrentHashMap<String, Set<String>>()

    init {
        // =========================================================================
        // التحميل المسبق لكافة الروابط والتثبيتات في ذاكرة الوصول السريع عند التهيئة
        // =========================================================================
        try {
            sharedPrefs.all.forEach { (key, value) ->
                if (key.startsWith(PREFIX_CAT_LINK) && value is String) {
                    categoryMapCache[key.removePrefix(PREFIX_CAT_LINK)] = value
                } else if (key.startsWith(PREFIX_KEY_PINNED_IN) && value is Set<*>) {
                    @Suppress("UNCHECKED_CAST")
                    val set = (value as? Set<String>) ?: emptySet()
                    pinnedMapCache[key.removePrefix(PREFIX_KEY_PINNED_IN)] = set
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing category and pinned cache from SharedPreferences", e)
        }
    }

    /** تدفق حالة لمجموعة معرفات العملاء المثبتين في التصنيف الحالي */
    private val _pinnedCustomerIds = MutableStateFlow<Set<String>>(getPinnedForCategory(null))
    val pinnedCustomerIds = _pinnedCustomerIds.asStateFlow()

    /** عداد إشعار لتحديث واجهة المستخدم وإعادة قراءة الحسابات */
    private val _categoryUpdateTrigger = MutableStateFlow(0)
    val categoryUpdateTrigger = _categoryUpdateTrigger.asStateFlow()

    /**
     * [إطلاق إشعار التحديث - triggerUpdate]:
     * يزيد قيمة العداد لإعلام الشاشات بضرورة إعادة رسم وتحديث البيانات.
     */
    fun triggerUpdate() {
        _categoryUpdateTrigger.value++
    }

    /**
     * [استرجاع خارطة التصنيفات الكاملة - getCategoryMap]:
     * تعيد الخارطة المباشرة لروابط كافة العملاء بالتصنيفات.
     */
    fun getCategoryMap(): Map<String, String> = categoryMapCache

    /**
     * [جلب معرفات العملاء المثبتين في تصنيف معين - getPinnedForCategory]:
     * يقرأ من الذاكرة المؤقتة أو من الإعدادات عند عدم التوفر.
     */
    fun getPinnedForCategory(category: String?): Set<String> {
        val catKey = category ?: KEY_GLOBAL_ALL
        return pinnedMapCache[catKey] ?: run {
            val fromPrefs = sharedPrefs.getStringSet("$PREFIX_KEY_PINNED_IN$catKey", emptySet())?.toSet() ?: emptySet()
            pinnedMapCache[catKey] = fromPrefs
            fromPrefs
        }
    }

    /**
     * [التأكد من وجود التصنيف الافتراضي للحسابات المقفلة - ensureClosedCategoryExists]:
     * يفحص وجود تصنيف الحسابات المسددة/المقفلة وينشئه تلقائياً إذا لم يكن موجوداً.
     */
    suspend fun ensureClosedCategoryExists() = withContext(Dispatchers.IO) {
        val categories = repository.customCategoriesFlow.first()
        val hasClosed = categories.any { it.isSystemClosed }
        if (!hasClosed) {
            val defaultClosedName = application.getString(R.string.category_system_closed)
            val currentClosedName = sharedPrefs.getString(KEY_CLOSED_CUSTOM_NAME, defaultClosedName) ?: defaultClosedName
            repository.saveCustomCategory(
                CustomCategory(
                    name = currentClosedName,
                    tabType = TAB_TYPE_HABAYEB,
                    iconEmoji = DEFAULT_EMOJI,
                    displayOrder = 0,
                    isSystemClosed = true
                )
            )
        }
    }

    /**
     * [تحميل المثبتين لتصنيف معين في تدفق الحالة - loadPinnedForCategory]:
     */
    fun loadPinnedForCategory(category: String?) {
        val pinnedSet = getPinnedForCategory(category)
        _pinnedCustomerIds.value = pinnedSet
    }

    /**
     * [تبديل حالة تثبيت العميل - togglePinCustomer]:
     * يضيف العميل إلى قائمة المثبتين أو يزيله منها مع فحص الحد الأقصى (3 عملاء).
     *
     * @param customerId معرف العميل.
     * @param selectedCategory التصنيف النشط حالياً.
     * @return true إذا تمت العملية بنجاح، false إذا تجاوز الحد الأقصى.
     */
    suspend fun togglePinCustomer(customerId: String, selectedCategory: String?): Boolean = withContext(Dispatchers.IO) {
        val catKey = selectedCategory ?: KEY_GLOBAL_ALL
        val activePinnedSet = (pinnedMapCache[catKey] ?: getPinnedForCategory(selectedCategory)).toMutableSet()
        if (activePinnedSet.contains(customerId)) {
            activePinnedSet.remove(customerId)
        } else {
            if (activePinnedSet.size >= MAX_PINNED_COUNT) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        application,
                        application.getString(R.string.habayeb_pin_limit_toast),
                        Toast.LENGTH_SHORT
                    ).show()
                }
                return@withContext false
            }
            activePinnedSet.add(customerId)
        }
        val immutableSet = activePinnedSet.toSet()
        pinnedMapCache[catKey] = immutableSet
        sharedPrefs.edit().putStringSet("$PREFIX_KEY_PINNED_IN$catKey", activePinnedSet).apply()
        _pinnedCustomerIds.value = immutableSet
        triggerUpdate()
        true
    }

    /**
     * [تعيين أو إلغاء تصنيف لمجموعة عملاء - assignCategoryToCustomers]:
     * يربط قائمة من معرفات العملاء بتصنيف محدد، أو يزيل التصنيف إذا كان null.
     */
    suspend fun assignCategoryToCustomers(customerIds: List<String>, category: String?) = withContext(Dispatchers.IO) {
        val editor = sharedPrefs.edit()
        customerIds.forEach { id ->
            if (category == null) {
                categoryMapCache.remove(id)
                editor.remove("$PREFIX_CAT_LINK$id")
            } else {
                categoryMapCache[id] = category
                editor.putString("$PREFIX_CAT_LINK$id", category)
            }
        }
        editor.apply()
        triggerUpdate()
    }

    /**
     * [جلب تصنيف عميل معين - getCustomerCategory]:
     */
    fun getCustomerCategory(customerId: String): String? = categoryMapCache[customerId]

    /**
     * [تعديل اسم تصنيف الحسابات المقفلة - renameClosedCategory]:
     * يحدث اسم تصنيف النظام للحسابات المغلقة في قاعدة البيانات والإعدادات.
     */
    suspend fun renameClosedCategory(newName: String) = withContext(Dispatchers.IO) {
        val systemClosed = repository.customCategoriesFlow.first().find { it.isSystemClosed }
        if (systemClosed != null) {
            repository.saveCustomCategory(systemClosed.copy(name = newName))
        } else {
            repository.saveCustomCategory(
                CustomCategory(
                    name = newName,
                    tabType = TAB_TYPE_HABAYEB,
                    iconEmoji = DEFAULT_EMOJI,
                    displayOrder = 0,
                    isSystemClosed = true
                )
            )
        }
        sharedPrefs.edit().putString(KEY_CLOSED_CUSTOM_NAME, newName).apply()
        triggerUpdate()
    }

    /**
     * [حفظ تصنيف مخصص جديد - saveCustomCategory]:
     * ينشئ تصنيفاً جديداً برتبة عرض تالية لأعلى رتبة متوفرة.
     */
    suspend fun saveCustomCategory(name: String) = withContext(Dispatchers.IO) {
        val maxOrder = repository.customCategoriesFlow.first().maxOfOrNull { it.displayOrder } ?: 0
        repository.saveCustomCategory(
            CustomCategory(
                name = name,
                tabType = TAB_TYPE_HABAYEB,
                iconEmoji = "",
                displayOrder = maxOrder + 1
            )
        )
        triggerUpdate()
    }

    /**
     * [إعادة تسمية تصنيف مخصص - renameCustomCategory]:
     * يحدث اسم التصنيف ويرحل كافة روابط العملاء والتثبيتات المرتبطة به تلقائياً.
     */
    suspend fun renameCustomCategory(category: CustomCategory, newName: String) = withContext(Dispatchers.IO) {
        try {
            val oldName = category.name
            if (oldName == newName) return@withContext

            repository.saveCustomCategory(category.copy(name = newName))

            val editor = sharedPrefs.edit()
            categoryMapCache.forEach { (customerId, cat) ->
                if (cat == oldName) {
                    categoryMapCache[customerId] = newName
                    editor.putString("$PREFIX_CAT_LINK$customerId", newName)
                }
            }

            val oldPinnedKey = "$PREFIX_KEY_PINNED_IN$oldName"
            val newPinnedKey = "$PREFIX_KEY_PINNED_IN$newName"
            val pinnedSet = pinnedMapCache.remove(oldName) ?: sharedPrefs.getStringSet(oldPinnedKey, null)?.toSet()
            if (pinnedSet != null) {
                pinnedMapCache[newName] = pinnedSet
                editor.putStringSet(newPinnedKey, pinnedSet).remove(oldPinnedKey)
            }
            editor.apply()

            triggerUpdate()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * [حذف تصنيف مخصص مع خيار معالجة الحسابات التابعة - deleteCustomCategoryWithChoice]:
     * يحذف التصنيف ويفك ارتباط حساباته، أو ينقلها بكافة معاملاتها لسلة المحذوفات.
     */
    suspend fun deleteCustomCategoryWithChoice(category: CustomCategory, deleteLinkedAccounts: Boolean) = withContext(Dispatchers.IO) {
        try {
            val editor = sharedPrefs.edit()
            val targetCategoryName = category.name
            val linkedCustomerIds = categoryMapCache.filter { it.value == targetCategoryName }.keys.toSet()

            if (deleteLinkedAccounts) {
                val allCustomers = repository.getAllCustomersDirect().filter { it.id in linkedCustomerIds }
                for (customer in allCustomers) {
                    val customerTxs = repository.getTransactionsForCustomerDirect(customer.id)
                    repository.softDeleteHabayebBundleToTrash(customer, customerTxs)
                    repository.deleteCustomerAndTransactions(customer.id)
                    categoryMapCache.remove(customer.id)
                    editor.remove("$PREFIX_CAT_LINK${customer.id}")
                }
            } else {
                for (id in linkedCustomerIds) {
                    categoryMapCache.remove(id)
                    editor.remove("$PREFIX_CAT_LINK$id")
                }
            }
            pinnedMapCache.remove(targetCategoryName)
            editor.remove("$PREFIX_KEY_PINNED_IN$targetCategoryName")
            editor.apply()

            repository.deleteCustomCategory(category)
            triggerUpdate()
            VibrationHelper.triggerDeleteVibration(application)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * [تحريك التصنيف لليسار في الواجهة العربية RTL - moveCategoryLeft]:
     * في الواجهات العربية يعني التحريك لليسار الانتقال للموقع ذي الفهرس الأعلى (index + 1).
     */
    suspend fun moveCategoryLeft(currentOrder: List<String>, categoryName: String) = withContext(Dispatchers.IO) {
        val index = currentOrder.indexOf(categoryName)
        // In RTL Arabic layout, moving visually to the LEFT means moving towards higher index (index + 1)
        if (index >= 0 && index < currentOrder.size - 1) {
            val newList = currentOrder.toMutableList()
            val temp = newList[index]
            newList[index] = newList[index + 1]
            newList[index + 1] = temp
            triggerUpdate()
            repository.updateCustomCategoriesOrder(newList)
        }
    }

    /**
     * [تحريك التصنيف لليمين في الواجهة العربية RTL - moveCategoryRight]:
     * في الواجهات العربية يعني التحريك لليمين الانتقال للموقع ذي الفهرس الأقل (index - 1).
     */
    suspend fun moveCategoryRight(currentOrder: List<String>, categoryName: String) = withContext(Dispatchers.IO) {
        val index = currentOrder.indexOf(categoryName)
        // In RTL Arabic layout, moving visually to the RIGHT means moving towards lower index (index - 1)
        if (index > 0) {
            val newList = currentOrder.toMutableList()
            val temp = newList[index]
            newList[index] = newList[index - 1]
            newList[index - 1] = temp
            triggerUpdate()
            repository.updateCustomCategoriesOrder(newList)
        }
    }

    /**
     * [إعادة ترتيب كامل التصنيفات - reorderCategories]:
     * يحدث ترتيب عرض التصنيفات في قاعدة البيانات ويطلق إشعار التحديث.
     */
    suspend fun reorderCategories(newList: List<String>) = withContext(Dispatchers.IO) {
        repository.updateCustomCategoriesOrder(newList)
        triggerUpdate()
    }

    /**
     * الثوابت والمفاتيح المعمارية لمدير التصنيفات.
     */
    companion object {
        private const val TAG = "HabayebCategoryManager"
        const val PREFIX_CAT_LINK = "CAT_LINK_"
        private const val KEY_GLOBAL_ALL = "GLOBAL_ALL"
        private const val KEY_CLOSED_CUSTOM_NAME = "CLOSED_CUSTOM_NAME_KEY"
        private const val PREFIX_KEY_PINNED_IN = "KEY_PINNED_IN_"
        private const val TAB_TYPE_HABAYEB = "HABAYEB"
        private const val DEFAULT_EMOJI = "📁"
        private const val MAX_PINNED_COUNT = 3
    }
}

