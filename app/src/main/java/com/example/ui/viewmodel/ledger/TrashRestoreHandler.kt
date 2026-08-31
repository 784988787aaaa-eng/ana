/*
 * ╔══════════════════════════════════════════════════════════════════════╗
 * ║ التوثيق العربي الهندسي — MASTERCLASS DOCUMENTATION                 ║
 * ╚══════════════════════════════════════════════════════════════════════╝
 *
 * الملف: app/src/main/java/com/example/ui/viewmodel/ledger/TrashRestoreHandler.kt
 * القطاع المعماري: ViewModels & UI State.
 *
 * الوصف المعماري:
 * معالج عمليات الاستعادة من سلة المحذوفات ضمن طبقة الحالة، مع إبقاء قرار التنفيذ متوافقاً مع خدمات البيانات.
 *
 * الرؤية التعليمية والبصرية:
 * تخيل شاشة الهاتف أثناء تفاعل المستخدم: يضغط على زر أو يغيّر قيمة،
 * فتتولد إشارة، ثم تُعالج في طبقة الحالة، ثم تتغير الحالة التي تقرأها
 * Compose لإعادة رسم الشاشة. هذا الملف يقع في تلك السلسلة ويجب قراءته
 * باعتباره عقداً بين «ما فعله المستخدم» و«ما تراه الشاشة».
 *
 * قاعدة الثبات البرمجي:
 * النص التنفيذي الأصلي محفوظ حرفياً بعد هذا الرأس. الإضافات هنا توثيقية
 * فقط ولا تستبدل أي تعليمة أو اسماً أو قيمة أو منطقاً تنفيذياً.
 */

// --- الفهرس التوثيقي للعناصر البرمجية ---
// السطر 16: object TrashRestoreHandler — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 17: private const val TAG — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 18: private const val PREFS_MIZAN_SEC — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 19: private const val TABLE_HABAYEB_BUNDLE — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 21: fun restorePrefsForDeletedItem — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 24: val root — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 25: val custData — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 26: val cId — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 27: val sharedPrefs — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 30: val catLink — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 35: val pinnedCats — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 37: val catKey — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 38: val key — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 39: val existingSet — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 40: val newSet — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// --- نهاية الفهرس التوثيقي ---

package com.example.ui.viewmodel.ledger

import android.content.Context
import android.util.Log
import com.example.data.local.entities.DeletedItemEntity
import org.json.JSONObject

/**
 * معالج استعادة تفضيلات وسجلات سلة المهملات (Trash Restore Preferences Handler)
 *
 * المسؤولية المعمارية:
 * 1. استخراج واستعادة البيانات الوصفية (Metadata) المرتبطة بالعميل المستعاد من سلة المهملات مثل (روابط التصنيف CAT_LINK_ وتثبيتات الفئات).
 * 2. عزل مسؤولية تحديث التفضيلات المشتركة خارج ViewModel لمنع تضخم الكود وفصل إدارة الحالة عن تخزين الإعدادات.
 * 3. حماية المعاملات من الانهيار مع تسجيل أي استثناءات تالفة دون إخفاء الأخطاء.
 */
object TrashRestoreHandler {
    private const val TAG = "TrashRestoreHandler"
    private const val PREFS_MIZAN_SEC = "mizan_sec_prefs"
    private const val TABLE_HABAYEB_BUNDLE = "habayeb_bundle"

    fun restorePrefsForDeletedItem(context: Context, item: DeletedItemEntity) {
        try {
            if (item.originalTableName == TABLE_HABAYEB_BUNDLE) {
                val root = JSONObject(item.jsonData)
                val custData = root.getJSONObject("customer")
                val cId = custData.getString("id")
                val sharedPrefs = context.getSharedPreferences(PREFS_MIZAN_SEC, Context.MODE_PRIVATE)

                if (custData.has("categoryLink")) {
                    val catLink = custData.getString("categoryLink")
                    sharedPrefs.edit().putString("CAT_LINK_$cId", catLink).apply()
                }

                if (custData.has("pinnedCategories")) {
                    val pinnedCats = custData.getJSONArray("pinnedCategories")
                    for (i in 0 until pinnedCats.length()) {
                        val catKey = pinnedCats.getString(i)
                        val key = "KEY_PINNED_IN_$catKey"
                        val existingSet = sharedPrefs.getStringSet(key, emptySet()) ?: emptySet()
                        val newSet = existingSet.toMutableSet().apply { add(cId) }
                        sharedPrefs.edit().putStringSet(key, newSet).apply()
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error restoring preferences for trash item ${item.id}", e)
        }
    }
}


/*
 * --- ملاحظات وتوصيات المعمارية البرمجية ---
 * 1) يجب أن تبقى ViewModel طبقة تنسيق للحالة والأحداث، لا مستودعاً لقواعد
 *    المجال المالية التي ينبغي أن تعيش في طبقاتها المتخصصة.
 * 2) يوصى مستقبلاً بمراجعة دورة حياة كل Coroutine/Flow والتأكد من ارتباطها
 *    بـ viewModelScope أو نطاقها المقصود لمنع التسرب أو العمل بعد زوال الشاشة.
 * 3) عند تعديل UiState يجب الحفاظ على دلالة الحالات الانتقالية مثل التحميل،
 *    النجاح، الخطأ، والفراغ حتى لا تظهر واجهة مضللة للمستخدم.
 * 4) أي تغيير في الأحداث أو العقود العامة يجب أن يرافقه Regression Test
 *    يثبت أن التفاعل الحالي في Compose لم يتغير.
 * 5) الحسابات المالية والـ BigDecimal يجب أن تبقى في مسارها الدقيق، وألا
 *    تتحول إلى Double/Float داخل طبقة العرض إلا بقرار موثق وصريح.
 * 6) هذه التوصيات مرجعية مستقبلية فقط ولا تمثل أي تغيير في التنفيذ الحالي.
 */
