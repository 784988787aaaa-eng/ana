package com.smartledger.aldaftar.ui.screens.ledger.components

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartledger.aldaftar.R
import com.smartledger.aldaftar.data.local.entities.FixedCommitment
import com.smartledger.aldaftar.ui.components.MizanAnimatedDialog
import com.smartledger.aldaftar.ui.components.MizanDialogCard
import com.smartledger.aldaftar.ui.theme.MizanDialogTokens
import com.smartledger.aldaftar.ui.theme.MizanIconSizes
import java.math.BigDecimal

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

    MizanAnimatedDialog(
        onDismissRequest = onDismissRequest
    ) { dismiss ->
        MizanDialogCard(
            maxWidth = 420.dp,
            maxHeight = 620.dp,
            contentPadding = PaddingValues(horizontal = MizanDialogTokens.compactPadding, vertical = 6.dp),
            modifier = modifier
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                CommitmentHeaderClean(
                    onCloseClick = dismiss,
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
                            .padding(vertical = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = stringResource(id = R.string.ledger_commitments_empty_title),
                            fontSize = 13.5.sp,
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
                    shape = MizanDialogTokens.buttonShape,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(MizanDialogTokens.buttonHeight)
                        .clip(MizanDialogTokens.buttonShape)
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

