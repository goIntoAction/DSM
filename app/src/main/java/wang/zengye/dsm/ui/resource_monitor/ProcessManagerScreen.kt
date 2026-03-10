package wang.zengye.dsm.ui.resource_monitor

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import wang.zengye.dsm.ui.components.EmptyState
import wang.zengye.dsm.ui.components.ErrorState
import wang.zengye.dsm.util.formatSize
import androidx.compose.ui.res.stringResource
import wang.zengye.dsm.R
import androidx.compose.ui.graphics.StrokeCap

/**
 * 进程管理页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProcessManagerScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: ProcessManagerViewModel = hiltViewModel()
) {
    val uiState by viewModel.state.collectAsStateWithLifecycle()
    
    // 过滤后的进程列表
    val filteredProcesses = remember(uiState.processes, uiState.searchQuery) {
        if (uiState.searchQuery.isEmpty()) {
            uiState.processes
        } else {
            uiState.processes.filter { 
                it.name.contains(uiState.searchQuery, ignoreCase = true) ||
                it.command.contains(uiState.searchQuery, ignoreCase = true) ||
                it.user.contains(uiState.searchQuery, ignoreCase = true)
            }
        }
    }
    
    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.resmon_process_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.common_back))
                    }
                },
                actions = {
                    // 自动刷新开关
                    IconButton(onClick = { viewModel.sendIntent(ProcessManagerIntent.ToggleAutoRefresh) }) {
                        Icon(
                            if (uiState.autoRefresh) Icons.Default.Sync else Icons.Default.SyncDisabled,
                            contentDescription = if (uiState.autoRefresh) stringResource(R.string.resmon_stop_auto_refresh) else stringResource(R.string.resmon_start_auto_refresh),
                            tint = if (uiState.autoRefresh) 
                                MaterialTheme.colorScheme.primary 
                            else 
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    // 手动刷新
                    IconButton(
                        onClick = { viewModel.sendIntent(ProcessManagerIntent.Refresh) },
                        enabled = !uiState.isRefreshing
                    ) {
                        if (uiState.isRefreshing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Default.Refresh, contentDescription = stringResource(R.string.common_refresh))
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 搜索框
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = { viewModel.sendIntent(ProcessManagerIntent.SetSearchQuery(it)) },
                placeholder = { Text(stringResource(R.string.resmon_search_process)) },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null)
                },
                trailingIcon = {
                    if (uiState.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.sendIntent(ProcessManagerIntent.SetSearchQuery("")) }) {
                            Icon(Icons.Default.Clear, contentDescription = stringResource(R.string.common_close))
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )
            
            // 排序选择
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SortChip(
                    label = "CPU",
                    selected = uiState.sortBy == "cpu",
                    onClick = { viewModel.sendIntent(ProcessManagerIntent.SetSortBy("cpu")) }
                )
                SortChip(
                    label = stringResource(R.string.resmon_sort_memory),
                    selected = uiState.sortBy == "memory",
                    onClick = { viewModel.sendIntent(ProcessManagerIntent.SetSortBy("memory")) }
                )
                SortChip(
                    label = stringResource(R.string.resmon_sort_name),
                    selected = uiState.sortBy == "name",
                    onClick = { viewModel.sendIntent(ProcessManagerIntent.SetSortBy("name")) }
                )
                SortChip(
                    label = "PID",
                    selected = uiState.sortBy == "pid",
                    onClick = { viewModel.sendIntent(ProcessManagerIntent.SetSortBy("pid")) }
                )
            }
            
            // 统计信息
            Surface(
                modifier = Modifier.fillMaxWidth(),
                tonalElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(R.string.resmon_process_count, filteredProcesses.size),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    if (uiState.autoRefresh) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(12.dp),
                                strokeWidth = 1.dp
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = stringResource(R.string.resmon_auto_refreshing),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
            
            // 错误提示
            if (uiState.error != null && uiState.processes.isEmpty()) {
                ErrorState(
                    message = uiState.error ?: stringResource(R.string.common_load_failed),
                    onRetry = { viewModel.sendIntent(ProcessManagerIntent.LoadProcesses) },
                    modifier = Modifier.fillMaxSize()
                )
                return@Scaffold
            }
            
            if (filteredProcesses.isEmpty()) {
                EmptyState(
                    message = if (uiState.searchQuery.isNotEmpty()) stringResource(R.string.resmon_no_match_process) else stringResource(R.string.resmon_no_process),
                    modifier = Modifier.fillMaxSize()
                )
                return@Scaffold
            }
            
            // 进程列表
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(
                    items = filteredProcesses,
                    key = { it.pid }
                ) { process ->
                    ProcessItemCard(
                        process = process
                    )
                }
            }
        }
    }
}

/**
 * 排序选择芯片
 */
@Composable
internal fun SortChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
        leadingIcon = {
            if (selected) {
                Icon(
                    Icons.Default.ArrowDownward,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    )
}

/**
 * 进程项
 */
@Composable
internal fun ProcessItemCard(
    process: ProcessItem
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // 第一行：进程名和PID
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = process.name,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "PID: ${process.pid}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // 第二行：用户和状态
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = process.user,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                AssistChip(
                    onClick = {},
                    label = { 
                        Text(
                            when (process.state) {
                                "R" -> stringResource(R.string.resmon_state_running)
                                "S" -> stringResource(R.string.resmon_state_sleeping)
                                "D" -> stringResource(R.string.resmon_state_waiting)
                                "Z" -> stringResource(R.string.resmon_state_zombie)
                                "T" -> stringResource(R.string.resmon_state_stopped)
                                else -> process.state
                            },
                            style = MaterialTheme.typography.labelSmall
                        ) 
                    },
                    modifier = Modifier.height(20.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // 第三行：CPU和内存使用
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // CPU
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "CPU",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        LinearProgressIndicator(
                            progress = { (process.cpu.toFloat() / 100f).coerceIn(0f, 1f) },
                            modifier = Modifier
                                .weight(1f)
                                .height(4.dp),
                            color = when {
                                process.cpu > 80 -> MaterialTheme.colorScheme.error
                                process.cpu > 50 -> MaterialTheme.colorScheme.tertiary
                                else -> MaterialTheme.colorScheme.primary
                            },
                    strokeCap = StrokeCap.Butt,
                    gapSize = 0.dp,
                    drawStopIndicator = {})
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = String.format("%.1f%%", process.cpu),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
                
                // 内存
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.resmon_sort_memory),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        LinearProgressIndicator(
                            progress = { (process.memoryPercent.toFloat() / 100f).coerceIn(0f, 1f) },
                            modifier = Modifier
                                .weight(1f)
                                .height(4.dp),
                            color = when {
                                process.memoryPercent > 80 -> MaterialTheme.colorScheme.error
                                process.memoryPercent > 50 -> MaterialTheme.colorScheme.tertiary
                                else -> MaterialTheme.colorScheme.primary
                            },
                    strokeCap = StrokeCap.Butt,
                    gapSize = 0.dp,
                    drawStopIndicator = {})
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = formatSize(process.memory),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}

