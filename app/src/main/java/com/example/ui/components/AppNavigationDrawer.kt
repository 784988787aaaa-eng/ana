/**
 * التوثيق المعماري العربي — مشروع «الدفتر الذكي / ميزان الدار».
 * الملف: app/src/main/java/com/example/ui/components/AppNavigationDrawer.kt
 * المسؤولية: مكوّن التنقل الجانبي الرئيسي الذي يربط أقسام التطبيق ومسارات الانتقال من خلال درج Compose.
 *
 * القراءة التعليمية: يوضح هذا الملف كيف تنتقل حالة التطبيق من الطبقة المشتركة
 * إلى المشهد المرئي على الهاتف، مع تفسير العقود والحالة والتوابع والتفاعلات.
 * الكتلة التنفيذية الأصلية أدناه محفوظة حرفياً؛ الإضافات التوثيقية لا تعدّل
 * أي رمز تنفيذي وفق قاعدة Zero Code Alteration.
 */

// --- فهرس التوثيق التعليمي للسطر التنفيذي ---
// توثيق السطر 22: التوجيه الحزمي يحدد الموضع المنطقي للملف داخل طبقة الواجهة.
// توثيق السطر 27: الاستيراد التالي اعتماد أصلي يحتاجه الملف؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 28: الاستيراد التالي اعتماد أصلي يحتاجه الملف؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 29: الاستيراد التالي اعتماد أصلي يحتاجه الملف؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 30: الاستيراد التالي اعتماد أصلي يحتاجه الملف؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 31: الاستيراد التالي اعتماد أصلي يحتاجه الملف؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 32: الاستيراد التالي اعتماد أصلي يحتاجه الملف؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 33: الاستيراد التالي اعتماد أصلي يحتاجه الملف؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 34: الاستيراد التالي اعتماد أصلي يحتاجه الملف؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 35: الاستيراد التالي اعتماد أصلي يحتاجه الملف؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 36: الاستيراد التالي اعتماد أصلي يحتاجه الملف؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 37: الاستيراد التالي اعتماد أصلي يحتاجه الملف؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 38: الاستيراد التالي اعتماد أصلي يحتاجه الملف؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 39: الاستيراد التالي اعتماد أصلي يحتاجه الملف؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 40: الاستيراد التالي اعتماد أصلي يحتاجه الملف؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 41: الاستيراد التالي اعتماد أصلي يحتاجه الملف؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 42: الاستيراد التالي اعتماد أصلي يحتاجه الملف؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 43: الاستيراد التالي اعتماد أصلي يحتاجه الملف؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 44: الاستيراد التالي اعتماد أصلي يحتاجه الملف؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 45: الاستيراد التالي اعتماد أصلي يحتاجه الملف؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 46: الاستيراد التالي اعتماد أصلي يحتاجه الملف؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 47: الاستيراد التالي اعتماد أصلي يحتاجه الملف؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 48: الاستيراد التالي اعتماد أصلي يحتاجه الملف؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 49: الاستيراد التالي اعتماد أصلي يحتاجه الملف؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 50: الاستيراد التالي اعتماد أصلي يحتاجه الملف؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 51: الاستيراد التالي اعتماد أصلي يحتاجه الملف؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 52: الاستيراد التالي اعتماد أصلي يحتاجه الملف؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 53: الاستيراد التالي اعتماد أصلي يحتاجه الملف؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 54: الاستيراد التالي اعتماد أصلي يحتاجه الملف؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 55: الاستيراد التالي اعتماد أصلي يحتاجه الملف؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 56: الاستيراد التالي اعتماد أصلي يحتاجه الملف؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 57: الاستيراد التالي اعتماد أصلي يحتاجه الملف؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 58: الاستيراد التالي اعتماد أصلي يحتاجه الملف؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 59: الاستيراد التالي اعتماد أصلي يحتاجه الملف؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 76: التعليمة الوصفية التالية جزء من العقد الأصلي للواجهة أو التجميع، ولم يُغيّر سلوكها.
// توثيق السطر 77: التعليمة الوصفية التالية جزء من العقد الأصلي للواجهة أو التجميع، ولم يُغيّر سلوكها.
// توثيق السطر 122: التفرع التالي يوزع السلوك بحسب الحالة الأصلية.
// توثيق السطر 125: الفرع البديل التالي جزء من مسار التنفيذ الأصلي.
// توثيق السطر 315: الشرط التالي يحافظ على قرار التنفيذ الأصلي.
// توثيق السطر 323: الشرط التالي يحافظ على قرار التنفيذ الأصلي.
// توثيق السطر 329: الشرط التالي يحافظ على قرار التنفيذ الأصلي.

/**
 * =====================================================================
 * ملف: درج التنقل الجانبي للتطبيق (AppNavigationDrawer.kt)
 * =====================================================================
 * 
 * [الغرض العام والتعليمي من الملف]:
 * يمثل هذا المكون واجهة درج التنقل الجانبي الرئيسية (Navigation Drawer) في التطبيق،
 * مبنياً باستخدام Jetpack Compose و Material 3. يتيح للمستخدم الوصول السريع إلى
 * ملف النشاط التجاري، التقارير الشاملة، إعدادات العملة، إعدادات الأمان والقفل،
 * سلة المحذوفات، تفعيل النسخة الاحترافية (Pro)، النسخ الاحتياطي، والتبديل الفوري بين الوضع الليلي والنهاري.
 * 
 * [المسؤوليات المعمارية والتقنية للملف]:
 * 1. واجهة التنقل الموحدة (Unified Navigation Sheet):
 *    - استخدام [ModalDrawerSheet] مع مراعاة حواف الشاشة والـ Insets والتجاوب مع مختلف قياسات الشاشات.
 * 2. التحكم السريع في المظهر (Fast Theme Toggle):
 *    - زر تفاعلي في رأس القائمة للتبديل الفوري بين المظهر الفاتح والداكن وحفظ التفضيل سريعاً.
 * 3. إدارة الحوارات المنبثقة المتكاملة (Integrated Modal Dialogs Management):
 *    - فتح وإغلاق حوارات العملات [CurrencySettingsDialog]، الهوية التجارية [BusinessProfileDialog]، والأمان [SecurityDialog].
 * 4. التغذية اللمسية والتواصل والدعم (Haptic Feedback & Direct Support Action):
 *    - تفعيل الاهتزازات التفاعلية عند النقر، وتوفير أزرار مباشرة للاتصال بالدعم الفني أو المراسلة عبر واتساب.
 */
package com.example.ui.components

// ---------------------------------------------------------------------
// استيراد حزم Compose ومكونات Material 3 والرموز والمساعدات
// ---------------------------------------------------------------------
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
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
import com.example.R
import com.example.data.local.entities.AppSettings
import com.example.ui.helper.dialPhoneNumber
import com.example.ui.helper.openWhatsAppChat
import com.example.ui.navigation.Screen
import com.example.ui.screens.BusinessProfileDialog
import com.example.ui.screens.SecurityDialog
import com.example.ui.theme.mizanColors
import com.example.ui.viewmodel.SecurityAndLicenseViewModel

/**
 * [مكون درج التنقل الجانبي الرئيسي - AppNavigationDrawer]:
 * 
 * @param currentScreen الشاشة النشطة الحالية لتحديد العنصر المختار.
 * @param onScreenSelected حدث الانتقال لشاشة محددة.
 * @param onBackupClick حدث فتح شاشة النسخ الاحتياطي.
 * @param isActivated حالة تفعيل النسخة الاحترافية.
 * @param onActivateProClick حدث النقر على ترقية النسخة الاحترافية.
 * @param settings إعدادات التطبيق الحالية (المظهر، العملات، وغيرها).
 * @param securityViewModel نموذج العرض الخاص بإدارة الأمان وكلمات المرور.
 * @param onSaveSettings دالة حفظ الإعدادات المحدثة.
 * @param versionName رقم إصدار التطبيق للعرض في التذييل.
 * @param onComprehensiveReportClick حدث فتح التقرير الشامل.
 * @param modifier مخصصات التنسيق الخارجي.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigationDrawer(
    currentScreen: Screen,
    onScreenSelected: (Screen) -> Unit,
    onBackupClick: () -> Unit,
    isActivated: Boolean,
    onActivateProClick: () -> Unit,
    settings: AppSettings,
    securityViewModel: SecurityAndLicenseViewModel,
    onSaveSettings: (AppSettings, String, Double, Boolean) -> Unit,
    versionName: String,
    onComprehensiveReportClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val supportPhoneNumber = stringResource(id = R.string.support_phone_number)
    
    // حالات التحكم بظهور النوافذ المنبثقة من الدرج
    var isShowingCurrencySettings by remember { mutableStateOf(false) }
    var isShowingBusinessProfile by remember { mutableStateOf(false) }
    var isShowingSecuritySettings by remember { mutableStateOf(false) }

    ModalDrawerSheet(
        drawerContainerColor = MaterialTheme.colorScheme.surface,
        modifier = modifier
            .fillMaxWidth(0.85f)
            .widthIn(max = 310.dp)
            .fillMaxHeight(),
        windowInsets = WindowInsets(0, 0, 0, 0)
    ) {
        // =====================================================================
        // قسم: رأس الدرج الجانبي (Drawer Header) مع زر تبديل الوضع الليلي
        // =====================================================================
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
                .background(MaterialTheme.colorScheme.primary)
        ) {
            Box(
                modifier = Modifier.fillMaxWidth()
            ) {
                val systemDark = isSystemInDarkTheme()
                val isDark = remember(settings.themeMode, systemDark) {
                    when (settings.themeMode) {
                        1 -> false
                        2 -> true
                        else -> systemDark
                    }
                }

                IconButton(
                    onClick = {
                        val newMode = if (isDark) 1 else 2
                        saveFastThemePreference(context, newMode)
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
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 24.dp, vertical = 22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .background(
                                color = MaterialTheme.mizanColors.headerControlContainer,
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    Text(
                        text = stringResource(id = R.string.app_name_main),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // =====================================================================
        // قسم: قائمة عناصر الدرج القابلة للتمرير (Scrollable Items Column)
        // =====================================================================
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            DrawerItem(
                selected = false,
                icon = Icons.Default.People,
                label = stringResource(id = R.string.drawer_business_profile_label),
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    isShowingBusinessProfile = true
                }
            )

            DrawerItem(
                selected = false,
                icon = Icons.Default.Assessment,
                label = stringResource(id = R.string.drawer_comprehensive_report_label),
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onComprehensiveReportClick()
                }
            )
            
            DrawerItem(
                selected = false,
                icon = Icons.Default.Settings,
                label = stringResource(id = R.string.drawer_currency_label),
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    isShowingCurrencySettings = true
                }
            )
            
            DrawerItem(
                selected = false,
                icon = Icons.Default.Lock,
                label = stringResource(id = R.string.drawer_security_label),
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    isShowingSecuritySettings = true
                }
            )
            
            DrawerItem(
                selected = currentScreen == Screen.TRASH,
                icon = Icons.Default.Delete,
                label = stringResource(id = R.string.drawer_trash_label),
                onClick = { onScreenSelected(Screen.TRASH) }
            )

            DrawerItem(
                selected = false,
                icon = if (isActivated) Icons.Default.Verified else Icons.Default.Star,
                label = if (isActivated) stringResource(id = R.string.drawer_activate_pro_success) else stringResource(id = R.string.drawer_activate_pro),
                onClick = onActivateProClick
            )

            DrawerItem(
                selected = false,
                icon = Icons.Default.Refresh,
                label = stringResource(id = R.string.drawer_backup_label1),
                onClick = onBackupClick
            )
        }
        
        // =====================================================================
        // قسم: تذييل الدرج (Drawer Footer) ومعلومات المطور وروابط الاتصال
        // =====================================================================
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f), thickness = 1.dp)
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = stringResource(id = R.string.drawer_app_version, versionName),
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                fontWeight = FontWeight.Normal,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = stringResource(id = R.string.developer_credit),
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                fontWeight = FontWeight.Normal,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(10.dp))
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ContactIcon(
                    icon = Icons.Default.Call,
                    onClick = {
                        dialPhoneNumber(context, supportPhoneNumber)
                    }
                )
                
                ContactIcon(
                    icon = Icons.Default.Share,
                    onClick = {
                        val msg = context.getString(R.string.whatsapp_contact_msg)
                        openWhatsAppChat(context, supportPhoneNumber, msg)
                    }
                )
            }
        }
    }

    // عرض الحوارات المنبثقة عند تفعيل حالتها
    if (isShowingCurrencySettings) {
        CurrencySettingsDialog(
            settings = settings,
            onSaveSettings = onSaveSettings,
            onDismiss = { isShowingCurrencySettings = false }
        )
    }

    if (isShowingBusinessProfile) {
        BusinessProfileDialog(
            onDismiss = { isShowingBusinessProfile = false }
        )
    }

    if (isShowingSecuritySettings) {
        SecurityDialog(
            settings = settings,
            viewModel = securityViewModel,
            onDismiss = { isShowingSecuritySettings = false }
        )
    }
}

/**
 * [دالة مساعدة لحفظ وضع الثيم السريع في التفضيلات - saveFastThemePreference]:
 * تخزن خيار المظهر بشكل متزامن سريع ليتم تطبيقه فورياً دون تأخير.
 */
private fun saveFastThemePreference(context: Context, newMode: Int) {
    val sharedPrefs = context.getSharedPreferences("fast_theme_prefs", Context.MODE_PRIVATE)
    sharedPrefs.edit().putInt("key_fast_theme_mode", newMode).apply()
}


// --- ملاحظات وتوصيات المعمارية البرمجية ---
// 1) تم الحفاظ على الكتلة التنفيذية الأصلية دون تعديل أو حذف أو إعادة ترتيب.
// 2) يوصى مستقبلاً بإبقاء مكونات Compose المشتركة صغيرة ومحددة المسؤولية لتقليل إعادة التركيب غير الضرورية.
// 3) ينبغي إبقاء حالات الحوار والتنقل قابلة للتتبع من مصدر واحد عند اتساع عدد المسارات، مع الحفاظ على السلوك الحالي.
// 4) يوصى بعزل أدوات Intent وحفظ الملفات والاهتزاز عن واجهات Compose قدر الإمكان عبر عقود واضحة، دون تعديل النسخة الحالية.
// 5) في نظام Theme والألوان والطباعة، يوصى بالحفاظ على مصدر تصميم مركزي ومنع القيم المتناثرة مستقبلاً.
// 6) أي تحسين لاحق يجب أن يسبقه اختبار تكافؤ سلوكي/مرئي مناسب قبل الدمج.
