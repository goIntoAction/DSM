package wang.zengye.dsm.ui.photos

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import wang.zengye.dsm.R
import wang.zengye.dsm.data.api.DsmApiHelper
import wang.zengye.dsm.data.model.AlbumItem
import wang.zengye.dsm.data.model.PhotoItem
import wang.zengye.dsm.ui.components.*
import wang.zengye.dsm.ui.file.MediaListManager
import wang.zengye.dsm.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotosScreen(
    onNavigateBack: () -> Unit = {},
    onNavigateToAlbum: (Long, String) -> Unit = { _, _ -> },
    onNavigateToPhoto: (Long, List<PhotoItem>) -> Unit = { _, _ -> },
    onNavigateToVideoPlayer: (String, String) -> Unit = { _, _ -> },
    viewModel: PhotosViewModel = hiltViewModel()
) {
    val uiState by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    // 处理 Event
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is PhotosEvent.ShowError -> {
                    snackbarHostState.showSnackbar(event.message)
                }
                is PhotosEvent.UploadComplete -> {
                    val message = context.getString(R.string.photos_upload_complete, event.successCount, event.failCount)
                    snackbarHostState.showSnackbar(message)
                }
                is PhotosEvent.AlbumCreated -> {
                    snackbarHostState.showSnackbar(context.getString(R.string.photos_album_created))
                }
            }
        }
    }

    // 照片选择器
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia()
    ) { uris ->
        if (uris.isNotEmpty()) {
            viewModel.sendIntent(PhotosIntent.UploadPhotos(uris))
        }
    }
    
    // 创建相册对话框
    var showCreateAlbumDialog by remember { mutableStateOf(false) }
    var newAlbumName by remember { mutableStateOf("") }

    val tabs = listOf(
        stringResource(R.string.photos_tab_timeline),
        stringResource(R.string.photos_tab_photos),
        stringResource(R.string.photos_tab_albums),
        stringResource(R.string.photos_tab_places)
    )

    // 使用 remember 缓存回调函数
    val onRefresh = remember(viewModel) {
        { viewModel.sendIntent(PhotosIntent.Refresh) }
    }

    val onSetTab0 = remember(viewModel) {
        { viewModel.sendIntent(PhotosIntent.SetTab(0)) }
    }

    val onSetTab1 = remember(viewModel) {
        { viewModel.sendIntent(PhotosIntent.SetTab(1)) }
    }

    Scaffold(
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.photos_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Medium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.common_back)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = Color.Transparent
                ),
                actions = {
                    // 上传按钮
                    IconButton(
                        onClick = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)
                            )
                        },
                        enabled = !uiState.uploadState.isUploading
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.AddPhotoAlternate,
                            contentDescription = stringResource(R.string.photos_upload)
                        )
                    }
                    // 刷新按钮
                    IconButton(
                        onClick = onRefresh,
                        enabled = !uiState.isRefreshing
                    ) {
                        if (uiState.isRefreshing) {
                            CircularProgressIndicator(
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.primary
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Outlined.Refresh,
                                contentDescription = stringResource(R.string.common_refresh)
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            // 在相册 Tab 显示创建相册按钮
            if (uiState.currentTab == 2) {
                ExtendedFloatingActionButton(
                    onClick = { showCreateAlbumDialog = true },
                    icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                    text = { Text(stringResource(R.string.photos_create_album)) },
                    shape = RoundedCornerShape(CornerRadius.Large)
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Tab 栏 - MD3风格
            SecondaryScrollableTabRow(
                selectedTabIndex = uiState.currentTab,
                containerColor = Color.Transparent,
                edgePadding = Spacing.PageHorizontal
            ) {
                tabs.forEachIndexed { index, title ->
                    val onTabClick = remember(index, viewModel) {
                        { viewModel.sendIntent(PhotosIntent.SetTab(index)) }
                    }
                    Tab(
                        selected = uiState.currentTab == index,
                        onClick = onTabClick,
                        text = {
                            Text(
                                text = title,
                                fontWeight = if (uiState.currentTab == index) FontWeight.Medium else FontWeight.Normal
                            )
                        },
                        selectedContentColor = MaterialTheme.colorScheme.primary,
                        unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            when {
                uiState.isLoading -> {
                    LoadingState(
                        message = stringResource(R.string.photos_loading),
                        modifier = Modifier.fillMaxSize()
                    )
                }

                uiState.error != null -> {
                    ErrorState(
                        message = uiState.error ?: stringResource(R.string.photos_load_failed),
                        onRetry = onRefresh,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                
                else -> {
                    when (uiState.currentTab) {
                        0 -> {
                            TimelineView(
                                groups = uiState.timelineGroups,
                                onGroupClick = { group ->
                                    // 点击时间线组，加载该日期的照片
                                    viewModel.sendIntent(PhotosIntent.LoadPhotosByDate(group.timestamp, group.date))
                                }
                            )
                        }
                        1 -> {
                            // 如果有选中日期，显示日期标题和返回按钮
                            val selectedDate = uiState.selectedDate
                            if (selectedDate != null) {
                                Column {
                                    Surface(
                                        modifier = Modifier.fillMaxWidth(),
                                        color = MaterialTheme.colorScheme.surfaceVariant
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .padding(horizontal = 16.dp, vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            IconButton(onClick = { viewModel.sendIntent(PhotosIntent.ClearSelectedDate) }) {
                                                Icon(
                                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                                    contentDescription = stringResource(R.string.common_back)
                                                )
                                            }
                                            Text(
                                                text = selectedDate,
                                                style = MaterialTheme.typography.titleMedium,
                                                modifier = Modifier.padding(start = 8.dp)
                                            )
                                            Spacer(modifier = Modifier.weight(1f))
                                            Text(
                                                text = stringResource(R.string.photos_timeline_count, uiState.photos.size),
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                    
                                    val onPhotoClick = remember(onNavigateToPhoto, uiState.photos) {
                                        { photoId: Long ->
                                            MediaListManager.setPhotoList(uiState.photos)
                                            onNavigateToPhoto(photoId, uiState.photos)
                                        }
                                    }
                                    PhotosGrid(
                                        photos = uiState.photos,
                                        onPhotoClick = onPhotoClick,
                                        onNavigateToVideoPlayer = onNavigateToVideoPlayer
                                    )
                                }
                            } else {
                                val onPhotoClick = remember(onNavigateToPhoto, uiState.photos) {
                                    { photoId: Long ->
                                        MediaListManager.setPhotoList(uiState.photos)
                                        onNavigateToPhoto(photoId, uiState.photos)
                                    }
                                }
                                PhotosGrid(
                                    photos = uiState.photos,
                                    onPhotoClick = onPhotoClick,
                                    onNavigateToVideoPlayer = onNavigateToVideoPlayer
                                )
                            }
                        }
                        2 -> {
                            val onAlbumClick = remember(onNavigateToAlbum) {
                                { album: AlbumItem ->
                                    onNavigateToAlbum(album.id, album.name)
                                }
                            }
                            AlbumsGrid(
                                albums = uiState.albums,
                                onAlbumClick = onAlbumClick
                            )
                        }
                        3 -> {
                            PlacesView(places = uiState.places)
                        }
                    }
                }
            }
        }
    }
    
    // 上传进度对话框
    if (uiState.uploadState.isUploading) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text(stringResource(R.string.photos_uploading)) },
            text = {
                Column {
                    Text(
                        text = stringResource(
                            R.string.photos_upload_progress,
                            uiState.uploadState.progress,
                            uiState.uploadState.currentFile,
                            uiState.uploadState.totalFiles
                        )
                    )
                    Spacer(modifier = Modifier.height(Spacing.Medium))
                    LinearProgressIndicator(
                        progress = { uiState.uploadState.progress / 100f },
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (uiState.uploadState.fileName.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(Spacing.Small))
                        Text(
                            text = uiState.uploadState.fileName,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.sendIntent(PhotosIntent.CancelUpload) }
                ) {
                    Text(stringResource(R.string.common_cancel))
                }
            }
        )
    }
    
    // 创建相册对话框
    if (showCreateAlbumDialog) {
        AlertDialog(
            onDismissRequest = { 
                showCreateAlbumDialog = false
                newAlbumName = ""
            },
            title = { Text(stringResource(R.string.photos_create_album_title)) },
            text = {
                OutlinedTextField(
                    value = newAlbumName,
                    onValueChange = { newAlbumName = it },
                    label = { Text(stringResource(R.string.photos_create_album_name)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newAlbumName.isNotBlank()) {
                            viewModel.sendIntent(PhotosIntent.CreateAlbum(newAlbumName))
                            showCreateAlbumDialog = false
                            newAlbumName = ""
                        }
                    },
                    enabled = newAlbumName.isNotBlank()
                ) {
                    Text(stringResource(R.string.common_confirm))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showCreateAlbumDialog = false
                        newAlbumName = ""
                    }
                ) {
                    Text(stringResource(R.string.common_cancel))
                }
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun PhotosGrid(
    photos: List<PhotoItem>,
    onPhotoClick: (Long) -> Unit,
    onNavigateToVideoPlayer: (String, String) -> Unit = { _, _ -> }
) {
    if (photos.isEmpty()) {
        EmptyState(
            message = stringResource(R.string.photos_no_photos_simple),
            icon = Icons.Outlined.PhotoLibrary,
            modifier = Modifier.fillMaxSize()
        )
    } else {
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(0.dp),
            horizontalArrangement = Arrangement.spacedBy(1.dp),
            verticalArrangement = Arrangement.spacedBy(1.dp)
        ) {
            items(photos, key = { it.id }) { photo ->
                val photoOnClick = remember(photo, onPhotoClick, onNavigateToVideoPlayer) {
                    {
                        if (photo.isVideo) {
                            // 视频直接播放
                            val url = DsmApiHelper.getPhotoDownloadUrl(photo.id)
                            onNavigateToVideoPlayer(url, photo.filename)
                        } else {
                            // 照片进入预览
                            onPhotoClick(photo.id)
                        }
                    }
                }
                PhotoItem(
                    photo = photo,
                    onClick = photoOnClick
                )
            }
        }
    }
}

@Composable
internal fun PhotoItem(
    photo: PhotoItem,
    onClick: () -> Unit
) {
    // 使用 unitId 和 cacheKey 构建缩略图 URL
    val thumbnail = photo.additional?.thumbnail
    val thumbnailUrl = if (thumbnail != null && thumbnail.unitId > 0 && thumbnail.cacheKey.isNotEmpty()) {
        DsmApiHelper.getPhotoThumbnailUrl(
            unitId = thumbnail.unitId,
            cacheKey = thumbnail.cacheKey,
            size = "sm"
        )
    } else {
        // 兼容：如果没有 thumbnail 信息，使用 photoId
        DsmApiHelper.getPhotoThumbnailUrl(photo.id)
    }
    
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clickable(onClick = onClick)
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        AsyncImage(
            model = thumbnailUrl,
            contentDescription = photo.filename,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // 视频标识 - 左下角半透明角标
        if (photo.isVideo) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.PlayCircle,
                    contentDescription = stringResource(R.string.photos_video),
                    modifier = Modifier.size(20.dp),
                    tint = Color.White.copy(alpha = 0.9f)
                )
            }
        }
    }
}

@Composable
internal fun AlbumsGrid(
    albums: List<AlbumItem>,
    onAlbumClick: (AlbumItem) -> Unit
) {
    if (albums.isEmpty()) {
        EmptyState(
            message = stringResource(R.string.photos_no_albums_simple),
            icon = Icons.Outlined.PhotoAlbum,
            modifier = Modifier.fillMaxSize()
        )
    } else {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(0.dp),
            horizontalArrangement = Arrangement.spacedBy(1.dp),
            verticalArrangement = Arrangement.spacedBy(1.dp)
        ) {
            items(albums, key = { it.id }) { album ->
                val albumOnClick = remember(album, onAlbumClick) {
                    { onAlbumClick(album) }
                }
                AlbumItem(
                    album = album,
                    onClick = albumOnClick
                )
            }
        }
    }
}

@Composable
internal fun AlbumItem(
    album: AlbumItem,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        // 封面图铺满，无圆角、无卡片包裹
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            val thumbnailUrl = if (album.additional?.thumbnail?.cacheKey?.isNotEmpty() == true) {
                DsmApiHelper.getPhotoThumbnailUrl(
                    unitId = album.additional.thumbnail.id,
                    cacheKey = album.additional.thumbnail.cacheKey,
                    size = "m"
                )
            } else {
                DsmApiHelper.getPhotoThumbnailUrl(album.id)
            }

            AsyncImage(
                model = thumbnailUrl,
                contentDescription = album.name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // 共享标识
            if (album.shared) {
                Icon(
                    imageVector = Icons.Filled.Share,
                    contentDescription = stringResource(R.string.photos_shared),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .size(16.dp),
                    tint = Color.White
                )
            }
        }

        // 文字区域
        Column(
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp)
        ) {
            Text(
                text = album.name,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = stringResource(R.string.photos_album_count, album.itemCount),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
internal fun TimelineView(
    groups: List<TimelineGroup>,
    onGroupClick: (TimelineGroup) -> Unit
) {
    if (groups.isEmpty()) {
        EmptyState(
            message = stringResource(R.string.photos_no_timeline),
            icon = Icons.Outlined.Timeline,
            modifier = Modifier.fillMaxSize()
        )
    } else {
        LazyVerticalGrid(
            columns = GridCells.Fixed(1),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(Spacing.Standard),
            verticalArrangement = Arrangement.spacedBy(Spacing.MediumSmall)
        ) {
            items(groups, key = { it.timestamp }) { group ->
                TimelineItem(group = group, onClick = { onGroupClick(group) })
            }
        }
    }
}

@Composable
private fun TimelineItem(
    group: TimelineGroup,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.Standard),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(CornerRadius.Medium),
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Filled.CalendarMonth,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(Spacing.Standard))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = group.date,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = stringResource(R.string.photos_timeline_count, group.count),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
internal fun PlacesView(places: List<PlaceInfo>) {
    if (places.isEmpty()) {
        EmptyState(
            message = stringResource(R.string.photos_no_places),
            icon = Icons.Outlined.Place,
            modifier = Modifier.fillMaxSize()
        )
    } else {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 160.dp),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(Spacing.Standard),
            horizontalArrangement = Arrangement.spacedBy(Spacing.MediumSmall),
            verticalArrangement = Arrangement.spacedBy(Spacing.MediumSmall)
        ) {
            items(places, key = { it.id }) { place ->
                PlaceItem(place)
            }
        }
    }
}

@Composable
private fun PlaceItem(place: PlaceInfo) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.Standard),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(CornerRadius.Medium),
                color = MaterialTheme.colorScheme.tertiaryContainer,
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Filled.Place,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(Spacing.Medium))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = place.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = stringResource(R.string.photos_place_count, place.count),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
