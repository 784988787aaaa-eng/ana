package com.smartledger.aldaftar.ui.screens.habayeb.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
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
import com.smartledger.aldaftar.R
import java.math.BigDecimal
import java.math.RoundingMode
import com.smartledger.aldaftar.ui.theme.mizanColors
import com.smartledger.aldaftar.ui.screens.habayeb.utils.CurrencyConfig

import com.smartledger.aldaftar.domain.model.TransactionType

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
    initialType: String = TransactionType.OWED_BY_THEM.value,
    modifier: Modifier = Modifier
) {
    val clearedStr = stringResource(id = R.string.status_account_cleared)
    val remainingOnHimStr = stringResource(id = R.string.status_remaining_on_him)
    val remainingForHimStr = stringResource(id = R.string.status_remaining_for_him)
    val remainingWithHimStr = stringResource(id = R.string.status_remaining_with_him)

    val mizanColors = MaterialTheme.mizanColors
    val surfaceVariantColor = MaterialTheme.colorScheme.onSurfaceVariant
    val outlineColor = MaterialTheme.colorScheme.outline

    val chipState = remember(amount, currencyCode, initialType, mizanColors, clearedStr, remainingOnHimStr, remainingForHimStr, remainingWithHimStr, surfaceVariantColor, outlineColor) {
        val cmp = amount.setScale(4, RoundingMode.HALF_EVEN).compareTo(BigDecimal.ZERO)
        val isZero = cmp == 0
        val isPositive = cmp > 0
        val isNegative = cmp < 0

        val redColor = mizanColors.debt
        val redHeaderColor = mizanColors.chipDebtText
        val greenColor = mizanColors.credit
        val greenHeaderColor = mizanColors.chipCreditText

        val (chipColor, headerTextColor, stateLabel) = when {
            isZero -> Triple(outlineColor, outlineColor, clearedStr)
            initialType == TransactionType.OWED_TO_THEM.value -> {
                if (isNegative) {
                    Triple(greenColor, greenHeaderColor, remainingForHimStr)
                } else if (isPositive) {
                    Triple(redColor, redHeaderColor, remainingWithHimStr)
                } else {
                    Triple(outlineColor, outlineColor, clearedStr)
                }
            }
            else -> {
                if (isPositive) {
                    Triple(redColor, redHeaderColor, remainingOnHimStr)
                } else if (isNegative) {
                    Triple(greenColor, greenHeaderColor, remainingForHimStr)
                } else {
                    Triple(outlineColor, outlineColor, clearedStr)
                }
            }
        }

        val bdAmount = amount.abs()
        val formattedAmountStr = "${com.smartledger.aldaftar.ui.helper.HabayebMathHelper.formatSmart(bdAmount)} $currencyCode"
        Triple(chipColor, headerTextColor, Pair(stateLabel, formattedAmountStr))
    }

    val targetChipColor = chipState.first
    val targetHeaderTextColor = chipState.second
    val stateLabel = chipState.third.first
    val formattedAmountStr = chipState.third.second

    val targetBgColor = if (isSelected) targetChipColor.copy(alpha = 0.16f) else targetChipColor.copy(alpha = 0.09f)
    val targetBorderColor = if (isSelected) targetChipColor else targetChipColor.copy(alpha = 0.45f)
    val borderWidth = if (isSelected) 1.5.dp else 1.dp

    Column(
        modifier = modifier
            .defaultMinSize(minHeight = 52.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(targetBgColor)
            .border(borderWidth, targetBorderColor, RoundedCornerShape(10.dp))
            .clickable(onClick = onSelect)
            .padding(horizontal = 6.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stateLabel,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = targetHeaderTextColor,
            textAlign = TextAlign.Center,
            maxLines = 1,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(2.dp))
        AutoSizeText(
            text = formattedAmountStr,
            fontSize = 14.5.sp,
            fontWeight = FontWeight.Black,
            color = targetChipColor,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun BalanceCompactChip(
    amount: Double,
    currencyCode: String,
    isSelected: Boolean,
    onSelect: () -> Unit,
    initialType: String = TransactionType.OWED_BY_THEM.value,
    modifier: Modifier = Modifier
) {
    BalanceCompactChip(
        amount = BigDecimal.valueOf(amount),
        currencyCode = currencyCode,
        isSelected = isSelected,
        onSelect = onSelect,
        initialType = initialType,
        modifier = modifier
    )
}

@Composable
fun CustomerSummaryCard(
    currencySymbol: String,
    netDebtMap: Map<String, BigDecimal> = emptyMap(),
    netDebtBDMap: Map<String, BigDecimal> = emptyMap(),
    initialType: String = TransactionType.OWED_BY_THEM.value,
    selectedCurrencyFilter: String? = null,
    onCurrencyFilterSelected: (String?) -> Unit = {}
) {
    // The dashboard/account summary is intentionally expressed in the app's
    // configured local currency only. Foreign balances remain available in
    // transaction details/reports and are never mixed into the primary card.
    val localCurrency = remember(currencySymbol) {
        CurrencyConfig.getBySymbol(currencySymbol)?.symbol ?: currencySymbol
    }
    val localAmount = remember(netDebtMap, netDebtBDMap, localCurrency) {
        val source = if (netDebtBDMap.isNotEmpty()) netDebtBDMap else netDebtMap.mapValues { BigDecimal.valueOf(it.value.toDouble()) }
        source[localCurrency] ?: source[currencySymbol] ?: BigDecimal.ZERO
    }
    val selected = selectedCurrencyFilter == localCurrency || selectedCurrencyFilter == currencySymbol
    BalanceCompactChip(
        amount = localAmount,
        currencyCode = localCurrency,
        isSelected = selected,
        onSelect = {
            onCurrencyFilterSelected(if (selected) null else localCurrency)
        },
        initialType = initialType,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 2.dp)
    )
}
