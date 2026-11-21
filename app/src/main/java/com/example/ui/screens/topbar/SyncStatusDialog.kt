package com.example.ui.screens.topbar

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.core.localization.LocalAppStrings
import com.example.core.sync.SyncState
import com.example.core.sync.SyncStatusSnapshot
import com.example.data.local.entity.SyncOutboxEntity
import com.example.ui.theme.AmberContainer
import com.example.ui.theme.AmberSecondary
import com.example.ui.theme.NavyContainer
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.OnAmberContainer
import com.example.ui.theme.StatusDanger
import com.example.ui.theme.StatusDangerBg
import com.example.ui.theme.StatusInfo
import com.example.ui.theme.StatusInfoBg
import com.example.ui.theme.StatusSuccess
import com.example.ui.theme.StatusSuccessBg
import com.example.ui.theme.StatusWarningBg

@Composable
fun SyncStatusDialog(
    snapshot: SyncStatusSnapshot,
    outbox: List<SyncOutboxEntity>,
    onSyncNow: () -> Unit,
    onRetryFailed: () -> Unit,
    onClearSynced: () -> Unit,
    onDismiss: () -> Unit
) {
    val tb = LocalAppStrings.current.topBar

    val (stateColor, stateBg, stateIcon, stateTitle) = when (snapshot.syncState) {
        SyncState.ONLINE -> Quad(StatusSuccess, StatusSuccessBg, Icons.Default.CheckCircle, tb.syncOnline)
        SyncState.OFFLINE -> Quad(AmberSecondary, AmberContainer, Icons.Default.CloudOff, tb.syncOffline)
        SyncState.SYNCHRONIZING -> Quad(StatusInfo, StatusInfoBg, Icons.Default.CloudSync, tb.syncSynchronizing)
        SyncState.SYNC_ERROR -> Quad(StatusDanger, StatusDangerBg, Icons.Default.ErrorOutline, tb.syncError)
    }

    BtpCenterDialogShell(
        title = tb.syncStatusTitle,
        subtitle = tb.syncSubtitle,
        onDismiss = onDismiss
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = stateBg),
                border = BorderStroke(1.dp, stateColor.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(color = stateColor.copy(alpha = 0.16f), shape = CircleShape, modifier = Modifier.size(44.dp)) {
                        Box(contentAlignment = Alignment.Center) {
                            if (snapshot.syncState == SyncState.SYNCHRONIZING) {
                                CircularProgressIndicator(color = stateColor, strokeWidth = 2.5.dp, modifier = Modifier.size(22.dp))
                            } else {
                                Icon(stateIcon, contentDescription = null, tint = stateColor, modifier = Modifier.size(24.dp))
                            }
                        }
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(stateTitle, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = stateColor)
                        Text(
                            text = "${tb.syncLastSyncedLabel}: ${snapshot.lastSyncedAt?.let { formatTimestampShort(it) } ?: tb.syncNeverSynced}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (!snapshot.isNetworkAvailable) {
                            Text(
                                text = tb.syncOfflineQueueNotice,
                                style = MaterialTheme.typography.labelSmall,
                                color = OnAmberContainer,
                                fontWeight = FontWeight.Medium
                            )
                        } else if (snapshot.lastErrorMessage.isNotBlank()) {
                            Text(
                                text = snapshot.lastErrorMessage,
                                style = MaterialTheme.typography.labelSmall,
                                color = StatusDanger
                            )
                        }
                    }
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SyncMetricChip(label = tb.syncPendingLabel, value = snapshot.pendingCount, bg = StatusWarningBg, fg = AmberSecondary, modifier = Modifier.weight(1f))
                SyncMetricChip(label = tb.syncErrorCountLabel, value = snapshot.errorCount, bg = StatusDangerBg, fg = StatusDanger, modifier = Modifier.weight(1f))
                SyncMetricChip(label = tb.syncSyncedCountLabel, value = snapshot.syncedCount, bg = StatusSuccessBg, fg = StatusSuccess, modifier = Modifier.weight(1f))
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = if (snapshot.errorCount > 0) onRetryFailed else onSyncNow,
                    colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (snapshot.errorCount > 0) tb.syncRetryBtn else tb.syncNowBtn, fontWeight = FontWeight.Bold)
                }
                OutlinedButton(
                    onClick = onClearSynced,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.DeleteSweep, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(tb.syncClearSyncedBtn, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }

            Text(
                text = tb.syncQueueTitle,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )

            if (outbox.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(tb.syncQueueEmpty, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(bottom = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(outbox, key = { it.id }) { item ->
                        val (fg, bg) = when (item.status) {
                            "SYNCED" -> StatusSuccess to StatusSuccessBg
                            "ERROR" -> StatusDanger to StatusDangerBg
                            "SYNCING" -> StatusInfo to StatusInfoBg
                            else -> AmberSecondary to AmberContainer
                        }
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Surface(color = NavyContainer, shape = RoundedCornerShape(6.dp)) {
                                            Text(
                                                text = "${item.entityType} • ${item.operation}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = NavyPrimary,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                        Text(
                                            text = formatTimestampShort(item.createdAt),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Text(
                                        text = item.payloadSummary,
                                        style = MaterialTheme.typography.bodySmall,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    if (item.lastError.isNotBlank()) {
                                        Text(
                                            text = item.lastError,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = StatusDanger
                                        )
                                    }
                                }
                                Surface(color = bg, shape = RoundedCornerShape(50)) {
                                    Text(
                                        text = tb.syncOpLabel(item.status),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = fg,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SyncMetricChip(
    label: String,
    value: Int,
    bg: Color,
    fg: Color,
    modifier: Modifier = Modifier
) {
    Surface(color = bg, shape = RoundedCornerShape(12.dp), modifier = modifier) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "$value", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = fg)
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = fg, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
