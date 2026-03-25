package wang.zengye.dsm.ui.resource_monitor

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.compose.cartesian.data.lineSeries
import com.patrykandpatrick.vico.compose.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLine
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.common.Fill
import wang.zengye.dsm.ui.components.ErrorState
import wang.zengye.dsm.util.formatSize
import androidx.compose.ui.res.stringResource
import wang.zengye.dsm.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerformanceScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: PerformanceViewModel = hiltViewModel()
) {
    val uiState by viewModel.state.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.addObserver(viewModel)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(viewModel)
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.resmon_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.common_back))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = Color.Transparent
                ),
                actions = {
                    IconButton(onClick = { viewModel.sendIntent(PerformanceIntent.ToggleAutoRefresh) }) {
                        Icon(
                            if (uiState.autoRefresh) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                            contentDescription = if (uiState.autoRefresh) stringResource(R.string.resmon_pause) else stringResource(R.string.resmon_auto_refresh)
                        )
                    }
                    IconButton(onClick = { viewModel.sendIntent(PerformanceIntent.RefreshOnce) }) {
                        Icon(Icons.Filled.Refresh, contentDescription = stringResource(R.string.common_refresh))
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (uiState.error != null) {
                ErrorState(
                    message = uiState.error ?: stringResource(R.string.common_load_failed),
                    onRetry = { viewModel.sendIntent(PerformanceIntent.RefreshOnce) }
                )
            } else {
                // CPU 使用率
                UsageCard(
                    title = stringResource(R.string.resmon_cpu_usage),
                    usage = uiState.current.cpuUsage,
                    history = uiState.cpuHistory,
                    color = MaterialTheme.colorScheme.primary
                )
                
                // 内存使用率
                UsageCard(
                    title = stringResource(R.string.resmon_memory_usage),
                    usage = uiState.current.memoryUsage,
                    history = uiState.memoryHistory,
                    color = MaterialTheme.colorScheme.tertiary,
                    subtitle = "${formatSize(uiState.current.memoryUsed)} / ${formatSize(uiState.current.memoryTotal)}"
                )
                
                // 网络流量
                NetworkCard(
                    rxSpeed = uiState.current.networkRx,
                    txSpeed = uiState.current.networkTx,
                    rxHistory = uiState.networkRxHistory,
                    txHistory = uiState.networkTxHistory
                )
                
                // 刷新间隔设置
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.resmon_refresh_interval),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    listOf(1, 3, 5, 10).forEach { seconds ->
                        FilterChip(
                            selected = uiState.refreshInterval == seconds,
                            onClick = { viewModel.sendIntent(PerformanceIntent.SetRefreshInterval(seconds)) },
                            label = { Text("${seconds}s") },
                            modifier = Modifier.padding(horizontal = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun UsageCard(
    title: String,
    usage: Int,
    history: List<Int>,
    color: Color,
    subtitle: String? = null
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = "$usage%",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = when {
                        usage > 90 -> MaterialTheme.colorScheme.error
                        usage > 70 -> MaterialTheme.colorScheme.tertiary
                        else -> color
                    }
                )
            }
            
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // 进度条
            LinearProgressIndicator(
                progress = { usage / 100f },
                modifier = Modifier.fillMaxWidth(),
                color = when {
                    usage > 90 -> MaterialTheme.colorScheme.error
                    usage > 70 -> MaterialTheme.colorScheme.tertiary
                    else -> color
                },
                    strokeCap = StrokeCap.Butt,
                    gapSize = 0.dp,
                    drawStopIndicator = {})
            
            // 简单的历史图表
            if (history.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                SimpleLineChart(
                    values = history,
                    color = color,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                )
            }
        }
    }
}

@Composable
private fun NetworkCard(
    rxSpeed: Long,
    txSpeed: Long,
    rxHistory: List<Long>,
    txHistory: List<Long>
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.resmon_network_traffic),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Filled.ArrowDownward,
                        contentDescription = stringResource(R.string.resmon_download),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = formatSize(rxSpeed) + "/s",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = stringResource(R.string.resmon_download),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Filled.ArrowUpward,
                        contentDescription = stringResource(R.string.resmon_upload),
                        tint = MaterialTheme.colorScheme.tertiary
                    )
                    Text(
                        text = formatSize(txSpeed) + "/s",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = stringResource(R.string.resmon_upload),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            if (rxHistory.isNotEmpty() && txHistory.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                // 简单的网络历史图表
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        SimpleLineChart(
                            values = rxHistory.map { it.toInt() },
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }
}

/**
 * 简单的折线图组件（使用 vico）
 */
@Composable
private fun SimpleLineChart(
    values: List<Int>,
    color: Color,
    modifier: Modifier = Modifier
) {
    if (values.isEmpty()) return

    val modelProducer = remember { CartesianChartModelProducer() }

    LaunchedEffect(values) {
        modelProducer.runTransaction {
            lineSeries { series(values.map { it.toDouble() }) }
        }
    }

    CartesianChartHost(
        chart = rememberCartesianChart(
            rememberLineCartesianLayer(
                lineProvider = LineCartesianLayer.LineProvider.series(
                    LineCartesianLayer.rememberLine(
                        fill = LineCartesianLayer.LineFill.single(Fill(color)),
                        areaFill = LineCartesianLayer.AreaFill.single(
                            Fill(color.copy(alpha = 0.2f))
                        ),
                    )
                )
            ),
            startAxis = null,
            bottomAxis = null,
        ),
        modelProducer = modelProducer,
        modifier = modifier,
    )
}
