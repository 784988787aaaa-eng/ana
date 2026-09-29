package com.smartledger.aldaftar.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Single source of truth for dialog geometry and motion.
 *
 * Dialog 24dp | Inner card 16dp | Actions 12dp
 * Padding 16dp | Header/content gap 16dp
 * Enter 120ms / 0.92f -> 1f | Exit 90ms / 1f -> 0.98f
 *
 * Compatibility aliases point to the canonical tokens so screens can migrate
 * without a broad architectural rewrite.
 */
object MizanDialogTokens {
    val maxWidth: Dp = 380.dp
    val compactMaxWidth: Dp = 360.dp

    val dialogRadius: Dp = 24.dp
    val innerCardRadius: Dp = 16.dp
    val actionRadius: Dp = 12.dp

    val dialogHorizontal: Dp = 16.dp
    val dialogVertical: Dp = 16.dp
    val headerContentGap: Dp = 16.dp

    val enterDuration: Int = 120
    val exitDuration: Int = 90
    const val enterScale: Float = 0.92f
    const val exitScale: Float = 0.98f

    val outerPadding: Dp = dialogHorizontal
    val compactPadding: Dp = 12.dp
    val verticalGap: Dp = 8.dp
    val sectionGap: Dp = 12.dp
    val titleSize = 16.sp
    val bodySize = 13.sp
    val labelSize = 12.sp
    val buttonHeight: Dp = 44.dp
    val inputHeight: Dp = 50.dp
    val radius: Dp = dialogRadius
    val sheetTopRadius: Dp = 20.dp
    val inputRadius: Dp = actionRadius
    val buttonRadius: Dp = actionRadius

    val shape = RoundedCornerShape(dialogRadius)
    val innerCardShape = RoundedCornerShape(innerCardRadius)
    val inputShape = RoundedCornerShape(inputRadius)
    val buttonShape = RoundedCornerShape(actionRadius)
}
