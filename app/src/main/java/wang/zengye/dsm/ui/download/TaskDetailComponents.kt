package wang.zengye.dsm.ui.download

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import wang.zengye.dsm.R
import wang.zengye.dsm.ui.components.EmptyState
import wang.zengye.dsm.ui.components.LoadingState
import wang.zengye.dsm.util.appString
import wang.zengye.dsm.util.formatSize
import java.text.SimpleDateFormat
import java.util.*

@Composable
internal fun GeneralTab(task: DownloadTaskDetail, context: Context) {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            DetailCard(stringResource(R.string.download_filename), task.title)
        }
        item {
            DetailCard(stringResource(R.string.download_save_location), task.destination)
        }
        item {
            DetailCard(stringResource(R.string.download_file_size), if (task.size > 0) formatSize(task.size) else stringResource(R.string.common_unknown))
        }
        item {
            DetailCard(stringResource(R.string.download_username), task.username)
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                DetailCard(stringResource(R.string.download_url), task.uri, modifier = Modifier.weight(1f))
                IconButton(onClick = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("uri", task.uri)
                    clipboard.setPrimaryClip(clip)
                    Toast.makeText(context, context.getString(R.string.download_copied), Toast.LENGTH_SHORT).show()
                }) {
                    Icon(Icons.Default.ContentCopy, contentDescription = stringResource(R.string.common_copy))
                }
            }
        }
        item {
            DetailCard(stringResource(R.string.download_created_time), if (task.createdTime > 0) dateFormat.format(Date(task.createdTime * 1000)) else stringResource(R.string.common_unknown))
        }
        item {
            DetailCard(stringResource(R.string.download_completed_time), if (task.completedTime > 0) dateFormat.format(Date(task.completedTime * 1000)) else stringResource(R.string.common_unknown))
        }
        item {
            DetailCard(stringResource(R.string.download_estimated_wait), if (task.waitingSeconds > 0) formatTimeRemaining(task.waitingSeconds) else stringResource(R.string.common_unknown))
        }
    }
}

@Composable
internal fun TransferTab(task: DownloadTaskDetail) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(stringResource(R.string.download_status), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = getStatusText(task.status),
                        style = MaterialTheme.typography.bodyLarge,
                        color = getStatusColor(task.status)
                    )
                }
            }
        }
        item {
            // 进度条
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(stringResource(R.string.download_progress), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            "${(task.progress * 100).toInt()}%",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { task.progress },
                        modifier = Modifier.fillMaxWidth(),
                    strokeCap = StrokeCap.Butt,
                    gapSize = 0.dp,
                    drawStopIndicator = {})
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "${formatSize(task.sizeDownloaded)} / ${if (task.size > 0) formatSize(task.size) else stringResource(R.string.common_unknown)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Card(modifier = Modifier.weight(1f)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(stringResource(R.string.download_speed), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "${formatSize(task.speedDownload)}/s",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Card(modifier = Modifier.weight(1f)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(stringResource(R.string.download_upload_speed), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "${formatSize(task.speedUpload)}/s",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun TrackersTab(
    trackers: List<TrackerItem>,
    isLoading: Boolean,
    onNavigateToTrackerManager: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        SmallFloatingActionButton(
            onClick = onNavigateToTrackerManager,
            modifier = Modifier.padding(16.dp).align(Alignment.End)
        ) {
            Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.download_manage_tracker))
        }

        if (isLoading) {
            LoadingState(message = stringResource(R.string.common_loading))
        } else if (trackers.isEmpty()) {
            EmptyState(message = stringResource(R.string.download_no_tracker))
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(trackers, key = { it.url }) { tracker ->
                    TrackerItemCard(tracker)
                }
            }
        }
    }
}

@Composable
internal fun PeersTab(
    peers: List<PeerItem>,
    isLoading: Boolean,
    onNavigateToPeerList: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        if (isLoading) {
            LoadingState(message = stringResource(R.string.common_loading))
        } else if (peers.isEmpty()) {
            EmptyState(message = stringResource(R.string.download_no_peer))
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(stringResource(R.string.download_peer_count, peers.size))
                        TextButton(onClick = onNavigateToPeerList) {
                            Text(stringResource(R.string.download_view_details))
                        }
                    }
                }
                items(peers, key = { "${it.ip}:${it.port}" }) { peer ->
                    PeerItemCard(peer)
                }
            }
        }
    }
}

@Composable
internal fun FilesTab(
    files: List<TaskFileItem>,
    isLoading: Boolean
) {
    if (isLoading) {
        LoadingState(message = stringResource(R.string.common_loading))
    } else if (files.isEmpty()) {
        EmptyState(message = stringResource(R.string.download_no_files))
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(files, key = { it.index }) { file ->
                FileItemCard(file)
            }
        }
    }
}

@Composable
internal fun DetailCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.bodyMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
internal fun TrackerItemCard(tracker: TrackerItem) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                tracker.url,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    stringResource(R.string.download_seeds, if (tracker.seeds >= 0) tracker.seeds.toString() else "-"),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    stringResource(R.string.download_peer_label, if (tracker.peers >= 0) tracker.peers.toString() else "-"),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
                Text(
                    tracker.status,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (tracker.status == "Success") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
internal fun PeerItemCard(peer: PeerItem) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(peer.ip, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                Text(peer.client, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(stringResource(R.string.download_peer_progress, (peer.progress * 100).toInt()), style = MaterialTheme.typography.bodySmall)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.ArrowDownward, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                    Text(formatSize(peer.speedDownload) + "/s", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.ArrowUpward, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.secondary)
                    Text(formatSize(peer.speedUpload) + "/s", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
                }
            }
        }
    }
}

@Composable
internal fun FileItemCard(file: TaskFileItem) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                file.fileName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "${formatSize(file.sizeDownloaded)} / ${formatSize(file.size)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (file.size > 0) {
                    Text(
                        "${(file.sizeDownloaded * 100 / file.size)}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Surface(
                    color = when (file.priority) {
                        "high" -> MaterialTheme.colorScheme.primaryContainer
                        "low" -> MaterialTheme.colorScheme.surfaceVariant
                        else -> MaterialTheme.colorScheme.secondaryContainer
                    },
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        when (file.priority) {
                            "high" -> stringResource(R.string.download_priority_high)
                            "low" -> stringResource(R.string.download_priority_low)
                            else -> stringResource(R.string.download_priority_normal)
                        },
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
internal fun getStatusText(status: Int): String {
    return when (status) {
        1 -> stringResource(R.string.download_status_waiting)
        2 -> stringResource(R.string.download_status_downloading)
        3 -> stringResource(R.string.download_status_paused)
        4 -> stringResource(R.string.download_status_finishing)
        5 -> stringResource(R.string.download_status_finished)
        6 -> stringResource(R.string.download_status_checking)
        7 -> stringResource(R.string.download_status_queued)
        8 -> stringResource(R.string.download_status_moving)
        101 -> stringResource(R.string.download_status_error)
        102 -> stringResource(R.string.download_status_no_response)
        103 -> stringResource(R.string.download_status_dest_not_exist)
        104 -> stringResource(R.string.download_status_dest_exist)
        105 -> stringResource(R.string.download_status_no_space)
        106 -> stringResource(R.string.download_status_invalid_task)
        107 -> stringResource(R.string.download_status_unsupported)
        108 -> stringResource(R.string.download_status_dest_readonly)
        109 -> stringResource(R.string.download_status_system_error)
        113 -> stringResource(R.string.download_status_duplicate)
        else -> stringResource(R.string.download_status_unknown, status)
    }
}

@Composable
internal fun getStatusColor(status: Int) = when (status) {
    1, 6, 7, 8 -> MaterialTheme.colorScheme.onSurfaceVariant
    2 -> MaterialTheme.colorScheme.primary
    3 -> MaterialTheme.colorScheme.outline
    5 -> MaterialTheme.colorScheme.primary
    101, 102, 103, 104, 105, 106, 107, 108, 109, 113 -> MaterialTheme.colorScheme.error
    else -> MaterialTheme.colorScheme.onSurface
}

internal fun formatTimeRemaining(seconds: Int): String {
    if (seconds <= 0) return appString(R.string.common_unknown)

    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    val secs = seconds % 60

    return when {
        hours > 0 -> appString(R.string.download_time_hours_minutes, hours, minutes)
        minutes > 0 -> appString(R.string.download_time_minutes_seconds, minutes, secs)
        else -> appString(R.string.download_time_seconds, secs)
    }
}
