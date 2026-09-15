package com.smartledger.aldaftar.ui.components

import android.util.Log
import android.view.WindowManager
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
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import com.smartledger.aldaftar.R
import com.smartledger.aldaftar.data.local.entities.AppSettings
import com.smartledger.aldaftar.ui.helper.HabayebMathHelper
import com.smartledger.aldaftar.ui.screens.habayeb.utils.CurrencyConfig
import com.smartledger.aldaftar.ui.screens.habayeb.utils.ExchangeRateHelper
import kotlinx.coroutines.android.awaitFrame
import java.math.BigDecimal
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.material.icons.filled.MonetizationOn
import com.smartledger.aldaftar.ui.theme.CairoFontFamily
import com.smartledger.aldaftar.ui.theme.UniversalDialogHeader
import com.smartledger.aldaftar.ui.theme.UniversalDialogSurface
import com.smartledger.aldaftar.ui.theme.arabicInputTextStyle
import com.smartledger.aldaftar.ui.components.RequestFocusAndShowKeyboard
import androidx.compose.foundation.layout.heightIn

private const val TAG = "CurrencySettingsDialog"

@Composable
fun CurrencySettingsDialog(
    settings: AppSettings,
    onSaveSettings: (AppSettings, String, Double, Boolean) -> Unit,
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
    val keyboardController = LocalSoftwareKeyboardController.current
    RequestFocusAndShowKeyboard(
        focusRequester = rateFocusRequester,
        key = "${state.localDefaultCurrency}:${state.selectedTargetCurrency}"
    )

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            val view = LocalView.current
            DisposableEffect(view) {
                val window = (view.parent as? DialogWindowProvider)?.window
                window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
                onDispose {}
            }
            UniversalDialogSurface(
                isExpanded = false,
                modifier = Modifier
                    .heightIn(max = 520.dp)
                    .imePadding()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    UniversalDialogHeader(
                        title = stringResource(R.string.currency_settings_dialog_title),
                        icon = Icons.Default.MonetizationOn,
                        onDismiss = onDismiss
                    )

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
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = stringResource(R.string.currency_settings_dialog_default),
                fontFamily = CairoFontFamily,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.40f))
                    .border(0.8.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.40f), RoundedCornerShape(12.dp))
                    .padding(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                currenciesToDisplay.forEach { symbol ->
                    val isSelected = localDefaultCurrency == symbol
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(34.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color.Transparent)
                            .then(
                                if (isSelected) Modifier.border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                                else Modifier
                            )
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onDefaultCurrencyChange(symbol)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = symbol,
                            fontFamily = CairoFontFamily,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        Column(
            modifier = Modifier.weight(1.35f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = stringResource(R.string.currency_settings_dialog_target),
                fontFamily = CairoFontFamily,
                fontSize = 11.5.sp,
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
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.40f))
                    .border(0.8.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.40f), RoundedCornerShape(10.dp))
                    .padding(3.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                availableTargets.forEach { symbol ->
                    val isSelected = selectedTargetCurrency == symbol
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(34.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onTargetCurrencyChange(symbol)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = symbol,
                            fontFamily = CairoFontFamily,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f), RoundedCornerShape(10.dp))
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "1 $selectedTargetCurrency =",
                    fontFamily = CairoFontFamily,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
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
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        textStyle = TextStyle(
                            textAlign = TextAlign.Center,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            fontFeatureSettings = "tnum",
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
                                        fontFamily = CairoFontFamily,
                                        fontSize = 11.5.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
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
                    fontFamily = CairoFontFamily,
                    fontSize = 12.sp,
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
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .weight(1.3f)
                .height(44.dp),
            contentPadding = PaddingValues(0.dp)
        ) {
            Text(
                text = stringResource(R.string.currency_settings_dialog_save),
                fontFamily = CairoFontFamily,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }

        OutlinedButton(
            onClick = onDismiss,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .weight(1f)
                .height(44.dp),
            contentPadding = PaddingValues(0.dp)
        ) {
            Text(
                text = stringResource(R.string.currency_settings_dialog_cancel),
                fontFamily = CairoFontFamily,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
