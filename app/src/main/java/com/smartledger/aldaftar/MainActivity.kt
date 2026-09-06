/**
 * =====================================================================
 * توثيق عربي للمسار التنفيذي.
 * =====================================================================
 * 
 * [الغرض العام والتعليمي من الملف]:
 * يمثل هذا الملف النشاط الأحادي الأساسي للتطبيق ()
 * المبني بالكامل باستخدام . يتحكم في:
 * 1. تهيئة شاشة البداية () وإبقائها معروضة ريثما تكتمل قراءة الإعدادات.
 * 2. تفعيل التصميم الممتد حتى حواف الشاشة ().
 * 3. جدولة وتشغيل العمال الخلفيين للنسخ والتذكير ().
 * 4. إدارة قفل التطبيق برمز المرور ().
 * 5. إدارة نافذة الترحيب التفاعلية () وطلب الأذونات في الوقت المناسب.
 * 6. تطبيق اتجاه الواجهة العربي الشامل من اليمين إلى اليسار ().
 * 7. تطبيق السمة المرئية (الوضع الفاتح / الداكن / النظام) بسلاسة وفورية.
 * 8. إنشاء نسخة احتياطية محلية صامتة عند مغادرة التطبيق (إيقاف النشاط).
 */
package com.smartledger.aldaftar

// ---------------------------------------------------------------------
// استيراد حزم أندرويد و  ونماذج العرض () وإدارة الحالة
// ---------------------------------------------------------------------
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
import com.smartledger.aldaftar.ui.components.WelcomeOnboardingDialog
import com.smartledger.aldaftar.ui.main.MainAppLayout
import com.smartledger.aldaftar.ui.screens.AppLockScreen
import com.smartledger.aldaftar.ui.theme.AppTheme
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.smartledger.aldaftar.ui.viewmodel.FinanceViewModel
import com.smartledger.aldaftar.ui.viewmodel.HabayebFinanceViewModel
import com.smartledger.aldaftar.ui.viewmodel.BackupSyncViewModel
import com.smartledger.aldaftar.ui.screens.habayeb.utils.HabayebRecurringManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch
import androidx.lifecycle.lifecycleScope

/**
 * توثيق عربي للمسار التنفيذي.
 * ترث من النشاط الأساسي لدعم دوال التوافق والمصادقة الحيوية ونظام .
 */
class MainActivity : FragmentActivity() {
    private lateinit var backupSyncViewModel: BackupSyncViewModel

    /**
     * [دالة دورة الحياة - إنشاء النشاط]:
     * تهيئ شاشة البداية، ونماذج العرض، وتبني شجرة واجهات .
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        // تثبيت شاشة البداية الرسمية
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        // تفعيل تصميم الحواف الشفافة الحديث 
        enableEdgeToEdge()

        // تهيئة نماذج العرض المركزية () المرتبطة بدورة حياة النشاط
        val viewModel = androidx.lifecycle.ViewModelProvider(this)[FinanceViewModel::class.java]
        val securityViewModel = androidx.lifecycle.ViewModelProvider(this)[com.smartledger.aldaftar.ui.viewmodel.SecurityAndLicenseViewModel::class.java]
        backupSyncViewModel = androidx.lifecycle.ViewModelProvider(this)[BackupSyncViewModel::class.java]

        // إبقاء شاشة البداية ظاهرة حتى تنتهي قاعدة البيانات من تحميل الإعدادات بالكامل
        splashScreen.setKeepOnScreenCondition {
            !viewModel.isSettingsLoaded.value
        }

        // إطلاق مهام التهيئة الخلفية المستقلة عن مسار الواجهة
        lifecycleScope.launch(Dispatchers.IO) {
            AutoBackupWorker.scheduleDailyBackupWorker(this@MainActivity)
            AutoBackupWorker.checkAndTriggerBackupIfMissed(this@MainActivity)
            BackupReminderWorker.scheduleReminder(this@MainActivity)
        }

        // قراءة تفضيلات القفل والسمة السريعة لمنع وميض الشاشة عند الإقلاع
        val secPrefs = getSharedPreferences("mizan_sec_prefs", MODE_PRIVATE)
        val isPasscodeEnabledFast = secPrefs.getBoolean("fast_passcode_enabled", false)

        val sharedPrefs = getSharedPreferences("fast_theme_prefs", MODE_PRIVATE)
        val cachedThemeMode = sharedPrefs.getInt("key_fast_theme_mode", 0) // صفر للنظام، واحد للوضع الفاتح، واثنان للوضع الداكن

        // بناء واجهة المستخدم الرسومية عبر 
        setContent {
            val habayebViewModel: HabayebFinanceViewModel = viewModel()

            val context = LocalContext.current

            // مراقبة أحداث الأمان وطرد الجلسة غير المصرح بها
            LaunchedEffect(securityViewModel) {
                securityViewModel.startRealtimeMonitoring(this@MainActivity)
                securityViewModel.kickoutEvent.collect { reason ->
                    android.widget.Toast.makeText(
                        this@MainActivity,
                        reason,
                        android.widget.Toast.LENGTH_LONG
                    ).show()
                }
            }

            // فحص وتنفيذ المعاملات المتكررة (الرواتب والأقساط المستحقة) في الخلفية عند الإقلاع
            LaunchedEffect(habayebViewModel) {
                withContext(Dispatchers.IO) {
                    // 
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

                // الاستماع لأحداث الواجهة مثل رسائل رسالة مؤقتة ومربعات التفعيل
                viewModel.uiEventFlow.collect { event ->
                    when (event) {
                        is com.smartledger.aldaftar.ui.viewmodel.UiEvent.ShowToast -> {
                            android.widget.Toast.makeText(
                                context,
                                context.getString(event.messageRes),
                                if (event.isLong) android.widget.Toast.LENGTH_LONG else android.widget.Toast.LENGTH_SHORT
                            ).show()
                        }
                        is com.smartledger.aldaftar.ui.viewmodel.UiEvent.ShowActivationDialog -> {
                            securityViewModel.showActivationRequired.value = true
                        }
                    }
                }
            }

            // تتبع حالة الإعدادات التلقائية المحدثة
            val settings by viewModel.settingsState.collectAsStateWithLifecycle()
            val isSettingsLoaded by viewModel.isSettingsLoaded.collectAsStateWithLifecycle()
            
            var isUnlocked by rememberSaveable { mutableStateOf(!isPasscodeEnabledFast) }

            // مزامنة حالة تفعيل رمز المرور في التفضيلات السريعة
            LaunchedEffect(settings.isPasscodeEnabled) {
                if (isSettingsLoaded) {
                    secPrefs.edit().putBoolean("fast_passcode_enabled", settings.isPasscodeEnabled).apply()
                }
            }

            var showOnboardingDialog by remember { mutableStateOf(false) }

            // عرض نافذة الترحيب بالتشغيل الأول بعد جاهزية الواجهة مباشرة
            val isReallyFirstLaunch = settings.isFirstLaunch && !viewModel.hasShownOnboarding()
            LaunchedEffect(isReallyFirstLaunch) {
                if (isReallyFirstLaunch) {
                    // توثيق عربي للمسار التنفيذي.
                    kotlinx.coroutines.delay(400)
                    showOnboardingDialog = true
                }
            }

            // احتساب السمة المرئية المختارة (فاتح / داكن / تتبع النظام) فوراً دون أي تأخير
            val themeMode by viewModel.themeModeState.collectAsStateWithLifecycle()
            val isSystemDark = androidx.compose.foundation.isSystemInDarkTheme()
            val darkTheme = remember(themeMode, isSystemDark) {
                when (themeMode) {
                    1 -> false
                    2 -> true
                    else -> isSystemDark
                }
            }

            // تطبيق السمة وموفر الاتجاه العربي (الاتجاه العربي)
            AppTheme(darkTheme = darkTheme) {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    // 1. نافذة الترحيب والإعداد الأولي عند التثبيت
                    if (isReallyFirstLaunch && showOnboardingDialog) {
                        WelcomeOnboardingDialog(
                            onDismiss = {
                                viewModel.markOnboardingShown() // حفظ الحالة أولاً في التفضيلات المحلية
                                val updated = settings.copy(isFirstLaunch = false)
                                viewModel.saveSettings(updated)
                                showOnboardingDialog = false
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
     * [دالة دورة الحياة - إيقاف النشاط]:
     * تستدعى عندما ينتقل التطبيق إلى الخلفية. تنفذ نسخة احتياطية صامتة وسريعة للحفاظ على البيانات.
     */
    override fun onStop() {
        super.onStop()
        try {
            if (::backupSyncViewModel.isInitialized) {
                backupSyncViewModel.triggerSilentLocalBackup()
            }
        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "Background backup trigger failed: ${e.javaClass.simpleName}")
        }
    }


}
