package com.smartledger.aldaftar.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.smartledger.aldaftar.R
import com.smartledger.aldaftar.data.local.entities.AppSettings
import com.smartledger.aldaftar.domain.HashUtils
import com.smartledger.aldaftar.ui.components.MizanAnimatedDialog
import com.smartledger.aldaftar.ui.components.MizanDialogCard
import com.smartledger.aldaftar.ui.screens.security.components.SecurityActivePanel
import com.smartledger.aldaftar.ui.screens.security.components.SecuritySetupForm
import com.smartledger.aldaftar.ui.theme.MizanDialogTokens
import com.smartledger.aldaftar.ui.theme.mizanColors
import com.smartledger.aldaftar.ui.viewmodel.SecurityViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

private suspend fun buildSecuritySettings(
    passcode: String,
    recoveryPhrase: String,
    recoveryHint: String,
    currentSettings: AppSettings
): AppSettings = coroutineScope {
    // PBKDF2 remains intentionally strong; the two independent derivations run off the UI thread.
    val passcodeHash = async(Dispatchers.Default) { HashUtils.hashString(passcode) }
    val recoveryHash = async(Dispatchers.Default) { HashUtils.hashString(recoveryPhrase.trim()) }
    currentSettings.copy(
        isPasscodeEnabled = true,
        passcodeHash = passcodeHash.await(),
        recoveryPhraseHash = recoveryHash.await(),
        recoveryHint = recoveryHint.trim().takeIf { it.isNotBlank() }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecurityScreen(
    settings: AppSettings,
    viewModel: SecurityViewModel,
    onBack: () -> Unit,
    contentPadding: PaddingValues = PaddingValues()
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val mizanColors = MaterialTheme.mizanColors

    val currentSettings by viewModel.settingsState.collectAsStateWithLifecycle()
    val isAlreadyPasscodeEnabled = currentSettings.isPasscodeEnabled
    var isEditingPasscode by remember { mutableStateOf(false) }

    var passcode by remember { mutableStateOf("") }
    var confirmPasscode by remember { mutableStateOf("") }
    var recoveryPhrase by remember { mutableStateOf("") }
    var recoveryHint by remember { mutableStateOf("") }
    var checkAcknowledged by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            modifier = Modifier.size(32.dp),
                            shape = RoundedCornerShape(9.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                            border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f))
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (isAlreadyPasscodeEnabled) Icons.Default.VerifiedUser else Icons.Default.Shield,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Text(
                            text = stringResource(id = R.string.sec_title),
                            fontSize = 15.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = if (isAlreadyPasscodeEnabled) mizanColors.creditContainer else MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = stringResource(
                                    id = if (isAlreadyPasscodeEnabled) R.string.sec_status_active else R.string.sec_status_inactive
                                ),
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isAlreadyPasscodeEnabled) mizanColors.credit else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (isEditingPasscode && isAlreadyPasscodeEnabled) {
                            isEditingPasscode = false
                        } else {
                            onBack()
                        }
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(id = R.string.habayeb_back),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier.border(
                    width = 0.5.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            if (!isAlreadyPasscodeEnabled || isEditingPasscode) {
                SecuritySetupForm(
                    passcode = passcode,
                    onPasscodeChange = { passcode = it },
                    confirmPasscode = confirmPasscode,
                    onConfirmPasscodeChange = { confirmPasscode = it },
                    recoveryPhrase = recoveryPhrase,
                    onRecoveryPhraseChange = { recoveryPhrase = it },
                    recoveryHint = recoveryHint,
                    onRecoveryHintChange = { recoveryHint = it },
                    checkAcknowledged = checkAcknowledged,
                    onCheckAcknowledgedChange = { checkAcknowledged = it },
                    isSaving = isSaving,
                    isInDialog = false,
                    onCancelEdit = if (isEditingPasscode && isAlreadyPasscodeEnabled) {
                        { isEditingPasscode = false }
                    } else {
                        null
                    },
                    onSave = {
                        val isValid = passcode.length == 4 &&
                            confirmPasscode == passcode &&
                            recoveryPhrase.isNotBlank() &&
                            checkAcknowledged &&
                            !isSaving
                        if (isValid) {
                            isSaving = true
                            coroutineScope.launch {
                                try {
                                    val updated = buildSecuritySettings(passcode, recoveryPhrase, recoveryHint, currentSettings)
                                    viewModel.saveSettingsSync(updated)
                                    isSaving = false
                                    isEditingPasscode = false
                                    Toast.makeText(context, context.getString(R.string.sec_toast_enabled_success), Toast.LENGTH_SHORT).show()
                                    onBack()
                                } catch (e: Exception) {
                                    isSaving = false
                                    Toast.makeText(
                                        context,
                                        e.message ?: context.getString(R.string.toast_operation_failed),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }
                    }
                )
            } else {
                SecurityActivePanel(
                    currentSettings = currentSettings,
                    viewModel = viewModel,
                    isInDialog = false,
                    onChangePasscode = {
                        passcode = ""
                        confirmPasscode = ""
                        recoveryPhrase = ""
                        recoveryHint = currentSettings.recoveryHint ?: ""
                        checkAcknowledged = false
                        isEditingPasscode = true
                    },
                    onDeactivateSecurity = {
                        coroutineScope.launch {
                            try {
                                val updated = currentSettings.copy(
                                    isPasscodeEnabled = false,
                                    passcodeHash = null,
                                    recoveryPhraseHash = null,
                                    recoveryHint = null
                                )
                                viewModel.saveSettingsSync(updated)
                                isEditingPasscode = false
                                Toast.makeText(context, context.getString(R.string.sec_toast_disabled), Toast.LENGTH_SHORT).show()
                            } catch (e: Exception) {
                                Toast.makeText(
                                    context,
                                    e.message ?: context.getString(R.string.toast_operation_failed),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecurityDialog(
    settings: AppSettings,
    viewModel: SecurityViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val currentSettings by viewModel.settingsState.collectAsStateWithLifecycle()
    val isAlreadyPasscodeEnabled = currentSettings.isPasscodeEnabled
    var isEditingPasscodeInDialog by remember { mutableStateOf(false) }

    var passcode by remember { mutableStateOf("") }
    var confirmPasscode by remember { mutableStateOf("") }
    var recoveryPhrase by remember { mutableStateOf("") }
    var recoveryHint by remember { mutableStateOf("") }
    var checkAcknowledged by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }

    MizanAnimatedDialog(
        onDismissRequest = onDismiss
    ) { dismissDialog ->
        MizanDialogCard(
            maxWidth = MizanDialogTokens.maxWidth,
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp)
        ) {
            // Ultra-Compact Single-Line Bar (Eliminates redundant multi-line header & description)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = if (isAlreadyPasscodeEnabled && !isEditingPasscodeInDialog) {
                            Icons.Default.VerifiedUser
                        } else {
                            Icons.Default.Security
                        },
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(19.dp)
                    )
                    Text(
                        text = stringResource(id = R.string.sec_title),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                IconButton(
                    onClick = {
                        if (isEditingPasscodeInDialog && isAlreadyPasscodeEnabled) {
                            isEditingPasscodeInDialog = false
                        } else {
                            dismissDialog()
                        }
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(id = R.string.desc_close),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f),
                thickness = 0.6.dp
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (!isAlreadyPasscodeEnabled || isEditingPasscodeInDialog) {
                    SecuritySetupForm(
                        passcode = passcode,
                        onPasscodeChange = { passcode = it },
                        confirmPasscode = confirmPasscode,
                        onConfirmPasscodeChange = { confirmPasscode = it },
                        recoveryPhrase = recoveryPhrase,
                        onRecoveryPhraseChange = { recoveryPhrase = it },
                        recoveryHint = recoveryHint,
                        onRecoveryHintChange = { recoveryHint = it },
                        checkAcknowledged = checkAcknowledged,
                        onCheckAcknowledgedChange = { checkAcknowledged = it },
                        isSaving = isSaving,
                        isInDialog = true,
                        onCancelEdit = if (isEditingPasscodeInDialog && isAlreadyPasscodeEnabled) {
                            { isEditingPasscodeInDialog = false }
                        } else {
                            null
                        },
                        onSave = {
                            val isValid = passcode.length == 4 &&
                                confirmPasscode == passcode &&
                                recoveryPhrase.isNotBlank() &&
                                checkAcknowledged &&
                                !isSaving
                            if (isValid) {
                                isSaving = true
                                coroutineScope.launch {
                                    try {
                                        val updated = buildSecuritySettings(passcode, recoveryPhrase, recoveryHint, currentSettings)
                                        viewModel.saveSettingsSync(updated)
                                        isSaving = false
                                        isEditingPasscodeInDialog = false
                                        Toast.makeText(context, context.getString(R.string.sec_toast_enabled_success), Toast.LENGTH_SHORT).show()
                                        onDismiss()
                                    } catch (e: Exception) {
                                        isSaving = false
                                        Toast.makeText(
                                            context,
                                            e.message ?: context.getString(R.string.toast_operation_failed),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            }
                        }
                    )
                } else {
                    SecurityActivePanel(
                        currentSettings = currentSettings,
                        viewModel = viewModel,
                        isInDialog = true,
                        onChangePasscode = {
                            passcode = ""
                            confirmPasscode = ""
                            recoveryPhrase = ""
                            recoveryHint = currentSettings.recoveryHint ?: ""
                            checkAcknowledged = false
                            isEditingPasscodeInDialog = true
                        },
                        onDeactivateSecurity = {
                            coroutineScope.launch {
                                try {
                                    val updated = currentSettings.copy(
                                        isPasscodeEnabled = false,
                                        passcodeHash = null,
                                        recoveryPhraseHash = null,
                                        recoveryHint = null
                                    )
                                    viewModel.saveSettingsSync(updated)
                                    isEditingPasscodeInDialog = false
                                    Toast.makeText(context, context.getString(R.string.sec_toast_disabled), Toast.LENGTH_SHORT).show()
                                } catch (e: Exception) {
                                    Toast.makeText(
                                        context,
                                        e.message ?: context.getString(R.string.toast_operation_failed),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}
