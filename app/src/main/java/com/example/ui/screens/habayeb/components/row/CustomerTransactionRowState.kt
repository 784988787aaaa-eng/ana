/*
 * ╔══════════════════════════════════════════════════════════════════════╗
 * ║ التوثيق العربي الهندسي — MASTERCLASS / BATCH 07                   ║
 * ╚══════════════════════════════════════════════════════════════════════╝
 *
 * الملف: app/src/main/java/com/example/ui/screens/habayeb/components/row/CustomerTransactionRowState.kt
 * القطاع المعماري: Habayeb UI/UX.
 *
 * الوصف المعماري:
 * مكوّن صف (CustomerTransactionRowState) يترجم سجل العميل/المعاملة إلى تمثيل بصري قابل للتفاعل.
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
 * البصمة SHA-256 للنص الأصلي قبل التوثيق: 3fa49d94774c077d11d120950dc3f635640be686d00f58560737e9e9cad6a2e9
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
 * السطر 10: استيراد اعتماد خارجي/داخلي تستخدمه الشيفرة الأصلية.
 * السطر 11: استيراد اعتماد خارجي/داخلي تستخدمه الشيفرة الأصلية.
 * السطر 12: استيراد اعتماد خارجي/داخلي تستخدمه الشيفرة الأصلية.
 * السطر 13: استيراد اعتماد خارجي/داخلي تستخدمه الشيفرة الأصلية.
 * السطر 14: استيراد اعتماد خارجي/داخلي تستخدمه الشيفرة الأصلية.
 * السطر 15: استيراد اعتماد خارجي/داخلي تستخدمه الشيفرة الأصلية.
 * السطر 16: استيراد اعتماد خارجي/داخلي تستخدمه الشيفرة الأصلية.
 * السطر 17: استيراد اعتماد خارجي/داخلي تستخدمه الشيفرة الأصلية.
 * السطر 18: استيراد اعتماد خارجي/داخلي تستخدمه الشيفرة الأصلية.
 * السطر 19: استيراد اعتماد خارجي/داخلي تستخدمه الشيفرة الأصلية.
 * السطر 20: استيراد اعتماد خارجي/داخلي تستخدمه الشيفرة الأصلية.
 * السطر 21: استيراد اعتماد خارجي/داخلي تستخدمه الشيفرة الأصلية.
 * السطر 22: استيراد اعتماد خارجي/داخلي تستخدمه الشيفرة الأصلية.
 * السطر 23: استيراد اعتماد خارجي/داخلي تستخدمه الشيفرة الأصلية.
 * السطر 25: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 27: تعريف نوع/كلاس/كائن أو alias؛ يمثل عقداً معمارياً في الملف.
 * السطر 28: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 29: تعليمة annotation تضبط سلوك المترجم أو Compose أو إطار العمل.
 * السطر 30: تعليمة annotation تضبط سلوك المترجم أو Compose أو إطار العمل.
 * السطر 31: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 33: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 34: تعليمة annotation تضبط سلوك المترجم أو Compose أو إطار العمل.
 * السطر 35: تعليمة annotation تضبط سلوك المترجم أو Compose أو إطار العمل.
 * السطر 36: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 38: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 39: تعليمة annotation تضبط سلوك المترجم أو Compose أو إطار العمل.
 * السطر 40: تعليمة annotation تضبط سلوك المترجم أو Compose أو إطار العمل.
 * السطر 41: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 43: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 44: تعليمة annotation تضبط سلوك المترجم أو Compose أو إطار العمل.
 * السطر 45: تعليمة annotation تضبط سلوك المترجم أو Compose أو إطار العمل.
 * السطر 46: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 48: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 49: تعليمة annotation تضبط سلوك المترجم أو Compose أو إطار العمل.
 * السطر 50: تعليمة annotation تضبط سلوك المترجم أو Compose أو إطار العمل.
 * السطر 51: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 53: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 54: تعليمة annotation تضبط سلوك المترجم أو Compose أو إطار العمل.
 * السطر 55: تعليمة annotation تضبط سلوك المترجم أو Compose أو إطار العمل.
 * السطر 56: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 58: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 59: تعليمة annotation تضبط سلوك المترجم أو Compose أو إطار العمل.
 * السطر 60: تعليمة annotation تضبط سلوك المترجم أو Compose أو إطار العمل.
 * السطر 61: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 63: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 64: تعليمة annotation تضبط سلوك المترجم أو Compose أو إطار العمل.
 * السطر 65: تعليمة annotation تضبط سلوك المترجم أو Compose أو إطار العمل.
 * السطر 66: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 68: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 69: تعليمة annotation تضبط سلوك المترجم أو Compose أو إطار العمل.
 * السطر 70: تعليمة annotation تضبط سلوك المترجم أو Compose أو إطار العمل.
 * السطر 71: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 73: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 74: تعليمة annotation تضبط سلوك المترجم أو Compose أو إطار العمل.
 * السطر 75: تعليمة annotation تضبط سلوك المترجم أو Compose أو إطار العمل.
 * السطر 76: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 78: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 79: تعليمة annotation تضبط سلوك المترجم أو Compose أو إطار العمل.
 * السطر 80: تعليمة annotation تضبط سلوك المترجم أو Compose أو إطار العمل.
 * السطر 81: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 83: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 84: تعليمة annotation تضبط سلوك المترجم أو Compose أو إطار العمل.
 * السطر 85: تعليمة annotation تضبط سلوك المترجم أو Compose أو إطار العمل.
 * السطر 86: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 88: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 89: تعليمة annotation تضبط سلوك المترجم أو Compose أو إطار العمل.
 * السطر 90: تعليمة annotation تضبط سلوك المترجم أو Compose أو إطار العمل.
 * السطر 91: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 93: تعليق أصلي من الشيفرة؛ محفوظ دون تعديل.
 * السطر 94: تعريف دالة؛ نقطة تنفيذ أو Composable تستقبل مدخلات وتنتج أثراً أو واجهة.
 * السطر 95: تعريف دالة؛ نقطة تنفيذ أو Composable تستقبل مدخلات وتنتج أثراً أو واجهة.
 * السطر 96: تعريف دالة؛ نقطة تنفيذ أو Composable تستقبل مدخلات وتنتج أثراً أو واجهة.
 * السطر 97: تعريف دالة؛ نقطة تنفيذ أو Composable تستقبل مدخلات وتنتج أثراً أو واجهة.
 * السطر 98: تعريف دالة؛ نقطة تنفيذ أو Composable تستقبل مدخلات وتنتج أثراً أو واجهة.
 * السطر 99: تعريف دالة؛ نقطة تنفيذ أو Composable تستقبل مدخلات وتنتج أثراً أو واجهة.
 * السطر 100: تعريف دالة؛ نقطة تنفيذ أو Composable تستقبل مدخلات وتنتج أثراً أو واجهة.
 * السطر 101: تعريف دالة؛ نقطة تنفيذ أو Composable تستقبل مدخلات وتنتج أثراً أو واجهة.
 * السطر 102: تعريف دالة؛ نقطة تنفيذ أو Composable تستقبل مدخلات وتنتج أثراً أو واجهة.
 * السطر 103: تعريف دالة؛ نقطة تنفيذ أو Composable تستقبل مدخلات وتنتج أثراً أو واجهة.
 * السطر 104: تعريف دالة؛ نقطة تنفيذ أو Composable تستقبل مدخلات وتنتج أثراً أو واجهة.
 * السطر 105: تعريف دالة؛ نقطة تنفيذ أو Composable تستقبل مدخلات وتنتج أثراً أو واجهة.
 * السطر 106: تعريف دالة؛ نقطة تنفيذ أو Composable تستقبل مدخلات وتنتج أثراً أو واجهة.
 * السطر 107: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 109: تعليمة annotation تضبط سلوك المترجم أو Compose أو إطار العمل.
 * السطر 110: تعريف نوع/كلاس/كائن أو alias؛ يمثل عقداً معمارياً في الملف.
 * السطر 111: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 112: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 113: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 114: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 115: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 116: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 117: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 118: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 119: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 120: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 121: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 122: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 123: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 125: تعريف نوع/كلاس/كائن أو alias؛ يمثل عقداً معمارياً في الملف.
 * السطر 126: تعريف دالة؛ نقطة تنفيذ أو Composable تستقبل مدخلات وتنتج أثراً أو واجهة.
 * السطر 127: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 128: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 129: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 130: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 131: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 132: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 133: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 134: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 135: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 136: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 137: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 138: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 139: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 140: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 141: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 142: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 143: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 144: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 146: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 147: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 148: تعبير lambda يحدد الإجراء الناتج عن تفاعل أو تحويل.
 * السطر 149: المسار البديل للشرط السابق.
 * السطر 150: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 152: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 153: تعبير lambda يحدد الإجراء الناتج عن تفاعل أو تحويل.
 * السطر 154: تعبير lambda يحدد الإجراء الناتج عن تفاعل أو تحويل.
 * السطر 155: المسار البديل للشرط السابق.
 * السطر 156: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 158: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 159: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 160: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 161: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 163: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 165: فرع شرطي يحدد مسار التنفيذ حسب الحالة الحالية.
 * السطر 166: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 167: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 168: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 169: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 170: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 171: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 173: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 174: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 175: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 176: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 177: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 178: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 179: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 180: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 181: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 182: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 184: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 186: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 187: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 188: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 189: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 191: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 192: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 193: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 194: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 196: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 197: تعبير lambda يحدد الإجراء الناتج عن تفاعل أو تحويل.
 * السطر 198: تعبير lambda يحدد الإجراء الناتج عن تفاعل أو تحويل.
 * السطر 199: تعبير lambda يحدد الإجراء الناتج عن تفاعل أو تحويل.
 * السطر 200: تعبير lambda يحدد الإجراء الناتج عن تفاعل أو تحويل.
 * السطر 201: المسار البديل للشرط السابق.
 * السطر 202: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 204: إرجاع قيمة إلى المستدعي وفق العقد الأصلي.
 * السطر 205: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 206: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 207: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 208: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 209: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 210: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 211: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 212: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 213: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 214: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 215: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 216: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 217: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 218: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 220: تعريف دالة؛ نقطة تنفيذ أو Composable تستقبل مدخلات وتنتج أثراً أو واجهة.
 * السطر 221: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 222: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 223: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 224: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 225: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 226: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 227: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 228: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 229: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 230: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 231: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 232: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * --- نهاية الفهرس السطري ---
 */

package com.example.ui.screens.habayeb.components.row

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.R
import com.example.data.local.entities.HabayebTransaction
import com.example.domain.model.TransactionType
import com.example.ui.helper.HabayebMathHelper
import com.example.ui.screens.habayeb.utils.CurrencyConfig
import com.example.ui.screens.habayeb.utils.HabayebDateFormatter
import com.example.ui.theme.financialCreditColor
import com.example.ui.theme.financialDebtColor
import com.example.ui.theme.mizanColors
import com.example.ui.viewmodel.FinanceConstants
import java.math.BigDecimal
import java.util.Date

private const val CURRENCY_NONE_TAG = "NONE"

object RowColors {
    val creditGreen: Color
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.mizanColors.credit

    val debtRed: Color
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.mizanColors.debt

    val mutedGray: Color
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.mizanColors.contentSecondary

    val alertGoldBg: Color
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.mizanColors.alertGoldBackground

    val alertGoldBorder: Color
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.mizanColors.alertGoldBorder

    val alertGoldText: Color
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.mizanColors.alertGoldText

    val infoBlueBg: Color
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.mizanColors.infoBlueBackground

    val infoBlueBorder: Color
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.mizanColors.infoBlueBorder

    val infoBlueText: Color
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.mizanColors.infoBlueText

    val successGreenBg: Color
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.mizanColors.successGreenBackground

    val successGreenBorder: Color
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.mizanColors.successGreenBorder

    val warningRedBorder: Color
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.mizanColors.debtBorder

    val darkGray: Color
        @Composable
        @ReadOnlyComposable
        get() = MaterialTheme.mizanColors.contentSecondary

    // Backwards compatibility helpers
    fun creditGreen(isDark: Boolean) = financialCreditColor(isDark)
    fun debtRed(isDark: Boolean) = financialDebtColor(isDark)
    fun mutedGray(isDark: Boolean): Color = financialDebtColor(isDark)
    fun alertGoldBg(isDark: Boolean): Color = financialDebtColor(isDark)
    fun alertGoldBorder(isDark: Boolean): Color = financialDebtColor(isDark)
    fun alertGoldText(isDark: Boolean): Color = financialDebtColor(isDark)
    fun infoBlueBg(isDark: Boolean): Color = financialDebtColor(isDark)
    fun infoBlueBorder(isDark: Boolean): Color = financialDebtColor(isDark)
    fun infoBlueText(isDark: Boolean): Color = financialDebtColor(isDark)
    fun successGreenBg(isDark: Boolean): Color = financialDebtColor(isDark)
    fun successGreenBorder(isDark: Boolean): Color = financialDebtColor(isDark)
    fun warningRedBorder(isDark: Boolean): Color = financialDebtColor(isDark)
    fun darkGray(isDark: Boolean): Color = financialDebtColor(isDark)
}

@Immutable
data class TransactionRowCachedData(
    val cleanDescription: String,
    val indicatorColor: Color,
    val txArrow: ImageVector,
    val formattedAmount: String,
    val displayCurrency: String,
    val equivalentAmountText: String?,
    val dayNameResId: Int,
    val dateStr: String,
    val timeStr: String,
    val isTxForeign: Boolean,
    val isCalculated: Boolean,
    val typeResId: Int
)

object CustomerTransactionRowStateCalculator {
    fun calculate(
        tx: HabayebTransaction,
        currencySymbol: String,
        initialType: String,
        debtColor: Color,
        creditColor: Color
    ): TransactionRowCachedData {
        val parsedCurrencyInfo = CurrencyConfig.parseTransactionCurrency(tx.description, CURRENCY_NONE_TAG)
        val txCurrencySymbol = if (tx.currencyCode != FinanceConstants.DEFAULT_CURRENCY_CODE && tx.currencyCode.isNotBlank()) {
            tx.currencyCode
        } else if (parsedCurrencyInfo.first != CURRENCY_NONE_TAG) {
            parsedCurrencyInfo.first
        } else if (tx.baseCurrencyCode != FinanceConstants.DEFAULT_CURRENCY_CODE && tx.baseCurrencyCode.isNotBlank()) {
            tx.baseCurrencyCode
        } else {
            currencySymbol
        }
        val isTxForeign = txCurrencySymbol != currencySymbol
        val cleanDescription = if (parsedCurrencyInfo.first != CURRENCY_NONE_TAG) parsedCurrencyInfo.second else tx.description

        val txType = TransactionType.fromValue(tx.type)
        val indicatorColor = when (txType) {
            TransactionType.OWED_BY_THEM, TransactionType.OWED_TO_THEM -> debtColor
            else -> creditColor
        }

        val txArrow = when (txType) {
            TransactionType.OWED_BY_THEM, TransactionType.PAYMENT_TO_THEM -> Icons.Default.ArrowUpward
            TransactionType.PAYMENT_BY_THEM, TransactionType.OWED_TO_THEM -> Icons.Default.ArrowDownward
            else -> Icons.Default.ArrowUpward
        }

        val displayAmount: BigDecimal
        val displayCurrency: String
        val equivalentAmount: BigDecimal?
        val equivalentCurrency: String?

        val sourceAmount = if (tx.foreignAmount.compareTo(BigDecimal.ZERO) > 0) tx.foreignAmount else tx.amount

        if (tx.isRateCalculated) {
            val baseCurrency = if (tx.baseCurrencyCode != FinanceConstants.DEFAULT_CURRENCY_CODE && tx.baseCurrencyCode.isNotBlank()) {
                tx.baseCurrencyCode
            } else {
                FinanceConstants.FALLBACK_CURRENCY_SYMBOL
            }
            val origCurrency = txCurrencySymbol
            
            displayAmount = sourceAmount
            displayCurrency = origCurrency
            equivalentAmount = tx.equivalentAmount
            equivalentCurrency = baseCurrency
        } else {
            displayAmount = sourceAmount
            displayCurrency = txCurrencySymbol
            equivalentAmount = null
            equivalentCurrency = null
        }

        val formattedAmount = HabayebMathHelper.formatSmart(displayAmount)

        val equivalentAmountText = if (equivalentAmount != null && equivalentCurrency != null) {
            val formattedEquiv = HabayebMathHelper.formatSmart(equivalentAmount)
            "($formattedEquiv $equivalentCurrency)"
        } else null

        val d = Date(tx.timestamp * 1000L)
        val dateStr = HabayebDateFormatter.formatShortDate(d)
        val timeStr = HabayebDateFormatter.formatTime12h(d)
        val dayNameResId = HabayebDateFormatter.getDayOfWeekResId(tx.timestamp)

        val typeResId = when (txType) {
            TransactionType.OWED_BY_THEM -> R.string.habayeb_pdf_tx_owed_by
            TransactionType.PAYMENT_BY_THEM -> R.string.habayeb_pdf_tx_payment_by
            TransactionType.OWED_TO_THEM -> R.string.habayeb_pdf_tx_owed_to
            TransactionType.PAYMENT_TO_THEM -> R.string.habayeb_pdf_tx_payment_to
            else -> R.string.habayeb_pdf_tx_generic
        }

        return TransactionRowCachedData(
            cleanDescription = cleanDescription,
            indicatorColor = indicatorColor,
            txArrow = txArrow,
            formattedAmount = formattedAmount,
            displayCurrency = displayCurrency,
            equivalentAmountText = equivalentAmountText,
            dayNameResId = dayNameResId,
            dateStr = dateStr,
            timeStr = timeStr,
            isTxForeign = isTxForeign,
            isCalculated = tx.isRateCalculated,
            typeResId = typeResId
        )
    }

    fun calculate(
        tx: HabayebTransaction,
        isDark: Boolean,
        currencySymbol: String,
        initialType: String
    ): TransactionRowCachedData = calculate(
        tx = tx,
        currencySymbol = currencySymbol,
        initialType = initialType,
        debtColor = financialDebtColor(isDark),
        creditColor = financialCreditColor(isDark)
    )
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
