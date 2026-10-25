package com.smartledger.aldaftar.ui.screens.business

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
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
import com.smartledger.aldaftar.domain.business.BusinessPhoneFormatter
import com.smartledger.aldaftar.domain.business.CountryCode
import com.smartledger.aldaftar.ui.theme.MizanDialogTokens
import com.smartledger.aldaftar.ui.viewmodel.PhoneInputData

@Composable
fun BusinessProfilePhonesSection(
    phoneInputs: List<PhoneInputData>,
    phoneErrors: Map<Int, String> = emptyMap(),
    onPhoneDataChange: (Int, PhoneInputData) -> Unit,
    onRemovePhone: (Int) -> Unit,
    onAddPhone: () -> Unit,
    isDialog: Boolean,
    activeThemeColor: Color
) {
    val focusManager = LocalFocusManager.current

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(0.9.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f)),
        tonalElevation = if (isDialog) 0.dp else 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 9.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Section Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        modifier = Modifier.size(28.dp),
                        shape = RoundedCornerShape(8.dp),
                        color = activeThemeColor.copy(alpha = 0.12f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Phone,
                                contentDescription = null,
                                tint = activeThemeColor,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = stringResource(id = R.string.biz_phones_section),
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.testTag("biz_phones_section")
                        )
                        val validCount = phoneInputs.count { it.nationalNumber.isNotBlank() }
                        if (validCount > 0) {
                            Text(
                                text = validCount.toString(),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = activeThemeColor,
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(activeThemeColor.copy(alpha = 0.14f))
                                    .padding(horizontal = 7.dp, vertical = 1.dp)
                            )
                        }
                    }
                }

                if (phoneInputs.size < 3) {
                    FilledTonalButton(
                        onClick = onAddPhone,
                        modifier = Modifier.height(32.dp),
                        shape = RoundedCornerShape(9.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = activeThemeColor.copy(alpha = 0.12f),
                            contentColor = activeThemeColor
                        ),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddCircleOutline,
                            contentDescription = null,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = stringResource(id = R.string.biz_btn_add_phone),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            phoneInputs.forEachIndexed { index, phoneData ->
                val primaryLabel = stringResource(id = R.string.biz_label_primary_phone)
                val secLabel = stringResource(id = R.string.biz_label_secondary_phone, index + 1)
                val fieldLabel = if (index == 0) primaryLabel else secLabel
                val errorMessage = phoneErrors[index]
                val isLastItem = index == phoneInputs.lastIndex

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateContentSize(),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // 1. Country Code Selector
                        CountryCodeSelector(
                            selectedCode = phoneData.countryCode,
                            onCodeSelected = { newCode ->
                                onPhoneDataChange(index, phoneData.copy(countryCode = newCode))
                            },
                            activeThemeColor = activeThemeColor
                        )

                        // 2. Phone National Number Input
                        OutlinedTextField(
                            value = phoneData.nationalNumber,
                            onValueChange = { rawInput ->
                                val clean = BusinessPhoneFormatter.cleanDigits(rawInput).replace("+", "")
                                if (clean.length <= 15) {
                                    onPhoneDataChange(index, phoneData.copy(nationalNumber = clean))
                                }
                            },
                            label = { Text(text = fieldLabel, fontSize = 11.5.sp) },
                            placeholder = { Text(text = stringResource(id = R.string.biz_placeholder_phone), fontSize = 11.5.sp) },
                            isError = errorMessage != null,
                            singleLine = true,
                            shape = MizanDialogTokens.inputShape,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("biz_phone_input_$index"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = activeThemeColor,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                                focusedLabelColor = activeThemeColor,
                                cursorColor = activeThemeColor,
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.22f),
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.12f)
                            ),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Phone,
                                imeAction = if (isLastItem) ImeAction.Done else ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = { focusManager.moveFocus(FocusDirection.Down) },
                                onDone = { focusManager.clearFocus() }
                            ),
                            textStyle = LocalTextStyle.current.copy(
                                textAlign = TextAlign.Start,
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        )

                        // 3. Delete button for secondary numbers
                        if (index > 0) {
                            IconButton(
                                onClick = { onRemovePhone(index) },
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(MaterialTheme.colorScheme.error.copy(alpha = 0.08f))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeleteOutline,
                                    contentDescription = stringResource(id = R.string.biz_desc_delete_phone),
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    if (errorMessage != null) {
                        Text(
                            text = errorMessage,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CountryCodeSelector(
    selectedCode: String,
    onCodeSelected: (String) -> Unit,
    activeThemeColor: Color
) {
    var expanded by remember { mutableStateOf(false) }
    val currentCountry = remember(selectedCode) {
        BusinessPhoneFormatter.SUPPORTED_COUNTRY_CODES.find { it.code == selectedCode }
            ?: CountryCode(selectedCode, "", "🌐")
    }

    Box {
        Surface(
            modifier = Modifier
                .height(56.dp)
                .clip(MizanDialogTokens.inputShape)
                .clickable { expanded = true },
            shape = MizanDialogTokens.inputShape,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.22f)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "${currentCountry.flagEmoji} ${currentCountry.code}",
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = stringResource(id = R.string.biz_select_country_code_desc),
                    tint = activeThemeColor,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(MaterialTheme.colorScheme.surface)
        ) {
            BusinessPhoneFormatter.SUPPORTED_COUNTRY_CODES.forEach { country ->
                DropdownMenuItem(
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(text = country.flagEmoji, fontSize = 16.sp)
                            Text(
                                text = "${country.countryNameAr} (${country.code})",
                                fontSize = 13.sp,
                                fontWeight = if (country.code == selectedCode) FontWeight.Bold else FontWeight.Normal,
                                color = if (country.code == selectedCode) activeThemeColor else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    onClick = {
                        onCodeSelected(country.code)
                        expanded = false
                    }
                )
            }
        }
    }
}
