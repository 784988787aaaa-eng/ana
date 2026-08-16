package com.example.ui.screens.ledger.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.R
import com.example.data.local.entities.FixedCommitment
import com.example.ui.theme.EmeraldPrimary
import java.math.BigDecimal

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

    val context = LocalContext.current
    val nameFocus = remember { FocusRequester() }
    val targetFocus = remember { FocusRequester() }
    val progressFocus = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    val view = androidx.compose.ui.platform.LocalView.current
    DisposableEffect(view) {
        val window = (view.parent as? androidx.compose.ui.window.DialogWindowProvider)?.window
        window?.setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
        onDispose {}
    }

    val keyboardController = androidx.compose.ui.platform.LocalSoftwareKeyboardController.current

    val initialName = editingCommitment?.name ?: ""
    val initialTarget = editingCommitment?.targetAmount?.let { if (it.compareTo(java.math.BigDecimal.ZERO) > 0) it.toInt().toString() else "" } ?: ""
    val initialProgress = editingCommitment?.currentProgress?.let { if (it.compareTo(java.math.BigDecimal.ZERO) > 0) it.toInt().toString() else "" } ?: ""

    var obligationNameTfv by remember(editingCommitment) {
        mutableStateOf(TextFieldValue(text = initialName, selection = TextRange(initialName.length)))
    }
    var targetAmtTfv by remember(editingCommitment) {
        mutableStateOf(TextFieldValue(text = initialTarget, selection = TextRange(initialTarget.length)))
    }
    var progressAmtTfv by remember(editingCommitment) {
        mutableStateOf(TextFieldValue(text = initialProgress, selection = TextRange(initialProgress.length)))
    }

    val obligationName = obligationNameTfv.text
    val targetAmtStr = targetAmtTfv.text
    val progressAmtStr = progressAmtTfv.text

    LaunchedEffect(Unit) {
        try {
            kotlinx.coroutines.delay(150)
            if (editingCommitment == null) {
                nameFocus.requestFocus()
            } else {
                targetFocus.requestFocus()
            }
            keyboardController?.show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = androidx.compose.ui.window.DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = true
        )
    ) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Surface(
                modifier = modifier
                    .widthIn(max = 320.dp)
                    .fillMaxWidth(0.84f)
                    .clip(RoundedCornerShape(20.dp)),
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .imePadding()
                        .verticalScroll(rememberScrollState())
                        .padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Sleek, minimal and creative header
                    Text(
                        text = if (editingCommitment != null) stringResource(id = R.string.ledger_edit_commitment_title) else stringResource(id = R.string.ledger_add_commitment_title),
                        fontWeight = FontWeight.ExtraBold,
                        color = EmeraldPrimary,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Input fields - Name
                    OutlinedTextField(
                        value = obligationNameTfv,
                        onValueChange = { if (editingCommitment == null) obligationNameTfv = it },
                        enabled = (editingCommitment == null),
                        label = { Text(stringResource(id = R.string.ledger_commitment_name_label), fontSize = 10.sp) },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EmeraldPrimary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.25f),
                            disabledBorderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.12f),
                            focusedLabelColor = EmeraldPrimary,
                            unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            disabledTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(nameFocus),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { targetFocus.requestFocus() }),
                        textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Right, fontSize = 12.sp)
                    )

                    // Numeric fields side by side to save height and look ultra-modern
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            OutlinedTextField(
                                value = targetAmtTfv,
                                onValueChange = { targetAmtTfv = it },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                                keyboardActions = KeyboardActions(onNext = { progressFocus.requestFocus() }),
                                label = { Text(stringResource(id = R.string.ledger_commitment_target_amount_label), fontSize = 10.sp) },
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = EmeraldPrimary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.25f),
                                    focusedLabelColor = EmeraldPrimary,
                                    unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .focusRequester(targetFocus),
                                textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center, fontSize = 12.sp)
                            )
                        }

                        Box(modifier = Modifier.weight(1f)) {
                            OutlinedTextField(
                                value = progressAmtTfv,
                                onValueChange = { progressAmtTfv = it },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                                label = { Text(stringResource(id = R.string.ledger_commitment_current_progress_label), fontSize = 10.sp) },
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = EmeraldPrimary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.25f),
                                    focusedLabelColor = EmeraldPrimary,
                                    unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .focusRequester(progressFocus),
                                textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center, fontSize = 12.sp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    // Highly polished Cancel/Save action buttons (balanced and compact)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = onDismissRequest,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurfaceVariant),
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp),
                            contentPadding = PaddingValues(vertical = 0.dp)
                        ) {
                            Text(
                                text = stringResource(id = R.string.common_cancel),
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.sp
                            )
                        }

                        Button(
                            onClick = {
                                val tar = targetAmtStr.toBigDecimalOrNull() ?: BigDecimal.ZERO
                                val prg = progressAmtStr.toBigDecimalOrNull() ?: BigDecimal.ZERO
                                if (obligationName.isNotBlank() && tar > BigDecimal.ZERO) {
                                    com.example.ui.helper.VibrationHelper.triggerSuccessVibration(context)
                                    onSaveCommitment(obligationName, tar, prg)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1.2f)
                                .height(38.dp),
                            contentPadding = PaddingValues(vertical = 0.dp)
                        ) {
                            Text(
                                text = stringResource(id = R.string.ledger_save_commitment_btn),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
