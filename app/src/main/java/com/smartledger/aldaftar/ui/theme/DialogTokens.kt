package com.smartledger.aldaftar.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Phase 2 release design contract.
 *
 * Dialogs and compact surfaces should use these values rather than inventing
 * one-off dimensions. Touch targets remain >= 44dp; the visual density is
 * achieved through spacing and typography, not by making controls hard to tap.
 */
object MizanDialogTokens {
    val maxWidth: Dp = 380.dp
    val compactMaxWidth: Dp = 360.dp
    val outerPadding: Dp = 14.dp
    val compactPadding: Dp = 12.dp
    val verticalGap: Dp = 8.dp
    val sectionGap: Dp = 12.dp
    val titleSize = 16.sp
    val bodySize = 13.sp
    val labelSize = 12.sp
    val buttonHeight: Dp = 44.dp
    val inputHeight: Dp = 50.dp
    val radius: Dp = 16.dp
    val sheetTopRadius: Dp = 20.dp
    val inputRadius: Dp = 11.dp
    val buttonRadius: Dp = 11.dp

    val shape = RoundedCornerShape(radius)
    val inputShape = RoundedCornerShape(inputRadius)
    val buttonShape = RoundedCornerShape(buttonRadius)
}
