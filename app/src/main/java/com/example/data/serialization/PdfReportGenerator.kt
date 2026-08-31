/*
 * ╔══════════════════════════════════════════════════════════════════════╗
 * ║ التوثيق العربي الهندسي — MASTERCLASS DOCUMENTATION                 ║
 * ╚══════════════════════════════════════════════════════════════════════╝
 *
 * الملف: app/src/main/java/com/example/data/serialization/PdfReportGenerator.kt
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
 * واجهة إنشاء تقارير PDF وربط بيانات التقرير بمحرك الرسم والإخراج النهائي.
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
// السطر 157: object PdfReportGenerator — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 160: private const val TAG — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 162: private const val MIME_TYPE_PDF — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 175: private fun generatePdfFileInternal — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 182: val summary — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 183: val customerUiState — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 191: var totalPages — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 192: var dryPageCount — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 210: val header — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 211: val pdfDocument — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 212: val pageWidth — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 213: val pageHeight — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 216: var currentPageNumber — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 217: var pageInfo — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 218: var page — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 219: var canvas — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 221: val now — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 235: val paintTitle — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 274: val sanitizedName — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 275: val fileName — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 276: val file — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 295: private fun generateAllCustomersPdfFileInternal — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 301: val summary — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 302: val totalItems — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 305: var totalPages — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 307: var dryY — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 308: var dryPages — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 310: val rowHeight — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 320: val header — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 321: val pdfDocument — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 322: val pageWidth — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 323: val pageHeight — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 326: var currentPageNumber — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 327: var pageInfo — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 328: var page — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 329: var canvas — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 331: val now — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 332: val docDateText — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 333: val docTimeText — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 348: val paintTitle — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 368: var currentY — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 371: val rowHeight — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 381: val paintMiniHeader — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 389: val paintMiniLine — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 408: val dir — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 412: val file — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 432: fun triggerShareOrViewIntent — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 437: fun triggerShareOrViewIntent — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 444: fun generateAndHandleCustomerPdfReport — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 459: fun generateAndHandleCustomerPdfReport — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 482: fun generateAndHandleCustomerPdfReportAsync — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 494: val file — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 535: fun generateAndHandleCustomerPdfReportAsync — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 554: fun generateAndHandleAllCustomersPdfReportAsync — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 565: val file — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 582: fun generateAndHandleAllCustomersPdfReportAsync — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// --- نهاية الفهرس التوثيقي ---

/*
 * ╔══════════════════════════════════════════════════════════════════════╗
 * ║ التوثيق العربي الهندسي — MASTERCLASS DOCUMENTATION                 ║
 * ╚══════════════════════════════════════════════════════════════════════╝
 *
 * الملف: /mnt/data/source_full/app/src/main/java/com/example/data/serialization/PdfReportGenerator.kt
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
 * واجهة إنشاء تقارير PDF وربط بيانات التقرير بمحرك الرسم والإخراج النهائي.
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
// السطر 68: object PdfReportGenerator — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 71: private const val TAG — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 73: private const val MIME_TYPE_PDF — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 86: private fun generatePdfFileInternal — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 93: val summary — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 94: val customerUiState — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 102: var totalPages — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 103: var dryPageCount — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 121: val header — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 122: val pdfDocument — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 123: val pageWidth — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 124: val pageHeight — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 127: var currentPageNumber — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 128: var pageInfo — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 129: var page — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 130: var canvas — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 132: val now — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 146: val paintTitle — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 185: val sanitizedName — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 186: val fileName — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 187: val file — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 206: private fun generateAllCustomersPdfFileInternal — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 212: val summary — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 213: val totalItems — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 216: var totalPages — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 218: var dryY — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 219: var dryPages — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 221: val rowHeight — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 231: val header — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 232: val pdfDocument — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 233: val pageWidth — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 234: val pageHeight — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 237: var currentPageNumber — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 238: var pageInfo — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 239: var page — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 240: var canvas — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 242: val now — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 243: val docDateText — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 244: val docTimeText — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 259: val paintTitle — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 279: var currentY — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 282: val rowHeight — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 292: val paintMiniHeader — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 300: val paintMiniLine — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 319: val dir — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 323: val file — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 343: fun triggerShareOrViewIntent — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 348: fun triggerShareOrViewIntent — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 355: fun generateAndHandleCustomerPdfReport — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 370: fun generateAndHandleCustomerPdfReport — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 393: fun generateAndHandleCustomerPdfReportAsync — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 405: val file — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 446: fun generateAndHandleCustomerPdfReportAsync — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 465: fun generateAndHandleAllCustomersPdfReportAsync — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 476: val file — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 493: fun generateAndHandleAllCustomersPdfReportAsync — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// --- نهاية الفهرس التوثيقي ---

/**
 * =====================================================================
 * ملف: محرك ومنسق تقارير PDF المالية (PdfReportGenerator.kt)
 * =====================================================================
 * 
 * [الغرض العام والتعليمي من الملف]:
 * يمثل هذا الكائن المحرك التنفيذي الأساسي لرسم وتوليد مستندات PDF الاحترافية
 * المعتمدة على لوحة رسم أندرويد الأصلية [android.graphics.pdf.PdfDocument].
 * يقوم بحساب المسافات وقياس النصوص العربية وتوزيع السجلات على صفحات متعددة (Pagination)
 * وفق المقاس العالمي A4 (595x842 نقطة)، مع معالجة ذكية للترويسة التجارية وشعار المنشأة
 * وخاتمة الصفحات والتقارير الشاملة لكافة العملاء.
 * 
 * [المسؤوليات المعمارية والتقنية]:
 * 1. الجولة التجريبية الاستباقية (Dry Run Pass):
 *    - محاكاة رسم الجداول لحساب العدد الدقيق للصفحات الإجمالية قبل الرسم الفعلي (مثال: صفحة 1 من 3).
 * 2. معالجة النصوص والاتجاه العربي الأصيل (RTL):
 *    - التوافق مع [StaticLayout] لرسم النصوص العربية والوصف المالي دون تقطيع أو تشويه.
 * 3. التدوير الذكي للموارد والتنظيف:
 *    - إدارة وتفريغ صور الشعارات والبيتماب [recycleBitmapsSafely] لمنع تسريب الذاكرة (Memory Leaks).
 * 4. إدارة قنوات التصدير والإرسال:
 *    - دعم المشاركة عبر النظام، الحفظ المباشر بذاكرة الجهاز، والإرسال الفوري عبر تطبيق واتساب.
 */
package com.example.data.serialization

// ---------------------------------------------------------------------
// استيراد حزم سياق أندرويد والرسومات والطباعة ووثائق PDF والكيانات وتزامن كوتلن
// ---------------------------------------------------------------------
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.text.Layout
import android.util.Log
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.R
import com.example.data.local.entities.HabayebCustomer
import com.example.data.local.entities.HabayebTransaction
import com.example.data.serialization.pdf.BusinessHeaderData
import com.example.data.serialization.pdf.BusinessProfileLoader
import com.example.data.serialization.pdf.PdfAction
import com.example.data.serialization.pdf.PdfColors
import com.example.data.serialization.pdf.PdfDrawingUtils
import com.example.data.serialization.pdf.PdfIntentLauncher
import com.example.data.serialization.pdf.PdfPageRenderer
import com.example.data.serialization.pdf.PdfReportCalculator
import com.example.data.serialization.pdf.PdfRowRenderer
import com.example.ui.state.CustomerUiState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.Date

typealias PdfAction = PdfAction
typealias BusinessHeaderData = BusinessHeaderData
typealias BusinessProfileLoader = BusinessProfileLoader

/**
 * [الكائن الأحادي لمحرك تقارير PDF - PdfReportGenerator]:
 * يوفر واجهات توليد وتصدير كشوف الحسابات الفردية والتقارير الشاملة بصيغة PDF.
 */
object PdfReportGenerator {

    /** وسم السجلات التشخيصية */
    private const val TAG = "PdfReportGenerator"
    /** نوع الوسائط المعياري لمستندات PDF */
    private const val MIME_TYPE_PDF = "application/pdf"

    /**
     * [التوليد الداخلي لكشف حساب العميل الفردي - generatePdfFileInternal]:
     * يبني مستند PDF متعدد الصفحات يتضمن ترويسة العمل وكشف حركة الحساب والخاتمة.
     *
     * @param context سياق التطبيق.
     * @param customer بيانات العميل المستهدف.
     * @param transactions قائمة معاملات العميل.
     * @param currencySymbol رمز العملة الرئيسية.
     * @param primaryColorHex لون السمة التنسيقي.
     * @return ملف الـ PDF المؤقت المنشأ، أو null عند الفشل.
     */
    private fun generatePdfFileInternal(
        context: Context,
        customer: HabayebCustomer,
        transactions: List<HabayebTransaction>,
        currencySymbol: String,
        primaryColorHex: String = PdfColors.PRIMARY_EMERALD
    ): File? {
        val summary = PdfReportCalculator.calculateSingleCustomerReport(transactions, currencySymbol)
        val customerUiState = CustomerUiState(
            id = customer.id.toString(),
            name = customer.name,
            phone = customer.phone,
            originalCustomer = customer
        )

        // 1. الجولة التجريبية (Dry Run) لحساب إجمالي عدد الصفحات بدقة متناهية
        var totalPages = 1
        var dryPageCount = 1
        PdfPageRenderer.drawCustomerStatementSheet(
            canvas = null,
            context = context,
            customer = customerUiState,
            summary = summary,
            startY = 98f,
            primaryColorHex = primaryColorHex,
            currencySymbol = currencySymbol,
            isDryRun = true,
            includeCustomerHeaderBanner = false,
            onPageBreakNeeded = { _ ->
                dryPageCount++
                null
            }
        )
        totalPages = dryPageCount

        val header = BusinessProfileLoader.load(context)
        val pdfDocument = PdfDocument()
        val pageWidth = 595
        val pageHeight = 842

        return try {
            var currentPageNumber = 1
            var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, currentPageNumber).create()
            var page = pdfDocument.startPage(pageInfo)
            var canvas = page.canvas

            val now = Date()
            PdfPageRenderer.drawBusinessHeader(
                canvas = canvas,
                displayedName = header.displayedName,
                displayedDesc = header.displayedDesc,
                phonesStr = header.phonesStr,
                hasLogo = header.hasLogo,
                scaledLogo = header.scaledLogo,
                logoW = header.logoW,
                logoH = header.logoH,
                docDateText = context.getString(R.string.pdf_doc_date, PdfPageRenderer.formatDayAr(now), PdfPageRenderer.formatDateEn(now)),
                docTimeText = context.getString(R.string.pdf_doc_time, PdfPageRenderer.formatTimeAr(now))
            )

            val paintTitle = Paint().apply {
                color = Color.parseColor(PdfColors.TEXT_DARK)
                textSize = 14.5f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }
            PdfDrawingUtils.drawArabicText(canvas, context.getString(R.string.pdf_statement_title, customer.name), 25f, 74f, 545, paintTitle, Layout.Alignment.ALIGN_CENTER)

            // بدء رسم جدول المعاملات
            PdfPageRenderer.drawCustomerStatementSheet(
                canvas = canvas,
                context = context,
                customer = customerUiState,
                summary = summary,
                startY = 98f,
                primaryColorHex = primaryColorHex,
                currencySymbol = currencySymbol,
                isDryRun = false,
                includeCustomerHeaderBanner = false,
                onPageBreakNeeded = { newHeader ->
                    PdfPageRenderer.drawFooter(canvas, currentPageNumber, totalPages, primaryColorHex, context)
                    pdfDocument.finishPage(page)

                    currentPageNumber++
                    pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, currentPageNumber).create()
                    page = pdfDocument.startPage(pageInfo)
                    canvas = page.canvas

                    if (newHeader) {
                        PdfPageRenderer.drawSubsequentPageHeader(canvas, customer.name, primaryColorHex, context)
                        PdfPageRenderer.drawTableHeader(canvas, 32f, context, customer.initialType)
                    }
                    canvas
                }
            )

            PdfPageRenderer.drawFooter(canvas, currentPageNumber, totalPages, primaryColorHex, context)
            pdfDocument.finishPage(page)

            val sanitizedName = customer.name.replace("[\\\\/:*?\"<>|]".toRegex(), "_")
            val fileName = "habayeb_${sanitizedName}_${System.currentTimeMillis() % 100000}.pdf"
            val file = File(context.cacheDir, fileName)
            FileOutputStream(file).use { outputStream ->
                pdfDocument.writeTo(outputStream)
                outputStream.flush()
            }
            file
        } catch (e: Exception) {
            Log.e(TAG, "Error generating customer PDF", e)
            null
        } finally {
            pdfDocument.close()
            PdfIntentLauncher.recycleBitmapsSafely(header.rawBitmap, header.scaledLogo)
        }
    }

    /**
     * [التوليد الداخلي لتقرير كافة العملاء الشامل - generateAllCustomersPdfFileInternal]:
     * يبني وثيقة PDF تحوي بطاقة ملخص الأرصدة الكلية وجدول كافة العملاء وأرصدتهم.
     */
    private fun generateAllCustomersPdfFileInternal(
        context: Context,
        customers: List<CustomerUiState>,
        currencySymbol: String,
        primaryColorHex: String = PdfColors.PRIMARY_EMERALD
    ): File? {
        val summary = PdfReportCalculator.calculateComprehensiveReport(customers)
        val totalItems = customers.size

        // 1. الجولة التجريبية لحساب إجمالي عدد الصفحات
        var totalPages = 1
        run {
            var dryY = 186f
            var dryPages = 1
            for (c in customers) {
                val rowHeight = PdfRowRenderer.calculateCustomerSummaryRowHeight(context, c)
                if (dryY + rowHeight > 760f) {
                    dryPages++
                    dryY = 76f
                }
                dryY += rowHeight
            }
            totalPages = dryPages
        }

        val header = BusinessProfileLoader.load(context)
        val pdfDocument = PdfDocument()
        val pageWidth = 595
        val pageHeight = 842

        return try {
            var currentPageNumber = 1
            var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, currentPageNumber).create()
            var page = pdfDocument.startPage(pageInfo)
            var canvas = page.canvas

            val now = Date()
            val docDateText = context.getString(R.string.pdf_doc_date, PdfPageRenderer.formatDayAr(now), PdfPageRenderer.formatDateEn(now))
            val docTimeText = context.getString(R.string.pdf_doc_time, PdfPageRenderer.formatTimeAr(now))

            PdfPageRenderer.drawBusinessHeader(
                canvas = canvas,
                displayedName = header.displayedName,
                displayedDesc = header.displayedDesc,
                phonesStr = header.phonesStr,
                hasLogo = header.hasLogo,
                scaledLogo = header.scaledLogo,
                logoW = header.logoW,
                logoH = header.logoH,
                docDateText = docDateText,
                docTimeText = docTimeText
            )

            val paintTitle = Paint().apply {
                color = Color.parseColor(PdfColors.TEXT_DARK)
                textSize = 14f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }
            PdfDrawingUtils.drawArabicText(canvas, context.getString(R.string.pdf_comprehensive_report_title), 25f, 74f, 545, paintTitle, Layout.Alignment.ALIGN_CENTER)

            PdfRowRenderer.drawComprehensiveSummaryCard(
                canvas = canvas,
                context = context,
                primaryColorHex = primaryColorHex,
                summary = summary,
                totalItems = totalItems,
                currencySymbol = currencySymbol,
                startY = 94f
            )

            PdfPageRenderer.drawAllCustomersTableHeader(canvas, 156f, context)

            var currentY = 186f

            for ((index, c) in customers.withIndex()) {
                val rowHeight = PdfRowRenderer.calculateCustomerSummaryRowHeight(context, c)
                if (currentY + rowHeight > 760f) {
                    PdfPageRenderer.drawFooter(canvas, currentPageNumber, totalPages, primaryColorHex, context)
                    pdfDocument.finishPage(page)

                    currentPageNumber++
                    pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, currentPageNumber).create()
                    page = pdfDocument.startPage(pageInfo)
                    canvas = page.canvas

                    val paintMiniHeader = Paint().apply {
                        color = Color.parseColor(primaryColorHex)
                        textSize = 9f
                        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                        isAntiAlias = true
                    }
                    PdfDrawingUtils.drawArabicText(canvas, context.getString(R.string.pdf_comprehensive_report_subpage, currentPageNumber), 25f, 22f, 545, paintMiniHeader, Layout.Alignment.ALIGN_NORMAL)

                    val paintMiniLine = Paint().apply {
                        color = Color.parseColor(PdfColors.HEADER_BORDER)
                        strokeWidth = 0.5f
                        style = Paint.Style.STROKE
                    }
                    canvas.drawLine(25f, 34f, 570f, 34f, paintMiniLine)

                    currentY = 46f
                    PdfPageRenderer.drawAllCustomersTableHeader(canvas, currentY, context)
                    currentY += 30f
                }

                PdfRowRenderer.drawCustomerSummaryRow(canvas, context, index, c, currentY, rowHeight, currencySymbol)
                currentY += rowHeight
            }

            PdfPageRenderer.drawFooter(canvas, currentPageNumber, totalPages, primaryColorHex, context)
            pdfDocument.finishPage(page)

            val dir = File(context.cacheDir, "documents")
            if (!dir.exists()) {
                dir.mkdirs()
            }
            val file = File(dir, "Statement_Comprehensive_${System.currentTimeMillis()}.pdf")
            FileOutputStream(file).use { outputStream ->
                pdfDocument.writeTo(outputStream)
                outputStream.flush()
            }

            file
        } catch (e: Exception) {
            Log.e(TAG, "Error generating all customers PDF", e)
            null
        } finally {
            pdfDocument.close()
            PdfIntentLauncher.recycleBitmapsSafely(header.rawBitmap, header.scaledLogo)
        }
    }

    /**
     * [إطلاق نية المشاركة أو العرض - triggerShareOrViewIntent]:
     * يفوض الإطلاق إلى [PdfIntentLauncher].
     */
    fun triggerShareOrViewIntent(context: Context, file: File?, action: PdfAction) {
        PdfIntentLauncher.triggerShareOrViewIntent(context, file, action)
    }

    /** إطلاق نية المشاركة بنص الإجراء */
    fun triggerShareOrViewIntent(context: Context, file: File?, action: String) {
        PdfIntentLauncher.triggerShareOrViewIntent(context, file, PdfAction.from(action))
    }

    /**
     * [توليد ومعالجة تقرير العميل بنطاق كوروتين ممرر - generateAndHandleCustomerPdfReport]:
     */
    fun generateAndHandleCustomerPdfReport(
        context: Context,
        scope: CoroutineScope,
        customer: HabayebCustomer,
        transactions: List<HabayebTransaction>,
        currencySymbol: String,
        action: String,
        primaryColorHex: String = PdfColors.PRIMARY_EMERALD
    ) {
        generateAndHandleCustomerPdfReportAsync(context, scope, customer, transactions, currencySymbol, PdfAction.from(action), primaryColorHex)
    }

    /**
     * [توليد ومعالجة تقرير العميل بنطاق رئيسي افتراضي - generateAndHandleCustomerPdfReport]:
     */
    fun generateAndHandleCustomerPdfReport(
        context: Context,
        customer: HabayebCustomer,
        transactions: List<HabayebTransaction>,
        currencySymbol: String,
        action: String,
        primaryColorHex: String = PdfColors.PRIMARY_EMERALD
    ) {
        generateAndHandleCustomerPdfReportAsync(
            context,
            CoroutineScope(Dispatchers.Main),
            customer,
            transactions,
            currencySymbol,
            PdfAction.from(action),
            primaryColorHex
        )
    }

    /**
     * [توليد ومعالجة تقرير العميل اللاتزامني الأساسي - generateAndHandleCustomerPdfReportAsync]:
     * يبني ملف الـ PDF ثم يوجهه للمشاركة، أو الحفظ المحلي، أو الإرسال المباشر عبر واتساب.
     */
    fun generateAndHandleCustomerPdfReportAsync(
        context: Context,
        scope: CoroutineScope,
        customer: HabayebCustomer,
        transactions: List<HabayebTransaction>,
        currencySymbol: String,
        action: PdfAction,
        primaryColorHex: String = PdfColors.PRIMARY_EMERALD,
        onFinished: () -> Unit = {}
    ) {
        scope.launch(Dispatchers.IO) {
            try {
                val file = generatePdfFileInternal(context, customer, transactions, currencySymbol, primaryColorHex)
                withContext(Dispatchers.Main) {
                    if (action == PdfAction.SAVE_LOCAL) {
                        if (file != null) {
                            com.example.ui.helper.LocalFileSaver.saveAndShowToast(
                                context = context,
                                cachedFile = file,
                                mimeType = MIME_TYPE_PDF,
                                displayName = file.name
                            )
                        } else {
                            Toast.makeText(context, context.getString(R.string.toast_operation_failed), Toast.LENGTH_SHORT).show()
                        }
                    } else if (action == PdfAction.WHATSAPP_DIRECT) {
                        if (file != null) {
                            com.example.ui.screens.habayeb.utils.CustomerShareHelper.triggerWhatsAppDirectFile(
                                context = context,
                                customer = customer,
                                file = file,
                                mimeType = MIME_TYPE_PDF
                            )
                        } else {
                            Toast.makeText(context, context.getString(R.string.toast_operation_failed), Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        triggerShareOrViewIntent(context, file, action)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error generating customer PDF async", e)
            } finally {
                withContext(Dispatchers.Main) {
                    onFinished()
                }
            }
        }
    }

    /**
     * [توليد ومعالجة تقرير العميل اللاتزامني بنص الإجراء - generateAndHandleCustomerPdfReportAsync]:
     */
    fun generateAndHandleCustomerPdfReportAsync(
        context: Context,
        scope: CoroutineScope,
        customer: HabayebCustomer,
        transactions: List<HabayebTransaction>,
        currencySymbol: String,
        action: String,
        primaryColorHex: String = PdfColors.PRIMARY_EMERALD,
        onFinished: () -> Unit = {}
    ) {
        generateAndHandleCustomerPdfReportAsync(
            context, scope, customer, transactions, currencySymbol, PdfAction.from(action), primaryColorHex, onFinished
        )
    }

    /**
     * [توليد ومعالجة تقرير كافة العملاء اللاتزامني - generateAndHandleAllCustomersPdfReportAsync]:
     * يبني تقرير PDF شامل يضم جميع العملاء وأرصدتهم الإجمالية.
     */
    fun generateAndHandleAllCustomersPdfReportAsync(
        context: Context,
        scope: CoroutineScope,
        customers: List<CustomerUiState>,
        currencySymbol: String,
        action: PdfAction,
        primaryColorHex: String = PdfColors.PRIMARY_EMERALD,
        onFinished: () -> Unit = {}
    ) {
        scope.launch(Dispatchers.IO) {
            try {
                val file = generateAllCustomersPdfFileInternal(context, customers, currencySymbol, primaryColorHex)
                withContext(Dispatchers.Main) {
                    triggerShareOrViewIntent(context, file, action)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error generating all customers PDF async", e)
            } finally {
                withContext(Dispatchers.Main) {
                    onFinished()
                }
            }
        }
    }

    /**
     * [توليد ومعالجة تقرير كافة العملاء بنص الإجراء - generateAndHandleAllCustomersPdfReportAsync]:
     */
    fun generateAndHandleAllCustomersPdfReportAsync(
        context: Context,
        scope: CoroutineScope,
        customers: List<CustomerUiState>,
        currencySymbol: String,
        action: String,
        primaryColorHex: String = PdfColors.PRIMARY_EMERALD,
        onFinished: () -> Unit = {}
    ) {
        generateAndHandleAllCustomersPdfReportAsync(
            context, scope, customers, currencySymbol, PdfAction.from(action), primaryColorHex, onFinished
        )
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
