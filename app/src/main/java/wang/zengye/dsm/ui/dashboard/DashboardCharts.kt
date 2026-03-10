package wang.zengye.dsm.ui.dashboard

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.MaterialTheme
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
 * 迷你图表组件（使用 vico）
 */
@Composable
internal fun MiniChart(
    history: List<Int>,
    color: Color,
    modifier: Modifier = Modifier
) {
    if (history.size < 2) {
        // 没有历史数据时显示占位
        androidx.compose.foundation.Canvas(modifier = modifier) {
            drawLine(
                color = color.copy(alpha = 0.3f),
                start = androidx.compose.ui.geometry.Offset(0f, size.height / 2),
                end = androidx.compose.ui.geometry.Offset(size.width, size.height / 2),
                strokeWidth = 2f
            )
        }
        return
    }

    val modelProducer = remember { CartesianChartModelProducer() }

    LaunchedEffect(history) {
        modelProducer.runTransaction {
            lineSeries { series(history.map { it.toDouble() }) }
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
                ),
                rangeProvider = CartesianLayerRangeProvider.fixed(minY = 0.0, maxY = 100.0)
            ),
            startAxis = null,
            bottomAxis = null,
        ),
        modelProducer = modelProducer,
        modifier = modifier,
    )
}

/**
 * 网络流量图表（使用 vico）
 */
@Composable
internal fun NetworkTrafficChart(
    rxHistory: List<Long>,
    txHistory: List<Long>,
    modifier: Modifier = Modifier
) {
    if (rxHistory.isEmpty() && txHistory.isEmpty()) return

    val primaryColor = MaterialTheme.colorScheme.primary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary

    val modelProducer = remember { CartesianChartModelProducer() }

    val allData = rxHistory + txHistory
    val maxValue = allData.maxOrNull()?.toDouble()?.takeIf { it > 0 } ?: 1.0

    LaunchedEffect(rxHistory, txHistory) {
        modelProducer.runTransaction {
            lineSeries {
                if (rxHistory.isNotEmpty()) {
                    series(rxHistory.map { it.toDouble() })
                }
                if (txHistory.isNotEmpty()) {
                    series(txHistory.map { it.toDouble() })
                }
            }
        }
    }

    val lines = mutableListOf<LineCartesianLayer.Line>()

    if (rxHistory.isNotEmpty()) {
        lines.add(
            LineCartesianLayer.rememberLine(
                fill = LineCartesianLayer.LineFill.single(Fill(primaryColor)),
                areaFill = LineCartesianLayer.AreaFill.single(
                    Fill(primaryColor.copy(alpha = 0.15f))
                ),
            )
        )
    }

    if (txHistory.isNotEmpty()) {
        lines.add(
            LineCartesianLayer.rememberLine(
                fill = LineCartesianLayer.LineFill.single(Fill(tertiaryColor)),
                areaFill = LineCartesianLayer.AreaFill.single(
                    Fill(tertiaryColor.copy(alpha = 0.15f))
                ),
            )
        )
    }

    CartesianChartHost(
        chart = rememberCartesianChart(
            rememberLineCartesianLayer(
                lineProvider = LineCartesianLayer.LineProvider.series(lines),
                rangeProvider = CartesianLayerRangeProvider.fixed(minY = 0.0, maxY = maxValue)
            ),
            startAxis = null,
            bottomAxis = null,
        ),
        modelProducer = modelProducer,
        modifier = modifier,
    )
}