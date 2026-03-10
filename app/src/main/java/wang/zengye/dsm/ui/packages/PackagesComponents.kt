package wang.zengye.dsm.ui.packages

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import wang.zengye.dsm.R
import wang.zengye.dsm.data.model.PackageInfo
import wang.zengye.dsm.ui.theme.*

/**
 * 套件图标
 */
@Composable
private fun PackageIcon(
    thumbnailUrl: String,
    displayName: String
) {
    if (thumbnailUrl.isNotEmpty()) {
        AsyncImage(
            model = thumbnailUrl,
            contentDescription = displayName,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(CornerRadius.Medium))
        )
    } else {
        // 回退显示首字母
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = displayName.firstOrNull()?.uppercase() ?: "?",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

/**
 * 套件状态徽章
 */
@Composable
private fun PackageStatusBadge(
    status: String,
    isRunning: Boolean,
    isStopped: Boolean
) {
    val (containerColor, contentColor, icon) = when {
        isRunning -> Triple(
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.onPrimaryContainer,
            Icons.Filled.PlayArrow
        )
        isStopped -> Triple(
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.onSurfaceVariant,
            Icons.Filled.Stop
        )
        else -> Triple(
            MaterialTheme.colorScheme.tertiaryContainer,
            MaterialTheme.colorScheme.onTertiaryContainer,
            Icons.Filled.Circle
        )
    }

    Surface(
        color = containerColor,
        shape = RoundedCornerShape(CornerRadius.Small)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(12.dp),
                tint = contentColor
            )
            Text(
                text = status,
                style = MaterialTheme.typography.labelSmall,
                color = contentColor,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

/**
 * 操作按钮组
 */
@Composable
private fun PackageActionButtons(
    packageInfo: PackageInfo,
    isOperating: Boolean,
    onStart: () -> Unit,
    onStop: () -> Unit,
    onOpen: () -> Unit,
    onInstall: () -> Unit,
    onShowMenu: () -> Unit
) {
    if (isOperating) {
        CircularProgressIndicator(
            modifier = Modifier.size(24.dp),
            strokeWidth = 2.dp
        )
    } else if (!packageInfo.installed) {
        // 未安装 - 显示安装按钮
        FilledTonalIconButton(
            onClick = onInstall,
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                Icons.Outlined.Download,
                contentDescription = stringResource(R.string.packages_install),
                modifier = Modifier.size(20.dp)
            )
        }
    } else {
        // 已安装 - 显示操作按钮
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            when {
                packageInfo.isRunning -> {
                    if (packageInfo.launchable) {
                        IconButton(onClick = onOpen, modifier = Modifier.size(40.dp)) {
                            Icon(
                                Icons.AutoMirrored.Filled.OpenInNew,
                                contentDescription = stringResource(R.string.packages_open),
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    FilledTonalIconButton(
                        onClick = onStop,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            Icons.Filled.Stop,
                            contentDescription = stringResource(R.string.common_stop),
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
                packageInfo.isStopped -> {
                    FilledTonalIconButton(
                        onClick = onStart,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            Icons.Filled.PlayArrow,
                            contentDescription = stringResource(R.string.common_start),
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // 更多菜单
            IconButton(
                onClick = onShowMenu,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    Icons.Filled.MoreVert,
                    contentDescription = stringResource(R.string.packages_more),
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
internal fun PackageItem(
    packageInfo: PackageInfo,
    isOperating: Boolean,
    onStart: () -> Unit,
    onStop: () -> Unit,
    onOpen: () -> Unit,
    onUninstall: () -> Unit,
    onInstall: () -> Unit = {}
) {
    var showMenu by remember { mutableStateOf(false) }

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.PageHorizontal, vertical = Spacing.ExtraSmall),
        shape = AppShapes.Card
    ) {
        Column(
            modifier = Modifier.padding(Spacing.CardPadding)
        ) {
            // 主行：图标 + 内容 + 操作按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 套件图标 - 使用AsyncImage加载或显示首字母
                Surface(
                    modifier = Modifier.size(48.dp),
                    shape = RoundedCornerShape(CornerRadius.Medium),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    PackageIcon(
                        thumbnailUrl = packageInfo.thumbnailUrl,
                        displayName = packageInfo.displayName
                    )
                }

                Spacer(modifier = Modifier.width(Spacing.Standard))

                // 内容区域
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    // 套件名称
                    Text(
                        text = packageInfo.displayName,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    // 版本号行
                    if (packageInfo.version.isNotEmpty()) {
                        Text(
                            text = "v${packageInfo.version}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.width(Spacing.Small))

                // 操作按钮 - 包裹在Box中以便菜单定位
                Box {
                    PackageActionButtons(
                        packageInfo = packageInfo,
                        isOperating = isOperating,
                        onStart = onStart,
                        onStop = onStop,
                        onOpen = onOpen,
                        onInstall = onInstall,
                        onShowMenu = { showMenu = true }
                    )

                    // DropdownMenu - 相对于Box定位
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        if (packageInfo.launchable && packageInfo.isRunning) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.packages_open)) },
                                onClick = {
                                    showMenu = false
                                    onOpen()
                                },
                                leadingIcon = {
                                    Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null)
                                }
                            )
                        }

                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.packages_uninstall)) },
                            onClick = {
                                showMenu = false
                                onUninstall()
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Delete, contentDescription = null)
                            },
                            colors = MenuDefaults.itemColors(
                                textColor = MaterialTheme.colorScheme.error
                            )
                        )
                    }
                }
            }

            // 状态和描述行
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = Spacing.Small),
                horizontalArrangement = Arrangement.spacedBy(Spacing.Small),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 状态徽章（仅已安装套件显示）
                if (packageInfo.installed && packageInfo.status.isNotEmpty()) {
                    PackageStatusBadge(
                        status = packageInfo.statusText,
                        isRunning = packageInfo.isRunning,
                        isStopped = packageInfo.isStopped
                    )
                }

                // 描述文字
                if (packageInfo.description.isNotEmpty()) {
                    Text(
                        text = packageInfo.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}
