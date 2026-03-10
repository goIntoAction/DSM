package wang.zengye.dsm.ui.photos

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import kotlin.math.abs
import wang.zengye.dsm.R
import wang.zengye.dsm.data.api.DsmApiHelper
import wang.zengye.dsm.data.model.PhotoItem
import wang.zengye.dsm.ui.file.MediaListManager
import wang.zengye.dsm.util.formatSize
import java.text.SimpleDateFormat
import java.util.*

/**
 * 照片预览页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoPreviewScreen(
    photoId: Long,
    photoList: List<PhotoItem> = emptyList(),
    onNavigateBack: () -> Unit,
    onNavigateToVideoPlayer: (String, String) -> Unit = { _, _ -> },
    viewModel: PhotoPreviewViewModel = hiltViewModel()
) {
    val uiState by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    val linkCopiedText = stringResource(R.string.photos_link_copied)

    // 从 MediaListManager 获取照片列表
    val mediaPhotoList = remember { MediaListManager.photoList }
    val effectivePhotoList = if (photoList.isNotEmpty()) photoList else mediaPhotoList

    // 处理事件
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is PhotoPreviewEvent.ShowError -> {
                    snackbarHostState.showSnackbar(event.message)
                }
            }
        }
    }

    // 当 photoList 为空时，从 ViewModel 加载照片
    LaunchedEffect(photoId) {
        if (effectivePhotoList.isEmpty()) {
            viewModel.sendIntent(PhotoPreviewIntent.LoadPhoto(photoId))
        }
    }

    // 找到当前照片在列表中的位置
    val initialPage = effectivePhotoList.indexOfFirst { it.id == photoId }.coerceAtLeast(0)
    
    val pagerState = rememberPagerState(
        initialPage = initialPage,
        pageCount = { effectivePhotoList.size.coerceAtLeast(1) }
    )
    
    // 当前显示的照片
    val currentPhoto = if (effectivePhotoList.isNotEmpty() && pagerState.currentPage < effectivePhotoList.size) {
        effectivePhotoList[pagerState.currentPage]
    } else if (effectivePhotoList.isNotEmpty()) {
        effectivePhotoList.find { it.id == photoId }
    } else {
        uiState.photo
    }
    
    var showControls by remember { mutableStateOf(true) }
    
    // 存储每个页面的缩放和偏移状态
    val pageStates = remember { mutableStateMapOf<Int, Pair<Float, Offset>>() }
    
    // 当前页面的缩放状态
    val currentScale = pageStates[pagerState.currentPage]?.first ?: 1f
    
    // 更新 ViewModel 中的照片
    LaunchedEffect(currentPhoto) {
        currentPhoto?.let { viewModel.sendIntent(PhotoPreviewIntent.SetPhoto(it)) }
    }
    
    Box(modifier = Modifier.fillMaxSize()) {
        // 照片查看器
        if (effectivePhotoList.isNotEmpty()) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                // 当当前页面放大时，禁用 pager 的滑动
                userScrollEnabled = currentScale <= 1.001f
            ) { page ->
                val photo = if (page < effectivePhotoList.size) effectivePhotoList[page] else null
                
                if (photo != null) {
                    val initialScale = pageStates[page]?.first ?: 1f
                    val initialOffset = pageStates[page]?.second ?: Offset.Zero
                    
                    PhotoViewer(
                        photo = photo,
                        initialScale = initialScale,
                        initialOffset = initialOffset,
                        onPageStateChange = { scale, offset ->
                            pageStates[page] = scale to offset
                        },
                        onTap = { showControls = !showControls }
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(stringResource(R.string.photos_cannot_load), color = Color.White)
                    }
                }
            }
        } else {
            // 单张照片模式
            val photo = uiState.photo
            if (photo != null) {
                PhotoViewer(
                    photo = photo,
                    initialScale = 1f,
                    initialOffset = Offset.Zero,
                    onPageStateChange = { _, _ -> },
                    onTap = { showControls = !showControls }
                )
            } else if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color.White)
                }
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(stringResource(R.string.photos_cannot_load), color = Color.White)
                }
            }
        }
        
        // 顶部控制栏
        AnimatedVisibility(
            visible = showControls,
            enter = slideInVertically { -it } + fadeIn(),
            exit = slideOutVertically { -it } + fadeOut(),
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            TopAppBar(
                title = {
                    Text(
                        text = currentPhoto?.filename ?: "",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.common_back))
                    }
                },
                actions = {
                    if (currentPhoto?.isVideo == true) {
                        IconButton(onClick = {
                            val url = DsmApiHelper.getPhotoDownloadUrl(currentPhoto.id)
                            onNavigateToVideoPlayer(url, currentPhoto.filename)
                        }) {
                            Icon(Icons.Default.PlayArrow, contentDescription = stringResource(R.string.photos_play))
                        }
                    }
                    
                    IconButton(onClick = { viewModel.sendIntent(PhotoPreviewIntent.ToggleInfo) }) {
                        Icon(Icons.Default.Info, contentDescription = stringResource(R.string.common_info))
                    }
                    
                    IconButton(onClick = {
                        currentPhoto?.let {
                            val url = DsmApiHelper.getPhotoDownloadUrl(it.id)
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("photo_url", url))
                            Toast.makeText(context, linkCopiedText, Toast.LENGTH_SHORT).show()
                        }
                    }) {
                        Icon(Icons.Default.Share, contentDescription = stringResource(R.string.photos_share))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black.copy(alpha = 0.5f),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        }
        
        // 底部信息栏
        AnimatedVisibility(
            visible = showControls,
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            BottomPhotoInfo(
                photo = currentPhoto,
                currentIndex = pagerState.currentPage + 1,
                totalCount = effectivePhotoList.size
            )
        }
    }
    
    // 照片详情对话框
    if (uiState.showInfo && currentPhoto != null) {
        PhotoInfoDialog(
            photo = currentPhoto,
            onDismiss = { viewModel.sendIntent(PhotoPreviewIntent.ToggleInfo) }
        )
    }
}

/**
 * 照片查看器
 */
@Composable
private fun PhotoViewer(
    photo: PhotoItem,
    initialScale: Float,
    initialOffset: Offset,
    onPageStateChange: (Float, Offset) -> Unit,
    onTap: () -> Unit
) {
    var currentScale by remember(initialScale) { mutableFloatStateOf(initialScale) }
    var currentOffset by remember(initialOffset) { mutableStateOf(initialOffset) }
    
    // 双击检测状态 - 必须在 pointerInput 外部保持
    var lastTapTime by remember { mutableLongStateOf(0L) }
    var lastTapPosition by remember { mutableStateOf(Offset.Zero) }
    
    val thumbnail = photo.additional?.thumbnail
    val imageUrl = if (thumbnail != null && thumbnail.unitId > 0 && thumbnail.cacheKey.isNotEmpty()) {
        DsmApiHelper.getPhotoThumbnailUrl(
            unitId = thumbnail.unitId,
            cacheKey = thumbnail.cacheKey,
            size = "xl"
        )
    } else {
        DsmApiHelper.getPhotoDownloadUrl(photo.id)
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(Unit) {
                // 统一处理所有触摸手势
                awaitEachGesture {
                    val firstDown = awaitFirstDown()
                    
                    var isGestureConsumed = false
                    var pointersCount = 1
                    
                    // 持续处理手势直到所有手指抬起
                    do {
                        val event = awaitPointerEvent()
                        pointersCount = event.changes.count { it.pressed }
                        
                        when (event.type) {
                            PointerEventType.Move -> {
                                if (pointersCount >= 2) {
                                    // 双指手势：缩放或移动
                                    val zoom = event.calculateZoom()
                                    val pan = event.calculatePan()
                                    
                                    isGestureConsumed = true
                                    
                                    val newScale = (currentScale * zoom).coerceIn(1f, 5f)
                                    
                                    if (abs(newScale - currentScale) > 0.001f || pan != Offset.Zero) {
                                        currentScale = newScale
                                        
                                        if (currentScale <= 1.001f) {
                                            currentOffset = Offset.Zero
                                        } else {
                                            val maxOffsetX = (size.width * (currentScale - 1f) / 2f)
                                            val maxOffsetY = (size.height * (currentScale - 1f) / 2f)
                                            
                                            currentOffset = Offset(
                                                x = (currentOffset.x + pan.x).coerceIn(-maxOffsetX, maxOffsetX),
                                                y = (currentOffset.y + pan.y).coerceIn(-maxOffsetY, maxOffsetY)
                                            )
                                        }
                                        onPageStateChange(currentScale, currentOffset)
                                    }
                                    
                                    event.changes.forEach { it.consume() }
                                } else if (pointersCount == 1 && currentScale > 1.001f) {
                                    // 单指拖动（仅放大状态）
                                    val pan = event.calculatePan()
                                    isGestureConsumed = true
                                    
                                    val maxOffsetX = (size.width * (currentScale - 1f) / 2f)
                                    val maxOffsetY = (size.height * (currentScale - 1f) / 2f)
                                    
                                    currentOffset = Offset(
                                        x = (currentOffset.x + pan.x).coerceIn(-maxOffsetX, maxOffsetX),
                                        y = (currentOffset.y + pan.y).coerceIn(-maxOffsetY, maxOffsetY)
                                    )
                                    onPageStateChange(currentScale, currentOffset)
                                    
                                    event.changes.forEach { it.consume() }
                                }
                            }
                            else -> {}
                        }
                    } while (event.changes.any { it.pressed })
                    
                    // 手势结束后的处理
                    if (!isGestureConsumed) {
                        val currentTime = System.currentTimeMillis()
                        val tapPosition = firstDown.position
                        
                        // 检测双击（300ms 内，位置相近）
                        if (currentTime - lastTapTime < 300 && 
                            abs(tapPosition.x - lastTapPosition.x) < 100 &&
                            abs(tapPosition.y - lastTapPosition.y) < 100) {
                            
                            // 双击处理
                            if (currentScale > 1.001f) {
                                // 已放大，恢复原始大小
                                currentScale = 1f
                                currentOffset = Offset.Zero
                            } else {
                                // 未放大，放大到 3 倍
                                currentScale = 3f
                                currentOffset = Offset(
                                    x = (size.width / 2 - tapPosition.x) * 2f,
                                    y = (size.height / 2 - tapPosition.y) * 2f
                                )
                            }
                            onPageStateChange(currentScale, currentOffset)
                            
                            // 重置双击检测状态
                            lastTapTime = 0L
                            lastTapPosition = Offset.Zero
                        } else {
                            // 第一次点击
                            if (currentScale <= 1.001f) {
                                // 未放大状态下的单击
                                onTap()
                            }
                            lastTapTime = currentTime
                            lastTapPosition = tapPosition
                        }
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = imageUrl,
            contentDescription = photo.filename,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(
                    scaleX = currentScale,
                    scaleY = currentScale,
                    translationX = currentOffset.x,
                    translationY = currentOffset.y
                ),
            contentScale = ContentScale.Fit
        )
        
        if (photo.isVideo) {
            Surface(
                modifier = Modifier.align(Alignment.Center),
                color = Color.Black.copy(alpha = 0.5f),
                shape = MaterialTheme.shapes.large
            ) {
                Icon(
                    imageVector = Icons.Default.PlayCircleFilled,
                    contentDescription = stringResource(R.string.photos_video),
                    modifier = Modifier.size(64.dp),
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
private fun BottomPhotoInfo(
    photo: PhotoItem?,
    currentIndex: Int,
    totalCount: Int
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.Black.copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$currentIndex / $totalCount",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White
            )
            
            if (photo != null) {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        text = formatSize(photo.filesize),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                    
                    photo.additional?.resolution?.let { res ->
                        Text(
                            text = "${res.width} × ${res.height}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                    
                    if (photo.time > 0) {
                        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        Text(
                            text = dateFormat.format(Date(photo.time * 1000)),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PhotoInfoDialog(
    photo: PhotoItem,
    onDismiss: () -> Unit
) {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.photos_detail_title)) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DetailRow(stringResource(R.string.photos_filename), photo.filename)
                DetailRow(stringResource(R.string.photos_filesize), formatSize(photo.filesize))

                photo.additional?.resolution?.let { res ->
                    DetailRow(stringResource(R.string.photos_resolution), stringResource(R.string.photos_resolution_format, res.width, res.height))
                }

                if (photo.time > 0) {
                    DetailRow(stringResource(R.string.photos_taken_time), dateFormat.format(Date(photo.time * 1000)))
                }

                DetailRow(stringResource(R.string.photos_type), if (photo.isVideo) stringResource(R.string.photos_type_video) else stringResource(R.string.photos_type_photo))

                photo.additional?.videoMeta?.let { meta ->
                    if (meta.duration > 0) {
                        val minutes = meta.duration / 60
                        val seconds = meta.duration % 60
                        DetailRow(stringResource(R.string.photos_duration), stringResource(R.string.photos_duration_format, minutes, seconds))
                    }
                }

                photo.additional?.address?.let { addr ->
                    if (addr.city.isNotEmpty() || addr.country.isNotEmpty()) {
                        val location = listOfNotNull(
                            addr.country,
                            addr.state,
                            addr.city,
                            addr.district
                        ).filter { it.isNotEmpty() }.joinToString(", ")
                        if (location.isNotEmpty()) {
                            DetailRow(stringResource(R.string.photos_location), location)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.photos_close))
            }
        }
    )
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}