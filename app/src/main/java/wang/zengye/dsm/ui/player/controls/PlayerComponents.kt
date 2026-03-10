package wang.zengye.dsm.ui.player.controls

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeDown
import androidx.compose.material.icons.automirrored.filled.VolumeMute
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.BrightnessHigh
import androidx.compose.material.icons.filled.BrightnessLow
import androidx.compose.material.icons.filled.BrightnessMedium
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.layout
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.times
import dev.vivvvek.seeker.Seeker
import dev.vivvvek.seeker.SeekerDefaults
import dev.vivvvek.seeker.Segment
import `is`.xyz.mpv.Utils

/**
 * 进度条与时间显示
 * 与 mpvKt 一致：高度48.dp，时间宽度92.dp
 */
@Composable
fun SeekbarWithTimers(
    position: Float,
    duration: Float,
    remaining: Float,
    readAheadValue: Float,
    onValueChange: (Float) -> Unit,
    onValueChangeFinished: () -> Unit,
    timersInverted: Pair<Boolean, Boolean>,
    positionTimerOnClick: () -> Unit,
    durationTimerOnClick: () -> Unit,
    chapters: List<Segment>,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.height(48.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        VideoTimer(
            value = position,
            isInverted = timersInverted.first,
            onClick = positionTimerOnClick,
            modifier = Modifier.width(92.dp),
        )
        Seeker(
            value = position.coerceIn(0f, duration),
            range = 0f..duration,
            onValueChange = onValueChange,
            onValueChangeFinished = onValueChangeFinished,
            readAheadValue = readAheadValue,
            segments = chapters,
            modifier = Modifier.weight(1f),
            colors = SeekerDefaults.seekerColors(
                progressColor = MaterialTheme.colorScheme.primary,
                thumbColor = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.background,
                readAheadColor = MaterialTheme.colorScheme.inversePrimary,
            ),
        )
        VideoTimer(
            value = if (timersInverted.second) -remaining else duration,
            isInverted = timersInverted.second,
            onClick = durationTimerOnClick,
            modifier = Modifier.width(92.dp),
        )
    }
}

/**
 * 视频时间显示
 */
@Composable
fun VideoTimer(
    value: Float,
    isInverted: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    val interactionSource = remember { MutableInteractionSource() }
    Text(
        modifier = modifier
            .fillMaxHeight()
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(),
                onClick = onClick,
            )
            .wrapContentHeight(Alignment.CenterVertically),
        text = Utils.prettyTime(value.toInt(), isInverted),
        color = Color.White,
        textAlign = TextAlign.Center,
    )
}

/**
 * 垂直滑块
 */
@Composable
fun VerticalSlider(
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    modifier: Modifier = Modifier,
) {
    require(range.contains(value)) { "Value must be within the provided range" }
    Box(
        modifier = modifier
            .height(120.dp)
            .aspectRatio(0.2f)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.BottomCenter,
    ) {
        val targetHeight by animateFloatAsState(
            ((value - range.start) / (range.endInclusive - range.start)).coerceIn(0f, 1f),
            label = "vsliderheight"
        )
        Box(
            Modifier
                .fillMaxWidth()
                .fillMaxHeight(targetHeight)
                .background(MaterialTheme.colorScheme.tertiary),
        )
    }
}

/**
 * 垂直滑块 (Int版本)
 */
@Composable
fun VerticalSlider(
    value: Int,
    range: ClosedRange<Int>,
    modifier: Modifier = Modifier,
) {
    require(range.contains(value)) { "Value must be within the provided range" }
    Box(
        modifier = modifier
            .height(120.dp)
            .aspectRatio(0.2f)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.BottomCenter,
    ) {
        val targetHeight by animateFloatAsState(
            ((value - range.start - 0f) / (range.endInclusive - range.start)).coerceIn(0f, 1f),
            label = "vsliderheight"
        )
        Box(
            Modifier
                .fillMaxWidth()
                .fillMaxHeight(targetHeight)
                .background(MaterialTheme.colorScheme.tertiary),
        )
    }
}

/**
 * 亮度滑块
 */
@Composable
fun BrightnessSlider(
    brightness: Float,
    range: ClosedFloatingPointRange<Float>,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            (brightness * 100).toInt().toString(),
            style = MaterialTheme.typography.bodySmall,
        )
        VerticalSlider(brightness, range)
        Icon(
            when ((brightness / (range.endInclusive - range.start))) {
                in 0f..0.3f -> Icons.Default.BrightnessLow
                in 0.3f..0.6f -> Icons.Default.BrightnessMedium
                in 0.6f..1f -> Icons.Default.BrightnessHigh
                else -> Icons.Default.BrightnessMedium
            },
            contentDescription = null,
        )
    }
}

/**
 * 音量滑块
 */
@Composable
fun VolumeSlider(
    volume: Int,
    range: ClosedRange<Int>,
    modifier: Modifier = Modifier,
) {
    val percentage = ((volume - range.start).toFloat() / (range.endInclusive - range.start) * 100).toInt()
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            "$volume",
            style = MaterialTheme.typography.bodySmall,
        )
        VerticalSlider(volume, range)
        Icon(
            when (percentage) {
                0 -> Icons.AutoMirrored.Default.VolumeOff
                in 0..30 -> Icons.AutoMirrored.Default.VolumeMute
                in 30..60 -> Icons.AutoMirrored.Default.VolumeDown
                in 60..100 -> Icons.AutoMirrored.Default.VolumeUp
                else -> Icons.AutoMirrored.Default.VolumeOff
            },
            contentDescription = null,
        )
    }
}

/**
 * 双击快进快退三角形动画
 * 与 mpvKt 一致
 */
@Composable
fun DoubleTapSeekTriangles(
    isForward: Boolean,
    modifier: Modifier = Modifier
) {
    val animationDuration = 750L

    val alpha1 = remember { Animatable(0f) }
    val alpha2 = remember { Animatable(0f) }
    val alpha3 = remember { Animatable(0f) }

    LaunchedEffect(animationDuration) {
        while (true) {
            alpha1.animateTo(1f, animationSpec = tween((animationDuration / 5).toInt()))
            alpha2.animateTo(1f, animationSpec = tween((animationDuration / 5).toInt()))
            alpha3.animateTo(1f, animationSpec = tween((animationDuration / 5).toInt()))
            alpha1.animateTo(0f, animationSpec = tween((animationDuration / 5).toInt()))
            alpha2.animateTo(0f, animationSpec = tween((animationDuration / 5).toInt()))
            alpha3.animateTo(0f, animationSpec = tween((animationDuration / 5).toInt()))
        }
    }

    val rotation = if (isForward) 0f else 180f
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.graphicsLayer { rotationZ = rotation },
    ) {
        DoubleTapArrow(alpha1.value)
        DoubleTapArrow(alpha2.value)
        DoubleTapArrow(alpha3.value)
    }
}

@Composable
private fun DoubleTapArrow(alpha: Float) {
    // 使用三角形代替资源文件
    Box(
        modifier = Modifier
            .size(width = 16.dp, height = 20.dp)
            .graphicsLayer { this.alpha = alpha }
    ) {
        Icon(
            Icons.Default.PlayArrow,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.fillMaxSize()
        )
    }
}

/**
 * 播放速度指示器
 */
@Composable
fun MultipleSpeedPlayerUpdate(
    currentSpeed: Float,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "${currentSpeed}x",
            style = MaterialTheme.typography.headlineMedium.copy(
                shadow = androidx.compose.ui.graphics.Shadow(
                    color = Color.Black,
                    blurRadius = 5f,
                ),
            ),
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
        )
    }
}

/**
 * 文本指示器
 */
@Composable
fun TextPlayerUpdate(
    text: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.headlineMedium.copy(
                shadow = androidx.compose.ui.graphics.Shadow(
                    color = Color.Black,
                    blurRadius = 5f,
                ),
            ),
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
        )
    }
}

// 扩展函数
fun Float.toFixed(precision: Int = 1): Float {
    val factor = Math.pow(10.0, precision.toDouble()).toFloat()
    return (this * factor).toInt() / factor
}