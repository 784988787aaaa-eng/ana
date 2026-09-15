package com.smartledger.aldaftar.ui.screens.ledger.components

import android.content.Context
import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartledger.aldaftar.R
import com.smartledger.aldaftar.data.local.entities.FixedCommitment
import com.smartledger.aldaftar.ui.components.MizanAnimatedDialog
import com.smartledger.aldaftar.ui.components.MizanDialogActions
import com.smartledger.aldaftar.ui.components.MizanDialogCard
import com.smartledger.aldaftar.ui.components.MizanDialogHeader
import com.smartledger.aldaftar.ui.components.requestFocusAndShowKeyboard
import com.smartledger.aldaftar.ui.theme.MizanDialogTokens
import com.smartledger.aldaftar.ui.viewmodel.FinanceViewModel
import com.smartledger.aldaftar.ui.viewmodel.ledger.MonthLedger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

private const val TAG = "MainLedgerDialogs"

@Composable
fun DeleteDaysConfirmDialog(
    showDeleteDaysDialog: Boolean,
    onDismiss: () -> Unit,
    monthlyLedger: List<MonthLedger>,
    selectedDayKeys: MutableList<String>,
    viewModel: FinanceViewModel,
    scope: CoroutineScope,
    context: Context,
    onSuccess: () -> Unit
) {
    if (showDeleteDaysDialog) {
        MizanAnimatedDialog(
            onDismissRequest = onDismiss
        ) { dismiss ->
            MizanDialogCard(
                maxWidth = MizanDialogTokens.compactMaxWidth,
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp)
            ) {
                MizanDialogHeader(
                    title = stringResource(id = R.string.ledger_bulk_delete_days_title),
                    icon = Icons.Default.Delete,
                    iconTint = MaterialTheme.colorScheme.error,
                    onCloseClick = dismiss
                )

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.ledger_bulk_delete_days_msg),
                        fontSize = 12.5.sp,
                        lineHeight = 18.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    MizanDialogActions(
                        confirmText = stringResource(id = R.string.ledger_bulk_delete_days_confirm_btn),
                        onConfirm = {
                            dismiss()
                            scope.launch {
                                val txsToDelete = mutableListOf<String>()
                                monthlyLedger.forEach { ml ->
                                    ml.days.forEach { day ->
                                        val dayKey = "${ml.monthKey}_${day.dayNumber}"
                                        if (selectedDayKeys.contains(dayKey)) {
                                            day.transactions.forEach { tx ->
                                                txsToDelete.add(tx.id)
                                            }
                                        }
                                    }
                                }
                                viewModel.deleteTransactionsBulk(txsToDelete, context.getString(R.string.ledger_bulk_delete_days_desc))
                                onSuccess()
                            }
                        },
                        confirmColor = MaterialTheme.colorScheme.error,
                        cancelText = stringResource(id = R.string.common_cancel),
                        onCancel = dismiss
                    )
                }
            }
        }
    }
}

@Composable
fun ReorderCommitmentDialog(
    reorderCommitmentTarget: FixedCommitment?,
    commitmentsSize: Int,
    onDismiss: () -> Unit,
    onApplyReorder: (FixedCommitment, Int) -> Unit,
    context: Context
) {
    if (reorderCommitmentTarget != null) {
        var targetPositionStr by remember { mutableStateOf("") }
        var errorMsg by remember { mutableStateOf("") }
        val focusRequester = remember { FocusRequester() }
        val keyboardController = androidx.compose.ui.platform.LocalSoftwareKeyboardController.current

        val view = androidx.compose.ui.platform.LocalView.current
        DisposableEffect(view) {
            val window = (view.parent as? androidx.compose.ui.window.DialogWindowProvider)?.window
            window?.setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE or android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
            onDispose {}
        }

        LaunchedEffect(Unit) {
            try {
                requestFocusAndShowKeyboard(
                    focusRequester = focusRequester,
                    keyboardController = keyboardController
                )
            } catch (e: Exception) {
                Log.w(TAG, "Failed to request focus or show keyboard: ${e.message}")
            }
        }

        val applyAction = {
            val pos = targetPositionStr.toIntOrNull()
            if (pos == null || pos < 1 || pos > commitmentsSize) {
                errorMsg = context.getString(R.string.ledger_reorder_input_range_error, commitmentsSize)
            } else {
                onApplyReorder(reorderCommitmentTarget, pos)
            }
        }

        MizanAnimatedDialog(
            onDismissRequest = onDismiss
        ) { dismiss ->
            MizanDialogCard(
                maxWidth = MizanDialogTokens.compactMaxWidth,
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp)
            ) {
                MizanDialogHeader(
                    title = stringResource(id = R.string.ledger_reorder_dialog_title),
                    icon = Icons.Default.FormatListNumbered,
                    iconTint = MaterialTheme.colorScheme.primary,
                    onCloseClick = dismiss
                )

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.ledger_reorder_dialog_prompt, reorderCommitmentTarget.name),
                        fontSize = 12.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    OutlinedTextField(
                        value = targetPositionStr,
                        onValueChange = {
                            targetPositionStr = it
                            errorMsg = ""
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(onDone = { keyboardController?.hide() }),
                        singleLine = true,
                        shape = MizanDialogTokens.inputShape,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f),
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent
                        ),
                        modifier = Modifier
                            .width(88.dp)
                            .height(MizanDialogTokens.inputHeight)
                            .focusRequester(focusRequester),
                        textStyle = TextStyle(
                            textAlign = TextAlign.Center,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        isError = errorMsg.isNotEmpty()
                    )

                    if (errorMsg.isNotEmpty()) {
                        Text(
                            text = errorMsg,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center
                        )
                    } else {
                        Text(
                            text = "(من 1 إلى $commitmentsSize)",
                            fontSize = 11.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    MizanDialogActions(
                        confirmText = stringResource(id = R.string.ledger_reorder_apply),
                        onConfirm = { applyAction() },
                        cancelText = stringResource(id = R.string.common_cancel),
                        onCancel = dismiss
                    )
                }
            }
        }
    }
}
