package com.smartledger.aldaftar.ui.screens.habayeb.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartledger.aldaftar.R
import com.smartledger.aldaftar.data.local.entities.HabayebCustomer
import com.smartledger.aldaftar.domain.communication.CustomerCommunicationConfig
import com.smartledger.aldaftar.domain.communication.CustomerPhoneHelper

private val WhatsAppBrandGreen = Color(0xFF25D366)

@Composable
fun CustomerAutoCommunicationSection(
    customer: HabayebCustomer,
    communicationConfig: CustomerCommunicationConfig,
    activeThemeColor: Color,
    onToggleWhatsApp: (Boolean) -> Unit,
    onToggleSms: (Boolean) -> Unit,
    onRequestEditPhone: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isPhoneValid = CustomerPhoneHelper.isValidDestination(customer.phone)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("auto_communication_section"),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Right side (RTL): Title and hint
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(activeThemeColor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = null,
                        tint = activeThemeColor,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Column {
                    Text(
                        text = "الإرسال الفوري",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "إشعار العميل فور حفظ المعاملة",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                }
            }

            // Left side (RTL): Independent toggle buttons for WhatsApp and SMS
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // WhatsApp Button
                val isWaActive = communicationConfig.autoWhatsApp && isPhoneValid
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isWaActive) WhatsAppBrandGreen.copy(alpha = 0.14f) else Color.Transparent,
                    border = BorderStroke(
                        width = if (isWaActive) 1.5.dp else 1.dp,
                        color = if (isWaActive) WhatsAppBrandGreen else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                    ),
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .clickable {
                            if (!communicationConfig.autoWhatsApp) {
                                if (isPhoneValid) {
                                    onToggleWhatsApp(true)
                                } else {
                                    onRequestEditPhone()
                                }
                            } else {
                                onToggleWhatsApp(false)
                            }
                        }
                        .testTag("auto_comm_whatsapp_btn")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Chat,
                            contentDescription = "WhatsApp",
                            tint = if (isWaActive) WhatsAppBrandGreen else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "واتساب",
                            fontSize = 11.sp,
                            fontWeight = if (isWaActive) FontWeight.Bold else FontWeight.Medium,
                            color = if (isWaActive) WhatsAppBrandGreen else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = if (isWaActive) "●" else "○",
                            fontSize = 9.sp,
                            color = if (isWaActive) WhatsAppBrandGreen else MaterialTheme.colorScheme.outline
                        )
                    }
                }

                // SMS Button
                val isSmsActive = communicationConfig.autoSms && isPhoneValid
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isSmsActive) activeThemeColor.copy(alpha = 0.14f) else Color.Transparent,
                    border = BorderStroke(
                        width = if (isSmsActive) 1.5.dp else 1.dp,
                        color = if (isSmsActive) activeThemeColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                    ),
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .clickable {
                            if (!communicationConfig.autoSms) {
                                if (isPhoneValid) {
                                    onToggleSms(true)
                                } else {
                                    onRequestEditPhone()
                                }
                            } else {
                                onToggleSms(false)
                            }
                        }
                        .testTag("auto_comm_sms_btn")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sms,
                            contentDescription = "SMS",
                            tint = if (isSmsActive) activeThemeColor else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "SMS",
                            fontSize = 11.sp,
                            fontWeight = if (isSmsActive) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSmsActive) activeThemeColor else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = if (isSmsActive) "●" else "○",
                            fontSize = 9.sp,
                            color = if (isSmsActive) activeThemeColor else MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }
        }
    }
}
