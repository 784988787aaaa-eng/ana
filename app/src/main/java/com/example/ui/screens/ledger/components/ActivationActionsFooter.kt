package com.example.ui.screens.ledger.components

/*
 * =====================================================================================
 * مكونات تذييل وإجراءات شاشة التفعيل والترخيص (Activation Actions & Footer Components)
 * -------------------------------------------------------------------------------------
 * [الوصف والهدف]:
 * حزمة من المكونات البصرية التفاعلية المخصصة لإدارة شاشة التفعيل والترخيص:
 * 1. شريط التبويب المقسم (Segmented Tabs) للتبديل بين التفعيل بحساب Google والتفعيل بدون إنترنت بمفتاح المنتج.
 * 2. بطاقة التفعيل عبر Google وتفاصيل النسخ الاحتياطي وحالة البريد الإلكتروني.
 * 3. شريط عرض معرّف الجهاز الفريد (Device ID) مع زر النسخ المباشر للحافظة.
 * 4. لافتة التغذية الراجعة المتحركة (Feedback Banner) لعرض رسائل التنبيه وأزرار التواصل مع الدعم.
 * 5. أزرار الإجراءات السفلية (Footer Buttons) لطلب التفعيل عبر واتساب أو الاستمرار في وضع التصفح.
 * 6. دالة فتح محادثة الدعم الفني المباشر عبر واتساب.
 * =====================================================================================
 */

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.whatsappColor

/*
 * =====================================================================================
 * شريط التبويب المقسم (ActivationSegmentedTabs)
 * -------------------------------------------------------------------------------------
 * [المُدخلات]:
 * - selectedTab: رقم التبويب المحدد حالياً (0: Google، 1: مفتاح الترخيص).
 * - onTabSelected: دالة رد النداء عند اختيار تبويب جديد.
 * =====================================================================================
 */
@Composable
fun ActivationSegmentedTabs(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
            .padding(3.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // التبويب 0: حساب Google والتفعيل السحابي
        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(10.dp))
                .background(
                    if (selectedTab == 0) MaterialTheme.colorScheme.surface else Color.Transparent
                )
                .clickable { onTabSelected(0) }
                .padding(vertical = 7.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.CloudSync,
                    contentDescription = null,
                    tint = if (selectedTab == 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = stringResource(R.string.licensing_tab_email),
                    fontSize = 11.sp,
                    fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                    color = if (selectedTab == 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // التبويب 1: مفتاح المنتج والتفعيل غير المتصل (Offline)
        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(10.dp))
                .background(
                    if (selectedTab == 1) MaterialTheme.colorScheme.surface else Color.Transparent
                )
                .clickable { onTabSelected(1) }
                .padding(vertical = 7.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.VpnKey,
                    contentDescription = null,
                    tint = if (selectedTab == 1) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = stringResource(R.string.licensing_tab_offline),
                    fontSize = 11.sp,
                    fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                    color = if (selectedTab == 1) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/*
 * =====================================================================================
 * محتوى تبويب حساب Google (ActivationGoogleTabContent)
 * -------------------------------------------------------------------------------------
 * [المُدخلات]:
 * - storedEmail: البريد الإلكتروني المسجل إن وجد.
 * - isLicenseLoading: مؤشر حالة جاري التحقق من الترخيص.
 * - onGoogleSignInClick: رد النداء لبدء تسجيل الدخول بـ Google.
 * - onGoogleActivateClick: رد النداء لتفعيل الترخيص بالسيرفر للبريد المسجل.
 * =====================================================================================
 */
@Composable
fun ActivationGoogleTabContent(
    storedEmail: String?,
    isLicenseLoading: Boolean,
    onGoogleSignInClick: () -> Unit,
    onGoogleActivateClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (!storedEmail.isNullOrBlank()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(
                                text = storedEmail,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = stringResource(R.string.licensing_fluent_cloud_backup_only),
                                fontSize = 9.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Button(
                        onClick = onGoogleActivateClick,
                        enabled = !isLicenseLoading,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        if (isLicenseLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(12.dp),
                                strokeWidth = 1.5.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Text(
                                text = stringResource(R.string.licensing_fluent_btn_activate_now),
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }
            } else {
                Button(
                    onClick = onGoogleSignInClick,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(38.dp)
                        .testTag("google_login_button"),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CloudQueue,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stringResource(R.string.licensing_fluent_btn_google_signin),
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.5.sp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
        }
    }
}

/*
 * =====================================================================================
 * شريط معرف الجهاز (ActivationDeviceIdBar)
 * -------------------------------------------------------------------------------------
 * [المُدخلات]:
 * - deviceId: معرف الجهاز الفريد.
 * - onCopyClick: رد النداء لنسخ المعرف إلى الحافظة.
 * =====================================================================================
 */
@Composable
fun ActivationDeviceIdBar(
    deviceId: String,
    onCopyClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
            .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f), RoundedCornerShape(10.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = Icons.Default.PhoneAndroid,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = deviceId,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.testTag("device_id_text"),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = onCopyClick,
                modifier = Modifier
                    .size(26.dp)
                    .testTag("copy_device_id_button")
            ) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = stringResource(R.string.licensing_fluent_copy_device_id),
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

/*
 * =====================================================================================
 * لافتة التغذية الراجعة والخطأ (ActivationFeedbackBanner)
 * -------------------------------------------------------------------------------------
 * [المُدخلات]:
 * - actionFeedbackMessage: نص رسالة الخطأ أو التنبيه.
 * - onWhatsAppRequestClick: رد النداء لطلب المساعدة عبر واتساب.
 * =====================================================================================
 */
@Composable
fun ActivationFeedbackBanner(
    actionFeedbackMessage: String?,
    onWhatsAppRequestClick: () -> Unit
) {
    val whatsappAccent = whatsappColor(MaterialTheme.colorScheme.background.luminance() < 0.5f)
    AnimatedVisibility(
        visible = actionFeedbackMessage != null,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically()
    ) {
        Column(modifier = Modifier.padding(top = 10.dp)) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.70f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.35f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = actionFeedbackMessage ?: "",
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    FilledTonalButton(
                        onClick = onWhatsAppRequestClick,
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = whatsappAccent.copy(alpha = 0.15f),
                            contentColor = whatsappAccent
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(30.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Chat,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = stringResource(R.string.licensing_btn_whatsapp_request),
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

/*
 * =====================================================================================
 * تذييل أزرار التفاعل والإغلاق (ActivationActionsFooter)
 * -------------------------------------------------------------------------------------
 * [المُدخلات]:
 * - onWhatsAppClick: رد النداء لفتح محادثة واتساب مع الدعم.
 * - onDismiss: رد النداء لإغلاق الحوار والمتابعة في وضع عدم التفعيل.
 * =====================================================================================
 */
@Composable
fun ActivationActionsFooter(
    onWhatsAppClick: () -> Unit,
    onDismiss: () -> Unit
) {
    val whatsappAccent = whatsappColor(MaterialTheme.colorScheme.background.luminance() < 0.5f)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Button(
            onClick = onWhatsAppClick,
            colors = ButtonDefaults.buttonColors(containerColor = whatsappAccent),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .weight(1f)
                .height(38.dp)
                .testTag("whatsapp_contact_button"),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Chat,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = stringResource(R.string.licensing_whatsapp_short),
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.5.sp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }

        OutlinedButton(
            onClick = onDismiss,
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
            modifier = Modifier
                .weight(1f)
                .height(38.dp),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
        ) {
            Text(
                text = stringResource(R.string.licensing_browse_offline),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

/*
 * =====================================================================================
 * دالة مساعدة لفتح الدعم الفني عبر واتساب (openWhatsAppSupportDirect)
 * -------------------------------------------------------------------------------------
 * [المُدخلات]:
 * - context: سياق التطبيق لإطلاق النية (Intent).
 * - msg: الرسالة الافتراضية المرفقة بطلب الدعم.
 * =====================================================================================
 */
fun openWhatsAppSupportDirect(context: Context, msg: String) {
    val intent = Intent(
        Intent.ACTION_VIEW,
        Uri.parse("https://api.whatsapp.com/send?phone=967774004399&text=" + Uri.encode(msg))
    )
    try {
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, context.getString(R.string.licensing_fluent_toast_whatsapp_missing), Toast.LENGTH_SHORT).show()
    }
}

