package wang.zengye.dsm.ui.components

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import android.util.LruCache
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import okhttp3.Request
import wang.zengye.dsm.R
import wang.zengye.dsm.data.api.DsmApiHelper
import java.io.File

/**
 * PDF 预览组件
 * 使用 Android 原生 PdfRenderer API，逐页渲染为 Bitmap
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfViewerScreen(
    pdfUrl: String,
    pdfTitle: String = "",
    onBack: () -> Unit,
    onSave: (() -> Unit)? = null
) {
    val context = LocalContext.current
    var totalPages by remember { mutableIntStateOf(0) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    var pdfRenderer by remember { mutableStateOf<PdfRenderer?>(null) }
    var tempFile by remember { mutableStateOf<File?>(null) }

    // Bitmap 缓存 - 最大为可用堆的 1/8，上限 40MB
    val bitmapCache = remember {
        val maxSize = (Runtime.getRuntime().maxMemory() / 8).toInt().coerceAtMost(40 * 1024 * 1024)
        object : LruCache<Int, Bitmap>(maxSize) {
            override fun sizeOf(key: Int, value: Bitmap): Int = value.allocationByteCount
        }
    }

    // PdfRenderer 同一时刻只能打开一个 Page
    val renderMutex = remember { Mutex() }

    // 缩放和平移状态
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    val lazyListState = rememberLazyListState()
    val currentPage by remember {
        derivedStateOf { lazyListState.firstVisibleItemIndex + 1 }
    }

    // 使用带认证的 OkHttp 客户端下载 PDF
    LaunchedEffect(pdfUrl) {
        isLoading = true
        errorMessage = null
        withContext(Dispatchers.IO) {
            try {
                val file = File(context.cacheDir, "temp_pdf_${System.currentTimeMillis()}.pdf")
                val request = Request.Builder().url(pdfUrl).build()
                DsmApiHelper.okHttpClient.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        errorMessage = "HTTP ${response.code}"
                        isLoading = false
                        return@withContext
                    }
                    response.body.byteStream().use { input ->
                        file.outputStream().use { output -> input.copyTo(output) }
                    }
                }
                val fd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
                val renderer = PdfRenderer(fd)
                tempFile = file
                pdfRenderer = renderer
                totalPages = renderer.pageCount
                isLoading = false
            } catch (e: Exception) {
                errorMessage = e.message ?: "Unknown error"
                isLoading = false
            }
        }
    }

    // 释放 PdfRenderer 和临时文件
    DisposableEffect(Unit) {
        onDispose {
            pdfRenderer?.close()
            bitmapCache.evictAll()
            tempFile?.delete()
        }
    }

    // 双指缩放和平移手势
    val transformableState = rememberTransformableState { zoomChange, panChange, _ ->
        val newScale = (scale * zoomChange).coerceIn(1f, 5f)
        scale = newScale
        offset = if (newScale <= 1f) {
            Offset.Zero
        } else {
            Offset(x = offset.x + panChange.x, y = offset.y + panChange.y)
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = { Text(pdfTitle.ifEmpty { stringResource(R.string.components_pdf_viewer) }) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.common_back)
                        )
                    }
                },
                actions = {
                    if (totalPages > 0) {
                        Text(
                            text = "$currentPage / $totalPages",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                errorMessage != null -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Filled.Error,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = errorMessage ?: stringResource(R.string.components_load_failed),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
                else -> {
                    val renderer = pdfRenderer
                    if (renderer != null) {
                        LazyColumn(
                            state = lazyListState,
                            modifier = Modifier
                                .fillMaxSize()
                                .transformable(state = transformableState)
                                .graphicsLayer(
                                    scaleX = scale,
                                    scaleY = scale,
                                    translationX = offset.x,
                                    translationY = offset.y
                                ),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(8.dp)
                        ) {
                            items(totalPages) { pageIndex ->
                                PdfPage(
                                    renderer = renderer,
                                    pageIndex = pageIndex,
                                    cache = bitmapCache,
                                    renderMutex = renderMutex
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PdfPage(
    renderer: PdfRenderer,
    pageIndex: Int,
    cache: LruCache<Int, Bitmap>,
    renderMutex: Mutex
) {
    var bitmap by remember(pageIndex) { mutableStateOf(cache.get(pageIndex)) }

    LaunchedEffect(pageIndex) {
        if (bitmap != null) return@LaunchedEffect
        withContext(Dispatchers.IO) {
            try {
                renderMutex.withLock {
                    val page = renderer.openPage(pageIndex)
                    // 以 2 倍渲染，保证高分屏清晰度
                    val renderWidth = page.width * 2
                    val renderHeight = page.height * 2
                    val bmp = Bitmap.createBitmap(renderWidth, renderHeight, Bitmap.Config.ARGB_8888)
                    bmp.eraseColor(android.graphics.Color.WHITE)
                    page.render(bmp, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    page.close()
                    cache.put(pageIndex, bmp)
                    bitmap = bmp
                }
            } catch (_: Exception) {
                // PdfRenderer 可能在页面释放期间已关闭
            }
        }
    }

    if (bitmap != null) {
        Image(
            bitmap = bitmap!!.asImageBitmap(),
            contentDescription = "Page ${pageIndex + 1}",
            modifier = Modifier.fillMaxWidth(),
            contentScale = ContentScale.FillWidth
        )
    } else {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(modifier = Modifier.size(32.dp))
        }
    }
}
