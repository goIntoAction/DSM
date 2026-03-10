package wang.zengye.dsm.ui.download

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import wang.zengye.dsm.R
import wang.zengye.dsm.ui.components.*
import wang.zengye.dsm.ui.theme.*
import wang.zengye.dsm.util.formatSize

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadScreen(
    onNavigateToPeerList: (String) -> Unit = {},
    onNavigateToTrackerManager: (String) -> Unit = {},
    onNavigateToAddTask: () -> Unit = {},
    onNavigateToTaskDetail: (String) -> Unit = {},
    viewModel: DownloadViewModel = hiltViewModel()
) {
    val uiState by viewModel.state.collectAsStateWithLifecycle()
    val filteredTasks = remember(uiState.tasks, uiState.filter) {
        val tasks = uiState.tasks
        when (uiState.filter) {
            "downloading" -> tasks.filter { it.isDownloading }
            "finished" -> tasks.filter { it.isFinished }
            "paused" -> tasks.filter { it.isPaused }
            "error" -> tasks.filter { it.isError }
            else -> tasks
        }
    }

    // 使用 remember 缓存回调函数
    val onRefresh = remember(viewModel) {
        { viewModel.sendIntent(DownloadIntent.Refresh) }
    }

    val onSetFilterAll = remember(viewModel) {
        { viewModel.sendIntent(DownloadIntent.SetFilter("all")) }
    }

    val onSetFilterDownloading = remember(viewModel) {
        { viewModel.sendIntent(DownloadIntent.SetFilter("downloading")) }
    }

    val onSetFilterFinished = remember(viewModel) {
        { viewModel.sendIntent(DownloadIntent.SetFilter("finished")) }
    }

    val onSetFilterPaused = remember(viewModel) {
        { viewModel.sendIntent(DownloadIntent.SetFilter("paused")) }
    }

    Scaffold(
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.download_management),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Medium
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = Color.Transparent
                ),
                actions = {
                    if (uiState.isRefreshing) {
                        CircularProgressIndicator(
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                    }
                    IconButton(
                        onClick = onRefresh,
                        enabled = !uiState.isRefreshing
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Refresh,
                            contentDescription = stringResource(R.string.dashboard_refresh)
                        )
                    }
                    IconButton(onClick = onNavigateToAddTask) {
                        Icon(
                            imageVector = Icons.Outlined.Add,
                            contentDescription = stringResource(R.string.download_add_task)
                        )
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
            // 统计信息卡片
            if (uiState.statistic.totalTask > 0) {
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.PageHorizontal, vertical = Spacing.Small),
                    shape = AppShapes.Card
                ) {
                    Column(
                        modifier = Modifier.padding(Spacing.CardPadding)
                    ) {
                        // 速度显示
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            SpeedIndicator(
                                icon = Icons.Filled.ArrowDownward,
                                label = stringResource(R.string.dashboard_download),
                                speed = "${formatSize(uiState.downloadSpeed)}/s",
                                color = MaterialTheme.colorScheme.primary
                            )

                            SpeedIndicator(
                                icon = Icons.Filled.ArrowUpward,
                                label = stringResource(R.string.dashboard_upload),
                                speed = "${formatSize(uiState.uploadSpeed)}/s",
                                color = MaterialTheme.colorScheme.tertiary
                            )
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = Spacing.Standard),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )

                        // 统计数字
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            StatisticItem(
                                label = stringResource(R.string.download_downloading),
                                count = uiState.statistic.downloading,
                                color = MaterialTheme.colorScheme.primary
                            )
                            StatisticItem(
                                label = stringResource(R.string.download_finished),
                                count = uiState.statistic.finished,
                                color = MaterialTheme.success
                            )
                            StatisticItem(
                                label = stringResource(R.string.download_paused),
                                count = uiState.statistic.paused,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                            StatisticItem(
                                label = stringResource(R.string.common_error),
                                count = uiState.statistic.error,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }

            // 筛选标签 - 可横向滚动
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = Spacing.PageHorizontal, vertical = Spacing.Small),
                horizontalArrangement = Arrangement.spacedBy(Spacing.Small)
            ) {
                FilterChip(
                    selected = uiState.filter == "all",
                    onClick = onSetFilterAll,
                    label = { Text(stringResource(R.string.download_all_with_count, uiState.statistic.totalTask)) }
                )
                FilterChip(
                    selected = uiState.filter == "downloading",
                    onClick = onSetFilterDownloading,
                    label = { Text(stringResource(R.string.download_downloading_with_count, uiState.statistic.downloading)) }
                )
                FilterChip(
                    selected = uiState.filter == "finished",
                    onClick = onSetFilterFinished,
                    label = { Text(stringResource(R.string.download_finished_with_count, uiState.statistic.finished)) }
                )
                FilterChip(
                    selected = uiState.filter == "paused",
                    onClick = onSetFilterPaused,
                    label = { Text(stringResource(R.string.download_paused_with_count, uiState.statistic.paused)) }
                )
            }

            when {
                uiState.isLoading -> {
                    LoadingState(
                        message = stringResource(R.string.download_loading_tasks),
                        modifier = Modifier.fillMaxSize()
                    )
                }

                uiState.error != null -> {
                    ErrorState(
                        message = uiState.error ?: stringResource(R.string.dashboard_load_failed),
                        onRetry = onRefresh,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                !uiState.isLoading && filteredTasks.isEmpty() -> {
                    val filterName = when(uiState.filter) {
                        "downloading" -> stringResource(R.string.download_downloading)
                        "finished" -> stringResource(R.string.download_finished)
                        "paused" -> stringResource(R.string.download_paused)
                        "error" -> stringResource(R.string.common_error)
                        else -> ""
                    }
                    EmptyState(
                        message = if (uiState.filter == "all") stringResource(R.string.download_no_tasks) else stringResource(R.string.download_no_filter_tasks, filterName),
                        icon = Icons.Outlined.Download,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = Spacing.Small)
                    ) {
                        items(filteredTasks, key = { it.id }) { task ->
                            val onToggleClick = remember(viewModel, task) {
                                {
                                    if (task.isDownloading) {
                                        viewModel.sendIntent(DownloadIntent.PauseTask(task.id))
                                    } else {
                                        viewModel.sendIntent(DownloadIntent.ResumeTask(task.id))
                                    }
                                }
                            }
                            val onDeleteClick = remember(viewModel, task) {
                                { viewModel.sendIntent(DownloadIntent.DeleteTask(task.id)) }
                            }
                            val onTaskClick = remember(onNavigateToTaskDetail, task) {
                                { onNavigateToTaskDetail(task.id) }
                            }
                            DownloadTaskItem(
                                task = task,
                                onToggleClick = onToggleClick,
                                onDeleteClick = onDeleteClick,
                                onClick = onTaskClick
                            )
                        }

                        // 底部间距
                        item {
                            Spacer(modifier = Modifier.height(Spacing.ExtraLarge))
                        }
                    }
                }
            }
        }
    }
}
