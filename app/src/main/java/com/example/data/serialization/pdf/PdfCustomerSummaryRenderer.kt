/*
 * ╔══════════════════════════════════════════════════════════════════════╗
 * ║ التوثيق العربي الهندسي — MASTERCLASS DOCUMENTATION                 ║
 * ╚══════════════════════════════════════════════════════════════════════╝
 *
 * الملف: app/src/main/java/com/example/data/serialization/pdf/PdfCustomerSummaryRenderer.kt
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
 * مسؤول رسم ملخص العميل داخل صفحات PDF.
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
// السطر 132: object PdfCustomerSummaryRenderer — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 135: private val paintCardBg — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 140: private val paintCardBorder — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 156: fun calculateCustomerSummaryRowHeight — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 162: val nameHeight — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 163: val phoneHeight — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 164: val colNameTotal — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 166: val foreignList — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 167: val foreignStr — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 168: val formatted — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 169: val prefix — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 172: val foreignHeight — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 181: fun drawCustomerSummaryRow — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 190: val hasForeign — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 203: val textYOffset — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 209: val nameLayout — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 210: val nameTotalH — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 211: val nameYOffset — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 219: val totalBd — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 220: val isPositive — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 221: val isNegative — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 222: val formattedPrimary — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 223: val balancePaint — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 227: val foreignList — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 228: val foreignStr — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 232: val formatted — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 233: val prefix — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 237: val foreignLayout — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 238: val foreignYOffset — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 242: val statusStr — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 249: val statusPaint — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 257: fun drawBookletIndexHeader — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 258: val paintHeaderBg — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 262: val paintHeaderBorder — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 277: val paintHeaderText — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 294: fun calculateBookletIndexRowHeight — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 295: val nameHeight — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 303: fun drawBookletIndexRow — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 320: val textYOffset — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 326: val nameLayout — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 327: val nameYOffset — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 334: val balText — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 338: val statusStr — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 343: val statusColor — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 348: val paintStatus — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 361: fun drawComprehensiveSummaryCard — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 370: val nonZeroForeign — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 371: val cardHeight — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 372: val endY — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 376: val netPrimary — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 377: val netPrimaryFormatted — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 378: val netPrimaryStatus — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 386: val primarySummary — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 396: val paintMainSummary — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 407: val foreignSummary — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 408: val status — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 415: val paintForeignSummary — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// --- نهاية الفهرس التوثيقي ---

/*
 * ╔══════════════════════════════════════════════════════════════════════╗
 * ║ التوثيق العربي الهندسي — MASTERCLASS DOCUMENTATION                 ║
 * ╚══════════════════════════════════════════════════════════════════════╝
 *
 * الملف: /mnt/data/source_full/app/src/main/java/com/example/data/serialization/pdf/PdfCustomerSummaryRenderer.kt
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
 * مسؤول رسم ملخص العميل داخل صفحات PDF.
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
// السطر 42: object PdfCustomerSummaryRenderer — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 45: private val paintCardBg — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 50: private val paintCardBorder — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 66: fun calculateCustomerSummaryRowHeight — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 72: val nameHeight — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 73: val phoneHeight — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 74: val colNameTotal — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 76: val foreignList — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 77: val foreignStr — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 78: val formatted — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 79: val prefix — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 82: val foreignHeight — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 91: fun drawCustomerSummaryRow — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 100: val hasForeign — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 113: val textYOffset — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 119: val nameLayout — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 120: val nameTotalH — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 121: val nameYOffset — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 129: val totalBd — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 130: val isPositive — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 131: val isNegative — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 132: val formattedPrimary — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 133: val balancePaint — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 137: val foreignList — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 138: val foreignStr — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 142: val formatted — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 143: val prefix — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 147: val foreignLayout — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 148: val foreignYOffset — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 152: val statusStr — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 159: val statusPaint — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 167: fun drawBookletIndexHeader — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 168: val paintHeaderBg — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 172: val paintHeaderBorder — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 187: val paintHeaderText — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 204: fun calculateBookletIndexRowHeight — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 205: val nameHeight — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 213: fun drawBookletIndexRow — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 230: val textYOffset — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 236: val nameLayout — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 237: val nameYOffset — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 244: val balText — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 248: val statusStr — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 253: val statusColor — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 258: val paintStatus — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 271: fun drawComprehensiveSummaryCard — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 280: val nonZeroForeign — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 281: val cardHeight — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 282: val endY — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 286: val netPrimary — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 287: val netPrimaryFormatted — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 288: val netPrimaryStatus — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 296: val primarySummary — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 306: val paintMainSummary — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 317: val foreignSummary — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 318: val status — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 325: val paintForeignSummary — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// --- نهاية الفهرس التوثيقي ---

/**
 * =====================================================================
 * ملف: مكون رسم ملخصات العملاء في تقارير PDF (PdfCustomerSummaryRenderer.kt)
 * =====================================================================
 * 
 * [الغرض العام والتعليمي من الملف]:
 * يتولى هذا الكائن مسؤولية حساب مقاسات ورسم صفوف كشف أرصدة العملاء
 * (Customer Balances Directory)، وفهرس كتيب الحسابات (Booklet Index)،
 * وبطاقة الملخص الإجمالي الشامل للحسابات والعملات الأجنبية في مستندات PDF.
 * 
 * [المسؤوليات المعمارية والتقنية]:
 * 1. الحساب الديناميكي لارتفاع الصفوف (Dynamic Row Height Calculation):
 *    - قياس أطوال نصوص أسماء العملاء وأرقام هواتفهم وقوائم العملات الأجنبية لمنع تداخل النصوص.
 * 2. رسم خلايا الجداول والفواصل بدقة (Table Layout & Divider Drawing):
 *    - محاذاة الأعمدة (رقم متسلسل، الاسم ورقم الهاتف، الرصيد الأساسي، العملات الأجنبية، والحالة المحاسبية).
 * 3. تمييز حالات الأرصدة بالألوان والدلالات المحاسبية:
 *    - تطبيق ألوان متباينة للأرصدة المدينة والدائنة والمتزنة.
 * 4. رسم بطاقة الملخص الشامل (Comprehensive Summary Card):
 *    - عرض إجمالي ما لنا وما علينا، وصافي الحسابات الإجمالي، وملخص العملات الأجنبية غير الصفرية.
 */
package com.example.data.serialization.pdf

// ---------------------------------------------------------------------
// استيراد حزم سياق أندرويد والرسومات والخطوط وتخطيط النصوص والكيانات
// ---------------------------------------------------------------------
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.text.Layout
import com.example.R
import com.example.data.serialization.pdf.PdfDrawingUtils.drawArabicText
import com.example.ui.helper.HabayebMathHelper
import com.example.ui.state.CustomerUiState
import java.math.BigDecimal

/**
 * [الكائن الأحادي لرسم ملخصات العملاء - PdfCustomerSummaryRenderer]:
 * يقدم وظائف قياس ورسم صفوف الدليل وبطاقات التجميع في تقارير PDF.
 */
object PdfCustomerSummaryRenderer {

    /** فرشاة رسم خلفية بطاقات الملخصات */
    private val paintCardBg = Paint().apply {
        color = Color.parseColor(PdfColors.CARD_BG)
        style = Paint.Style.FILL
    }
    /** فرشاة رسم حدود وإطارات بطاقات الملخصات */
    private val paintCardBorder = Paint().apply {
        color = Color.parseColor(PdfColors.HEADER_BORDER)
        strokeWidth = 0.5f
        style = Paint.Style.STROKE
    }

    /**
     * [حساب ارتفاع صف ملخص العميل - calculateCustomerSummaryRowHeight]:
     * يقيس المساحة الرأسية المطلوبة للاسم والهاتف والعملات الأجنبية.
     *
     * @param context سياق التطبيق.
     * @param c كائن حالة واجهة العميل.
     * @param nameWidth العرض المتاح لعمود الاسم.
     * @param foreignWidth العرض المتاح لعمود العملات الأجنبية.
     * @return الارتفاع المناسب للصف بالنقاط.
     */
    fun calculateCustomerSummaryRowHeight(
        context: Context,
        c: CustomerUiState,
        nameWidth: Int = 175,
        foreignWidth: Int = 125
    ): Float {
        val nameHeight = PdfDrawingUtils.measureTextHeight(c.name, PdfPaints.paintCellBold, nameWidth)
        val phoneHeight = if (c.phone.isNotBlank()) 14 else 0
        val colNameTotal = nameHeight + phoneHeight

        val foreignList = c.foreignDebts.filter { it.value.compareTo(BigDecimal.ZERO) != 0 }
        val foreignStr = if (foreignList.isEmpty()) "-" else foreignList.entries.joinToString("\n") { (curr, bd) ->
            val formatted = HabayebMathHelper.formatSmart(bd.abs())
            val prefix = if (bd.compareTo(BigDecimal.ZERO) > 0) "+" else "-"
            "$prefix$formatted $curr"
        }
        val foreignHeight = PdfDrawingUtils.measureTextHeight(foreignStr, PdfPaints.paintCellNormal, foreignWidth)

        return maxOf(colNameTotal + 14f, foreignHeight + 14f, 34f)
    }

    /**
     * [رسم صف ملخص العميل في دليل الحسابات - drawCustomerSummaryRow]:
     * يرسم الخلايا الخمس للعميل مع الفواصل وخلفية العملات الأجنبية إن وجدت.
     */
    fun drawCustomerSummaryRow(
        canvas: Canvas,
        context: Context,
        index: Int,
        c: CustomerUiState,
        currentY: Float,
        rowHeight: Float,
        currencySymbol: String
    ) {
        val hasForeign = c.foreignDebts.any { it.value.compareTo(BigDecimal.ZERO) != 0 }
        if (hasForeign) {
            canvas.drawRect(25f, currentY, 570f, currentY + rowHeight, PdfPaints.paintForeignBg)
        }

        canvas.drawLine(25f, currentY + rowHeight, 570f, currentY + rowHeight, PdfPaints.paintRowDivider)

        // Vertical dividers
        canvas.drawLine(535f, currentY, 535f, currentY + rowHeight, PdfPaints.paintRowDivider)
        canvas.drawLine(360f, currentY, 360f, currentY + rowHeight, PdfPaints.paintRowDivider)
        canvas.drawLine(230f, currentY, 230f, currentY + rowHeight, PdfPaints.paintRowDivider)
        canvas.drawLine(105f, currentY, 105f, currentY + rowHeight, PdfPaints.paintRowDivider)

        val textYOffset = (rowHeight - 12f) / 2f

        // Col 1: Index
        drawArabicText(canvas, (index + 1).toString(), 535f, currentY + textYOffset, 35, PdfPaints.paintCellNormal, Layout.Alignment.ALIGN_CENTER)

        // Col 2: Name & Phone with Dynamic Layout
        val nameLayout = PdfDrawingUtils.createStaticLayout(c.name, PdfPaints.paintCellBold, 170, Layout.Alignment.ALIGN_NORMAL)
        val nameTotalH = nameLayout.height + if (c.phone.isNotBlank()) 14f else 0f
        val nameYOffset = ((rowHeight - nameTotalH) / 2f).coerceAtLeast(3f)

        PdfDrawingUtils.drawStaticLayout(canvas, nameLayout, 365f, currentY + nameYOffset)
        if (c.phone.isNotBlank()) {
            drawArabicText(canvas, c.phone, 365f, currentY + nameYOffset + nameLayout.height + 1f, 170, PdfPaints.paintMutedText, Layout.Alignment.ALIGN_NORMAL)
        }

        // Col 3: Primary Balance
        val totalBd = c.defaultCurrencyTotal
        val isPositive = totalBd.compareTo(BigDecimal.ZERO) > 0
        val isNegative = totalBd.compareTo(BigDecimal.ZERO) < 0
        val formattedPrimary = HabayebMathHelper.formatSmart(totalBd.abs()) + " " + currencySymbol
        val balancePaint = if (isPositive) PdfPaints.paintOwedText else if (isNegative) PdfPaints.paintPaymentText else PdfPaints.paintCellNormal
        drawArabicText(canvas, formattedPrimary, 230f, currentY + textYOffset, 130, balancePaint, Layout.Alignment.ALIGN_CENTER)

        // Col 4: Foreign Currencies with Dynamic Layout
        val foreignList = c.foreignDebts.filter { it.value.compareTo(BigDecimal.ZERO) != 0 }
        val foreignStr = if (foreignList.isEmpty()) {
            "-"
        } else {
            foreignList.entries.joinToString("\n") { (curr, bd) ->
                val formatted = HabayebMathHelper.formatSmart(bd.abs())
                val prefix = if (bd.compareTo(BigDecimal.ZERO) > 0) "+" else "-"
                "$prefix$formatted $curr"
            }
        }
        val foreignLayout = PdfDrawingUtils.createStaticLayout(foreignStr, PdfPaints.paintCellNormal, 120, Layout.Alignment.ALIGN_CENTER)
        val foreignYOffset = ((rowHeight - foreignLayout.height) / 2f).coerceAtLeast(3f)
        PdfDrawingUtils.drawStaticLayout(canvas, foreignLayout, 105f, currentY + foreignYOffset)

        // Col 5: Status
        val statusStr = if (isPositive) {
            context.getString(R.string.pdf_status_owed_word)
        } else if (isNegative) {
            context.getString(R.string.pdf_status_to_him_word)
        } else {
            context.getString(R.string.pdf_status_balanced_word)
        }
        val statusPaint = if (isPositive) PdfPaints.paintOwedText else if (isNegative) PdfPaints.paintPaymentText else PdfPaints.paintMutedText
        drawArabicText(canvas, statusStr, 25f, currentY + textYOffset, 80, statusPaint, Layout.Alignment.ALIGN_CENTER)
    }

    /**
     * [رسم ترويسة جدول فهرس الكتيب - drawBookletIndexHeader]:
     * يرسم شريط العناوين الداكن مع أسماء الأعمدة في مستند PDF.
     */
    fun drawBookletIndexHeader(canvas: Canvas, y: Float, context: Context) {
        val paintHeaderBg = Paint().apply {
            color = Color.parseColor(PdfColors.HEADER_BG)
            style = Paint.Style.FILL
        }
        val paintHeaderBorder = Paint().apply {
            color = Color.parseColor(PdfColors.HEADER_BORDER)
            strokeWidth = 0.5f
            style = Paint.Style.STROKE
        }
        canvas.drawRect(25f, y, 570f, y + 24f, paintHeaderBg)
        canvas.drawLine(25f, y, 570f, y, paintHeaderBorder)
        canvas.drawLine(25f, y + 24f, 570f, y + 24f, paintHeaderBorder)

        // Vertical dividers
        canvas.drawLine(535f, y, 535f, y + 24f, paintHeaderBorder)
        canvas.drawLine(305f, y, 305f, y + 24f, paintHeaderBorder)
        canvas.drawLine(205f, y, 205f, y + 24f, paintHeaderBorder)
        canvas.drawLine(105f, y, 105f, y + 24f, paintHeaderBorder)

        val paintHeaderText = Paint().apply {
            color = Color.parseColor(PdfColors.HEADER_TEXT)
            textSize = 9f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        drawArabicText(canvas, context.getString(R.string.pdf_col_m), 535f, y + 6f, 35, paintHeaderText, Layout.Alignment.ALIGN_CENTER)
        drawArabicText(canvas, context.getString(R.string.pdf_index_col_name), 305f, y + 6f, 230, paintHeaderText, Layout.Alignment.ALIGN_NORMAL)
        drawArabicText(canvas, context.getString(R.string.pdf_index_col_phone), 205f, y + 6f, 100, paintHeaderText, Layout.Alignment.ALIGN_NORMAL)
        drawArabicText(canvas, context.getString(R.string.pdf_index_col_balance), 105f, y + 6f, 100, paintHeaderText, Layout.Alignment.ALIGN_CENTER)
        drawArabicText(canvas, context.getString(R.string.pdf_index_col_status), 25f, y + 6f, 80, paintHeaderText, Layout.Alignment.ALIGN_CENTER)
    }

    /**
     * [حساب ارتفاع صف فهرس الكتيب - calculateBookletIndexRowHeight]:
     * يقيس ارتفاع صف العميل في فهرس الكتيب.
     */
    fun calculateBookletIndexRowHeight(customer: CustomerUiState, availableWidth: Int = 225): Float {
        val nameHeight = PdfDrawingUtils.measureTextHeight(customer.name, PdfPaints.paintCellBold, availableWidth)
        return (nameHeight + 10f).coerceAtLeast(24f)
    }

    /**
     * [رسم صف فهرس الكتيب - drawBookletIndexRow]:
     * يرسم بيانات العميل في فهرس الكتيب مع حالته ورصيده النهائي.
     */
    fun drawBookletIndexRow(
        canvas: Canvas,
        context: Context,
        index: Int,
        customer: CustomerUiState,
        currentY: Float,
        rowHeight: Float,
        currencySymbol: String
    ) {
        canvas.drawLine(25f, currentY + rowHeight, 570f, currentY + rowHeight, PdfPaints.paintRowDivider)

        // Vertical dividers
        canvas.drawLine(535f, currentY, 535f, currentY + rowHeight, PdfPaints.paintRowDivider)
        canvas.drawLine(305f, currentY, 305f, currentY + rowHeight, PdfPaints.paintRowDivider)
        canvas.drawLine(205f, currentY, 205f, currentY + rowHeight, PdfPaints.paintRowDivider)
        canvas.drawLine(105f, currentY, 105f, currentY + rowHeight, PdfPaints.paintRowDivider)

        val textYOffset = (rowHeight - 12f) / 2f

        // Column: No (م)
        drawArabicText(canvas, (index + 1).toString(), 535f, currentY + textYOffset, 35, PdfPaints.paintCellNormal, Layout.Alignment.ALIGN_CENTER)

        // Column: Name with Dynamic Layout
        val nameLayout = PdfDrawingUtils.createStaticLayout(customer.name, PdfPaints.paintCellBold, 225, Layout.Alignment.ALIGN_NORMAL)
        val nameYOffset = ((rowHeight - nameLayout.height) / 2f).coerceAtLeast(2f)
        PdfDrawingUtils.drawStaticLayout(canvas, nameLayout, 310f, currentY + nameYOffset)

        // Column: Phone
        drawArabicText(canvas, customer.phone.ifEmpty { "-" }, 205f, currentY + textYOffset, 100, PdfPaints.paintCellNormal, Layout.Alignment.ALIGN_NORMAL)

        // Column: Final Balance
        val balText = "${HabayebMathHelper.formatSmart(customer.defaultCurrencyTotal.abs())} $currencySymbol"
        drawArabicText(canvas, balText, 105f, currentY + textYOffset, 100, PdfPaints.paintCellBold, Layout.Alignment.ALIGN_CENTER)

        // Column: Status
        val statusStr = when {
            customer.defaultCurrencyTotal > BigDecimal.ZERO -> context.getString(R.string.pdf_status_for_us)
            customer.defaultCurrencyTotal < BigDecimal.ZERO -> context.getString(R.string.pdf_status_on_us)
            else -> context.getString(R.string.pdf_status_balanced)
        }
        val statusColor = when {
            customer.defaultCurrencyTotal > BigDecimal.ZERO -> PdfColors.PAYMENT_TEXT
            customer.defaultCurrencyTotal < BigDecimal.ZERO -> PdfColors.OWED_TEXT
            else -> PdfColors.TEXT_LIGHT
        }
        val paintStatus = Paint().apply {
            color = Color.parseColor(statusColor)
            textSize = 8.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        drawArabicText(canvas, statusStr, 25f, currentY + textYOffset, 80, paintStatus, Layout.Alignment.ALIGN_CENTER)
    }

    /**
     * [رسم بطاقة الملخص الإجمالي الشامل - drawComprehensiveSummaryCard]:
     * يرسم بطاقة مستديرة الحواف تعرض الأرصدة المجمعة والعملات الأجنبية.
     */
    fun drawComprehensiveSummaryCard(
        canvas: Canvas,
        context: Context,
        primaryColorHex: String,
        summary: ComprehensivePdfSummary,
        totalItems: Int,
        currencySymbol: String,
        startY: Float = 98f
    ) {
        val nonZeroForeign = summary.foreignTotalsMap.filter { it.value.compareTo(BigDecimal.ZERO) != 0 }
        val cardHeight = if (nonZeroForeign.isNotEmpty()) 54f else 46f
        val endY = startY + cardHeight
        canvas.drawRoundRect(25f, startY, 570f, endY, 6f, 6f, paintCardBg)
        canvas.drawRoundRect(25f, startY, 570f, endY, 6f, 6f, paintCardBorder)

        val netPrimary = summary.netPrimary
        val netPrimaryFormatted = HabayebMathHelper.formatSmart(netPrimary.abs()) + " " + currencySymbol
        val netPrimaryStatus = if (netPrimary.compareTo(BigDecimal.ZERO) > 0) {
            context.getString(R.string.pdf_status_for_us)
        } else if (netPrimary.compareTo(BigDecimal.ZERO) < 0) {
            context.getString(R.string.pdf_status_on_us)
        } else {
            context.getString(R.string.pdf_status_balanced_word)
        }

        val primarySummary = context.getString(
            R.string.pdf_comprehensive_accounts_summary,
            totalItems,
            currencySymbol,
            HabayebMathHelper.formatSmart(summary.totalOwedByThem),
            HabayebMathHelper.formatSmart(summary.totalOwedToThem),
            netPrimaryFormatted,
            netPrimaryStatus
        )

        val paintMainSummary = Paint().apply {
            color = Color.parseColor(PdfColors.TEXT_CHARCOAL)
            textSize = 9.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        if (nonZeroForeign.isEmpty()) {
            drawArabicText(canvas, primarySummary, 30f, startY + 16f, 535, paintMainSummary, Layout.Alignment.ALIGN_CENTER)
        } else {
            drawArabicText(canvas, primarySummary, 30f, startY + 10f, 535, paintMainSummary, Layout.Alignment.ALIGN_CENTER)
            val foreignSummary = context.getString(R.string.pdf_other_currencies_balances) + " " + nonZeroForeign.entries.joinToString("   |   ") { (curr, bd) ->
                val status = if (bd.compareTo(BigDecimal.ZERO) > 0) {
                    context.getString(R.string.pdf_status_for_us)
                } else {
                    context.getString(R.string.pdf_status_on_us)
                }
                "$curr: " + HabayebMathHelper.formatSmart(bd.abs()) + " ($status)"
            }
            val paintForeignSummary = Paint().apply {
                color = Color.parseColor(primaryColorHex)
                textSize = 9f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }
            drawArabicText(canvas, foreignSummary, 30f, startY + 30f, 535, paintForeignSummary, Layout.Alignment.ALIGN_CENTER)
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
