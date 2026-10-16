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
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Sync
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartledger.aldaftar.R
import com.smartledger.aldaftar.data.local.entities.AppSettings
import com.smartledger.aldaftar.ui.helper.HabayebMathHelper
import com.smartledger.aldaftar.ui.screens.habayeb.utils.ExchangeRateHelper
import com.smartledger.aldaftar.ui.theme.MizanDialogTokens
import java.math.BigDecimal
import java.math.RoundingMode

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
            maxWidth = MizanDialogTokens.maxWidth,
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp)
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
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // 1. Prestigious Dialog Header
                            MizanDialogHeader(
                                title = stringResource(R.string.currency_settings_dialog_title),
                                subtitle = stringResource(R.string.currency_settings_dialog_subtitle),
                                icon = Icons.Default.MonetizationOn,
                                iconTint = MaterialTheme.colorScheme.primary,
                                onCloseClick = dismissDialog
                            )

                            // 2. Primary Currency Card (العملة الأساسية)
                            CompactPrimaryCurrencyCard(
                                currencies = state.currenciesToDisplay,
                                selectedCurrency = state.localDefaultCurrency,
                                currencyYer = currencyYer,
                                currencySar = currencySar,
                                currencyUsd = currencyUsd,
                                onSelectCurrency = { newDefault -> state.onDefaultCurrencyChange(newDefault) },
                                haptic = haptic
                            )

                            // 3. Exchange Rate Studio Card (أسعار الصرف)
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

                            // 4. Unified Action Buttons (Save & Cancel)
                            MizanDialogActions(
                                confirmText = stringResource(R.string.currency_settings_dialog_save),
                                confirmIcon = Icons.Default.Check,
                                onConfirm = {
                                    state.handleSave(
                                        settings = settings,
                                        onSaveSettings = onSaveSettings,
                                        onDismiss = dismissDialog
                                    )
                                },
                                cancelText = stringResource(R.string.currency_settings_dialog_cancel),
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
private fun CompactPrimaryCurrencyCard(
    currencies: List<String>,
    selectedCurrency: String,
    currencyYer: String,
    currencySar: String,
    currencyUsd: String,
    onSelectCurrency: (String) -> Unit,
    haptic: HapticFeedback
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.14f),
        border = BorderStroke(0.9.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.22f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(1.dp)
                ) {
                    Text(
                        text = stringResource(R.string.currency_primary_section_title),
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(R.string.currency_primary_section_hint),
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Surface(
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)
                ) {
                    Text(
                        text = stringResource(R.string.currency_primary_badge),
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
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
                            .height(44.dp),
                        shape = RoundedCornerShape(11.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                        contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                        border = BorderStroke(
                            width = if (isSelected) 1.5.dp else 1.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f)
                        ),
                        shadowElevation = if (isSelected) 2.dp else 0.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 4.dp, vertical = 3.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                }
                                Text(
                                    text = symbol,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                            Text(
                                text = currencyName,
                                fontSize = 9.5.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
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

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.32f),
        border = BorderStroke(0.9.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // 1. Section Title & Segmented Target Currency Selector
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(R.string.currency_exchange_section_title),
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(R.string.currency_edit_rate_for),
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Segmented Target Currency Tabs
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
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
                            modifier = Modifier
                                .weight(1f)
                                .height(36.dp),
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) {
                                MaterialTheme.colorScheme.secondaryContainer
                            } else {
                                MaterialTheme.colorScheme.surface
                            },
                            border = BorderStroke(
                                width = if (isSelected) 1.2.dp else 0.8.dp,
                                color = if (isSelected) {
                                    MaterialTheme.colorScheme.secondary
                                } else {
                                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                }
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = if (isSelected) Icons.Default.Check else Icons.Default.Sync,
                                    contentDescription = null,
                                    tint = if (isSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = symbol,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "($currencyName)",
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                    color = if (isSelected) MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.85f) else MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }

            // 2. Interactive Conversion Equation Studio Box
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(
                        width = if (isFocused) 1.5.dp else 1.dp,
                        color = if (isFocused) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.65f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = stringResource(R.string.currency_settings_dialog_unit_rate_prefix, displayPair.first),
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary,
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
                        value = rateInputStr,
                        onValueChange = onRateInputChange,
                        singleLine = true,
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(onDone = {
                            keyboardController?.hide()
                            focusManager.clearFocus()
                        }),
                        textStyle = TextStyle(
                            textAlign = TextAlign.Center,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
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
                                        fontSize = 12.sp,
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
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                ) {
                    Text(
                        text = displayPair.second,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
                    )
                }

                IconButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onToggleDirection()
                    },
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(9.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Sync,
                        contentDescription = stringResource(R.string.currency_exchange_toggle_desc),
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(17.dp)
                    )
                }
            }

            // 3. Live Rate & Reciprocal Summary Strip
            val currentRateFormatted = if (currentRateValue > BigDecimal.ZERO) {
                val directStr = stringResource(
                    R.string.currency_direct_rate_pattern,
                    displayPair.first,
                    HabayebMathHelper.formatActiveRateBadge(currentRateValue),
                    displayPair.second
                )
                val reciprocal = runCatching {
                    BigDecimal.ONE.divide(currentRateValue, 6, RoundingMode.HALF_EVEN).stripTrailingZeros()
                }.getOrNull()
                if (reciprocal != null && reciprocal > BigDecimal.ZERO) {
                    stringResource(
                        R.string.currency_reciprocal_rate_pattern,
                        directStr,
                        displayPair.second,
                        HabayebMathHelper.formatActiveRateBadge(reciprocal),
                        displayPair.first
                    )
                } else {
                    directStr
                }
            } else {
                stringResource(R.string.currency_no_rate_set)
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(9.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.75f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.75f),
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = currentRateFormatted,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
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
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        MizanDialogHeader(
            title = stringResource(id = R.string.currency_update_rate_title),
            subtitle = stringResource(id = R.string.currency_revalue_subtitle),
            icon = Icons.Default.Refresh,
            iconTint = MaterialTheme.colorScheme.primary,
            onCloseClick = onBack
        )

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
                    BigDecimal.ONE.divide(newRate, com.smartledger.aldaftar.domain.model.FinancialPolicy.rateScale, RoundingMode.HALF_EVEN)
                }.getOrDefault(newRate)
            }
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(
                        R.string.currency_direct_rate_pattern,
                        canonicalBase,
                        HabayebMathHelper.formatActiveRateBadge(canonicalDisplayRate),
                        canonicalTarget
                    ),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Text(
            text = stringResource(id = R.string.currency_update_rate_confirm_msg, targetCurrency),
            fontSize = 12.sp,
            lineHeight = 18.sp,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp)
        )

        // Option 1 (Default / Recommended): Apply to new / future transactions only
        CompactRevalueChoiceCard(
            title = stringResource(R.string.currency_update_future_only_title),
            badge = stringResource(R.string.currency_recommended_badge),
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
            badge = null,
            description = stringResource(R.string.currency_update_past_future_desc),
            icon = Icons.Default.Sync,
            iconTint = MaterialTheme.colorScheme.secondary,
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
            borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f),
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onConfirmHistoricalAndFuture()
            }
        )

        OutlinedButton(
            onClick = onBack,
            shape = MizanDialogTokens.buttonShape,
            modifier = Modifier
                .fillMaxWidth()
                .height(42.dp),
            contentPadding = PaddingValues(0.dp)
        ) {
            Text(
                text = stringResource(R.string.currency_settings_dialog_cancel),
                fontSize = 12.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun CompactRevalueChoiceCard(
    title: String,
    badge: String?,
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
        shape = RoundedCornerShape(13.dp),
        color = containerColor,
        border = BorderStroke(1.dp, borderColor)
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
                    .size(34.dp)
                    .clip(RoundedCornerShape(9.dp))
                    .background(iconTint.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(18.dp)
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = title,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (!badge.isNullOrBlank()) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = iconTint.copy(alpha = 0.16f)
                        ) {
                            Text(
                                text = badge,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = iconTint
                            )
                        }
                    }
                }
                Text(
                    text = description,
                    fontSize = 10.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 15.sp
                )
            }
        }
    }
}
