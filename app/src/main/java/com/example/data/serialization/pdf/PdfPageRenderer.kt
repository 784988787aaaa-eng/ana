/*
 * ╔══════════════════════════════════════════════════════════════════════╗
 * ║ التوثيق العربي الهندسي — MASTERCLASS DOCUMENTATION                 ║
 * ╚══════════════════════════════════════════════════════════════════════╝
 *
 * الملف: app/src/main/java/com/example/data/serialization/pdf/PdfPageRenderer.kt
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
 * المكوّن الذي يرسم الصفحة الفعلية ويجمع عناصر التقرير ضمن مساحة الصفحة.
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
// السطر 153: data class PdfBusinessHeaderData — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 154: val displayedName — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 155: val displayedDesc — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 156: val phonesStr — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 157: val hasLogo — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 158: val scaledLogo — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 159: val logoW — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 160: val logoH — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 161: val docDateText — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 162: val docTimeText — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 169: object PdfPageRenderer — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 171: private val dayFormatAr — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 172: private val dateFormatEn — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 173: private val timeFormatAr — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 175: private val paintHeaderBg — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 179: private val paintHeaderBorder — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 184: private val paintHeaderText — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 190: private val paintAllCustomersHeaderText — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 196: private val paintMiniLine — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 201: private val paintMiniHeader — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 206: private val paintFooterText — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 212: private val paintBizName — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 218: private val paintBizDesc — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 224: private val paintBizPhones — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 230: private val paintLeft1 — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 236: private val paintLeft2 — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 242: private val paintDivider — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 250: fun formatDayAr — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 254: fun formatDateEn — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 258: fun formatTimeAr — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 264: fun drawTableHeader — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 276: val isOwedToThem — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 277: val col4Text — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 278: val col5Text — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 292: fun drawSubsequentPageHeader — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 294: val miniHeaderText — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 303: fun drawFooter — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 304: val footerTextLeft — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 305: val footerTextRight — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 314: fun drawBusinessHeader — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 333: fun drawBusinessHeader — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 345: val rightColX — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 346: val maxColWidth — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 348: val namePaint — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 349: var nameSize — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 357: val descPaint — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 358: var descSize — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 366: val phonePaint — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 367: var phoneSize — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 376: val logoX — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 377: val logoY — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 390: fun drawAllCustomersTableHeader — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 409: fun drawSingleTransactionRow — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 419: fun drawCustomerSummaryRow — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 429: fun drawComprehensiveSummaryCard — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 442: fun drawCustomerStatementSheet — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 454: var workingY — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 455: var currentCanvas — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 469: val bannerBg — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 474: val accentBar — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 481: val paintBannerText — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 487: val bannerText — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 504: val sortedTxs — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 511: val paintEmptyText — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 530: var runningBal — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 531: var totalCol4 — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 532: var totalCol5 — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 533: val isOwedToThemAccount — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 536: val tx — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 538: val isCol4 — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 552: val calculatedHeight — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 580: val extraSummaryHeight — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 597: val netBalance — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// --- نهاية الفهرس التوثيقي ---

/*
 * ╔══════════════════════════════════════════════════════════════════════╗
 * ║ التوثيق العربي الهندسي — MASTERCLASS DOCUMENTATION                 ║
 * ╚══════════════════════════════════════════════════════════════════════╝
 *
 * الملف: /mnt/data/source_full/app/src/main/java/com/example/data/serialization/pdf/PdfPageRenderer.kt
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
 * المكوّن الذي يرسم الصفحة الفعلية ويجمع عناصر التقرير ضمن مساحة الصفحة.
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
// السطر 47: data class PdfBusinessHeaderData — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 48: val displayedName — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 49: val displayedDesc — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 50: val phonesStr — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 51: val hasLogo — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 52: val scaledLogo — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 53: val logoW — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 54: val logoH — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 55: val docDateText — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 56: val docTimeText — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 63: object PdfPageRenderer — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 65: private val dayFormatAr — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 66: private val dateFormatEn — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 67: private val timeFormatAr — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 69: private val paintHeaderBg — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 73: private val paintHeaderBorder — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 78: private val paintHeaderText — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 84: private val paintAllCustomersHeaderText — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 90: private val paintMiniLine — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 95: private val paintMiniHeader — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 100: private val paintFooterText — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 106: private val paintBizName — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 112: private val paintBizDesc — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 118: private val paintBizPhones — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 124: private val paintLeft1 — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 130: private val paintLeft2 — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 136: private val paintDivider — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 144: fun formatDayAr — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 148: fun formatDateEn — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 152: fun formatTimeAr — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 158: fun drawTableHeader — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 170: val isOwedToThem — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 171: val col4Text — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 172: val col5Text — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 186: fun drawSubsequentPageHeader — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 188: val miniHeaderText — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 197: fun drawFooter — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 198: val footerTextLeft — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 199: val footerTextRight — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 208: fun drawBusinessHeader — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 227: fun drawBusinessHeader — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 239: val rightColX — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 240: val maxColWidth — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 242: val namePaint — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 243: var nameSize — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 251: val descPaint — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 252: var descSize — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 260: val phonePaint — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 261: var phoneSize — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 270: val logoX — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 271: val logoY — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 284: fun drawAllCustomersTableHeader — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 303: fun drawSingleTransactionRow — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 313: fun drawCustomerSummaryRow — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 323: fun drawComprehensiveSummaryCard — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 336: fun drawCustomerStatementSheet — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 348: var workingY — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 349: var currentCanvas — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 363: val bannerBg — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 368: val accentBar — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 375: val paintBannerText — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 381: val bannerText — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 398: val sortedTxs — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 405: val paintEmptyText — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 424: var runningBal — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 425: var totalCol4 — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 426: var totalCol5 — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 427: val isOwedToThemAccount — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 430: val tx — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 432: val isCol4 — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 446: val calculatedHeight — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 474: val extraSummaryHeight — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 491: val netBalance — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// --- نهاية الفهرس التوثيقي ---

/**
 * =====================================================================
 * ملف: مكون رسم الصفحات والترويسات والتذييلات في PDF (PdfPageRenderer.kt)
 * =====================================================================
 * 
 * [الغرض العام والتعليمي من الملف]:
 * يمثل هذا الكائن العمود الفقري لتنسيق صفحات PDF في التطبيق، حيث يحتوي
 * على آليات رسم ترويسة المنشأة الرسمية (مع الشعار ومعلومات الاتصال والتاريخ)،
 * وترويسات الجداول (Table Headers)، وتذييل الصفحات مع أرقام الصفحات (Footers)،
 * وترويسات الصفحات الفرعية المتتابعة، وبناء ورقة كشف حساب العميل الكاملة [drawCustomerStatementSheet].
 * 
 * [المسؤوليات المعمارية والتقنية]:
 * 1. تنسيق التواريخ والأوقات باللغة العربية (Locale-Aware Formatting):
 *    - دوال منسقة متزامنة [formatDayAr], [formatDateEn], [formatTimeAr].
 * 2. الضبط الديناميكي لحجم الخطوط (Adaptive Font Sizing):
 *    - تصغير حجم الخط آلياً عند زيادة طول اسم المنشأة أو شعارها اللفظي لمنع تجاوز الهوامش.
 * 3. إدارة فواصل الصفحات الذكية (Pagination & Page Breaks):
 *    - حساب الارتفاع المستهلك مع تنبيه رد النداء [onPageBreakNeeded] لنقل الرسام للصفحة التالية وإعادة رسم الترويسات.
 * 4. تجميع كشوفات الحسابات وتدفق المعاملات والصفوف الختامية:
 *    - دمج شريط تعريف العميل، جدول المعاملات، صف الإجماليات، وشريط الصافي النهائي.
 */
package com.example.data.serialization.pdf

// ---------------------------------------------------------------------
// استيراد حزم سياق أندرويد والرسومات وتنسيق التواريخ والنصوص والكيانات
// ---------------------------------------------------------------------
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.text.Layout
import com.example.R
import com.example.data.serialization.pdf.PdfDrawingUtils.drawArabicText
import com.example.domain.model.TransactionType
import com.example.ui.state.CustomerUiState
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * [وعاء بيانات ترويسة المنشأة في تقارير PDF - PdfBusinessHeaderData]:
 * يجمع البيانات النصية والبصرية وتواريخ التقرير المنسقة.
 */
data class PdfBusinessHeaderData(
    val displayedName: String,
    val displayedDesc: String,
    val phonesStr: String,
    val hasLogo: Boolean,
    val scaledLogo: Bitmap?,
    val logoW: Float,
    val logoH: Float,
    val docDateText: String,
    val docTimeText: String
)

/**
 * [الكائن الأحادي لرسم وتخطيط صفحات PDF - PdfPageRenderer]:
 * يقدم وظائف رسم الترويسات وتخطيط الجداول وتذييلات الصفحات.
 */
object PdfPageRenderer {

    private val dayFormatAr = SimpleDateFormat("EEEE", Locale("ar"))
    private val dateFormatEn = SimpleDateFormat("yyyy/MM/dd", Locale.ENGLISH)
    private val timeFormatAr = SimpleDateFormat("hh:mm a", Locale("ar"))

    private val paintHeaderBg = Paint().apply {
        color = Color.parseColor(PdfColors.HEADER_BG)
        style = Paint.Style.FILL
    }
    private val paintHeaderBorder = Paint().apply {
        color = Color.parseColor(PdfColors.HEADER_BORDER)
        strokeWidth = 0.5f
        style = Paint.Style.STROKE
    }
    private val paintHeaderText = Paint().apply {
        color = Color.parseColor(PdfColors.HEADER_TEXT)
        textSize = 10f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        isAntiAlias = true
    }
    private val paintAllCustomersHeaderText = Paint().apply {
        color = Color.parseColor(PdfColors.HEADER_TEXT)
        textSize = 9.5f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        isAntiAlias = true
    }
    private val paintMiniLine = Paint().apply {
        color = Color.parseColor(PdfColors.HEADER_BORDER)
        strokeWidth = 0.5f
        style = Paint.Style.STROKE
    }
    private val paintMiniHeader = Paint().apply {
        textSize = 9.5f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        isAntiAlias = true
    }
    private val paintFooterText = Paint().apply {
        color = Color.parseColor(PdfColors.TEXT_MUTED_GREY)
        textSize = 8.5f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
        isAntiAlias = true
    }
    private val paintBizName = Paint().apply {
        color = Color.parseColor(PdfColors.TEXT_DARK)
        textSize = 14.5f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        isAntiAlias = true
    }
    private val paintBizDesc = Paint().apply {
        color = Color.parseColor(PdfColors.TEXT_MEDIUM)
        textSize = 9.5f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        isAntiAlias = true
    }
    private val paintBizPhones = Paint().apply {
        color = Color.parseColor(PdfColors.TEXT_LIGHT)
        textSize = 8.5f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        isAntiAlias = true
    }
    private val paintLeft1 = Paint().apply {
        color = Color.parseColor(PdfColors.TEXT_MEDIUM)
        textSize = 9f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        isAntiAlias = true
    }
    private val paintLeft2 = Paint().apply {
        color = Color.parseColor(PdfColors.TEXT_LIGHT)
        textSize = 8.5f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        isAntiAlias = true
    }
    private val paintDivider = Paint().apply {
        color = Color.parseColor(PdfColors.HEADER_BORDER)
        strokeWidth = 1f
        style = Paint.Style.STROKE
    }

    /** تنسيق اسم اليوم باللغة العربية */
    @Synchronized
    fun formatDayAr(date: Date): String = dayFormatAr.format(date)

    /** تنسيق التاريخ الميلادي بصيغة yyyy/MM/dd */
    @Synchronized
    fun formatDateEn(date: Date): String = dateFormatEn.format(date)

    /** تنسيق الوقت بالساعة والدقيقة صباحاً/مساءً بالعربية */
    @Synchronized
    fun formatTimeAr(date: Date): String = timeFormatAr.format(date)

    /**
     * [رسم ترويسة جدول كشف الحساب - drawTableHeader]:
     * يرسم رؤوس الأعمدة الستة لكشف حساب العميل وفق طبيعة حسابه (لنا أو علينا).
     */
    fun drawTableHeader(canvas: Canvas, y: Float, context: Context, initialType: String = TransactionType.OWED_BY_THEM.value) {
        canvas.drawRect(25f, y, 570f, y + 25f, paintHeaderBg)
        canvas.drawLine(25f, y, 570f, y, paintHeaderBorder)
        canvas.drawLine(25f, y + 25f, 570f, y + 25f, paintHeaderBorder)

        // Draw vertical grid lines in table header
        canvas.drawLine(545f, y, 545f, y + 25f, paintHeaderBorder)
        canvas.drawLine(455f, y, 455f, y + 25f, paintHeaderBorder)
        canvas.drawLine(260f, y, 260f, y + 25f, paintHeaderBorder)
        canvas.drawLine(180f, y, 180f, y + 25f, paintHeaderBorder)
        canvas.drawLine(100f, y, 100f, y + 25f, paintHeaderBorder)

        val isOwedToThem = initialType == TransactionType.OWED_TO_THEM.value
        val col4Text = if (isOwedToThem) context.getString(R.string.pdf_col_owed_to) else context.getString(R.string.pdf_col_owed_by)
        val col5Text = if (isOwedToThem) context.getString(R.string.pdf_col_paid) else context.getString(R.string.pdf_col_received)

        drawArabicText(canvas, context.getString(R.string.pdf_col_m), 545f, y + 6f, 25, paintHeaderText, Layout.Alignment.ALIGN_CENTER)
        drawArabicText(canvas, context.getString(R.string.pdf_col_date), 455f, y + 6f, 90, paintHeaderText, Layout.Alignment.ALIGN_CENTER)
        drawArabicText(canvas, context.getString(R.string.pdf_col_description), 260f, y + 6f, 195, paintHeaderText, Layout.Alignment.ALIGN_NORMAL)
        drawArabicText(canvas, col4Text, 180f, y + 6f, 80, paintHeaderText, Layout.Alignment.ALIGN_CENTER)
        drawArabicText(canvas, col5Text, 100f, y + 6f, 80, paintHeaderText, Layout.Alignment.ALIGN_CENTER)
        drawArabicText(canvas, context.getString(R.string.pdf_col_remaining), 25f, y + 6f, 75, paintHeaderText, Layout.Alignment.ALIGN_CENTER)
    }

    /**
     * [رسم ترويسة مصغرة للصفحات اللاحقة - drawSubsequentPageHeader]:
     * يضع شريطاً رفيعاً باسم العميل في أعلى الصفحات الإضافية من كشف حسابه.
     */
    fun drawSubsequentPageHeader(canvas: Canvas, customerName: String, primaryColorHex: String, context: Context) {
        paintMiniHeader.color = Color.parseColor(primaryColorHex)
        val miniHeaderText = context.getString(R.string.pdf_mini_header_text, customerName)
        drawArabicText(canvas, miniHeaderText, 25f, 16f, 545, paintMiniHeader, Layout.Alignment.ALIGN_CENTER)
        canvas.drawLine(25f, 28f, 570f, 28f, paintMiniLine)
    }

    /**
     * [رسم تذييل الصفحة - drawFooter]:
     * يضع رقم الصفحة وإجمالي الصفحات مع عبارة التوثيق المعتمد أسفل الصفحة.
     */
    fun drawFooter(canvas: Canvas, pageNum: Int, totalPages: Int, primaryColorHex: String, context: Context) {
        val footerTextLeft = context.getString(R.string.pdf_footer_page, pageNum, totalPages)
        val footerTextRight = context.getString(R.string.pdf_footer_certified)

        drawArabicText(canvas, footerTextLeft, 25f, 815f, 150, paintFooterText, Layout.Alignment.ALIGN_OPPOSITE)
        drawArabicText(canvas, footerTextRight, 200f, 815f, 370, paintFooterText, Layout.Alignment.ALIGN_NORMAL)
    }

    /**
     * [رسم ترويسة المنشأة الكاملة - drawBusinessHeader]:
     */
    fun drawBusinessHeader(canvas: Canvas, headerData: PdfBusinessHeaderData) {
        drawBusinessHeader(
            canvas = canvas,
            displayedName = headerData.displayedName,
            displayedDesc = headerData.displayedDesc,
            phonesStr = headerData.phonesStr,
            hasLogo = headerData.hasLogo,
            scaledLogo = headerData.scaledLogo,
            logoW = headerData.logoW,
            logoH = headerData.logoH,
            docDateText = headerData.docDateText,
            docTimeText = headerData.docTimeText
        )
    }

    /**
     * [رسم ترويسة المنشأة بالمعاملات المباشرة - drawBusinessHeader]:
     * يرسم اسم المنشأة والوصف وأرقام الهواتف على اليمين، والشعار بالوسط، والتاريخ والوقت على اليسار.
     */
    fun drawBusinessHeader(
        canvas: Canvas,
        displayedName: String,
        displayedDesc: String,
        phonesStr: String,
        hasLogo: Boolean,
        scaledLogo: Bitmap?,
        logoW: Float,
        logoH: Float,
        docDateText: String,
        docTimeText: String
    ) {
        val rightColX = 360f
        val maxColWidth = 210f

        val namePaint = Paint(paintBizName)
        var nameSize = 14.5f
        namePaint.textSize = nameSize
        while (namePaint.measureText(displayedName) > maxColWidth && nameSize > 9.0f) {
            nameSize -= 0.4f
            namePaint.textSize = nameSize
        }
        drawArabicText(canvas, displayedName, rightColX, 20f, 210, namePaint, Layout.Alignment.ALIGN_NORMAL)

        val descPaint = Paint(paintBizDesc)
        var descSize = 9.5f
        descPaint.textSize = descSize
        while (descPaint.measureText(displayedDesc) > maxColWidth && descSize > 6.0f) {
            descSize -= 0.3f
            descPaint.textSize = descSize
        }
        drawArabicText(canvas, displayedDesc, rightColX, 38f, 210, descPaint, Layout.Alignment.ALIGN_NORMAL)

        val phonePaint = Paint(paintBizPhones)
        var phoneSize = 8.5f
        phonePaint.textSize = phoneSize
        while (phonePaint.measureText(phonesStr) > maxColWidth && phoneSize > 5.0f) {
            phoneSize -= 0.3f
            phonePaint.textSize = phoneSize
        }
        drawArabicText(canvas, phonesStr, rightColX, 52f, 210, phonePaint, Layout.Alignment.ALIGN_NORMAL)

        if (hasLogo && scaledLogo != null) {
            val logoX = 297.5f - (logoW / 2f)
            val logoY = 20f + ((45f - logoH) / 2f)
            canvas.drawBitmap(scaledLogo, logoX, logoY, null)
        }

        drawArabicText(canvas, docDateText, 25f, 22f, 180, paintLeft1, Layout.Alignment.ALIGN_OPPOSITE)
        drawArabicText(canvas, docTimeText, 25f, 36f, 180, paintLeft2, Layout.Alignment.ALIGN_OPPOSITE)

        canvas.drawLine(25f, 68f, 570f, 68f, paintDivider)
    }

    /**
     * [رسم ترويسة جدول كشف كافة العملاء - drawAllCustomersTableHeader]:
     */
    fun drawAllCustomersTableHeader(canvas: Canvas, y: Float, context: Context) {
        canvas.drawRect(25f, y, 570f, y + 26f, paintHeaderBg)

        canvas.drawLine(25f, y, 570f, y, paintHeaderBorder)
        canvas.drawLine(25f, y + 26f, 570f, y + 26f, paintHeaderBorder)

        // Vertical dividers
        canvas.drawLine(535f, y, 535f, y + 26f, paintHeaderBorder)
        canvas.drawLine(360f, y, 360f, y + 26f, paintHeaderBorder)
        canvas.drawLine(230f, y, 230f, y + 26f, paintHeaderBorder)
        canvas.drawLine(105f, y, 105f, y + 26f, paintHeaderBorder)

        drawArabicText(canvas, context.getString(R.string.pdf_col_m), 535f, y + 7f, 35, paintAllCustomersHeaderText, Layout.Alignment.ALIGN_CENTER)
        drawArabicText(canvas, context.getString(R.string.pdf_col_account_name), 360f, y + 7f, 175, paintAllCustomersHeaderText, Layout.Alignment.ALIGN_NORMAL)
        drawArabicText(canvas, context.getString(R.string.pdf_col_primary_balance), 230f, y + 7f, 130, paintAllCustomersHeaderText, Layout.Alignment.ALIGN_CENTER)
        drawArabicText(canvas, context.getString(R.string.pdf_col_other_currencies), 105f, y + 7f, 125, paintAllCustomersHeaderText, Layout.Alignment.ALIGN_CENTER)
        drawArabicText(canvas, context.getString(R.string.pdf_col_status), 25f, y + 7f, 80, paintAllCustomersHeaderText, Layout.Alignment.ALIGN_CENTER)
    }

    fun drawSingleTransactionRow(
        canvas: Canvas,
        context: Context,
        index: Int,
        pt: ProcessedTransaction,
        currentY: Float,
        rowHeight: Float,
        runningBal: BigDecimal
    ) = PdfRowRenderer.drawSingleTransactionRow(canvas, context, index, pt, currentY, rowHeight, runningBal)

    fun drawCustomerSummaryRow(
        canvas: Canvas,
        context: Context,
        index: Int,
        c: CustomerUiState,
        currentY: Float,
        rowHeight: Float,
        currencySymbol: String
    ) = PdfRowRenderer.drawCustomerSummaryRow(canvas, context, index, c, currentY, rowHeight, currencySymbol)

    fun drawComprehensiveSummaryCard(
        canvas: Canvas,
        context: Context,
        primaryColorHex: String,
        summary: ComprehensivePdfSummary,
        totalItems: Int,
        currencySymbol: String
    ) = PdfRowRenderer.drawComprehensiveSummaryCard(canvas, context, primaryColorHex, summary, totalItems, currencySymbol)

    /**
     * [رسم ورقة كشف حساب العميل التفصيلية - drawCustomerStatementSheet]:
     * يرسم شريط العميل، جدول المعاملات، الإجماليات، وشريط الصافي، مع تدوير الصفحات تلقائياً عند الامتلاء.
     */
    fun drawCustomerStatementSheet(
        canvas: Canvas?,
        context: Context,
        customer: CustomerUiState,
        summary: SingleCustomerPdfSummary,
        startY: Float,
        primaryColorHex: String,
        currencySymbol: String,
        isDryRun: Boolean = false,
        includeCustomerHeaderBanner: Boolean = true,
        onPageBreakNeeded: ((newHeader: Boolean) -> Canvas?)? = null
    ): Float {
        var workingY = startY
        var currentCanvas = canvas

        // 1. Customer Header Banner (Optional)
        if (includeCustomerHeaderBanner) {
            if (workingY > 42f) {
                if (workingY + 100f > 780f) {
                    currentCanvas = onPageBreakNeeded?.invoke(false) ?: currentCanvas
                    workingY = 42f
                } else {
                    workingY += 15f
                }
            }

            if (!isDryRun && currentCanvas != null) {
                val bannerBg = Paint().apply {
                    color = Color.parseColor(primaryColorHex)
                    alpha = 15
                    style = Paint.Style.FILL
                }
                val accentBar = Paint().apply {
                    color = Color.parseColor(primaryColorHex)
                    style = Paint.Style.FILL
                }
                currentCanvas.drawRect(25f, workingY, 570f, workingY + 32f, bannerBg)
                currentCanvas.drawRect(566f, workingY, 570f, workingY + 32f, accentBar)

                val paintBannerText = Paint().apply {
                    color = Color.parseColor(PdfColors.TEXT_DARK)
                    textSize = 9.5f
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    isAntiAlias = true
                }
                val bannerText = context.getString(
                    R.string.pdf_customer_banner_text,
                    customer.name,
                    customer.phone.ifEmpty { "-" }
                )
                drawArabicText(currentCanvas, bannerText, 25f, workingY + 9f, 545, paintBannerText, Layout.Alignment.ALIGN_CENTER)
            }
            workingY += 32f
        }

        // 2. Table Header
        if (!isDryRun && currentCanvas != null) {
            drawTableHeader(currentCanvas, workingY, context, customer.originalCustomer.initialType)
        }
        workingY += 28f

        // 3. Transaction Rows
        val sortedTxs = summary.sortedProcessedTxs
        if (sortedTxs.isEmpty()) {
            if (workingY + 25f > 780f) {
                currentCanvas = onPageBreakNeeded?.invoke(true) ?: currentCanvas
                workingY = 75f
            }
            if (!isDryRun && currentCanvas != null) {
                val paintEmptyText = Paint().apply {
                    color = Color.parseColor(PdfColors.TEXT_MUTED_GREY)
                    textSize = 9f
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                    isAntiAlias = true
                }
                currentCanvas.drawLine(25f, workingY + 25f, 570f, workingY + 25f, PdfPaints.paintRowDivider)
                drawArabicText(
                    currentCanvas,
                    context.getString(R.string.pdf_no_transactions),
                    25f,
                    workingY + 7f,
                    545,
                    paintEmptyText,
                    Layout.Alignment.ALIGN_CENTER
                )
            }
            workingY += 25f
        } else {
            var runningBal = BigDecimal.ZERO
            var totalCol4 = BigDecimal.ZERO
            var totalCol5 = BigDecimal.ZERO
            val isOwedToThemAccount = customer.originalCustomer.initialType == TransactionType.OWED_TO_THEM.value

            for ((txIndex, pt) in sortedTxs.withIndex()) {
                val tx = pt.tx

                val isCol4 = if (isOwedToThemAccount) {
                    tx.type == TransactionType.OWED_TO_THEM.value || tx.type == TransactionType.PAYMENT_BY_THEM.value
                } else {
                    tx.type == TransactionType.OWED_BY_THEM.value || tx.type == TransactionType.PAYMENT_TO_THEM.value
                }

                if (isCol4) {
                    runningBal = runningBal.add(pt.baseCurrencyAmount)
                    totalCol4 = totalCol4.add(pt.baseCurrencyAmount)
                } else {
                    runningBal = runningBal.subtract(pt.baseCurrencyAmount)
                    totalCol5 = totalCol5.add(pt.baseCurrencyAmount)
                }

                val calculatedHeight = PdfRowRenderer.calculateTransactionRowHeight(
                    context = context,
                    pt = pt,
                    initialType = customer.originalCustomer.initialType,
                    availableWidth = 190
                )

                if (workingY + calculatedHeight > 780f) {
                    currentCanvas = onPageBreakNeeded?.invoke(true) ?: currentCanvas
                    workingY = 75f
                }

                if (!isDryRun && currentCanvas != null) {
                    PdfRowRenderer.drawSingleTransactionRow(
                        currentCanvas,
                        context,
                        txIndex,
                        pt,
                        workingY,
                        calculatedHeight,
                        runningBal,
                        customer.originalCustomer.initialType
                    )
                }
                workingY += calculatedHeight
            }

            // 4. Totals & Final Net Banner
            val extraSummaryHeight = 60f + (if (summary.uncalculatedForeignSums.isNotEmpty()) 24f + summary.uncalculatedForeignSums.size * 20f else 0f)
            if (workingY + extraSummaryHeight > 780f) {
                currentCanvas = onPageBreakNeeded?.invoke(false) ?: currentCanvas
                workingY = 42f
            }

            if (!isDryRun && currentCanvas != null) {
                workingY = PdfRowRenderer.drawTotalsRow(
                    currentCanvas,
                    context,
                    workingY,
                    totalCol4,
                    totalCol5,
                    currencySymbol,
                    customer.originalCustomer.initialType
                )
                workingY += 4f
                val netBalance = totalCol4.subtract(totalCol5)
                workingY = PdfRowRenderer.drawFinalNetBanner(
                    currentCanvas,
                    context,
                    workingY,
                    netBalance,
                    currencySymbol,
                    customer.originalCustomer.initialType
                )
                workingY = PdfRowRenderer.drawForeignCurrenciesSummary(
                    currentCanvas,
                    context,
                    workingY,
                    summary.uncalculatedForeignSums,
                    currencySymbol
                )
            } else {
                workingY += 25f + 4f + 30f + 8f
                if (summary.uncalculatedForeignSums.isNotEmpty()) {
                    workingY += 4f + 24f + (summary.uncalculatedForeignSums.size * 20f)
                }
            }
        }

        return workingY
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
