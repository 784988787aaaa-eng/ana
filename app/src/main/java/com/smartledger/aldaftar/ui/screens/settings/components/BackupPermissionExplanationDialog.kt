package com.smartledger.aldaftar.ui.screens.settings.components

import androidx.compose.material3.MaterialTheme

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.SettingsSuggest
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartledger.aldaftar.R
import com.smartledger.aldaftar.ui.theme.MizanDialogTokens
import com.smartledger.aldaftar.ui.theme.BrandPrimary
import com.smartledger.aldaftar.ui.theme.StatusInfoBlue
import com.smartledger.aldaftar.ui.theme.StatusWarningAmber

@Composable
fun BackupPermissionExplanationDialog(
    onDismiss: () -> Unit,
    onGrantPermissions: () -> Unit,
    onUseInternalStorage: () -> Unit
) {
    com.smartledger.aldaftar.ui.components.MizanAnimatedDialog(
        onDismissRequest = onDismiss
    ) { dismissDialog ->
        androidx.compose.runtime.CompositionLocalProvider(
            androidx.compose.ui.platform.LocalLayoutDirection provides androidx.compose.ui.unit.LayoutDirection.Rtl
        ) {
            com.smartledger.aldaftar.ui.components.MizanDialogCard(
                maxWidth = MizanDialogTokens.compactMaxWidth,
                contentPadding = PaddingValues(
                    horizontal = MizanDialogTokens.dialogHorizontal,
                    vertical = MizanDialogTokens.dialogVertical
                )
            ) {
                com.smartledger.aldaftar.ui.components.MizanDialogHeader(
                    title = stringResource(R.string.settings_permissions_dialog_title),
                    icon = Icons.Default.Folder,
                    iconTint = BrandPrimary,
                    onCloseClick = dismissDialog
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(R.string.settings_permissions_dialog_desc),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp,
                        modifier = Modifier.fillMaxWidth()
                    )

                    com.smartledger.aldaftar.ui.components.MizanDialogInnerCard {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Folder, contentDescription = null, tint = BrandPrimary, modifier = Modifier.size(16.dp))
                                Text(stringResource(R.string.settings_permissions_storage_label), fontSize = 11.5.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
                            }

                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.SettingsSuggest, contentDescription = null, tint = StatusInfoBlue, modifier = Modifier.size(16.dp))
                                    Text(stringResource(R.string.settings_permissions_manage_files_label), fontSize = 11.5.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
                                }
                            }

                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = StatusWarningAmber, modifier = Modifier.size(16.dp))
                                    Text(stringResource(R.string.settings_permissions_notifications_label), fontSize = 11.5.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
                                }
                            }
                        }
                    }

                    Text(
                        text = stringResource(R.string.settings_permissions_fallback_note),
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 16.sp,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    com.smartledger.aldaftar.ui.components.MizanDialogActions(
                        confirmText = stringResource(R.string.settings_permissions_grant_btn),
                        onConfirm = onGrantPermissions,
                        cancelText = stringResource(R.string.settings_permissions_internal_storage_btn),
                        onCancel = onUseInternalStorage
                    )
                }
            }
        }
    }
}
