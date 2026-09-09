package com.smartledger.aldaftar.ui.screens.license

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.smartledger.aldaftar.ui.viewmodel.LicenseViewModel

@Composable
fun LicenseDialog(viewModel: LicenseViewModel, onDismiss: () -> Unit) {
    val state by viewModel.snapshot.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()
    val busy by viewModel.isBusy.collectAsStateWithLifecycle()
    var token by remember { mutableStateOf("") }
    var accountCode by remember { mutableStateOf("") }
    var activationCode by remember { mutableStateOf("") }
    val support = remember { viewModel.supportCodes() }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("الترخيص") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(when {
                    state.isPaid && state.type?.name == "LOCAL" -> "✓ ترخيص محلي مفعل"
                    state.isPaid -> "✓ ترخيص حساب مفعل"
                    state.status.name == "VERIFICATION_REQUIRED" -> "يلزم التحقق من الاتصال"
                    else -> "التجربة: ${state.trialUsed}/${state.trialLimit}"
                })
                if (!state.isPaid) {
                    Text("رمز الجهاز: ${viewModel.deviceCode()}")
                    OutlinedTextField(value = accountCode, onValueChange = { accountCode = it.uppercase() }, singleLine = true, modifier = Modifier.fillMaxWidth(), label = { Text("كود الحساب") }, enabled = !busy)
                    OutlinedTextField(value = activationCode, onValueChange = { activationCode = it }, singleLine = true, modifier = Modifier.fillMaxWidth(), label = { Text("رمز التفعيل") }, enabled = !busy)
                    Button(onClick = { viewModel.activateAccount(accountCode, activationCode) }, enabled = !busy && accountCode.isNotBlank() && activationCode.isNotBlank(), modifier = Modifier.fillMaxWidth()) { Text("تفعيل عبر السحابة") }
                    OutlinedButton(onClick = { viewModel.reconnectAccount(accountCode) }, enabled = !busy && accountCode.isNotBlank() && activationCode.isBlank(), modifier = Modifier.fillMaxWidth()) { Text("إعادة ربط هذا الجهاز") }
                    HorizontalDivider()
                    OutlinedTextField(value = token, onValueChange = { token = it }, modifier = Modifier.fillMaxWidth(), minLines = 3, label = { Text("أو رمز ترخيص موقع") }, enabled = !busy)
                    Button(onClick = { viewModel.applyToken(token) }, enabled = !busy && token.isNotBlank(), modifier = Modifier.fillMaxWidth()) { Text("تفعيل الرمز") }
                    if (busy) LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    Text("للدعم: ${support.first} · ${support.second}", style = MaterialTheme.typography.bodySmall)
                }
                if (state.isPaid && state.type?.name == "ACCOUNT") {
                    TextButton(onClick = { viewModel.signOutAccount() }) { Text("فصل حساب الترخيص من هذا الجهاز") }
                }
                if (message != null) Text(message!!, color = MaterialTheme.colorScheme.primary)
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("إغلاق") } }
    )
}
