package com.smartledger.aldaftar.ui.screens.admin

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartledger.aldaftar.ui.components.MizanAnimatedDialog
import com.smartledger.aldaftar.ui.components.MizanDialogCard
import com.smartledger.aldaftar.ui.components.MizanDialogHeader
import com.smartledger.aldaftar.ui.theme.MizanDialogTokens
import com.smartledger.aldaftar.ui.viewmodel.AdminLicenseViewModel

/**
 * نافذة مخادعة (Decoy Dialog) تظهر للعميل العادي بدعوى "النافذة قيد التطوير..".
 * عند الضغط مرتين متتاليتين (Double Click) على النافذة، يفتح النظام السري لإدارة التراخيص فوراً.
 */
@Composable
fun AdminAuthDialog(
    viewModel: AdminLicenseViewModel,
    onDismiss: () -> Unit,
    onSuccess: () -> Unit
) {
    var lastClickTime by remember { mutableLongStateOf(0L) }

    val handleSecretTap = {
        val now = System.currentTimeMillis()
        if (now - lastClickTime < 700L) {
            // Secret double tap detected!
            viewModel.authenticate("Mansour#2100$")
            onSuccess()
        } else {
            lastClickTime = now
        }
    }

    MizanAnimatedDialog(onDismissRequest = onDismiss) {
        MizanDialogCard(
            maxWidth = MizanDialogTokens.compactMaxWidth,
            modifier = Modifier.clickable { handleSecretTap() }
        ) {
            MizanDialogHeader(
                title = "تنبيه النظام",
                subtitle = "حالة القسم البرمجي",
                icon = Icons.Default.Engineering,
                iconTint = MaterialTheme.colorScheme.secondary,
                onCloseClick = onDismiss
            )

            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { handleSecretTap() }
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                    Text(
                        text = "النافذة قيد التطوير..",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(MizanDialogTokens.buttonHeight),
                shape = MizanDialogTokens.buttonShape
            ) {
                Text(
                    text = "إغلاق",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
    }
}
