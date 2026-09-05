package com.smartledger.aldaftar.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.smartledger.aldaftar.data.CloudSyncState
import com.smartledger.aldaftar.data.local.entities.AppSettings
import com.smartledger.aldaftar.ui.screens.settings.components.BackupSheetHeader
import com.smartledger.aldaftar.ui.screens.settings.components.QuadBackupCard
import com.smartledger.aldaftar.ui.viewmodel.BackupSyncViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupRestoreBottomSheet(
    settings: AppSettings,
    backupSyncViewModel: BackupSyncViewModel,
    onExportMzd: () -> Unit = {},
    onImportMzd: () -> Unit = {},
    onDiscoverLegacy: () -> Unit = {},
    onDismiss: () -> Unit,
    onOpenCloudBackupsList: () -> Unit = {},
    onRestoreSuccess: (AppSettings) -> Unit = {}
) {
    val syncState by backupSyncViewModel.googleDriveSyncState.collectAsStateWithLifecycle()
    val storedEmail = remember(syncState) { backupSyncViewModel.googleDriveSyncHelper.getStoredEmail() }
    val isConnected = remember(storedEmail, syncState) {
        !storedEmail.isNullOrEmpty() || syncState is CloudSyncState.Authenticated || syncState is CloudSyncState.Success
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
            dragHandle = {
                androidx.compose.material3.BottomSheetDefaults.DragHandle(
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp)
                    .padding(bottom = 12.dp)
                    .navigationBarsPadding(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // العنوان وحالة الاتصال
                BackupSheetHeader(
                    isConnected = isConnected
                )

                // بطاقة النسخ والاستعادة الشاملة
                QuadBackupCard(
                    backupSyncViewModel = backupSyncViewModel,
                    settings = settings,
                    onRestoreSuccess = { restoredSettings ->
                        onRestoreSuccess(restoredSettings)
                        onDismiss()
                    },
                    onDiscoverLegacy = onDiscoverLegacy
                )
            }
        }
    }
}
