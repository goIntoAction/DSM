package wang.zengye.dsm.ui.download

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import wang.zengye.dsm.R
import wang.zengye.dsm.ui.components.EmptyState
import wang.zengye.dsm.ui.components.ErrorState
import wang.zengye.dsm.ui.components.LoadingState

/**
 * Peer列表页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PeerListScreen(
    taskId: String = "",
    taskName: String = "",
    onNavigateBack: () -> Unit = {},
    viewModel: PeerListViewModel = hiltViewModel()
) {
    val uiState by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(taskId) {
        if (taskId.isNotEmpty()) {
            viewModel.sendIntent(PeerListIntent.SetTask(taskId, taskName))
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(stringResource(R.string.download_peer_list))
                        if (uiState.taskName.isNotEmpty()) {
                            Text(
                                text = uiState.taskName,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.common_back))
                    }
                },
                actions = {
                    // 自动刷新开关
                    IconButton(onClick = { viewModel.sendIntent(PeerListIntent.ToggleAutoRefresh) }) {
                        Icon(
                            if (uiState.autoRefresh) Icons.Default.Sync else Icons.Default.SyncDisabled,
                            contentDescription = if (uiState.autoRefresh) stringResource(R.string.download_stop_auto_refresh) else stringResource(R.string.download_start_auto_refresh),
                            tint = if (uiState.autoRefresh)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    // 手动刷新
                    IconButton(onClick = { viewModel.sendIntent(PeerListIntent.LoadPeers) }) {
                        Icon(Icons.Default.Refresh, contentDescription = stringResource(R.string.dashboard_refresh))
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
            // 排序选择
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SortChip(
                    label = stringResource(R.string.download_sort_progress),
                    selected = uiState.sortType == "progress",
                    onClick = { viewModel.sendIntent(PeerListIntent.SetSortType("progress")) }
                )
                SortChip(
                    label = stringResource(R.string.download_sort_download),
                    selected = uiState.sortType == "download",
                    onClick = { viewModel.sendIntent(PeerListIntent.SetSortType("download")) }
                )
                SortChip(
                    label = stringResource(R.string.download_sort_upload),
                    selected = uiState.sortType == "upload",
                    onClick = { viewModel.sendIntent(PeerListIntent.SetSortType("upload")) }
                )
                SortChip(
                    label = "IP",
                    selected = uiState.sortType == "ip",
                    onClick = { viewModel.sendIntent(PeerListIntent.SetSortType("ip")) }
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
                        text = stringResource(R.string.download_peer_total, uiState.peers.size),
                        style = MaterialTheme.typography.bodyMedium
                    )

                    val totalDownload = uiState.peers.sumOf { it.downloadSpeed }
                    val totalUpload = uiState.peers.sumOf { it.uploadSpeed }

                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.ArrowDownward,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = formatSpeed(totalDownload),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.ArrowUpward,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.tertiary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = formatSpeed(totalUpload),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }

            when {
                uiState.isLoading && uiState.peers.isEmpty() -> {
                    LoadingState(
                        message = stringResource(R.string.download_loading_peers),
                        modifier = Modifier.fillMaxSize()
                    )
                }

                uiState.error != null && uiState.peers.isEmpty() -> {
                    ErrorState(
                        message = uiState.error ?: stringResource(R.string.components_load_failed),
                        onRetry = { viewModel.sendIntent(PeerListIntent.LoadPeers) },
                        modifier = Modifier.fillMaxSize()
                    )
                }

                uiState.peers.isEmpty() -> {
                    EmptyState(
                        message = stringResource(R.string.download_no_peers),
                        modifier = Modifier.fillMaxSize()
                    )
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        items(
                            items = uiState.peers,
                            key = { "${it.ip}:${it.port}" }
                        ) { peer ->
                            PeerItem(peer = peer)
                        }
                    }
                }
            }
        }
    }
}
