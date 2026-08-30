/**
 * =====================================================================
 * ملف: SecuritySetupForm.kt
 * الحزمة: com.example.ui.screens.security.components
 * 
 * [الوصف والمسؤولية المعمارية]:
 * يمثل هذا الملف نموذج إعداد وتفعيل نظام الأمان وقفل التطبيق للمرة الأولى
 * أو عند إعادة ضبط رمز المرور. يحتوي هذا النموذج على حقول إدخال رمز المرور
 * المكون من 4 أرقام وتأكيده، عبارة الاسترداد السرية الإلزامية وتلميح التذكير،
 * ومربع الإقرار بالمسؤولية، مع زر الحفظ والتفعيل النهائي.
 * 
 * [تدفق التجربة والتحقق الذكي]:
 * - يدعم التنقل التلقائي الذكي للتركيز (Auto-Focus) بين الحقول فور اكتمال كتابة 4 أرقام.
 * - يطهر المدخلات تلقائياً عبر `toEnglishDigits()` لتوحيد الأرقام ومنع أخطاء لوحة المفاتيح.
 * - يتيح إظهار وإخفاء رموز المرور بصرياً لحماية الخصوصية أثناء الكتابة.
 * - يتحقق من مطابقة الرمز وتأكيده واستيفاء كافة الشروط قبل تفعيل زر الحفظ.
 * =====================================================================
 */
package com.example.ui.screens.security.components

// ---------------------------------------------------------------------
// استيراد الأدوات البرمجية وحزم واجهة Jetpack Compose ومكونات الأمان
// ---------------------------------------------------------------------
import android.util.Log
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.EmeraldPrimary
import com.example.domain.StringUtils.toEnglishDigits

/**
 * الثوابت الداخلية الخاصة بالتسجيل والوسوم الوصفية وعلامات الاختبار المؤتمت.
 */
private const val TAG = "SecuritySetupForm"
private const val CD_TOGGLE_VISIBILITY = "Toggle Visibility"
private const val TEST_TAG_PIN_CODE_INPUT = "pin_code_input"
private const val TEST_TAG_PIN_CODE_CONFIRM_INPUT = "pin_code_confirm_input"
private const val TEST_TAG_RECOVERY_PHRASE_INPUT = "recovery_phrase_input"
private const val TEST_TAG_RECOVERY_HINT_INPUT = "recovery_hint_input"
private const val TEST_TAG_SECURITY_SAVE_BUTTON = "security_save_button"

/**
 * =====================================================================
 * [نموذج إعداد وتفعيل الأمان - SecuritySetupForm]:
 * 
 * [الهدف والغرض]:
 * نموذج إدخال متكامل لضبط قفل التطبيق وعبارات الأمان والإقرار بالشروط وتفعيل الحماية.
 * 
 * [البيانات المستلمة]:
 * @param passcode رمز المرور الرئيسي المدخل حالياً (4 أرقام).
 * @param onPasscodeChange دالة استدعاء لتحديث قيمة رمز المرور.
 * @param confirmPasscode رمز تأكيد المرور المدخل.
 * @param onConfirmPasscodeChange دالة استدعاء لتحديث قيمة تأكيد رمز المرور.
 * @param recoveryPhrase عبارة الاسترداد السرية المدخلة لاسترجاع الحساب في حال نسيان الرمز.
 * @param onRecoveryPhraseChange دالة استدعاء لتحديث عبارة الاسترداد.
 * @param recoveryHint تلميح تذكير اختياري لعبارة الاسترداد.
 * @param onRecoveryHintChange دالة استدعاء لتحديث تلميح الاسترداد.
 * @param checkAcknowledged حالة مربع تأكيد الإقرار بالمسؤولية.
 * @param onCheckAcknowledgedChange دالة استدعاء لتغيير حالة الإقرار.
 * @param isSaving مؤشر حالة الحفظ الجارية لمنع تكرار الإرسال وإظهار مؤشر التحميل.
 * @param onSave دالة استدعاء لتنفيذ حفظ وتفعيل خيارات الأمان في قاعدة البيانات.
 * @param modifier مخصصات الأبعاد والتموضع.
 * =====================================================================
 */
@Composable
fun SecuritySetupForm(
    passcode: String,
    onPasscodeChange: (String) -> Unit,
    confirmPasscode: String,
    onConfirmPasscodeChange: (String) -> Unit,
    recoveryPhrase: String,
    onRecoveryPhraseChange: (String) -> Unit,
    recoveryHint: String,
    onRecoveryHintChange: (String) -> Unit,
    checkAcknowledged: Boolean,
    onCheckAcknowledgedChange: (Boolean) -> Unit,
    isSaving: Boolean,
    onSave: () -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    val isDark = MaterialTheme.colorScheme.background.run { red < 0.5f }

    // حالات التحكم في إظهار أو إخفاء الرمز كنقاط سرية
    var passcodeVisible by remember { mutableStateOf(false) }
    var confirmPasscodeVisible by remember { mutableStateOf(false) }

    // كائنات إدارة سلسلة التركيز والتنقل بين الحقول
    val passcodeFocus = remember { FocusRequester() }
    val confirmPasscodeFocus = remember { FocusRequester() }
    val recoveryPhraseFocus = remember { FocusRequester() }
    val recoveryHintFocus = remember { FocusRequester() }

    val keyboardController = androidx.compose.ui.platform.LocalSoftwareKeyboardController.current

    // تأثير برامجي فوري لاستدعاء لوحة المفاتيح والتركيز التلقائي على الحقل الأول فور الفتح
    LaunchedEffect(Unit) {
        try {
            kotlinx.coroutines.android.awaitFrame()
            passcodeFocus.requestFocus()
            keyboardController?.show()
        } catch (e: Exception) {
            Log.w(TAG, "Failed to request focus or show keyboard: ${e.message}")
        }
    }

    // بطاقة الحاوية الرئيسية لنموذج الإعداد
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 0.5.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(16.dp)
            )
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp) // تقارب الحقول بمسافات دقيقة واحترافية تمنع التشتت
        ) {
            // عنوان قسم إعداد رمز المرور
            Text(
                text = stringResource(id = R.string.sec_setup_title),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(bottom = 4.dp)
            )

            // -------------------------------------------------------------
            // حقل إدخال رمز المرور الأول (Passcode Input)
            // -------------------------------------------------------------
            OutlinedTextField(
                value = passcode,
                onValueChange = { input ->
                    val clean = input.toEnglishDigits()
                    if (clean.length <= 4 && clean.all { c -> c.isDigit() }) {
                        onPasscodeChange(clean)
                        // انتقال تلقائي فوري لحقل التأكيد عند اكتمال 4 أرقام
                        if (clean.length == 4) {
                            confirmPasscodeFocus.requestFocus()
                        }
                    }
                },
                label = { Text(stringResource(id = R.string.sec_label_code), fontSize = 12.sp) },
                placeholder = { Text(stringResource(id = R.string.sec_placeholder_code), fontSize = 12.sp) },
                singleLine = true,
                trailingIcon = {
                    IconButton(onClick = { passcodeVisible = !passcodeVisible }) {
                        Icon(
                            imageVector = if (passcodeVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = CD_TOGGLE_VISIBILITY,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                textStyle = LocalTextStyle.current.copy(
                    textAlign = TextAlign.Center,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                ),
                visualTransformation = if (passcodeVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword, imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { confirmPasscodeFocus.requestFocus() }),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = if (isDark) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(passcodeFocus)
                    .testTag(TEST_TAG_PIN_CODE_INPUT)
            )

            // -------------------------------------------------------------
            // حقل تأكيد رمز المرور (Confirm Passcode Input)
            // -------------------------------------------------------------
            OutlinedTextField(
                value = confirmPasscode,
                onValueChange = { input ->
                    val clean = input.toEnglishDigits()
                    if (clean.length <= 4 && clean.all { c -> c.isDigit() }) {
                        onConfirmPasscodeChange(clean)
                        // انتقال تلقائي لحقل عبارة الاسترداد عند اكتمال 4 أرقام
                        if (clean.length == 4) {
                            recoveryPhraseFocus.requestFocus()
                        }
                    }
                },
                label = { Text(stringResource(id = R.string.sec_label_confirm), fontSize = 12.sp) },
                placeholder = { Text(stringResource(id = R.string.sec_placeholder_confirm), fontSize = 12.sp) },
                singleLine = true,
                trailingIcon = {
                    IconButton(onClick = { confirmPasscodeVisible = !confirmPasscodeVisible }) {
                        Icon(
                            imageVector = if (confirmPasscodeVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = CD_TOGGLE_VISIBILITY,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                textStyle = LocalTextStyle.current.copy(
                    textAlign = TextAlign.Center,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                ),
                visualTransformation = if (confirmPasscodeVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword, imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { recoveryPhraseFocus.requestFocus() }),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = if (isDark) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(confirmPasscodeFocus)
                    .testTag(TEST_TAG_PIN_CODE_CONFIRM_INPUT)
            )

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f), 
                thickness = 0.5.dp,
                modifier = Modifier.padding(vertical = 4.dp)
            )

            // عنوان قسم خيارات الاسترداد الاحتياطية
            Text(
                text = stringResource(id = R.string.sec_recovery_title),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            // -------------------------------------------------------------
            // حقل إدخال عبارة الاسترداد الإلزامية (Recovery Phrase)
            // -------------------------------------------------------------
            OutlinedTextField(
                value = recoveryPhrase,
                onValueChange = onRecoveryPhraseChange,
                label = { Text(stringResource(id = R.string.sec_label_recovery), fontSize = 12.sp) },
                placeholder = { Text(stringResource(id = R.string.sec_placeholder_recovery), fontSize = 12.sp) },
                singleLine = true,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Key,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                },
                textStyle = LocalTextStyle.current.copy(
                    textAlign = TextAlign.Start,
                    fontSize = 14.sp
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { recoveryHintFocus.requestFocus() }),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = if (isDark) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(recoveryPhraseFocus)
                    .testTag(TEST_TAG_RECOVERY_PHRASE_INPUT)
            )

            // -------------------------------------------------------------
            // حقل إدخال تلميح الاسترداد الاختياري (Recovery Hint)
            // -------------------------------------------------------------
            OutlinedTextField(
                value = recoveryHint,
                onValueChange = onRecoveryHintChange,
                label = { Text(stringResource(id = R.string.sec_label_hint), fontSize = 12.sp) },
                placeholder = { Text(stringResource(id = R.string.sec_placeholder_hint), fontSize = 12.sp) },
                singleLine = true,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Lightbulb,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                },
                textStyle = LocalTextStyle.current.copy(
                    textAlign = TextAlign.Start,
                    fontSize = 14.sp
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = if (isDark) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(recoveryHintFocus)
                    .testTag(TEST_TAG_RECOVERY_HINT_INPUT)
            )

            // -------------------------------------------------------------
            // مربع تأكيد الإقرار بالمسؤولية وفهم مخاطر نسيان البيانات
            // -------------------------------------------------------------
            val ackBg = if (isDark) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)
            } else {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.08f)
            }
            val ackText = MaterialTheme.colorScheme.onSurface
            val checkboxBorderColor = MaterialTheme.colorScheme.outline

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(ackBg)
                    .clickable { onCheckAcknowledgedChange(!checkAcknowledged) }
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(id = R.string.sec_checkbox_ack),
                    fontSize = 11.sp,
                    color = ackText,
                    textAlign = TextAlign.Start,
                    lineHeight = 16.sp,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                )
                Checkbox(
                    checked = checkAcknowledged,
                    onCheckedChange = onCheckAcknowledgedChange,
                    colors = CheckboxDefaults.colors(
                        checkedColor = MaterialTheme.colorScheme.primary,
                        uncheckedColor = checkboxBorderColor
                    ),
                    modifier = Modifier.size(24.dp)
                )
            }

            // فحص صحة شروط التفعيل بالكامل قبل تمكين زر الحفظ
            val isValid = passcode.length == 4 &&
                    confirmPasscode == passcode &&
                    recoveryPhrase.isNotBlank() &&
                    checkAcknowledged &&
                    !isSaving

            // -------------------------------------------------------------
            // زر حفظ وتفعيل نظام الأمان (Save & Activate Button)
            // -------------------------------------------------------------
            Button(
                onClick = onSave,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                ),
                enabled = isValid,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .padding(top = 4.dp) // تباعد دقيق ليعطي الزر متنفساً حركياً ملحوظاً
                    .testTag(TEST_TAG_SECURITY_SAVE_BUTTON)
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary, 
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = stringResource(id = R.string.sec_btn_activate),
                        color = if (isValid) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

