package wang.zengye.dsm.ui.docker

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import wang.zengye.dsm.R
import wang.zengye.dsm.ui.components.EmptyState
import wang.zengye.dsm.ui.components.ErrorState
import wang.zengye.dsm.ui.components.LoadingState
import wang.zengye.dsm.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DockerContainerDetailScreen(
    containerName: String,
    onNavigateBack: () -> Unit,
    viewModel: DockerContainerDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.state.collectAsStateWithLifecycle()
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("总览", "进程", "日志")
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(containerName) {
        viewModel.sendIntent(DockerContainerDetailIntent.LoadDetail(containerName))
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is DockerContainerDetailEvent.ShowError -> {
                    snackbarHostState.showSnackbar(event.message)
                }
            }
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = containerName,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.common_back))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = Color.Transparent
                ),
                actions = {
                    IconButton(onClick = { viewModel.sendIntent(DockerContainerDetailIntent.Refresh) }) {
                        Icon(Icons.Filled.Refresh, contentDescription = stringResource(R.string.common_refresh))
                    }
                }
            )
        }
    ) { paddingValues ->
        when {
            uiState.isLoading -> {
                LoadingState(
                    message = stringResource(R.string.common_loading),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
            }

            uiState.error != null -> {
                ErrorState(
                    message = uiState.error ?: stringResource(R.string.common_load_failed),
                    onRetry = { viewModel.sendIntent(DockerContainerDetailIntent.Refresh) },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
            }

            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    // Tab 栏
                    TabRow(
                        selectedTabIndex = selectedTabIndex,
                        containerColor = Color.Transparent
                    ) {
                        tabs.forEachIndexed { index, title ->
                            Tab(
                                selected = selectedTabIndex == index,
                                onClick = { selectedTabIndex = index },
                                text = { Text(title) }
                            )
                        }
                    }

                    // Tab 内容
                    when (selectedTabIndex) {
                        0 -> OverviewTab(uiState)
                        1 -> ProcessesTab(uiState)
                        2 -> LogsTab(uiState, onSelectDate = { viewModel.sendIntent(DockerContainerDetailIntent.LoadLogs(it)) })
                    }
                }
            }
        }
    }
}

@Composable
private fun OverviewTab(uiState: ContainerDetailUiState) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(Spacing.PageHorizontal, Spacing.Medium),
        verticalArrangement = Arrangement.spacedBy(Spacing.Medium)
    ) {
        // 基本信息
        item {
            InfoCard(title = "基本信息") {
                InfoRow(label = "状态", value = if (uiState.status == "running") "运行中" else "已停止")
                InfoRow(label = "启动时间", value = formatUpTime(uiState.upTime))
                InfoRow(
                    label = "CPU优先级",
                    value = when {
                        uiState.cpuPriority > 50 -> "高"
                        uiState.cpuPriority == 50 -> "中"
                        else -> "低"
                    }
                )
                InfoRow(
                    label = "内存限制",
                    value = if (uiState.memoryLimit > 0) formatSize(uiState.memoryLimit) else "自动"
                )
                if (uiState.shortcutEnabled && uiState.shortcutUrl.isNotEmpty()) {
                    InfoRow(label = "快捷方式", value = uiState.shortcutUrl)
                }
                if (uiState.command.isNotEmpty()) {
                    InfoRow(label = "执行命令", value = uiState.command, maxLines = 3)
                }
            }
        }

        // 端口设置
        if (uiState.ports.isNotEmpty()) {
            item {
                InfoCard(title = "端口设置") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        TableCell(text = "本地端口", weight = 1f, isHeader = true)
                        TableCell(text = "容器端口", weight = 1f, isHeader = true)
                        TableCell(text = "类型", weight = 0.6f, isHeader = true)
                    }
                    uiState.ports.forEach { port ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            TableCell(text = port.hostPort, weight = 1f)
                            TableCell(text = port.containerPort, weight = 1f)
                            TableCell(text = port.type, weight = 0.6f)
                        }
                    }
                }
            }
        }

        // 卷
        if (uiState.volumes.isNotEmpty()) {
            item {
                InfoCard(title = "卷") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        TableCell(text = "文件/文件夹", weight = 1.5f, isHeader = true)
                        TableCell(text = "装载路径", weight = 1.5f, isHeader = true)
                        TableCell(text = "类型", weight = 0.6f, isHeader = true)
                    }
                    uiState.volumes.forEach { volume ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            TableCell(
                                text = volume.hostVolumeFile,
                                weight = 1.5f,
                                maxLines = 1
                            )
                            TableCell(
                                text = volume.mountPoint,
                                weight = 1.5f,
                                maxLines = 1
                            )
                            TableCell(text = volume.type, weight = 0.6f)
                        }
                    }
                }
            }
        }

        // 网络
        if (uiState.networks.isNotEmpty()) {
            item {
                InfoCard(title = "网络") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        TableCell(text = "网络名称", weight = 1f, isHeader = true)
                        TableCell(text = "驱动", weight = 1f, isHeader = true)
                    }
                    uiState.networks.forEach { network ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            TableCell(text = network.name, weight = 1f)
                            TableCell(text = network.driver, weight = 1f)
                        }
                    }
                }
            }
        }

        // 链接
        if (uiState.links.isNotEmpty()) {
            item {
                InfoCard(title = "链接") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        TableCell(text = "容器名称", weight = 1f, isHeader = true)
                        TableCell(text = "别名", weight = 1f, isHeader = true)
                    }
                    uiState.links.forEach { link ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            TableCell(text = link.linkContainer, weight = 1f)
                            TableCell(text = link.alias, weight = 1f)
                        }
                    }
                }
            }
        }

        // 环境变量
        if (uiState.envVariables.isNotEmpty()) {
            item {
                InfoCard(title = "环境变量") {
                    uiState.envVariables.forEach { env ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "${env.key}:",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.weight(0.4f, fill = false)
                            )
                            Text(
                                text = env.value,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(0.6f),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProcessesTab(uiState: ContainerDetailUiState) {
    when {
        uiState.processesLoading -> {
            LoadingState(
                message = "加载进程中...",
                modifier = Modifier.fillMaxSize()
            )
        }

        uiState.processes.isEmpty() -> {
            EmptyState(
                message = "没有进程信息",
                modifier = Modifier.fillMaxSize()
            )
        }

        else -> {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(Spacing.PageHorizontal, Spacing.Medium),
                verticalArrangement = Arrangement.spacedBy(Spacing.MediumSmall)
            ) {
                items(uiState.processes, key = { it.pid }) { process ->
                    ProcessCard(process)
                }
            }
        }
    }
}

@Composable
private fun ProcessCard(process: ContainerProcess) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = AppShapes.Card,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.cardSurface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.CardPadding)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.Medium)
            ) {
                Text(
                    text = "进程: ${process.pid}",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Text(
                    text = "CPU: ${String.format("%.1f", process.cpu)}%",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Text(
                    text = "RAM: ${formatSize(process.memory)}",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
            if (process.command.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = process.command,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun LogsTab(
    uiState: ContainerDetailUiState,
    onSelectDate: (String) -> Unit
) {
    if (uiState.logDates.isEmpty()) {
        EmptyState(
            message = "没有日志",
            modifier = Modifier.fillMaxSize()
        )
        return
    }

    Row(modifier = Modifier.fillMaxSize()) {
        // 日期选择列表
        LazyColumn(
            modifier = Modifier
                .width(80.dp)
                .fillMaxHeight()
                .background(MaterialTheme.colorScheme.surfaceContainerLow),
            contentPadding = PaddingValues(vertical = Spacing.Small),
            verticalArrangement = Arrangement.spacedBy(Spacing.Small)
        ) {
            items(uiState.logDates) { date ->
                DateChip(
                    date = date,
                    isSelected = date == uiState.selectedLogDate,
                    onClick = { onSelectDate(date) }
                )
            }
        }

        // 日志列表
        Box(modifier = Modifier.weight(1f)) {
            when {
                uiState.logsLoading -> {
                    LoadingState(
                        message = "加载日志中...",
                        modifier = Modifier.fillMaxSize()
                    )
                }

                uiState.logs.isEmpty() -> {
                    EmptyState(
                        message = "没有日志记录",
                        modifier = Modifier.fillMaxSize()
                    )
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(Spacing.Small),
                        verticalArrangement = Arrangement.spacedBy(Spacing.Small)
                    ) {
                        items(uiState.logs.reversed()) { log ->
                            LogCard(log)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DateChip(
    date: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        shape = RoundedCornerShape(CornerRadius.Small),
        color = if (isSelected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surface
        },
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (date.length >= 5) {
                Text(
                    text = date.substring(5),
                    style = MaterialTheme.typography.labelMedium
                )
                Text(
                    text = date.substring(0, 4),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Text(
                    text = date,
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }
}

@Composable
private fun LogCard(log: ContainerLog) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(CornerRadius.Small),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.Small)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(CornerRadius.ExtraSmall),
                    color = if (log.stream == "stdout") {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.errorContainer
                }
                ) {
                    Text(
                        text = log.stream,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        color = if (log.stream == "stdout") {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onErrorContainer
                        }
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = log.created,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (log.text.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = log.text,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun InfoCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = AppShapes.Card,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.cardSurface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.CardPadding)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(Spacing.Small))
            content()
        }
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String,
    maxLines: Int = 1
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = maxLines,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, fill = false)
        )
    }
}

@Composable
private fun RowScope.TableCell(
    text: String,
    weight: Float,
    isHeader: Boolean = false,
    maxLines: Int = 1
) {
    Text(
        text = text,
        style = if (isHeader) MaterialTheme.typography.labelMedium else MaterialTheme.typography.bodySmall,
        fontWeight = if (isHeader) FontWeight.Medium else FontWeight.Normal,
        color = if (isHeader) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier
            .weight(weight)
            .padding(vertical = 4.dp),
        textAlign = androidx.compose.ui.text.style.TextAlign.Center
    )
}

// 辅助函数
private fun formatUpTime(upTime: Long): String {
    if (upTime <= 0) return "未启动"
    val now = System.currentTimeMillis() / 1000
    val diff = now - upTime
    val hours = diff / 3600
    val days = hours / 24
    return when {
        days > 0 -> "$days 天前"
        hours > 0 -> "$hours 小时前"
        else -> "${diff / 60} 分钟前"
    }
}

private fun formatSize(bytes: Long): String {
    if (bytes < 1024) return "$bytes B"
    val kb = bytes / 1024.0
    if (kb < 1024) return String.format("%.1f KB", kb)
    val mb = kb / 1024.0
    if (mb < 1024) return String.format("%.1f MB", mb)
    val gb = mb / 1024.0
    return String.format("%.1f GB", gb)
}
