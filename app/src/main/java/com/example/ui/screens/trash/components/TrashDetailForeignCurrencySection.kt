/**
 * التوثيق المعماري العربي — مشروع «الدفتر الذكي / ميزان الدار».
 * الملف: app/src/main/java/com/example/ui/screens/trash/components/TrashDetailForeignCurrencySection.kt
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
// توثيق السطر 21: التعليمة الوصفية التالية تضبط طريقة معالجة التعريف الأصلي ولا تُعديلاً على منطقه.
// توثيق السطر 22: الدالة التالية هي نقطة تنفيذ أصلية؛ تستقبل مدخلاتها وتنفذ مسؤوليتها كما في المصدر دون تعديل.
// توثيق السطر 32: الشرط التالي يحافظ على قرار التنفيذ الأصلي كما هو.
// توثيق السطر 50: الشرط التالي يحافظ على قرار التنفيذ الأصلي كما هو.

package com.example.ui.screens.trash.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.screens.trash.utils.ParsedTrashData

@Composable
fun TrashDetailForeignCurrencySection(
    parsedData: ParsedTrashData,
    modifier: Modifier = Modifier
) {
    TrashDetailInfoCard(
        icon = Icons.Default.CurrencyExchange,
        title = stringResource(R.string.trash_tx_foreign_title),
        modifier = modifier
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            if (parsedData.isRateCalculated) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.trash_exchange_rate_label),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = parsedData.exchangeRateVal,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                if (parsedData.equivalentAmountText.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.trash_tx_amount_title),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = parsedData.equivalentAmountText,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            } else {
                Text(
                    text = stringResource(R.string.trash_exchange_inactive),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                    fontWeight = FontWeight.Medium
                )
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
