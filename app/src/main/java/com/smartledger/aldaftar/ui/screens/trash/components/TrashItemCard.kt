package com.smartledger.aldaftar.ui.screens.trash.components

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.RestoreFromTrash
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartledger.aldaftar.R
import com.smartledger.aldaftar.data.local.entities.DeletedItemEntity
import com.smartledger.aldaftar.ui.helper.getInitialColor
import com.smartledger.aldaftar.ui.screens.trash.utils.ParsedTrashData
import com.smartledger.aldaftar.ui.theme.financialCreditColor
import com.smartledger.aldaftar.ui.theme.financialDebtColor
import com.smartledger.aldaftar.ui.theme.isDark

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TrashItemCard(
    item: DeletedItemEntity,
    parsedData: ParsedTrashData,
    isSelected: Boolean,
    isSelectionMode: Boolean = false,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onRestore: () -> Unit,
    onPermanentDelete: () -> Unit,
    onOpenCustomerOverlay: () -> Unit = {},
    onOpenTransactionDetail: () -> Unit = {}
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val isDark = MaterialTheme.isDark
    val creditColor = financialCreditColor(isDark)
    val debtColor = financialDebtColor(isDark)

    val isBundleOrCustomer = item.originalTableName == "habayeb_bundle" || item.originalTableName == "habayeb_customers"
    val isTransaction = item.originalTableName == "habayeb_transactions"

    // Resolve primary headline: For transactions, prioritize the associated account/customer name if available
    val primaryHeading = remember(isTransaction, parsedData.customerName, parsedData.titleText) {
        if (isTransaction && parsedData.customerName.isNotBlank()) {
            parsedData.customerName
        } else {
            parsedData.titleText
        }
    }

    val avatarColor = remember(primaryHeading) { getInitialColor(primaryHeading) }
    val firstLetter = remember(primaryHeading) {
        primaryHeading.trim().firstOrNull()?.toString()?.uppercase() ?: "؟"
    }

    val transactionLabel = stringResource(R.string.trash_type_transaction)
    val accountLabel = stringResource(R.string.trash_type_account)
    val noNotesPlaceholder = stringResource(R.string.trash_no_notes)

    val typeBadgeLabel = remember(item.originalTableName, parsedData.txTypeDisplay, transactionLabel, accountLabel) {
        when (item.originalTableName) {
            "habayeb_transactions" -> parsedData.txTypeDisplay.ifBlank { transactionLabel }
            "habayeb_customers", "habayeb_bundle" -> accountLabel
            else -> item.originalTableName
        }
    }

    // Meaningful note/description (excludes empty or "لا توجد ملاحظات" placeholder)
    val meaningfulNote = remember(parsedData.rawDescription, parsedData.titleText, noNotesPlaceholder) {
        val candidate = parsedData.rawDescription.trim().ifEmpty {
            if (isTransaction) parsedData.titleText.trim() else ""
        }
        if (candidate.isBlank() || candidate == noNotesPlaceholder) "" else candidate
    }

    val amountColor = if (parsedData.isExpense) debtColor else creditColor
    val typeBadgeColor = if (isTransaction) amountColor else primaryColor

    val cardBorder = if (isSelected) {
        BorderStroke(1.5.dp, primaryColor)
    } else {
        BorderStroke(0.8.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = if (isDark) 0.35f else 0.28f))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .combinedClickable(
                onClick = {
                    if (isSelectionMode) {
                        onClick()
                    } else {
                        if (isBundleOrCustomer) {
                            onOpenCustomerOverlay()
                        } else if (isTransaction) {
                            onOpenTransactionDetail()
                        } else {
                            onClick()
                        }
                    }
                },
                onLongClick = onLongClick
            ),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                primaryColor.copy(alpha = if (isDark) 0.18f else 0.08f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 0.dp else 0.5.dp),
        border = cardBorder
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 9.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // =========================================================================
            // Row 1: Primary Identity (Avatar/Checkbox + Heading + Type Badge) & Financial Amount
            // =========================================================================
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (isSelected) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(primaryColor)
                                .border(1.dp, MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f), RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(avatarColor.copy(alpha = 0.12f))
                                .border(1.dp, avatarColor.copy(alpha = 0.55f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = firstLetter,
                                color = avatarColor,
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = primaryHeading,
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false)
                            )

                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = typeBadgeColor.copy(alpha = 0.11f)
                            ) {
                                Text(
                                    text = typeBadgeLabel,
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = typeBadgeColor,
                                    maxLines = 1,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.5.dp)
                                )
                            }

                            if (isBundleOrCustomer && parsedData.bundleTransactions.isNotEmpty()) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f)
                                ) {
                                    Text(
                                        text = stringResource(R.string.trash_tx_count_badge, parsedData.bundleTransactions.size),
                                        fontSize = 9.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                                        maxLines = 1,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.5.dp)
                                    )
                                }
                            }
                        }

                        if (isBundleOrCustomer && parsedData.phone.isNotBlank()) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Phone,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                    modifier = Modifier.size(11.dp)
                                )
                                Text(
                                    text = parsedData.phone,
                                    fontSize = 10.5.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }

                if (parsedData.amountText.isNotEmpty()) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(1.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Text(
                                text = parsedData.amountText,
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Black,
                                color = amountColor,
                                maxLines = 1
                            )
                            if (isTransaction) {
                                Icon(
                                    imageVector = if (parsedData.isExpense) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                                    contentDescription = null,
                                    tint = amountColor,
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                        }

                        if (parsedData.equivalentAmountText.isNotEmpty()) {
                            Text(
                                text = parsedData.equivalentAmountText,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            // =========================================================================
            // Row 2: Flexible Description / Notes (Rendered ONLY when non-empty, up to 2 lines)
            // =========================================================================
            if (meaningfulNote.isNotEmpty() && meaningfulNote != primaryHeading) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.28f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = meaningfulNote,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.88f),
                        lineHeight = 16.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
                    )
                }
            }

            // =========================================================================
            // Row 3: Footer Metadata (Deletion Timestamp + Foreign Rate) & Distinct Actions
            // =========================================================================
            HorizontalDivider(
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = parsedData.parsedDate,
                        fontSize = 10.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (parsedData.isForeign) {
                        val badgeColor = if (parsedData.isRateCalculated) primaryColor else MaterialTheme.colorScheme.onSurfaceVariant
                        val badgeBg = if (parsedData.isRateCalculated) primaryColor.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surfaceVariant
                        val badgeText = if (parsedData.isRateCalculated) {
                            stringResource(R.string.trash_exchange_active, parsedData.exchangeRateVal)
                        } else {
                            stringResource(R.string.trash_exchange_inactive)
                        }

                        Surface(
                            shape = RoundedCornerShape(5.dp),
                            color = badgeBg
                        ) {
                            Text(
                                text = badgeText,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = badgeColor,
                                maxLines = 1,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                            )
                        }
                    }
                }

                if (!isSelectionMode) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Restore Action Pill
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = creditColor.copy(alpha = 0.11f),
                            border = BorderStroke(0.8.dp, creditColor.copy(alpha = 0.35f)),
                            modifier = Modifier
                                .height(30.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable(onClick = onRestore)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 9.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.RestoreFromTrash,
                                    contentDescription = stringResource(id = R.string.trash_action_restore_btn),
                                    tint = creditColor,
                                    modifier = Modifier.size(15.dp)
                                )
                                Text(
                                    text = stringResource(id = R.string.trash_restore_short),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = creditColor
                                )
                            }
                        }

                        // Permanent Delete Button
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = debtColor.copy(alpha = 0.09f),
                            border = BorderStroke(0.8.dp, debtColor.copy(alpha = 0.30f)),
                            modifier = Modifier
                                .size(30.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable(onClick = onPermanentDelete)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.DeleteForever,
                                    contentDescription = stringResource(id = R.string.trash_delete_permanently),
                                    tint = debtColor,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
