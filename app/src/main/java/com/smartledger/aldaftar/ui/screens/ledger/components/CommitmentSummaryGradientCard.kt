package com.smartledger.aldaftar.ui.screens.ledger.components

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartledger.aldaftar.ui.theme.mizanColors
import java.math.BigDecimal

import java.math.RoundingMode

private fun String.toWesternDigits(): String {
    var result = this
    val eastern = charArrayOf('٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩')
    val western = charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9')
    for (i in 0..9) {
        result = result.replace(eastern[i], western[i])
    }
    return result
}

@Composable
fun CommitmentSummaryGradientCard(
    totalTargetSum: BigDecimal,
    totalAllocatedSum: BigDecimal,
    coveredCount: Int,
    totalCount: Int,
    currencySymbol: String,
    formatCurrency: (BigDecimal, String) -> String
) {
    val mizanColors = MaterialTheme.mizanColors
    val primaryColor = MaterialTheme.colorScheme.primary
    val overallPercentFloat = if (totalTargetSum > BigDecimal.ZERO) {
        totalAllocatedSum.divide(totalTargetSum, 4, RoundingMode.HALF_EVEN).toFloat().coerceIn(0f, 1f)
    } else 0f
    val overallPercent = (overallPercentFloat * 100).toInt()
    val isFullyCovered = totalAllocatedSum >= totalTargetSum && totalTargetSum > BigDecimal.ZERO

    val progressGradient = remember(mizanColors.credit, primaryColor) {
        Brush.horizontalGradient(
            colors = listOf(
                mizanColors.credit,
                primaryColor
            )
        )
    }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = mizanColors.appSurfaceContainer
        ),
        border = BorderStroke(
            1.dp,
            if (isFullyCovered) mizanColors.creditBorder.copy(alpha = 0.35f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.20f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Line 1: Coverage title & Percentage capsule
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
                    color = if (isFullyCovered) mizanColors.credit else mizanColors.brandPrimaryContainer,
                    border = BorderStroke(1.dp, if (isFullyCovered) mizanColors.creditBorder else MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = "$overallPercent% مكتمل",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isFullyCovered) mizanColors.onCredit else mizanColors.onBrandPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.5.dp)
                    )
                }
            }

            // Line 2: Matching Gradient Progress Bar (6dp)
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

            // Line 3: Financial Summary Text
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
