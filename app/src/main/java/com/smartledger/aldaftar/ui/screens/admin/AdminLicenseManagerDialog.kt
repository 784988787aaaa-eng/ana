package com.smartledger.aldaftar.ui.screens.admin

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.*
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartledger.aldaftar.R
import com.smartledger.aldaftar.domain.admin.AdminIssuedLicense
import com.smartledger.aldaftar.ui.components.MizanAnimatedDialog
import com.smartledger.aldaftar.ui.components.MizanDialogCard
import com.smartledger.aldaftar.ui.components.MizanDialogHeader
import com.smartledger.aldaftar.ui.theme.MizanDialogTokens
import com.smartledger.aldaftar.ui.viewmodel.AdminLicenseViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AdminLicenseManagerDialog(
    viewModel: AdminLicenseViewModel,
    onDismiss: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    val isProcessing by viewModel.isProcessing.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val successMessage by viewModel.successMessage.collectAsState()
    val lastIssued by viewModel.lastIssuedLicense.collectAsState()
    val licenses by viewModel.allLicenses.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current

    MizanAnimatedDialog(onDismissRequest = onDismiss) {
        MizanDialogCard(
            maxWidth = 580.dp,
            maxHeight = 720.dp
        ) {
            MizanDialogHeader(
                title = stringResource(R.string.admin_manager_title),
                subtitle = stringResource(R.string.admin_manager_subtitle),
                icon = Icons.Default.AdminPanelSettings,
                iconTint = MaterialTheme.colorScheme.primary,
                onClose = onDismiss
            )

            // Tabs Selector
            TabRow(
                selectedTabIndex = selectedTab,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp)),
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.AddCircleOutline, null, Modifier.size(18.dp))
                            Text(stringResource(R.string.admin_tab_new_license), fontWeight = FontWeight.Bold)
                        }
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.FormatListBulleted, null, Modifier.size(18.dp))
                            Text(
                                text = "${stringResource(R.string.admin_tab_registry)} (${licenses.size})",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                )
            }

            // Message Banners
            if (!errorMessage.isNullOrBlank()) {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.ErrorOutline, null, tint = MaterialTheme.colorScheme.error)
                        Text(
                            text = errorMessage ?: "",
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            if (!successMessage.isNullOrBlank()) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, null, tint = MaterialTheme.colorScheme.primary)
                        Text(
                            text = successMessage ?: "",
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            if (selectedTab == 0) {
                // Tab 0: Issue New License
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    AdminNewLicenseForm(
                        viewModel = viewModel,
                        isProcessing = isProcessing,
                        lastIssued = lastIssued,
                        onGenerate = { viewModel.generateLicense() }
                    )
                }
            } else {
                // Tab 1: License Registry Log
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    AdminLicenseRegistryView(
                        licenses = licenses,
                        searchQuery = searchQuery,
                        onSearchChange = { viewModel.searchQuery.value = it },
                        onToggleStatus = { viewModel.toggleLicenseStatus(it) },
                        onDelete = { viewModel.deleteLicense(it) }
                    )
                }
            }
        }
    }
}

@Composable
private fun AdminNewLicenseForm(
    viewModel: AdminLicenseViewModel,
    isProcessing: Boolean,
    lastIssued: AdminIssuedLicense?,
    onGenerate: () -> Unit
) {
    val accountCode by viewModel.accountOrDeviceCode.collectAsState()
    val email by viewModel.customerEmail.collectAsState()
    val name by viewModel.customerName.collectAsState()
    val licenseType by viewModel.selectedLicenseType.collectAsState()
    val plan by viewModel.selectedPlan.collectAsState()
    val trialDays by viewModel.trialDays.collectAsState()
    val maxDevices by viewModel.maxDevices.collectAsState()
    val notes by viewModel.notes.collectAsState()

    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current

    // 1. Client & Device Section
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        )
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "1. بيانات العميل والجهاز",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )

            // Account / Device Code
            OutlinedTextField(
                value = accountCode,
                onValueChange = { viewModel.accountOrDeviceCode.value = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.admin_field_account_code)) },
                placeholder = { Text("SLD-XXXX-XXXX-XXXX أو SL-XXXX-XXXX") },
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.Devices, null, tint = MaterialTheme.colorScheme.primary) },
                shape = MizanDialogTokens.inputShape
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        val clip = clipboard.getText()?.text
                        if (!clip.isNullOrBlank()) {
                            viewModel.accountOrDeviceCode.value = clip.trim()
                            Toast.makeText(context, "تم لصق كود الجهاز", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(Icons.Default.ContentPaste, null, Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(stringResource(R.string.admin_btn_paste), fontSize = 12.sp)
                }

                FilledTonalButton(
                    onClick = {
                        viewModel.fillCurrentDeviceCode()
                        Toast.makeText(context, "تم جلب معرف هذا الجهاز", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(Icons.Default.Smartphone, null, Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(stringResource(R.string.admin_btn_current_device), fontSize = 12.sp)
                }
            }

            // Customer Name
            OutlinedTextField(
                value = name,
                onValueChange = { viewModel.customerName.value = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.admin_field_customer_name)) },
                placeholder = { Text("مثال: متجر الأمل أو محمد المنصوري") },
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.Person, null, tint = MaterialTheme.colorScheme.primary) },
                shape = MizanDialogTokens.inputShape
            )

            // Customer Email
            OutlinedTextField(
                value = email,
                onValueChange = { viewModel.customerEmail.value = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.admin_field_customer_email)) },
                placeholder = { Text("customer@example.com (اختياري)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                leadingIcon = { Icon(Icons.Default.Email, null, tint = MaterialTheme.colorScheme.primary) },
                shape = MizanDialogTokens.inputShape
            )
        }
    }

    // 2. License Specifications Section
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        )
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "2. نوع الترخيص والصلاحية",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )

            // License Type Selector
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("نوع الترخيص:", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    LicenseTypeFilterChip(
                        selected = licenseType == "FULL",
                        label = "ترخيص شامل 🌟",
                        onClick = { viewModel.selectedLicenseType.value = "FULL" },
                        modifier = Modifier.weight(1f)
                    )
                    LicenseTypeFilterChip(
                        selected = licenseType == "LOCAL",
                        label = "محلي (أوفلاين) 💻",
                        onClick = { viewModel.selectedLicenseType.value = "LOCAL" },
                        modifier = Modifier.weight(1f)
                    )
                    LicenseTypeFilterChip(
                        selected = licenseType == "ACCOUNT",
                        label = "سحابي (متصل) ☁️",
                        onClick = { viewModel.selectedLicenseType.value = "ACCOUNT" },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Plan Selector (Lifetime vs Trial)
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("مدة الترخيص:", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = plan == "LIFETIME",
                        onClick = { viewModel.selectedPlan.value = "LIFETIME" },
                        label = { Text(stringResource(R.string.admin_plan_lifetime), fontWeight = FontWeight.Bold) },
                        leadingIcon = { if (plan == "LIFETIME") Icon(Icons.Default.Check, null) },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = plan == "TRIAL",
                        onClick = { viewModel.selectedPlan.value = "TRIAL" },
                        label = { Text(stringResource(R.string.admin_plan_trial), fontWeight = FontWeight.Bold) },
                        leadingIcon = { if (plan == "TRIAL") Icon(Icons.Default.Check, null) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Trial Days input (if plan == TRIAL)
            if (plan == "TRIAL") {
                OutlinedTextField(
                    value = trialDays,
                    onValueChange = { viewModel.trialDays.value = it.filter { ch -> ch.isDigit() } },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.admin_field_trial_days)) },
                    placeholder = { Text("30") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.Timer, null, tint = MaterialTheme.colorScheme.primary) },
                    shape = MizanDialogTokens.inputShape
                )
            }

            // Max Devices
            OutlinedTextField(
                value = maxDevices,
                onValueChange = { viewModel.maxDevices.value = it.filter { ch -> ch.isDigit() } },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.admin_field_max_devices)) },
                placeholder = { Text("1") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.Pin, null, tint = MaterialTheme.colorScheme.primary) },
                shape = MizanDialogTokens.inputShape
            )

            // Notes
            OutlinedTextField(
                value = notes,
                onValueChange = { viewModel.notes.value = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.admin_field_notes)) },
                placeholder = { Text("ملاحظات إضافية، اسم المحل أو رقم الهاتف...") },
                maxLines = 2,
                leadingIcon = { Icon(Icons.Default.Notes, null, tint = MaterialTheme.colorScheme.primary) },
                shape = MizanDialogTokens.inputShape
            )
        }
    }

    // Submit Button
    Button(
        onClick = onGenerate,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = MizanDialogTokens.buttonShape,
        enabled = !isProcessing && accountCode.isNotBlank()
    ) {
        if (isProcessing) {
            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary)
            Spacer(Modifier.width(8.dp))
            Text("جاري التوقيع الرقمي...")
        } else {
            Icon(Icons.Default.VpnKey, null, Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text(stringResource(R.string.admin_btn_generate), fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }
    }

    // Result Card when generated
    if (lastIssued != null) {
        AdminIssuedResultCard(
            license = lastIssued,
            onActivateCurrentDevice = {
                viewModel.activateCurrentDeviceWithIssued(lastIssued.activationToken) { ok ->
                    if (ok) {
                        Toast.makeText(context, "تم تفعيل الترخيص على جهازك الآن!", Toast.LENGTH_LONG).show()
                    }
                }
            }
        )
    }
}

@Composable
private fun LicenseTypeFilterChip(
    selected: Boolean,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Text(
                text = label,
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        modifier = modifier
    )
}

@Composable
private fun AdminIssuedResultCard(
    license: AdminIssuedLicense,
    onActivateCurrentDevice: () -> Unit
) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.5.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(14.dp)),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
        )
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.Verified, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.admin_issued_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "رقم الترخيص: ${license.licenseId} | ${license.customerName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            // Token box
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(
                        text = "رمز الترخيص المشفر (Token):",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = license.activationToken,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        clipboard.setText(AnnotatedString(license.activationToken))
                        Toast.makeText(context, "تم نسخ الرمز المشفر للحافظة", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.ContentCopy, null, Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(stringResource(R.string.admin_btn_copy_token), fontSize = 12.sp)
                }

                Button(
                    onClick = {
                        shareLicenseWhatsApp(context, license)
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366))
                ) {
                    Icon(Icons.Default.Share, null, Modifier.size(16.dp), tint = Color.White)
                    Spacer(Modifier.width(4.dp))
                    Text("واتساب", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                }
            }

            // Activate on this device button
            FilledTonalButton(
                onClick = onActivateCurrentDevice,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Bolt, null, Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text(stringResource(R.string.admin_btn_activate_now), fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun AdminLicenseRegistryView(
    licenses: List<AdminIssuedLicense>,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onToggleStatus: (AdminIssuedLicense) -> Unit,
    onDelete: (AdminIssuedLicense) -> Unit
) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current

    // Statistics Bar
    val lifetimeCount = licenses.count { it.plan == "LIFETIME" }
    val trialCount = licenses.count { it.plan == "TRIAL" }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        StatItem("الإجمالي", licenses.size.toString(), Icons.Default.AllInbox)
        StatItem("مدى الحياة", lifetimeCount.toString(), Icons.Default.AllInclusive)
        StatItem("تجريبي", trialCount.toString(), Icons.Default.HourglassTop)
    }

    // Search bar
    OutlinedTextField(
        value = searchQuery,
        onValueChange = onSearchChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text("بحث بالاسم، الإيميل، معرف الجهاز...") },
        singleLine = true,
        leadingIcon = { Icon(Icons.Default.Search, null) },
        trailingIcon = {
            if (searchQuery.isNotBlank()) {
                IconButton(onClick = { onSearchChange("") }) {
                    Icon(Icons.Default.Close, null)
                }
            }
        },
        shape = RoundedCornerShape(10.dp)
    )

    if (licenses.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.FolderOpen,
                    null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "لا توجد تراخيص في السجل حتى الآن",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 420.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(licenses, key = { it.licenseId }) { lic ->
                AdminLicenseItemCard(
                    license = lic,
                    onCopyToken = {
                        clipboard.setText(AnnotatedString(lic.activationToken))
                        Toast.makeText(context, "تم نسخ الرمز المشفر", Toast.LENGTH_SHORT).show()
                    },
                    onShareWhatsApp = {
                        shareLicenseWhatsApp(context, lic)
                    },
                    onToggleActive = { onToggleStatus(lic) },
                    onDelete = { onDelete(lic) }
                )
            }
        }
    }
}

@Composable
private fun StatItem(label: String, value: String, icon: ImageVector) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(icon, null, Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
        Text("$label: ", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
private fun AdminLicenseItemCard(
    license: AdminIssuedLicense,
    onCopyToken: () -> Unit,
    onShareWhatsApp: () -> Unit,
    onToggleActive: () -> Unit,
    onDelete: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault()) }
    val dateStr = remember(license.issuedAt) { dateFormat.format(Date(license.issuedAt)) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = license.licenseId,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    SuggestionChip(
                        onClick = {},
                        label = {
                            Text(
                                text = when (license.licenseType) {
                                    "FULL" -> "شامل 🌟"
                                    "ACCOUNT" -> "سحابي ☁️"
                                    else -> "محلي 💻"
                                },
                                fontSize = 10.sp
                            )
                        },
                        modifier = Modifier.height(24.dp)
                    )
                }

                // Active toggle
                AssistChip(
                    onClick = onToggleActive,
                    label = {
                        Text(
                            text = if (license.isActive) "نشط" else "معطل",
                            fontSize = 11.sp,
                            color = if (license.isActive) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error
                        )
                    },
                    leadingIcon = {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (license.isActive) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error)
                        )
                    },
                    modifier = Modifier.height(26.dp)
                )
            }

            Text(
                text = "${license.customerName} ${if (license.customerEmail.isNotBlank()) "(${license.customerEmail})" else ""}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = "كود الجهاز: ${license.accountCode}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontFamily = FontFamily.Monospace
            )

            if (license.notes.isNotBlank()) {
                Text(
                    text = "ملاحظات: ${license.notes}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = "تاريخ الإصدار: $dateStr | الخطة: ${if (license.plan == "LIFETIME") "مدى الحياة" else "تجريبي (${license.trialDays} يوم)"}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onCopyToken, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.ContentCopy, contentDescription = "نسخ الرمز", modifier = Modifier.size(18.dp))
                }
                IconButton(onClick = onShareWhatsApp, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Share, contentDescription = "مشاركة واتساب", modifier = Modifier.size(18.dp), tint = Color(0xFF25D366))
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = "حذف الترخيص", modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

private fun shareLicenseWhatsApp(context: Context, license: AdminIssuedLicense) {
    val message = buildString {
        appendLine("مرحباً ${license.customerName}،")
        appendLine("تم إصدار ترخيص برنامج الدفتر الذكي بنجاح 🌟")
        appendLine("-------------------------")
        appendLine("معرف الترخيص: ${license.licenseId}")
        appendLine("نوع الترخيص: ${when (license.licenseType) {
            "FULL" -> "ترخيص شامل (محلي وسحابي)"
            "ACCOUNT" -> "ترخيص سحابي متصل"
            else -> "ترخيص محلي أوفلاين"
        }}")
        appendLine("الخطة: ${if (license.plan == "LIFETIME") "دائم مدى الحياة" else "تجريبي (${license.trialDays} يوم)"}")
        appendLine("معرف جهازك: ${license.accountCode}")
        appendLine("-------------------------")
        appendLine("رمز التفعيل المشفر الخاص بك:")
        appendLine(license.activationToken)
        appendLine("-------------------------")
        appendLine("طريقة التفعيل في التطبيق:")
        appendLine("1. افتح القائمة الجانبية ثم اضغط 'ترخيص البرنامج'")
        appendLine("2. انتقل لتبويب 'رمز الترخيص المشفر'")
        appendLine("3. الصق الرمز أعلاه واضغط 'تفعيل الترخيص'")
    }

    try {
        val sendIntent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("https://api.whatsapp.com/send?text=${Uri.encode(message)}")
        }
        context.startActivity(sendIntent)
    } catch (e: Exception) {
        // Fallback to standard share
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, message)
        }
        context.startActivity(Intent.createChooser(shareIntent, "مشاركة الترخيص عبر"))
    }
}
