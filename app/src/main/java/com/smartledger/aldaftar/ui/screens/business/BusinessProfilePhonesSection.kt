package com.smartledger.aldaftar.ui.screens.business

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.draw.rotate
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
import com.smartledger.aldaftar.ui.components.RequestFocusAndShowKeyboard
import com.smartledger.aldaftar.ui.theme.CairoFontFamily
import com.smartledger.aldaftar.ui.theme.arabicInputTextStyle
import com.smartledger.aldaftar.ui.theme.universalTextFieldColors

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
    var phonesExpanded by remember { mutableStateOf(false) }
    val focusRequesters = remember(effectivePhones.size) { List(effectivePhones.size) { FocusRequester() } }
    val arrowRotation by animateFloatAsState(
        targetValue = if (phonesExpanded) 180f else 0f,
        animationSpec = tween(durationMillis = 200),
        label = "arrow_rotation"
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .clickable { phonesExpanded = !phonesExpanded },
            shape = RoundedCornerShape(12.dp),
            color = activeThemeColor.copy(alpha = 0.05f),
            border = BorderStroke(0.8.dp, activeThemeColor.copy(alpha = 0.18f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Phone, 
                        contentDescription = null, 
                        tint = activeThemeColor, 
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = stringResource(id = R.string.biz_phones_section),
                        fontFamily = CairoFontFamily,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.testTag("biz_phones_section")
                    )
                    val count = phoneList.count { it.isNotBlank() }
                    if (count > 0) {
                        Text(
                            text = count.toString(),
                            fontFamily = CairoFontFamily,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF16A34A),
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(Color(0xFF22C55E).copy(alpha = 0.15f))
                                .padding(horizontal = 7.dp, vertical = 1.5.dp)
                        )
                    }
                }
                Icon(
                    imageVector = Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = activeThemeColor,
                    modifier = Modifier
                        .size(20.dp)
                        .rotate(arrowRotation)
                )
            }
        }

        if (phonesExpanded) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (effectivePhones.size < 3) {
                        androidx.compose.material3.TextButton(
                            onClick = onAddPhone,
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 6.dp, vertical = 0.dp),
                            modifier = Modifier.defaultMinSize(minWidth = 44.dp, minHeight = 36.dp)
                        ) {
                            Icon(Icons.Default.AddCircleOutline, null, tint = activeThemeColor, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.size(4.dp))
                            Text(
                                text = stringResource(id = R.string.biz_btn_add_phone),
                                fontFamily = CairoFontFamily,
                                fontSize = 11.5.sp,
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
                            label = { 
                                Text(
                                    text = phoneLabel, 
                                    fontFamily = CairoFontFamily,
                                    fontSize = 12.sp 
                                ) 
                            },
                            placeholder = { 
                                Text(
                                    text = placeholderText, 
                                    fontFamily = CairoFontFamily,
                                    fontSize = 12.sp 
                                ) 
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .defaultMinSize(minHeight = 56.dp)
                                .focusRequester(focusRequester),
                            colors = universalTextFieldColors(primary = activeThemeColor),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Phone,
                                imeAction = if (isLastItem) ImeAction.Done else ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = { focusManager.moveFocus(FocusDirection.Down) },
                                onDone = { focusManager.clearFocus() }
                            ),
                            textStyle = arabicInputTextStyle(
                                textAlign = TextAlign.Start, 
                                fontSize = 14.sp
                            ).copy(fontFamily = CairoFontFamily)
                        )

                        if (index > 0) {
                            IconButton(
                                onClick = { onRemovePhone(index) },
                                modifier = Modifier.size(44.dp),
                                colors = IconButtonDefaults.iconButtonColors(
                                    contentColor = MaterialTheme.colorScheme.error.copy(alpha = 0.80f)
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = stringResource(id = R.string.biz_desc_delete_phone),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }

                RequestFocusAndShowKeyboard(
                    focusRequester = focusRequesters.first(),
                    enabled = phonesExpanded,
                    key = phonesExpanded to effectivePhones.size
                )
            }
        }
    }
}
