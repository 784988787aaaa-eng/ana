/**
 * التوثيق المعماري العربي — مشروع «الدفتر الذكي / ميزان الدار».
 * الملف: app/src/main/java/com/example/ui/screens/trash/components/TrashBundlePeekList.kt
 * المسؤولية: مكوّن عرض لقائمة أو بطاقة عنصر محذوف مع إبراز المعلومات الأساسية وإجراءات المستخدم.
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
// توثيق السطر 22: الاستيراد التالي يجلب اعتماداً تحتاجه الشيفرة الأصلية؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 23: الاستيراد التالي يجلب اعتماداً تحتاجه الشيفرة الأصلية؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 24: الاستيراد التالي يجلب اعتماداً تحتاجه الشيفرة الأصلية؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 25: الاستيراد التالي يجلب اعتماداً تحتاجه الشيفرة الأصلية؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 26: الاستيراد التالي يجلب اعتماداً تحتاجه الشيفرة الأصلية؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 27: الاستيراد التالي يجلب اعتماداً تحتاجه الشيفرة الأصلية؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 28: الاستيراد التالي يجلب اعتماداً تحتاجه الشيفرة الأصلية؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 30: التعليمة الوصفية التالية تضبط طريقة معالجة التعريف الأصلي ولا تُعديلاً على منطقه.
// توثيق السطر 31: الدالة التالية هي نقطة تنفيذ أصلية؛ تستقبل مدخلاتها وتنفذ مسؤوليتها كما في المصدر دون تعديل.
// توثيق السطر 41: الشرط التالي يحافظ على قرار التنفيذ الأصلي كما هو.
// توثيق السطر 61: المتغير التالي يحتفظ بالحالة/القيمة التي تستخدمها الشيفرة الأصلية في هذا الموضع؛ الاسم والقيمة محفوظان حرفياً.
// توثيق السطر 62: المتغير التالي يحتفظ بالحالة/القيمة التي تستخدمها الشيفرة الأصلية في هذا الموضع؛ الاسم والقيمة محفوظان حرفياً.
// توثيق السطر 64: الحلقة التالية تمر على العناصر وفق منطق المصدر الأصلي.
// توثيق السطر 65: المتغير التالي يحتفظ بالحالة/القيمة التي تستخدمها الشيفرة الأصلية في هذا الموضع؛ الاسم والقيمة محفوظان حرفياً.
// توثيق السطر 66: المتغير التالي يحتفظ بالحالة/القيمة التي تستخدمها الشيفرة الأصلية في هذا الموضع؛ الاسم والقيمة محفوظان حرفياً.
// توثيق السطر 100: الشرط التالي يحافظ على قرار التنفيذ الأصلي كما هو.
// توثيق السطر 125: الشرط التالي يحافظ على قرار التنفيذ الأصلي كما هو.
// توثيق السطر 154: الشرط التالي يحافظ على قرار التنفيذ الأصلي كما هو.

package com.example.ui.screens.trash.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.screens.trash.utils.ParsedBundleTransaction

@Composable
fun TrashBundlePeekList(
    isExpanded: Boolean,
    isSelected: Boolean,
    bundleTransactions: List<ParsedBundleTransaction>,
    primaryColor: Color,
    secondaryColor: Color,
    errorColor: Color,
    onRestoreSingleTx: (String) -> Unit,
    onShowAllClick: () -> Unit
) {
    if (bundleTransactions.isNotEmpty()) {
        AnimatedVisibility(
            visible = isExpanded && !isSelected,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.trash_bundle_transactions_label),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                )

                val peekLimit = 3
                val totalTxs = bundleTransactions.size

                for (i in 0 until minOf(peekLimit, totalTxs)) {
                    val tx = bundleTransactions[i]
                    val txColor = if (tx.isNegative) errorColor else secondaryColor

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 1.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                            .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(2.dp)
                                    .height(16.dp)
                                    .clip(RoundedCornerShape(1.dp))
                                    .background(txColor)
                            )
                            Column {
                                Text(
                                    text = tx.description,
                                    fontSize = 10.sp,
                                    fontWeight = if (tx.hasNotes) FontWeight.Medium else FontWeight.Light,
                                    color = if (tx.hasNotes) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (tx.dateText.isNotEmpty()) {
                                    Text(
                                        text = tx.dateText,
                                        fontSize = 8.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                    )
                                }
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Column(
                                horizontalAlignment = Alignment.End,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = tx.displayAmountText,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = txColor
                                )

                                if (tx.exchangeRateText.isNotEmpty()) {
                                    Text(
                                        text = tx.exchangeRateText,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Normal,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(primaryColor.copy(alpha = 0.1f))
                                    .clickable { onRestoreSingleTx(tx.id) },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Restore,
                                    contentDescription = stringResource(id = R.string.trash_restore_single_tx_desc),
                                    tint = primaryColor,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                    }
                }

                if (totalTxs > peekLimit) {
                    Spacer(modifier = Modifier.height(2.dp))
                    TextButton(
                        onClick = onShowAllClick,
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .height(24.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(
                            text = stringResource(id = R.string.trash_show_remaining_transactions, totalTxs - peekLimit),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = primaryColor
                        )
                    }
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
