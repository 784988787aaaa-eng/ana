package com.smartledger.aldaftar.ui.components

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.smartledger.aldaftar.R
import com.smartledger.aldaftar.data.local.entities.AppSettings
import com.smartledger.aldaftar.ui.helper.dialPhoneNumber
import com.smartledger.aldaftar.ui.helper.openWhatsAppChat
import com.smartledger.aldaftar.ui.navigation.Screen
import com.smartledger.aldaftar.ui.theme.BrandPrimaryLight
import com.smartledger.aldaftar.ui.theme.CairoFontFamily
import com.smartledger.aldaftar.ui.theme.isDark
import com.smartledger.aldaftar.ui.theme.mizanColors
import com.smartledger.aldaftar.ui.viewmodel.BusinessProfileViewModel
import com.smartledger.aldaftar.ui.viewmodel.LicenseViewModel
import com.smartledger.aldaftar.ui.viewmodel.SecurityViewModel
import java.io.File
import java.math.BigDecimal
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigationDrawer(
    currentScreen: Screen,
    onScreenSelected: (Screen) -> Unit,
    onBackupClick: () -> Unit,
    settings: AppSettings,
    securityViewModel: SecurityViewModel,
    licenseViewModel: LicenseViewModel,
    onLicenseClick: () -> Unit,
    businessProfileViewModel: BusinessProfileViewModel,
    onSaveSettings: (AppSettings, String, BigDecimal, Boolean) -> Unit,
    versionName: String,
    onComprehensiveReportClick: () -> Unit,
    onBusinessProfileClick: () -> Unit,
    onCurrencySettingsClick: () -> Unit,
    onSecurityClick: () -> Unit,
    onPrivacyPolicyClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val supportPhoneNumber = stringResource(id = R.string.support_phone_number)
    val licenseSnapshot by licenseViewModel.snapshot.collectAsStateWithLifecycle()
    val businessProfile by businessProfileViewModel.profile.collectAsStateWithLifecycle()
    val mizanColors = MaterialTheme.mizanColors

    var logoBitmap by remember { mutableStateOf<Bitmap?>(null) }
    LaunchedEffect(businessProfile.logoPath) {
        logoBitmap = if (businessProfile.logoPath.isBlank()) {
            null
        } else {
            try {
                withContext(Dispatchers.IO) {
                    val file = File(businessProfile.logoPath)
                    if (file.exists()) BitmapFactory.decodeFile(file.absolutePath) else null
                }
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                null
            }
        }
    }

    ModalDrawerSheet(
        drawerContainerColor = MaterialTheme.colorScheme.surface,
        modifier = modifier
            .fillMaxWidth(0.80f)
            .widthIn(min = 264.dp, max = 308.dp)
            .fillMaxHeight(),
        windowInsets = WindowInsets(0, 0, 0, 0)
    ) {
        // Drawer Identity Header (Connected to Business Profile + Theme Toggle)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp))
                .background(MaterialTheme.colorScheme.primary)
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            val isDark = MaterialTheme.isDark
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            color = BrandPrimaryLight.copy(alpha = 0.22f),
                            shape = CircleShape
                        )
                        .border(
                            width = 1.2.dp,
                            color = Color.White.copy(alpha = 0.35f),
                            shape = CircleShape
                        )
                        .clickable { onBusinessProfileClick() },
                    contentAlignment = Alignment.Center
                ) {
                    if (logoBitmap != null) {
                        Image(
                            bitmap = logoBitmap!!.asImageBitmap(),
                            contentDescription = stringResource(id = R.string.biz_logo_desc),
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Storefront,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onBusinessProfileClick() },
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = businessProfile.name.ifBlank { stringResource(id = R.string.app_name_main) },
                        fontFamily = CairoFontFamily,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.5.sp,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = businessProfile.description.ifBlank { stringResource(id = R.string.drawer_app_subtitle) },
                        fontFamily = CairoFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.80f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                IconButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        val newMode = if (isDark) 1 else 2
                        onSaveSettings(settings.copy(themeMode = newMode), "", BigDecimal.ZERO, false)
                    },
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.14f))
                ) {
                    Icon(
                        imageVector = if (isDark) Icons.Default.WbSunny else Icons.Default.NightsStay,
                        contentDescription = stringResource(id = R.string.desc_toggle_dark_mode),
                        tint = Color.White,
                        modifier = Modifier.size(19.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Navigation & Management Items
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 10.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (currentScreen != Screen.HABAYEB) {
                DrawerItem(
                    selected = false,
                    icon = Icons.Default.AccountBalanceWallet,
                    label = stringResource(id = R.string.app_name_main),
                    onClick = { onScreenSelected(Screen.HABAYEB) }
                )
            }

            DrawerItem(
                selected = false,
                icon = Icons.Default.Storefront,
                label = stringResource(id = R.string.drawer_business_profile_label),
                badgeText = if (businessProfile.name.isNotBlank()) "✓" else null,
                badgeColor = mizanColors.credit,
                onClick = onBusinessProfileClick
            )

            DrawerItem(
                selected = false,
                icon = Icons.Default.Assessment,
                label = stringResource(id = R.string.drawer_comprehensive_report_label),
                onClick = onComprehensiveReportClick
            )

            DrawerItem(
                selected = false,
                icon = Icons.Default.MonetizationOn,
                label = stringResource(id = R.string.drawer_currency_label),
                badgeText = settings.currencySymbol.takeIf { it.isNotBlank() },
                onClick = onCurrencySettingsClick
            )

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                thickness = 0.6.dp,
                modifier = Modifier.padding( horizontal = 8.dp, vertical = 4.dp)
            )

            DrawerItem(
                selected = false,
                icon = if (settings.isPasscodeEnabled) Icons.Default.VerifiedUser else Icons.Default.Lock,
                label = stringResource(id = R.string.drawer_security_label),
                badgeText = if (settings.isPasscodeEnabled) "نشط" else null,
                badgeColor = mizanColors.credit,
                onClick = onSecurityClick
            )

            DrawerItem(
                selected = false,
                icon = Icons.Default.CloudSync,
                label = stringResource(id = R.string.drawer_backup_label1),
                onClick = onBackupClick
            )

            DrawerItem(
                selected = currentScreen == Screen.TRASH,
                icon = Icons.Default.DeleteOutline,
                label = stringResource(id = R.string.drawer_trash_label),
                onClick = { onScreenSelected(Screen.TRASH) }
            )

            DrawerItem(
                selected = false,
                icon = Icons.Default.Verified,
                label = if (licenseSnapshot.isPaid) {
                    stringResource(id = R.string.drawer_license_active)
                } else {
                    stringResource(id = R.string.drawer_license_label)
                },
                badgeColor = if (licenseSnapshot.isPaid) mizanColors.credit else MaterialTheme.colorScheme.primary,
                onClick = onLicenseClick
            )
        }

        // Compact Footer: Support Actions + Privacy Policy + Version
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 6.dp)
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                thickness = 0.8.dp
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ContactIcon(
                        icon = Icons.Default.Call,
                        contentDescription = stringResource(id = R.string.settings_desc_call_support),
                        onClick = { dialPhoneNumber(context, supportPhoneNumber) }
                    )

                    ContactIcon(
                        icon = Icons.Default.Chat,
                        contentDescription = stringResource(id = R.string.settings_desc_whatsapp),
                        onClick = {
                            val msg = context.getString(R.string.whatsapp_contact_msg)
                            openWhatsAppChat(context, supportPhoneNumber, msg)
                        }
                    )
                }

                Text(
                    text = stringResource(id = R.string.drawer_privacy_policy_label),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onPrivacyPolicyClick()
                        }
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                )
            }

            Text(
                text = "${stringResource(id = R.string.drawer_app_version, versionName)} • ${stringResource(id = R.string.developer_credit)}",
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f),
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(bottom = 2.dp)
            )
        }
    }
}
