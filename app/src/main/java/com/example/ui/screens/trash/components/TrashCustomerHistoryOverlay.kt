/**
 * التوثيق المعماري العربي — مشروع «الدفتر الذكي / ميزان الدار».
 * الملف: app/src/main/java/com/example/ui/screens/trash/components/TrashCustomerHistoryOverlay.kt
 * المسؤولية: طبقة عرض فوق الشاشة لاستعراض معلومات مرتبطة بالعنصر المحذوف دون مغادرة السياق الحالي.
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
// توثيق السطر 29: الاستيراد التالي يجلب اعتماداً تحتاجه الشيفرة الأصلية؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 30: الاستيراد التالي يجلب اعتماداً تحتاجه الشيفرة الأصلية؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 31: الاستيراد التالي يجلب اعتماداً تحتاجه الشيفرة الأصلية؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 32: الاستيراد التالي يجلب اعتماداً تحتاجه الشيفرة الأصلية؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 34: التعليمة الوصفية التالية تضبط طريقة معالجة التعريف الأصلي ولا تُعديلاً على منطقه.
// توثيق السطر 35: الدالة التالية هي نقطة تنفيذ أصلية؛ تستقبل مدخلاتها وتنفذ مسؤوليتها كما في المصدر دون تعديل.
// توثيق السطر 46: المتغير التالي يحتفظ بالحالة/القيمة التي تستخدمها الشيفرة الأصلية في هذا الموضع؛ الاسم والقيمة محفوظان حرفياً.
// توثيق السطر 48: المتغير التالي يحتفظ بالحالة/القيمة التي تستخدمها الشيفرة الأصلية في هذا الموضع؛ الاسم والقيمة محفوظان حرفياً.
// توثيق السطر 49: المتغير التالي يحتفظ بالحالة/القيمة التي تستخدمها الشيفرة الأصلية في هذا الموضع؛ الاسم والقيمة محفوظان حرفياً.
// توثيق السطر 50: المتغير التالي يحتفظ بالحالة/القيمة التي تستخدمها الشيفرة الأصلية في هذا الموضع؛ الاسم والقيمة محفوظان حرفياً.
// توثيق السطر 51: المتغير التالي يحتفظ بالحالة/القيمة التي تستخدمها الشيفرة الأصلية في هذا الموضع؛ الاسم والقيمة محفوظان حرفياً.
// توثيق السطر 52: المتغير التالي يحتفظ بالحالة/القيمة التي تستخدمها الشيفرة الأصلية في هذا الموضع؛ الاسم والقيمة محفوظان حرفياً.
// توثيق السطر 53: المتغير التالي يحتفظ بالحالة/القيمة التي تستخدمها الشيفرة الأصلية في هذا الموضع؛ الاسم والقيمة محفوظان حرفياً.
// توثيق السطر 54: المتغير التالي يحتفظ بالحالة/القيمة التي تستخدمها الشيفرة الأصلية في هذا الموضع؛ الاسم والقيمة محفوظان حرفياً.
// توثيق السطر 168: الشرط التالي يحافظ على قرار التنفيذ الأصلي كما هو.
// توثيق السطر 177: المتغير التالي يحتفظ بالحالة/القيمة التي تستخدمها الشيفرة الأصلية في هذا الموضع؛ الاسم والقيمة محفوظان حرفياً.
// توثيق السطر 178: المتغير التالي يحتفظ بالحالة/القيمة التي تستخدمها الشيفرة الأصلية في هذا الموضع؛ الاسم والقيمة محفوظان حرفياً.
// توثيق السطر 179: المتغير التالي يحتفظ بالحالة/القيمة التي تستخدمها الشيفرة الأصلية في هذا الموضع؛ الاسم والقيمة محفوظان حرفياً.
// توثيق السطر 180: المتغير التالي يحتفظ بالحالة/القيمة التي تستخدمها الشيفرة الأصلية في هذا الموضع؛ الاسم والقيمة محفوظان حرفياً.
// توثيق السطر 253: الشرط التالي يحافظ على قرار التنفيذ الأصلي كما هو.
// توثيق السطر 279: المتغير التالي يحتفظ بالحالة/القيمة التي تستخدمها الشيفرة الأصلية في هذا الموضع؛ الاسم والقيمة محفوظان حرفياً.
// توثيق السطر 280: المتغير التالي يحتفظ بالحالة/القيمة التي تستخدمها الشيفرة الأصلية في هذا الموضع؛ الاسم والقيمة محفوظان حرفياً.
// توثيق السطر 281: المتغير التالي يحتفظ بالحالة/القيمة التي تستخدمها الشيفرة الأصلية في هذا الموضع؛ الاسم والقيمة محفوظان حرفياً.
// توثيق السطر 301: الشرط التالي يحافظ على قرار التنفيذ الأصلي كما هو.
// توثيق السطر 332: الشرط التالي يحافظ على قرار التنفيذ الأصلي كما هو.

package com.example.ui.screens.trash.components

import java.math.BigDecimal

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.example.data.local.entities.DeletedItemEntity
import com.example.ui.helper.HabayebMathHelper
import com.example.ui.helper.getInitialColor
import com.example.ui.screens.trash.utils.ParsedTrashData
import com.example.ui.theme.financialCreditColor
import com.example.ui.theme.financialDebtColor

@Composable
fun TrashCustomerHistoryOverlay(
    item: DeletedItemEntity,
    parsedData: ParsedTrashData,
    currencySymbol: String,
    onDismiss: () -> Unit,
    onRestoreFullAccount: () -> Unit,
    onDeleteFullAccountPermanently: () -> Unit,
    onRestoreSingleTx: (String) -> Unit
) {
    BackHandler { onDismiss() }

    var showConfirmDeleteAccount by remember { mutableStateOf(false) }

    val primaryColor = MaterialTheme.colorScheme.primary
    val errorColor = MaterialTheme.colorScheme.error
    val isDark = MaterialTheme.colorScheme.background.run { red < 0.5f }
    val creditColor = financialCreditColor(isDark)
    val debtColor = financialDebtColor(isDark)
    val avatarColor = remember(parsedData.titleText) { getInitialColor(parsedData.titleText) }
    val firstLetter = remember(parsedData.titleText) {
        parsedData.titleText.trim().firstOrNull()?.toString()?.uppercase() ?: "؟"
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // Header Bar
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 3.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.Default.ArrowForward,
                                contentDescription = stringResource(id = R.string.trash_back),
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        // Avatar
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(avatarColor.copy(alpha = 0.15f))
                                .border(1.dp, avatarColor, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = firstLetter,
                                color = avatarColor,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Column {
                            Text(
                                text = parsedData.titleText,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // Top Action Buttons
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = onRestoreFullAccount,
                            colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.RestoreFromTrash,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = stringResource(id = R.string.trash_action_restore_btn),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }

                        IconButton(
                            onClick = { showConfirmDeleteAccount = true },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(errorColor.copy(alpha = 0.1f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteForever,
                                contentDescription = null,
                                tint = errorColor,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            // Multi-Currency Breakdown Summary Bar
            if (parsedData.currencyBreakdown.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    parsedData.currencyBreakdown.forEach { (curr, sum) ->
                        val isNeg = sum < BigDecimal.ZERO
                        val chipBg = if (isNeg) debtColor.copy(alpha = 0.1f) else creditColor.copy(alpha = 0.1f)
                        val chipText = if (isNeg) debtColor else creditColor
                        val formattedSum = com.example.ui.helper.HabayebMathHelper.formatSmart(sum.abs())

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = chipBg,
                            border = androidx.compose.foundation.BorderStroke(0.5.dp, chipText.copy(alpha = 0.3f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = if (sum.compareTo(BigDecimal.ZERO) == 0) stringResource(id = R.string.trash_status_balanced) else if (isNeg) stringResource(id = R.string.trash_status_due_prefix) else stringResource(id = R.string.trash_status_owed_prefix),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = chipText
                                )
                                Text(
                                    text = "$formattedSum $curr",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = chipText
                                )
                            }
                        }
                    }
                }
            }

            // Transactions List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, top = 12.dp, end = 16.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(parsedData.bundleTransactions, key = { it.id }) { tx ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = tx.description,
                                    fontSize = 13.sp,
                                    fontWeight = if (!tx.hasNotes) FontWeight.Normal else FontWeight.Bold,
                                    color = if (!tx.hasNotes) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = tx.dateText,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                    )

                                    if (tx.exchangeRateText.isNotEmpty()) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(creditColor.copy(alpha = 0.12f))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = stringResource(id = R.string.trash_rate_active_format, tx.exchangeRateText),
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = creditColor
                                            )
                                        }
                                    }
                                }
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.End,
                                    verticalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    val isNegative = tx.isNegative
                                    val txColor = if (isNegative) debtColor else creditColor
                                    val arrowSymbol = if (isNegative) "↗" else "↙"

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                                    ) {
                                        Text(
                                            text = tx.displayAmountText,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = txColor
                                        )
                                        Text(
                                            text = arrowSymbol,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = txColor
                                        )
                                    }

                                    if (tx.equivalentAmountText.isNotEmpty()) {
                                        Text(
                                            text = tx.equivalentAmountText,
                                            fontSize = 11.sp,
                                            color = txColor.copy(alpha = 0.8f)
                                        )
                                    }
                                }

                                IconButton(
                                    onClick = { onRestoreSingleTx(tx.id) },
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(primaryColor.copy(alpha = 0.12f))
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Restore,
                                        contentDescription = stringResource(id = R.string.trash_action_restore_btn),
                                        tint = primaryColor,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showConfirmDeleteAccount) {
        AlertDialog(
            onDismissRequest = { showConfirmDeleteAccount = false },
            title = {
                Text(
                    text = stringResource(id = R.string.trash_delete_warning_title),
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(text = stringResource(id = R.string.trash_delete_warning_desc))
            },
            confirmButton = {
                Button(
                    onClick = {
                        showConfirmDeleteAccount = false
                        onDeleteFullAccountPermanently()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = errorColor)
                ) {
                    Text(text = stringResource(id = R.string.trash_delete_permanently))
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDeleteAccount = false }) {
                    Text(text = stringResource(id = R.string.trash_cancel))
                }
            }
        )
    }
}

// --- ملاحظات وتوصيات المعمارية البرمجية ---
// 1) تم الحفاظ على التنفيذ الأصلي دون تعديل، والحفاظ على أسماء الأنواع والدوال والمتغيرات والعقود.
// 2) ينبغي مستقبلاً تقييم فصل parsing عن presentation عندما تتوسع أنواع عناصر سلة المحذوفات، مع عدم تغيير النسخة الحالية.
// 3) يوصى بمراقبة كلفة إعادة التحليل والفرز على القوائم الكبيرة، خصوصاً عند استخدام Compose وDispatchers.Default.
// 4) أي تحسين لاحق يجب أن يمر باختبارات تكافؤ سلوكي قبل اعتماده، لأن سلة المحذوفات جزء حساس من استرجاع البيانات.
// 5) هذه الملاحظات توصيات مستقبلية فقط ولا تمثل تعديلاً على الشيفرة الحالية.
