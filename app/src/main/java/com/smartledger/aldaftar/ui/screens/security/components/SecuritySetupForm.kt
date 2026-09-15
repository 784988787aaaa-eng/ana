package com.smartledger.aldaftar.ui.screens.security.components

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
import com.smartledger.aldaftar.R
import com.smartledger.aldaftar.ui.theme.BrandPrimary
import com.smartledger.aldaftar.ui.theme.CairoFontFamily
import com.smartledger.aldaftar.ui.theme.arabicInputTextStyle
import com.smartledger.aldaftar.ui.theme.universalTextFieldColors
import com.smartledger.aldaftar.ui.theme.MizanDialogTokens
import com.smartledger.aldaftar.ui.components.RequestFocusAndShowKeyboard
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween

import com.smartledger.aldaftar.platform.contacts.StringUtils.toEnglishDigits

private const val TAG = "SecuritySetupForm"
private const val CD_TOGGLE_VISIBILITY = "Toggle Visibility"
private const val TEST_TAG_PIN_CODE_INPUT = "pin_code_input"
private const val TEST_TAG_PIN_CODE_CONFIRM_INPUT = "pin_code_confirm_input"
private const val TEST_TAG_RECOVERY_PHRASE_INPUT = "recovery_phrase_input"
private const val TEST_TAG_RECOVERY_HINT_INPUT = "recovery_hint_input"
private const val TEST_TAG_SECURITY_SAVE_BUTTON = "security_save_button"

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

    var passcodeVisible by remember { mutableStateOf(false) }
    var confirmPasscodeVisible by remember { mutableStateOf(false) }

    val passcodeFocus = remember { FocusRequester() }
    val confirmPasscodeFocus = remember { FocusRequester() }
    val recoveryPhraseFocus = remember { FocusRequester() }
    val recoveryHintFocus = remember { FocusRequester() }

    val keyboardController = androidx.compose.ui.platform.LocalSoftwareKeyboardController.current
    RequestFocusAndShowKeyboard(focusRequester = passcodeFocus)

    val passcodeStrength = remember(passcode) {
        when {
            passcode.isEmpty() -> 0f to Color.Gray.copy(alpha = 0.3f)
            passcode.length < 3 -> 0.33f to Color(0xFFE57373)
            passcode.length == 3 -> 0.66f to Color(0xFFFFA726)
            passcode in listOf("0000", "1111", "1234", "2222", "1212", "4321") -> 0.66f to Color(0xFFFFA726)
            else -> 1f to Color(0xFF4CAF50)
        }
    }
    val animatedProgress by animateFloatAsState(
        targetValue = passcodeStrength.first,
        animationSpec = tween(durationMillis = 300),
        label = "strengthProgress"
    )
    val animatedStrengthColor by animateColorAsState(
        targetValue = passcodeStrength.second,
        animationSpec = tween(durationMillis = 300),
        label = "strengthColor"
    )

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
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = stringResource(id = R.string.sec_setup_title),
                fontFamily = CairoFontFamily,
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(bottom = 1.dp)
            )

            OutlinedTextField(
                value = passcode,
                onValueChange = { input ->
                    val clean = input.toEnglishDigits()
                    if (clean.length <= 4 && clean.all { c -> c.isDigit() }) {
                        onPasscodeChange(clean)
                        if (clean.length == 4) {
                            confirmPasscodeFocus.requestFocus()
                        }
                    }
                },
                label = { Text(stringResource(id = R.string.sec_label_code), fontFamily = CairoFontFamily, fontSize = 12.sp) },
                placeholder = { Text(stringResource(id = R.string.sec_placeholder_code), fontFamily = CairoFontFamily, fontSize = 12.sp) },
                singleLine = true,
                trailingIcon = {
                    IconButton(
                        onClick = { passcodeVisible = !passcodeVisible },
                        modifier = Modifier.size(44.dp)
                    ) {
                        Icon(
                            imageVector = if (passcodeVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = CD_TOGGLE_VISIBILITY,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                textStyle = arabicInputTextStyle(
                    textAlign = TextAlign.Center,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                ),
                visualTransformation = if (passcodeVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword, imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { confirmPasscodeFocus.requestFocus() }),
                shape = RoundedCornerShape(12.dp),
                colors = universalTextFieldColors(),
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 50.dp)
                    .focusRequester(passcodeFocus)
                    .testTag(TEST_TAG_PIN_CODE_INPUT)
            )

            if (passcode.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(id = R.string.sec_strength_label),
                            fontFamily = CairoFontFamily,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = when {
                                passcodeStrength.first >= 1f -> stringResource(R.string.sec_strength_strong)
                                passcodeStrength.first >= 0.6f -> stringResource(R.string.sec_strength_medium)
                                else -> stringResource(R.string.sec_strength_weak)
                            },
                            fontFamily = CairoFontFamily,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = animatedStrengthColor
                        )
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(animatedProgress)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(2.dp))
                                .background(animatedStrengthColor)
                        )
                    }
                }
            }

            OutlinedTextField(
                value = confirmPasscode,
                onValueChange = { input ->
                    val clean = input.toEnglishDigits()
                    if (clean.length <= 4 && clean.all { c -> c.isDigit() }) {
                        onConfirmPasscodeChange(clean)
                        if (clean.length == 4) {
                            recoveryPhraseFocus.requestFocus()
                        }
                    }
                },
                label = { Text(stringResource(id = R.string.sec_label_confirm), fontFamily = CairoFontFamily, fontSize = 12.sp) },
                placeholder = { Text(stringResource(id = R.string.sec_placeholder_confirm), fontFamily = CairoFontFamily, fontSize = 12.sp) },
                singleLine = true,
                trailingIcon = {
                    IconButton(
                        onClick = { confirmPasscodeVisible = !confirmPasscodeVisible },
                        modifier = Modifier.size(44.dp)
                    ) {
                        Icon(
                            imageVector = if (confirmPasscodeVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = CD_TOGGLE_VISIBILITY,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                textStyle = arabicInputTextStyle(
                    textAlign = TextAlign.Center,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                ),
                visualTransformation = if (confirmPasscodeVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword, imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { recoveryPhraseFocus.requestFocus() }),
                shape = RoundedCornerShape(12.dp),
                colors = universalTextFieldColors(),
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 50.dp)
                    .focusRequester(confirmPasscodeFocus)
                    .testTag(TEST_TAG_PIN_CODE_CONFIRM_INPUT)
            )

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f), 
                thickness = 0.5.dp,
                modifier = Modifier.padding(vertical = 1.dp)
            )

            Text(
                text = stringResource(id = R.string.sec_recovery_title),
                fontFamily = CairoFontFamily,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            OutlinedTextField(
                value = recoveryPhrase,
                onValueChange = onRecoveryPhraseChange,
                label = { Text(stringResource(id = R.string.sec_label_recovery), fontFamily = CairoFontFamily, fontSize = 12.sp) },
                placeholder = { Text(stringResource(id = R.string.sec_placeholder_recovery), fontFamily = CairoFontFamily, fontSize = 12.sp) },
                singleLine = true,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Key,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                },
                textStyle = arabicInputTextStyle(
                    textAlign = TextAlign.Start,
                    fontSize = 14.sp
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { recoveryHintFocus.requestFocus() }),
                shape = RoundedCornerShape(12.dp),
                colors = universalTextFieldColors(),
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 50.dp)
                    .focusRequester(recoveryPhraseFocus)
                    .testTag(TEST_TAG_RECOVERY_PHRASE_INPUT)
            )

            OutlinedTextField(
                value = recoveryHint,
                onValueChange = onRecoveryHintChange,
                label = { Text(stringResource(id = R.string.sec_label_hint), fontFamily = CairoFontFamily, fontSize = 12.sp) },
                placeholder = { Text(stringResource(id = R.string.sec_placeholder_hint), fontFamily = CairoFontFamily, fontSize = 12.sp) },
                singleLine = true,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Lightbulb,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                },
                textStyle = arabicInputTextStyle(
                    textAlign = TextAlign.Start,
                    fontSize = 14.sp
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                shape = RoundedCornerShape(12.dp),
                colors = universalTextFieldColors(),
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 50.dp)
                    .focusRequester(recoveryHintFocus)
                    .testTag(TEST_TAG_RECOVERY_HINT_INPUT)
            )

            val ackBg = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.12f)
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
                    fontFamily = CairoFontFamily,
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
                        checkedColor = BrandPrimary,
                        uncheckedColor = checkboxBorderColor
                    ),
                    modifier = Modifier.size(24.dp)
                )
            }

            val isValid = passcode.length == 4 &&
                    confirmPasscode == passcode &&
                    recoveryPhrase.isNotBlank() &&
                    checkAcknowledged &&
                    !isSaving

            Button(
                onClick = onSave,
                colors = ButtonDefaults.buttonColors(
                    containerColor = BrandPrimary,
                    disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                ),
                enabled = isValid,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .padding(top = 4.dp)
                    .testTag(TEST_TAG_SECURITY_SAVE_BUTTON)
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary, 
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = stringResource(id = R.string.sec_btn_activate),
                        fontFamily = CairoFontFamily,
                        color = if (isValid) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}
