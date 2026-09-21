package com.smartledger.aldaftar.ui.screens.habayeb.components

import android.widget.Toast
import java.math.BigDecimal
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
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartledger.aldaftar.R
import com.smartledger.aldaftar.ui.components.MizanAnimatedDialog
import com.smartledger.aldaftar.ui.components.MizanDialogCard
import com.smartledger.aldaftar.ui.components.MizanDialogInnerCard
import com.smartledger.aldaftar.ui.components.RequestFocusAndShowKeyboard
import com.smartledger.aldaftar.ui.screens.habayeb.utils.CurrencyConfig
import com.smartledger.aldaftar.ui.theme.MizanDialogTokens
import com.smartledger.aldaftar.ui.theme.mizanColors

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
    var rateTfv by remember(initialRateStr) {
        mutableStateOf(
            androidx.compose.ui.text.input.TextFieldValue(
                text = initialRateStr,
                selection = androidx.compose.ui.text.TextRange(initialRateStr.length)
            )
        )
    }
    val rateStr = rateTfv.text
    var isChecked by remember { mutableStateOf(false) }
    var showUncheckedError by remember { mutableStateOf(false) }
    var isFocused by remember { mutableStateOf(false) }

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
            else -> activeThemeColor.copy(alpha = 0.85f)
        },
        animationSpec = tween(durationMillis = 180),
        label = "statusColor"
    )

    val validRateToastStr = stringResource(id = R.string.habayeb_toast_enter_valid_rate)
    val confirmRateFirstToastStr = stringResource(id = R.string.habayeb_toast_confirm_rate_first)

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Column(
            modifier = modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            // Conversion Display & Input in a compact styled Mizan card
            MizanDialogInnerCard(
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .border(
                            width = if (isFocused) 1.5.dp else 1.dp,
                            color = if (isFocused) activeThemeColor else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(5.dp),
                        color = activeThemeColor.copy(alpha = 0.10f)
                    ) {
                        Text(
                            text = "1 $selectedCurrency =",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = activeThemeColor,
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
                            value = rateTfv,
                            onValueChange = { inputTfv ->
                                val cleanedText = CurrencyConfig.normalizeDigits(inputTfv.text)
                                val isPartialDecimal = cleanedText.isEmpty() || cleanedText == "." || cleanedText.endsWith(".")
                                val parsed = cleanedText.toBigDecimalOrNull()
                                val decimalPlaces = cleanedText.substringAfter('.', "").length
                                if (isPartialDecimal || (parsed != null && parsed > BigDecimal.ZERO && decimalPlaces <= com.smartledger.aldaftar.domain.model.FinancialPolicy.rateScale)) {
                                    rateTfv = inputTfv.copy(text = cleanedText)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .onFocusChanged { isFocused = it.isFocused }
                                .focusRequester(focusRequester),
                            singleLine = true,
                            cursorBrush = androidx.compose.ui.graphics.SolidColor(activeThemeColor),
                            textStyle = TextStyle(
                                textAlign = TextAlign.Center,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = activeThemeColor
                            ),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
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
                                            fontSize = 11.sp,
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
                        shape = RoundedCornerShape(5.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    ) {
                        Text(
                            text = rateTargetCurrency,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            // Compact Confirmation Check Card
            Surface(
                onClick = {
                    isChecked = !isChecked
                    if (isChecked) {
                        showUncheckedError = false
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = if (isChecked) activeThemeColor.copy(alpha = 0.10f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                border = BorderStroke(
                    width = 1.dp,
                    color = if (isChecked) activeThemeColor else if (showUncheckedError) mizanColors.debt else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(18.dp)
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
                                modifier = Modifier.size(11.dp)
                            )
                        }
                    }

                    Text(
                        text = stringResource(id = R.string.habayeb_confirm_exchange_rate_question),
                        fontSize = 11.5.sp,
                        fontWeight = if (isChecked) FontWeight.Bold else FontWeight.Medium,
                        color = if (isChecked) activeThemeColor else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Text(
                text = stringResource(id = R.string.habayeb_exchange_rate_hint_text),
                fontSize = 9.5.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
                lineHeight = 12.sp
            )

            // Compact Action Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.habayeb_cancel),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Button(
                    onClick = {
                        val rateBD = try { BigDecimal(rateStr.trim()) } catch (_: Exception) { null }
                        if (rateBD == null || rateBD.compareTo(BigDecimal.ZERO) <= 0) {
                            Toast.makeText(context, validRateToastStr, Toast.LENGTH_SHORT).show()
                        } else if (!isChecked) {
                            showUncheckedError = true
                            Toast.makeText(context, confirmRateFirstToastStr, Toast.LENGTH_SHORT).show()
                        } else {
                            onConfirm(rateBD)
                        }
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = statusColor),
                    modifier = Modifier
                        .weight(1.2f)
                        .height(36.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.habayeb_save),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
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
            maxWidth = MizanDialogTokens.compactMaxWidth
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
                            .background(activeThemeColor.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.MonetizationOn,
                            contentDescription = null,
                            tint = activeThemeColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Text(
                        text = stringResource(id = R.string.currency_settings_dialog_title),
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                IconButton(
                    onClick = dismissDialog,
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
