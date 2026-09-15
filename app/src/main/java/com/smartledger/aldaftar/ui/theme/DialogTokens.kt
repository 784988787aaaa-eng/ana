package com.smartledger.aldaftar.ui.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartledger.aldaftar.R

/**
 * Universal Dialog & Input Architecture Contract (Phases 1 & 2).
 *
 * Enforces strict geometric bounds, Cairo typography protections,
 * and standard RTL header & input conventions across all dialog surfaces.
 */
object MizanDialogTokens {
    val minWidth: Dp = 280.dp
    val maxWidth: Dp = 360.dp
    val standardMaxWidth: Dp = 360.dp
    val expandedMaxWidth: Dp = 440.dp
    val compactMaxWidth: Dp = 360.dp

    val outerPadding: Dp = 14.dp
    val compactPadding: Dp = 12.dp
    val verticalGap: Dp = 8.dp
    val sectionGap: Dp = 12.dp

    val titleSize: TextUnit = 15.sp
    val bodySize: TextUnit = 13.sp
    val labelSize: TextUnit = 12.sp

    val buttonHeight: Dp = 44.dp
    val inputMinHeight: Dp = 56.dp
    val inputHeight: Dp = 56.dp

    val radius: Dp = 20.dp
    val sheetTopRadius: Dp = 20.dp
    val inputRadius: Dp = 12.dp
    val buttonRadius: Dp = 12.dp

    val tonalElevation: Dp = 6.dp
    val borderWidth: Dp = 0.8.dp

    val shape = RoundedCornerShape(radius)
    val inputShape = RoundedCornerShape(inputRadius)
    val buttonShape = RoundedCornerShape(buttonRadius)

    val inputLineHeight: TextUnit = 22.sp
    val inputTextSize: TextUnit = 14.sp
    val inputLineHeightStyle = LineHeightStyle(
        alignment = LineHeightStyle.Alignment.Center,
        trim = LineHeightStyle.Trim.None
    )

    @Composable
    fun dialogBorder(isDark: Boolean = MaterialTheme.isDark): BorderStroke =
        BorderStroke(
            borderWidth,
            MaterialTheme.colorScheme.outlineVariant.copy(
                alpha = if (isDark) 0.20f else 0.35f
            )
        )
}

/**
 * Universal RTL Dialog Header adhering to the strict architectural directive:
 * - Start (Right in RTL): Semantic icon in a 36dp capsule (10% alpha primary) + Cairo Bold 15sp title.
 * - End (Left in RTL): 32dp circular close button with >= 44dp touch target.
 * - Bottom: 0.5dp divider with 30% alpha outlineVariant, followed by 10dp vertical spacing.
 */
@Composable
fun UniversalDialogHeader(
    title: String,
    icon: ImageVector,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.weight(1f, fill = false)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }
            Text(
                text = title,
                fontSize = MizanDialogTokens.titleSize,
                fontWeight = FontWeight.Bold,
                fontFamily = CairoFontFamily,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Box(
            modifier = Modifier
                .size(44.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = ripple(bounded = false, radius = 22.dp),
                    onClick = onDismiss
                ),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.50f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(id = R.string.desc_close),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(8.dp))
    HorizontalDivider(
        thickness = 0.5.dp,
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.30f)
    )
    Spacer(modifier = Modifier.height(10.dp))
}

/**
 * Universal Dialog Surface Shell with standard width constraints,
 * 20dp symmetric corner radii, tonalElevation 6dp, and IME padding.
 */
@Composable
fun UniversalDialogSurface(
    modifier: Modifier = Modifier,
    isExpanded: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
) {
    val isDark = MaterialTheme.isDark
    Surface(
        modifier = modifier
            .fillMaxWidth(0.92f)
            .widthIn(
                min = MizanDialogTokens.minWidth,
                max = if (isExpanded) MizanDialogTokens.expandedMaxWidth else MizanDialogTokens.standardMaxWidth
            )
            .imePadding(),
        shape = MizanDialogTokens.shape,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = MizanDialogTokens.tonalElevation,
        border = MizanDialogTokens.dialogBorder(isDark)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = MizanDialogTokens.outerPadding,
                    vertical = MizanDialogTokens.compactPadding
                ),
            content = content
        )
    }
}

/**
 * Cairo-optimized input TextStyle that preserves Arabic descenders
 * ("ي", "ر", "ز", "ع") using balanced lineHeight and centered LineHeightStyle.
 */
@Composable
fun arabicInputTextStyle(
    textAlign: TextAlign = TextAlign.Start,
    fontSize: TextUnit = MizanDialogTokens.inputTextSize,
    fontWeight: FontWeight = FontWeight.Normal,
    color: Color = Color.Unspecified
): TextStyle = TextStyle(
    fontFamily = CairoFontFamily,
    fontSize = fontSize,
    fontWeight = fontWeight,
    textAlign = textAlign,
    lineHeight = MizanDialogTokens.inputLineHeight,
    lineHeightStyle = MizanDialogTokens.inputLineHeightStyle,
    color = color
)

/**
 * Standardized OutlinedTextField colors with 1.5dp focus and 1dp idle borders.
 */
@Composable
fun universalTextFieldColors(
    primary: Color = MaterialTheme.colorScheme.primary,
    primaryColor: Color = primary
): TextFieldColors = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = primaryColor,
    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
    focusedLabelColor = primaryColor,
    unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
    cursorColor = primaryColor,
    focusedContainerColor = Color.Transparent,
    unfocusedContainerColor = Color.Transparent
)
