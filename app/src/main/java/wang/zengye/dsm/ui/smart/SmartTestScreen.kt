package wang.zengye.dsm.ui.smart

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.res.stringResource
import wang.zengye.dsm.R
import wang.zengye.dsm.util.formatSize

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmartTestScreen(
    diskId: String = "",
    onBack: () -> Unit = {}
) {
    val viewModel: SmartTestViewModel = hiltViewModel()
    val uiState by viewModel.state.collectAsStateWithLifecycle()

    // 当传入 diskId 时，自动选中对应的磁盘
    LaunchedEffect(diskId) {
        if (diskId.isNotEmpty()) {
            viewModel.sendIntent(SmartTestIntent.SelectDiskById(diskId))
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.smart_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.common_back))
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.sendIntent(SmartTestIntent.Refresh) }) {
                        Icon(Icons.Filled.Refresh, contentDescription = stringResource(R.string.common_retry))
                    }
                }
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 磁盘列表
                item {
                    Text(
                        text = stringResource(R.string.smart_disk_smart_status),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                items(uiState.disks) { disk ->
                    SmartDiskCard(
                        disk = disk,
                        isTesting = uiState.isTesting && uiState.testingDiskId == disk.diskId,
                        onStartTest = { viewModel.sendIntent(SmartTestIntent.StartSmartTest(disk.diskId)) }
                    )
                }

                // SMART测试日志
                if (uiState.testLogs.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.smart_test_history),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    items(uiState.testLogs) { log ->
                        SmartTestLogItem(log)
                    }
                }
            }
        }
    }
}

@Composable
private fun SmartDiskCard(
    disk: SmartTestInfo,
    isTesting: Boolean,
    onStartTest: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = disk.diskName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = disk.diskId,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // 健康状态
                Surface(
                    color = when (disk.health) {
                        "normal" -> MaterialTheme.colorScheme.primaryContainer
                        else -> MaterialTheme.colorScheme.errorContainer
                    },
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = disk.health.ifEmpty { stringResource(R.string.common_unknown) },
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 磁盘信息
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.smart_temperature),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = stringResource(R.string.smart_temperature_format, disk.temperature),
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (disk.temperature > 60) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                    )
                }
                Column {
                    Text(
                        text = stringResource(R.string.smart_power_on_hours),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formatPowerOnDays(disk.powerOnHours),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Column {
                    Text(
                        text = stringResource(R.string.smart_capacity),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formatSize(disk.capacity),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 测试按钮
            Button(
                onClick = onStartTest,
                enabled = !isTesting,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isTesting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.smart_testing))
                } else {
                    Icon(Icons.Filled.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.smart_start_test))
                }
            }
        }
    }
}

@Composable
private fun SmartTestLogItem(log: SmartTestLog) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.smart_test_type, log.testType),
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = stringResource(R.string.smart_test_time, log.startTime),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Surface(
                color = when (log.status) {
                    "finished" -> if (log.result == "normal") MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer
                    "running" -> MaterialTheme.colorScheme.tertiaryContainer
                    else -> MaterialTheme.colorScheme.surfaceVariant
                },
                shape = MaterialTheme.shapes.small
            ) {
                Text(
                    text = when (log.status) {
                        "finished" -> log.result.ifEmpty { stringResource(R.string.smart_test_finished) }
                        "running" -> "${log.progress}%"
                        else -> stringResource(R.string.common_unknown)
                    },
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}


@Composable
private fun formatPowerOnDays(hours: Long): String {
    if (hours <= 0) return "-"
    val days = hours / 24
    return stringResource(R.string.smart_power_on_days, days)
}
