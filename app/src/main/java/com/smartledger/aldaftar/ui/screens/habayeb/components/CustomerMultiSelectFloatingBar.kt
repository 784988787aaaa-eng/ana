package com.smartledger.aldaftar.ui.screens.habayeb.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.smartledger.aldaftar.ui.components.MizanSelectionBar

@Composable
fun CustomerMultiSelectFloatingBar(
    selectedCount: Int,
    activeThemeColor: Color,
    onBulkDelete: () -> Unit,
    onBulkAssignCategory: () -> Unit,
    onCancelSelection: () -> Unit,
    onToggleSelectAll: () -> Unit,
    totalCount: Int
) {
    MizanSelectionBar(
        selectedCount = selectedCount,
        totalCount = totalCount,
        onDismiss = onCancelSelection,
        onToggleAll = onToggleSelectAll,
        onDelete = onBulkDelete,
        modifier = Modifier,
        leadingActions = {
            IconButton(onClick = onBulkAssignCategory, modifier = Modifier.size(40.dp)) {
                Icon(Icons.Default.Folder, contentDescription = null, tint = activeThemeColor, modifier = Modifier.size(19.dp))
            }
        }
    )
}
