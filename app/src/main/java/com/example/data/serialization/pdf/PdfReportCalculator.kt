/*
 * ╔══════════════════════════════════════════════════════════════════════╗
 * ║ التوثيق العربي الهندسي — MASTERCLASS DOCUMENTATION                 ║
 * ╚══════════════════════════════════════════════════════════════════════╝
 *
 * الملف: app/src/main/java/com/example/data/serialization/pdf/PdfReportCalculator.kt
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
 * محرك حساب المقاييس والقيم التجميعية التي يعتمد عليها تقرير PDF.
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
// السطر 128: data class ProcessedTransaction — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 129: val tx — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 130: val resolvedCurrency — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 131: val resolvedAmount — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 132: val isTxForeign — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 133: val baseCurrencyAmount — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 134: val pureBaseAmount — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 150: data class SingleCustomerPdfSummary — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 151: val sortedProcessedTxs — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 152: val totalDebts — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 153: val totalPayments — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 154: val totalDebtsBase — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 155: val totalPaymentsBase — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 156: val calculatedNetDebt — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 157: val uncalculatedForeignSums — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 158: val hasMultipleCurrencies — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 170: data class ComprehensivePdfSummary — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 171: val totalOwedByThem — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 172: val totalOwedToThem — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 173: val netPrimary — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 174: val foreignTotalsMap — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 181: object PdfReportCalculator — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 191: fun calculateSingleCustomerReport — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 195: val calcResult — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 201: val normDefaultSymbol — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 202: val sortedTxs — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 204: val processedList — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 205: var totalDebtsBase — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 206: var totalPaymentsBase — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 210: val affectsReportPrimaryCurrency — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 213: val baseCurrencyAmount — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 215: val isTxForeign — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 220: val isPureBase — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 221: val pureBaseAmount — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 223: val txType — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 244: val owedBy — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 245: val payTo — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 246: val totalDebts — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 248: val payBy — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 249: val owedTo — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 250: val totalPayments — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 252: val calculatedNetDebt — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 253: val uncalculatedForeignSums — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 257: val hasMultipleCurrencies — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 278: fun calculateComprehensiveReport — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 281: var totalOwedByThem — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 282: var totalOwedToThem — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 283: val foreignTotalsMap — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 286: val bdVal — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 299: val netPrimary — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// --- نهاية الفهرس التوثيقي ---

/*
 * ╔══════════════════════════════════════════════════════════════════════╗
 * ║ التوثيق العربي الهندسي — MASTERCLASS DOCUMENTATION                 ║
 * ╚══════════════════════════════════════════════════════════════════════╝
 *
 * الملف: /mnt/data/source_full/app/src/main/java/com/example/data/serialization/pdf/PdfReportCalculator.kt
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
 * محرك حساب المقاييس والقيم التجميعية التي يعتمد عليها تقرير PDF.
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
// السطر 45: data class ProcessedTransaction — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 46: val tx — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 47: val resolvedCurrency — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 48: val resolvedAmount — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 49: val isTxForeign — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 50: val baseCurrencyAmount — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 51: val pureBaseAmount — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 67: data class SingleCustomerPdfSummary — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 68: val sortedProcessedTxs — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 69: val totalDebts — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 70: val totalPayments — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 71: val totalDebtsBase — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 72: val totalPaymentsBase — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 73: val calculatedNetDebt — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 74: val uncalculatedForeignSums — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 75: val hasMultipleCurrencies — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 87: data class ComprehensivePdfSummary — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 88: val totalOwedByThem — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 89: val totalOwedToThem — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 90: val netPrimary — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 91: val foreignTotalsMap — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 98: object PdfReportCalculator — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 108: fun calculateSingleCustomerReport — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 112: val calcResult — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 118: val normDefaultSymbol — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 119: val sortedTxs — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 121: val processedList — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 122: var totalDebtsBase — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 123: var totalPaymentsBase — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 127: val affectsReportPrimaryCurrency — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 130: val baseCurrencyAmount — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 132: val isTxForeign — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 137: val isPureBase — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 138: val pureBaseAmount — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 140: val txType — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 161: val owedBy — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 162: val payTo — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 163: val totalDebts — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 165: val payBy — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 166: val owedTo — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 167: val totalPayments — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 169: val calculatedNetDebt — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 170: val uncalculatedForeignSums — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 174: val hasMultipleCurrencies — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 195: fun calculateComprehensiveReport — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 198: var totalOwedByThem — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 199: var totalOwedToThem — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 200: val foreignTotalsMap — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 203: val bdVal — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 216: val netPrimary — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// --- نهاية الفهرس التوثيقي ---

/**
 * =====================================================================
 * ملف: محرك الحسابات المالية لتقارير PDF (PdfReportCalculator.kt)
 * =====================================================================
 * 
 * [الغرض العام والتعليمي من الملف]:
 * يمثل هذا الكائن العقل الحسابي المالي لكافة تقارير PDF المطبوعة في التطبيق.
 * يتولى مسؤولية معالجة المعاملات المالية وترتيبها زمنياً، وتصنيف المبالغ وفق
 * العملة الأساسية للتقرير والعملات الأجنبية، وحساب الأرصدة المتراكمة،
 * وإجماليات المديونيات (لنا) والمدفوعات والمستحقات (علينا)، والصافي الكلي بدقة [BigDecimal].
 * 
 * [المسؤوليات المعمارية والتقنية]:
 * 1. الحساب الدقيق عالي الحساسية (High-Precision BigDecimal Math):
 *    - حماية العمليات المالية من أخطاء الفاصلة العائمة [Floating Point Errors].
 * 2. الترتيب الزمني الصارم للمعاملات:
 *    - الفرز وفق التاريخ والوقت [timestamp] ثم المعرف [id] لضمان اتساق الأرصدة التراكمية.
 * 3. الفصل المحاسبي بين العملة الأساسية والعملات الأجنبية:
 *    - التمييز بين المعاملات المحسوبة بسعر الصرف والمعاملات الأجنبية الصرفة.
 * 4. حساب الملخص الشامل لكافة العملاء [calculateComprehensiveReport]:
 *    - تجميع إجمالي ما لنا على العملاء وما علينا لهم عبر كافة العملات.
 */
package com.example.data.serialization.pdf

// ---------------------------------------------------------------------
// استيراد الكيانات والنماذج وتكوينات العملات وحزم الحسابات الرياضية
// ---------------------------------------------------------------------
import com.example.data.local.entities.HabayebTransaction
import com.example.domain.model.TransactionType
import com.example.ui.screens.habayeb.utils.CurrencyConfig
import com.example.ui.state.CustomerUiState
import com.example.ui.viewmodel.FinanceConstants
import java.math.BigDecimal

/**
 * [معاملة مالية معالجة ومجهزة للطباعة - ProcessedTransaction]:
 * تحتوي على الكيان الأصلي وبيانات العملة والمبالغ المحسوبة.
 *
 * @property tx المعاملة المالية الأصلية.
 * @property resolvedCurrency رمز العملة النهائي.
 * @property resolvedAmount المبلغ النهائي المعتمد للطباعة.
 * @property isTxForeign هل المعاملة بعملة أجنبية أو محولة بسعر صرف.
 * @property baseCurrencyAmount المبلغ بالعملة الأساسية للتقرير (أو صفر إن كانت أجنبية صرفة).
 * @property pureBaseAmount المبلغ الصافي بالعملة الأساسية دون أسعار صرف.
 */
data class ProcessedTransaction(
    val tx: HabayebTransaction,
    val resolvedCurrency: String,
    val resolvedAmount: BigDecimal,
    val isTxForeign: Boolean,
    val baseCurrencyAmount: BigDecimal,
    val pureBaseAmount: BigDecimal
)

/**
 * [ملخص كشف حساب عميل واحد لـ PDF - SingleCustomerPdfSummary]:
 * يجمع قائمة المعاملات المرتبة وإجماليات المديونيات والمقبوضات وصافي الرصيد.
 *
 * @property sortedProcessedTxs قائمة المعاملات مرتبة زمنياً ومعالجة.
 * @property totalDebts إجمالي المبالغ المدينة (لنا).
 * @property totalPayments إجمالي المقبوضات/المسددات.
 * @property totalDebtsBase إجمالي الديون بالعملة الأساسية الصرفة.
 * @property totalPaymentsBase إجمالي المسددات بالعملة الأساسية الصرفة.
 * @property calculatedNetDebt صافي الرصيد النهائي بالعملة الأساسية.
 * @property uncalculatedForeignSums خريطة أرصدة العملات الأجنبية غير المحولة.
 * @property hasMultipleCurrencies ما إذا كان الحساب يحتوي على أكثر من عملة.
 */
data class SingleCustomerPdfSummary(
    val sortedProcessedTxs: List<ProcessedTransaction>,
    val totalDebts: BigDecimal,
    val totalPayments: BigDecimal,
    val totalDebtsBase: BigDecimal,
    val totalPaymentsBase: BigDecimal,
    val calculatedNetDebt: BigDecimal,
    val uncalculatedForeignSums: Map<String, BigDecimal>,
    val hasMultipleCurrencies: Boolean
)

/**
 * [ملخص تقرير دفتر الحسابات الشامل لـ PDF - ComprehensivePdfSummary]:
 * يجمع إجماليات كافة حسابات العملاء على مستوى المنشأة.
 *
 * @property totalOwedByThem إجمالي ما لنا على جميع العملاء (الديون الخارجية).
 * @property totalOwedToThem إجمالي ما علينا لجميع العملاء (الالتزامات).
 * @property netPrimary صافي الرصيد العام بالعملة الأساسية.
 * @property foreignTotalsMap خريطة إجماليات العملات الأجنبية لكافة الحسابات.
 */
data class ComprehensivePdfSummary(
    val totalOwedByThem: BigDecimal,
    val totalOwedToThem: BigDecimal,
    val netPrimary: BigDecimal,
    val foreignTotalsMap: Map<String, BigDecimal>
)

/**
 * [الكائن الأحادي لمحرك حسابات تقارير PDF - PdfReportCalculator]:
 * يوفر خوارزميات المعالجة المالية والحسابات التراكمية.
 */
object PdfReportCalculator {

    /**
     * [حساب ومعالجة كشف حساب عميل فردي - calculateSingleCustomerReport]:
     * يرتب المعاملات ويفصل الأرصدة ويحسب إجماليات المديونيات والصافي.
     *
     * @param transactions قائمة معاملات العميل الخام من قاعدة البيانات.
     * @param currencySymbol رمز العملة الأساسية المعتمدة للتقرير.
     * @return كائن [SingleCustomerPdfSummary] متكامل ومجهز للعرض.
     */
    fun calculateSingleCustomerReport(
        transactions: List<HabayebTransaction>,
        currencySymbol: String
    ): SingleCustomerPdfSummary {
        val calcResult = com.example.ui.screens.habayeb.utils.CustomerHistoryCalculator.calculate(
            transactions,
            currencySymbol,
            exchangeRatesJson = null
        )

        val normDefaultSymbol = CurrencyConfig.getBySymbol(currencySymbol)?.symbol ?: currencySymbol
        val sortedTxs = transactions.sortedWith(compareBy<HabayebTransaction> { it.timestamp }.thenBy { it.id })

        val processedList = ArrayList<ProcessedTransaction>(sortedTxs.size)
        var totalDebtsBase = BigDecimal.ZERO
        var totalPaymentsBase = BigDecimal.ZERO

        for (tx in sortedTxs) {
            val (resolvedCurrency, resolvedAmount) = CurrencyConfig.getTransactionCurrencyAndAmountBigDecimal(tx, currencySymbol)
            val affectsReportPrimaryCurrency = (resolvedCurrency == normDefaultSymbol)

            // Strictly assign baseCurrencyAmount only if this transaction targets the report's primary currency!
            val baseCurrencyAmount = if (affectsReportPrimaryCurrency) resolvedAmount else BigDecimal.ZERO

            val isTxForeign = (tx.currencyCode != FinanceConstants.DEFAULT_CURRENCY_CODE && 
                               tx.currencyCode.isNotBlank() && 
                               tx.currencyCode != normDefaultSymbol) || 
                              tx.isRateCalculated

            val isPureBase = (tx.currencyCode == FinanceConstants.DEFAULT_CURRENCY_CODE || tx.currencyCode.isBlank() || tx.currencyCode == normDefaultSymbol) && !tx.isRateCalculated
            val pureBaseAmount = if (isPureBase) tx.foreignAmount else BigDecimal.ZERO

            val txType = TransactionType.fromValue(tx.type)
            if (affectsReportPrimaryCurrency) {
                if (txType == TransactionType.OWED_BY_THEM || txType == TransactionType.PAYMENT_TO_THEM) {
                    totalDebtsBase = totalDebtsBase.add(pureBaseAmount)
                } else if (txType == TransactionType.PAYMENT_BY_THEM || txType == TransactionType.OWED_TO_THEM) {
                    totalPaymentsBase = totalPaymentsBase.add(pureBaseAmount)
                }
            }

            processedList.add(
                ProcessedTransaction(
                    tx = tx,
                    resolvedCurrency = resolvedCurrency,
                    resolvedAmount = resolvedAmount,
                    isTxForeign = isTxForeign,
                    baseCurrencyAmount = baseCurrencyAmount,
                    pureBaseAmount = pureBaseAmount
                )
            )
        }

        val owedBy = calcResult.owedByThemBDMap[normDefaultSymbol] ?: BigDecimal.ZERO
        val payTo = calcResult.paymentToThemBDMap[normDefaultSymbol] ?: BigDecimal.ZERO
        val totalDebts = owedBy.add(payTo)

        val payBy = calcResult.paymentByThemBDMap[normDefaultSymbol] ?: BigDecimal.ZERO
        val owedTo = calcResult.owedToThemBDMap[normDefaultSymbol] ?: BigDecimal.ZERO
        val totalPayments = payBy.add(owedTo)

        val calculatedNetDebt = calcResult.netDebtBigDecimalMap[normDefaultSymbol] ?: BigDecimal.ZERO
        val uncalculatedForeignSums = calcResult.netDebtBigDecimalMap.filterKeys { key ->
            key != normDefaultSymbol && ((calcResult.netDebtBigDecimalMap[key] ?: BigDecimal.ZERO).compareTo(BigDecimal.ZERO) != 0)
        }

        val hasMultipleCurrencies = uncalculatedForeignSums.isNotEmpty() || processedList.any { pt -> pt.isTxForeign }

        return SingleCustomerPdfSummary(
            sortedProcessedTxs = processedList,
            totalDebts = totalDebts,
            totalPayments = totalPayments,
            totalDebtsBase = totalDebtsBase,
            totalPaymentsBase = totalPaymentsBase,
            calculatedNetDebt = calculatedNetDebt,
            uncalculatedForeignSums = uncalculatedForeignSums,
            hasMultipleCurrencies = hasMultipleCurrencies
        )
    }

    /**
     * [حساب الملخص المالي الشامل لكافة العملاء - calculateComprehensiveReport]:
     * يجمع إجماليات المديونيات والالتزامات وصافي الرصيد لكافة الحسابات.
     *
     * @param customers قائمة حالات واجهة المستخدم لكافة العملاء.
     * @return كائن [ComprehensivePdfSummary] يحتوي على الإجماليات الموحدة.
     */
    fun calculateComprehensiveReport(
        customers: List<CustomerUiState>
    ): ComprehensivePdfSummary {
        var totalOwedByThem = BigDecimal.ZERO
        var totalOwedToThem = BigDecimal.ZERO
        val foreignTotalsMap = mutableMapOf<String, BigDecimal>()

        for (c in customers) {
            val bdVal = c.defaultCurrencyTotal
            if (bdVal.compareTo(BigDecimal.ZERO) > 0) {
                totalOwedByThem = totalOwedByThem.add(bdVal)
            } else if (bdVal.compareTo(BigDecimal.ZERO) < 0) {
                totalOwedToThem = totalOwedToThem.add(bdVal.abs())
            }
            for ((curr, valBd) in c.foreignDebts) {
                if (valBd.compareTo(BigDecimal.ZERO) != 0) {
                    foreignTotalsMap[curr] = (foreignTotalsMap[curr] ?: BigDecimal.ZERO).add(valBd)
                }
            }
        }

        val netPrimary = totalOwedByThem.subtract(totalOwedToThem)

        return ComprehensivePdfSummary(
            totalOwedByThem = totalOwedByThem,
            totalOwedToThem = totalOwedToThem,
            netPrimary = netPrimary,
            foreignTotalsMap = foreignTotalsMap
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
