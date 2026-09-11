package com.smartledger.aldaftar.ui.screens

import android.app.Activity
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.smartledger.aldaftar.data.cloud.GoogleDriveInternalAuth
import com.smartledger.aldaftar.data.local.entities.AppSettings
import com.smartledger.aldaftar.domain.model.CloudBackupFile
import com.smartledger.aldaftar.ui.viewmodel.BackupSyncViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Design System Colors matching the target aesthetic
private val PrimaryPurple = Color(0xFF4A148C)
private val TonalLavender = Color(0xFFEDE7F6)
private val TonalLavenderBorder = Color(0xFFDDD6FE)
private val ArchiveBlue = Color(0xFF00A0E9)
private val LightGreenPill = Color(0xFFE8F5E9)
private val DarkGreenPill = Color(0xFF2E7D32)
private val LightRedDanger = Color(0xFFFFF1F2)
private val DangerBorder = Color(0xFFFECDD3)
private val DangerText = Color(0xFFDC2626)
private val CardBorderColor = Color(0xFFE2E8F0)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupRestoreBottomSheet(
    backupSyncViewModel: BackupSyncViewModel,
    onDismiss: () -> Unit,
    onRestoreSuccess: (AppSettings) -> Unit = {}
) {
    val connected by backupSyncViewModel.cloudConnected.collectAsStateWithLifecycle()
    val email by backupSyncViewModel.cloudEmail.collectAsStateWithLifecycle()
    val busy by backupSyncViewModel.isBusy.collectAsStateWithLifecycle()
    val busyMessage by backupSyncViewModel.busyMessage.collectAsStateWithLifecycle()
    val error by backupSyncViewModel.error.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var manualOptions by remember { mutableStateOf(false) }
    var archiveOpen by remember { mutableStateOf(false) }
    var resetOpen by remember { mutableStateOf(false) }
    var clientId by remember { mutableStateOf<String?>(null) }

    // SAF Launchers for Local Backup Export & Import
    val createDocumentLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { uri ->
        if (uri != null) {
            backupSyncViewModel.exportBackupBytes { bytes ->
                if (bytes != null) {
                    runCatching {
                        context.contentResolver.openOutputStream(uri)?.use { os ->
                            os.write(bytes)
                            os.flush()
                        }
                        Toast.makeText(context, "تم تصدير النسخة الاحتياطية بنجاح", Toast.LENGTH_SHORT).show()
                    }.onFailure {
                        Toast.makeText(context, "تعذر حفظ ملف النسخة الاحتياطية", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(context, "تعذر إنشاء النسخة الاحتياطية", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    val openDocumentLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            runCatching {
                val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                if (bytes != null) {
                    backupSyncViewModel.restoreFromBytes(bytes) { success, settings, errorMsg ->
                        if (success) {
                            Toast.makeText(context, "تمت استعادة النسخة الاحتياطية بنجاح", Toast.LENGTH_SHORT).show()
                            if (settings != null) {
                                onRestoreSuccess(settings)
                            }
                            onDismiss()
                        } else {
                            Toast.makeText(context, errorMsg ?: "فشلت استعادة النسخة الاحتياطية", Toast.LENGTH_LONG).show()
                        }
                    }
                } else {
                    Toast.makeText(context, "تعذر قراءة ملف النسخة الاحتياطية", Toast.LENGTH_SHORT).show()
                }
            }.onFailure {
                Toast.makeText(context, "تعذر فتح الملف المحدد", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val googleClient = remember {
        GoogleDriveInternalAuth(context).client()
    }
    val signInLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode != Activity.RESULT_OK) return@rememberLauncherForActivityResult
        runCatching {
            GoogleSignIn.getSignedInAccountFromIntent(result.data)
                .getResult(com.google.android.gms.common.api.ApiException::class.java)
        }.onSuccess { account ->
            backupSyncViewModel.signInWithGoogle(account, account.serverAuthCode) { success ->
                if (success) {
                    Toast.makeText(context, "تم ربط حساب Google Drive بنجاح", Toast.LENGTH_SHORT).show()
                }
            }
        }.onFailure { ex ->
            Toast.makeText(context, "تعذر تسجيل الدخول بحساب Google: ${ex.localizedMessage ?: ex.message}", Toast.LENGTH_LONG).show()
        }
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            dragHandle = {
                Box(
                    modifier = Modifier
                        .padding(top = 10.dp, bottom = 6.dp)
                        .width(42.dp)
                        .height(4.5.dp)
                        .clip(RoundedCornerShape(50))
                        .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
                )
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header with Title and Connection Pill Badge
                BackupMainHeader(connected = connected)

                // 1. Google Drive Cloud Sync Card
                CloudSyncCard(
                    connected = connected,
                    email = email,
                    busy = busy,
                    manualOptions = manualOptions,
                    onInternalConnect = { signInLauncher.launch(googleClient.signInIntent) },
                    onManualToggle = { manualOptions = !manualOptions },
                    onManualConnect = { backupSyncViewModel.connectCloud() },
                    onCreateCloudBackup = {
                        backupSyncViewModel.createCloudBackup { remote, _ ->
                            if (remote != null) {
                                Toast.makeText(context, "تم إنشاء النسخة السحابية وتأمينها بنجاح في Google Drive", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "تم حفظ النسخة محليًا (يرجى التأكد من اتصال السحابة)", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    onRestoreCloud = { archiveOpen = true },
                    onOpenArchive = { archiveOpen = true },
                    onDisconnect = { backupSyncViewModel.disconnectCloud() }
                )

                // 2. Local Backup Card (.mzd)
                LocalBackupCard(
                    busy = busy,
                    onExportLocal = {
                        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH).format(Date())
                        createDocumentLauncher.launch("smartledger_backup_$timestamp.mzd")
                    },
                    onImportLocal = {
                        openDocumentLauncher.launch(arrayOf("*/*"))
                    }
                )

                // 3. Clear All Data & Reset Button (Danger)
                ResetAllDataButton(
                    busy = busy,
                    onClick = { resetOpen = true }
                )

                // Operation Status / Busy Indicator
                if (busy) {
                    OperationStatusBanner(text = busyMessage ?: "جارٍ تنفيذ العملية...")
                }

                // Error Message Display
                error?.takeIf(String::isNotBlank)?.let {
                    Text(
                        text = it,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // Cloud Archive Bottom Sheet
        if (archiveOpen) {
            CloudArchiveBottomSheet(
                vm = backupSyncViewModel,
                onDismiss = { archiveOpen = false },
                onRestoreSuccess = {
                    onRestoreSuccess(it)
                    archiveOpen = false
                },
                onConnect = {
                    archiveOpen = false
                    signInLauncher.launch(googleClient.signInIntent)
                }
            )
        }

        // Reset Confirmation Dialog
        if (resetOpen) {
            AlertDialog(
                onDismissRequest = { resetOpen = false },
                icon = { Icon(Icons.Default.DeleteForever, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(28.dp)) },
                title = { Text("تأكيد مسح كافة البيانات", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
                text = { Text("سيتم حذف جميع سجلات الدفتر، المعاملات، والحسابات نهائيًا. لن تتمكن من التراجع عن هذا الإجراء إلا باستعادة نسخة احتياطية.", fontSize = 13.sp) },
                confirmButton = {
                    Button(
                        onClick = {
                            backupSyncViewModel.clearLocalCopyAndWipeMemory {
                                resetOpen = false
                                Toast.makeText(context, "تمت إعادة ضبط البيانات بنجاح", Toast.LENGTH_SHORT).show()
                                onDismiss()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("مسح وإعادة الضبط", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = { resetOpen = false },
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("إلغاء")
                    }
                }
            )
        }
    }
}

@Composable
private fun BackupMainHeader(connected: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left side pill badge
        Surface(
            shape = RoundedCornerShape(50),
            color = if (connected) LightGreenPill else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(if (connected) DarkGreenPill else MaterialTheme.colorScheme.onSurfaceVariant)
                )
                Text(
                    text = if (connected) "متصل بالسحابة" else "غير متصل بالسحابة",
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (connected) DarkGreenPill else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Right side Title + Cloud icon
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "النسخ الاحتياطي والمزامنة",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Icon(
                imageVector = Icons.Default.CloudQueue,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = PrimaryPurple
            )
        }
    }
}

@Composable
private fun CloudSyncCard(
    connected: Boolean,
    email: String?,
    busy: Boolean,
    manualOptions: Boolean,
    onInternalConnect: () -> Unit,
    onManualToggle: () -> Unit,
    onManualConnect: () -> Unit,
    onCreateCloudBackup: () -> Unit,
    onRestoreCloud: () -> Unit,
    onOpenArchive: () -> Unit,
    onDisconnect: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, CardBorderColor),
        shadowElevation = 0.5.dp
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Card Title Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "المزامنة السحابية (Google Drive)",
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Icon(
                    imageVector = Icons.Default.CloudSync,
                    contentDescription = null,
                    modifier = Modifier.size(22.dp),
                    tint = PrimaryPurple
                )
            }

            if (!connected) {
                // Not connected UI
                Button(
                    onClick = onInternalConnect,
                    enabled = !busy,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple)
                ) {
                    Icon(Icons.Default.Link, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("ربط حساب Google Drive", fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                }

                TextButton(
                    onClick = onManualToggle,
                    modifier = Modifier.fillMaxWidth().height(32.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        text = if (manualOptions) "إخفاء خيارات الربط اليدوي" else "خيارات الربط اليدوي عبر المتصفح",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryPurple
                    )
                }

                if (manualOptions) {
                    ManualConnectionCard(busy = busy, onConnect = onManualConnect)
                }
            } else {
                // Connected Account Box
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    color = TonalLavender.copy(alpha = 0.6f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "الحساب المتصل: ${email ?: "حساب Google"}",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = PrimaryPurple
                        )
                    }
                }

                // Subtitle Info
                Text(
                    text = "يتم حفظ ومزامنة السجلات بأمان مع هذا الحساب.",
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF7F1D1D),
                    textAlign = TextAlign.Start,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 2.dp)
                )

                // Row of 2 Buttons: Cloud Backup & Cloud Restore
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Right button: إنشاء نسخة سحابية (Solid Purple)
                    Button(
                        onClick = onCreateCloudBackup,
                        enabled = !busy,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple)
                    ) {
                        Text("إنشاء نسخة سحابية", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                    }

                    // Left button: استعادة نسخة احتياطية (Tonal Lavender)
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable(enabled = !busy, onClick = onRestoreCloud),
                        shape = RoundedCornerShape(12.dp),
                        color = TonalLavender,
                        border = BorderStroke(1.dp, TonalLavenderBorder.copy(alpha = 0.6f))
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "استعادة نسخة احتياطية",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryPurple
                            )
                        }
                    }
                }

                // Full Width Button: سجل النسخ السحابية (Blue)
                Button(
                    onClick = onOpenArchive,
                    enabled = !busy,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ArchiveBlue)
                ) {
                    Icon(
                        imageVector = Icons.Default.BackupTable,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = Color.White
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "سجل النسخ السحابية",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                // Full Width Button: قطع الاتصال بالحساب (Light Red / Outlined)
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable(enabled = !busy, onClick = onDisconnect),
                    shape = RoundedCornerShape(12.dp),
                    color = LightRedDanger,
                    border = BorderStroke(1.dp, DangerBorder)
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = DangerText
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "قطع الاتصال بالحساب",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = DangerText
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LocalBackupCard(
    busy: Boolean,
    onExportLocal: () -> Unit,
    onImportLocal: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, CardBorderColor),
        shadowElevation = 0.5.dp
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Card Title Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "النسخ الاحتياطي المحلي (.mzd)",
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Icon(
                    imageVector = Icons.Default.Save,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = PrimaryPurple
                )
            }

            // Subtitle
            Text(
                text = "تصدير أو استيراد ملف النسخة الاحتياطية",
                fontSize = 11.sp,
                fontWeight = FontWeight.Normal,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(2.dp))

            // Row of 2 Buttons: Export & Import
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Right button: تصدير نسخة احتياطية (Solid Purple)
                Button(
                    onClick = onExportLocal,
                    enabled = !busy,
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple)
                ) {
                    Text("تصدير نسخة احتياطية", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                }

                // Left button: استيراد نسخة احتياطية (Tonal Lavender)
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable(enabled = !busy, onClick = onImportLocal),
                    shape = RoundedCornerShape(12.dp),
                    color = TonalLavender,
                    border = BorderStroke(1.dp, TonalLavenderBorder.copy(alpha = 0.6f))
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "استيراد نسخة احتياطية",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryPurple
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ResetAllDataButton(
    busy: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(enabled = !busy, onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = LightRedDanger,
        border = BorderStroke(1.dp, DangerBorder)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.DeleteForever,
                contentDescription = null,
                modifier = Modifier.size(19.dp),
                tint = DangerText
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = "مسح كافة البيانات وإعادة الضبط",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = DangerText
            )
        }
    }
}

@Composable
private fun OperationStatusBanner(text: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                strokeWidth = 2.dp,
                color = PrimaryPurple
            )
            Text(
                text = text,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ManualConnectionCard(busy: Boolean, onConnect: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(10.dp)),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("خطوات الربط اليدوي", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Text("١. سجّل الدخول عبر المتصفح وامنح الصلاحية.", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("٢. بعد اكتمال الربط، عد إلى التطبيق وسيتعرف على الحساب تلقائيًا.", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            OutlinedButton(
                onClick = onConnect,
                enabled = !busy,
                modifier = Modifier.fillMaxWidth().height(36.dp),
                shape = RoundedCornerShape(9.dp)
            ) {
                Text("فتح الربط اليدوي عبر المتصفح", fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CloudArchiveBottomSheet(
    vm: BackupSyncViewModel,
    onDismiss: () -> Unit,
    onRestoreSuccess: (AppSettings) -> Unit,
    onConnect: () -> Unit
) {
    val connected by vm.cloudConnected.collectAsStateWithLifecycle()
    val email by vm.cloudEmail.collectAsStateWithLifecycle()
    val items by vm.cloudBackups.collectAsStateWithLifecycle()
    val busy by vm.isBusy.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var search by remember { mutableStateOf("") }
    var searchActive by remember { mutableStateOf(false) }
    var selectionMode by remember { mutableStateOf(false) }
    var selected by remember { mutableStateOf<Set<String>>(emptySet()) }
    var restoreItem by remember { mutableStateOf<CloudBackupFile?>(null) }
    var deleteItem by remember { mutableStateOf<CloudBackupFile?>(null) }
    var deleteMany by remember { mutableStateOf(false) }

    LaunchedEffect(connected) { if (connected) vm.refreshCloud() }
    LaunchedEffect(search) { vm.setCloudSearch(search) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp)
                .padding(bottom = 30.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (!searchActive) {
                ArchiveHeader(
                    selection = selectionMode,
                    count = items.size,
                    onSearch = { searchActive = true },
                    onSelection = {
                        selectionMode = !selectionMode
                        selected = emptySet()
                    },
                    onDismiss = onDismiss
                )
            } else {
                SearchHeader(
                    search = search,
                    onSearchChange = { search = it },
                    onClose = {
                        searchActive = false
                        search = ""
                    }
                )
            }

            HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            if (!connected) {
                EmptyCloudState(onConnect)
            } else if (busy && items.isEmpty()) {
                LoadingCloudState()
            } else {
                ArchiveStats(
                    email = email,
                    count = items.size,
                    selection = selectionMode,
                    selected = selected.size,
                    onRefresh = {
                        if (selectionMode) {
                            selected = if (selected.size == items.size) emptySet() else items.map { it.id }.toSet()
                        } else {
                            vm.refreshCloud()
                        }
                    }
                )

                if (items.isEmpty()) {
                    EmptyListState()
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 380.dp)
                    ) {
                        items(items, key = { it.id }) { item ->
                            CloudBackupRow(
                                item = item,
                                selection = selectionMode,
                                checked = selected.contains(item.id),
                                onClick = {
                                    if (selectionMode) {
                                        selected = if (selected.contains(item.id)) selected - item.id else selected + item.id
                                    } else {
                                        restoreItem = item
                                    }
                                },
                                onDelete = { deleteItem = item }
                            )
                        }
                    }
                }
            }

            // Bottom Actions inside Archive
            if (selectionMode && selected.isNotEmpty()) {
                Button(
                    onClick = { deleteMany = true },
                    modifier = Modifier.fillMaxWidth().height(46.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Delete, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("حذف ${selected.size} نسخ محددة", fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                }
            } else {
                Button(
                    onClick = {
                        vm.createCloudBackup { remote, _ ->
                            if (remote != null) {
                                Toast.makeText(context, "تم رفع النسخة السحابية بنجاح إلى Google Drive", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(46.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.CloudUpload, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("إنشاء نسخة سحابية جديدة الآن", fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    restoreItem?.let { item ->
        AlertDialog(
            onDismissRequest = { restoreItem = null },
            title = { Text("تأكيد استعادة النسخة السحابية", fontWeight = FontWeight.Bold, fontSize = 15.sp) },
            text = { Text("سيتم استبدال السجلات الحالية بالبيانات المحفوظة في النسخة السحابية (${item.name}). هل تريد المتابعة؟", fontSize = 13.sp) },
            confirmButton = {
                Button(
                    onClick = {
                        vm.restoreCloud(item) { ok, settings, _ ->
                            restoreItem = null
                            if (ok && settings != null) onRestoreSuccess(settings)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("استعادة النسخة", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { restoreItem = null },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("إلغاء")
                }
            }
        )
    }

    deleteItem?.let { item ->
        AlertDialog(
            onDismissRequest = { deleteItem = null },
            title = { Text("حذف النسخة السحابية", fontWeight = FontWeight.Bold, fontSize = 15.sp) },
            text = { Text("سيتم حذف النسخة (${item.name}) نهائيًا من حساب Google Drive.", fontSize = 13.sp) },
            confirmButton = {
                Button(
                    onClick = { vm.deleteCloudBackups(setOf(item.id)) { deleteItem = null } },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("حذف نهائي", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { deleteItem = null },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("إلغاء")
                }
            }
        )
    }

    if (deleteMany) {
        AlertDialog(
            onDismissRequest = { deleteMany = false },
            title = { Text("حذف النسخ المحددة", fontWeight = FontWeight.Bold, fontSize = 15.sp) },
            text = { Text("هل أنت متأكد من حذف ${selected.size} نسخة محددة نهائيًا من السحابة؟", fontSize = 13.sp) },
            confirmButton = {
                Button(
                    onClick = {
                        vm.deleteCloudBackups(selected) {
                            deleteMany = false
                            selected = emptySet()
                            selectionMode = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("حذف المحدد", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { deleteMany = false },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("إلغاء")
                }
            }
        )
    }
}

@Composable
private fun ArchiveHeader(
    selection: Boolean,
    count: Int,
    onSearch: () -> Unit,
    onSelection: () -> Unit,
    onDismiss: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.CloudSync, null, Modifier.size(22.dp), tint = PrimaryPurple)
            Text("أرشيف النسخ السحابية", fontSize = 14.5.sp, fontWeight = FontWeight.Bold)
        }
        Row {
            TextButton(onClick = onSearch, contentPadding = PaddingValues(6.dp)) {
                Text("بحث", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = PrimaryPurple)
            }
            TextButton(onClick = onSelection, contentPadding = PaddingValues(6.dp)) {
                Text(if (selection) "إلغاء التحديد" else "تحديد", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = PrimaryPurple)
            }
            TextButton(onClick = onDismiss, contentPadding = PaddingValues(6.dp)) {
                Text("إغلاق", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun SearchHeader(search: String, onSearchChange: (String) -> Unit, onClose: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        IconButton(onClick = onClose, Modifier.size(32.dp)) {
            Icon(Icons.Default.ArrowForward, "إغلاق البحث", Modifier.size(20.dp))
        }
        BasicTextField(
            value = search,
            onValueChange = onSearchChange,
            modifier = Modifier.weight(1f),
            textStyle = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface),
            singleLine = true,
            decorationBox = { inner ->
                if (search.isBlank()) Text("بحث في النسخ السحابية...", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                inner()
            }
        )
        if (search.isNotBlank()) {
            IconButton(onClick = { onSearchChange("") }, Modifier.size(28.dp)) {
                Icon(Icons.Default.Clear, "مسح البحث", Modifier.size(18.dp))
            }
        }
    }
}

@Composable
private fun ArchiveStats(
    email: String?,
    count: Int,
    selection: Boolean,
    selected: Int,
    onRefresh: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(horizontalAlignment = Alignment.Start) {
            Text("عدد النسخ المحفوظة: $count", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Text(email ?: "حساب Google متصل", fontSize = 9.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        TextButton(onClick = onRefresh, contentPadding = PaddingValues(6.dp)) {
            Text(
                text = if (selection) if (selected == count && count > 0) "إلغاء الكل" else "تحديد الكل" else "تحديث السجلات",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = ArchiveBlue
            )
        }
    }
}

@Composable
private fun CloudBackupRow(
    item: CloudBackupFile,
    selection: Boolean,
    checked: Boolean,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                if (checked) 1.5.dp else 0.8.dp,
                if (checked) ArchiveBlue else CardBorderColor,
                RoundedCornerShape(12.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (checked) TonalLavender.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(horizontalAlignment = Alignment.Start) {
                Text(
                    text = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(item.modifiedTime)),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = SimpleDateFormat("hh:mm a", Locale.US).format(Date(item.modifiedTime)),
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${"%.1f".format(Locale.US, item.size / 1024.0)} ك.ب",
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (selection) {
                    IconButton(onClick = onClick, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = if (checked) ArchiveBlue else MaterialTheme.colorScheme.outlineVariant
                        )
                    }
                } else {
                    IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "حذف",
                            modifier = Modifier.size(18.dp),
                            tint = DangerText.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyCloudState(onConnect: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp, horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Icon(Icons.Default.CloudOff, null, Modifier.size(56.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
        Text("السحابة غير متصلة", fontSize = 14.5.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text("قم بربط حساب Google Drive لتصفح واسترجاع نسخك الاحتياطية السحابية بأمان.", fontSize = 11.5.sp, lineHeight = 18.sp, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Button(
            onClick = onConnect,
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple),
            shape = RoundedCornerShape(10.dp)
        ) {
            Icon(Icons.Default.Link, null, Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text("ربط حساب Google Drive", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun LoadingCloudState() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 36.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        CircularProgressIndicator(Modifier.size(32.dp), color = PrimaryPurple)
        Text("جارٍ تحميل النسخ السحابية...", fontSize = 11.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun EmptyListState() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 36.dp, horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(Icons.Default.BackupTable, null, Modifier.size(50.dp), tint = MaterialTheme.colorScheme.outlineVariant)
        Text("لا توجد نسخ سحابية محفوظة", fontSize = 13.5.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text("أنشئ نسختك الأولى لتأمين بياناتك وسجلاتك المالية في السحابة.", fontSize = 11.5.sp, lineHeight = 17.sp, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
