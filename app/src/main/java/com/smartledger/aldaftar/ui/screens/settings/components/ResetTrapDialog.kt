package com.smartledger.aldaftar.ui.screens.settings.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.smartledger.aldaftar.R
import com.smartledger.aldaftar.ui.theme.BrandPrimary
import com.smartledger.aldaftar.ui.theme.DebtRed
import com.smartledger.aldaftar.ui.theme.MizanDialogTokens

@Composable
fun ResetTrapDialog(
    onDismiss: () -> Unit,
    onConfirmDelete: () -> Unit
) {
    com.smartledger.aldaftar.ui.components.MizanAnimatedDialog(
        onDismissRequest = onDismiss
    ) { dismissDialog ->
        androidx.compose.runtime.CompositionLocalProvider(
            androidx.compose.ui.platform.LocalLayoutDirection provides androidx.compose.ui.unit.LayoutDirection.Rtl
        ) {
            com.smartledger.aldaftar.ui.components.MizanDialogCard(
                maxWidth = MizanDialogTokens.compactMaxWidth,
                contentPadding = PaddingValues(
                    horizontal = MizanDialogTokens.dialogHorizontal,
                    vertical = MizanDialogTokens.dialogVertical
                )
            ) {
                com.smartledger.aldaftar.ui.components.MizanDialogHeader(
                    title = stringResource(R.string.settings_trap_dialog_title),
                    icon = androidx.compose.material.icons.Icons.Default.WarningAmber,
                    iconTint = DebtRed,
                    onCloseClick = dismissDialog
                )

                Text(
                    text = stringResource(R.string.settings_trap_dialog_desc),
                    fontSize = 12.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = dismissDialog,
                    colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary),
                    shape = MizanDialogTokens.buttonShape,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(MizanDialogTokens.buttonHeight)
                ) {
                    Text(
                        text = stringResource(R.string.settings_trap_dialog_cancel),
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.5.sp
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                TextButton(
                    onClick = onConfirmDelete,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(R.string.settings_trap_dialog_confirm),
                        color = MaterialTheme.colorScheme.outline,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
