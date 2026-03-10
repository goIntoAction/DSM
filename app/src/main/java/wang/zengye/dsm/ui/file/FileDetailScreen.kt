package wang.zengye.dsm.ui.file

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import wang.zengye.dsm.R
import wang.zengye.dsm.ui.components.ErrorState
import wang.zengye.dsm.ui.components.LoadingState
import wang.zengye.dsm.util.formatSize
import wang.zengye.dsm.util.formatDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileDetailScreen(
    filePath: String,
    onNavigateBack: () -> Unit,
    onFileDeleted: () -> Unit = {},
    onNavigateToVideoPlayer: (String, String) -> Unit = { _, _ -> },
    onNavigateToAudioPlayer: (String, String) -> Unit = { _, _ -> },
    onDownloadFile: (String, String) -> Unit = { _, _ -> },
    viewModel: FileDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.state.collectAsStateWithLifecycle()
    var newName by remember { mutableStateOf("") }

    // 收集事件
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is FileDetailEvent.ShowError -> { /* 错误已在状态中处理 */ }
                is FileDetailEvent.ShowMessage -> { /* 消息已在状态中处理 */ }
                is FileDetailEvent.FileDeleted -> {
                    onFileDeleted()
                    onNavigateBack()
                }
                is FileDetailEvent.FileRenamed -> { /* 文件已重命名 */ }
                is FileDetailEvent.StartDownload -> {
                    onDownloadFile(event.path, event.fileName)
                }
            }
        }
    }

    LaunchedEffect(filePath) {
        viewModel.sendIntent(FileDetailIntent.LoadFileDetail(filePath))
    }

    // 显示消息
    LaunchedEffect(uiState.operationMessage) {
        uiState.operationMessage?.let {
            // 可以用 Snackbar 显示
            viewModel.sendIntent(FileDetailIntent.ClearMessage)
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.file_detail)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.common_back))
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.sendIntent(FileDetailIntent.ShowRenameDialog) }) {
                        Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.file_rename))
                    }
                    IconButton(onClick = { viewModel.sendIntent(FileDetailIntent.ShowDeleteDialog) }) {
                        Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.common_delete))
                    }
                },
            )
        }
    ) { paddingValues ->
        when {
            uiState.isLoading -> {
                LoadingState(
                    message = stringResource(R.string.common_loading),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
            }

            uiState.error != null -> {
                ErrorState(
                    message = uiState.error ?: stringResource(R.string.dashboard_load_failed),
                    onRetry = { viewModel.sendIntent(FileDetailIntent.LoadFileDetail(filePath)) },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
            }

            uiState.fileInfo != null -> {
                val file = uiState.fileInfo!!
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 文件图标和名称
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = if (file.isDir) Icons.Default.Folder else Icons.AutoMirrored.Filled.InsertDriveFile,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = file.name,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (file.isDir) stringResource(R.string.file_type_folder) else formatSize(file.size),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // 基本信息
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.file_basic_info),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )

                            DetailItem(stringResource(R.string.file_type), if (file.isDir) stringResource(R.string.file_type_folder) else stringResource(R.string.file_type_file))
                            DetailItem(stringResource(R.string.file_size), formatSize(file.size))

                            file.modifiedTime.takeIf { it > 0 }?.let {
                                DetailItem(stringResource(R.string.file_modified_time), formatDateTime(it))
                            }
                            file.createdTime.takeIf { it > 0 }?.let {
                                DetailItem(stringResource(R.string.file_created_time), formatDateTime(it))
                            }
                            file.accessedTime.takeIf { it > 0 }?.let {
                                DetailItem(stringResource(R.string.file_accessed_time), formatDateTime(it))
                            }

                            if (file.owner.isNotEmpty()) {
                                DetailItem(stringResource(R.string.file_owner), file.owner)
                            }
                        }
                    }

                    // 路径信息
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.file_path_label),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            SelectionContainer {
                                Text(
                                    text = file.path,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }

                    // 权限信息
                    file.permission?.let { perm ->
                        Card(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = stringResource(R.string.file_permission),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )

                                if (perm.posix.isNotEmpty()) {
                                    DetailItem("POSIX", perm.posix)
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    PermissionChip(stringResource(R.string.file_perm_read), perm.read)
                                    PermissionChip(stringResource(R.string.file_perm_write), perm.write)
                                    PermissionChip(stringResource(R.string.file_perm_exec), perm.execute)
                                    PermissionChip(stringResource(R.string.common_delete), perm.delete)
                                }
                            }
                        }
                    }

                    // 操作按钮
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // 收藏按钮
                        OutlinedButton(
                            onClick = { viewModel.sendIntent(FileDetailIntent.ToggleFavorite(file.path, file.name)) },
                            modifier = Modifier.weight(1f),
                            enabled = !uiState.isAddingFavorite
                        ) {
                            Icon(
                                if (uiState.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                                contentDescription = null
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (uiState.isFavorite) stringResource(R.string.file_favorited) else stringResource(R.string.file_favorite))
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (!file.isDir) {
                        // 获取文件扩展名判断类型
                        val ext = file.name.substringAfterLast(".").lowercase()
                        val isVideo = ext in listOf("mp4", "mkv", "avi", "mov", "wmv", "flv", "webm", "ts", "m4v", "rmvb", "rm", "3gp", "f4v", "vob")
                        val isAudio = ext in listOf("mp3", "wav", "flac", "aac", "m4a", "ogg", "wma", "ape", "alac", "opus")

                        // 视频/音频播放按钮
                        if (isVideo || isAudio) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Button(
                                    onClick = {
                                        val url = wang.zengye.dsm.data.api.DsmApiHelper.getDownloadUrl(file.path)
                                        if (isVideo) {
                                            onNavigateToVideoPlayer(url, file.name)
                                        } else {
                                            onNavigateToAudioPlayer(url, file.name)
                                        }
                                    },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        if (isVideo) Icons.Default.VideoFile else Icons.Default.AudioFile,
                                        contentDescription = null
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(if (isVideo) stringResource(R.string.file_play_video) else stringResource(R.string.file_play_audio))
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = { viewModel.sendIntent(FileDetailIntent.DownloadFile) },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Download, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(stringResource(R.string.file_download))
                            }

                            OutlinedButton(
                                onClick = { viewModel.sendIntent(FileDetailIntent.ShowShareDialog) },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Share, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(stringResource(R.string.common_share))
                            }
                        }
                    } else {
                        // 文件夹分享
                        OutlinedButton(
                            onClick = { viewModel.sendIntent(FileDetailIntent.ShowShareDialog) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(R.string.file_create_share_link))
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }

        // 重命名对话框
        if (uiState.showRenameDialog && uiState.fileInfo != null) {
            AlertDialog(
                onDismissRequest = { viewModel.sendIntent(FileDetailIntent.HideRenameDialog) },
                title = { Text(stringResource(R.string.file_rename)) },
                text = {
                    OutlinedTextField(
                        value = newName.ifEmpty { uiState.fileInfo!!.name },
                        onValueChange = { newName = it },
                        label = { Text(stringResource(R.string.file_new_name)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            val name = newName.ifEmpty { uiState.fileInfo!!.name }
                            viewModel.sendIntent(FileDetailIntent.RenameFile(filePath, name))
                        },
                        enabled = !uiState.isRenaming
                    ) {
                        if (uiState.isRenaming) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text(stringResource(R.string.common_confirm))
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { viewModel.sendIntent(FileDetailIntent.HideRenameDialog) },
                        enabled = !uiState.isRenaming
                    ) {
                        Text(stringResource(R.string.common_cancel))
                    }
                }
            )
        }

        // 删除确认对话框
        if (uiState.showDeleteDialog && uiState.fileInfo != null) {
            AlertDialog(
                onDismissRequest = { viewModel.sendIntent(FileDetailIntent.HideDeleteDialog) },
                title = { Text(stringResource(R.string.file_delete_confirm_title)) },
                text = {
                    Text(stringResource(R.string.file_delete_confirm_message, uiState.fileInfo!!.name))
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.sendIntent(FileDetailIntent.DeleteFile(filePath))
                        },
                        enabled = !uiState.isDeleting
                    ) {
                        if (uiState.isDeleting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text(stringResource(R.string.common_delete), color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { viewModel.sendIntent(FileDetailIntent.HideDeleteDialog) },
                        enabled = !uiState.isDeleting
                    ) {
                        Text(stringResource(R.string.common_cancel))
                    }
                }
            )
        }

        // 分享对话框
        if (uiState.showShareDialog && uiState.fileInfo != null) {
            ShareDialog(
                fileName = uiState.fileInfo!!.name,
                shareUrl = uiState.shareUrl,
                isCreating = uiState.isCreatingShare,
                onCreateShare = { viewModel.sendIntent(FileDetailIntent.CreateShareLink(filePath)) },
                onDismiss = { viewModel.sendIntent(FileDetailIntent.HideShareDialog) }
            )
        }
    }
}