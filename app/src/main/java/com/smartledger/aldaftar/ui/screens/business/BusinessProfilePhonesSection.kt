package com.smartledger.aldaftar.ui.screens.business

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
    val effectivePhones = remember(phoneList) { if (phoneList.isEmpty()) listOf("") else phoneList }
    val focusRequesters = remember(effectivePhones.size) { List(effectivePhones.size) { FocusRequester() } }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(id = R.string.biz_phones_section),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.testTag("biz_phones_section")
            )

            if (effectivePhones.size < 3) {
                androidx.compose.material3.TextButton(
                    onClick = onAddPhone,
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    modifier = Modifier.defaultMinSize(minWidth = 44.dp, minHeight = 36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AddCircleOutline,
                        contentDescription = null,
                        tint = activeThemeColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.padding(horizontal = 2.dp))
                    Text(
                        text = stringResource(id = R.string.biz_btn_add_phone),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = activeThemeColor
                    )
                }
            }
        }

        for (index in effectivePhones.indices) {
            val phone = effectivePhones[index]
            val primaryLabel = stringResource(id = R.string.biz_label_primary_phone)
            val secLabel = stringResource(id = R.string.biz_label_secondary_phone, index + 1)
            val phoneLabel = if (index == 0) primaryLabel else secLabel
            val placeholderText = stringResource(id = R.string.biz_placeholder_phone)
            val focusRequester = focusRequesters.getOrNull(index) ?: remember { FocusRequester() }
            val isLastItem = index == effectivePhones.lastIndex

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                OutlinedTextField(
                    value = phone,
                    onValueChange = { newVal -> if (newVal.length <= 16) onPhoneChange(index, newVal) },
                    label = { Text(text = phoneLabel, fontSize = 12.sp) },
                    placeholder = { Text(text = placeholderText, fontSize = 12.sp) },
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
                    textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Start, fontSize = 13.sp)
                )

                if (index > 0) {
                    IconButton(
                        onClick = { onRemovePhone(index) },
                        modifier = Modifier.defaultMinSize(minWidth = 44.dp, minHeight = 44.dp),
                        colors = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = stringResource(id = R.string.biz_desc_delete_phone),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}
