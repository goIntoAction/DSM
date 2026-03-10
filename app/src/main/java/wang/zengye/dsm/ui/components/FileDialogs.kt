package wang.zengye.dsm.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import wang.zengye.dsm.ui.file.FileItem
import wang.zengye.dsm.util.formatSize
import wang.zengye.dsm.util.formatDateTime
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.stringResource
import wang.zengye.dsm.R

/**
 * 文件详情对话框
 */
@Composable
fun FileDetailDialog(
    file: FileItem,
    onDismiss: () -> Unit,
    onRename: () -> Unit = {},
    onDelete: () -> Unit = {},
    onDownload: () -> Unit = {},
    onShare: () -> Unit = {}
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (file.isDir) Icons.Default.Folder else Icons.Default.InsertDriveFile,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = file.name,
                    maxLines = 1,
                    modifier = Modifier.weight(1f)
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DetailRow(stringResource(R.string.file_type), if (file.isDir) stringResource(R.string.file_type_folder) else stringResource(R.string.file_type_file))
                if (!file.isDir) {
                    DetailRow(stringResource(R.string.file_size), formatSize(file.size))
                }
                DetailRow(stringResource(R.string.file_modified_time), formatDateTime(file.modified))
                if (file.owner.isNotEmpty()) {
                    DetailRow(stringResource(R.string.file_owner), file.owner)
                }
                if (file.permission.isNotEmpty()) {
                    DetailRow(stringResource(R.string.file_permission), file.permission)
                }
                SelectionContainer {
                    DetailRow(stringResource(R.string.file_path_label), file.path)
                }
            }
        },
        confirmButton = {
            Row {
                TextButton(onClick = onRename) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(stringResource(R.string.file_rename))
                }
                TextButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(stringResource(R.string.common_delete), color = MaterialTheme.colorScheme.error)
                }
            }
        },
        dismissButton = {
            Row {
                if (!file.isDir) {
                    TextButton(onClick = onDownload) {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource(R.string.file_download))
                    }
                }
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.common_close))
                }
            }
        }
    )
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
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
 * 重命名对话框
 */
@Composable
fun RenameDialog(
    currentName: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var newName by remember { mutableStateOf(currentName) }
    var isError by remember { mutableStateOf(false) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.file_rename_title)) },
        text = {
            OutlinedTextField(
                value = newName,
                onValueChange = {
                    newName = it
                    isError = it.isBlank()
                },
                label = { Text(stringResource(R.string.file_new_name_label)) },
                isError = isError,
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (newName.isNotBlank() && newName != currentName) {
                        onConfirm(newName)
                    } else {
                        isError = true
                    }
                }
            ) {
                Text(stringResource(R.string.common_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.common_cancel))
            }
        }
    )
}

/**
 * 删除确认对话框
 */
@Composable
fun DeleteConfirmDialog(
    fileNames: List<String>,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.file_delete_confirm_title)) },
        text = {
            Column {
                if (fileNames.size == 1) {
                    Text(stringResource(R.string.file_delete_single_confirm, fileNames.first()))
                } else {
                    Text(stringResource(R.string.file_delete_multi_confirm, fileNames.size))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = fileNames.take(5).joinToString("\n") { "• $it" } +
                            if (fileNames.size > 5) "\n" + stringResource(R.string.file_and_more, fileNames.size - 5) else "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(R.string.common_delete), color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.common_cancel))
            }
        }
    )
}

/**
 * 创建文件夹对话框
 */
@Composable
fun CreateFolderDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var folderName by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.file_new_folder)) },
        text = {
            OutlinedTextField(
                value = folderName,
                onValueChange = {
                    folderName = it
                    isError = it.isBlank()
                },
                label = { Text(stringResource(R.string.file_folder_name)) },
                isError = isError,
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (folderName.isNotBlank()) {
                        onConfirm(folderName)
                    } else {
                        isError = true
                    }
                }
            ) {
                Text(stringResource(R.string.common_create))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.common_cancel))
            }
        }
    )
}

/**
 * 操作进度对话框
 */
@Composable
fun OperationProgressDialog(
    message: String,
    progress: Float,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.file_operating)) },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(message)
                Spacer(modifier = Modifier.height(16.dp))
                LinearProgressIndicator(
                    progress = { progress / 100f },
                    modifier = Modifier.fillMaxWidth(),
                    strokeCap = StrokeCap.Butt,
                    gapSize = 0.dp,
                    drawStopIndicator = {})
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "${progress.toInt()}%",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
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

/**
 * 文件操作底部菜单
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileActionBottomSheet(
    file: FileItem,
    onDismiss: () -> Unit,
    onRename: () -> Unit = {},
    onDelete: () -> Unit = {},
    onCopy: () -> Unit = {},
    onMove: () -> Unit = {},
    onDownload: () -> Unit = {},
    onShare: () -> Unit = {},
    onCompress: () -> Unit = {},
    onFavorite: () -> Unit = {}
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            // 文件名标题
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (file.isDir) Icons.Default.Folder else Icons.Default.InsertDriveFile,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = file.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            HorizontalDivider()
            
            // 操作列表
            ActionItem(
                icon = Icons.Default.Edit,
                title = stringResource(R.string.file_rename),
                onClick = {
                    onDismiss()
                    onRename()
                }
            )

            ActionItem(
                icon = Icons.Default.ContentCopy,
                title = stringResource(R.string.file_copy_to),
                onClick = {
                    onDismiss()
                    onCopy()
                }
            )

            ActionItem(
                icon = Icons.Default.DriveFileMove,
                title = stringResource(R.string.file_move_to),
                onClick = {
                    onDismiss()
                    onMove()
                }
            )
            
            if (!file.isDir) {
                ActionItem(
                    icon = Icons.Default.Download,
                    title = stringResource(R.string.file_download),
                    onClick = {
                        onDismiss()
                        onDownload()
                    }
                )
            }

            ActionItem(
                icon = Icons.Default.Share,
                title = stringResource(R.string.file_share_action),
                onClick = {
                    onDismiss()
                    onShare()
                }
            )

            ActionItem(
                icon = Icons.Default.FolderZip,
                title = stringResource(R.string.file_compress),
                onClick = {
                    onDismiss()
                    onCompress()
                }
            )

            ActionItem(
                icon = Icons.Default.Star,
                title = stringResource(R.string.file_add_to_favorite),
                onClick = {
                    onDismiss()
                    onFavorite()
                }
            )

            HorizontalDivider()

            ActionItem(
                icon = Icons.Default.Delete,
                title = stringResource(R.string.common_delete),
                color = MaterialTheme.colorScheme.error,
                onClick = {
                    onDismiss()
                    onDelete()
                }
            )
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ActionItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    color: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = color
        )
    }
}
