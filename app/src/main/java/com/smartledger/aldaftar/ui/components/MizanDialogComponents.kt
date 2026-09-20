package com.smartledger.aldaftar.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartledger.aldaftar.R
import com.smartledger.aldaftar.ui.theme.MizanDialogTokens

/**
 * Standard Dialog Container Surface
 * Enforces unified border, corner radius, surface color, elevation, and RTL alignment.
 */
@Composable
fun MizanDialogCard(
    modifier: Modifier = Modifier,
    maxWidth: Dp = MizanDialogTokens.maxWidth,
    maxHeight: Dp = Dp.Unspecified,
    contentPadding: PaddingValues = PaddingValues(
        horizontal = MizanDialogTokens.dialogHorizontal,
        vertical = MizanDialogTokens.dialogVertical
    ),
    content: @Composable ColumnScope.() -> Unit
) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        val sizeModifier = if (maxHeight != Dp.Unspecified) {
            modifier
                .fillMaxWidth(0.92f)
                .widthIn(max = maxWidth)
                .heightIn(max = maxHeight)
        } else {
            modifier
                .fillMaxWidth(0.92f)
                .widthIn(max = maxWidth)
        }

        Surface(
            modifier = sizeModifier
                .navigationBarsPadding()
                .imePadding(),
            shape = RoundedCornerShape(MizanDialogTokens.dialogRadius),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
            ),
            shadowElevation = 8.dp,
            tonalElevation = 2.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(contentPadding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(MizanDialogTokens.verticalGap),
                content = content
            )
        }
    }
}

/**
 * Standard Dialog Header
 * Start: Icon badge + Title + Subtitle
 * End: Uniform close button or custom action
 */
@Composable
fun MizanDialogHeader(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    icon: ImageVector? = null,
    iconTint: Color = MaterialTheme.colorScheme.primary,
    isCentered: Boolean = false,
    onCloseClick: (() -> Unit)? = null,
    closeButtonAlignment: Alignment = Alignment.TopEnd,
    endAction: (@Composable () -> Unit)? = null
) {
    val haptic = LocalHapticFeedback.current

    if (isCentered) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .padding(bottom = MizanDialogTokens.headerContentGap - MizanDialogTokens.verticalGap)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = if (onCloseClick != null || endAction != null) 36.dp else 0.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (icon != null) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(iconTint.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = iconTint,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (!subtitle.isNullOrBlank()) {
                    Text(
                        text = subtitle,
                        fontSize = 11.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        lineHeight = 16.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            if (endAction != null) {
                Box(
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    endAction()
                }
            } else if (onCloseClick != null) {
                IconButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onCloseClick?.invoke()
                    },
                    modifier = Modifier
                        .align(closeButtonAlignment)
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(R.string.desc_close),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    } else {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .heightIn(min = 40.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (icon != null) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(iconTint.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = iconTint,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Column(
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = title,
                        fontSize = 15.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (!subtitle.isNullOrBlank()) {
                        Text(
                            text = subtitle,
                            fontSize = 11.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            if (endAction != null) {
                endAction()
            } else if (onCloseClick != null) {
                IconButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onCloseClick()
                    },
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(R.string.desc_close),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

/**
 * Standard inner content card.
 */
@Composable
fun MizanDialogInnerCard(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(12.dp),
    containerColor: Color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.34f),
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MizanDialogTokens.innerCardShape,
        color = containerColor,
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(contentPadding),
            content = content
        )
    }
}

/**
 * Standard Dialog Action Buttons Row
 * Cancel on start/left, Confirm/Save on end/right, with consistent 44dp height and tokens.
 */
@Composable
fun MizanDialogActions(
    confirmText: String,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
    confirmEnabled: Boolean = true,
    confirmIcon: ImageVector? = null,
    confirmColor: Color = MaterialTheme.colorScheme.primary,
    confirmContainerColor: Color = confirmColor,
    confirmContentColor: Color = MaterialTheme.colorScheme.onPrimary,
    isConfirmLoading: Boolean = false,
    cancelText: String = stringResource(R.string.common_cancel),
    onCancel: (() -> Unit)? = null,
    extraActionText: String? = null,
    onExtraAction: (() -> Unit)? = null,
    extraActionColor: Color = MaterialTheme.colorScheme.error
) {
    val finalContainerColor = if (confirmContainerColor != MaterialTheme.colorScheme.primary) confirmContainerColor else confirmColor
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (extraActionText != null && onExtraAction != null) {
            OutlinedButton(
                onClick = onExtraAction,
                shape = MizanDialogTokens.buttonShape,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = extraActionColor
                ),
                border = BorderStroke(1.dp, extraActionColor.copy(alpha = 0.4f)),
                modifier = Modifier
                    .height(MizanDialogTokens.buttonHeight)
                    .weight(1f),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
            ) {
                Text(
                    text = extraActionText,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        if (onCancel != null) {
            OutlinedButton(
                onClick = onCancel,
                shape = MizanDialogTokens.buttonShape,
                modifier = Modifier
                    .height(MizanDialogTokens.buttonHeight)
                    .weight(1f),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
            ) {
                Text(
                    text = cancelText,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
            }
        }

        Button(
            onClick = onConfirm,
            enabled = confirmEnabled && !isConfirmLoading,
            shape = MizanDialogTokens.buttonShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = finalContainerColor,
                contentColor = confirmContentColor,
                disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
            ),
            modifier = Modifier
                .height(MizanDialogTokens.buttonHeight)
                .weight(if (onCancel != null && extraActionText != null) 1.2f else if (onCancel != null || extraActionText != null) 1.25f else 1f),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp)
        ) {
            if (isConfirmLoading) {
                CircularProgressIndicator(
                    color = confirmContentColor,
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    if (confirmIcon != null) {
                        Icon(
                            imageVector = confirmIcon,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    Text(
                        text = confirmText,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        maxLines = 1
                    )
                }
            }
        }
    }
}
