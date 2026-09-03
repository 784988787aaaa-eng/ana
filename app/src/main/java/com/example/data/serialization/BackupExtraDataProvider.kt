/**
 * =====================================================================
 * ملف: مزود واستخراج البيانات الإضافية للنسخ الاحتياطي (BackupExtraDataProvider.kt)
 * =====================================================================
 * 
 * [الغرض العام والتعليمي من الملف]:
 * يتولى هذا المكون استخراج وجمع البيانات الوصفية (Metadata) والتفضيلات المخزنة
 * خارج جداول المعاملات الرئيسية، مثل روابط تصنيفات العملاء، وقوائم الحسابات المثبتة،
 * وترتيب التبويبات المخصص، والتصنيفات الفرعية، لتضمينها في حزمة النسخة الاحتياطية.
 * 
 * [المسؤوليات المعمارية والتقنية]:
 * 1. جمع التفضيلات المشتركة من كلا ملفي التفضيلات (`mizan_sec_prefs` و `mizan_finance_prefs`).
 * 2. استخراج خريطة روابط العملاء بالتصنيفات [categoryLinks].
 * 3. استخراج خريطة الحسابات المثبتة في أعلى القوائم [pinnedMap].
 * 4. جلب التصنيفات المخصصة مباشرة من قاعدة بيانات Room [CustomCategory].
 * 5. تجميع كل هذه الأوعية في كائن موحد [BackupExtraData] لتقديمه لمسلسل النسخ الاحتياطي.
 */
package com.example.data.serialization

// ---------------------------------------------------------------------
// استيراد حزم سياق أندرويد والتفضيلات المشتركة وقاعدة بيانات Room والتدفقات
// ---------------------------------------------------------------------
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.data.local.AppDatabase
import com.example.data.local.entities.CustomCategory
import com.example.data.local.entities.HabayebCustomer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

/**
 * [وعاء البيانات الإضافية للنسخ الاحتياطي - BackupExtraData]:
 * يجمع التفضيلات والروابط والتصنيفات في كائن بيانات واحد جاهز للتسلسل.
 *
 * @property categoryLinks خريطة تربط معرف العميل باسم تصنيفه المخصص.
 * @property pinnedMap خريطة تربط مفتاح التصنيف بمجموعة معرفات العملاء المثبتين فيه.
 * @property categoryOrderList نص JSON يحدد ترتيب عرض التصنيفات في الواجهة.
 * @property closedCustomName التسمية المخصصة لتبويب الحسابات المقفلة.
 * @property customCategories قائمة الكيانات المخزنة للتصنيفات المخصصة.
 */
data class BackupExtraData(
    val categoryLinks: Map<String, String> = emptyMap(),
    val pinnedMap: Map<String, Set<String>> = emptyMap(),
    val categoryOrderList: String? = null,
    val closedCustomName: String? = null,
    val customCategories: List<CustomCategory> = emptyList()
)

/**
 * [الكائن الأحادي لمزود البيانات الإضافية - BackupExtraDataProvider]:
 * يوفر دوالاً متخصصة لاستخراج وقراءة التفضيلات والحالات الوصفية بدقة وأمان.
 */
object BackupExtraDataProvider {

    /** وسم السجلات التشخيصية */
    private const val TAG = "BackupExtraDataProvider"
    /** أسماء ملفات التفضيلات المستهدفة */
    private const val PREF_MIZAN_SEC = "mizan_sec_prefs"
    private const val PREF_MIZAN_FINANCE = "mizan_finance_prefs"

    /** بادئات ومفاتيح قراءة التفضيلات */
    private const val PREFIX_CAT_LINK = "CAT_LINK_"
    private const val PREFIX_KEY_PINNED_IN = "KEY_PINNED_IN_"
    private const val KEY_CATEGORY_ORDER_LIST_PREF = "CATEGORY_ORDER_LIST_KEY"
    private const val KEY_CLOSED_CUSTOM_NAME_PREF = "CLOSED_CUSTOM_NAME_KEY"

    /**
     * [استخراج روابط الفئات المخصصة للعملاء - getCategoryLinks]:
     * يمر على قائمة العملاء ويستخرج اسم التصنيف المربوط بكل عميل من التفضيلات.
     *
     * @param financePrefs تفضيلات النظام المالي.
     * @param sharedPrefs تفضيلات الأمان المشتركة.
     * @param habayebCustomers قائمة عملاء الحبايب.
     * @return خريطة مفتاحها معرف العميل وقيمتها اسم التصنيف.
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
     * [استخراج خريطة العملاء المثبتين - getPinnedCategoriesMap]:
     * يجمع كافة مجموعات التثبيت من التفضيلات ويربطها بمفاتيح التصنيفات المقابلة.
     *
     * @param financePrefs تفضيلات النظام المالي.
     * @param sharedPrefs تفضيلات الأمان المشتركة.
     * @return خريطة مفتاحها اسم التصنيف وقيمتها مجموعة معرفات العملاء المثبتين.
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
     * [استخراج تفضيلات الترتيب والأسماء المخصصة - getUserPreferences]:
     * يجلب إعدادات ترتيب التبويبات واسم تبويب الحسابات المقفلة.
     *
     * @return زوج يحتوي على (نص ترتيب التصنيفات، الاسم المخصص للمقفلين).
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
     * [استخراج الفئات المخصصة من قاعدة البيانات - getCustomCategoriesData]:
     * يستعلم عن كافة التصنيفات المخصصة المسجلة في جدول [CustomCategory].
     *
     * @param context سياق التطبيق للوصول لقاعدة البيانات.
     * @return قائمة كائنات التصنيفات المخصصة.
     */
    suspend fun getCustomCategoriesData(context: Context): List<CustomCategory> = withContext(Dispatchers.IO) {
        try {
            val db = AppDatabase.getDatabase(context)
            db.customCategoryDao().getAllCustomCategoriesFlow().first()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load custom categories for backup payload", e)
            emptyList()
        }
    }

    /**
     * [تجميع كافة البيانات الإضافية في وعاء موحد - fetchExtraBackupData]:
     * الدالة الرئيسية لتجميع كافة البيانات الوصفية والإضافية بالتوازي على خيوط IO.
     *
     * @param context سياق التطبيق.
     * @param habayebCustomers قائمة عملاء الحبايب.
     * @return كائن [BackupExtraData] المحتوي على كافة الإضافات.
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

