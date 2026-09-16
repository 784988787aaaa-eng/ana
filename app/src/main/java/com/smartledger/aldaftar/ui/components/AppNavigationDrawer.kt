package com.smartledger.aldaftar.ui.components

import java.math.BigDecimal
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartledger.aldaftar.R
import com.smartledger.aldaftar.data.local.entities.AppSettings
import com.smartledger.aldaftar.ui.helper.dialPhoneNumber
import com.smartledger.aldaftar.ui.helper.openWhatsAppChat
import com.smartledger.aldaftar.ui.navigation.Screen
import com.smartledger.aldaftar.ui.theme.CairoFontFamily
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
    onSaveSettings: (AppSettings, String, BigDecimal, Boolean) -> Unit,
    versionName: String,
    onComprehensiveReportClick: () -> Unit,
    onBusinessProfileClick: () -> Unit,
    onCurrencySettingsClick: () -> Unit,
    onSecurityClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val supportPhoneNumber = stringResource(id = R.string.support_phone_number)
    val licenseSnapshot by licenseViewModel.snapshot.collectAsStateWithLifecycle()

    ModalDrawerSheet(
        drawerContainerColor = MaterialTheme.colorScheme.surface,
        modifier = modifier
            .fillMaxWidth(0.78f)
            .widthIn(min = 260.dp, max = 300.dp)
            .fillMaxHeight(),
        windowInsets = WindowInsets(0, 0, 0, 0)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp))
                .background(MaterialTheme.colorScheme.primary)
        ) {
            Box(
                modifier = Modifier.fillMaxWidth()
            ) {
                val isDark = MaterialTheme.isDark

                IconButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        val newMode = if (isDark) 1 else 2
                        onSaveSettings(settings.copy(themeMode = newMode), "", BigDecimal.ZERO, false)
                    },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .statusBarsPadding()
                        .padding(8.dp)
                        .size(40.dp)
                        .clip(CircleShape)
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
                        .padding(horizontal = 20.dp, vertical = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(
                                color = Color(0xFF8B5CF6).copy(alpha = 0.20f),
                                shape = CircleShape
                            )
                            .border(
                                width = 1.dp,
                                color = Color.White.copy(alpha = 0.30f),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    Text(
                        text = stringResource(id = R.string.app_name_main),
                        fontFamily = CairoFontFamily,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 17.sp,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = stringResource(id = R.string.drawer_app_subtitle),
                        fontFamily = CairoFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.75f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(4.dp)
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
                label = if (licenseSnapshot.isPaid) stringResource(id = R.string.drawer_license_active) else stringResource(id = R.string.drawer_license_label),
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
        }
        
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f), thickness = 1.dp)
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = stringResource(id = R.string.drawer_app_version, versionName),
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                fontWeight = FontWeight.Normal,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(3.dp))
            
            Text(
                text = stringResource(id = R.string.developer_credit),
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                fontWeight = FontWeight.Normal,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ContactIcon(
                    icon = Icons.Default.Call,
                    contentDescription = stringResource(id = R.string.settings_desc_call_support),
                    onClick = {
                        dialPhoneNumber(context, supportPhoneNumber)
                    }
                )
                
                ContactIcon(
                    icon = Icons.Default.Share,
                    contentDescription = stringResource(id = R.string.whatsapp_contact_msg),
                    onClick = {
                        val msg = context.getString(R.string.whatsapp_contact_msg)
                        openWhatsAppChat(context, supportPhoneNumber, msg)
                    }
                )
            }
        }
    }
}
