package com.smartledger.aldaftar.ui.screens.trash.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.AutoDelete
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartledger.aldaftar.R
import com.smartledger.aldaftar.ui.screens.TrashFilterType
import com.smartledger.aldaftar.ui.screens.TrashSortType

@Composable
fun TrashFilterToolbar(
    selectedFilter: TrashFilterType,
    selectedSort: TrashSortType,
    autoCleanupPeriod: String,
    onFilterSelected: (TrashFilterType) -> Unit,
    onSortSelected: (TrashSortType) -> Unit,
    onAutoCleanupPeriodChanged: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showFilterMenu by remember { mutableStateOf(false) }
    var showSortMenu by remember { mutableStateOf(false) }
    var showCleanupMenu by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .horizontalScroll(rememberScrollState()),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // ==========================================
        // 1. زر مدمج للتصنيف (الكل، العمليات، الحسابات)
        // ==========================================
        val isFilterActive = selectedFilter != TrashFilterType.ALL
        val filterLabel = when (selectedFilter) {
            TrashFilterType.ALL -> stringResource(id = R.string.trash_filter_all_label)
            TrashFilterType.TRANSACTIONS -> stringResource(id = R.string.trash_filter_transactions)
            TrashFilterType.CUSTOMERS -> stringResource(id = R.string.trash_filter_customers)
        }

        Box {
            Surface(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    showFilterMenu = true
                },
                shape = RoundedCornerShape(12.dp),
                color = if (isFilterActive) {
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.85f)
                } else {
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                },
                border = BorderStroke(
                    width = 1.dp,
                    color = if (isFilterActive) {
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                    } else {
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                    }
                ),
                modifier = Modifier.height(34.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = when (selectedFilter) {
                            TrashFilterType.ALL -> Icons.Default.Tune
                            TrashFilterType.TRANSACTIONS -> Icons.Default.ReceiptLong
                            TrashFilterType.CUSTOMERS -> Icons.Default.People
                        },
                        contentDescription = null,
                        tint = if (isFilterActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(alpha = 0.85f),
                        modifier = Modifier.size(15.dp)
                    )
                    Text(
                        text = stringResource(id = R.string.trash_filter_type_label, filterLabel),
                        fontSize = 11.5.sp,
                        fontWeight = if (isFilterActive) FontWeight.Bold else FontWeight.SemiBold,
                        color = if (isFilterActive) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                    )
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = if (isFilterActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
                        modifier = Modifier.size(15.dp)
                    )
                }
            }

            DropdownMenu(
                expanded = showFilterMenu,
                onDismissRequest = { showFilterMenu = false },
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surface)
                    .border(
                        0.5.dp,
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                        RoundedCornerShape(14.dp)
                    )
            ) {
                val filterOptions = listOf(
                    Triple(TrashFilterType.ALL, stringResource(id = R.string.trash_filter_all_label), Icons.Default.Layers),
                    Triple(TrashFilterType.TRANSACTIONS, stringResource(id = R.string.trash_filter_transactions), Icons.Default.ReceiptLong),
                    Triple(TrashFilterType.CUSTOMERS, stringResource(id = R.string.trash_filter_customers), Icons.Default.People)
                )

                filterOptions.forEach { (type, label, icon) ->
                    val isSelected = selectedFilter == type
                    DropdownMenuItem(
                        leadingIcon = {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        text = {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = label,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 12.5.sp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        },
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp),
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onFilterSelected(type)
                            showFilterMenu = false
                        }
                    )
                }
            }
        }

        // ==========================================
        // 2. زر الترتيب المدمج
        // ==========================================
        val sortLabel = when (selectedSort) {
            TrashSortType.NEWEST_DELETED -> stringResource(id = R.string.trash_sort_newest)
            TrashSortType.OLDEST_DELETED -> stringResource(id = R.string.trash_sort_oldest)
            TrashSortType.HIGHEST_AMOUNT -> stringResource(id = R.string.trash_sort_highest)
            TrashSortType.ALPHABETICAL -> stringResource(id = R.string.trash_sort_alphabetical)
        }

        Box {
            Surface(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    showSortMenu = true
                },
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
                modifier = Modifier.height(34.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Sort,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(15.dp)
                    )
                    Text(
                        text = stringResource(id = R.string.trash_sort_btn_label) + ": " + sortLabel,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
                        modifier = Modifier.size(15.dp)
                    )
                }
            }

            DropdownMenu(
                expanded = showSortMenu,
                onDismissRequest = { showSortMenu = false },
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surface)
                    .border(
                        0.5.dp,
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                        RoundedCornerShape(14.dp)
                    )
            ) {
                val sortOptions = listOf(
                    TrashSortType.NEWEST_DELETED to stringResource(id = R.string.trash_sort_newest),
                    TrashSortType.OLDEST_DELETED to stringResource(id = R.string.trash_sort_oldest),
                    TrashSortType.HIGHEST_AMOUNT to stringResource(id = R.string.trash_sort_highest),
                    TrashSortType.ALPHABETICAL to stringResource(id = R.string.trash_sort_alphabetical)
                )

                sortOptions.forEach { (sortType, label) ->
                    val isSelected = selectedSort == sortType
                    DropdownMenuItem(
                        text = {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = label,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 12.5.sp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        },
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp),
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onSortSelected(sortType)
                            showSortMenu = false
                        }
                    )
                }
            }
        }

        // ==========================================
        // 3. زر التنظيف التلقائي المدمج
        // ==========================================
        val selectedCleanupLabel = when (autoCleanupPeriod) {
            "week" -> stringResource(R.string.trash_auto_cleanup_week)
            "month" -> stringResource(R.string.trash_auto_cleanup_month)
            "3months" -> stringResource(R.string.trash_auto_cleanup_3months)
            "6months" -> stringResource(R.string.trash_auto_cleanup_6months)
            "year" -> stringResource(R.string.trash_auto_cleanup_year)
            else -> stringResource(R.string.trash_auto_cleanup_never)
        }

        Box {
            Surface(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    showCleanupMenu = true
                },
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
                modifier = Modifier.height(34.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoDelete,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(15.dp)
                    )
                    Text(
                        text = stringResource(R.string.trash_auto_cleanup_label, selectedCleanupLabel),
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
                        modifier = Modifier.size(15.dp)
                    )
                }
            }

            DropdownMenu(
                expanded = showCleanupMenu,
                onDismissRequest = { showCleanupMenu = false },
                modifier = Modifier
                    .width(180.dp)
                    .background(MaterialTheme.colorScheme.surface)
                    .border(
                        0.5.dp,
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                        RoundedCornerShape(14.dp)
                    )
            ) {
                val periods = listOf(
                    "week" to stringResource(R.string.trash_auto_cleanup_week),
                    "month" to stringResource(R.string.trash_auto_cleanup_month),
                    "3months" to stringResource(R.string.trash_auto_cleanup_3months),
                    "6months" to stringResource(R.string.trash_auto_cleanup_6months),
                    "year" to stringResource(R.string.trash_auto_cleanup_year),
                    "never" to stringResource(R.string.trash_auto_cleanup_never)
                )

                periods.forEach { (periodKey, periodName) ->
                    val isSelected = autoCleanupPeriod == periodKey
                    DropdownMenuItem(
                        text = {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = periodName,
                                    fontSize = 12.5.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        },
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp),
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onAutoCleanupPeriodChanged(periodKey)
                            showCleanupMenu = false
                        }
                    )
                }
            }
        }
    }
}
