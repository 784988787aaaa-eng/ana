/**
 * التوثيق المعماري العربي — مشروع «الدفتر الذكي / ميزان الدار».
 * الملف: app/src/main/java/com/example/ui/components/MainAppContent.kt
 * المسؤولية: الحاوية الرئيسية لمحتوى التطبيق التي تنسق الشاشة الحالية مع عناصر التنقل والواجهة المشتركة.
 *
 * القراءة التعليمية: يوضح هذا الملف كيف تنتقل حالة التطبيق من الطبقة المشتركة
 * إلى المشهد المرئي على الهاتف، مع تفسير العقود والحالة والتوابع والتفاعلات.
 * الكتلة التنفيذية الأصلية أدناه محفوظة حرفياً؛ الإضافات التوثيقية لا تعدّل
 * أي رمز تنفيذي وفق قاعدة Zero Code Alteration.
 */

// --- فهرس التوثيق التعليمي للسطر التنفيذي ---
// توثيق السطر 1: التوجيه الحزمي يحدد الموضع المنطقي للملف داخل طبقة الواجهة.
// توثيق السطر 3: الاستيراد التالي اعتماد أصلي يحتاجه الملف؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 4: الاستيراد التالي اعتماد أصلي يحتاجه الملف؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 5: الاستيراد التالي اعتماد أصلي يحتاجه الملف؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 6: الاستيراد التالي اعتماد أصلي يحتاجه الملف؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 7: الاستيراد التالي اعتماد أصلي يحتاجه الملف؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 8: الاستيراد التالي اعتماد أصلي يحتاجه الملف؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 9: الاستيراد التالي اعتماد أصلي يحتاجه الملف؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 10: الاستيراد التالي اعتماد أصلي يحتاجه الملف؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 11: الاستيراد التالي اعتماد أصلي يحتاجه الملف؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 12: الاستيراد التالي اعتماد أصلي يحتاجه الملف؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 13: الاستيراد التالي اعتماد أصلي يحتاجه الملف؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 14: الاستيراد التالي اعتماد أصلي يحتاجه الملف؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 15: الاستيراد التالي اعتماد أصلي يحتاجه الملف؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 16: الاستيراد التالي اعتماد أصلي يحتاجه الملف؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 17: الاستيراد التالي اعتماد أصلي يحتاجه الملف؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 18: الاستيراد التالي اعتماد أصلي يحتاجه الملف؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 19: الاستيراد التالي اعتماد أصلي يحتاجه الملف؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 20: الاستيراد التالي اعتماد أصلي يحتاجه الملف؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 21: الاستيراد التالي اعتماد أصلي يحتاجه الملف؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 22: الاستيراد التالي اعتماد أصلي يحتاجه الملف؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 23: الاستيراد التالي اعتماد أصلي يحتاجه الملف؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 24: الاستيراد التالي اعتماد أصلي يحتاجه الملف؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 25: الاستيراد التالي اعتماد أصلي يحتاجه الملف؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 26: الاستيراد التالي اعتماد أصلي يحتاجه الملف؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 27: الاستيراد التالي اعتماد أصلي يحتاجه الملف؛ لم يتم تغيير أي اعتماد.
// توثيق السطر 29: التعليمة الوصفية التالية جزء من العقد الأصلي للواجهة أو التجميع، ولم يُغيّر سلوكها.
// توثيق السطر 79: الشرط التالي يحافظ على قرار التنفيذ الأصلي.
// توثيق السطر 113: التفرع التالي يوزع السلوك بحسب الحالة الأصلية.

package com.example.ui.components

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
    val drawerStateHolder = rememberMainAppContentState(
        isDrawerOpen = isDrawerOpen,
        onMenuClick = onMenuClick
    )

    Box(modifier = modifier.fillMaxSize()) {
        val navFadeSpec = remember {
            spring<Float>(
                dampingRatio = 0.9f,
                stiffness = 500f
            )
        }
        val navOffsetSpec = remember {
            spring<IntOffset>(
                dampingRatio = 0.9f,
                stiffness = 500f
            )
        }

        AnimatedContent(
            targetState = currentScreen,
            transitionSpec = {
                val isInitialSub = initialState == Screen.SETTINGS || initialState == Screen.TRASH || initialState == Screen.BUSINESS_PROFILE || initialState == Screen.SECURITY
                val isTargetSub = targetState == Screen.SETTINGS || targetState == Screen.TRASH || targetState == Screen.BUSINESS_PROFILE || targetState == Screen.SECURITY

                if (isTargetSub && !isInitialSub) {
                    // Entering secondary/settings screen: Vertical spring entrance with subtle fade
                    val slideIn = slideInVertically(animationSpec = navOffsetSpec) { (it * 0.12f).toInt() } +
                            fadeIn(animationSpec = navFadeSpec)
                    val slideOut = fadeOut(animationSpec = navFadeSpec)
                    slideIn togetherWith slideOut
                } else if (isInitialSub && !isTargetSub) {
                    // Exiting secondary screen back to main: Subtle fade & slide down
                    val slideIn = fadeIn(animationSpec = navFadeSpec)
                    val slideOut = slideOutVertically(animationSpec = navOffsetSpec) { (it * 0.12f).toInt() } +
                            fadeOut(animationSpec = navFadeSpec)
                    slideIn togetherWith slideOut
                } else {
                    // Lateral tab switching (Ledger <-> Habayeb): Horizontal translation with clean fade
                    val isForward = targetState.ordinal > initialState.ordinal
                    val slideIn = if (isForward) {
                        slideInHorizontally(animationSpec = navOffsetSpec) { width -> (width * 0.2f).toInt() } +
                        fadeIn(animationSpec = navFadeSpec)
                    } else {
                        slideInHorizontally(animationSpec = navOffsetSpec) { width -> (-width * 0.2f).toInt() } +
                        fadeIn(animationSpec = navFadeSpec)
                    }
                    val slideOut = if (isForward) {
                        slideOutHorizontally(animationSpec = navOffsetSpec) { width -> (-width * 0.2f).toInt() } +
                        fadeOut(animationSpec = navFadeSpec)
                    } else {
                        slideOutHorizontally(animationSpec = navOffsetSpec) { width -> (width * 0.2f).toInt() } +
                        fadeOut(animationSpec = navFadeSpec)
                    }
                    slideIn togetherWith slideOut
                }
            },
            label = "ScreenSwitch"
        ) { screen ->
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

// --- ملاحظات وتوصيات المعمارية البرمجية ---
// 1) تم الحفاظ على الكتلة التنفيذية الأصلية دون تعديل أو حذف أو إعادة ترتيب.
// 2) يوصى مستقبلاً بإبقاء مكونات Compose المشتركة صغيرة ومحددة المسؤولية لتقليل إعادة التركيب غير الضرورية.
// 3) ينبغي إبقاء حالات الحوار والتنقل قابلة للتتبع من مصدر واحد عند اتساع عدد المسارات، مع الحفاظ على السلوك الحالي.
// 4) يوصى بعزل أدوات Intent وحفظ الملفات والاهتزاز عن واجهات Compose قدر الإمكان عبر عقود واضحة، دون تعديل النسخة الحالية.
// 5) في نظام Theme والألوان والطباعة، يوصى بالحفاظ على مصدر تصميم مركزي ومنع القيم المتناثرة مستقبلاً.
// 6) أي تحسين لاحق يجب أن يسبقه اختبار تكافؤ سلوكي/مرئي مناسب قبل الدمج.
