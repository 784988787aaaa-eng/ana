package com.example.ui.screens.settings.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.CreditContainerDark
import com.example.ui.theme.CreditContainerLight
import com.example.ui.theme.CreditGreen
import com.example.ui.theme.CreditGreenDark
import com.example.ui.theme.SelectionGreen

@Composable
fun BackupSheetHeader(
    isConnected: Boolean,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f, fill = false),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = stringResource(R.string.backup_sheet_title),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = stringResource(R.string.backup_sheet_subtitle),
                    fontSize = 11.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 15.sp
                )
            }
        }

        val connectedBg = if (isDark) CreditContainerDark else CreditContainerLight
        val connectedText = if (isDark) CreditGreenDark else CreditGreen
        val connectedBorder = if (isDark) MaterialTheme.colorScheme.tertiary.copy(alpha = 0.4f) else MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f)

        val disconnectedBg = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        val disconnectedText = MaterialTheme.colorScheme.onSurfaceVariant
        val disconnectedBorder = MaterialTheme.colorScheme.outlineVariant

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(if (isConnected) connectedBg else disconnectedBg)
                .border(
                    width = 0.8.dp,
                    color = if (isConnected) connectedBorder else disconnectedBorder,
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(horizontal = 10.dp, vertical = 5.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(if (isConnected) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.outline)
                )
                Icon(
                    imageVector = if (isConnected) Icons.Default.CloudDone else Icons.Default.CloudOff,
                    contentDescription = null,
                    tint = if (isConnected) connectedText else disconnectedText,
                    modifier = Modifier.size(13.dp)
                )
                Text(
                    text = if (isConnected) stringResource(R.string.backup_status_connected) else stringResource(R.string.backup_status_local),
                    fontSize = 11.sp,
                    color = if (isConnected) connectedText else disconnectedText,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

