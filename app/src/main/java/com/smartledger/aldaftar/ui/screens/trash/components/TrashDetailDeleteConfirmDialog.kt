package com.smartledger.aldaftar.ui.screens.trash.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartledger.aldaftar.R
import com.smartledger.aldaftar.ui.theme.MizanDialogTokens

import androidx.compose.foundation.layout.height

@Composable
fun TrashDetailDeleteConfirmDialog(
    onConfirmPermanentDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    val errorColor = MaterialTheme.colorScheme.error

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = onConfirmPermanentDelete,
                colors = ButtonDefaults.buttonColors(containerColor = errorColor),
                shape = MizanDialogTokens.buttonShape,
                modifier = androidx.compose.ui.Modifier.height(MizanDialogTokens.buttonHeight)
            ) {
                Text(
                    text = stringResource(id = R.string.trash_delete_permanently),
                    color = MaterialTheme.colorScheme.onError,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.5.sp,
                    maxLines = 1
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                shape = MizanDialogTokens.buttonShape,
                modifier = androidx.compose.ui.Modifier.height(MizanDialogTokens.buttonHeight)
            ) {
                Text(
                    text = stringResource(id = R.string.trash_cancel),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.5.sp,
                    maxLines = 1
                )
            }
        },
        title = {
            Text(
                text = stringResource(id = R.string.trash_delete_warning_title),
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            Text(
                text = stringResource(id = R.string.trash_delete_warning_desc),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = MizanDialogTokens.shape,
    )
}
