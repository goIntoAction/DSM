package wang.zengye.dsm.ui.performance

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberAxisGuidelineComponent
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianLayerRangeProvider
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.compose.cartesian.data.lineSeries
import com.patrykandpatrick.vico.compose.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLine
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.marker.CartesianMarker
import com.patrykandpatrick.vico.compose.cartesian.marker.DefaultCartesianMarker
import com.patrykandpatrick.vico.compose.cartesian.marker.LineCartesianLayerMarkerTarget
import com.patrykandpatrick.vico.compose.cartesian.marker.rememberDefaultCartesianMarker
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.common.Fill
import com.patrykandpatrick.vico.compose.common.Insets
import com.patrykandpatrick.vico.compose.common.LayeredComponent
import com.patrykandpatrick.vico.compose.common.MarkerCornerBasedShape
import com.patrykandpatrick.vico.compose.common.component.ShapeComponent
import com.patrykandpatrick.vico.compose.common.component.TextComponent
import com.patrykandpatrick.vico.compose.common.component.rememberShapeComponent
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent
import wang.zengye.dsm.R
import wang.zengye.dsm.ui.components.*
import wang.zengye.dsm.ui.theme.*
import wang.zengye.dsm.util.formatSize

@Composable
internal fun CpuTab(uiState: PerformanceUiState) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = Spacing.PageHorizontal, vertical = Spacing.Standard)
    ) {
        // 当前CPU使用率
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = AppShapes.Card
        ) {
            Column(modifier = Modifier.padding(Spacing.CardPadding)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LeadingIcon(icon = Icons.Outlined.Memory)
                    Spacer(modifier = Modifier.width(Spacing.Standard))
                    Text(
                        text = stringResource(R.string.performance_cpu_usage),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(Spacing.Standard))

                Text(
                    text = "${uiState.currentData.cpuUsage}%",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                    color = getUsageColor(uiState.currentData.cpuUsage)
                )

                Spacer(modifier = Modifier.height(Spacing.Standard))

                LinearProgressIndicator(
                    progress = { uiState.currentData.cpuUsage / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = getUsageColor(uiState.currentData.cpuUsage),
                    trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                    strokeCap = androidx.compose.ui.graphics.StrokeCap.Butt,
                    gapSize = 0.dp,
                    drawStopIndicator = {}
                )
            }
        }

        Spacer(modifier = Modifier.height(Spacing.CardSpacing))

        // CPU历史图表
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = AppShapes.Card
        ) {
            Column(modifier = Modifier.padding(Spacing.CardPadding)) {
                Text(
                    text = stringResource(R.string.performance_usage_trend),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(Spacing.Standard))
                if (uiState.cpuHistory.isNotEmpty()) {
                    VicoLineChart(
                        data = uiState.cpuHistory,
                        maxValue = 100f,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        valueFormatter = CartesianValueFormatter.decimal(suffix = "%")
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        LoadingState(message = stringResource(R.string.performance_loading_data))
                    }
                }
            }
        }
    }
}

@Composable
internal fun MemoryTab(uiState: PerformanceUiState) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = Spacing.PageHorizontal, vertical = Spacing.Standard)
    ) {
        // 当前内存使用率
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = AppShapes.Card
        ) {
            Column(modifier = Modifier.padding(Spacing.CardPadding)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LeadingIcon(icon = Icons.Outlined.Storage)
                    Spacer(modifier = Modifier.width(Spacing.Standard))
                    Text(
                        text = stringResource(R.string.performance_memory_usage),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(Spacing.Standard))

                Text(
                    text = "${uiState.currentData.memoryUsage}%",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                    color = getUsageColor(uiState.currentData.memoryUsage)
                )

                Spacer(modifier = Modifier.height(Spacing.ExtraSmall))

                Text(
                    text = "${formatSize(uiState.currentData.memoryUsed)} / ${formatSize(uiState.currentData.memoryTotal)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(Spacing.Standard))

                LinearProgressIndicator(
                    progress = { uiState.currentData.memoryUsage / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = getUsageColor(uiState.currentData.memoryUsage),
                    trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                    strokeCap = androidx.compose.ui.graphics.StrokeCap.Butt,
                    gapSize = 0.dp,
                    drawStopIndicator = {}
                )
            }
        }

        Spacer(modifier = Modifier.height(Spacing.CardSpacing))

        // 内存历史图表
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = AppShapes.Card
        ) {
            Column(modifier = Modifier.padding(Spacing.CardPadding)) {
                Text(
                    text = stringResource(R.string.performance_usage_trend),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(Spacing.Standard))
                if (uiState.memoryHistory.isNotEmpty()) {
                    VicoLineChart(
                        data = uiState.memoryHistory,
                        maxValue = 100f,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        valueFormatter = CartesianValueFormatter.decimal(suffix = "%")
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        LoadingState(message = stringResource(R.string.performance_loading_data))
                    }
                }
            }
        }
    }
}

@Composable
internal fun NetworkTab(uiState: PerformanceUiState) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = Spacing.PageHorizontal, vertical = Spacing.Standard)
    ) {
        // 当前网速
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = AppShapes.Card
        ) {
            Column(modifier = Modifier.padding(Spacing.CardPadding)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LeadingIcon(icon = Icons.Outlined.NetworkCheck)
                    Spacer(modifier = Modifier.width(Spacing.Standard))
                    Text(
                        text = stringResource(R.string.performance_network_speed),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(Spacing.Standard))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    SpeedIndicator(
                        icon = Icons.Filled.ArrowDownward,
                        speed = formatSize(uiState.currentData.networkRx) + "/s",
                        label = stringResource(R.string.performance_download),
                        color = MaterialTheme.colorScheme.primary
                    )
                    SpeedIndicator(
                        icon = Icons.Filled.ArrowUpward,
                        speed = formatSize(uiState.currentData.networkTx) + "/s",
                        label = stringResource(R.string.performance_upload),
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(Spacing.CardSpacing))

        // 网络历史图表
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = AppShapes.Card
        ) {
            Column(modifier = Modifier.padding(Spacing.CardPadding)) {
                Text(
                    text = stringResource(R.string.performance_traffic_trend),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(Spacing.Standard))

                // 图例
                ChartLegend(
                    items = listOf(
                        LegendItem(color = MaterialTheme.colorScheme.primary, label = stringResource(R.string.performance_download)),
                        LegendItem(color = MaterialTheme.colorScheme.tertiary, label = stringResource(R.string.performance_upload))
                    )
                )

                Spacer(modifier = Modifier.height(Spacing.Small))

                if (uiState.networkRxHistory.isNotEmpty() || uiState.networkTxHistory.isNotEmpty()) {
                    val maxValue = (uiState.networkRxHistory + uiState.networkTxHistory).maxOrNull()?.toFloat()?.takeIf { it > 0 } ?: 1f
                    VicoDualLineChart(
                        data1 = uiState.networkRxHistory,
                        data2 = uiState.networkTxHistory,
                        maxValue = maxValue,
                        color1 = MaterialTheme.colorScheme.primary,
                        color2 = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        LoadingState(message = stringResource(R.string.performance_loading_data))
                    }
                }
            }
        }
    }
}

@Composable
internal fun DiskTab(uiState: PerformanceUiState) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = Spacing.PageHorizontal, vertical = Spacing.Standard)
    ) {
        // 当前磁盘IO
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = AppShapes.Card
        ) {
            Column(modifier = Modifier.padding(Spacing.CardPadding)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LeadingIcon(icon = Icons.Outlined.Storage)
                    Spacer(modifier = Modifier.width(Spacing.Standard))
                    Text(
                        text = stringResource(R.string.performance_disk_io),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(Spacing.Standard))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    SpeedIndicator(
                        icon = Icons.Filled.ArrowDownward,
                        speed = formatSize(uiState.currentData.diskRead) + "/s",
                        label = stringResource(R.string.performance_read),
                        color = MaterialTheme.colorScheme.primary
                    )
                    SpeedIndicator(
                        icon = Icons.Filled.ArrowUpward,
                        speed = formatSize(uiState.currentData.diskWrite) + "/s",
                        label = stringResource(R.string.performance_write),
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(Spacing.CardSpacing))

        // 磁盘IO历史图表
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = AppShapes.Card
        ) {
            Column(modifier = Modifier.padding(Spacing.CardPadding)) {
                Text(
                    text = stringResource(R.string.performance_io_trend),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(Spacing.Standard))

                ChartLegend(
                    items = listOf(
                        LegendItem(color = MaterialTheme.colorScheme.primary, label = stringResource(R.string.performance_read)),
                        LegendItem(color = MaterialTheme.colorScheme.tertiary, label = stringResource(R.string.performance_write))
                    )
                )

                Spacer(modifier = Modifier.height(Spacing.Small))

                if (uiState.diskReadHistory.isNotEmpty() || uiState.diskWriteHistory.isNotEmpty()) {
                    val maxValue = (uiState.diskReadHistory + uiState.diskWriteHistory).maxOrNull()?.toFloat()?.takeIf { it > 0 } ?: 1f
                    VicoDualLineChart(
                        data1 = uiState.diskReadHistory,
                        data2 = uiState.diskWriteHistory,
                        maxValue = maxValue,
                        color1 = MaterialTheme.colorScheme.primary,
                        color2 = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        LoadingState(message = stringResource(R.string.performance_loading_data))
                    }
                }
            }
        }
    }
}

@Composable
internal fun VolumeTab(uiState: PerformanceUiState) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = Spacing.PageHorizontal, vertical = Spacing.Standard)
    ) {
        // 当前存储IO
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = AppShapes.Card
        ) {
            Column(modifier = Modifier.padding(Spacing.CardPadding)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LeadingIcon(icon = Icons.Outlined.Folder)
                    Spacer(modifier = Modifier.width(Spacing.Standard))
                    Text(
                        text = stringResource(R.string.performance_volume_io),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(Spacing.Standard))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    SpeedIndicator(
                        icon = Icons.Filled.ArrowDownward,
                        speed = formatSize(uiState.currentData.volumeRead) + "/s",
                        label = stringResource(R.string.performance_read),
                        color = MaterialTheme.colorScheme.primary
                    )
                    SpeedIndicator(
                        icon = Icons.Filled.ArrowUpward,
                        speed = formatSize(uiState.currentData.volumeWrite) + "/s",
                        label = stringResource(R.string.performance_write),
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(Spacing.CardSpacing))

        // 存储卷IO历史图表
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = AppShapes.Card
        ) {
            Column(modifier = Modifier.padding(Spacing.CardPadding)) {
                Text(
                    text = stringResource(R.string.performance_io_trend),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(Spacing.Standard))

                ChartLegend(
                    items = listOf(
                        LegendItem(color = MaterialTheme.colorScheme.primary, label = stringResource(R.string.performance_read)),
                        LegendItem(color = MaterialTheme.colorScheme.tertiary, label = stringResource(R.string.performance_write))
                    )
                )

                Spacer(modifier = Modifier.height(Spacing.Small))

                if (uiState.volumeReadHistory.isNotEmpty() || uiState.volumeWriteHistory.isNotEmpty()) {
                    val maxValue = (uiState.volumeReadHistory + uiState.volumeWriteHistory).maxOrNull()?.toFloat()?.takeIf { it > 0 } ?: 1f
                    VicoDualLineChart(
                        data1 = uiState.volumeReadHistory,
                        data2 = uiState.volumeWriteHistory,
                        maxValue = maxValue,
                        color1 = MaterialTheme.colorScheme.primary,
                        color2 = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        LoadingState(message = stringResource(R.string.performance_loading_data))
                    }
                }
            }
        }
    }
}

// ==================== 辅助组件 ====================

@Composable
internal fun SpeedIndicator(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    speed: String,
    label: String,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            shape = RoundedCornerShape(CornerRadius.Medium),
            color = color.copy(alpha = 0.1f),
            modifier = Modifier.size(48.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(Spacing.Small))
        Text(
            text = speed,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

internal data class LegendItem(
    val color: Color,
    val label: String
)

@Composable
internal fun ChartLegend(
    items: List<LegendItem>,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        items.forEachIndexed { index, item ->
            if (index > 0) {
                Spacer(modifier = Modifier.width(Spacing.Standard))
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(RoundedCornerShape(CornerRadius.ExtraSmall))
                        .background(item.color)
                )
                Spacer(modifier = Modifier.width(Spacing.ExtraSmall))
                Text(
                    text = item.label,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// ==================== Vico 图表组件 ====================

/**
 * 记住 Marker 组件
 */
@Composable
private fun rememberVicoMarker(
    valueFormatter: DefaultCartesianMarker.ValueFormatter = DefaultCartesianMarker.ValueFormatter.default()
): CartesianMarker {
    val labelBackgroundShape = MarkerCornerBasedShape(CircleShape)
    val labelBackground = rememberShapeComponent(
        fill = Fill(MaterialTheme.colorScheme.background),
        shape = labelBackgroundShape,
        strokeFill = Fill(MaterialTheme.colorScheme.outline),
        strokeThickness = 1.dp,
    )
    val label = rememberTextComponent(
        style = TextStyle(
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            fontSize = 12.sp,
        ),
        padding = Insets(8.dp, 4.dp),
        background = labelBackground,
        minWidth = TextComponent.MinWidth.fixed(40.dp),
    )
    val indicatorFrontComponent = rememberShapeComponent(Fill(MaterialTheme.colorScheme.surface), CircleShape)
    val guideline = rememberAxisGuidelineComponent()
    
    return rememberDefaultCartesianMarker(
        label = label,
        valueFormatter = valueFormatter,
        indicator = { color ->
            LayeredComponent(
                back = ShapeComponent(Fill(color.copy(alpha = 0.15f)), CircleShape),
                front = LayeredComponent(
                    back = ShapeComponent(fill = Fill(color), shape = CircleShape),
                    front = indicatorFrontComponent,
                    padding = Insets(5.dp),
                ),
                padding = Insets(10.dp),
            )
        },
        indicatorSize = 36.dp,
        guideline = guideline,
    )
}

/**
 * 单数据折线图（使用 vico）
 */
@Composable
internal fun VicoLineChart(
    data: List<Int>,
    maxValue: Float,
    modifier: Modifier = Modifier,
    lineColor: Color = MaterialTheme.colorScheme.primary,
    valueFormatter: CartesianValueFormatter = CartesianValueFormatter.decimal()
) {
    if (data.isEmpty()) return

    val modelProducer = remember { CartesianChartModelProducer() }
    
    LaunchedEffect(data) {
        modelProducer.runTransaction {
            lineSeries { series(data.map { it.toDouble() }) }
        }
    }

    val rangeProvider = CartesianLayerRangeProvider.fixed(maxY = maxValue.toDouble())
    val markerValueFormatter = DefaultCartesianMarker.ValueFormatter.default(suffix = "%")

    CartesianChartHost(
        chart = rememberCartesianChart(
            rememberLineCartesianLayer(
                lineProvider = LineCartesianLayer.LineProvider.series(
                    LineCartesianLayer.rememberLine(
                        fill = LineCartesianLayer.LineFill.single(Fill(lineColor)),
                        areaFill = LineCartesianLayer.AreaFill.single(
                            Fill(lineColor.copy(alpha = 0.2f))
                        ),
                    )
                ),
                rangeProvider = rangeProvider
            ),
            startAxis = VerticalAxis.rememberStart(
                valueFormatter = valueFormatter
            ),
            bottomAxis = HorizontalAxis.rememberBottom(),
            marker = rememberVicoMarker(markerValueFormatter),
        ),
        modelProducer = modelProducer,
        modifier = modifier,
    )
}

/**
 * 双数据折线图（使用 vico）
 */
@Composable
internal fun VicoDualLineChart(
    data1: List<Long>,
    data2: List<Long>,
    maxValue: Float,
    color1: Color,
    color2: Color,
    modifier: Modifier = Modifier
) {
    if (data1.isEmpty() && data2.isEmpty()) return

    val modelProducer = remember { CartesianChartModelProducer() }
    
    LaunchedEffect(data1, data2) {
        modelProducer.runTransaction {
            lineSeries {
                if (data1.isNotEmpty()) {
                    series(data1.map { it.toDouble() })
                }
                if (data2.isNotEmpty()) {
                    series(data2.map { it.toDouble() })
                }
            }
        }
    }

    // 自定义值格式化器 - 格式化为存储大小
    val sizeValueFormatter = CartesianValueFormatter { _, value, _ ->
        formatSize(value.toLong())
    }

    val rangeProvider = CartesianLayerRangeProvider.fixed(maxY = maxValue.toDouble())
    val markerValueFormatter = DefaultCartesianMarker.ValueFormatter { _, targets ->
        buildString {
            targets.forEachIndexed { index, target ->
                if (index > 0) append(" / ")
                val lineTarget = target as? LineCartesianLayerMarkerTarget
                val entry = lineTarget?.points?.firstOrNull()?.entry
                append(formatSize(entry?.y?.toLong() ?: 0L))
            }
        }
    }

    val lines = mutableListOf<LineCartesianLayer.Line>()
    
    if (data1.isNotEmpty()) {
        lines.add(
            LineCartesianLayer.rememberLine(
                fill = LineCartesianLayer.LineFill.single(Fill(color1)),
                areaFill = LineCartesianLayer.AreaFill.single(
                    Fill(color1.copy(alpha = 0.15f))
                ),
            )
        )
    }
    
    if (data2.isNotEmpty()) {
        lines.add(
            LineCartesianLayer.rememberLine(
                fill = LineCartesianLayer.LineFill.single(Fill(color2)),
                areaFill = LineCartesianLayer.AreaFill.single(
                    Fill(color2.copy(alpha = 0.15f))
                ),
            )
        )
    }

    CartesianChartHost(
        chart = rememberCartesianChart(
            rememberLineCartesianLayer(
                lineProvider = LineCartesianLayer.LineProvider.series(lines),
                rangeProvider = rangeProvider
            ),
            startAxis = VerticalAxis.rememberStart(
                valueFormatter = sizeValueFormatter
            ),
            bottomAxis = HorizontalAxis.rememberBottom(),
            marker = rememberVicoMarker(markerValueFormatter),
        ),
        modelProducer = modelProducer,
        modifier = modifier,
    )
}