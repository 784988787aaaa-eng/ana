/**
 * =====================================================================
 * ملف: مدير التفضيلات المشتركة المركزي (PreferenceManager.kt)
 * =====================================================================
 * 
 * [الغرض العام والتعليمي من الملف]:
 * يدير هذا المكون التفضيلات التخزينية الخفيفة في التطبيق عبر كائنات [SharedPreferences]،
 * ويعتمد نمط "الكتابة المزدوجة المتزامنة" (Dual-Write Consistency Pattern) لضمان عدم
 * فقدان الإعدادات وروابط التصنيفات أثناء التحديثات أو الانتقال بين الإصدارات القديمة والحديثة.
 * 
 * [المسؤوليات المعمارية والتقنية]:
 * 1. إدارة ملفي التفضيلات المتزامنين:
 *    - `mizan_sec_prefs`: الملف الأمني والتاريخي المشترك.
 *    - `mizan_finance_prefs`: ملف التفضيلات المالية وتصنيفات الحسابات.
 * 2. الكتابة المزدوجة المتزامنة (Dual Preference Write):
 *    - تطبيق أي تعديل على كلا الملفين معاً بدفعة واحدة لمنع التضارب وضمان استمرارية الروابط.
 * 3. إدارة روابط العملاء بالتصنيفات (Category Linkage):
 *    - ربط كل حساب عميل بتصنيف فرعي معين لسهولة التصفية والتنظيم.
 * 4. إدارة الترتيب المخصص والتسميات:
 *    - حفظ ترتيب التبويبات والتصنيفات في واجهة المستخدم، وتخصيص اسم الحسابات المقفلة.
 */
package com.example.data.repository

// ---------------------------------------------------------------------
// استيراد حزم سياق أندرويد والتفضيلات المشتركة
// ---------------------------------------------------------------------
import android.content.Context
import android.content.SharedPreferences

/**
 * [فئة مدير التفضيلات المشتركة المركزي - PreferenceManager]:
 * توفر واجهة موحدة للتعامل مع ملفات التفضيلات المتعددة.
 *
 * @param context سياق التطبيق للوصول إلى ملفات التفضيلات في وضع الخصوصية.
 */
class PreferenceManager(private val context: Context) {

    /**
     * [الكائن المرافق للثوابت والمفاتيح التخزينية]:
     */
    companion object {
        /** اسم ملف التفضيلات الأمني والتاريخي */
        const val PREFS_MIZAN_SEC = "mizan_sec_prefs"
        /** اسم ملف تفضيلات النظام المالي */
        const val PREFS_MIZAN_FINANCE = "mizan_finance_prefs"

        /** بادئة مفتاح ربط العميل بالتصنيف */
        const val PREF_CAT_LINK_PREFIX = "CAT_LINK_"
        /** بادئة مفتاح قائمة العملاء المثبتين في التصنيف */
        const val PREF_KEY_PINNED_PREFIX = "KEY_PINNED_IN_"
        /** مفتاح تخزين الترتيب المخصص للتصنيفات بصيغة JSON */
        const val PREF_CATEGORY_ORDER_LIST_KEY = "CATEGORY_ORDER_LIST_KEY"
        /** مفتاح تخصيص اسم تبويب الحسابات المقفلة */
        const val PREF_CLOSED_CUSTOM_NAME_KEY = "CLOSED_CUSTOM_NAME_KEY"
    }

    /**
     * [جلب التفضيلات الأمنية - getSecurityPreferences]:
     * يستخرج ملف التفضيلات الخاص بالجلسات والأمان.
     */
    fun getSecurityPreferences(): SharedPreferences =
        context.getSharedPreferences(PREFS_MIZAN_SEC, Context.MODE_PRIVATE)

    /**
     * [جلب التفضيلات المالية - getFinancePreferences]:
     * يستخرج ملف التفضيلات الخاص بالحسابات والتصنيفات.
     */
    fun getFinancePreferences(): SharedPreferences =
        context.getSharedPreferences(PREFS_MIZAN_FINANCE, Context.MODE_PRIVATE)

    /**
     * [الكتابة المزدوجة المتزامنة - writeDualPreference]:
     * تنفذ التعديل التخزيني على كلا الملفين بالتوازي وتطبق التغييرات في الخلفية [apply].
     *
     * @param action دالة لامبدا تستقبل محرري التفضيلات لملء البيانات.
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
     * [استرجاع تصنيف العميل - getCategoryLinkForCustomer]:
     * يبحث عن التصنيف المرتبط بالعميل في الملف المالي، وإذا لم يجده يبحث في الملف الأمني القديم.
     *
     * @param customerId معرف العميل.
     * @return اسم التصنيف المربوط أو null.
     */
    fun getCategoryLinkForCustomer(customerId: String): String? {
        val financePrefs = getFinancePreferences()
        val secPrefs = getSecurityPreferences()
        return financePrefs.getString("$PREF_CAT_LINK_PREFIX$customerId", null)
            ?: secPrefs.getString("$PREF_CAT_LINK_PREFIX$customerId", null)
    }

    /**
     * [حفظ ارتباط العميل بالتصنيف - saveCategoryLinkForCustomer]:
     * يسجل ربط العميل بالتصنيف الممرر في كلا الملفين المتزامنين.
     *
     * @param customerId معرف العميل.
     * @param categoryName اسم التصنيف المستهدف.
     */
    fun saveCategoryLinkForCustomer(customerId: String, categoryName: String) {
        writeDualPreference { sharedEdit, financeEdit ->
            sharedEdit.putString("$PREF_CAT_LINK_PREFIX$customerId", categoryName)
            financeEdit.putString("$PREF_CAT_LINK_PREFIX$customerId", categoryName)
        }
    }

    /**
     * [حذف ارتباط العميل بالتصنيف - removeCategoryLinkForCustomer]:
     * يزيل الرابط المخصص للعميل ويعيده للتصنيف الافتراضي.
     *
     * @param customerId معرف العميل.
     */
    fun removeCategoryLinkForCustomer(customerId: String) {
        writeDualPreference { sharedEdit, financeEdit ->
            sharedEdit.remove("$PREF_CAT_LINK_PREFIX$customerId")
            financeEdit.remove("$PREF_CAT_LINK_PREFIX$customerId")
        }
    }

    /**
     * [استرجاع ترتيب التصنيفات - getCategoryOrderList]:
     * يجلب قائمة الترتيب المخصص للتصنيفات.
     */
    fun getCategoryOrderList(): String? {
        val financePrefs = getFinancePreferences()
        val secPrefs = getSecurityPreferences()
        return financePrefs.getString(PREF_CATEGORY_ORDER_LIST_KEY, null)
            ?: secPrefs.getString(PREF_CATEGORY_ORDER_LIST_KEY, null)
    }

    /**
     * [حفظ ترتيب التصنيفات - saveCategoryOrderList]:
     * يحفظ الترتيب الجديد لشريط التصنيفات في كلا الملفين.
     *
     * @param orderListJson مصفوفة الترتيب بصيغة JSON.
     */
    fun saveCategoryOrderList(orderListJson: String) {
        writeDualPreference { sharedEdit, financeEdit ->
            sharedEdit.putString(PREF_CATEGORY_ORDER_LIST_KEY, orderListJson)
            financeEdit.putString(PREF_CATEGORY_ORDER_LIST_KEY, orderListJson)
        }
    }

    /**
     * [استرجاع الاسم المخصص للحسابات المقفلة - getClosedCustomName]:
     * يجلب التسمية المخصصة التي حددها المستخدم لتبويب المقفلين.
     */
    fun getClosedCustomName(): String? {
        val financePrefs = getFinancePreferences()
        val secPrefs = getSecurityPreferences()
        return financePrefs.getString(PREF_CLOSED_CUSTOM_NAME_KEY, null)
            ?: secPrefs.getString(PREF_CLOSED_CUSTOM_NAME_KEY, null)
    }

    /**
     * [حفظ الاسم المخصص للحسابات المقفلة - saveClosedCustomName]:
     * يحدث اسم التبويب المقفل في التفضيلات المتزامنة.
     *
     * @param name الاسم المخصص الجديد.
     */
    fun saveClosedCustomName(name: String) {
        writeDualPreference { sharedEdit, financeEdit ->
            sharedEdit.putString(PREF_CLOSED_CUSTOM_NAME_KEY, name)
            financeEdit.putString(PREF_CLOSED_CUSTOM_NAME_KEY, name)
        }
    }
}

