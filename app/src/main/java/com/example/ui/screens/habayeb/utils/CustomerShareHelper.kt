/*
 * ╔══════════════════════════════════════════════════════════════════════╗
 * ║ التوثيق العربي الهندسي — MASTERCLASS / BATCH 07                   ║
 * ╚══════════════════════════════════════════════════════════════════════╝
 *
 * الملف: app/src/main/java/com/example/ui/screens/habayeb/utils/CustomerShareHelper.kt
 * القطاع المعماري: Habayeb UI/UX.
 *
 * الوصف المعماري:
 * مكوّن مخصص للعملاء (CustomerShareHelper) يعرض أو يدير تفاعلاً محدداً ضمن دورة حياة حساب العميل.
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
 * البصمة SHA-256 للنص الأصلي قبل التوثيق: 815b7ba67c38d277899a23fb28003a25ad64c60f6d37dbb5bde9ecafdd0e64a2
 *
 * --- الفهرس السطري التعليمي ---
 * السطر 1: تعريف الحزمة التي ينتمي إليها الملف.
 * السطر 3: استيراد اعتماد خارجي/داخلي تستخدمه الشيفرة الأصلية.
 * السطر 4: استيراد اعتماد خارجي/داخلي تستخدمه الشيفرة الأصلية.
 * السطر 5: استيراد اعتماد خارجي/داخلي تستخدمه الشيفرة الأصلية.
 * السطر 6: استيراد اعتماد خارجي/داخلي تستخدمه الشيفرة الأصلية.
 * السطر 7: استيراد اعتماد خارجي/داخلي تستخدمه الشيفرة الأصلية.
 * السطر 8: استيراد اعتماد خارجي/داخلي تستخدمه الشيفرة الأصلية.
 * السطر 9: استيراد اعتماد خارجي/داخلي تستخدمه الشيفرة الأصلية.
 * السطر 11: تعليق أصلي من الشيفرة؛ محفوظ دون تعديل.
 * السطر 12: تعليق أصلي من الشيفرة؛ محفوظ دون تعديل.
 * السطر 13: تعليق أصلي من الشيفرة؛ محفوظ دون تعديل.
 * السطر 14: تعليق أصلي من الشيفرة؛ محفوظ دون تعديل.
 * السطر 15: تعليق أصلي من الشيفرة؛ محفوظ دون تعديل.
 * السطر 16: تعليق أصلي من الشيفرة؛ محفوظ دون تعديل.
 * السطر 17: تعليق أصلي من الشيفرة؛ محفوظ دون تعديل.
 * السطر 18: تعليق أصلي من الشيفرة؛ محفوظ دون تعديل.
 * السطر 19: تعريف نوع/كلاس/كائن أو alias؛ يمثل عقداً معمارياً في الملف.
 * السطر 21: تعريف دالة؛ نقطة تنفيذ أو Composable تستقبل مدخلات وتنتج أثراً أو واجهة.
 * السطر 22: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 23: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 24: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 25: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 26: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 27: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 29: تعليق أصلي من الشيفرة؛ محفوظ دون تعديل.
 * السطر 30: معالجة استثناءات للحفاظ على مسار التنفيذ المتفق عليه.
 * السطر 31: تعليق أصلي من الشيفرة؛ محفوظ دون تعديل.
 * السطر 32: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 33: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 34: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 35: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 36: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 37: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 38: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 39: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 40: معالجة استثناءات للحفاظ على مسار التنفيذ المتفق عليه.
 * السطر 41: تعليق أصلي من الشيفرة؛ محفوظ دون تعديل.
 * السطر 42: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 43: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 44: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 45: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 46: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 47: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 48: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 49: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 50: تعليق أصلي من الشيفرة؛ محفوظ دون تعديل.
 * السطر 51: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 52: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 53: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 54: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 55: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 56: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 57: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 58: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 60: تعليق أصلي من الشيفرة؛ محفوظ دون تعديل.
 * السطر 61: تعليق أصلي من الشيفرة؛ محفوظ دون تعديل.
 * السطر 62: تعليق أصلي من الشيفرة؛ محفوظ دون تعديل.
 * السطر 63: تعليق أصلي من الشيفرة؛ محفوظ دون تعديل.
 * السطر 64: تعليق أصلي من الشيفرة؛ محفوظ دون تعديل.
 * السطر 65: تعريف دالة؛ نقطة تنفيذ أو Composable تستقبل مدخلات وتنتج أثراً أو واجهة.
 * السطر 66: إرجاع قيمة إلى المستدعي وفق العقد الأصلي.
 * السطر 67: تعبير lambda يحدد الإجراء الناتج عن تفاعل أو تحويل.
 * السطر 68: تعبير lambda يحدد الإجراء الناتج عن تفاعل أو تحويل.
 * السطر 69: تعبير lambda يحدد الإجراء الناتج عن تفاعل أو تحويل.
 * السطر 70: تعبير lambda يحدد الإجراء الناتج عن تفاعل أو تحويل.
 * السطر 71: المسار البديل للشرط السابق.
 * السطر 72: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 73: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 75: تعريف دالة؛ نقطة تنفيذ أو Composable تستقبل مدخلات وتنتج أثراً أو واجهة.
 * السطر 76: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 77: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 78: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 79: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 80: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 81: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 82: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 83: تعليق أصلي من الشيفرة؛ محفوظ دون تعديل.
 * السطر 84: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 85: تعبير lambda يحدد الإجراء الناتج عن تفاعل أو تحويل.
 * السطر 86: تعبير lambda يحدد الإجراء الناتج عن تفاعل أو تحويل.
 * السطر 87: تعبير lambda يحدد الإجراء الناتج عن تفاعل أو تحويل.
 * السطر 88: تعبير lambda يحدد الإجراء الناتج عن تفاعل أو تحويل.
 * السطر 89: المسار البديل للشرط السابق.
 * السطر 90: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 92: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 94: تعليق أصلي من الشيفرة؛ محفوظ دون تعديل.
 * السطر 95: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 96: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 97: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 98: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 99: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 100: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 101: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 102: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 103: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 104: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 105: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 106: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 107: فرع شرطي يحدد مسار التنفيذ حسب الحالة الحالية.
 * السطر 108: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 109: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 110: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 111: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 112: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 113: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 115: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 116: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 117: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 119: تعليق أصلي من الشيفرة؛ محفوظ دون تعديل.
 * السطر 120: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 121: فرع شرطي يحدد مسار التنفيذ حسب الحالة الحالية.
 * السطر 122: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 123: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 124: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 126: تعليق أصلي من الشيفرة؛ محفوظ دون تعديل.
 * السطر 127: فرع شرطي يحدد مسار التنفيذ حسب الحالة الحالية.
 * السطر 128: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 129: تكرار على عناصر أو عدد محدد وفق التنفيذ الأصلي.
 * السطر 130: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 131: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 132: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 134: فرع شرطي يحدد مسار التنفيذ حسب الحالة الحالية.
 * السطر 135: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 136: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 137: اختيار مسار تنفيذ بناءً على حالة/قيمة محددة.
 * السطر 138: تعبير lambda يحدد الإجراء الناتج عن تفاعل أو تحويل.
 * السطر 139: تعبير lambda يحدد الإجراء الناتج عن تفاعل أو تحويل.
 * السطر 140: تعبير lambda يحدد الإجراء الناتج عن تفاعل أو تحويل.
 * السطر 141: تعبير lambda يحدد الإجراء الناتج عن تفاعل أو تحويل.
 * السطر 142: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 143: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 144: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 146: تكرار على عناصر أو عدد محدد وفق التنفيذ الأصلي.
 * السطر 147: فرع شرطي يحدد مسار التنفيذ حسب الحالة الحالية.
 * السطر 148: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 149: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 150: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 151: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 152: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 153: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 154: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 155: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 156: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 157: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 159: تعليق أصلي من الشيفرة؛ محفوظ دون تعديل.
 * السطر 160: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 161: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 162: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 163: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 164: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 165: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 166: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 167: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 168: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 170: إرجاع قيمة إلى المستدعي وفق العقد الأصلي.
 * السطر 171: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 173: تعريف دالة؛ نقطة تنفيذ أو Composable تستقبل مدخلات وتنتج أثراً أو واجهة.
 * السطر 174: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 175: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 176: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 177: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 178: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 179: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 180: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 181: تعبير lambda يحدد الإجراء الناتج عن تفاعل أو تحويل.
 * السطر 182: تعبير lambda يحدد الإجراء الناتج عن تفاعل أو تحويل.
 * السطر 183: المسار البديل للشرط السابق.
 * السطر 184: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 185: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 186: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 188: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 189: فرع شرطي يحدد مسار التنفيذ حسب الحالة الحالية.
 * السطر 190: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 191: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 192: تكرار على عناصر أو عدد محدد وفق التنفيذ الأصلي.
 * السطر 193: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 194: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 195: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 197: فرع شرطي يحدد مسار التنفيذ حسب الحالة الحالية.
 * السطر 198: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 199: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 200: اختيار مسار تنفيذ بناءً على حالة/قيمة محددة.
 * السطر 201: تعبير lambda يحدد الإجراء الناتج عن تفاعل أو تحويل.
 * السطر 202: تعبير lambda يحدد الإجراء الناتج عن تفاعل أو تحويل.
 * السطر 203: تعبير lambda يحدد الإجراء الناتج عن تفاعل أو تحويل.
 * السطر 204: تعبير lambda يحدد الإجراء الناتج عن تفاعل أو تحويل.
 * السطر 205: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 206: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 207: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 209: تكرار على عناصر أو عدد محدد وفق التنفيذ الأصلي.
 * السطر 210: فرع شرطي يحدد مسار التنفيذ حسب الحالة الحالية.
 * السطر 211: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 212: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 213: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 214: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 215: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 216: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 217: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 218: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 219: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 220: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 221: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 223: إرجاع قيمة إلى المستدعي وفق العقد الأصلي.
 * السطر 224: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 226: تعريف دالة؛ نقطة تنفيذ أو Composable تستقبل مدخلات وتنتج أثراً أو واجهة.
 * السطر 227: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 228: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 229: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 230: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 231: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 232: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 233: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 234: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 235: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 237: تعريف دالة؛ نقطة تنفيذ أو Composable تستقبل مدخلات وتنتج أثراً أو واجهة.
 * السطر 238: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 239: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 240: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 241: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 242: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 243: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 244: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 245: معالجة استثناءات للحفاظ على مسار التنفيذ المتفق عليه.
 * السطر 246: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 247: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 248: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 249: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 250: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 251: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 252: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 253: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 254: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 255: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 256: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 258: تعريف دالة؛ نقطة تنفيذ أو Composable تستقبل مدخلات وتنتج أثراً أو واجهة.
 * السطر 259: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 260: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 261: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 262: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 263: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 264: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 265: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 266: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 267: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 268: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 270: تعريف دالة؛ نقطة تنفيذ أو Composable تستقبل مدخلات وتنتج أثراً أو واجهة.
 * السطر 271: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 272: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 273: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 274: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 275: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 276: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 277: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 278: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 279: معالجة استثناءات للحفاظ على مسار التنفيذ المتفق عليه.
 * السطر 280: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 281: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 282: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 283: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 284: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 285: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 286: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 287: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 288: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 289: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 290: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 292: تعريف دالة؛ نقطة تنفيذ أو Composable تستقبل مدخلات وتنتج أثراً أو واجهة.
 * السطر 293: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 294: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 295: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 296: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 297: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 298: فرع شرطي يحدد مسار التنفيذ حسب الحالة الحالية.
 * السطر 299: معالجة استثناءات للحفاظ على مسار التنفيذ المتفق عليه.
 * السطر 300: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 301: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 302: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 303: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 305: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 306: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 307: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 308: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 309: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 310: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 312: معالجة استثناءات للحفاظ على مسار التنفيذ المتفق عليه.
 * السطر 313: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 314: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 315: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 316: معالجة استثناءات للحفاظ على مسار التنفيذ المتفق عليه.
 * السطر 317: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 318: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 319: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 320: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 321: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 322: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 323: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 324: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 325: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 326: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 327: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 328: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * --- نهاية الفهرس السطري ---
 */

package com.example.ui.screens.habayeb.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.example.R
import com.example.data.local.entities.HabayebCustomer
import com.example.data.local.entities.HabayebTransaction
import com.example.ui.helper.formatCurrency

/**
 * مساعد مشاركة كشوفات ومطالبات العملاء (Customer Statement Sharing Helper)
 *
 * المسؤوليات المعمارية:
 * 1. صياغة وتنسيق رسائل المطالبات وكشوف الحسابات بطريقة مهنية واضحة مع إبراز العملات وأرقام الحسابات.
 * 2. دعم التوافقية العالية لمشاركة الرسائل عبر مختلف أجهزة ومصنعي Android (SMS Intents & System Choosers).
 * 3. عزل منطق المشاركة النصية خارج Composable لتحقيق فصل تام للمسؤوليات.
 */
object CustomerShareHelper {

    private fun sendSmsReliably(context: Context, rawPhone: String, body: String, fallbackChooserTitleId: Int) {
        val cleanPhone = rawPhone.replace(" ", "")
            .replace("-", "")
            .replace("(", "")
            .replace(")", "")
            .replace("[", "")
            .replace("]", "")
        
        // Try multiple methods sequentially to support 100% of Android OEMs (Samsung, Xiaomi, Huawei, Pixel, etc.)
        try {
            // Method 1: ACTION_SENDTO with smsto: scheme (standard Android)
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse(if (cleanPhone.isBlank()) "smsto:" else "smsto:$cleanPhone")
                putExtra("sms_body", body)
                putExtra("body", body)
                putExtra(Intent.EXTRA_TEXT, body)
            }
            context.startActivity(intent)
        } catch (e1: Exception) {
            try {
                // Method 2: ACTION_VIEW with sms: scheme (fallback for some devices)
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse(if (cleanPhone.isBlank()) "sms:" else "sms:$cleanPhone")
                    putExtra("sms_body", body)
                    putExtra("body", body)
                    putExtra(Intent.EXTRA_TEXT, body)
                }
                context.startActivity(intent)
            } catch (e2: Exception) {
                // Method 3: System Intent Chooser
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, body)
                }
                context.startActivity(Intent.createChooser(shareIntent, context.getString(fallbackChooserTitleId)))
            }
        }
    }

    /**
     * Resolves smart, precise transaction title based on account direction.
     * For "حساب له" (OWED_TO_THEM) -> payment is "تسديد"
     * For "حساب عليه" (OWED_BY_THEM) -> payment is "استلام"
     */
    fun resolveTxTypeTitle(context: Context, txType: String, isAccountOwedToThem: Boolean): String {
        return when (txType) {
            "OWED_BY_THEM" -> context.getString(R.string.habayeb_pdf_tx_owed_by)
            "PAYMENT_BY_THEM" -> context.getString(R.string.habayeb_pdf_tx_payment_by)
            "OWED_TO_THEM" -> context.getString(R.string.habayeb_pdf_tx_owed_to)
            "PAYMENT_TO_THEM" -> context.getString(R.string.habayeb_pdf_tx_payment_to)
            else -> context.getString(R.string.pdf_tx_type_new)
        }
    }

    fun buildSingleTxShareBody(
        context: Context,
        tx: HabayebTransaction,
        customer: HabayebCustomer,
        netDebt: java.math.BigDecimal,
        currencySymbol: String,
        allCustomerTxs: List<HabayebTransaction> = emptyList()
    ): String {
        // 1. Header Line
        val header = when (tx.type) {
            "OWED_BY_THEM" -> context.getString(R.string.msg_header_debt_against)
            "PAYMENT_BY_THEM" -> context.getString(R.string.msg_header_payment_against)
            "OWED_TO_THEM" -> context.getString(R.string.msg_header_debt_for)
            "PAYMENT_TO_THEM" -> context.getString(R.string.msg_header_payment_for)
            else -> context.getString(R.string.msg_header_debt_against)
        }

        val bullet = context.getString(R.string.msg_bullet)

        // 2. Main Transaction Amount Line
        val isExchangeTx = tx.isForeign && tx.isRateCalculated
        val amountLine = if (isExchangeTx) {
            val foreignSymbol = if (tx.currencyCode != "DEFAULT" && tx.currencyCode.isNotBlank()) tx.currencyCode else ""
            val foreignAmtFormatted = com.example.ui.helper.HabayebMathHelper.formatSmart(tx.foreignAmount)
            val arrow = context.getString(R.string.msg_exchange_arrow)
            val equivAmtFormatted = com.example.ui.helper.HabayebMathHelper.formatSmart(tx.equivalentAmount)
            val ratePrefix = context.getString(R.string.msg_rate_prefix)
            val rateFormatted = com.example.ui.helper.HabayebMathHelper.formatRate(tx.exchangeRate)
            "$bullet $foreignAmtFormatted $foreignSymbol $arrow $equivAmtFormatted $currencySymbol $ratePrefix $rateFormatted"
        } else if (tx.isForeign) {
            val foreignSymbol = if (tx.currencyCode != "DEFAULT" && tx.currencyCode.isNotBlank()) tx.currencyCode else ""
            val foreignAmtFormatted = com.example.ui.helper.HabayebMathHelper.formatSmart(
                if (tx.foreignAmount.compareTo(java.math.BigDecimal.ZERO) > 0) tx.foreignAmount else tx.amount
            )
            "$bullet $foreignAmtFormatted $foreignSymbol"
        } else {
            val amtFormatted = com.example.ui.helper.HabayebMathHelper.formatSmart(tx.amount)
            "$bullet $amtFormatted $currencySymbol"
        }

        val lines = mutableListOf<String>()
        lines.add(header)
        lines.add(amountLine)

        // 3. Note / Statement Line (only if present)
        val cleanDetails = CurrencyConfig.getCleanDetails(tx.description)
        if (cleanDetails.isNotBlank()) {
            val statementPrefix = context.getString(R.string.msg_statement_prefix)
            lines.add("$statementPrefix $cleanDetails")
        }

        // 4. Cumulative Foreign Balances (only for unconverted foreign transactions)
        if (allCustomerTxs.isNotEmpty()) {
            val foreignMap = mutableMapOf<String, java.math.BigDecimal>()
            for (t in allCustomerTxs) {
                val (tCurrency, bdAmount) = CurrencyConfig.getTransactionCurrencyAndAmountBigDecimal(t, currencySymbol)
                val normCurrency = CurrencyConfig.getBySymbol(tCurrency)?.symbol ?: tCurrency
                val normDefault = CurrencyConfig.getBySymbol(currencySymbol)?.symbol ?: currencySymbol

                if (normCurrency != normDefault) {
                    val safeBd = bdAmount.setScale(4, java.math.RoundingMode.HALF_EVEN)
                    val currVal = foreignMap[normCurrency] ?: java.math.BigDecimal.ZERO
                    when (t.type) {
                        "OWED_BY_THEM" -> foreignMap[normCurrency] = currVal.add(safeBd)
                        "PAYMENT_BY_THEM" -> foreignMap[normCurrency] = currVal.subtract(safeBd)
                        "OWED_TO_THEM" -> foreignMap[normCurrency] = currVal.subtract(safeBd)
                        "PAYMENT_TO_THEM" -> foreignMap[normCurrency] = currVal.add(safeBd)
                    }
                }
            }

            for ((fSymbol, fNetBd) in foreignMap) {
                if (fNetBd.compareTo(java.math.BigDecimal.ZERO) != 0) {
                    val foreignTotalPrefix = if (fNetBd.compareTo(java.math.BigDecimal.ZERO) > 0) {
                        context.getString(R.string.msg_foreign_total_against)
                    } else {
                        context.getString(R.string.msg_foreign_total_for)
                    }
                    val formattedForeignNet = com.example.ui.helper.HabayebMathHelper.formatSmart(fNetBd.abs())
                    lines.add("$foreignTotalPrefix $formattedForeignNet $fSymbol")
                }
            }
        }

        // 5. Total Local Balance Line
        val totalPrefix = if (netDebt.compareTo(java.math.BigDecimal.ZERO) > 0) {
            context.getString(R.string.msg_total_against)
        } else if (netDebt.compareTo(java.math.BigDecimal.ZERO) < 0) {
            context.getString(R.string.msg_total_for)
        } else {
            context.getString(R.string.msg_total_against)
        }
        val formattedNetDebt = com.example.ui.helper.HabayebMathHelper.formatSmart(netDebt.abs())
        lines.add("$totalPrefix $formattedNetDebt $currencySymbol")

        return lines.joinToString("\n")
    }

    fun buildStatementShareBody(
        context: Context,
        customer: HabayebCustomer,
        netDebt: java.math.BigDecimal,
        currencySymbol: String,
        allCustomerTxs: List<HabayebTransaction> = emptyList()
    ): String {
        val debtStatus = when {
            netDebt.compareTo(java.math.BigDecimal.ZERO) > 0 -> context.getString(R.string.habayeb_statement_status_owed_by_them, formatCurrency(netDebt.abs(), currencySymbol))
            netDebt.compareTo(java.math.BigDecimal.ZERO) < 0 -> context.getString(R.string.habayeb_statement_status_owed_to_them, formatCurrency(netDebt.abs(), currencySymbol))
            else -> context.getString(R.string.habayeb_statement_status_balanced_new, formatCurrency(java.math.BigDecimal.ZERO, currencySymbol))
        }
        val title = context.getString(R.string.habayeb_statement_header, customer.name)
        val footer = context.getString(R.string.habayeb_statement_footer)

        val foreignLines = mutableListOf<String>()
        if (allCustomerTxs.isNotEmpty()) {
            val isAccountOwedToThem = customer.initialType == "OWED_TO_THEM"
            val foreignMap = mutableMapOf<String, java.math.BigDecimal>()
            for (t in allCustomerTxs) {
                val (tCurrency, bdAmount) = CurrencyConfig.getTransactionCurrencyAndAmountBigDecimal(t, currencySymbol)
                val normCurrency = CurrencyConfig.getBySymbol(tCurrency)?.symbol ?: tCurrency
                val normDefault = CurrencyConfig.getBySymbol(currencySymbol)?.symbol ?: currencySymbol

                if (normCurrency != normDefault) {
                    val safeBd = bdAmount.setScale(4, java.math.RoundingMode.HALF_EVEN)
                    val currVal = foreignMap[normCurrency] ?: java.math.BigDecimal.ZERO
                    when (t.type) {
                        "OWED_BY_THEM" -> foreignMap[normCurrency] = currVal.add(safeBd)
                        "PAYMENT_BY_THEM" -> foreignMap[normCurrency] = currVal.subtract(safeBd)
                        "OWED_TO_THEM" -> foreignMap[normCurrency] = currVal.subtract(safeBd)
                        "PAYMENT_TO_THEM" -> foreignMap[normCurrency] = currVal.add(safeBd)
                    }
                }
            }

            for ((fSymbol, fNetBd) in foreignMap) {
                if (fNetBd.compareTo(java.math.BigDecimal.ZERO) != 0) {
                    val foreignTotalPrefix = if (fNetBd.compareTo(java.math.BigDecimal.ZERO) > 0) {
                        context.getString(R.string.msg_foreign_total_against)
                    } else {
                        context.getString(R.string.msg_foreign_total_for)
                    }
                    val formattedForeignNet = com.example.ui.helper.HabayebMathHelper.formatSmart(fNetBd.abs())
                    foreignLines.add("\n$foreignTotalPrefix $formattedForeignNet $fSymbol")
                }
            }
        }
        val foreignText = if (foreignLines.isNotEmpty()) foreignLines.joinToString("") + "\n" else ""

        return "$title• $debtStatus\n$foreignText$footer"
    }

    fun triggerSmsStatement(
        context: Context,
        customer: HabayebCustomer,
        debt: java.math.BigDecimal,
        currencySymbol: String,
        allCustomerTxs: List<HabayebTransaction> = emptyList()
    ) {
        val body = buildStatementShareBody(context, customer, debt, currencySymbol, allCustomerTxs)
        sendSmsReliably(context, customer.phone, body, R.string.habayeb_statement_send)
    }

    fun triggerWhatsAppStatement(
        context: Context,
        customer: HabayebCustomer,
        debt: java.math.BigDecimal,
        currencySymbol: String,
        allCustomerTxs: List<HabayebTransaction> = emptyList()
    ) {
        val body = buildStatementShareBody(context, customer, debt, currencySymbol, allCustomerTxs)
        try {
            val waUrl = "https://wa.me/${customer.phone.replace("+", "").replace(" ", "")}?text=${Uri.encode(body)}"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(waUrl))
            context.startActivity(intent)
        } catch (e: Exception) {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, body)
            }
            context.startActivity(Intent.createChooser(shareIntent, context.getString(R.string.habayeb_statement_send_whatsapp)))
        }
    }

    fun triggerSingleTxSms(
        context: Context,
        tx: HabayebTransaction,
        customer: HabayebCustomer,
        netDebt: java.math.BigDecimal,
        currencySymbol: String,
        allCustomerTxs: List<HabayebTransaction> = emptyList()
    ) {
        val body = buildSingleTxShareBody(context, tx, customer, netDebt, currencySymbol, allCustomerTxs)
        sendSmsReliably(context, customer.phone, body, R.string.habayeb_tx_send_notice)
    }

    fun triggerSingleTxWhatsApp(
        context: Context,
        tx: HabayebTransaction,
        customer: HabayebCustomer,
        netDebt: java.math.BigDecimal,
        currencySymbol: String,
        allCustomerTxs: List<HabayebTransaction> = emptyList()
    ) {
        val body = buildSingleTxShareBody(context, tx, customer, netDebt, currencySymbol, allCustomerTxs)
        try {
            val waUrl = "https://wa.me/${customer.phone.replace("+", "").replace(" ", "")}?text=${Uri.encode(body)}"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(waUrl))
            context.startActivity(intent)
        } catch (e: Exception) {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, body)
            }
            context.startActivity(Intent.createChooser(shareIntent, context.getString(R.string.habayeb_tx_whatsapp_choose)))
        }
    }

    fun triggerWhatsAppDirectFile(
        context: Context,
        customer: HabayebCustomer,
        file: java.io.File,
        mimeType: String
    ) {
        if (customer.phone.isBlank()) return
        try {
            val authority = "${context.packageName}.fileprovider"
            val uri = androidx.core.content.FileProvider.getUriForFile(context, authority, file)
            val cleanPhone = customer.phone.replace("+", "").replace(" ", "").replace("-", "").trim()
            val jid = "$cleanPhone@s.whatsapp.net"
            
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra("jid", jid)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            
            try {
                intent.setPackage("com.whatsapp")
                context.startActivity(intent)
            } catch (e: Exception) {
                try {
                    intent.setPackage("com.whatsapp.w4b")
                    context.startActivity(intent)
                } catch (e2: Exception) {
                    intent.setPackage(null)
                    context.startActivity(Intent.createChooser(intent, context.getString(R.string.pdf_chooser_title)))
                }
            }
        } catch (e: Exception) {
            android.widget.Toast.makeText(context, context.getString(R.string.toast_operation_failed), android.widget.Toast.LENGTH_SHORT).show()
        }
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
