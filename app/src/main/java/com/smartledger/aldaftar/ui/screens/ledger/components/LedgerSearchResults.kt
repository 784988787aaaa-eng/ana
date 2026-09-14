package com.smartledger.aldaftar.ui.screens.ledger.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartledger.aldaftar.R
import com.smartledger.aldaftar.data.local.entities.TransactionDb
import com.smartledger.aldaftar.presentation.utils.DateUtils
import com.smartledger.aldaftar.ui.theme.BrandPrimary
import com.smartledger.aldaftar.ui.theme.CreditGreen
import com.smartledger.aldaftar.ui.theme.DebtRed
import java.math.BigDecimal

@Composable
fun LedgerSearchResults(
    query: String,
    results: List<TransactionDb>,
    formatCurrency: (BigDecimal) -> String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        if (query.isBlank()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(id = R.string.ledger_search_empty_state),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )
            }
            return
        }

        if (results.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(id = R.string.ledger_search_no_results),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )
            }
            return
        }

        Text(
            text = stringResource(id = R.string.ledger_search_results_count, results.size),
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            itemsIndexed(items = results, key = { _, tx -> tx.id }) { index, tx ->
                SearchResultItem(
                    tx = tx,
                    nextTx = results.getOrNull(index + 1),
                    formatCurrency = formatCurrency
                )
            }
        }
    }
}

@Composable
private fun SearchResultItem(
    tx: TransactionDb,
    nextTx: TransactionDb?,
    formatCurrency: (BigDecimal) -> String
) {
    val context = LocalContext.current
    val dayName = remember(tx.timestamp) { DateUtils.getDayOfWeekArabic(tx.timestamp) }
    val fullDate = remember(tx.timestamp) { DateUtils.formatDateFull(tx.timestamp) }
    val timeStr = remember(tx.timestamp) { DateUtils.formatTime24Or12(tx.timestamp) }
    val formattedAmount = remember(tx.amount, formatCurrency) { formatCurrency(tx.amount) }
    val interval = remember(tx.timestamp, nextTx?.timestamp) {
        if (nextTx != null) DateUtils.formatDurationBetween(tx.timestamp, nextTx.timestamp, context) else ""
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.Start) {
                    Text(
                        text = formattedAmount,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (tx.type == "INCOME") CreditGreen else DebtRed,
                        fontSize = 13.sp
                    )
                    Text(
                        text = timeStr,
                        fontSize = 9.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = tx.description.ifBlank { if (tx.type == "INCOME") stringResource(id = R.string.ledger_category_overall_income) else stringResource(id = R.string.ledger_category_expense) },
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = BrandPrimary,
                        textAlign = TextAlign.Right
                    )
                    Text(
                        text = "$dayName - $fullDate",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        if (nextTx != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp, horizontal = 16.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = interval,
                    fontSize = 9.sp,
                    color = BrandPrimary.copy(alpha = 0.6f),
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(10.dp),
                    tint = BrandPrimary.copy(alpha = 0.4f)
                )
            }
        }
    }
}
