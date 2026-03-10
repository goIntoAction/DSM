package wang.zengye.dsm.ui.storage

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.res.stringResource
import wang.zengye.dsm.R
import wang.zengye.dsm.ui.components.EmptyState
import wang.zengye.dsm.ui.components.ErrorState
import wang.zengye.dsm.ui.components.LoadingState
import wang.zengye.dsm.ui.storage.SmartTestIntent

/**
 * SMART检测页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmartTestScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: SmartTestViewModel = hiltViewModel()
) {
    val uiState by viewModel.state.collectAsStateWithLifecycle()

    // 测试类型选择对话框
    if (uiState.showTestDialog && uiState.selectedDisk != null) {
        SmartTestTypeDialog(
            disk = uiState.selectedDisk!!,
            isRunning = uiState.isRunningTest,
            onDismiss = { viewModel.sendIntent(SmartTestIntent.HideTestDialog) },
            onStartTest = { testType ->
                viewModel.sendIntent(SmartTestIntent.StartTest(uiState.selectedDisk!!.device, testType))
            }
        )
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.smart_storage_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.common_back))
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.sendIntent(SmartTestIntent.LoadSmartInfo) }) {
                        Icon(Icons.Default.Refresh, contentDescription = stringResource(R.string.common_retry))
                    }
                }
            )
        }
    ) { paddingValues ->
        when {
            uiState.isLoading -> {
                LoadingState(
                    message = stringResource(R.string.smart_storage_loading_disks),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
            }

            uiState.error != null && uiState.disks.isEmpty() -> {
                ErrorState(
                    message = uiState.error ?: stringResource(R.string.smart_storage_load_failed),
                    onRetry = { viewModel.sendIntent(SmartTestIntent.LoadSmartInfo) },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
            }

            uiState.disks.isEmpty() -> {
                EmptyState(
                    message = stringResource(R.string.smart_storage_no_disks_detected),
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
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // 健康状态概览
                    item {
                        HealthOverviewCard(disks = uiState.disks)
                    }

                    // 磁盘列表
                    items(
                        items = uiState.disks,
                        key = { it.device }
                    ) { disk ->
                        SmartDiskCard(
                            disk = disk,
                            isSelected = uiState.selectedDisk?.device == disk.device,
                            onClick = { viewModel.sendIntent(SmartTestIntent.SelectDisk(disk)) }
                        )
                    }

                    // 选中磁盘的测试日志
                    if (uiState.selectedDisk != null && uiState.testLogs.isNotEmpty()) {
                        item {
                            Text(
                                text = stringResource(R.string.smart_storage_test_logs),
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }

                        items(
                            items = uiState.testLogs,
                            key = { it.id }
                        ) { log ->
                            TestLogItem(log = log)
                        }
                    }
                }
            }
        }
    }
}
