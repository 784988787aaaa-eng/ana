/**
 * التوثيق المعماري العربي — مشروع «الدفتر الذكي / ميزان الدار».
 * الملف: app/src/main/java/com/example/ui/theme/Type.kt
 * المسؤولية: تعريف أنماط Typography المستخدمة في نظام تصميم التطبيق.
 *
 * القراءة التعليمية: يوضح هذا الملف كيف تنتقل حالة التطبيق من الطبقة المشتركة
 * إلى المشهد المرئي على الهاتف، مع تفسير العقود والحالة والتوابع والتفاعلات.
 * الكتلة التنفيذية الأصلية أدناه محفوظة حرفياً؛ الإضافات التوثيقية لا تعدّل
 * أي رمز تنفيذي وفق قاعدة Zero Code Alteration.
 */

// --- فهرس التوثيق التعليمي للسطر التنفيذي ---
// توثيق السطر 1: التوجيه الحزمي يحدد الموضع المنطقي للملف داخل طبقة الواجهة.
// توثيق السطر 3: الاستيراد التالي اعتماد أصلي يحتاجه الملف؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 4: الاستيراد التالي اعتماد أصلي يحتاجه الملف؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 5: الاستيراد التالي اعتماد أصلي يحتاجه الملف؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 6: الاستيراد التالي اعتماد أصلي يحتاجه الملف؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 7: الاستيراد التالي اعتماد أصلي يحتاجه الملف؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 8: الاستيراد التالي اعتماد أصلي يحتاجه الملف؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 9: الاستيراد التالي اعتماد أصلي يحتاجه الملف؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 10: الاستيراد التالي اعتماد أصلي يحتاجه الملف؛ لم يتم تغيير أي اعتماد.

package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.sp
import com.example.R

val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

val CairoFont = GoogleFont("Cairo")

val CairoFontFamily = FontFamily(
    Font(googleFont = CairoFont, fontProvider = provider, weight = FontWeight.Normal),
    Font(googleFont = CairoFont, fontProvider = provider, weight = FontWeight.Medium),
    Font(googleFont = CairoFont, fontProvider = provider, weight = FontWeight.SemiBold),
    Font(googleFont = CairoFont, fontProvider = provider, weight = FontWeight.Bold)
)

val Typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = CairoFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp,
    ),
    titleLarge = TextStyle(
        fontFamily = CairoFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontFamily = CairoFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    ),
    labelLarge = TextStyle(
        fontFamily = CairoFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = CairoFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    )
)

// --- ملاحظات وتوصيات المعمارية البرمجية ---
// 1) تم الحفاظ على الكتلة التنفيذية الأصلية دون تعديل أو حذف أو إعادة ترتيب.
// 2) يوصى مستقبلاً بإبقاء مكونات Compose المشتركة صغيرة ومحددة المسؤولية لتقليل إعادة التركيب غير الضرورية.
// 3) ينبغي إبقاء حالات الحوار والتنقل قابلة للتتبع من مصدر واحد عند اتساع عدد المسارات، مع الحفاظ على السلوك الحالي.
// 4) يوصى بعزل أدوات Intent وحفظ الملفات والاهتزاز عن واجهات Compose قدر الإمكان عبر عقود واضحة، دون تعديل النسخة الحالية.
// 5) في نظام Theme والألوان والطباعة، يوصى بالحفاظ على مصدر تصميم مركزي ومنع القيم المتناثرة مستقبلاً.
// 6) أي تحسين لاحق يجب أن يسبقه اختبار تكافؤ سلوكي/مرئي مناسب قبل الدمج.
