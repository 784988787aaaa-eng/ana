package com.smartledger.aldaftar.ui.screens.habayeb.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartledger.aldaftar.R
import com.smartledger.aldaftar.ui.state.CustomerUiState

@Composable
fun HabayebListSection(
    listState: LazyListState,
    filteredCustomers: List<CustomerUiState>,
    isInitialized: Boolean = true,
    isLoading: Boolean = false,
    selectedFilterTab: Int,
    selectedCategory: String? = null,
    selectedCustomerIds: List<String>,
    isPrivacyMode: Boolean,
    pinnedCustomerIds: Set<String>,
    isMultiSelectActive: Boolean,
    activeThemeColor: Color,
    activeSubColor: Color,
    currencySymbol: String,
    haptic: HapticFeedback,
    onCustomerClick: (CustomerUiState) -> Unit,
    onCustomerLongClick: (String) -> Unit,
    onQuickAdd: (CustomerUiState) -> Unit,
    getCustomerCategory: (String) -> String?,
    onRemoveFromCategory: (String) -> Unit,
    highlightedCustomerId: String? = null,
    modifier: Modifier = Modifier
) {
    val scrollbarMetrics = remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val visibleItems = layoutInfo.visibleItemsInfo
            val totalItems = layoutInfo.totalItemsCount
            if (visibleItems.isEmpty() || totalItems == 0) {
                Pair(0f, 0f)
            } else {
                val sumHeight = visibleItems.sumOf { it.size }
                val avgItemHeight = sumHeight.toFloat() / visibleItems.size
                val estimatedTotalHeight = avgItemHeight * totalItems
                val estimatedScrollOffset = (listState.firstVisibleItemIndex * avgItemHeight) + listState.firstVisibleItemScrollOffset
                val viewportHeight = layoutInfo.viewportEndOffset - layoutInfo.viewportStartOffset
                val sizeFraction = if (estimatedTotalHeight > 0f) (viewportHeight.toFloat() / estimatedTotalHeight).coerceIn(0.1f, 1f) else 1f
                val maxScrollOffset = estimatedTotalHeight - viewportHeight
                val offsetFraction = if (maxScrollOffset > 0f) (estimatedScrollOffset / maxScrollOffset).coerceIn(0f, 1f) else 0f
                Pair(offsetFraction, sizeFraction)
            }
        }
    }

    val catKey = selectedCategory ?: "all"

    LazyColumn(
        state = listState,
        modifier = modifier
            .drawWithContent {
                drawContent()
                val metrics = scrollbarMetrics.value
                val offsetFraction = metrics.first
                val sizeFraction = metrics.second
                if (sizeFraction < 1.0f) {
                    val thicknessPx = 2.5.dp.toPx()
                    val marginPx = 6.dp.toPx()
                    val thumbLeft = marginPx
                    val thumbHeight = (size.height * sizeFraction).coerceIn(40.dp.toPx(), size.height)
                    val maxScrollableHeight = size.height - thumbHeight
                    val thumbTop = maxScrollableHeight * offsetFraction

                    drawRoundRect(
                        color = activeThemeColor.copy(alpha = 0.85f),
                        topLeft = Offset(x = thumbLeft, y = thumbTop),
                        size = Size(width = thicknessPx, height = thumbHeight),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(1.5.dp.toPx(), 1.5.dp.toPx())
                    )
                }
            },
        contentPadding = PaddingValues(top = 0.dp, bottom = 72.dp)
    ) {
        if (!isInitialized || isLoading) {
            // حالة التهيئة/التحميل (Loading/Uninitialized): يُمنع فيها منعاً باتاً رسم شاشة "قائمة الحسابات فارغة"
            items(count = 7, key = { "placeholder_customer_$it" }) {
                LoadingCustomerPlaceholderRow(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 1.5.dp)
                )
            }
        } else if (filteredCustomers.isEmpty()) {
            // حالة الفراغ الحقيقي (Empty): لا تُعرض إلا بعد انتهاء استعلام قاعدة البيانات وعودة النتيجة الصريحة برقم 0
            item(key = "empty_state_${catKey}_$selectedFilterTab") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillParentMaxHeight(0.6f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🤝", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = when (selectedFilterTab) {
                                1 -> stringResource(id = R.string.habayeb_no_debtors)
                                2 -> stringResource(id = R.string.habayeb_no_creditors)
                                else -> stringResource(id = R.string.habayeb_empty_list)
                            },
                            textAlign = TextAlign.Center,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 20.sp
                        )
                    }
                }
            }
        } else {
            // حالة وجود بيانات (Success/Populated): عرض القائمة فوراً
            items(
                items = filteredCustomers,
                key = { it.id },
                contentType = { "customer_row" }
            ) { customer ->
                val isPinned = pinnedCustomerIds.contains(customer.id)
                val isHighlighted = customer.id == highlightedCustomerId
                val isSelected = selectedCustomerIds.contains(customer.id)
                RenderCustomerRowItem(
                    customer = customer,
                    isPinned = isPinned,
                    isHighlighted = isHighlighted,
                    isSelected = isSelected,
                    activeThemeColor = activeThemeColor,
                    activeSubColor = activeSubColor,
                    haptic = haptic,
                    onCustomerClick = onCustomerClick,
                    onCustomerLongClick = onCustomerLongClick,
                    onQuickAdd = onQuickAdd,
                    getCustomerCategory = getCustomerCategory,
                    onRemoveFromCategory = onRemoveFromCategory
                )
            }
        }
    }
}

@Composable
private fun RenderCustomerRowItem(
    customer: CustomerUiState,
    isPinned: Boolean,
    isHighlighted: Boolean,
    isSelected: Boolean,
    activeThemeColor: Color,
    activeSubColor: Color,
    haptic: HapticFeedback,
    onCustomerClick: (CustomerUiState) -> Unit,
    onCustomerLongClick: (String) -> Unit,
    onQuickAdd: (CustomerUiState) -> Unit,
    getCustomerCategory: (String) -> String?,
    onRemoveFromCategory: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val onRemoveFromCategoryClick = remember(customer.id, onRemoveFromCategory) {
        { onRemoveFromCategory(customer.id) }
    }

    Box(
        modifier = modifier.padding(horizontal = 12.dp, vertical = 1.5.dp)
    ) {
        CustomerItemRow(
            isPinned = isPinned,
            isHighlighted = isHighlighted,
            customer = customer,
            isSelected = isSelected,
            activeThemeColor = activeThemeColor,
            activeSubColor = activeSubColor,
            haptic = haptic,
            onCustomerClick = onCustomerClick,
            onCustomerLongClick = onCustomerLongClick,
            onQuickAdd = onQuickAdd,
            currentActiveCategory = getCustomerCategory(customer.id),
            onRemoveFromCategory = onRemoveFromCategoryClick
        )
    }
}

@Composable
private fun LoadingCustomerPlaceholderRow(
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        ),
        modifier = modifier
            .fillMaxWidth()
            .height(68.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                )
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(
                        modifier = Modifier
                            .width(110.dp)
                            .height(14.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f))
                    )
                    Box(
                        modifier = Modifier
                            .width(70.dp)
                            .height(10.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.07f))
                    )
                }
            }
            Box(
                modifier = Modifier
                    .width(60.dp)
                    .height(16.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
            )
        }
    }
}
