/**
 * =====================================================================
 * ملف: TransactionRecordDialog.kt
 * الحزمة: com.example.ui.screens.ledger.components
 * 
 * [الوصف والمسؤولية المعمارية]:
 * يمثل هذا الملف نافذة الحوار السريعة لتسجيل وتعديل الحركات المالية في دفتر اليومية.
 * تتيح هذه النافذة للمستخدم إدخال القيود المالية المباشرة (إيراد / وارد أو مصروف / منصرف)
 * أو تعديل قيد مالي سابق، مع توفير آلة حاسبة منبثقة مدمجة لحساب المبالغ المعقدة،
 * وتكييف ألوان الواجهة ديناميكياً (الأخضر للواردات والأحمر للمصروفات).
 * 
 * [تدفق البيانات وتجربة المستخدم]:
 * - يدعم النافذة الضبط التلقائي للوحة المفاتيح والتركيز الفوري على حقل المبلغ.
 * - يدعم تطهير الأرقام وتوحيد الأرقام العربية والإنجليزية عبر `CurrencyConfig.normalizeDigits`.
 * - يمنع النقر المزدوج أو التكرار عند الحفظ ويفعل الاهتزاز اللمسي التأكيدي (Haptic Feedback).
 * =====================================================================
 */
package com.example.ui.screens.ledger.components

// ---------------------------------------------------------------------
// استيراد الحزم الرياضية ومكونات واجهة المستخدم
// ---------------------------------------------------------------------
import java.math.BigDecimal
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.R
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import com.example.data.local.entities.TransactionDb
import com.example.ui.screens.CalculatorDialog
import com.example.ui.screens.habayeb.utils.CurrencyConfig
import com.example.ui.theme.financialCreditColor
import com.example.ui.theme.financialDebtColor

/**
 * =====================================================================
 * [نافذة تسجيل وتعديل القيد المالي - TransactionRecordDialog]:
 * 
 * [الهدف والغرض]:
 * توفير واجهة إدخال مبسطة وسريعة لتسجيل العمليات المالية المباشرة (وارد / منصرف)
 * في دفتر اليومية مع التحقق الفوري من صحة المبلغ وإمكانية استخدام الآلة الحاسبة.
 * 
 * [البيانات والمعاملات المستلمة]:
 * @param showTxDialog متغير منطقي يتحكم بظهور أو إخفاء نافذة الحوار.
 * @param txDialogType نوع العملية المالية ("INCOME" للوارد أو "EXPENSE" للمنصرف).
 * @param editingTransaction كائن المعاملة المُراد تعديلها (يكون null عند إضافة معاملة جديدة).
 * @param currencySymbol رمز العملة النقدية المعروض بجانب حقل المبلغ.
 * @param onDismiss دالة الاستدعاء الارتجاعي لإغلاق نافذة الحوار وإلغاء العملية.
 * @param onSave دالة حفظ القيد المالي وتمرير تفاصيله إلى الـ ViewModel لتخزينه في قاعدة البيانات.
 * @param modifier مخصصات المظهر والأبعاد.
 * =====================================================================
 */
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
    // التحقق المسبق: عدم بناء أي عناصر واجهة إذا كانت النافذة مخفية
    if (!showTxDialog) return

    val context = LocalContext.current

    // -----------------------------------------------------------------
    // تهيئة القيم الأولية وحالات الإدخال النصي
    // -----------------------------------------------------------------
    val initialAmount = remember(editingTransaction, showTxDialog) { editingTransaction?.amount?.toPlainString() ?: "" }
    val initialDesc = remember(editingTransaction, showTxDialog) { editingTransaction?.description ?: "" }

    // إدارة حقول الإدخال عبر TextFieldValue لضمان بقاء مؤشر الكتابة في نهاية النص
    var numAmountTfv by remember(editingTransaction, showTxDialog) {
        mutableStateOf(TextFieldValue(text = initialAmount, selection = TextRange(initialAmount.length)))
    }
    var descriptionTfv by remember(editingTransaction, showTxDialog) {
        mutableStateOf(TextFieldValue(text = initialDesc, selection = TextRange(initialDesc.length)))
    }
    val numAmount = numAmountTfv.text
    val descriptionStr = descriptionTfv.text

    // تحديد التصنيف الافتراضي (إيراد عام أو مصروف عام) في حال لم يتم تحديد تصنيف مخصص
    val categoryName = remember(editingTransaction, txDialogType) {
        editingTransaction?.category ?: if (txDialogType == "INCOME") context.getString(R.string.ledger_category_overall_income) else context.getString(R.string.ledger_category_expense)
    }

    // حالات التحكم في الآلة الحاسبة ومنع الحفظ المزدوج المتزامن
    var showCalcPopup by rememberSaveable { mutableStateOf(false) }
    var isSavingTx by remember(showTxDialog) { mutableStateOf(false) }

    // تنقية وتوحيد الأرقام وتحويلها إلى كائن BigDecimal بدقة عالية
    val parsedAmount = remember(numAmount) {
        val norm = CurrencyConfig.normalizeDigits(numAmount)
        try { BigDecimal(norm.trim()) } catch (_: Exception) { BigDecimal.ZERO }
    }
    // تفعيل زر التأكيد فقط إذا كان المبلغ أكبر من الصفر والعملية غير جارية حالياً
    val isConfirmButtonEnabled = !isSavingTx && parsedAmount.compareTo(BigDecimal.ZERO) > 0

    // -----------------------------------------------------------------
    // إدارة التركيز ولوحة المفاتيح
    // -----------------------------------------------------------------
    val focusRequester = remember { FocusRequester() }
    val descriptionFocusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val softwareKeyboardController = LocalSoftwareKeyboardController.current

    // ضبط نافذة الحوار لإبقاء لوحة المفاتيح ظاهرة دون إغلاق غير مقصود
    val view = androidx.compose.ui.platform.LocalView.current
    DisposableEffect(view) {
        val window = (view.parent as? androidx.compose.ui.window.DialogWindowProvider)?.window
        window?.setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
        onDispose {}
    }

    // التركيز البرمجي التلقائي على حقل إدخال المبلغ فور فتح الحوار
    LaunchedEffect(Unit) {
        try {
            kotlinx.coroutines.delay(150)
            focusRequester.requestFocus()
            softwareKeyboardController?.show()
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
        }
    }

    // -----------------------------------------------------------------
    // الألوان الديناميكية والهوية البصرية المالية
    // -----------------------------------------------------------------
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val isIncome = txDialogType == "INCOME"

    // اختيار اللون المالي (أخضر للواردات / أحمر للمصروفات) بما يتوافق مع الوضع الليلي والنهاري
    val themeColor = if (isIncome) {
        financialCreditColor(isDark)
    } else {
        financialDebtColor(isDark)
    }
    val themeColorSub = themeColor.copy(alpha = 0.85f)

    val dialogBgColor = MaterialTheme.colorScheme.surface
    val textInputBgColor = if (isDark) themeColor.copy(alpha = 0.12f) else themeColor.copy(alpha = 0.05f)
    val textColor = MaterialTheme.colorScheme.onSurface

    // -----------------------------------------------------------------
    // هيكل نافذة الحوار وبطاقة الإدخال
    // -----------------------------------------------------------------
    Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = true
        )
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = dialogBgColor,
            tonalElevation = 0.dp, // تعطيل الارتفاع النغمي لمنع ظهور تدرجات رمادية غير متناسقة
            border = BorderStroke(2.dp, themeColor),
            modifier = Modifier
                .width(280.dp)
                .wrapContentHeight()
                .imePadding()
        ) {
            Column(
                modifier = Modifier
                    .padding(14.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // -------------------------------------------------------------
                // عنوان نافذة الحوار (إضافة وارد / إضافة منصرف / تعديل العملية)
                // -------------------------------------------------------------
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
                            fontSize = 15.sp
                        ),
                        textAlign = TextAlign.Center
                    )
                }

                // -------------------------------------------------------------
                // حقول إدخال المبلغ والبيان
                // -------------------------------------------------------------
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // حقل إدخال المبلغ المالي مع أيقونة تشغيل الآلة الحاسبة
                    OutlinedTextField(
                        value = numAmountTfv,
                        onValueChange = { numAmountTfv = it },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
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
                                modifier = Modifier.size(32.dp)
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
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = textColor,
                            unfocusedTextColor = textColor,
                            focusedContainerColor = textInputBgColor,
                            unfocusedContainerColor = textInputBgColor,
                            focusedBorderColor = themeColor,
                            unfocusedBorderColor = themeColor.copy(alpha = 0.6f),
                            focusedLabelColor = themeColor,
                            unfocusedLabelColor = if (isDark) themeColor.copy(alpha = 0.8f) else themeColorSub
                        ),
                        textStyle = LocalTextStyle.current.copy(
                            color = textColor,
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    )

                    // حقل إدخال بيان أو وصف الحركة المالية
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
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = textColor,
                            unfocusedTextColor = textColor,
                            focusedContainerColor = textInputBgColor,
                            unfocusedContainerColor = textInputBgColor,
                            focusedBorderColor = themeColor,
                            unfocusedBorderColor = themeColor.copy(alpha = 0.6f),
                            focusedLabelColor = themeColor,
                            unfocusedLabelColor = if (isDark) themeColor.copy(alpha = 0.8f) else themeColorSub
                        ),
                        textStyle = LocalTextStyle.current.copy(
                            color = textColor,
                            textAlign = TextAlign.Right,
                            fontSize = 13.sp
                        ),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                focusManager.clearFocus()
                                softwareKeyboardController?.hide()
                                if (isConfirmButtonEnabled && !isSavingTx && parsedAmount.compareTo(BigDecimal.ZERO) > 0) {
                                    isSavingTx = true
                                    onSave(
                                        editingTransaction?.id,
                                        txDialogType,
                                        categoryName,
                                        parsedAmount,
                                        descriptionStr
                                    )
                                }
                            }
                        )
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // -------------------------------------------------------------
                // أزرار التحكم والإجراءات (إلغاء / حفظ)
                // -------------------------------------------------------------
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // زر الإلغاء
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = if (isDark) themeColor.copy(alpha = 0.9f) else themeColorSub
                        )
                    ) {
                        Text(
                            text = stringResource(id = R.string.common_cancel),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }

                    // زر الحفظ والتأكيد
                    Button(
                        enabled = isConfirmButtonEnabled && !isSavingTx,
                        onClick = {
                            if (isSavingTx) return@Button
                            if (parsedAmount.compareTo(BigDecimal.ZERO) > 0) {
                                isSavingTx = true
                                onSave(
                                    editingTransaction?.id,
                                    txDialogType,
                                    categoryName,
                                    parsedAmount,
                                    descriptionStr
                                )
                            }
                        },
                        modifier = Modifier.weight(1.2f),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = themeColor,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                            disabledContainerColor = themeColor.copy(alpha = 0.35f),
                            disabledContentColor = if (isDark) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.4f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                        )
                    ) {
                        Text(
                            text = stringResource(id = R.string.ledger_save_tx_btn),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }

    // -----------------------------------------------------------------
    // نافذة الآلة الحاسبة المنبثقة لإجراء العمليات الحسابية وتمرير الناتج مباشرة
    // -----------------------------------------------------------------
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

