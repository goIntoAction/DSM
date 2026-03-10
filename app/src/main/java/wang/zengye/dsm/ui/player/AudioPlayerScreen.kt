package wang.zengye.dsm.ui.player

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import `is`.xyz.mpv.MPVLib
import `is`.xyz.mpv.Utils
import wang.zengye.dsm.R
import wang.zengye.dsm.data.api.DsmApiHelper
import wang.zengye.dsm.ui.file.MediaListManager
import wang.zengye.dsm.ui.theme.*

/**
 * 音频播放器界面
 * 支持上一个/下一个切换
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioPlayerScreen(
    audioUrl: String,
    audioTitle: String = "",
    onBack: () -> Unit
) {
    // 是否已初始化
    var isInitialized by remember { mutableStateOf(false) }
    
    // 从 MediaListManager 获取媒体列表
    val mediaList = remember { MediaListManager.audioList.map { MediaItem(DsmApiHelper.getDownloadUrl(it.path), it.name) } }
    
    // 使用 MPVLib 的 Flow API 获取实时状态
    val pausedState by if (isInitialized) MPVLib.propBoolean["pause"].collectAsState(initial = null) else remember { mutableStateOf(null) }
    val positionState by if (isInitialized) MPVLib.propInt["time-pos"].collectAsState(initial = null) else remember { mutableStateOf(null) }
    val durationState by if (isInitialized) MPVLib.propInt["duration"].collectAsState(initial = null) else remember { mutableStateOf(null) }

    val paused = pausedState ?: true
    val position = positionState ?: 0
    val duration = durationState ?: 0
    
    // 当前播放索引（可变状态，切歌时同步更新）
    var currentIndex by remember {
        mutableIntStateOf(mediaList.indexOfFirst { it.url == audioUrl }.coerceAtLeast(0))
    }

    val hasPrevious = currentIndex > 0
    val hasNext = currentIndex < mediaList.size - 1

    // 自动播放防重复标记
    var autoPlayTriggered by remember { mutableStateOf(false) }

    // 初始化播放
    LaunchedEffect(audioUrl) {
        isInitialized = false
        autoPlayTriggered = false
        if (audioUrl.isNotEmpty()) {
            MPVLib.command("loadfile", audioUrl)
            isInitialized = true
        }
    }

    // 自动播放下一首（带防重复）
    LaunchedEffect(position, duration, isInitialized, hasNext) {
        if (isInitialized && duration > 0 && position >= duration - 1 && hasNext && !autoPlayTriggered) {
            autoPlayTriggered = true
            val nextIndex = currentIndex + 1
            val nextItem = mediaList[nextIndex]
            MPVLib.command("loadfile", nextItem.url)
            currentIndex = nextIndex
        }
        // position 回到起始时重置标记
        if (position < duration - 2) {
            autoPlayTriggered = false
        }
    }

    // 播放指定媒体
    fun playMedia(index: Int) {
        if (index in mediaList.indices) {
            val item = mediaList[index]
            MPVLib.command("loadfile", item.url)
            currentIndex = index
            autoPlayTriggered = false
        }
    }
    
    Scaffold(
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = audioTitle.ifEmpty { stringResource(R.string.player_audio_title) },
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium
                        )
                        if (mediaList.size > 1) {
                            Text(
                                text = "${currentIndex + 1} / ${mediaList.size}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.common_back))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = Color.Transparent
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // 封面占位区域
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .clip(RoundedCornerShape(CornerRadius.Large))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primaryContainer,
                                MaterialTheme.colorScheme.secondaryContainer
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.MusicNote,
                    contentDescription = null,
                    modifier = Modifier.size(80.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.5f)
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // 歌曲名称
            Text(
                text = audioTitle.ifEmpty { stringResource(R.string.player_unknown_track) },
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Medium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = Spacing.PageHorizontal)
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // 进度条
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.PageHorizontal)
            ) {
                Slider(
                    value = position.toFloat(),
                    onValueChange = { pos -> 
                        if (isInitialized) {
                            MPVLib.command("seek", pos.toInt().toString(), "absolute") 
                        }
                    },
                    valueRange = 0f..duration.toFloat().coerceAtLeast(1f),
                    modifier = Modifier.fillMaxWidth()
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = Utils.prettyTime(position),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = Utils.prettyTime(duration),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // 播放控制
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 上一个
                IconButton(
                    onClick = {
                        if (hasPrevious) {
                            playMedia(currentIndex - 1)
                        }
                    },
                    enabled = hasPrevious,
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(
                        Icons.Filled.SkipPrevious,
                        contentDescription = stringResource(R.string.player_previous),
                        modifier = Modifier.size(32.dp),
                        tint = if (hasPrevious) MaterialTheme.colorScheme.onSurface 
                               else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                    )
                }
                
                // 播放/暂停
                FilledIconButton(
                    onClick = { if (isInitialized) MPVLib.command("cycle", "pause") },
                    modifier = Modifier.size(72.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        if (paused) Icons.Filled.PlayArrow else Icons.Filled.Pause,
                        contentDescription = if (paused) stringResource(R.string.player_play) else stringResource(R.string.player_pause),
                        modifier = Modifier.size(40.dp),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
                
                // 下一个
                IconButton(
                    onClick = {
                        if (hasNext) {
                            playMedia(currentIndex + 1)
                        }
                    },
                    enabled = hasNext,
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(
                        Icons.Filled.SkipNext,
                        contentDescription = stringResource(R.string.player_next),
                        modifier = Modifier.size(32.dp),
                        tint = if (hasNext) MaterialTheme.colorScheme.onSurface 
                               else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                    )
                }
            }
            
            // 播放列表提示
            if (mediaList.size > 1) {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = stringResource(R.string.player_track_count, mediaList.size),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * 媒体项（用于播放列表）
 */
data class MediaItem(
    val url: String,
    val name: String
)
