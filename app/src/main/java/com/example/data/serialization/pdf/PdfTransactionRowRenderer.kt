/*
 * ╔══════════════════════════════════════════════════════════════════════╗
 * ║ التوثيق العربي الهندسي — MASTERCLASS DOCUMENTATION                 ║
 * ╚══════════════════════════════════════════════════════════════════════╝
 *
 * الملف: app/src/main/java/com/example/data/serialization/pdf/PdfTransactionRowRenderer.kt
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
 * رسم صف المعاملة المالية المفردة داخل جداول وتقارير PDF.
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
// السطر 133: object PdfTransactionRowRenderer — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 144: fun buildTransactionDescriptionText — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 149: val tx — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 150: val isOwedToThemAccount — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 152: val txTypeStr — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 159: val cleanDetails — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 168: val origCurrency — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 169: val origAmount — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 170: val formattedAmount — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 171: val formattedRate — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 174: val origCurrency — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 175: val origAmount — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 176: val formattedAmount — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 192: fun calculateTransactionRowHeight — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 198: val descText — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 199: val textHeight — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 216: fun drawSingleTransactionRow — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 226: val tx — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 227: val isTxForeign — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 228: val hasBaseAmount — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 244: val textYOffset — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 247: val seqNo — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 251: val txTimestampMs — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 252: val txDate — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 253: val dayName — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 254: val formattedDate — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 255: val fullDateStr — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 257: val layoutDate — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 258: val dateYOffset — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 262: val txLabel — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 263: val layoutDesc — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 264: val descYOffset — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 268: val formattedAmount — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 273: val isOwedToThemAccount — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 274: val isCol4 — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 283: val badgeLeft — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 284: val badgeTop — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 285: val badgeRight — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 286: val badgeBottom — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 287: val badgePaint — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 288: val textPaint — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 300: val badgeLeft — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 301: val badgeTop — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 302: val badgeRight — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 303: val badgeBottom — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 304: val badgePaint — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 305: val textPaint — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 312: val formattedRunning — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 313: val isBalanced — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 314: val isPositive — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 315: val runningBalColor — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 320: val paintRunning — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 321: val balText — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// --- نهاية الفهرس التوثيقي ---

/*
 * ╔══════════════════════════════════════════════════════════════════════╗
 * ║ التوثيق العربي الهندسي — MASTERCLASS DOCUMENTATION                 ║
 * ╚══════════════════════════════════════════════════════════════════════╝
 *
 * الملف: /mnt/data/source_full/app/src/main/java/com/example/data/serialization/pdf/PdfTransactionRowRenderer.kt
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
 * رسم صف المعاملة المالية المفردة داخل جداول وتقارير PDF.
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
// السطر 47: object PdfTransactionRowRenderer — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 58: fun buildTransactionDescriptionText — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 63: val tx — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 64: val isOwedToThemAccount — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 66: val txTypeStr — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 73: val cleanDetails — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 82: val origCurrency — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 83: val origAmount — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 84: val formattedAmount — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 85: val formattedRate — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 88: val origCurrency — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 89: val origAmount — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 90: val formattedAmount — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 106: fun calculateTransactionRowHeight — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 112: val descText — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 113: val textHeight — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 130: fun drawSingleTransactionRow — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 140: val tx — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 141: val isTxForeign — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 142: val hasBaseAmount — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 158: val textYOffset — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 161: val seqNo — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 165: val txTimestampMs — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 166: val txDate — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 167: val dayName — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 168: val formattedDate — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 169: val fullDateStr — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 171: val layoutDate — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 172: val dateYOffset — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 176: val txLabel — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 177: val layoutDesc — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 178: val descYOffset — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 182: val formattedAmount — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 187: val isOwedToThemAccount — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 188: val isCol4 — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 197: val badgeLeft — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 198: val badgeTop — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 199: val badgeRight — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 200: val badgeBottom — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 201: val badgePaint — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 202: val textPaint — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 214: val badgeLeft — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 215: val badgeTop — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 216: val badgeRight — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 217: val badgeBottom — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 218: val badgePaint — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 219: val textPaint — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 226: val formattedRunning — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 227: val isBalanced — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 228: val isPositive — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 229: val runningBalColor — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 234: val paintRunning — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// السطر 235: val balText — عنصر موثق ضمن مسؤولية الملف؛ راجع جسم العنصر لمعرفة المدخلات والمخرجات ومسار التنفيذ الفعلي.
// --- نهاية الفهرس التوثيقي ---

/**
 * =====================================================================
 * ملف: رسام صفوف المعاملات الفردية في PDF (PdfTransactionRowRenderer.kt)
 * =====================================================================
 * 
 * [الغرض العام والتعليمي من الملف]:
 * يختص هذا الكائن بالرسم التفصيلي الدقيق لكل معاملة مالية داخل جدول كشف حساب العميل.
 * يتولى مسؤولية صياغة الوصف المالي المفصل (نوع العملية، البيان، ملاحظات سعر الصرف أو النقد الأجنبي)،
 * وقياس الارتفاع الرأسي الديناميكي للصف بناءً على طول الوصف، ورسم الخلايا الست:
 * 1. رقم التسلسل (#).
 * 2. التاريخ واسم اليوم بالعربية.
 * 3. البيان وتفاصيل الصرف بـ [StaticLayout].
 * 4. المبلغ المدين (لنا) مع شارة ملونة.
 * 5. المبلغ الدائن (علينا / دفعة) مع شارة ملونة.
 * 6. الرصيد التراكمي بعد العملية بتلوين دلالي.
 * 
 * [المسؤوليات المعمارية والتقنية]:
 * 1. الصياغة الوصفية المتقدمة للمعاملات (Rich Description Formatting):
 *    - إظهار عمليات تحويل العملات مع سعر الصرف المعتمد والمبلغ الأصلي.
 * 2. الحساب الديناميكي لارتفاع الصف (Dynamic Row Height Calculation):
 *    - قياس أسطر البيان لتفادي تداخل النصوص أو اقتطاعها.
 * 3. رسم الشارات الملونة للأرقام (Visual Badge Embellishment):
 *    - رسم مستطيلات ذات حواف منحنية خلف المبالغ للتمييز السريع بين المقبوضات والمديونيات.
 */
package com.example.data.serialization.pdf

// ---------------------------------------------------------------------
// استيراد حزم سياق أندرويد والرسومات وتخطيط النصوص والرياضيات والتواريخ
// ---------------------------------------------------------------------
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.text.Layout
import com.example.R
import com.example.data.serialization.pdf.PdfDrawingUtils.drawArabicText
import com.example.domain.model.TransactionType
import com.example.ui.helper.HabayebMathHelper
import com.example.ui.screens.habayeb.utils.CurrencyConfig
import java.math.BigDecimal
import java.util.Date

/**
 * [الكائن الأحادي لرسم صفوف المعاملات - PdfTransactionRowRenderer]:
 * يقدم وظائف بناء البيان وحساب الارتفاع ورسم صفوف المعاملات الفردية.
 */
object PdfTransactionRowRenderer {

    /**
     * [بناء النص التوضيحي المفصل للمعاملة - buildTransactionDescriptionText]:
     * يجمع نوع الحركة مع البيان المخصص ومعلومات سعر الصرف أو النقد الأجنبي.
     *
     * @param context سياق التطبيق لجلب مسميات أنواع المعاملات.
     * @param pt كائن المعاملة المعالجة.
     * @param initialType طبيعة الحساب الأصلية (لنا أم علينا).
     * @return النص التوضيحي المكتمل للطباعة.
     */
    fun buildTransactionDescriptionText(
        context: Context,
        pt: ProcessedTransaction,
        initialType: String = TransactionType.OWED_BY_THEM.value
    ): String {
        val tx = pt.tx
        val isOwedToThemAccount = initialType == TransactionType.OWED_TO_THEM.value

        val txTypeStr = when (tx.type) {
            TransactionType.OWED_BY_THEM.value -> context.getString(R.string.pdf_tx_type_owed_by_them)
            TransactionType.PAYMENT_BY_THEM.value -> if (isOwedToThemAccount) context.getString(R.string.pdf_tx_type_payment_to_them) else context.getString(R.string.pdf_tx_type_payment_by_them)
            TransactionType.OWED_TO_THEM.value -> context.getString(R.string.pdf_tx_type_owed_to_them)
            TransactionType.PAYMENT_TO_THEM.value -> context.getString(R.string.pdf_tx_type_payment_to_them)
            else -> context.getString(R.string.pdf_tx_type_new)
        }
        val cleanDetails = CurrencyConfig.getCleanDetails(tx.description)

        return buildString {
            append(txTypeStr)
            if (cleanDetails.isNotEmpty()) {
                append(" - ")
                append(cleanDetails)
            }
            if (tx.isRateCalculated) {
                val origCurrency = CurrencyConfig.getBySymbol(tx.currencyCode)?.symbol ?: tx.currencyCode
                val origAmount = if (tx.foreignAmount.compareTo(BigDecimal.ZERO) > 0) tx.foreignAmount else tx.amount
                val formattedAmount = HabayebMathHelper.formatSmart(origAmount)
                val formattedRate = HabayebMathHelper.formatRate(tx.exchangeRate)
                append("\n[ صرف: $formattedAmount $origCurrency × $formattedRate ]")
            } else if (pt.isTxForeign) {
                val origCurrency = CurrencyConfig.getBySymbol(tx.currencyCode)?.symbol ?: tx.currencyCode
                val origAmount = if (tx.foreignAmount.compareTo(BigDecimal.ZERO) > 0) tx.foreignAmount else tx.amount
                val formattedAmount = HabayebMathHelper.formatSmart(origAmount)
                append("\n[ $formattedAmount $origCurrency - نقد أجنبي ]")
            }
        }
    }

    /**
     * [حساب الارتفاع الرأسي لصف المعاملة - calculateTransactionRowHeight]:
     * يقيس ارتفاع النص المتولد ضمن العرض المتاح مع إضافة الهوامش القياسية.
     *
     * @param context سياق التطبيق.
     * @param pt كائن المعاملة المعالجة.
     * @param initialType طبيعة الحساب.
     * @param availableWidth العرض المخصص لعمود البيان بالنقاط (افتراضياً 190).
     * @return الارتفاع الرأسي المحسوب بالنقاط (بحد أدنى 32 نقطة).
     */
    fun calculateTransactionRowHeight(
        context: Context,
        pt: ProcessedTransaction,
        initialType: String = TransactionType.OWED_BY_THEM.value,
        availableWidth: Int = 190
    ): Float {
        val descText = buildTransactionDescriptionText(context, pt, initialType)
        val textHeight = PdfDrawingUtils.measureTextHeight(descText, PdfPaints.textPaintDesc, availableWidth)
        return (textHeight + 14f).coerceAtLeast(32f)
    }

    /**
     * [رسم صف معاملة فردية في كشف الحساب - drawSingleTransactionRow]:
     * يرسم خلايا الصف الست وخطوط الشبكة والشارات التوضيحية للأرصدة.
     *
     * @param canvas لوحة الرسم.
     * @param context سياق التطبيق.
     * @param index ترتيب المعاملة التسلسلي (يبدأ من 0).
     * @param pt كائن المعاملة المعالجة.
     * @param currentY الإحداثي الرأسي للرسم.
     * @param rowHeight الارتفاع المحسوب للصف.
     * @param runningBal الرصيد التراكمي المحسوب بعد هذه المعاملة.
     * @param initialType طبيعة الحساب.
     */
    fun drawSingleTransactionRow(
        canvas: Canvas,
        context: Context,
        index: Int,
        pt: ProcessedTransaction,
        currentY: Float,
        rowHeight: Float,
        runningBal: BigDecimal,
        initialType: String = TransactionType.OWED_BY_THEM.value
    ) {
        val tx = pt.tx
        val isTxForeign = pt.isTxForeign
        val hasBaseAmount = pt.baseCurrencyAmount.compareTo(BigDecimal.ZERO) > 0

        if (isTxForeign) {
            canvas.drawRect(25f, currentY, 570f, currentY + rowHeight, PdfPaints.paintForeignBg)
        }

        // Horizontal bottom divider
        canvas.drawLine(25f, currentY + rowHeight, 570f, currentY + rowHeight, PdfPaints.paintRowDivider)

        // Vertical grid lines between columns
        canvas.drawLine(545f, currentY, 545f, currentY + rowHeight, PdfPaints.paintRowDivider)
        canvas.drawLine(455f, currentY, 455f, currentY + rowHeight, PdfPaints.paintRowDivider)
        canvas.drawLine(260f, currentY, 260f, currentY + rowHeight, PdfPaints.paintRowDivider)
        canvas.drawLine(180f, currentY, 180f, currentY + rowHeight, PdfPaints.paintRowDivider)
        canvas.drawLine(100f, currentY, 100f, currentY + rowHeight, PdfPaints.paintRowDivider)

        val textYOffset = (rowHeight - 12f) / 2f

        // Col 1: Sequence Number (#)
        val seqNo = (index + 1).toString()
        drawArabicText(canvas, seqNo, 545f, currentY + textYOffset, 25, PdfPaints.paintCellNormal, Layout.Alignment.ALIGN_CENTER)

        // Col 2: Date & Day together in a single row
        val txTimestampMs = if (tx.timestamp > 1000000000000L) tx.timestamp else tx.timestamp * 1000
        val txDate = Date(txTimestampMs)
        val dayName = try { PdfPageRenderer.formatDayAr(txDate) } catch (e: Exception) { "" }
        val formattedDate = try { PdfPageRenderer.formatDateEn(txDate) } catch (e: Exception) { "" }
        val fullDateStr = if (dayName.isNotBlank()) "$dayName $formattedDate" else formattedDate

        val layoutDate = PdfDrawingUtils.createStaticLayout(fullDateStr, PdfPaints.paintDateText, 90, Layout.Alignment.ALIGN_CENTER)
        val dateYOffset = ((rowHeight - layoutDate.height) / 2f).coerceAtLeast(2f)
        PdfDrawingUtils.drawStaticLayout(canvas, layoutDate, 455f, currentY + dateYOffset)

        // Col 3: Details with Dynamic StaticLayout
        val txLabel = buildTransactionDescriptionText(context, pt, initialType)
        val layoutDesc = PdfDrawingUtils.createStaticLayout(txLabel, PdfPaints.textPaintDesc, 190, Layout.Alignment.ALIGN_NORMAL)
        val descYOffset = ((rowHeight - layoutDesc.height) / 2f).coerceAtLeast(2f)
        PdfDrawingUtils.drawStaticLayout(canvas, layoutDesc, 262f, currentY + descYOffset)

        // Amounts
        val formattedAmount = if (hasBaseAmount) {
            HabayebMathHelper.formatSmart(pt.baseCurrencyAmount)
        } else "-"

        // Col 4 & Col 5
        val isOwedToThemAccount = initialType == TransactionType.OWED_TO_THEM.value
        val isCol4 = if (isOwedToThemAccount) {
            tx.type == TransactionType.OWED_TO_THEM.value || tx.type == TransactionType.PAYMENT_BY_THEM.value
        } else {
            tx.type == TransactionType.OWED_BY_THEM.value || tx.type == TransactionType.PAYMENT_TO_THEM.value
        }

        if (hasBaseAmount) {
            if (isCol4) {
                // Col 4 Badge & Text (180f)
                val badgeLeft = 184f
                val badgeTop = currentY + ((rowHeight - 18f) / 2f)
                val badgeRight = 256f
                val badgeBottom = badgeTop + 18f
                val badgePaint = if (isOwedToThemAccount) PdfPaints.paintPaymentBg else PdfPaints.paintOwedBg
                val textPaint = if (isOwedToThemAccount) PdfPaints.paintPaymentText else PdfPaints.paintOwedText

                canvas.drawRoundRect(badgeLeft, badgeTop, badgeRight, badgeBottom, 3f, 3f, badgePaint)
                drawArabicText(canvas, formattedAmount, 180f, currentY + textYOffset, 80, textPaint, Layout.Alignment.ALIGN_CENTER)

                // Col 5: empty dash (-)
                drawArabicText(canvas, "-", 100f, currentY + textYOffset, 80, PdfPaints.paintEmptyDash, Layout.Alignment.ALIGN_CENTER)
            } else {
                // Col 4: empty dash (-)
                drawArabicText(canvas, "-", 180f, currentY + textYOffset, 80, PdfPaints.paintEmptyDash, Layout.Alignment.ALIGN_CENTER)

                // Col 5 Badge & Text (100f)
                val badgeLeft = 104f
                val badgeTop = currentY + ((rowHeight - 18f) / 2f)
                val badgeRight = 176f
                val badgeBottom = badgeTop + 18f
                val badgePaint = if (isOwedToThemAccount) PdfPaints.paintOwedBg else PdfPaints.paintPaymentBg
                val textPaint = if (isOwedToThemAccount) PdfPaints.paintOwedText else PdfPaints.paintPaymentText

                canvas.drawRoundRect(badgeLeft, badgeTop, badgeRight, badgeBottom, 3f, 3f, badgePaint)
                drawArabicText(canvas, formattedAmount, 100f, currentY + textYOffset, 80, textPaint, Layout.Alignment.ALIGN_CENTER)
            }

            // Col 6: Running Balance (الرصيد)
            val formattedRunning = HabayebMathHelper.formatSmart(runningBal.abs())
            val isBalanced = runningBal.compareTo(BigDecimal.ZERO) == 0
            val isPositive = runningBal.compareTo(BigDecimal.ZERO) > 0
            val runningBalColor = when {
                isBalanced -> PdfColors.TEXT_DARK
                isOwedToThemAccount -> if (isPositive) PdfColors.PAYMENT_TEXT else PdfColors.OWED_TEXT
                else -> if (isPositive) PdfColors.OWED_TEXT else PdfColors.PAYMENT_TEXT
            }
            val paintRunning = Paint(PdfPaints.paintCellBold).apply { color = Color.parseColor(runningBalColor) }
            val balText = if (isBalanced) "-" else formattedRunning
            drawArabicText(canvas, balText, 25f, currentY + textYOffset, 75, paintRunning, Layout.Alignment.ALIGN_CENTER)
        } else {
            // Transaction belongs to another currency ledger, so it doesn't affect this ledger's amounts
            drawArabicText(canvas, "-", 180f, currentY + textYOffset, 80, PdfPaints.paintEmptyDash, Layout.Alignment.ALIGN_CENTER)
            drawArabicText(canvas, "-", 100f, currentY + textYOffset, 80, PdfPaints.paintEmptyDash, Layout.Alignment.ALIGN_CENTER)
            drawArabicText(canvas, "-", 25f, currentY + textYOffset, 75, PdfPaints.paintEmptyDash, Layout.Alignment.ALIGN_CENTER)
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
