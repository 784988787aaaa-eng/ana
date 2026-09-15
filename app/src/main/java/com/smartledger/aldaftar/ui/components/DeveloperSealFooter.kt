package com.smartledger.aldaftar.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartledger.aldaftar.R
import com.smartledger.aldaftar.ui.helper.dialPhoneNumber
import com.smartledger.aldaftar.ui.helper.openWhatsAppChat
import com.smartledger.aldaftar.ui.theme.CairoFontFamily
import com.smartledger.aldaftar.ui.theme.WhatsAppGreen
import com.smartledger.aldaftar.ui.theme.WhatsAppLightGreen

@Composable
fun DeveloperSealFooter(
    versionName: String? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val supportPhone = stringResource(R.string.support_phone_number)
    val whatsappMsg = stringResource(R.string.whatsapp_contact_msg)
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.40f)
        ),
        border = BorderStroke(
            0.8.dp, 
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (!versionName.isNullOrBlank()) {
                Text(
                    text = stringResource(id = R.string.drawer_app_version, versionName),
                    fontFamily = CairoFontFamily,
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Normal,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.70f),
                    textAlign = TextAlign.Center
                )
            }

            Text(
                text = stringResource(R.string.developer_credit),
                fontFamily = CairoFontFamily,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            
            Text(
                text = supportPhone,
                fontSize = 11.sp,
                fontWeight = FontWeight.Normal,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.60f),
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ContactIcon(
                    icon = Icons.Default.Call,
                    contentDescription = stringResource(id = R.string.settings_desc_call_support),
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.30f),
                    iconTint = MaterialTheme.colorScheme.primary,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        dialPhoneNumber(context, supportPhone)
                    }
                )
                
                ContactIcon(
                    icon = Icons.Default.Share,
                    contentDescription = whatsappMsg,
                    containerColor = WhatsAppLightGreen.copy(alpha = 0.20f),
                    iconTint = WhatsAppGreen,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        openWhatsAppChat(context, supportPhone, whatsappMsg)
                    }
                )
            }
        }
    }
}

