/*
 * ╔══════════════════════════════════════════════════════════════════════╗
 * ║ التوثيق العربي الهندسي — MASTERCLASS DOCUMENTATION                 ║
 * ╚══════════════════════════════════════════════════════════════════════╝
 *
 * الملف: app/src/main/java/com/example/ui/viewmodel/FinanceConstants.kt
 * القطاع المعماري: ViewModels & UI State.
 *
 * الوصف المعماري:
 * عقود وثوابت مشتركة لطبقة إدارة الأموال، بما في ذلك مفاتيح التفضيلات وأنواع العمليات والقيم الافتراضية.
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
// السطر 11: object FinanceConstants — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 12: const val PREFS_NAME — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 13: const val KEY_ONBOARDING_SHOWN — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 14: const val KEY_CATEGORIES_POPULATED — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 15: const val KEY_ACTIVATION_CODE — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 16: const val KEY_LINK_HABAYEB_DEBTS — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 19: const val PREFS_FLOATING_SEARCH — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 20: const val KEY_FLOATING_SEARCH_ACTIVE — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 23: const val EXTRA_NAVIGATE_TO — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 24: const val DEST_BACKUP_SETTINGS — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 27: const val DEFAULT_FALLBACK_VERSION — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 30: const val PREFS_BACKUP — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 31: const val KEY_LAST_SUCCESSFUL_BACKUP — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 34: const val MIME_TYPE_JSON — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 35: const val MIME_TYPE_ALL_APP — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 36: const val BACKUP_FILE_PREFIX — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 37: const val BACKUP_CLOUD_FILE_PREFIX — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 38: const val BACKUP_SILENT_FILE_NAME — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 39: const val BACKUP_FILE_EXTENSION — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 40: const val BACKUP_DATE_FORMAT — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 43: const val DEFAULT_CURRENCY_CODE — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 44: const val FALLBACK_CURRENCY_SYMBOL — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 46: const val CATEGORY_CLOSED — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 47: const val TYPE_OWED_TO_THEM — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 48: const val TYPE_OWED_BY_THEM — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 50: const val FREQ_DAILY — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 51: const val FREQ_WEEKLY — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 52: const val FREQ_MONTHLY — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// السطر 55: enum class HabayebTransactionType — عنصر ضمن عقد الملف؛ تُفهم مدخلاته ومخرجاته من توقيعه وجسمه الأصلي دون تعديل.
// --- نهاية الفهرس التوثيقي ---

package com.example.ui.viewmodel

/**
 * الثوابت المحاسبية والمفاتيح المشتركة لطبقة إدارة الأموال (Finance Contracts & Constants)
 *
 * التوثيق المعماري:
 * 1. قيم `DEFAULT_CURRENCY_CODE` و `FALLBACK_CURRENCY_SYMBOL` تمثل العقود القياسية للنظام في حال غياب رمز العملة.
 * 2. مفاتيح التفضيلات (`PREFS_NAME`, `KEY_ONBOARDING_SHOWN`, `KEY_LINK_HABAYEB_DEBTS`) ثابتة لضمان التوافقية العكسية.
 * 3. قيم الأنواع والتكرار (`FREQ_DAILY`, `TYPE_OWED_BY_THEM`, الخ) تتطابق مع عقود قاعدة البيانات وسلسلة الـ JSON.
 */
object FinanceConstants {
    const val PREFS_NAME = "mizan_sec_prefs"
    const val KEY_ONBOARDING_SHOWN = "onboarding_shown"
    const val KEY_CATEGORIES_POPULATED = "categories_populated"
    const val KEY_ACTIVATION_CODE = "m_act_code"
    const val KEY_LINK_HABAYEB_DEBTS = "KEY_LINK_HABAYEB_DEBTS"

    // مفاتيح التفضيلات المشتركة والبحث العائم
    const val PREFS_FLOATING_SEARCH = "floating_search_prefs"
    const val KEY_FLOATING_SEARCH_ACTIVE = "KEY_FLOATING_SEARCH_ACTIVE"

    // مفاتيح التنقل وIntent
    const val EXTRA_NAVIGATE_TO = "navigate_to"
    const val DEST_BACKUP_SETTINGS = "backup_settings"

    // الإصدار الافتراضي الآمن
    const val DEFAULT_FALLBACK_VERSION = "1.2"

    // مفاتيح التفضيلات للنسخ الاحتياطي
    const val PREFS_BACKUP = "mizan_backup_prefs"
    const val KEY_LAST_SUCCESSFUL_BACKUP = "last_successful_backup_timestamp"

    // عقود النسخ الاحتياطي وصيغ الملفات
    const val MIME_TYPE_JSON = "application/json"
    const val MIME_TYPE_ALL_APP = "application/*"
    const val BACKUP_FILE_PREFIX = "Mizan_"
    const val BACKUP_CLOUD_FILE_PREFIX = "Mzd_"
    const val BACKUP_SILENT_FILE_NAME = "Mizan_Silent_Backup.mzd"
    const val BACKUP_FILE_EXTENSION = ".mzd"
    const val BACKUP_DATE_FORMAT = "yyyy-MM-dd_HH-mm"
    
    // قيم افتراضية آمنة
    const val DEFAULT_CURRENCY_CODE = "DEFAULT"
    const val FALLBACK_CURRENCY_SYMBOL = "ر.ي"

    const val CATEGORY_CLOSED = "CLOSED"
    const val TYPE_OWED_TO_THEM = "OWED_TO_THEM"
    const val TYPE_OWED_BY_THEM = "OWED_BY_THEM"

    const val FREQ_DAILY = "DAILY"
    const val FREQ_WEEKLY = "WEEKLY"
    const val FREQ_MONTHLY = "MONTHLY"
}

enum class HabayebTransactionType {
    OWED_BY_THEM,
    PAYMENT_BY_THEM,
    OWED_TO_THEM,
    PAYMENT_TO_THEM
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
