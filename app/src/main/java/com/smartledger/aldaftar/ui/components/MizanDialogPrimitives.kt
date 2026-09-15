package com.smartledger.aldaftar.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartledger.aldaftar.ui.theme.MizanDialogTokens

/**
 * Shared dialog surface used by the compact Mizan dialogs.
 *
 * Keeping this primitive in one place prevents each dialog from inventing its
 * own width, padding, shape and surface treatment.
 */
@Composable
fun MizanDialogCard(
    modifier: Modifier = Modifier,
    maxWidth: Dp = MizanDialogTokens.maxWidth,
    maxHeight: Dp? = null,
    contentPadding: PaddingValues = PaddingValues(MizanDialogTokens.outerPadding),
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier
            .widthIn(max = maxWidth)
            .then(
                if (maxHeight != null) Modifier.heightIn(max = maxHeight) else Modifier
            ),
        shape = MizanDialogTokens.shape,
        color = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        tonalElevation = 3.dp,
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(contentPadding),
            verticalArrangement = Arrangement.spacedBy(MizanDialogTokens.verticalGap)
        ) {
            content()
        }
    }
}

/**
 * Standard compact dialog header: icon, title and a close action.
 */
@Composable
fun MizanDialogHeader(
    title: String,
    icon: ImageVector,
    iconTint: Color = MaterialTheme.colorScheme.primary,
    onCloseClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(22.dp)
        )

        Spacer(Modifier.width(10.dp))

        Text(
            text = title,
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = MizanDialogTokens.titleSize,
            fontWeight = FontWeight.Bold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )

        if (onCloseClick != null) {
            IconButton(
                onClick = onCloseClick,
                modifier = Modifier.size(44.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "إغلاق",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Consistent confirm/cancel actions for Mizan dialogs.
 */
@Composable
fun MizanDialogActions(
    confirmText: String,
    onConfirm: () -> Unit,
    cancelText: String,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
    confirmEnabled: Boolean = true,
    confirmContainerColor: Color? = null,
    confirmContentColor: Color? = null
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedButton(
            onClick = onCancel,
            modifier = Modifier.height(MizanDialogTokens.buttonHeight)
        ) {
            Text(
                text = cancelText,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        val colors = if (confirmContainerColor != null || confirmContentColor != null) {
            androidx.compose.material3.ButtonDefaults.buttonColors(
                containerColor = confirmContainerColor
                    ?: MaterialTheme.colorScheme.primary,
                contentColor = confirmContentColor
                    ?: MaterialTheme.colorScheme.onPrimary
            )
        } else {
            androidx.compose.material3.ButtonDefaults.buttonColors()
        }

        Button(
            onClick = onConfirm,
            enabled = confirmEnabled,
            modifier = Modifier.height(MizanDialogTokens.buttonHeight),
            colors = colors
        ) {
            Text(
                text = confirmText,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
