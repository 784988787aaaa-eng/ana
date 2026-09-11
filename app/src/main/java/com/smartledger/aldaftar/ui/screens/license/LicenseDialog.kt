package com.smartledger.aldaftar.ui.screens.license

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.smartledger.aldaftar.data.account.UnifiedAccountSession
import com.smartledger.aldaftar.data.cloud.GoogleDriveInternalAuth
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
    val session by viewModel.session.collectAsStateWithLifecycle()
    val state by viewModel.snapshot.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()
    val busy by viewModel.isBusy.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current

    var selectedMode by rememberSaveable { mutableIntStateOf(0) }
    var activationCode by rememberSaveable { mutableStateOf("") }
    var signedToken by rememberSaveable { mutableStateOf("") }

    val googleClient = remember {
        GoogleDriveInternalAuth(context).client()
    }
    val googleSignInLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode != Activity.RESULT_OK) return@rememberLauncherForActivityResult
        runCatching {
            GoogleSignIn.getSignedInAccountFromIntent(result.data)
                .getResult(com.google.android.gms.common.api.ApiException::class.java)
        }.onSuccess { account ->
            viewModel.signInWithGoogle(account, account.serverAuthCode)
        }.onFailure { ex ->
            Toast.makeText(context, "تعذر تسجيل الدخول بحساب Google: ${ex.localizedMessage ?: ex.message}", Toast.LENGTH_LONG).show()
        }
    }

    val title = when {
        state.isPaid -> "الترخيص مفعل"
        state.isTrialExpired -> "انتهى الحد المسموح"
        state.status == LicenseStatus.VERIFICATION_REQUIRED -> "تحديث الترخيص"
        state.status == LicenseStatus.REVOKED -> "الترخيص غير صالح"
        else -> "تفعيل الترخيص"
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        CompositionLocalProvider(
            LocalLayoutDirection provides LayoutDirection.Rtl
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .widthIn(max = 420.dp)
                    .wrapContentHeight()
                    .heightIn(max = 620.dp),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(
                    1.dp,
                    if (state.isPaid) MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
                    else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
                ),
                tonalElevation = 4.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // 1. Header
                    CompactLicenseHeader(
                        title = title,
                        active = state.isPaid,
                        onDismiss = onDismiss
                    )

                    if (state.isPaid) {
                        // 2. Active License Card
                        ActiveLicenseCompactCard(
                            session = session,
                            type = state.type,
                            onSignOut = { viewModel.signOutUnified() }
                        )
                    } else {
                        // 3. State Banner (Trial count or Alert)
                        CompactStateBanner(
                            isExpired = state.isTrialExpired,
                            isRevoked = state.status == LicenseStatus.REVOKED,
                            isVerification = state.status == LicenseStatus.VERIFICATION_REQUIRED,
                            trialUsed = state.trialUsed,
                            trialLimit = state.trialLimit
                        )

                        // 4. Mode Selector Tabs
                        CompactModeTabs(
                            selected = selectedMode,
                            onSelected = {
                                selectedMode = it
                                viewModel.clearMessage()
                            }
                        )

                        // 5. Mode Content
                        if (selectedMode == 0) {
                            UnifiedAccountLoginSection(
                                session = session,
                                activationCode = activationCode,
                                busy = busy,
                                onActivationChange = { activationCode = it },
                                onSignInGoogle = {
                                    googleSignInLauncher.launch(googleClient.signInIntent)
                                },
                                onSignOutGoogle = {
                                    viewModel.signOutUnified()
                                },
                                onActivate = {
                                    viewModel.activateWithCode(activationCode)
                                }
                            )
                        } else {
                            SignedTokenCompactSection(
                                token = signedToken,
                                busy = busy,
                                onTokenChange = { signedToken = it },
                                onActivate = {
                                    viewModel.applyToken(signedToken)
                                }
                            )
                        }

                        // 6. Compact Device Code Row
                        CompactDeviceCodeRow(
                            deviceCode = viewModel.deviceCode(),
                            onCopy = {
                                clipboard.setText(AnnotatedString(viewModel.deviceCode()))
                                Toast.makeText(context, "تم نسخ رمز الجهاز", Toast.LENGTH_SHORT).show()
                            }
                        )

                        // 7. WhatsApp Support Button
                        CompactWhatsAppButton(
                            context = context,
                            email = session.email,
                            accountCode = session.accountCode,
                            deviceCode = viewModel.deviceCode()
                        )
                    }

                    // 8. Progress Bar
                    if (busy) {
                        LinearProgressIndicator(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(3.dp)
                                .clip(RoundedCornerShape(2.dp)),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    // 9. Message / Error Banner
                    if (!message.isNullOrBlank()) {
                        CompactMessageBanner(
                            message = message!!,
                            isError = !state.isPaid
                        )
                    }

                    // 10. Bottom Action Button
                    if (state.isPaid) {
                        Button(
                            onClick = onDismiss,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(40.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Check, null, Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("إغلاق", fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(38.dp),
                            shape = RoundedCornerShape(11.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        ) {
                            Text("المتابعة لاحقًا", fontSize = 11.5.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CompactLicenseHeader(
    title: String,
    active: Boolean,
    onDismiss: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(
                        if (active) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                        else MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (active) Icons.Default.VerifiedUser else Icons.Default.VpnKey,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Column {
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (active) "الترخيص نشط ومُعتمد" else "حماية السجلات والبيانات",
                    fontSize = 10.sp,
                    color = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        IconButton(
            onClick = onDismiss,
            modifier = Modifier
                .size(30.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Icon(
                Icons.Default.Close,
                contentDescription = "إغلاق",
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun CompactStateBanner(
    isExpired: Boolean,
    isRevoked: Boolean,
    isVerification: Boolean,
    trialUsed: Int,
    trialLimit: Int
) {
    val isAlert = isExpired || isRevoked || isVerification
    val bgColor = if (isAlert) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.25f)
    else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
    val borderColor = if (isAlert) MaterialTheme.colorScheme.error.copy(alpha = 0.25f)
    else MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
    val textColor = if (isAlert) MaterialTheme.colorScheme.onErrorContainer
    else MaterialTheme.colorScheme.onPrimaryContainer

    val description = when {
        isExpired -> "انتهت المعاملات المجانية المتاحة ($trialUsed من $trialLimit). سجّل الدخول بحسابك أو أدخل رمز الترخيص للمتابعة."
        isRevoked -> "تعذر اعتماد الترخيص الحالي. يرجى تفعيل ترخيص صالح للمتابعة."
        isVerification -> "يلزم إعادة التحقق من الترخيص عبر الإنترنت لمتابعة العمليات."
        else -> "المتبقي من التجربة المجانية: ${trialLimit - trialUsed} معاملة."
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = bgColor,
        border = BorderStroke(0.8.dp, borderColor)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = if (isAlert) Icons.Default.Info else Icons.Default.CheckCircle,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = if (isAlert) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
            )
            Text(
                text = description,
                fontSize = 10.5.sp,
                lineHeight = 15.sp,
                fontWeight = FontWeight.Medium,
                color = textColor
            )
        }
    }
}

@Composable
private fun CompactModeTabs(
    selected: Int,
    onSelected: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
            .padding(3.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        CompactTabItem(
            modifier = Modifier.weight(1f),
            selected = selected == 0,
            icon = Icons.Default.AccountCircle,
            label = "تسجيل الدخول بالحساب",
            onClick = { onSelected(0) }
        )
        CompactTabItem(
            modifier = Modifier.weight(1f),
            selected = selected == 1,
            icon = Icons.Default.VpnKey,
            label = "رمز الترخيص المحلي",
            onClick = { onSelected(1) }
        )
    }
}

@Composable
private fun CompactTabItem(
    modifier: Modifier,
    selected: Boolean,
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        color = if (selected) MaterialTheme.colorScheme.surface else Color.Transparent,
        shadowElevation = if (selected) 1.dp else 0.dp
    ) {
        Row(
            modifier = Modifier.padding(vertical = 7.dp, horizontal = 4.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.width(5.dp))
            Text(
                text = label,
                fontSize = 10.5.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun UnifiedAccountLoginSection(
    session: UnifiedAccountSession,
    activationCode: String,
    busy: Boolean,
    onActivationChange: (String) -> Unit,
    onSignInGoogle: () -> Unit,
    onSignOutGoogle: () -> Unit,
    onActivate: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (!session.isSignedIn) {
            // Unconnected State
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "استخدم حساب Google الموحد للترخيص والنسخ الاحتياطي السحابي معاً.",
                        fontSize = 10.5.sp,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 15.sp
                    )

                    Button(
                        onClick = onSignInGoogle,
                        enabled = !busy,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(42.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(Icons.Default.AccountCircle, null, Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "تسجيل الدخول بحساب Google",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        } else {
            // Connected Google Account Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.06f),
                border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.AccountCircle,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        Column {
                            Text(
                                text = session.email ?: "حساب Google متصل",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "الحساب متصل وموحد",
                                fontSize = 9.5.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    TextButton(
                        onClick = onSignOutGoogle,
                        enabled = !busy,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            "تسجيل الخروج",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Activation code field for the connected account
            Text(
                text = "أدخل رمز التفعيل الممنوح لك لربط الترخيص بهذا الحساب:",
                fontSize = 10.5.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedTextField(
                value = activationCode,
                onValueChange = onActivationChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                singleLine = true,
                enabled = !busy,
                label = { Text("رمز التفعيل (من المطور)", fontSize = 10.sp) },
                placeholder = { Text("أدخل رمز التفعيل هنا...", fontSize = 9.5.sp) },
                shape = RoundedCornerShape(10.dp)
            )

            Button(
                onClick = onActivate,
                enabled = !busy && activationCode.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(38.dp),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.LockOpen, null, Modifier.size(15.dp))
                Spacer(Modifier.width(6.dp))
                Text("تفعيل الترخيص للحساب", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun SignedTokenCompactSection(
    token: String,
    busy: Boolean,
    onTokenChange: (String) -> Unit,
    onActivate: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        OutlinedTextField(
            value = token,
            onValueChange = onTokenChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(76.dp),
            minLines = 2,
            maxLines = 3,
            enabled = !busy,
            label = { Text("رمز الترخيص المحلي", fontSize = 10.sp) },
            placeholder = { Text("الصق رمز الترخيص الموقع هنا...", fontSize = 9.5.sp) },
            shape = RoundedCornerShape(10.dp)
        )

        Button(
            onClick = onActivate,
            enabled = !busy && token.isNotBlank(),
            modifier = Modifier
                .fillMaxWidth()
                .height(38.dp),
            shape = RoundedCornerShape(10.dp)
        ) {
            Icon(Icons.Default.VpnKey, null, Modifier.size(15.dp))
            Spacer(Modifier.width(6.dp))
            Text("تفعيل برمز الترخيص", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun CompactDeviceCodeRow(
    deviceCode: String,
    onCopy: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(9.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        border = BorderStroke(0.6.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.PhoneAndroid,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = "رمز الجهاز:",
                fontSize = 9.5.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = deviceCode,
                modifier = Modifier.weight(1f),
                fontSize = 10.5.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            IconButton(
                onClick = onCopy,
                modifier = Modifier.size(26.dp)
            ) {
                Icon(
                    Icons.Default.ContentCopy,
                    contentDescription = "نسخ",
                    modifier = Modifier.size(13.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun CompactWhatsAppButton(
    context: Context,
    email: String?,
    accountCode: String?,
    deviceCode: String
) {
    Button(
        onClick = {
            openDeveloperWhatsApp(context, email, accountCode, deviceCode)
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(36.dp),
        shape = RoundedCornerShape(9.dp),
        colors = ButtonDefaults.buttonColors(containerColor = WhatsAppGreen)
    ) {
        Icon(Icons.Default.Chat, null, Modifier.size(15.dp), tint = Color.White)
        Spacer(Modifier.width(6.dp))
        Text(
            text = "طلب ترخيص أو مساعدة عبر واتساب",
            fontSize = 10.5.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

@Composable
private fun ActiveLicenseCompactCard(
    session: UnifiedAccountSession,
    type: LicenseType?,
    onSignOut: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f))
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "الترخيص نشط ومُعتمد",
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                if (session.isSignedIn) {
                    TextButton(
                        onClick = onSignOut,
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            "تسجيل الخروج",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

            if (!session.email.isNullOrBlank()) {
                Text(
                    text = "الحساب: ${session.email}",
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Text(
                    text = "نوع الترخيص: ${if (type == LicenseType.LOCAL) "ترخيص محلي" else "ترخيص حساب"}",
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = "جميع العمليات غير محدودة ومحمية على هذا الجهاز.",
                fontSize = 9.5.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun CompactMessageBanner(
    message: String,
    isError: Boolean
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = if (isError) MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
        else MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
        border = BorderStroke(
            0.8.dp,
            if (isError) MaterialTheme.colorScheme.error.copy(alpha = 0.3f)
            else MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = if (isError) Icons.Default.ErrorOutline else Icons.Default.CheckCircle,
                contentDescription = null,
                modifier = Modifier.size(15.dp),
                tint = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
            )
            Text(
                text = message,
                fontSize = 10.5.sp,
                color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

private fun openDeveloperWhatsApp(
    context: Context,
    email: String?,
    accountCode: String?,
    deviceCode: String
) {
    val msg = buildString {
        append("مرحباً، أريد تفعيل ترخيص تطبيق الدفتر الذكي.\n")
        if (!email.isNullOrBlank()) {
            append("الحساب: ${email.trim()}\n")
        }
        if (!accountCode.isNullOrBlank() && accountCode != "غير مرتبط") {
            append("كود الحساب: ${accountCode.trim()}\n")
        }
        append("رمز الجهاز: $deviceCode")
    }
    val uri = Uri.parse("https://api.whatsapp.com/send?phone=$SUPPORT_WHATSAPP&text=${Uri.encode(msg)}")
    val intent = Intent(Intent.ACTION_VIEW, uri).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    runCatching { context.startActivity(intent) }
        .onFailure {
            Toast.makeText(context, "تعذر فتح واتساب على هذا الجهاز", Toast.LENGTH_SHORT).show()
        }
}
