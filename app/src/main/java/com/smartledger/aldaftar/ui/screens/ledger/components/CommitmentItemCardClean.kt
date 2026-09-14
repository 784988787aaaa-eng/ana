package com.smartledger.aldaftar.ui.screens.ledger.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartledger.aldaftar.R
import com.smartledger.aldaftar.data.local.entities.FixedCommitment
import com.smartledger.aldaftar.ui.theme.isDark
import com.smartledger.aldaftar.ui.theme.mizanColors
import androidx.compose.ui.text.style.TextOverflow
import com.smartledger.aldaftar.ui.theme.MizanIconSizes
import com.smartledger.aldaftar.ui.theme.MizanTouchTarget
import java.math.BigDecimal

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
fun CommitmentItemCardClean(
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
    val mizanColors = MaterialTheme.mizanColors
    val isCovered = remaining.compareTo(BigDecimal.ZERO) <= 0
    val progressFraction = if (fc.targetAmount > BigDecimal.ZERO) {
        allocated.divide(fc.targetAmount, 6, java.math.RoundingMode.HALF_UP)
            .coerceIn(BigDecimal.ZERO, BigDecimal.ONE).toFloat()
    } else 0f
    val progressPercent = (progressFraction * 100).toInt()
    val isDark = MaterialTheme.isDark
    val primaryColor = MaterialTheme.colorScheme.primary
    val itemGradient = remember(mizanColors.credit, primaryColor) {
        Brush.horizontalGradient(listOf(mizanColors.credit, primaryColor))
    }

    Card(
        shape = RoundedCornerShape(13.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCovered) mizanColors.creditContainer.copy(alpha = if (isDark) 0.10f else 0.04f)
            else MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            1.dp,
            if (isCovered) mizanColors.creditBorder.copy(alpha = 0.28f)
            else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 9.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(MizanTouchTarget.minimum).clip(CircleShape).clickable {
                        onCheckedChange(fc, !isCovered)
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    },
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier.size(21.dp).clip(CircleShape)
                            .background(if (isCovered) mizanColors.credit else Color.Transparent)
                            .border(1.5.dp, if (isCovered) mizanColors.credit else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = .45f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isCovered) Icon(Icons.Default.Check, null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(13.dp))
                    }
                }

                Text(
                    text = fc.name.toWesternDigits(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = if (isCovered) mizanColors.credit else MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f).padding(horizontal = 4.dp)
                )

                Surface(
                    shape = RoundedCornerShape(7.dp),
                    color = if (isCovered) mizanColors.creditContainer else MaterialTheme.colorScheme.primaryContainer.copy(alpha = .5f)
                ) {
                    Text(
                        text = if (isCovered) "مكتمل" else "${progressPercent}%",
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.5.sp,
                        color = if (isCovered) mizanColors.credit else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
                Spacer(Modifier.width(5.dp))
                Text(
                    text = formatCurrency(remaining.coerceAtLeast(BigDecimal.ZERO), currencySymbol).toWesternDigits(),
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isCovered) mizanColors.credit else MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }

            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.weight(1f).height(5.dp).clip(RoundedCornerShape(3.dp))) {
                    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = .22f)))
                    if (progressFraction > 0f) {
                        Box(
                            Modifier.fillMaxWidth(progressFraction).fillMaxHeight().clip(RoundedCornerShape(3.dp))
                                .background(if (isCovered) Brush.horizontalGradient(listOf(mizanColors.credit, mizanColors.credit)) else itemGradient)
                        )
                    }
                }
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "${formatCurrency(fc.targetAmount, currencySymbol).toWesternDigits()}",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = .8f),
                    maxLines = 1
                )
                IconButton(onClick = { onEditCommitmentClick(fc) }, modifier = Modifier.size(MizanTouchTarget.minimum)) {
                    Icon(Icons.Default.Edit, stringResource(R.string.ledger_edit_commitment_title), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = .7f), modifier = Modifier.size(MizanIconSizes.sm))
                }
                IconButton(onClick = { onDeleteClick(fc); haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove) }, modifier = Modifier.size(MizanTouchTarget.minimum)) {
                    Icon(Icons.Default.Delete, stringResource(R.string.ledger_commitment_delete), tint = MaterialTheme.colorScheme.error.copy(alpha = .75f), modifier = Modifier.size(MizanIconSizes.sm))
                }
                var dragOffset by remember { mutableFloatStateOf(0f) }
                Box(
                    modifier = Modifier.size(MizanTouchTarget.minimum).clip(CircleShape)
                        .clickable { onSetReorderTarget(fc) }
                        .pointerInput(Unit) {
                            detectDragGestures(
                                onDragStart = { dragOffset = 0f },
                                onDrag = { _, dragAmount ->
                                    dragOffset += dragAmount.y
                                    if (dragOffset > 60f) {
                                        dragOffset = 0f
                                        val pos = index + 2
                                        if (pos <= totalCommitmentsCount) {
                                            onReorderCommitment(fc, pos)
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        }
                                    } else if (dragOffset < -60f) {
                                        dragOffset = 0f
                                        val pos = index
                                        if (pos >= 1) {
                                            onReorderCommitment(fc, pos)
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        }
                                    }
                                },
                                onDragEnd = { dragOffset = 0f }
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Menu, stringResource(R.string.ledger_reorder_apply), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = .65f), modifier = Modifier.size(MizanIconSizes.sm))
                }
            }
        }
    }
}
