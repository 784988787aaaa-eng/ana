package com.smartledger.aldaftar.ui.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import android.provider.OpenableColumns
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.smartledger.aldaftar.R
import com.smartledger.aldaftar.domain.model.CloudBackupFile
import com.smartledger.aldaftar.ui.viewmodel.BackupSyncViewModel
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupRestoreBottomSheet(
    backupSyncViewModel: BackupSyncViewModel,
    onDismiss: () -> Unit,
    onRestoreSuccess: (AppSettings) -> Unit = {}
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val localBackups by backupSyncViewModel.localBackups.collectAsStateWithLifecycle()
    val cloudBackups by backupSyncViewModel.cloudBackups.collectAsStateWithLifecycle()
    val cloudConnected by backupSyncViewModel.cloudConnected.collectAsStateWithLifecycle()
    val cloudSearch by backupSyncViewModel.cloudSearch.collectAsStateWithLifecycle()
    val busy by backupSyncViewModel.isBusy.collectAsStateWithLifecycle()
    val automaticBackupEnabled by backupSyncViewModel.automaticBackupEnabled.collectAsStateWithLifecycle()
    val error by backupSyncViewModel.error.collectAsStateWithLifecycle()

    var selectedCloud by remember { mutableStateOf<Set<String>>(emptySet()) }
    var restoreLocal by remember { mutableStateOf<File?>(null) }
    var restoreCloud by remember { mutableStateOf<CloudBackupFile?>(null) }
    var deleteLocal by remember { mutableStateOf<File?>(null) }
    var deleteCloud by remember { mutableStateOf<Set<String>>(emptySet()) }
    var recoveryCode by remember { mutableStateOf<String?>(null) }
    var showRecoveryDialog by remember { mutableStateOf(false) }
    var showRecoveryInfo by remember { mutableStateOf(false) }
    var pendingLocalAction by remember { mutableStateOf<(() -> Unit)?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) pendingLocalAction?.invoke() else Toast.makeText(context, "يلزم السماح بالوصول إلى مجلد المستندات", Toast.LENGTH_LONG).show()
        pendingLocalAction = null
    }
    val allFilesAccessLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (Environment.isExternalStorageManager()) pendingLocalAction?.invoke()
        else Toast.makeText(context, "لم يتم منح صلاحية مجلد المستندات", Toast.LENGTH_LONG).show()
        pendingLocalAction = null
    }

    fun ensureLocalAccess(action: () -> Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (Environment.isExternalStorageManager()) action()
            else {
                pendingLocalAction = action
                runCatching {
                    allFilesAccessLauncher.launch(
                        Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                            data = android.net.Uri.parse("package:${context.packageName}")
                        }
                    )
                }.onFailure {
                    pendingLocalAction = null
                    Toast.makeText(context, "تعذر فتح إعدادات صلاحية المستندات", Toast.LENGTH_LONG).show()
                }
            }
        } else if (androidx.core.content.ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            action()
        } else {
            permissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
    }

    val filePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        val bytes = runCatching { context.contentResolver.openInputStream(uri)?.use { it.readBytes() } }.getOrNull()
        val name = runCatching {
            context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) cursor.getString(0) else null
            }
        }.getOrNull()?.takeIf { it.endsWith(".slb", true) }
        if (bytes == null) {
            Toast.makeText(context, "تعذر قراءة ملف النسخة الاحتياطية", Toast.LENGTH_LONG).show()
        } else {
            ensureLocalAccess {
                backupSyncViewModel.saveCloudCopyToLocal(bytes, name) { file ->
                    Toast.makeText(context, if (file != null) "تم استيراد النسخة إلى الأرشيف المحلي" else "تعذر حفظ النسخة", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        backupSyncViewModel.refreshLocalBackups()
        backupSyncViewModel.refreshCloud()
    }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Box(Modifier.fillMaxWidth()) {
            LazyColumn(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 28.dp)
            ) {
                item {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("النسخ الاحتياطي والاسترداد", fontWeight = FontWeight.Bold)
                            Text("نسخ محلية وسحابية آمنة بامتداد SLB", style = MaterialTheme.typography.bodySmall)
                        }
                        IconButton(onClick = { backupSyncViewModel.refreshLocalBackups(); backupSyncViewModel.refreshCloud() }, enabled = !busy) {
                            Icon(Icons.Default.Refresh, contentDescription = "تحديث")
                        }
                    }
                }

                item {
                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Cloud, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(if (cloudConnected) "Google Drive متصل" else "Google Drive غير متصل", fontWeight = FontWeight.Bold)
                                    Text(
                                        if (cloudConnected) "النسخ اليدوي والتلقائي يمكنهما الرفع إلى السحابة"
                                        else "اربط الحساب مرة واحدة لتمكين الرفع السحابي",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                                if (cloudConnected) {
                                    TextButton(onClick = { backupSyncViewModel.disconnectCloud() }, enabled = !busy) { Text("فصل") }
                                } else {
                                    Button(onClick = {
                                        backupSyncViewModel.connectCloud { ok ->
                                            Toast.makeText(context, if (ok) "تم ربط Google Drive بنجاح" else "تعذر إكمال ربط Google Drive", Toast.LENGTH_LONG).show()
                                        }
                                    }, enabled = !busy) { Text("ربط Google Drive") }
                                }
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f)) {
                                    Text("النسخ التلقائي اليومي", fontWeight = FontWeight.Medium)
                                    Text(
                                        if (automaticBackupEnabled) "يُحفظ محليًا، ويُرفع إلى Drive تلقائيًا عند الاتصال"
                                        else "متوقف حاليًا",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                                Switch(
                                    checked = automaticBackupEnabled,
                                    onCheckedChange = { enabled ->
                                        if (enabled) ensureLocalAccess { backupSyncViewModel.setAutomaticBackupEnabled(true) }
                                        else backupSyncViewModel.setAutomaticBackupEnabled(false)
                                    },
                                    enabled = !busy
                                )
                            }
                        }
                    }
                }

                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        Button(
                            onClick = {
                                ensureLocalAccess {
                                    backupSyncViewModel.createLocalBackup { file ->
                                        Toast.makeText(context, if (file != null) "تم إنشاء النسخة وحفظها محليًا" else "تعذر إنشاء النسخة", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            enabled = !busy,
                            modifier = Modifier.weight(1f)
                        ) { Text("إنشاء نسخة") }
                        if (cloudConnected) {
                            Button(
                                onClick = {
                                    ensureLocalAccess {
                                        backupSyncViewModel.uploadLatestToCloud { file ->
                                            Toast.makeText(context, if (file != null) "تم إنشاء النسخة ورفعها إلى Google Drive" else "تعذر رفع النسخة", Toast.LENGTH_LONG).show()
                                        }
                                    }
                                },
                                enabled = !busy,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.CloudUpload, contentDescription = null)
                                Spacer(Modifier.width(4.dp))
                                Text("نسخ إلى Drive")
                            }
                        }
                    }
                }

                item {
                    OutlinedButton(
                        onClick = { filePicker.launch(arrayOf("application/octet-stream", "application/vnd.smartledger.backup", "*/*")) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !busy
                    ) {
                        Icon(Icons.Default.CloudDownload, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text("استيراد ملف نسخة احتياطية")
                    }
                }

                item {
                    OutlinedButton(onClick = { showRecoveryInfo = true }, modifier = Modifier.fillMaxWidth(), enabled = !busy) {
                        Text("عرض مفتاح استعادة النسخ")
                    }
                }

                item { Text("الأرشيف المحلي (${localBackups.size})", fontWeight = FontWeight.Bold) }
                if (localBackups.isEmpty()) {
                    item { Text("لا توجد نسخ محلية محفوظة حاليًا", style = MaterialTheme.typography.bodySmall) }
                } else {
                    items(localBackups, key = { it.absolutePath }) { file ->
                        BackupFileRow(
                            file.name,
                            "${file.length()} بايت",
                            onRestore = { restoreLocal = file },
                            onDelete = { deleteLocal = file }
                        )
                    }
                }

                item { Spacer(Modifier.height(4.dp)); Text("الأرشيف السحابي (${cloudBackups.size})", fontWeight = FontWeight.Bold) }
                if (!cloudConnected) {
                    item { Text("اربط Google Drive لعرض الأرشيف السحابي", style = MaterialTheme.typography.bodySmall) }
                } else {
                    item {
                        OutlinedTextField(
                            value = cloudSearch,
                            onValueChange = backupSyncViewModel::setCloudSearch,
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("بحث بالاسم أو الشهر") },
                            enabled = !busy
                        )
                    }
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            TextButton(onClick = { selectedCloud = cloudBackups.map { it.id }.toSet() }, enabled = cloudBackups.isNotEmpty() && !busy) { Text("تحديد الكل") }
                            TextButton(onClick = { selectedCloud = emptySet() }, enabled = selectedCloud.isNotEmpty() && !busy) { Text("إلغاء التحديد") }
                        }
                    }
                    if (cloudBackups.isEmpty()) {
                        item { Text("لا توجد نسخ سحابية مطابقة", style = MaterialTheme.typography.bodySmall) }
                    } else {
                        items(cloudBackups, key = { it.id }) { item ->
                            CloudFileRow(
                                item,
                                selected = item.id in selectedCloud,
                                onSelect = { selectedCloud = if (item.id in selectedCloud) selectedCloud - item.id else selectedCloud + item.id },
                                onDownload = {
                                    ensureLocalAccess {
                                        backupSyncViewModel.downloadCloudToLocal(item) { file ->
                                            Toast.makeText(context, if (file != null) "تم تنزيل النسخة إلى الأرشيف المحلي" else "تعذر تنزيل النسخة", Toast.LENGTH_LONG).show()
                                        }
                                    }
                                },
                                onRestore = { restoreCloud = item }
                            )
                        }
                    }
                    if (selectedCloud.isNotEmpty()) {
                        item {
                            Button(
                                onClick = { deleteCloud = selectedCloud },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = !busy
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = null)
                                Spacer(Modifier.width(6.dp))
                                Text("حذف المحدد (${selectedCloud.size})")
                            }
                        }
                    }
                }
            }

            if (busy) {
                Surface(Modifier.fillMaxWidth().align(Alignment.TopCenter), tonalElevation = 4.dp) {
                    Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        LinearProgressIndicator(Modifier.fillMaxWidth())
                        Text("جاري تنفيذ العملية، يرجى الانتظار...", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }

    if (error != null && !busy) {
        AlertDialog(
            onDismissRequest = backupSyncViewModel::clearError,
            title = { Text("تعذر تنفيذ العملية") },
            text = { Text(error ?: "حدث خطأ غير متوقع") },
            confirmButton = { TextButton(onClick = backupSyncViewModel::clearError) { Text("حسنًا") } }
        )
    }

    if (deleteLocal != null) {
        AlertDialog(
            onDismissRequest = { deleteLocal = null },
            title = { Text("تأكيد حذف النسخة") },
            text = { Text("سيتم حذف ${deleteLocal?.name} من الأرشيف المحلي نهائيًا.") },
            confirmButton = {
                Button(onClick = {
                    val file = deleteLocal ?: return@Button
                    deleteLocal = null
                    backupSyncViewModel.deleteLocalBackup(file) { ok ->
                        if (!ok) Toast.makeText(context, "تعذر حذف النسخة", Toast.LENGTH_LONG).show()
                    }
                }) { Text("حذف") }
            },
            dismissButton = { TextButton(onClick = { deleteLocal = null }) { Text("إلغاء") } }
        )
    }

    if (deleteCloud.isNotEmpty()) {
        AlertDialog(
            onDismissRequest = { deleteCloud = emptySet() },
            title = { Text("تأكيد حذف النسخ") },
            text = { Text("سيتم حذف ${deleteCloud.size} نسخة من Google Drive نهائيًا. لا يمكن التراجع عن ذلك.") },
            confirmButton = {
                Button(onClick = {
                    val ids = deleteCloud
                    deleteCloud = emptySet()
                    backupSyncViewModel.deleteCloudBackups(ids) { deleted ->
                        selectedCloud = selectedCloud - ids
                        if (deleted < ids.size) Toast.makeText(context, "تم حذف $deleted من أصل ${ids.size} نسخة", Toast.LENGTH_LONG).show()
                    }
                }) { Text("حذف نهائي") }
            },
            dismissButton = { TextButton(onClick = { deleteCloud = emptySet() }) { Text("إلغاء") } }
        )
    }

    if (restoreLocal != null || restoreCloud != null) {
        AlertDialog(
            onDismissRequest = { restoreLocal = null; restoreCloud = null },
            title = { Text("تأكيد الاستعادة") },
            text = { Text("سيتم استبدال البيانات الحالية بالبيانات الموجودة في النسخة. لا تتأثر حالة الترخيص أو مفتاح الاستعادة المحلي.") },
            confirmButton = { Button(onClick = { showRecoveryDialog = true }) { Text("متابعة") } },
            dismissButton = { TextButton(onClick = { restoreLocal = null; restoreCloud = null }) { Text("إلغاء") } }
        )
    }

    if (showRecoveryInfo) {
        AlertDialog(
            onDismissRequest = { showRecoveryInfo = false },
            title = { Text("مفتاح استعادة النسخ") },
            text = {
                Text("هذا المفتاح ضروري لاستعادة نسخة مشفرة على جهاز آخر. احتفظ به خارج الهاتف في مكان آمن؛ لا يمكن استخراج مفتاح النسخة من ملف SLB بدونه.")
            },
            confirmButton = { TextButton(onClick = { showRecoveryInfo = false }) { Text("إغلاق") } }
        )
    }

    if (showRecoveryDialog) {
        AlertDialog(
            onDismissRequest = { showRecoveryDialog = false },
            title = { Text("مفتاح استعادة النسخة") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("على هذا الجهاز يمكن الاستعادة تلقائيًا. عند الاستعادة على جهاز آخر أدخل مفتاح الاستعادة الذي حفظته سابقًا.")
                    OutlinedTextField(
                        value = recoveryCode ?: "",
                        onValueChange = { recoveryCode = it },
                        singleLine = true,
                        label = { Text("مفتاح الاستعادة") }
                    )
                    TextButton(onClick = { recoveryCode = backupSyncViewModel.recoveryCode() }) { Text("استخدام مفتاح هذا الجهاز") }
                }
            },
            confirmButton = {
                Button(onClick = {
                    val code = recoveryCode?.takeIf { it.isNotBlank() }
                    val local = restoreLocal
                    val cloudItem = restoreCloud
                    if (local != null) {
                        backupSyncViewModel.restoreFromLocalFile(local, code) { ok, restored, message ->
                            showRecoveryDialog = false
                            restoreLocal = null
                            if (ok && restored != null) onRestoreSuccess(restored)
                            else Toast.makeText(context, message ?: "تعذر استعادة النسخة", Toast.LENGTH_LONG).show()
                        }
                    } else if (cloudItem != null) {
                        backupSyncViewModel.restoreCloud(cloudItem, code) { ok, restored, message ->
                            showRecoveryDialog = false
                            restoreCloud = null
                            if (ok && restored != null) onRestoreSuccess(restored)
                            else Toast.makeText(context, message ?: "تعذر استعادة النسخة", Toast.LENGTH_LONG).show()
                        }
                    }
                }, enabled = !busy) { Text("استعادة") }
            },
            dismissButton = { TextButton(onClick = { showRecoveryDialog = false }) { Text("إلغاء") } }
        )
    }
}

@Composable
private fun BackupFileRow(name: String, size: String, onRestore: () -> Unit, onDelete: () -> Unit) {
    ListItem(
        headlineContent = { Text(name) },
        supportingContent = { Text(size) },
        leadingContent = { Icon(Icons.Default.Folder, contentDescription = null) },
        trailingContent = {
            Row {
                IconButton(onClick = onRestore) { Icon(Icons.Default.Restore, contentDescription = "استعادة") }
                IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, contentDescription = "حذف") }
            }
        }
    )
}

@Composable
private fun CloudFileRow(
    item: CloudBackupFile,
    selected: Boolean,
    onSelect: () -> Unit,
    onDownload: () -> Unit,
    onRestore: () -> Unit
) {
    ListItem(
        headlineContent = { Text(item.name) },
        supportingContent = { Text("${item.month} • ${item.size} بايت") },
        leadingContent = { Checkbox(checked = selected, onCheckedChange = { onSelect() }) },
        trailingContent = {
            Row {
                IconButton(onClick = onDownload) { Icon(Icons.Default.CloudDownload, contentDescription = "تنزيل") }
                IconButton(onClick = onRestore) { Icon(Icons.Default.Restore, contentDescription = "استعادة") }
            }
        }
    )
}
