package com.smartledger.aldaftar.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartledger.aldaftar.R
import com.smartledger.aldaftar.domain.evaluateSimpleExpression
import com.smartledger.aldaftar.ui.components.MizanAnimatedDialog
import com.smartledger.aldaftar.ui.components.MizanDialogCard
import com.smartledger.aldaftar.ui.components.MizanDialogHeader
import com.smartledger.aldaftar.ui.theme.MizanDialogTokens
import com.smartledger.aldaftar.ui.theme.mizanColors
import java.math.BigDecimal

@Composable
fun CalculatorDialog(
    onDismiss: () -> Unit,
    onValueConfirmed: (BigDecimal) -> Unit,
    activeThemeColor: Color? = null,
    activeSubColor: Color? = null
) {
    var rawExpression by remember { mutableStateOf("") }

    val brandPrimary = activeThemeColor ?: MaterialTheme.colorScheme.primary

    val calculatedResult = remember(rawExpression) {
        if (rawExpression.isEmpty()) null
        else evaluateSimpleExpression(rawExpression)
    }

    val isExpressionValid = remember(rawExpression, calculatedResult) {
        if (rawExpression.isEmpty()) {
            true
        } else {
            calculatedResult != null || rawExpression.toBigDecimalOrNull() != null
        }
    }

    fun handleDigit(digit: String) {
        if (rawExpression == "0") {
            rawExpression = digit
        } else {
            rawExpression += digit
        }
    }

    fun handleOperator(op: String) {
        if (rawExpression.isEmpty()) {
            if (op == "-") {
                rawExpression = "-"
            }
            return
        }
        val lastChar = rawExpression.last()
        if (lastChar in listOf('+', '-', '×', '÷')) {
            rawExpression = rawExpression.dropLast(1) + op
        } else {
            rawExpression += op
        }
    }

    fun handleClear() {
        rawExpression = ""
    }

    fun handleBackspace() {
        if (rawExpression.isNotEmpty()) {
            rawExpression = rawExpression.dropLast(1)
        }
    }

    fun evaluate() {
        val result = calculatedResult ?: evaluateSimpleExpression(rawExpression)
        if (result != null) {
            rawExpression = if (result.remainder(BigDecimal.ONE).compareTo(BigDecimal.ZERO) == 0) {
                result.toBigInteger().toString()
            } else {
                result.toPlainString()
            }
        }
    }

    fun confirmAndDismiss() {
        if (!isExpressionValid) return
        val finalBigDecimal = calculatedResult ?: evaluateSimpleExpression(rawExpression)
        val finalValue: BigDecimal = finalBigDecimal ?: (rawExpression.toBigDecimalOrNull() ?: BigDecimal.ZERO)
        onValueConfirmed(finalValue)
    }

    val mizanColors = MaterialTheme.mizanColors
    val isIncomeTheme = remember(activeThemeColor, mizanColors) {
        activeThemeColor == mizanColors.credit ||
            activeThemeColor == com.smartledger.aldaftar.ui.theme.CreditGreen ||
            activeThemeColor == com.smartledger.aldaftar.ui.theme.FinancialSelectionGreen
    }
    val isExpenseTheme = remember(activeThemeColor, mizanColors) {
        activeThemeColor == mizanColors.debt ||
            activeThemeColor == com.smartledger.aldaftar.ui.theme.DebtRed
    }

    val accentColor = remember(isIncomeTheme, isExpenseTheme, mizanColors, brandPrimary) {
        when {
            isIncomeTheme -> mizanColors.credit
            isExpenseTheme -> mizanColors.debt
            else -> brandPrimary
        }
    }

    MizanAnimatedDialog(
        onDismissRequest = onDismiss
    ) { dismissDialog ->
        MizanDialogCard(
            maxWidth = 350.dp,
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp)
        ) {
            MizanDialogHeader(
                title = stringResource(id = R.string.calc_title),
                icon = Icons.Default.Calculate,
                iconTint = accentColor,
                onCloseClick = dismissDialog
            )

            // Financial Expression & Result Display Screen (LTR math direction)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 84.dp),
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.42f),
                border = BorderStroke(1.2.dp, accentColor.copy(alpha = 0.45f))
            ) {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = rawExpression.ifEmpty { stringResource(id = R.string.calc_default_zero) },
                            fontSize = 26.sp,
                            lineHeight = 32.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (rawExpression.isEmpty()) {
                                MaterialTheme.mizanColors.contentTertiary
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            },
                            textAlign = TextAlign.End,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.fillMaxWidth()
                        )

                        if (calculatedResult != null && calculatedResult.toPlainString() != rawExpression) {
                            val formattedPreview = if (calculatedResult.remainder(BigDecimal.ONE).compareTo(BigDecimal.ZERO) == 0) {
                                calculatedResult.toBigInteger().toString()
                            } else {
                                calculatedResult.toPlainString()
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "= $formattedPreview",
                                fontSize = 15.sp,
                                lineHeight = 19.sp,
                                color = accentColor,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.End,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }

            if (rawExpression.isNotEmpty() && !isExpressionValid) {
                Text(
                    text = stringResource(R.string.calc_invalid_expression),
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }

            // Keypad Grid
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        CalcButton(text = "⌫", isBackspace = true, brandPrimary = accentColor, modifier = Modifier.weight(1f)) { handleBackspace() }
                        CalcButton(text = "9", isNumber = true, brandPrimary = accentColor, modifier = Modifier.weight(1f)) { handleDigit("9") }
                        CalcButton(text = "8", isNumber = true, brandPrimary = accentColor, modifier = Modifier.weight(1f)) { handleDigit("8") }
                        CalcButton(text = "7", isNumber = true, brandPrimary = accentColor, modifier = Modifier.weight(1f)) { handleDigit("7") }
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        CalcButton(text = "×", isOp = true, brandPrimary = accentColor, modifier = Modifier.weight(1f)) { handleOperator("×") }
                        CalcButton(text = "6", isNumber = true, brandPrimary = accentColor, modifier = Modifier.weight(1f)) { handleDigit("6") }
                        CalcButton(text = "5", isNumber = true, brandPrimary = accentColor, modifier = Modifier.weight(1f)) { handleDigit("5") }
                        CalcButton(text = "4", isNumber = true, brandPrimary = accentColor, modifier = Modifier.weight(1f)) { handleDigit("4") }
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        CalcButton(text = "-", isOp = true, brandPrimary = accentColor, modifier = Modifier.weight(1f)) { handleOperator("-") }
                        CalcButton(text = "3", isNumber = true, brandPrimary = accentColor, modifier = Modifier.weight(1f)) { handleDigit("3") }
                        CalcButton(text = "2", isNumber = true, brandPrimary = accentColor, modifier = Modifier.weight(1f)) { handleDigit("2") }
                        CalcButton(text = "1", isNumber = true, brandPrimary = accentColor, modifier = Modifier.weight(1f)) { handleDigit("1") }
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        CalcButton(text = "+", isOp = true, brandPrimary = accentColor, modifier = Modifier.weight(1f)) { handleOperator("+") }
                        CalcButton(text = "C", isClear = true, brandPrimary = accentColor, modifier = Modifier.weight(1f)) { handleClear() }
                        CalcButton(text = "0", isNumber = true, brandPrimary = accentColor, modifier = Modifier.weight(1f)) { handleDigit("0") }
                        CalcButton(text = ".", isNumber = true, brandPrimary = accentColor, modifier = Modifier.weight(1f)) { handleDigit(".") }
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        CalcButton(text = "=", isEquals = true, brandPrimary = accentColor, modifier = Modifier.weight(1.6f)) { evaluate() }
                        CalcButton(text = "÷", isOp = true, brandPrimary = accentColor, modifier = Modifier.weight(1f)) { handleOperator("÷") }
                        CalcButton(text = "OK", isAction = true, brandPrimary = accentColor, enabled = isExpressionValid, modifier = Modifier.weight(1.4f)) { confirmAndDismiss() }
                    }
                }
            }
        }
    }
}

@Composable
fun CalcButton(
    text: String,
    modifier: Modifier = Modifier,
    isNumber: Boolean = false,
    isOp: Boolean = false,
    isBackspace: Boolean = false,
    isClear: Boolean = false,
    isAction: Boolean = false,
    isEquals: Boolean = false,
    brandPrimary: Color = MaterialTheme.colorScheme.primary,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    val backgroundColor = when {
        isAction -> brandPrimary
        isEquals -> brandPrimary.copy(alpha = 0.16f)
        isClear || isBackspace -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.45f)
        isOp -> brandPrimary.copy(alpha = 0.12f)
        else -> MaterialTheme.colorScheme.surface
    }

    val textColor = when {
        isAction -> MaterialTheme.mizanColors.contentOnBrand
        isEquals -> brandPrimary
        isClear || isBackspace -> MaterialTheme.colorScheme.error
        isOp -> brandPrimary
        else -> MaterialTheme.colorScheme.onSurface
    }

    val buttonBorderColor = when {
        isAction -> brandPrimary
        isEquals || isOp -> brandPrimary.copy(alpha = 0.35f)
        isClear || isBackspace -> MaterialTheme.colorScheme.error.copy(alpha = 0.35f)
        else -> MaterialTheme.colorScheme.outlineVariant
    }

    val haptic = LocalHapticFeedback.current

    Surface(
        onClick = {
            if (enabled) {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onClick()
            }
        },
        enabled = enabled,
        modifier = modifier.height(48.dp),
        shape = MizanDialogTokens.buttonShape,
        color = if (enabled) backgroundColor else backgroundColor.copy(alpha = 0.4f),
        border = BorderStroke(1.dp, if (enabled) buttonBorderColor else buttonBorderColor.copy(alpha = 0.3f)),
        shadowElevation = if (isNumber && enabled) 0.5.dp else 0.dp
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (isAction) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = if (enabled) textColor else textColor.copy(alpha = 0.4f),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = text,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (enabled) textColor else textColor.copy(alpha = 0.4f),
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                Text(
                    text = text,
                    fontSize = if (isClear || isBackspace) 17.sp else 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (enabled) textColor else textColor.copy(alpha = 0.4f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
