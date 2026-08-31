package com.example.ui.screens

import androidx.compose.material3.MaterialTheme

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
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
import androidx.compose.ui.window.Dialog
import com.example.R
import com.example.domain.evaluateSimpleExpression

import java.math.BigDecimal
import com.example.ui.theme.mizanColors

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun CalculatorDialog(
    onDismiss: () -> Unit,
    onValueConfirmed: (BigDecimal) -> Unit,
    activeThemeColor: Color? = null,
    activeSubColor: Color? = null
) {
    var rawExpression by remember { mutableStateOf("") }
    
    // Fallback to app's primary theme color dynamically
    val brandPrimary = activeThemeColor ?: MaterialTheme.colorScheme.primary

    // Evaluate preview in real-time
    val resultPreview = remember(rawExpression) {
        if (rawExpression.isEmpty()) null
        else evaluateSimpleExpression(rawExpression)
    }

    val isExpressionValid = remember(rawExpression) {
        if (rawExpression.isEmpty()) {
            true
        } else {
            val eval = evaluateSimpleExpression(rawExpression)
            val direct = rawExpression.toDoubleOrNull()
            eval != null || direct != null
        }
    }

    val haptic = LocalHapticFeedback.current

    fun performClickFeedback() {
        try {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        } catch (e: Exception) {
            // Ignore in environment without active haptic device
        }
    }

    fun handleDigit(digit: String) {
        performClickFeedback()
        if (rawExpression == "0") {
            rawExpression = digit
        } else {
            rawExpression += digit
        }
    }

    fun handleOperator(op: String) {
        performClickFeedback()
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
        performClickFeedback()
        rawExpression = ""
    }

    fun handleBackspace() {
        performClickFeedback()
        if (rawExpression.isNotEmpty()) {
            rawExpression = rawExpression.dropLast(1)
        }
    }

    fun evaluate() {
        performClickFeedback()
        val result = evaluateSimpleExpression(rawExpression)
        if (result != null) {
            rawExpression = if (result.remainder(java.math.BigDecimal.ONE).compareTo(java.math.BigDecimal.ZERO) == 0) {
                result.toBigInteger().toString()
            } else {
                result.toPlainString()
            }
        }
    }

    fun confirmAndDismiss() {
        if (!isExpressionValid) return
        performClickFeedback()
        val finalBigDecimal = evaluateSimpleExpression(rawExpression)
        val finalValue = if (finalBigDecimal != null) {
            finalBigDecimal
        } else {
            rawExpression.toBigDecimalOrNull() ?: BigDecimal.ZERO
        }
        onValueConfirmed(finalValue)
    }

    val mizanColors = MaterialTheme.mizanColors
    val isIncomeTheme = remember(activeThemeColor, mizanColors) {
        activeThemeColor == mizanColors.credit || activeThemeColor == com.example.ui.theme.CreditGreen || activeThemeColor == com.example.ui.theme.SelectionGreen
    }
    val isExpenseTheme = remember(activeThemeColor, mizanColors) {
        activeThemeColor == mizanColors.debt || activeThemeColor == com.example.ui.theme.DebtRed
    }

    val surfaceVarColor = MaterialTheme.colorScheme.surfaceVariant
    // Calculate background color with clean theme tokens
    val calcBgColor = remember(isIncomeTheme, isExpenseTheme, mizanColors, surfaceVarColor) {
        when {
            isIncomeTheme -> mizanColors.creditContainer
            isExpenseTheme -> mizanColors.debtContainer
            else -> surfaceVarColor
        }
    }

    // Border color matching the mode
    val calcBorderColor = remember(isIncomeTheme, isExpenseTheme, mizanColors, brandPrimary) {
        when {
            isIncomeTheme -> mizanColors.credit
            isExpenseTheme -> mizanColors.debt
            else -> brandPrimary
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = calcBgColor
            ),
            border = BorderStroke(2.dp, calcBorderColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp), // Zero elevation to remove automatic gray overlays
            modifier = Modifier
                .widthIn(max = 360.dp)
                .fillMaxWidth()
                .padding(4.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header of Calculator
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(id = R.string.calc_close_desc),
                            tint = brandPrimary
                        )
                    }

                    Text(
                        text = stringResource(id = R.string.calc_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = brandPrimary,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Digital Display Screen
                val displayBgColor = MaterialTheme.colorScheme.surface
                
                val displayBorderColor = when {
                    isIncomeTheme -> mizanColors.credit.copy(alpha = 0.6f)
                    isExpenseTheme -> mizanColors.debt.copy(alpha = 0.6f)
                    else -> MaterialTheme.colorScheme.outlineVariant
                }

                val displayTextColor = MaterialTheme.colorScheme.onSurface

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = displayBgColor),
                    border = BorderStroke(1.5.dp, displayBorderColor)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.End
                    ) {
                        // Expression Line
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.End,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = rawExpression.ifEmpty { stringResource(id = R.string.calc_default_zero) },
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (rawExpression.isEmpty()) MaterialTheme.mizanColors.contentTertiary else displayTextColor,
                                textAlign = TextAlign.Right,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            
                            // Theme-matched Cursor Blink
                            BlinkingCursor(cursorColor = brandPrimary)
                        }

                        // Preview / Result Line
                        if (resultPreview != null && resultPreview.toPlainString() != rawExpression) {
                            val formattedPreview = if (resultPreview.remainder(java.math.BigDecimal.ONE).compareTo(java.math.BigDecimal.ZERO) == 0) {
                                resultPreview.toBigInteger().toString()
                            } else {
                                resultPreview.toPlainString()
                            }
                            Text(
                                text = "= $formattedPreview",
                                fontSize = 16.sp,
                                color = brandPrimary,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Right
                            )
                        }
                    }
                }

                if (rawExpression.isNotEmpty() && !isExpressionValid) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = stringResource(R.string.calc_invalid_expression),
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Arabic Right-to-Left Layout Keyboard
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Row 1: [⌫] [9] [8] [7]
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            CalcButton(text = "⌫", isBackspace = true, brandPrimary = brandPrimary, modifier = Modifier.weight(1f)) { handleBackspace() }
                            CalcButton(text = "9", isNumber = true, brandPrimary = brandPrimary, modifier = Modifier.weight(1f)) { handleDigit("9") }
                            CalcButton(text = "8", isNumber = true, brandPrimary = brandPrimary, modifier = Modifier.weight(1f)) { handleDigit("8") }
                            CalcButton(text = "7", isNumber = true, brandPrimary = brandPrimary, modifier = Modifier.weight(1f)) { handleDigit("7") }
                        }

                        // Row 2: [×] [6] [5] [4]
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            CalcButton(text = "×", isOp = true, brandPrimary = brandPrimary, modifier = Modifier.weight(1f)) { handleOperator("×") }
                            CalcButton(text = "6", isNumber = true, brandPrimary = brandPrimary, modifier = Modifier.weight(1f)) { handleDigit("6") }
                            CalcButton(text = "5", isNumber = true, brandPrimary = brandPrimary, modifier = Modifier.weight(1f)) { handleDigit("5") }
                            CalcButton(text = "4", isNumber = true, brandPrimary = brandPrimary, modifier = Modifier.weight(1f)) { handleDigit("4") }
                        }

                        // Row 3: [-] [3] [2] [1]
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            CalcButton(text = "-", isOp = true, brandPrimary = brandPrimary, modifier = Modifier.weight(1f)) { handleOperator("-") }
                            CalcButton(text = "3", isNumber = true, brandPrimary = brandPrimary, modifier = Modifier.weight(1f)) { handleDigit("3") }
                            CalcButton(text = "2", isNumber = true, brandPrimary = brandPrimary, modifier = Modifier.weight(1f)) { handleDigit("2") }
                            CalcButton(text = "1", isNumber = true, brandPrimary = brandPrimary, modifier = Modifier.weight(1f)) { handleDigit("1") }
                        }

                        // Row 4: [+] [C] [0] [.]
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            CalcButton(text = "+", isOp = true, brandPrimary = brandPrimary, modifier = Modifier.weight(1f)) { handleOperator("+") }
                            CalcButton(text = "C", isOp = true, brandPrimary = brandPrimary, modifier = Modifier.weight(1f)) { handleClear() }
                            CalcButton(text = "0", isNumber = true, brandPrimary = brandPrimary, modifier = Modifier.weight(1f)) { handleDigit("0") }
                            CalcButton(text = ".", isNumber = true, brandPrimary = brandPrimary, modifier = Modifier.weight(1f)) { handleDigit(".") }
                        }

                        // Row 5: [=] [÷] [OK]
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            CalcButton(text = "=", isEquals = true, brandPrimary = brandPrimary, modifier = Modifier.weight(2f)) { evaluate() }
                            CalcButton(text = "÷", isOp = true, brandPrimary = brandPrimary, modifier = Modifier.weight(1f)) { handleOperator("÷") }
                            CalcButton(text = "OK", isAction = true, brandPrimary = brandPrimary, enabled = isExpressionValid, modifier = Modifier.weight(1f)) { confirmAndDismiss() }
                        }
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
    isAction: Boolean = false,
    isEquals: Boolean = false,
    brandPrimary: Color = MaterialTheme.colorScheme.primary,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    val isIncomeTheme = brandPrimary == MaterialTheme.mizanColors.credit || brandPrimary == MaterialTheme.mizanColors.selection
    val isExpenseTheme = brandPrimary == MaterialTheme.mizanColors.debt

    // Determine button colors dynamically based on active semantic roles
    val backgroundColor = when {
        isEquals -> brandPrimary
        isBackspace || isOp || isAction -> {
            if (isIncomeTheme) MaterialTheme.mizanColors.creditContainer
            else if (isExpenseTheme) MaterialTheme.mizanColors.debtContainer
            else MaterialTheme.colorScheme.surfaceVariant
        }
        else -> { // Numbers
            if (isIncomeTheme) MaterialTheme.mizanColors.creditContainer
            else if (isExpenseTheme) MaterialTheme.mizanColors.debtContainer
            else MaterialTheme.colorScheme.surface
        }
    }

    val textColor = when {
        isEquals -> MaterialTheme.mizanColors.contentOnBrand
        isBackspace || isOp || isAction -> brandPrimary
        else -> MaterialTheme.colorScheme.onSurface
    }

    val buttonBorderColor = when {
        isIncomeTheme -> MaterialTheme.mizanColors.creditBorder
        isExpenseTheme -> MaterialTheme.mizanColors.debtBorder
        else -> MaterialTheme.colorScheme.outlineVariant
    }

    val clickableModifier = if (enabled) {
        Modifier.clickable(onClick = onClick)
    } else {
        Modifier
    }

    Card(
        modifier = modifier
            .height(52.dp)
            .then(clickableModifier),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (enabled) backgroundColor else backgroundColor.copy(alpha = 0.5f)
        ),
        border = BorderStroke(1.dp, if (enabled) buttonBorderColor else buttonBorderColor.copy(alpha = 0.3f)),
        elevation = CardDefaults.cardElevation(defaultElevation = if (enabled) 1.dp else 0.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                fontSize = if (isAction) 16.sp else 22.sp,
                fontWeight = FontWeight.Bold,
                color = if (enabled) textColor else textColor.copy(alpha = 0.4f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun BlinkingCursor(cursorColor: Color) {
    val infiniteTransition = rememberInfiniteTransition(label = "cursor")
    val cursorAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cursorBlink"
    )
    if (cursorAlpha > 0.5f) {
        Text(
            text = "|",
            color = cursorColor,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            modifier = androidx.compose.ui.Modifier.padding(start = 2.dp)
        )
    }
}
