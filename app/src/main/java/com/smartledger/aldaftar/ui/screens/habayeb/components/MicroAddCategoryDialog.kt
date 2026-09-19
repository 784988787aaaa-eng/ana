package com.smartledger.aldaftar.ui.screens.habayeb.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartledger.aldaftar.R
import com.smartledger.aldaftar.ui.components.MizanAnimatedDialog
import com.smartledger.aldaftar.ui.components.MizanDialogActions
import com.smartledger.aldaftar.ui.components.MizanDialogCard
import com.smartledger.aldaftar.ui.components.MizanDialogHeader
import com.smartledger.aldaftar.ui.components.RequestFocusAndShowKeyboard
import com.smartledger.aldaftar.ui.theme.MizanDialogTokens

@Composable
fun MicroAddCategoryDialog(
    activeThemeColor: Color,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var categoryName by rememberSaveable { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    MizanAnimatedDialog(
        onDismissRequest = onDismiss
    ) { dismiss ->
        RequestFocusAndShowKeyboard(focusRequester = focusRequester, autoShow = true)

        val handleSave = {
            val trimmed = categoryName.trim()
            if (trimmed.isNotBlank()) {
                keyboardController?.hide()
                onSave(trimmed)
                dismiss()
            }
        }

        MizanDialogCard(
            maxWidth = MizanDialogTokens.compactMaxWidth,
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp)
        ) {
            MizanDialogHeader(
                title = stringResource(R.string.habayeb_category_add_title),
                icon = Icons.Default.Category,
                iconTint = activeThemeColor,
                onCloseClick = dismiss
            )

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = categoryName,
                    onValueChange = { categoryName = it },
                    placeholder = { Text(stringResource(R.string.habayeb_category_add_placeholder), fontSize = 13.5.sp) },
                    singleLine = true,
                    textStyle = LocalTextStyle.current.copy(
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Start
                    ),
                    shape = MizanDialogTokens.inputShape,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = activeThemeColor,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                        focusedLabelColor = activeThemeColor,
                        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { keyboardController?.hide() }),
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester)
                )

                Spacer(modifier = Modifier.height(2.dp))

                MizanDialogActions(
                    confirmText = stringResource(R.string.habayeb_category_save),
                    onConfirm = handleSave,
                    confirmEnabled = categoryName.trim().isNotBlank(),
                    cancelText = stringResource(R.string.habayeb_category_cancel),
                    onCancel = dismiss
                )
            }
        }
    }
}


