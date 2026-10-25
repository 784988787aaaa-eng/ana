package com.smartledger.aldaftar.ui.screens.settings.components

import android.widget.Toast
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartledger.aldaftar.R
import com.smartledger.aldaftar.ui.theme.MizanDialogTokens
import com.smartledger.aldaftar.ui.viewmodel.BackupSyncViewModel

@Composable
fun BackupResetConfirmationFlow(
    viewModel: BackupSyncViewModel,
    onDismiss: () -> Unit,
    onSuccessReset: () -> Unit
) {
    val context = LocalContext.current
    var currentStep by remember { mutableStateOf(1) }

    if (currentStep == 1) {
        com.smartledger.aldaftar.ui.components.MizanDeleteConfirmationDialog(
            title = stringResource(R.string.backup_reset1_title),
            message = stringResource(R.string.backup_reset1_desc),
            confirmText = stringResource(R.string.backup_reset_confirm_btn),
            onConfirm = { currentStep = 2 },
            onDismiss = onDismiss
        )
    } else if (currentStep == 2) {
        com.smartledger.aldaftar.ui.components.MizanDeleteConfirmationDialog(
            title = stringResource(R.string.backup_reset2_title),
            message = stringResource(R.string.backup_reset2_desc),
            confirmText = stringResource(R.string.backup_reset_final_btn),
            onConfirm = {
                viewModel.clearLocalCopyAndWipeMemory {
                    Toast.makeText(context, context.getString(R.string.backup_toast_reset_success), Toast.LENGTH_LONG).show()
                    onSuccessReset()
                }
            },
            onDismiss = onDismiss
        )
    }
}
