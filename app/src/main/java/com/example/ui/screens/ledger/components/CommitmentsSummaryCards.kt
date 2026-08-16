package com.example.ui.screens.ledger.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.local.entities.FixedCommitment
import java.math.BigDecimal

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
fun CommitmentsSummaryCards(
    commitments: List<FixedCommitment>,
    computedCommitments: List<Triple<FixedCommitment, BigDecimal, BigDecimal>>,
    totalCash: BigDecimal,
    currencySymbol: String,
    formatCurrency: (BigDecimal, String) -> String,
    modifier: Modifier = Modifier
) {
    if (commitments.isEmpty()) return

    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f

    val (totalRemainingCommitments, netAmount) = remember(computedCommitments, totalCash) {
        val remaining = computedCommitments.fold(BigDecimal.ZERO) { acc, triple -> acc.add(triple.third) }
        val allocated = computedCommitments.fold(BigDecimal.ZERO) { acc, triple ->
            val needed = (triple.first.targetAmount.subtract(triple.first.currentProgress)).max(BigDecimal.ZERO)
            acc.add(needed.subtract(triple.third))
        }
        val net = (totalCash.subtract(allocated)).max(BigDecimal.ZERO)
        Pair(remaining, net)
    }

    val outlineVarColor = MaterialTheme.colorScheme.outlineVariant
    val netBg = remember(isDark) { if (isDark) Color(0xFF1B3B22) else Color(0xFFEDF7ED) }
    val netTextColor = remember(isDark) { if (isDark) Color(0xFF81C784) else Color(0xFF2E7D32) }
    val netBorderColor = remember(isDark, outlineVarColor) { if (isDark) Color(0xFF2E7D32).copy(alpha = 0.4f) else outlineVarColor.copy(alpha = 0.3f) }

    val remainingBg = remember(isDark) { if (isDark) Color(0xFF4C1C1C) else Color(0xFFFDEDED) }
    val remainingTextColor = remember(isDark) { if (isDark) Color(0xFFE57373) else Color(0xFFC62828) }
    val remainingBorderColor = remember(isDark, outlineVarColor) { if (isDark) Color(0xFFC62828).copy(alpha = 0.4f) else outlineVarColor.copy(alpha = 0.3f) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Card 1: Net Amount Capsule
        Box(
            modifier = Modifier
                .weight(1f)
                .height(38.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(netBg)
                .border(
                    BorderStroke(0.8.dp, netBorderColor),
                    RoundedCornerShape(12.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(horizontal = 4.dp)
            ) {
                Text(
                    text = "${stringResource(R.string.ledger_net_prefix)} ",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = netTextColor
                )
                Text(
                    text = formatCurrency(netAmount, currencySymbol).toWesternDigits(),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = netTextColor
                )
            }
        }

        // Card 2: Remaining Commitments Capsule
        Box(
            modifier = Modifier
                .weight(1f)
                .height(38.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(remainingBg)
                .border(
                    BorderStroke(0.8.dp, remainingBorderColor),
                    RoundedCornerShape(12.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(horizontal = 4.dp)
            ) {
                Text(
                    text = "${stringResource(R.string.ledger_remaining_commitments)} ",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = remainingTextColor
                )
                Text(
                    text = formatCurrency(totalRemainingCommitments, currencySymbol).toWesternDigits(),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = remainingTextColor
                )
            }
        }
    }
}
