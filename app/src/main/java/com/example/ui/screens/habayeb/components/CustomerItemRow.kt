package com.example.ui.screens.habayeb.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.domain.model.TransactionType
import com.example.ui.helper.AutoScaleText
import com.example.ui.helper.HabayebMathHelper
import com.example.ui.helper.getInitialColor
import com.example.ui.screens.habayeb.utils.HabayebDateFormatter
import com.example.ui.state.CustomerUiState
import java.math.BigDecimal
import java.math.RoundingMode

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CustomerItemRow(
    customer: CustomerUiState,
    isSelected: Boolean,
    activeThemeColor: Color,
    activeSubColor: Color,
    isPinned: Boolean = false,
    isHighlighted: Boolean = false,
    haptic: HapticFeedback,
    onCustomerClick: (CustomerUiState) -> Unit,
    onCustomerLongClick: (String) -> Unit,
    onQuickAdd: (CustomerUiState) -> Unit,
    currentActiveCategory: String? = null,
    onRemoveFromCategory: (() -> Unit)? = null
) {
    val lastTxTime = customer.lastTransactionTimestamp
    val textSecondaryColor = MaterialTheme.colorScheme.onSurfaceVariant
    val formattedDate = remember(lastTxTime) {
        HabayebDateFormatter.formatFullDateTime(lastTxTime)
    }
    val nonZeroForeign = remember(customer.foreignDebts) {
        customer.foreignDebts.filter { entry ->
            entry.value.setScale(4, RoundingMode.HALF_EVEN)
                       .compareTo(BigDecimal.ZERO) != 0
        }
    }
    val verticalPadding = 6.dp

    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f

    val targetContainerColor = when {
        isSelected -> activeThemeColor.copy(alpha = if (isDark) 0.24f else 0.14f)
        isHighlighted -> activeThemeColor.copy(alpha = if (isDark) 0.08f else 0.04f)
        else -> MaterialTheme.colorScheme.surface
    }
    val animatedContainerColor by animateColorAsState(
        targetValue = targetContainerColor,
        animationSpec = tween(durationMillis = 200),
        label = "customerCardBg"
    )

    val targetBorderWidth = if (isSelected) 1.5.dp else if (isHighlighted) 0.5.dp else 0.dp
    val animatedBorderWidth by animateDpAsState(
        targetValue = targetBorderWidth,
        animationSpec = tween(durationMillis = 200),
        label = "customerBorderWidth"
    )

    val targetBorderColor = if (isSelected) {
        activeThemeColor
    } else if (isHighlighted) {
        activeThemeColor.copy(alpha = 0.25f)
    } else {
        Color.Transparent
    }
    val animatedBorderColor by animateColorAsState(
        targetValue = targetBorderColor,
        animationSpec = tween(durationMillis = 200),
        label = "customerBorderColor"
    )

    Card(
        shape = RoundedCornerShape(18.dp),
        border = if (animatedBorderWidth > 0.dp) {
            androidx.compose.foundation.BorderStroke(animatedBorderWidth, animatedBorderColor)
        } else null,
        colors = CardDefaults.cardColors(containerColor = animatedContainerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 1.dp else 3.dp),
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = { onCustomerClick(customer) },
                onLongClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onCustomerLongClick(customer.id)
                }
            )
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = verticalPadding, horizontal = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        CustomerAvatarWithBadge(
                            customerName = customer.name,
                            isSelected = isSelected,
                            activeThemeColor = activeThemeColor,
                            onQuickAdd = { onQuickAdd(customer) }
                        )

                        CustomerInfoSection(
                            customerName = customer.name,
                            isSelected = isSelected,
                            activeThemeColor = activeThemeColor,
                            hasNonZeroForeign = nonZeroForeign.isNotEmpty(),
                            formattedDate = formattedDate,
                            textSecondaryColor = textSecondaryColor,
                            currentActiveCategory = currentActiveCategory,
                            onRemoveFromCategory = onRemoveFromCategory,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    CustomerDebtSummarySection(
                        customer = customer,
                        textSecondaryColor = textSecondaryColor,
                        modifier = Modifier.padding(start = 6.dp)
                    )
                }
            }

            if (isPinned) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(top = 4.dp, start = 8.dp)
                ) {
                    Text(
                        text = "📌",
                        fontSize = 9.sp,
                        modifier = Modifier.graphicsLayer {
                            rotationZ = -15f
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun CustomerAvatarWithBadge(
    customerName: String,
    isSelected: Boolean,
    activeThemeColor: Color,
    onQuickAdd: () -> Unit
) {
    val firstLetter = remember(customerName) {
        customerName.trim().firstOrNull()?.toString()?.uppercase() ?: "؟"
    }
    val avatarColor = remember(customerName) { getInitialColor(customerName) }

    Box(
        modifier = Modifier
            .size(38.dp)
            .clickable { onQuickAdd() },
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            // Selected Check Circle Avatar (Prominent & High-Contrast)
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(activeThemeColor)
                    .border(1.5.dp, Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        } else {
            // Main Avatar circle in the center
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(avatarColor.copy(alpha = 0.12f))
                    .border(0.5.dp, avatarColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = firstLetter,
                    color = avatarColor,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Floating Badge in the bottom-end corner
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .align(Alignment.BottomEnd)
                    .background(activeThemeColor, CircleShape)
                    .border(1.dp, Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(id = R.string.habayeb_add_tx_button_clean),
                    tint = Color.White,
                    modifier = Modifier.size(8.dp)
                )
            }
        }
    }
}

@Composable
private fun CustomerInfoSection(
    customerName: String,
    isSelected: Boolean,
    activeThemeColor: Color,
    hasNonZeroForeign: Boolean,
    formattedDate: String,
    textSecondaryColor: Color,
    currentActiveCategory: String?,
    onRemoveFromCategory: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.Start
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .padding(end = 5.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(activeThemeColor)
                        .padding(horizontal = 4.dp, vertical = 1.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = stringResource(id = R.string.ledger_done_btn),
                        tint = Color.White,
                        modifier = Modifier.size(11.dp)
                    )
                }
            }
            Text(
                text = customerName,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false)
            )
            if (hasNonZeroForeign) {
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(activeThemeColor.copy(alpha = 0.08f), RoundedCornerShape(10.dp))
                        .border(0.5.dp, activeThemeColor.copy(alpha = 0.25f), RoundedCornerShape(10.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(5.dp)
                                .clip(CircleShape)
                                .background(activeThemeColor, CircleShape)
                        )
                        Text(
                            text = stringResource(id = R.string.currency_foreign_cash),
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = activeThemeColor
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(1.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = stringResource(id = R.string.habayeb_last_modified, formattedDate),
                fontSize = 10.sp,
                fontWeight = FontWeight.Normal,
                color = textSecondaryColor
            )

            if (currentActiveCategory != null) {
                val categoryEmoji = remember(currentActiveCategory) {
                    com.example.domain.extractEmoji(currentActiveCategory, "🏷️")
                }
                val isDarkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f
                val categoryBgColor = remember(categoryEmoji, isDarkTheme) {
                    com.example.domain.getEmojiBgColor(categoryEmoji, isDarkTheme)
                }
                val categoryTextColor = if (isDarkTheme) Color.White else Color(0xFF1E293B)

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .widthIn(max = 105.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(categoryBgColor, RoundedCornerShape(6.dp))
                        .padding(horizontal = 4.dp, vertical = 1.dp)
                ) {
                    Text(
                        text = "$categoryEmoji $currentActiveCategory",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = categoryTextColor,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (onRemoveFromCategory != null) {
                        Spacer(modifier = Modifier.width(2.dp))
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .clickable { onRemoveFromCategory() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = null,
                                tint = categoryTextColor.copy(alpha = 0.7f),
                                modifier = Modifier.size(6.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CustomerDebtSummarySection(
    customer: CustomerUiState,
    textSecondaryColor: Color,
    modifier: Modifier = Modifier
) {
    val netDebtVal = customer.displayNetDebt
    val netDebtDecimal = remember(netDebtVal) {
        HabayebMathHelper.toBigDecimal(netDebtVal)
    }
    val isZero = remember(netDebtDecimal) { netDebtDecimal.compareTo(BigDecimal.ZERO) == 0 }
    val isPositive = remember(netDebtDecimal) { netDebtDecimal.compareTo(BigDecimal.ZERO) > 0 }
    val isNegative = remember(netDebtDecimal) { netDebtDecimal.compareTo(BigDecimal.ZERO) < 0 }
    val itemCurrencySymbol = customer.displayCurrencySymbol
    val initialType = customer.originalCustomer.initialType

    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val activeRed = if (isDark) Color(0xFFFF5252) else Color(0xFFDC2626)
    val activeGreen = if (isDark) Color(0xFF34D399) else Color(0xFF059669)

    val (debtColor, isOwedByThem) = remember(initialType, isPositive, isNegative, isZero, activeRed, activeGreen, textSecondaryColor) {
        when (initialType) {
            TransactionType.OWED_BY_THEM.value -> {
                if (isPositive) Pair(activeRed, true)
                else if (isNegative) Pair(activeGreen, false)
                else Pair(textSecondaryColor, null as Boolean?)
            }
            TransactionType.OWED_TO_THEM.value -> {
                if (isNegative) Pair(activeGreen, false)
                else if (isPositive) Pair(activeRed, true)
                else Pair(textSecondaryColor, null as Boolean?)
            }
            else -> Pair(textSecondaryColor, null as Boolean?)
        }
    }

    val resolvedStatusText = when (initialType) {
        TransactionType.OWED_BY_THEM.value -> {
            if (isPositive) stringResource(id = R.string.status_remaining_on_him)
            else if (isNegative) stringResource(id = R.string.status_remaining_for_him)
            else stringResource(id = R.string.habayeb_balanced)
        }
        TransactionType.OWED_TO_THEM.value -> {
            if (isNegative) stringResource(id = R.string.status_remaining_for_him)
            else if (isPositive) stringResource(id = R.string.status_remaining_with_him)
            else stringResource(id = R.string.habayeb_balanced)
        }
        else -> stringResource(id = R.string.habayeb_balanced)
    }

    Column(
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
    ) {
        if (!isZero) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(1.dp)
            ) {
                if (isOwedByThem != null) {
                    Text(
                        text = if (isOwedByThem) "▼" else "▲",
                        color = debtColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(end = 2.dp)
                    )
                }
                AutoScaleText(
                    text = "${HabayebMathHelper.formatSmart(netDebtDecimal.abs())} $itemCurrencySymbol",
                    baseFontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    color = debtColor
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = resolvedStatusText,
                fontSize = 10.sp,
                fontWeight = FontWeight.Normal,
                color = textSecondaryColor
            )
        } else {
            Text(
                text = stringResource(id = R.string.habayeb_status_balanced_short),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = textSecondaryColor
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = stringResource(id = R.string.habayeb_status_balanced),
                fontSize = 10.sp,
                fontWeight = FontWeight.Normal,
                color = textSecondaryColor
            )
        }
    }
}
