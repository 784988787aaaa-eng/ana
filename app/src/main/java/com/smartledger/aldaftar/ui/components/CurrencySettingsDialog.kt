package com.smartledger.aldaftar.ui.components

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.ui.text.input.ImeAction
import android.util.Log
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.smartledger.aldaftar.R
import com.smartledger.aldaftar.data.local.entities.AppSettings
import com.smartledger.aldaftar.ui.helper.HabayebMathHelper
import com.smartledger.aldaftar.ui.screens.habayeb.utils.CurrencyConfig
import com.smartledger.aldaftar.ui.screens.habayeb.utils.ExchangeRateHelper
import kotlinx.coroutines.android.awaitFrame
import java.math.BigDecimal
import com.smartledger.aldaftar.ui.theme.MizanDialogTokens

private const val TAG = "CurrencySettingsDialog"

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

    Dialog(
        onDismissRequest = {
            hideKeyboardAndClearFocus(focusManager, keyboardController)
            onDismiss()
        },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .widthIn(max = MizanDialogTokens.compactMaxWidth)
                .padding(4.dp)
                .imePadding()
                .animateContentSize(animationSpec = tween(200)),
            shape = MizanDialogTokens.shape,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = MizanDialogTokens.outerPadding, vertical = MizanDialogTokens.compactPadding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                CurrencyDialogHeader(onDismiss = onDismiss)

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f), thickness = 0.8.dp)

                CurrencySelectorColumns(
                    currenciesToDisplay = state.currenciesToDisplay,
                    localDefaultCurrency = state.localDefaultCurrency,
                    selectedTargetCurrency = state.selectedTargetCurrency,
                    rateInputStr = state.rateInputStr,
                    rateFocusRequester = rateFocusRequester,
                    haptic = haptic,
                    currencyYer = currencyYer,
                    currencyUsd = currencyUsd,
                    onDefaultCurrencyChange = { newDefault -> state.onDefaultCurrencyChange(newDefault) },
                    onTargetCurrencyChange = { newTarget -> state.onTargetCurrencyChange(newTarget) },
                    onRateInputChange = { newInput -> state.onRateInputChange(newInput) }
                )

                Spacer(modifier = Modifier.height(4.dp))

                CurrencyActionButtons(
                    haptic = haptic,
                    onDismiss = onDismiss,
                    onSave = {
                        state.handleSave(
                            settings = settings,
                            onSaveSettings = onSaveSettings,
                            onDismiss = onDismiss
                        )
                    }
                )
            }
        }
    }

    val revalueState = state.activeDialogState as? CurrencyDialogState.RevalueConfirm
    if (revalueState != null) {
        val targetCurrency = revalueState.targetCurrency
        val newRate = revalueState.newRate

        CurrencyRevalueConfirmDialog(
            targetCurrency = targetCurrency,
            newRate = newRate,
            onConfirmHistoricalAndFuture = {
                state.handleConfirmHistoricalAndFuture(
                    settings = settings,
                    targetCurrency = targetCurrency,
                    newRate = newRate,
                    onSaveSettings = onSaveSettings,
                    onDismiss = onDismiss
                )
            },
            onConfirmFutureOnly = {
                state.handleConfirmFutureOnly(
                    settings = settings,
                    targetCurrency = targetCurrency,
                    newRate = newRate,
                    onSaveSettings = onSaveSettings,
                    onDismiss = onDismiss
                )
            },
            onDismiss = {
                state.activeDialogState = CurrencyDialogState.None
            }
        )
    }
}

@Composable
private fun CurrencyDialogHeader(onDismiss: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(R.string.currency_settings_dialog_title),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        
        IconButton(
            onClick = onDismiss,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .size(44.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = stringResource(R.string.currency_settings_dialog_close),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun CurrencySelectorColumns(
    currenciesToDisplay: List<String>,
    localDefaultCurrency: String,
    selectedTargetCurrency: String,
    rateInputStr: String,
    rateFocusRequester: FocusRequester,
    haptic: HapticFeedback,
    currencyYer: String,
    currencyUsd: String,
    onDefaultCurrencyChange: (String) -> Unit,
    onTargetCurrencyChange: (String) -> Unit,
    onRateInputChange: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = stringResource(R.string.currency_settings_dialog_default),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                    .padding(3.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                currenciesToDisplay.forEach { symbol ->
                    val isSelected = localDefaultCurrency == symbol
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color.Transparent)
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onDefaultCurrencyChange(symbol)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = symbol,
                            fontSize = 11.5.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        Column(
            modifier = Modifier.weight(1.3f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = stringResource(R.string.currency_settings_dialog_target),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            val availableTargets = remember(currenciesToDisplay, localDefaultCurrency) {
                currenciesToDisplay.filter { it != localDefaultCurrency }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                    .padding(2.dp),
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                availableTargets.forEach { symbol ->
                    val isSelected = selectedTargetCurrency == symbol
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(30.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onTargetCurrencyChange(symbol)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = symbol,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(id = R.string.currency_settings_dialog_unit_rate_prefix, selectedTargetCurrency),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

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
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = {
                            keyboardController?.hide()
                            focusManager.clearFocus()
                        }),
                        textStyle = TextStyle(
                            textAlign = TextAlign.Center,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(rateFocusRequester),
                        decorationBox = { innerTextField ->
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                if (rateInputStr.isEmpty()) {
                                    Text(
                                        text = stringResource(R.string.currency_settings_dialog_price),
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center
                                    )
                                }
                                innerTextField()
                            }
                        }
                    )
                }

                Text(
                    text = localDefaultCurrency,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun CurrencyActionButtons(
    haptic: HapticFeedback,
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Button(
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onSave()
            },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            shape = MizanDialogTokens.buttonShape,
            modifier = Modifier
                .weight(1.3f)
                .height(MizanDialogTokens.buttonHeight),
            contentPadding = PaddingValues(0.dp)
        ) {
            Text(
                text = stringResource(R.string.currency_settings_dialog_save),
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }

        OutlinedButton(
            onClick = onDismiss,
            shape = MizanDialogTokens.buttonShape,
            modifier = Modifier
                .weight(1f)
                .height(MizanDialogTokens.buttonHeight),
            contentPadding = PaddingValues(0.dp)
        ) {
            Text(
                text = stringResource(R.string.currency_settings_dialog_cancel),
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
