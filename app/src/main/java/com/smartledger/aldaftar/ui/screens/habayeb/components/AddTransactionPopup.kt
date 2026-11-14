package com.smartledger.aldaftar.ui.screens.habayeb.components

import android.widget.Toast
import kotlinx.coroutines.launch
import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import com.smartledger.aldaftar.ui.theme.mizanColors
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.focus.focusProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.smartledger.aldaftar.R
import com.smartledger.aldaftar.ui.theme.CairoFontFamily
import com.smartledger.aldaftar.ui.theme.MizanDialogTokens
import com.smartledger.aldaftar.data.local.entities.HabayebCustomer
import com.smartledger.aldaftar.data.local.entities.HabayebTransaction
import com.smartledger.aldaftar.domain.model.FinancialPolicy
import com.smartledger.aldaftar.domain.model.TransactionType
import com.smartledger.aldaftar.ui.helper.HabayebMathHelper
import com.smartledger.aldaftar.ui.screens.CalculatorDialog
import com.smartledger.aldaftar.ui.screens.habayeb.utils.CurrencyConfig
import com.smartledger.aldaftar.ui.screens.habayeb.utils.ExchangeRateHelper
import com.smartledger.aldaftar.ui.viewmodel.HabayebFinanceViewModel
import java.math.BigDecimal

private const val INITIAL_EMPTY_TEXT = ""

@Composable
fun AddTransactionPopup(
    customer: HabayebCustomer,
    viewModel: HabayebFinanceViewModel,
    initialSelectedType: String = TransactionType.OWED_BY_THEM.value,
    editingTransaction: HabayebTransaction? = null,
    onDismiss: () -> Unit,
    onTransactionSaved: () -> Unit = {},
    activeThemeColor: Color,
    activeSubColor: Color
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    val customersUiState by viewModel.customersUiState.collectAsStateWithLifecycle()
    val customerState = remember(customersUiState.customers, customer.id) {
        customersUiState.customers.find { it.id == customer.id }
    }
    val netDebt = customerState?.netDebt ?: java.math.BigDecimal.ZERO

    val settings by viewModel.settingsState.collectAsStateWithLifecycle()
    val currencySymbol = settings.currencySymbol
    val initialCurrencyAndDesc = remember(editingTransaction) {
        if (editingTransaction != null) {
            CurrencyConfig.parseTransactionCurrency(editingTransaction.description, currencySymbol)
        } else {
            Pair(currencySymbol, INITIAL_EMPTY_TEXT)
        }
    }

    val historicalTransactionCurrency = editingTransaction?.let {
        it.currencyCode.takeIf { code -> code.isNotBlank() && code != "DEFAULT" }
            ?: it.baseCurrencyCode.takeIf { code -> code.isNotBlank() && code != "DEFAULT" }
    }
    var selectedTransactionCurrency by rememberSaveable {
        mutableStateOf(historicalTransactionCurrency ?: initialCurrencyAndDesc.first)
    }
    val historicalBaseCurrency = editingTransaction?.baseCurrencyCode?.takeIf { it.isNotBlank() && it != "DEFAULT" }
    val editingOriginalCurrency = historicalTransactionCurrency
    val isCalculatedOnOriginal = editingTransaction != null &&
        editingTransaction.isRateCalculated &&
        selectedTransactionCurrency == editingOriginalCurrency
    val rateBaseCurrency = if (isCalculatedOnOriginal) {
        historicalBaseCurrency ?: currencySymbol
    } else {
        currencySymbol
    }

    val isForeignSelected = (selectedTransactionCurrency != currencySymbol) || isCalculatedOnOriginal
    var applyExchangeRate by rememberSaveable { mutableStateOf(editingTransaction?.isRateCalculated ?: false) }
    var manualEditedRateStr by rememberSaveable(editingTransaction?.id, selectedTransactionCurrency) {
        mutableStateOf<String?>(null)
    }

    val currentRateVal = ExchangeRateHelper.getRate(settings.exchangeRatesJson, selectedTransactionCurrency, rateBaseCurrency)
    val settingsRate = currentRateVal
    val manualOverrideRate = manualEditedRateStr?.toBigDecimalOrNull()?.takeIf { it > BigDecimal.ZERO }

    val effectiveRateBd = remember(editingTransaction, selectedTransactionCurrency, settingsRate, manualOverrideRate) {
        when {
            manualOverrideRate != null -> manualOverrideRate
            editingTransaction != null && editingTransaction.currencyCode == selectedTransactionCurrency && editingTransaction.exchangeRate.compareTo(BigDecimal.ZERO) > 0 ->
                editingTransaction.exchangeRate
            else -> settingsRate
        }
    }

    val initialAmountText = remember(editingTransaction) {
        editingTransaction?.let {
            val source = if (it.foreignAmount.compareTo(BigDecimal.ZERO) > 0) it.foreignAmount else it.amount
            val orig = source.stripTrailingZeros()
            if (orig.compareTo(BigDecimal.ZERO) == 0) "0"
            else if (orig.scale() <= 0) orig.toBigInteger().toString()
            else orig.toPlainString()
        } ?: INITIAL_EMPTY_TEXT
    }
    val initialDescText = remember(editingTransaction) {
        if (editingTransaction != null) initialCurrencyAndDesc.second else INITIAL_EMPTY_TEXT
    }

    var amountTfv by rememberSaveable(editingTransaction?.id, stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(text = initialAmountText, selection = TextRange(initialAmountText.length)))
    }
    var descTfv by rememberSaveable(editingTransaction?.id, stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(text = initialDescText, selection = TextRange(initialDescText.length)))
    }

    val amountStr = amountTfv.text
    val descStr = descTfv.text
    val isLendOperationSelected = TransactionType.fromValue(customer.initialType) == TransactionType.OWED_BY_THEM || customer.initialType == TransactionType.OWED_BY_THEM.value
    var selectedType by rememberSaveable { mutableStateOf(editingTransaction?.type ?: initialSelectedType) }

    // Focus requester for amount input
    val amountFocusRequester = remember { FocusRequester() }
    val descFocusRequester = remember { FocusRequester() }
    val softwareKeyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    val onSanitizedAmountChange: (TextFieldValue) -> Unit = { newTfv ->
        val raw = newTfv.text
        val cleanedText = HabayebMathHelper.sanitizeAmountInput(raw)
        if (cleanedText != null) {
            val rawEnd = newTfv.selection.end.coerceIn(0, raw.length)
            val prefixCleaned = HabayebMathHelper.normalizeAmountDigits(raw.substring(0, rawEnd))
            val newCursor = prefixCleaned.length.coerceIn(0, cleanedText.length)
            amountTfv = TextFieldValue(text = cleanedText, selection = TextRange(newCursor))
        }
    }

    var dateMillis by rememberSaveable { mutableStateOf(editingTransaction?.timestamp?.let { it * 1000 } ?: System.currentTimeMillis()) }
    var showCustomDatePicker by remember { mutableStateOf(false) }

    if (showCustomDatePicker) {
        CustomDateTimePickerDialog(
            initialMillis = dateMillis,
            onDismiss = { showCustomDatePicker = false },
            onDateTimeSelected = { millis ->
                dateMillis = millis
                showCustomDatePicker = false
            }
        )
    }
    var showCalculator by rememberSaveable { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }
    var showRateSetupOverlay by rememberSaveable { mutableStateOf(false) }
    var tempRateStr by rememberSaveable { mutableStateOf(INITIAL_EMPTY_TEXT) }

    val mizanColors = MaterialTheme.mizanColors
    val debtRedColor = mizanColors.debt
    val creditGreenColor = mizanColors.credit

    val scope = rememberCoroutineScope()
    val executeSave = { finalActionType: String ->
        if (!isSaving) {
            isSaving = true
            focusManager.clearFocus()
            softwareKeyboardController?.hide()

            val cleanAmountStr = HabayebMathHelper.normalizeAmountDigits(amountStr).trim()
            val rawAmountBd = CurrencyConfig.parseBigDecimalOrNull(cleanAmountStr)
            val amountBd = rawAmountBd?.let { FinancialPolicy.normalize(it) }
            val hasStoredRate = ExchangeRateHelper.hasRate(settings.exchangeRatesJson, selectedTransactionCurrency, rateBaseCurrency) ||
                effectiveRateBd.compareTo(BigDecimal.ZERO) > 0
            if (amountBd == null) {
                Toast.makeText(context, context.getString(R.string.habayeb_toast_valid_amount), Toast.LENGTH_SHORT).show()
                isSaving = false
            } else if (isForeignSelected && applyExchangeRate && (!hasStoredRate)) {
                tempRateStr = INITIAL_EMPTY_TEXT
                showRateSetupOverlay = true
                isSaving = false
            } else if (amountBd.compareTo(BigDecimal.ZERO) <= 0 && descStr.trim().isBlank()) {
                Toast.makeText(context, context.getString(R.string.habayeb_toast_amount_or_details_required), Toast.LENGTH_SHORT).show()
                isSaving = false
            } else if (amountBd.compareTo(BigDecimal.ZERO) < 0) {
                Toast.makeText(context, context.getString(R.string.habayeb_toast_valid_amount), Toast.LENGTH_SHORT).show()
                isSaving = false
            } else {
                val finalEquivalentAmountBd = if (isForeignSelected && applyExchangeRate) {
                    CurrencyConfig.convertDirectedAmount(amountBd, selectedTransactionCurrency, rateBaseCurrency, effectiveRateBd, selectedTransactionCurrency, rateBaseCurrency)
                } else {
                    BigDecimal.ZERO
                }
                val saveAmountBd = if (isForeignSelected && applyExchangeRate) finalEquivalentAmountBd else amountBd
                val saveCurrencyCode = selectedTransactionCurrency
                val saveDescStr = CurrencyConfig.formatDescriptionWithCurrency(descStr.trim(), selectedTransactionCurrency)
                val saveTimestamp = dateMillis / 1000
                val saveEditingTxId = editingTransaction?.id

                // Provide instant feedback and dismiss dialog safely
                haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                onTransactionSaved()
                onDismiss()

                // Execute on viewModelScope to ensure persistence survives dialog dismissal
                viewModel.submitHabayebTransaction(
                    customerId = customer.id,
                    type = finalActionType,
                    amount = saveAmountBd,
                    desc = saveDescStr,
                    timestamp = saveTimestamp,
                    editingTxId = saveEditingTxId,
                    isForeign = isForeignSelected,
                    currencyCode = saveCurrencyCode,
                    foreignAmount = amountBd,
                    exchangeRate = if (applyExchangeRate) effectiveRateBd else BigDecimal.ZERO,
                    isRateCalculated = isForeignSelected && applyExchangeRate,
                    equivalentAmount = finalEquivalentAmountBd,
                    baseCurrencySymbol = rateBaseCurrency
                )
            }
        }
    }

    val handleActionClick = { type: String ->
        if (!isSaving) {
            val cleanAmountStr = HabayebMathHelper.normalizeAmountDigits(amountStr).trim()
            val amountBd = CurrencyConfig.parseBigDecimalOrNull(cleanAmountStr)?.let { FinancialPolicy.normalize(it) }
            if (amountBd == null) {
                Toast.makeText(context, context.getString(R.string.habayeb_toast_valid_amount), Toast.LENGTH_SHORT).show()
            } else if (amountBd <= BigDecimal.ZERO && descStr.trim().isBlank()) {
                Toast.makeText(context, context.getString(R.string.add_transaction_error_empty), Toast.LENGTH_SHORT).show()
            } else if (amountBd < BigDecimal.ZERO) {
                Toast.makeText(context, context.getString(R.string.habayeb_toast_valid_amount), Toast.LENGTH_SHORT).show()
            } else {
                focusManager.clearFocus()
                softwareKeyboardController?.hide()
                executeSave(type)
            }
        }
    }

    com.smartledger.aldaftar.ui.components.MizanAnimatedDialog(
        onDismissRequest = onDismiss
    ) { dismissDialog ->
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .imePadding()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                Crossfade(
                    targetState = showRateSetupOverlay,
                    label = "FormTransition",
                    animationSpec = androidx.compose.animation.core.tween(
                        durationMillis = com.smartledger.aldaftar.ui.theme.MizanAnimationTokens.DURATION_CROSSFADE,
                        easing = com.smartledger.aldaftar.ui.theme.MizanAnimationTokens.easeInOut
                    )
                ) { isSetup ->
                    if (isSetup) {
                        BackHandler {
                            showRateSetupOverlay = false
                            applyExchangeRate = false
                        }
                        ExchangeRateSetupContent(
                            selectedCurrency = selectedTransactionCurrency,
                            rateTargetCurrency = rateBaseCurrency,
                            initialRateStr = tempRateStr,
                            activeThemeColor = activeThemeColor,
                            onDismiss = {
                                showRateSetupOverlay = false
                                applyExchangeRate = false
                            },
                            onConfirm = { newRate ->
                                manualEditedRateStr = FinancialPolicy.normalizeRate(newRate).toPlainString()
                                val newSettings = settings.copy(
                                    exchangeRatesJson = ExchangeRateHelper.setRate(settings.exchangeRatesJson, selectedTransactionCurrency, rateBaseCurrency, newRate)
                                )
                                viewModel.saveSettings(newSettings)
                                applyExchangeRate = true
                                showRateSetupOverlay = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        Column(
                            modifier = Modifier
                                .padding(8.dp)
                                .verticalScroll(rememberScrollState()),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 4.dp, vertical = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = if (editingTransaction != null) stringResource(id = R.string.add_transaction_title_edit) else stringResource(id = R.string.add_transaction_title_new),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontFamily = CairoFontFamily,
                                        color = activeThemeColor
                                    )
                                    Text(
                                        text = stringResource(id = R.string.add_transaction_account_label, "${customer.name.take(15)}${if (customer.name.length > 15) ".." else ""}"),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = CairoFontFamily,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                IconButton(
                                    onClick = onDismiss,
                                    modifier = Modifier
                                        .align(Alignment.CenterStart)
                                        .size(36.dp)
                                        .focusProperties { canFocus = false }
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = stringResource(id = R.string.habayeb_go_back),
                                        tint = activeThemeColor,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            val dynamicThemeColor = if (isLendOperationSelected) debtRedColor else creditGreenColor

                            Spacer(modifier = Modifier.height(4.dp))

                            AddTransactionFormFields(
                                amountTfv = amountTfv,
                                onAmountChange = onSanitizedAmountChange,
                                descTfv = descTfv,
                                onDescChange = { descTfv = it },
                                selectedTransactionCurrency = selectedTransactionCurrency,
                                dateMillis = dateMillis,
                                dynamicThemeColor = dynamicThemeColor,
                                amountFocusRequester = amountFocusRequester,
                                descFocusRequester = descFocusRequester,
                                onOpenCalculator = { showCalculator = true },
                                onOpenDatePicker = { showCustomDatePicker = true },
                                onDone = {
                                    focusManager.clearFocus()
                                    softwareKeyboardController?.hide()
                                }
                            )

                            Spacer(modifier = Modifier.height(4.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 1.dp, modifier = Modifier.padding(horizontal = 16.dp))
                            Spacer(modifier = Modifier.height(4.dp))

                            TransactionCurrencySelector(
                                selectedTransactionCurrency = selectedTransactionCurrency,
                                currencySymbol = currencySymbol,
                                dynamicThemeColor = dynamicThemeColor,
                                activeThemeColor = activeThemeColor,
                                applyExchangeRate = applyExchangeRate,
                                exchangeRatesJson = settings.exchangeRatesJson,
                                editingTransaction = editingTransaction,
                                haptic = haptic,
                                onCurrencySelected = { sym ->
                                    selectedTransactionCurrency = sym
                                    applyExchangeRate = false
                                    manualEditedRateStr = null
                                },
                                onApplyExchangeRateChange = { applyExchangeRate = it },
                                onSetupRateClick = { rate ->
                                    tempRateStr = rate
                                    showRateSetupOverlay = true
                                },
                                manualOverrideRate = manualOverrideRate
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    enabled = !isSaving,
                                    onClick = { handleActionClick(if (isLendOperationSelected) TransactionType.OWED_BY_THEM.value else TransactionType.OWED_TO_THEM.value) },
                                    colors = ButtonDefaults.buttonColors(containerColor = debtRedColor, contentColor = mizanColors.onDebt),
                                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f).height(42.dp)
                                ) {
                                    Text(
                                        text = if (isLendOperationSelected) stringResource(id = R.string.tx_action_debt_on_him) else stringResource(id = R.string.tx_action_debt_to_him),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = CairoFontFamily,
                                        color = mizanColors.onDebt
                                    )
                                }

                                Button(
                                    enabled = !isSaving,
                                    onClick = { handleActionClick(if (isLendOperationSelected) TransactionType.PAYMENT_BY_THEM.value else TransactionType.PAYMENT_TO_THEM.value) },
                                    colors = ButtonDefaults.buttonColors(containerColor = creditGreenColor, contentColor = mizanColors.onCredit),
                                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
                                    shape = MizanDialogTokens.buttonShape,
                                    modifier = Modifier.weight(1f).height(MizanDialogTokens.buttonHeight)
                                ) {
                                    Text(
                                        text = if (isLendOperationSelected) stringResource(id = R.string.btn_receive) else stringResource(id = R.string.btn_pay),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = CairoFontFamily,
                                        color = mizanColors.onCredit
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCalculator) {
        CalculatorDialog(
            onDismiss = { showCalculator = false },
            onValueConfirmed = { value ->
                val normalized = FinancialPolicy.normalize(value).stripTrailingZeros()
                val calcStr = if (normalized.compareTo(BigDecimal.ZERO) == 0) {
                    "0"
                } else if (normalized.scale() <= 0) {
                    normalized.toBigInteger().toString()
                } else {
                    normalized.toPlainString()
                }
                amountTfv = TextFieldValue(text = calcStr, selection = TextRange(calcStr.length))
                showCalculator = false
            },
            activeThemeColor = activeThemeColor,
            activeSubColor = activeSubColor
        )
    }
}
