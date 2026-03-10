package wang.zengye.dsm.ui.control_panel

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import wang.zengye.dsm.R
import wang.zengye.dsm.util.formatDateTime

/**
 * SSH状态卡片
 */
@Composable
internal fun SshStatusCard(
    setting: TerminalSetting,
    onEdit: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (setting.enabled) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = stringResource(R.string.terminal_ssh_service),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (setting.enabled) stringResource(R.string.terminal_running_port, setting.port) else stringResource(R.string.common_stopped),
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.terminal_edit))
                }

                Spacer(modifier = Modifier.width(8.dp))

                Surface(
                    color = if (setting.enabled) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.outline
                    },
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = if (setting.enabled) stringResource(R.string.terminal_status_enabled) else stringResource(R.string.terminal_status_disabled),
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}

/**
 * 设置行
 */
@Composable
internal fun SettingRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * 终端会话卡片
 */
@Composable
internal fun TerminalSessionCard(
    session: TerminalSession,
    onDisconnect: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = MaterialTheme.colorScheme.primary,
                        shape = MaterialTheme.shapes.extraSmall
                    ) {
                        Text(
                            text = session.user,
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "@ ${session.ip}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = stringResource(R.string.terminal_started_at, formatDateTime(session.startTime)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(onClick = onDisconnect) {
                Icon(
                    imageVector = Icons.Default.LinkOff,
                    contentDescription = stringResource(R.string.terminal_disconnect),
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

/**
 * 编辑终端设置对话框
 */
@Composable
internal fun EditTerminalDialog(
    currentSetting: TerminalSetting,
    onDismiss: () -> Unit,
    onSave: (TerminalSetting) -> Unit
) {
    var enabled by remember { mutableStateOf(currentSetting.enabled) }
    var port by remember { mutableStateOf(currentSetting.port.toString()) }
    var allowRoot by remember { mutableStateOf(currentSetting.allowRootLogin) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.terminal_ssh_settings)) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(stringResource(R.string.terminal_enable_ssh_short))
                    Switch(
                        checked = enabled,
                        onCheckedChange = { enabled = it }
                    )
                }

                OutlinedTextField(
                    value = port,
                    onValueChange = {
                        if (it.all { c -> c.isDigit() } && it.length <= 5) {
                            port = it
                        }
                    },
                    label = { Text(stringResource(R.string.terminal_port)) },
                    enabled = enabled,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(stringResource(R.string.terminal_allow_root))
                    Switch(
                        checked = allowRoot,
                        onCheckedChange = { allowRoot = it },
                        enabled = enabled
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onSave(TerminalSetting(
                        enabled = enabled,
                        port = port.toIntOrNull() ?: 22,
                        allowRootLogin = allowRoot
                    ))
                }
            ) {
                Text(stringResource(R.string.common_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.common_cancel))
            }
        }
    )
}
