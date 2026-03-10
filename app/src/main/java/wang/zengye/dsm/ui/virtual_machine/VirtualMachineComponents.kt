package wang.zengye.dsm.ui.virtual_machine

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import wang.zengye.dsm.R

/**
 * 虚拟机卡片
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun VmItemCard(
    vm: VmItem,
    isOperating: Boolean,
    operationType: String,
    onStart: () -> Unit,
    onStop: () -> Unit,
    onForceStop: () -> Unit,
    onRestart: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    var showStopDialog by remember { mutableStateOf(false) }

    // 强制停止确认对话框
    if (showStopDialog) {
        AlertDialog(
            onDismissRequest = { showStopDialog = false },
            title = { Text(stringResource(R.string.vm_force_stop_title)) },
            text = { Text(stringResource(R.string.vm_force_stop_confirm, vm.name)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showStopDialog = false
                        onForceStop()
                    }
                ) {
                    Text(stringResource(R.string.vm_force_stop), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showStopDialog = false }) {
                    Text(stringResource(R.string.common_cancel))
                }
            }
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                    Box(
                        modifier = Modifier.size(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isOperating) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = when {
                                    vm.isRunning -> Icons.Default.PlayCircle
                                    vm.isStopped -> Icons.Default.StopCircle
                                    vm.isSuspended -> Icons.Default.PauseCircle
                                    else -> Icons.Default.HelpOutline
                                },
                                contentDescription = null,
                                modifier = Modifier.size(32.dp),
                                tint = when {
                                    vm.isRunning -> MaterialTheme.colorScheme.primary
                                    vm.isStopped -> MaterialTheme.colorScheme.onSurfaceVariant
                                    else -> MaterialTheme.colorScheme.tertiary
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = vm.name,
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            AssistChip(
                                onClick = {},
                                label = {
                                    Text(
                                        when {
                                            vm.isRunning -> stringResource(R.string.vm_status_running)
                                            vm.isStopped -> stringResource(R.string.vm_status_stopped)
                                            vm.isSuspended -> stringResource(R.string.vm_status_suspended)
                                            else -> vm.status
                                        },
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                },
                                modifier = Modifier.height(24.dp)
                            )
                        }
                    }
                }

                // 更多操作菜单
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = stringResource(R.string.common_more))
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        if (vm.isStopped) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.common_start)) },
                                onClick = {
                                    showMenu = false
                                    onStart()
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                                },
                                enabled = !isOperating
                            )
                        }

                        if (vm.isRunning) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.vm_shutdown)) },
                                onClick = {
                                    showMenu = false
                                    onStop()
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.PowerSettingsNew, contentDescription = null)
                                },
                                enabled = !isOperating
                            )

                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.common_restart)) },
                                onClick = {
                                    showMenu = false
                                    onRestart()
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.RestartAlt, contentDescription = null)
                                },
                                enabled = !isOperating
                            )

                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.vm_force_stop)) },
                                onClick = {
                                    showMenu = false
                                    showStopDialog = true
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Dangerous, contentDescription = null)
                                },
                                colors = MenuDefaults.itemColors(
                                    textColor = MaterialTheme.colorScheme.error
                                ),
                                enabled = !isOperating
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 详细信息
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // CPU
                Column {
                    Text(
                        text = stringResource(R.string.vm_cpu_label),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = stringResource(R.string.vm_cpu_cores, vm.vcpu),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                // 内存
                Column {
                    Text(
                        text = stringResource(R.string.vm_memory_label),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formatMemory(vm.memory),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                // 磁盘
                Column {
                    Text(
                        text = stringResource(R.string.vm_disk_label),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formatDisk(vm.diskSize),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                // 网络
                if (vm.network.isNotEmpty()) {
                    Column {
                        Text(
                            text = stringResource(R.string.vm_network_label),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = vm.network,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            // 快捷操作按钮
            if (isOperating) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = when (operationType) {
                            "start" -> stringResource(R.string.vm_starting)
                            "stop" -> stringResource(R.string.vm_stopping)
                            "restart" -> stringResource(R.string.vm_restarting)
                            else -> stringResource(R.string.vm_operating)
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

/**
 * 格式化内存大小
 */
internal fun formatMemory(memory: Long): String {
    return when {
        memory < 1024 -> "${memory} MB"
        else -> String.format("%.1f GB", memory / 1024.0)
    }
}

/**
 * 格式化磁盘大小
 */
internal fun formatDisk(size: Long): String {
    return when {
        size < 1024 -> "${size} MB"
        size < 1024 * 1024 -> String.format("%.1f GB", size / 1024.0)
        else -> String.format("%.1f TB", size / (1024.0 * 1024))
    }
}
