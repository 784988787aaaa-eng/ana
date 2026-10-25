package com.smartledger.aldaftar.ui.screens.habayeb.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartledger.aldaftar.R
import com.smartledger.aldaftar.ui.components.MizanAnimatedDialog
import com.smartledger.aldaftar.ui.components.MizanDialogActions
import com.smartledger.aldaftar.ui.components.MizanDialogCard
import com.smartledger.aldaftar.ui.components.MizanDialogHeader
import com.smartledger.aldaftar.ui.theme.MizanDialogTokens
import com.smartledger.aldaftar.ui.theme.mizanColors
import com.smartledger.aldaftar.ui.viewmodel.FinanceConstants

@Composable
fun CustomerTypeChangeSection(
    currentType: String,
    activeThemeColor: Color,
    onUpdateCustomerType: (String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var pendingTypeChange by remember { mutableStateOf<String?>(null) }
    val mizanColors = MaterialTheme.mizanColors

    val activeRedColor = mizanColors.debt
    val activeGreenColor = mizanColors.credit
    val inactiveBgColor = MaterialTheme.colorScheme.surface
    val inactiveTextColor = MaterialTheme.colorScheme.onSurfaceVariant

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
            .padding(vertical = 8.dp, horizontal = 10.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = stringResource(id = R.string.context_menu_change_type_label),
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val isToThemSelected = currentType == FinanceConstants.TYPE_OWED_TO_THEM
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(34.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isToThemSelected) activeGreenColor else inactiveBgColor)
                        .border(
                            width = 1.dp,
                            color = if (isToThemSelected) activeGreenColor else MaterialTheme.colorScheme.outlineVariant,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickable {
                            if (!isToThemSelected) {
                                pendingTypeChange = FinanceConstants.TYPE_OWED_TO_THEM
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(id = R.string.habayeb_to_them),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isToThemSelected) mizanColors.onCredit else inactiveTextColor
                    )
                }

                val isByThemSelected = currentType == FinanceConstants.TYPE_OWED_BY_THEM
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(34.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isByThemSelected) activeRedColor else inactiveBgColor)
                        .border(
                            width = 1.dp,
                            color = if (isByThemSelected) activeRedColor else MaterialTheme.colorScheme.outlineVariant,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickable {
                            if (!isByThemSelected) {
                                pendingTypeChange = FinanceConstants.TYPE_OWED_BY_THEM
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(id = R.string.habayeb_owed),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isByThemSelected) mizanColors.onDebt else inactiveTextColor
                    )
                }
            }
        }
    }

    if (pendingTypeChange != null) {
        val confirmButtonColor = when (pendingTypeChange) {
            FinanceConstants.TYPE_OWED_TO_THEM -> mizanColors.credit
            FinanceConstants.TYPE_OWED_BY_THEM -> mizanColors.debt
            else -> activeThemeColor
        }
        MizanAnimatedDialog(
            onDismissRequest = { pendingTypeChange = null }
        ) { dismissConfirm ->
            MizanDialogCard(
                maxWidth = MizanDialogTokens.compactMaxWidth,
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp)
            ) {
                MizanDialogHeader(
                    title = stringResource(id = R.string.context_menu_change_type_confirm_title),
                    icon = Icons.Default.SwapHoriz,
                    iconTint = confirmButtonColor,
                    onCloseClick = dismissConfirm
                )

                Text(
                    text = stringResource(id = R.string.context_menu_change_type_confirm_msg),
                    fontSize = 13.sp,
                    lineHeight = 19.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                MizanDialogActions(
                    confirmText = stringResource(id = R.string.context_menu_confirm),
                    confirmColor = confirmButtonColor,
                    onConfirm = {
                        pendingTypeChange?.let { newType ->
                            onUpdateCustomerType(newType)
                        }
                        pendingTypeChange = null
                        onDismiss()
                    },
                    cancelText = stringResource(id = R.string.context_menu_cancel),
                    onCancel = dismissConfirm
                )
            }
        }
    }
}
