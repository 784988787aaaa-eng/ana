/*
 * ╔══════════════════════════════════════════════════════════════════════╗
 * ║ التوثيق العربي الهندسي — MASTERCLASS DOCUMENTATION                 ║
 * ╚══════════════════════════════════════════════════════════════════════╝
 *
 * الملف: app/src/main/java/com/example/data/serialization/excel/AllCustomersExcelEngine.kt
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
 * محرك إنشاء ملف Excel جامع لبيانات جميع العملاء مع الحفاظ على البنية المالية والتنسيق.
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
// السطر 122: object AllCustomersExcelEngine — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 125: private const val TAG — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 127: private const val LOCALE_AR — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 129: private const val LOCALE_EN — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 131: private const val FILE_PREFIX_ALL — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 134: private val DATE_FORMATTER_EN — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 136: private val TIME_FORMATTER_AR — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 138: private val DAY_FORMATTER_AR — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 149: fun generate — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 154: val fileName — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 155: val file — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 158: val bizHeader — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 159: val now — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 160: val dayName — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 161: val dateFormatted — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 162: val timeFormatted — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 163: val docDateText — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 165: var totalOwedByThem — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 166: var totalOwedToThem — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 167: val foreignSumsMap — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 170: val bdVal — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 182: val grandNetBalance — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 184: val columns — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 192: val rowsList — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 193: val mergesList — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 196: val rTitle — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 202: val rBiz — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 210: val rBizSub — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 221: val rStats — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 222: val statsText — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 238: val rHeader — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 247: var rIdx — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 249: val bdVal — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 250: val isPositive — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 251: val isNegative — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 253: val balanceStyle — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 259: val statusText — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 265: val foreignList — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 266: val foreignStr — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 267: val formatted — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 268: val prefix — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 272: val phoneVal — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 273: val fullAccountText — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 275: val rRow — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 286: val rTotals — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 300: val rFooter — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// --- نهاية الفهرس التوثيقي ---

/*
 * ╔══════════════════════════════════════════════════════════════════════╗
 * ║ التوثيق العربي الهندسي — MASTERCLASS DOCUMENTATION                 ║
 * ╚══════════════════════════════════════════════════════════════════════╝
 *
 * الملف: /mnt/data/source_full/app/src/main/java/com/example/data/serialization/excel/AllCustomersExcelEngine.kt
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
 * محرك إنشاء ملف Excel جامع لبيانات جميع العملاء مع الحفاظ على البنية المالية والتنسيق.
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
// السطر 43: object AllCustomersExcelEngine — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 46: private const val TAG — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 48: private const val LOCALE_AR — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 50: private const val LOCALE_EN — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 52: private const val FILE_PREFIX_ALL — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 55: private val DATE_FORMATTER_EN — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 57: private val TIME_FORMATTER_AR — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 59: private val DAY_FORMATTER_AR — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 70: fun generate — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 75: val fileName — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 76: val file — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 79: val bizHeader — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 80: val now — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 81: val dayName — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 82: val dateFormatted — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 83: val timeFormatted — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 84: val docDateText — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 86: var totalOwedByThem — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 87: var totalOwedToThem — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 88: val foreignSumsMap — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 91: val bdVal — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 103: val grandNetBalance — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 105: val columns — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 113: val rowsList — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 114: val mergesList — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 117: val rTitle — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 123: val rBiz — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 131: val rBizSub — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 142: val rStats — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 143: val statsText — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 159: val rHeader — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 168: var rIdx — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 170: val bdVal — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 171: val isPositive — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 172: val isNegative — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 174: val balanceStyle — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 180: val statusText — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 186: val foreignList — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 187: val foreignStr — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 188: val formatted — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 189: val prefix — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 193: val phoneVal — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 194: val fullAccountText — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 196: val rRow — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 207: val rTotals — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 221: val rFooter — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// --- نهاية الفهرس التوثيقي ---

/**
 * =====================================================================
 * ملف: محرك جداول إكسل الشاملة لكافة العملاء (AllCustomersExcelEngine.kt)
 * =====================================================================
 * 
 * [الغرض العام والتعليمي من الملف]:
 * يتولى هذا المحرك المتخصص بناء وتوليد ملفات إكسل (.xlsx) عالية التنسيق والجودة
 * لتقرير الأرصدة الشامل لجميع العملاء، بالاعتماد على محرك OpenXML الداخلي الخفيف
 * دون الحاجة لمكتبات خارجية ثقيلة (مثل Apache POI).
 * 
 * [المسؤوليات المعمارية والتقنية]:
 * 1. التجميع الحسابي الدقيق:
 *    - حساب إجمالي ما لنا (له) وما علينا (عليه) وصافي الأرصدة بدقة [BigDecimal].
 *    - تجميع وتحليل ديون العملات الأجنبية لكل عميل ودمجها في خريطة إحصائية شاملة.
 * 2. بناء هيكل جداول OpenXML المنسقة:
 *    - تحديد عروض الأعمدة ونطاقات الدمج (Merge Ranges) وصفوف الترويسة وبطاقة الإحصاءات.
 * 3. تطبيق أنماط التلوين التمييزي:
 *    - تلوين المبالغ الدائنة باللون الأحمر/المدين بالأخضر/المتزنة بالرمادي لتسهيل القراءة السريعة.
 * 4. إدارة التواريخ والأمان:
 *    - استخدام [ThreadLocal] لمفرقات التواريخ لضمان الأمان المتزامن في بيئات الكوروتين المتعددة.
 */
package com.example.data.serialization.excel

// ---------------------------------------------------------------------
// استيراد حزم سياق أندرويد والسجلات والواجهات والعمليات الحسابية والملفات
// ---------------------------------------------------------------------
import android.content.Context
import android.util.Log
import com.example.R
import com.example.data.serialization.BusinessProfileLoader
import com.example.ui.helper.HabayebMathHelper
import com.example.ui.state.CustomerUiState
import java.io.File
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * [الكائن الأحادي لمحرك إكسل الشامل - AllCustomersExcelEngine]:
 * يبني مصنف عمل إكسل يعرض كشفاً تفصيلياً بجميع حسابات العملاء وأرصدتهم.
 */
object AllCustomersExcelEngine {

    /** وسم السجلات التشخيصية */
    private const val TAG = "AllCustomersExcel"
    /** رمز اللغة العربية */
    private const val LOCALE_AR = "ar"
    /** رمز اللغة الإنجليزية */
    private const val LOCALE_EN = "en"
    /** بادئة اسم ملف التقرير الشامل */
    private const val FILE_PREFIX_ALL = "all_accounts_"

    /** منسق التاريخ الإنجليزي الآمن متعدد الخيوط */
    private val DATE_FORMATTER_EN = ThreadLocal.withInitial { SimpleDateFormat("yyyy/MM/dd", Locale(LOCALE_EN)) }
    /** منسق الوقت العربي الآمن متعدد الخيوط */
    private val TIME_FORMATTER_AR = ThreadLocal.withInitial { SimpleDateFormat("hh:mm a", Locale(LOCALE_AR)) }
    /** منسق اسم اليوم العربي الآمن متعدد الخيوط */
    private val DAY_FORMATTER_AR = ThreadLocal.withInitial { SimpleDateFormat("EEEE", Locale(LOCALE_AR)) }

    /**
     * [توليد ملف إكسل لجميع الحسابات - generate]:
     * يجمع الحسابات ويبني ورقة العمل والجداول وخلايا الدمج ثم ينشئ الملف في مجلد التخزين المؤقت.
     *
     * @param context سياق التطبيق لجلب النصوص والموارد.
     * @param customers قائمة العملاء مع حالات أرصدتهم.
     * @param currencySymbol رمز العملة المحلية الأساسية.
     * @return ملف الـ XLSX المتولد، أو null عند حدوث استثناء.
     */
    fun generate(
        context: Context,
        customers: List<CustomerUiState>,
        currencySymbol: String
    ): File? {
        val fileName = "${FILE_PREFIX_ALL}${System.currentTimeMillis() % 100000}.xlsx"
        val file = File(context.cacheDir, fileName)

        try {
            val bizHeader = BusinessProfileLoader.load(context)
            val now = Date()
            val dayName = try { DAY_FORMATTER_AR.get().format(now) } catch (e: Exception) { "" }
            val dateFormatted = try { DATE_FORMATTER_EN.get().format(now) } catch (e: Exception) { "" }
            val timeFormatted = try { TIME_FORMATTER_AR.get().format(now) } catch (e: Exception) { "" }
            val docDateText = "$dayName $dateFormatted"

            var totalOwedByThem = BigDecimal.ZERO
            var totalOwedToThem = BigDecimal.ZERO
            val foreignSumsMap = mutableMapOf<String, BigDecimal>()

            customers.forEach { c ->
                val bdVal = c.defaultCurrencyTotal
                if (bdVal.compareTo(BigDecimal.ZERO) > 0) {
                    totalOwedByThem = totalOwedByThem.add(bdVal)
                } else if (bdVal.compareTo(BigDecimal.ZERO) < 0) {
                    totalOwedToThem = totalOwedToThem.add(bdVal.abs())
                }
                c.foreignDebts.forEach { (curr, valBd) ->
                    if (valBd.compareTo(BigDecimal.ZERO) != 0) {
                        foreignSumsMap[curr] = (foreignSumsMap[curr] ?: BigDecimal.ZERO).add(valBd)
                    }
                }
            }
            val grandNetBalance = totalOwedByThem.subtract(totalOwedToThem)

            val columns = listOf(
                XlsxOpenXmlBuilder.SheetColumn(1, 1, 6.0),   // م
                XlsxOpenXmlBuilder.SheetColumn(2, 2, 38.0),  // الحساب / الهاتف
                XlsxOpenXmlBuilder.SheetColumn(3, 3, 22.0),  // الرصيد الأساسي
                XlsxOpenXmlBuilder.SheetColumn(4, 4, 24.0),  // العملات الأخرى
                XlsxOpenXmlBuilder.SheetColumn(5, 5, 18.0)   // الحالة
            )

            val rowsList = mutableListOf<XlsxOpenXmlBuilder.Row>()
            val mergesList = mutableListOf<XlsxOpenXmlBuilder.MergeRange>()

            // 1. Title Row
            val rTitle = XlsxOpenXmlBuilder.Row(1, ht = 32)
            rTitle.cell(0, context.getString(R.string.excel_all_title), 15)
            rowsList.add(rTitle)
            mergesList.add(XlsxOpenXmlBuilder.MergeRange("A1:E1"))

            // 2. Biz info
            val rBiz = XlsxOpenXmlBuilder.Row(2, ht = 22)
            rBiz.cell(0, bizHeader.displayedName + " - " + bizHeader.displayedDesc, 16)
            rBiz.cell(3, context.getString(R.string.excel_date_format, docDateText), 17)
            rowsList.add(rBiz)
            mergesList.add(XlsxOpenXmlBuilder.MergeRange("A2:C2"))
            mergesList.add(XlsxOpenXmlBuilder.MergeRange("D2:E2"))

            // 3. Phone / Certified Badge
            val rBizSub = XlsxOpenXmlBuilder.Row(3, ht = 22)
            rBizSub.cell(0, context.getString(R.string.excel_phone_format, bizHeader.phonesStr), 16)
            rBizSub.cell(3, "", 17)
            rowsList.add(rBizSub)
            mergesList.add(XlsxOpenXmlBuilder.MergeRange("A3:C3"))
            mergesList.add(XlsxOpenXmlBuilder.MergeRange("D3:E3"))

            // Spacer
            rowsList.add(XlsxOpenXmlBuilder.Row(4, ht = 12))

            // 4. Summary card overview
            val rStats = XlsxOpenXmlBuilder.Row(5, ht = 28)
            val statsText = context.getString(
                R.string.excel_all_stats_format,
                HabayebMathHelper.formatSmart(totalOwedByThem),
                currencySymbol,
                HabayebMathHelper.formatSmart(totalOwedToThem),
                HabayebMathHelper.formatSmart(grandNetBalance),
                customers.size
            )
            rStats.cell(0, statsText, 7)
            rowsList.add(rStats)
            mergesList.add(XlsxOpenXmlBuilder.MergeRange("A5:E5"))

            // Spacer
            rowsList.add(XlsxOpenXmlBuilder.Row(6, ht = 12))

            // 5. Table Header Row
            val rHeader = XlsxOpenXmlBuilder.Row(7, ht = 28)
            rHeader.cell(0, context.getString(R.string.excel_col_seq), 1)
            rHeader.cell(1, context.getString(R.string.pdf_col_account_name), 1)
            rHeader.cell(2, context.getString(R.string.pdf_col_primary_balance) + " ($currencySymbol)", 1)
            rHeader.cell(3, context.getString(R.string.pdf_col_other_currencies), 1)
            rHeader.cell(4, context.getString(R.string.pdf_col_status), 1)
            rowsList.add(rHeader)

            // 6. Customers Loop
            var rIdx = 8
            customers.forEachIndexed { index, c ->
                val bdVal = c.defaultCurrencyTotal
                val isPositive = bdVal.compareTo(BigDecimal.ZERO) > 0
                val isNegative = bdVal.compareTo(BigDecimal.ZERO) < 0

                val balanceStyle = when {
                    isPositive -> 2  // Red
                    isNegative -> 3  // Green
                    else -> 4        // Normal Gray/Center
                }

                val statusText = when {
                    isPositive -> context.getString(R.string.pdf_status_owed_word)
                    isNegative -> context.getString(R.string.pdf_status_to_him_word)
                    else -> context.getString(R.string.pdf_status_balanced_word)
                }

                val foreignList = c.foreignDebts.filter { it.value.compareTo(BigDecimal.ZERO) != 0 }
                val foreignStr = if (foreignList.isEmpty()) "-" else foreignList.entries.joinToString("  |  ") { (curr, bd) ->
                    val formatted = HabayebMathHelper.formatSmart(bd.abs())
                    val prefix = if (bd.compareTo(BigDecimal.ZERO) > 0) "+" else "-"
                    "$prefix$formatted $curr"
                }

                val phoneVal = c.phone.ifEmpty { "-" }
                val fullAccountText = context.getString(R.string.excel_account_phone_format, c.name, phoneVal)

                val rRow = XlsxOpenXmlBuilder.Row(rIdx, ht = 24)
                rRow.cell(0, index + 1, 6)
                rRow.cell(1, fullAccountText, 5) // bold text aligned right
                rRow.cell(2, bdVal.abs(), balanceStyle)
                rRow.cell(3, foreignStr, 6)
                rRow.cell(4, statusText, balanceStyle)
                rowsList.add(rRow)
                rIdx++
            }

            // Totals Row
            val rTotals = XlsxOpenXmlBuilder.Row(rIdx, ht = 28)
            rTotals.cell(0, context.getString(R.string.excel_totals_icon, context.getString(R.string.pdf_summary_independent_totals)), 11)
            rTotals.cell(2, grandNetBalance.abs(), 14)
            rTotals.cell(3, "-", 14)
            rTotals.cell(4, "-", 14)
            rowsList.add(rTotals)
            mergesList.add(XlsxOpenXmlBuilder.MergeRange("A$rIdx:B$rIdx"))
            rIdx++

            // Spacer
            rowsList.add(XlsxOpenXmlBuilder.Row(rIdx, ht = 16))
            rIdx++

            // Certified Signature Footer
            val rFooter = XlsxOpenXmlBuilder.Row(rIdx, ht = 24)
            rFooter.cell(0, context.getString(R.string.excel_footer_certified_icon, context.getString(R.string.pdf_footer_certified)), 17)
            rFooter.cell(3, context.getString(R.string.excel_footer_signature), 16)
            rowsList.add(rFooter)
            mergesList.add(XlsxOpenXmlBuilder.MergeRange("A$rIdx:C$rIdx"))
            mergesList.add(XlsxOpenXmlBuilder.MergeRange("D$rIdx:E$rIdx"))

            XlsxOpenXmlBuilder.buildXlsxFile(
                sheetName = context.getString(R.string.excel_sheet_all),
                columns = columns,
                rows = rowsList,
                merges = mergesList,
                file = file
            )
            return file
        } catch (e: Exception) {
            Log.e(TAG, "Error writing XLSX All Customers file", e)
            return null
        }
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
