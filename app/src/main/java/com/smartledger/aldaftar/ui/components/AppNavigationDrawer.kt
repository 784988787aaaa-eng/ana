package com.smartledger.aldaftar.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartledger.aldaftar.R
import com.smartledger.aldaftar.data.local.entities.AppSettings
import com.smartledger.aldaftar.ui.navigation.Screen
import com.smartledger.aldaftar.ui.theme.isDark
import com.smartledger.aldaftar.ui.theme.CairoFontFamily
import com.smartledger.aldaftar.ui.theme.PrimaryGradient
import com.smartledger.aldaftar.ui.viewmodel.SecurityViewModel
import com.smartledger.aldaftar.ui.viewmodel.LicenseViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

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
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val licenseSnapshot by licenseViewModel.snapshot.collectAsStateWithLifecycle()

    ModalDrawerSheet(
        drawerContainerColor = MaterialTheme.colorScheme.surface,
        modifier = modifier
            .fillMaxWidth(0.80f)
            .widthIn(min = 280.dp, max = 320.dp)
            .fillMaxHeight(),
        windowInsets = WindowInsets(0, 0, 0, 0)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            // Header Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
                    .background(PrimaryGradient)
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val isDark = MaterialTheme.isDark

                    IconButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            val newMode = if (isDark) 1 else 2
                            onSaveSettings(settings.copy(themeMode = newMode), "", 0.0, false)
                        },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .statusBarsPadding()
                            .padding(8.dp)
                    ) {
                        Icon(
                            imageVector = if (isDark) Icons.Default.WbSunny else Icons.Default.NightsStay,
                            contentDescription = stringResource(id = R.string.desc_toggle_dark_mode),
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(horizontal = 16.dp, vertical = 18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.20f))
                                .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.30f)), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountBalanceWallet,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = stringResource(id = R.string.app_name_main),
                            fontFamily = CairoFontFamily,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )

                        Text(
                            text = stringResource(id = R.string.drawer_app_subtitle),
                            fontFamily = CairoFontFamily,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White.copy(alpha = 0.75f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Scrollable Items Section
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                DrawerItem(
                    selected = false,
                    icon = Icons.Default.People,
                    label = stringResource(id = R.string.drawer_business_profile_label),
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
                    onClick = onCurrencySettingsClick
                )

                DrawerItem(
                    selected = false,
                    icon = Icons.Default.Lock,
                    label = stringResource(id = R.string.drawer_security_label),
                    onClick = onSecurityClick
                )

                DrawerItem(
                    selected = false,
                    icon = Icons.Default.Verified,
                    label = if (licenseSnapshot.isPaid) {
                        stringResource(id = R.string.drawer_license_active)
                    } else {
                        stringResource(id = R.string.drawer_license_label)
                    },
                    badge = if (licenseSnapshot.isPaid) {
                        {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF22C55E))
                            )
                        }
                    } else null,
                    onClick = onLicenseClick
                )

                DrawerItem(
                    selected = currentScreen == Screen.TRASH,
                    icon = Icons.Default.Delete,
                    label = stringResource(id = R.string.drawer_trash_label),
                    onClick = { onScreenSelected(Screen.TRASH) }
                )

                DrawerItem(
                    selected = false,
                    icon = Icons.Default.Refresh,
                    label = stringResource(id = R.string.drawer_backup_label1),
                    onClick = onBackupClick
                )

                Spacer(modifier = Modifier.height(6.dp))
            }

            // Footer & Developer Seal Section
            DeveloperSealFooter(
                versionName = versionName,
                modifier = Modifier
                    .navigationBarsPadding()
                    .padding(bottom = 6.dp)
            )
        }
    }
}
