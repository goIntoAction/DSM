package wang.zengye.dsm.ui.download

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import wang.zengye.dsm.R
import wang.zengye.dsm.ui.components.ErrorState
import wang.zengye.dsm.ui.components.LoadingState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadTaskDetailScreen(
    taskId: String,
    onNavigateBack: () -> Unit,
    onNavigateToTrackerManager: (String) -> Unit = {},
    onNavigateToPeerList: (String) -> Unit = {},
    viewModel: TaskDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(taskId) {
        viewModel.sendIntent(TaskDetailIntent.LoadTaskDetail(taskId))
    }

    // 删除确认对话框
    if (uiState.showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.sendIntent(TaskDetailIntent.HideDeleteDialog) },
            title = { Text(stringResource(R.string.download_delete_task)) },
            text = { Text(stringResource(R.string.download_delete_task_confirm)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.sendIntent(TaskDetailIntent.DeleteTask(taskId) {
                            onNavigateBack()
                        })
                    },
                    enabled = !uiState.isDeleting
                ) {
                    if (uiState.isDeleting) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                    } else {
                        Text(stringResource(R.string.common_delete), color = MaterialTheme.colorScheme.error)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.sendIntent(TaskDetailIntent.HideDeleteDialog) }) {
                    Text(stringResource(R.string.common_cancel))
                }
            }
        )
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.download_task_detail)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.common_back))
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.sendIntent(TaskDetailIntent.ShowDeleteDialog) }) {
                        Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.common_delete))
                    }
                },
            )
        }
    ) { paddingValues ->
        when {
            uiState.isLoading -> {
                LoadingState(
                    message = stringResource(R.string.common_loading),
                    modifier = Modifier.fillMaxSize().padding(paddingValues)
                )
            }
            uiState.error != null -> {
                ErrorState(
                    message = uiState.error ?: stringResource(R.string.common_load_failed),
                    onRetry = { viewModel.sendIntent(TaskDetailIntent.LoadTaskDetail(taskId)) },
                    modifier = Modifier.fillMaxSize().padding(paddingValues)
                )
            }
            uiState.task != null -> {
                val task = uiState.task!!
                val isBtDownloading = task.type == "bt" && task.status == 2

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    // Tab 栏
                    val tabs = if (isBtDownloading) {
                        listOf(stringResource(R.string.download_tab_general), stringResource(R.string.download_tab_transfer), stringResource(R.string.download_tab_tracker), stringResource(R.string.download_tab_peer), stringResource(R.string.download_tab_files))
                    } else {
                        listOf(stringResource(R.string.download_tab_general), stringResource(R.string.download_tab_transfer))
                    }

                    val pagerState = rememberPagerState(pageCount = { tabs.size })

                    ScrollableTabRow(
                        selectedTabIndex = pagerState.currentPage,
                        edgePadding = 16.dp
                    ) {
                        tabs.forEachIndexed { index, title ->
                            Tab(
                                selected = pagerState.currentPage == index,
                                onClick = {
                                    // 使用 coroutine scope 来滚动
                                },
                                text = { Text(title) }
                            )
                        }
                    }

                    // 内容区域
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize()
                    ) { page ->
                        when (page) {
                            0 -> GeneralTab(task, context)
                            1 -> TransferTab(task)
                            2 -> if (isBtDownloading) {
                                TrackersTab(
                                    trackers = uiState.trackers,
                                    isLoading = uiState.isLoadingTrackers,
                                    onNavigateToTrackerManager = { onNavigateToTrackerManager(taskId) }
                                )
                            } else Unit
                            3 -> if (isBtDownloading) {
                                PeersTab(
                                    peers = uiState.peers,
                                    isLoading = uiState.isLoadingPeers,
                                    onNavigateToPeerList = { onNavigateToPeerList(taskId) }
                                )
                            } else Unit
                            4 -> if (isBtDownloading) {
                                FilesTab(
                                    files = uiState.files,
                                    isLoading = uiState.isLoadingFiles
                                )
                            } else Unit
                        }
                    }
                }
            }
        }
    }
}
