package wang.zengye.dsm.ui.docker

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import wang.zengye.dsm.R
import wang.zengye.dsm.ui.components.EmptyState
import wang.zengye.dsm.ui.components.ErrorState
import wang.zengye.dsm.ui.components.LoadingState

/**
 * Docker网络列表页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NetworkListScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: NetworkListViewModel = hiltViewModel()
) {
    val uiState by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is NetworkListEvent.ShowError -> {
                    snackbarHostState.showSnackbar(event.message)
                }
                is NetworkListEvent.CreateSuccess -> {
                    snackbarHostState.showSnackbar("网络创建成功")
                }
                is NetworkListEvent.DeleteSuccess -> {
                    snackbarHostState.showSnackbar("网络删除成功")
                }
            }
        }
    }

    // 创建网络对话框
    if (uiState.showCreateDialog) {
        CreateNetworkDialog(
            isCreating = uiState.isCreating,
            error = uiState.createError,
            onDismiss = { viewModel.sendIntent(NetworkListIntent.HideCreateDialog) },
            onCreate = { name, driver, subnet, gateway ->
                viewModel.sendIntent(NetworkListIntent.CreateNetwork(name, driver, subnet, gateway))
            }
        )
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.docker_network_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.common_back))
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.sendIntent(NetworkListIntent.ShowCreateDialog) }) {
                        Icon(Icons.Default.Add, contentDescription = stringResource(R.string.docker_create_network))
                    }
                    IconButton(onClick = { viewModel.sendIntent(NetworkListIntent.LoadNetworks) }) {
                        Icon(Icons.Default.Refresh, contentDescription = stringResource(R.string.dashboard_refresh))
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

            uiState.error != null && uiState.networks.isEmpty() -> {
                ErrorState(
                    message = uiState.error ?: stringResource(R.string.dashboard_load_failed),
                    onRetry = { viewModel.sendIntent(NetworkListIntent.LoadNetworks) },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
            }

            uiState.networks.isEmpty() -> {
                EmptyState(
                    message = stringResource(R.string.docker_no_networks),
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
                                text = stringResource(R.string.docker_network_count, uiState.networks.size),
                                style = MaterialTheme.typography.bodyMedium
                            )

                            val totalContainers = uiState.networks.sumOf { it.containers.size }
                            Text(
                                text = stringResource(R.string.docker_network_connected_containers, totalContainers),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        items(
                            items = uiState.networks,
                            key = { it.id }
                        ) { network ->
                            NetworkCard(
                                network = network,
                                onDelete = { viewModel.sendIntent(NetworkListIntent.DeleteNetwork(network.id)) }
                            )
                        }
                    }
                }
            }
        }
    }
}
