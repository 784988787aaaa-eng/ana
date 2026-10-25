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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.AutoDelete
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.ui.text.style.TextOverflow
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

    val isFilterActive = selectedFilter != TrashFilterType.ALL
    val isSortCustom = selectedSort != TrashSortType.NEWEST_DELETED
    val isCleanupActive = !autoCleanupPeriod.equals("never", ignoreCase = true) && autoCleanupPeriod.isNotBlank()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 5.dp)
            .horizontalScroll(rememberScrollState()),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // 1. Category / Type Filter Chip
        val filterLabel = when (selectedFilter) {
            TrashFilterType.ALL -> stringResource(id = R.string.trash_filter_type_label, stringResource(id = R.string.trash_filter_all_label))
            TrashFilterType.TRANSACTIONS -> stringResource(id = R.string.trash_filter_transactions)
            TrashFilterType.CUSTOMERS -> stringResource(id = R.string.trash_filter_customers)
        }

        Box {
            Surface(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    showFilterMenu = true
                },
                shape = RoundedCornerShape(10.dp),
                color = if (isFilterActive) {
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.85f)
                } else {
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.42f)
                },
                border = BorderStroke(
                    width = 0.8.dp,
                    color = if (isFilterActive) {
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                    } else {
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                    }
                ),
                modifier = Modifier.height(32.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 9.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Icon(
                        imageVector = when (selectedFilter) {
                            TrashFilterType.ALL -> Icons.Default.Tune
                            TrashFilterType.TRANSACTIONS -> Icons.Default.ReceiptLong
                            TrashFilterType.CUSTOMERS -> Icons.Default.People
                        },
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = filterLabel,
                        fontSize = 11.sp,
                        fontWeight = if (isFilterActive) FontWeight.Bold else FontWeight.SemiBold,
                        color = if (isFilterActive) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = if (isFilterActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            DropdownMenu(
                expanded = showFilterMenu,
                onDismissRequest = { showFilterMenu = false },
                modifier = Modifier
                    .widthIn(min = 175.dp)
                    .background(MaterialTheme.colorScheme.surface)
                    .border(
                        0.5.dp,
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
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
                                modifier = Modifier.size(17.dp)
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
                                    fontSize = 12.sp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                            }
                        },
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onFilterSelected(type)
                            showFilterMenu = false
                        }
                    )
                }
            }
        }

        // 2. Sort Order Chip
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
                shape = RoundedCornerShape(10.dp),
                color = if (isSortCustom) {
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.85f)
                } else {
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.42f)
                },
                border = BorderStroke(
                    width = 0.8.dp,
                    color = if (isSortCustom) {
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                    } else {
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                    }
                ),
                modifier = Modifier.height(32.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 9.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Sort,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = sortLabel,
                        fontSize = 11.sp,
                        fontWeight = if (isSortCustom) FontWeight.Bold else FontWeight.SemiBold,
                        color = if (isSortCustom) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = if (isSortCustom) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            DropdownMenu(
                expanded = showSortMenu,
                onDismissRequest = { showSortMenu = false },
                modifier = Modifier
                    .widthIn(min = 175.dp)
                    .background(MaterialTheme.colorScheme.surface)
                    .border(
                        0.5.dp,
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
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
                                    fontSize = 12.sp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                            }
                        },
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onSortSelected(sortType)
                            showSortMenu = false
                        }
                    )
                }
            }
        }

        // 3. Auto-Cleanup Chip
        val selectedCleanupLabel = when (autoCleanupPeriod.lowercase()) {
            "week", "7_days" -> stringResource(R.string.trash_auto_cleanup_week)
            "month", "30_days" -> stringResource(R.string.trash_auto_cleanup_month)
            "3months", "90_days" -> stringResource(R.string.trash_auto_cleanup_3months)
            "6months", "180_days" -> stringResource(R.string.trash_auto_cleanup_6months)
            "year", "365_days" -> stringResource(R.string.trash_auto_cleanup_year)
            else -> stringResource(R.string.trash_auto_cleanup_never)
        }

        Box {
            Surface(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    showCleanupMenu = true
                },
                shape = RoundedCornerShape(10.dp),
                color = if (isCleanupActive) {
                    MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.65f)
                } else {
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.42f)
                },
                border = BorderStroke(
                    width = 0.8.dp,
                    color = if (isCleanupActive) {
                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.55f)
                    } else {
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                    }
                ),
                modifier = Modifier.height(32.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 9.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoDelete,
                        contentDescription = null,
                        tint = if (isCleanupActive) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = stringResource(R.string.trash_auto_cleanup_label, selectedCleanupLabel),
                        fontSize = 11.sp,
                        fontWeight = if (isCleanupActive) FontWeight.Bold else FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            DropdownMenu(
                expanded = showCleanupMenu,
                onDismissRequest = { showCleanupMenu = false },
                modifier = Modifier
                    .widthIn(min = 175.dp)
                    .background(MaterialTheme.colorScheme.surface)
                    .border(
                        0.5.dp,
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
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
                    val isSelected = autoCleanupPeriod.equals(periodKey, ignoreCase = true)
                    DropdownMenuItem(
                        text = {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = periodName,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                            }
                        },
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onAutoCleanupPeriodChanged(periodKey)
                            showCleanupMenu = false
                        }
                    )
                }
            }
        }

        // Quick Reset Chip when a filter or custom sort is active
        if (isFilterActive || isSortCustom) {
            Surface(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onFilterSelected(TrashFilterType.ALL)
                    onSortSelected(TrashSortType.NEWEST_DELETED)
                },
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.35f),
                border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.35f)),
                modifier = Modifier.height(32.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = stringResource(id = R.string.trash_filter_reset_short),
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error,
                        maxLines = 1
                    )
                }
            }
        }
    }
}
