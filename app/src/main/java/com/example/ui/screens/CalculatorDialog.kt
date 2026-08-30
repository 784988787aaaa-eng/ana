package com.example.ui.screens

/*
 * =====================================================================================
 * حزمة الآلة الحاسبة المالية المدمجة (Financial Calculator Dialog Package)
 * -------------------------------------------------------------------------------------
 * تحتوي هذه الفئة على مربع حوار الآلة الحاسبة التفاعلية المخصصة (CalculatorDialog)،
 * والمصممة لتسهيل العمليات الحسابية أثناء إدخال المبالغ النقدية والمعاملات اليومية
 * مع دعم التقييم الفوري للمعادلات والتغذية اللمسية وتكيّف الألوان حسب نوع القيد.
 * =====================================================================================
 */

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

/*
 * =====================================================================================
 * نافذة الآلة الحاسبة المالية (CalculatorDialog)
 * -------------------------------------------------------------------------------------
 * [الوصف والهدف]:
 * حوار منبثق (Dialog) يوفر لوحة أرقام وعمليات حسابية (+, -, ×, ÷) متقدمة:
 * 1. حساب فوري ومعاينة النتيجة بشكل مباشر تحت التعبير الحسابي.
 * 2. مؤشر كتابة وامض (Blinking Cursor) وتنسيق عربي من اليمين لليسار.
 * 3. تكييف الألوان الديناميكي مع نوع المعاملة (أخضر للمقبوضات/الدائن، أحمر للمدفوعات/المدين).
 * 4. تغذية لمسية حساسة (Haptic Feedback) عند الضغط على كل مفتاح.
 *
 * [المُدخلات]:
 * - onDismiss: رد نداء إغلاق نافذة الحاسبة دون اعتماد القيمة.
 * - onValueConfirmed: رد نداء تأكيد النتيجة النهائية وتمرير كائن BigDecimal للحقل المالي.
 * - activeThemeColor: اللون الأساسي النشط للسمة (أخضر أو أحمر أو أزرق).
 * - activeSubColor: اللون الثانوي للسمة.
 * =====================================================================================
 */
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun CalculatorDialog(
    onDismiss: () -> Unit,
    onValueConfirmed: (BigDecimal) -> Unit,
    activeThemeColor: Color? = null,
    activeSubColor: Color? = null
) {
    // التعبير الحسابي الخام المدخل من قبل المستخدم
    var rawExpression by remember { mutableStateOf("") }
    
    // اللون الأساسي النشط للعلامة التجارية أو السمة
    val isDarkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val isIncomeTheme = activeThemeColor == com.example.ui.theme.CreditGreen || activeThemeColor == com.example.ui.theme.SelectionGreen || activeThemeColor == com.example.ui.theme.CreditGreenDark
    val isExpenseTheme = activeThemeColor == com.example.ui.theme.DebtRed || activeThemeColor == com.example.ui.theme.DebtRedDark
    val brandPrimary = when {
        isIncomeTheme -> com.example.ui.theme.financialCreditColor(isDarkTheme)
        isExpenseTheme -> com.example.ui.theme.financialDebtColor(isDarkTheme)
        else -> MaterialTheme.colorScheme.primary
    }

    /*
     * ---------------------------------------------------------------------------------
     * تقييم فوري للمعادلة الحسابية في الوقت الفعلي (Real-time Evaluation)
     * ---------------------------------------------------------------------------------
     */
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

    /*
     * إطلاق اهتزاز لمسي خفيف عند الضغط
     */
    fun performClickFeedback() {
        try {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        } catch (e: Exception) {
            // تجاهل الاستثناء في الأجهزة التي لا تدعم الاهتزاز
        }
    }

    /*
     * معالجة إدخال الأرقام والنقطة العشرية
     */
    fun handleDigit(digit: String) {
        performClickFeedback()
        if (rawExpression == "0") {
            rawExpression = digit
        } else {
            rawExpression += digit
        }
    }

    /*
     * معالجة إدخال العمليات الرياضية (+, -, ×, ÷)
     */
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
            // استبدال العملية السابقة بالعملية الجديدة
            rawExpression = rawExpression.dropLast(1) + op
        } else {
            rawExpression += op
        }
    }

    /*
     * مسح التعبير الحسابي بالكامل (Clear)
     */
    fun handleClear() {
        performClickFeedback()
        rawExpression = ""
    }

    /*
     * حذف آخر خانة مدخلة (Backspace)
     */
    fun handleBackspace() {
        performClickFeedback()
        if (rawExpression.isNotEmpty()) {
            rawExpression = rawExpression.dropLast(1)
        }
    }

    /*
     * تقييم النتيجة وتحديث سطر التعبير الحسابي بالقيمة النهائية (=)
     */
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

    /*
     * تأكيد المبلغ وإرجاعه إلى الحقل المالي وإغلاق الحوار (OK)
     */
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

    /*
     * ---------------------------------------------------------------------------------
     * حساب تدرجات الألوان وخلفية الحاسبة بناءً على نمط السمة (إيراد / مصروف)
     * ---------------------------------------------------------------------------------
     */
    val isDark = isDarkTheme

    val surfaceVarColor = MaterialTheme.colorScheme.surfaceVariant
    val calcBgColor = remember(isDark, isIncomeTheme, isExpenseTheme, surfaceVarColor) {
        when {
            isIncomeTheme -> com.example.ui.theme.financialCreditBg(isDark)
            isExpenseTheme -> com.example.ui.theme.financialDebtBg(isDark)
            else -> surfaceVarColor
        }
    }

    val calcBorderColor = remember(isIncomeTheme, isExpenseTheme, brandPrimary) {
        when {
            isIncomeTheme -> com.example.ui.theme.financialCreditColor(isDark)
            isExpenseTheme -> com.example.ui.theme.financialDebtColor(isDark)
            else -> brandPrimary
        }
    }

    /*
     * ---------------------------------------------------------------------------------
     * نافذة الحوار وبطاقة الآلة الحاسبة
     * ---------------------------------------------------------------------------------
     */
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = calcBgColor
            ),
            border = BorderStroke(2.dp, calcBorderColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
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
                // ترويسة الحاسبة مع زر الإغلاق
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

                // الشاشة الرقمية لعرض المعادلة والنتيجة
                val displayBgColor = if (isDark) {
                    when {
                        isIncomeTheme -> com.example.ui.theme.CreditContainerDark
                        isExpenseTheme -> com.example.ui.theme.DebtContainerDark
                        else -> MaterialTheme.colorScheme.surface
                    }
                } else {
                    MaterialTheme.colorScheme.surface
                }
                
                val displayBorderColor = when {
                    isIncomeTheme -> com.example.ui.theme.financialCreditColor(isDark).copy(alpha = 0.6f)
                    isExpenseTheme -> com.example.ui.theme.financialDebtColor(isDark).copy(alpha = 0.6f)
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
                        // سطر كتابة التعبير الرياضي مع المؤشر الوامض
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.End,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = rawExpression.ifEmpty { stringResource(id = R.string.calc_default_zero) },
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (rawExpression.isEmpty()) MaterialTheme.colorScheme.onSurfaceVariant else displayTextColor,
                                textAlign = TextAlign.Right,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            
                            // وميض المؤشر المتناسق مع السمة
                            BlinkingCursor(cursorColor = brandPrimary)
                        }

                        // سطر المعاينة اللحظية للنتيجة
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

                // لوحة المفاتيح بالاتجاه العربي (RTL)
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // الصف الأول: [⌫] [9] [8] [7]
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            CalcButton(text = "⌫", isBackspace = true, brandPrimary = brandPrimary, modifier = Modifier.weight(1f)) { handleBackspace() }
                            CalcButton(text = "9", isNumber = true, brandPrimary = brandPrimary, modifier = Modifier.weight(1f)) { handleDigit("9") }
                            CalcButton(text = "8", isNumber = true, brandPrimary = brandPrimary, modifier = Modifier.weight(1f)) { handleDigit("8") }
                            CalcButton(text = "7", isNumber = true, brandPrimary = brandPrimary, modifier = Modifier.weight(1f)) { handleDigit("7") }
                        }

                        // الصف الثاني: [×] [6] [5] [4]
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            CalcButton(text = "×", isOp = true, brandPrimary = brandPrimary, modifier = Modifier.weight(1f)) { handleOperator("×") }
                            CalcButton(text = "6", isNumber = true, brandPrimary = brandPrimary, modifier = Modifier.weight(1f)) { handleDigit("6") }
                            CalcButton(text = "5", isNumber = true, brandPrimary = brandPrimary, modifier = Modifier.weight(1f)) { handleDigit("5") }
                            CalcButton(text = "4", isNumber = true, brandPrimary = brandPrimary, modifier = Modifier.weight(1f)) { handleDigit("4") }
                        }

                        // الصف الثالث: [-] [3] [2] [1]
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            CalcButton(text = "-", isOp = true, brandPrimary = brandPrimary, modifier = Modifier.weight(1f)) { handleOperator("-") }
                            CalcButton(text = "3", isNumber = true, brandPrimary = brandPrimary, modifier = Modifier.weight(1f)) { handleDigit("3") }
                            CalcButton(text = "2", isNumber = true, brandPrimary = brandPrimary, modifier = Modifier.weight(1f)) { handleDigit("2") }
                            CalcButton(text = "1", isNumber = true, brandPrimary = brandPrimary, modifier = Modifier.weight(1f)) { handleDigit("1") }
                        }

                        // الصف الرابع: [+] [C] [0] [.]
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            CalcButton(text = "+", isOp = true, brandPrimary = brandPrimary, modifier = Modifier.weight(1f)) { handleOperator("+") }
                            CalcButton(text = "C", isOp = true, brandPrimary = brandPrimary, modifier = Modifier.weight(1f)) { handleClear() }
                            CalcButton(text = "0", isNumber = true, brandPrimary = brandPrimary, modifier = Modifier.weight(1f)) { handleDigit("0") }
                            CalcButton(text = ".", isNumber = true, brandPrimary = brandPrimary, modifier = Modifier.weight(1f)) { handleDigit(".") }
                        }

                        // الصف الخامس: [=] [÷] [OK]
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

/*
 * =====================================================================================
 * زر لوحة الآلة الحاسبة المخصص (CalcButton)
 * -------------------------------------------------------------------------------------
 * [الوصف والهدف]:
 * عنصر زر قابل للنقر مصمم بنسب تباين عالية وارتفاع لمسي مريح مع تلوين وظيفي.
 * =====================================================================================
 */
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
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val isIncomeTheme = brandPrimary == com.example.ui.theme.CreditGreen || brandPrimary == com.example.ui.theme.CreditGreenDark || brandPrimary == com.example.ui.theme.SelectionGreen
    val isExpenseTheme = brandPrimary == com.example.ui.theme.DebtRed || brandPrimary == com.example.ui.theme.DebtRedDark
    val resolvedBrandPrimary = when {
        isIncomeTheme -> com.example.ui.theme.financialCreditColor(isDark)
        isExpenseTheme -> com.example.ui.theme.financialDebtColor(isDark)
        else -> brandPrimary.takeUnless { it == Color.Unspecified } ?: MaterialTheme.colorScheme.primary
    }

    // تحديد ألوان الزر ديناميكياً
    val backgroundColor = when {
        isEquals -> resolvedBrandPrimary
        isBackspace -> {
            if (isDark) {
                if (isIncomeTheme) com.example.ui.theme.CreditContainerDark else com.example.ui.theme.DebtContainerDark
            } else {
                if (isIncomeTheme) com.example.ui.theme.financialCreditBg(isDark) else com.example.ui.theme.financialDebtBg(isDark)
            }
        }
        isOp || isAction -> {
            if (isDark) {
                if (isIncomeTheme) com.example.ui.theme.CreditContainerDark else com.example.ui.theme.DebtContainerDark
            } else {
                if (isIncomeTheme) com.example.ui.theme.financialCreditBg(isDark) else com.example.ui.theme.financialDebtBg(isDark)
            }
        }
        else -> { // الأرقام
            if (isDark) {
                if (isIncomeTheme) com.example.ui.theme.CreditContainerDark else com.example.ui.theme.DebtContainerDark
            } else {
                MaterialTheme.colorScheme.surface
            }
        }
    }

    val textColor = when {
        isEquals -> MaterialTheme.colorScheme.onPrimary
        isBackspace -> {
            if (isDark) MaterialTheme.colorScheme.onSurface else resolvedBrandPrimary
        }
        isOp || isAction -> resolvedBrandPrimary
        else -> { // الأرقام
            MaterialTheme.colorScheme.onSurface
        }
    }

    val buttonBorderColor = when {
        isIncomeTheme -> com.example.ui.theme.financialCreditColor(isDark).copy(alpha = 0.4f)
        isExpenseTheme -> com.example.ui.theme.financialDebtColor(isDark).copy(alpha = 0.4f)
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

/*
 * =====================================================================================
 * مؤشر الكتابة الوامض (BlinkingCursor)
 * -------------------------------------------------------------------------------------
 * [الوصف والهدف]:
 * عنصر رسومي يحاكي وميض مؤشر الإدخال النصي لتوفير تجربة مستخدم ديناميكية وحية.
 * =====================================================================================
 */
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

