package com.smartledger.aldaftar.ui.screens

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.smartledger.aldaftar.data.cloud.GoogleDriveInternalAuth
import com.smartledger.aldaftar.data.local.entities.AppSettings
import com.smartledger.aldaftar.domain.model.CloudBackupFile
import com.smartledger.aldaftar.ui.theme.FinancialSelectionGreen
import com.smartledger.aldaftar.ui.viewmodel.BackupSyncViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val EmeraldPrimary = FinancialSelectionGreen
private val InfoBlue = Color(0xFF3B82F6)

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

    LaunchedEffect(Unit) { backupSyncViewModel.googleClientId { clientId = it } }

    val googleClient = remember(clientId) {
        clientId?.takeIf(String::isNotBlank)?.let { GoogleDriveInternalAuth(context).client(it) }
    }
    val signInLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode != Activity.RESULT_OK) return@rememberLauncherForActivityResult
        runCatching {
            GoogleSignIn.getSignedInAccountFromIntent(result.data)
                .getResult(com.google.android.gms.common.api.ApiException::class.java)
        }.onSuccess { account ->
            account.serverAuthCode?.takeIf(String::isNotBlank)?.let { code ->
                backupSyncViewModel.connectCloudWithServerAuthCode(code, account.email)
            }
        }
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
            dragHandle = { BottomSheetDefaults.DragHandle() }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 12.dp)
                    .padding(bottom = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                BackupHeader(connected)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CloudSection(
                            connected = connected,
                            email = email,
                            busy = busy,
                            manualOptions = manualOptions,
                            onInternalConnect = { googleClient?.let { signInLauncher.launch(it.signInIntent) } },
                            onManualToggle = { manualOptions = !manualOptions },
                            onManualConnect = { backupSyncViewModel.connectCloud() },
                            onArchive = { archiveOpen = true },
                            onCreateBackup = { backupSyncViewModel.createCloudBackup() },
                            onDisconnect = { backupSyncViewModel.disconnectCloud() }
                        )
                        OutlinedButton(
                            onClick = { resetOpen = true },
                            modifier = Modifier.fillMaxWidth().height(36.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                        ) {
                            Icon(Icons.Default.DeleteForever, null, Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("مسح البيانات وإعادة الضبط", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                if (busy) OperationStatus(busyMessage ?: "جاري تنفيذ العملية")
                error?.takeIf(String::isNotBlank)?.let {
                    Text(it, modifier = Modifier.fillMaxWidth(), fontSize = 10.sp, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
                }
            }
        }

        if (archiveOpen) {
            CloudArchiveBottomSheet(
                vm = backupSyncViewModel,
                onDismiss = { archiveOpen = false },
                onRestoreSuccess = onRestoreSuccess,
                onConnect = {
                    archiveOpen = false
                    googleClient?.let { signInLauncher.launch(it.signInIntent) }
                }
            )
        }
    }

    if (resetOpen) {
        AlertDialog(
            onDismissRequest = { resetOpen = false },
            title = { Text("تأكيد مسح البيانات", fontWeight = FontWeight.Bold) },
            text = { Text("سيتم حذف جميع بيانات الدفتر نهائيًا. لا يمكن التراجع عن هذا الإجراء.") },
            confirmButton = {
                TextButton(onClick = {
                    backupSyncViewModel.clearLocalCopyAndWipeMemory {
                        resetOpen = false
                        onDismiss()
                    }
                }) { Text("مسح وإعادة الضبط", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { resetOpen = false }) { Text("إلغاء") } }
        )
    }
}

@Composable
private fun BackupHeader(connected: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 2.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(horizontalAlignment = Alignment.End) {
            Text("النسخ الاحتياطي والمزامنة", fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Text(
                if (connected) "متصل بالسحابة" else "الحفظ المحلي يعمل تلقائيًا",
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = if (connected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Icon(Icons.Default.Backup, null, Modifier.size(20.dp), tint = EmeraldPrimary)
    }
}

@Composable
private fun CloudSection(
    connected: Boolean,
    email: String?,
    busy: Boolean,
    manualOptions: Boolean,
    onInternalConnect: () -> Unit,
    onManualToggle: () -> Unit,
    onManualConnect: () -> Unit,
    onArchive: () -> Unit,
    onCreateBackup: () -> Unit,
    onDisconnect: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("المزامنة السحابية (Google Drive)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Icon(Icons.Default.CloudSync, null, Modifier.size(18.dp), tint = EmeraldPrimary)
        }

        if (!connected) {
            Button(
                onClick = onInternalConnect,
                enabled = !busy,
                modifier = Modifier.fillMaxWidth().height(36.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
            ) {
                Icon(Icons.Default.Link, null, Modifier.size(15.dp))
                Spacer(Modifier.width(6.dp))
                Text("ربط حساب Google Drive", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
            TextButton(
                onClick = onManualToggle,
                modifier = Modifier.fillMaxWidth().height(28.dp),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
            ) {
                Text(
                    if (manualOptions) "إخفاء خيارات الربط اليدوي" else "خيارات الربط اليدوي",
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = EmeraldPrimary
                )
            }
            if (manualOptions) ManualConnectionCard(busy, onManualConnect)
        } else {
            ConnectedAccount(email)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                SmallAction("إنشاء نسخة سحابية", Icons.Default.CloudUpload, Modifier.weight(1f), busy, onCreateBackup)
                SmallAction("سجل النسخ السحابية", Icons.Default.BackupTable, Modifier.weight(1f), busy, onArchive)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                SmallAction("استعادة نسخة", Icons.Default.Restore, Modifier.weight(1f), busy, onArchive)
                SmallAction("قطع الاتصال", Icons.Default.LinkOff, Modifier.weight(1f), busy, onDisconnect)
            }
        }
    }
}

@Composable
private fun ConnectedAccount(email: String?) {
    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = .5f),
            shape = RoundedCornerShape(8.dp)
        ) {
            Row(Modifier.padding(horizontal = 8.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CheckCircle, null, Modifier.size(16.dp), tint = EmeraldPrimary)
                Spacer(Modifier.width(6.dp))
                Text("الحساب المتصل: ${email ?: "حساب Google"}", fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
            }
        }
        Text(
            "يتم حفظ ومزامنة السجلات بأمان مع هذا الحساب.",
            modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.errorContainer.copy(alpha = .25f), RoundedCornerShape(6.dp)).padding(horizontal = 8.dp, vertical = 5.dp),
            fontSize = 9.sp,
            lineHeight = 12.sp,
            color = MaterialTheme.colorScheme.onErrorContainer,
            textAlign = TextAlign.End
        )
    }
}

@Composable
private fun ManualConnectionCard(busy: Boolean, onConnect: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(10.dp)),
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("خطوات الربط اليدوي", fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
            Text("١. سجّل الدخول عبر المتصفح وامنح الصلاحية.", fontSize = 9.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("٢. بعد اكتمال الربط، عد إلى التطبيق.", fontSize = 9.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            OutlinedButton(
                onClick = onConnect,
                enabled = !busy,
                modifier = Modifier.fillMaxWidth().height(34.dp),
                shape = RoundedCornerShape(9.dp)
            ) { Text("فتح الربط اليدوي عبر المتصفح", fontSize = 10.sp, fontWeight = FontWeight.Bold) }
        }
    }
}

@Composable
private fun SmallAction(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier, busy: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = !busy,
        modifier = modifier.height(36.dp),
        shape = RoundedCornerShape(10.dp),
        contentPadding = PaddingValues(horizontal = 6.dp)
    ) {
        Icon(icon, null, Modifier.size(15.dp))
        Spacer(Modifier.width(5.dp))
        Text(title, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun OperationStatus(text: String) {
    Surface(Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(8.dp)) {
        Row(Modifier.padding(horizontal = 10.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            CircularProgressIndicator(Modifier.size(14.dp), strokeWidth = 2.dp)
            Text(text, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
        Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(bottom = 80.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            if (!searchActive) {
                ArchiveHeader(selectionMode, selected.size, onSearch = { searchActive = true }, onSelection = { selectionMode = !selectionMode; selected = emptySet() }, onDismiss = onDismiss)
            } else {
                SearchHeader(search, onSearchChange = { search = it }, onClose = { searchActive = false; search = "" })
            }
            HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)
            if (!connected) EmptyCloudState(onConnect)
            else if (busy && items.isEmpty()) LoadingCloudState()
            else {
                ArchiveStats(email, items.size, selectionMode, selected.size, onRefresh = { if (selectionMode) selected = if (selected.size == items.size) emptySet() else items.map { it.id }.toSet() else vm.refreshCloud() })
                if (items.isEmpty()) EmptyListState()
                else LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth().heightIn(max = 430.dp)) {
                    items(items, key = { it.id }) { item ->
                        CloudBackupRow(item, selectionMode, selected.contains(item.id), onClick = { if (selectionMode) selected = if (selected.contains(item.id)) selected - item.id else selected + item.id else restoreItem = item }, onDelete = { deleteItem = item })
                    }
                }
            }
        }
        Surface(Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.surface.copy(alpha = .96f), shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)) {
            Column(Modifier.padding(16.dp)) {
                if (selectionMode && selected.isNotEmpty()) {
                    Button(onClick = { deleteMany = true }, modifier = Modifier.fillMaxWidth().height(48.dp), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error), shape = RoundedCornerShape(14.dp)) {
                        Icon(Icons.Default.Delete, null, Modifier.size(20.dp)); Spacer(Modifier.width(8.dp)); Text("حذف ${selected.size} نسخ", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Button(onClick = { vm.createCloudBackup() }, modifier = Modifier.fillMaxWidth().height(48.dp), shape = RoundedCornerShape(14.dp)) {
                        Icon(Icons.Default.CloudUpload, null, Modifier.size(20.dp)); Spacer(Modifier.width(8.dp)); Text("إنشاء نسخة سحابية الآن", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    restoreItem?.let { item ->
        AlertDialog(onDismissRequest = { restoreItem = null }, title = { Text("تأكيد استعادة النسخة السحابية", fontWeight = FontWeight.Bold) }, text = { Text("سيتم استبدال السجلات الحالية بالبيانات المحفوظة في النسخة السحابية (${item.name}). هل تريد المتابعة؟") }, confirmButton = { TextButton(onClick = { vm.restoreCloud(item) { ok, settings, _ -> restoreItem = null; if (ok && settings != null) onRestoreSuccess(settings) } }) { Text("استعادة النسخة") } }, dismissButton = { TextButton(onClick = { restoreItem = null }) { Text("إلغاء") } })
    }
    deleteItem?.let { item ->
        AlertDialog(onDismissRequest = { deleteItem = null }, title = { Text("حذف النسخة السحابية", fontWeight = FontWeight.Bold) }, text = { Text("سيتم حذف النسخة (${item.name}) نهائيًا من حساب Google Drive. لا يمكن التراجع عن هذا الإجراء.") }, confirmButton = { TextButton(onClick = { vm.deleteCloudBackups(setOf(item.id)) { deleteItem = null } }) { Text("حذف نهائي", color = MaterialTheme.colorScheme.error) } }, dismissButton = { TextButton(onClick = { deleteItem = null }) { Text("إلغاء") } })
    }
    if (deleteMany) AlertDialog(onDismissRequest = { deleteMany = false }, title = { Text("حذف النسخ المحددة", fontWeight = FontWeight.Bold) }, text = { Text("هل أنت متأكد من حذف ${selected.size} نسخة محددة نهائيًا من السحابة؟") }, confirmButton = { TextButton(onClick = { vm.deleteCloudBackups(selected) { deleteMany = false; selected = emptySet(); selectionMode = false } }) { Text("حذف المحدد", color = MaterialTheme.colorScheme.error) } }, dismissButton = { TextButton(onClick = { deleteMany = false }) { Text("إلغاء") } })
}

@Composable
private fun ArchiveHeader(selection: Boolean, count: Int, onSearch: () -> Unit, onSelection: () -> Unit, onDismiss: () -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.CloudSync, null, Modifier.size(20.dp), tint = EmeraldPrimary)
            Text("أرشيف النسخ السحابية", fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
        Row {
            TextButton(onClick = onSearch, contentPadding = PaddingValues(6.dp)) { Text("بحث", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
            TextButton(onClick = onSelection, contentPadding = PaddingValues(6.dp)) { Text(if (selection) "رجوع" else "تحديد متعدد", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
            TextButton(onClick = onDismiss, contentPadding = PaddingValues(6.dp)) { Text("إغلاق", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
        }
    }
}

@Composable
private fun SearchHeader(search: String, onSearchChange: (String) -> Unit, onClose: () -> Unit) {
    Row(Modifier.fillMaxWidth().height(44.dp).clip(RoundedCornerShape(10.dp)).background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .5f)).padding(horizontal = 10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        IconButton(onClick = onClose, Modifier.size(32.dp)) { Icon(Icons.Default.ArrowForward, "إغلاق البحث", Modifier.size(20.dp)) }
        BasicTextField(value = search, onValueChange = onSearchChange, modifier = Modifier.weight(1f), textStyle = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface), singleLine = true, decorationBox = { inner -> if (search.isBlank()) Text("بحث في النسخ السحابية...", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant); inner() })
        if (search.isNotBlank()) IconButton(onClick = { onSearchChange("") }, Modifier.size(28.dp)) { Icon(Icons.Default.Clear, "مسح البحث", Modifier.size(18.dp)) }
    }
}

@Composable
private fun ArchiveStats(email: String?, count: Int, selection: Boolean, selected: Int, onRefresh: () -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Column(horizontalAlignment = Alignment.End) {
            Text(email ?: "حساب متصل", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("عدد النسخ المحفوظة: $count", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("المساحة السحابية المستخدمة", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        TextButton(onClick = onRefresh, contentPadding = PaddingValues(6.dp)) {
            Text(if (selection) if (selected == count && count > 0) "إلغاء التحديد" else "تحديد الكل" else "تحديث", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = InfoBlue)
        }
    }
}

@Composable
private fun CloudBackupRow(item: CloudBackupFile, selection: Boolean, checked: Boolean, onClick: () -> Unit, onDelete: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().border(if (checked) 1.5.dp else .8.dp, if (checked) InfoBlue else MaterialTheme.colorScheme.outlineVariant.copy(alpha = .5f), RoundedCornerShape(10.dp)), colors = CardDefaults.cardColors(if (checked) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(10.dp), onClick = onClick) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 6.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column(horizontalAlignment = Alignment.End) {
                Text(SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(item.modifiedTime)), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text(SimpleDateFormat("hh:mm a", Locale.US).format(Date(item.modifiedTime)), fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("${"%.1f".format(Locale.US, item.size / 1024.0)} ك.ب", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (selection) IconButton(onClick = onClick, Modifier.size(28.dp)) { Icon(Icons.Default.CheckCircle, null, Modifier.size(20.dp), tint = if (checked) InfoBlue else MaterialTheme.colorScheme.outlineVariant) }
                else IconButton(onClick = onDelete, Modifier.size(28.dp)) { Icon(Icons.Default.MoreVert, null, Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant) }
            }
        }
    }
}

@Composable
private fun EmptyCloudState(onConnect: () -> Unit) {
    Column(Modifier.fillMaxWidth().padding(vertical = 32.dp, horizontal = 16.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Icon(Icons.Default.CloudOff, null, Modifier.size(64.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = .6f))
        Text("السحابة غير متصلة", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text("قم بربط حساب Google Drive لتصفح واسترجاع نسخك الاحتياطية السحابية بأمان.", fontSize = 12.sp, lineHeight = 20.sp, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Button(onClick = onConnect, colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary), shape = RoundedCornerShape(10.dp), contentPadding = PaddingValues(top = 8.dp)) { Icon(Icons.Default.Link, null, Modifier.size(16.dp)); Spacer(Modifier.width(6.dp)); Text("ربط حساب Google Drive", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
    }
}

@Composable
private fun LoadingCloudState() {
    Column(Modifier.fillMaxWidth().padding(vertical = 40.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        CircularProgressIndicator(Modifier.size(36.dp), color = EmeraldPrimary)
        Text("جاري تحميل النسخ السحابية", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun EmptyListState() {
    Column(Modifier.fillMaxWidth().padding(vertical = 40.dp, horizontal = 20.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Icon(Icons.Default.BackupTable, null, Modifier.size(56.dp), tint = MaterialTheme.colorScheme.outlineVariant)
        Text("لا توجد نسخ سحابية", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text("أنشئ نسختك الأولى لتأمين بياناتك وسجلاتك المالية في السحابة.", fontSize = 12.sp, lineHeight = 18.sp, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
