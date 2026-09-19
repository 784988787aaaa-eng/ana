package com.smartledger.aldaftar.ui.screens.trash.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.smartledger.aldaftar.R
import com.smartledger.aldaftar.ui.components.MizanDeleteConfirmationDialog

@Composable
fun TrashDialogsManager(
    showEmptyConfirm: Boolean,
    onDismissEmptyConfirm: () -> Unit,
    onConfirmEmptyTrash: () -> Unit
) {
    if (!showEmptyConfirm) return
    MizanDeleteConfirmationDialog(
        title = stringResource(R.string.trash_confirm_empty_title),
        message = stringResource(R.string.trash_confirm_empty_desc),
        confirmText = stringResource(R.string.trash_empty_confirm_btn),
        onConfirm = onConfirmEmptyTrash,
        onDismiss = onDismissEmptyConfirm
    )
}
