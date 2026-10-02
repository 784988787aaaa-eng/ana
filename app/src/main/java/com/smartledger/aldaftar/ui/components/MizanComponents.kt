package com.smartledger.aldaftar.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartledger.aldaftar.ui.theme.CairoFontFamily
import com.smartledger.aldaftar.ui.theme.MizanElevation
import com.smartledger.aldaftar.ui.theme.MizanIconSizes
import com.smartledger.aldaftar.ui.theme.MizanRadii
import com.smartledger.aldaftar.ui.theme.MizanSpacing
import com.smartledger.aldaftar.ui.theme.MizanTouchTarget
import com.smartledger.aldaftar.ui.theme.mizanColors

/**
 * Mizan Button Variants
 */
enum class MizanButtonVariant {
    Primary,
    Secondary,
    Outlined,
    Danger,
    Text
}

/**
 * Mizan Button Sizes
 */
enum class MizanButtonSize {
    Small,
    Medium,
    Large
}

/**
 * Canonical Mizan Button with unified sizing, typography, tokens, loading state, and haptics.
 */
@Composable
fun MizanButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: MizanButtonVariant = MizanButtonVariant.Primary,
    size: MizanButtonSize = MizanButtonSize.Medium,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    shape: Shape = MizanRadii.shapeMd
) {
    val haptic = LocalHapticFeedback.current
    val height = when (size) {
        MizanButtonSize.Small -> MizanTouchTarget.compactButtonHeight
        MizanButtonSize.Medium -> MizanTouchTarget.standardButtonHeight
        MizanButtonSize.Large -> MizanTouchTarget.largeButtonHeight
    }
    val contentPadding = when (size) {
        MizanButtonSize.Small -> PaddingValues(horizontal = MizanSpacing.md, vertical = 0.dp)
        MizanButtonSize.Medium -> PaddingValues(horizontal = MizanSpacing.lg, vertical = 0.dp)
        MizanButtonSize.Large -> PaddingValues(horizontal = MizanSpacing.xl, vertical = 0.dp)
    }
    val fontSize = when (size) {
        MizanButtonSize.Small -> 12.sp
        MizanButtonSize.Medium -> 13.5.sp
        MizanButtonSize.Large -> 15.sp
    }
    val iconSize = when (size) {
        MizanButtonSize.Small -> MizanIconSizes.xs
        MizanButtonSize.Medium -> MizanIconSizes.sm
        MizanButtonSize.Large -> MizanIconSizes.md
    }

    val handleClick = {
        if (enabled && !isLoading) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onClick()
        }
    }

    when (variant) {
        MizanButtonVariant.Primary -> {
            Button(
                onClick = handleClick,
                modifier = modifier.height(height),
                enabled = enabled && !isLoading,
                shape = shape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                    disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                ),
                contentPadding = contentPadding
            ) {
                ButtonInnerContent(
                    text = text,
                    isLoading = isLoading,
                    leadingIcon = leadingIcon,
                    trailingIcon = trailingIcon,
                    fontSize = fontSize,
                    iconSize = iconSize,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
        MizanButtonVariant.Secondary -> {
            FilledTonalButton(
                onClick = handleClick,
                modifier = modifier.height(height),
                enabled = enabled && !isLoading,
                shape = shape,
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                ),
                contentPadding = contentPadding
            ) {
                ButtonInnerContent(
                    text = text,
                    isLoading = isLoading,
                    leadingIcon = leadingIcon,
                    trailingIcon = trailingIcon,
                    fontSize = fontSize,
                    iconSize = iconSize,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
        MizanButtonVariant.Outlined -> {
            OutlinedButton(
                onClick = handleClick,
                modifier = modifier.height(height),
                enabled = enabled && !isLoading,
                shape = shape,
                border = BorderStroke(
                    1.dp,
                    if (enabled) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                ),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary
                ),
                contentPadding = contentPadding
            ) {
                ButtonInnerContent(
                    text = text,
                    isLoading = isLoading,
                    leadingIcon = leadingIcon,
                    trailingIcon = trailingIcon,
                    fontSize = fontSize,
                    iconSize = iconSize,
                    contentColor = MaterialTheme.colorScheme.primary
                )
            }
        }
        MizanButtonVariant.Danger -> {
            Button(
                onClick = handleClick,
                modifier = modifier.height(height),
                enabled = enabled && !isLoading,
                shape = shape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                ),
                contentPadding = contentPadding
            ) {
                ButtonInnerContent(
                    text = text,
                    isLoading = isLoading,
                    leadingIcon = leadingIcon,
                    trailingIcon = trailingIcon,
                    fontSize = fontSize,
                    iconSize = iconSize,
                    contentColor = MaterialTheme.colorScheme.onError
                )
            }
        }
        MizanButtonVariant.Text -> {
            TextButton(
                onClick = handleClick,
                modifier = modifier.height(height),
                enabled = enabled && !isLoading,
                shape = shape,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary
                ),
                contentPadding = contentPadding
            ) {
                ButtonInnerContent(
                    text = text,
                    isLoading = isLoading,
                    leadingIcon = leadingIcon,
                    trailingIcon = trailingIcon,
                    fontSize = fontSize,
                    iconSize = iconSize,
                    contentColor = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun ButtonInnerContent(
    text: String,
    isLoading: Boolean,
    leadingIcon: ImageVector?,
    trailingIcon: ImageVector?,
    fontSize: androidx.compose.ui.unit.TextUnit,
    iconSize: Dp,
    contentColor: Color
) {
    if (isLoading) {
        CircularProgressIndicator(
            modifier = Modifier.size(iconSize + 2.dp),
            strokeWidth = 2.dp,
            color = contentColor
        )
    } else {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (leadingIcon != null) {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    modifier = Modifier.size(iconSize)
                )
                Spacer(modifier = Modifier.width(MizanSpacing.xs))
            }
            Text(
                text = text,
                fontSize = fontSize,
                fontWeight = FontWeight.Bold,
                fontFamily = CairoFontFamily,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (trailingIcon != null) {
                Spacer(modifier = Modifier.width(MizanSpacing.xs))
                Icon(
                    imageVector = trailingIcon,
                    contentDescription = null,
                    modifier = Modifier.size(iconSize)
                )
            }
        }
    }
}

/**
 * Standard Mizan Card
 */
enum class MizanCardVariant {
    Elevated,
    Outlined,
    Filled,
    Container
}

@Composable
fun MizanCard(
    modifier: Modifier = Modifier,
    variant: MizanCardVariant = MizanCardVariant.Outlined,
    shape: Shape = MizanRadii.shapeMd,
    contentPadding: PaddingValues = PaddingValues(MizanSpacing.md),
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val containerColor = when (variant) {
        MizanCardVariant.Elevated -> MaterialTheme.colorScheme.surface
        MizanCardVariant.Outlined -> MaterialTheme.colorScheme.surface
        MizanCardVariant.Filled -> MaterialTheme.colorScheme.surfaceVariant
        MizanCardVariant.Container -> MaterialTheme.colorScheme.surfaceContainer
    }
    val elevation = when (variant) {
        MizanCardVariant.Elevated -> CardDefaults.elevatedCardElevation(defaultElevation = MizanElevation.cardElevated)
        else -> CardDefaults.cardElevation(defaultElevation = MizanElevation.flat)
    }
    val border = when (variant) {
        MizanCardVariant.Outlined -> BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        else -> null
    }

    val cardModifier = if (onClick != null) {
        modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = ripple(),
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onClick()
            }
        )
    } else {
        modifier
    }

    Card(
        modifier = cardModifier,
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = elevation,
        border = border
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(contentPadding),
            content = content
        )
    }
}

/**
 * Standard Mizan Empty State Composable
 */
@Composable
fun MizanEmptyState(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    emojiOrIcon: String? = "📋",
    iconVector: ImageVector? = null,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(MizanSpacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (iconVector != null) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = iconVector,
                    contentDescription = null,
                    modifier = Modifier.size(MizanIconSizes.lg),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else if (!emojiOrIcon.isNullOrBlank()) {
            Text(
                text = emojiOrIcon,
                fontSize = 44.sp,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(MizanSpacing.md))

        Text(
            text = title,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = CairoFontFamily,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        if (!subtitle.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(MizanSpacing.xs))
            Text(
                text = subtitle,
                fontSize = 12.5.sp,
                fontFamily = CairoFontFamily,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )
        }

        if (actionText != null && onActionClick != null) {
            Spacer(modifier = Modifier.height(MizanSpacing.lg))
            MizanButton(
                text = actionText,
                onClick = onActionClick,
                size = MizanButtonSize.Small,
                variant = MizanButtonVariant.Outlined
            )
        }
    }
}

/**
 * Standard Mizan Loading State Composable
 */
@Composable
fun MizanLoadingState(
    modifier: Modifier = Modifier,
    message: String? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(MizanSpacing.xxl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(36.dp),
            strokeWidth = 3.dp,
            color = MaterialTheme.colorScheme.primary
        )
        if (!message.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(MizanSpacing.md))
            Text(
                text = message,
                fontSize = 13.sp,
                fontFamily = CairoFontFamily,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Standard Mizan Error State Composable
 */
@Composable
fun MizanErrorState(
    message: String,
    modifier: Modifier = Modifier,
    retryText: String? = null,
    onRetry: (() -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(MizanSpacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("⚠️", fontSize = 40.sp)
        Spacer(modifier = Modifier.height(MizanSpacing.sm))
        Text(
            text = message,
            fontSize = 13.5.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = CairoFontFamily,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center
        )
        if (retryText != null && onRetry != null) {
            Spacer(modifier = Modifier.height(MizanSpacing.md))
            MizanButton(
                text = retryText,
                onClick = onRetry,
                size = MizanButtonSize.Small,
                variant = MizanButtonVariant.Outlined
            )
        }
    }
}

/**
 * Standard Mizan Financial Status Badge / Chip
 */
enum class MizanBadgeVariant {
    Credit,
    Debt,
    Neutral,
    Warning,
    Info
}

@Composable
fun MizanBadge(
    text: String,
    modifier: Modifier = Modifier,
    variant: MizanBadgeVariant = MizanBadgeVariant.Neutral,
    icon: ImageVector? = null,
    shape: Shape = MizanRadii.shapePill
) {
    val mizanColors = MaterialTheme.mizanColors
    val (bgColor, textColor, borderColor) = when (variant) {
        MizanBadgeVariant.Credit -> Triple(mizanColors.creditContainer, mizanColors.credit, mizanColors.creditBorder)
        MizanBadgeVariant.Debt -> Triple(mizanColors.debtContainer, mizanColors.debt, mizanColors.debtBorder)
        MizanBadgeVariant.Neutral -> Triple(
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.onSurfaceVariant,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )
        MizanBadgeVariant.Warning -> Triple(mizanColors.warningContainer, mizanColors.warning, mizanColors.warningBorder)
        MizanBadgeVariant.Info -> Triple(mizanColors.infoContainer, mizanColors.info, mizanColors.info)
    }

    Surface(
        modifier = modifier,
        shape = shape,
        color = bgColor,
        border = BorderStroke(0.5.dp, borderColor)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = MizanSpacing.sm, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(MizanSpacing.xxs)
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(MizanIconSizes.xs),
                    tint = textColor
                )
            }
            Text(
                text = text,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = CairoFontFamily,
                color = textColor,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
        }
    }
}
