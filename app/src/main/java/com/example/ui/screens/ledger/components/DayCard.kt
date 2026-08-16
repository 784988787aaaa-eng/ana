package com.example.ui.screens.ledger.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.local.entities.TransactionDb
import com.example.domain.DateUtils
import com.example.domain.getEmojiBgColor
import com.example.domain.model.TransactionType
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.SoftGreen
import com.example.ui.theme.SoftRed
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import com.example.ui.viewmodel.DayLedger
import java.math.BigDecimal

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DayCard(
    dayLedger: DayLedger,
    dayKey: String,
    isDaySelected: Boolean,
    isDaySelectionMode: Boolean,
    isExpanded: Boolean,
    haptic: HapticFeedback,
    currencySymbol: String,
    formatCurrency: (BigDecimal, String) -> String,
    onDeleteTransaction: (String) -> Unit,
    onEditTransaction: (TransactionDb) -> Unit,
    onDayClick: (String) -> Unit,
    onDayLongClick: (String) -> Unit,
    isSelectionMode: Boolean = false,
    selectedTxIds: List<String> = emptyList(),
    onTransactionSelectToggle: (String) -> Unit = {}
) {
    var txIdToDelete by remember { mutableStateOf<String?>(null) }
    val bgMatColor = MaterialTheme.colorScheme.background
    val isDark = remember(bgMatColor) { bgMatColor.run { red < 0.5f } }

    // Alternating gentle cash flow background colors (beautiful light/dark gradients)
    val cardBrush = remember(isDark, isDaySelected, dayLedger.netAmount) {
        if (isDark) {
            if (isDaySelected) {
                com.example.ui.theme.SelectedItemGradientDark
            } else if (dayLedger.netAmount.compareTo(BigDecimal.ZERO) >= 0) {
                com.example.ui.theme.IncomeGradientDark
            } else {
                com.example.ui.theme.ExpenseGradientDark
            }
        } else {
            if (isDaySelected) {
                com.example.ui.theme.SelectedItemGradientLight
            } else if (dayLedger.netAmount.compareTo(BigDecimal.ZERO) >= 0) {
                com.example.ui.theme.IncomeGradientLight
            } else {
                com.example.ui.theme.ExpenseGradientLight
            }
        }
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        border = if (isDaySelected) {
            BorderStroke(1.5.dp, com.example.ui.theme.SelectionGreen)
        } else {
            BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 2.dp)
            .combinedClickable(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onDayClick(dayKey)
                },
                onLongClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onDayLongClick(dayKey)
                }
            )
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(cardBrush)
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left: Net balance indicator & beautiful arrow matching "السجل اليومي" header
                val formattedNetAmount = remember(dayLedger.netAmount, currencySymbol) {
                    (if (dayLedger.netAmount.compareTo(BigDecimal.ZERO) > 0) "+" else "") +
                            formatCurrency(dayLedger.netAmount, currencySymbol)
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = formattedNetAmount,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 13.sp,
                        color = if (dayLedger.netAmount.compareTo(BigDecimal.ZERO) >= 0) SoftGreen else SoftRed
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(EmeraldPrimary.copy(alpha = 0.08f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            tint = EmeraldPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = if (isExpanded) stringResource(id = R.string.ledger_hide) else stringResource(id = R.string.ledger_details_label),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldPrimary
                        )
                    }
                }

                // Right: Day title and Date description along with circular Selection indicator (Checkbox)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = stringResource(id = R.string.ledger_days_prefix, dayLedger.dayNumber, dayLedger.dayOfWeek),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = dayLedger.fullDate,
                            fontSize = 9.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }

                    if (isDaySelectionMode) {
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(if (isDaySelected) com.example.ui.theme.SelectionGreen else MaterialTheme.colorScheme.surface)
                                .border(1.5.dp, com.example.ui.theme.SelectionGreen, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isDaySelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                    }
                }
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant,
                        thickness = 1.dp,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )

                    val sortedTxs = remember(dayLedger.transactions) {
                        dayLedger.transactions.sortedByDescending { it.timestamp }
                    }

                    if (sortedTxs.isEmpty()) {
                        Text(
                            text = stringResource(id = R.string.ledger_no_txs_today),
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            textAlign = TextAlign.Center
                        )
                    } else {
                        sortedTxs.forEach { tx ->
                            val isSelected = selectedTxIds.contains(tx.id)
                            val isIncome = tx.type == TransactionType.INCOME.value
                            val itemBg = remember(isIncome, isDark) {
                                if (isDark) {
                                    if (isIncome) com.example.ui.theme.CreditContainerDark else com.example.ui.theme.DebtContainerDark
                                } else {
                                    if (isIncome) com.example.ui.theme.CreditContainerLight else com.example.ui.theme.DebtContainerLight
                                }
                            }
                            val formattedTxAmount = remember(tx.amount, isIncome, currencySymbol) {
                                (if (isIncome) "+" else "-") +
                                        formatCurrency(tx.amount, currencySymbol)
                            }
                            val formattedTxTime = remember(tx.timestamp) {
                                DateUtils.formatTime24Or12(tx.timestamp)
                            }
                            val parsedEmoji = remember(isIncome) { if (isIncome) "💰" else "🛒" }
                            val emojiBg = remember(parsedEmoji, isDark) { getEmojiBgColor(parsedEmoji, isDark) }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) EmeraldPrimary.copy(alpha = if (isDark) 0.18f else 0.12f) else itemBg)
                                    .combinedClickable(
                                        onClick = {
                                            if (isSelectionMode) {
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                onTransactionSelectToggle(tx.id)
                                            }
                                        },
                                        onLongClick = {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            onTransactionSelectToggle(tx.id)
                                        }
                                    )
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    IconButton(
                                        modifier = Modifier.size(24.dp),
                                        onClick = {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            txIdToDelete = tx.id
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = stringResource(id = R.string.ledger_commitment_delete),
                                            tint = SoftRed,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }

                                    IconButton(
                                        modifier = Modifier.size(24.dp),
                                        onClick = {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            onEditTransaction(tx)
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = stringResource(id = R.string.ledger_edit_transaction_title),
                                            tint = EmeraldPrimary,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(2.dp))

                                    Column(horizontalAlignment = Alignment.Start) {
                                        Text(
                                            text = formattedTxAmount,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = if (isIncome) SoftGreen else SoftRed
                                        )
                                        Text(
                                            text = formattedTxTime,
                                            fontSize = 8.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = tx.description.ifBlank { stringResource(id = R.string.ledger_unspecified_description) },
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            textAlign = TextAlign.Right
                                        )
                                    }

                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .background(emojiBg, RoundedCornerShape(6.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(text = parsedEmoji, fontSize = 11.sp)
                                    }

                                    // Circular Selection Checkbox (Sleek, rounded capsule select indicator)
                                    Box(
                                        modifier = Modifier
                                            .size(18.dp)
                                            .clip(CircleShape)
                                            .background(if (isSelected) EmeraldPrimary else Color.Transparent)
                                            .border(
                                                1.2.dp,
                                                if (isSelected) EmeraldPrimary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                                CircleShape
                                            )
                                            .clickable {
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                onTransactionSelectToggle(tx.id)
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(10.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Compact and gorgeous calculation summaries next to the WhatsApp button
                        val context = LocalContext.current
                        val (dailyIncome, dailyExpense) = remember(dayLedger.transactions) {
                            var inc = BigDecimal.ZERO
                            var exp = BigDecimal.ZERO
                            for (t in dayLedger.transactions) {
                                if (t.type == TransactionType.INCOME.value) {
                                    inc = inc.add(t.amount)
                                } else if (t.type == TransactionType.EXPENSE.value) {
                                    exp = exp.add(t.amount)
                                }
                            }
                            Pair(inc, exp)
                        }
                        val dailyNet = remember(dailyIncome, dailyExpense) {
                            dailyIncome.subtract(dailyExpense)
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 10.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // 1. Dynamic calculations / summary metrics for the day (Spans Full Width!)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Income badge (Full Width Weight 1f)
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center,
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isDark) Color(0xFF14241B) else Color(0xFFE8F5E9))
                                        .border(
                                            1.dp,
                                            if (isDark) Color(0xFF2E7D32).copy(alpha = 0.35f) else Color(0xFF81C784).copy(alpha = 0.45f),
                                            RoundedCornerShape(12.dp)
                                        )
                                        .padding(vertical = 4.dp, horizontal = 4.dp)
                                ) {
                                    Text(
                                        text = stringResource(id = R.string.ledger_daily_income),
                                        fontSize = 10.sp,
                                        color = if (isDark) Color(0xFF81C784) else Color(0xFF2E7D32),
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = formatCurrency(dailyIncome, currencySymbol),
                                        fontSize = 12.sp,
                                        color = if (isDark) Color(0xFF81C784) else Color(0xFF2E7D32),
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }

                                // Expense badge (Full Width Weight 1f)
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center,
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isDark) Color(0xFF2D1A1A) else Color(0xFFFFEBEE))
                                        .border(
                                            1.dp,
                                            if (isDark) Color(0xFFC62828).copy(alpha = 0.35f) else Color(0xFFE57373).copy(alpha = 0.45f),
                                            RoundedCornerShape(12.dp)
                                        )
                                        .padding(vertical = 4.dp, horizontal = 4.dp)
                                ) {
                                    Text(
                                        text = stringResource(id = R.string.ledger_daily_expense),
                                        fontSize = 10.sp,
                                        color = if (isDark) Color(0xFFE57373) else Color(0xFFC62828),
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = formatCurrency(dailyExpense, currencySymbol),
                                        fontSize = 12.sp,
                                        color = if (isDark) Color(0xFFE57373) else Color(0xFFC62828),
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }

                                // Daily Net badge (Full Width Weight 1f)
                                val netColor = if (dailyNet > BigDecimal.ZERO) SoftGreen else if (dailyNet < BigDecimal.ZERO) SoftRed else MaterialTheme.colorScheme.onSurfaceVariant
                                val netBg = if (isDark) {
                                    if (dailyNet > BigDecimal.ZERO) Color(0xFF14241B) else if (dailyNet < BigDecimal.ZERO) Color(0xFF2D1A1A) else Color(0xFF2C2C2C)
                                } else {
                                    if (dailyNet > BigDecimal.ZERO) Color(0xFFE8F5E9) else if (dailyNet < BigDecimal.ZERO) Color(0xFFFFEBEE) else Color(0xFFF5F5F5)
                                }
                                val netSign = if (dailyNet > BigDecimal.ZERO) "+" else ""

                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center,
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(netBg)
                                        .border(
                                            1.dp,
                                            netColor.copy(alpha = if (isDark) 0.35f else 0.45f),
                                            RoundedCornerShape(12.dp)
                                        )
                                        .padding(vertical = 4.dp, horizontal = 4.dp)
                                ) {
                                    Text(
                                        text = stringResource(id = R.string.ledger_daily_net),
                                        fontSize = 10.sp,
                                        color = netColor,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = netSign + formatCurrency(dailyNet, currencySymbol),
                                        fontSize = 12.sp,
                                        color = netColor,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                            }

                            // 2. WhatsApp Share button (Sleek full-width integration)
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF25D366).copy(alpha = 0.08f),
                                border = BorderStroke(1.dp, Color(0xFF25D366).copy(alpha = 0.25f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        val builder = java.lang.StringBuilder()
                                        builder.append(context.getString(R.string.ledger_daily_record_share_title, dayLedger.dayOfWeek, dayLedger.fullDate))
                                        builder.append(context.getString(R.string.ledger_share_total_income, formatCurrency(dailyIncome, currencySymbol)))
                                        builder.append(context.getString(R.string.ledger_share_total_expense, formatCurrency(dailyExpense, currencySymbol)))
                                        builder.append("___________________\n\n")

                                        val txs = dayLedger.transactions.sortedBy { it.timestamp }
                                        if (txs.isEmpty()) {
                                            builder.append(context.getString(R.string.ledger_no_txs_today))
                                        } else {
                                            txs.forEach { tx ->
                                                val isTxIncome = tx.type == TransactionType.INCOME.value
                                                val icon = if (isTxIncome) "🟢 (+)" else "🔴 (-)"
                                                val title = tx.description.ifBlank { if (isTxIncome) context.getString(R.string.transaction_income) else context.getString(R.string.transaction_expense) }
                                                builder.append(context.getString(R.string.ledger_share_tx_format, icon, title, formatCurrency(tx.amount, currencySymbol)))
                                            }
                                        }

                                        val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                            type = "text/plain"
                                            putExtra(android.content.Intent.EXTRA_TEXT, builder.toString())
                                        }
                                        try {
                                            shareIntent.setPackage("com.whatsapp")
                                            context.startActivity(shareIntent)
                                        } catch (e: android.content.ActivityNotFoundException) {
                                            try {
                                                shareIntent.setPackage("com.whatsapp.w4b")
                                                context.startActivity(shareIntent)
                                            } catch (e2: android.content.ActivityNotFoundException) {
                                                shareIntent.setPackage(null)
                                                context.startActivity(android.content.Intent.createChooser(shareIntent, context.getString(R.string.ledger_share_via)))
                                            }
                                        }
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(vertical = 6.dp, horizontal = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text("💬", fontSize = 11.sp, modifier = Modifier.padding(end = 4.dp))
                                    Text(
                                        text = stringResource(id = R.string.ledger_whatsapp_whatsapp),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF25D366)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (txIdToDelete != null) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            AlertDialog(
                onDismissRequest = { txIdToDelete = null },
                title = {
                    Text(
                        text = stringResource(id = R.string.ledger_confirm_delete_tx_title),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Right,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                text = {
                    Text(
                        text = stringResource(id = R.string.ledger_confirm_delete_tx_msg),
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Right,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val id = txIdToDelete
                            if (id != null) {
                                onDeleteTransaction(id)
                            }
                            txIdToDelete = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SoftRed),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = stringResource(id = R.string.ledger_confirm_delete_btn),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = { txIdToDelete = null },
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = stringResource(id = R.string.common_cancel),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 13.sp
                        )
                    }
                },
                shape = RoundedCornerShape(24.dp),
                containerColor = MaterialTheme.colorScheme.surface,
                properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
                modifier = Modifier.fillMaxWidth(0.90f)
            )
        }
    }
}
