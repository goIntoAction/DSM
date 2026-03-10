package wang.zengye.dsm.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import wang.zengye.dsm.data.model.FileType
import wang.zengye.dsm.ui.theme.*
import wang.zengye.dsm.util.formatSize
import wang.zengye.dsm.util.timeAgo

// ==================== 列表项组件 ====================

/**
 * 文件图标组件
 */
@Composable
fun FileIcon(
    fileName: String,
    isDirectory: Boolean,
    modifier: Modifier = Modifier,
    size: Int = 48
) {
    val icon: ImageVector = when {
        isDirectory -> Icons.Filled.Folder
        else -> when (FileType.fromName(fileName)) {
            FileType.IMAGE -> Icons.Filled.Image
            FileType.VIDEO -> Icons.Filled.VideoFile
            FileType.AUDIO -> Icons.Filled.AudioFile
            FileType.DOCUMENT -> Icons.Filled.Description
            FileType.ARCHIVE -> Icons.Filled.FolderZip
            FileType.APK -> Icons.Filled.Android
            FileType.CODE -> Icons.Filled.Code
            else -> Icons.AutoMirrored.Filled.InsertDriveFile
        }
    }
    
    val iconColor = if (isDirectory) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    
    Surface(
        shape = CircleShape,
        color = iconColor.copy(alpha = 0.12f),
        modifier = modifier.size(size.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = fileName,
                modifier = Modifier.size((size * 0.5).dp),
                tint = iconColor
            )
        }
    }
}

/**
 * 文件列表项 - MD3风格
 */
@Composable
fun FileListItem(
    fileName: String,
    isDirectory: Boolean,
    fileSize: Long = 0,
    modifiedTime: Long = 0,
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {}
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = Spacing.PageHorizontal,
                    vertical = Spacing.Medium
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FileIcon(
                fileName = fileName,
                isDirectory = isDirectory,
                size = 44
            )
            
            Spacer(modifier = Modifier.width(Spacing.Standard))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = fileName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                if (!isDirectory) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(Spacing.Small)
                    ) {
                        Text(
                            text = formatSize(fileSize),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        if (modifiedTime > 0) {
                            Text(
                                text = "·",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = timeAgo(modifiedTime),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            
            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

/**
 * 设置项 - MD3风格
 */
@Composable
fun SettingItem(
    title: String,
    subtitle: String? = null,
    icon: ImageVector? = null,
    onClick: () -> Unit = {}
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = Spacing.PageHorizontal,
                    vertical = Spacing.Medium
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(Spacing.Standard))
            }
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                
                if (subtitle != null) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
