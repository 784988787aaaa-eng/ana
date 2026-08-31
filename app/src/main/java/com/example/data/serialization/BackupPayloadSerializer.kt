/*
 * ╔══════════════════════════════════════════════════════════════════════╗
 * ║ التوثيق العربي الهندسي — MASTERCLASS DOCUMENTATION                 ║
 * ╚══════════════════════════════════════════════════════════════════════╝
 *
 * الملف: app/src/main/java/com/example/data/serialization/BackupPayloadSerializer.kt
 * الدور المعماري: طبقة Serialization / Export.
 *
 * الرؤية التشغيلية:
 * هذا الملف يمثل جزءاً من المسار الذي يحول البيانات الداخلية في التطبيق
 * إلى مخرجات يمكن حفظها أو مشاركتها أو طباعتها خارج التطبيق. أثناء التشغيل
 * تبدأ الرحلة من بيانات Room/Domain، ثم تمر عبر هذا المكوّن، ثم تنتهي
 * بملف أو بنية قابلة للاستهلاك خارج التطبيق. لذلك يجب اعتبار هذا الملف
 * عقداً حساساً بين نموذج البيانات الداخلي وشكل البيانات الخارجي.
 *
 * الوصف المعماري:
 * المحوّل الرئيسي لبنية بيانات النسخة الاحتياطية إلى/من التمثيل القابل للتخزين والاستعادة.
 *
 * قاعدة الثبات البرمجي:
 * الكود الأصلي يبدأ بعد هذا الرأس مباشرة، وقد تم الحفاظ عليه حرفياً دون
 * تعديل أسماء أو أنواع أو قيم أو منطق تنفيذي. الإضافات في هذه النسخة
 * توثيقية فقط.
 *
 * قراءة تعليمية:
 * تخيل شاشة التطبيق بعد ضغط المستخدم على «تصدير»؛ البيانات التي تظهر
 * أمامه لا تُنسخ عشوائياً، بل تمر بسلسلة تحويل منظمة. هذا الملف هو إحدى
 * حلقات تلك السلسلة: يستقبل البنية المتوقعة، يطبق قواعد التنسيق/التسلسل
 * الخاصة به، ثم يسلم النتيجة للمرحلة التالية.
 */

// --- فهرس العناصر البرمجية في الملف ---
// السطر 191: data class BackupPayloadData — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 192: val settings — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 193: val commitments — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 194: val transactions — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 195: val habayebCustomers — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 196: val habayebTransactions — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 197: val deletedItems — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 198: val customCategories — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 199: val categoryLinks — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 200: val pinnedCustomerIdsByCategory — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 201: val categoryOrderList — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 202: val closedCustomName — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 209: object BackupPayloadSerializer — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 214: private const val KEY_MIZAN_AL_DAR_DB — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 215: private const val KEY_HABAYEB_DEBTS_DB — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 216: private const val KEY_METADATA — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 217: private const val KEY_APP_NAME — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 218: private const val KEY_APP_VERSION — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 219: private const val KEY_BACKUP_TIMESTAMP — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 220: private const val KEY_SECURITY_HASH — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 222: private const val KEY_SETTINGS — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 223: private const val KEY_CURRENCY_SYMBOL — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 224: private const val KEY_SCHOOL_EXPENSES_ENABLED — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 225: private const val KEY_EXCHANGE_RATES_JSON — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 227: private const val KEY_FIXED_COMMITMENTS — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 228: private const val KEY_NAME — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 229: private const val KEY_TARGET_AMOUNT — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 230: private const val KEY_CURRENT_PROGRESS — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 231: private const val KEY_ORDER_INDEX — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 233: private const val KEY_TRANSACTIONS — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 234: private const val KEY_ID — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 235: private const val KEY_TIMESTAMP — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 236: private const val KEY_TYPE — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 237: private const val KEY_CATEGORY — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 238: private const val KEY_AMOUNT — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 239: private const val KEY_DESCRIPTION — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 241: private const val KEY_HABAYEB_DEBTS — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 242: private const val KEY_CUSTOMERS — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 243: private const val KEY_PHONE — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 244: private const val KEY_NOTES — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 245: private const val KEY_CREATED_AT — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 246: private const val KEY_INITIAL_TYPE — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 247: private const val KEY_CATEGORY_LINK — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 249: private const val KEY_DEBT_TRANSACTIONS — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 250: private const val KEY_CUSTOMER_ID — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 251: private const val KEY_LINKED_MAIN_TX_ID — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 252: private const val KEY_IS_FOREIGN — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 253: private const val KEY_CURRENCY_CODE — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 254: private const val KEY_FOREIGN_AMOUNT — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 255: private const val KEY_EXCHANGE_RATE — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 256: private const val KEY_IS_RATE_CALCULATED — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 257: private const val KEY_EQUIVALENT_AMOUNT — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 258: private const val KEY_BASE_CURRENCY_CODE — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 260: private const val KEY_DELETED_ITEMS — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 261: private const val KEY_SOURCE_SYSTEM — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 262: private const val KEY_ORIGINAL_TABLE_NAME — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 263: private const val KEY_JSON_DATA — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 264: private const val KEY_DELETED_AT — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 266: private const val KEY_PINNED_CUSTOMER_IDS_BY_CATEGORY — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 267: private const val KEY_CATEGORY_ORDER_LIST — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 268: private const val KEY_CLOSED_CUSTOM_NAME — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 270: private const val KEY_CUSTOM_CATEGORIES — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 271: private const val KEY_TAB_TYPE — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 272: private const val KEY_ICON_EMOJI — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 273: private const val KEY_DISPLAY_ORDER — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 274: private const val KEY_IS_SYSTEM_CLOSED — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 277: fun calculateSha256Hash — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 281: fun calculateIntegrityHash — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 290: fun validatePayloadBeforeExport — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 303: fun validateJsonStructure — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 307: val root — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 313: val hasValidSchema — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 333: fun exportBackupToWriter — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 336: val jsonWriter — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 397: val catLink — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 415: val cleanLinkedId — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 496: suspend fun exportBackupToStream — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 512: suspend fun exportBackupToFile — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 528: suspend fun exportBackupToJson — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 531: val stringWriter — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 542: suspend fun exportBackupToJson — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 551: val extraData — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 553: val payloadData — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 578: fun getBigDecimal — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 580: val raw — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 582: val valueStr — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 586: val cleaned — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 603: suspend fun importBackupFromJson — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 607: val root — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 608: val sourceObj — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 610: val settingsObj — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 611: val fallbackCurrency — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 612: val settings — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 623: val commitmentsList — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 624: val commitmentsArr — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 627: val obj — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 639: val transactionsList — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 640: val transactionsArr — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 643: val obj — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// --- نهاية الفهرس التوثيقي ---

/*
 * ╔══════════════════════════════════════════════════════════════════════╗
 * ║ التوثيق العربي الهندسي — MASTERCLASS DOCUMENTATION                 ║
 * ╚══════════════════════════════════════════════════════════════════════╝
 *
 * الملف: /mnt/data/source_full/app/src/main/java/com/example/data/serialization/BackupPayloadSerializer.kt
 * الدور المعماري: طبقة Serialization / Export.
 *
 * الرؤية التشغيلية:
 * هذا الملف يمثل جزءاً من المسار الذي يحول البيانات الداخلية في التطبيق
 * إلى مخرجات يمكن حفظها أو مشاركتها أو طباعتها خارج التطبيق. أثناء التشغيل
 * تبدأ الرحلة من بيانات Room/Domain، ثم تمر عبر هذا المكوّن، ثم تنتهي
 * بملف أو بنية قابلة للاستهلاك خارج التطبيق. لذلك يجب اعتبار هذا الملف
 * عقداً حساساً بين نموذج البيانات الداخلي وشكل البيانات الخارجي.
 *
 * الوصف المعماري:
 * المحوّل الرئيسي لبنية بيانات النسخة الاحتياطية إلى/من التمثيل القابل للتخزين والاستعادة.
 *
 * قاعدة الثبات البرمجي:
 * الكود الأصلي يبدأ بعد هذا الرأس مباشرة، وقد تم الحفاظ عليه حرفياً دون
 * تعديل أسماء أو أنواع أو قيم أو منطق تنفيذي. الإضافات في هذه النسخة
 * توثيقية فقط.
 *
 * قراءة تعليمية:
 * تخيل شاشة التطبيق بعد ضغط المستخدم على «تصدير»؛ البيانات التي تظهر
 * أمامه لا تُنسخ عشوائياً، بل تمر بسلسلة تحويل منظمة. هذا الملف هو إحدى
 * حلقات تلك السلسلة: يستقبل البنية المتوقعة، يطبق قواعد التنسيق/التسلسل
 * الخاصة به، ثم يسلم النتيجة للمرحلة التالية.
 */

// --- فهرس العناصر البرمجية في الملف ---
// السطر 59: data class BackupPayloadData — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 60: val settings — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 61: val commitments — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 62: val transactions — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 63: val habayebCustomers — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 64: val habayebTransactions — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 65: val deletedItems — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 66: val customCategories — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 67: val categoryLinks — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 68: val pinnedCustomerIdsByCategory — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 69: val categoryOrderList — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 70: val closedCustomName — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 77: object BackupPayloadSerializer — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 82: private const val KEY_MIZAN_AL_DAR_DB — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 83: private const val KEY_HABAYEB_DEBTS_DB — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 84: private const val KEY_METADATA — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 85: private const val KEY_APP_NAME — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 86: private const val KEY_APP_VERSION — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 87: private const val KEY_BACKUP_TIMESTAMP — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 88: private const val KEY_SECURITY_HASH — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 90: private const val KEY_SETTINGS — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 91: private const val KEY_CURRENCY_SYMBOL — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 92: private const val KEY_SCHOOL_EXPENSES_ENABLED — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 93: private const val KEY_EXCHANGE_RATES_JSON — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 95: private const val KEY_FIXED_COMMITMENTS — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 96: private const val KEY_NAME — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 97: private const val KEY_TARGET_AMOUNT — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 98: private const val KEY_CURRENT_PROGRESS — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 99: private const val KEY_ORDER_INDEX — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 101: private const val KEY_TRANSACTIONS — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 102: private const val KEY_ID — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 103: private const val KEY_TIMESTAMP — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 104: private const val KEY_TYPE — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 105: private const val KEY_CATEGORY — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 106: private const val KEY_AMOUNT — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 107: private const val KEY_DESCRIPTION — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 109: private const val KEY_HABAYEB_DEBTS — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 110: private const val KEY_CUSTOMERS — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 111: private const val KEY_PHONE — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 112: private const val KEY_NOTES — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 113: private const val KEY_CREATED_AT — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 114: private const val KEY_INITIAL_TYPE — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 115: private const val KEY_CATEGORY_LINK — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 117: private const val KEY_DEBT_TRANSACTIONS — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 118: private const val KEY_CUSTOMER_ID — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 119: private const val KEY_LINKED_MAIN_TX_ID — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 120: private const val KEY_IS_FOREIGN — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 121: private const val KEY_CURRENCY_CODE — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 122: private const val KEY_FOREIGN_AMOUNT — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 123: private const val KEY_EXCHANGE_RATE — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 124: private const val KEY_IS_RATE_CALCULATED — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 125: private const val KEY_EQUIVALENT_AMOUNT — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 126: private const val KEY_BASE_CURRENCY_CODE — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 128: private const val KEY_DELETED_ITEMS — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 129: private const val KEY_SOURCE_SYSTEM — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 130: private const val KEY_ORIGINAL_TABLE_NAME — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 131: private const val KEY_JSON_DATA — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 132: private const val KEY_DELETED_AT — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 134: private const val KEY_PINNED_CUSTOMER_IDS_BY_CATEGORY — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 135: private const val KEY_CATEGORY_ORDER_LIST — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 136: private const val KEY_CLOSED_CUSTOM_NAME — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 138: private const val KEY_CUSTOM_CATEGORIES — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 139: private const val KEY_TAB_TYPE — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 140: private const val KEY_ICON_EMOJI — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 141: private const val KEY_DISPLAY_ORDER — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 142: private const val KEY_IS_SYSTEM_CLOSED — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 145: fun calculateSha256Hash — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 149: fun calculateIntegrityHash — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 158: fun validatePayloadBeforeExport — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 171: fun validateJsonStructure — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 175: val root — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 181: val hasValidSchema — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 201: fun exportBackupToWriter — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 204: val jsonWriter — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 265: val catLink — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 283: val cleanLinkedId — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 364: suspend fun exportBackupToStream — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 380: suspend fun exportBackupToFile — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 396: suspend fun exportBackupToJson — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 399: val stringWriter — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 410: suspend fun exportBackupToJson — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 419: val extraData — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 421: val payloadData — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 446: fun getBigDecimal — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 448: val raw — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 450: val valueStr — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 454: val cleaned — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 471: suspend fun importBackupFromJson — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 475: val root — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 476: val sourceObj — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 478: val settingsObj — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 479: val fallbackCurrency — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 480: val settings — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 491: val commitmentsList — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 492: val commitmentsArr — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 495: val obj — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 507: val transactionsList — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 508: val transactionsArr — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 511: val obj — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// --- نهاية الفهرس التوثيقي ---

/**
 * =====================================================================
 * ملف: محرك تسلسل وتصدير حمولة النسخ الاحتياطي (BackupPayloadSerializer.kt)
 * =====================================================================
 * 
 * [الغرض العام والتعليمي من الملف]:
 * يمثل هذا المكون العصب المركزي لعمليات تحويل البيانات المالية وقواعد بيانات التطبيق
 * الثنائية إلى صيغة نصية مهيكلة ومعيارية JSON، والعكس. يعتمد على تقنية التدفق المتسلسل
 * المباشر (Streaming Serialization) عبر [android.util.JsonWriter] لضمان استهلاك ذاكرة ثابت
 * ومنع أخطاء نفاد الذاكرة (OutOfMemoryError) أثناء تصدير مئات الآلاف من السجلات.
 * 
 * [المسؤوليات المعمارية والتقنية]:
 * 1. الحفاظ الصارم على الدقة المالية لـ [BigDecimal]:
 *    - تحويل الأرقام حصرياً عبر `toPlainString()` دون أي تحويل وسيط إلى أرقام عشرية عائمة (Float/Double).
 * 2. التوافق التاريخي العكسي (Backward Compatibility):
 *    - تثبيت مفاتيح بنية الـ JSON التاريخية لضمان استيراد النسخ القديمة دون أدنى تعارض.
 * 3. المعالجة الآمنة للتدفقات (Stream-Based I/O):
 *    - إتاحة التصدير المباشر إلى ملفات [File]، ومسارات خروج [OutputStream]، ومحررات نصوص [Writer].
 * 4. التدقيق الاستباقي للبنية والتحقق التشفيري:
 *    - فحص سلامة الحقول الإلزامية ورمز العملة وحساب التجزئة التشفيرية SHA-256 للبيانات.
 */
package com.example.data.serialization

// ---------------------------------------------------------------------
// استيراد حزم سياق أندرويد والكيانات ومحولات الأرقام ومعالجة JSON والتزامن
// ---------------------------------------------------------------------
import android.content.Context
import com.example.data.local.BigDecimalConverter
import com.example.data.local.entities.AppSettings
import com.example.data.local.entities.CustomCategory
import com.example.data.local.entities.DatabaseDefaults
import com.example.data.local.entities.DeletedItemEntity
import com.example.data.local.entities.FixedCommitment
import com.example.data.local.entities.HabayebCustomer
import com.example.data.local.entities.HabayebTransaction
import com.example.data.local.entities.TransactionDb
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.IOException
import java.math.BigDecimal

/**
 * [وعاء بيانات النسخ الاحتياطي الكامل - BackupPayloadData]:
 * يجمع كافة كيانات وتفضيلات وروابط النظام المالي في كائن موحد قبل التصدير.
 *
 * @property settings إعدادات التطبيق والعملة وأسعار الصرف.
 * @property commitments قائمة الالتزامات المالية الثابتة.
 * @property transactions قائمة قيود اليومية العامة.
 * @property habayebCustomers قائمة بطاقات عملاء الحبايب.
 * @property habayebTransactions قائمة معاملات ديون الحبايب والعملات الأجنبية.
 * @property deletedItems عناصر سلة المهملات.
 * @property customCategories التصنيفات المخصصة.
 * @property categoryLinks خريطة ربط العملاء بالتصنيفات.
 * @property pinnedCustomerIdsByCategory خريطة العملاء المثبتين حسب التصنيف.
 * @property categoryOrderList ترتيب التبويبات المخصص.
 * @property closedCustomName التسمية المخصصة للحسابات المقفلة.
 */
data class BackupPayloadData(
    val settings: AppSettings,
    val commitments: List<FixedCommitment>,
    val transactions: List<TransactionDb>,
    val habayebCustomers: List<HabayebCustomer> = emptyList(),
    val habayebTransactions: List<HabayebTransaction> = emptyList(),
    val deletedItems: List<DeletedItemEntity> = emptyList(),
    val customCategories: List<CustomCategory> = emptyList(),
    val categoryLinks: Map<String, String> = emptyMap(),
    val pinnedCustomerIdsByCategory: Map<String, Set<String>> = emptyMap(),
    val categoryOrderList: String? = null,
    val closedCustomName: String? = null
)

/**
 * [الكائن الأحادي لمحرك تسلسل النسخ الاحتياطي - BackupPayloadSerializer]:
 * يدير عمليات التحويل الثنائي وكتابة واستيراد حزم النسخ الاحتياطي.
 */
object BackupPayloadSerializer {

    // =================================================================
    // مفاتيح بنية الـ JSON المعيارية (ثابتة لحماية التوافق التاريخي)
    // =================================================================
    private const val KEY_MIZAN_AL_DAR_DB = "mizan_al_dar_db"
    private const val KEY_HABAYEB_DEBTS_DB = "habayeb_debts_db"
    private const val KEY_METADATA = "metadata"
    private const val KEY_APP_NAME = "app_name"
    private const val KEY_APP_VERSION = "app_version"
    private const val KEY_BACKUP_TIMESTAMP = "backup_timestamp"
    private const val KEY_SECURITY_HASH = "security_hash"

    private const val KEY_SETTINGS = "settings"
    private const val KEY_CURRENCY_SYMBOL = "currency_symbol"
    private const val KEY_SCHOOL_EXPENSES_ENABLED = "school_expenses_enabled"
    private const val KEY_EXCHANGE_RATES_JSON = "exchange_rates_json"

    private const val KEY_FIXED_COMMITMENTS = "fixed_commitments"
    private const val KEY_NAME = "name"
    private const val KEY_TARGET_AMOUNT = "target_amount"
    private const val KEY_CURRENT_PROGRESS = "current_progress"
    private const val KEY_ORDER_INDEX = "order_index"

    private const val KEY_TRANSACTIONS = "transactions"
    private const val KEY_ID = "id"
    private const val KEY_TIMESTAMP = "timestamp"
    private const val KEY_TYPE = "type"
    private const val KEY_CATEGORY = "category"
    private const val KEY_AMOUNT = "amount"
    private const val KEY_DESCRIPTION = "description"

    private const val KEY_HABAYEB_DEBTS = "habayeb_debts"
    private const val KEY_CUSTOMERS = "customers"
    private const val KEY_PHONE = "phone"
    private const val KEY_NOTES = "notes"
    private const val KEY_CREATED_AT = "created_at"
    private const val KEY_INITIAL_TYPE = "initial_type"
    private const val KEY_CATEGORY_LINK = "category_link"

    private const val KEY_DEBT_TRANSACTIONS = "debt_transactions"
    private const val KEY_CUSTOMER_ID = "customer_id"
    private const val KEY_LINKED_MAIN_TX_ID = "linked_main_tx_id"
    private const val KEY_IS_FOREIGN = "is_foreign"
    private const val KEY_CURRENCY_CODE = "currency_code"
    private const val KEY_FOREIGN_AMOUNT = "foreign_amount"
    private const val KEY_EXCHANGE_RATE = "exchange_rate"
    private const val KEY_IS_RATE_CALCULATED = "is_rate_calculated"
    private const val KEY_EQUIVALENT_AMOUNT = "equivalent_amount"
    private const val KEY_BASE_CURRENCY_CODE = "base_currency_code"

    private const val KEY_DELETED_ITEMS = "deleted_items"
    private const val KEY_SOURCE_SYSTEM = "sourceSystem"
    private const val KEY_ORIGINAL_TABLE_NAME = "originalTableName"
    private const val KEY_JSON_DATA = "jsonData"
    private const val KEY_DELETED_AT = "deletedAt"

    private const val KEY_PINNED_CUSTOMER_IDS_BY_CATEGORY = "pinned_customer_ids_by_category"
    private const val KEY_CATEGORY_ORDER_LIST = "category_order_list"
    private const val KEY_CLOSED_CUSTOM_NAME = "closed_custom_name"

    private const val KEY_CUSTOM_CATEGORIES = "custom_categories"
    private const val KEY_TAB_TYPE = "tab_type"
    private const val KEY_ICON_EMOJI = "icon_emoji"
    private const val KEY_DISPLAY_ORDER = "display_order"
    private const val KEY_IS_SYSTEM_CLOSED = "is_system_closed"

    /** حساب بصمة التجزئة SHA-256 للنصوص */
    fun calculateSha256Hash(input: String): String =
        BackupIntegrityManager.calculateSha256Hash(input)

    /** حساب البصمة المنطقية الحتمية للحمولة */
    fun calculateIntegrityHash(data: BackupPayloadData): String =
        BackupIntegrityManager.calculateIntegrityHash(data)

    /**
     * [التحقق من سلامة البيانات قبل التصدير - validatePayloadBeforeExport]:
     * يفحص صحة الحقول الأساسية كرمز العملة قبل الشروع في التصدير.
     *
     * @param data بيانات حمولة النسخة الاحتياطية.
     */
    fun validatePayloadBeforeExport(data: BackupPayloadData) {
        if (data.settings.currencySymbol.isBlank()) {
            throw IllegalArgumentException("رمز العملة في الإعدادات لا يمكن أن يكون فارغاً")
        }
    }

    /**
     * [التحقق من صحة بنية JSON الأساسية - validateJsonStructure]:
     * يتأكد من سلامة نص الـ JSON ووجود الأقسام والجداول الرئيسية قبل المعالجة.
     *
     * @param rawJson النص الخام لملف النسخة.
     * @return كائن [JSONObject] الجذري.
     */
    fun validateJsonStructure(rawJson: String): JSONObject {
        if (rawJson.isBlank()) {
            throw IOException("نص النسخة الاحتياطية فارغ")
        }
        val root = try {
            JSONObject(rawJson)
        } catch (e: Exception) {
            throw IOException("صيغة JSON غير صالحة للنسخة الاحتياطية: ${e.message}", e)
        }

        val hasValidSchema = root.has(KEY_METADATA) ||
                root.has(KEY_SETTINGS) ||
                root.has(KEY_TRANSACTIONS) ||
                root.has(KEY_MIZAN_AL_DAR_DB) ||
                root.has(KEY_HABAYEB_DEBTS_DB)

        if (!hasValidSchema) {
            throw IOException("بنية ملف النسخة الاحتياطية غير معروفة أو تفتقد للعناصر الأساسية")
        }

        return root
    }

    /**
     * [التصدير المتدفق المباشر إلى كاتب - exportBackupToWriter]:
     * يكتب عناصر الحمولة تباعاً عبر [android.util.JsonWriter] دون تجميعها كنص ضخم في الذاكرة.
     *
     * @param data بيانات الحمولة الشاملة.
     * @param writer كاتب الإدخال/الإخراج المستهدف.
     */
    fun exportBackupToWriter(data: BackupPayloadData, writer: java.io.Writer) {
        validatePayloadBeforeExport(data)

        val jsonWriter = android.util.JsonWriter(writer)
        jsonWriter.beginObject()

        // البيانات الوصفية (Metadata)
        jsonWriter.name(KEY_METADATA)
        jsonWriter.beginObject()
        jsonWriter.name(KEY_APP_NAME).value("Mizan Al-Dar")
        jsonWriter.name(KEY_APP_VERSION).value("1.1.0")
        jsonWriter.name(KEY_BACKUP_TIMESTAMP).value(System.currentTimeMillis() / 1000)
        jsonWriter.name(KEY_SECURITY_HASH).value(calculateIntegrityHash(data))
        jsonWriter.endObject()

        // الإعدادات العامة (Settings)
        jsonWriter.name(KEY_SETTINGS)
        jsonWriter.beginObject()
        jsonWriter.name(KEY_CURRENCY_SYMBOL).value(data.settings.currencySymbol)
        jsonWriter.name(KEY_SCHOOL_EXPENSES_ENABLED).value(data.settings.schoolExpensesEnabled)
        jsonWriter.name(KEY_EXCHANGE_RATES_JSON).value(data.settings.exchangeRatesJson)
        jsonWriter.endObject()

        // الالتزامات المالية الثابتة (Fixed Commitments)
        jsonWriter.name(KEY_FIXED_COMMITMENTS)
        jsonWriter.beginArray()
        for (fc in data.commitments) {
            jsonWriter.beginObject()
            jsonWriter.name(KEY_NAME).value(fc.name)
            jsonWriter.name(KEY_TARGET_AMOUNT).value(fc.targetAmount.toPlainString())
            jsonWriter.name(KEY_CURRENT_PROGRESS).value(fc.currentProgress.toPlainString())
            jsonWriter.name(KEY_ORDER_INDEX).value(fc.orderIndex)
            jsonWriter.endObject()
        }
        jsonWriter.endArray()

        // قيود اليومية العامة (Transactions)
        jsonWriter.name(KEY_TRANSACTIONS)
        jsonWriter.beginArray()
        for (tx in data.transactions) {
            jsonWriter.beginObject()
            jsonWriter.name(KEY_ID).value(tx.id)
            jsonWriter.name(KEY_TIMESTAMP).value(tx.timestamp)
            jsonWriter.name(KEY_TYPE).value(tx.type)
            jsonWriter.name(KEY_CATEGORY).value(tx.category)
            jsonWriter.name(KEY_AMOUNT).value(tx.amount.toPlainString())
            jsonWriter.name(KEY_DESCRIPTION).value(tx.description)
            jsonWriter.endObject()
        }
        jsonWriter.endArray()

        // ديون الحبايب والعملاء والعملات الأجنبية (Habayeb Debts)
        jsonWriter.name(KEY_HABAYEB_DEBTS)
        jsonWriter.beginObject()
        jsonWriter.name(KEY_CUSTOMERS)
        jsonWriter.beginArray()
        for (c in data.habayebCustomers) {
            jsonWriter.beginObject()
            jsonWriter.name(KEY_ID).value(c.id)
            jsonWriter.name(KEY_NAME).value(c.name)
            jsonWriter.name(KEY_PHONE).value(c.phone)
            jsonWriter.name(KEY_NOTES).value(c.notes)
            jsonWriter.name(KEY_CREATED_AT).value(c.createdAt)
            jsonWriter.name(KEY_INITIAL_TYPE).value(c.initialType)
            val catLink = data.categoryLinks[c.id]
            if (catLink != null) {
                jsonWriter.name(KEY_CATEGORY_LINK).value(catLink)
            }
            jsonWriter.endObject()
        }
        jsonWriter.endArray()

        jsonWriter.name(KEY_DEBT_TRANSACTIONS)
        jsonWriter.beginArray()
        for (t in data.habayebTransactions) {
            jsonWriter.beginObject()
            jsonWriter.name(KEY_ID).value(t.id)
            jsonWriter.name(KEY_CUSTOMER_ID).value(t.customerId)
            jsonWriter.name(KEY_TYPE).value(t.type)
            jsonWriter.name(KEY_AMOUNT).value(t.amount.toPlainString())
            jsonWriter.name(KEY_TIMESTAMP).value(t.timestamp)
            jsonWriter.name(KEY_DESCRIPTION).value(t.description)
            val cleanLinkedId = t.linkedMainTxId?.trim()?.takeIf { 
                it.isNotBlank() && !it.equals("null", ignoreCase = true) && it != "0" && it != t.id 
            }
            if (cleanLinkedId != null) {
                jsonWriter.name(KEY_LINKED_MAIN_TX_ID).value(cleanLinkedId)
            } else {
                jsonWriter.name(KEY_LINKED_MAIN_TX_ID).nullValue()
            }
            jsonWriter.name(KEY_IS_FOREIGN).value(t.isForeign)
            jsonWriter.name(KEY_CURRENCY_CODE).value(t.currencyCode)
            jsonWriter.name(KEY_FOREIGN_AMOUNT).value(t.foreignAmount.toPlainString())
            jsonWriter.name(KEY_EXCHANGE_RATE).value(t.exchangeRate.toPlainString())
            jsonWriter.name(KEY_IS_RATE_CALCULATED).value(t.isRateCalculated)
            jsonWriter.name(KEY_EQUIVALENT_AMOUNT).value(t.equivalentAmount.toPlainString())
            jsonWriter.name(KEY_BASE_CURRENCY_CODE).value(t.baseCurrencyCode)
            jsonWriter.endObject()
        }
        jsonWriter.endArray()
        jsonWriter.endObject()

        // سلة المهملات والمحذوفات (Deleted Items)
        jsonWriter.name(KEY_DELETED_ITEMS)
        jsonWriter.beginArray()
        for (di in data.deletedItems) {
            jsonWriter.beginObject()
            jsonWriter.name(KEY_ID).value(di.id)
            jsonWriter.name(KEY_SOURCE_SYSTEM).value(di.sourceSystem)
            jsonWriter.name(KEY_ORIGINAL_TABLE_NAME).value(di.originalTableName)
            jsonWriter.name(KEY_JSON_DATA).value(di.jsonData)
            jsonWriter.name(KEY_DELETED_AT).value(di.deletedAt)
            jsonWriter.endObject()
        }
        jsonWriter.endArray()

        // الحسابات المثبتة وترتيب التصنيفات (Pinned Customers & Order)
        if (data.pinnedCustomerIdsByCategory.isNotEmpty()) {
            jsonWriter.name(KEY_PINNED_CUSTOMER_IDS_BY_CATEGORY)
            jsonWriter.beginObject()
            for ((catKey, set) in data.pinnedCustomerIdsByCategory.toSortedMap()) {
                jsonWriter.name(catKey)
                jsonWriter.beginArray()
                set.sorted().forEach { jsonWriter.value(it) }
                jsonWriter.endArray()
            }
            jsonWriter.endObject()
        }

        if (data.categoryOrderList != null) {
            jsonWriter.name(KEY_CATEGORY_ORDER_LIST).value(data.categoryOrderList)
        }
        if (data.closedCustomName != null) {
            jsonWriter.name(KEY_CLOSED_CUSTOM_NAME).value(data.closedCustomName)
        }

        // التصنيفات المخصصة (Custom Categories)
        if (data.customCategories.isNotEmpty()) {
            jsonWriter.name(KEY_CUSTOM_CATEGORIES)
            jsonWriter.beginArray()
            for (cc in data.customCategories) {
                jsonWriter.beginObject()
                jsonWriter.name(KEY_NAME).value(cc.name)
                jsonWriter.name(KEY_TAB_TYPE).value(cc.tabType)
                jsonWriter.name(KEY_ICON_EMOJI).value(cc.iconEmoji)
                jsonWriter.name(KEY_DISPLAY_ORDER).value(cc.displayOrder)
                jsonWriter.name(KEY_IS_SYSTEM_CLOSED).value(cc.isSystemClosed)
                jsonWriter.endObject()
            }
            jsonWriter.endArray()
        }

        jsonWriter.endObject()
        jsonWriter.flush()
    }

    /**
     * [التصدير المتدفق المباشر إلى تيار مخرجات - exportBackupToStream]:
     * يتدفق البيانات مباشرة عبر OutputStream على خيوط Dispatchers.IO.
     *
     * @param data بيانات الحمولة.
     * @param outputStream تيار المخرجات المستهدف.
     */
    suspend fun exportBackupToStream(
        data: BackupPayloadData,
        outputStream: java.io.OutputStream
    ) = withContext(Dispatchers.IO) {
        outputStream.bufferedWriter(Charsets.UTF_8).use { writer ->
            exportBackupToWriter(data, writer)
        }
    }

    /**
     * [التصدير المباشر إلى ملف محلي - exportBackupToFile]:
     * ينشئ ملف النسخة ويكتب البيانات فيه بشكل متدفق وسريع.
     *
     * @param data بيانات الحمولة.
     * @param targetFile الملف المستهدف على القرص.
     */
    suspend fun exportBackupToFile(
        data: BackupPayloadData,
        targetFile: java.io.File
    ) = withContext(Dispatchers.IO) {
        java.io.FileOutputStream(targetFile).use { fos ->
            exportBackupToStream(data, fos)
        }
    }

    /**
     * [تصدير الحمولة كنص JSON موحد - exportBackupToJson]:
     * يحول كائن [BackupPayloadData] إلى سلسلة نصية كاملة بصيغة JSON.
     *
     * @param data كائن الحمولة.
     * @return نص الـ JSON الناتج.
     */
    suspend fun exportBackupToJson(
        data: BackupPayloadData
    ): String = withContext(Dispatchers.IO) {
        val stringWriter = java.io.StringWriter()
        stringWriter.use { sw ->
            exportBackupToWriter(data, sw)
        }
        stringWriter.toString()
    }

    /**
     * [دالة التصدير المتوافقة مع الإصدارات السابقة - exportBackupToJson]:
     * تجمع المعاملات والكيانات والتفضيلات وتصدر نص الـ JSON الشامل.
     */
    suspend fun exportBackupToJson(
        settings: AppSettings,
        commitments: List<FixedCommitment>,
        transactions: List<TransactionDb>,
        habayebCustomers: List<HabayebCustomer> = emptyList(),
        habayebTransactions: List<HabayebTransaction> = emptyList(),
        deletedItems: List<DeletedItemEntity> = emptyList(),
        context: Context? = null
    ): String = withContext(Dispatchers.IO) {
        val extraData = context?.let { BackupExtraDataProvider.fetchExtraBackupData(it, habayebCustomers) }
            ?: BackupExtraData()
        val payloadData = BackupPayloadData(
            settings = settings,
            commitments = commitments,
            transactions = transactions,
            habayebCustomers = habayebCustomers,
            habayebTransactions = habayebTransactions,
            deletedItems = deletedItems,
            customCategories = extraData.customCategories,
            categoryLinks = extraData.categoryLinks,
            pinnedCustomerIdsByCategory = extraData.pinnedMap,
            categoryOrderList = extraData.categoryOrderList,
            closedCustomName = extraData.closedCustomName
        )
        exportBackupToJson(payloadData)
    }

    /**
     * [استخراج قيمة BigDecimal بأمان ودقة - getBigDecimal]:
     * يستخرج القيمة الرقمية بدقة متناهية دون تحويل وسيط إلى أرقام عشرية عائمة لمنع فقدان الهللات.
     *
     * @param obj كائن JSON الحاوي للحقل.
     * @param key اسم الحقل الرقمي.
     * @param fallback القيمة الاحتياطية عند الغياب.
     * @return كائن [BigDecimal] المطابق.
     */
    fun getBigDecimal(obj: JSONObject, key: String, fallback: String = "0"): BigDecimal {
        if (!obj.has(key)) return BigDecimal(fallback)
        val raw = obj.opt(key) ?: return BigDecimal(fallback)
        if (raw is BigDecimal) return raw
        val valueStr = raw.toString().trim()
        if (valueStr.isEmpty() || valueStr.equals("null", ignoreCase = true)) {
            return BigDecimal(fallback)
        }
        val cleaned = BigDecimalConverter.cleanNumberString(valueStr)
        if (cleaned.isEmpty()) return BigDecimal(fallback)
        return try {
            BigDecimal(cleaned)
        } catch (_: Exception) {
            BigDecimal(fallback)
        }
    }

    /**
     * [استيراد وتفكيك حمولة النسخة من JSON - importBackupFromJson]:
     * يفكك نص الـ JSON إلى نماذج الكيانات الأساسية (الإعدادات، الالتزامات، واليومية).
     *
     * @param jsonString نص النسخة الاحتياطية.
     * @param context سياق التطبيق لجلب العملة الافتراضية.
     * @return ثلاثية تحتوي على (الإعدادات، قائمة الالتزامات، قائمة قيود اليومية).
     */
    suspend fun importBackupFromJson(
        jsonString: String,
        context: Context? = null
    ): Triple<AppSettings, List<FixedCommitment>, List<TransactionDb>> = withContext(Dispatchers.IO) {
        val root = validateJsonStructure(jsonString)
        val sourceObj = if (root.has(KEY_MIZAN_AL_DAR_DB)) root.getJSONObject(KEY_MIZAN_AL_DAR_DB) else root

        val settingsObj = sourceObj.optJSONObject(KEY_SETTINGS)
        val fallbackCurrency = context?.getString(com.example.R.string.currency_yer) ?: DatabaseDefaults.DEFAULT_CURRENCY_SYMBOL
        val settings = if (settingsObj != null) {
            AppSettings(
                currencySymbol = settingsObj.optString(KEY_CURRENCY_SYMBOL, fallbackCurrency),
                schoolExpensesEnabled = settingsObj.optBoolean(KEY_SCHOOL_EXPENSES_ENABLED, true),
                themeMode = 0,
                exchangeRatesJson = settingsObj.optString(KEY_EXCHANGE_RATES_JSON, "{}")
            )
        } else {
            AppSettings()
        }

        val commitmentsList = mutableListOf<FixedCommitment>()
        val commitmentsArr = sourceObj.optJSONArray(KEY_FIXED_COMMITMENTS)
        if (commitmentsArr != null) {
            for (i in 0 until commitmentsArr.length()) {
                val obj = commitmentsArr.getJSONObject(i)
                commitmentsList.add(
                    FixedCommitment(
                        name = obj.getString(KEY_NAME),
                        targetAmount = getBigDecimal(obj, KEY_TARGET_AMOUNT),
                        currentProgress = getBigDecimal(obj, KEY_CURRENT_PROGRESS),
                        orderIndex = obj.optInt(KEY_ORDER_INDEX, i)
                    )
                )
            }
        }

        val transactionsList = mutableListOf<TransactionDb>()
        val transactionsArr = sourceObj.optJSONArray(KEY_TRANSACTIONS)
        if (transactionsArr != null) {
            for (i in 0 until transactionsArr.length()) {
                val obj = transactionsArr.getJSONObject(i)
                transactionsList.add(
                    TransactionDb(
                        id = obj.getString(KEY_ID),
                        timestamp = obj.getLong(KEY_TIMESTAMP),
                        type = obj.getString(KEY_TYPE),
                        category = obj.getString(KEY_CATEGORY),
                        amount = getBigDecimal(obj, KEY_AMOUNT),
                        description = obj.optString(KEY_DESCRIPTION, "")
                    )
                )
            }
        }

        Triple(settings, commitmentsList, transactionsList)
    }
}



/*
 * --- ملاحظات وتوصيات المعمارية البرمجية ---
 * 1) هذا الملف جزء من حدود التحويل بين نموذج التطبيق والمخرج الخارجي؛
 *    أي تغيير مستقبلي يجب أن يسبقه اختبار توافق مع المستهلكين الحاليين.
 * 2) يجب الحفاظ على دقة القيم المالية وعدم إجراء تحويلات تقريبية غير مقصودة.
 * 3) يفضّل مستقبلاً فصل مسؤولية بناء البيانات عن مسؤولية I/O عندما يسمح
 *    التصميم بذلك، مع إبقاء السلوك الحالي ثابتاً أثناء أي Refactoring.
 * 4) أي تعديل في صيغة المخرج يجب أن يرافقه اختبار Regression يثبت أن
 *    الملفات القديمة والجديدة قابلة للقراءة وفق متطلبات المشروع.
 * 5) عند التعامل مع بيانات المستخدم، ينبغي استمرار تطبيق سياسات الخصوصية
 *    والصلاحيات والمشاركة الآمنة قبل إرسال الملفات إلى تطبيقات خارجية.
 * 6) لا تمثل هذه الملاحظات تغييراً في التنفيذ الحالي؛ هي نقاط هندسية
 *    مرجعية لأي مرحلة تطوير مستقبلية.
 */


/*
 * --- ملاحظات وتوصيات المعمارية البرمجية ---
 * 1) هذا الملف جزء من حدود التحويل بين نموذج التطبيق والمخرج الخارجي؛
 *    أي تغيير مستقبلي يجب أن يسبقه اختبار توافق مع المستهلكين الحاليين.
 * 2) يجب الحفاظ على دقة القيم المالية وعدم إجراء تحويلات تقريبية غير مقصودة.
 * 3) يفضّل مستقبلاً فصل مسؤولية بناء البيانات عن مسؤولية I/O عندما يسمح
 *    التصميم بذلك، مع إبقاء السلوك الحالي ثابتاً أثناء أي Refactoring.
 * 4) أي تعديل في صيغة المخرج يجب أن يرافقه اختبار Regression يثبت أن
 *    الملفات القديمة والجديدة قابلة للقراءة وفق متطلبات المشروع.
 * 5) عند التعامل مع بيانات المستخدم، ينبغي استمرار تطبيق سياسات الخصوصية
 *    والصلاحيات والمشاركة الآمنة قبل إرسال الملفات إلى تطبيقات خارجية.
 * 6) لا تمثل هذه الملاحظات تغييراً في التنفيذ الحالي؛ هي نقاط هندسية
 *    مرجعية لأي مرحلة تطوير مستقبلية.
 */
