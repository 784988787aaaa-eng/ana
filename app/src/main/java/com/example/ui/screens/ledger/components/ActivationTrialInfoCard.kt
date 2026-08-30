package com.example.ui.screens.ledger.components

/*
 * =====================================================================================
 * بطاقة معلومات الترخيص وحالة التجربة (Activation & Trial Info Components)
 * -------------------------------------------------------------------------------------
 * [الوصف والهدف]:
 * حزمة من المكونات البصرية لعرض حالة ترخيص التطبيق ومزايا النسخة المفعلة:
 * 1. لافتة حالة الترخيص العلوية (ActivationStatusBanner) التي تغير ألوانها ورسالتها التوضيحية ديناميكياً.
 * 2. واجهة الحساب المفعل (ActivationActivatedBody) التي تعرض تفاصيل الحساب المربوط ونوع الترخيص وأزرار الخروج.
 * 3. شبكة المزايا المتاحة (Feature Entitlements Grid) لعرض ميزات النسخة الاحترافية (حركات غير محدودة، نسخ سحابي، تقارير PDF).
 * 4. شارة الميزة (ActivationFeatureBadge) لعرض كل ميزة بشكل مضغوط وأنيق.
 * =====================================================================================
 */

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.LicenseGreenBg
import com.example.ui.theme.LicenseGreenText

/*
 * =====================================================================================
 * لافتة حالة الترخيص (ActivationStatusBanner)
 * -------------------------------------------------------------------------------------
 * [المُدخلات]:
 * - isActivated: هل النسخة مفعلة برخصة صالحة.
 * - isAutoTriggered: هل ظهر التنبيه تلقائياً لانتهاء الفترة التجريبية.
 * =====================================================================================
 */
@Composable
fun ActivationStatusBanner(
    isActivated: Boolean,
    isAutoTriggered: Boolean
) {
    val bannerBg = when {
        isActivated -> MaterialTheme.colorScheme.tertiaryContainer
        isAutoTriggered -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.70f)
        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
    }
    val bannerBorder = when {
        isActivated -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.35f)
        isAutoTriggered -> MaterialTheme.colorScheme.error.copy(alpha = 0.35f)
        else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
    }
    val textColor = when {
        isActivated -> MaterialTheme.colorScheme.onTertiaryContainer
        isAutoTriggered -> MaterialTheme.colorScheme.onErrorContainer
        else -> MaterialTheme.colorScheme.onSurface
    }
    val descText = when {
        isActivated -> stringResource(R.string.licensing_fluent_desc_active)
        isAutoTriggered -> stringResource(R.string.licensing_fluent_desc_trial)
        else -> stringResource(R.string.licensing_fluent_desc_default)
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = bannerBg),
        border = BorderStroke(1.dp, bannerBorder),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = descText,
            fontWeight = FontWeight.Medium,
            fontSize = 11.sp,
            color = textColor,
            textAlign = TextAlign.Center,
            lineHeight = 16.sp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp)
        )
    }
}

/**
 * بطاقة عرض استهلاك الحد المجاني وشريط التقدم (Trial Quota & Usage Progress Card)
 */
@Composable
fun ActivationTrialQuotaCard(
    usedCount: Int,
    maxCount: Int = 100,
    isExpired: Boolean = false
) {
    val progress = (usedCount.toFloat() / maxCount.toFloat()).coerceIn(0f, 1f)
    val remaining = (maxCount - usedCount).coerceAtLeast(0)

    val containerBg = if (isExpired) {
        MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.70f)
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
    }

    val borderColor = if (isExpired) {
        MaterialTheme.colorScheme.error.copy(alpha = 0.35f)
    } else {
        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)
    }

    val progressColor = if (isExpired) {
        MaterialTheme.colorScheme.error
    } else if (progress > 0.8f) {
        com.example.ui.theme.warningColor(MaterialTheme.colorScheme.background.luminance() < 0.5f)
    } else {
        MaterialTheme.colorScheme.primary
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = containerBg),
        border = BorderStroke(1.dp, borderColor),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isExpired) {
                        stringResource(R.string.licensing_trial_limit_reached, maxCount)
                    } else {
                        stringResource(R.string.licensing_trial_active_title)
                    },
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isExpired) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = stringResource(R.string.licensing_trial_usage_summary, usedCount, maxCount),
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = progressColor,
                trackColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
            )

            if (!isExpired) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.licensing_trial_remaining_summary, remaining),
                    fontSize = 9.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}

/*
 * =====================================================================================
 * واجهة تفاصيل النسخة المفعلة (ActivationActivatedBody)
 * -------------------------------------------------------------------------------------
 * [المُدخلات]:
 * - storedEmail: البريد الإلكتروني للحساب المربوط.
 * - activatedEmail: البريد الذي تم التفعيل به.
 * - onLogout: رد النداء لتسجيل الخروج وإلغاء الربط.
 * - onDismiss: رد النداء لإغلاق الحوار.
 * =====================================================================================
 */
@Composable
fun ActivationActivatedBody(
    storedEmail: String?,
    activatedEmail: String,
    onLogout: () -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // بطاقة حالة الترخيص
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
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = stringResource(R.string.licensing_fluent_license_status_label),
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.5.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.licensing_fluent_cloud_synced_badge),
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))

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
                                    text = stringResource(R.string.licensing_fluent_license_type_digital),
                                    fontSize = 9.sp,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        IconButton(
                            onClick = onLogout,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Logout,
                                contentDescription = stringResource(R.string.licensing_fluent_btn_signout),
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.VpnKey,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stringResource(R.string.licensing_fluent_license_type_offline),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        // شبكة مزايا النسخة الكاملة (Entitlements Feature Grid 2x2)
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                ActivationFeatureBadge(
                    icon = Icons.Default.AllInclusive,
                    label = stringResource(R.string.licensing_fluent_feature_unlimited_tx),
                    modifier = Modifier.weight(1f)
                )
                ActivationFeatureBadge(
                    icon = Icons.Default.CloudSync,
                    label = stringResource(R.string.licensing_fluent_feature_cloud_sync),
                    modifier = Modifier.weight(1f)
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                ActivationFeatureBadge(
                    icon = Icons.Default.PictureAsPdf,
                    label = stringResource(R.string.licensing_fluent_feature_pdf_reports),
                    modifier = Modifier.weight(1f)
                )
                ActivationFeatureBadge(
                    icon = Icons.Default.Update,
                    label = stringResource(R.string.licensing_fluent_feature_updates),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // زر الإغلاق والمتابعة
        Button(
            onClick = onDismiss,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .testTag("dialog_close_active_button")
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = stringResource(R.string.licensing_fluent_btn_close),
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}

/*
 * =====================================================================================
 * شارة ميزة مفعلة (ActivationFeatureBadge)
 * -------------------------------------------------------------------------------------
 * [المُدخلات]:
 * - icon: أيقونة الميزة.
 * - label: اسم الميزة المعروض.
 * - modifier: مغير التنسيق لتحديد الحجم والمحاذاة.
 * =====================================================================================
 */
@Composable
fun ActivationFeatureBadge(
    icon: ImageVector,
    label: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
            .padding(horizontal = 6.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(5.dp))
        Text(
            text = label,
            fontSize = 9.5.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

