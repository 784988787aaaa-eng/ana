package com.smartledger.aldaftar.ui.screens.admin

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartledger.aldaftar.domain.admin.AdminIssuedLicense
import com.smartledger.aldaftar.ui.components.MizanAnimatedDialog
import com.smartledger.aldaftar.ui.components.MizanDialogCard
import com.smartledger.aldaftar.ui.theme.MizanDialogTokens
import com.smartledger.aldaftar.ui.viewmodel.AdminLicenseViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AdminLicenseManagerDialog(
    viewModel: AdminLicenseViewModel,
    onDismiss: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val isProcessing by viewModel.isProcessing.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val successMessage by viewModel.successMessage.collectAsState()
    val lastIssued by viewModel.lastIssuedLicense.collectAsState()
    val licenses by viewModel.allLicenses.collectAsState()

    val accountCode by viewModel.accountOrDeviceCode.collectAsState()
    val email by viewModel.customerEmail.collectAsState()
    val phone by viewModel.customerPhone.collectAsState()
    val name by viewModel.customerName.collectAsState()
    val licenseType by viewModel.selectedLicenseType.collectAsState()
    val plan by viewModel.selectedPlan.collectAsState()
    val trialDays by viewModel.trialDays.collectAsState()
    val maxDevices by viewModel.maxDevices.collectAsState()

    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current

    MizanAnimatedDialog(onDismissRequest = onDismiss) {
        MizanDialogCard(
            maxWidth = 520.dp,
            maxHeight = 580.dp,
            contentPadding = PaddingValues(12.dp)
        ) {
            // Header Bar (Compact & Clean)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.VpnKey,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "إدارة وإصدار التراخيص",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "إغلاق", modifier = Modifier.size(18.dp))
                }
            }

            // Compact Tabs
            TabRow(
                selectedTabIndex = selectedTab,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp)
                    .clip(RoundedCornerShape(8.dp)),
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Text("إصدار ترخيص", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Text("سجل التراخيص (${licenses.size})", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                )
            }

            // Status Banners
            if (!errorMessage.isNullOrBlank()) {
                Text(
                    text = errorMessage ?: "",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }

            if (!successMessage.isNullOrBlank()) {
                Text(
                    text = successMessage ?: "",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }

            if (selectedTab == 0) {
                // Form View
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState())
                        .imePadding(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // 1. License Type Selector (Local vs Cloud)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FilterChip(
                            selected = licenseType == "LOCAL",
                            onClick = { viewModel.selectedLicenseType.value = "LOCAL" },
                            label = { Text("💻 محلي (أوفلاين)", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = licenseType == "ACCOUNT",
                            onClick = { viewModel.selectedLicenseType.value = "ACCOUNT" },
                            label = { Text("☁️ سحابي (24 خانة)", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // 2. Account / Device Code field with Paste & Current Device buttons
                    OutlinedTextField(
                        value = accountCode,
                        onValueChange = { viewModel.accountOrDeviceCode.value = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(if (licenseType == "ACCOUNT") "كود الحساب (SLD-...)" else "معرف الجهاز (SLD-...)", fontSize = 12.sp) },
                        placeholder = { Text("SLD-L7XY-P7DX-DR", fontSize = 11.sp) },
                        singleLine = true,
                        trailingIcon = {
                            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                IconButton(
                                    onClick = {
                                        val clip = clipboard.getText()?.text
                                        if (!clip.isNullOrBlank()) {
                                            viewModel.accountOrDeviceCode.value = clip.trim()
                                            Toast.makeText(context, "تم لصق الكود", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.ContentPaste, "لصق", Modifier.size(16.dp))
                                }
                                IconButton(
                                    onClick = {
                                        viewModel.fillCurrentDeviceCode()
                                        Toast.makeText(context, "تم جلب كود هذا الجهاز", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.Smartphone, "جهازي", Modifier.size(16.dp))
                                }
                            }
                        },
                        shape = MizanDialogTokens.inputShape
                    )

                    // 3. Customer Phone & Email Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        OutlinedTextField(
                            value = phone,
                            onValueChange = { viewModel.customerPhone.value = it },
                            modifier = Modifier.weight(1f),
                            label = { Text("رقم الواتساب", fontSize = 11.sp) },
                            placeholder = { Text("966500000000", fontSize = 10.sp) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            leadingIcon = {
                                Icon(Icons.Default.Phone, null, Modifier.size(15.dp), tint = Color(0xFF25D366))
                            },
                            shape = MizanDialogTokens.inputShape
                        )

                        OutlinedTextField(
                            value = email,
                            onValueChange = { viewModel.customerEmail.value = it },
                            modifier = Modifier.weight(1f),
                            label = { Text("البريد الإلكتروني", fontSize = 11.sp) },
                            placeholder = { Text("user@gmail.com", fontSize = 10.sp) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            shape = MizanDialogTokens.inputShape
                        )
                    }

                    // 4. Customer Name & Plan Selection
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { viewModel.customerName.value = it },
                            modifier = Modifier.weight(1.2f),
                            label = { Text("اسم العميل / النشاط", fontSize = 11.sp) },
                            placeholder = { Text("مؤسسة الوفاق", fontSize = 10.sp) },
                            singleLine = true,
                            shape = MizanDialogTokens.inputShape
                        )

                        Row(
                            modifier = Modifier.weight(1f),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            FilterChip(
                                selected = plan == "LIFETIME",
                                onClick = { viewModel.selectedPlan.value = "LIFETIME" },
                                label = { Text("دائم", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                                modifier = Modifier.weight(1f)
                            )
                            FilterChip(
                                selected = plan == "TRIAL",
                                onClick = { viewModel.selectedPlan.value = "TRIAL" },
                                label = { Text("تجربة", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // 5. If Trial: Days & Max Devices Row
                    if (plan == "TRIAL") {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            OutlinedTextField(
                                value = trialDays,
                                onValueChange = { viewModel.trialDays.value = it },
                                modifier = Modifier.weight(1f),
                                label = { Text("أيام التجربة", fontSize = 11.sp) },
                                placeholder = { Text("30", fontSize = 10.sp) },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                shape = MizanDialogTokens.inputShape
                            )
                            OutlinedTextField(
                                value = maxDevices,
                                onValueChange = { viewModel.maxDevices.value = it },
                                modifier = Modifier.weight(1f),
                                label = { Text("عدد الأجهزة", fontSize = 11.sp) },
                                placeholder = { Text("1", fontSize = 10.sp) },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                shape = MizanDialogTokens.inputShape
                            )
                        }
                    }

                    // 6. Generate & Activate Button
                    Button(
                        onClick = { viewModel.generateLicense() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp),
                        shape = MizanDialogTokens.buttonShape,
                        enabled = !isProcessing && accountCode.isNotBlank()
                    ) {
                        if (isProcessing) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), color = MaterialTheme.colorScheme.onPrimary)
                            Spacer(Modifier.width(6.dp))
                            Text("جاري الإصدار والتوقيع...", fontSize = 13.sp)
                        } else {
                            Icon(Icons.Default.FlashOn, null, Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = if (licenseType == "ACCOUNT") "توليد كود التفعيل السحابي (24 خانة) ⚡" else "توليد وتوقيع الترخيص المحلي ⚡",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }

                    // 7. Display Token Result from DB / Signer
                    if (lastIssued != null) {
                        IssuedLicenseResultCard(
                            license = lastIssued!!,
                            onActivateLocal = {
                                viewModel.activateCurrentDeviceWithIssued(lastIssued!!.activationToken) { ok ->
                                    if (ok) {
                                        Toast.makeText(context, "تم تفعيل الترخيص على هذا الجهاز بنجاح!", Toast.LENGTH_LONG).show()
                                    }
                                }
                            }
                        )
                    }
                }
            } else {
                // Registry Log View
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .imePadding(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (licenses.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("لا توجد تراخيص محفوظة في قاعدة البيانات حتى الآن", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 400.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            items(licenses, key = { it.licenseId }) { lic ->
                                LicenseRegistryItemCard(
                                    license = lic,
                                    onCopyToken = {
                                        val toCopy = if (lic.licenseType == "ACCOUNT") lic.shortActivationCode else lic.activationToken
                                        clipboard.setText(AnnotatedString(toCopy))
                                        Toast.makeText(context, "تم نسخ كود التفعيل", Toast.LENGTH_SHORT).show()
                                    },
                                    onShareWhatsApp = {
                                        shareLicenseWhatsApp(context, lic)
                                    },
                                    onToggleStatus = { viewModel.toggleLicenseStatus(lic) },
                                    onDelete = { viewModel.deleteLicense(lic) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun IssuedLicenseResultCard(
    license: AdminIssuedLicense,
    onActivateLocal: () -> Unit
) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val isCloud = license.licenseType == "ACCOUNT"

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp)),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (isCloud) "كود التفعيل السحابي (24 خانة):" else "رمز التفعيل المشفر (محلي):",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = if (isCloud) "☁️ سحابي" else "💻 محلي أوفلاين",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            if (isCloud) {
                // Cloud 24-character code display
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = license.shortActivationCode,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        IconButton(
                            onClick = {
                                clipboard.setText(AnnotatedString(license.shortActivationCode))
                                Toast.makeText(context, "تم نسخ كود التفعيل السحابي", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.size(26.dp)
                        ) {
                            Icon(Icons.Default.ContentCopy, "نسخ", Modifier.size(15.dp))
                        }
                    }
                }
            } else {
                // Local RSA signed token display
                Text(
                    text = license.activationToken,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 9.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(4.dp))
                        .padding(6.dp)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        val toCopy = if (isCloud) license.shortActivationCode else license.activationToken
                        clipboard.setText(AnnotatedString(toCopy))
                        Toast.makeText(context, "تم نسخ الرمز للحافظة", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.weight(1f).height(34.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    Icon(Icons.Default.ContentCopy, null, Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("نسخ الرمز", fontSize = 11.sp)
                }

                Button(
                    onClick = onActivateLocal,
                    modifier = Modifier.weight(1f).height(34.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    Icon(Icons.Default.Check, null, Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("تفعيل جهازي", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { shareLicenseWhatsApp(context, license) },
                    modifier = Modifier.height(34.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    Icon(Icons.Default.Share, null, Modifier.size(14.dp), tint = Color.White)
                    Spacer(Modifier.width(4.dp))
                    Text("واتساب", fontSize = 11.sp, color = Color.White)
                }
            }
        }
    }
}

@Composable
private fun LicenseRegistryItemCard(
    license: AdminIssuedLicense,
    onCopyToken: () -> Unit,
    onShareWhatsApp: () -> Unit,
    onToggleStatus: () -> Unit,
    onDelete: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("MM/dd HH:mm", Locale.getDefault()) }
    val dateStr = remember(license.issuedAt) { dateFormat.format(Date(license.issuedAt)) }
    val isCloud = license.licenseType == "ACCOUNT"

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = license.licenseId,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = if (isCloud) "☁️ سحابي" else "💻 محلي",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(if (license.isActive) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error)
                            .clickable { onToggleStatus() }
                    )
                }

                if (isCloud && license.shortActivationCode.isNotBlank()) {
                    Text(
                        text = "الكود: ${license.shortActivationCode}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Text(
                    text = "${license.customerName} | ${license.accountCode} | $dateStr",
                    fontSize = 10.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                IconButton(onClick = onCopyToken, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.ContentCopy, "نسخ", Modifier.size(15.dp))
                }
                IconButton(onClick = onShareWhatsApp, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Share, "واتساب", Modifier.size(15.dp), tint = Color(0xFF25D366))
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.DeleteOutline, "حذف", Modifier.size(15.dp), tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

private fun shareLicenseWhatsApp(context: Context, license: AdminIssuedLicense) {
    val isCloud = license.licenseType == "ACCOUNT"
    val codeToSend = if (isCloud) license.shortActivationCode else license.activationToken

    val message = buildString {
        appendLine("مرحباً ${license.customerName.ifBlank { "عميلنا الكريم" }}،")
        appendLine("تم إصدار ترخيص برنامج الدفتر الذكي الخاص بك:")
        appendLine("• نوع الترخيص: ${if (isCloud) "سحابي (مرتبط بالحساب)" else "محلي (أوفلاين)"}")
        appendLine("• الخطة: ${if (license.plan == "LIFETIME") "مدى الحياة (دائم)" else "تجريبي (${license.trialDays} يوم)"}")
        appendLine("• كود الحساب / الجهاز: ${license.accountCode}")
        appendLine("• كود التفعيل:")
        appendLine(codeToSend)
        appendLine("\nطريقة التفعيل: افتح التطبيق -> القائمة الجانبية -> تفعيل الترخيص -> الصق الكود واضغط تفعيل.")
    }

    // Clean phone number (digits only)
    val phoneClean = license.customerPhone.filter { it.isDigit() }

    try {
        val url = if (phoneClean.isNotBlank()) {
            "https://api.whatsapp.com/send?phone=$phoneClean&text=${Uri.encode(message)}"
        } else {
            "https://api.whatsapp.com/send?text=${Uri.encode(message)}"
        }
        val sendIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(sendIntent)
    } catch (e: Exception) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, message)
        }
        context.startActivity(Intent.createChooser(shareIntent, "مشاركة الترخيص عبر"))
    }
}
