package com.smartledger.aldaftar.ui.screens.settings.components

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartledger.aldaftar.R
import com.smartledger.aldaftar.ui.theme.MizanDialogTokens

@Composable
fun RestoreWarningDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    com.smartledger.aldaftar.ui.components.MizanDeleteConfirmationDialog(
        title = stringResource(R.string.backup_restore_warn_title),
        message = stringResource(R.string.backup_restore_warn_desc),
        confirmText = stringResource(R.string.backup_restore_confirm_btn),
        icon = Icons.Default.CloudDownload,
        onConfirm = {
            onConfirm()
            onDismiss()
        },
        onDismiss = onDismiss
    )
}
