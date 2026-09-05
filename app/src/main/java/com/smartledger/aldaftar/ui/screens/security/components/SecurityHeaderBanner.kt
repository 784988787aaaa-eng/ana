package com.smartledger.aldaftar.ui.screens.security.components

import androidx.compose.material3.MaterialTheme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartledger.aldaftar.R
import com.smartledger.aldaftar.ui.theme.mizanColors

@Composable
fun SecurityHeaderBanner(
    isAlreadyPasscodeEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 0.5.dp, 
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), 
                shape = RoundedCornerShape(16.dp)
            )
    ) {
        val mizanColors = MaterialTheme.mizanColors
        val iconBg = if (isAlreadyPasscodeEnabled) mizanColors.creditContainer else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        val iconTint = if (isAlreadyPasscodeEnabled) mizanColors.credit else MaterialTheme.colorScheme.primary

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // هندسة محاذاة النص والبدء من اليمين لتناسب القراءة العربية الطبيعية
            Column(
                horizontalAlignment = Alignment.Start,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = if (isAlreadyPasscodeEnabled) stringResource(id = R.string.sec_status_active) else stringResource(id = R.string.sec_status_inactive),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Start
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (isAlreadyPasscodeEnabled) stringResource(id = R.string.sec_desc_active) else stringResource(id = R.string.sec_desc_inactive),
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Start,
                    lineHeight = 16.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // شارة الأمان الذكية مستقرة في جهة اليسار بصورة عصرية متزنة
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = iconBg,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isAlreadyPasscodeEnabled) Icons.Default.VerifiedUser else Icons.Default.Security,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
