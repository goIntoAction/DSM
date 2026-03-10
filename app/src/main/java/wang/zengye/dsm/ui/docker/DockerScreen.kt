package wang.zengye.dsm.ui.docker
import androidx.compose.ui.graphics.Color

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import wang.zengye.dsm.R
import wang.zengye.dsm.data.model.DockerContainer
import wang.zengye.dsm.ui.components.EmptyState
import wang.zengye.dsm.ui.components.ErrorState
import wang.zengye.dsm.ui.components.LoadingState
import wang.zengye.dsm.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DockerScreen(
    onNavigateToImages: () -> Unit = {},
    onNavigateToNetworks: () -> Unit = {},
    onNavigateToContainerDetail: (String) -> Unit = {},
    viewModel: DockerViewModel = hiltViewModel()
) {
    val uiState by viewModel.state.collectAsStateWithLifecycle()
    var showMenu by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    
    // 显示操作结果消息
    LaunchedEffect(uiState.operationError, uiState.operationSuccess) {
        uiState.operationError?.let { message ->
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Short
            )
            viewModel.sendIntent(DockerIntent.ClearOperationMessage)
        }
        uiState.operationSuccess?.let { message ->
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Short
            )
            viewModel.sendIntent(DockerIntent.ClearOperationMessage)
        }
    }
    
    val runningCount = uiState.containers.count { it.isRunning }
    val stoppedCount = uiState.containers.size - runningCount

    Scaffold(
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.docker_containers)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = Color.Transparent
                ),
                actions = {
                    IconButton(onClick = { viewModel.sendIntent(DockerIntent.Refresh) }) {
                        Icon(Icons.Filled.Refresh, contentDescription = stringResource(R.string.dashboard_refresh))
                    }
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Filled.MoreVert, contentDescription = stringResource(R.string.docker_more))
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.docker_image_management)) },
                            onClick = {
                                showMenu = false
                                onNavigateToImages()
                            },
                            leadingIcon = { Icon(Icons.Filled.Layers, contentDescription = null) }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.docker_network_management)) },
                            onClick = {
                                showMenu = false
                                onNavigateToNetworks()
                            },
                            leadingIcon = { Icon(Icons.Filled.Hub, contentDescription = null) }
                        )
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
                    message = uiState.error ?: stringResource(R.string.dashboard_load_failed),
                    onRetry = { viewModel.sendIntent(DockerIntent.Refresh) },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
            }

            uiState.containers.isEmpty() -> {
                EmptyState(
                    message = stringResource(R.string.docker_no_containers),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(Spacing.PageHorizontal),
                    verticalArrangement = Arrangement.spacedBy(Spacing.CardSpacing)
                ) {
                    // 容器统计 Chip
                    item {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(Spacing.MediumSmall)
                        ) {
                            AssistChip(
                                onClick = {},
                                label = { Text(stringResource(R.string.docker_running_count, runningCount)) },
                                leadingIcon = {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(RoundedCornerShape(CornerRadius.Full))
                                            .background(MaterialTheme.colorScheme.primary)
                                    )
                                },
                                shape = RoundedCornerShape(CornerRadius.Full)
                            )
                            AssistChip(
                                onClick = {},
                                label = { Text(stringResource(R.string.docker_stopped_count, stoppedCount)) },
                                leadingIcon = {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(RoundedCornerShape(CornerRadius.Full))
                                            .background(MaterialTheme.colorScheme.outline)
                                    )
                                },
                                shape = RoundedCornerShape(CornerRadius.Full)
                            )
                        }
                    }

                    items(uiState.containers, key = { it.name }) { container ->
                        DockerContainerItem(
                            container = container,
                            isLoading = uiState.operatingContainer == container.name,
                            onStartClick = { viewModel.sendIntent(DockerIntent.StartContainer(container.name)) },
                            onStopClick = { viewModel.sendIntent(DockerIntent.StopContainer(container.name)) },
                            onRestartClick = { viewModel.sendIntent(DockerIntent.RestartContainer(container.name)) },
                            onClick = { onNavigateToContainerDetail(container.name) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DockerContainerItem(
    container: DockerContainer,
    isLoading: Boolean = false,
    onStartClick: () -> Unit,
    onStopClick: () -> Unit,
    onRestartClick: () -> Unit,
    onClick: () -> Unit = {}
) {
    val statusColor = if (container.isRunning) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.outline
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = AppShapes.Card,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.cardSurface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = Elevation.MediumLow),
        onClick = onClick
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            // 左侧状态竖条
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .defaultMinSize(minHeight = 100.dp)
                    .clip(RoundedCornerShape(topStart = CornerRadius.Card, bottomStart = CornerRadius.Card))
                    .background(statusColor)
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(Spacing.CardPadding)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = container.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // 镜像名标签
                        Surface(
                            shape = RoundedCornerShape(CornerRadius.Full),
                            color = MaterialTheme.colorScheme.surfaceContainerHigh
                        ) {
                            Text(
                                text = container.image,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Surface(
                        color = if (container.isRunning) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        },
                        shape = RoundedCornerShape(CornerRadius.Full)
                    ) {
                        Text(
                            text = if (container.isRunning) stringResource(R.string.common_running) else stringResource(R.string.common_stopped),
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            color = if (container.isRunning) {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                }

                container.networkSettings?.let { network ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Lan,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = network.ipAddress,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(Spacing.Medium))

                // 操作按钮
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isLoading) {
                        // 加载中显示进度指示器
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    } else if (container.isRunning) {
                        FilledTonalButton(
                            onClick = onStopClick,
                            modifier = Modifier.height(32.dp),
                            enabled = !isLoading,
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer,
                                contentColor = MaterialTheme.colorScheme.onErrorContainer
                            )
                        ) {
                            Icon(Icons.Filled.Stop, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(stringResource(R.string.common_stop), style = MaterialTheme.typography.labelMedium)
                        }

                        Spacer(modifier = Modifier.width(Spacing.MediumSmall))

                        FilledTonalButton(
                            onClick = onRestartClick,
                            modifier = Modifier.height(32.dp),
                            enabled = !isLoading,
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                        ) {
                            Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(stringResource(R.string.common_restart), style = MaterialTheme.typography.labelMedium)
                        }
                    } else {
                        FilledTonalButton(
                            onClick = onStartClick,
                            modifier = Modifier.height(32.dp),
                            enabled = !isLoading,
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                        ) {
                            Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(stringResource(R.string.common_start), style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }
        }
    }
}
