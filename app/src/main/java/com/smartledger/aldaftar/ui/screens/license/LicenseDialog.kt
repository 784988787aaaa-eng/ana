package com.smartledger.aldaftar.ui.screens.license

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.smartledger.aldaftar.domain.license.LicenseStatus
import com.smartledger.aldaftar.domain.license.LicenseType
import com.smartledger.aldaftar.ui.theme.WhatsAppGreen
import com.smartledger.aldaftar.ui.viewmodel.LicenseViewModel

private const val SUPPORT_WHATSAPP = "967774004399"

@Composable
fun LicenseDialog(
    viewModel: LicenseViewModel,
    onDismiss: () -> Unit,
    forced: Boolean = false
) {
    val state by viewModel.snapshot.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()
    val busy by viewModel.isBusy.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current

    val locked = forced || state.requiresActivation
    var selectedMode by rememberSaveable { mutableIntStateOf(0) }
    var accountCode by rememberSaveable { mutableStateOf("") }
    var activationCode by rememberSaveable { mutableStateOf("") }
    var signedToken by rememberSaveable { mutableStateOf("") }

    val title = when {
        state.isPaid -> "الترخيص مفعل"
        state.isTrialExpired -> "انتهت المهلة المجانية"
        state.status == LicenseStatus.VERIFICATION_REQUIRED -> "يلزم إعادة التحقق"
        state.status == LicenseStatus.REVOKED -> "الترخيص غير صالح"
        else -> "تفعيل الدفتر الذكي"
    }

    Dialog(
        onDismissRequest = { if (!locked) onDismiss() },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = !locked,
            dismissOnClickOutside = !locked
        )
    ) {
        CompositionLocalProvider(
            LocalLayoutDirection provides LayoutDirection.Rtl
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.94f)
                    .wrapContentHeight()
                    .heightIn(max = 690.dp),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    LicenseHeader(
                        title = title,
                        active = state.isPaid,
                        locked = locked,
                        onDismiss = onDismiss
                    )

                    if (state.isPaid) {
                        ActiveLicenseCard(state.accountCode, state.type)
                    } else {
                        LicenseStateBanner(state)

                        if (state.status == LicenseStatus.REVOKED) {
                            NoticeCard(
                                icon = Icons.Default.Block,
                                text = "تعذر اعتماد الترخيص الحالي. استخدم بيانات تفعيل صحيحة أو تواصل مع المطور.",
                                error = true
                            )
                        }

                        LicenseModeTabs(
                            selected = selectedMode,
                            onSelected = { selectedMode = it }
                        )

                        if (selectedMode == 0) {
                            AccountActivationSection(
                                accountCode = accountCode,
                                activationCode = activationCode,
                                busy = busy,
                                onAccountChange = { accountCode = it.uppercase() },
                                onActivationChange = { activationCode = it },
                                onActivate = { viewModel.activateAccount(accountCode, activationCode) },
                                onReconnect = {
                                    viewModel.reconnectAccount(accountCode)
                                }
                            )
                        } else {
                            SignedTokenSection(
                                token = signedToken,
                                busy = busy,
                                onTokenChange = { signedToken = it },
                                onActivate = { viewModel.applyToken(signedToken) }
                            )
                        }

                        DeviceCodeCard(
                            deviceCode = viewModel.deviceCode(),
                            onCopy = {
                                clipboard.setText(AnnotatedString(viewModel.deviceCode()))
                                Toast.makeText(context, "تم نسخ رمز الجهاز", Toast.LENGTH_SHORT).show()
                            }
                        )

                        DeveloperContactCard(
                            context = context,
                            accountCode = accountCode,
                            deviceCode = viewModel.deviceCode()
                        )
                    }

                    if (busy) {
                        LinearProgressIndicator(
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    if (!message.isNullOrBlank()) {
                        NoticeCard(
                            icon = Icons.Default.Info,
                            text = message!!,
                            error = true
                        )
                    }

                    if (state.isPaid) {
                        Button(
                            onClick = onDismiss,
                            modifier = Modifier.fillMaxWidth().height(40.dp),
                            shape = RoundedCornerShape(11.dp)
                        ) {
                            Icon(Icons.Default.Check, null, Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("إغلاق", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    } else if (!locked) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier.fillMaxWidth().height(38.dp),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("المتابعة لاحقًا", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LicenseHeader(title: String, active: Boolean, locked: Boolean, onDismiss: () -> Unit) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(34.dp).clip(CircleShape)
                    .background(
                        if (active) MaterialTheme.colorScheme.primary.copy(alpha = .12f)
                        else MaterialTheme.colorScheme.error.copy(alpha = .10f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (active) Icons.Default.VerifiedUser else Icons.Default.VpnKey,
                    null,
                    Modifier.size(19.dp),
                    tint = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
            }
            Spacer(Modifier.width(8.dp))
            Column {
                Text(title, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface)
                Text(
                    if (active) "حالة الترخيص: نشط" else "حماية الحساب والبيانات",
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        if (!locked) {
            IconButton(
                onClick = onDismiss,
                modifier = Modifier.size(30.dp).clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .65f))
            ) {
                Icon(Icons.Default.Close, "إغلاق", Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun LicenseStateBanner(state: com.smartledger.aldaftar.domain.license.LicenseSnapshot) {
    val expired = state.isTrialExpired
    val verification = state.status == LicenseStatus.VERIFICATION_REQUIRED
    val revoked = state.status == LicenseStatus.REVOKED
    val error = expired || verification || revoked
    val bg = if (error) MaterialTheme.colorScheme.error.copy(alpha = .07f)
             else MaterialTheme.colorScheme.primary.copy(alpha = .07f)
    val border = if (error) MaterialTheme.colorScheme.error.copy(alpha = .25f)
                  else MaterialTheme.colorScheme.primary.copy(alpha = .22f)
    val text = when {
        expired -> "انتهت المهلة المجانية بعد استخدام ${state.trialUsed} من ${state.trialLimit} معاملة. فعّل الترخيص للمتابعة."
        verification -> "انتهت صلاحية التحقق المحلي. أعد ربط الترخيص بالإنترنت للمتابعة."
        revoked -> "هذا الترخيص مرفوض حاليًا. لا يمكن متابعة العمليات المحمية حتى يتم اعتماد ترخيص صحيح."
        else -> "لديك ${state.trialLimit - state.trialUsed} معاملة متاحة ضمن التجربة المجانية."
    }
    Card(
        Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(bg),
        border = BorderStroke(1.dp, border)
    ) {
        Row(Modifier.fillMaxWidth().padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(
                if (error) Icons.Default.WarningAmber else Icons.Default.Info,
                null,
                Modifier.size(18.dp),
                tint = if (error) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.width(7.dp))
            Text(text, fontSize = 10.5.sp, lineHeight = 15.sp,
                fontWeight = FontWeight.Medium,
                color = if (error) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Start)
        }
    }
}

@Composable
private fun LicenseModeTabs(selected: Int, onSelected: (Int) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(11.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .45f))
            .padding(3.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        LicenseTab(0, selected == 0, Icons.Default.CloudQueue, "حساب الترخيص", onSelected)
        LicenseTab(1, selected == 1, Icons.Default.VpnKey, "رمز ترخيص موقع", onSelected)
    }
}

@Composable
private fun RowScope.LicenseTab(index: Int, selected: Boolean, icon: androidx.compose.ui.graphics.vector.ImageVector, text: String, onSelected: (Int) -> Unit) {
    Box(
        modifier = Modifier.weight(1f).clip(RoundedCornerShape(9.dp))
            .background(if (selected) MaterialTheme.colorScheme.surface else Color.Transparent)
            .padding(vertical = 7.dp),
        contentAlignment = Alignment.Center
    ) {
        TextButton(onClick = { onSelected(index) }, contentPadding = PaddingValues(0.dp)) {
            Icon(icon, null, Modifier.size(15.dp), tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.width(5.dp))
            Text(text, fontSize = 10.5.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun AccountActivationSection(
    accountCode: String,
    activationCode: String,
    busy: Boolean,
    onAccountChange: (String) -> Unit,
    onActivationChange: (String) -> Unit,
    onActivate: () -> Unit,
    onReconnect: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .18f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = .35f))
    ) {
        Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
            Text("تفعيل ترخيص الحساب", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text("أدخل كود الحساب ورمز التفعيل المرسل لك من المطور.", fontSize = 9.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            OutlinedTextField(
                value = accountCode,
                onValueChange = onAccountChange,
                modifier = Modifier.fillMaxWidth().height(53.dp),
                singleLine = true,
                enabled = !busy,
                label = { Text("كود الحساب", fontSize = 10.sp) },
                placeholder = { Text("SL-XXXX-XXXX", fontSize = 10.sp) },
                shape = RoundedCornerShape(10.dp)
            )
            OutlinedTextField(
                value = activationCode,
                onValueChange = onActivationChange,
                modifier = Modifier.fillMaxWidth().height(53.dp),
                singleLine = true,
                enabled = !busy,
                label = { Text("رمز التفعيل", fontSize = 10.sp) },
                shape = RoundedCornerShape(10.dp)
            )
            Button(
                onClick = onActivate,
                enabled = !busy && accountCode.isNotBlank() && activationCode.isNotBlank(),
                modifier = Modifier.fillMaxWidth().height(40.dp),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.VerifiedUser, null, Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("تفعيل الترخيص الآن", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
            if (accountCode.isNotBlank() && activationCode.isBlank()) {
                OutlinedButton(
                    onClick = onReconnect,
                    enabled = !busy,
                    modifier = Modifier.fillMaxWidth().height(34.dp),
                    shape = RoundedCornerShape(9.dp)
                ) {
                    Icon(Icons.Default.Link, null, Modifier.size(15.dp))
                    Spacer(Modifier.width(5.dp))
                    Text("إعادة ربط الترخيص بهذا الجهاز", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun SignedTokenSection(
    token: String,
    busy: Boolean,
    onTokenChange: (String) -> Unit,
    onActivate: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .18f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = .35f))
    ) {
        Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
            Text("التفعيل برمز ترخيص موقع", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text("الصق الرمز الموقع الذي استلمته من المطور كما هو دون تعديل.", fontSize = 9.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            OutlinedTextField(
                value = token,
                onValueChange = onTokenChange,
                modifier = Modifier.fillMaxWidth().height(90.dp),
                minLines = 3,
                enabled = !busy,
                label = { Text("رمز الترخيص", fontSize = 10.sp) },
                placeholder = { Text("الصق الرمز هنا...", fontSize = 10.sp) },
                shape = RoundedCornerShape(10.dp)
            )
            Button(
                onClick = onActivate,
                enabled = !busy && token.isNotBlank(),
                modifier = Modifier.fillMaxWidth().height(40.dp),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.VpnKey, null, Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("تفعيل الرمز", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun DeviceCodeCard(deviceCode: String, onCopy: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .35f))
            .border(.6.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = .3f), RoundedCornerShape(10.dp))
            .padding(horizontal = 9.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.PhoneAndroid, null, Modifier.size(15.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.width(5.dp))
        Text("رمز الجهاز", fontSize = 9.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.width(6.dp))
        Text(deviceCode, Modifier.weight(1f), fontSize = 10.5.sp, fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace, maxLines = 1, overflow = TextOverflow.Ellipsis)
        IconButton(onClick = onCopy, Modifier.size(28.dp)) {
            Icon(Icons.Default.ContentCopy, "نسخ", Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
private fun ActiveLicenseCard(accountCode: String?, type: LicenseType?) {
    Card(
        Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.primary.copy(alpha = .07f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = .22f))
    ) {
        Column(Modifier.padding(11.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CheckCircle, null, Modifier.size(19.dp), tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(6.dp))
                Text("الترخيص نشط ومُعتمد", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = .35f))
            Text(
                if (!accountCode.isNullOrBlank()) "كود الحساب: $accountCode"
                else "نوع الترخيص: ${if (type == LicenseType.LOCAL) "محلي" else "حساب"}",
                fontSize = 10.5.sp, fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text("تم اعتماد الترخيص لهذا الجهاز ويمكن متابعة العمليات المحمية.", fontSize = 9.5.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun NoticeCard(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String, error: Boolean) {
    Card(
        Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            if (error) MaterialTheme.colorScheme.error.copy(alpha = .07f)
            else MaterialTheme.colorScheme.surfaceVariant
        ),
        border = BorderStroke(1.dp, if (error) MaterialTheme.colorScheme.error.copy(alpha = .2f) else MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(Modifier.padding(9.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, Modifier.size(16.dp), tint = if (error) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(6.dp))
            Text(text, fontSize = 10.sp, lineHeight = 14.sp, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
private fun DeveloperContactCard(context: Context, accountCode: String, deviceCode: String) {
    Card(
        Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(WhatsAppGreen.copy(alpha = .07f)),
        border = BorderStroke(1.dp, WhatsAppGreen.copy(alpha = .22f))
    ) {
        Column(Modifier.padding(9.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
            Text("التواصل مع المطور", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Text(
                "لشراء الترخيص أو طلب المساعدة، أرسل للمطور كود الحساب ورمز الجهاز مباشرة.",
                fontSize = 9.5.sp, lineHeight = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Button(
                onClick = {
                    openDeveloperWhatsApp(context, accountCode, deviceCode)
                },
                modifier = Modifier.fillMaxWidth().height(38.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = WhatsAppGreen)
            ) {
                Icon(Icons.Default.Chat, null, Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("تواصل مباشرة مع المطور عبر واتساب", fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

private fun openDeveloperWhatsApp(context: Context, accountCode: String, deviceCode: String) {
    val msg = buildString {
        append("مرحباً، أريد تفعيل تطبيق الدفتر الذكي.\n")
        append("كود الحساب: ")
        append(if (accountCode.isBlank()) "غير متوفر" else accountCode)
        append("\nرمز الجهاز: ")
        append(deviceCode)
    }
    val uri = Uri.parse("https://api.whatsapp.com/send?phone=$SUPPORT_WHATSAPP&text=${Uri.encode(msg)}")
    val intent = Intent(Intent.ACTION_VIEW, uri).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    runCatching { context.startActivity(intent) }
        .onFailure {
            Toast.makeText(context, "تعذر فتح واتساب على هذا الجهاز", Toast.LENGTH_SHORT).show()
        }
}
