package wang.zengye.dsm.ui.file

import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import coil.request.ImageRequest
import me.saket.telephoto.zoomable.coil.ZoomableAsyncImage
import me.saket.telephoto.zoomable.rememberZoomableImageState
import me.saket.telephoto.zoomable.rememberZoomableState
import wang.zengye.dsm.R
import wang.zengye.dsm.data.api.DsmApiHelper

/**
 * 文件图片查看器页面
 * 使用 telephoto 库实现缩放、双击放大、左右滑动切换
 * 
 * 交互逻辑：
 * - 未放大时：左右滑动切换上/下一张图片
 * - 放大时：左右滑动调整查看位置
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun FileImageViewerScreen(
    initialPath: String,
    onNavigateBack: () -> Unit,
    onNavigateToDetail: (String) -> Unit = {}
) {
    val imageList = remember { MediaListManager.imageList }
    val initialPage = imageList.indexOfFirst { it.path == initialPath }.coerceAtLeast(0)

    val pagerState = rememberPagerState(
        initialPage = initialPage,
        pageCount = { imageList.size.coerceAtLeast(1) }
    )

    val currentImage = if (imageList.isNotEmpty() && pagerState.currentPage < imageList.size) {
        imageList[pagerState.currentPage]
    } else {
        imageList.find { it.path == initialPath }
    }

    var showControls by remember { mutableStateOf(true) }
    
    // 存储每个页面的缩放状态
    val zoomStates = remember { mutableStateMapOf<Int, me.saket.telephoto.zoomable.ZoomableState>() }
    
    // 判断是否有任何页面处于放大状态
    val isAnyPageZoomed = zoomStates.values.any { state ->
        (state.zoomFraction ?: 0f) > 0.1f
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            key = { if (it < imageList.size) imageList[it].path else it },
            // 当有页面放大时，禁用 pager 的滑动，允许用户在放大区域内拖动
            userScrollEnabled = !isAnyPageZoomed
        ) { page ->
            val image = if (page < imageList.size) imageList[page] else null

            if (image != null) {
                ZoomableImageItem(
                    imagePath = image.path,
                    onClick = { showControls = !showControls },
                    onZoomStateChanged = { zoomState ->
                        zoomStates[page] = zoomState
                    }
                )
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(stringResource(R.string.file_image_load_failed), color = Color.White)
                }
            }
        }

        // 顶部控制栏
        AnimatedVisibility(
            visible = showControls,
            enter = fadeIn() + slideInVertically { -it },
            exit = fadeOut() + slideOutVertically { -it },
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = currentImage?.name ?: stringResource(R.string.file_image_preview),
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (imageList.size > 1) {
                            Text(
                                text = "${pagerState.currentPage + 1} / ${imageList.size}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.common_back)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        currentImage?.let { onNavigateToDetail(it.path) }
                    }) {
                        Icon(
                            imageVector = Icons.Filled.Info,
                            contentDescription = stringResource(R.string.common_details)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black.copy(alpha = 0.5f),
                    navigationIconContentColor = Color.White,
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        }
    }
}

@Composable
private fun ZoomableImageItem(
    imagePath: String,
    onClick: () -> Unit,
    onZoomStateChanged: (me.saket.telephoto.zoomable.ZoomableState) -> Unit
) {
    val context = LocalContext.current
    val imageUrl = DsmApiHelper.getDownloadUrl(imagePath)

    val zoomableState = rememberZoomableState()
    val imageState = rememberZoomableImageState(zoomableState)
    
    // 当页面离开视野时重置缩放
    LaunchedEffect(zoomableState) {
        onZoomStateChanged(zoomableState)
    }

    ZoomableAsyncImage(
        model = ImageRequest.Builder(context)
            .data(imageUrl)
            .crossfade(true)
            .build(),
        contentDescription = null,
        state = imageState,
        modifier = Modifier.fillMaxSize(),
        onClick = { onClick() }
    )
}