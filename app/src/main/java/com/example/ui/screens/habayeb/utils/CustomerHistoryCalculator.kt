/*
 * ╔══════════════════════════════════════════════════════════════════════╗
 * ║ التوثيق العربي الهندسي — MASTERCLASS / BATCH 07                   ║
 * ╚══════════════════════════════════════════════════════════════════════╝
 *
 * الملف: app/src/main/java/com/example/ui/screens/habayeb/utils/CustomerHistoryCalculator.kt
 * القطاع المعماري: Habayeb UI/UX.
 *
 * الوصف المعماري:
 * مكوّن مخصص للعملاء (CustomerHistoryCalculator) يعرض أو يدير تفاعلاً محدداً ضمن دورة حياة حساب العميل.
 *
 * الرؤية التعليمية والبصرية:
 * عند قراءة هذا الملف، تخيل شاشة الهاتف في واجهة «الحبايب»: كل عنصر Compose
 * ظاهر أمام المستخدم له هنا تمثيل برمجي يحدد موضعه، حالته، وما يحدث بعد النقر
 * أو الإدخال أو السحب أو الاختيار. الملف يصف طبقة العرض والتنسيق؛ أما الحسابات
 * المالية ومصادر البيانات فتظل في العقود التي يستدعيها الكود الأصلي.
 *
 * بروتوكول القدسية البرمجية:
 * تم إدراج النص التنفيذي الأصلي كما هو حرفياً بعد هذا الرأس، دون حذف أو تعديل
 * أو إعادة ترتيب لأي تعليمة. جميع الإضافات التوثيقية في هذا الملف تعليقات فقط.
 * البصمة SHA-256 للنص الأصلي قبل التوثيق: 3ac024089bf403ca5f5ac1ce1cd06094dc4f6cf505687fc6e36737b33b47786e
 *
 * --- الفهرس السطري التعليمي ---
 * السطر 1: تعريف الحزمة التي ينتمي إليها الملف.
 * السطر 3: استيراد اعتماد خارجي/داخلي تستخدمه الشيفرة الأصلية.
 * السطر 4: استيراد اعتماد خارجي/داخلي تستخدمه الشيفرة الأصلية.
 * السطر 5: استيراد اعتماد خارجي/داخلي تستخدمه الشيفرة الأصلية.
 * السطر 6: استيراد اعتماد خارجي/داخلي تستخدمه الشيفرة الأصلية.
 * السطر 8: تعليمة annotation تضبط سلوك المترجم أو Compose أو إطار العمل.
 * السطر 9: تعريف نوع/كلاس/كائن أو alias؛ يمثل عقداً معمارياً في الملف.
 * السطر 10: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 11: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 12: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 13: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 14: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 16: تعليمة annotation تضبط سلوك المترجم أو Compose أو إطار العمل.
 * السطر 17: تعريف نوع/كلاس/كائن أو alias؛ يمثل عقداً معمارياً في الملف.
 * السطر 18: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 19: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 20: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 21: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 22: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 23: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 24: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 25: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 26: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 27: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 28: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 29: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 30: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 31: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 32: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 33: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 34: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 35: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 36: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 37: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 39: تعليق أصلي من الشيفرة؛ محفوظ دون تعديل.
 * السطر 40: تعليق أصلي من الشيفرة؛ محفوظ دون تعديل.
 * السطر 41: تعليق أصلي من الشيفرة؛ محفوظ دون تعديل.
 * السطر 42: تعليق أصلي من الشيفرة؛ محفوظ دون تعديل.
 * السطر 43: تعليق أصلي من الشيفرة؛ محفوظ دون تعديل.
 * السطر 44: تعليق أصلي من الشيفرة؛ محفوظ دون تعديل.
 * السطر 45: تعريف نوع/كلاس/كائن أو alias؛ يمثل عقداً معمارياً في الملف.
 * السطر 46: تعريف دالة؛ نقطة تنفيذ أو Composable تستقبل مدخلات وتنتج أثراً أو واجهة.
 * السطر 47: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 48: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 49: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 50: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 51: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 52: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 54: تعليق أصلي من الشيفرة؛ محفوظ دون تعديل.
 * السطر 55: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 56: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 57: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 59: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 60: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 61: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 62: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 64: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 65: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 67: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 68: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 69: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 71: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 72: تكرار على عناصر أو عدد محدد وفق التنفيذ الأصلي.
 * السطر 73: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 74: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 76: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 77: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 79: تعليق أصلي من الشيفرة؛ محفوظ دون تعديل.
 * السطر 80: اختيار مسار تنفيذ بناءً على حالة/قيمة محددة.
 * السطر 81: تعبير lambda يحدد الإجراء الناتج عن تفاعل أو تحويل.
 * السطر 82: تعبير lambda يحدد الإجراء الناتج عن تفاعل أو تحويل.
 * السطر 83: تعبير lambda يحدد الإجراء الناتج عن تفاعل أو تحويل.
 * السطر 84: تعبير lambda يحدد الإجراء الناتج عن تفاعل أو تحويل.
 * السطر 85: المسار البديل للشرط السابق.
 * السطر 86: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 88: تعليق أصلي من الشيفرة؛ محفوظ دون تعديل.
 * السطر 89: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 90: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 91: تعبير lambda يحدد الإجراء الناتج عن تفاعل أو تحويل.
 * السطر 92: تعبير lambda يحدد الإجراء الناتج عن تفاعل أو تحويل.
 * السطر 93: المسار البديل للشرط السابق.
 * السطر 94: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 96: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 97: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 98: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 99: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 101: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 102: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 104: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 105: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 106: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 107: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 108: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 109: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 111: تكرار على عناصر أو عدد محدد وفق التنفيذ الأصلي.
 * السطر 112: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 113: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 114: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 115: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 117: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 118: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 119: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 120: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 122: تعليق أصلي من الشيفرة؛ محفوظ دون تعديل.
 * السطر 123: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 124: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 125: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 126: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 128: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 129: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 131: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 132: فرع شرطي يحدد مسار التنفيذ حسب الحالة الحالية.
 * السطر 133: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 134: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 135: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 136: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 137: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 138: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 139: فرع شرطي يحدد مسار التنفيذ حسب الحالة الحالية.
 * السطر 140: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 141: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 142: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 143: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 144: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 145: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 146: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 148: إرجاع قيمة إلى المستدعي وفق العقد الأصلي.
 * السطر 149: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 150: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 151: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 152: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 153: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 154: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 155: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 156: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 157: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 158: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 159: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 160: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 161: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 162: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 163: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 164: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 165: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 167: تعريف دالة؛ نقطة تنفيذ أو Composable تستقبل مدخلات وتنتج أثراً أو واجهة.
 * السطر 168: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 169: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 170: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 171: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 172: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 173: فرع شرطي يحدد مسار التنفيذ حسب الحالة الحالية.
 * السطر 174: إرجاع قيمة إلى المستدعي وفق العقد الأصلي.
 * السطر 175: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 176: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 177: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 178: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 179: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 180: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 182: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 183: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 184: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 185: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 186: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 187: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 188: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 190: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 192: تكرار على عناصر أو عدد محدد وفق التنفيذ الأصلي.
 * السطر 193: فرع شرطي يحدد مسار التنفيذ حسب الحالة الحالية.
 * السطر 194: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 195: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 196: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 197: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 199: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 200: اختيار مسار تنفيذ بناءً على حالة/قيمة محددة.
 * السطر 201: تعبير lambda يحدد الإجراء الناتج عن تفاعل أو تحويل.
 * السطر 202: تعبير lambda يحدد الإجراء الناتج عن تفاعل أو تحويل.
 * السطر 203: تعبير lambda يحدد الإجراء الناتج عن تفاعل أو تحويل.
 * السطر 204: تعبير lambda يحدد الإجراء الناتج عن تفاعل أو تحويل.
 * السطر 205: المسار البديل للشرط السابق.
 * السطر 206: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 207: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 209: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 210: تكرار على عناصر أو عدد محدد وفق التنفيذ الأصلي.
 * السطر 211: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 212: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 213: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 214: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 215: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 216: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 218: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 219: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 220: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 221: فرع شرطي يحدد مسار التنفيذ حسب الحالة الحالية.
 * السطر 222: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 223: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 224: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 225: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 226: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 227: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 228: فرع شرطي يحدد مسار التنفيذ حسب الحالة الحالية.
 * السطر 229: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 230: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 231: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 232: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 233: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 234: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 235: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 237: إرجاع قيمة إلى المستدعي وفق العقد الأصلي.
 * السطر 238: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 239: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 240: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 241: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 242: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 243: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 244: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * --- نهاية الفهرس السطري ---
 */

package com.example.ui.screens.habayeb.utils

import com.example.data.local.entities.HabayebTransaction
import com.example.domain.model.TransactionType
import java.math.BigDecimal
import java.math.RoundingMode

@androidx.compose.runtime.Immutable
data class CustomerSummaryResult(
    val netDebtBigDecimalMap: Map<String, BigDecimal>,
    val primaryDisplayCurrency: String,
    val netDebt: BigDecimal,
    val lastTimestamp: Long
)

@androidx.compose.runtime.Immutable
data class CustomerHistoryCalculationResult(
    val currencyKeys: List<String>,
    val owedByThemMap: Map<String, BigDecimal>,
    val paymentByThemMap: Map<String, BigDecimal>,
    val owedToThemMap: Map<String, BigDecimal>,
    val paymentToThemMap: Map<String, BigDecimal>,
    val netDebtMap: Map<String, BigDecimal>,
    val runningBalances: Map<String, BigDecimal>,
    val txSequenceNumbers: Map<String, Int>,
    val primaryDisplayCurrency: String,
    val netDebt: BigDecimal,
    val netDebtBigDecimalMap: Map<String, BigDecimal> = emptyMap(),
    val owedByThemBDMap: Map<String, BigDecimal> = emptyMap(),
    val paymentByThemBDMap: Map<String, BigDecimal> = emptyMap(),
    val owedToThemBDMap: Map<String, BigDecimal> = emptyMap(),
    val paymentToThemBDMap: Map<String, BigDecimal> = emptyMap()
) {
    val runningBalancesDouble: Map<String, Double> by lazy {
        runningBalances.mapValues { it.value.toDouble() }
    }
}

/**
 * حاسبة تاريخ ديون وسجلات العميل (CustomerHistoryCalculator)
 * تُشكل المصدر الموحد المعتمد لحساب رصيد العميل التراكمي وتعيين التسلسل الزمني للمعاملات.
 * يتم استخدام BigDecimal بدقة 4 أرقام عشرية للحفاظ على الدقة المالية التامة ومنع أخطاء التقريب المبكر،
 * مع إرجاع القيم المنسقة بدقة حصرية عند حدود العرض النهائي.
 */
object CustomerHistoryCalculator {
    fun calculate(
        allCustomerTxs: List<HabayebTransaction>,
        currencySymbol: String,
        exchangeRatesJson: String?
    ): CustomerHistoryCalculationResult {
        val safeRatesJson = exchangeRatesJson ?: ""
        val totalCount = allCustomerTxs.size

        // Sort chronologically once (timestamp, then id for deterministic stability)
        val chronological = if (totalCount <= 1) allCustomerTxs else allCustomerTxs.sortedWith(
            compareBy<HabayebTransaction> { it.timestamp }.thenBy { it.id }
        )

        val owedByThemBD = HashMap<String, BigDecimal>(4)
        val paymentByThemBD = HashMap<String, BigDecimal>(4)
        val owedToThemBD = HashMap<String, BigDecimal>(4)
        val paymentToThemBD = HashMap<String, BigDecimal>(4)

        val currencySet = LinkedHashSet<String>(4)
        currencySet.add(currencySymbol)

        val balancesMap = HashMap<String, BigDecimal>(totalCount)
        val currentBalBDMap = HashMap<String, BigDecimal>(4)
        val txSequenceNumbers = HashMap<String, Int>(totalCount)

        var seq = 1
        for (tx in chronological) {
            val (txCurrency, bdAmount) = CurrencyConfig.getTransactionCurrencyAndAmountBigDecimal(tx, currencySymbol, safeRatesJson)
            currencySet.add(txCurrency)

            val safeBd = bdAmount.setScale(4, RoundingMode.HALF_EVEN)
            val txType = TransactionType.fromValue(tx.type)

            // Accumulate by type using exact BigDecimal math
            when (txType) {
                TransactionType.OWED_BY_THEM -> owedByThemBD[txCurrency] = (owedByThemBD[txCurrency] ?: BigDecimal.ZERO).add(safeBd)
                TransactionType.PAYMENT_BY_THEM -> paymentByThemBD[txCurrency] = (paymentByThemBD[txCurrency] ?: BigDecimal.ZERO).add(safeBd)
                TransactionType.OWED_TO_THEM -> owedToThemBD[txCurrency] = (owedToThemBD[txCurrency] ?: BigDecimal.ZERO).add(safeBd)
                TransactionType.PAYMENT_TO_THEM -> paymentToThemBD[txCurrency] = (paymentToThemBD[txCurrency] ?: BigDecimal.ZERO).add(safeBd)
                else -> {}
            }

            // Calculate running balance using exact BigDecimal math
            var currentBalBD = currentBalBDMap[txCurrency] ?: BigDecimal.ZERO
            currentBalBD = when (txType) {
                TransactionType.OWED_BY_THEM, TransactionType.PAYMENT_TO_THEM -> currentBalBD.add(safeBd)
                TransactionType.PAYMENT_BY_THEM, TransactionType.OWED_TO_THEM -> currentBalBD.subtract(safeBd)
                else -> currentBalBD
            }.setScale(4, RoundingMode.HALF_EVEN)

            currentBalBDMap[txCurrency] = currentBalBD
            balancesMap[tx.id] = currentBalBD
            txSequenceNumbers[tx.id] = seq++
        }

        val currencyKeys = currencySet.toList()
        val numCurrencies = currencyKeys.size

        val owedByThemMap = HashMap<String, BigDecimal>(numCurrencies)
        val paymentByThemMap = HashMap<String, BigDecimal>(numCurrencies)
        val owedToThemMap = HashMap<String, BigDecimal>(numCurrencies)
        val paymentToThemMap = HashMap<String, BigDecimal>(numCurrencies)
        val netDebtMap = HashMap<String, BigDecimal>(numCurrencies)
        val netDebtBDMap = HashMap<String, BigDecimal>(numCurrencies)

        for (curr in currencyKeys) {
            val owedBy = (owedByThemBD[curr] ?: BigDecimal.ZERO).setScale(4, RoundingMode.HALF_EVEN)
            val payBy = (paymentByThemBD[curr] ?: BigDecimal.ZERO).setScale(4, RoundingMode.HALF_EVEN)
            val owedTo = (owedToThemBD[curr] ?: BigDecimal.ZERO).setScale(4, RoundingMode.HALF_EVEN)
            val payTo = (paymentToThemBD[curr] ?: BigDecimal.ZERO).setScale(4, RoundingMode.HALF_EVEN)

            owedByThemMap[curr] = owedBy
            paymentByThemMap[curr] = payBy
            owedToThemMap[curr] = owedTo
            paymentToThemMap[curr] = payTo

            // netDebt = owedBy - payBy - owedTo + payTo
            val netDebtBD = owedBy.subtract(payBy).subtract(owedTo).add(payTo).setScale(4, RoundingMode.HALF_EVEN)
            netDebtMap[curr] = netDebtBD
            netDebtBDMap[curr] = netDebtBD
        }

        val primaryDisplayCurrency: String
        val netDebt: BigDecimal

        val baseNetBd = netDebtBDMap[currencySymbol] ?: BigDecimal.ZERO
        if (baseNetBd.setScale(4, RoundingMode.HALF_EVEN).compareTo(BigDecimal.ZERO) != 0) {
            primaryDisplayCurrency = currencySymbol
            netDebt = baseNetBd
        } else {
            val nonZeroForeignEntry = netDebtBDMap.entries.firstOrNull { (k, v) ->
                k != currencySymbol && v.setScale(4, RoundingMode.HALF_EVEN).compareTo(BigDecimal.ZERO) != 0
            }
            if (nonZeroForeignEntry != null) {
                primaryDisplayCurrency = nonZeroForeignEntry.key
                netDebt = nonZeroForeignEntry.value
            } else {
                primaryDisplayCurrency = currencySymbol
                netDebt = BigDecimal.ZERO
            }
        }

        return CustomerHistoryCalculationResult(
            currencyKeys = currencyKeys,
            owedByThemMap = owedByThemMap,
            paymentByThemMap = paymentByThemMap,
            owedToThemMap = owedToThemMap,
            paymentToThemMap = paymentToThemMap,
            netDebtMap = netDebtMap,
            runningBalances = balancesMap,
            txSequenceNumbers = txSequenceNumbers,
            primaryDisplayCurrency = primaryDisplayCurrency,
            netDebt = netDebt,
            netDebtBigDecimalMap = netDebtBDMap,
            owedByThemBDMap = owedByThemBD,
            paymentByThemBDMap = paymentByThemBD,
            owedToThemBDMap = owedToThemBD,
            paymentToThemBDMap = paymentToThemBD
        )
    }

    fun calculateSummary(
        allCustomerTxs: List<HabayebTransaction>,
        currencySymbol: String,
        exchangeRatesJson: String?,
        fallbackCreatedAt: Long
    ): CustomerSummaryResult {
        if (allCustomerTxs.isEmpty()) {
            return CustomerSummaryResult(
                netDebtBigDecimalMap = emptyMap(),
                primaryDisplayCurrency = currencySymbol,
                netDebt = BigDecimal.ZERO,
                lastTimestamp = fallbackCreatedAt
            )
        }

        val safeRatesJson = exchangeRatesJson ?: ""
        val owedByThemBD = HashMap<String, BigDecimal>(2)
        val paymentByThemBD = HashMap<String, BigDecimal>(2)
        val owedToThemBD = HashMap<String, BigDecimal>(2)
        val paymentToThemBD = HashMap<String, BigDecimal>(2)
        val currencySet = LinkedHashSet<String>(2)
        currencySet.add(currencySymbol)

        var maxTime = fallbackCreatedAt

        for (tx in allCustomerTxs) {
            if (tx.timestamp > maxTime) {
                maxTime = tx.timestamp
            }
            val (txCurrency, bdAmount) = CurrencyConfig.getTransactionCurrencyAndAmountBigDecimal(tx, currencySymbol, safeRatesJson)
            currencySet.add(txCurrency)

            val safeBd = bdAmount.setScale(4, RoundingMode.HALF_EVEN)
            when (TransactionType.fromValue(tx.type)) {
                TransactionType.OWED_BY_THEM -> owedByThemBD[txCurrency] = (owedByThemBD[txCurrency] ?: BigDecimal.ZERO).add(safeBd)
                TransactionType.PAYMENT_BY_THEM -> paymentByThemBD[txCurrency] = (paymentByThemBD[txCurrency] ?: BigDecimal.ZERO).add(safeBd)
                TransactionType.OWED_TO_THEM -> owedToThemBD[txCurrency] = (owedToThemBD[txCurrency] ?: BigDecimal.ZERO).add(safeBd)
                TransactionType.PAYMENT_TO_THEM -> paymentToThemBD[txCurrency] = (paymentToThemBD[txCurrency] ?: BigDecimal.ZERO).add(safeBd)
                else -> {}
            }
        }

        val netDebtBDMap = HashMap<String, BigDecimal>(currencySet.size)
        for (curr in currencySet) {
            val owedBy = owedByThemBD[curr] ?: BigDecimal.ZERO
            val payBy = paymentByThemBD[curr] ?: BigDecimal.ZERO
            val owedTo = owedToThemBD[curr] ?: BigDecimal.ZERO
            val payTo = paymentToThemBD[curr] ?: BigDecimal.ZERO
            netDebtBDMap[curr] = owedBy.subtract(payBy).subtract(owedTo).add(payTo).setScale(4, RoundingMode.HALF_EVEN)
        }

        val primaryDisplayCurrency: String
        val netDebt: BigDecimal
        val baseNetBd = netDebtBDMap[currencySymbol] ?: BigDecimal.ZERO
        if (baseNetBd.setScale(4, RoundingMode.HALF_EVEN).compareTo(BigDecimal.ZERO) != 0) {
            primaryDisplayCurrency = currencySymbol
            netDebt = baseNetBd
        } else {
            val nonZeroForeignEntry = netDebtBDMap.entries.firstOrNull { (k, v) ->
                k != currencySymbol && v.setScale(4, RoundingMode.HALF_EVEN).compareTo(BigDecimal.ZERO) != 0
            }
            if (nonZeroForeignEntry != null) {
                primaryDisplayCurrency = nonZeroForeignEntry.key
                netDebt = nonZeroForeignEntry.value
            } else {
                primaryDisplayCurrency = currencySymbol
                netDebt = BigDecimal.ZERO
            }
        }

        return CustomerSummaryResult(
            netDebtBigDecimalMap = netDebtBDMap,
            primaryDisplayCurrency = primaryDisplayCurrency,
            netDebt = netDebt,
            lastTimestamp = maxTime
        )
    }
}




/*
 * // --- ملاحظات وتوصيات المعمارية البرمجية ---
 * 1) يجب أن تبقى Composable هنا مسؤولة عن العرض والتنسيق واستقبال التفاعل،
 *    بينما تبقى قواعد المجال والحساب المالي في طبقات Domain/UseCase المناسبة.
 * 2) يوصى بالحفاظ على Unidirectional Data Flow: الحالة تدخل إلى الشاشة،
 *    والتفاعل يخرج كأحداث واضحة، بدلاً من إنشاء مصادر حالة متنافسة داخل الواجهة.
 * 3) عند وجود قوائم طويلة، يجب مراقبة إعادة التركيب وعمليات allocation داخل
 *    item content، خصوصاً في LazyColumn، حتى لا يتحول العرض إلى نقطة اختناق.
 * 4) أي نص أو رقم مالي معروض للمستخدم يجب أن يمر عبر formatter المعتمد،
 *    وألا يعاد حساب القيمة المالية داخل Composable باستخدام Double/Float.
 * 5) الحوارات والأوراق السفلية ينبغي أن تستمد visibility من State واحد واضح،
 *    مع منع بقاء حالة قديمة بعد إغلاق الحوار أو تغيير العميل النشط.
 * 6) يجب الحفاظ على دعم RTL، وألا تعتمد المحاذاة أو اتجاه الحركة على افتراض
 *    ثابت للغة؛ لأن واجهة التطبيق العربية جزء من العقد البصري.
 * 7) أي تعديل مستقبلي على animation أو haptic feedback يجب أن يراعي الأداء
 *    ودورة الحياة وألا يسبب إطلاق آثار متكررة أثناء إعادة التركيب.
 * 8) التوصيات أعلاه ملاحظات هندسية مستقبلية فقط، ولا تمثل أي تعديل في الكود الحالي.
 */
