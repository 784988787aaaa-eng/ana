package com.example.ui.screens.habayeb.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import java.math.BigDecimal
import java.math.RoundingMode

@Composable
fun AutoSizeText(
    text: String,
    fontSize: TextUnit,
    fontWeight: FontWeight? = null,
    color: Color = Color.Unspecified,
    textAlign: TextAlign? = null,
    modifier: Modifier = Modifier,
    maxLines: Int = 1
) {
    var fontSizeState by remember(text, fontSize) { mutableStateOf(fontSize) }
    var readyToDraw by remember(text, fontSize) { mutableStateOf(false) }

    Text(
        text = text,
        style = TextStyle(fontSize = fontSizeState, fontWeight = fontWeight, color = color),
        textAlign = textAlign,
        maxLines = maxLines,
        overflow = TextOverflow.Clip,
        softWrap = false,
        modifier = modifier.drawWithContent {
            if (readyToDraw) {
                drawContent()
            }
        },
        onTextLayout = { textLayoutResult ->
            if (textLayoutResult.hasVisualOverflow) {
                val currentSize = fontSizeState.value
                if (currentSize > 8f) {
                    fontSizeState = (currentSize - 0.5f).sp
                } else {
                    readyToDraw = true
                }
            } else {
                readyToDraw = true
            }
        }
    )
}

@Composable
fun BalanceCompactChip(
    amount: BigDecimal,
    currencyCode: String,
    isSelected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    val clearedStr = stringResource(id = R.string.status_account_cleared)
    val remainingOnHimStr = stringResource(id = R.string.status_remaining_on_him)
    val remainingForHimStr = stringResource(id = R.string.status_remaining_for_him)

    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val surfaceVariantColor = MaterialTheme.colorScheme.onSurfaceVariant

    val chipState = remember(amount, currencyCode, isDark, clearedStr, remainingOnHimStr, remainingForHimStr, surfaceVariantColor) {
        val cmp = amount.setScale(4, RoundingMode.HALF_EVEN).compareTo(BigDecimal.ZERO)
        val isZero = cmp == 0

        val redColor = if (isDark) Color(0xFFFF5252) else Color(0xFFDC2626)
        val redHeaderColor = if (isDark) Color(0xFFFF8A8A) else Color(0xFFB91C1C)
        val greenColor = if (isDark) Color(0xFF34D399) else Color(0xFF059669)
        val greenHeaderColor = if (isDark) Color(0xFF6EE7B7) else Color(0xFF047857)

        val (chipColor, headerTextColor, stateLabel) = when {
            isZero -> Triple(Color(0xFF757575), Color(0xFF757575), clearedStr)
            cmp > 0 -> Triple(redColor, redHeaderColor, remainingOnHimStr)
            cmp < 0 -> Triple(greenColor, greenHeaderColor, remainingForHimStr)
            else -> Triple(surfaceVariantColor, surfaceVariantColor, clearedStr)
        }

        val bdAmount = amount.abs()
        val formattedAmountStr = "${com.example.ui.helper.HabayebMathHelper.formatSmart(bdAmount)} $currencyCode"
        Triple(chipColor, headerTextColor, Pair(stateLabel, formattedAmountStr))
    }

    val targetChipColor = chipState.first
    val targetHeaderTextColor = chipState.second
    val stateLabel = chipState.third.first
    val formattedAmountStr = chipState.third.second

    val targetBgColor = if (isSelected) targetChipColor.copy(alpha = 0.16f) else if (isDark) targetChipColor.copy(alpha = 0.10f) else targetChipColor.copy(alpha = 0.08f)
    val targetBorderColor = if (isSelected) targetChipColor else targetChipColor.copy(alpha = 0.45f)
    val borderWidth = if (isSelected) 1.5.dp else 1.dp

    val animBgColor by androidx.compose.animation.animateColorAsState(targetValue = targetBgColor, animationSpec = androidx.compose.animation.core.tween(220), label = "compactChipBg")
    val animBorderColor by androidx.compose.animation.animateColorAsState(targetValue = targetBorderColor, animationSpec = androidx.compose.animation.core.tween(220), label = "compactChipBorder")
    val animHeaderTextColor by androidx.compose.animation.animateColorAsState(targetValue = targetHeaderTextColor, animationSpec = androidx.compose.animation.core.tween(220), label = "compactChipHeader")
    val animChipColor by androidx.compose.animation.animateColorAsState(targetValue = targetChipColor, animationSpec = androidx.compose.animation.core.tween(220), label = "compactChipMain")

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(animBgColor)
            .border(borderWidth, animBorderColor, RoundedCornerShape(10.dp))
            .clickable(onClick = onSelect)
            .padding(horizontal = 4.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stateLabel,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = animHeaderTextColor,
            textAlign = TextAlign.Center
        )
        AutoSizeText(
            text = formattedAmountStr,
            fontSize = 15.sp,
            fontWeight = FontWeight.Black,
            color = animChipColor,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun BalanceCompactChip(
    amount: Double,
    currencyCode: String,
    isSelected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    BalanceCompactChip(
        amount = BigDecimal.valueOf(amount),
        currencyCode = currencyCode,
        isSelected = isSelected,
        onSelect = onSelect,
        modifier = modifier
    )
}

@Composable
fun CustomerSummaryCard(
    currencySymbol: String,
    netDebtMap: Map<String, Double> = emptyMap(),
    netDebtBDMap: Map<String, BigDecimal> = emptyMap(),
    selectedCurrencyFilter: String? = null,
    onCurrencyFilterSelected: (String?) -> Unit = {}
) {
    val effectiveBDMap = remember(netDebtMap, netDebtBDMap) {
        if (netDebtBDMap.isNotEmpty()) {
            netDebtBDMap
        } else {
            netDebtMap.mapValues { BigDecimal.valueOf(it.value) }
        }
    }

    val allCurrencies = remember(effectiveBDMap, currencySymbol) {
        val foreignCurrencies = effectiveBDMap.keys.filter { it != currencySymbol }.sorted()
        listOf(currencySymbol) + foreignCurrencies
    }

    val isCompact = allCurrencies.size <= 3

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 2.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        val rowModifier = if (isCompact) {
            Modifier
                .fillMaxWidth()
                .padding(vertical = 1.dp)
        } else {
            Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(vertical = 1.dp)
        }

        Row(
            modifier = rowModifier,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            for (curr in allCurrencies) {
                val netDebtVal = effectiveBDMap[curr] ?: BigDecimal.ZERO
                val chipModifier = if (isCompact) Modifier.weight(1f) else Modifier.widthIn(min = 100.dp)
                BalanceCompactChip(
                    amount = netDebtVal,
                    currencyCode = curr,
                    isSelected = selectedCurrencyFilter == curr,
                    onSelect = {
                        if (selectedCurrencyFilter == curr) {
                            onCurrencyFilterSelected(null)
                        } else {
                            onCurrencyFilterSelected(curr)
                        }
                    },
                    modifier = chipModifier
                )
            }
        }
    }
}
