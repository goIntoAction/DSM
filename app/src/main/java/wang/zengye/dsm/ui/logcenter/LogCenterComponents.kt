package wang.zengye.dsm.ui.logcenter

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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

@Composable
internal fun RecentLogsTab(uiState: LogCenterUiState) {
    if (uiState.isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else if (uiState.error != null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = uiState.error ?: stringResource(R.string.dashboard_load_failed),
                color = MaterialTheme.colorScheme.error
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(uiState.recentLogs) { log ->
                LogEntryCard(log)
            }
        }
    }
}

@Composable
internal fun HistoryTab(uiState: LogCenterUiState) {
    if (uiState.isLoadingHistory) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else if (uiState.histories.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.History,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.log_no_history),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(uiState.histories) { log ->
                LogEntryCard(log)
            }
        }
    }
}

@Composable
internal fun LogsTab(uiState: LogCenterUiState, viewModel: LogCenterViewModel) {
    var searchText by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize()) {
        // 搜索栏
        OutlinedTextField(
            value = searchText,
            onValueChange = {
                searchText = it
                viewModel.sendIntent(LogCenterIntent.SetSearchQuery(it))
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            placeholder = { Text(stringResource(R.string.log_search_placeholder)) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (searchText.isNotEmpty()) {
                    IconButton(onClick = {
                        searchText = ""
                        viewModel.sendIntent(LogCenterIntent.SetSearchQuery(""))
                    }) {
                        Icon(Icons.Default.Clear, contentDescription = stringResource(R.string.file_search_clear))
                    }
                }
            },
            singleLine = true
        )

        // 日志类型选择器
        ScrollableTabRow(
            selectedTabIndex = uiState.selectedLogType,
            modifier = Modifier.fillMaxWidth()
        ) {
            uiState.logTypeNames.forEachIndexed { index, nameRes ->
                Tab(
                    selected = uiState.selectedLogType == index,
                    onClick = { viewModel.sendIntent(LogCenterIntent.LoadLogs(index)) },
                    text = { Text(stringResource(nameRes)) }
                )
            }
        }

        // 级别筛选芯片
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            uiState.levelOptions.forEach { levelRes ->
                val level = stringResource(levelRes)
                FilterChip(
                    selected = uiState.selectedLevel == level || (levelRes == R.string.log_level_all && uiState.selectedLevel == null),
                    onClick = {
                        viewModel.sendIntent(LogCenterIntent.SetLevelFilter(if (levelRes == R.string.log_level_all) null else level))
                    },
                    label = {
                        Text(when (levelRes) {
                            R.string.log_level_all -> "${stringResource(R.string.common_all)} (${uiState.logs.size})"
                            R.string.log_level_error -> "${stringResource(R.string.log_level_error)} (${uiState.errorCount})"
                            R.string.log_level_warning -> "${stringResource(R.string.log_level_warning)} (${uiState.warnCount})"
                            R.string.log_level_info -> "${stringResource(R.string.log_level_info)} (${uiState.infoCount})"
                            else -> level
                        })
                    },
                    leadingIcon = if (levelRes != R.string.log_level_all) {
                        {
                            Icon(
                                imageVector = when (levelRes) {
                                    R.string.log_level_error -> Icons.Default.Error
                                    R.string.log_level_warning -> Icons.Default.Warning
                                    R.string.log_level_info -> Icons.Default.Info
                                    else -> Icons.Default.Circle
                                },
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = when (levelRes) {
                                    R.string.log_level_error -> MaterialTheme.colorScheme.error
                                    R.string.log_level_warning -> MaterialTheme.colorScheme.tertiary
                                    R.string.log_level_info -> MaterialTheme.colorScheme.primary
                                    else -> MaterialTheme.colorScheme.onSurface
                                }
                            )
                        }
                    } else null
                )
            }
        }

        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (uiState.filteredLogs.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.SearchOff,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = if (uiState.searchQuery.isNotEmpty() || uiState.selectedLevel != null)
                            stringResource(R.string.log_no_matching_logs)
                        else stringResource(R.string.log_no_logs),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.filteredLogs) { log ->
                    LogEntryCard(log)
                }
            }
        }
    }
}

@Composable
internal fun StatisticsTab(uiState: LogCenterUiState) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(R.string.log_statistics),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            StatCard(
                title = stringResource(R.string.log_level_error),
                count = uiState.errorCount,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = stringResource(R.string.log_level_warning),
                count = uiState.warnCount,
                color = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = stringResource(R.string.log_level_info),
                count = uiState.infoCount,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = stringResource(R.string.file_detail),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.log_statistics_description),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
internal fun StatCard(
    title: String,
    count: Int,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
internal fun LogEntryCard(log: LogEntry) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 日志级别标签
                Surface(
                    color = when (log.level) {
                        "error", "critical" -> MaterialTheme.colorScheme.errorContainer
                        "warning", "warn" -> MaterialTheme.colorScheme.tertiaryContainer
                        "info", "notice" -> MaterialTheme.colorScheme.primaryContainer
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    },
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = log.level.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                Text(
                    text = log.time,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = log.message,
                style = MaterialTheme.typography.bodyMedium
            )

            if (log.user.isNotEmpty() || log.category.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = listOfNotNull(
                        log.category.takeIf { it.isNotEmpty() },
                        log.user.takeIf { it.isNotEmpty() }
                    ).joinToString(" | "),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
