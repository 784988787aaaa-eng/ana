/**
 * التوثيق المعماري العربي — مشروع «الدفتر الذكي / ميزان الدار».
 * الملف: app/src/main/java/com/example/ui/screens/trash/components/TrashDetailAmountCard.kt
 * المسؤولية: مكوّن عرض تفصيلي لعنصر محذوف، يركز على جزء محدد من البيانات المحاسبية أو الزمنية أو هوية العميل.
 *
 * القراءة التعليمية: يبدأ هذا الملف بالعقد الحزمي والاستيرادات، ثم الأنواع/الحالة،
 * ثم نقاط Compose والتوابع ومسارات العرض والتفاعل. التعليقات المضافة تشرح وظيفة
 * العناصر الأصلية، بينما الكتلة التنفيذية نفسها محفوظة حرفياً دون إعادة صياغة.
 * الرؤية البصرية: كل قيمة حالة تقود في النهاية إلى بطاقة أو صف أو حوار أو طبقة
 * على شاشة الهاتف؛ لذا يُقرأ الملف كمسار من «بيانات المحذوف» إلى «المشهد المرئي».
 * قاعدة السلامة: Zero Code Alteration — لا تعديل على أي رمز تنفيذي أصلي.
 */

// --- فهرس التوثيق التعليمي للسطر التنفيذي ---
// توثيق السطر 1: التوجيه الحزمي: يحدد المسار المنطقي لهذا الملف داخل بنية التطبيق.
// توثيق السطر 3: الاستيراد التالي يجلب اعتماداً تحتاجه الشيفرة الأصلية؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 4: الاستيراد التالي يجلب اعتماداً تحتاجه الشيفرة الأصلية؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 5: الاستيراد التالي يجلب اعتماداً تحتاجه الشيفرة الأصلية؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 6: الاستيراد التالي يجلب اعتماداً تحتاجه الشيفرة الأصلية؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 7: الاستيراد التالي يجلب اعتماداً تحتاجه الشيفرة الأصلية؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 8: الاستيراد التالي يجلب اعتماداً تحتاجه الشيفرة الأصلية؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 9: الاستيراد التالي يجلب اعتماداً تحتاجه الشيفرة الأصلية؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 10: الاستيراد التالي يجلب اعتماداً تحتاجه الشيفرة الأصلية؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 11: الاستيراد التالي يجلب اعتماداً تحتاجه الشيفرة الأصلية؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 12: الاستيراد التالي يجلب اعتماداً تحتاجه الشيفرة الأصلية؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 13: الاستيراد التالي يجلب اعتماداً تحتاجه الشيفرة الأصلية؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 14: الاستيراد التالي يجلب اعتماداً تحتاجه الشيفرة الأصلية؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 15: الاستيراد التالي يجلب اعتماداً تحتاجه الشيفرة الأصلية؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 16: الاستيراد التالي يجلب اعتماداً تحتاجه الشيفرة الأصلية؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 17: الاستيراد التالي يجلب اعتماداً تحتاجه الشيفرة الأصلية؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 18: الاستيراد التالي يجلب اعتماداً تحتاجه الشيفرة الأصلية؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 19: الاستيراد التالي يجلب اعتماداً تحتاجه الشيفرة الأصلية؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 20: الاستيراد التالي يجلب اعتماداً تحتاجه الشيفرة الأصلية؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 21: الاستيراد التالي يجلب اعتماداً تحتاجه الشيفرة الأصلية؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 23: التعليمة الوصفية التالية تضبط طريقة معالجة التعريف الأصلي ولا تُعديلاً على منطقه.
// توثيق السطر 24: الدالة التالية هي نقطة تنفيذ أصلية؛ تستقبل مدخلاتها وتنفذ مسؤوليتها كما في المصدر دون تعديل.
// توثيق السطر 53: الشرط التالي يحافظ على قرار التنفيذ الأصلي كما هو.

package com.example.ui.screens.trash.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.screens.trash.utils.ParsedTrashData

@Composable
fun TrashDetailAmountCard(
    parsedData: ParsedTrashData,
    amountColor: Color,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = amountColor.copy(alpha = if (isDark) 0.12f else 0.08f)
        ),
        border = BorderStroke(1.dp, amountColor.copy(alpha = 0.25f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = parsedData.amountText,
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                color = amountColor,
                textAlign = TextAlign.Center
            )

            if (parsedData.txTypeDisplay.isNotEmpty()) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = amountColor.copy(alpha = 0.15f),
                    border = BorderStroke(0.5.dp, amountColor.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = parsedData.txTypeDisplay,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = amountColor,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

// --- ملاحظات وتوصيات المعمارية البرمجية ---
// 1) تم الحفاظ على التنفيذ الأصلي دون تعديل، والحفاظ على أسماء الأنواع والدوال والمتغيرات والعقود.
// 2) ينبغي مستقبلاً تقييم فصل parsing عن presentation عندما تتوسع أنواع عناصر سلة المحذوفات، مع عدم تغيير النسخة الحالية.
// 3) يوصى بمراقبة كلفة إعادة التحليل والفرز على القوائم الكبيرة، خصوصاً عند استخدام Compose وDispatchers.Default.
// 4) أي تحسين لاحق يجب أن يمر باختبارات تكافؤ سلوكي قبل اعتماده، لأن سلة المحذوفات جزء حساس من استرجاع البيانات.
// 5) هذه الملاحظات توصيات مستقبلية فقط ولا تمثل تعديلاً على الشيفرة الحالية.
