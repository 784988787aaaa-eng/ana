package com.smartledger.aldaftar.ui.screens.habayeb.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartledger.aldaftar.domain.communication.CustomerCommunicationConfig
import com.smartledger.aldaftar.domain.communication.CustomerPhoneHelper

/**
 * زران ذكيان صغيران للتحكم في الإرسال الفوري التلقائي (واتساب و SMS)
 * مدمجان في النوافذ الذكية (حذف/تعديل/خيارات/مشاركة) دون إزدحام جدول المعاملات
 */
@Composable
fun SmartAutoCommunicationToggles(
    customerPhone: String,
    communicationConfig: CustomerCommunicationConfig,
    activeThemeColor: Color,
    onToggleWhatsApp: (Boolean) -> Unit,
    onToggleSms: (Boolean) -> Unit,
    onRequestEditPhone: () -> Unit,
    modifier: Modifier = Modifier,
    testTagPrefix: String = "smart_auto_comm"
) {
    val isPhoneValid = remember(customerPhone) { CustomerPhoneHelper.isValidDestination(customerPhone) }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("${testTagPrefix}_row"),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = null,
                    tint = activeThemeColor,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = "إرسال فوري:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // WhatsApp Smart Compact Button
                val isWaActive = communicationConfig.autoWhatsApp && isPhoneValid
                val waColor = Color(0xFF25D366)
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isWaActive) waColor.copy(alpha = 0.15f) else Color.Transparent,
                    border = BorderStroke(
                        width = if (isWaActive) 1.5.dp else 1.dp,
                        color = if (isWaActive) waColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                    ),
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            if (!communicationConfig.autoWhatsApp) {
                                if (isPhoneValid) onToggleWhatsApp(true) else onRequestEditPhone()
                            } else {
                                onToggleWhatsApp(false)
                            }
                        }
                        .testTag("${testTagPrefix}_wa_btn")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Chat,
                            contentDescription = "WhatsApp",
                            tint = if (isWaActive) waColor else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = "واتساب",
                            fontSize = 11.sp,
                            fontWeight = if (isWaActive) FontWeight.Bold else FontWeight.Medium,
                            color = if (isWaActive) waColor else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = if (isWaActive) "●" else "○",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isWaActive) waColor else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    }
                }

                // SMS Smart Compact Button
                val isSmsActive = communicationConfig.autoSms && isPhoneValid
                val smsColor = Color(0xFF3B82F6)
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isSmsActive) smsColor.copy(alpha = 0.15f) else Color.Transparent,
                    border = BorderStroke(
                        width = if (isSmsActive) 1.5.dp else 1.dp,
                        color = if (isSmsActive) smsColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                    ),
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            if (!communicationConfig.autoSms) {
                                if (isPhoneValid) onToggleSms(true) else onRequestEditPhone()
                            } else {
                                onToggleSms(false)
                            }
                        }
                        .testTag("${testTagPrefix}_sms_btn")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sms,
                            contentDescription = "SMS",
                            tint = if (isSmsActive) smsColor else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = "SMS",
                            fontSize = 11.sp,
                            fontWeight = if (isSmsActive) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSmsActive) smsColor else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = if (isSmsActive) "●" else "○",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSmsActive) smsColor else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }
    }
}
