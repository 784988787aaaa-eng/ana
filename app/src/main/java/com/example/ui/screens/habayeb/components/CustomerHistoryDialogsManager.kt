/*
 * ╔══════════════════════════════════════════════════════════════════════╗
 * ║ التوثيق العربي الهندسي — MASTERCLASS / BATCH 07                   ║
 * ╚══════════════════════════════════════════════════════════════════════╝
 *
 * الملف: app/src/main/java/com/example/ui/screens/habayeb/components/CustomerHistoryDialogsManager.kt
 * القطاع المعماري: Habayeb UI/UX.
 *
 * الوصف المعماري:
 * حوار Compose متخصص (CustomerHistoryDialogsManager) يعرض قراراً أو إعداداً أو إدخالاً في سياق شاشة الحبايب.
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
 * البصمة SHA-256 للنص الأصلي قبل التوثيق: 2b4fc889a31b13fda49fbcfc4d394e7c747c7173b36e514cd6af48fffdf79d33
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
 * السطر 19: تعريف نوع/كلاس/كائن أو alias؛ يمثل عقداً معمارياً في الملف.
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
 * السطر 33: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 34: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 35: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 37: تعليمة annotation تضبط سلوك المترجم أو Compose أو إطار العمل.
 * السطر 38: تعريف دالة؛ نقطة تنفيذ أو Composable تستقبل مدخلات وتنتج أثراً أو واجهة.
 * السطر 39: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 40: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 41: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 42: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 43: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 44: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 45: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 46: تعبير lambda يحدد الإجراء الناتج عن تفاعل أو تحويل.
 * السطر 47: تعبير lambda يحدد الإجراء الناتج عن تفاعل أو تحويل.
 * السطر 48: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 49: تعبير lambda يحدد الإجراء الناتج عن تفاعل أو تحويل.
 * السطر 50: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 51: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 52: تعبير lambda يحدد الإجراء الناتج عن تفاعل أو تحويل.
 * السطر 53: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 54: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 55: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 57: تعريف دالة؛ نقطة تنفيذ أو Composable تستقبل مدخلات وتنتج أثراً أو واجهة.
 * السطر 58: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 59: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 61: فرع شرطي يحدد مسار التنفيذ حسب الحالة الحالية.
 * السطر 62: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 63: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 64: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 65: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 66: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 67: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 68: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 69: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 70: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 71: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 72: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 74: فرع شرطي يحدد مسار التنفيذ حسب الحالة الحالية.
 * السطر 75: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 76: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 77: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 78: تعبير lambda يحدد الإجراء الناتج عن تفاعل أو تحويل.
 * السطر 79: فرع شرطي يحدد مسار التنفيذ حسب الحالة الحالية.
 * السطر 80: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 81: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 82: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 83: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 84: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 85: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 86: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 87: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 88: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 89: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 90: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 91: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 92: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 94: فرع شرطي يحدد مسار التنفيذ حسب الحالة الحالية.
 * السطر 95: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 96: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 97: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 98: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 99: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 100: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 101: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 102: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 103: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 104: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 105: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 106: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 108: فرع شرطي يحدد مسار التنفيذ حسب الحالة الحالية.
 * السطر 109: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 110: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 111: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 112: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 113: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 114: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 116: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 117: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 118: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 119: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 120: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 121: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 122: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 123: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 124: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 125: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 126: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 127: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 128: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 129: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 130: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 132: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 133: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 134: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 135: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 136: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 137: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 138: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 139: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 140: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 141: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 142: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 143: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 144: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 145: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 146: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 147: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 148: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
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
 * السطر 167: فرع شرطي يحدد مسار التنفيذ حسب الحالة الحالية.
 * السطر 168: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 169: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 170: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 171: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 172: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 173: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 174: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 175: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 176: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 177: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 178: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 180: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 181: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 182: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 183: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 184: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 185: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 186: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 187: تعبير lambda يحدد الإجراء الناتج عن تفاعل أو تحويل.
 * السطر 188: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 189: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 190: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 191: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 192: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 193: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 194: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 195: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 196: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 198: فرع شرطي يحدد مسار التنفيذ حسب الحالة الحالية.
 * السطر 199: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 200: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 201: تعبير lambda يحدد الإجراء الناتج عن تفاعل أو تحويل.
 * السطر 202: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 203: تعبير lambda يحدد الإجراء الناتج عن تفاعل أو تحويل.
 * السطر 204: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 205: تعبير lambda يحدد الإجراء الناتج عن تفاعل أو تحويل.
 * السطر 206: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 207: تعبير lambda يحدد الإجراء الناتج عن تفاعل أو تحويل.
 * السطر 208: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 209: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 210: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 211: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 213: فرع شرطي يحدد مسار التنفيذ حسب الحالة الحالية.
 * السطر 214: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 215: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 216: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 217: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 218: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 219: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 220: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 221: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 222: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 223: تعبير lambda يحدد الإجراء الناتج عن تفاعل أو تحويل.
 * السطر 224: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 225: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 226: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 227: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 228: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 229: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 230: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 231: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 232: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 233: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 234: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 235: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 236: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 237: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 238: تعبير lambda يحدد الإجراء الناتج عن تفاعل أو تحويل.
 * السطر 239: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 240: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 241: تعبير lambda يحدد الإجراء الناتج عن تفاعل أو تحويل.
 * السطر 242: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 243: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 244: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 245: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 246: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * --- نهاية الفهرس السطري ---
 */

package com.example.ui.screens.habayeb.components

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.example.R
import com.example.data.local.entities.HabayebCustomer
import com.example.data.local.entities.HabayebTransaction
import com.example.ui.screens.habayeb.utils.CustomerShareHelper
import com.example.ui.screens.habayeb.utils.ExchangeRateHelper
import com.example.ui.screens.habayeb.utils.HabayebRecurringManager
import com.example.ui.viewmodel.FinanceConstants
import com.example.ui.viewmodel.HabayebFinanceViewModel
import java.math.BigDecimal

data class CustomerHistoryDialogState(
    val confirmDeleteCust: Boolean = false,
    val showEditNameDialog: Boolean = false,
    val editingTransactionForDialog: HabayebTransaction? = null,
    val showAddTransactionDialogFromHistory: HabayebCustomer? = null,
    val defaultTransactionTypeFromHistory: String = FinanceConstants.TYPE_OWED_BY_THEM,
    val transactionForOptionsDialog: HabayebTransaction? = null,
    val transactionForAutoRepeatDialog: HabayebTransaction? = null,
    val showDeleteBulkTxConfirmDialog: Boolean = false,
    val showFilterMenu: Boolean = false,
    val dateFilterMode: Int = 0,
    val customStartDate: Long? = null,
    val customEndDate: Long? = null,
    val typeFilterMode: Int = 0,
    val showRateModifyDialog: Boolean = false,
    val exchangeTxToModify: HabayebTransaction? = null
)

@Composable
fun CustomerHistoryDialogsManager(
    activeCustomer: HabayebCustomer,
    viewModel: HabayebFinanceViewModel,
    currencySymbol: String,
    netDebt: BigDecimal = BigDecimal.ZERO,
    activeThemeColor: Color,
    activeSubColor: Color,
    dialogState: CustomerHistoryDialogState,
    onDialogStateChange: ((CustomerHistoryDialogState) -> CustomerHistoryDialogState) -> Unit,
    onCustomerDeleted: () -> Unit,
    selectedTxIds: SnapshotStateList<String>,
    onIsTxMultiSelectActiveChange: (Boolean) -> Unit,
    activeRecurringTxIds: Set<String>,
    txSequenceNumbers: Map<String, Int>,
    onRefreshRecurringTrigger: () -> Unit,
    allCustomerTxs: List<HabayebTransaction> = emptyList()
) {
    val context = LocalContext.current

    fun updateState(transform: (CustomerHistoryDialogState) -> CustomerHistoryDialogState) {
        onDialogStateChange(transform)
    }

    if (dialogState.confirmDeleteCust) {
        CustomerDeleteConfirmationDialog(
            customer = activeCustomer,
            onConfirm = {
                viewModel.deleteHabayebCustomer(activeCustomer.id)
                Toast.makeText(context, context.getString(R.string.habayeb_toast_delete_success), Toast.LENGTH_SHORT).show()
                updateState { it.copy(confirmDeleteCust = false) }
                onCustomerDeleted()
            },
            onDismiss = { updateState { it.copy(confirmDeleteCust = false) } }
        )
    }

    if (dialogState.showEditNameDialog) {
        CustomerEditDialog(
            customer = activeCustomer,
            activeThemeColor = activeThemeColor,
            onConfirm = { newName, newPhone ->
                if (newName.isNotBlank()) {
                    viewModel.updateHabayebCustomer(
                        activeCustomer.copy(
                            name = newName.trim(),
                            phone = newPhone.trim()
                        )
                    )
                    Toast.makeText(context, context.getString(R.string.habayeb_toast_update_success), Toast.LENGTH_SHORT).show()
                }
                updateState { it.copy(showEditNameDialog = false) }
            },
            onDismiss = { updateState { it.copy(showEditNameDialog = false) } }
        )
    }

    if (dialogState.showAddTransactionDialogFromHistory != null) {
        AddTransactionPopup(
            customer = dialogState.showAddTransactionDialogFromHistory,
            viewModel = viewModel,
            initialSelectedType = dialogState.defaultTransactionTypeFromHistory,
            editingTransaction = dialogState.editingTransactionForDialog,
            onDismiss = {
                updateState { it.copy(showAddTransactionDialogFromHistory = null, editingTransactionForDialog = null) }
            },
            activeThemeColor = activeThemeColor,
            activeSubColor = activeSubColor
        )
    }

    if (dialogState.transactionForOptionsDialog != null) {
        val optTx = dialogState.transactionForOptionsDialog
        val isRecurringOriginal = optTx.id in activeRecurringTxIds
        val cleanLinkedId = optTx.linkedMainTxId?.trim()?.takeIf { it.isNotEmpty() && it != "0" && it != optTx.id }
        val parentSeq = if (cleanLinkedId != null) {
            txSequenceNumbers[cleanLinkedId]
        } else null

        val onWhatsAppShare = remember(optTx, activeCustomer, netDebt, currencySymbol, allCustomerTxs) {
            { CustomerShareHelper.triggerSingleTxWhatsApp(context, optTx, activeCustomer, netDebt, currencySymbol, allCustomerTxs) }
        }
        val onSmsShare = remember(optTx, activeCustomer, netDebt, currencySymbol, allCustomerTxs) {
            { CustomerShareHelper.triggerSingleTxSms(context, optTx, activeCustomer, netDebt, currencySymbol, allCustomerTxs) }
        }
        val onDeleteAutoRepeat = remember(optTx) {
            {
                val txId = optTx.id
                HabayebRecurringManager.deleteConfigForTransaction(context, txId)
                Toast.makeText(context, context.getString(R.string.habayeb_toast_stop_recurring_success), Toast.LENGTH_SHORT).show()
                onRefreshRecurringTrigger()
                updateState { it.copy(transactionForOptionsDialog = null) }
            }
        }

        TransactionOptionsDialog(
            transaction = optTx,
            customerName = activeCustomer.name,
            onDismiss = { updateState { it.copy(transactionForOptionsDialog = null) } },
            onEdit = {
                updateState {
                    it.copy(
                        editingTransactionForDialog = optTx,
                        defaultTransactionTypeFromHistory = optTx.type,
                        showAddTransactionDialogFromHistory = activeCustomer,
                        transactionForOptionsDialog = null
                    )
                }
            },
            onDelete = {
                val txId = optTx.id
                viewModel.deleteHabayebTransaction(txId)
                HabayebRecurringManager.deleteConfigForTransaction(context, txId)
                Toast.makeText(context, context.getString(R.string.habayeb_toast_delete_tx_success), Toast.LENGTH_SHORT).show()
                onRefreshRecurringTrigger()
                updateState { it.copy(transactionForOptionsDialog = null) }
            },
            onAutoRepeat = {
                updateState { it.copy(transactionForAutoRepeatDialog = optTx, transactionForOptionsDialog = null) }
            },
            onWhatsAppShare = onWhatsAppShare,
            onSmsShare = onSmsShare,
            activeThemeColor = activeThemeColor,
            activeSubColor = activeSubColor,
            isRecurringOriginal = isRecurringOriginal,
            onDeleteAutoRepeat = onDeleteAutoRepeat,
            parentSeqNumber = parentSeq
        )
    }

    if (dialogState.transactionForAutoRepeatDialog != null) {
        RecurringTransactionPopup(
            transaction = dialogState.transactionForAutoRepeatDialog,
            customerName = activeCustomer.name,
            onDismiss = {
                updateState { it.copy(transactionForAutoRepeatDialog = null) }
                onRefreshRecurringTrigger()
            },
            activeThemeColor = activeThemeColor,
            activeSubColor = activeSubColor
        )
    }

    DeleteBulkTxConfirmDialog(
        show = dialogState.showDeleteBulkTxConfirmDialog,
        selectedCount = selectedTxIds.size,
        onDismiss = { updateState { it.copy(showDeleteBulkTxConfirmDialog = false) } },
        onConfirm = {
            val idsToDelete = selectedTxIds.toList()
            viewModel.deleteMultipleHabayebTransactions(idsToDelete)
            idsToDelete.forEach { txId ->
                HabayebRecurringManager.deleteConfigForTransaction(context, txId)
            }
            Toast.makeText(context, context.getString(R.string.habayeb_toast_delete_bulk_success), Toast.LENGTH_SHORT).show()
            selectedTxIds.clear()
            onIsTxMultiSelectActiveChange(false)
            onRefreshRecurringTrigger()
            updateState { it.copy(showDeleteBulkTxConfirmDialog = false) }
        }
    )

    if (dialogState.showFilterMenu) {
        CustomerHistoryFilterSheet(
            dateFilterMode = dialogState.dateFilterMode,
            onDateFilterModeChange = { mode -> updateState { it.copy(dateFilterMode = mode) } },
            customStartDate = dialogState.customStartDate,
            onCustomStartDateChange = { start -> updateState { it.copy(customStartDate = start) } },
            customEndDate = dialogState.customEndDate,
            onCustomEndDateChange = { end -> updateState { it.copy(customEndDate = end) } },
            typeFilterMode = dialogState.typeFilterMode,
            onTypeFilterModeChange = { mode -> updateState { it.copy(typeFilterMode = mode) } },
            activeThemeColor = activeThemeColor,
            onDismissRequest = { updateState { it.copy(showFilterMenu = false) } }
        )
    }

    if (dialogState.showRateModifyDialog && dialogState.exchangeTxToModify != null) {
        val txToModify = dialogState.exchangeTxToModify
        ExchangeRateModifyDialog(
            show = dialogState.showRateModifyDialog,
            tx = txToModify,
            currencySymbol = currencySymbol,
            activeThemeColor = activeThemeColor,
            onDismissRequest = {
                updateState { it.copy(showRateModifyDialog = false, exchangeTxToModify = null) }
            },
            onConfirmRateSetup = { targetCurrency, newRate ->
                val settings = viewModel.settingsState.value
                val targetCurr = currencySymbol
                val newSettings = settings.copy(
                    exchangeRatesJson = ExchangeRateHelper.setRate(settings.exchangeRatesJson, targetCurr, targetCurrency, newRate)
                )
                viewModel.saveSettings(newSettings)
                val rateBigDecimal = newRate
                viewModel.updateTransactionExchangeRate(txToModify.id, rateBigDecimal, true)
                updateState { it.copy(showRateModifyDialog = false, exchangeTxToModify = null) }
            },
            onDeactivateExchange = {
                viewModel.updateTransactionExchangeRate(txToModify.id, txToModify.exchangeRate, false)
                updateState { it.copy(showRateModifyDialog = false, exchangeTxToModify = null) }
            },
            hasStoredRateForCurrency = { curr ->
                ExchangeRateHelper.hasRate(viewModel.settingsState.value.exchangeRatesJson, currencySymbol, curr)
            },
            getStoredRateForCurrency = { curr ->
                ExchangeRateHelper.getRate(viewModel.settingsState.value.exchangeRatesJson, currencySymbol, curr)
            }
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
