package com.smartledger.aldaftar.ui.screens.habayeb.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.smartledger.aldaftar.ui.components.MizanSelectionBar

@Composable
fun MultiSelectFloatingBar(
    isVisible: Boolean,
    selectedTxIds: List<String>,
    totalTxCount: Int,
    activeThemeColor: Color,
    contentPadding: PaddingValues,
    onCancel: () -> Unit,
    onToggleSelectAll: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
        modifier = modifier.padding(bottom = contentPadding.calculateBottomPadding() + 8.dp)
    ) {
        MizanSelectionBar(
            selectedCount = selectedTxIds.size,
            totalCount = totalTxCount,
            onDismiss = onCancel,
            onToggleAll = onToggleSelectAll,
            onDelete = onDelete
        )
    }
}
