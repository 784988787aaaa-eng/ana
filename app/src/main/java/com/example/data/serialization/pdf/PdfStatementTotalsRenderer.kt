/*
 * ╔══════════════════════════════════════════════════════════════════════╗
 * ║ التوثيق العربي الهندسي — MASTERCLASS DOCUMENTATION                 ║
 * ╚══════════════════════════════════════════════════════════════════════╝
 *
 * الملف: app/src/main/java/com/example/data/serialization/pdf/PdfStatementTotalsRenderer.kt
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
 * رسم الإجماليات والملخصات الحسابية في كشف PDF.
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
// السطر 114: object PdfStatementTotalsRenderer — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 129: fun drawTotalsRow — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 138: val rowHeight — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 139: val paintBg — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 154: val paintTitle — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 165: val isOwedToThemAccount — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 166: val col4Color — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 167: val col5Color — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 169: val formattedDebts — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 170: val paintDebts — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 175: val formattedPayments — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 176: val paintPayments — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 198: fun drawFinalNetBanner — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 206: val bannerHeight — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 207: val isOwedToThemAccount — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 208: val rawPositive — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 209: val rawNegative — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 211: val isOwedToThemStatus — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 212: val isOwedByThemStatus — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 214: val bannerBgColor — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 220: val paintBannerBg — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 224: val paintBannerBorder — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 233: val statusText — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 239: val textColor — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 245: val paintTextLabel — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 252: val formattedAmount — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 271: fun drawForeignCurrenciesSummary — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 280: var y — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 281: val itemHeight — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 282: val boxHeight — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 284: val paintBg — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 288: val paintBorder — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 293: val paintTitle — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 299: val paintItem — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 311: var itemY — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 313: val isPositive — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 314: val isNegative — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 315: val statusText — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 316: val foreignTag — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 317: val lineStr — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// --- نهاية الفهرس التوثيقي ---

/*
 * ╔══════════════════════════════════════════════════════════════════════╗
 * ║ التوثيق العربي الهندسي — MASTERCLASS DOCUMENTATION                 ║
 * ╚══════════════════════════════════════════════════════════════════════╝
 *
 * الملف: /mnt/data/source_full/app/src/main/java/com/example/data/serialization/pdf/PdfStatementTotalsRenderer.kt
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
 * رسم الإجماليات والملخصات الحسابية في كشف PDF.
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
// السطر 41: object PdfStatementTotalsRenderer — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 56: fun drawTotalsRow — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 65: val rowHeight — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 66: val paintBg — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 81: val paintTitle — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 92: val isOwedToThemAccount — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 93: val col4Color — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 94: val col5Color — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 96: val formattedDebts — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 97: val paintDebts — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 102: val formattedPayments — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 103: val paintPayments — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 125: fun drawFinalNetBanner — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 133: val bannerHeight — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 134: val isOwedToThemAccount — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 135: val rawPositive — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 136: val rawNegative — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 138: val isOwedToThemStatus — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 139: val isOwedByThemStatus — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 141: val bannerBgColor — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 147: val paintBannerBg — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 151: val paintBannerBorder — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 160: val statusText — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 166: val textColor — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 172: val paintTextLabel — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 179: val formattedAmount — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 198: fun drawForeignCurrenciesSummary — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 207: var y — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 208: val itemHeight — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 209: val boxHeight — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 211: val paintBg — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 215: val paintBorder — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 220: val paintTitle — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 226: val paintItem — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 238: var itemY — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 240: val isPositive — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 241: val isNegative — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 242: val statusText — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 243: val foreignTag — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 244: val lineStr — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// --- نهاية الفهرس التوثيقي ---

/**
 * =====================================================================
 * ملف: رسام إجماليات كشف الحساب وأشرطة الصافي (PdfStatementTotalsRenderer.kt)
 * =====================================================================
 * 
 * [الغرض العام والتعليمي من الملف]:
 * يختص هذا الكائن برسم وتنسيق الخواتيم المالية لكشوف حسابات العملاء في تقارير PDF.
 * يشمل ذلك صف مجموع المديونيات والمقبوضات [drawTotalsRow]،
 * والشريط البارز الملون لصافي الرصيد النهائي مع تحديد حالته (رصيد لنا / رصيد له / الحساب متزن) [drawFinalNetBanner]،
 * وصندوق ملخص مديونيات العملات الأجنبية غير المحولة [drawForeignCurrenciesSummary].
 * 
 * [المسؤوليات المعمارية والتقنية]:
 * 1. رسم صف إجماليات العمليات (Totals Row Rendering):
 *    - محاذاة مجاميع المدين والدائن مع أعمدة الجدول وتطبيق الألوان الدلالية.
 * 2. التلوين الشرطي لشريط الصافي النهائي (Conditional Net Balance Banner):
 *    - تطبيق اللون الأخضر للمستحقات (له)، والأحمر للمديونيات (لنا)، والرمادي للاتزان التام.
 * 3. حصر وتنسيق العملات الأجنبية المستقلة (Multi-Currency Breakdown):
 *    - رسم صندوق ذي زوايا منحنية يعرض تفاصيل أرصدة العملات الأجنبية كلاً على حدة.
 */
package com.example.data.serialization.pdf

// ---------------------------------------------------------------------
// استيراد حزم سياق أندرويد والرسومات وتخطيط النصوص والرياضيات المالية
// ---------------------------------------------------------------------
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.text.Layout
import com.example.R
import com.example.data.serialization.pdf.PdfDrawingUtils.drawArabicText
import com.example.domain.model.TransactionType
import com.example.ui.helper.HabayebMathHelper
import java.math.BigDecimal

/**
 * [الكائن الأحادي لرسم إجماليات كشف الحساب - PdfStatementTotalsRenderer]:
 * يحتوي على دوال رسم الصفوف الختامية وصناديق ملخصات الأرصدة.
 */
object PdfStatementTotalsRenderer {

    /**
     * [رسم صف إجماليات كشف الحساب - drawTotalsRow]:
     * يرسم صفاً مميزاً بلون خلفية خاص يجمع عمودي المدين والدائن.
     *
     * @param canvas لوحة الرسم الحالية.
     * @param context سياق التطبيق لجلب النصوص المترجمة.
     * @param currentY الإحداثي الرأسي لبدء رسم الصف.
     * @param totalDebts إجمالي المبالغ المدينة.
     * @param totalPayments إجمالي المقبوضات/المسددات.
     * @param currencySymbol رمز العملة.
     * @param initialType طبيعة الحساب الأصلية (لنا أم علينا).
     * @return الإحداثي الرأسي Y بعد اكتمال رسم الصف.
     */
    fun drawTotalsRow(
        canvas: Canvas,
        context: Context,
        currentY: Float,
        totalDebts: BigDecimal,
        totalPayments: BigDecimal,
        currencySymbol: String,
        initialType: String = TransactionType.OWED_BY_THEM.value
    ): Float {
        val rowHeight = 24f
        val paintBg = Paint().apply {
            color = Color.parseColor(PdfColors.TOTALS_ROW_BG)
            style = Paint.Style.FILL
        }
        canvas.drawRect(25f, currentY, 570f, currentY + rowHeight, paintBg)
        canvas.drawLine(25f, currentY, 570f, currentY, PdfPaints.paintRowDivider)
        canvas.drawLine(25f, currentY + rowHeight, 570f, currentY + rowHeight, PdfPaints.paintRowDivider)

        // Draw vertical column dividers
        canvas.drawLine(545f, currentY, 545f, currentY + rowHeight, PdfPaints.paintRowDivider)
        canvas.drawLine(455f, currentY, 455f, currentY + rowHeight, PdfPaints.paintRowDivider)
        canvas.drawLine(260f, currentY, 260f, currentY + rowHeight, PdfPaints.paintRowDivider)
        canvas.drawLine(180f, currentY, 180f, currentY + rowHeight, PdfPaints.paintRowDivider)
        canvas.drawLine(100f, currentY, 100f, currentY + rowHeight, PdfPaints.paintRowDivider)

        val paintTitle = Paint().apply {
            color = Color.parseColor(PdfColors.TEXT_DARK)
            textSize = 9.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        drawArabicText(canvas, "-", 545f, currentY + 5f, 25, PdfPaints.paintEmptyDash, Layout.Alignment.ALIGN_CENTER)
        drawArabicText(canvas, "-", 455f, currentY + 5f, 90, PdfPaints.paintEmptyDash, Layout.Alignment.ALIGN_CENTER)
        drawArabicText(canvas, context.getString(R.string.pdf_totals_operations_title), 260f, currentY + 5f, 195, paintTitle, Layout.Alignment.ALIGN_NORMAL)

        val isOwedToThemAccount = initialType == TransactionType.OWED_TO_THEM.value
        val col4Color = if (isOwedToThemAccount) PdfColors.PAYMENT_TEXT else PdfColors.OWED_TEXT
        val col5Color = if (isOwedToThemAccount) PdfColors.OWED_TEXT else PdfColors.PAYMENT_TEXT

        val formattedDebts = HabayebMathHelper.formatSmart(totalDebts)
        val paintDebts = Paint(PdfPaints.paintCellBold).apply {
            color = Color.parseColor(col4Color)
        }
        drawArabicText(canvas, formattedDebts, 180f, currentY + 5f, 80, paintDebts, Layout.Alignment.ALIGN_CENTER)

        val formattedPayments = HabayebMathHelper.formatSmart(totalPayments)
        val paintPayments = Paint(PdfPaints.paintCellBold).apply {
            color = Color.parseColor(col5Color)
        }
        drawArabicText(canvas, formattedPayments, 100f, currentY + 5f, 80, paintPayments, Layout.Alignment.ALIGN_CENTER)

        drawArabicText(canvas, "-", 25f, currentY + 5f, 75, PdfPaints.paintEmptyDash, Layout.Alignment.ALIGN_CENTER)

        return currentY + rowHeight
    }

    /**
     * [رسم شريط الصافي النهائي الملون - drawFinalNetBanner]:
     * يرسم صندوقاً عريضاً بحواف مستديرة ولون دلالي يوضح موقف الحساب الإجمالي.
     *
     * @param canvas لوحة الرسم.
     * @param context سياق التطبيق.
     * @param currentY الإحداثي الرأسي.
     * @param netBalance صافي الرصيد المحسوب.
     * @param currencySymbol رمز العملة.
     * @param initialType طبيعة الحساب.
     * @return الإحداثي الرأسي Y التالي.
     */
    fun drawFinalNetBanner(
        canvas: Canvas,
        context: Context,
        currentY: Float,
        netBalance: BigDecimal,
        currencySymbol: String,
        initialType: String = TransactionType.OWED_BY_THEM.value
    ): Float {
        val bannerHeight = 30f
        val isOwedToThemAccount = initialType == TransactionType.OWED_TO_THEM.value
        val rawPositive = netBalance.compareTo(BigDecimal.ZERO) > 0
        val rawNegative = netBalance.compareTo(BigDecimal.ZERO) < 0

        val isOwedToThemStatus = if (isOwedToThemAccount) rawPositive else rawNegative
        val isOwedByThemStatus = if (isOwedToThemAccount) rawNegative else rawPositive

        val bannerBgColor = when {
            isOwedByThemStatus -> PdfColors.BANNER_OWED_BG
            isOwedToThemStatus -> PdfColors.BANNER_PAYMENT_BG
            else -> PdfColors.TOTALS_ROW_BG
        }

        val paintBannerBg = Paint().apply {
            color = Color.parseColor(bannerBgColor)
            style = Paint.Style.FILL
        }
        val paintBannerBorder = Paint().apply {
            color = Color.parseColor(if (isOwedByThemStatus) PdfColors.DEBT_BORDER else if (isOwedToThemStatus) PdfColors.CREDIT_BORDER else PdfColors.HEADER_BORDER)
            strokeWidth = 1f
            style = Paint.Style.STROKE
        }

        canvas.drawRoundRect(25f, currentY, 570f, currentY + bannerHeight, 4f, 4f, paintBannerBg)
        canvas.drawRoundRect(25f, currentY, 570f, currentY + bannerHeight, 4f, 4f, paintBannerBorder)

        val statusText = when {
            isOwedByThemStatus -> context.getString(R.string.pdf_net_banner_owed_by)
            isOwedToThemStatus -> context.getString(R.string.pdf_net_banner_owed_to)
            else -> context.getString(R.string.pdf_net_banner_balanced)
        }

        val textColor = when {
            isOwedByThemStatus -> PdfColors.OWED_TEXT
            isOwedToThemStatus -> PdfColors.PAYMENT_TEXT
            else -> PdfColors.TEXT_DARK
        }

        val paintTextLabel = Paint().apply {
            color = Color.parseColor(textColor)
            textSize = 10.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val formattedAmount = "${HabayebMathHelper.formatSmart(netBalance.abs())} $currencySymbol"

        drawArabicText(canvas, statusText, 250f, currentY + 7f, 310, paintTextLabel, Layout.Alignment.ALIGN_NORMAL)
        drawArabicText(canvas, formattedAmount, 35f, currentY + 7f, 200, paintTextLabel, Layout.Alignment.ALIGN_OPPOSITE)

        return currentY + bannerHeight + 8f
    }

    /**
     * [رسم ملخص مديونيات العملات الأجنبية - drawForeignCurrenciesSummary]:
     * يرسم صندوقاً تفصيلياً يوضح أرصدة كل عملة أجنبية لم يتم تحويلها بسعر صرف.
     *
     * @param canvas لوحة الرسم.
     * @param context سياق التطبيق.
     * @param currentY الإحداثي الرأسي.
     * @param uncalculatedForeignSums خريطة أرصدة العملات الأجنبية.
     * @param currencySymbol رمز العملة الأساسية.
     * @return الإحداثي الرأسي Y التالي.
     */
    fun drawForeignCurrenciesSummary(
        canvas: Canvas,
        context: Context,
        currentY: Float,
        uncalculatedForeignSums: Map<String, BigDecimal>,
        currencySymbol: String
    ): Float {
        if (uncalculatedForeignSums.isEmpty()) return currentY

        var y = currentY + 4f
        val itemHeight = 20f
        val boxHeight = 24f + (uncalculatedForeignSums.size * itemHeight)

        val paintBg = Paint().apply {
            color = Color.parseColor(PdfColors.CARD_BG)
            style = Paint.Style.FILL
        }
        val paintBorder = Paint().apply {
            color = Color.parseColor(PdfColors.HEADER_BORDER)
            strokeWidth = 1f
            style = Paint.Style.STROKE
        }
        val paintTitle = Paint().apply {
            color = Color.parseColor(PdfColors.PRIMARY_EMERALD)
            textSize = 9.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        val paintItem = Paint().apply {
            color = Color.parseColor(PdfColors.TEXT_CHARCOAL)
            textSize = 9f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        canvas.drawRoundRect(25f, y, 570f, y + boxHeight, 4f, 4f, paintBg)
        canvas.drawRoundRect(25f, y, 570f, y + boxHeight, 4f, 4f, paintBorder)

        drawArabicText(canvas, context.getString(R.string.pdf_independent_totals_uncalculated), 35f, y + 6f, 520, paintTitle, Layout.Alignment.ALIGN_NORMAL)

        var itemY = y + 24f
        for ((curr, amount) in uncalculatedForeignSums) {
            val isPositive = amount.compareTo(BigDecimal.ZERO) > 0
            val isNegative = amount.compareTo(BigDecimal.ZERO) < 0
            val statusText = if (isPositive) context.getString(R.string.pdf_status_owed_word) else if (isNegative) context.getString(R.string.pdf_status_to_him_word) else context.getString(R.string.pdf_status_balanced_word)
            val foreignTag = context.getString(R.string.pdf_foreign_currency_tag)
            val lineStr = "• ${context.getString(R.string.pdf_total_currency_prefix, curr)}: ${HabayebMathHelper.formatSmart(amount.abs())} $curr ($statusText - $foreignTag)"
            drawArabicText(canvas, lineStr, 40f, itemY, 510, paintItem, Layout.Alignment.ALIGN_NORMAL)
            itemY += itemHeight
        }

        return y + boxHeight + 6f
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
