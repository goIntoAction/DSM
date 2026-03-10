package wang.zengye.dsm.ui.virtual_machine

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
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
 * 虚拟机管理页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VirtualMachineScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: VirtualMachineViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.vm_management)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.common_back))
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.sendIntent(VirtualMachineIntent.LoadVms) }) {
                        Icon(Icons.Default.Refresh, contentDescription = stringResource(R.string.common_refresh))
                    }
                }
            )
        }
    ) { paddingValues ->
        when {
            uiState.isLoading -> {
                LoadingState(
                    message = stringResource(R.string.vm_loading),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
            }

            uiState.error != null && uiState.vms.isEmpty() -> {
                ErrorState(
                    message = uiState.error ?: stringResource(R.string.common_load_failed),
                    onRetry = { viewModel.sendIntent(VirtualMachineIntent.LoadVms) },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
            }

            uiState.vms.isEmpty() -> {
                EmptyState(
                    message = stringResource(R.string.vm_no_vms),
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
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            val runningCount = uiState.vms.count { it.isRunning }
                            val stoppedCount = uiState.vms.count { it.isStopped }

                            Text(
                                text = stringResource(R.string.vm_total_count, uiState.vms.size),
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            Text(
                                text = stringResource(R.string.vm_running_count, runningCount),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = stringResource(R.string.vm_stopped_count, stoppedCount),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

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

                    // 虚拟机列表
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        items(
                            items = uiState.vms,
                            key = { it.guestId }
                        ) { vm ->
                            VmItemCard(
                                vm = vm,
                                isOperating = uiState.operatingVmId == vm.guestId,
                                operationType = uiState.operationType,
                                onStart = { viewModel.sendIntent(VirtualMachineIntent.StartVm(vm)) },
                                onStop = { viewModel.sendIntent(VirtualMachineIntent.StopVm(vm, false)) },
                                onForceStop = { viewModel.sendIntent(VirtualMachineIntent.StopVm(vm, true)) },
                                onRestart = { viewModel.sendIntent(VirtualMachineIntent.RestartVm(vm)) }
                            )
                        }
                    }
                }
            }
        }
    }
}
