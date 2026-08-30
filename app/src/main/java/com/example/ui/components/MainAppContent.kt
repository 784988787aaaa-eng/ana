package com.example.ui.components

/*
 * =====================================================================================
 * حزمة المكونات وموجه شاشات التطبيق (Main Application Navigation & Content Host)
 * -------------------------------------------------------------------------------------
 * تحتوي هذه الحزمة على حاوية المحتوى الرئيسي وموجه الشاشات الأساسي الذي يدير التنقل
 * الحركي والانتقالات البصرية بين مختلف شاشات التطبيق.
 * =====================================================================================
 */

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.ui.unit.IntOffset
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.example.data.local.entities.AppSettings
import com.example.ui.navigation.Screen
import com.example.ui.screens.*
import com.example.ui.viewmodel.FinanceViewModel
import com.example.ui.viewmodel.HabayebFinanceViewModel
import com.example.ui.viewmodel.SecurityAndLicenseViewModel
import com.example.ui.viewmodel.BackupSyncViewModel

/*
 * =====================================================================================
 * المكون الرئيسي: حاوية المحتوى والانتقالات الحركية (MainAppContent)
 * -------------------------------------------------------------------------------------
 * [الوصف والهدف]:
 * الحاوية المركزية المسؤولة عن تبديل وعرض شاشات التطبيق الرئيسية والفرعية:
 * 1. استضافة الشاشة النشطة حالياً (Current Screen) استناداً إلى حالة التنقل.
 * 2. توفير انتقالات حركية سلسة وسريعة الاستجابة (Custom Spring Animations):
 *    - انزلاق عمودي وتكبير للشاشات الفرعية (الإعدادات، سلة المهملات، الملف التعريفي، الأمان).
 *    - انزلاق أفقي للشاشات المتوازية (شاشة الحبايب وشاشة دفتر اليومية).
 * 3. تمرير نماذج العرض (ViewModels) وحالة القائمة الجانبية وشريط البحث إلى الشاشة النشطة.
 *
 * [البيانات والمُدخلات]:
 * - currentScreen: مسار الشاشة الحالية المراد عرضها (Enum Screen).
 * - viewModel & habayebViewModel & securityViewModel & backupSyncViewModel: نماذج إدارة البيانات.
 * - settings: إعدادات التطبيق العامة.
 * - contentPadding: الهوامش المتروكة لأشرطة النظام العلوية والسفلية.
 * - onNavigate & onMenuClick & onExit: أحداث التنقل والتحكم.
 * =====================================================================================
 */
@Composable
fun MainAppContent(
    currentScreen: Screen,
    viewModel: FinanceViewModel,
    habayebViewModel: HabayebFinanceViewModel,
    securityViewModel: SecurityAndLicenseViewModel,
    backupSyncViewModel: BackupSyncViewModel,
    settings: AppSettings,
    contentPadding: PaddingValues = PaddingValues(),
    onNavigate: (Screen) -> Unit,
    onMenuClick: () -> Unit,
    onExit: () -> Unit,
    isDrawerOpen: Boolean = false,
    onHeaderDoubleClick: () -> Unit = {},
    isFloatingSearchActive: Boolean = false,
    onFloatingSearchActiveChanged: (Boolean) -> Unit = {},
    isSearchActive: Boolean = false,
    onSearchActiveChanged: (Boolean) -> Unit = {},
    isHistoryOverlayActive: Boolean = false,
    onHistoryOverlayActiveChanged: (Boolean) -> Unit = {},
    isHistorySearchActive: Boolean = false,
    onHistorySearchActiveChanged: (Boolean) -> Unit = {},
    onFabOverlayChanged: (((@Composable () -> Unit)?) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    /*
     * ---------------------------------------------------------------------------------
     * تذكر حالة وسلوك القائمة الجانبية (Drawer State Holder)
     * ---------------------------------------------------------------------------------
     */
    val drawerStateHolder = rememberMainAppContentState(
        isDrawerOpen = isDrawerOpen,
        onMenuClick = onMenuClick
    )

    /*
     * ---------------------------------------------------------------------------------
     * إعداد محركات الحركات الفيزيائية (Spring Animation Specs)
     * ---------------------------------------------------------------------------------
     * تخصيص مواصفات النابض الفيزيائي (Stiffness & Damping) لإنتاج حركة طبيعية مريحة للعين.
     * ---------------------------------------------------------------------------------
     */
    Box(modifier = modifier.fillMaxSize()) {
        val premiumSpring = remember {
            spring<Float>(
                dampingRatio = 0.85f,
                stiffness = 350f
            )
        }
        val premiumOffsetSpring = remember {
            spring<IntOffset>(
                dampingRatio = 0.85f,
                stiffness = 350f
            )
        }

        /*
         * -----------------------------------------------------------------------------
         * الانتقال المتحرك بين الشاشات (Animated Content Host)
         * -----------------------------------------------------------------------------
         * يقوم بفحص نوع الشاشة الحالية والسابقة لتحديد اتجاه الحركة (عمودي أم أفقي).
         * -----------------------------------------------------------------------------
         */
        AnimatedContent(
            targetState = currentScreen,
            transitionSpec = {
                val isInitialSub = initialState == Screen.SETTINGS || initialState == Screen.TRASH || initialState == Screen.BUSINESS_PROFILE || initialState == Screen.SECURITY
                val isTargetSub = targetState == Screen.SETTINGS || targetState == Screen.TRASH || targetState == Screen.BUSINESS_PROFILE || targetState == Screen.SECURITY

                if (isTargetSub && !isInitialSub) {
                    // الدخول إلى شاشة فرعية: انزلاق للأعلى من الأسفل مع ظهور تدريجي وتكبير خفيف
                    val slideIn = slideInVertically(animationSpec = premiumOffsetSpring) { it } +
                            fadeIn(animationSpec = premiumSpring) +
                            scaleIn(initialScale = 0.95f, animationSpec = premiumSpring)
                    val slideOut = fadeOut(animationSpec = premiumSpring) +
                            scaleOut(targetScale = 0.95f, animationSpec = premiumSpring)
                    slideIn togetherWith slideOut
                } else if (isInitialSub && !isTargetSub) {
                    // الخروج من شاشة فرعية: انزلاق للأسفل مع اختفاء تدريجي وتصغير خفيف
                    val slideIn = fadeIn(animationSpec = premiumSpring) +
                            scaleIn(initialScale = 0.95f, animationSpec = premiumSpring)
                    val slideOut = slideOutVertically(animationSpec = premiumOffsetSpring) { it } +
                            fadeOut(animationSpec = premiumSpring) +
                            scaleOut(targetScale = 0.95f, animationSpec = premiumSpring)
                    slideIn togetherWith slideOut
                } else {
                    // التنقل الأفقي المتبادل بين الشاشات الرئيسية
                    val isForward = targetState.ordinal > initialState.ordinal
                    val slideIn = if (isForward) {
                        slideInHorizontally(animationSpec = premiumOffsetSpring) { width -> width } +
                        fadeIn(animationSpec = premiumSpring) +
                        scaleIn(initialScale = 0.95f, animationSpec = premiumSpring)
                    } else {
                        slideInHorizontally(animationSpec = premiumOffsetSpring) { width -> -width } +
                        fadeIn(animationSpec = premiumSpring) +
                        scaleIn(initialScale = 1.05f, animationSpec = premiumSpring)
                    }
                    val slideOut = if (isForward) {
                        slideOutHorizontally(animationSpec = premiumOffsetSpring) { width -> -width } +
                        fadeOut(animationSpec = premiumSpring) +
                        scaleOut(targetScale = 1.05f, animationSpec = premiumSpring)
                    } else {
                        slideOutHorizontally(animationSpec = premiumOffsetSpring) { width -> width } +
                        fadeOut(animationSpec = premiumSpring) +
                        scaleOut(targetScale = 0.95f, animationSpec = premiumSpring)
                    }
                    slideIn togetherWith slideOut
                }
            },
            label = "ScreenSwitch"
        ) { screen ->
            /*
             * -------------------------------------------------------------------------
             * توجيه وعرض الشاشة المطلوبة وتمرير المعاملات (Screen Routing Branching)
             * -------------------------------------------------------------------------
             */
            when (screen) {
                Screen.HABAYEB -> {
                    HabayebScreen(
                        viewModel = habayebViewModel,
                        securityViewModel = securityViewModel,
                        onMenuClick = { drawerStateHolder.handleMenuClick() },
                        onClose = onExit,
                        contentPadding = contentPadding,
                        isDrawerOpen = drawerStateHolder.isDrawerOpen,
                        onHeaderDoubleClick = onHeaderDoubleClick,
                        isFloatingSearchActive = isFloatingSearchActive,
                        onFloatingSearchActiveChanged = onFloatingSearchActiveChanged,
                        isSearchActive = isSearchActive,
                        onSearchActiveChanged = onSearchActiveChanged,
                        isHistoryOverlayActive = isHistoryOverlayActive,
                        onHistoryOverlayActiveChanged = onHistoryOverlayActiveChanged,
                        isHistorySearchActive = isHistorySearchActive,
                        onHistorySearchActiveChanged = onHistorySearchActiveChanged,
                        onFabOverlayChanged = onFabOverlayChanged
                    )
                }
                Screen.LEDGER -> {
                    MainLedgerView(
                        viewModel = viewModel,
                        habayebViewModel = habayebViewModel,
                        securityViewModel = securityViewModel,
                        settings = settings,
                        onBackIntercept = {},
                        onMenuClick = { drawerStateHolder.handleMenuClick() },
                        isDrawerOpen = drawerStateHolder.isDrawerOpen,
                        isFloatingSearchActive = isFloatingSearchActive,
                        onFloatingSearchActiveChanged = onFloatingSearchActiveChanged,
                        isSearchActive = isSearchActive,
                        onSearchActiveChanged = onSearchActiveChanged,
                        contentPadding = contentPadding
                    )
                }
                Screen.SETTINGS -> {
                    SettingsView(
                        viewModel = viewModel,
                        habayebViewModel = habayebViewModel,
                        backupSyncViewModel = backupSyncViewModel,
                        settings = settings,
                        onNavigateToSecurity = { onNavigate(Screen.SECURITY) },
                        contentPadding = contentPadding
                    )
                }
                Screen.TRASH -> {
                    TrashScreen(
                        viewModel = viewModel,
                        habayebViewModel = habayebViewModel,
                        onBack = { onNavigate(Screen.HABAYEB) },
                        contentPadding = contentPadding
                    )
                }
                Screen.BUSINESS_PROFILE -> {
                    BusinessProfileScreen(
                        onBack = { onNavigate(Screen.HABAYEB) },
                        contentPadding = contentPadding
                    )
                }
                Screen.SECURITY -> {
                    SecurityScreen(
                        settings = settings,
                        viewModel = securityViewModel,
                        onBack = { onNavigate(Screen.LEDGER) },
                        contentPadding = contentPadding
                    )
                }
            }
        }
    }
}

