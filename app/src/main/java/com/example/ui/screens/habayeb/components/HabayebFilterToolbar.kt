package com.example.ui.screens.habayeb.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.R
import com.example.data.local.entities.CustomCategory
import com.example.ui.viewmodel.FinanceConstants
import kotlin.math.abs

/**
 * شريط التصفية المجهري المطور (Micro Filter Ribbon):
 * يجمع الفرز الذكي والتصنيفات الحرة في صف واحد متناسق.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HabayebFilterToolbar(
    selectedCategory: String?,
    customCategories: List<CustomCategory>,
    orderedCategories: List<String>,
    categoryCounts: Map<String, Int>,
    activeCustomersCount: Int,
    financialSortMode: Int,
    onFinancialSortModeChanged: (Int) -> Unit,
    historicalSortMode: Int,
    onHistoricalSortModeChanged: (Int) -> Unit,
    activeThemeColor: Color,
    activeSubColor: Color,
    haptic: HapticFeedback,
    onScrollToTop: () -> Unit,
    onCategorySelected: (String?) -> Unit,
    onAddCategoryClick: () -> Unit,
    onRenameCategory: (CustomCategory, String) -> Unit,
    onDeleteCategory: (CustomCategory, Boolean) -> Unit,
    onMoveCategoryLeft: (String) -> Unit,
    onMoveCategoryRight: (String) -> Unit,
    onReorderCategories: (List<String>) -> Unit,
    closedCategoryName: String,
    onRenameClosedCategory: (String) -> Unit
) {
    val textPrimary = MaterialTheme.colorScheme.onSurface
    val neutralWhite = MaterialTheme.colorScheme.surface
    val backgroundLight = MaterialTheme.colorScheme.surfaceVariant

    var isMenuExpanded by remember { mutableStateOf(false) }
    var activeCategoryOptions by remember { mutableStateOf<String?>(null) }
    var categoryToDelete by remember { mutableStateOf<CustomCategory?>(null) }

    var localCategories by remember(orderedCategories) { mutableStateOf(orderedCategories.toList()) }
    var draggedIndex by remember { mutableStateOf<Int?>(null) }
    var dragOffset by remember { mutableStateOf(0f) }
    var totalDragDistance by remember { mutableStateOf(0f) }

    val itemPositions = remember { mutableStateMapOf<Int, Pair<Float, Float>>() }

    BackHandler(enabled = activeCategoryOptions != null) {
        activeCategoryOptions = null
    }

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(34.dp)
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Sort Icon
            Box {
                CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 0.dp) {
                    IconButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            isMenuExpanded = true
                        },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SwapVert,
                            contentDescription = stringResource(id = R.string.filter_sort_default),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                DropdownMenu(
                    expanded = isMenuExpanded,
                    onDismissRequest = { isMenuExpanded = false },
                    modifier = Modifier.background(neutralWhite)
                ) {
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = stringResource(id = R.string.filter_sort_default),
                                fontSize = 12.sp,
                                fontWeight = if (financialSortMode == 0 && historicalSortMode == 1) FontWeight.Bold else FontWeight.Normal,
                                color = textPrimary
                            )
                        },
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onFinancialSortModeChanged(0)
                            onHistoricalSortModeChanged(1)
                            isMenuExpanded = false
                            onScrollToTop()
                        }
                    )
                    HorizontalDivider(color = backgroundLight)
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = stringResource(id = R.string.filter_sort_largest),
                                fontSize = 12.sp,
                                fontWeight = if (financialSortMode == 1) FontWeight.Bold else FontWeight.Normal,
                                color = textPrimary
                            )
                        },
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onHistoricalSortModeChanged(0)
                            onFinancialSortModeChanged(1)
                            isMenuExpanded = false
                            onScrollToTop()
                        }
                    )
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = stringResource(id = R.string.filter_sort_smallest),
                                fontSize = 12.sp,
                                fontWeight = if (financialSortMode == 2) FontWeight.Bold else FontWeight.Normal,
                                color = textPrimary
                            )
                        },
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onHistoricalSortModeChanged(0)
                            onFinancialSortModeChanged(2)
                            isMenuExpanded = false
                            onScrollToTop()
                        }
                    )
                    HorizontalDivider(color = backgroundLight)
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = stringResource(id = R.string.filter_sort_oldest),
                                fontSize = 12.sp,
                                fontWeight = if (historicalSortMode == 2) FontWeight.Bold else FontWeight.Normal,
                                color = textPrimary
                            )
                        },
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onFinancialSortModeChanged(0)
                            onHistoricalSortModeChanged(2)
                            isMenuExpanded = false
                            onScrollToTop()
                        }
                    )
                }
            }

            // "All" Category Chip
            CustomCategoryChip(
                selected = selectedCategory == null,
                label = "${stringResource(id = R.string.category_default_all)} ($activeCustomersCount)",
                activeThemeColor = activeThemeColor,
                onClick = { onCategorySelected(null) },
                onLongClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                }
            )

            // Dynamic Categories Ribbon
            Row(
                modifier = Modifier
                    .weight(1f)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                localCategories.forEachIndexed { index, catKey ->
                    key(catKey) {
                        val isDragged = (draggedIndex == index)
                        val dragModifier = remember(catKey, isDragged) {
                            Modifier
                                .zIndex(if (isDragged) 10f else 1f)
                                .graphicsLayer {
                                    translationX = if (isDragged) dragOffset else 0f
                                }
                                .onGloballyPositioned { coordinates ->
                                    if (!isDragged) {
                                        val left = coordinates.positionInParent().x
                                        val right = left + coordinates.size.width
                                        itemPositions[index] = Pair(left, right)
                                    }
                                }
                                .pointerInput(catKey) {
                                    detectDragGesturesAfterLongPress(
                                        onDragStart = { _ ->
                                            draggedIndex = index
                                            dragOffset = 0f
                                            totalDragDistance = 0f
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        },
                                        onDrag = { change, dragAmount ->
                                            change.consume()
                                            val currentIndex = draggedIndex ?: return@detectDragGesturesAfterLongPress
                                            dragOffset += dragAmount.x
                                            totalDragDistance += abs(dragAmount.x)

                                            val currentPos = itemPositions[currentIndex]
                                            if (currentPos != null) {
                                                val itemWidth = currentPos.second - currentPos.first
                                                val originalCenter = currentPos.first + itemWidth / 2f
                                                val currentCenter = originalCenter + dragOffset

                                                for (i in localCategories.indices) {
                                                    if (i == currentIndex) continue
                                                    val otherPos = itemPositions[i] ?: continue
                                                    val otherWidth = otherPos.second - otherPos.first
                                                    val otherCenter = otherPos.first + otherWidth / 2f

                                                    if ((i < currentIndex && currentCenter < otherCenter) || 
                                                        (i > currentIndex && currentCenter > otherCenter)) {
                                                        
                                                        val newList = localCategories.toMutableList()
                                                        val temp = newList[currentIndex]
                                                        newList[currentIndex] = newList[i]
                                                        newList[i] = temp
                                                        localCategories = newList

                                                        val distance = otherCenter - originalCenter
                                                        dragOffset -= distance
                                                        draggedIndex = i

                                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                        break
                                                    }
                                                }
                                            }
                                        },
                                        onDragEnd = {
                                            if (totalDragDistance < 15f) {
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                activeCategoryOptions = catKey
                                            } else {
                                                onReorderCategories(localCategories)
                                            }
                                            draggedIndex = null
                                            dragOffset = 0f
                                        },
                                        onDragCancel = {
                                            draggedIndex = null
                                            dragOffset = 0f
                                        }
                                    )
                                }
                        }

                        if (catKey == FinanceConstants.CATEGORY_CLOSED) {
                            val count = categoryCounts[FinanceConstants.CATEGORY_CLOSED] ?: 0
                            val isSelected = selectedCategory == FinanceConstants.CATEGORY_CLOSED
                            CustomCategoryChip(
                                selected = isSelected,
                                label = "$closedCategoryName ($count)",
                                activeThemeColor = activeThemeColor,
                                modifier = dragModifier,
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    onCategorySelected(if (isSelected) null else FinanceConstants.CATEGORY_CLOSED)
                                },
                                onLongClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    activeCategoryOptions = FinanceConstants.CATEGORY_CLOSED
                                }
                            )
                        } else {
                            val customCat = customCategories.find { it.name == catKey }
                            if (customCat != null) {
                                val count = categoryCounts[catKey] ?: 0
                                val isSelected = selectedCategory == catKey
                                CustomCategoryChip(
                                    selected = isSelected,
                                    label = "$catKey ($count)",
                                    activeThemeColor = activeThemeColor,
                                    modifier = dragModifier,
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        onCategorySelected(if (isSelected) null else catKey)
                                    },
                                    onLongClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        activeCategoryOptions = catKey
                                    }
                                )
                            }
                        }
                    }
                }

                // Add Category Button (+)
                CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 0.dp) {
                    IconButton(
                        onClick = onAddCategoryClick,
                        modifier = Modifier
                            .size(24.dp)
                            .padding(horizontal = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = stringResource(R.string.habayeb_category_add_desc),
                            tint = activeThemeColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        // Category Options Sub-panel
        activeCategoryOptions?.let { categoryKey ->
            Spacer(modifier = Modifier.height(4.dp))
            CategoryOptionsPanel(
                categoryKey = categoryKey,
                closedCategoryName = closedCategoryName,
                customCategories = customCategories,
                activeThemeColor = activeThemeColor,
                onDismiss = { activeCategoryOptions = null },
                onRename = onRenameCategory,
                onRenameClosed = onRenameClosedCategory,
                onDelete = { cat -> categoryToDelete = cat },
                onMoveLeft = onMoveCategoryLeft,
                onMoveRight = onMoveCategoryRight
            )
        }
    }

    // Category Delete Confirmation Dialog
    categoryToDelete?.let { cat ->
        CategoryDeleteConfirmationDialog(
            categoryName = cat.name,
            activeThemeColor = activeThemeColor,
            onDismiss = { categoryToDelete = null },
            onConfirmDelete = { deleteLinkedAccounts ->
                onDeleteCategory(cat, deleteLinkedAccounts)
                categoryToDelete = null
                activeCategoryOptions = null
            }
        )
    }
}

/**
 * شريحة تصنيف مخصصة مبدعة
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CustomCategoryChip(
    selected: Boolean,
    label: String,
    activeThemeColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val targetContainerColor = if (selected) activeThemeColor else if (isDark) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant
    val targetTextColor = if (selected) MaterialTheme.colorScheme.onPrimary else if (isDark) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant

    val containerColor by animateColorAsState(targetValue = targetContainerColor, animationSpec = tween(200), label = "chipBg")
    val textColor by animateColorAsState(targetValue = targetTextColor, animationSpec = tween(200), label = "chipText")

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = containerColor,
        modifier = modifier
            .height(28.dp)
            .clip(RoundedCornerShape(8.dp))
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                fontSize = 9.5.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = textColor
            )
        }
    }
}

/**
 * شريط العمليات المتعددة العائم
 */
@Composable
fun CustomerMultiSelectFloatingBar(
    selectedCount: Int,
    activeThemeColor: Color,
    onBulkDelete: () -> Unit,
    onBulkAssignCategory: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 12.dp)
            .height(48.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(id = R.string.multi_select_count, selectedCount),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBulkAssignCategory, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Default.Folder,
                        contentDescription = null,
                        tint = activeThemeColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
                IconButton(onClick = onBulkDelete, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
