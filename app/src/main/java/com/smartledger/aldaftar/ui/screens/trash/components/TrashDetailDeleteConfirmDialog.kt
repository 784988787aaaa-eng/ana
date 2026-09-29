package com.smartledger.aldaftar.ui.screens.trash.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.smartledger.aldaftar.R
import com.smartledger.aldaftar.ui.components.MizanDeleteConfirmationDialog

@Composable
fun TrashDetailDeleteConfirmDialog(
    onConfirmPermanentDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    MizanDeleteConfirmationDialog(
        title = stringResource(R.string.trash_delete_warning_title),
        message = stringResource(R.string.trash_delete_warning_desc),
        confirmText = stringResource(R.string.trash_delete_permanently),
        onConfirm = onConfirmPermanentDelete,
        onDismiss = onDismiss
    )
}
