package com.smartledger.aldaftar.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.smartledger.aldaftar.ui.theme.MizanContentTokens
import com.smartledger.aldaftar.ui.theme.MizanIconSizes
import com.smartledger.aldaftar.ui.theme.MizanRadii
import com.smartledger.aldaftar.ui.theme.MizanSpacing
import com.smartledger.aldaftar.ui.theme.MizanTouchTarget
import com.smartledger.aldaftar.R
import com.smartledger.aldaftar.data.local.entities.AppSettings
import com.smartledger.aldaftar.ui.helper.dialPhoneNumber
import com.smartledger.aldaftar.ui.helper.openWhatsAppChat
import com.smartledger.aldaftar.ui.navigation.Screen
import com.smartledger.aldaftar.ui.theme.isDark
import com.smartledger.aldaftar.ui.screens.BusinessProfileDialog
import com.smartledger.aldaftar.ui.screens.SecurityDialog
import com.smartledger.aldaftar.ui.theme.mizanColors
import com.smartledger.aldaftar.ui.viewmodel.SecurityViewModel
import com.smartledger.aldaftar.ui.viewmodel.LicenseViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.smartledger.aldaftar.ui.screens.license.LicenseDialog

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
    businessProfileViewModel: com.smartledger.aldaftar.ui.viewmodel.BusinessProfileViewModel,
    onSaveSettings: (AppSettings, String, Double, Boolean) -> Unit,
    versionName: String,
    onComprehensiveReportClick: () -> Unit,
    onBusinessProfileClick: () -> Unit,
    onCurrencySettingsClick: () -> Unit,
    onSecurityClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ModalDrawerSheet(
        drawerContainerColor = MaterialTheme.colorScheme.surface,
        modifier = modifier.fillMaxHeight().fillMaxWidth(0.82f).widthIn(min = 280.dp, max = 320.dp),
        windowInsets = WindowInsets(0, 0, 0, 0)
    ) {
        AppNavigationDrawerContent(
            currentScreen, onScreenSelected, onBackupClick, settings, licenseViewModel,
            onLicenseClick, onSaveSettings, versionName, onComprehensiveReportClick,
            onBusinessProfileClick, onCurrencySettingsClick, onSecurityClick
        )
    }
}

@Composable
fun AppNavigationDrawerContent(
    currentScreen: Screen,
    onScreenSelected: (Screen) -> Unit,
    onBackupClick: () -> Unit,
    settings: AppSettings,
    licenseViewModel: LicenseViewModel,
    onLicenseClick: () -> Unit,
    onSaveSettings: (AppSettings, String, Double, Boolean) -> Unit,
    versionName: String,
    onComprehensiveReportClick: () -> Unit,
    onBusinessProfileClick: () -> Unit,
    onCurrencySettingsClick: () -> Unit,
    onSecurityClick: () -> Unit
) {
    val context = LocalContext.current
    val supportPhoneNumber = stringResource(id = R.string.support_phone_number)
    val licenseSnapshot by licenseViewModel.snapshot.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(bottomStart = MizanRadii.xl, bottomEnd = MizanRadii.xl))
                .background(MaterialTheme.colorScheme.primary)
        ) {
            Box(Modifier.fillMaxWidth()) {
                val dark = MaterialTheme.isDark
                IconButton(
                    onClick = { onSaveSettings(settings.copy(themeMode = if (dark) 1 else 2), "", 0.0, false) },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .statusBarsPadding()
                        .padding(MizanSpacing.xs)
                        .size(MizanTouchTarget.iconButtonSize)
                ) {
                    Icon(
                        imageVector = if (dark) Icons.Default.WbSunny else Icons.Default.NightsStay,
                        contentDescription = stringResource(id = R.string.desc_toggle_dark_mode),
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(MizanIconSizes.sm)
                    )
                }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = MizanSpacing.xl, vertical = MizanSpacing.lg),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(MizanIconSizes.xl)
                            .background(MaterialTheme.mizanColors.headerControlContainer, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Home, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(MizanIconSizes.md))
                    }
                    Spacer(Modifier.height(MizanSpacing.sm))
                    Text(
                        text = stringResource(id = R.string.app_name_main),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }

        Spacer(Modifier.height(MizanSpacing.sm))
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = MizanSpacing.sm)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(MizanSpacing.xs)
        ) {
            DrawerItem(false, Icons.Default.People, stringResource(R.string.drawer_business_profile_label), onBusinessProfileClick)
            DrawerItem(false, Icons.Default.Assessment, stringResource(R.string.drawer_comprehensive_report_label), onComprehensiveReportClick)
            DrawerItem(false, Icons.Default.MonetizationOn, stringResource(R.string.drawer_currency_label), onCurrencySettingsClick)
            DrawerItem(false, Icons.Default.Lock, stringResource(R.string.drawer_security_label), onSecurityClick)
            DrawerItem(false, Icons.Default.Verified, if (licenseSnapshot.isPaid) stringResource(R.string.drawer_license_active) else stringResource(R.string.drawer_license_label), onLicenseClick)
            DrawerItem(currentScreen == Screen.TRASH, Icons.Default.Delete, stringResource(R.string.drawer_trash_label)) { onScreenSelected(Screen.TRASH) }
            DrawerItem(false, Icons.Default.Refresh, stringResource(R.string.drawer_backup_label1), onBackupClick)
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = MizanSpacing.lg, vertical = MizanSpacing.sm)
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(Modifier.height(MizanSpacing.sm))
            Text(stringResource(R.string.drawer_app_version, versionName), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
            Spacer(Modifier.height(MizanSpacing.xs))
            Text(stringResource(R.string.developer_credit), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
            Spacer(Modifier.height(MizanSpacing.sm))
            Row(horizontalArrangement = Arrangement.spacedBy(MizanSpacing.lg), verticalAlignment = Alignment.CenterVertically) {
                ContactIcon(Icons.Default.Call, stringResource(R.string.settings_desc_call_support)) { dialPhoneNumber(context, supportPhoneNumber) }
                ContactIcon(Icons.Default.Share, stringResource(R.string.whatsapp_contact_msg)) {
                    openWhatsAppChat(context, supportPhoneNumber, context.getString(R.string.whatsapp_contact_msg))
                }
            }
        }
    }
}
