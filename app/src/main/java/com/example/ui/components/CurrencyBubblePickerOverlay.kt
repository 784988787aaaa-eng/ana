package com.example.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.res.stringResource
import com.example.R
import com.example.data.local.entities.AppSettings
import com.example.ui.screens.habayeb.utils.CurrencyConfig

sealed interface CurrencyDialogState {
    object None : CurrencyDialogState
    data class RevalueConfirm(val targetCurrency: String, val newRate: Double) : CurrencyDialogState
}

@Composable
fun CurrencySettingsDialog(
    settings: AppSettings,
    onSaveSettings: (AppSettings, String, Double, Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    
    var localDefaultCurrency by remember { mutableStateOf(settings.currencySymbol) }
    
    // Hold local rates json
    var localExchangeRatesJson by remember { mutableStateOf(settings.exchangeRatesJson) }

    val currencyYer = stringResource(id = R.string.currency_yer)
    val currencySar = stringResource(id = R.string.currency_sar)
    val currencyUsd = stringResource(id = R.string.currency_usd)
    val currenciesToDisplay = listOf(currencyYer, currencySar, currencyUsd)

    // Select which target currency to configure
    var selectedTargetCurrency by remember(localDefaultCurrency) {
        mutableStateOf(
            if (localDefaultCurrency == currencyYer) currencyUsd else currencyYer
        )
    }

    // Determine current rate being configured
    val currentRateValue = com.example.ui.screens.habayeb.utils.ExchangeRateHelper.getRate(localExchangeRatesJson, localDefaultCurrency, selectedTargetCurrency)

    var rateInputStr by remember(localDefaultCurrency, selectedTargetCurrency) {
        mutableStateOf(if (currentRateValue > 0.0 && currentRateValue != 1.0) com.example.ui.helper.HabayebMathHelper.formatRate(currentRateValue) else "")
    }

    val rateFocusRequester = remember { FocusRequester() }

    // State for revaluation confirmation dialog
    var activeDialogState by remember { mutableStateOf<CurrencyDialogState>(CurrencyDialogState.None) }

    val keyboardController = androidx.compose.ui.platform.LocalSoftwareKeyboardController.current

    // Auto-focus on exchange rate field upon entering
    LaunchedEffect(localDefaultCurrency, selectedTargetCurrency) {
        try {
            kotlinx.coroutines.android.awaitFrame()
            rateFocusRequester.requestFocus()
            keyboardController?.show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        val view = androidx.compose.ui.platform.LocalView.current
        DisposableEffect(view) {
            val window = (view.parent as? androidx.compose.ui.window.DialogWindowProvider)?.window
            window?.setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
            onDispose {}
        }
        Card(
            modifier = Modifier
                .width(280.dp) // Perfect mid-width to host side-by-side contents gracefully
                .padding(4.dp)
                .imePadding()
                .animateContentSize(animationSpec = tween(200)),
            // Highly creative, modern asymmetrical rounded leaf/petal shape
            shape = RoundedCornerShape(
                topStart = 28.dp,
                bottomEnd = 28.dp,
                topEnd = 6.dp,
                bottomStart = 6.dp
            ),
            border = BorderStroke(1.2.dp, MaterialTheme.colorScheme.outlineVariant),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Centered Mini Title & Close Trigger
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.currency_settings_dialog_title),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )
                    
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .size(18.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(R.string.currency_settings_dialog_close),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 1.dp)

                // Side-by-Side configuration Row (No flag graphics, 100% text-based pure aesthetic)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Left Column: Default main App Currency
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.currency_settings_dialog_default),
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                                .padding(2.dp),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            currenciesToDisplay.forEach { symbol ->
                                val isSelected = localDefaultCurrency == symbol
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(22.dp)
                                        .clip(RoundedCornerShape(topStart = 6.dp, bottomEnd = 6.dp, topEnd = 2.dp, bottomStart = 2.dp))
                                        .background(if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else Color.Transparent)
                                        .clickable {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            val oldDefault = localDefaultCurrency
                                            localDefaultCurrency = symbol
                                            if (selectedTargetCurrency == symbol) {
                                                selectedTargetCurrency = if (symbol == currencyYer) currencyUsd else currencyYer
                                            }
                                            localExchangeRatesJson = com.example.ui.screens.habayeb.utils.ExchangeRateHelper.migrateRates(
                                                localExchangeRatesJson,
                                                oldDefault,
                                                symbol
                                            )
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = symbol,
                                        fontSize = 9.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    // Right Column: Target exchange rates
                    Column(
                        modifier = Modifier.weight(1.3f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.currency_settings_dialog_target),
                            fontSize = 8.sp,
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
                                .clip(RoundedCornerShape(6.dp))
                                .background(MaterialTheme.colorScheme.outlineVariant)
                                .padding(1.5.dp),
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            availableTargets.forEach { symbol ->
                                val isSelected = selectedTargetCurrency == symbol
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(20.dp)
                                        .clip(RoundedCornerShape(topStart = 5.dp, bottomEnd = 5.dp, topEnd = 1.5.dp, bottomStart = 1.5.dp))
                                        .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                                        .clickable {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            selectedTargetCurrency = symbol
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = symbol,
                                        fontSize = 8.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        // Ultra-compact custom zero-padding equation input field
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(24.dp)
                                .clip(RoundedCornerShape(topStart = 6.dp, bottomEnd = 6.dp, topEnd = 2.dp, bottomStart = 2.dp))
                                .background(MaterialTheme.colorScheme.surface)
                                .border(0.8.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(topStart = 6.dp, bottomEnd = 6.dp, topEnd = 2.dp, bottomStart = 2.dp))
                                .padding(horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "1 $selectedTargetCurrency =",
                                fontSize = 7.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 2.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                BasicTextField(
                                    value = rateInputStr,
                                    onValueChange = { newVal ->
                                        val cleaned = CurrencyConfig.normalizeDigits(newVal)
                                        rateInputStr = cleaned
                                        val parsed = cleaned.toDoubleOrNull() ?: 1.0
                                        localExchangeRatesJson = com.example.ui.screens.habayeb.utils.ExchangeRateHelper.setRate(localExchangeRatesJson, localDefaultCurrency, selectedTargetCurrency, parsed)
                                    },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    textStyle = androidx.compose.ui.text.TextStyle(
                                        textAlign = TextAlign.Center,
                                        fontSize = 9.sp,
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
                                                    fontSize = 8.sp,
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
                                fontSize = 7.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                // Centered action buttons with creative matched edges
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Button(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            val finalRate = rateInputStr.trim().toDoubleOrNull() ?: currentRateValue
                            if (finalRate > 0.0) {
                                val migratedOriginalJson = com.example.ui.screens.habayeb.utils.ExchangeRateHelper.migrateRates(
                                    settings.exchangeRatesJson,
                                    settings.currencySymbol,
                                    localDefaultCurrency
                                )
                                val alreadyHasRate = com.example.ui.screens.habayeb.utils.ExchangeRateHelper.hasRate(
                                    migratedOriginalJson,
                                    localDefaultCurrency,
                                    selectedTargetCurrency
                                )
                                val existingRate = com.example.ui.screens.habayeb.utils.ExchangeRateHelper.getRate(
                                    migratedOriginalJson,
                                    localDefaultCurrency,
                                    selectedTargetCurrency
                                )
                                val oldRateBD = java.math.BigDecimal.valueOf(existingRate)
                                val newRateBD = java.math.BigDecimal.valueOf(finalRate)
                                val rateChanged = existingRate > 0.0 && oldRateBD.compareTo(newRateBD) != 0

                                if (alreadyHasRate && rateChanged) {
                                    activeDialogState = CurrencyDialogState.RevalueConfirm(selectedTargetCurrency, finalRate)
                                } else {
                                    val updatedExchangeRatesJson = com.example.ui.screens.habayeb.utils.ExchangeRateHelper.setRate(
                                        localExchangeRatesJson,
                                        localDefaultCurrency,
                                        selectedTargetCurrency,
                                        finalRate
                                    )
                                    val updatedSettings = settings.copy(
                                        currencySymbol = localDefaultCurrency,
                                        exchangeRatesJson = updatedExchangeRatesJson
                                    )
                                    onSaveSettings(updatedSettings, selectedTargetCurrency, finalRate, false)
                                    onDismiss()
                                }
                            } else {
                                // Save standard settings if no valid rate is edited
                                val updatedSettings = settings.copy(
                                    currencySymbol = localDefaultCurrency,
                                    exchangeRatesJson = localExchangeRatesJson
                                )
                                onSaveSettings(updatedSettings, "", 0.0, false)
                                onDismiss()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(topStart = 8.dp, bottomEnd = 8.dp, topEnd = 2.dp, bottomStart = 2.dp),
                        modifier = Modifier
                            .weight(1.3f)
                            .height(24.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.currency_settings_dialog_save),
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }

                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.outlineVariant,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        shape = RoundedCornerShape(topStart = 8.dp, bottomEnd = 8.dp, topEnd = 2.dp, bottomStart = 2.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(24.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.currency_settings_dialog_cancel),
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }

    val revalueState = activeDialogState as? CurrencyDialogState.RevalueConfirm
    if (revalueState != null) {
        val targetCurrency = revalueState.targetCurrency
        val newRate = revalueState.newRate

        Dialog(
            onDismissRequest = { 
                activeDialogState = CurrencyDialogState.None
            },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Card(
                modifier = Modifier
                    .width(280.dp)
                    .padding(6.dp)
                    .animateContentSize(animationSpec = tween(200)),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh, 
                            contentDescription = null, 
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = stringResource(id = R.string.currency_update_rate_title), 
                            fontWeight = FontWeight.Bold, 
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.8.dp)

                    // Message Text
                    Text(
                        text = stringResource(id = R.string.currency_update_rate_confirm_msg, targetCurrency),
                        fontSize = 10.5.sp,
                        lineHeight = 15.sp,
                        textAlign = TextAlign.Start,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    // Buttons Row - beautifully aligned
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Button(
                            onClick = {
                                val updatedSettings = settings.copy(
                                    currencySymbol = localDefaultCurrency,
                                    exchangeRatesJson = com.example.ui.screens.habayeb.utils.ExchangeRateHelper.setRate(
                                        localExchangeRatesJson,
                                        localDefaultCurrency,
                                        targetCurrency,
                                        newRate
                                    )
                                )
                                onSaveSettings(updatedSettings, targetCurrency, newRate, true)
                                activeDialogState = CurrencyDialogState.None
                                onDismiss()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier
                                .weight(1.1f)
                                .height(28.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(
                                text = stringResource(id = R.string.currency_update_past_future),
                                color = Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Button(
                            onClick = {
                                val updatedSettings = settings.copy(
                                    currencySymbol = localDefaultCurrency,
                                    exchangeRatesJson = com.example.ui.screens.habayeb.utils.ExchangeRateHelper.setRate(
                                        localExchangeRatesJson,
                                        localDefaultCurrency,
                                        targetCurrency,
                                        newRate
                                    )
                                )
                                onSaveSettings(updatedSettings, targetCurrency, newRate, false)
                                activeDialogState = CurrencyDialogState.None
                                onDismiss()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                            ),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(28.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(
                                text = stringResource(id = R.string.currency_update_future_only),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
