package com.smartledger.aldaftar.ui.screens.ledger.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartledger.aldaftar.R
import com.smartledger.aldaftar.ui.theme.MizanIconSizes

@Composable
fun CommitmentHeaderClean(onCloseClick: () -> Unit, onShareClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().height(42.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onCloseClick, modifier = Modifier.size(38.dp)) {
            Box(Modifier.size(30.dp).clip(CircleShape).background(MaterialTheme.colorScheme.onSurface.copy(alpha=.055f)),
                contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Close, stringResource(R.string.report_btn_close),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(17.dp))
            }
        }
        Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(stringResource(R.string.ledger_commitments_dialog_title), fontSize = 15.sp,
                fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Text(stringResource(R.string.ledger_commitments_header_subtitle), fontSize = 9.5.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        IconButton(onClick = onShareClick, modifier = Modifier.size(38.dp)) {
            Box(Modifier.size(30.dp).clip(CircleShape).background(MaterialTheme.colorScheme.onSurface.copy(alpha=.055f)),
                contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Share, stringResource(R.string.ledger_whatsapp_whatsapp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(17.dp))
            }
        }
    }
}
