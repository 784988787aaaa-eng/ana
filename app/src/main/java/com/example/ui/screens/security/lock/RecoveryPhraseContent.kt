/*
 * توثيق هندسي عربي — RecoveryPhraseContent.kt
 *
 * المسؤولية المعمارية:
 * واجهة إدخال عبارة الاسترداد لمسار فتح القفل البديل. تعرض التعليمات والحقل والإجراءات اللازمة للتحقق من العبارة دون خلطها بمسار رمز المرور.
 *
 * القراءة التعليمية:
 * هذا الملف جزء من مسار الأمان في التطبيق؛ أي تفاعل مرئي هنا يجب فهمه كحالة من حالات
 * الحماية: ما يراه المستخدم على الشاشة هو انعكاس لحالة داخلية، وليس بديلاً عن قرار
 * أمني صادر من طبقات المجال والتخزين الآمن. التوثيق التالي لا يغير التنفيذ الأصلي.
 *
 * مبدأ الثبات: الشيفرة التنفيذية الأصلية محفوظة دون حذف أو استبدال أو تعديل.
 */

package com.example.ui.screens.security.lock

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.domain.StringUtils.toEnglishDigits
import com.example.ui.theme.mizanColors

/**
 * Visual content for the Recovery Phrase screen when the user forgets the PIN.
 */
@Composable
fun RecoveryPhraseContent(
    recoveryPhraseInput: String,
    onRecoveryPhraseChange: (String) -> Unit,
    recoveryHint: String?,
    showHintText: Boolean,
    onToggleHint: () -> Unit,
    onVerifyClick: () -> Unit,
    onReturnToKeypadClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val mizanColors = MaterialTheme.mizanColors

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(mizanColors.error.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Security,
                contentDescription = stringResource(id = R.string.lock_recover_account),
                tint = mizanColors.error,
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = stringResource(id = R.string.lock_recovery_title),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = mizanColors.securityForeground,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = stringResource(id = R.string.lock_recovery_desc),
            fontSize = 12.sp,
            color = mizanColors.securityForegroundMuted,
            textAlign = TextAlign.Center,
            lineHeight = 18.sp
        )

        Spacer(modifier = Modifier.height(28.dp))

        OutlinedTextField(
            value = recoveryPhraseInput,
            onValueChange = { onRecoveryPhraseChange(it.toEnglishDigits()) },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("recovery_phrase_input_lock"),
            label = { Text(stringResource(id = R.string.lock_recovery_phrase_hint), color = mizanColors.securityForegroundMuted) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = mizanColors.error,
                unfocusedBorderColor = mizanColors.securityInputBorder,
                focusedLabelColor = mizanColors.error,
                unfocusedLabelColor = mizanColors.securityForegroundMuted,
                focusedTextColor = mizanColors.securityForeground,
                unfocusedTextColor = mizanColors.securityForeground
            ),
            singleLine = true,
            textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center),
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    if (recoveryPhraseInput.isNotBlank()) {
                        onVerifyClick()
                    }
                }
            )
        )

        if (!recoveryHint.isNullOrBlank()) {
            Row(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .clickable { onToggleHint() },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Lightbulb,
                    contentDescription = null,
                    tint = mizanColors.warning,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (showHintText) stringResource(id = R.string.lock_hide_hint) else stringResource(id = R.string.lock_show_hint),
                    color = mizanColors.warning,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        AnimatedVisibility(visible = showHintText && !recoveryHint.isNullOrBlank()) {
            Text(
                text = stringResource(id = R.string.lock_hint_prefix, recoveryHint ?: ""),
                color = mizanColors.securityForeground,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onVerifyClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("verify_recovery_unlock_btn"),
            colors = ButtonDefaults.buttonColors(containerColor = mizanColors.error),
            shape = RoundedCornerShape(16.dp),
            enabled = recoveryPhraseInput.isNotBlank()
        ) {
            Text(
                text = stringResource(id = R.string.lock_verify_and_unlock),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = mizanColors.securityForeground
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(
            onClick = onReturnToKeypadClick
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = stringResource(id = R.string.lock_back_to_keypad_desc),
                    tint = mizanColors.securityForegroundMuted
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = stringResource(id = R.string.lock_return_to_keypad),
                    color = mizanColors.securityForegroundMuted,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}


/*
 * --- ملاحظات وتوصيات المعمارية البرمجية ---
 *
 * - تمييز عبارة الاسترداد بوضوح بصري عن رمز المرور مع الحفاظ على عدم كشفها في السجل أو الرسائل.
 * - اختبار حالات النص الطويل ولوحة المفاتيح والعرض على الشاشات الصغيرة.
 */
