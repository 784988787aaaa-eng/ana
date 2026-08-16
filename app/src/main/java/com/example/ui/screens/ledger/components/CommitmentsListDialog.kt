package com.example.ui.screens.ledger.components

import android.content.Intent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.R
import com.example.data.local.entities.FixedCommitment
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.SoftGreen
import com.example.ui.theme.SoftRed
import kotlinx.coroutines.launch
import java.math.BigDecimal

private const val AFFORDABLE_INDICATOR_EMOJI = "🟢"

private fun String.toWesternDigits(): String {
    var result = this
    val eastern = charArrayOf('٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩')
    val western = charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9')
    for (i in 0..9) {
        result = result.replace(eastern[i], western[i])
    }
    return result
}

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
    val scope = rememberCoroutineScope()

    val commitmentsScaleFraction = remember { Animatable(0f) }
    var commitmentToDelete by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(showCommitmentsListSheet) {
        commitmentsScaleFraction.animateTo(
            targetValue = 1f,
            animationSpec = tween(350, easing = FastOutSlowInEasing)
        )
    }

    val closeAction = {
        scope.launch {
            commitmentsScaleFraction.animateTo(
                targetValue = 0f,
                animationSpec = tween(250, easing = FastOutSlowInEasing)
            )
            onDismissRequest()
        }
    }

    Dialog(onDismissRequest = { closeAction() }) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                modifier = modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .graphicsLayer(
                        scaleX = commitmentsScaleFraction.value,
                        scaleY = commitmentsScaleFraction.value,
                        alpha = commitmentsScaleFraction.value,
                        transformOrigin = TransformOrigin(0.5f, 1f)
                    )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp, bottom = 12.dp, start = 16.dp, end = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val shareAction = {
                        val builder = StringBuilder()
                        builder.append(context.getString(R.string.ledger_commitment_box_title)).append("\n")
                        var idx = 1
                        commitments.forEach { fc ->
                            val line = context.getString(R.string.ledger_commitment_share_format, idx, fc.name, formatCurrency(fc.targetAmount, currencySymbol))
                            builder.append(line.toWesternDigits())
                            idx++
                        }

                        val totalReq = commitments.fold(BigDecimal.ZERO) { acc, fc -> acc.add(fc.targetAmount) }
                        val totalRemaining = computedCommitments.fold(BigDecimal.ZERO) { acc, triple -> acc.add(triple.third) }

                        builder.append(context.getString(R.string.ledger_commitment_total_req, formatCurrency(totalReq, currencySymbol)).toWesternDigits())
                        builder.append(context.getString(R.string.ledger_commitment_total_current, formatCurrency(totalCash, currencySymbol)).toWesternDigits())
                        builder.append(context.getString(R.string.ledger_commitment_total_remaining, formatCurrency(totalRemaining, currencySymbol)).toWesternDigits())

                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, builder.toString())
                        }
                        try {
                            shareIntent.setPackage("com.whatsapp")
                            context.startActivity(shareIntent)
                        } catch (e: Exception) {
                            shareIntent.setPackage(null)
                            context.startActivity(Intent.createChooser(shareIntent, context.getString(R.string.ledger_share_via)))
                        }
                    }

                    CommitmentHeaderRow(
                        onAddClick = onAddCommitmentClick,
                        onShareClick = shareAction
                    )

                    if (commitments.isEmpty()) {
                        Text(
                            text = stringResource(id = R.string.ledger_commitment_empty_state),
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 32.dp)
                        )
                    } else {
                        val (totalTargetSum, totalAllocatedSum, coveredCount) = remember(commitments, computedCommitments) {
                            val target = commitments.fold(BigDecimal.ZERO) { acc, fc -> acc.add(fc.targetAmount) }
                            val allocated = computedCommitments.fold(BigDecimal.ZERO) { acc, triple -> acc.add(triple.second) }
                            val covered = computedCommitments.count { it.third.compareTo(BigDecimal.ZERO) <= 0 }
                            Triple(target, allocated, covered)
                        }

                        CommitmentSummaryCard(
                            totalTargetSum = totalTargetSum,
                            totalAllocatedSum = totalAllocatedSum,
                            coveredCount = coveredCount,
                            totalCount = commitments.size,
                            currencySymbol = currencySymbol,
                            formatCurrency = formatCurrency
                        )

                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f, fill = false)
                        ) {
                            itemsIndexed(computedCommitments) { index, (fc, allocated, remaining) ->
                                CommitmentItemCard(
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

                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { closeAction() },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(id = R.string.ledger_done_btn), fontSize = 14.sp)
                    }
                }

                if (commitmentToDelete != null) {
                    AlertDialog(
                        onDismissRequest = { commitmentToDelete = null },
                        title = {
                            Text(
                                text = stringResource(id = R.string.ledger_confirm_delete_commitment_title),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Right,
                                modifier = Modifier.fillMaxWidth()
                            )
                        },
                        text = {
                            Text(
                                text = stringResource(id = R.string.ledger_confirm_delete_commitment_msg),
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Right,
                                modifier = Modifier.fillMaxWidth()
                            )
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    val name = commitmentToDelete
                                    if (name != null) {
                                        onDeleteCommitment(name)
                                    }
                                    commitmentToDelete = null
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
                                onClick = { commitmentToDelete = null },
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
    }
}

@Composable
private fun CommitmentHeaderRow(
    onAddClick: () -> Unit,
    onShareClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onAddClick,
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(EmeraldPrimary)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = stringResource(id = R.string.ledger_add_commitment_title),
                tint = Color.White,
                modifier = Modifier.size(22.dp)
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = stringResource(id = R.string.ledger_goals_and_commitments),
                fontWeight = FontWeight.ExtraBold,
                color = EmeraldPrimary,
                fontSize = 17.sp
            )
        }

        val shareBtnBg = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.45f)
        val shareBtnTint = MaterialTheme.colorScheme.onSecondaryContainer
        IconButton(
            onClick = onShareClick,
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(shareBtnBg)
        ) {
            Icon(
                imageVector = Icons.Default.Share,
                contentDescription = stringResource(id = R.string.ledger_whatsapp_whatsapp),
                tint = shareBtnTint,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun CommitmentSummaryCard(
    totalTargetSum: BigDecimal,
    totalAllocatedSum: BigDecimal,
    coveredCount: Int,
    totalCount: Int,
    currencySymbol: String,
    formatCurrency: (BigDecimal, String) -> String
) {
    val overallPercent = if (totalTargetSum > BigDecimal.ZERO) {
        (totalAllocatedSum.toDouble() / totalTargetSum.toDouble() * 100).coerceIn(0.0, 100.0).toInt()
    } else 0

    val isFullyCovered = totalAllocatedSum >= totalTargetSum && totalTargetSum > BigDecimal.ZERO

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isFullyCovered) SoftGreen.copy(alpha = 0.12f) else EmeraldPrimary.copy(alpha = 0.08f)
        ),
        border = BorderStroke(
            1.dp,
            if (isFullyCovered) SoftGreen.copy(alpha = 0.35f) else EmeraldPrimary.copy(alpha = 0.25f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = stringResource(id = R.string.ledger_commitment_covered_count, coveredCount, totalCount).toWesternDigits(),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isFullyCovered) SoftGreen else EmeraldPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "(${formatCurrency(totalAllocatedSum, currencySymbol)} / ${formatCurrency(totalTargetSum, currencySymbol)})".toWesternDigits(),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (isFullyCovered) SoftGreen else EmeraldPrimary
            ) {
                Text(
                    text = stringResource(id = R.string.ledger_commitment_percent_format, overallPercent).toWesternDigits(),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                )
            }
        }
    }
}

@Composable
private fun CommitmentItemCard(
    index: Int,
    fc: FixedCommitment,
    allocated: BigDecimal,
    remaining: BigDecimal,
    totalCash: BigDecimal,
    currencySymbol: String,
    formatCurrency: (BigDecimal, String) -> String,
    totalCommitmentsCount: Int,
    onCheckedChange: (FixedCommitment, Boolean) -> Unit,
    onSetReorderTarget: (FixedCommitment) -> Unit,
    onReorderCommitment: (FixedCommitment, Int) -> Unit,
    onEditCommitmentClick: (FixedCommitment) -> Unit,
    onDeleteClick: (FixedCommitment) -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val isCovered = remaining.compareTo(BigDecimal.ZERO) <= 0
    val progressFraction = if (fc.targetAmount.compareTo(BigDecimal.ZERO) > 0) {
        (allocated.toDouble() / fc.targetAmount.toDouble()).coerceIn(0.0, 1.0).toFloat()
    } else 0f
    val progressPercent = (progressFraction * 100).toInt()

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCovered) SoftGreen.copy(alpha = 0.04f) else MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            1.dp,
            if (isCovered) SoftGreen.copy(alpha = 0.25f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.ledger_commitment_percent_format, progressPercent).toWesternDigits(),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isCovered) SoftGreen else EmeraldPrimary
                    )

                    if (isCovered) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = SoftGreen.copy(alpha = 0.15f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = SoftGreen,
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = stringResource(id = R.string.ledger_commitment_completed),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = SoftGreen
                                )
                            }
                        }
                    } else {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = SoftRed.copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = "-${formatCurrency(remaining, currencySymbol)}".toWesternDigits(),
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = SoftRed,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Column(horizontalAlignment = Alignment.End) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            val neededToComplete = (fc.targetAmount.subtract(fc.currentProgress)).max(BigDecimal.ZERO)
                            val canAffordButNotCovered = !isCovered && totalCash >= neededToComplete
                            if (canAffordButNotCovered) {
                                val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                                val alphaAnim by infiniteTransition.animateFloat(
                                    initialValue = 0.2f,
                                    targetValue = 1.0f,
                                    animationSpec = infiniteRepeatable(
                                        animation = tween(800, easing = LinearEasing),
                                        repeatMode = RepeatMode.Reverse
                                    ),
                                    label = "alpha"
                                )
                                Text(AFFORDABLE_INDICATOR_EMOJI, modifier = Modifier.alpha(alphaAnim), fontSize = 10.sp)
                            }
                            Text(
                                text = fc.name.toWesternDigits(),
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = if (isCovered) SoftGreen else EmeraldPrimary
                            )
                        }
                    }
                    Checkbox(
                        checked = isCovered,
                        onCheckedChange = { checked ->
                            onCheckedChange(fc, checked)
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        },
                        colors = CheckboxDefaults.colors(checkedColor = SoftGreen, checkmarkColor = Color.White),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    var dragOffset by remember { mutableFloatStateOf(0f) }
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                            .clickable {
                                onSetReorderTarget(fc)
                            }
                            .pointerInput(Unit) {
                                detectDragGestures(
                                    onDragStart = { _ -> dragOffset = 0f },
                                    onDrag = { _, dragAmount ->
                                        dragOffset += dragAmount.y
                                        if (dragOffset > 70f) {
                                            dragOffset = 0f
                                            val pos = index + 2
                                            if (pos <= totalCommitmentsCount) {
                                                onReorderCommitment(fc, pos)
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            }
                                        } else if (dragOffset < -70f) {
                                            dragOffset = 0f
                                            val pos = index
                                            if (pos >= 1) {
                                                onReorderCommitment(fc, pos)
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            }
                                        }
                                    },
                                    onDragEnd = { dragOffset = 0f }
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = stringResource(id = R.string.ledger_reorder_apply),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    IconButton(
                        onClick = { onEditCommitmentClick(fc) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = stringResource(id = R.string.ledger_edit_commitment_title),
                            tint = EmeraldPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    IconButton(
                        onClick = {
                            onDeleteClick(fc)
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = stringResource(id = R.string.ledger_commitment_delete),
                            tint = SoftRed,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Text(
                    text = stringResource(id = R.string.ledger_commitment_target_prefix, formatCurrency(fc.targetAmount, currencySymbol)).toWesternDigits(),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (fc.targetAmount.compareTo(BigDecimal.ZERO) > 0) {
                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { progressFraction },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = if (isCovered) SoftGreen else EmeraldPrimary,
                    trackColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)
                )
            }
        }
    }
}
