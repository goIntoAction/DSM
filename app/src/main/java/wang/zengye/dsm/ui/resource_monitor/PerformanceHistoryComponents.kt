package wang.zengye.dsm.ui.resource_monitor

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianLayerRangeProvider
import com.patrykandpatrick.vico.compose.cartesian.data.lineSeries
import com.patrykandpatrick.vico.compose.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLine
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.common.Fill

/**
 * 时间范围选择器
 */
@Composable
internal fun TimeRangeSelector(
    selectedRange: TimeRange,
    onRangeSelected: (TimeRange) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
        ) {
            TimeRange.values().forEach { range ->
                FilterChip(
                    selected = selectedRange == range,
                    onClick = { onRangeSelected(range) },
                    label = { Text(range.label) }
                )
            }
        }
    }
}

/**
 * 性能指标卡片
 */
@Composable
internal fun MetricCard(
    metric: PerformanceMetric,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // 标题行
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .padding(2.dp)
                    ) {
                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            shape = MaterialTheme.shapes.extraSmall,
                            color = metric.color
                        ) {}
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = metric.label,
                        style = MaterialTheme.typography.titleSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 当前值
            Row(
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = String.format("%.1f", metric.currentValue),
                    style = MaterialTheme.typography.headlineMedium,
                    color = metric.color
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = metric.unit,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 迷你图表
            if (metric.dataPoints.isNotEmpty()) {
                MiniChart(
                    dataPoints = metric.dataPoints,
                    color = metric.color,
                    maxValue = metric.maxValue,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                )
            }
        }
    }
}

/**
 * 迷你图表（使用 vico）
 */
@Composable
internal fun MiniChart(
    dataPoints: List<PerformanceDataPoint>,
    color: Color,
    maxValue: Float,
    modifier: Modifier = Modifier
) {
    if (dataPoints.isEmpty()) return

    val modelProducer = remember { CartesianChartModelProducer() }
    val validMax = if (maxValue <= 0) 100.0 else maxValue.toDouble()

    LaunchedEffect(dataPoints) {
        modelProducer.runTransaction {
            lineSeries { series(dataPoints.map { it.value.toDouble() }) }
        }
    }

    val rangeProvider = CartesianLayerRangeProvider.fixed(maxY = validMax)

    CartesianChartHost(
        chart = rememberCartesianChart(
            rememberLineCartesianLayer(
                lineProvider = LineCartesianLayer.LineProvider.series(
                    LineCartesianLayer.rememberLine(
                        fill = LineCartesianLayer.LineFill.single(Fill(color)),
                        areaFill = LineCartesianLayer.AreaFill.single(
                            Fill(color.copy(alpha = 0.15f))
                        ),
                    )
                ),
                rangeProvider = rangeProvider
            ),
            startAxis = null,
            bottomAxis = null,
        ),
        modelProducer = modelProducer,
        modifier = modifier,
    )
}

/**
 * 综合图表（使用 vico）
 */
@Composable
internal fun CombinedChart(
    metrics: List<PerformanceMetric>,
    modifier: Modifier = Modifier
) {
    if (metrics.isEmpty() || metrics.all { it.dataPoints.isEmpty() }) return

    val modelProducer = remember { CartesianChartModelProducer() }
    
    // 找出最大数据点数量和最大值
    val maxDataPoints = metrics.maxOfOrNull { it.dataPoints.size } ?: 0
    val globalMaxValue = metrics.maxOfOrNull { 
        if (it.maxValue <= 0) 100.0 else it.maxValue.toDouble() 
    } ?: 100.0

    LaunchedEffect(metrics) {
        modelProducer.runTransaction {
            lineSeries {
                metrics.forEach { metric ->
                    series(metric.dataPoints.map { it.value.toDouble() })
                }
            }
        }
    }

    val rangeProvider = CartesianLayerRangeProvider.fixed(maxY = globalMaxValue)

    // 为每个指标创建线条配置
    val lines = metrics.mapIndexed { index, metric ->
        LineCartesianLayer.rememberLine(
            fill = LineCartesianLayer.LineFill.single(Fill(metric.color)),
        )
    }

    CartesianChartHost(
        chart = rememberCartesianChart(
            rememberLineCartesianLayer(
                lineProvider = LineCartesianLayer.LineProvider.series(lines),
                rangeProvider = rangeProvider
            ),
            startAxis = VerticalAxis.rememberStart(),
            bottomAxis = HorizontalAxis.rememberBottom(),
        ),
        modelProducer = modelProducer,
        modifier = modifier,
    )
}
