package com.smartledger.aldaftar.ui.screens.habayeb.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartledger.aldaftar.R
import com.smartledger.aldaftar.ui.theme.MizanElevation
import com.smartledger.aldaftar.ui.theme.MizanIconSizes
import com.smartledger.aldaftar.ui.theme.MizanRadii
import com.smartledger.aldaftar.ui.theme.MizanTouchTarget

@Composable
fun HabayebSortDropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    financialSortMode: Int,
    historicalSortMode: Int,
    onFinancialSortModeChanged: (Int) -> Unit,
    onHistoricalSortModeChanged: (Int) -> Unit,
    onScrollToTop: () -> Unit,
    neutralWhite: Color = MaterialTheme.colorScheme.surface,
    textPrimary: Color = MaterialTheme.colorScheme.onSurface,
    backgroundLight: Color = MaterialTheme.colorScheme.outlineVariant,
    modifier: Modifier = Modifier
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        modifier = modifier
            .widthIn(min = 180.dp, max = 220.dp)
            .background(MaterialTheme.colorScheme.surface, shape = MizanRadii.shapeLg),
        shape = MizanRadii.shapeLg,
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = MizanElevation.dialog,
        shadowElevation = MizanElevation.dialog,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
    ) {
        val isDefaultActive = financialSortMode == 0 && historicalSortMode == 1
        val isLargestActive = financialSortMode == 1
        val isSmallestActive = financialSortMode == 2
        val isOldestActive = historicalSortMode == 2

        val sortOptions = listOf(
            SortMenuItemData(
                label = stringResource(id = R.string.filter_sort_default),
                isSelected = isDefaultActive,
                onClick = {
                    onFinancialSortModeChanged(0)
                    onHistoricalSortModeChanged(1)
                }
            ),
            SortMenuItemData(
                label = stringResource(id = R.string.filter_sort_largest),
                isSelected = isLargestActive,
                onClick = {
                    onHistoricalSortModeChanged(0)
                    onFinancialSortModeChanged(1)
                }
            ),
            SortMenuItemData(
                label = stringResource(id = R.string.filter_sort_smallest),
                isSelected = isSmallestActive,
                onClick = {
                    onHistoricalSortModeChanged(0)
                    onFinancialSortModeChanged(2)
                }
            ),
            SortMenuItemData(
                label = stringResource(id = R.string.filter_sort_oldest),
                isSelected = isOldestActive,
                onClick = {
                    onFinancialSortModeChanged(0)
                    onHistoricalSortModeChanged(2)
                }
            )
        )

        Column(
            modifier = Modifier
                .padding(vertical = 4.dp, horizontal = 4.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            sortOptions.forEach { item ->
                val bgShape = RoundedCornerShape(10.dp)
                val activeBg = if (item.isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.10f) else Color.Transparent
                val textColor = if (item.isSelected) MaterialTheme.colorScheme.primary else textPrimary
                val fontWeight = if (item.isSelected) FontWeight.SemiBold else FontWeight.Normal

                DropdownMenuItem(
                    text = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = item.label,
                                fontSize = 13.sp,
                                fontWeight = fontWeight,
                                color = textColor
                            )
                            if (item.isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(MizanIconSizes.sm)
                                )
                            }
                        }
                    },
                    onClick = {
                        onDismissRequest()
                        item.onClick()
                        onScrollToTop()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = MizanTouchTarget.minimum)
                        .background(activeBg, shape = bgShape),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
    }
}

private data class SortMenuItemData(
    val label: String,
    val isSelected: Boolean,
    val onClick: () -> Unit
)
