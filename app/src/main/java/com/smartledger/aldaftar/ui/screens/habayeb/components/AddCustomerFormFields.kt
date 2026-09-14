package com.smartledger.aldaftar.ui.screens.habayeb.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartledger.aldaftar.R

@Composable
fun AddCustomerFormFields(
    nameStr: String,
    onNameChange: (String) -> Unit,
    phoneStr: String,
    onPhoneChange: (String) -> Unit,
    notesStr: String,
    onNotesChange: (String) -> Unit,
    initialAmountStr: String,
    onInitialAmountChange: (String) -> Unit,
    isDuplicateName: Boolean,
    selectedTransactionCurrency: String,
    activeThemeColor: Color,
    onCalculatorClick: () -> Unit,
    onCalendarClick: () -> Unit,
    onContactPickerClick: () -> Unit,
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
    isDark: Boolean = false,
    focusRequester: FocusRequester,
    initialAmountFocusRequester: FocusRequester,
    notesFocusRequester: FocusRequester,
    phoneFocusRequester: FocusRequester
) {
    val fieldShape = remember { RoundedCornerShape(8.dp) }
    val unfocusedBorder = MaterialTheme.colorScheme.outlineVariant
    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = activeThemeColor,
        focusedLabelColor = activeThemeColor,
        cursorColor = activeThemeColor,
        unfocusedBorderColor = unfocusedBorder,
        errorBorderColor = MaterialTheme.colorScheme.error
    )

    val inputTextStyle = androidx.compose.material3.LocalTextStyle.current.copy(fontSize = 13.5.sp)

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(
            value = nameStr,
            onValueChange = onNameChange,
            textStyle = inputTextStyle,
            label = { Text(stringResource(id = R.string.hint_account_name), fontSize = 11.sp) },
            placeholder = { Text(stringResource(id = R.string.habayeb_edit_name_desc), fontSize = 11.sp) },
            singleLine = true,
            shape = fieldShape,
            isError = isDuplicateName && nameStr.isNotBlank(),
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { initialAmountFocusRequester.requestFocus() }),
            colors = fieldColors
        )
        if (isDuplicateName && nameStr.isNotBlank()) {
            Text(
                text = stringResource(id = R.string.habayeb_error_duplicate_name),
                color = MaterialTheme.colorScheme.error,
                fontSize = 10.sp,
                modifier = Modifier
                    .padding(start = 8.dp, top = 2.dp, bottom = 4.dp)
                    .align(Alignment.Start)
            )
        }

        OutlinedTextField(
            value = initialAmountStr,
            onValueChange = onInitialAmountChange,
            textStyle = inputTextStyle,
            label = { Text(stringResource(id = R.string.hint_opening_balance), fontSize = 11.sp) },
            placeholder = { Text("0", fontSize = 11.sp) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { notesFocusRequester.requestFocus() }),
            singleLine = true,
            shape = fieldShape,
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(initialAmountFocusRequester),
            colors = fieldColors,
            leadingIcon = {
                IconButton(onClick = onCalculatorClick, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = Icons.Default.Calculate,
                        contentDescription = stringResource(id = R.string.habayeb_calculator),
                        tint = activeThemeColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
            },
            trailingIcon = {
                Text(
                    text = selectedTransactionCurrency,
                    fontSize = 11.sp,
                    color = activeThemeColor,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }
        )

        OutlinedTextField(
            value = notesStr,
            onValueChange = onNotesChange,
            textStyle = inputTextStyle,
            label = { Text(stringResource(id = R.string.hint_description), fontSize = 11.sp) },
            placeholder = { Text(stringResource(id = R.string.hint_description), fontSize = 11.sp) },
            singleLine = true,
            shape = fieldShape,
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(notesFocusRequester),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { phoneFocusRequester.requestFocus() }),
            colors = fieldColors,
            trailingIcon = {
                IconButton(
                    onClick = onCalendarClick,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = stringResource(id = R.string.habayeb_tx_date),
                        tint = activeThemeColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        )

        OutlinedTextField(
            value = phoneStr,
            onValueChange = onPhoneChange,
            textStyle = inputTextStyle,
            label = { Text(stringResource(id = R.string.habayeb_phone_label), fontSize = 11.sp) },
            placeholder = { Text(stringResource(id = R.string.habayeb_contact_picker), fontSize = 11.sp) },
            singleLine = true,
            shape = fieldShape,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { onDone() }),
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(phoneFocusRequester),
            colors = fieldColors,
            trailingIcon = {
                IconButton(onClick = onContactPickerClick, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = Icons.Default.Contacts,
                        contentDescription = stringResource(id = R.string.habayeb_contact_picker),
                        tint = activeThemeColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        )
    }
}
