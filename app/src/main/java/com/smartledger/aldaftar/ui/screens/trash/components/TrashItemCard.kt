package com.smartledger.aldaftar.ui.screens.trash.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.RestoreFromTrash
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartledger.aldaftar.R
import com.smartledger.aldaftar.data.local.entities.DeletedItemEntity
import com.smartledger.aldaftar.ui.helper.VibrationHelper
import com.smartledger.aldaftar.ui.helper.getInitialColor
import com.smartledger.aldaftar.ui.screens.trash.utils.ParsedTrashData
import com.smartledger.aldaftar.ui.theme.CairoFontFamily
import com.smartledger.aldaftar.ui.theme.MizanDialogTokens
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
    var showDeleteConfirm by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    val primaryColor = MaterialTheme.colorScheme.primary
    val errorColor = MaterialTheme.colorScheme.error
    val isDark = MaterialTheme.isDark
    val creditColor = financialCreditColor(isDark)
    val debtColor = financialDebtColor(isDark)

    val isBundleOrCustomer = item.originalTableName == "habayeb_bundle" || item.originalTableName == "habayeb_customers"
    val isTransaction = item.originalTableName == "habayeb_transactions"

    val avatarKey = if (isTransaction && parsedData.customerName.isNotEmpty()) {
        parsedData.customerName
    } else {
        parsedData.titleText
    }
    val avatarColor = remember(avatarKey) { getInitialColor(avatarKey) }
    val firstLetter = remember(avatarKey) {
        avatarKey.trim().firstOrNull()?.toString()?.uppercase() ?: "؟"
    }

    val transactionLabel = stringResource(R.string.trash_type_transaction)
    val accountLabel = stringResource(R.string.trash_type_account)
    val typeLabel = remember(item.originalTableName, transactionLabel, accountLabel) {
        when (item.originalTableName) {
            "habayeb_transactions" -> transactionLabel
            "habayeb_customers", "habayeb_bundle" -> accountLabel
            else -> item.originalTableName
        }
    }

    val cardBorder = if (isSelected) {
        BorderStroke(1.5.dp, primaryColor)
    } else {
        BorderStroke(0.8.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = if (isDark) 0.35f else 0.25f))
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
                primaryColor.copy(alpha = if (isDark) 0.15f else 0.08f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 0.dp else 0.5.dp),
        border = cardBorder
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
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
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(primaryColor)
                            .border(1.dp, MaterialTheme.colorScheme.onPrimary, CircleShape),
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
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(avatarColor.copy(alpha = 0.12f))
                            .border(1.dp, avatarColor.copy(alpha = 0.5f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = firstLetter,
                            fontFamily = CairoFontFamily,
                            color = avatarColor,
                            fontSize = 14.sp,
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
                            text = parsedData.titleText,
                            fontFamily = CairoFontFamily,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )

                        Surface(
                            shape = RoundedCornerShape(5.dp),
                            color = primaryColor.copy(alpha = 0.1f)
                        ) {
                            Text(
                                text = typeLabel,
                                fontFamily = CairoFontFamily,
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = primaryColor,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                            )
                        }
                    }

                    if (parsedData.subText.isNotEmpty()) {
                        Text(
                            text = parsedData.subText,
                            fontFamily = CairoFontFamily,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = parsedData.parsedDate,
                            fontFamily = CairoFontFamily,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            maxLines = 1
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
                                shape = RoundedCornerShape(4.dp),
                                color = badgeBg
                            ) {
                                Text(
                                    text = badgeText,
                                    fontFamily = CairoFontFamily,
                                    fontSize = 8.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = badgeColor,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                if (parsedData.amountText.isNotEmpty()) {
                    val amountColor = if (parsedData.isExpense) debtColor else creditColor

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Text(
                            text = parsedData.amountText,
                            fontFamily = CairoFontFamily,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            color = amountColor
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
                }

                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(primaryColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                } else if (!isSelectionMode) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onRestore,
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .background(creditColor.copy(alpha = 0.10f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.RestoreFromTrash,
                                    contentDescription = stringResource(id = R.string.trash_action_restore_btn),
                                    tint = creditColor,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        IconButton(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                VibrationHelper.triggerDeleteVibration(context)
                                showDeleteConfirm = true
                            },
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .background(debtColor.copy(alpha = 0.10f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeleteForever,
                                    contentDescription = stringResource(id = R.string.trash_delete_permanently),
                                    tint = debtColor,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            confirmButton = {
                Button(
                    onClick = {
                        VibrationHelper.triggerDeleteVibration(context)
                        onPermanentDelete()
                        showDeleteConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = errorColor),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.height(44.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.trash_delete_permanently),
                        fontFamily = CairoFontFamily,
                        color = MaterialTheme.colorScheme.onError,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showDeleteConfirm = false },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.height(44.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.trash_cancel),
                        fontFamily = CairoFontFamily,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.sp
                    )
                }
            },
            title = {
                Text(
                    text = stringResource(id = R.string.trash_delete_warning_title),
                    fontFamily = CairoFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.error
                )
            },
            text = {
                Text(
                    text = stringResource(id = R.string.trash_delete_warning_desc),
                    fontFamily = CairoFontFamily,
                    fontSize = 12.5.sp,
                    lineHeight = 18.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = MizanDialogTokens.shape
        )
    }
}
