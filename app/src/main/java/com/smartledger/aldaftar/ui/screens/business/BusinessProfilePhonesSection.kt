package com.smartledger.aldaftar.ui.screens.business

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartledger.aldaftar.R

@Composable
fun BusinessProfilePhonesSection(
    phoneList: List<String>,
    onPhoneChange: (Int, String) -> Unit,
    onRemovePhone: (Int) -> Unit,
    onAddPhone: () -> Unit,
    isDialog: Boolean,
    activeThemeColor: Color
) {
    val focusManager = LocalFocusManager.current
    val focusRequesters = remember(phoneList.size) { List(phoneList.size) { FocusRequester() } }

    LaunchedEffect(phoneList.size) {
        if (phoneList.size > 1) {
            focusRequesters.lastOrNull()?.requestFocus()
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isDialog) 0.dp else 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(id = R.string.biz_phones_section),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.testTag("biz_phones_section")
            )

            for (index in phoneList.indices) {
                val phone = phoneList[index]
                val primaryLabel = stringResource(id = R.string.biz_label_primary_phone)
                val secLabel = stringResource(id = R.string.biz_label_secondary_phone, index + 1)
                val phoneLabel = if (index == 0) primaryLabel else secLabel
                val placeholderText = stringResource(id = R.string.biz_placeholder_phone)
                val focusRequester = focusRequesters.getOrNull(index) ?: remember { FocusRequester() }
                val isLastItem = index == phoneList.lastIndex

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { newVal -> if (newVal.length <= 16) onPhoneChange(index, newVal) },
                        label = { Text(text = phoneLabel, fontSize = 13.sp) },
                        placeholder = { Text(text = placeholderText, fontSize = 13.sp) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .focusRequester(focusRequester),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = activeThemeColor,
                            focusedLabelColor = activeThemeColor,
                            cursorColor = activeThemeColor
                        ),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Phone,
                            imeAction = if (isLastItem) ImeAction.Done else ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) },
                            onDone = { focusManager.clearFocus() }
                        ),
                        textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Start)
                    )

                    if (index > 0) {
                        IconButton(
                            onClick = { onRemovePhone(index) },
                            modifier = Modifier.defaultMinSize(minWidth = 48.dp, minHeight = 48.dp),
                            colors = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.error)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = stringResource(id = R.string.biz_desc_delete_phone)
                            )
                        }
                    }

                    if (index == phoneList.lastIndex && phoneList.size < 3) {
                        IconButton(
                            onClick = { onAddPhone() },
                            modifier = Modifier.defaultMinSize(minWidth = 48.dp, minHeight = 48.dp),
                            colors = IconButtonDefaults.iconButtonColors(contentColor = activeThemeColor)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddCircleOutline,
                                contentDescription = stringResource(id = R.string.biz_btn_add_phone)
                            )
                        }
                    }
                }
            }
        }
    }
}
