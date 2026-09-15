package com.smartledger.aldaftar.ui.components

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
import com.smartledger.aldaftar.ui.theme.CairoFontFamily

@Composable
fun DrawerItem(
    selected: Boolean,
    icon: ImageVector,
    label: String,
    badge: (@Composable () -> Unit)? = null,
    onClick: () -> Unit
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val accentColor = MaterialTheme.colorScheme.secondary
    val containerColor = if (selected) primaryColor.copy(alpha = 0.12f) else Color.Transparent
    val textPrimary = MaterialTheme.colorScheme.onSurface
    val fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium

    Surface(
        onClick = onClick,
        color = containerColor,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(primaryColor.copy(alpha = if (selected) 0.16f else 0.08f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = primaryColor,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = label,
                    fontFamily = CairoFontFamily,
                    fontSize = 13.5.sp,
                    fontWeight = fontWeight,
                    color = textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )

                if (badge != null) {
                    Spacer(modifier = Modifier.width(8.dp))
                    badge()
                }
            }

            if (selected) {
                Box(
                    modifier = Modifier
                        .width(3.5.dp)
                        .fillMaxHeight(0.60f)
                        .align(Alignment.CenterStart)
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
    contentDescription: String? = null,
    containerColor: Color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
    iconTint: Color = MaterialTheme.colorScheme.primary
) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = false, radius = 22.dp),
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(containerColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = iconTint,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}


