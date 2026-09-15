package com.smartledger.aldaftar.ui.screens.ledger.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartledger.aldaftar.R
import com.smartledger.aldaftar.ui.components.MizanAnimatedDialog
import com.smartledger.aldaftar.ui.components.MizanDialogActions
import com.smartledger.aldaftar.ui.components.MizanDialogCard
import com.smartledger.aldaftar.ui.components.MizanDialogHeader
import com.smartledger.aldaftar.ui.theme.MizanDialogTokens

@Composable
fun CommitmentDeleteConfirmationDialog(
    commitmentName: String?,
    onConfirmDelete: (String) -> Unit,
    onDismiss: () -> Unit
) {
    if (commitmentName == null) return

    MizanAnimatedDialog(
        onDismissRequest = onDismiss
    ) { dismiss ->
        MizanDialogCard(
            maxWidth = MizanDialogTokens.compactMaxWidth,
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp)
        ) {
            MizanDialogHeader(
                title = stringResource(id = R.string.ledger_confirm_delete_commitment_title),
                icon = Icons.Default.Delete,
                iconTint = MaterialTheme.colorScheme.error,
                onCloseClick = dismiss
            )

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.ledger_confirm_delete_commitment_msg),
                    fontSize = 13.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(2.dp))

                MizanDialogActions(
                    confirmText = stringResource(id = R.string.ledger_confirm_delete_btn),
                    onConfirm = {
                        onConfirmDelete(commitmentName)
                        dismiss()
                    },
                    confirmContainerColor = MaterialTheme.colorScheme.error,
                    confirmContentColor = MaterialTheme.colorScheme.onError,
                    cancelText = stringResource(id = R.string.common_cancel),
                    onCancel = dismiss
                )
            }
        }
    }
}

