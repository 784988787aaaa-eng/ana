package com.smartledger.aldaftar.ui.screens.settings.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.smartledger.aldaftar.R
import com.smartledger.aldaftar.data.backup.LegacyBackupCandidate

/**
 * يعرض النسخ التي اكتشفها منتقي المستندات ويمنع بدء الاستعادة دون اختيار صريح.
 */
@Composable
fun LegacyBackupCandidatesDialog(
    candidates: List<LegacyBackupCandidate>,
    onSelect: (LegacyBackupCandidate) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.backup_legacy_candidates_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(R.string.backup_legacy_candidates_desc),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(candidates, key = { it.uri.toString() }) { candidate ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = candidate.displayName,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = candidate.sizeBytes?.let { stringResource(R.string.backup_file_size_bytes, it) }
                                        ?: stringResource(R.string.backup_file_size_unknown),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            Button(onClick = { onSelect(candidate) }) {
                                Text(stringResource(R.string.backup_select_file))
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {}
    )
}
