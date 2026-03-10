package wang.zengye.dsm.ui.player.controls

import android.view.ViewGroup
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import `is`.xyz.mpv.MPVLib
import `is`.xyz.mpv.Utils
import kotlinx.coroutines.delay
import wang.zengye.dsm.data.api.DsmApiHelper
import wang.zengye.dsm.ui.file.MediaListManager
import wang.zengye.dsm.ui.player.MPVPlayerView
import wang.zengye.dsm.ui.player.MediaItem
import wang.zengye.dsm.ui.player.controls.sheets.MoreSheet
import wang.zengye.dsm.ui.player.controls.sheets.PlaybackSpeedSheet
import wang.zengye.dsm.ui.theme.Spacing
import kotlin.math.abs

// 动画规格 - 与 mpvKt 一致
fun <T> playerControlsExitAnimationSpec(): FiniteAnimationSpec<T> = tween(
    durationMillis = 300,
    easing = FastOutSlowInEasing,
)

fun <T> playerControlsEnterAnimationSpec(): FiniteAnimationSpec<T> = tween(
    durationMillis = 100,
    easing = LinearOutSlowInEasing,
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun PlayerControls(
    videoUrl: String,
    videoTitle: String = "",
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? androidx.activity.ComponentActivity
    val lifecycleOwner = LocalLifecycleOwner.current

    // 播放器视图引用
    var playerView by remember { mutableStateOf<MPVPlayerView?>(null) }
    var isInitialized by remember { mutableStateOf(false) }

    // 媒体列表
    val mediaList = remember { MediaListManager.videoList.map { MediaItem(DsmApiHelper.getDownloadUrl(it.path), it.name) } }
    var currentMediaIndex by remember(videoUrl, mediaList) {
        mutableStateOf(mediaList.indexOfFirst { it.url == videoUrl }.coerceAtLeast(0))
    }
    val hasPrevious = currentMediaIndex > 0
    val hasNext = currentMediaIndex < mediaList.size - 1

    // MPVLib 状态
    val pausedForCache by if (isInitialized) MPVLib.propBoolean["paused-for-cache"].collectAsState(initial = null) else remember { mutableStateOf(null) }
    val paused by if (isInitialized) MPVLib.propBoolean["pause"].collectAsState(initial = null) else remember { mutableStateOf(null) }
    val duration by if (isInitialized) MPVLib.propInt["duration"].collectAsState(initial = null) else remember { mutableStateOf(null) }
    val position by if (isInitialized) MPVLib.propInt["time-pos"].collectAsState(initial = null) else remember { mutableStateOf(null) }
    val playbackSpeed by if (isInitialized) MPVLib.propFloat["speed"].collectAsState(initial = null) else remember { mutableStateOf(null) }
    val readAhead by if (isInitialized) MPVLib.propFloat["demuxer-cache-time"].collectAsState(initial = null) else remember { mutableStateOf(null) }
    val remaining by if (isInitialized) MPVLib.propFloat["playtime-remaining"].collectAsState(initial = null) else remember { mutableStateOf(null) }
    val mediaName by if (isInitialized) MPVLib.propString["media-title"].collectAsState() else remember { mutableStateOf(null) }

    // UI 状态
    var controlsShown by remember { mutableStateOf(true) }
    var areControlsLocked by remember { mutableStateOf(false) }
    var seekBarShown by remember { mutableStateOf(false) }
    var isSeeking by remember { mutableStateOf(false) }
    var resetControls by remember { mutableStateOf(true) }

    // 手势状态
    var gestureSeekAmount by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    var doubleTapSeekAmount by remember { mutableIntStateOf(0) }
    var seekText by remember { mutableStateOf<String?>(null) }
    var wasPausedBeforeSeek by remember { mutableStateOf(false) }

    // 亮度音量
    var brightness by remember { mutableFloatStateOf(0.5f) }
    var volume by remember { mutableIntStateOf(50) }
    var isBrightnessSliderShown by remember { mutableStateOf(false) }
    var isVolumeSliderShown by remember { mutableStateOf(false) }

    // Sheet状态
    var showSpeedSheet by remember { mutableStateOf(false) }
    var showMoreSheet by remember { mutableStateOf(false) }
    var speedPresets by remember { mutableStateOf(listOf("0.50", "1.00", "1.25", "1.50", "2.00")) }
    var invertDuration by remember { mutableStateOf(true) }

    // 方向锁定（从 SharedPreferences 读取记忆）
    val orientationPrefs = remember {
        context.getSharedPreferences("player_prefs", android.content.Context.MODE_PRIVATE)
    }
    var isOrientationLocked by remember {
        mutableStateOf(orientationPrefs.getBoolean("orientation_locked", false))
    }

    // 初始化时应用方向锁定
    LaunchedEffect(isOrientationLocked) {
        if (isOrientationLocked) {
            // 锁定为当前实际方向
            val currentOrientation = context.resources.configuration.orientation
            activity?.requestedOrientation = if (currentOrientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE) {
                android.content.pm.ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
            } else {
                android.content.pm.ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
            }
        } else {
            activity?.requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_SENSOR
        }
    }

    // rememberUpdatedState 避免 pointerInput 闭包捕获过期值
    val currentControlsShown by rememberUpdatedState(controlsShown)
    val currentPaused by rememberUpdatedState(paused)
    val currentPosition by rememberUpdatedState(position)
    val currentDuration by rememberUpdatedState(duration)
    val currentWasPausedBeforeSeek by rememberUpdatedState(wasPausedBeforeSeek)
    val currentInvertDuration by rememberUpdatedState(invertDuration)
    val currentDoubleTapSeekAmount by rememberUpdatedState(doubleTapSeekAmount)

    // 音量管理器
    val audioManager = remember {
        context.getSystemService(android.content.Context.AUDIO_SERVICE) as android.media.AudioManager
    }
    val maxVolume = remember {
        audioManager.getStreamMaxVolume(android.media.AudioManager.STREAM_MUSIC)
    }

    // 自动隐藏控制层
    LaunchedEffect(controlsShown, paused, isSeeking, resetControls) {
        if (controlsShown && paused == false && !isSeeking) {
            delay(3000)
            controlsShown = false
        }
    }

    // 重置双击快进快退
    LaunchedEffect(doubleTapSeekAmount) {
        if (doubleTapSeekAmount != 0) {
            delay(800)
            doubleTapSeekAmount = 0
            seekText = null
            seekBarShown = false
        }
    }

    // 隐藏亮度音量滑块
    LaunchedEffect(isBrightnessSliderShown) {
        if (isBrightnessSliderShown) delay(2000)
        isBrightnessSliderShown = false
    }
    LaunchedEffect(isVolumeSliderShown) {
        if (isVolumeSliderShown) delay(2000)
        isVolumeSliderShown = false
    }

    // 初始化亮度
    LaunchedEffect(Unit) {
        try {
            val b = android.provider.Settings.System.getFloat(
                context.contentResolver,
                android.provider.Settings.System.SCREEN_BRIGHTNESS
            )
            brightness = b / 255f
        } catch (_: Exception) {}
        volume = audioManager.getStreamVolume(android.media.AudioManager.STREAM_MUSIC)
    }

    // 自动播放下一个
    LaunchedEffect(position, duration, isInitialized, hasNext) {
        if (isInitialized && (duration ?: 0) > 0 && (position ?: 0) >= (duration ?: 0) - 1 && hasNext) {
            val nextItem = mediaList[currentMediaIndex + 1]
            MPVLib.command("loadfile", nextItem.url)
            currentMediaIndex++
        }
    }

    fun playMedia(index: Int) {
        if (index in mediaList.indices) {
            val item = mediaList[index]
            MPVLib.command("loadfile", item.url)
            currentMediaIndex = index
        }
    }

    // 点击事件重置隐藏计时器
    fun onButtonsClick() {
        resetControls = !resetControls
    }

    val interactionSource = remember { MutableInteractionSource() }

    // 使用 remember + rememberUpdatedState 避免 pointerInput 闭包捕获过期值
    val onToggleControls = remember {
        { controlsShown = !currentControlsShown }
    }

    val onDoubleTapLeft = remember {
        {
            var amount = currentDoubleTapSeekAmount
            if (amount >= 0) amount = 0
            amount -= 10
            val newPos = ((currentPosition ?: 0) + amount).coerceAtLeast(0)
            MPVLib.command("seek", newPos.toString(), "absolute")
            doubleTapSeekAmount = amount
            seekText = "${amount}s"
            seekBarShown = true
        }
    }

    val onDoubleTapRight = remember {
        {
            var amount = currentDoubleTapSeekAmount
            if (amount <= 0) amount = 0
            amount += 10
            val newPos = ((currentPosition ?: 0) + amount).coerceAtMost(currentDuration ?: 0)
            MPVLib.command("seek", newPos.toString(), "absolute")
            doubleTapSeekAmount = amount
            seekText = "+${amount}s"
            seekBarShown = true
        }
    }
    
    val onDoubleTapCenter = remember {
        { MPVLib.command("cycle", "pause") }
    }
    
    val onHorizontalDragStart = remember {
        {
            wasPausedBeforeSeek = currentPaused ?: true
            isSeeking = true
            if (currentPaused == false) MPVLib.setPropertyBoolean("pause", true)
        }
    }

    val onHorizontalDrag = remember {
        { dragAmount: Float ->
            val dur = currentDuration ?: 0
            val pos = currentPosition ?: 0
            val sensitivity = if (dur > 0) dur.toFloat() / 1000 * 0.5f else 1f
            val seekDelta = (dragAmount * sensitivity).toInt()
            val newPos = (pos + seekDelta).coerceIn(0, dur)
            gestureSeekAmount = pos to seekDelta
            MPVLib.command("seek", newPos.toString(), "absolute")
        }
    }

    val onHorizontalDragEnd = remember {
        {
            gestureSeekAmount = null
            isSeeking = false
            if (!currentWasPausedBeforeSeek) MPVLib.setPropertyBoolean("pause", false)
        }
    }
    
    val onBrightnessChange = remember {
        { newBrightness: Float ->
            brightness = newBrightness
            activity?.window?.attributes = activity.window?.attributes?.apply {
                screenBrightness = newBrightness
            }
            isBrightnessSliderShown = true
        }
    }

    val onVolumeChange = remember {
        { newVolume: Int ->
            volume = newVolume
            audioManager.setStreamVolume(android.media.AudioManager.STREAM_MUSIC, newVolume, 0)
            isVolumeSliderShown = true
        }
    }
    
    val onSeekValueChange = remember {
        { value: Float ->
            isSeeking = true
            MPVLib.command("seek", value.toInt().toString(), "absolute")
        }
    }
    
    val onSeekValueChangeFinished = remember {
        { isSeeking = false }
    }
    
    val onLockControls = remember {
        { areControlsLocked = true }
    }
    
    val onCycleRotation = remember(activity) {
        {
            activity?.requestedOrientation = when (activity?.requestedOrientation) {
                android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE,
                android.content.pm.ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE,
                android.content.pm.ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE,
                -> android.content.pm.ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
                else -> android.content.pm.ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
            }
        }
    }

    val onToggleOrientationLock = remember {
        {
            val newLocked = !isOrientationLocked
            isOrientationLocked = newLocked
            orientationPrefs.edit().putBoolean("orientation_locked", newLocked).apply()
        }
    }
    
    val onPlaybackSpeedChange = remember {
        { speed: Float -> MPVLib.setPropertyFloat("speed", speed) }
    }
    
    val onOpenSpeedSheet = remember {
        { showSpeedSheet = true }
    }
    
    val onPreviousClick = remember(hasPrevious, currentMediaIndex) {
        { if (hasPrevious) playMedia(currentMediaIndex - 1) }
    }
    
    val onNextClick = remember(hasNext, currentMediaIndex) {
        { if (hasNext) playMedia(currentMediaIndex + 1) }
    }
    
    val onAspectClick = remember {
        {
            val current = MPVLib.getPropertyDouble("video-aspect-override") ?: -1.0
            val next = when {
                current < 0 -> 0.0                          // 原始 → 填充
                current == 0.0 -> 16.0 / 9.0                // 填充 → 16:9
                abs(current - 16.0 / 9.0) < 0.01 -> 4.0 / 3.0  // 16:9 → 4:3
                else -> -1.0                                // 4:3 → 原始
            }
            MPVLib.setPropertyDouble("video-aspect-override", next)
        }
    }
    
    val onDismissSpeedSheet = remember {
        { showSpeedSheet = false }
    }
    
    val onDismissMoreSheet = remember {
        { showMoreSheet = false }
    }
    
    val onShowMoreSheet = remember {
        { showMoreSheet = true }
    }

    // 透明背景动画
    val transparentOverlay by animateFloatAsState(
        if (controlsShown && !areControlsLocked) 0.8f else 0f,
        animationSpec = playerControlsExitAnimationSpec(),
        label = "controls_transparent_overlay",
    )

    Box(modifier = Modifier.fillMaxSize()) {
        // MPV 播放器视图
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                Utils.copyAssets(ctx)
                MPVPlayerView(ctx).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    initialize(ctx.filesDir.path, ctx.cacheDir.path)
                    isInitialized = true
                    if (videoUrl.isNotEmpty()) {
                        playUrl(videoUrl)
                    }
                    playerView = this
                }
            }
        )

        // 手势处理层
        if (!areControlsLocked) {
            GestureHandler(
                controlsShown = controlsShown,
                areControlsLocked = areControlsLocked,
                paused = paused,
                duration = duration ?: 0,
                position = position ?: 0,
                brightness = brightness,
                volume = volume,
                maxVolume = maxVolume,
                onToggleControls = onToggleControls,
                onDoubleTapLeft = onDoubleTapLeft,
                onDoubleTapRight = onDoubleTapRight,
                onDoubleTapCenter = onDoubleTapCenter,
                onHorizontalDragStart = onHorizontalDragStart,
                onHorizontalDrag = onHorizontalDrag,
                onHorizontalDragEnd = onHorizontalDragEnd,
                onBrightnessChange = onBrightnessChange,
                onVolumeChange = onVolumeChange,
                interactionSource = interactionSource
            )
        }

        // 双击快进快退椭圆指示器
        DoubleTapToSeekOvals(
            amount = doubleTapSeekAmount,
            text = seekText,
            showOvals = true,
            showSeekIcon = true,
            showSeekTime = true,
            interactionSource = interactionSource
        )

        // 控制层 - 使用 ConstraintLayout
        CompositionLocalProvider(
            LocalContentColor provides Color.White,
            LocalLayoutDirection provides LayoutDirection.Ltr,
        ) {
            ConstraintLayout(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            Pair(0f, Color.Black),
                            Pair(0.2f, Color.Transparent),
                            Pair(0.7f, Color.Transparent),
                            Pair(1f, Color.Black),
                        ),
                        alpha = transparentOverlay,
                    )
                    .padding(horizontal = Spacing.Standard)
            ) {
                val (topLeftControls, topRightControls) = createRefs()
                val (volumeSlider, brightnessSlider) = createRefs()
                val unlockControlsButton = createRef()
                val (bottomRightControls, bottomLeftControls) = createRefs()
                val playerPauseButton = createRef()
                val seekbar = createRef()

                // 亮度滑块
                AnimatedVisibility(
                    isBrightnessSliderShown,
                    enter = slideInHorizontally(playerControlsEnterAnimationSpec()) { it } + fadeIn(playerControlsEnterAnimationSpec()),
                    exit = slideOutHorizontally(playerControlsExitAnimationSpec()) { it } + fadeOut(playerControlsExitAnimationSpec()),
                    modifier = Modifier.constrainAs(brightnessSlider) {
                        end.linkTo(parent.end, Spacing.Standard)
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                    },
                ) { BrightnessSlider(brightness, 0f..1f) }

                // 音量滑块
                AnimatedVisibility(
                    isVolumeSliderShown,
                    enter = slideInHorizontally(playerControlsEnterAnimationSpec()) { -it } + fadeIn(playerControlsEnterAnimationSpec()),
                    exit = slideOutHorizontally(playerControlsExitAnimationSpec()) { -it } + fadeOut(playerControlsExitAnimationSpec()),
                    modifier = Modifier.constrainAs(volumeSlider) {
                        start.linkTo(parent.start, Spacing.Standard)
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                    },
                ) { VolumeSlider(volume, 0..maxVolume) }

                // 解锁按钮（锁定状态时显示）
                AnimatedVisibility(
                    controlsShown && areControlsLocked,
                    enter = fadeIn(),
                    exit = fadeOut(),
                    modifier = Modifier.constrainAs(unlockControlsButton) {
                        top.linkTo(parent.top, Spacing.Standard)
                        start.linkTo(parent.start, Spacing.Standard)
                    },
                ) {
                    val onUnlockClick = remember { { areControlsLocked = false } }
                    ControlsButton(
                        Icons.Filled.Lock,
                        onClick = onUnlockClick,
                    )
                }

                // 中间播放/暂停按钮或加载指示器
                AnimatedVisibility(
                    visible = (controlsShown && !areControlsLocked || gestureSeekAmount != null) || pausedForCache == true,
                    enter = fadeIn(playerControlsEnterAnimationSpec()),
                    exit = fadeOut(playerControlsExitAnimationSpec()),
                    modifier = Modifier.constrainAs(playerPauseButton) {
                        end.linkTo(parent.absoluteRight)
                        start.linkTo(parent.absoluteLeft)
                        top.linkTo(parent.top)
                        bottom.linkTo(parent.bottom)
                    },
                ) {
                    when {
                        gestureSeekAmount != null -> {
                            Text(
                                text = "${if (gestureSeekAmount!!.second >= 0) '+' else '-'}${Utils.prettyTime(abs(gestureSeekAmount!!.second))}\n${Utils.prettyTime(gestureSeekAmount!!.first + gestureSeekAmount!!.second)}",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    shadow = Shadow(Color.Black, blurRadius = 5f),
                                ),
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                            )
                        }
                        pausedForCache == true -> {
                            CircularProgressIndicator(
                                Modifier.size(96.dp),
                                strokeWidth = 6.dp,
                            )
                        }
                        controlsShown && !areControlsLocked -> {
                            val iconInteraction = remember { MutableInteractionSource() }
                            val onPlayPauseClick = remember {
                                {
                                    onButtonsClick()
                                    MPVLib.command("cycle", "pause")
                                }
                            }
                            androidx.compose.foundation.Image(
                                imageVector = if (paused == false) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(96.dp)
                                    .clip(CircleShape)
                                    .clickable(
                                        iconInteraction,
                                        ripple(),
                                        onClick = onPlayPauseClick
                                    )
                                    .padding(Spacing.Standard),
                            )
                        }
                    }
                }

                // 进度条
                AnimatedVisibility(
                    visible = (controlsShown || seekBarShown) && !areControlsLocked,
                    enter = slideInVertically(playerControlsEnterAnimationSpec()) { it } + fadeIn(playerControlsEnterAnimationSpec()),
                    exit = slideOutVertically(playerControlsExitAnimationSpec()) { it } + fadeOut(playerControlsExitAnimationSpec()),
                    modifier = Modifier.constrainAs(seekbar) {
                        bottom.linkTo(parent.bottom, Spacing.Standard)
                    },
                ) {
                    SeekbarWithTimers(
                        position = (position ?: 0).toFloat(),
                        duration = (duration ?: 0).toFloat(),
                        remaining = remaining ?: 0f,
                        readAheadValue = readAhead ?: 0f,
                        onValueChange = onSeekValueChange,
                        onValueChangeFinished = onSeekValueChangeFinished,
                        timersInverted = Pair(false, invertDuration),
                        positionTimerOnClick = {},
                        durationTimerOnClick = remember { { invertDuration = !currentInvertDuration } },
                        chapters = emptyList(),
                    )
                }

                // 左上角控制栏
                val displayTitle = videoTitle.ifEmpty { mediaName ?: mediaList.getOrNull(currentMediaIndex)?.name ?: "" }
                AnimatedVisibility(
                    controlsShown && !areControlsLocked,
                    enter = slideInHorizontally(playerControlsEnterAnimationSpec()) { -it } + fadeIn(playerControlsEnterAnimationSpec()),
                    exit = slideOutHorizontally(playerControlsExitAnimationSpec()) { -it } + fadeOut(playerControlsExitAnimationSpec()),
                    modifier = Modifier.constrainAs(topLeftControls) {
                        top.linkTo(parent.top, Spacing.Standard)
                        start.linkTo(parent.start)
                        width = Dimension.fillToConstraints
                        end.linkTo(topRightControls.start)
                    },
                ) {
                    TopLeftPlayerControls(
                        mediaTitle = displayTitle,
                        onBackClick = onBack,
                    )
                }

                // 右上角控制栏
                AnimatedVisibility(
                    controlsShown && !areControlsLocked,
                    enter = slideInHorizontally(playerControlsEnterAnimationSpec()) { it } + fadeIn(playerControlsEnterAnimationSpec()),
                    exit = slideOutHorizontally(playerControlsExitAnimationSpec()) { it } + fadeOut(playerControlsExitAnimationSpec()),
                    modifier = Modifier.constrainAs(topRightControls) {
                        top.linkTo(parent.top, Spacing.Standard)
                        end.linkTo(parent.end)
                    },
                ) {
                    TopRightPlayerControls(
                        onSubtitlesClick = onShowMoreSheet,
                        onAudioClick = onShowMoreSheet,
                        onMoreClick = onShowMoreSheet,
                    )
                }

                // 左下角控制栏
                AnimatedVisibility(
                    controlsShown && !areControlsLocked,
                    enter = slideInHorizontally(playerControlsEnterAnimationSpec()) { -it } + fadeIn(playerControlsEnterAnimationSpec()),
                    exit = slideOutHorizontally(playerControlsExitAnimationSpec()) { -it } + fadeOut(playerControlsExitAnimationSpec()),
                    modifier = Modifier.constrainAs(bottomLeftControls) {
                        bottom.linkTo(seekbar.top)
                        start.linkTo(seekbar.start)
                        width = Dimension.fillToConstraints
                        end.linkTo(bottomRightControls.start)
                    },
                ) {
                    BottomLeftPlayerControls(
                        playbackSpeed = playbackSpeed ?: 1f,
                        isOrientationLocked = isOrientationLocked,
                        onLockControls = onLockControls,
                        onCycleRotation = onCycleRotation,
                        onToggleOrientationLock = onToggleOrientationLock,
                        onPlaybackSpeedChange = onPlaybackSpeedChange,
                        onOpenSpeedSheet = onOpenSpeedSheet,
                    )
                }

                // 右下角控制栏
                AnimatedVisibility(
                    controlsShown && !areControlsLocked,
                    enter = slideInHorizontally(playerControlsEnterAnimationSpec()) { it } + fadeIn(playerControlsEnterAnimationSpec()),
                    exit = slideOutHorizontally(playerControlsExitAnimationSpec()) { it } + fadeOut(playerControlsExitAnimationSpec()),
                    modifier = Modifier.constrainAs(bottomRightControls) {
                        bottom.linkTo(seekbar.top)
                        end.linkTo(seekbar.end)
                    },
                ) {
                    BottomRightPlayerControls(
                        hasPrevious = hasPrevious,
                        hasNext = hasNext,
                        onPreviousClick = onPreviousClick,
                        onNextClick = onNextClick,
                        isPipAvailable = false,
                        onPipClick = {},
                        onAspectClick = onAspectClick,
                    )
                }
            }
        }

        // 播放速度面板
        if (showSpeedSheet) {
            val onSpeedChange = remember { { speed: Float -> MPVLib.setPropertyFloat("speed", speed) } }
            val onAddSpeedPreset = remember(speedPresets) {
                { speed: Float ->
                    val preset = String.format("%.2f", speed)
                    if (preset !in speedPresets) {
                        speedPresets = (speedPresets + preset).sorted()
                    }
                }
            }
            val onRemoveSpeedPreset = remember(speedPresets) {
                { speed: Float ->
                    speedPresets = speedPresets - String.format("%.2f", speed)
                }
            }
            val onResetPresets = remember {
                { speedPresets = listOf("0.50", "1.00", "1.25", "1.50", "2.00") }
            }
            PlaybackSpeedSheet(
                speed = playbackSpeed ?: 1f,
                speedPresets = speedPresets.map { it.toFloat() },
                onSpeedChange = onSpeedChange,
                onAddSpeedPreset = onAddSpeedPreset,
                onRemoveSpeedPreset = onRemoveSpeedPreset,
                onResetPresets = onResetPresets,
                onDismissRequest = onDismissSpeedSheet
            )
        }

        // 更多面板
        if (showMoreSheet) {
            MoreSheet(
                onDismissRequest = onDismissMoreSheet
            )
        }
    }

    // 生命周期管理
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> MPVLib.setPropertyBoolean("pause", true)
                Lifecycle.Event.ON_DESTROY -> playerView?.destroy()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            playerView?.destroy()
        }
    }
}

/**
 * 手势处理器
 */
@Composable
fun GestureHandler(
    controlsShown: Boolean,
    areControlsLocked: Boolean,
    paused: Boolean?,
    duration: Int,
    position: Int,
    brightness: Float,
    volume: Int,
    maxVolume: Int,
    onToggleControls: () -> Unit,
    onDoubleTapLeft: () -> Unit,
    onDoubleTapRight: () -> Unit,
    onDoubleTapCenter: () -> Unit,
    onHorizontalDragStart: () -> Unit,
    onHorizontalDrag: (Float) -> Unit,
    onHorizontalDragEnd: () -> Unit,
    onBrightnessChange: (Float) -> Unit,
    onVolumeChange: (Int) -> Unit,
    interactionSource: MutableInteractionSource,
) {
    // rememberUpdatedState 确保 pointerInput 闭包始终读到最新值
    val currentBrightness by rememberUpdatedState(brightness)
    val currentVolume by rememberUpdatedState(volume)
    val currentMaxVolume by rememberUpdatedState(maxVolume)
    val currentOnToggleControls by rememberUpdatedState(onToggleControls)
    val currentOnDoubleTapLeft by rememberUpdatedState(onDoubleTapLeft)
    val currentOnDoubleTapRight by rememberUpdatedState(onDoubleTapRight)
    val currentOnDoubleTapCenter by rememberUpdatedState(onDoubleTapCenter)
    val currentOnHorizontalDragStart by rememberUpdatedState(onHorizontalDragStart)
    val currentOnHorizontalDrag by rememberUpdatedState(onHorizontalDrag)
    val currentOnHorizontalDragEnd by rememberUpdatedState(onHorizontalDragEnd)
    val currentOnBrightnessChange by rememberUpdatedState(onBrightnessChange)
    val currentOnVolumeChange by rememberUpdatedState(onVolumeChange)
    val currentAreControlsLocked by rememberUpdatedState(areControlsLocked)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { currentOnToggleControls() },
                    onDoubleTap = { offset ->
                        if (currentAreControlsLocked) return@detectTapGestures
                        when {
                            offset.x > size.width * 3 / 5 -> currentOnDoubleTapRight()
                            offset.x < size.width * 2 / 5 -> currentOnDoubleTapLeft()
                            else -> currentOnDoubleTapCenter()
                        }
                    }
                )
            }
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragStart = {
                        if (!currentAreControlsLocked) currentOnHorizontalDragStart()
                    },
                    onDragEnd = {
                        if (!currentAreControlsLocked) currentOnHorizontalDragEnd()
                    },
                    onDragCancel = {
                        if (!currentAreControlsLocked) currentOnHorizontalDragEnd()
                    }
                ) { change, dragAmount ->
                    if (!currentAreControlsLocked) {
                        currentOnHorizontalDrag(dragAmount)
                        change.consume()
                    }
                }
            }
            .pointerInput(Unit) {
                detectVerticalDragGestures { change, dragAmount ->
                    if (currentAreControlsLocked) return@detectVerticalDragGestures
                    if (change.position.x < size.width / 2) {
                        currentOnBrightnessChange((currentBrightness - dragAmount * 0.002f).coerceIn(0f, 1f))
                    } else {
                        currentOnVolumeChange((currentVolume - (dragAmount * currentMaxVolume * 0.01f).toInt()).coerceIn(0, currentMaxVolume))
                    }
                    change.consume()
                }
            }
    )
}

/**
 * 双击快进快退椭圆指示器
 */
@Composable
fun DoubleTapToSeekOvals(
    amount: Int,
    text: String?,
    showOvals: Boolean,
    showSeekIcon: Boolean,
    showSeekTime: Boolean,
    interactionSource: MutableInteractionSource,
) {
    if (amount == 0) return

    val isForward = amount > 0
    val alpha by animateFloatAsState(
        targetValue = if (amount != 0) 1f else 0f,
        animationSpec = tween(300),
        label = "seek_oval_alpha",
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .align(if (isForward) Alignment.CenterEnd else Alignment.CenterStart)
                .padding(horizontal = 48.dp)
                .graphicsLayer { this.alpha = alpha },
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                if (showSeekIcon) {
                    DoubleTapSeekTriangles(isForward = isForward)
                }
                if (showSeekTime && text != null) {
                    Text(
                        text = text,
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            shadow = Shadow(Color.Black, blurRadius = 5f),
                        ),
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}