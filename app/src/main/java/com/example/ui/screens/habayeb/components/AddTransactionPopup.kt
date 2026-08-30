package com.example.ui.screens.habayeb.components

/*
 * =====================================================================================
 * حزمة نافذة إضافة وتعديل المعاملة المالية (Add/Edit Transaction Popup Package)
 * -------------------------------------------------------------------------------------
 * تحتوي هذه الفئة على الحاوية التفاعلية الكاملة لتسجيل حركة مالية جديدة أو تعديل حركة قائمة:
 * - إدارة نموذج الإدخال الكامل (المبلغ، البيان، التاريخ المخصص، العملة، وسعر الصرف).
 * - التحقق من صحة المدخلات المالية، التحويل الآلي بين العملات الأجنبية والمحلية.
 * - دعم التنقل السلس عبر Crossfade بين نموذج المعاملة وشاشة ضبط سعر الصرف.
 * - أزرار العمليات المزدوجة (قيد/سداد) المتكيفة ديناميكياً مع طبيعة حساب العميل (له/عليه).
 * =====================================================================================
 */

import android.widget.Toast
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
import com.example.domain.model.SaveTransactionResult
import kotlinx.coroutines.launch
import com.example.ui.theme.financialCreditColor
import com.example.ui.theme.financialDebtColor
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.local.entities.HabayebCustomer
import com.example.data.local.entities.HabayebTransaction
import com.example.domain.model.TransactionType
import com.example.ui.screens.CalculatorDialog
import com.example.ui.screens.habayeb.utils.CurrencyConfig
import com.example.ui.screens.habayeb.utils.ExchangeRateHelper
import com.example.ui.viewmodel.HabayebFinanceViewModel
import java.math.BigDecimal

/**
 * قيمة افتراضية فارغة مخصصة لحالات حقول الإدخال والوصف ومعدلات التحويل داخل هذا المكون فقط.
 */
private const val INITIAL_EMPTY_TEXT = ""

/*
 * =====================================================================================
 * نافذة الحوار المنبثقة لإضافة وتعديل المعاملة (AddTransactionPopup)
 * -------------------------------------------------------------------------------------
 * [الوصف والهدف]:
 * نافذة حوارية شاملة تتيح للمستخدم إضافة أو تعديل معاملة مالية مرتبطة بعميل محدد.
 * تتولى النافذة ما يلي:
 * 1. تهيئة الحقول من بيانات المعاملة السابقة في حال التعديل أو تجهيز قيم افتراضية جديدة.
 * 2. دعم فتح نوافذ فرعية مثل منتقي التاريخ والوقت المخصص (CustomDateTimePickerDialog) والآلة الحاسبة (CalculatorDialog).
 * 3. التحقق المالي وتطبيق أسعار الصرف وتحويل العملات عبر `ExchangeRateHelper` و `CurrencyConfig`.
 * 4. حفظ المعاملة عبر `HabayebFinanceViewModel` وتقديم تغذية راجعة للمستخدم عبر Toast وإغلاق النافذة.
 *
 * [المُدخلات]:
 * - customer: كائن العميل المستهدف بالمعاملة.
 * - viewModel: نموذج العرض المالي لتنفيذ عمليات الحفظ والتحديث.
 * - initialSelectedType: النوع الافتراضي المحدد للمعاملة.
 * - editingTransaction: المعاملة الحالية قيد التعديل (أو null في حالة الإضافة الجديدة).
 * - onDismiss: رد نداء لإغلاق النافذة المنبثقة.
 * - onTransactionSaved: رد نداء عند نجاح حفظ المعاملة.
 * - activeThemeColor: لون السمة الأساسي للواجهة.
 * - activeSubColor: لون السمة الفرعي للواجهة.
 * =====================================================================================
 */
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
    val customerState = customersUiState.customers.find { it.id == customer.id }
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

    var selectedTransactionCurrency by rememberSaveable {
        mutableStateOf(editingTransaction?.currencyCode?.let { if (it == "DEFAULT") currencySymbol else it } ?: initialCurrencyAndDesc.first)
    }

    val isForeignSelected = selectedTransactionCurrency != currencySymbol
    var applyExchangeRate by rememberSaveable { mutableStateOf(editingTransaction?.isRateCalculated ?: false) }

    val currentRateVal = ExchangeRateHelper.getRate(settings.exchangeRatesJson, currencySymbol, selectedTransactionCurrency)
    val settingsRate = if (currentRateVal <= 0.0) 1.0 else currentRateVal

    val effectiveRateBd = remember(editingTransaction, selectedTransactionCurrency, settingsRate) {
        if (editingTransaction != null && editingTransaction.currencyCode == selectedTransactionCurrency && editingTransaction.exchangeRate.compareTo(BigDecimal.ZERO) > 0) {
            editingTransaction.exchangeRate
        } else {
            BigDecimal.valueOf(settingsRate)
        }
    }

    val initialAmountText = remember(editingTransaction) {
        editingTransaction?.let {
            val fa = it.foreignAmount
            if (fa.scale() <= 0 || fa.stripTrailingZeros().scale() <= 0) fa.toBigInteger().toString() else fa.toPlainString()
        } ?: INITIAL_EMPTY_TEXT
    }
    val initialDescText = remember(editingTransaction) {
        if (editingTransaction != null) initialCurrencyAndDesc.second else INITIAL_EMPTY_TEXT
    }

    var amountTfv by remember(editingTransaction) {
        mutableStateOf(TextFieldValue(text = initialAmountText, selection = TextRange(initialAmountText.length)))
    }
    var descTfv by remember(editingTransaction) {
        mutableStateOf(TextFieldValue(text = initialDescText, selection = TextRange(initialDescText.length)))
    }

    val amountStr = amountTfv.text
    val descStr = descTfv.text
    val isLendOperationSelected = TransactionType.fromValue(customer.initialType) == TransactionType.OWED_BY_THEM || customer.initialType == TransactionType.OWED_BY_THEM.value
    var selectedType by rememberSaveable { mutableStateOf(editingTransaction?.type ?: initialSelectedType) }

    val amountFocusRequester = remember { FocusRequester() }
    val descFocusRequester = remember { FocusRequester() }
    val softwareKeyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    val view = androidx.compose.ui.platform.LocalView.current
    DisposableEffect(view) {
        val window = (view.parent as? androidx.compose.ui.window.DialogWindowProvider)?.window
        window?.setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
        onDispose {}
    }

    LaunchedEffect(Unit) {
        try {
            kotlinx.coroutines.delay(150)
            amountFocusRequester.requestFocus()
            softwareKeyboardController?.show()
        } catch (e: Exception) {
            e.printStackTrace()
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

    val isDark = MaterialTheme.colorScheme.background.run { red < 0.5f }
    val debtRedColor = financialDebtColor(isDark)
    val creditGreenColor = financialCreditColor(isDark)

    val scope = rememberCoroutineScope()
    val executeSave = { finalActionType: String ->
        softwareKeyboardController?.hide()
        if (!isSaving) {
            isSaving = true

            val cleanAmountStr = CurrencyConfig.normalizeDigits(amountStr).trim()
            val amountBd = CurrencyConfig.parseBigDecimal(cleanAmountStr)
            val hasStoredRate = ExchangeRateHelper.hasRate(settings.exchangeRatesJson, currencySymbol, selectedTransactionCurrency)
            val currentRateVal = ExchangeRateHelper.getRate(settings.exchangeRatesJson, currencySymbol, selectedTransactionCurrency)

            if (isForeignSelected && applyExchangeRate && (!hasStoredRate || currentRateVal == 1.0)) {
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
                    CurrencyConfig.convertAmountBigDecimal(amountBd, currencySymbol, selectedTransactionCurrency, effectiveRateBd)
                } else {
                    BigDecimal.ZERO
                }
                val saveAmountBd = if (isForeignSelected && applyExchangeRate) finalEquivalentAmountBd else amountBd
                val saveDescStr = CurrencyConfig.formatDescriptionWithCurrency(descStr.trim(), selectedTransactionCurrency)
                val saveTimestamp = dateMillis / 1000
                val saveEditingTxId = editingTransaction?.id

                scope.launch {
                    val result = viewModel.addHabayebTransaction(
                        customerId = customer.id,
                        type = finalActionType,
                        amount = saveAmountBd,
                        desc = saveDescStr,
                        timestamp = saveTimestamp,
                        editingTxId = saveEditingTxId,
                        isForeign = isForeignSelected,
                        currencyCode = selectedTransactionCurrency,
                        foreignAmount = amountBd,
                        exchangeRate = if (applyExchangeRate) effectiveRateBd else BigDecimal.ONE,
                        isRateCalculated = isForeignSelected && applyExchangeRate,
                        equivalentAmount = finalEquivalentAmountBd
                    )
                    isSaving = false
                    when (result) {
                        is SaveTransactionResult.Success -> {
                            haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                            Toast.makeText(context, context.getString(R.string.habayeb_toast_tx_save_success), Toast.LENGTH_SHORT).show()
                            onTransactionSaved()
                            onDismiss()
                        }
                        is SaveTransactionResult.TrialExpired -> {
                            Toast.makeText(context, context.getString(R.string.licensing_trial_expired_toast), Toast.LENGTH_LONG).show()
                            onDismiss()
                        }
                        is SaveTransactionResult.Error -> {
                            Toast.makeText(context, result.message ?: context.getString(R.string.toast_save_failed), Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = true
        )
    ) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .imePadding()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                Crossfade(targetState = showRateSetupOverlay, label = "FormTransition") { isSetup ->
                    if (isSetup) {
                        BackHandler {
                            showRateSetupOverlay = false
                            applyExchangeRate = false
                        }
                        ExchangeRateSetupContent(
                            selectedCurrency = selectedTransactionCurrency,
                            initialRateStr = tempRateStr,
                            activeThemeColor = activeThemeColor,
                            onDismiss = {
                                showRateSetupOverlay = false
                                applyExchangeRate = false
                            },
                            onConfirm = { newRate ->
                                val newSettings = settings.copy(
                                    exchangeRatesJson = ExchangeRateHelper.setRate(settings.exchangeRatesJson, currencySymbol, selectedTransactionCurrency, newRate)
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
                                        color = activeThemeColor
                                    )
                                    Text(
                                        text = stringResource(id = R.string.add_transaction_account_label, "${customer.name.take(15)}${if (customer.name.length > 15) ".." else ""}"),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                IconButton(
                                    onClick = onDismiss,
                                    modifier = Modifier
                                        .align(Alignment.CenterStart)
                                        .size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = stringResource(id = R.string.habayeb_go_back),
                                        tint = activeThemeColor,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            val dynamicThemeColor = if (isLendOperationSelected) debtRedColor else creditGreenColor

                            Spacer(modifier = Modifier.height(4.dp))

                            AddTransactionFormFields(
                                amountTfv = amountTfv,
                                onAmountChange = { amountTfv = it },
                                descTfv = descTfv,
                                onDescChange = { descTfv = it },
                                selectedTransactionCurrency = selectedTransactionCurrency,
                                dateMillis = dateMillis,
                                dynamicThemeColor = dynamicThemeColor,
                                amountFocusRequester = amountFocusRequester,
                                descFocusRequester = descFocusRequester,
                                onOpenCalculator = { showCalculator = true },
                                onOpenDatePicker = { showCustomDatePicker = true }
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
                                },
                                onApplyExchangeRateChange = { applyExchangeRate = it },
                                onSetupRateClick = { rate ->
                                    tempRateStr = rate
                                    showRateSetupOverlay = true
                                }
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            val handleActionClick = { type: String ->
                                val cleanAmountStr = CurrencyConfig.normalizeDigits(amountStr).trim()
                                val amountBd = CurrencyConfig.parseBigDecimal(cleanAmountStr)
                                if (amountBd <= BigDecimal.ZERO && descStr.trim().isBlank()) {
                                    Toast.makeText(context, context.getString(R.string.add_transaction_error_empty), Toast.LENGTH_SHORT).show()
                                } else if (amountBd < BigDecimal.ZERO) {
                                    Toast.makeText(context, context.getString(R.string.habayeb_toast_valid_amount), Toast.LENGTH_SHORT).show()
                                } else {
                                    focusManager.clearFocus()
                                    softwareKeyboardController?.hide()
                                    executeSave(type)
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    enabled = !isSaving,
                                    onClick = { handleActionClick(if (isLendOperationSelected) TransactionType.OWED_BY_THEM.value else TransactionType.OWED_TO_THEM.value) },
                                    colors = ButtonDefaults.buttonColors(containerColor = debtRedColor, contentColor = MaterialTheme.colorScheme.onError),
                                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f).height(42.dp)
                                ) {
                                    Text(
                                        text = if (isLendOperationSelected) stringResource(id = R.string.tx_action_debt_on_him) else stringResource(id = R.string.tx_action_debt_to_him),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onError
                                    )
                                }

                                Button(
                                    enabled = !isSaving,
                                    onClick = { handleActionClick(if (isLendOperationSelected) TransactionType.PAYMENT_BY_THEM.value else TransactionType.PAYMENT_TO_THEM.value) },
                                    colors = ButtonDefaults.buttonColors(containerColor = creditGreenColor, contentColor = MaterialTheme.colorScheme.onTertiary),
                                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f).height(42.dp)
                                ) {
                                    Text(
                                        text = if (isLendOperationSelected) stringResource(id = R.string.btn_receive) else stringResource(id = R.string.btn_pay),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onTertiary
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
                val calcStr = if (value.remainder(BigDecimal.ONE).compareTo(BigDecimal.ZERO) == 0) {
                    value.toBigInteger().toString()
                } else {
                    value.stripTrailingZeros().toPlainString()
                }
                amountTfv = TextFieldValue(text = calcStr, selection = TextRange(calcStr.length))
                showCalculator = false
            },
            activeThemeColor = activeThemeColor,
            activeSubColor = activeSubColor
        )
    }
}
