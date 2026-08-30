package com.example.ui.screens.ledger.components

/*
 * =====================================================================================
 * قسم إدخال مفتاح التفعيل اليدوي (Activation Key Input Section)
 * -------------------------------------------------------------------------------------
 * [الوصف والهدف]:
 * مكون واجهة رسومية يوفر حقل إدخال مخصص لمفتاح الترخيص والتحقق اليدوي بدون إنترنت:
 * 1. يوفر حقل إدخال منسق ومقيد بسطر واحد مع دعم لوحة المفاتيح المخصصة وزر إتمام الإدخال (IME Done).
 * 2. يدعم إظهار حالة الخطأ (isCodeError) وتغيير ألوان الحدود ورسالة التنبيه المرافقة.
 * 3. يحتوي على زر التفعيل الفوري المرتبط بالتحقق من صحة الكود ومطابقته لمعرف الجهاز.
 * =====================================================================================
 */

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.EmeraldPrimary

/*
 * =====================================================================================
 * دالة العرض لقسم إدخال المفتاح (ActivationKeyInputSection Composable)
 * -------------------------------------------------------------------------------------
 * [المُدخلات]:
 * - activationCodeInput: القيمة النصية الحالية المدخلة في حقل المفتاح.
 * - isCodeError: هل الرمز المدخل خاطئ أو غير متطابق.
 * - onCodeInputChange: رد النداء عند تعديل نص الحقل.
 * - onVerifyManualCode: رد النداء لبدء التحقق من صحة المفتاح وتفعيل التطبيق.
 * =====================================================================================
 */
@Composable
fun ActivationKeyInputSection(
    activationCodeInput: String,
    isCodeError: Boolean,
    onCodeInputChange: (String) -> Unit,
    onVerifyManualCode: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                OutlinedTextField(
                    value = activationCodeInput,
                    onValueChange = onCodeInputChange,
                    placeholder = {
                        Text(
                            text = stringResource(R.string.licensing_code_placeholder),
                            fontSize = 11.sp
                        )
                    },
                    isError = isCodeError,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(onDone = { onVerifyManualCode() }),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("activation_code_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                        errorBorderColor = MaterialTheme.colorScheme.error
                    )
                )

                Button(
                    onClick = onVerifyManualCode,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .height(48.dp)
                        .testTag("verify_code_button"),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                ) {
                    Text(
                        text = stringResource(R.string.licensing_fluent_btn_activate_now),
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }

            if (isCodeError) {
                Text(
                    text = stringResource(R.string.licensing_fluent_product_key_error),
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 10.5.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

