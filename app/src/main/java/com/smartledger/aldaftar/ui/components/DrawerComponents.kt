package com.smartledger.aldaftar.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartledger.aldaftar.ui.theme.MizanTouchTarget

@Composable
fun DrawerItem(
    selected: Boolean,
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    subtitle: String? = null,
    badgeText: String? = null,
    badgeColor: Color = MaterialTheme.colorScheme.primary
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val accentColor = MaterialTheme.colorScheme.secondary
    val containerColor = if (selected) {
        primaryColor.copy(alpha = 0.11f)
    } else {
        Color.Transparent
    }
    val borderStroke = if (selected) {
        BorderStroke(0.8.dp, primaryColor.copy(alpha = 0.25f))
    } else {
        null
    }
    val textPrimary = if (selected) primaryColor else MaterialTheme.colorScheme.onSurface
    val fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.SemiBold

    Surface(
        onClick = onClick,
        color = containerColor,
        border = borderStroke,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 46.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    modifier = Modifier.size(34.dp),
                    shape = RoundedCornerShape(9.dp),
                    color = primaryColor.copy(alpha = if (selected) 0.16f else 0.09f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = primaryColor,
                            modifier = Modifier.size(19.dp)
                        )
                    }
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(1.dp)
                ) {
                    Text(
                        text = label,
                        fontSize = 13.5.sp,
                        fontWeight = fontWeight,
                        color = textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (!subtitle.isNullOrBlank()) {
                        Text(
                            text = subtitle,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                if (!badgeText.isNullOrBlank()) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = badgeColor.copy(alpha = 0.12f),
                        border = BorderStroke(0.6.dp, badgeColor.copy(alpha = 0.28f))
                    ) {
                        Text(
                            text = badgeText,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = badgeColor,
                            maxLines = 1,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            if (selected) {
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(24.dp)
                        .align(Alignment.CenterEnd)
                        .background(
                            color = accentColor,
                            shape = RoundedCornerShape(2.dp)
                        )
                )
            }
        }
    }
}

@Composable
fun ContactIcon(
    icon: ImageVector,
    onClick: () -> Unit,
    contentDescription: String? = null
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val backgroundLight = MaterialTheme.colorScheme.primary.copy(alpha = 0.09f)

    Box(
        modifier = Modifier
            .size(MizanTouchTarget.minimum)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true, radius = 20.dp),
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(backgroundLight),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = primaryColor,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
