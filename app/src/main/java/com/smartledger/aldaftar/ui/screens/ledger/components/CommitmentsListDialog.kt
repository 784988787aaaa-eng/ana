package com.smartledger.aldaftar.ui.screens.ledger.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.smartledger.aldaftar.R
import com.smartledger.aldaftar.data.local.entities.FixedCommitment
import com.smartledger.aldaftar.ui.theme.MizanIconSizes
import com.smartledger.aldaftar.ui.theme.MizanTouchTarget
import java.math.BigDecimal
import com.smartledger.aldaftar.ui.theme.MizanDialogTokens

@Composable
fun CommitmentsListDialog(
    showCommitmentsListSheet: Boolean,
    commitments: List<FixedCommitment>,
    computedCommitments: List<Triple<FixedCommitment, BigDecimal, BigDecimal>>,
    totalCash: BigDecimal,
    currencySymbol: String,
    formatCurrency: (BigDecimal, String) -> String,
    onDismissRequest: () -> Unit,
    onAddCommitmentClick: () -> Unit,
    onEditCommitmentClick: (FixedCommitment) -> Unit,
    onDeleteCommitment: (String) -> Unit,
    onReorderCommitment: (FixedCommitment, Int) -> Unit,
    onCheckedChange: (FixedCommitment, Boolean) -> Unit,
    onSetReorderTarget: (FixedCommitment) -> Unit,
    modifier: Modifier = Modifier
) {
    if (!showCommitmentsListSheet) return

    val context = LocalContext.current
    var commitmentToDelete by remember { mutableStateOf<String?>(null) }

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = true
        )
    ) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
                modifier = modifier
                    .fillMaxWidth(0.92f)
                    .widthIn(max = 420.dp)
                    .heightIn(max = 620.dp)
                    .padding(vertical = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = MizanDialogTokens.compactPadding, vertical = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    CommitmentHeaderClean(
                        onCloseClick = onDismissRequest,
                        onShareClick = {
                            CommitmentShareHelper.shareCommitments(
                                context = context,
                                commitments = commitments,
                                computedCommitments = computedCommitments,
                                totalCash = totalCash,
                                currencySymbol = currencySymbol,
                                formatCurrency = formatCurrency
                            )
                        }
                    )

                    if (commitments.isEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = stringResource(id = R.string.ledger_commitments_empty_title),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = stringResource(id = R.string.ledger_commitments_empty_desc),
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        val (totalTargetSum, totalAllocatedSum, coveredCount) = remember(commitments, computedCommitments) {
                            val target = commitments.fold(BigDecimal.ZERO) { acc, fc -> acc.add(fc.targetAmount) }
                            val allocated = computedCommitments.fold(BigDecimal.ZERO) { acc, triple -> acc.add(triple.second) }
                            val covered = computedCommitments.count { it.third.compareTo(BigDecimal.ZERO) <= 0 }
                            Triple(target, allocated, covered)
                        }

                        CommitmentSummaryGradientCard(
                            totalTargetSum = totalTargetSum,
                            totalAllocatedSum = totalAllocatedSum,
                            coveredCount = coveredCount,
                            totalCount = commitments.size,
                            currencySymbol = currencySymbol,
                            formatCurrency = formatCurrency
                        )

                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(5.dp),
                            modifier = Modifier
                                .weight(1f, fill = false)
                                .fillMaxWidth()
                        ) {
                            itemsIndexed(
                                items = computedCommitments,
                                key = { _, item -> item.first.name }
                            ) { index, (fc, allocated, remaining) ->
                                CommitmentItemCardClean(
                                    index = index,
                                    fc = fc,
                                    allocated = allocated,
                                    remaining = remaining,
                                    totalCash = totalCash,
                                    currencySymbol = currencySymbol,
                                    formatCurrency = formatCurrency,
                                    totalCommitmentsCount = commitments.size,
                                    onCheckedChange = onCheckedChange,
                                    onSetReorderTarget = onSetReorderTarget,
                                    onReorderCommitment = onReorderCommitment,
                                    onEditCommitmentClick = onEditCommitmentClick,
                                    onDeleteClick = { fcToDelete ->
                                        commitmentToDelete = fcToDelete.name
                                    }
                                )
                            }
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                onAddCommitmentClick()
                            }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(MizanIconSizes.sm)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = stringResource(id = R.string.ledger_commitment_add_btn_label),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }

                CommitmentDeleteConfirmationDialog(
                    commitmentName = commitmentToDelete,
                    onConfirmDelete = { name ->
                        onDeleteCommitment(name)
                    },
                    onDismiss = {
                        commitmentToDelete = null
                    }
                )
            }
        }
    }
}
