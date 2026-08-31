/*
 * ╔══════════════════════════════════════════════════════════════════════╗
 * ║ التوثيق العربي الهندسي — MASTERCLASS DOCUMENTATION                 ║
 * ╚══════════════════════════════════════════════════════════════════════╝
 *
 * الملف: app/src/main/java/com/example/data/serialization/excel/SingleCustomerExcelEngine.kt
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
 * محرك إنشاء تقرير Excel تفصيلي لعميل واحد، بما في ذلك معاملاته وملخصه المالي.
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
// السطر 153: object SingleCustomerExcelEngine — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 156: private const val TAG — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 158: private const val LOCALE_AR — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 160: private const val LOCALE_EN — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 162: private const val FILE_PREFIX — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 165: private val DATE_FORMATTER_EN — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 167: private val TIME_FORMATTER_AR — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 169: private val DAY_FORMATTER_AR — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 182: fun generate — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 189: val sanitizedName — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 190: val fileName — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 191: val file — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 194: val bizHeader — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 195: val now — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 196: val dayName — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 197: val dateFormatted — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 198: val timeFormatted — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 199: val docDateText — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 201: val isOwedToThemAccount — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 202: val col4HeaderText — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 203: val col5HeaderText — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 204: val accountTypeDesc — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 206: val summary — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 208: val columns — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 217: val rowsList — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 218: val mergesList — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 221: val rTitle — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 227: val rBiz — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 235: val rBizSub — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 246: val rCard — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 247: val phoneText — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 248: val cardText — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 257: val rTableHeader — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 267: var rIdx — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 268: val sortedTxs — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 270: val rEmpty — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 276: var runningBal — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 278: val tx — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 279: val isTxForeign — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 280: val hasBaseAmount — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 282: val txType — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 283: val isCol4 — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 289: val col4Amount — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 290: val col5Amount — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 300: val txDate — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 301: val rowDay — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 302: val rowDate — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 303: val fullDateStr — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 305: val typeName — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 313: val cleanDetails — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 314: var descText — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 320: val origCurrency — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 321: val origAmount — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 322: val formattedAmount — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 323: val formattedRate — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 326: val origCurrency — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 327: val origAmount — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 328: val formattedAmount — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 332: val rRow — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 360: val rTotals — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 375: val rawPositive — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 376: val rawNegative — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 377: val isOwedToThemStatus — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 378: val isOwedByThemStatus — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 386: val formattedNetBalance — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 388: val rBanner — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 399: val rForeignHeader — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 406: val isPositive — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 407: val isNegative — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 408: val statusText — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 409: val foreignTag — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 410: val lineStr — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 412: val rForeignLine — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 424: val rFooter — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// --- نهاية الفهرس التوثيقي ---

/*
 * ╔══════════════════════════════════════════════════════════════════════╗
 * ║ التوثيق العربي الهندسي — MASTERCLASS DOCUMENTATION                 ║
 * ╚══════════════════════════════════════════════════════════════════════╝
 *
 * الملف: /mnt/data/source_full/app/src/main/java/com/example/data/serialization/excel/SingleCustomerExcelEngine.kt
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
 * محرك إنشاء تقرير Excel تفصيلي لعميل واحد، بما في ذلك معاملاته وملخصه المالي.
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
// السطر 46: object SingleCustomerExcelEngine — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 49: private const val TAG — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 51: private const val LOCALE_AR — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 53: private const val LOCALE_EN — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 55: private const val FILE_PREFIX — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 58: private val DATE_FORMATTER_EN — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 60: private val TIME_FORMATTER_AR — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 62: private val DAY_FORMATTER_AR — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 75: fun generate — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 82: val sanitizedName — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 83: val fileName — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 84: val file — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 87: val bizHeader — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 88: val now — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 89: val dayName — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 90: val dateFormatted — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 91: val timeFormatted — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 92: val docDateText — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 94: val isOwedToThemAccount — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 95: val col4HeaderText — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 96: val col5HeaderText — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 97: val accountTypeDesc — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 99: val summary — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 101: val columns — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 110: val rowsList — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 111: val mergesList — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 114: val rTitle — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 120: val rBiz — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 128: val rBizSub — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 139: val rCard — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 140: val phoneText — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 141: val cardText — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 150: val rTableHeader — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 160: var rIdx — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 161: val sortedTxs — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 163: val rEmpty — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 169: var runningBal — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 171: val tx — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 172: val isTxForeign — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 173: val hasBaseAmount — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 175: val txType — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 176: val isCol4 — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 182: val col4Amount — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 183: val col5Amount — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 193: val txDate — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 194: val rowDay — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 195: val rowDate — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 196: val fullDateStr — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 198: val typeName — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 206: val cleanDetails — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 207: var descText — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 213: val origCurrency — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 214: val origAmount — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 215: val formattedAmount — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 216: val formattedRate — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 219: val origCurrency — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 220: val origAmount — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 221: val formattedAmount — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 225: val rRow — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 253: val rTotals — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 268: val rawPositive — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 269: val rawNegative — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 270: val isOwedToThemStatus — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 271: val isOwedByThemStatus — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 279: val formattedNetBalance — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 281: val rBanner — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 292: val rForeignHeader — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 299: val isPositive — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 300: val isNegative — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 301: val statusText — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 302: val foreignTag — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 303: val lineStr — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 305: val rForeignLine — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 317: val rFooter — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// --- نهاية الفهرس التوثيقي ---

/**
 * =====================================================================
 * ملف: محرك جداول إكسل لكشف حساب العميل الفردي (SingleCustomerExcelEngine.kt)
 * =====================================================================
 * 
 * [الغرض العام والتعليمي من الملف]:
 * يمثل هذا الكائن المحرك المسؤول عن إنشاء كشف حساب مالي تفصيلي بصيغة OpenXML (.xlsx)
 * لعميل أو مورد محدد، مع دعم كامل للرصيد التراكمي المستمر (Running Balance)،
 * وعزل العملات الأجنبية غير المحولة، وتطبيق القواعد المحاسبية لجهة الحساب (لنا/له).
 * 
 * [المسؤوليات المعمارية والتقنية]:
 * 1. التكيف المحاسبي مع طبيعة الحساب (Account Nature Adaptation):
 *    - إذا كان الحساب "له" (مورد): يعكس مسميات الأعمدة (مدين/دائن) لتناسب التزامات المنشأة.
 * 2. الحساب التراكمي الآني للأرصدة (Running Balance Calculation):
 *    - تحديث الرصيد سطراً بسطر بدقة [BigDecimal] لمنع تراكم أخطاء الفاصلة العائمة.
 * 3. توضيح أسعار الصرف والمعاملات الأجنبية:
 *    - إضافة نصوص وصفية دقيقة للعملة الأصلية وسعر التحويل إن وجد.
 * 4. توليد خلايا وجداول وبطاقات إجمالية منسقة بالكامل:
 *    - بناء بطاقة تعريف العميل، جدول الحركات، بطاقة الصافي النهائي، والعملات غير المحولة.
 */
package com.example.data.serialization.excel

// ---------------------------------------------------------------------
// استيراد حزم سياق أندرويد والسجلات والكيانات والنماذج والحسابات والمساعدات
// ---------------------------------------------------------------------
import android.content.Context
import android.util.Log
import com.example.R
import com.example.data.local.entities.HabayebCustomer
import com.example.data.local.entities.HabayebTransaction
import com.example.data.serialization.BusinessProfileLoader
import com.example.data.serialization.pdf.PdfReportCalculator
import com.example.domain.model.TransactionType
import com.example.ui.helper.HabayebMathHelper
import com.example.ui.screens.habayeb.utils.CurrencyConfig
import java.io.File
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * [الكائن الأحادي لمحرك إكسل كشف الحساب الفردي - SingleCustomerExcelEngine]:
 * يولد ملف .xlsx مصمم هندسياً لعرض حركة ورصيد حساب شخص أو جهة واحدة.
 */
object SingleCustomerExcelEngine {

    /** وسم السجلات التشخيصية */
    private const val TAG = "SingleCustomerExcel"
    /** رمز اللغة العربية */
    private const val LOCALE_AR = "ar"
    /** رمز اللغة الإنجليزية */
    private const val LOCALE_EN = "en"
    /** بادئة اسم ملف كشف الحساب */
    private const val FILE_PREFIX = "statement_"

    /** منسق التاريخ الإنجليزي الآمن متعدد الخيوط */
    private val DATE_FORMATTER_EN = ThreadLocal.withInitial { SimpleDateFormat("yyyy/MM/dd", Locale(LOCALE_EN)) }
    /** منسق الوقت العربي الآمن متعدد الخيوط */
    private val TIME_FORMATTER_AR = ThreadLocal.withInitial { SimpleDateFormat("hh:mm a", Locale(LOCALE_AR)) }
    /** منسق اسم اليوم العربي الآمن متعدد الخيوط */
    private val DAY_FORMATTER_AR = ThreadLocal.withInitial { SimpleDateFormat("EEEE", Locale(LOCALE_AR)) }

    /**
     * [توليد كشف حساب إكسل للعميل - generate]:
     * يبني مصنف عمل إكسل كامل يضم بيانات المنشأة، بطاقة العميل، جدول الحركات، والرصيد النهائي.
     *
     * @param context سياق التطبيق لجلب النصوص والموارد.
     * @param customer بيانات بطاقة العميل المستهدف.
     * @param transactions قائمة معاملات العميل.
     * @param currencySymbol رمز العملة الأساسية للتطبيق.
     * @param exchangeRatesJson مصفوفة أسعار الصرف المخزنة (احتياطياً).
     * @return ملف הـ XLSX المتولد في التخزين المؤقت، أو null عند الفشل.
     */
    fun generate(
        context: Context,
        customer: HabayebCustomer,
        transactions: List<HabayebTransaction>,
        currencySymbol: String,
        exchangeRatesJson: String = "{}"
    ): File? {
        val sanitizedName = customer.name.replace("[\\\\/:*?\"<>|]".toRegex(), "_")
        val fileName = "${FILE_PREFIX}${sanitizedName}_${System.currentTimeMillis() % 100000}.xlsx"
        val file = File(context.cacheDir, fileName)

        try {
            val bizHeader = BusinessProfileLoader.load(context)
            val now = Date()
            val dayName = try { DAY_FORMATTER_AR.get().format(now) } catch (e: Exception) { "" }
            val dateFormatted = try { DATE_FORMATTER_EN.get().format(now) } catch (e: Exception) { "" }
            val timeFormatted = try { TIME_FORMATTER_AR.get().format(now) } catch (e: Exception) { "" }
            val docDateText = "$dayName $dateFormatted"

            val isOwedToThemAccount = customer.initialType == TransactionType.OWED_TO_THEM.value
            val col4HeaderText = if (isOwedToThemAccount) context.getString(R.string.pdf_col_owed_to) else context.getString(R.string.pdf_col_owed_by)
            val col5HeaderText = if (isOwedToThemAccount) context.getString(R.string.pdf_col_paid) else context.getString(R.string.pdf_col_received)
            val accountTypeDesc = if (isOwedToThemAccount) context.getString(R.string.excel_type_supplier) else context.getString(R.string.excel_type_customer)

            val summary = PdfReportCalculator.calculateSingleCustomerReport(transactions, currencySymbol)

            val columns = listOf(
                XlsxOpenXmlBuilder.SheetColumn(1, 1, 6.0),   // م (Sequence)
                XlsxOpenXmlBuilder.SheetColumn(2, 2, 16.0),  // التاريخ (Date)
                XlsxOpenXmlBuilder.SheetColumn(3, 3, 44.0),  // البيان والتفاصيل (Desc)
                XlsxOpenXmlBuilder.SheetColumn(4, 4, 16.0),  // مدين (Debit)
                XlsxOpenXmlBuilder.SheetColumn(5, 5, 16.0),  // دائن (Credit)
                XlsxOpenXmlBuilder.SheetColumn(6, 6, 18.0)   // الرصيد (Running balance)
            )

            val rowsList = mutableListOf<XlsxOpenXmlBuilder.Row>()
            val mergesList = mutableListOf<XlsxOpenXmlBuilder.MergeRange>()

            // 1. Header Row
            val rTitle = XlsxOpenXmlBuilder.Row(1, ht = 32)
            rTitle.cell(0, context.getString(R.string.excel_single_title), 15)
            rowsList.add(rTitle)
            mergesList.add(XlsxOpenXmlBuilder.MergeRange("A1:F1"))

            // 2. Biz Profile
            val rBiz = XlsxOpenXmlBuilder.Row(2, ht = 22)
            rBiz.cell(0, bizHeader.displayedName + " - " + bizHeader.displayedDesc, 16)
            rBiz.cell(3, context.getString(R.string.excel_date_format, docDateText), 17)
            rowsList.add(rBiz)
            mergesList.add(XlsxOpenXmlBuilder.MergeRange("A2:C2"))
            mergesList.add(XlsxOpenXmlBuilder.MergeRange("D2:F2"))

            // 3. Sub Biz / Meta
            val rBizSub = XlsxOpenXmlBuilder.Row(3, ht = 22)
            rBizSub.cell(0, context.getString(R.string.excel_phone_format, bizHeader.phonesStr), 16)
            rBizSub.cell(3, "", 17)
            rowsList.add(rBizSub)
            mergesList.add(XlsxOpenXmlBuilder.MergeRange("A3:C3"))
            mergesList.add(XlsxOpenXmlBuilder.MergeRange("D3:F3"))

            // Spacer
            rowsList.add(XlsxOpenXmlBuilder.Row(4, ht = 12))

            // 4. Customer Card Row
            val rCard = XlsxOpenXmlBuilder.Row(5, ht = 28)
            val phoneText = customer.phone.ifEmpty { context.getString(R.string.csv_not_registered) }
            val cardText = context.getString(R.string.excel_account_card_format, customer.name, phoneText, accountTypeDesc)
            rCard.cell(0, cardText, 7)
            rowsList.add(rCard)
            mergesList.add(XlsxOpenXmlBuilder.MergeRange("A5:F5"))

            // Spacer
            rowsList.add(XlsxOpenXmlBuilder.Row(6, ht = 12))

            // 5. Table Headers Row
            val rTableHeader = XlsxOpenXmlBuilder.Row(7, ht = 28)
            rTableHeader.cell(0, context.getString(R.string.excel_col_seq), 1)
            rTableHeader.cell(1, context.getString(R.string.pdf_col_date), 1)
            rTableHeader.cell(2, context.getString(R.string.pdf_col_description), 1)
            rTableHeader.cell(3, col4HeaderText, 1)
            rTableHeader.cell(4, col5HeaderText, 1)
            rTableHeader.cell(5, context.getString(R.string.pdf_col_remaining) + " ($currencySymbol)", 1)
            rowsList.add(rTableHeader)

            // 6. Transactions loop
            var rIdx = 8
            val sortedTxs = summary.sortedProcessedTxs
            if (sortedTxs.isEmpty()) {
                val rEmpty = XlsxOpenXmlBuilder.Row(rIdx, ht = 28)
                rEmpty.cell(0, context.getString(R.string.pdf_no_transactions), 6)
                rowsList.add(rEmpty)
                mergesList.add(XlsxOpenXmlBuilder.MergeRange("A$rIdx:F$rIdx"))
                rIdx++
            } else {
                var runningBal = BigDecimal.ZERO
                sortedTxs.forEachIndexed { index, pt ->
                    val tx = pt.tx
                    val isTxForeign = pt.isTxForeign
                    val hasBaseAmount = pt.baseCurrencyAmount.compareTo(BigDecimal.ZERO) > 0

                    val txType = TransactionType.fromValue(tx.type)
                    val isCol4 = if (isOwedToThemAccount) {
                        txType == TransactionType.OWED_TO_THEM || txType == TransactionType.PAYMENT_BY_THEM
                    } else {
                        txType == TransactionType.OWED_BY_THEM || txType == TransactionType.PAYMENT_TO_THEM
                    }

                    val col4Amount = if (hasBaseAmount && isCol4) pt.baseCurrencyAmount else BigDecimal.ZERO
                    val col5Amount = if (hasBaseAmount && !isCol4) pt.baseCurrencyAmount else BigDecimal.ZERO

                    if (hasBaseAmount) {
                        if (isCol4) {
                            runningBal = runningBal.add(pt.baseCurrencyAmount)
                        } else {
                            runningBal = runningBal.subtract(pt.baseCurrencyAmount)
                        }
                    }

                    val txDate = Date(if (tx.timestamp > 1000000000000L) tx.timestamp else tx.timestamp * 1000)
                    val rowDay = try { DAY_FORMATTER_AR.get().format(txDate) } catch (e: Exception) { "" }
                    val rowDate = try { DATE_FORMATTER_EN.get().format(txDate) } catch (e: Exception) { "" }
                    val fullDateStr = "$rowDay $rowDate"

                    val typeName = when (txType) {
                        TransactionType.OWED_BY_THEM -> context.getString(R.string.pdf_tx_type_owed_by_them)
                        TransactionType.PAYMENT_BY_THEM -> if (isOwedToThemAccount) context.getString(R.string.pdf_tx_type_payment_to_them) else context.getString(R.string.pdf_tx_type_payment_by_them)
                        TransactionType.OWED_TO_THEM -> context.getString(R.string.pdf_tx_type_owed_to_them)
                        TransactionType.PAYMENT_TO_THEM -> context.getString(R.string.pdf_tx_type_payment_to_them)
                        else -> context.getString(R.string.pdf_tx_type_new)
                    }

                    val cleanDetails = CurrencyConfig.getCleanDetails(tx.description)
                    var descText = typeName
                    if (cleanDetails.isNotBlank()) {
                        descText += " - $cleanDetails"
                    }

                    if (tx.isRateCalculated) {
                        val origCurrency = CurrencyConfig.getBySymbol(tx.currencyCode)?.symbol ?: tx.currencyCode
                        val origAmount = if (tx.foreignAmount.compareTo(BigDecimal.ZERO) > 0) tx.foreignAmount else tx.amount
                        val formattedAmount = HabayebMathHelper.formatSmart(origAmount)
                        val formattedRate = HabayebMathHelper.formatRate(tx.exchangeRate)
                        descText += context.getString(R.string.excel_tx_rate_note, formattedAmount, origCurrency, formattedRate)
                    } else if (isTxForeign) {
                        val origCurrency = CurrencyConfig.getBySymbol(tx.currencyCode)?.symbol ?: tx.currencyCode
                        val origAmount = if (tx.foreignAmount.compareTo(BigDecimal.ZERO) > 0) tx.foreignAmount else tx.amount
                        val formattedAmount = HabayebMathHelper.formatSmart(origAmount)
                        descText += context.getString(R.string.excel_tx_foreign_note, formattedAmount, origCurrency)
                    }

                    val rRow = XlsxOpenXmlBuilder.Row(rIdx, ht = 24)
                    rRow.cell(0, index + 1, 6)
                    rRow.cell(1, fullDateStr, 6)
                    rRow.cell(2, descText, 0)

                    if (col4Amount.compareTo(BigDecimal.ZERO) > 0) {
                        rRow.cell(3, col4Amount, 2)
                    } else {
                        rRow.cell(3, "-", 6)
                    }

                    if (col5Amount.compareTo(BigDecimal.ZERO) > 0) {
                        rRow.cell(4, col5Amount, 3)
                    } else {
                        rRow.cell(4, "-", 6)
                    }

                    if (hasBaseAmount) {
                        rRow.cell(5, runningBal, 4)
                    } else {
                        rRow.cell(5, "-", 6)
                    }

                    rowsList.add(rRow)
                    rIdx++
                }

                // Independent Totals
                val rTotals = XlsxOpenXmlBuilder.Row(rIdx, ht = 28)
                rTotals.cell(0, context.getString(R.string.excel_totals_icon, context.getString(R.string.pdf_summary_independent_totals)), 11)
                rTotals.cell(3, summary.totalDebts, 12)
                rTotals.cell(4, summary.totalPayments, 13)
                rTotals.cell(5, currencySymbol, 14)
                rowsList.add(rTotals)
                mergesList.add(XlsxOpenXmlBuilder.MergeRange("A$rIdx:C$rIdx"))
                rIdx++
            }

            // Spacer
            rowsList.add(XlsxOpenXmlBuilder.Row(rIdx, ht = 12))
            rIdx++

            // Net balance Banner
            val rawPositive = summary.calculatedNetDebt.compareTo(BigDecimal.ZERO) > 0
            val rawNegative = summary.calculatedNetDebt.compareTo(BigDecimal.ZERO) < 0
            val isOwedToThemStatus = if (isOwedToThemAccount) rawPositive else rawNegative
            val isOwedByThemStatus = if (isOwedToThemAccount) rawNegative else rawPositive

            val (bannerStyle, statusTitle) = when {
                isOwedByThemStatus -> 8 to context.getString(R.string.pdf_net_banner_owed_by)
                isOwedToThemStatus -> 9 to context.getString(R.string.pdf_net_banner_owed_to)
                else -> 10 to context.getString(R.string.pdf_net_banner_balanced)
            }

            val formattedNetBalance = "${HabayebMathHelper.formatSmart(summary.calculatedNetDebt.abs())} $currencySymbol"

            val rBanner = XlsxOpenXmlBuilder.Row(rIdx, ht = 28)
            rBanner.cell(0, context.getString(R.string.excel_net_banner_format, statusTitle, formattedNetBalance), bannerStyle)
            rowsList.add(rBanner)
            mergesList.add(XlsxOpenXmlBuilder.MergeRange("A$rIdx:F$rIdx"))
            rIdx++

            // Unconverted Foreign summary
            if (summary.uncalculatedForeignSums.isNotEmpty()) {
                rowsList.add(XlsxOpenXmlBuilder.Row(rIdx, ht = 12))
                rIdx++

                val rForeignHeader = XlsxOpenXmlBuilder.Row(rIdx, ht = 24)
                rForeignHeader.cell(0, context.getString(R.string.excel_foreign_icon, context.getString(R.string.pdf_independent_totals_uncalculated)), 19)
                rowsList.add(rForeignHeader)
                mergesList.add(XlsxOpenXmlBuilder.MergeRange("A$rIdx:F$rIdx"))
                rIdx++

                for ((curr, amount) in summary.uncalculatedForeignSums) {
                    val isPositive = amount.compareTo(BigDecimal.ZERO) > 0
                    val isNegative = amount.compareTo(BigDecimal.ZERO) < 0
                    val statusText = if (isPositive) context.getString(R.string.pdf_status_owed_word) else if (isNegative) context.getString(R.string.pdf_status_to_him_word) else context.getString(R.string.pdf_status_balanced_word)
                    val foreignTag = context.getString(R.string.pdf_foreign_currency_tag)
                    val lineStr = context.getString(R.string.excel_foreign_line_format, context.getString(R.string.pdf_total_currency_prefix, curr), HabayebMathHelper.formatSmart(amount.abs()), curr, statusText, foreignTag)

                    val rForeignLine = XlsxOpenXmlBuilder.Row(rIdx, ht = 22)
                    rForeignLine.cell(0, lineStr, 7)
                    rowsList.add(rForeignLine)
                    mergesList.add(XlsxOpenXmlBuilder.MergeRange("A$rIdx:F$rIdx"))
                    rIdx++
                }
            }

            // Certified Signature
            rowsList.add(XlsxOpenXmlBuilder.Row(rIdx, ht = 16))
            rIdx++

            val rFooter = XlsxOpenXmlBuilder.Row(rIdx, ht = 24)
            rFooter.cell(0, context.getString(R.string.excel_footer_certified_icon, context.getString(R.string.pdf_footer_certified)), 17)
            rFooter.cell(3, context.getString(R.string.excel_footer_signature), 16)
            rowsList.add(rFooter)
            mergesList.add(XlsxOpenXmlBuilder.MergeRange("A$rIdx:C$rIdx"))
            mergesList.add(XlsxOpenXmlBuilder.MergeRange("D$rIdx:F$rIdx"))

            XlsxOpenXmlBuilder.buildXlsxFile(
                sheetName = context.getString(R.string.excel_sheet_single),
                columns = columns,
                rows = rowsList,
                merges = mergesList,
                file = file
            )
            return file
        } catch (e: Exception) {
            Log.e(TAG, "Error writing XLSX statement file", e)
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
