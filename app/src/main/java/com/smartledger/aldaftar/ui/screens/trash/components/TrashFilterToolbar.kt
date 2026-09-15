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
import androidx.compose.material.icons.filled.AutoDelete
import androidx.compose.material.icons.filled.FilterList
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartledger.aldaftar.R
import com.smartledger.aldaftar.ui.screens.TrashFilterType
import com.smartledger.aldaftar.ui.screens.TrashSortType
import com.smartledger.aldaftar.ui.theme.CairoFontFamily

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
    var showSortMenu by remember { mutableStateOf(false) }
    var showCleanupMenu by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 4.dp)
            .horizontalScroll(rememberScrollState()),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        val filters = listOf(
            TrashFilterType.ALL to stringResource(id = R.string.trash_filter_all_label),
            TrashFilterType.TRANSACTIONS to stringResource(id = R.string.trash_filter_transactions),
            TrashFilterType.CUSTOMERS to stringResource(id = R.string.trash_filter_customers)
        )

        filters.forEach { (type, label) ->
            val isSelected = selectedFilter == type
            Surface(
                onClick = {
                    onFilterSelected(if (isSelected && type != TrashFilterType.ALL) TrashFilterType.ALL else type)
                },
                shape = RoundedCornerShape(10.dp),
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                border = if (isSelected) null else BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
                modifier = Modifier.height(32.dp)
            ) {
                Box(
                    modifier = Modifier.padding(horizontal = 11.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        fontFamily = CairoFontFamily,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Box {
            Surface(
                onClick = { showSortMenu = true },
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
                modifier = Modifier.height(32.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 9.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = stringResource(id = R.string.trash_sort_btn_label),
                        fontFamily = CairoFontFamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "▾",
                        fontSize = 9.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }

            DropdownMenu(
                expanded = showSortMenu,
                onDismissRequest = { showSortMenu = false },
                modifier = Modifier
                    .shadow(6.dp, RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(
                        0.5.dp,
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                        RoundedCornerShape(12.dp)
                    )
            ) {
                val sortOptions = listOf(
                    TrashSortType.NEWEST_DELETED to stringResource(id = R.string.trash_sort_newest),
                    TrashSortType.OLDEST_DELETED to stringResource(id = R.string.trash_sort_oldest),
                    TrashSortType.HIGHEST_AMOUNT to stringResource(id = R.string.trash_sort_highest),
                    TrashSortType.ALPHABETICAL to stringResource(id = R.string.trash_sort_alphabetical)
                )

                sortOptions.forEach { (sortType, sortLabel) ->
                    val isSelected = selectedSort == sortType
                    DropdownMenuItem(
                        text = {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = sortLabel,
                                    fontFamily = CairoFontFamily,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 11.5.sp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                                if (isSelected) {
                                    Text(
                                        text = "✔",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        },
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                        onClick = {
                            onSortSelected(sortType)
                            showSortMenu = false
                        }
                    )
                }
            }
        }

        Box {
            val selectedCleanupLabel = when (autoCleanupPeriod) {
                "week" -> stringResource(R.string.trash_auto_cleanup_week)
                "month" -> stringResource(R.string.trash_auto_cleanup_month)
                "3months" -> stringResource(R.string.trash_auto_cleanup_3months)
                "6months" -> stringResource(R.string.trash_auto_cleanup_6months)
                "year" -> stringResource(R.string.trash_auto_cleanup_year)
                else -> stringResource(R.string.trash_auto_cleanup_never)
            }

            Surface(
                onClick = { showCleanupMenu = true },
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
                modifier = Modifier.height(32.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 9.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoDelete,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = stringResource(R.string.trash_auto_cleanup_label, selectedCleanupLabel),
                        fontFamily = CairoFontFamily,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "▾",
                        fontSize = 9.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }

            DropdownMenu(
                expanded = showCleanupMenu,
                onDismissRequest = { showCleanupMenu = false },
                modifier = Modifier
                    .width(170.dp)
                    .shadow(6.dp, RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(
                        0.5.dp,
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                        RoundedCornerShape(12.dp)
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
                                    fontFamily = CairoFontFamily,
                                    fontSize = 11.5.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                                if (isSelected) {
                                    Text(
                                        text = "✔",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        },
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                        onClick = {
                            onAutoCleanupPeriodChanged(periodKey)
                            showCleanupMenu = false
                        }
                    )
                }
            }
        }
    }
}
