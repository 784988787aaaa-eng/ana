package com.smartledger.aldaftar.ui.screens.ledger.components

import java.math.BigDecimal
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartledger.aldaftar.R
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import com.smartledger.aldaftar.data.local.entities.TransactionDb
import com.smartledger.aldaftar.ui.screens.CalculatorDialog
import com.smartledger.aldaftar.ui.screens.habayeb.utils.CurrencyConfig
import com.smartledger.aldaftar.ui.theme.mizanColors
import com.smartledger.aldaftar.ui.theme.MizanDialogTokens
import com.smartledger.aldaftar.ui.components.requestFocusAndShowKeyboard

@Composable
fun TransactionRecordDialog(
    showTxDialog: Boolean,
    txDialogType: String,
    editingTransaction: TransactionDb?,
    currencySymbol: String,
    onDismiss: () -> Unit,
    onSave: (id: String?, type: String, category: String, amount: BigDecimal, description: String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (!showTxDialog) return

    val mizanColors = MaterialTheme.mizanColors
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val initialAmount = remember(editingTransaction, showTxDialog) { editingTransaction?.amount?.toPlainString() ?: "" }
    val initialDesc = remember(editingTransaction, showTxDialog) { editingTransaction?.description ?: "" }

    var numAmountTfv by rememberSaveable(editingTransaction?.id, showTxDialog, stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(text = initialAmount, selection = TextRange(initialAmount.length)))
    }
    var descriptionTfv by rememberSaveable(editingTransaction?.id, showTxDialog, stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(text = initialDesc, selection = TextRange(initialDesc.length)))
    }
    val numAmount = numAmountTfv.text
    val descriptionStr = descriptionTfv.text

    val categoryName = remember(editingTransaction, txDialogType) {
        editingTransaction?.category ?: if (txDialogType == "INCOME") context.getString(R.string.ledger_category_overall_income) else context.getString(R.string.ledger_category_expense)
    }

    var showCalcPopup by rememberSaveable { mutableStateOf(false) }
    var isSavingTx by remember(showTxDialog) { mutableStateOf(false) }

    val parsedAmount = remember(numAmount) {
        val norm = CurrencyConfig.normalizeDigits(numAmount)
        try { BigDecimal(norm.trim()) } catch (_: Exception) { BigDecimal.ZERO }
    }
    val isConfirmButtonEnabled = !isSavingTx && parsedAmount.compareTo(BigDecimal.ZERO) > 0

    val focusRequester = remember { FocusRequester() }
    val descriptionFocusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val softwareKeyboardController = LocalSoftwareKeyboardController.current

    val view = androidx.compose.ui.platform.LocalView.current
    DisposableEffect(view) {
        val window = (view.parent as? androidx.compose.ui.window.DialogWindowProvider)?.window
        window?.setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE or android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        onDispose {}
    }

    LaunchedEffect(Unit) {
        try {
            requestFocusAndShowKeyboard(
                focusRequester = focusRequester,
                keyboardController = softwareKeyboardController
            )
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
        }
    }

    val isIncome = txDialogType == "INCOME"

    val themeColor = if (isIncome) {
        mizanColors.credit
    } else {
        mizanColors.debt
    }
    val themeColorSub = themeColor.copy(alpha = 0.85f)

    val dialogBgColor = MaterialTheme.colorScheme.surface
    val textInputBgColor = if (isIncome) mizanColors.creditContainer.copy(alpha = 0.35f) else mizanColors.debtContainer.copy(alpha = 0.35f)
    val textColor = MaterialTheme.colorScheme.onSurface

    val onSanitizedAmountChange: (TextFieldValue) -> Unit = { newTfv ->
        val raw = newTfv.text
        if (raw.isEmpty()) {
            numAmountTfv = newTfv
        } else {
            val normalized = CurrencyConfig.normalizeDigits(raw).replace(" ", "")
            val dotCount = normalized.count { it == '.' }
            val isValidChars = normalized.all { it.isDigit() || it == '.' }
            val dotIdx = normalized.indexOf('.')
            val validDecimals = dotIdx == -1 || (normalized.length - dotIdx - 1 <= 2)
            if (isValidChars && dotCount <= 1 && validDecimals) {
                val cleanedText = if (normalized.startsWith("0") && normalized.length > 1 && normalized[1] != '.') {
                    normalized.trimStart('0').ifEmpty { "0" }
                } else if (normalized.startsWith(".")) {
                    "0$normalized"
                } else {
                    normalized
                }
                val diff = cleanedText.length - raw.length
                val newCursor = (newTfv.selection.end + diff).coerceIn(0, cleanedText.length)
                numAmountTfv = TextFieldValue(text = cleanedText, selection = TextRange(newCursor))
            }
        }
    }

    com.smartledger.aldaftar.ui.components.MizanAnimatedDialog(
        onDismissRequest = onDismiss
    ) { dismissDialog ->
        Surface(
            shape = MizanDialogTokens.shape,
            color = dialogBgColor,
            tonalElevation = 0.dp, // تعطيل الارتفاع اللوني. to prevent neutral gray overlays
            border = BorderStroke(1.dp, themeColor.copy(alpha = 0.7f)),
            modifier = Modifier
                .widthIn(max = 350.dp)
                .fillMaxWidth(0.92f)
                .wrapContentHeight()
                .imePadding()
        ) {
            Column(
                modifier = Modifier
                    .padding(MizanDialogTokens.outerPadding)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp)
                ) {
                    Text(
                        text = if (editingTransaction != null) stringResource(id = R.string.ledger_edit_transaction_title) else if (isIncome) stringResource(id = R.string.ledger_add_income_title) else stringResource(id = R.string.ledger_add_expense_title),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = themeColor,
                            fontSize = 14.5.sp
                        ),
                        textAlign = TextAlign.Center
                    )
                }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = numAmountTfv,
                        onValueChange = onSanitizedAmountChange,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { descriptionFocusRequester.requestFocus() }
                        ),
                        label = {
                            Text(
                                text = stringResource(id = R.string.ledger_amount_label, currencySymbol),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        singleLine = true,
                        leadingIcon = {
                            IconButton(
                                onClick = { showCalcPopup = true },
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Calculate,
                                    contentDescription = stringResource(id = R.string.habayeb_calculator),
                                    tint = themeColor,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester),
                        shape = MizanDialogTokens.inputShape,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = textColor,
                            unfocusedTextColor = textColor,
                            focusedContainerColor = textInputBgColor,
                            unfocusedContainerColor = textInputBgColor,
                            focusedBorderColor = themeColor,
                            unfocusedBorderColor = themeColor.copy(alpha = 0.6f),
                            focusedLabelColor = themeColor,
                            unfocusedLabelColor = themeColorSub
                        ),
                        textStyle = LocalTextStyle.current.copy(
                            color = textColor,
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    )

                    OutlinedTextField(
                        value = descriptionTfv,
                        onValueChange = { descriptionTfv = it },
                        label = {
                            Text(
                                text = stringResource(id = if (isIncome) R.string.ledger_desc_income_hint else R.string.ledger_desc_expense_hint),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        singleLine = true,
                        maxLines = 1,
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(descriptionFocusRequester),
                        shape = MizanDialogTokens.inputShape,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = textColor,
                            unfocusedTextColor = textColor,
                            focusedContainerColor = textInputBgColor,
                            unfocusedContainerColor = textInputBgColor,
                            focusedBorderColor = themeColor,
                            unfocusedBorderColor = themeColor.copy(alpha = 0.6f),
                            focusedLabelColor = themeColor,
                            unfocusedLabelColor = themeColorSub
                        ),
                        textStyle = LocalTextStyle.current.copy(
                            color = textColor,
                            textAlign = TextAlign.Start,
                            fontSize = 13.sp
                        ),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                softwareKeyboardController?.hide()
                                focusManager.clearFocus()
                            }
                        )
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = dismissDialog,
                        modifier = Modifier
                            .weight(1f)
                            .height(MizanDialogTokens.buttonHeight),
                        shape = MizanDialogTokens.buttonShape,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = themeColorSub
                        )
                    ) {
                        Text(
                            text = stringResource(id = R.string.common_cancel),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            maxLines = 1
                        )
                    }

                    Button(
                        enabled = isConfirmButtonEnabled,
                        onClick = {
                            if (isSavingTx) return@Button
                            if (parsedAmount.compareTo(BigDecimal.ZERO) > 0) {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                isSavingTx = true
                                onSave(
                                    editingTransaction?.id,
                                    txDialogType,
                                    categoryName,
                                    parsedAmount,
                                    descriptionStr
                                )
                                onDismiss()
                            }
                        },
                        modifier = Modifier
                            .weight(1.2f)
                            .height(MizanDialogTokens.buttonHeight),
                        shape = MizanDialogTokens.buttonShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = themeColor,
                            contentColor = if (isIncome) mizanColors.onCredit else mizanColors.onDebt,
                            disabledContainerColor = themeColor.copy(alpha = 0.35f),
                            disabledContentColor = mizanColors.contentDisabled
                        )
                    ) {
                        Text(
                            text = stringResource(id = R.string.ledger_save_tx_btn),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }

    if (showCalcPopup) {
        CalculatorDialog(
            onDismiss = { showCalcPopup = false },
            onValueConfirmed = { calcResult ->
                val resultStr = if (calcResult.remainder(java.math.BigDecimal.ONE).compareTo(java.math.BigDecimal.ZERO) == 0) {
                    calcResult.toBigInteger().toString()
                } else {
                    calcResult.stripTrailingZeros().toPlainString()
                }
                numAmountTfv = TextFieldValue(text = resultStr, selection = TextRange(resultStr.length))
                showCalcPopup = false
            },
            activeThemeColor = themeColor,
            activeSubColor = themeColor.copy(alpha = 0.15f)
        )
    }
}
