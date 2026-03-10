package wang.zengye.dsm.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianLayerRangeProvider
import com.patrykandpatrick.vico.compose.cartesian.data.lineSeries
import com.patrykandpatrick.vico.compose.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLine
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.common.Fill
import wang.zengye.dsm.ui.theme.*
import wang.zengye.dsm.util.formatSize

// ==================== 卡片组件 ====================

/**
 * MD3 Elevated Card - 带阴影的提升卡片
 * 适用于需要强调的重要卡片
 */
@Composable
fun ElevatedCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val cardColors = CardDefaults.cardColors(
        containerColor = MaterialTheme.cardSurface
    )
    val cardElevation = CardDefaults.cardElevation(
        defaultElevation = Elevation.MediumLow
    )
    if (onClick != null) {
        Card(
            modifier = modifier,
            onClick = onClick,
            shape = AppShapes.Card,
            colors = cardColors,
            elevation = cardElevation,
            content = content
        )
    } else {
        Card(
            modifier = modifier,
            shape = AppShapes.Card,
            colors = cardColors,
            elevation = cardElevation,
            content = content
        )
    }
}

/**
 * MD3 Filled Card - 填充卡片
 * 适用于一般内容展示
 */
@Composable
fun FilledCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerHigh,
    content: @Composable ColumnScope.() -> Unit
) {
    if (onClick != null) {
        Card(
            modifier = modifier,
            onClick = onClick,
            shape = AppShapes.Card,
            colors = CardDefaults.cardColors(containerColor = containerColor),
            content = content
        )
    } else {
        Card(
            modifier = modifier,
            shape = AppShapes.Card,
            colors = CardDefaults.cardColors(containerColor = containerColor),
            content = content
        )
    }
}

/**
 * MD3 Outlined Card - 轮廓卡片
 * 适用于次要内容或分组
 */
@Composable
fun OutlinedCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    if (onClick != null) {
        OutlinedCard(
            modifier = modifier,
            onClick = onClick,
            shape = AppShapes.Card,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            content = content
        )
    } else {
        OutlinedCard(
            modifier = modifier,
            shape = AppShapes.Card,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            content = content
        )
    }
}

/**
 * 使用率卡片 - 带渐变背景
 */
@Composable
fun UsageCard(
    title: String,
    usage: Int,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    history: List<Int> = emptyList()
) {
    val usageColor = getUsageColor(usage)

    ElevatedCard(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.CardPadding)
        ) {
            // 标题行
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 图标带圆形背景
                Surface(
                    shape = CircleShape,
                    color = usageColor.copy(alpha = 0.12f),
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = usageColor,
                            modifier = Modifier.size(Dimensions.IconStandard)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(Spacing.Standard))

                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )

                // 使用率百分比
                Text(
                    text = "$usage%",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = usageColor
                )
            }

            // 历史趋势图
            if (history.isNotEmpty()) {
                Spacer(modifier = Modifier.height(Spacing.Medium))
                UsageHistoryChart(
                    history = history,
                    usage = usage,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                )
            }

            // 进度条
            Spacer(modifier = Modifier.height(Spacing.Medium))

            LinearProgressIndicator(
                progress = { usage / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(CornerRadius.Full)),
                color = usageColor,
                trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                strokeCap = StrokeCap.Round,
                gapSize = 0.dp,
                drawStopIndicator = {}
            )

            // 副标题
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(Spacing.Small))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * 使用率历史趋势图组件（使用 vico）
 */
@Composable
private fun UsageHistoryChart(
    history: List<Int>,
    usage: Int,
    modifier: Modifier = Modifier
) {
    val usageColor = getUsageColor(usage)

    if (history.size < 2) {
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
                        fill = LineCartesianLayer.LineFill.single(Fill(usageColor)),
                        areaFill = LineCartesianLayer.AreaFill.single(
                            Fill(usageColor.copy(alpha = 0.2f))
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
