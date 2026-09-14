package com.smartledger.aldaftar.ui.screens.business

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import com.smartledger.aldaftar.R
import com.smartledger.aldaftar.ui.components.RequestFocusAndShowKeyboard

private const val MAX_BIZ_NAME_LENGTH = 40
private const val MAX_BIZ_DESC_LENGTH = 45

@Composable
fun BusinessProfileInfoSection(
    bizName: String,
    onBizNameChange: (String) -> Unit,
    bizDesc: String,
    onBizDescChange: (String) -> Unit,
    isDialog: Boolean,
    activeThemeColor: Color,
    onDescriptionNext: () -> Unit
) {
    val dialogNameFocusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val nameInteractionSource = remember { MutableInteractionSource() }
    val descInteractionSource = remember { MutableInteractionSource() }
    val nameFocused by nameInteractionSource.collectIsFocusedAsState()
    val descFocused by descInteractionSource.collectIsFocusedAsState()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isDialog) 0.dp else 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            OutlinedTextField(
                value = bizName,
                onValueChange = { if (it.length <= MAX_BIZ_NAME_LENGTH) onBizNameChange(it) },
                label = { Text(text = stringResource(id = R.string.biz_label_name), fontSize = 12.sp) },
                placeholder = { Text(text = stringResource(id = R.string.biz_placeholder_name), fontSize = 13.sp) },
                trailingIcon = {
                    if (nameFocused) {
                        Text(
                            text = stringResource(id = R.string.biz_character_counter, bizName.length, MAX_BIZ_NAME_LENGTH),
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f)
                        )
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("biz_name_input")
                    .focusRequester(dialogNameFocusRequester),
                interactionSource = nameInteractionSource,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = activeThemeColor,
                    focusedLabelColor = activeThemeColor,
                    cursorColor = activeThemeColor
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Right, fontSize = 14.sp)
            )

            if (isDialog) {
                RequestFocusAndShowKeyboard(focusRequester = dialogNameFocusRequester)
            }

            OutlinedTextField(
                value = bizDesc,
                onValueChange = { if (it.length <= MAX_BIZ_DESC_LENGTH) onBizDescChange(it) },
                label = { Text(text = stringResource(id = R.string.biz_label_desc), fontSize = 12.sp) },
                placeholder = { Text(text = stringResource(id = R.string.biz_placeholder_desc), fontSize = 13.sp) },
                trailingIcon = {
                    if (descFocused) {
                        Text(
                            text = stringResource(id = R.string.biz_character_counter, bizDesc.length, MAX_BIZ_DESC_LENGTH),
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f)
                        )
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("biz_desc_input"),
                interactionSource = descInteractionSource,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = activeThemeColor,
                    focusedLabelColor = activeThemeColor,
                    cursorColor = activeThemeColor
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = {
                    onDescriptionNext()
                }),
                textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Right, fontSize = 14.sp)
            )
        }
    }
}
