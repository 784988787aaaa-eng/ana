package com.example.ui.screens.ledger.components

/*
 * =====================================================================================
 * بطاقة ملخص الالتزامات بالتدرج اللوني (Commitment Summary Gradient Card Component)
 * -------------------------------------------------------------------------------------
 * [الوصف والهدف]:
 * بطاقة إحصائية مجمعة تلخص إجمالي التغطية المالية للالتزامات والأهداف:
 * 1. تعرض عدد الأهداف المكتملة مقابل إجمالي الأهداف المسجلة (مثال: تغطية 3 من 5 أهداف).
 * 2. تحتوي على شريط تقدم إجمالي بتدرج لوني انسيابي (Gradient Progress Bar 6dp).
 * 3. تعرض كبسولة النسبة المئوية العامة المكتملة مع تغيير النمط البصري عند الاكتمال الكامل (100%).
 * 4. تلخص إجمالي المبالغ المتاحة حالياً مقابل إجمالي المبالغ المطلوبة للالتزامات كافة.
 * =====================================================================================
 */

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.financialCreditColor
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
 * دالة العرض لبطاقة الملخص بالتدرج اللوني (CommitmentSummaryGradientCard Composable)
 * -------------------------------------------------------------------------------------
 * [المُدخلات]:
 * - totalTargetSum: إجمالي المبالغ المطلوبة لجميع الالتزامات.
 * - totalAllocatedSum: إجمالي المبالغ المخصصة حالياً.
 * - coveredCount: عدد الالتزامات المكتملة تغطيتها.
 * - totalCount: إجمالي عدد الالتزامات المسجلة.
 * - currencySymbol: رمز العملة المعتمد.
 * - formatCurrency: دالة تنسيق المبالغ المالية.
 * =====================================================================================
 */
@Composable
fun CommitmentSummaryGradientCard(
    totalTargetSum: BigDecimal,
    totalAllocatedSum: BigDecimal,
    coveredCount: Int,
    totalCount: Int,
    currencySymbol: String,
    formatCurrency: (BigDecimal, String) -> String
) {
    val overallPercentFloat = if (totalTargetSum > BigDecimal.ZERO) {
        (totalAllocatedSum.toDouble() / totalTargetSum.toDouble()).coerceIn(0.0, 1.0).toFloat()
    } else 0f
    val overallPercent = (overallPercentFloat * 100).toInt()
    val isFullyCovered = totalAllocatedSum >= totalTargetSum && totalTargetSum > BigDecimal.ZERO
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val creditColor = financialCreditColor(isDark)
    val secondaryColor = MaterialTheme.colorScheme.secondary

    val progressGradient = remember(isDark, creditColor, secondaryColor) {
        Brush.horizontalGradient(
            colors = listOf(
                creditColor,
                secondaryColor
            )
        )
    }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) MaterialTheme.colorScheme.surfaceContainerHigh else MaterialTheme.colorScheme.surfaceContainerLow
        ),
        border = BorderStroke(
            1.dp,
            if (isFullyCovered) creditColor.copy(alpha = 0.35f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.20f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // السطر 1: عنوان التغطية وكبسولة النسبة المئوية
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "تغطية $coveredCount من $totalCount أهداف",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isFullyCovered) creditColor else MaterialTheme.colorScheme.primary.copy(alpha = if (isDark) 0.25f else 0.12f),
                    border = BorderStroke(1.dp, if (isFullyCovered) creditColor else MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = "$overallPercent% مكتمل",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isFullyCovered) MaterialTheme.colorScheme.onTertiary else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.5.dp)
                    )
                }
            }

            // السطر 2: شريط التقدم بتدرج لوني انسيابي (6dp)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(3.dp))
                        .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
                )
                if (overallPercentFloat > 0f) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(overallPercentFloat)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(3.dp))
                            .background(progressGradient)
                    )
                }
            }

            // السطر 3: نصوص الملخص المالي
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "المتاح حالياً: ${formatCurrency(totalAllocatedSum, currencySymbol)}".toWesternDigits(),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "المطلوب: ${formatCurrency(totalTargetSum, currencySymbol)}".toWesternDigits(),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

