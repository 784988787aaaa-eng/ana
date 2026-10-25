package com.smartledger.aldaftar.ui.screens.security.lock

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartledger.aldaftar.R
import com.smartledger.aldaftar.platform.contacts.StringUtils.toEnglishDigits
import com.smartledger.aldaftar.ui.theme.BrandSecondary
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
    isVerifying: Boolean = false,
    errorMessage: String? = null
) {
    val mizanColors = MaterialTheme.mizanColors
    val keyboardController = LocalSoftwareKeyboardController.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .widthIn(max = 420.dp)
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(62.dp)
                .clip(CircleShape)
                .background(BrandSecondary.copy(alpha = 0.16f))
                .border(1.2.dp, BrandSecondary.copy(alpha = 0.35f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Security,
                contentDescription = stringResource(id = R.string.lock_recover_account),
                tint = BrandSecondary,
                modifier = Modifier.size(30.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(id = R.string.lock_recovery_title),
            fontSize = 18.sp,
            fontWeight = FontWeight.ExtraBold,
            color = mizanColors.securityForeground,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = stringResource(id = R.string.lock_recovery_desc),
            fontSize = 12.sp,
            color = mizanColors.securityForegroundMuted,
            textAlign = TextAlign.Center,
            lineHeight = 17.sp
        )

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value = recoveryPhraseInput,
            onValueChange = { if (!isVerifying) onRecoveryPhraseChange(it.toEnglishDigits()) },
            enabled = !isVerifying,
            isError = !errorMessage.isNullOrBlank(),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("recovery_phrase_input_lock"),
            label = {
                Text(
                    stringResource(id = R.string.lock_recovery_phrase_hint),
                    color = mizanColors.securityForegroundMuted,
                    fontSize = 12.sp
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Key,
                    contentDescription = null,
                    tint = if (!errorMessage.isNullOrBlank()) mizanColors.error else BrandSecondary,
                    modifier = Modifier.size(18.dp)
                )
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = BrandSecondary,
                unfocusedBorderColor = mizanColors.securityInputBorder,
                errorBorderColor = mizanColors.error,
                focusedLabelColor = BrandSecondary,
                unfocusedLabelColor = mizanColors.securityForegroundMuted,
                focusedTextColor = mizanColors.securityForeground,
                unfocusedTextColor = mizanColors.securityForeground,
                errorTextColor = mizanColors.securityForeground
            ),
            shape = RoundedCornerShape(14.dp),
            singleLine = true,
            textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center, fontSize = 14.sp),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(
                onDone = {
                    keyboardController?.hide()
                    if (recoveryPhraseInput.isNotBlank() && !isVerifying) {
                        onVerifyClick()
                    }
                }
            )
        )

        AnimatedVisibility(visible = !errorMessage.isNullOrBlank()) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = mizanColors.error.copy(alpha = 0.16f),
                border = BorderStroke(0.8.dp, mizanColors.error.copy(alpha = 0.45f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .testTag("recovery_error_banner")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ErrorOutline,
                        contentDescription = null,
                        tint = mizanColors.error,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = errorMessage.orEmpty(),
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = mizanColors.error
                    )
                }
            }
        }

        if (!recoveryHint.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(10.dp))
            Surface(
                shape = RoundedCornerShape(50),
                color = mizanColors.warning.copy(alpha = 0.14f),
                border = BorderStroke(0.8.dp, mizanColors.warning.copy(alpha = 0.35f)),
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .clickable(enabled = !isVerifying) { onToggleHint() }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Lightbulb,
                        contentDescription = null,
                        tint = mizanColors.warning,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (showHintText) {
                            stringResource(id = R.string.lock_hide_hint)
                        } else {
                            stringResource(id = R.string.lock_show_hint)
                        },
                        color = mizanColors.warning,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        AnimatedVisibility(visible = showHintText && !recoveryHint.isNullOrBlank()) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = mizanColors.securityKeyBackground,
                border = BorderStroke(0.8.dp, mizanColors.securityKeyBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.lock_hint_prefix, recoveryHint ?: ""),
                    color = mizanColors.securityForeground,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = onVerifyClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("verify_recovery_unlock_btn"),
            colors = ButtonDefaults.buttonColors(
                containerColor = BrandSecondary,
                disabledContainerColor = mizanColors.securityKeyBackground
            ),
            shape = RoundedCornerShape(14.dp),
            enabled = recoveryPhraseInput.isNotBlank() && !isVerifying
        ) {
            if (isVerifying) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = mizanColors.securityForeground
                    )
                    Text(
                        text = stringResource(id = R.string.lock_verifying_recovery),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = mizanColors.securityForeground
                    )
                }
            } else {
                Text(
                    text = stringResource(id = R.string.lock_verify_and_unlock),
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = mizanColors.securityForeground
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        TextButton(
            onClick = onReturnToKeypadClick,
            enabled = !isVerifying
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(id = R.string.lock_back_to_keypad_desc),
                    tint = mizanColors.securityForegroundMuted,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = stringResource(id = R.string.lock_return_to_keypad),
                    color = mizanColors.securityForegroundMuted,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

