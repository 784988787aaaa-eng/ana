package com.smartledger.aldaftar.ui.screens.security.lock

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartledger.aldaftar.R
import com.smartledger.aldaftar.platform.contacts.StringUtils.toEnglishDigits
import com.smartledger.aldaftar.ui.components.RequestFocusAndShowKeyboard
import com.smartledger.aldaftar.ui.theme.CairoFontFamily
import com.smartledger.aldaftar.ui.theme.mizanColors

@Composable
fun RecoveryPhraseContent(
    recoveryPhraseInput: String,
    onRecoveryPhraseChange: (String) -> Unit,
    recoveryHint: String?,
    showHintText: Boolean,
    onToggleHint: () -> Unit,
    onVerifyClick: () -> Unit,
    onReturnToKeypadClick: () -> Unit,
    modifier: Modifier = Modifier,
    recoveryErrorMessage: String? = null
) {
    val mizanColors = MaterialTheme.mizanColors
    val keyboardController = androidx.compose.ui.platform.LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }

    RequestFocusAndShowKeyboard(focusRequester = focusRequester, autoShow = true)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(mizanColors.error.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Security,
                contentDescription = stringResource(id = R.string.lock_recover_account),
                tint = mizanColors.error,
                modifier = Modifier.size(28.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = stringResource(id = R.string.lock_recovery_title),
            fontFamily = CairoFontFamily,
            fontSize = 17.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = stringResource(id = R.string.lock_recovery_desc),
            fontFamily = CairoFontFamily,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            lineHeight = 18.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = recoveryPhraseInput,
            onValueChange = { onRecoveryPhraseChange(it.toEnglishDigits()) },
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester)
                .testTag("recovery_phrase_input_lock"),
            label = {
                Text(
                    text = stringResource(id = R.string.lock_recovery_phrase_hint),
                    fontFamily = CairoFontFamily,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            isError = !recoveryErrorMessage.isNullOrBlank(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = mizanColors.error,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                focusedLabelColor = mizanColors.error,
                unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface
            ),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            textStyle = LocalTextStyle.current.copy(
                fontFamily = CairoFontFamily,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            ),
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    keyboardController?.hide()
                    if (recoveryPhraseInput.isNotBlank()) {
                        onVerifyClick()
                    }
                }
            )
        )

        AnimatedVisibility(visible = !recoveryErrorMessage.isNullOrBlank()) {
            Text(
                text = recoveryErrorMessage.orEmpty(),
                fontFamily = CairoFontFamily,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = mizanColors.error,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 6.dp)
            )
        }

        if (!recoveryHint.isNullOrBlank()) {
            Row(
                modifier = Modifier
                    .padding(top = 10.dp, bottom = 4.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onToggleHint() }
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Lightbulb,
                    contentDescription = null,
                    tint = mizanColors.warning,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (showHintText) stringResource(id = R.string.lock_hide_hint) else stringResource(id = R.string.lock_show_hint),
                    fontFamily = CairoFontFamily,
                    color = mizanColors.warning,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        AnimatedVisibility(visible = showHintText && !recoveryHint.isNullOrBlank()) {
            Text(
                text = stringResource(id = R.string.lock_hint_prefix, recoveryHint ?: "").toEnglishDigits(),
                fontFamily = CairoFontFamily,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 12.5.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(vertical = 6.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onVerifyClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("verify_recovery_unlock_btn"),
            colors = ButtonDefaults.buttonColors(containerColor = mizanColors.error),
            shape = RoundedCornerShape(12.dp),
            enabled = recoveryPhraseInput.isNotBlank()
        ) {
            Text(
                text = stringResource(id = R.string.lock_verify_and_unlock),
                fontFamily = CairoFontFamily,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = mizanColors.securityForeground
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

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
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = stringResource(id = R.string.lock_return_to_keypad),
                    fontFamily = CairoFontFamily,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
