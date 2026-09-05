/**
 * =====================================================================
 * ملف: ثوابت وعقود منظومة النسخ الاحتياطي 
 * =====================================================================
 * 
 * [الغرض العام والتعليمي من الملف]:
 * يمثل هذا الكائن المرجع المعياري الموحد لجميع الثوابت والاتفاقيات المتعلقة
 * بمنظومة النسخ الاحتياطي السحابي والمحلي والاستعادة في تطبيق "الدفتر الذكي".
 * 
 * [المسؤوليات المعمارية والتقنية]:
 * 1. توحيد امتدادات وبادئات الملفات الرسمية .
 * 2. تثبيت أنماط وتنسيقات التواريخ لضمان فرز الملفات بدقة وسهولة استرجاعها.
 * 3. تثبيت مفاتيح التفضيلات المشتركة لتسجيل أوقات النسخ وحالة المزامنة المعلقة.
 * 4. الحفاظ الصارم على أسماء حقول البيانات المنظمة لقواعد البيانات السابقة والمحدثة لمنع كسر التوافق العكسي .
 */
package com.smartledger.aldaftar.data.backup

/**
 * [كائن الثوابت المركزية]:
 * يجمع كافة الثوابت غير القابلة للتغيير لمنظومة النسخ والاستعادة.
 */
object BackupConstants {

    // -----------------------------------------------------------------
    // 1. امتدادات وبادئات أسماء الملفات المحلية والسحابية والمؤقتة
    // -----------------------------------------------------------------
    /** الامتداد الرسمي لملفات النسخ المشفرة لتطبيق الميزان */
    const val BACKUP_FILE_EXTENSION = ".mzd"
    /** البادئة المستخدمة للملفات المحفوظة محلياً بواسطة المستخدم */
    const val BACKUP_FILE_PREFIX = "Mizan_"
    /** البادئة المستخدمة لملفات المزامنة السحابية على خدمة التخزين السحابي */
    const val BACKUP_CLOUD_FILE_PREFIX = "Mzd_"
    /** اسم ملف النسخ الصامت التلقائي اليومي */
    const val BACKUP_SILENT_FILE_NAME = "Mizan_Silent_Backup.mzd"
    /** بادئة الملفات المؤقتة أثناء عمليات المعالجة والكتابة */
    const val BACKUP_TEMP_PREFIX = "tmp_backup_"
    /** لاحقة الملفات المؤقتة */
    const val BACKUP_TEMP_SUFFIX = ".tmp"
    /** اسم ملف المرآة المحلي المتطابق مع السحابة للطوارئ */
    const val MIRROR_FILE_NAME = "google_drive_mirror.mzd"

    // -----------------------------------------------------------------
    // 2. أنماط تنسيق التواريخ والوقت في أسماء الملفات
    // -----------------------------------------------------------------
    /** نمط التاريخ والوقت الكامل لفرز النسخ الاحتياطية */
    const val BACKUP_DATE_FORMAT = "yyyy-MM-dd_HH-mm"
    /** نمط الشهر والسنة لتصنيف العمليات */
    const val MONTH_DATE_PATTERN = "yyyy-MM"

    // -----------------------------------------------------------------
    // 3. مفاتيح التخزين المشترك للتفضيلات 
    // -----------------------------------------------------------------
    /** اسم ملف تفضيلات النسخ الاحتياطي */
    const val PREFS_BACKUP = "mizan_backup_prefs"
    /** مفتاح حفظ الطابع الزمني لآخر نسخة احتياطية ناجحة */
    const val KEY_LAST_SUCCESSFUL_BACKUP = "last_successful_backup_timestamp"
    /** مفتاح حالة وجود رفع سحابي معلق قيد الانتظار للشبكة */
    const val KEY_PENDING_CLOUD_UPLOAD = "pending_cloud_upload"

    // -----------------------------------------------------------------
    // 4. أنواع تعريف المحتوى لتسجيل ومشاركة الملفات في نظام أندرويد
    // -----------------------------------------------------------------
    const val MIME_TYPE_JSON = "application/json"
    const val MIME_TYPE_ALL_APP = "application/*"
    const val MIME_TYPE_OCTET_STREAM = "application/octet-stream"

    // -----------------------------------------------------------------
    // 5. محددات الدقة الحسابية والمالية
    // -----------------------------------------------------------------
    /** عدد الخانات العشرية القياسي للحسابات الدقيقة */
    const val FINANCIAL_SCALE = 4

    // -----------------------------------------------------------------
    // 6. مفاتيح بنية البيانات المنظمة لتوافق النسخ وقواعد البيانات
    // -----------------------------------------------------------------
    const val JSON_KEY_MIZAN_AL_DAR_DB = "mizan_al_dar_db"
    const val JSON_KEY_HABAYEB_DEBTS_DB = "habayeb_debts_db"
    const val JSON_KEY_SETTINGS = "settings"
    const val JSON_KEY_TRANSACTIONS = "transactions"
    const val JSON_KEY_COMMITMENTS = "commitments"
    const val JSON_KEY_FIXED_COMMITMENTS = "fixed_commitments"
    const val JSON_KEY_HABAYEB_CUSTOMERS = "habayeb_customers"
    const val JSON_KEY_HABAYEB_TRANSACTIONS = "habayeb_transactions"
    const val JSON_KEY_DEBT_TRANSACTIONS = "debt_transactions"
    const val JSON_KEY_CUSTOMERS = "customers"
    const val JSON_KEY_DELETED_ITEMS = "deleted_items"
    const val JSON_KEY_CUSTOM_CATEGORIES = "custom_categories"
    const val JSON_KEY_PINNED_CUSTOMERS = "pinned_customer_ids_by_category"
    const val JSON_KEY_CATEGORY_ORDER_LIST = "category_order_list"
    const val JSON_KEY_CLOSED_CUSTOM_NAME = "closed_custom_name"
}

