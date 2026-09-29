package com.smartledger.aldaftar.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartledger.aldaftar.R
import com.smartledger.aldaftar.ui.theme.MizanDialogTokens

/**
 * Unified selection surface for lists throughout the app.
 * Selection is a UI state only; destructive actions remain behind confirmation.
 */
@Composable
fun MizanSelectionBar(
    selectedCount: Int,
    totalCount: Int,
    onDismiss: () -> Unit,
    onToggleAll: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    leadingActions: @Composable RowScope.() -> Unit = {}
) {
    val allSelected = totalCount > 0 && selectedCount >= totalCount
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f)),
        tonalElevation = 1.dp,
        shadowElevation = 3.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            IconButton(onClick = onDismiss, modifier = Modifier.size(40.dp)) {
                Icon(Icons.Default.Close, stringResource(R.string.common_cancel), Modifier.size(19.dp))
            }

            Text(
                text = stringResource(R.string.text_selected_count, selectedCount),
                modifier = Modifier.weight(1f),
                fontSize = 12.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            leadingActions()

            if (onToggleAll != null && totalCount > 0) {
                OutlinedButton(
                    onClick = onToggleAll,
                    modifier = Modifier.height(38.dp),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    contentPadding = PaddingValues(horizontal = 10.dp)
                ) {
                    Icon(
                        if (allSelected) Icons.Default.Check else Icons.Default.SelectAll,
                        contentDescription = null,
                        modifier = Modifier.size(17.dp)
                    )
                    Spacer(Modifier.width(5.dp))
                    Text(
                        text = stringResource(if (allSelected) R.string.habayeb_all_selected else R.string.habayeb_select_all),
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            if (onDelete != null && selectedCount > 0) {
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = stringResource(R.string.habayeb_action_delete),
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
    }
}

@Composable
fun MizanDeleteConfirmationDialog(
    title: String,
    message: String,
    confirmText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    icon: ImageVector = Icons.Default.Delete,
    confirmColor: Color = MaterialTheme.colorScheme.error,
    confirmIcon: ImageVector? = Icons.Default.Delete
) {
    MizanAnimatedDialog(onDismissRequest = onDismiss) { dismiss ->
        MizanDialogCard(
            maxWidth = MizanDialogTokens.compactMaxWidth,
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp)
        ) {
            MizanDialogHeader(
                title = title,
                icon = icon,
                iconTint = confirmColor,
                onCloseClick = dismiss
            )
            Text(
                text = message,
                modifier = Modifier.fillMaxWidth(),
                fontSize = 13.sp,
                lineHeight = 20.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            MizanDialogActions(
                confirmText = confirmText,
                confirmIcon = confirmIcon,
                confirmContainerColor = confirmColor,
                confirmContentColor = MaterialTheme.colorScheme.onError,
                onConfirm = {
                    onConfirm()
                    dismiss()
                },
                cancelText = stringResource(R.string.common_cancel),
                onCancel = dismiss
            )
        }
    }
}
