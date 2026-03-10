package wang.zengye.dsm.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import coil.compose.AsyncImage
import coil.request.ImageRequest
import wang.zengye.dsm.R

/**
 * 图片预览组件
 * 支持缩放、滑动、分享等功能
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImagePreviewScreen(
    imageUrls: List<String>,
    initialIndex: Int = 0,
    onNavigateBack: () -> Unit,
    onSaveImage: ((String) -> Unit)? = null,
    onShareImage: ((String) -> Unit)? = null
) {
    var currentPage by remember { mutableIntStateOf(initialIndex) }
    val pagerState = rememberPagerState(
        initialPage = initialIndex,
        pageCount = { imageUrls.size }
    )

    // 缩放状态
    var scale by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    // 工具栏显示状态
    var showTopBar by remember { mutableStateOf(true) }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            if (showTopBar) {
                TopAppBar(
                    title = {
                        Text("${currentPage + 1} / ${imageUrls.size}")
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = stringResource(R.string.components_close)
                            )
                        }
                    },
                    actions = {
                        // 保存图片
                        if (onSaveImage != null) {
                            IconButton(
                                onClick = {
                                    if (imageUrls.isNotEmpty() && currentPage < imageUrls.size) {
                                        onSaveImage(imageUrls[currentPage])
                                    }
                                }
                            ) {
                                Icon(
                                    Icons.Default.SaveAlt,
                                    contentDescription = stringResource(R.string.components_save_image)
                                )
                            }
                        }
                        // 分享
                        if (onShareImage != null) {
                            IconButton(
                                onClick = {
                                    if (imageUrls.isNotEmpty() && currentPage < imageUrls.size) {
                                        onShareImage(imageUrls[currentPage])
                                    }
                                }
                            ) {
                                Icon(
                                    Icons.Default.Share,
                                    contentDescription = stringResource(R.string.components_share)
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Black.copy(alpha = 0.7f),
                        titleContentColor = Color.White,
                        navigationIconContentColor = Color.White,
                        actionIconContentColor = Color.White
                    )
                )
            }
        },
        containerColor = Color.Black
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                val imageUrl = imageUrls[page]

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectTransformGestures { _, pan, zoom, _ ->
                                scale = (scale * zoom).coerceIn(1f, 5f)
                                if (scale > 1f) {
                                    offsetX += pan.x
                                    offsetY += pan.y
                                } else {
                                    offsetX = 0f
                                    offsetY = 0f
                                }
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(imageUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = stringResource(R.string.components_image_desc, page + 1),
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer(
                                scaleX = scale,
                                scaleY = scale,
                                translationX = offsetX,
                                translationY = offsetY
                            ),
                        contentScale = ContentScale.Fit
                    )
                }
            }

            // 页面指示器
            if (imageUrls.size > 1) {
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    repeat(imageUrls.size) { index ->
                        Box(
                            modifier = Modifier
                                .padding(2.dp)
                                .size(8.dp)
                                .background(
                                    if (index == pagerState.currentPage)
                                        Color.White
                                    else
                                        Color.White.copy(alpha = 0.4f),
                                    shape = MaterialTheme.shapes.small
                                )
                        )
                    }
                }
            }

            // 双击放大提示
            if (scale == 1f && imageUrls.isNotEmpty()) {
                Text(
                    text = stringResource(R.string.components_pinch_zoom),
                    color = Color.White.copy(alpha = 0.5f),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 60.dp)
                )
            }
        }
    }
}

/**
 * 缩略图网格组件
 */
@Composable
fun ThumbnailGrid(
    imageUrls: List<String>,
    onImageClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
    columns: Int = 3
) {
    val context = LocalContext.current

    LazyVerticalGrid(
        columns = androidx.compose.foundation.lazy.grid.GridCells.Fixed(columns),
        modifier = modifier,
        contentPadding = PaddingValues(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        itemsIndexed(imageUrls) { index, imageUrl ->

            Card(
                onClick = { onImageClick(index) },
                modifier = Modifier
                    .aspectRatio(1f)
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = stringResource(R.string.components_image_desc, index),
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}
