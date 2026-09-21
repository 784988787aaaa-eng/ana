package com.smartledger.aldaftar.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartledger.aldaftar.R
import com.smartledger.aldaftar.data.local.entities.AppSettings
import com.smartledger.aldaftar.ui.helper.HabayebMathHelper
import com.smartledger.aldaftar.ui.screens.habayeb.utils.ExchangeRateHelper
import com.smartledger.aldaftar.ui.theme.MizanDialogTokens
import java.math.BigDecimal

@Composable
fun CurrencySettingsDialog(
    settings: AppSettings,
    onSaveSettings: (AppSettings, String, BigDecimal, Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    val haptic = LocalHapticFeedback.current

    val currencyYer = stringResource(id = R.string.currency_yer)
    val currencySar = stringResource(id = R.string.currency_sar)
    val currencyUsd = stringResource(id = R.string.currency_usd)

    val state = rememberCurrencySettingsState(
        settings = settings,
        currencyYer = currencyYer,
        currencySar = currencySar,
        currencyUsd = currencyUsd
    )

    val rateFocusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    DisposableEffect(Unit) {
        onDispose {
            hideKeyboardAndClearFocus(focusManager, keyboardController)
        }
    }

    MizanAnimatedDialog(
        onDismissRequest = onDismiss
    ) { dismissDialog ->
        ConfigureDialogImeWindow()
        MizanDialogCard(
            maxWidth = MizanDialogTokens.compactMaxWidth
        ) {
            AnimatedContent(
                targetState = state.activeDialogState,
                transitionSpec = {
                    (fadeIn(animationSpec = tween(120)) + slideInVertically(
                        animationSpec = tween(120),
                        initialOffsetY = { it / 10 }
                    )).togetherWith(
                        fadeOut(animationSpec = tween(120)) + slideOutVertically(
                            animationSpec = tween(120),
                            targetOffsetY = { -it / 10 }
                        )
                    )
                },
                label = "currencyDialogState"
            ) { dialogState ->
                when (dialogState) {
                    CurrencyDialogState.None -> {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState()),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            // Ultra-compact horizontal header
                            CompactDialogHeader(
                                title = stringResource(R.string.currency_settings_dialog_title),
                                icon = Icons.Default.MonetizationOn,
                                iconTint = MaterialTheme.colorScheme.primary,
                                onClose = dismissDialog
                            )

                            // 1. Primary Currency (العملة الأساسية)
                            CompactPrimaryCurrencyCard(
                                currencies = state.currenciesToDisplay,
                                selectedCurrency = state.localDefaultCurrency,
                                currencyYer = currencyYer,
                                currencySar = currencySar,
                                currencyUsd = currencyUsd,
                                onSelectCurrency = { newDefault -> state.onDefaultCurrencyChange(newDefault) },
                                haptic = haptic
                            )

                            // 2. Exchange Rate Section (سعر الصرف)
                            CompactExchangeRateCard(
                                currenciesToDisplay = state.currenciesToDisplay,
                                localDefaultCurrency = state.localDefaultCurrency,
                                selectedTargetCurrency = state.selectedTargetCurrency,
                                displayPair = state.displayPair,
                                rateInputStr = state.rateInputStr,
                                currentRateValue = state.currentRateValue,
                                rateFocusRequester = rateFocusRequester,
                                currencyYer = currencyYer,
                                currencySar = currencySar,
                                currencyUsd = currencyUsd,
                                onTargetCurrencyChange = { newTarget -> state.onTargetCurrencyChange(newTarget) },
                                onRateInputChange = { newInput -> state.onRateInputChange(newInput) },
                                onToggleDirection = { state.toggleEquationDirection() },
                                haptic = haptic
                            )

                            // 3. Compact Action Buttons (Save & Cancel)
                            CompactActionButtons(
                                onSave = {
                                    state.handleSave(
                                        settings = settings,
                                        onSaveSettings = onSaveSettings,
                                        onDismiss = dismissDialog
                                    )
                                },
                                onCancel = dismissDialog
                            )
                        }
                    }

                    is CurrencyDialogState.RevalueConfirm -> {
                        CompactCurrencyRevalueConfirmContent(
                            targetCurrency = dialogState.targetCurrency,
                            baseCurrency = state.localDefaultCurrency,
                            newRate = dialogState.newRate,
                            onConfirmHistoricalAndFuture = {
                                state.handleConfirmHistoricalAndFuture(
                                    settings = settings,
                                    targetCurrency = dialogState.targetCurrency,
                                    newRate = dialogState.newRate,
                                    onSaveSettings = onSaveSettings,
                                    onDismiss = dismissDialog
                                )
                            },
                            onConfirmFutureOnly = {
                                state.handleConfirmFutureOnly(
                                    settings = settings,
                                    targetCurrency = dialogState.targetCurrency,
                                    newRate = dialogState.newRate,
                                    onSaveSettings = onSaveSettings,
                                    onDismiss = dismissDialog
                                )
                            },
                            onBack = {
                                state.activeDialogState = CurrencyDialogState.None
                            },
                            haptic = haptic
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CompactDialogHeader(
    title: String,
    icon: ImageVector,
    iconTint: Color,
    onClose: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 2.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(iconTint.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(16.dp)
                )
            }
            Text(
                text = title,
                fontSize = 14.5.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        IconButton(
            onClick = onClose,
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = stringResource(R.string.desc_close),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(15.dp)
            )
        }
    }
}

@Composable
private fun CompactPrimaryCurrencyCard(
    currencies: List<String>,
    selectedCurrency: String,
    currencyYer: String,
    currencySar: String,
    currencyUsd: String,
    onSelectCurrency: (String) -> Unit,
    haptic: HapticFeedback
) {
    MizanDialogInnerCard(
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 7.dp)
    ) {
        Text(
            text = stringResource(R.string.currency_primary_section_title),
            fontSize = 11.5.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(5.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            currencies.forEach { symbol ->
                val isSelected = selectedCurrency == symbol
                val currencyName = when (symbol) {
                    currencyYer -> stringResource(R.string.currency_name_yer)
                    currencySar -> stringResource(R.string.currency_name_sar)
                    currencyUsd -> stringResource(R.string.currency_name_usd)
                    else -> symbol
                }

                Surface(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onSelectCurrency(symbol)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(32.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                    contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                    border = BorderStroke(
                        width = 1.dp,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 4.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(11.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                        }
                        Text(
                            text = symbol,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = currencyName,
                            fontSize = 9.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CompactExchangeRateCard(
    currenciesToDisplay: List<String>,
    localDefaultCurrency: String,
    selectedTargetCurrency: String,
    displayPair: Pair<String, String>,
    rateInputStr: String,
    currentRateValue: BigDecimal,
    rateFocusRequester: FocusRequester,
    currencyYer: String,
    currencySar: String,
    currencyUsd: String,
    onTargetCurrencyChange: (String) -> Unit,
    onRateInputChange: (String) -> Unit,
    onToggleDirection: () -> Unit,
    haptic: HapticFeedback
) {
    val availableTargets = remember(currenciesToDisplay, localDefaultCurrency) {
        currenciesToDisplay.filter { it != localDefaultCurrency }
    }
    var isFocused by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    MizanDialogInnerCard(
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 7.dp)
    ) {
        // Header row with title on the start and currency tabs on the end!
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stringResource(R.string.currency_exchange_section_title),
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                availableTargets.forEach { symbol ->
                    val isSelected = selectedTargetCurrency == symbol
                    val currencyName = when (symbol) {
                        currencyYer -> stringResource(R.string.currency_name_yer)
                        currencySar -> stringResource(R.string.currency_name_sar)
                        currencyUsd -> stringResource(R.string.currency_name_usd)
                        else -> symbol
                    }

                    Surface(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onTargetCurrencyChange(symbol)
                        },
                        modifier = Modifier.height(26.dp),
                        shape = RoundedCornerShape(6.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        border = BorderStroke(
                            width = 1.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Text(
                                text = symbol,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "($currencyName)",
                                fontSize = 9.sp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(5.dp))

        // Conversion Equation Input Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(36.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surface)
                .border(
                    width = if (isFocused) 1.5.dp else 1.dp,
                    color = if (isFocused) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(horizontal = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(5.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
            ) {
                Text(
                    text = "1 ${displayPair.first} =",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 2.dp),
                contentAlignment = Alignment.Center
            ) {
                BasicTextField(
                    value = rateInputStr,
                    onValueChange = onRateInputChange,
                    singleLine = true,
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(onDone = {
                        keyboardController?.hide()
                        focusManager.clearFocus()
                    }),
                    textStyle = TextStyle(
                        textAlign = TextAlign.Center,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(rateFocusRequester)
                        .onFocusChanged { isFocused = it.isFocused },
                    decorationBox = { innerTextField ->
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (rateInputStr.isEmpty()) {
                                Text(
                                    text = stringResource(R.string.currency_settings_dialog_price),
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                    textAlign = TextAlign.Center
                                )
                            }
                            innerTextField()
                        }
                    }
                )
            }

            Surface(
                shape = RoundedCornerShape(5.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
            ) {
                Text(
                    text = displayPair.second,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }

            IconButton(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onToggleDirection()
                },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Sync,
                    contentDescription = "تبديل الاتجاه",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(3.dp))

        // Status note showing direct rate and reciprocal market equivalent
        val currentRateFormatted = if (currentRateValue > BigDecimal.ZERO) {
            val directStr = "1 ${displayPair.first} = ${HabayebMathHelper.formatActiveRateBadge(currentRateValue)} ${displayPair.second}"
            val reciprocal = runCatching {
                BigDecimal.ONE.divide(currentRateValue, 6, java.math.RoundingMode.HALF_EVEN).stripTrailingZeros()
            }.getOrNull()
            if (reciprocal != null && reciprocal > BigDecimal.ZERO) {
                "$directStr (أي: 1 ${displayPair.second} = ${HabayebMathHelper.formatActiveRateBadge(reciprocal)} ${displayPair.first})"
            } else {
                directStr
            }
        } else {
            stringResource(R.string.currency_no_rate_set)
        }

        Text(
            text = currentRateFormatted,
            fontSize = 9.5.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun CompactActionButtons(
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedButton(
            onClick = onCancel,
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .weight(1f)
                .height(36.dp),
            contentPadding = PaddingValues(0.dp)
        ) {
            Text(
                text = stringResource(R.string.currency_settings_dialog_cancel),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Button(
            onClick = onSave,
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            modifier = Modifier
                .weight(1.2f)
                .height(36.dp),
            contentPadding = PaddingValues(0.dp)
        ) {
            Text(
                text = stringResource(R.string.currency_settings_dialog_save),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}

@Composable
private fun CompactCurrencyRevalueConfirmContent(
    targetCurrency: String,
    baseCurrency: String,
    newRate: BigDecimal,
    onConfirmHistoricalAndFuture: () -> Unit,
    onConfirmFutureOnly: () -> Unit,
    onBack: () -> Unit,
    haptic: HapticFeedback
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        CompactDialogHeader(
            title = stringResource(id = R.string.currency_update_rate_title),
            icon = Icons.Default.Refresh,
            iconTint = MaterialTheme.colorScheme.primary,
            onClose = onBack
        )

        // Ultra-compact rate banner
        val canonicalPair = remember(targetCurrency, baseCurrency) {
            ExchangeRateHelper.getCanonicalPairOrder(targetCurrency, baseCurrency)
        }
        val canonicalBase = canonicalPair.first
        val canonicalTarget = canonicalPair.second
        val canonicalDisplayRate = remember(canonicalBase, canonicalTarget, targetCurrency, baseCurrency, newRate) {
            if (targetCurrency == canonicalBase && baseCurrency == canonicalTarget) {
                newRate
            } else {
                runCatching {
                    BigDecimal.ONE.divide(newRate, com.smartledger.aldaftar.domain.model.FinancialPolicy.rateScale, java.math.RoundingMode.HALF_EVEN)
                }.getOrDefault(newRate)
            }
        }

        MizanDialogInnerCard(
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 5.dp),
            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "1 $canonicalBase = ",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = HabayebMathHelper.formatActiveRateBadge(canonicalDisplayRate),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = " $canonicalTarget",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Text(
            text = stringResource(id = R.string.currency_update_rate_confirm_msg, targetCurrency),
            fontSize = 11.sp,
            lineHeight = 15.sp,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp)
        )

        // Option 1 (Default / Recommended): Apply to new / future transactions only
        CompactRevalueChoiceCard(
            title = "${stringResource(R.string.currency_update_future_only_title)} (افتراضياً)",
            description = stringResource(R.string.currency_update_future_only_desc),
            icon = Icons.Default.ArrowForward,
            iconTint = MaterialTheme.colorScheme.primary,
            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
            borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.45f),
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onConfirmFutureOnly()
            }
        )

        // Option 2: Apply to all transactions (Past and Future)
        CompactRevalueChoiceCard(
            title = stringResource(R.string.currency_update_past_future_title),
            description = stringResource(R.string.currency_update_past_future_desc),
            icon = Icons.Default.Sync,
            iconTint = MaterialTheme.colorScheme.secondary,
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
            borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onConfirmHistoricalAndFuture()
            }
        )

        OutlinedButton(
            onClick = onBack,
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(34.dp),
            contentPadding = PaddingValues(0.dp)
        ) {
            Text(
                text = stringResource(R.string.currency_settings_dialog_cancel),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun CompactRevalueChoiceCard(
    title: String,
    description: String,
    icon: ImageVector,
    iconTint: Color,
    containerColor: Color,
    borderColor: Color,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = containerColor,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(iconTint.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(15.dp)
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(1.dp)
            ) {
                Text(
                    text = title,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = description,
                    fontSize = 9.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 13.sp
                )
            }
        }
    }
}
