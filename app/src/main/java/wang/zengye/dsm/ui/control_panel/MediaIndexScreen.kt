package wang.zengye.dsm.ui.control_panel

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import wang.zengye.dsm.R
import wang.zengye.dsm.ui.components.ErrorState
import wang.zengye.dsm.ui.components.LoadingState

/**
 * 媒体索引设置页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaIndexScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: MediaIndexViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // 处理事件
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is MediaIndexEvent.ShowError -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
                is MediaIndexEvent.ReindexSuccess -> {
                    Toast.makeText(context, context.getString(R.string.media_index_rebuild), Toast.LENGTH_SHORT).show()
                }
                is MediaIndexEvent.SaveSuccess -> {
                    Toast.makeText(context, context.getString(R.string.common_save_success), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // 重建索引确认对话框
    if (state.showReindexDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.sendIntent(MediaIndexIntent.HideReindexDialog) },
            title = { Text(stringResource(R.string.media_index_rebuild_title)) },
            text = { Text(stringResource(R.string.media_index_rebuild_message)) },
            confirmButton = {
                TextButton(onClick = { viewModel.sendIntent(MediaIndexIntent.StartReindex) }) {
                    Text(stringResource(R.string.media_index_start_rebuild))
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.sendIntent(MediaIndexIntent.HideReindexDialog) }) {
                    Text(stringResource(R.string.common_cancel))
                }
            }
        )
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.media_index_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.common_back))
                    }
                },
                actions = {
                    if (state.isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        IconButton(onClick = { viewModel.sendIntent(MediaIndexIntent.SaveSettings) }) {
                            Icon(Icons.Default.Save, contentDescription = stringResource(R.string.media_index_save))
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        when {
            state.isLoading -> {
                LoadingState(
                    message = stringResource(R.string.media_index_loading),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
            }

            state.error != null -> {
                ErrorState(
                    message = state.error ?: stringResource(R.string.common_load_failed),
                    onRetry = { viewModel.sendIntent(MediaIndexIntent.LoadMediaIndexStatus) },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 索引状态卡片
                    item {
                        IndexStatusCard(
                            status = state.status,
                            onReindex = { viewModel.sendIntent(MediaIndexIntent.ShowReindexDialog) }
                        )
                    }

                    // 媒体类型设置
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = stringResource(R.string.media_index_media_type),
                                    style = MaterialTheme.typography.titleMedium
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                // 视频
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.VideoFile,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(stringResource(R.string.media_index_video_label))
                                    }
                                    Switch(
                                        checked = state.indexVideo,
                                        onCheckedChange = { viewModel.sendIntent(MediaIndexIntent.ToggleIndexVideo) }
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.Image,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.tertiary
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(stringResource(R.string.media_index_photo_label))
                                    }
                                    Switch(
                                        checked = state.indexPhoto,
                                        onCheckedChange = { viewModel.sendIntent(MediaIndexIntent.ToggleIndexPhoto) }
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // 音乐
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.AudioFile,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.secondary
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(stringResource(R.string.media_index_music_label))
                                    }
                                    Switch(
                                        checked = state.indexMusic,
                                        onCheckedChange = { viewModel.sendIntent(MediaIndexIntent.ToggleIndexMusic) }
                                    )
                                }
                            }
                        }
                    }

                    // 自动索引设置
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = stringResource(R.string.media_index_auto_index),
                                        style = MaterialTheme.typography.titleSmall
                                    )
                                    Text(
                                        text = stringResource(R.string.media_index_auto_index_detect_desc),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Switch(
                                    checked = state.autoIndex,
                                    onCheckedChange = { viewModel.sendIntent(MediaIndexIntent.ToggleAutoIndex) }
                                )
                            }
                        }
                    }

                    // 索引文件夹列表
                    if (state.folders.isNotEmpty()) {
                        item {
                            Text(
                                text = stringResource(R.string.media_index_folders),
                                style = MaterialTheme.typography.titleMedium
                            )
                        }

                        items(
                            items = state.folders,
                            key = { it.path }
                        ) { folder ->
                            MediaFolderItem(
                                folder = folder,
                                onToggle = { viewModel.sendIntent(MediaIndexIntent.ToggleFolder(folder.path)) }
                            )
                        }
                    }
                }
            }
        }
    }
}
