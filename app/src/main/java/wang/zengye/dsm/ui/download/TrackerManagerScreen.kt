package wang.zengye.dsm.ui.download

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import wang.zengye.dsm.R
import wang.zengye.dsm.ui.components.EmptyState
import wang.zengye.dsm.ui.components.ErrorState
import wang.zengye.dsm.ui.components.LoadingState

/**
 * Tracker管理页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackerManagerScreen(
    taskId: String,
    onNavigateBack: () -> Unit = {},
    viewModel: TrackerManagerViewModel = hiltViewModel()
) {
    val uiState by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(taskId) {
        viewModel.sendIntent(TrackerManagerIntent.LoadTrackers(taskId))
    }

    // 添加Tracker对话框
    if (uiState.showAddDialog) {
        AddTrackerDialog(
            trackerUrl = uiState.newTrackerUrl,
            isAdding = uiState.isAdding,
            onUrlChange = { viewModel.sendIntent(TrackerManagerIntent.UpdateNewTrackerUrl(it)) },
            onConfirm = { viewModel.sendIntent(TrackerManagerIntent.AddTracker(taskId)) },
            onDismiss = { viewModel.sendIntent(TrackerManagerIntent.HideDialog) }
        )
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.download_tracker_management)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.common_back))
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.sendIntent(TrackerManagerIntent.LoadTrackers(taskId)) }) {
                        Icon(Icons.Default.Refresh, contentDescription = stringResource(R.string.dashboard_refresh))
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.sendIntent(TrackerManagerIntent.ShowAddDialog) }) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.download_add_tracker))
            }
        }
    ) { paddingValues ->
        when {
            uiState.isLoading -> {
                LoadingState(
                    message = stringResource(R.string.download_loading_trackers),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
            }

            uiState.error != null && uiState.trackers.isEmpty() -> {
                ErrorState(
                    message = uiState.error ?: stringResource(R.string.components_load_failed),
                    onRetry = { viewModel.sendIntent(TrackerManagerIntent.LoadTrackers(taskId)) },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
            }

            uiState.trackers.isEmpty() -> {
                EmptyState(
                    message = stringResource(R.string.download_no_trackers),
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
                    // 错误提示
                    if (uiState.error != null) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            )
                        ) {
                            Text(
                                text = uiState.error!!,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        items(
                            items = uiState.trackers,
                            key = { it.url }
                        ) { tracker ->
                            TrackerItem(
                                tracker = tracker
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Tracker项
 */
@Composable
private fun TrackerItem(
    tracker: TrackerItem
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
                .padding(16.dp)
        ) {
            // URL
            Text(
                text = tracker.url,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 状态和信息
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 状态
                AssistChip(
                    onClick = {},
                    label = {
                        Text(
                            when (tracker.status) {
                                "working" -> stringResource(R.string.download_tracker_status_working)
                                "error" -> stringResource(R.string.download_tracker_status_error)
                                "updating" -> stringResource(R.string.download_tracker_status_updating)
                                "waiting" -> stringResource(R.string.download_tracker_status_waiting)
                                else -> tracker.status
                            },
                            style = MaterialTheme.typography.labelSmall
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = when (tracker.status) {
                                "working" -> Icons.Default.Check
                                "error" -> Icons.Default.Error
                                "updating" -> Icons.Default.Sync
                                else -> Icons.Default.Schedule
                            },
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                    },
                    modifier = Modifier.height(28.dp)
                )

                // 种子和peer数
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Grain,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${tracker.seeds}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Group,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${tracker.peers}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

/**
 * 添加Tracker对话框
 */
@Composable
private fun AddTrackerDialog(
    trackerUrl: String,
    isAdding: Boolean,
    onUrlChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.download_add_tracker)) },
        text = {
            Column {
                OutlinedTextField(
                    value = trackerUrl,
                    onValueChange = onUrlChange,
                    label = { Text(stringResource(R.string.download_tracker_url_label)) },
                    placeholder = { Text(stringResource(R.string.download_tracker_url_placeholder)) },
                    singleLine = false,
                    minLines = 3,
                    maxLines = 5,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                enabled = trackerUrl.isNotBlank() && !isAdding
            ) {
                if (isAdding) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(stringResource(R.string.common_add))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.common_cancel))
            }
        }
    )
}
