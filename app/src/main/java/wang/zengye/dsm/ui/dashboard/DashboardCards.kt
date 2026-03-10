package wang.zengye.dsm.ui.dashboard

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import wang.zengye.dsm.R
import wang.zengye.dsm.ui.components.BadgeStatus
import wang.zengye.dsm.ui.components.DonutProgress
import wang.zengye.dsm.ui.components.ElevatedCard
import wang.zengye.dsm.ui.components.StatusBadge
import wang.zengye.dsm.ui.theme.*
import wang.zengye.dsm.util.formatSize
import java.util.Calendar

/**
 * 问候语区域
 */
@Composable
internal fun GreetingSection(model: String) {
    Column {
        Text(
            text = getGreeting(),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun getGreeting(): String {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when {
        hour < 12 -> stringResource(R.string.dashboard_good_morning)
        hour < 18 -> stringResource(R.string.dashboard_good_afternoon)
        else -> stringResource(R.string.dashboard_good_evening)
    }
}

/**
 * 系统信息卡片 - Hero 渐变风格
 */
@Composable
internal fun SystemInfoCard(
    systemInfo: SystemInfoUi,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        shape = AppShapes.Card
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(primaryGradient())
                .padding(Spacing.CardPadding)
        ) {
            Column {
                // 主信息行
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 设备图标
                    Surface(
                        shape = RoundedCornerShape(CornerRadius.Large),
                        color = Color.White.copy(alpha = 0.2f),
                        modifier = Modifier.size(56.dp)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Dns,
                                contentDescription = null,
                                modifier = Modifier.size(28.dp),
                                tint = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(Spacing.Standard))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = systemInfo.model.ifEmpty { stringResource(R.string.dashboard_default_model) },
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Medium,
                            color = Color.White
                        )

                        if (systemInfo.dsmVersion.isNotEmpty()) {
                            Surface(
                                shape = RoundedCornerShape(CornerRadius.Full),
                                color = Color.White.copy(alpha = 0.2f),
                                modifier = Modifier.padding(top = 4.dp)
                            ) {
                                Text(
                                    text = "DSM ${systemInfo.dsmVersion}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Icon(
                        imageVector = Icons.Filled.ChevronRight,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.7f)
                    )
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = Spacing.Standard),
                    color = Color.White.copy(alpha = 0.2f)
                )

                // 详细信息
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    HeroInfoItem(
                        label = stringResource(R.string.dashboard_uptime),
                        value = formatUptime(systemInfo.uptime)
                    )
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = stringResource(R.string.dashboard_temperature),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${systemInfo.temperature}°C",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = if (systemInfo.temperatureWarning || systemInfo.temperature > 80) {
                                    Color(0xFFFFCDD2)
                                } else {
                                    Color.White
                                }
                            )
                            if (systemInfo.temperatureWarning || systemInfo.temperature > 80) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Filled.Warning,
                                    contentDescription = stringResource(R.string.dashboard_temperature_warning),
                                    modifier = Modifier.size(14.dp),
                                    tint = Color(0xFFFFCDD2)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Hero 卡片信息项（白色文字）
 */
@Composable
private fun HeroInfoItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.7f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = Color.White
        )
    }
}

/**
 * 资源使用情况区域 - 双列布局
 */
@Composable
internal fun ResourceUsageSection(
    cpuUsage: Int,
    cpuHistory: List<Int>,
    memoryUsage: Int,
    memoryHistory: List<Int>,
    memoryUsed: Long,
    memoryTotal: Long
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Spacing.CardSpacing)
    ) {
        // CPU 使用率
        ResourceUsageCard(
            title = "CPU",
            usage = cpuUsage,
            history = cpuHistory,
            icon = Icons.Filled.Memory,
            modifier = Modifier.weight(1f)
        )

        // 内存使用率
        ResourceUsageCard(
            title = stringResource(R.string.dashboard_memory),
            usage = memoryUsage,
            history = memoryHistory,
            icon = Icons.Filled.Storage,
            subtitle = "${formatSize(memoryUsed)} / ${formatSize(memoryTotal)}",
            modifier = Modifier.weight(1f)
        )
    }
}

/**
 * 资源使用率卡片 - 紧凑版
 */
@Composable
private fun ResourceUsageCard(
    title: String,
    usage: Int,
    history: List<Int>,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    subtitle: String? = null
) {
    val usageColor = getUsageColor(usage)

    ElevatedCard(
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(Spacing.CardPadding)
        ) {
            // 标题行
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = usageColor
                )

                Spacer(modifier = Modifier.width(Spacing.Small))

                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )

                Text(
                    text = "$usage%",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = usageColor
                )
            }

            // 历史趋势图
            Spacer(modifier = Modifier.height(Spacing.Small))
            MiniChart(
                history = history,
                color = usageColor,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(32.dp)
            )

            // 进度条
            Spacer(modifier = Modifier.height(Spacing.Small))
            LinearProgressIndicator(
                progress = { usage / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(CornerRadius.Full)),
                color = usageColor,
                trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                strokeCap = StrokeCap.Round,
                gapSize = 0.dp,
                drawStopIndicator = {}
            )

            // 副标题 - 保持相同高度，无内容时显示占位
            Spacer(modifier = Modifier.height(Spacing.ExtraSmall))
            Text(
                text = subtitle ?: " ",  // 空格占位，保持高度一致
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
    }
}

/**
 * 存储卷卡片 - MD3风格
 */
@Composable
internal fun VolumeCard(volume: VolumeInfoUi) {
    val usagePercent = (volume.usage * 100).toInt()
    val usageColor = getProgressColor(volume.usage)

    ElevatedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(Spacing.CardPadding)
        ) {
            // 标题行
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Storage,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = usageColor
                    )
                    Spacer(modifier = Modifier.width(Spacing.Small))
                    Text(
                        text = volume.name.ifEmpty { "Volume ${volume.id}" },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                }

                StatusBadge(
                    text = when (volume.status) {
                        "normal" -> stringResource(R.string.dashboard_status_normal)
                        "degrade" -> stringResource(R.string.dashboard_status_degrade)
                        else -> volume.status.ifEmpty { stringResource(R.string.common_unknown) }
                    },
                    status = when (volume.status) {
                        "normal" -> BadgeStatus.SUCCESS
                        "degrade" -> BadgeStatus.ERROR
                        else -> BadgeStatus.DEFAULT
                    }
                )
            }

            Spacer(modifier = Modifier.height(Spacing.Medium))

            // 存储空间显示
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.Standard),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 环形进度图
                Box(
                    modifier = Modifier.size(72.dp),
                    contentAlignment = Alignment.Center
                ) {
                    DonutProgress(
                        progress = volume.usage,
                        modifier = Modifier.fillMaxSize(),
                        strokeWidth = 8.dp
                    )
                    Text(
                        text = "$usagePercent%",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = usageColor
                    )
                }

                // 使用量信息
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    StorageInfoRow(
                        label = stringResource(R.string.dashboard_used),
                        value = formatSize(volume.usedSize)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    StorageInfoRow(
                        label = stringResource(R.string.dashboard_available),
                        value = formatSize(volume.totalSize - volume.usedSize)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    StorageInfoRow(
                        label = stringResource(R.string.dashboard_total),
                        value = formatSize(volume.totalSize)
                    )
                }
            }
        }
    }
}

/**
 * 存储信息行
 */
@Composable
private fun StorageInfoRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * 磁盘卡片 - MD3风格
 */
@Composable
internal fun DiskCard(disk: DiskInfoUi) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = AppShapes.Card
    ) {
        Column(
            modifier = Modifier.padding(Spacing.CardPadding)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = disk.name.ifEmpty { stringResource(R.string.dashboard_disk_name, disk.id) },
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium
                    )
                    if (disk.model.isNotEmpty()) {
                        Text(
                            text = disk.model,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${disk.temperature}°C",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = if (disk.temperature > 60) {
                                MaterialTheme.colorScheme.error
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            }
                        )
                        if (disk.temperature > 60) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Filled.Warning,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                    Text(
                        text = formatSize(disk.totalSize),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // 状态标签
            if (disk.health.isNotEmpty() || disk.smartStatus.isNotEmpty()) {
                Spacer(modifier = Modifier.height(Spacing.Small))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(Spacing.Small)
                ) {
                    if (disk.health.isNotEmpty()) {
                        StatusBadge(
                            text = disk.health,
                            status = if (disk.health == "normal") BadgeStatus.SUCCESS else BadgeStatus.ERROR
                        )
                    }

                    if (disk.smartStatus.isNotEmpty()) {
                        StatusBadge(
                            text = "SMART: ${disk.smartStatus}",
                            status = if (disk.smartStatus == "normal") BadgeStatus.SUCCESS else BadgeStatus.ERROR
                        )
                    }
                }
            }
        }
    }
}

/**
 * 网络卡片 - MD3风格
 */
@Composable
internal fun NetworkCard(network: NetworkInfo) {
    Log.d("NetworkCard", "Rendering: ${network.device}, rx=${network.rxSpeed}, tx=${network.txSpeed}")

    ElevatedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(Spacing.CardPadding)
        ) {
            // 网络名称
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Wifi,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(Spacing.Small))
                    Text(
                        text = network.name.ifEmpty { stringResource(R.string.dashboard_network_interface) },
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium
                    )
                }

                if (network.ip.isNotEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(CornerRadius.Small),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh
                    ) {
                        Text(
                            text = network.ip,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(Spacing.Medium))

            // 当前网速
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // 下载速度
                SpeedIndicator(
                    icon = Icons.Filled.ArrowDownward,
                    label = stringResource(R.string.dashboard_download),
                    speed = formatSize(network.rxSpeed) + "/s",
                    color = MaterialTheme.colorScheme.primary
                )

                // 上传速度
                SpeedIndicator(
                    icon = Icons.Filled.ArrowUpward,
                    label = stringResource(R.string.dashboard_upload),
                    speed = formatSize(network.txSpeed) + "/s",
                    color = MaterialTheme.colorScheme.tertiary
                )
            }

            // 流量历史图表
            if (network.rxHistory.isNotEmpty() || network.txHistory.isNotEmpty()) {
                Spacer(modifier = Modifier.height(Spacing.Medium))

                NetworkTrafficChart(
                    rxHistory = network.rxHistory,
                    txHistory = network.txHistory,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                )
            }
        }
    }
}

/**
 * 速度指示器
 */
@Composable
private fun SpeedIndicator(
    icon: ImageVector,
    label: String,
    speed: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            shape = CircleShape,
            color = color.copy(alpha = 0.12f),
            modifier = Modifier.size(32.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    modifier = Modifier.size(16.dp),
                    tint = color
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = speed,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * 快捷操作行 - 下载管理、终端、关机、重启（支持自动换行）
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun QuickActionsRow(
    onDownloads: () -> Unit,
    onTerminal: () -> Unit,
    onShutdown: () -> Unit,
    onReboot: () -> Unit
) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Spacing.MediumSmall),
        verticalArrangement = Arrangement.spacedBy(Spacing.MediumSmall)
    ) {
        QuickActionChip(
            icon = Icons.Outlined.CloudDownload,
            label = stringResource(R.string.download_manager_title),
            onClick = onDownloads,
            color = MaterialTheme.colorScheme.secondary
        )
        QuickActionChip(
            icon = Icons.Outlined.Terminal,
            label = stringResource(R.string.terminal_ssh_terminal),
            onClick = onTerminal,
            color = MaterialTheme.colorScheme.primary
        )
        QuickActionChip(
            icon = Icons.Outlined.PowerSettingsNew,
            label = stringResource(R.string.dashboard_shutdown),
            onClick = onShutdown,
            color = MaterialTheme.colorScheme.error
        )
        QuickActionChip(
            icon = Icons.Outlined.RestartAlt,
            label = stringResource(R.string.dashboard_reboot),
            onClick = onReboot,
            color = MaterialTheme.colorScheme.tertiary
        )
    }
}

/**
 * 电源操作行 - 关机/重启（保留用于控制面板等场景）
 */
@Composable
internal fun PowerActionsRow(
    onShutdown: () -> Unit,
    onReboot: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Spacing.MediumSmall)
    ) {
        QuickActionChip(
            icon = Icons.Outlined.PowerSettingsNew,
            label = stringResource(R.string.dashboard_shutdown),
            onClick = onShutdown,
            color = MaterialTheme.colorScheme.error
        )
        QuickActionChip(
            icon = Icons.Outlined.RestartAlt,
            label = stringResource(R.string.dashboard_reboot),
            onClick = onReboot,
            color = MaterialTheme.colorScheme.tertiary
        )
    }
}

/**
 * 快捷操作 Chip - MD3风格
 */
@Composable
private fun QuickActionChip(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    color: Color = Color.Unspecified,
    modifier: Modifier = Modifier
) {
    val chipColor = if (color == Color.Unspecified) MaterialTheme.colorScheme.primary else color
    AssistChip(
        onClick = onClick,
        modifier = modifier,
        label = {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = chipColor,
                maxLines = 1
            )
        },
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.size(18.dp),
                tint = chipColor
            )
        },
        shape = RoundedCornerShape(CornerRadius.Full),
        border = BorderStroke(
            1.dp,
            chipColor.copy(alpha = 0.3f)
        )
    )
}

/**
 * DSM 应用 ID 到本地路由的映射
 */
data class DsmAppEntry(
    val appId: String,
    val icon: ImageVector,
    val labelResId: Int,
    val route: String
)

/**
 * 应用启动器网格 - 动态根据已安装应用渲染
 */
@Composable
internal fun AppLauncherGrid(
    installedApps: List<String>,
    onNavigate: (String) -> Unit
) {
    // 定义所有已知应用的映射 - 只有这些有 Android 原生实现
    val knownApps = remember {
        listOf(
            // 系统内置应用（始终显示，不依赖 AppPrivilege）
            DsmAppEntry("SYNO.SDS.AdminCenter.Application", Icons.Outlined.Settings, R.string.control_panel_title, "control_panel"),
            DsmAppEntry("SYNO.SDS.PkgManApp.Instance", Icons.Outlined.Apps, R.string.dashboard_app_packages, "packages"),
            DsmAppEntry("SYNO.SDS.ResourceMonitor.Instance", Icons.Outlined.Speed, R.string.dashboard_performance, "performance"),
            DsmAppEntry("SYNO.SDS.StorageManager.Instance", Icons.Outlined.Storage, R.string.control_panel_storage, "storage"),
            DsmAppEntry("SYNO.SDS.LogCenter.Instance", Icons.Outlined.Article, R.string.control_panel_log_center, "logs"),
            DsmAppEntry("SYNO.SDS.LogCenter.BuiltIn", Icons.Outlined.Article, R.string.control_panel_log_center, "logs"),
            // 可选应用（根据 AppPrivilege 显示）
            DsmAppEntry("SYNO.SDS.App.FileStation3.Instance", Icons.Outlined.Folder, R.string.dashboard_app_file, "file"),
            DsmAppEntry("SYNO.Foto.AppInstance", Icons.Outlined.PhotoLibrary, R.string.dashboard_app_photos, "photos"),
            DsmAppEntry("SYNO.Photo.AppInstance", Icons.Outlined.PhotoLibrary, R.string.dashboard_app_photos, "photos"),
            DsmAppEntry("SYNO.SDS.Docker.Application", Icons.Outlined.Inventory2, R.string.dashboard_app_docker, "docker"),
            DsmAppEntry("SYNO.SDS.ContainerManager.Application", Icons.Outlined.Inventory2, R.string.dashboard_app_docker, "docker"),
            DsmAppEntry("SYNO.SDS.DownloadStation.Application", Icons.Outlined.CloudDownload, R.string.dashboard_app_download, "download"),
            DsmAppEntry("SYNO.SDS.Virtualization.Application", Icons.Outlined.Computer, R.string.dashboard_app_virtual_machine, "virtual_machine"),
        )
    }
    
    // 系统内置应用 - 始终显示
    val systemApps = remember {
        setOf(
            "SYNO.SDS.AdminCenter.Application",
            "SYNO.SDS.PkgManApp.Instance",
            "SYNO.SDS.ResourceMonitor.Instance",
            "SYNO.SDS.StorageManager.Instance",
            "SYNO.SDS.LogCenter.Instance",
            "SYNO.SDS.LogCenter.BuiltIn",
        )
    }

    // 根据 API 返回的应用列表过滤
    val visibleApps = remember(installedApps) {
        val seen = mutableSetOf<String>() // 按 route 去重
        knownApps.mapNotNull { app ->
            // 系统内置应用始终显示，其他应用需要检查权限
            val hasPermission = installedApps.contains(app.appId) || systemApps.contains(app.appId)
            if (hasPermission && seen.add(app.route)) app else null
        }
    }

    if (visibleApps.isEmpty()) return

    Column(
        verticalArrangement = Arrangement.spacedBy(Spacing.Small)
    ) {
        visibleApps.chunked(4).forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.Small)
            ) {
                rowItems.forEach { app ->
                    AppLauncherItem(
                        icon = app.icon,
                        label = stringResource(app.labelResId),
                        onClick = { onNavigate(app.route) },
                        modifier = Modifier.weight(1f)
                    )
                }
                repeat(4 - rowItems.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

/**
 * 应用启动器单项 - 图标 + 文字
 */
@Composable
private fun AppLauncherItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(CornerRadius.Medium),
        color = Color.Transparent
    ) {
        Column(
            modifier = Modifier.padding(vertical = Spacing.MediumSmall),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                shape = RoundedCornerShape(CornerRadius.Large),
                color = MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
            Spacer(modifier = Modifier.height(Spacing.ExtraSmall))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
    }
}

// ==================== 辅助函数 ====================

@Composable
internal fun formatUptime(optime: String): String {
    if (optime.isEmpty()) return "-"

    val parts = optime.split(":")
    if (parts.size != 3) return optime

    val hours = parts[0].toIntOrNull()
    val minutes = parts[1].toIntOrNull()
    
    if (hours == null || minutes == null) return optime

    val days = hours / 24
    val remainingHours = hours % 24

    return buildString {
        if (days > 0) append(stringResource(R.string.dashboard_uptime_days, days))
        append(stringResource(R.string.dashboard_uptime_hours_minutes, remainingHours, minutes))
    }
}
