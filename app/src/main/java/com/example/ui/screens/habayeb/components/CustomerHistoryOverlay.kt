/*
 * ╔══════════════════════════════════════════════════════════════════════╗
 * ║ التوثيق العربي الهندسي — MASTERCLASS / BATCH 07                   ║
 * ╚══════════════════════════════════════════════════════════════════════╝
 *
 * الملف: app/src/main/java/com/example/ui/screens/habayeb/components/CustomerHistoryOverlay.kt
 * القطاع المعماري: Habayeb UI/UX.
 *
 * الوصف المعماري:
 * مكوّن مخصص للعملاء (CustomerHistoryOverlay) يعرض أو يدير تفاعلاً محدداً ضمن دورة حياة حساب العميل.
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
 * البصمة SHA-256 للنص الأصلي قبل التوثيق: 029b723a58779d752e7c48dd80f9e45ab720c5d27ea6de81a1037be2cc1ea0a1
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
 * السطر 47: تعريف نوع/كلاس/كائن أو alias؛ يمثل عقداً معمارياً في الملف.
 * السطر 48: تعليمة annotation تضبط سلوك المترجم أو Compose أو إطار العمل.
 * السطر 49: تعريف دالة؛ نقطة تنفيذ أو Composable تستقبل مدخلات وتنتج أثراً أو واجهة.
 * السطر 50: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 51: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 52: تعبير lambda يحدد الإجراء الناتج عن تفاعل أو تحويل.
 * السطر 53: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 54: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 55: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 56: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 57: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 58: تعبير lambda يحدد الإجراء الناتج عن تفاعل أو تحويل.
 * السطر 59: تعبير lambda يحدد الإجراء الناتج عن تفاعل أو تحويل.
 * السطر 60: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 61: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 62: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 63: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 64: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 66: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 67: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 68: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 70: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 71: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 72: تحويل Flow إلى حالة قابلة للرسم داخل Compose.
 * السطر 74: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 75: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 76: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 77: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 78: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 80: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 81: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 82: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 84: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 85: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 86: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 87: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 89: أثر Compose مرتبط بدورة حياة التركيب أو تغيّر المفاتيح.
 * السطر 90: فرع شرطي يحدد مسار التنفيذ حسب الحالة الحالية.
 * السطر 91: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 92: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 93: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 94: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 96: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 97: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 98: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 100: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 101: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 102: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 103: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 104: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 105: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 106: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 107: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 108: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 109: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 110: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 111: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 113: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 114: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 115: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 117: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 118: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 120: أثر Compose مرتبط بدورة حياة التركيب أو تغيّر المفاتيح.
 * السطر 122: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 123: فرع شرطي يحدد مسار التنفيذ حسب الحالة الحالية.
 * السطر 124: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 125: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 126: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 127: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 128: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 129: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 130: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 131: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 132: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 133: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 134: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 136: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 137: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 138: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 139: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 140: تعبير lambda يحدد الإجراء الناتج عن تفاعل أو تحويل.
 * السطر 141: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 142: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 143: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 144: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 145: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 146: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 147: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 148: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 149: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 150: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 152: أثر Compose مرتبط بدورة حياة التركيب أو تغيّر المفاتيح.
 * السطر 153: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 154: تعبير lambda يحدد الإجراء الناتج عن تفاعل أو تحويل.
 * السطر 155: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 156: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 157: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 159: أثر Compose مرتبط بدورة حياة التركيب أو تغيّر المفاتيح.
 * السطر 160: تعبير lambda يحدد الإجراء الناتج عن تفاعل أو تحويل.
 * السطر 161: اختيار مسار تنفيذ بناءً على حالة/قيمة محددة.
 * السطر 162: تعبير lambda يحدد الإجراء الناتج عن تفاعل أو تحويل.
 * السطر 163: فرع شرطي يحدد مسار التنفيذ حسب الحالة الحالية.
 * السطر 164: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 165: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 166: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 167: تعبير lambda يحدد الإجراء الناتج عن تفاعل أو تحويل.
 * السطر 168: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 169: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 170: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 171: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 172: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 174: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 175: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 176: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 178: أثر Compose مرتبط بدورة حياة التركيب أو تغيّر المفاتيح.
 * السطر 179: فرع شرطي يحدد مسار التنفيذ حسب الحالة الحالية.
 * السطر 180: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 181: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 182: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 183: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 184: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 186: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 187: استدعاء Composable يرسم جزءاً من واجهة الهاتف أو ينظم تخطيطها.
 * السطر 188: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 189: تعديل بصري/تخطيطي لسلوك العنصر في Compose.
 * السطر 190: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 191: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 192: استدعاء Composable يرسم جزءاً من واجهة الهاتف أو ينظم تخطيطها.
 * السطر 193: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 194: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 195: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 196: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 197: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 198: استدعاء Composable يرسم جزءاً من واجهة الهاتف أو ينظم تخطيطها.
 * السطر 199: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 200: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 201: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 202: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 203: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 204: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
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
 * السطر 219: فرع شرطي يحدد مسار التنفيذ حسب الحالة الحالية.
 * السطر 220: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
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
 * السطر 234: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 235: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 236: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
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
 * السطر 248: تعبير lambda يحدد الإجراء الناتج عن تفاعل أو تحويل.
 * السطر 249: فرع شرطي يحدد مسار التنفيذ حسب الحالة الحالية.
 * السطر 250: فرع شرطي يحدد مسار التنفيذ حسب الحالة الحالية.
 * السطر 251: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 252: تعبير lambda يحدد الإجراء الناتج عن تفاعل أو تحويل.
 * السطر 253: فرع شرطي يحدد مسار التنفيذ حسب الحالة الحالية.
 * السطر 254: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 255: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 256: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 257: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 258: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 259: تعبير lambda يحدد الإجراء الناتج عن تفاعل أو تحويل.
 * السطر 260: تعبير lambda يحدد الإجراء الناتج عن تفاعل أو تحويل.
 * السطر 261: تعبير lambda يحدد الإجراء الناتج عن تفاعل أو تحويل.
 * السطر 262: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 263: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 264: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 265: تعديل بصري/تخطيطي لسلوك العنصر في Compose.
 * السطر 266: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 267: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 268: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
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
 * السطر 281: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 282: فرع شرطي يحدد مسار التنفيذ حسب الحالة الحالية.
 * السطر 283: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 284: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 285: تعريف قيمة/متغير؛ يحتفظ بحالة أو مرجع تستخدمه بقية الشيفرة.
 * السطر 286: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 287: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 288: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 289: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 290: فرع شرطي يحدد مسار التنفيذ حسب الحالة الحالية.
 * السطر 291: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 292: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 293: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 294: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 295: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 297: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 298: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 299: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 300: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 301: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 302: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 303: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 304: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 305: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 306: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 307: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 308: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 310: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 311: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 312: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 313: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 314: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 315: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 316: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 317: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 318: تعبير lambda يحدد الإجراء الناتج عن تفاعل أو تحويل.
 * السطر 319: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 320: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 321: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 322: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 323: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 324: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 325: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 326: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * السطر 327: تعليمة تنفيذية أصلية؛ محفوظة حرفياً ويُفهم أثرها ضمن السياق المحيط لها.
 * --- نهاية الفهرس السطري ---
 */

package com.example.ui.screens.habayeb.components

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.map
import com.example.R
import com.example.data.local.entities.HabayebCustomer
import com.example.ui.screens.habayeb.utils.CustomerHistoryCalculator
import com.example.ui.screens.habayeb.utils.HabayebRecurringManager
import com.example.ui.screens.habayeb.utils.rememberFilteredCustomerTransactions
import com.example.ui.viewmodel.HabayebFinanceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerHistoryOverlay(
    customer: HabayebCustomer,
    viewModel: HabayebFinanceViewModel,
    onDismiss: () -> Unit,
    activeThemeColor: Color,
    activeSubColor: Color,
    currencySymbol: String,
    contentPadding: PaddingValues = PaddingValues(),
    isSearchActive: Boolean = false,
    onSearchActiveChanged: (Boolean) -> Unit = {},
    onTxMultiSelectActiveChanged: (Boolean) -> Unit = {}
) {
    val bgColor = MaterialTheme.colorScheme.background
    val isDark = remember(bgColor) { bgColor.luminance() < 0.5f }
    val customers by viewModel.habayebCustomersState.collectAsStateWithLifecycle()
    val activeCustomer = customers.find { it.id == customer.id } ?: customer

    val initialTxs = remember(activeCustomer.id) {
        viewModel.getInitialTransactionsForCustomer(activeCustomer.id)
    }

    val transactions by remember(activeCustomer.id) {
        viewModel.getTransactionsForCustomerFlow(activeCustomer.id)
    }.collectAsStateWithLifecycle(initialValue = initialTxs)

    val settings by viewModel.settingsState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    var isPdfExporting by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    var txSearchQuery by remember { mutableStateOf("") }
    var showShareSheet by remember { mutableStateOf(false) }
    var dialogState by remember { mutableStateOf(CustomerHistoryDialogState()) }
    var selectedCurrencyFilter by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(showShareSheet, dialogState.showFilterMenu, isPdfExporting) {
        if (showShareSheet || dialogState.showFilterMenu || isPdfExporting) {
            focusManager.clearFocus()
            keyboardController?.hide()
        }
    }

    val allCustomerTxs = remember(transactions) {
        transactions.sortedBy { it.timestamp }
    }

    val displayedTxs by rememberFilteredCustomerTransactions(
        context = context,
        allCustomerTxs = allCustomerTxs,
        txSearchQuery = txSearchQuery,
        dateFilterMode = dialogState.dateFilterMode,
        customStartDate = dialogState.customStartDate,
        customEndDate = dialogState.customEndDate,
        typeFilterMode = dialogState.typeFilterMode,
        selectedCurrencyFilter = selectedCurrencyFilter,
        currencySymbol = currencySymbol,
        exchangeRatesJson = settings.exchangeRatesJson
    )

    val calcResult = remember(allCustomerTxs, currencySymbol, settings.exchangeRatesJson) {
        CustomerHistoryCalculator.calculate(allCustomerTxs, currencySymbol, settings.exchangeRatesJson)
    }

    var isTxMultiSelectActive by remember { mutableStateOf(false) }
    val selectedTxIds = remember { mutableStateListOf<String>() }

    LaunchedEffect(isTxMultiSelectActive) { onTxMultiSelectActiveChanged(isTxMultiSelectActive) }

    BackHandler {
        if (isTxMultiSelectActive) {
            isTxMultiSelectActive = false
            selectedTxIds.clear()
        } else if (selectedCurrencyFilter != null) {
            selectedCurrencyFilter = null
        } else if (isSearchActive || txSearchQuery.isNotEmpty()) {
            onSearchActiveChanged(false)
            txSearchQuery = ""
        } else {
            onDismiss()
        }
    }

    var refreshRecurringTrigger by remember { mutableStateOf(0) }
    val activeRecurringTxIds = remember(activeCustomer.id, refreshRecurringTrigger, allCustomerTxs) {
        val existingTxIds = allCustomerTxs.map { it.id }.toSet()
        HabayebRecurringManager.getAllConfigs(context)
            .filter { config ->
                config.isActive &&
                config.customerId == activeCustomer.id &&
                config.originalTxId.isNotBlank() &&
                !config.originalTxId.equals("null", ignoreCase = true) &&
                config.originalTxId != "0" &&
                existingTxIds.contains(config.originalTxId)
            }
            .map { it.originalTxId }
            .toSet()
    }

    LaunchedEffect(activeCustomer.id) {
        listState.scrollToItem(0)
        HabayebRecurringManager.checkAndExecuteRecurring(context, viewModel) { count ->
            Toast.makeText(context, context.getString(R.string.customer_history_toast_recurring_added, count, activeCustomer.name), Toast.LENGTH_LONG).show()
        }
    }

    LaunchedEffect(activeCustomer.id) {
        viewModel.uiEventFlow.collect { event ->
            when (event) {
                is com.example.ui.viewmodel.HabayebUiEvent.ScrollToAccount -> {
                    if (event.accountId == activeCustomer.id) {
                        listState.scrollToItem(0, 0)
                    }
                }
                is com.example.ui.viewmodel.HabayebUiEvent.ResetScrollToTop -> {
                    listState.scrollToItem(0, 0)
                }
            }
        }
    }

    val newestTxId = displayedTxs.firstOrNull()?.id
    var previousNewestTxId by remember(activeCustomer.id) { mutableStateOf(newestTxId) }
    var previousTxCount by remember(activeCustomer.id) { mutableStateOf(displayedTxs.size) }

    LaunchedEffect(newestTxId, displayedTxs.size) {
        if (displayedTxs.size > previousTxCount || (newestTxId != null && newestTxId != previousNewestTxId)) {
            listState.scrollToItem(0, 0)
        }
        previousTxCount = displayedTxs.size
        previousNewestTxId = newestTxId
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 1.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        CustomerHistoryTopBar(
                            customerName = activeCustomer.name,
                            customerPhone = activeCustomer.phone,
                            isSearchActive = isSearchActive,
                            txSearchQuery = txSearchQuery,
                            activeThemeColor = activeThemeColor,
                            isPdfExporting = isPdfExporting,
                            onSearchQueryChange = { txSearchQuery = it },
                            onSearchClose = {
                                onSearchActiveChanged(false)
                                txSearchQuery = ""
                            },
                            onSearchOpen = { onSearchActiveChanged(true) },
                            onDeleteClick = { dialogState = dialogState.copy(confirmDeleteCust = true) },
                            onEditClick = { dialogState = dialogState.copy(showEditNameDialog = true) },
                            onFilterClick = { dialogState = dialogState.copy(showFilterMenu = true) },
                            onShareClick = { showShareSheet = true },
                            onDismiss = onDismiss
                        )

                        if (!isSearchActive) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 1.dp)

                            CustomerSummaryCard(
                                currencySymbol = currencySymbol,
                                netDebtMap = calcResult.netDebtMap,
                                netDebtBDMap = calcResult.netDebtBigDecimalMap,
                                initialType = activeCustomer.initialType,
                                selectedCurrencyFilter = selectedCurrencyFilter,
                                onCurrencyFilterSelected = { selectedCurrencyFilter = it }
                            )
                        }
                    }
                }

                CustomerHistoryTableSection(
                    displayedTxs = displayedTxs,
                    listState = listState,
                    txSearchQuery = txSearchQuery,
                    activeCustomer = activeCustomer,
                    isDark = isDark,
                    currencySymbol = currencySymbol,
                    runningBalances = calcResult.runningBalances,
                    activeRecurringTxIds = activeRecurringTxIds,
                    txSequenceNumbers = calcResult.txSequenceNumbers,
                    selectedTxIds = selectedTxIds,
                    isTxMultiSelectActive = isTxMultiSelectActive,
                    activeThemeColor = activeThemeColor,
                    contentPadding = contentPadding,
                    onSelectToggle = { txId ->
                        if (selectedTxIds.contains(txId)) selectedTxIds.remove(txId) else selectedTxIds.add(txId)
                        if (selectedTxIds.isEmpty()) isTxMultiSelectActive = false
                    },
                    onLongClick = { txId ->
                        if (!isTxMultiSelectActive) {
                            isTxMultiSelectActive = true
                            selectedTxIds.add(txId)
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        }
                    },
                    onOptionsClick = { tx -> dialogState = dialogState.copy(transactionForOptionsDialog = tx) },
                    onScheduleClick = { tx -> dialogState = dialogState.copy(transactionForAutoRepeatDialog = tx) },
                    onExchangeRateClick = { tx ->
                        dialogState = dialogState.copy(exchangeTxToModify = tx, showRateModifyDialog = true)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )
            }

            MultiSelectFloatingBar(
                isVisible = isTxMultiSelectActive,
                selectedTxIds = selectedTxIds,
                totalTxCount = displayedTxs.size,
                activeThemeColor = activeThemeColor,
                contentPadding = contentPadding,
                onCancel = {
                    isTxMultiSelectActive = false
                    selectedTxIds.clear()
                },
                onToggleSelectAll = {
                    val allSelected = displayedTxs.isNotEmpty() && selectedTxIds.size >= displayedTxs.size
                    if (allSelected) {
                        selectedTxIds.clear()
                    } else {
                        val set = selectedTxIds.toSet()
                        displayedTxs.forEach { if (!set.contains(it.id)) selectedTxIds.add(it.id) }
                    }
                },
                onDelete = {
                    if (selectedTxIds.isNotEmpty()) dialogState = dialogState.copy(showDeleteBulkTxConfirmDialog = true)
                },
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }

    CustomerHistoryShareBottomSheet(
        showShareSheet = showShareSheet,
        activeCustomer = activeCustomer,
        allCustomerTxs = allCustomerTxs,
        currencySymbol = currencySymbol,
        exchangeRatesJson = settings.exchangeRatesJson,
        netDebt = calcResult.netDebt,
        activeThemeColor = activeThemeColor,
        onDismissRequest = { showShareSheet = false },
        onPdfExportStart = { isPdfExporting = true },
        onPdfExportFinish = { isPdfExporting = false }
    )

    CustomerHistoryDialogsManager(
        activeCustomer = activeCustomer,
        viewModel = viewModel,
        currencySymbol = currencySymbol,
        netDebt = calcResult.netDebt,
        activeThemeColor = activeThemeColor,
        activeSubColor = activeSubColor,
        dialogState = dialogState,
        onDialogStateChange = { transform -> dialogState = transform(dialogState) },
        onCustomerDeleted = onDismiss,
        selectedTxIds = selectedTxIds,
        onIsTxMultiSelectActiveChange = { isTxMultiSelectActive = it },
        activeRecurringTxIds = activeRecurringTxIds,
        txSequenceNumbers = calcResult.txSequenceNumbers,
        onRefreshRecurringTrigger = { refreshRecurringTrigger++ },
        allCustomerTxs = allCustomerTxs
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
