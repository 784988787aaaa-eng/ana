package com.smartledger.aldaftar.ui.screens.habayeb.components

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartledger.aldaftar.R
import com.smartledger.aldaftar.data.local.entities.HabayebCustomer
import com.smartledger.aldaftar.platform.contacts.StringUtils
import com.smartledger.aldaftar.ui.components.MizanAnimatedDialog
import com.smartledger.aldaftar.ui.components.MizanDialogActions
import com.smartledger.aldaftar.ui.components.MizanDialogCard
import com.smartledger.aldaftar.ui.components.MizanDialogHeader
import com.smartledger.aldaftar.ui.components.RequestFocusAndShowKeyboard
import com.smartledger.aldaftar.ui.helper.rememberContactPicker
import com.smartledger.aldaftar.ui.theme.MizanDialogTokens
import com.smartledger.aldaftar.ui.theme.mizanColors

@Composable
fun CustomerDeleteConfirmationDialog(
    customer: HabayebCustomer? = null,
    selectedCustomerIds: List<String> = emptyList(),
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val isSingle = customer != null || selectedCustomerIds.size == 1
    val singleCustomerName = customer?.name ?: ""

    val mizanColors = MaterialTheme.mizanColors
    val debtRed = mizanColors.debt

    MizanAnimatedDialog(
        onDismissRequest = onDismiss
    ) { dismiss ->
        MizanDialogCard(
            maxWidth = MizanDialogTokens.compactMaxWidth,
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp)
        ) {
            MizanDialogHeader(
                title = if (isSingle) {
                    stringResource(id = R.string.habayeb_delete_account_title)
                } else {
                    stringResource(id = R.string.habayeb_bulk_delete_title)
                },
                icon = Icons.Default.Delete,
                iconTint = debtRed,
                onCloseClick = dismiss
            )

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = if (isSingle) {
                        stringResource(id = R.string.habayeb_delete_account_confirm, singleCustomerName)
                    } else {
                        stringResource(id = R.string.habayeb_bulk_delete_confirm, selectedCustomerIds.size)
                    },
                    fontSize = 13.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(2.dp))

                MizanDialogActions(
                    confirmText = stringResource(id = R.string.habayeb_delete_yes),
                    onConfirm = {
                        onConfirm()
                        dismiss()
                    },
                    confirmContainerColor = debtRed,
                    confirmContentColor = mizanColors.onDebt,
                    cancelText = stringResource(id = R.string.habayeb_cancel),
                    onCancel = dismiss
                )
            }
        }
    }
}

@Composable
fun CustomerEditDialog(
    customer: HabayebCustomer,
    activeThemeColor: Color,
    existingCustomers: List<HabayebCustomer> = emptyList(),
    onConfirm: (name: String, phone: String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var editedNameTfv by remember(customer.name) {
        mutableStateOf(TextFieldValue(text = customer.name, selection = TextRange(customer.name.length)))
    }
    var editedPhoneTfv by remember(customer.phone) {
        mutableStateOf(TextFieldValue(text = customer.phone, selection = TextRange(customer.phone.length)))
    }

    var isSaving by remember { mutableStateOf(false) }

    val editedNameStr = editedNameTfv.text
    val editedPhoneStr = editedPhoneTfv.text

    val normalizedExistingNames = remember(existingCustomers, customer.id) {
        existingCustomers
            .filter { it.id != customer.id }
            .map { StringUtils.normalizeArabic(it.name.trim()) }
            .filter { it.isNotBlank() }
            .toSet()
    }

    val normalizedEditedName = remember(editedNameStr) { StringUtils.normalizeArabic(editedNameStr.trim()) }
    val isDuplicateName = remember(editedNameStr, normalizedExistingNames, isSaving) {
        !isSaving && editedNameStr.trim().isNotBlank() && normalizedEditedName.isNotBlank() && normalizedExistingNames.contains(normalizedEditedName)
    }

    val editNameFocusRequester = remember { FocusRequester() }
    val editPhoneFocusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    val launchContactPicker = rememberContactPicker { name, phone ->
        if (name.isNotBlank() && editedNameTfv.text.isBlank()) {
            editedNameTfv = TextFieldValue(text = name, selection = TextRange(name.length))
        }
        if (phone.isNotBlank()) {
            editedPhoneTfv = TextFieldValue(text = phone, selection = TextRange(phone.length))
        }
    }

    MizanAnimatedDialog(
        onDismissRequest = onDismiss
    ) { dismiss ->
        RequestFocusAndShowKeyboard(
            focusRequester = editNameFocusRequester,
            autoShow = true
        )

        val handleSave = {
            if (editedNameStr.trim().isNotBlank()) {
                if (isDuplicateName) {
                    Toast.makeText(context, context.getString(R.string.habayeb_error_duplicate_name), Toast.LENGTH_SHORT).show()
                } else {
                    isSaving = true
                    keyboardController?.hide()
                    onConfirm(editedNameStr.trim(), editedPhoneStr.trim())
                    dismiss()
                }
            }
        }

        MizanDialogCard(
            maxWidth = MizanDialogTokens.compactMaxWidth,
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp)
        ) {
            MizanDialogHeader(
                title = stringResource(id = R.string.habayeb_edit_name_title),
                icon = Icons.Default.Edit,
                iconTint = activeThemeColor,
                onCloseClick = dismiss
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Column {
                    OutlinedTextField(
                        value = editedNameTfv,
                        onValueChange = { editedNameTfv = it },
                        label = { Text(stringResource(id = R.string.habayeb_account_name), fontSize = 12.sp) },
                        singleLine = true,
                        isError = isDuplicateName && editedNameStr.isNotBlank(),
                        shape = MizanDialogTokens.buttonShape,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { editPhoneFocusRequester.requestFocus() }),
                        textStyle = LocalTextStyle.current.copy(
                            fontSize = 13.5.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Start
                        ),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = activeThemeColor.copy(alpha = 0.8f),
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(editNameFocusRequester),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            focusedBorderColor = activeThemeColor,
                            focusedLabelColor = activeThemeColor,
                            cursorColor = activeThemeColor,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                            errorBorderColor = MaterialTheme.colorScheme.error
                        )
                    )
                    if (isDuplicateName && editedNameStr.isNotBlank()) {
                        Text(
                            text = stringResource(id = R.string.habayeb_error_duplicate_name),
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(start = 8.dp, top = 2.dp)
                        )
                    }
                }

                OutlinedTextField(
                    value = editedPhoneTfv,
                    onValueChange = { editedPhoneTfv = it },
                    label = { Text(stringResource(id = R.string.habayeb_phone_label), fontSize = 12.sp) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Phone,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            keyboardController?.hide()
                        }
                    ),
                    textStyle = LocalTextStyle.current.copy(
                        fontSize = 13.5.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Start
                    ),
                    shape = MizanDialogTokens.buttonShape,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = null,
                            tint = activeThemeColor.copy(alpha = 0.8f),
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    trailingIcon = {
                        IconButton(
                            onClick = { launchContactPicker() },
                            modifier = Modifier
                                .padding(4.dp)
                                .size(34.dp)
                                .background(
                                    color = activeThemeColor.copy(alpha = 0.1f),
                                    shape = RoundedCornerShape(8.dp)
                                )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Contacts,
                                contentDescription = stringResource(id = R.string.habayeb_contact_picker),
                                tint = activeThemeColor,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(editPhoneFocusRequester),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedBorderColor = activeThemeColor,
                        focusedLabelColor = activeThemeColor,
                        cursorColor = activeThemeColor,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    )
                )

                Spacer(modifier = Modifier.height(2.dp))

                MizanDialogActions(
                    confirmText = stringResource(id = R.string.habayeb_save_edit),
                    onConfirm = handleSave,
                    confirmEnabled = editedNameStr.trim().isNotBlank(),
                    confirmContainerColor = activeThemeColor,
                    cancelText = stringResource(id = R.string.habayeb_cancel),
                    onCancel = dismiss
                )
            }
        }
    }
}
