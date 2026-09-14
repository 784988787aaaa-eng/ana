package com.smartledger.aldaftar.ui.screens.ledger.components

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.DialogWindowProvider
import com.smartledger.aldaftar.R
import com.smartledger.aldaftar.data.local.entities.FixedCommitment
import com.smartledger.aldaftar.ui.theme.MizanTouchTarget
import java.math.BigDecimal
import com.smartledger.aldaftar.ui.theme.MizanDialogTokens

private const val TAG = "CommitmentEditDialog"

@Composable
fun CommitmentEditDialog(
    showCommitmentDialog: Boolean,
    editingCommitment: FixedCommitment?,
    onDismissRequest: () -> Unit,
    onSaveCommitment: (name: String, targetAmount: BigDecimal, currentProgress: BigDecimal) -> Unit,
    onDeleteCommitment: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (!showCommitmentDialog) return

    val amountFocus = remember { FocusRequester() }
    val nameFocus = remember { FocusRequester() }
    val progressFocus = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    val initialName = editingCommitment?.name ?: ""
    val initialTarget = editingCommitment?.targetAmount?.let {
        if (it > BigDecimal.ZERO) it.stripTrailingZeros().toPlainString() else ""
    } ?: ""
    val initialProgress = editingCommitment?.currentProgress?.let {
        if (it > BigDecimal.ZERO) it.stripTrailingZeros().toPlainString() else ""
    } ?: ""

    var name by remember(editingCommitment) { mutableStateOf(TextFieldValue(initialName, TextRange(initialName.length))) }
    var target by remember(editingCommitment) { mutableStateOf(TextFieldValue(initialTarget, TextRange(initialTarget.length))) }
    var progress by remember(editingCommitment) { mutableStateOf(TextFieldValue(initialProgress, TextRange(initialProgress.length))) }

    val targetValue = target.text.toBigDecimalOrNull() ?: BigDecimal.ZERO
    val progressValue = progress.text.toBigDecimalOrNull() ?: BigDecimal.ZERO
    val valid = (editingCommitment != null || name.text.isNotBlank()) &&
            targetValue > BigDecimal.ZERO &&
            progressValue >= BigDecimal.ZERO &&
            progressValue <= targetValue

    val view = androidx.compose.ui.platform.LocalView.current
    DisposableEffect(Unit) {
        val window = (view.parent as? DialogWindowProvider)?.window
        window?.setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
        onDispose {}
    }

    LaunchedEffect(editingCommitment) {
        try {
            kotlinx.coroutines.android.awaitFrame()
            amountFocus.requestFocus()
            keyboardController?.show()
        } catch (e: Exception) { Log.w(TAG, "Focus failed: ${e.message}") }
    }

    com.smartledger.aldaftar.ui.components.MizanAnimatedDialog(
        onDismissRequest = onDismissRequest
    ) { dismiss ->
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Surface(
                modifier = modifier.fillMaxWidth(0.92f).widthIn(max = 370.dp).clip(MizanDialogTokens.shape),
                shape = MizanDialogTokens.shape,
                shadowElevation = 10.dp,
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = .35f))
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().navigationBarsPadding().imePadding()
                        .padding(horizontal = MizanDialogTokens.outerPadding, vertical = MizanDialogTokens.compactPadding),
                    verticalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                if (editingCommitment != null) stringResource(R.string.ledger_commitment_dialog_title_edit)
                                else stringResource(R.string.ledger_commitment_dialog_title_add),
                                fontSize = 16.sp, fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                if (editingCommitment != null) stringResource(R.string.ledger_commitment_edit_hint)
                                else stringResource(R.string.ledger_commitment_add_hint),
                                fontSize = 10.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        TextButton(onClick = dismiss, contentPadding = PaddingValues(horizontal = 8.dp)) {
                            Text(stringResource(R.string.common_cancel), fontSize = 12.sp)
                        }
                    }

                    CompactCommitmentField(
                        value = target, onValueChange = { target = it },
                        placeholder = stringResource(R.string.ledger_commitment_target_amount_label),
                        keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next,
                        focusRequester = amountFocus,
                        onNext = {
                            if (editingCommitment == null) nameFocus.requestFocus()
                            else progressFocus.requestFocus()
                        },
                        textAlign = TextAlign.Center
                    )

                    CompactCommitmentField(
                        value = name, onValueChange = { if (editingCommitment == null) name = it },
                        placeholder = stringResource(R.string.ledger_commitment_name_label),
                        keyboardType = KeyboardType.Text, imeAction = ImeAction.Next,
                        focusRequester = nameFocus, enabled = editingCommitment == null,
                        onNext = { progressFocus.requestFocus() }
                    )

                    CompactCommitmentField(
                        value = progress, onValueChange = { progress = it },
                        placeholder = stringResource(R.string.ledger_commitment_current_progress_label),
                        keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done,
                        focusRequester = progressFocus,
                        onNext = { focusManager.clearFocus() },
                        textAlign = TextAlign.Center
                    )

                    if (progressValue > targetValue && targetValue > BigDecimal.ZERO) {
                        Text(stringResource(R.string.ledger_commitment_progress_exceeds_target), fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.error, modifier = Modifier.fillMaxWidth())
                    }

                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = dismiss,
                            shape = MizanDialogTokens.buttonShape,
                            modifier = Modifier.weight(1f).height(44.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) { Text(stringResource(R.string.common_cancel), fontSize = 12.sp, fontWeight = FontWeight.SemiBold) }

                        Button(
                            onClick = {
                                if (valid) {
                                    onSaveCommitment(name.text.trim(), targetValue, progressValue)
                                }
                            },
                            enabled = valid,
                            shape = MizanDialogTokens.buttonShape,
                            modifier = Modifier.weight(1.35f).height(44.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(
                                if (editingCommitment != null) stringResource(R.string.ledger_commitment_dialog_save_edit)
                                else stringResource(R.string.ledger_commitment_dialog_save_goal),
                                fontSize = 12.5.sp, fontWeight = FontWeight.Bold
                            )
                        }
                    }

                }
            }
        }
    }
}

@Composable
private fun CompactCommitmentField(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    placeholder: String,
    keyboardType: KeyboardType,
    imeAction: ImeAction,
    focusRequester: FocusRequester,
    onNext: () -> Unit,
    textAlign: TextAlign = TextAlign.Start,
    enabled: Boolean = true
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        enabled = enabled,
        singleLine = true,
        placeholder = { Text(placeholder, fontSize = 11.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = .68f)) },
        shape = MizanDialogTokens.buttonShape,
        modifier = Modifier.fillMaxWidth().height(MizanDialogTokens.inputHeight).focusRequester(focusRequester),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = imeAction),
        keyboardActions = KeyboardActions(onNext = { onNext() }, onDone = { onNext() }),
        textStyle = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Medium, textAlign = textAlign),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = .42f),
            disabledBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = .28f),
            focusedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = .035f),
            unfocusedContainerColor = Color.Transparent,
            disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = .025f)
        )
    )
}
