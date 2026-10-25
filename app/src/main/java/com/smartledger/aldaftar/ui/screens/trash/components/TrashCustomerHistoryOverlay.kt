package com.smartledger.aldaftar.ui.screens.trash.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.RestoreFromTrash
import androidx.compose.material3.*
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
import com.smartledger.aldaftar.ui.helper.HabayebMathHelper
import com.smartledger.aldaftar.ui.helper.getInitialColor
import com.smartledger.aldaftar.ui.screens.trash.utils.ParsedTrashData
import com.smartledger.aldaftar.ui.theme.financialCreditColor
import com.smartledger.aldaftar.ui.theme.financialDebtColor
import com.smartledger.aldaftar.ui.theme.isDark
import java.math.BigDecimal

@Composable
fun TrashCustomerHistoryOverlay(
    item: DeletedItemEntity,
    parsedData: ParsedTrashData,
    currencySymbol: String,
    onDismiss: () -> Unit,
    onRestoreFullAccount: () -> Unit,
    onDeleteFullAccountPermanently: () -> Unit,
    onRestoreSingleTx: (String) -> Unit
) {
    BackHandler { onDismiss() }

    val primaryColor = MaterialTheme.colorScheme.primary
    val errorColor = MaterialTheme.colorScheme.error
    val isDark = MaterialTheme.isDark
    val creditColor = financialCreditColor(isDark)
    val debtColor = financialDebtColor(isDark)
    val avatarColor = remember(parsedData.titleText) { getInitialColor(parsedData.titleText) }
    val firstLetter = remember(parsedData.titleText) {
        parsedData.titleText.trim().firstOrNull()?.toString()?.uppercase() ?: "؟"
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(id = R.string.trash_back),
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(avatarColor.copy(alpha = 0.15f))
                                .border(1.dp, avatarColor, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = firstLetter,
                                color = avatarColor,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = parsedData.titleText,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (parsedData.phone.isNotBlank()) {
                                Text(
                                    text = parsedData.phone,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = onRestoreFullAccount,
                            colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.RestoreFromTrash,
                                contentDescription = null,
                                modifier = Modifier.size(15.dp),
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = stringResource(id = R.string.trash_action_restore_btn),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }

                        IconButton(
                            onClick = onDeleteFullAccountPermanently,
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(errorColor.copy(alpha = 0.1f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteForever,
                                contentDescription = stringResource(id = R.string.trash_delete_permanently),
                                tint = errorColor,
                                modifier = Modifier.size(17.dp)
                            )
                        }
                    }
                }
            }

            if (parsedData.currencyBreakdown.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    parsedData.currencyBreakdown.forEach { (curr, sum) ->
                        val isNeg = sum < BigDecimal.ZERO
                        val chipBg = if (isNeg) debtColor.copy(alpha = 0.1f) else creditColor.copy(alpha = 0.1f)
                        val chipText = if (isNeg) debtColor else creditColor
                        val formattedSum = HabayebMathHelper.formatSmart(sum.abs())

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = chipBg,
                            border = BorderStroke(0.5.dp, chipText.copy(alpha = 0.3f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = if (sum.compareTo(BigDecimal.ZERO) == 0) {
                                        stringResource(id = R.string.trash_status_balanced)
                                    } else if (isNeg) {
                                        stringResource(id = R.string.trash_status_due_prefix)
                                    } else {
                                        stringResource(id = R.string.trash_status_owed_prefix)
                                    },
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = chipText
                                )
                                Text(
                                    text = "$formattedSum $curr",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = chipText
                                )
                            }
                        }
                    }
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 14.dp, top = 8.dp, end = 14.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(parsedData.bundleTransactions, key = { it.id }) { tx ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
                        border = BorderStroke(0.6.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Text(
                                    text = tx.description,
                                    fontSize = 12.5.sp,
                                    fontWeight = if (!tx.hasNotes) FontWeight.Normal else FontWeight.Bold,
                                    color = if (!tx.hasNotes) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurface,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    lineHeight = 17.sp
                                )

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = tx.dateText,
                                        fontSize = 10.5.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                    )

                                    if (tx.exchangeRateText.isNotEmpty()) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(creditColor.copy(alpha = 0.12f))
                                                .padding(horizontal = 6.dp, vertical = 1.5.dp)
                                        ) {
                                            Text(
                                                text = stringResource(id = R.string.trash_rate_active_format, tx.exchangeRateText),
                                                fontSize = 9.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = creditColor
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.End,
                                    verticalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    val isNegative = tx.isNegative
                                    val txColor = if (isNegative) debtColor else creditColor
                                    val arrowSymbol = if (isNegative) "↗" else "↙"

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                                    ) {
                                        Text(
                                            text = tx.displayAmountText,
                                            fontSize = 13.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = txColor
                                        )
                                        Text(
                                            text = arrowSymbol,
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = txColor
                                        )
                                    }

                                    if (tx.equivalentAmountText.isNotEmpty()) {
                                        Text(
                                            text = tx.equivalentAmountText,
                                            fontSize = 10.5.sp,
                                            color = txColor.copy(alpha = 0.8f)
                                        )
                                    }
                                }

                                IconButton(
                                    onClick = { onRestoreSingleTx(tx.id) },
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(primaryColor.copy(alpha = 0.12f))
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Restore,
                                        contentDescription = stringResource(id = R.string.trash_action_restore_btn),
                                        tint = primaryColor,
                                        modifier = Modifier.size(17.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
