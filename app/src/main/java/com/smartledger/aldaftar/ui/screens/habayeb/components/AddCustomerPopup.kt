package com.smartledger.aldaftar.ui.screens.habayeb.components

import com.smartledger.aldaftar.ui.theme.MizanDialogTokens
import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.smartledger.aldaftar.R
import com.smartledger.aldaftar.platform.contacts.StringUtils
import com.smartledger.aldaftar.domain.model.TransactionType
import com.smartledger.aldaftar.ui.helper.rememberContactPicker
import com.smartledger.aldaftar.ui.screens.CalculatorDialog
import com.smartledger.aldaftar.ui.screens.habayeb.utils.ExchangeRateHelper
import com.smartledger.aldaftar.ui.theme.mizanColors
import com.smartledger.aldaftar.ui.viewmodel.HabayebFinanceViewModel
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import com.smartledger.aldaftar.ui.screens.habayeb.utils.CurrencyConfig
import java.util.Calendar
import kotlinx.coroutines.launch

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import com.smartledger.aldaftar.data.local.entities.CustomCategory

@Composable
fun AddCustomerPopup(
    viewModel: HabayebFinanceViewModel,
    onDismiss: () -> Unit,
    onCustomerAdded: (String) -> Unit = {},
    activeThemeColor: Color,
    activeSubColor: Color,
    initialCategory: String? = viewModel.resolveInitialCategoryForNewAccount(),
    customCategories: List<CustomCategory>? = null
) {
    val mizanColors = MaterialTheme.mizanColors
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val observedCategories by viewModel.customCategoriesState.collectAsStateWithLifecycle()
    val availableCategories = customCategories ?: observedCategories

    var nameStr by rememberSaveable { mutableStateOf("") }
    var phoneStr by rememberSaveable { mutableStateOf("") }
    var notesStr by rememberSaveable { mutableStateOf("") }
    var initialAmountStr by rememberSaveable { mutableStateOf("") }
    var initialType by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedCategoryName by rememberSaveable(initialCategory) {
        mutableStateOf(viewModel.resolveInitialCategoryForNewAccount(initialCategory))
    }
    var isCategoryPickerExpanded by rememberSaveable { mutableStateOf(false) }

    val settings by viewModel.settingsState.collectAsStateWithLifecycle()
    val debtRed = mizanColors.debt
    val creditGreen = mizanColors.credit
    val defaultPrimary = MaterialTheme.colorScheme.primary

    val dynamicThemeColor = remember(initialType, debtRed, creditGreen, defaultPrimary) {
        when (initialType) {
            TransactionType.OWED_BY_THEM.value -> debtRed     // عند اختيار "عليه": يتلون بالأحمر المالي الجذاب
            TransactionType.OWED_TO_THEM.value -> creditGreen // عند اختيار "له": يتلون بالأخضر المالي الجذاب
            else -> defaultPrimary // أرجواني افتراضي قبل تحديد النوع
        }
    }
    val currencySymbol = settings.currencySymbol
    var selectedTransactionCurrency by rememberSaveable { mutableStateOf(currencySymbol) }
    var applyExchangeRate by rememberSaveable { mutableStateOf(false) }
    var showRateSetupOverlay by rememberSaveable { mutableStateOf(false) }
    var tempRateStr by rememberSaveable { mutableStateOf("") }

    val settingsRate = remember(settings.exchangeRatesJson, currencySymbol, selectedTransactionCurrency) {
        val currentRateVal = ExchangeRateHelper.getRate(settings.exchangeRatesJson, selectedTransactionCurrency, currencySymbol)
        currentRateVal
    }

    var showCalculator by rememberSaveable { mutableStateOf(false) }
    var isSavingCustomer by rememberSaveable { mutableStateOf(false) }

    val existingCustomers by viewModel.habayebCustomersState.collectAsStateWithLifecycle()
    val normalizedInputName = remember(nameStr) { StringUtils.normalizeArabic(nameStr.trim()) }
    val isDuplicateName = remember(normalizedInputName, existingCustomers, isSavingCustomer) {
        if (isSavingCustomer || normalizedInputName.isBlank()) false
        else {
            existingCustomers.any { customer ->
                val normalizedExisting = StringUtils.normalizeArabic(customer.name.trim())
                normalizedExisting.isNotBlank() && normalizedExisting.equals(normalizedInputName, ignoreCase = true)
            }
        }
    }

    var selectedCalendar by remember { mutableStateOf(Calendar.getInstance()) }
    var showCustomDatePicker by remember { mutableStateOf(false) }

    if (showCustomDatePicker) {
        CustomDateTimePickerDialog(
            initialMillis = selectedCalendar.timeInMillis,
            onDismiss = { showCustomDatePicker = false },
            onDateTimeSelected = { millis ->
                selectedCalendar = (selectedCalendar.clone() as Calendar).apply { timeInMillis = millis }
                showCustomDatePicker = false
            }
        )
    }

    val haptic = LocalHapticFeedback.current
    val focusRequester = remember { FocusRequester() }
    val phoneFocusRequester = remember { FocusRequester() }
    val initialAmountFocusRequester = remember { FocusRequester() }
    val notesFocusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val softwareKeyboardController = LocalSoftwareKeyboardController.current

    val onSanitizedInitialAmountChange: (String) -> Unit = { raw ->
        if (raw.isEmpty()) {
            initialAmountStr = ""
        } else {
            val normalized = CurrencyConfig.normalizeDigits(raw).replace(" ", "")
            val dotCount = normalized.count { it == '.' }
            val isValidChars = normalized.all { it.isDigit() || it == '.' }
            val dotIdx = normalized.indexOf('.')
            val validDecimals = dotIdx == -1 || (normalized.length - dotIdx - 1 <= 2)
            if (isValidChars && dotCount <= 1 && validDecimals) {
                val cleaned = if (normalized.startsWith("0") && normalized.length > 1 && normalized[1] != '.') {
                    normalized.trimStart('0').ifEmpty { "0" }
                } else if (normalized.startsWith(".")) {
                    "0$normalized"
                } else {
                    normalized
                }
                initialAmountStr = cleaned
            }
        }
    }

    val launchContactPicker = rememberContactPicker { name, phone ->
        if (name.isNotBlank()) nameStr = name
        if (phone.isNotBlank()) phoneStr = phone
    }

    com.smartledger.aldaftar.ui.components.MizanAnimatedDialog(
        onDismissRequest = onDismiss
    ) { dismissDialog ->
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Card(
                shape = MizanDialogTokens.shape,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .widthIn(max = MizanDialogTokens.compactMaxWidth)
                    .fillMaxWidth(0.94f)
                    .imePadding()
                    .padding(2.dp)
            ) {
                Crossfade(
                    targetState = showRateSetupOverlay,
                    label = "CustomerFormTransition",
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
                            rateTargetCurrency = currencySymbol,
                            initialRateStr = tempRateStr,
                            activeThemeColor = dynamicThemeColor,
                            onDismiss = {
                                showRateSetupOverlay = false
                                applyExchangeRate = false
                            },
                            onConfirm = { newRate ->
                                val newSettings = settings.copy(
                                    exchangeRatesJson = ExchangeRateHelper.setRate(settings.exchangeRatesJson, selectedTransactionCurrency, currencySymbol, newRate)
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
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                .navigationBarsPadding()
                                .verticalScroll(rememberScrollState()),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                IconButton(onClick = dismissDialog, modifier = Modifier.size(44.dp)) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = stringResource(id = R.string.desc_close),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Text(
                                    text = stringResource(id = R.string.dialog_title_add_account),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = dynamicThemeColor,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(modifier = Modifier.size(44.dp))
                            }

                            if (availableCategories.isNotEmpty() || selectedCategoryName != null) {
                                AddCustomerCategorySelectorBar(
                                    selectedCategoryName = selectedCategoryName,
                                    availableCategories = availableCategories,
                                    isExpanded = isCategoryPickerExpanded,
                                    activeThemeColor = dynamicThemeColor,
                                    onToggleExpand = { isCategoryPickerExpanded = !isCategoryPickerExpanded },
                                    onSelectCategory = { newCat ->
                                        selectedCategoryName = newCat
                                        isCategoryPickerExpanded = false
                                    }
                                )
                            }

                            val performSave: () -> Unit = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                val formData = AddCustomerFormData(
                                    nameStr = nameStr,
                                    phoneStr = phoneStr,
                                    notesStr = notesStr,
                                    initialAmountStr = initialAmountStr,
                                    initialType = initialType,
                                    selectedTransactionCurrency = selectedTransactionCurrency,
                                    currencySymbol = currencySymbol,
                                    applyExchangeRate = applyExchangeRate,
                                    selectedCalendar = selectedCalendar,
                                    settingsRate = settingsRate,
                                    isDuplicateName = isDuplicateName,
                                    selectedCategory = selectedCategoryName
                                )
                                coroutineScope.launch {
                                    AddCustomerSaveHelper.handleSave(
                                        context = context,
                                        viewModel = viewModel,
                                        formData = formData,
                                        onIsSavingChange = { isSavingCustomer = it },
                                        onShowRateSetup = { rate ->
                                            tempRateStr = rate
                                            showRateSetupOverlay = true
                                        },
                                        onSuccess = onCustomerAdded,
                                        onDismiss = onDismiss
                                    )
                                }
                            }

                            AddCustomerFormFields(
                                nameStr = nameStr,
                                onNameChange = { nameStr = it },
                                phoneStr = phoneStr,
                                onPhoneChange = { phoneStr = it },
                                notesStr = notesStr,
                                onNotesChange = { notesStr = it },
                                initialAmountStr = initialAmountStr,
                                onInitialAmountChange = onSanitizedInitialAmountChange,
                                isDuplicateName = isDuplicateName,
                                selectedTransactionCurrency = selectedTransactionCurrency,
                                activeThemeColor = dynamicThemeColor,
                                onCalculatorClick = { showCalculator = true },
                                onCalendarClick = {
                                    focusManager.clearFocus()
                                    softwareKeyboardController?.hide()
                                    showCustomDatePicker = true
                                },
                                onContactPickerClick = { launchContactPicker() },
                                onDone = {
                                    focusManager.clearFocus()
                                    softwareKeyboardController?.hide()
                                },
                                focusRequester = focusRequester,
                                initialAmountFocusRequester = initialAmountFocusRequester,
                                notesFocusRequester = notesFocusRequester,
                                phoneFocusRequester = phoneFocusRequester
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            AddCustomerTypeAndCurrencySelector(
                                currencySymbol = currencySymbol,
                                selectedTransactionCurrency = selectedTransactionCurrency,
                                onCurrencySelected = {
                                    selectedTransactionCurrency = it
                                    applyExchangeRate = false
                                },
                                applyExchangeRate = applyExchangeRate,
                                onApplyExchangeRateChange = { applyExchangeRate = it },
                                initialType = initialType,
                                onTypeSelected = { initialType = it },
                                isSavingCustomer = isSavingCustomer,
                                onSaveClick = performSave,
                                activeThemeColor = dynamicThemeColor,
                                exchangeRatesJson = settings.exchangeRatesJson,
                                onRequestRateSetup = { rate ->
                                    tempRateStr = rate
                                    showRateSetupOverlay = true
                                }
                            )
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
                initialAmountStr = if (value.remainder(java.math.BigDecimal.ONE).compareTo(java.math.BigDecimal.ZERO) == 0) {
                    value.toBigInteger().toString()
                } else {
                    value.stripTrailingZeros().toPlainString()
                }
                showCalculator = false
            },
            activeThemeColor = dynamicThemeColor,
            activeSubColor = activeSubColor
        )
    }
}

@Composable
private fun AddCustomerCategorySelectorBar(
    selectedCategoryName: String?,
    availableCategories: List<CustomCategory>,
    isExpanded: Boolean,
    activeThemeColor: Color,
    onToggleExpand: () -> Unit,
    onSelectCategory: (String?) -> Unit
) {
    val hasCategory = !selectedCategoryName.isNullOrBlank()
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (hasCategory) {
            activeThemeColor.copy(alpha = 0.08f)
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        },
        border = BorderStroke(
            width = 1.dp,
            color = if (hasCategory) {
                activeThemeColor.copy(alpha = 0.28f)
            } else {
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
            }
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("add_customer_category_bar")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(enabled = availableCategories.isNotEmpty()) { onToggleExpand() }
                    .testTag("add_customer_category_selector"),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Folder,
                        contentDescription = null,
                        tint = if (hasCategory) activeThemeColor else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = if (hasCategory) {
                            "التصنيف: $selectedCategoryName"
                        } else {
                            "التصنيف: ${stringResource(id = R.string.context_menu_no_category)}"
                        },
                        fontSize = 12.5.sp,
                        fontWeight = if (hasCategory) FontWeight.Bold else FontWeight.Medium,
                        color = if (hasCategory) activeThemeColor else MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.testTag("add_customer_selected_category_label")
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (hasCategory) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))
                                .clickable { onSelectCategory(null) }
                                .testTag("add_customer_clear_category_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = stringResource(id = R.string.context_menu_no_category),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                    if (availableCategories.isNotEmpty()) {
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            AnimatedVisibility(visible = isExpanded && availableCategories.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(top = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val isNoneSelected = !hasCategory
                    FilterChip(
                        selected = isNoneSelected,
                        onClick = { onSelectCategory(null) },
                        label = {
                            Text(
                                text = stringResource(id = R.string.context_menu_no_category),
                                fontSize = 11.5.sp
                            )
                        },
                        modifier = Modifier
                            .height(30.dp)
                            .testTag("add_customer_category_chip_none")
                    )
                    availableCategories.forEach { cat ->
                        val isCatSelected = selectedCategoryName == cat.name
                        FilterChip(
                            selected = isCatSelected,
                            onClick = { onSelectCategory(cat.name) },
                            label = {
                                Text(
                                    text = cat.name,
                                    fontSize = 11.5.sp
                                )
                            },
                            modifier = Modifier
                                .height(30.dp)
                                .testTag("add_customer_category_chip_${cat.name}")
                        )
                    }
                }
            }
        }
    }
}
