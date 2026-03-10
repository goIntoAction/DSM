package wang.zengye.dsm.ui.storage

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import wang.zengye.dsm.R

/**
 * 健康状态概览卡片
 */
@Composable
internal fun HealthOverviewCard(disks: List<SmartDiskInfo>) {
    val healthyCount = disks.count { it.isHealthy }
    val warningCount = disks.count { it.hasWarning }
    val abnormalCount = disks.count { it.isAbnormal }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (abnormalCount > 0) {
                MaterialTheme.colorScheme.errorContainer
            } else if (warningCount > 0) {
                MaterialTheme.colorScheme.tertiaryContainer
            } else {
                MaterialTheme.colorScheme.primaryContainer
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatusItem(
                icon = Icons.Default.CheckCircle,
                label = stringResource(R.string.smart_storage_health_normal),
                count = healthyCount,
                color = if (abnormalCount > 0 || warningCount > 0)
                    MaterialTheme.colorScheme.onSurfaceVariant
                else
                    MaterialTheme.colorScheme.onPrimaryContainer
            )

            StatusItem(
                icon = Icons.Default.Warning,
                label = stringResource(R.string.smart_storage_health_warning),
                count = warningCount,
                color = if (warningCount > 0)
                    MaterialTheme.colorScheme.onErrorContainer
                else
                    MaterialTheme.colorScheme.onSurfaceVariant
            )

            StatusItem(
                icon = Icons.Default.Error,
                label = stringResource(R.string.smart_storage_health_abnormal),
                count = abnormalCount,
                color = if (abnormalCount > 0)
                    MaterialTheme.colorScheme.error
                else
                    MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
internal fun StatusItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    count: Int,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(28.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "$count",
            style = MaterialTheme.typography.headlineMedium,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = color
        )
    }
}

/**
 * SMART磁盘卡片
 */
@Composable
internal fun SmartDiskCard(
    disk: SmartDiskInfo,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.secondaryContainer
            else
                MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 4.dp else 1.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // 标题行
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 状态图标
                    Icon(
                        imageVector = when {
                            disk.isHealthy -> Icons.Default.CheckCircle
                            disk.hasWarning -> Icons.Default.Warning
                            else -> Icons.Default.Error
                        },
                        contentDescription = null,
                        tint = when {
                            disk.isHealthy -> MaterialTheme.colorScheme.primary
                            disk.hasWarning -> MaterialTheme.colorScheme.tertiary
                            else -> MaterialTheme.colorScheme.error
                        },
                        modifier = Modifier.size(28.dp)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = disk.model.ifEmpty { disk.device },
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = disk.device,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // 温度
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = when {
                        disk.temperature >= 60 -> MaterialTheme.colorScheme.errorContainer
                        disk.temperature >= 50 -> MaterialTheme.colorScheme.tertiaryContainer
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Thermostat,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = when {
                                disk.temperature >= 60 -> MaterialTheme.colorScheme.error
                                disk.temperature >= 50 -> MaterialTheme.colorScheme.tertiary
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${disk.temperature}°C",
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 详细信息
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.smart_storage_size),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formatDiskSize(disk.size),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                if (disk.serial.isNotEmpty()) {
                    Column {
                        Text(
                            text = stringResource(R.string.smart_storage_serial),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = disk.serial,
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // 测试进度
            if (disk.testInProgress) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        progress = { disk.testProgress / 100f },
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.smart_storage_test_in_progress, disk.testProgress),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // 展开/收起提示
            if (isSelected) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    FilledTonalButton(
                        onClick = { /* viewModel.showTestDialog() */ },
                        enabled = !disk.testInProgress
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource(R.string.smart_storage_run_test))
                    }
                }
            }
        }
    }
}

/**
 * 测试类型选择对话框
 */
@Composable
internal fun SmartTestTypeDialog(
    disk: SmartDiskInfo,
    isRunning: Boolean,
    onDismiss: () -> Unit,
    onStartTest: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.smart_storage_select_test_type)) },
        text = {
            Column {
                Text(
                    text = stringResource(R.string.smart_storage_disk_label, disk.model.ifEmpty { disk.device }),
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(16.dp))

                // 短测试
                ListTile(
                    leading = Icons.Default.Timer,
                    title = stringResource(R.string.smart_storage_short_test),
                    subtitle = stringResource(R.string.smart_storage_short_test_desc),
                    onClick = { onStartTest("short") },
                    enabled = !isRunning
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 长测试
                ListTile(
                    leading = Icons.Default.Schedule,
                    title = stringResource(R.string.smart_storage_long_test),
                    subtitle = stringResource(R.string.smart_storage_long_test_desc),
                    onClick = { onStartTest("long") },
                    enabled = !isRunning
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 传输测试
                ListTile(
                    leading = Icons.Default.Speed,
                    title = stringResource(R.string.smart_storage_conveyance_test),
                    subtitle = stringResource(R.string.smart_storage_conveyance_test_desc),
                    onClick = { onStartTest("conveyance") },
                    enabled = !isRunning
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.common_cancel))
            }
        }
    )
}

@Composable
internal fun ListTile(
    leading: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = leading,
                contentDescription = null,
                tint = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * 测试日志项
 */
@Composable
internal fun TestLogItem(log: SmartTestLog) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = when (log.testType) {
                        "short" -> stringResource(R.string.smart_storage_short_test)
                        "long" -> stringResource(R.string.smart_storage_long_test)
                        "conveyance" -> stringResource(R.string.smart_storage_conveyance_test)
                        else -> log.testType
                    },
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = formatTimestamp(log.startTime),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            AssistChip(
                onClick = {},
                label = {
                    Text(
                        when (log.status) {
                            "completed" -> stringResource(R.string.smart_storage_test_completed)
                            "running" -> stringResource(R.string.smart_storage_test_running)
                            "aborted" -> stringResource(R.string.smart_storage_test_aborted)
                            "error" -> stringResource(R.string.smart_storage_test_error)
                            else -> log.status
                        }
                    )
                },
                modifier = Modifier.height(28.dp)
            )
        }
    }
}

/**
 * 格式化磁盘大小
 */
internal fun formatDiskSize(size: Long): String {
    return when {
        size < 1024 -> "${size} GB"
        size < 1024 * 1024 -> String.format("%.1f TB", size / 1024.0)
        else -> String.format("%.2f PB", size / (1024.0 * 1024))
    }
}

/**
 * 格式化时间戳
 */
internal fun formatTimestamp(timestamp: Long): String {
    if (timestamp == 0L) return "-"
    val date = java.util.Date(timestamp * 1000)
    return java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault()).format(date)
}
