package wang.zengye.dsm.ui.download

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import wang.zengye.dsm.R
import wang.zengye.dsm.data.model.DownloadTask
import wang.zengye.dsm.ui.theme.*
import wang.zengye.dsm.util.formatSize

@Composable
internal fun SpeedIndicator(
    icon: ImageVector,
    label: String,
    speed: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = RoundedCornerShape(CornerRadius.Medium),
            color = color.copy(alpha = 0.12f),
            modifier = Modifier.size(40.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = color
                )
            }
        }
        Spacer(modifier = Modifier.width(Spacing.Standard))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = speed,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
internal fun StatisticItem(
    label: String,
    count: Int,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DownloadTaskItem(
    task: DownloadTask,
    onToggleClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onClick: () -> Unit = {}
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.PageHorizontal, vertical = Spacing.Small),
        onClick = onClick,
        shape = AppShapes.Card
    ) {
        Column(
            modifier = Modifier.padding(Spacing.CardPadding)
        ) {
            // 任务标题行
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 状态图标
                val (statusIcon, statusColor) = when {
                    task.isFinished -> Icons.Filled.CheckCircle to MaterialTheme.colorScheme.primary
                    task.isPaused -> Icons.Filled.PauseCircle to MaterialTheme.colorScheme.tertiary
                    task.isError -> Icons.Filled.Error to MaterialTheme.colorScheme.error
                    else -> Icons.Filled.Downloading to MaterialTheme.colorScheme.primary
                }

                Surface(
                    shape = RoundedCornerShape(CornerRadius.Medium),
                    color = statusColor.copy(alpha = 0.12f),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = statusIcon,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = statusColor
                        )
                    }
                }

                Spacer(modifier = Modifier.width(Spacing.Standard))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    // 显示文件大小和状态
                    val statusText = when {
                        task.isFinished -> stringResource(R.string.download_finished)
                        task.isPaused -> stringResource(R.string.download_paused)
                        task.isError -> stringResource(R.string.common_error)
                        else -> null
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = formatSize(task.size),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (statusText != null) {
                            Text(
                                text = " · $statusText",
                                style = MaterialTheme.typography.labelSmall,
                                color = statusColor
                            )
                        }
                    }
                }

                // 操作按钮：仅下载中和暂停状态显示开始/暂停
                if (task.isToggleable) {
                    IconButton(onClick = onToggleClick) {
                        Icon(
                            imageVector = if (task.isDownloading) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                            contentDescription = if (task.isDownloading) stringResource(R.string.download_paused) else stringResource(R.string.common_start)
                        )
                    }
                }

                IconButton(onClick = onDeleteClick) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = stringResource(R.string.common_delete),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }

            // 进度和速度信息
            if (!task.isFinished && task.additional?.transfer != null) {
                val transfer = task.additional.transfer!!
                val progress = transfer.progress

                Spacer(modifier = Modifier.height(Spacing.Small))

                // 进度条
                LinearProgressIndicator(
                    progress = { progress.toFloat() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                    strokeCap = StrokeCap.Butt,
                    gapSize = 0.dp,
                    drawStopIndicator = {}
                )

                Spacer(modifier = Modifier.height(4.dp))

                // 进度和速度信息
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${transfer.progressPercent}% · ${formatSize(transfer.sizeDownloaded)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (transfer.speedDownload > 0) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.ArrowDownward,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "${formatSize(transfer.speedDownload)}/s",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}
