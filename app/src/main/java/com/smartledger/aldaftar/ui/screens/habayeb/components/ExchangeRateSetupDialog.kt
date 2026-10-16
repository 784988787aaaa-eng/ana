package com.smartledger.aldaftar.ui.screens.habayeb.components

import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartledger.aldaftar.R
import com.smartledger.aldaftar.domain.model.FinancialPolicy
import com.smartledger.aldaftar.ui.components.MizanAnimatedDialog
import com.smartledger.aldaftar.ui.components.MizanDialogActions
import com.smartledger.aldaftar.ui.components.MizanDialogCard
import com.smartledger.aldaftar.ui.components.MizanDialogHeader
import com.smartledger.aldaftar.ui.components.MizanDialogInnerCard
import com.smartledger.aldaftar.ui.components.RequestFocusAndShowKeyboard
import com.smartledger.aldaftar.ui.helper.HabayebMathHelper
import com.smartledger.aldaftar.ui.screens.habayeb.utils.CurrencyConfig
import com.smartledger.aldaftar.ui.screens.habayeb.utils.ExchangeRateHelper
import com.smartledger.aldaftar.ui.theme.MizanDialogTokens
import com.smartledger.aldaftar.ui.theme.mizanColors
import java.math.BigDecimal
import java.math.RoundingMode

@Composable
fun ExchangeRateSetupContent(
    selectedCurrency: String,
    rateTargetCurrency: String,
    initialRateStr: String,
    activeThemeColor: Color,
    onDismiss: () -> Unit,
    onConfirm: (BigDecimal) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val canonicalPair = remember(selectedCurrency, rateTargetCurrency) {
        ExchangeRateHelper.getCanonicalPairOrder(selectedCurrency, rateTargetCurrency)
    }
    val canonicalBase = canonicalPair.first
    val canonicalTarget = canonicalPair.second
    var isEquationFlipped by remember { mutableStateOf(false) }
    val displayBase = if (!isEquationFlipped) canonicalBase else canonicalTarget
    val displayTarget = if (!isEquationFlipped) canonicalTarget else canonicalBase

    val computedInitialRate = remember(initialRateStr, displayBase, selectedCurrency) {
        val bd = initialRateStr.trim().toBigDecimalOrNull()
        if (bd != null && bd > BigDecimal.ZERO) {
            val rateForDisplay = if (selectedCurrency == displayBase) {
                bd
            } else {
                runCatching {
                    BigDecimal.ONE.divide(bd, FinancialPolicy.rateScale, RoundingMode.HALF_EVEN)
                }.getOrDefault(bd)
            }
            HabayebMathHelper.formatActiveRateBadge(rateForDisplay)
        } else {
            initialRateStr
        }
    }

    var rateTfv by remember(computedInitialRate) {
        mutableStateOf(
            TextFieldValue(
                text = computedInitialRate,
                selection = TextRange(computedInitialRate.length)
            )
        )
    }
    val rateStr = rateTfv.text
    var isChecked by remember { mutableStateOf(false) }
    var showUncheckedError by remember { mutableStateOf(false) }
    var isFocused by remember { mutableStateOf(false) }

    fun toggleDirection() {
        val currentBd = rateStr.trim().toBigDecimalOrNull()
        isEquationFlipped = !isEquationFlipped
        if (currentBd != null && currentBd > BigDecimal.ZERO) {
            val flippedBd = runCatching {
                BigDecimal.ONE.divide(currentBd, FinancialPolicy.rateScale, RoundingMode.HALF_EVEN)
            }.getOrNull()
            val newText = if (flippedBd != null) HabayebMathHelper.formatActiveRateBadge(flippedBd) else ""
            rateTfv = TextFieldValue(
                text = newText,
                selection = TextRange(newText.length)
            )
        }
    }

    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val mizanColors = MaterialTheme.mizanColors

    RequestFocusAndShowKeyboard(
        focusRequester = focusRequester,
        autoShow = true
    )

    val statusColor by animateColorAsState(
        targetValue = when {
            isChecked -> mizanColors.credit
            showUncheckedError -> mizanColors.debt
            else -> activeThemeColor
        },
        animationSpec = tween(durationMillis = 180),
        label = "statusColor"
    )

    val validRateToastStr = stringResource(id = R.string.habayeb_toast_enter_valid_rate)
    val confirmRateFirstToastStr = stringResource(id = R.string.habayeb_toast_confirm_rate_first)

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Column(
            modifier = modifier.padding(horizontal = 2.dp, vertical = 2.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // 1. Conversion Equation Studio Card
            MizanDialogInnerCard(
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .border(
                            width = if (isFocused) 1.5.dp else 1.dp,
                            color = if (isFocused) activeThemeColor else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.65f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = activeThemeColor.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = stringResource(R.string.currency_settings_dialog_unit_rate_prefix, displayBase),
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = activeThemeColor,
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        BasicTextField(
                            value = rateTfv,
                            onValueChange = { inputTfv ->
                                val cleanedText = CurrencyConfig.normalizeDigits(inputTfv.text)
                                val isPartialDecimal = cleanedText.isEmpty() || cleanedText == "." || cleanedText.endsWith(".")
                                val parsed = cleanedText.toBigDecimalOrNull()
                                val decimalPlaces = cleanedText.substringAfter('.', "").length
                                if (isPartialDecimal || (parsed != null && parsed > BigDecimal.ZERO && decimalPlaces <= FinancialPolicy.rateScale)) {
                                    rateTfv = inputTfv.copy(text = cleanedText)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .onFocusChanged { isFocused = it.isFocused }
                                .focusRequester(focusRequester),
                            singleLine = true,
                            cursorBrush = SolidColor(activeThemeColor),
                            textStyle = TextStyle(
                                textAlign = TextAlign.Center,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = activeThemeColor
                            ),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Decimal,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    keyboardController?.hide()
                                    focusManager.clearFocus()
                                }
                            ),
                            decorationBox = { innerTextField ->
                                Box(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (rateStr.isEmpty()) {
                                        Text(
                                            text = stringResource(id = R.string.habayeb_exchange_rate_placeholder),
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                                        innerTextField()
                                    }
                                }
                            }
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                    ) {
                        Text(
                            text = displayTarget,
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
                        )
                    }

                    IconButton(
                        onClick = { toggleDirection() },
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(9.dp))
                            .background(activeThemeColor.copy(alpha = 0.12f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sync,
                            contentDescription = stringResource(R.string.currency_exchange_toggle_desc),
                            tint = activeThemeColor,
                            modifier = Modifier.size(17.dp)
                        )
                    }
                }
            }

            // 2. Interactive Confirmation Check Card
            Surface(
                onClick = {
                    isChecked = !isChecked
                    if (isChecked) {
                        showUncheckedError = false
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = if (isChecked) activeThemeColor.copy(alpha = 0.10f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                border = BorderStroke(
                    width = 1.dp,
                    color = if (isChecked) activeThemeColor else if (showUncheckedError) mizanColors.debt else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(if (isChecked) activeThemeColor else Color.Transparent)
                            .border(
                                width = 1.5.dp,
                                color = if (isChecked) activeThemeColor else if (showUncheckedError) mizanColors.debt else MaterialTheme.colorScheme.outlineVariant,
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isChecked) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }

                    Text(
                        text = stringResource(id = R.string.habayeb_confirm_exchange_rate_question),
                        fontSize = 12.sp,
                        fontWeight = if (isChecked) FontWeight.Bold else FontWeight.Medium,
                        color = if (isChecked) activeThemeColor else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Text(
                text = stringResource(id = R.string.habayeb_exchange_rate_hint_text),
                fontSize = 10.5.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
                lineHeight = 15.sp
            )

            // 3. Action Buttons
            MizanDialogActions(
                confirmText = stringResource(id = R.string.habayeb_save),
                confirmIcon = Icons.Default.Check,
                confirmColor = statusColor,
                confirmContainerColor = statusColor,
                onConfirm = {
                    val rateBD = try { BigDecimal(rateStr.trim()) } catch (_: Exception) { null }
                    if (rateBD == null || rateBD.compareTo(BigDecimal.ZERO) <= 0) {
                        Toast.makeText(context, validRateToastStr, Toast.LENGTH_SHORT).show()
                    } else if (!isChecked) {
                        showUncheckedError = true
                        Toast.makeText(context, confirmRateFirstToastStr, Toast.LENGTH_SHORT).show()
                    } else {
                        val effectiveRate = if (displayBase == selectedCurrency && displayTarget == rateTargetCurrency) {
                            rateBD
                        } else {
                            runCatching {
                                BigDecimal.ONE.divide(rateBD, FinancialPolicy.rateScale, RoundingMode.HALF_EVEN)
                            }.getOrDefault(rateBD)
                        }
                        onConfirm(effectiveRate)
                    }
                },
                cancelText = stringResource(id = R.string.habayeb_cancel),
                onCancel = onDismiss
            )
        }
    }
}

@Composable
fun ExchangeRateSetupDialog(
    selectedCurrency: String,
    rateTargetCurrency: String,
    initialRateStr: String,
    activeThemeColor: Color,
    onDismiss: () -> Unit,
    onConfirm: (BigDecimal) -> Unit
) {
    MizanAnimatedDialog(
        onDismissRequest = onDismiss
    ) { dismissDialog ->
        MizanDialogCard(
            maxWidth = MizanDialogTokens.compactMaxWidth,
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp)
        ) {
            MizanDialogHeader(
                title = stringResource(id = R.string.currency_settings_dialog_title),
                subtitle = stringResource(id = R.string.currency_setup_dialog_subtitle),
                icon = Icons.Default.MonetizationOn,
                iconTint = activeThemeColor,
                onCloseClick = dismissDialog
            )

            ExchangeRateSetupContent(
                selectedCurrency = selectedCurrency,
                rateTargetCurrency = rateTargetCurrency,
                initialRateStr = initialRateStr,
                activeThemeColor = activeThemeColor,
                onDismiss = dismissDialog,
                onConfirm = onConfirm,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
