package wang.zengye.dsm.ui.security

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlin.math.sqrt
import wang.zengye.dsm.R

/**
 * 手势密码点
 */
private data class GesturePoint(
    val index: Int,
    val x: Float,
    val y: Float
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GesturePasswordScreen(
    onNavigateBack: () -> Unit = {},
    onVerifySuccess: () -> Unit = {}
) {
    val strDrawPattern = stringResource(R.string.security_draw_pattern)
    val strConfirmPattern = stringResource(R.string.security_confirm_pattern)
    val strPatternTooShort = stringResource(R.string.security_pattern_too_short)
    val strPatternSetSuccess = stringResource(R.string.security_pattern_set_success)
    val strPatternMismatch = stringResource(R.string.security_pattern_mismatch)
    val strPatternCorrect = stringResource(R.string.security_pattern_correct)
    val strPatternWrong = stringResource(R.string.security_pattern_wrong)
    val strDrawNewPattern = stringResource(R.string.security_draw_new_pattern)

    var currentPattern by remember { mutableStateOf(listOf<Int>()) }
    var isDrawing by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf(strDrawPattern) }
    var messageColor by remember { mutableStateOf(Color.Unspecified) }
    var isSettingMode by remember { mutableStateOf(false) }
    var confirmPattern by remember { mutableStateOf<List<Int>?>(null) }
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }

    // 9个点的位置（3x3网格）
    val points = remember {
        listOf(
            GesturePoint(0, 0.2f, 0.2f),
            GesturePoint(1, 0.5f, 0.2f),
            GesturePoint(2, 0.8f, 0.2f),
            GesturePoint(3, 0.2f, 0.5f),
            GesturePoint(4, 0.5f, 0.5f),
            GesturePoint(5, 0.8f, 0.5f),
            GesturePoint(6, 0.2f, 0.8f),
            GesturePoint(7, 0.5f, 0.8f),
            GesturePoint(8, 0.8f, 0.8f)
        )
    }

    val primaryColor = MaterialTheme.colorScheme.primary
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val errorColor = MaterialTheme.colorScheme.error
    val successColor = MaterialTheme.colorScheme.primary

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.security_gesture_password)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.common_back))
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            // 状态图标
            Icon(
                imageVector = if (isSettingMode) Icons.Filled.Lock else Icons.Filled.Gesture,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = primaryColor
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 提示文字
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = messageColor
            )

            Spacer(modifier = Modifier.height(32.dp))

            // 手势绘制区域
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .onSizeChanged { canvasSize = it }
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onPress = { offset ->
                                    isDrawing = true
                                    currentPattern = emptyList()

                                    // 检测触摸点
                                    val width = canvasSize.width.toFloat()
                                    val height = canvasSize.height.toFloat()
                                    if (width > 0 && height > 0) {
                                        val touchedPoint = points.find { point ->
                                            val pointX = point.x * width
                                            val pointY = point.y * height
                                            val distance = sqrt(
                                                (offset.x - pointX) * (offset.x - pointX) +
                                                        (offset.y - pointY) * (offset.y - pointY)
                                            )
                                            distance < 60f // 触摸半径
                                        }

                                        touchedPoint?.let {
                                            currentPattern = currentPattern + it.index
                                        }
                                    }

                                    tryAwaitRelease()
                                    isDrawing = false

                                    // 处理图案
                                    if (isSettingMode) {
                                        if (confirmPattern == null) {
                                            // 第一次绘制
                                            if (currentPattern.size >= 4) {
                                                confirmPattern = currentPattern
                                                message = strConfirmPattern
                                                messageColor = onSurfaceColor
                                            } else {
                                                message = strPatternTooShort
                                                messageColor = errorColor
                                            }
                                        } else {
                                            // 确认绘制
                                            if (currentPattern == confirmPattern) {
                                                message = strPatternSetSuccess
                                                messageColor = successColor
                                            } else {
                                                message = strPatternMismatch
                                                messageColor = errorColor
                                                confirmPattern = null
                                            }
                                        }
                                    } else {
                                        // 验证模式
                                        if (currentPattern.size >= 4) {
                                            message = strPatternCorrect
                                            messageColor = successColor
                                            onVerifySuccess()
                                        } else {
                                            message = strPatternWrong
                                            messageColor = errorColor
                                        }
                                    }

                                    currentPattern = emptyList()
                                }
                            )
                        }
                ) {
                    val width = this.size.width
                    val height = this.size.height
                    val radius = 20f

                    // 绘制点
                    points.forEach { point ->
                        val centerX = point.x * width
                        val centerY = point.y * height

                        // 外圈
                        drawCircle(
                            color = if (currentPattern.contains(point.index)) primaryColor else onSurfaceColor.copy(alpha = 0.3f),
                            radius = radius,
                            center = Offset(centerX, centerY)
                        )

                        // 内圈
                        drawCircle(
                            color = if (currentPattern.contains(point.index)) Color.White else onSurfaceColor.copy(alpha = 0.5f),
                            radius = radius * 0.5f,
                            center = Offset(centerX, centerY)
                        )
                    }

                    // 绘制连线
                    if (currentPattern.size > 1) {
                        for (i in 0 until currentPattern.size - 1) {
                            val p1 = points[currentPattern[i]]
                            val p2 = points[currentPattern[i + 1]]
                            drawLine(
                                color = primaryColor,
                                start = Offset(p1.x * width, p1.y * height),
                                end = Offset(p2.x * width, p2.y * height),
                                strokeWidth = 8f,
                                cap = StrokeCap.Round
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // 操作按钮
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (isSettingMode) {
                    OutlinedButton(
                        onClick = {
                            isSettingMode = false
                            confirmPattern = null
                            message = strDrawPattern
                            messageColor = Color.Unspecified
                        }
                    ) {
                        Text(stringResource(R.string.common_cancel))
                    }
                } else {
                    Button(
                        onClick = {
                            isSettingMode = true
                            confirmPattern = null
                            message = strDrawNewPattern
                            messageColor = onSurfaceColor
                        }
                    ) {
                        Icon(Icons.Filled.Lock, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.security_set_gesture_password))
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // 说明
            Text(
                text = stringResource(R.string.security_pattern_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
