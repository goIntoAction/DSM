package wang.zengye.dsm.ui.photos

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import wang.zengye.dsm.R
import wang.zengye.dsm.data.api.*
import wang.zengye.dsm.data.model.PhotoItem
import wang.zengye.dsm.ui.file.MediaListManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlbumDetailScreen(
    albumId: Long,
    albumName: String = "",
    onNavigateBack: () -> Unit,
    onNavigateToPhoto: (Long, List<PhotoItem>) -> Unit = { _, _ -> },
    viewModel: AlbumDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // 处理事件
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is AlbumDetailEvent.ShowError -> {
                    snackbarHostState.showSnackbar(event.message)
                }
                is AlbumDetailEvent.DeleteSuccess -> {
                    onNavigateBack()
                }
            }
        }
    }

    LaunchedEffect(albumId) {
        viewModel.sendIntent(AlbumDetailIntent.LoadAlbumPhotos(albumId))
    }

    // 删除确认对话框
    if (uiState.showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.sendIntent(AlbumDetailIntent.HideDeleteDialog) },
            title = { Text(stringResource(R.string.photos_delete_album_title)) },
            text = { Text(stringResource(R.string.photos_delete_album_confirm, albumName)) },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.sendIntent(AlbumDetailIntent.DeleteAlbum) },
                    enabled = !uiState.isDeleting
                ) {
                    if (uiState.isDeleting) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                    } else {
                        Text(stringResource(R.string.common_delete), color = MaterialTheme.colorScheme.error)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.sendIntent(AlbumDetailIntent.HideDeleteDialog) }) {
                    Text(stringResource(R.string.common_cancel))
                }
            }
        )
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = albumName.ifEmpty { stringResource(R.string.photos_albums) },
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
                    IconButton(onClick = { viewModel.sendIntent(AlbumDetailIntent.LoadAlbumPhotos(albumId)) }) {
                        Icon(Icons.Default.Refresh, contentDescription = stringResource(R.string.common_refresh))
                    }
                    IconButton(onClick = { viewModel.sendIntent(AlbumDetailIntent.ShowDeleteDialog) }) {
                        Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.photos_delete_album))
                    }
                },
            )
        }
    ) { paddingValues ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            uiState.error != null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = uiState.error ?: stringResource(R.string.photos_load_failed),
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { viewModel.sendIntent(AlbumDetailIntent.LoadAlbumPhotos(albumId)) }) {
                        Text(stringResource(R.string.common_retry))
                    }
                }
            }

            uiState.photos.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.PhotoLibrary,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = stringResource(R.string.photos_album_empty),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    // 统计信息
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        tonalElevation = 2.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = stringResource(R.string.photos_album_count, uiState.photos.size),
                                style = MaterialTheme.typography.bodyMedium
                            )

                            val videoCount = uiState.photos.count { it.isVideo }
                            if (videoCount > 0) {
                                Text(
                                    text = stringResource(R.string.photos_video_count, videoCount),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // 照片网格
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 120.dp),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        items(uiState.photos, key = { it.id }) { photo ->
                            AlbumPhotoItem(
                                photo = photo,
                                onClick = {
                                    MediaListManager.setPhotoList(uiState.photos)
                                    onNavigateToPhoto(photo.id, uiState.photos)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AlbumPhotoItem(
    photo: PhotoItem,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(MaterialTheme.shapes.small)
            .clickable(onClick = onClick)
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

        AsyncImage(
            model = thumbnailUrl,
            contentDescription = photo.filename,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // 视频标识
        if (photo.isVideo) {
            Surface(
                modifier = Modifier.align(Alignment.Center),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                shape = MaterialTheme.shapes.small
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = stringResource(R.string.photos_video),
                    modifier = Modifier.padding(4.dp)
                )
            }
        }
    }
}