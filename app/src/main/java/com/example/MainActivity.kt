/**
 * =====================================================================
 * ملف: النشاط الرئيسي للتطبيق (MainActivity.kt)
 * =====================================================================
 * 
 * [الغرض العام والتعليمي من الملف]:
 * يمثل هذا الملف النشاط الأحادي الأساسي للتطبيق (Single Activity Architecture)
 * المبني بالكامل باستخدام Jetpack Compose. يتحكم في:
 * 1. تهيئة شاشة البداية (Splash Screen) وإبقائها معروضة ريثما تكتمل قراءة الإعدادات.
 * 2. تفعيل التصميم الممتد حتى حواف الشاشة (Edge-to-Edge).
 * 3. جدولة وتشغيل العمال الخلفيين للنسخ والتذكير (Workers).
 * 4. إدارة قفل التطبيق برمز المرور (App Lock & Security Verification).
 * 5. إدارة نافذة الترحيب التفاعلية (Onboarding) وطلب الأذونات في الوقت المناسب.
 * 6. تطبيق اتجاه الواجهة العربي الشامل من اليمين إلى اليسار (RTL Layout Direction).
 * 7. تطبيق السمة المرئية (الوضع الفاتح / الداكن / النظام) بسلاسة وفورية.
 * 8. إنشاء نسخة احتياطية محلية صامتة عند مغادرة التطبيق (onStop).
 */
package com.example

// ---------------------------------------------------------------------
// استيراد حزم أندرويد و Jetpack Compose ونماذج العرض (ViewModels) وإدارة الحالة
// ---------------------------------------------------------------------
import androidx.activity.viewModels
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.WelcomeOnboardingDialog
import com.example.ui.main.MainAppLayout
import com.example.ui.screens.AppLockScreen
import com.example.ui.theme.AppTheme
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import com.example.ui.viewmodel.FinanceViewModel
import com.example.ui.viewmodel.HabayebFinanceViewModel
import com.example.ui.viewmodel.BackupSyncViewModel
import com.example.ui.screens.habayeb.utils.HabayebRecurringManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch
import androidx.lifecycle.lifecycleScope

/**
 * [فئة النشاط الرئيسي - MainActivity]:
 * ترث من `FragmentActivity` لدعم دوال التوافق والمصادقة الحيوية ونظام Compose.
 */
class MainActivity : FragmentActivity() {
    private val backupSyncViewModel: BackupSyncViewModel by viewModels()

    /**
     * [دالة دورة الحياة - onCreate]:
     * تهيئ شاشة البداية، ونماذج العرض، وتبني شجرة واجهات Compose.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        // تثبيت شاشة البداية الرسمية
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        // تفعيل تصميم الحواف الشفافة الحديث Edge-to-Edge
        enableEdgeToEdge()

        // تهيئة نماذج العرض المركزية (ViewModels) المرتبطة بدورة حياة النشاط
        val viewModel = androidx.lifecycle.ViewModelProvider(this)[FinanceViewModel::class.java]
        val securityViewModel = androidx.lifecycle.ViewModelProvider(this)[com.example.ui.viewmodel.SecurityAndLicenseViewModel::class.java]

        // إبقاء شاشة البداية ظاهرة حتى تنتهي قاعدة البيانات من تحميل الإعدادات بالكامل
        splashScreen.setKeepOnScreenCondition {
            !viewModel.isSettingsLoaded.value
        }

        // تأجيل العمليات الخلفية غير الحرجة (إعداد العمال وجلسة جوجل) إلى ما بعد رسم أول إطار للواجهة
        window.decorView.post {
            lifecycleScope.launch(Dispatchers.IO) {
                com.example.domain.GoogleAuthSessionManager.initialize(applicationContext)
                AutoBackupWorker.scheduleDailyBackupWorker(this@MainActivity)
                AutoBackupWorker.checkAndTriggerBackupIfMissed(this@MainActivity)
                BackupReminderWorker.scheduleReminder(this@MainActivity)
            }
        }

        // قراءة تفضيلات القفل والسمة السريعة لمنع وميض الشاشة عند الإقلاع
        val secPrefs = getSharedPreferences("mizan_sec_prefs", MODE_PRIVATE)
        val isPasscodeEnabledFast = secPrefs.getBoolean("fast_passcode_enabled", false)

        val sharedPrefs = getSharedPreferences("fast_theme_prefs", MODE_PRIVATE)
        val cachedThemeMode = sharedPrefs.getInt("key_fast_theme_mode", 0) // 0: System, 1: Light, 2: Dark

        // بناء واجهة المستخدم الرسومية عبر Jetpack Compose
        setContent {
            val habayebViewModel: HabayebFinanceViewModel = viewModel()

            val context = LocalContext.current

            // مراقبة أحداث الأمان وطرد الجلسة غير المصرح بها
            LaunchedEffect(securityViewModel) {
                securityViewModel.kickoutEvent.collect { reason ->
                    android.widget.Toast.makeText(
                        this@MainActivity,
                        reason,
                        android.widget.Toast.LENGTH_LONG
                    ).show()
                }
            }

            // تتبع حالة الإعدادات التلقائية المحدثة
            val settings by viewModel.settingsState.collectAsStateWithLifecycle()
            val isSettingsLoaded by viewModel.isSettingsLoaded.collectAsStateWithLifecycle()

            // تأجيل المهام غير الأساسية (مراقبة الترخيص والمعاملات المتكررة) إلى ما بعد رسم أول إطار مرئي
            LaunchedEffect(isSettingsLoaded) {
                if (isSettingsLoaded) {
                    kotlinx.coroutines.delay(800)
                    securityViewModel.startRealtimeMonitoring(this@MainActivity)
                    withContext(Dispatchers.IO) {
                        HabayebRecurringManager.checkAndExecuteRecurring(context, habayebViewModel) { count ->
                            android.os.Handler(android.os.Looper.getMainLooper()).post {
                                android.widget.Toast.makeText(
                                    context,
                                    context.getString(R.string.toast_recurring_txs_success, count),
                                    android.widget.Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    }
                }
            }

            // الاستماع لأحداث الواجهة مثل رسائل Toast ومربعات التفعيل
            LaunchedEffect(viewModel) {
                viewModel.uiEventFlow.collect { event ->
                    when (event) {
                        is com.example.ui.viewmodel.UiEvent.ShowToast -> {
                            android.widget.Toast.makeText(
                                context,
                                context.getString(event.messageRes),
                                if (event.isLong) android.widget.Toast.LENGTH_LONG else android.widget.Toast.LENGTH_SHORT
                            ).show()
                        }
                        is com.example.ui.viewmodel.UiEvent.ShowActivationDialog -> {
                            securityViewModel.showActivationRequired.value = true
                        }
                    }
                }
            }

            var isUnlocked by rememberSaveable { mutableStateOf(!isPasscodeEnabledFast) }

            // مزامنة حالة تفعيل رمز المرور في التفضيلات السريعة على مسار خلفي
            LaunchedEffect(settings.isPasscodeEnabled, isSettingsLoaded) {
                if (isSettingsLoaded) {
                    withContext(Dispatchers.IO) {
                        secPrefs.edit().putBoolean("fast_passcode_enabled", settings.isPasscodeEnabled).apply()
                    }
                }
            }

            var showOnboardingDialog by remember { mutableStateOf(false) }
            var shouldRequestPermissions by remember { mutableStateOf(false) }

            // تجهيز آلية طلب الأذونات المتعددة ديناميكياً
            val permissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
                contract = androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions()
            ) { permissions ->
                val allGranted = permissions.values.all { it }
                android.util.Log.d("MainActivity", "Permissions completed: allGranted=$allGranted")
            }

            LaunchedEffect(shouldRequestPermissions) {
                if (shouldRequestPermissions) {
                    val permissions = mutableListOf<String>()
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                        permissions.add(android.Manifest.permission.POST_NOTIFICATIONS)
                    }
                    if (android.os.Build.VERSION.SDK_INT <= android.os.Build.VERSION_CODES.S_V2) {
                        permissions.add(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                        permissions.add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    } else {
                        permissions.add(android.Manifest.permission.READ_MEDIA_IMAGES)
                    }
                    permissionLauncher.launch(permissions.toTypedArray())
                    shouldRequestPermissions = false
                }
            }

            // عرض نافذة الترحيب بالتشغيل الأول بعد تأخير أنيق (تذكر القراءة لمنع إعادة الاستعلام في Recomposition)
            val isReallyFirstLaunch = remember(settings.isFirstLaunch) {
                settings.isFirstLaunch && !viewModel.hasShownOnboarding()
            }
            LaunchedEffect(isReallyFirstLaunch) {
                if (isReallyFirstLaunch) {
                    // Let the user breathe, see and experience the app interface behind first (3500ms elegant delay)
                    kotlinx.coroutines.delay(3500)
                    showOnboardingDialog = true
                }
            }

            // احتساب السمة المرئية المختارة (فاتح / داكن / تتبع النظام)
            val isSystemDark = androidx.compose.foundation.isSystemInDarkTheme()
            val darkTheme = remember(isSettingsLoaded, settings.themeMode, cachedThemeMode, isSystemDark) {
                if (isSettingsLoaded) {
                    when (settings.themeMode) {
                        1 -> false
                        2 -> true
                        else -> isSystemDark
                    }
                } else {
                    when (cachedThemeMode) {
                        1 -> false
                        2 -> true
                        else -> isSystemDark
                    }
                }
            }

            // تطبيق السمة وموفر الاتجاه العربي (RTL)
            AppTheme(darkTheme = darkTheme) {
                // تحديث أشرطة النظام عند تغير السمة فقط لتجنب إعادة التنفيذ مع كل Recomposition
                LaunchedEffect(darkTheme) {
                    window.statusBarColor = android.graphics.Color.TRANSPARENT
                    window.navigationBarColor = android.graphics.Color.TRANSPARENT
                    val insetsController = WindowCompat.getInsetsController(window, window.decorView)
                    insetsController.isAppearanceLightStatusBars = !darkTheme
                    insetsController.isAppearanceLightNavigationBars = !darkTheme
                }
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    // 1. نافذة الترحيب والإعداد الأولي عند التثبيت
                    if (isReallyFirstLaunch && showOnboardingDialog) {
                        WelcomeOnboardingDialog(
                            onDismiss = {
                                viewModel.markOnboardingShown() // Persist in SharedPreferences first
                                val updated = settings.copy(isFirstLaunch = false)
                                viewModel.saveSettings(updated)
                                showOnboardingDialog = false
                                shouldRequestPermissions = true // Request storage/post permissions immediately after welcome greeting!
                            }
                        )
                    }

                    // 2. شاشة قفل التطبيق إذا كان مفتاح الحماية مفعلاً ولم يتم فك القفل بعد
                    if (settings.isPasscodeEnabled && !isUnlocked) {
                        AppLockScreen(
                            viewModel = securityViewModel,
                            onUnlockSuccess = { isUnlocked = true },
                            onUnlockBypassedAndDisabled = {
                                val updated = settings.copy(
                                    isPasscodeEnabled = false,
                                    passcodeHash = null,
                                    recoveryPhraseHash = null
                                )
                                securityViewModel.saveSettings(updated)
                                isUnlocked = true
                            }
                        )
                    } else {
                        // 3. تخطيط التطبيق الرئيسي بكافة شاشاته وتبويباته
                        MainAppLayout(
                            viewModel = viewModel,
                            habayebViewModel = habayebViewModel,
                            securityViewModel = securityViewModel,
                            backupSyncViewModel = backupSyncViewModel,
                            settings = settings,
                            onExit = { 
                                finishAffinity() 
                            }
                        )
                    }
                }
            }
        }
    }

    /**
     * [دالة دورة الحياة - onStop]:
     * تستدعى عندما ينتقل التطبيق إلى الخلفية. تنفذ نسخة احتياطية صامتة وسريعة للحفاظ على البيانات.
     */
    override fun onStop() {
        super.onStop()
        try {
            backupSyncViewModel.triggerSilentLocalBackup()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
