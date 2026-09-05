package com.smartledger.aldaftar.ui.screens.ledger.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartledger.aldaftar.R
import com.smartledger.aldaftar.data.local.entities.FixedCommitment
import com.smartledger.aldaftar.ui.helper.AutoScaleText
import com.smartledger.aldaftar.ui.theme.mizanColors
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

    val mizanColors = MaterialTheme.mizanColors

    val (totalRemainingCommitments, netAmount) = remember(computedCommitments, totalCash) {
        val remaining = computedCommitments.fold(BigDecimal.ZERO) { acc, triple -> acc.add(triple.third) }
        val allocated = computedCommitments.fold(BigDecimal.ZERO) { acc, triple ->
            val needed = (triple.first.targetAmount.subtract(triple.first.currentProgress)).max(BigDecimal.ZERO)
            acc.add(needed.subtract(triple.third))
        }
        val net = (totalCash.subtract(allocated)).max(BigDecimal.ZERO)
        Pair(remaining, net)
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Card 1: Right in RTL -> "الصافي" (Credit / Green)
        Card(
            modifier = Modifier
                .weight(1f)
                .defaultMinSize(minHeight = 52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = mizanColors.creditContainer
            ),
            border = BorderStroke(
                1.dp,
                mizanColors.creditBorder
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = stringResource(R.string.ledger_net_prefix).replace(":", "").trim(),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(2.dp))
                AutoScaleText(
                    text = formatCurrency(netAmount, currencySymbol).toWesternDigits(),
                    baseFontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = mizanColors.credit,
                    maxLines = 1,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Card 2: Left in RTL -> "باقي الالتزامات" (Debt / Red)
        Card(
            modifier = Modifier
                .weight(1f)
                .defaultMinSize(minHeight = 52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = mizanColors.debtContainer
            ),
            border = BorderStroke(
                1.dp,
                mizanColors.debtBorder
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = stringResource(R.string.ledger_remaining_commitments).replace(":", "").trim(),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(2.dp))
                AutoScaleText(
                    text = formatCurrency(totalRemainingCommitments, currencySymbol).toWesternDigits(),
                    baseFontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = mizanColors.debt,
                    maxLines = 1,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
