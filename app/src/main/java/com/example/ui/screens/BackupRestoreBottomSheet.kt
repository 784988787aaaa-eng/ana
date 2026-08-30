package com.example.ui.screens

/*
 * =====================================================================================
 * حزمة نوافذ النسخ الاحتياطي والاستعادة السفلية (Backup & Restore Bottom Sheet Package)
 * -------------------------------------------------------------------------------------
 * تحتوي هذه الفئة على الورقة السفلية المنبثقة (ModalBottomSheet) لإدارة منظومة
 * النسخ الاحتياطي الرباعي (Quad-Backup)، التصدير، الاستيراد، والمزامنة السحابية.
 * =====================================================================================
 */

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.CloudSyncState
import com.example.data.local.entities.AppSettings
import com.example.ui.screens.settings.components.BackupSheetHeader
import com.example.ui.screens.settings.components.QuadBackupCard
import com.example.ui.viewmodel.BackupSyncViewModel

/*
 * =====================================================================================
 * الورقة السفلية للنسخ الاحتياطي والاستعادة (BackupRestoreBottomSheet)
 * -------------------------------------------------------------------------------------
 * [الوصف والهدف]:
 * نافذة سفلية منبثقة (ModalBottomSheet) تتيح للمستخدم إدارة أمان بياناته المالية:
 * 1. عرض حالة اتصال حساب التخزين السحابي (Google Drive Sync Status).
 * 2. إدارة المزامنة السحابية، الاستعادة، والتصدير بأسلوب هادئ وواضح.
 * 3. إنشاء واستعادة النسخ الاحتياطية محلياً وسحابياً وتطبيق الإعدادات المسترجعة فوراً.
 * 4. تطبيق الاتجاه العربي من اليمين لليسار (RTL Layout) والتصميم المتجاوب لجميع الشاشات.
 *
 * [المُدخلات]:
 * - settings: إعدادات التطبيق الحالية بما فيها خيارات النسخ المجدول.
 * - backupSyncViewModel: نموذج بيانات المزامنة السحابية وإدارة الملفات.
 * - onExportMzd: رد نداء بدء تصدير ملف النسخة الاحتياطية محلياً.
 * - onImportMzd: رد نداء بدء استيراد ملف النسخة الاحتياطية.
 * - onDismiss: رد نداء إغلاق الورقة السفلية.
 * - onOpenCloudBackupsList: رد نداء فتح قائمة النسخ السحابية المخزنة.
 * - onRestoreSuccess: رد نداء نجاح الاستعادة وتمرير كائن الإعدادات المستعاد.
 * =====================================================================================
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupRestoreBottomSheet(
    settings: AppSettings,
    backupSyncViewModel: BackupSyncViewModel,
    onExportMzd: () -> Unit = {},
    onImportMzd: () -> Unit = {},
    onDismiss: () -> Unit,
    onOpenCloudBackupsList: () -> Unit = {},
    onRestoreSuccess: (AppSettings) -> Unit = {}
) {
    // تحديد السمة الحالية (داكنة أو فاتحة) لتعديل تباين الترويسة
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f

    /*
     * ---------------------------------------------------------------------------------
     * مراقبة حالة الاتصال والتخزين السحابي على Google Drive
     * ---------------------------------------------------------------------------------
     */
    val syncState by backupSyncViewModel.googleDriveSyncState.collectAsStateWithLifecycle()
    val storedEmail = remember(syncState) { backupSyncViewModel.googleDriveSyncHelper.getStoredEmail() }
    val isConnected = remember(storedEmail, syncState) {
        !storedEmail.isNullOrEmpty() || syncState is CloudSyncState.Authenticated || syncState is CloudSyncState.Success
    }

    /*
     * ---------------------------------------------------------------------------------
     * رسم الورقة السفلية المنبثقة بالاتجاه العربي (RTL)
     * ---------------------------------------------------------------------------------
     */
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            dragHandle = {
                BottomSheetDefaults.DragHandle(
                    modifier = Modifier.padding(top = 10.dp, bottom = 4.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.8f)
                )
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 20.dp)
                    .navigationBarsPadding(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 640.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // 1. ترويسة الورقة السفلية مع شارة حالة الاتصال السحابي
                    BackupSheetHeader(
                        isConnected = isConnected,
                        isDark = isDark
                    )

                    // 2. بطاقة إدارة منظومة النسخ الاحتياطي الرباعي الشامل
                    QuadBackupCard(
                        backupSyncViewModel = backupSyncViewModel,
                        settings = settings,
                        onRestoreSuccess = { restoredSettings ->
                            onRestoreSuccess(restoredSettings)
                            onDismiss()
                        }
                    )
                }
            }
        }
    }
}

