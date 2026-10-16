package com.smartledger.aldaftar.ui.screens.business

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartledger.aldaftar.R
import com.smartledger.aldaftar.ui.components.RequestFocusAndShowKeyboard
import com.smartledger.aldaftar.ui.theme.MizanDialogTokens

private const val MAX_BIZ_NAME_LENGTH = 40
private const val MAX_BIZ_DESC_LENGTH = 45

@Composable
fun BusinessProfileInfoSection(
    bizName: String,
    onBizNameChange: (String) -> Unit,
    bizDesc: String,
    onBizDescChange: (String) -> Unit,
    isDialog: Boolean,
    activeThemeColor: Color
) {
    val focusManager = LocalFocusManager.current
    val dialogNameFocusRequester = remember { FocusRequester() }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(0.9.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        tonalElevation = if (isDialog) 0.dp else 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Section Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    modifier = Modifier.size(30.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = activeThemeColor.copy(alpha = 0.12f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Business,
                            contentDescription = null,
                            tint = activeThemeColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                    Text(
                        text = stringResource(id = R.string.biz_details_section),
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(id = R.string.biz_details_subtitle),
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            OutlinedTextField(
                value = bizName,
                onValueChange = { if (it.length <= MAX_BIZ_NAME_LENGTH) onBizNameChange(it) },
                label = { Text(text = stringResource(id = R.string.biz_label_name), fontSize = 12.sp) },
                placeholder = { Text(text = stringResource(id = R.string.biz_placeholder_name), fontSize = 12.sp) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Storefront,
                        contentDescription = null,
                        tint = activeThemeColor,
                        modifier = Modifier.size(19.dp)
                    )
                },
                supportingText = {
                    Text(
                        text = stringResource(id = R.string.biz_char_counter_pattern, bizName.length, MAX_BIZ_NAME_LENGTH),
                        fontSize = 10.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.End
                    )
                },
                singleLine = true,
                shape = MizanDialogTokens.inputShape,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("biz_name_input")
                    .focusRequester(dialogNameFocusRequester),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = activeThemeColor,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    focusedLabelColor = activeThemeColor,
                    cursorColor = activeThemeColor,
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.22f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.12f)
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                textStyle = LocalTextStyle.current.copy(
                    textAlign = TextAlign.Right,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.SemiBold
                )
            )

            if (isDialog) {
                RequestFocusAndShowKeyboard(focusRequester = dialogNameFocusRequester, autoShow = true)
            }

            OutlinedTextField(
                value = bizDesc,
                onValueChange = { if (it.length <= MAX_BIZ_DESC_LENGTH) onBizDescChange(it) },
                label = { Text(text = stringResource(id = R.string.biz_label_desc), fontSize = 12.sp) },
                placeholder = { Text(text = stringResource(id = R.string.biz_placeholder_desc), fontSize = 12.sp) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = null,
                        tint = activeThemeColor,
                        modifier = Modifier.size(19.dp)
                    )
                },
                supportingText = {
                    Text(
                        text = stringResource(id = R.string.biz_char_counter_pattern, bizDesc.length, MAX_BIZ_DESC_LENGTH),
                        fontSize = 10.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.End
                    )
                },
                singleLine = true,
                maxLines = 1,
                shape = MizanDialogTokens.inputShape,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("biz_desc_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = activeThemeColor,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    focusedLabelColor = activeThemeColor,
                    cursorColor = activeThemeColor,
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.22f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.12f)
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                textStyle = LocalTextStyle.current.copy(
                    textAlign = TextAlign.Right,
                    fontSize = 13.5.sp
                )
            )
        }
    }
}
