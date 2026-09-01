package com.example.ui.screens.habayeb.components.header

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.helper.AutoScaleText
import com.example.ui.theme.mizanColors

@Composable
fun HabayebDualMetricCards(
    selectedFilterTab: Int,
    onFilterTabSelected: (Int) -> Unit,
    formattedOwedByThem: String,
    formattedOwedToThem: String,
    haptic: HapticFeedback,
    modifier: Modifier = Modifier,
    isDark: Boolean = false,
    greenColor: Color = Color.Unspecified,
    redColor: Color = Color.Unspecified
) {
    val mizanColors = MaterialTheme.mizanColors
    val effectiveDebtColor = if (redColor != Color.Unspecified) redColor else mizanColors.debt
    val effectiveCreditColor = if (greenColor != Color.Unspecified) greenColor else mizanColors.credit

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 8.dp, start = 16.dp, end = 16.dp, bottom = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Right Card: "لنا" (Red Color - Solid Safe Background)
        val isOwedBySelected = selectedFilterTab == 1
        val owedByCardBg = if (isOwedBySelected) mizanColors.chipDebtSelectedBackground else mizanColors.chipDebtUnselectedBackground
        val owedByBorderColor = if (isOwedBySelected) effectiveDebtColor else mizanColors.chipDebtUnselectedBorder

        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = owedByCardBg),
            elevation = CardDefaults.cardElevation(defaultElevation = if (isOwedBySelected) 3.dp else 1.dp),
            border = BorderStroke(
                width = if (isOwedBySelected) 1.5.dp else 1.dp,
                color = owedByBorderColor
            ),
            modifier = Modifier
                .weight(1f)
                .height(52.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(color = effectiveDebtColor)
                    ) {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onFilterTabSelected(if (isOwedBySelected) 0 else 1)
                    },
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = stringResource(id = R.string.habayeb_filter_owed_by),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = effectiveDebtColor,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    AutoScaleText(
                        text = formattedOwedByThem,
                        baseFontSize = 14.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = effectiveDebtColor,
                        maxLines = 1,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Left Card: "علينا" (Emerald Green Color - Solid Safe Background)
        val isOwedToSelected = selectedFilterTab == 2
        val owedToCardBg = if (isOwedToSelected) mizanColors.chipCreditSelectedBackground else mizanColors.chipCreditUnselectedBackground
        val owedToBorderColor = if (isOwedToSelected) effectiveCreditColor else mizanColors.chipCreditUnselectedBorder

        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = owedToCardBg),
            elevation = CardDefaults.cardElevation(defaultElevation = if (isOwedToSelected) 3.dp else 1.dp),
            border = BorderStroke(
                width = if (isOwedToSelected) 1.5.dp else 1.dp,
                color = owedToBorderColor
            ),
            modifier = Modifier
                .weight(1f)
                .height(52.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(color = effectiveCreditColor)
                    ) {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onFilterTabSelected(if (isOwedToSelected) 0 else 2)
                    },
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = stringResource(id = R.string.habayeb_filter_owed_to),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = effectiveCreditColor,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    AutoScaleText(
                        text = formattedOwedToThem,
                        baseFontSize = 14.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = effectiveCreditColor,
                        maxLines = 1,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
