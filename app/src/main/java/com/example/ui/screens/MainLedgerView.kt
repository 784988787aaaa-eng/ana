/*
 * ╔══════════════════════════════════════════════════════════════════════╗
 * ║ التوثيق العربي الهندسي — MASTERCLASS / BATCH 08                   ║
 * ╚══════════════════════════════════════════════════════════════════════╝
 *
 * الملف: app/src/main/java/com/example/ui/screens/MainLedgerView.kt
 * القطاع المعماري: Ledger UI/UX.
 *
 * الوصف المعماري:
 * مكوّن أساسي لواجهة دفتر الحسابات (MainLedgerView) ينسق العرض والتحديد والإجراءات المرتبطة بالسجل.
 *
 * الرؤية التعليمية والبصرية:
 * عند قراءة هذا الملف، تخيل شاشة الهاتف في واجهة «الدفتر»: كل عنصر Compose
 * ظاهر أمام المستخدم له هنا تمثيل برمجي يحدد موضعه، حالته، وما يحدث بعد النقر
 * أو الإدخال أو السحب أو الاختيار. الملف يصف طبقة العرض والتنسيق؛ أما الحسابات
 * المالية ومصادر البيانات فتظل في العقود التي يستدعيها الكود الأصلي.
 *
 * بروتوكول القدسية البرمجية:
 * تم إدراج النص التنفيذي الأصلي كما هو حرفياً بعد هذا الرأس، دون حذف أو تعديل
 * أو إعادة ترتيب لأي تعليمة. جميع الإضافات التوثيقية في هذا الملف تعليقات فقط.
 * البصمة SHA-256 للنص الأصلي قبل التوثيق: dbb5bf0054b72316859523258ce2c391e72335dd7cf09437085f30ce933c4fa1
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
 * السطر 24: استيراد اعتماد خارجي/داخلي تستخدمه الشيفرة الأصلية.
 * السطر 25: استيراد اعتماد خارجي/داخلي تستخدمه الشيفرة الأصلية.
 * السطر 26: استيراد اعتماد خارجي/داخلي تستخدمه الشيفرة الأصلية.
 * السطر 27: استيراد اعتماد خارجي/داخلي تستخدمه الشيفرة الأصلية.
 * السطر 28: استيراد اعتماد خارجي/داخلي تستخدمه الشيفرة الأصلية.
 * السطر 29: استيراد اعتماد خارجي/داخلي تستخدمه الشيفرة الأصلية.
 * السطر 30: استيراد اعتماد خارجي/داخلي تستخدمه الشيفرة الأصلية.
 * السطر 31: استيراد اعتماد خارجي/داخلي تستخدمه الشيفرة الأصلية.
 * السطر 32: استيراد اعتماد خارجي/داخلي تستخدمه الشيفرة الأصلية.
 * السطر 33: استيراد اعتماد خارجي/داخلي تستخدمه الشيفرة الأصلية.
 * السطر 34: استيراد اعتماد خارجي/داخلي تستخدمه الشيفرة الأصلية.
 * السطر 35: استيراد اعتماد خارجي/داخلي تستخدمه الشيفرة الأصلية.
 * السطر 36: استيراد اعتماد خارجي/داخلي تستخدمه الشيفرة الأصلية.
 * السطر 37: استيراد اعتماد خارجي/داخلي تستخدمه الشيفرة الأصلية.
 * السطر 38: استيراد اعتماد خارجي/داخلي تستخدمه الشيفرة الأصلية.
 * السطر 39: استيراد اعتماد خارجي/داخلي تستخدمه الشيفرة الأصلية.
 * السطر 40: استيراد اعتماد خارجي/داخلي تستخدمه الشيفرة الأصلية.
 * السطر 41: استيراد اعتماد خارجي/داخلي تستخدمه الشيفرة الأصلية.
 * السطر 42: استيراد اعتماد خارجي/داخلي تستخدمه الشيفرة الأصلية.
 * السطر 43: استيراد اعتماد خارجي/داخلي تستخدمه الشيفرة الأصلية.
 * السطر 44: استيراد اعتماد خارجي/داخلي تستخدمه الشيفرة الأصلية.
 * السطر 45: استيراد اعتماد خارجي/داخلي تستخدمه الشيفرة الأصلية.
 * السطر 46: استيراد اعتماد خارجي/داخلي تستخدمه الشيفرة الأصلية.
 * السطر 47: استيراد اعتماد خارجي/داخلي تستخدمه الشيفرة الأصلية.
 * السطر 48: استيراد اعتماد خارجي/داخلي تستخدمه الشيفرة الأصلية.
 * السطر 49: استيراد اعتماد خارجي/داخلي تستخدمه الشيفرة الأصلية.
 * السطر 51: تعليمة annotation تضبط سلوك المترجم أو Compose أو إطار العمل.
 * السطر 52: تعريف دالة؛ نقطة تنفيذ أو Composable تستقبل مدخلات وتنتج أثراً أو واجهة.
 * السطر 53: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 54: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 55: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 56: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 57: تعبير lambda يحدد الإجراء الناتج عن تفاعل أو تحويل.
 * السطر 58: تعبير lambda يحدد الإجراء الناتج عن تفاعل أو تحويل.
 * السطر 59: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 60: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 61: تعبير lambda يحدد الإجراء الناتج عن تفاعل أو تحويل.
 * السطر 62: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 63: تعبير lambda يحدد الإجراء الناتج عن تفاعل أو تحويل.
 * السطر 64: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 65: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 66: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 68: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 69: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 70: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 71: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 72: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 74: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 75: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 76: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 77: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 79: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 80: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 81: اختيار مسار تنفيذ بناءً على حالة/قيمة محددة.
 * السطر 82: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 83: فرع شرطي يحدد مسار التنفيذ حسب الحالة الحالية.
 * السطر 84: أثر Compose مرتبط بدورة حياة التركيب أو تغيّر المفاتيح.
 * السطر 85: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 86: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 87: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 88: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 89: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 90: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 91: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 92: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 94: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 95: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 96: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 97: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 98: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 99: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 100: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 102: أثر Compose مرتبط بدورة حياة التركيب أو تغيّر المفاتيح.
 * السطر 103: فرع شرطي يحدد مسار التنفيذ حسب الحالة الحالية.
 * السطر 104: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 105: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 106: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 107: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 108: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 110: أثر Compose مرتبط بدورة حياة التركيب أو تغيّر المفاتيح.
 * السطر 111: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 112: فرع شرطي يحدد مسار التنفيذ حسب الحالة الحالية.
 * السطر 113: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 114: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 115: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 117: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 118: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 120: أثر Compose مرتبط بدورة حياة التركيب أو تغيّر المفاتيح.
 * السطر 121: فرع شرطي يحدد مسار التنفيذ حسب الحالة الحالية.
 * السطر 122: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 123: تعليق أصلي من الشيفرة؛ محفوظ دون تعديل.
 * السطر 124: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 125: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 126: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 128: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 129: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 131: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 132: فرع شرطي يحدد مسار التنفيذ حسب الحالة الحالية.
 * السطر 133: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 134: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 135: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 136: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 137: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 138: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 139: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 140: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 141: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 143: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 144: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 146: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 147: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 148: تعبير lambda يحدد الإجراء الناتج عن تفاعل أو تحويل.
 * السطر 149: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 150: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 151: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 152: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 153: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 154: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 155: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 156: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 157: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 158: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 159: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 160: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 161: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 162: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 164: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 165: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 166: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 167: تعبير lambda يحدد الإجراء الناتج عن تفاعل أو تحويل.
 * السطر 168: تعبير lambda يحدد الإجراء الناتج عن تفاعل أو تحويل.
 * السطر 169: المسار البديل للشرط السابق.
 * السطر 170: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 171: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 173: استدعاء Composable يرسم جزءاً من واجهة الهاتف أو ينظم تخطيطها.
 * السطر 174: استدعاء Composable يرسم جزءاً من واجهة الهاتف أو ينظم تخطيطها.
 * السطر 175: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 176: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 177: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 178: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 179: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 180: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 181: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 182: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 183: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 184: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 185: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 186: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 187: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 188: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 189: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 190: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 191: تعبير lambda يحدد الإجراء الناتج عن تفاعل أو تحويل.
 * السطر 192: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 193: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 194: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 195: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 196: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 198: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 199: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 200: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 201: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 202: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 203: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 204: تعبير lambda يحدد الإجراء الناتج عن تفاعل أو تحويل.
 * السطر 205: تعبير lambda يحدد الإجراء الناتج عن تفاعل أو تحويل.
 * السطر 206: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 207: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 208: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 209: تعبير lambda يحدد الإجراء الناتج عن تفاعل أو تحويل.
 * السطر 210: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 211: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 212: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 213: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 214: تعبير lambda يحدد الإجراء الناتج عن تفاعل أو تحويل.
 * السطر 215: تعبير lambda يحدد الإجراء الناتج عن تفاعل أو تحويل.
 * السطر 216: تعبير lambda يحدد الإجراء الناتج عن تفاعل أو تحويل.
 * السطر 217: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 218: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 219: تعبير lambda يحدد الإجراء الناتج عن تفاعل أو تحويل.
 * السطر 220: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 221: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 222: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 224: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 225: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 226: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 227: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 228: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 229: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 230: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 231: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 232: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 233: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 234: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 235: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 237: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 238: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 239: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 240: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 241: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 242: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 243: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 244: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 245: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 246: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 247: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 248: فرع شرطي يحدد مسار التنفيذ حسب الحالة الحالية.
 * السطر 249: فرع شرطي يحدد مسار التنفيذ حسب الحالة الحالية.
 * السطر 250: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 251: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 252: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 253: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 254: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 255: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 256: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 257: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 258: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 260: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 261: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 262: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 263: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 264: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 265: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 266: تعبير lambda يحدد الإجراء الناتج عن تفاعل أو تحويل.
 * السطر 267: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 268: فرع شرطي يحدد مسار التنفيذ حسب الحالة الحالية.
 * السطر 269: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 270: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 271: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 272: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 273: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 274: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 275: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 276: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 277: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 278: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 279: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 280: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 281: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 282: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 283: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 284: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 285: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 286: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 287: تعبير lambda يحدد الإجراء الناتج عن تفاعل أو تحويل.
 * السطر 288: تعبير lambda يحدد الإجراء الناتج عن تفاعل أو تحويل.
 * السطر 289: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 290: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 291: تعبير lambda يحدد الإجراء الناتج عن تفاعل أو تحويل.
 * السطر 292: تعبير lambda يحدد الإجراء الناتج عن تفاعل أو تحويل.
 * السطر 293: تعبير lambda يحدد الإجراء الناتج عن تفاعل أو تحويل.
 * السطر 294: تعبير lambda يحدد الإجراء الناتج عن تفاعل أو تحويل.
 * السطر 295: تعبير lambda يحدد الإجراء الناتج عن تفاعل أو تحويل.
 * السطر 296: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 297: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 298: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 299: تعبير lambda يحدد الإجراء الناتج عن تفاعل أو تحويل.
 * السطر 300: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 301: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 302: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 303: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 304: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 305: تعبير lambda يحدد الإجراء الناتج عن تفاعل أو تحويل.
 * السطر 306: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 307: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 308: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 309: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 310: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 311: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 312: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 313: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 314: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 315: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 316: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 317: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 318: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 319: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 320: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 321: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 322: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 323: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 324: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 326: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 327: أثر Compose مرتبط بدورة حياة التركيب أو تغيّر المفاتيح.
 * السطر 328: فرع شرطي يحدد مسار التنفيذ حسب الحالة الحالية.
 * السطر 329: المسار البديل للشرط السابق.
 * السطر 330: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 332: فرع شرطي يحدد مسار التنفيذ حسب الحالة الحالية.
 * السطر 333: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 335: استدعاء Composable يرسم جزءاً من واجهة الهاتف أو ينظم تخطيطها.
 * السطر 336: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 337: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 338: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 339: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 340: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 341: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 342: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 343: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 344: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 345: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 346: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 347: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 348: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 349: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 350: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 351: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * --- نهاية الفهرس السطري ---
 */

package com.example.ui.screens

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.local.entities.AppSettings
import com.example.domain.FormatUtils
import com.example.ui.components.circularReveal
import com.example.ui.screens.ledger.components.LedgerBottomDock
import com.example.ui.screens.ledger.components.MainLedgerDialogsManager
import com.example.ui.screens.ledger.components.MainLedgerListSection
import com.example.ui.screens.ledger.components.MainLedgerSelectionBar
import com.example.ui.screens.ledger.components.PinnedMainLedgerHeader
import com.example.ui.viewmodel.FinanceViewModel
import com.example.ui.viewmodel.HabayebFinanceViewModel
import com.example.ui.viewmodel.SecurityAndLicenseViewModel
import kotlinx.coroutines.launch
import java.math.BigDecimal

@Composable
fun MainLedgerView(
    viewModel: FinanceViewModel,
    habayebViewModel: HabayebFinanceViewModel,
    securityViewModel: SecurityAndLicenseViewModel,
    settings: AppSettings,
    onBackIntercept: (Boolean) -> Unit,
    onMenuClick: () -> Unit = {},
    isDrawerOpen: Boolean = false,
    isFloatingSearchActive: Boolean = false,
    onFloatingSearchActiveChanged: (Boolean) -> Unit = {},
    isSearchActive: Boolean = false,
    onSearchActiveChanged: (Boolean) -> Unit = {},
    contentPadding: PaddingValues = PaddingValues()
) {
    val uiController = rememberMainLedgerUiController()

    val bottomPadding = contentPadding.calculateBottomPadding()
    val totalCash by viewModel.totalCashState.collectAsStateWithLifecycle()
    val commitments by viewModel.commitmentsState.collectAsStateWithLifecycle()
    val monthlyLedger by viewModel.monthlyLedgerState.collectAsStateWithLifecycle()
    val appSettingsState by viewModel.settingsState.collectAsStateWithLifecycle()

    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val view = LocalView.current

    val systemDark = isSystemInDarkTheme()
    val isDark = remember(appSettingsState.themeMode, systemDark) {
        when (appSettingsState.themeMode) { 1 -> false; 2 -> true; else -> systemDark }
    }
    if (!view.isInEditMode) {
        SideEffect {
            val window = (context as? Activity)?.window ?: return@SideEffect
            window.statusBarColor = android.graphics.Color.TRANSPARENT
            window.navigationBarColor = android.graphics.Color.TRANSPARENT
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = false
            insetsController.isAppearanceLightNavigationBars = !isDark
        }
    }

    val lazyListState = rememberLazyListState()
    val collapseFractionProvider = remember {
        { if (lazyListState.firstVisibleItemIndex > 0) 1f else (lazyListState.firstVisibleItemScrollOffset.toFloat() / 180f).coerceIn(0f, 1f) }
    }
    val isPinnedVisible = remember {
        derivedStateOf { collapseFractionProvider() > 0f }
    }

    LaunchedEffect(isSearchActive) {
        if (isSearchActive && uiController.activeDialogState !is MainLedgerDialogState.Search) {
            uiController.activeDialogState = MainLedgerDialogState.Search
        } else if (!isSearchActive && uiController.activeDialogState is MainLedgerDialogState.Search) {
            uiController.activeDialogState = MainLedgerDialogState.None
        }
    }

    LaunchedEffect(uiController.activeDialogState) {
        val isSearch = uiController.activeDialogState is MainLedgerDialogState.Search
        if (isSearch != isSearchActive) {
            onSearchActiveChanged(isSearch)
        }
    }

    val deviceId by securityViewModel.deviceIdState.collectAsStateWithLifecycle()
    val showActivationRequired by securityViewModel.showActivationRequired.collectAsStateWithLifecycle()

    LaunchedEffect(showActivationRequired) {
        if (showActivationRequired) {
            uiController.activeDialogState = MainLedgerDialogState.DeviceActivation
            // إعادة ضبط حالة مطالبة التفعيل عبر ViewModel لمنع تكرار فتح الحوار
            securityViewModel.resetActivationRequired()
        }
    }

    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val searchResults by viewModel.searchResultsState.collectAsStateWithLifecycle()

    BackHandler(enabled = !isDrawerOpen && (uiController.isHabayebActive || uiController.activeDialogState !is MainLedgerDialogState.None || uiController.isSelectionMode || uiController.isDaySelectionMode || uiController.expandedDayKeys.isNotEmpty())) {
        if (uiController.isHabayebActive) {
            uiController.isHabayebActive = false
        } else if (uiController.activeDialogState !is MainLedgerDialogState.None) {
            uiController.activeDialogState = MainLedgerDialogState.None
        } else if (uiController.isSelectionMode || uiController.isDaySelectionMode) {
            uiController.clearSelection()
        } else if (uiController.expandedDayKeys.isNotEmpty()) {
            uiController.expandedDayKeys = emptySet()
        }
    }

    val linkHabayebDebts by habayebViewModel.linkHabayebDebtsState.collectAsStateWithLifecycle()
    val habayebOwedByThemTotal by habayebViewModel.habayebOwedByThemTotalState.collectAsStateWithLifecycle()

    val computedCommitments = remember(commitments, totalCash, linkHabayebDebts, habayebOwedByThemTotal) {
        var remainingCash = if (linkHabayebDebts) totalCash.add(habayebOwedByThemTotal) else totalCash
        commitments.map { fc ->
            val target = fc.targetAmount
            val alreadyPaid = fc.currentProgress
            val needed = (target.subtract(alreadyPaid)).max(BigDecimal.ZERO)
            val allocatedFromCash = if (remainingCash >= needed) {
                remainingCash = remainingCash.subtract(needed)
                needed
            } else if (remainingCash > BigDecimal.ZERO) {
                val temp = remainingCash
                remainingCash = BigDecimal.ZERO
                temp
            } else BigDecimal.ZERO
            Triple(fc, alreadyPaid.add(allocatedFromCash), needed.subtract(allocatedFromCash))
        }
    }

    val isPrivacyMode by securityViewModel.isPrivacyModeEnabled.collectAsStateWithLifecycle()
    val allKeys = remember(monthlyLedger) { monthlyLedger.flatMap { ml -> ml.days.map { "${ml.monthKey}_${it.dayNumber}" } } }
    val selectedDayKeysCountText = when (uiController.selectedDayKeys.size) {
        1 -> stringResource(R.string.ledger_selected_days_count_1)
        2 -> stringResource(R.string.ledger_selected_days_count_2)
        else -> stringResource(R.string.ledger_selected_days_count_more, uiController.selectedDayKeys.size)
    }
    val isSelectAllChecked = uiController.selectedDayKeys.size == allKeys.size && allKeys.isNotEmpty()

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(modifier = Modifier.fillMaxSize()) {
            PinnedMainLedgerHeader(
                isDaySelectionMode = uiController.isDaySelectionMode,
                selectedDayKeys = uiController.selectedDayKeys,
                onCancelDaySelection = { uiController.cancelDaySelection() },
                onSelectAllDays = { uiController.selectAllDays(allKeys) },
                onDeleteSelectedDays = { if (uiController.selectedDayKeys.isNotEmpty()) uiController.activeDialogState = MainLedgerDialogState.DeleteDaysConfirm },
                isSelectAllChecked = isSelectAllChecked,
                selectedDayKeysCountText = selectedDayKeysCountText,
                onMenuClick = onMenuClick,
                onSearchClick = { uiController.activeDialogState = MainLedgerDialogState.Search },
                isFloatingSearchActive = isFloatingSearchActive,
                onFloatingSearchActiveChanged = onFloatingSearchActiveChanged,
                totalCash = totalCash,
                isPrivacyMode = isPrivacyMode,
                onTogglePrivacyMode = { securityViewModel.togglePrivacyMode() },
                currencySymbol = settings.currencySymbol,
                formatCurrency = { v, s -> FormatUtils.formatCurrency(v, s, context) },
                commitments = commitments,
                computedCommitments = computedCommitments,
                linkHabayebDebts = linkHabayebDebts,
                onLinkHabayebDebtsChange = { habayebViewModel.toggleLinkHabayebDebts(it) }
            )

            MainLedgerListSection(
                lazyListState = lazyListState,
                bottomPadding = bottomPadding,
                isDaySelectionMode = uiController.isDaySelectionMode,
                selectedDayKeys = uiController.selectedDayKeys,
                currencySymbol = settings.currencySymbol,
                formatCurrency = { v, s -> FormatUtils.formatCurrency(v, s, context) },
                formatDoubleCurrency = { v, s -> FormatUtils.formatDoubleCurrency(v, s, context) },
                monthlyLedger = monthlyLedger,
                isScreenReady = true,
                collapsedMonths = uiController.collapsedMonths,
                onToggleMonthCollapsed = { mKey -> uiController.toggleMonthCollapsed(mKey) },
                expandedDayKeys = uiController.expandedDayKeys,
                haptic = haptic,
                context = context,
                viewModel = viewModel,
                onEditTransaction = { tx -> uiController.activeDialogState = MainLedgerDialogState.AddTransaction(type = tx.type, editingTx = tx) },
                onDayClick = { key -> uiController.handleDayClick(key) },
                onDayLongClick = { key -> uiController.handleDayLongClick(key) },
                isSelectionMode = uiController.isSelectionMode,
                selectedTxIds = uiController.selectedTxIds,
                onTransactionSelectToggle = { txId -> uiController.handleTransactionSelectToggle(txId) },
                modifier = Modifier.weight(1f)
            )
        }

        LedgerBottomDock(
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = bottomPadding + 12.dp),
            isSelectionMode = uiController.isSelectionMode || uiController.isDaySelectionMode,
            selectedTxIdsCount = uiController.selectedTxIds.size,
            onDeleteSelectedClick = {
                viewModel.deleteTransactionsBulk(uiController.selectedTxIds.toList(), context.getString(R.string.ledger_delete_selected_warning, uiController.selectedTxIds.size))
                uiController.clearSelection()
            },
            onShowCommitmentsClick = { uiController.activeDialogState = MainLedgerDialogState.CommitmentsList },
            onAddIncomeClick = { uiController.activeDialogState = MainLedgerDialogState.AddTransaction(type = "INCOME", editingTx = null) },
            onAddExpenseClick = { uiController.activeDialogState = MainLedgerDialogState.AddTransaction(type = "EXPENSE", editingTx = null) }
        )

        MainLedgerSelectionBar(
            isSelectionActive = (uiController.isSelectionMode && uiController.selectedTxIds.isNotEmpty()) || (uiController.isDaySelectionMode && uiController.selectedDayKeys.isNotEmpty()),
            isDaySelectionMode = uiController.isDaySelectionMode,
            isSelectAllChecked = isSelectAllChecked,
            selectedDayKeysCountText = selectedDayKeysCountText,
            selectedTxCount = uiController.selectedTxIds.size,
            allKeys = allKeys,
            selectedDayKeys = uiController.selectedDayKeys,
            haptic = haptic,
            onClearSelection = { uiController.clearSelection() },
            onDeleteClick = {
                if (uiController.isDaySelectionMode) {
                    if (uiController.selectedDayKeys.isNotEmpty()) uiController.activeDialogState = MainLedgerDialogState.DeleteDaysConfirm
                } else if (uiController.selectedTxIds.isNotEmpty()) {
                    viewModel.deleteTransactionsBulk(uiController.selectedTxIds.toList(), context.getString(R.string.ledger_delete_selected_warning, uiController.selectedTxIds.size))
                    uiController.clearSelection()
                }
            },
            bottomPadding = bottomPadding,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }

    MainLedgerDialogsManager(
        showTxDialog = uiController.activeDialogState is MainLedgerDialogState.AddTransaction,
        txDialogType = (uiController.activeDialogState as? MainLedgerDialogState.AddTransaction)?.type ?: "EXPENSE",
        editingTransaction = (uiController.activeDialogState as? MainLedgerDialogState.AddTransaction)?.editingTx,
        currencySymbol = settings.currencySymbol,
        onDismissTxDialog = { uiController.dismissDialog() },
        onSaveTransaction = { id, type, cat, amt, desc ->
            val editingTx = (uiController.activeDialogState as? MainLedgerDialogState.AddTransaction)?.editingTx
            if (editingTx != null) {
                viewModel.updateTransaction(editingTx.copy(amount = BigDecimal(amt.toString()), description = desc, category = cat))
            } else {
                viewModel.addTransaction(type = type, category = cat, amount = amt, description = desc)
            }
            uiController.dismissDialog()
            scope.launch {
                lazyListState.scrollToItem(0)
            }
        },
        showSearch = uiController.activeDialogState is MainLedgerDialogState.Search,
        searchQuery = searchQuery,
        searchResults = searchResults,
        onSearchQueryChange = { viewModel.updateSearchQuery(it) },
        onDismissSearch = { uiController.dismissDialog() },
        showCommitmentsListSheet = uiController.activeDialogState is MainLedgerDialogState.CommitmentsList || uiController.activeDialogState is MainLedgerDialogState.AddCommitment || uiController.activeDialogState is MainLedgerDialogState.ReorderCommitment,
        commitments = commitments,
        computedCommitments = computedCommitments,
        totalCash = totalCash,
        formatCurrency = { v, s -> FormatUtils.formatCurrency(v, s, context) },
        formatBigDecimalCurrency = { v, s -> FormatUtils.formatCurrency(v, s, context) },
        onDismissCommitmentsList = { uiController.dismissDialog() },
        onAddCommitmentClick = { uiController.activeDialogState = MainLedgerDialogState.AddCommitment(editingCommitment = null) },
        onEditCommitmentClick = { fc -> uiController.activeDialogState = MainLedgerDialogState.AddCommitment(editingCommitment = fc) },
        onDeleteCommitment = { name -> viewModel.deleteCommitment(name) },
        onReorderCommitment = { fc, pos -> viewModel.reorderCommitment(fc, pos) },
        onCommitmentCheckedChange = { fc, checked -> viewModel.saveCommitment(fc.name, fc.targetAmount, if (checked) fc.targetAmount else BigDecimal.ZERO) },
        onSetReorderTarget = { fc -> uiController.activeDialogState = MainLedgerDialogState.ReorderCommitment(fc) },
        showCommitmentDialog = uiController.activeDialogState is MainLedgerDialogState.AddCommitment,
        editingCommitment = (uiController.activeDialogState as? MainLedgerDialogState.AddCommitment)?.editingCommitment,
        onDismissCommitmentDialog = { uiController.activeDialogState = MainLedgerDialogState.CommitmentsList },
        onSaveCommitment = { name, targetAmt, progress ->
            viewModel.saveCommitment(name, targetAmt, progress)
            uiController.activeDialogState = MainLedgerDialogState.CommitmentsList
        },
        reorderCommitmentTarget = (uiController.activeDialogState as? MainLedgerDialogState.ReorderCommitment)?.target,
        onDismissReorderTarget = { uiController.activeDialogState = MainLedgerDialogState.CommitmentsList },
        onApplyReorderTarget = { target, pos ->
            viewModel.reorderCommitment(target, pos)
            uiController.activeDialogState = MainLedgerDialogState.CommitmentsList
        },
        showActivationDialog = uiController.activeDialogState is MainLedgerDialogState.DeviceActivation,
        deviceId = deviceId,
        securityViewModel = securityViewModel,
        onDismissActivationDialog = { uiController.dismissDialog() },
        showDeleteDaysDialog = uiController.activeDialogState is MainLedgerDialogState.DeleteDaysConfirm,
        onDismissDeleteDaysDialog = { uiController.dismissDialog() },
        monthlyLedger = monthlyLedger,
        selectedDayKeys = uiController.selectedDayKeys,
        viewModel = viewModel,
        scope = scope,
        context = context,
        onSuccessDeleteDays = {
            uiController.clearSelection()
            uiController.dismissDialog()
        }
    )

    val animProgress = remember { Animatable(0f) }
    LaunchedEffect(uiController.isHabayebActive) {
        if (uiController.isHabayebActive) animProgress.animateTo(1f, animationSpec = tween(450, easing = FastOutSlowInEasing))
        else animProgress.animateTo(0f, animationSpec = tween(400, easing = FastOutSlowInEasing))
    }

    if (animProgress.value > 0f) {
        val revealCenter = Offset(250f, 400f)

        Box(
            modifier = Modifier.fillMaxSize().graphicsLayer {
                alpha = animProgress.value
                scaleX = animProgress.value
                scaleY = animProgress.value
                transformOrigin = TransformOrigin(0.5f, 0.5f)
            }.circularReveal(animProgress.value, revealCenter, isRelative = true)
        ) {
            HabayebScreen(
                viewModel = habayebViewModel,
                securityViewModel = securityViewModel,
                onMenuClick = onMenuClick,
                onClose = { scope.launch { uiController.isHabayebActive = false } }
            )
        }
    }
}



/*
 * --- ملاحظات وتوصيات المعمارية البرمجية ---
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
