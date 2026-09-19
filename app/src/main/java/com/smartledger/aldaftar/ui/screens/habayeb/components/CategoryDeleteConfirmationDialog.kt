package com.smartledger.aldaftar.ui.screens.habayeb.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartledger.aldaftar.R
import com.smartledger.aldaftar.ui.components.MizanAnimatedDialog
import com.smartledger.aldaftar.ui.components.MizanDialogCard
import com.smartledger.aldaftar.ui.components.MizanDialogHeader
import com.smartledger.aldaftar.ui.theme.MizanDialogTokens

@Composable
fun CategoryDeleteConfirmationDialog(
    categoryName: String,
    activeThemeColor: Color,
    onDismiss: () -> Unit,
    onConfirmDelete: (deleteLinkedAccounts: Boolean) -> Unit
) {
    MizanAnimatedDialog(onDismissRequest = onDismiss) { dismiss ->
        MizanDialogCard(
            maxWidth = MizanDialogTokens.compactMaxWidth,
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp)
        ) {
            MizanDialogHeader(
                title = stringResource(R.string.habayeb_category_delete_confirm, categoryName),
                icon = Icons.Default.Delete,
                iconTint = MaterialTheme.colorScheme.error,
                onCloseClick = dismiss
            )
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { onConfirmDelete(false); dismiss() },
                    modifier = Modifier.fillMaxWidth().height(42.dp),
                    shape = MizanDialogTokens.buttonShape,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    contentPadding = PaddingValues(horizontal = 12.dp)
                ) {
                    Text(
                        stringResource(R.string.habayeb_category_delete_only),
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                Button(
                    onClick = { onConfirmDelete(true); dismiss() },
                    modifier = Modifier.fillMaxWidth().height(42.dp),
                    shape = MizanDialogTokens.buttonShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp)
                ) {
                    Text(
                        stringResource(R.string.habayeb_category_delete_all_accounts),
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                OutlinedButton(
                    onClick = dismiss,
                    modifier = Modifier.fillMaxWidth().height(40.dp),
                    shape = MizanDialogTokens.buttonShape,
                    border = BorderStroke(1.dp, activeThemeColor.copy(alpha = 0.45f)),
                    contentPadding = PaddingValues(horizontal = 12.dp)
                ) {
                    Text(
                        stringResource(R.string.habayeb_category_delete_cancel),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
