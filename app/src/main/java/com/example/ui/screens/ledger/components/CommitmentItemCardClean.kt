package com.example.ui.screens.ledger.components

/*
 * =====================================================================================
 * بطاقة عرض عنصر الالتزام المالي (Commitment Item Card Clean Component)
 * -------------------------------------------------------------------------------------
 * [الوصف والهدف]:
 * بطاقة تفاعلية تعرض بيانات الالتزام المالي ونسبة الإنجاز بتصميم متقن وخفيف:
 * 1. تعرض اسم الالتزام المالي مع مؤشر دائري يتيح للمستخدم تحديد الالتزام كمكتمل يدوياً.
 * 2. شريط تقدم بتدرج لوني انسيابي (Gradient Progress Bar) يوضح نسبة تغطية المبلغ المستهدف.
 * 3. شارة ديناميكية للمبلغ المتبقي أو حالة الاكتمال بالنسبة المئوية.
 * 4. أزرار التحكم السريع: التعديل، الحذف، ومقبض تفاعلي لإعادة ترتيب الأولويات بالسحب أو النقر.
 * =====================================================================================
 */

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.local.entities.FixedCommitment
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.financialCreditColor
import com.example.ui.theme.financialDebtColor
import java.math.BigDecimal

/*
 * =====================================================================================
 * دالة مساعدة لتحويل الأرقام المشرقية إلى غربية (toWesternDigits)
 * =====================================================================================
 */
private fun String.toWesternDigits(): String {
    var result = this
    val eastern = charArrayOf('٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩')
    val western = charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9')
    for (i in 0..9) {
        result = result.replace(eastern[i], western[i])
    }
    return result
}

/*
 * =====================================================================================
 * دالة العرض لبطاقة الالتزام المالي (CommitmentItemCardClean Composable)
 * -------------------------------------------------------------------------------------
 * [المُدخلات]:
 * - index: الترتيب الحالي للعنصر في القائمة.
 * - fc: كائن الالتزام المالي (FixedCommitment).
 * - allocated: المبلغ المخصص والمغطى حالياً لهذا الالتزام.
 * - remaining: المبلغ المتبقي لاكتمال تغطية الالتزام.
 * - totalCash: إجمالي النقد المتاح للتغطية.
 * - currencySymbol: رمز العملة المعتمد.
 * - formatCurrency: دالة تنسيق المبالغ المالية.
 * - totalCommitmentsCount: إجمالي عدد الالتزامات في القائمة.
 * - onCheckedChange: رد النداء عند تغيير حالة التغطية اليدوية.
 * - onSetReorderTarget: رد النداء عند النقر على مقبض إعادة الترتيب.
 * - onReorderCommitment: رد النداء لنقل الالتزام لموقع ترتيبي جديد.
 * - onEditCommitmentClick: رد النداء لفتح حوار التعديل.
 * - onDeleteClick: رد النداء لطلب حذف الالتزام.
 * =====================================================================================
 */
@Composable
fun CommitmentItemCardClean(
    index: Int,
    fc: FixedCommitment,
    allocated: BigDecimal,
    remaining: BigDecimal,
    totalCash: BigDecimal,
    currencySymbol: String,
    formatCurrency: (BigDecimal, String) -> String,
    totalCommitmentsCount: Int,
    onCheckedChange: (FixedCommitment, Boolean) -> Unit,
    onSetReorderTarget: (FixedCommitment) -> Unit,
    onReorderCommitment: (FixedCommitment, Int) -> Unit,
    onEditCommitmentClick: (FixedCommitment) -> Unit,
    onDeleteClick: (FixedCommitment) -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val isCovered = remaining.compareTo(BigDecimal.ZERO) <= 0
    val progressFraction = if (fc.targetAmount.compareTo(BigDecimal.ZERO) > 0) {
        allocated.divide(fc.targetAmount, 6, java.math.RoundingMode.HALF_UP)
            .coerceIn(BigDecimal.ZERO, BigDecimal.ONE)
            .toFloat()
    } else 0f
    val progressPercent = (progressFraction * 100).toInt()
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val creditColor = financialCreditColor(isDark)
    val debtColor = financialDebtColor(isDark)
    val secondaryColor = MaterialTheme.colorScheme.secondary

    val itemGradient = remember(isDark, creditColor, secondaryColor) {
        Brush.horizontalGradient(
            colors = listOf(
                creditColor,
                secondaryColor
            )
        )
    }

    Card(
        shape = RoundedCornerShape(13.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCovered) creditColor.copy(alpha = if (isDark) 0.10f else 0.04f) else MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            1.dp,
            if (isCovered) creditColor.copy(alpha = 0.28f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 11.dp, vertical = 9.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // الطبقة 1: اسم الالتزام وحالة الإنجاز والنسبة
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // زر التأشير الدائري + اسم الالتزام
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(if (isCovered) creditColor else Color.Transparent)
                            .border(
                                1.5.dp,
                                if (isCovered) creditColor else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f),
                                CircleShape
                            )
                            .clickable {
                                onCheckedChange(fc, !isCovered)
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isCovered) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onTertiary,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }

                    Text(
                        text = fc.name.toWesternDigits(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.5.sp,
                        color = if (isCovered) creditColor else MaterialTheme.colorScheme.onSurface,
                        maxLines = 1
                    )
                }

                // شارة المتبقي أو المكتمل + النسبة المئوية
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (isCovered) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = creditColor.copy(alpha = 0.14f)
                        ) {
                            Text(
                                text = "مكتمل",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = creditColor,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    } else {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = debtColor.copy(alpha = 0.10f)
                        ) {
                            Text(
                                text = "متبقي: ${formatCurrency(remaining, currencySymbol)}".toWesternDigits(),
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = debtColor,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Text(
                        text = "$progressPercent%",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isCovered) creditColor else MaterialTheme.colorScheme.primary
                    )
                }
            }

            // الطبقة 2: شريط التقدم بتدرج لوني انسيابي (Progress Bar 5dp)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(2.5.dp))
                        .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.22f))
                )
                if (progressFraction > 0f) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progressFraction)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(2.5.dp))
                            .background(if (isCovered) Brush.horizontalGradient(listOf(creditColor, creditColor)) else itemGradient)
                    )
                }
            }

            // الطبقة 3: المبلغ المستهدف وأزرار الإجراءات السريعة
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // المبلغ المستهدف
                Text(
                    text = "المستهدف: ${formatCurrency(fc.targetAmount, currencySymbol)}".toWesternDigits(),
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )

                // أزرار التحكم المصغرة (تعديل، حذف، مقبض ترتيب)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    // زر التعديل ✏️
                    IconButton(
                        onClick = { onEditCommitmentClick(fc) },
                        modifier = Modifier.size(26.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = stringResource(id = R.string.ledger_edit_commitment_title),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f),
                            modifier = Modifier.size(13.dp)
                        )
                    }

                    // زر الحذف 🗑️
                    IconButton(
                        onClick = {
                            onDeleteClick(fc)
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        },
                        modifier = Modifier.size(26.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = stringResource(id = R.string.ledger_commitment_delete),
                            tint = debtColor.copy(alpha = 0.65f),
                            modifier = Modifier.size(13.dp)
                        )
                    }

                    // مقبض إعادة الترتيب بالسحب ☰
                    var dragOffset by remember { mutableFloatStateOf(0f) }
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .clickable {
                                onSetReorderTarget(fc)
                            }
                            .pointerInput(Unit) {
                                detectDragGestures(
                                    onDragStart = { _ -> dragOffset = 0f },
                                    onDrag = { _, dragAmount ->
                                        dragOffset += dragAmount.y
                                        if (dragOffset > 60f) {
                                            dragOffset = 0f
                                            val pos = index + 2
                                            if (pos <= totalCommitmentsCount) {
                                                onReorderCommitment(fc, pos)
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            }
                                        } else if (dragOffset < -60f) {
                                            dragOffset = 0f
                                            val pos = index
                                            if (pos >= 1) {
                                                onReorderCommitment(fc, pos)
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            }
                                        }
                                    },
                                    onDragEnd = { dragOffset = 0f }
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = stringResource(id = R.string.ledger_reorder_apply),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f),
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }
            }
        }
    }
}

