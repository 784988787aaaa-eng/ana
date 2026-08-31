/*
 * ╔══════════════════════════════════════════════════════════════════════╗
 * ║ التوثيق العربي الهندسي — MASTERCLASS DOCUMENTATION                 ║
 * ╚══════════════════════════════════════════════════════════════════════╝
 *
 * الملف: app/src/main/java/com/example/data/serialization/pdf/MasterBookletPdfEngine.kt
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
 * المحرك الأعلى لإنشاء الكتيب/التقرير الرئيسي PDF وتجميع الصفحات والمقاطع المالية.
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
// السطر 181: data class BusinessProfileData — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 182: val name — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 183: val desc — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 184: val phonesStr — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 185: val scaledLogo — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 186: val hasLogo — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 187: val logoW — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 188: val logoH — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 195: data class PdfReportMetaData — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 196: val docDateText — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 197: val docTimeText — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 198: val currencySymbol — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 199: val primaryColorHex — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 206: object MasterBookletPdfEngine — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 209: private const val TAG — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 210: private const val PREFS_BUSINESS_PROFILE — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 211: private const val PREF_BIZ_NAME — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 212: private const val PREF_BIZ_DESC — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 213: private const val PREF_BIZ_LOGO_PATH — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 214: private const val PREF_BIZ_PHONES — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 215: private const val DEFAULT_PHONES_JSON — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 216: private const val PHONE_DELIMITER — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 231: suspend fun generateBookletPdfAsync — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 242: var scaledLogoToRecycle — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 243: var rawBitmapToRecycle — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 245: val targetCustomers — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 251: val totalCustomers — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 258: val header — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 262: val now — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 263: val docDateText — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 264: val docTimeText — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 266: val businessProfile — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 276: val reportMetaData — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 284: val database — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 285: val repository — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 288: val txCacheMap — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 291: val summary — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 294: var totalPagesInDryRun — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 296: val dryDoc — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 298: val drawingCtx — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 311: val customerChunks — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 312: var processedCount — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 317: val transactions — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 320: val singleSummary — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 335: val pdfDocument — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 336: val outputFile — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 337: val drawingCtx — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 347: val canvas — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 357: val customerChunks — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 358: var processedCount — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 363: val transactions — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 365: val singleSummary — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 375: val outputDir — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 377: val file — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 408: private fun drawCoverAndIndexDryRun — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 423: val rowHeight — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 433: private fun drawCoverAndIndexReal — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 438: val context — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 439: val canvas — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 449: val paintTitle — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 462: val paintSub — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 475: val paintIndexTitle — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 493: val rowHeight — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 496: val nextCanvas — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 502: val currentCanvas — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 517: private fun drawCustomerLedgerSheet — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 557: class BookletDrawingContext — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 558: val context — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 559: val pdfDocument — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 560: val isDryRun — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 561: val totalPagesInDryRun — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 562: val businessProfile — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 563: val reportMetaData — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 565: val displayedName — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 566: val displayedDesc — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 567: val phonesStr — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 568: val scaledLogo — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 569: val hasLogo — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 570: val logoW — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 571: val logoH — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 573: val docDateText — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 574: val docTimeText — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 575: val currencySymbol — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 576: val primaryColorHex — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 577: var currentPageNumber — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 578: var currentY — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 579: var currentPageCanvas — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 580: var currentPageObject — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 586: fun startNewPage — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 591: val pageInfo — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 592: val page — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 600: private fun drawFooterOnCurrentPage — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 601: val canvas — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 611: fun finishLastPage — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// --- نهاية الفهرس التوثيقي ---

/*
 * ╔══════════════════════════════════════════════════════════════════════╗
 * ║ التوثيق العربي الهندسي — MASTERCLASS DOCUMENTATION                 ║
 * ╚══════════════════════════════════════════════════════════════════════╝
 *
 * الملف: /mnt/data/source_full/app/src/main/java/com/example/data/serialization/pdf/MasterBookletPdfEngine.kt
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
 * المحرك الأعلى لإنشاء الكتيب/التقرير الرئيسي PDF وتجميع الصفحات والمقاطع المالية.
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
// السطر 54: data class BusinessProfileData — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 55: val name — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 56: val desc — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 57: val phonesStr — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 58: val scaledLogo — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 59: val hasLogo — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 60: val logoW — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 61: val logoH — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 68: data class PdfReportMetaData — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 69: val docDateText — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 70: val docTimeText — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 71: val currencySymbol — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 72: val primaryColorHex — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 79: object MasterBookletPdfEngine — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 82: private const val TAG — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 83: private const val PREFS_BUSINESS_PROFILE — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 84: private const val PREF_BIZ_NAME — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 85: private const val PREF_BIZ_DESC — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 86: private const val PREF_BIZ_LOGO_PATH — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 87: private const val PREF_BIZ_PHONES — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 88: private const val DEFAULT_PHONES_JSON — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 89: private const val PHONE_DELIMITER — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 104: suspend fun generateBookletPdfAsync — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 115: var scaledLogoToRecycle — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 116: var rawBitmapToRecycle — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 118: val targetCustomers — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 124: val totalCustomers — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 131: val header — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 135: val now — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 136: val docDateText — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 137: val docTimeText — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 139: val businessProfile — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 149: val reportMetaData — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 157: val database — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 158: val repository — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 161: val txCacheMap — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 164: val summary — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 167: var totalPagesInDryRun — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 169: val dryDoc — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 171: val drawingCtx — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 184: val customerChunks — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 185: var processedCount — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 190: val transactions — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 193: val singleSummary — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 208: val pdfDocument — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 209: val outputFile — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 210: val drawingCtx — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 220: val canvas — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 230: val customerChunks — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 231: var processedCount — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 236: val transactions — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 238: val singleSummary — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 248: val outputDir — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 250: val file — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 281: private fun drawCoverAndIndexDryRun — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 296: val rowHeight — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 306: private fun drawCoverAndIndexReal — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 311: val context — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 312: val canvas — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 322: val paintTitle — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 335: val paintSub — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 348: val paintIndexTitle — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 366: val rowHeight — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 369: val nextCanvas — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 375: val currentCanvas — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 390: private fun drawCustomerLedgerSheet — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 430: class BookletDrawingContext — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 431: val context — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 432: val pdfDocument — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 433: val isDryRun — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 434: val totalPagesInDryRun — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 435: val businessProfile — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 436: val reportMetaData — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 438: val displayedName — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 439: val displayedDesc — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 440: val phonesStr — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 441: val scaledLogo — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 442: val hasLogo — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 443: val logoW — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 444: val logoH — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 446: val docDateText — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 447: val docTimeText — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 448: val currencySymbol — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 449: val primaryColorHex — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 450: var currentPageNumber — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 451: var currentY — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 452: var currentPageCanvas — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 453: var currentPageObject — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 459: fun startNewPage — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 464: val pageInfo — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 465: val page — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 473: private fun drawFooterOnCurrentPage — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 474: val canvas — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 484: fun finishLastPage — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// --- نهاية الفهرس التوثيقي ---

/**
 * =====================================================================
 * ملف: محرك دفاتر وكشوفات الأستاذ العامة المجمعة (MasterBookletPdfEngine.kt)
 * =====================================================================
 * 
 * [الغرض العام والتعليمي من الملف]:
 * يمثل هذا الكائن المحرك المتقدم المسؤول عن تجميع وتوليد كتيب الحسابات الماستر
 * (Comprehensive Master Ledger Booklet) بصيغة PDF لعدة عملاء أو لجميع الحسابات
 * دفعة واحدة، مع تدفق سلس ومستمر للصفحات، وإدارة ذكية للذاكرة، وحساب دقيق لإجمالي
 * الصفحات عبر جولتين: تجريبية لحساب المقاسات وفعلية للرسم.
 * 
 * [المسؤوليات المعمارية والتقنية]:
 * 1. المعالجة المجمعة المتدفقة (Chunked Processing & Streaming):
 *    - تقسيم معالجة العملاء إلى دفعات (Chunks of 50) مع إمكانية إلغاء الكوروتين [ensureActive].
 * 2. التخزين المؤقت المحلي للعمليات (Local In-Memory Caching):
 *    - استخدام [txCacheMap] لتفادي الاستعلام المتكرر من قاعدة البيانات بين الجولة التجريبية والفعلية.
 * 3. إدارة لوحات الرسم والصفحات وسياق الدفتر [BookletDrawingContext]:
 *    - إنشاء صفحات مقاس A4 ورسم الترويسة والفاصل السفلي لكل صفحة تلقائياً.
 * 4. إدارة الموارد وتدوير البيتماب (Bitmap Lifecycle Management):
 *    - ضمان تدوير وتحرير صور الشعارات النقطية لمنع نفاد الذاكرة (OOM) في التقارير الضخمة.
 */
package com.example.data.serialization.pdf

// ---------------------------------------------------------------------
// استيراد حزم أندرويد والرسومات وتوليد PDF وقواعد البيانات والكوروتين
// ---------------------------------------------------------------------
import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.text.Layout
import android.util.Log
import com.example.R
import com.example.data.local.AppDatabase
import com.example.data.repository.FinanceRepository
import com.example.data.serialization.BusinessProfileLoader
import com.example.ui.state.CustomerUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.Date
import kotlin.coroutines.coroutineContext

/**
 * [وعاء بيانات هوية المنشأة للكتيب - BusinessProfileData]:
 * يضم معلومات المنشأة وشعارها وأبعادها.
 */
data class BusinessProfileData(
    val name: String,
    val desc: String,
    val phonesStr: String,
    val scaledLogo: Bitmap?,
    val hasLogo: Boolean,
    val logoW: Float,
    val logoH: Float
)

/**
 * [البيانات الوصفية للتقرير المالي - PdfReportMetaData]:
 * يجمع التواريخ المنسقة والعملة ولون السمة الأساسي.
 */
data class PdfReportMetaData(
    val docDateText: String,
    val docTimeText: String,
    val currencySymbol: String,
    val primaryColorHex: String
)

/**
 * [الكائن الأحادي لمحرك دفتر الحسابات الماستر - MasterBookletPdfEngine]:
 * يبني كتيبات وكشوفات الحسابات المجمعة لكافة العملاء أو المحدد منهم.
 */
object MasterBookletPdfEngine {

    /** وسم السجلات التشخيصية */
    private const val TAG = "MasterBookletPdfEngine"
    private const val PREFS_BUSINESS_PROFILE = "business_profile"
    private const val PREF_BIZ_NAME = "biz_name"
    private const val PREF_BIZ_DESC = "biz_desc"
    private const val PREF_BIZ_LOGO_PATH = "biz_logo_path"
    private const val PREF_BIZ_PHONES = "biz_phones"
    private const val DEFAULT_PHONES_JSON = "[]"
    private const val PHONE_DELIMITER = " - "

    /**
     * [توليد كتيب الحسابات الماستر بصيغة PDF لاتزامياً - generateBookletPdfAsync]:
     * ينفذ الجولة التجريبية ثم الجولة الحقيقية لرسم كشوفات الحسابات المتتابعة وتحديث شريط التقدم.
     *
     * @param context سياق التطبيق.
     * @param allCustomers قائمة كافة العملاء المسجلين.
     * @param selectedIds المعرفات المختارة للطباعة إن وجدت.
     * @param onlySelected ما إذا كان المطلوب طباعة المحدد فقط.
     * @param currencySymbol رمز العملة الرئيسية.
     * @param primaryColorHex كود لون السمة الرئيسي.
     * @param onProgress دالة رد نداء لتحديث مؤشر التقدم (تمت معالجة X من إجمالي Y).
     * @param onFinished دالة رد نداء عند اكتمال التوليد مع ملف الـ PDF الناتج.
     */
    suspend fun generateBookletPdfAsync(
        context: Context,
        allCustomers: List<CustomerUiState>,
        selectedIds: List<String>,
        onlySelected: Boolean,
        currencySymbol: String,
        primaryColorHex: String = PdfColors.PRIMARY_EMERALD,
        onProgress: (processed: Int, total: Int) -> Unit,
        onFinished: (File?) -> Unit
    ) = withContext(Dispatchers.IO) {

        var scaledLogoToRecycle: android.graphics.Bitmap? = null
        var rawBitmapToRecycle: android.graphics.Bitmap? = null
        try {
            val targetCustomers = if (onlySelected && selectedIds.isNotEmpty()) {
                allCustomers.filter { selectedIds.contains(it.id) }
            } else {
                allCustomers
            }

            val totalCustomers = targetCustomers.size
            if (totalCustomers == 0) {
                onFinished(null)
                return@withContext
            }

            // Load business profile from shared BusinessProfileLoader
            val header = BusinessProfileLoader.load(context)
            scaledLogoToRecycle = header.scaledLogo
            rawBitmapToRecycle = header.rawBitmap

            val now = Date()
            val docDateText = context.getString(R.string.pdf_doc_date, PdfPageRenderer.formatDayAr(now), PdfPageRenderer.formatDateEn(now))
            val docTimeText = context.getString(R.string.pdf_doc_time, PdfPageRenderer.formatTimeAr(now))

            val businessProfile = BusinessProfileData(
                name = header.displayedName,
                desc = header.displayedDesc,
                phonesStr = header.phonesStr,
                scaledLogo = header.scaledLogo,
                hasLogo = header.hasLogo,
                logoW = header.logoW,
                logoH = header.logoH
            )

            val reportMetaData = PdfReportMetaData(
                docDateText = docDateText,
                docTimeText = docTimeText,
                currencySymbol = currencySymbol,
                primaryColorHex = primaryColorHex
            )

            // Initialize DB & Repository inside for streaming
            val database = AppDatabase.getDatabase(context)
            val repository = FinanceRepository(database, context.applicationContext as Application)

            // Memory Cache Map to avoid querying database twice during dry run and real pass
            val txCacheMap = mutableMapOf<String, List<com.example.data.local.entities.HabayebTransaction>>()

            // Let's compute Overall System Balances
            val summary = PdfReportCalculator.calculateComprehensiveReport(targetCustomers)

            // First Pass: DRY RUN to compute total pages accurately
            var totalPagesInDryRun = 1
            run {
                val dryDoc = PdfDocument()
                try {
                    val drawingCtx = BookletDrawingContext(
                        context = context,
                        pdfDocument = dryDoc,
                        isDryRun = true,
                        totalPagesInDryRun = 1,
                        businessProfile = businessProfile,
                        reportMetaData = reportMetaData
                    )

                    // Start detailed ledger sheets directly below the business header on Page 1
                    drawingCtx.currentY = 78f

                    // Render detail sections for each customer in chunks
                    val customerChunks = targetCustomers.chunked(50)
                    var processedCount = 0
                    for (chunk in customerChunks) {
                        coroutineContext.ensureActive()
                        for (customer in chunk) {
                            coroutineContext.ensureActive()
                            val transactions = txCacheMap.getOrPut(customer.id) {
                                repository.getTransactionsForCustomerDirect(customer.id)
                            }
                            val singleSummary = PdfReportCalculator.calculateSingleCustomerReport(transactions, currencySymbol)
                            drawCustomerLedgerSheet(drawingCtx, customer, singleSummary)
                            processedCount++
                        }
                    }
                    drawingCtx.finishLastPage()
                    totalPagesInDryRun = drawingCtx.currentPageNumber - 1
                } finally {
                    dryDoc.close()
                }
            }

            coroutineContext.ensureActive()

            // Second Pass: REAL PASS
            val pdfDocument = PdfDocument()
            val outputFile = try {
                val drawingCtx = BookletDrawingContext(
                    context = context,
                    pdfDocument = pdfDocument,
                    isDryRun = false,
                    totalPagesInDryRun = totalPagesInDryRun,
                    businessProfile = businessProfile,
                    reportMetaData = reportMetaData
                )

                // Draw Business Header on Page 1
                val canvas = drawingCtx.currentPageCanvas
                if (canvas != null) {
                    PdfPageRenderer.drawBusinessHeader(
                        canvas, drawingCtx.displayedName, drawingCtx.displayedDesc, drawingCtx.phonesStr,
                        drawingCtx.hasLogo, drawingCtx.scaledLogo, drawingCtx.logoW, drawingCtx.logoH, drawingCtx.docDateText, drawingCtx.docTimeText
                    )
                }
                drawingCtx.currentY = 78f

                // 2. Customers detailed ledger sheets in chunks
                val customerChunks = targetCustomers.chunked(50)
                var processedCount = 0
                for (chunk in customerChunks) {
                    coroutineContext.ensureActive()
                    for (customer in chunk) {
                        coroutineContext.ensureActive()
                        val transactions = txCacheMap[customer.id]
                            ?: repository.getTransactionsForCustomerDirect(customer.id)
                        val singleSummary = PdfReportCalculator.calculateSingleCustomerReport(transactions, currencySymbol)
                        drawCustomerLedgerSheet(drawingCtx, customer, singleSummary)
                        processedCount++
                        onProgress(processedCount, totalCustomers)
                    }
                }

                drawingCtx.finishLastPage()

                // Save PDF to cache file
                val outputDir = File(context.cacheDir, "pdf_reports")
                if (!outputDir.exists()) outputDir.mkdirs()
                val file = File(outputDir, "MasterBookletReport_${System.currentTimeMillis()}.pdf")
                FileOutputStream(file).use { outputStream ->
                    pdfDocument.writeTo(outputStream)
                    outputStream.flush()
                }
                file
            } finally {
                pdfDocument.close()
            }

            onFinished(outputFile)

        } catch (e: Exception) {
            Log.e(TAG, "Error generating booklet PDF", e)
            onFinished(null)
        } finally {
            try {
                if (rawBitmapToRecycle != null && !rawBitmapToRecycle.isRecycled) {
                    if (scaledLogoToRecycle != null && scaledLogoToRecycle != rawBitmapToRecycle && !scaledLogoToRecycle.isRecycled) {
                        scaledLogoToRecycle.recycle()
                    }
                    rawBitmapToRecycle.recycle()
                } else if (scaledLogoToRecycle != null && !scaledLogoToRecycle.isRecycled) {
                    scaledLogoToRecycle.recycle()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error recycling bitmaps", e)
            }
        }
    }

    private fun drawCoverAndIndexDryRun(ctx: BookletDrawingContext, customers: List<CustomerUiState>) {
        // Business header space
        ctx.currentY = 78f
        // Title space
        ctx.currentY += 22f
        // Subtitle space
        ctx.currentY += 18f

        // Index Title
        ctx.currentY += 18f
        // Index Header
        ctx.currentY += 24f

        // Index Rows
        customers.forEachIndexed { _, customer ->
            val rowHeight = PdfRowRenderer.calculateBookletIndexRowHeight(customer)
            if (ctx.currentY + rowHeight > 780f) {
                ctx.startNewPage()
                // Subsequent page header + index header space on new page
                ctx.currentY = 69f
            }
            ctx.currentY += rowHeight
        }
    }

    private fun drawCoverAndIndexReal(
        ctx: BookletDrawingContext,
        customers: List<CustomerUiState>,
        summary: com.example.data.serialization.pdf.ComprehensivePdfSummary
    ) {
        val context = ctx.context
        val canvas = ctx.currentPageCanvas ?: return

        // 1. Draw Business Header
        PdfPageRenderer.drawBusinessHeader(
            canvas, ctx.displayedName, ctx.displayedDesc, ctx.phonesStr,
            ctx.hasLogo, ctx.scaledLogo, ctx.logoW, ctx.logoH, ctx.docDateText, ctx.docTimeText
        )
        ctx.currentY = 78f

        // 2. Draw Title
        val paintTitle = Paint().apply {
            color = Color.parseColor(ctx.primaryColorHex)
            textSize = 14f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        PdfDrawingUtils.drawArabicText(
            canvas, context.getString(R.string.pdf_booklet_title),
            25f, ctx.currentY, 545, paintTitle, Layout.Alignment.ALIGN_CENTER
        )
        ctx.currentY += 22f

        // 3. Draw Subtitle
        val paintSub = Paint().apply {
            color = Color.parseColor(PdfColors.TEXT_MEDIUM)
            textSize = 9.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            isAntiAlias = true
        }
        PdfDrawingUtils.drawArabicText(
            canvas, context.getString(R.string.pdf_booklet_subtitle),
            25f, ctx.currentY, 545, paintSub, Layout.Alignment.ALIGN_CENTER
        )
        ctx.currentY += 18f

        // 4. Draw Index Title
        val paintIndexTitle = Paint().apply {
            color = Color.parseColor(ctx.primaryColorHex)
            textSize = 10.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        PdfDrawingUtils.drawArabicText(
            canvas, context.getString(R.string.pdf_index_title),
            25f, ctx.currentY, 545, paintIndexTitle, Layout.Alignment.ALIGN_NORMAL
        )
        ctx.currentY += 18f

        // 5. Draw Index Table Header
        PdfRowRenderer.drawBookletIndexHeader(canvas, ctx.currentY, context)
        ctx.currentY += 24f

        // 6. Draw Index Rows
        customers.forEachIndexed { index, customer ->
            val rowHeight = PdfRowRenderer.calculateBookletIndexRowHeight(customer)
            if (ctx.currentY + rowHeight > 780f) {
                ctx.startNewPage()
                val nextCanvas = ctx.currentPageCanvas ?: return@forEachIndexed
                // Redraw table header on next page
                PdfRowRenderer.drawBookletIndexHeader(nextCanvas, 45f, context)
                ctx.currentY = 69f
            }

            val currentCanvas = ctx.currentPageCanvas ?: return@forEachIndexed
            PdfRowRenderer.drawBookletIndexRow(
                canvas = currentCanvas,
                context = context,
                index = index,
                customer = customer,
                currentY = ctx.currentY,
                rowHeight = rowHeight,
                currencySymbol = ctx.currencySymbol
            )

            ctx.currentY += rowHeight
        }
    }

    private fun drawCustomerLedgerSheet(
        ctx: BookletDrawingContext,
        customer: CustomerUiState,
        summary: com.example.data.serialization.pdf.SingleCustomerPdfSummary
    ) {
        ctx.currentY = PdfPageRenderer.drawCustomerStatementSheet(
            canvas = ctx.currentPageCanvas,
            context = ctx.context,
            customer = customer,
            summary = summary,
            startY = ctx.currentY,
            primaryColorHex = ctx.primaryColorHex,
            currencySymbol = ctx.currencySymbol,
            isDryRun = ctx.isDryRun,
            includeCustomerHeaderBanner = true,
            onPageBreakNeeded = { newHeader ->
                ctx.startNewPage()
                if (newHeader && !ctx.isDryRun) {
                    ctx.currentPageCanvas?.let { currentCanvas ->
                        PdfPageRenderer.drawSubsequentPageHeader(currentCanvas, customer.name, ctx.primaryColorHex, ctx.context)
                        PdfPageRenderer.drawTableHeader(currentCanvas, 45f, ctx.context, customer.originalCustomer.initialType)
                    }
                }
                ctx.currentPageCanvas
            }
        )
    }
}

/**
 * [سياق وحالة رسم كتيب الحسابات - BookletDrawingContext]:
 * يدير حالة الصفحات الحالية ومؤشر الإحداثي الرأسي Y، وأرقام الصفحات، ورسم التذييلات تلقائياً.
 *
 * @property context سياق التطبيق.
 * @property pdfDocument كائن مستند الـ PDF قيد البناء.
 * @property isDryRun هل الجولة الحالية جولة تجريبية افتراضية لحساب عدد الصفحات فقط.
 * @property totalPagesInDryRun إجمالي الصفحات المحسوبة من الجولة السابقة.
 * @property businessProfile بيانات ومعلومات وهوية المنشأة.
 * @property reportMetaData البيانات الوصفية للتقرير.
 */
class BookletDrawingContext(
    val context: Context,
    val pdfDocument: PdfDocument,
    val isDryRun: Boolean,
    val totalPagesInDryRun: Int,
    val businessProfile: BusinessProfileData,
    val reportMetaData: PdfReportMetaData
) {
    val displayedName: String get() = businessProfile.name
    val displayedDesc: String get() = businessProfile.desc
    val phonesStr: String get() = businessProfile.phonesStr
    val scaledLogo: Bitmap? get() = businessProfile.scaledLogo
    val hasLogo: Boolean get() = businessProfile.hasLogo
    val logoW: Float get() = businessProfile.logoW
    val logoH: Float get() = businessProfile.logoH

    val docDateText: String get() = reportMetaData.docDateText
    val docTimeText: String get() = reportMetaData.docTimeText
    val currencySymbol: String get() = reportMetaData.currencySymbol
    val primaryColorHex: String get() = reportMetaData.primaryColorHex
    var currentPageNumber = 1
    var currentY = 42f
    var currentPageCanvas: Canvas? = null
    var currentPageObject: PdfDocument.Page? = null

    init {
        startNewPage()
    }

    fun startNewPage() {
        if (!isDryRun) {
            drawFooterOnCurrentPage()
            currentPageObject?.let { pdfDocument.finishPage(it) }

            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, currentPageNumber).create()
            val page = pdfDocument.startPage(pageInfo)
            currentPageCanvas = page.canvas
            currentPageObject = page
        }
        currentPageNumber++
        currentY = 42f
    }

    private fun drawFooterOnCurrentPage() {
        val canvas = currentPageCanvas ?: return
        PdfPageRenderer.drawFooter(
            canvas,
            currentPageNumber - 1,
            totalPagesInDryRun,
            primaryColorHex,
            context
        )
    }

    fun finishLastPage() {
        if (!isDryRun) {
            drawFooterOnCurrentPage()
            currentPageObject?.let { pdfDocument.finishPage(it) }
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
