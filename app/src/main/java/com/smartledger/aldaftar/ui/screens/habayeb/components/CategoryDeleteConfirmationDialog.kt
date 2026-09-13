package com.smartledger.aldaftar.ui.screens.habayeb.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.smartledger.aldaftar.R
import com.smartledger.aldaftar.ui.theme.MizanTouchTarget

@Composable
fun CategoryDeleteConfirmationDialog(
    categoryName: String,
    activeThemeColor: Color,
    onDismiss: () -> Unit,
    onConfirmDelete: (deleteLinkedAccounts: Boolean) -> Unit
) {
    val errorColor = MaterialTheme.colorScheme.error
    val buttonShape = remember { RoundedCornerShape(12.dp) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            modifier = Modifier
                .fillMaxWidth(0.90f)
                .widthIn(max = 340.dp)
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.habayeb_category_delete_confirm, categoryName),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(2.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = activeThemeColor,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    shape = buttonShape,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(MizanTouchTarget.standardButtonHeight)
                ) {
                    Text(
                        text = stringResource(R.string.habayeb_category_delete_cancel),
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                OutlinedButton(
                    onClick = {
                        onConfirmDelete(false)
                        onDismiss()
                    },
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    shape = buttonShape,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(MizanTouchTarget.standardButtonHeight)
                ) {
                    Text(
                        text = stringResource(R.string.habayeb_category_delete_only),
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                OutlinedButton(
                    onClick = {
                        onConfirmDelete(true)
                        onDismiss()
                    },
                    border = BorderStroke(1.dp, errorColor.copy(alpha = 0.5f)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = errorColor
                    ),
                    shape = buttonShape,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(MizanTouchTarget.standardButtonHeight)
                ) {
                    Text(
                        text = stringResource(R.string.habayeb_category_delete_all_accounts),
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
