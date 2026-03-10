package wang.zengye.dsm.ui.filestation

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
 * 远程文件夹卡片
 */
@Composable
internal fun RemoteFolderCard(
    folder: RemoteFolder,
    onDisconnect: () -> Unit,
    onUnmount: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
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
                    Icon(
                        imageVector = if (folder.connected) Icons.Default.FolderOpen else Icons.Default.FolderOff,
                        contentDescription = null,
                        tint = if (folder.connected)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = folder.name.ifEmpty { folder.server },
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = folder.server,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // 状态标签
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = if (folder.connected)
                        MaterialTheme.colorScheme.primaryContainer
                    else
                        MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = if (folder.connected) stringResource(R.string.remote_folder_connected) else stringResource(R.string.remote_folder_disconnected),
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = if (folder.connected)
                            MaterialTheme.colorScheme.onPrimaryContainer
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // 更多操作菜单
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = stringResource(R.string.remote_folder_more))
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        if (folder.connected) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.remote_folder_disconnect)) },
                                onClick = {
                                    showMenu = false
                                    onDisconnect()
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.LinkOff, contentDescription = null)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.remote_folder_unmount)) },
                                onClick = {
                                    showMenu = false
                                    onUnmount()
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Eject, contentDescription = null)
                                }
                            )
                        } else {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.remote_folder_reconnect)) },
                                onClick = { showMenu = false },
                                leadingIcon = {
                                    Icon(Icons.Default.Link, contentDescription = null)
                                }
                            )
                        }
                    }
                }
            }

            // 详细信息
            if (folder.path.isNotEmpty() || folder.mountPoint.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    if (folder.path.isNotEmpty()) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.remote_folder_remote_path),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = folder.path,
                                style = MaterialTheme.typography.bodyMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    if (folder.mountPoint.isNotEmpty()) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.remote_folder_mount_point),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = folder.mountPoint,
                                style = MaterialTheme.typography.bodyMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            // 类型标签
            if (folder.type.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                AssistChip(
                    onClick = {},
                    label = {
                        Text(
                            when (folder.type.lowercase()) {
                                "cifs" -> "SMB/CIFS"
                                "nfs" -> "NFS"
                                else -> folder.type.uppercase()
                            }
                        )
                    },
                    modifier = Modifier.height(24.dp)
                )
            }
        }
    }
}

/**
 * 添加远程文件夹对话框
 */
@Composable
internal fun AddRemoteFolderDialog(
    server: String,
    path: String,
    username: String,
    password: String,
    onServerChange: (String) -> Unit,
    onPathChange: (String) -> Unit,
    onUsernameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.remote_folder_add)) },
        text = {
            Column {
                OutlinedTextField(
                    value = server,
                    onValueChange = onServerChange,
                    label = { Text(stringResource(R.string.remote_folder_server_address) + " *") },
                    placeholder = { Text(stringResource(R.string.remote_folder_server_placeholder)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = path,
                    onValueChange = onPathChange,
                    label = { Text(stringResource(R.string.remote_folder_shared_path)) },
                    placeholder = { Text(stringResource(R.string.remote_folder_path_placeholder)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = username,
                    onValueChange = onUsernameChange,
                    label = { Text(stringResource(R.string.remote_folder_username)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = onPasswordChange,
                    label = { Text(stringResource(R.string.remote_folder_password)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                enabled = server.isNotEmpty()
            ) {
                Text(stringResource(R.string.common_add))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.common_cancel))
            }
        }
    )
}
