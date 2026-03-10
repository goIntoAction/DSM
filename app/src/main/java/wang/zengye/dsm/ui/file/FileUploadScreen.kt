package wang.zengye.dsm.ui.file

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import wang.zengye.dsm.R
import wang.zengye.dsm.service.UploadTask
import wang.zengye.dsm.service.UploadStatus
import wang.zengye.dsm.util.formatSize

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileUploadScreen(
    targetPath: String = "/",
    onNavigateBack: () -> Unit,
    onUploadComplete: () -> Unit = {},
    viewModel: UploadViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // 文件选择器
    val filePickerLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        if (uris.isNotEmpty()) {
            viewModel.sendIntent(FileUploadIntent.AddTasks(uris, context))
        }
    }

    LaunchedEffect(targetPath) {
        viewModel.sendIntent(FileUploadIntent.SetTargetPath(targetPath))
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is FileUploadEvent.ShowError -> {
                    snackbarHostState.showSnackbar(event.message)
                }
                is FileUploadEvent.UploadCompleted -> {
                    onUploadComplete()
                }
            }
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.file_upload_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.common_back))
                    }
                },
                actions = {
                    if (uiState.tasks.any { it.status == UploadStatus.COMPLETED }) {
                        IconButton(onClick = { viewModel.sendIntent(FileUploadIntent.ClearCompleted) }) {
                            Icon(Icons.Default.ClearAll, contentDescription = stringResource(R.string.file_clear_completed))
                        }
                    }
                },
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 目标路径
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Folder,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = stringResource(R.string.file_target_path),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = uiState.targetPath,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // 任务列表
            if (uiState.tasks.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudUpload,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = stringResource(R.string.file_select_files),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(uiState.tasks, key = { it.id }) { task ->
                        UploadTaskItem(
                            task = task,
                            isCurrentUploading = uiState.currentUploadingId == task.id,
                            onCancel = { viewModel.sendIntent(FileUploadIntent.CancelTask(task.id, context)) },
                            onRemove = { viewModel.sendIntent(FileUploadIntent.RemoveTask(task.id)) },
                            onRetry = { viewModel.sendIntent(FileUploadIntent.RetryTask(task.id, context)) }
                        )
                    }
                }
            }

            // 底部操作栏
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { filePickerLauncher.launch(arrayOf("*/*")) },
                    modifier = Modifier.weight(1f),
                    enabled = !uiState.isUploading
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.file_add_files))
                }

                Button(
                    onClick = {
                        viewModel.sendIntent(FileUploadIntent.StartUpload(context))
                    },
                    modifier = Modifier.weight(1f),
                    enabled = !uiState.isUploading && uiState.tasks.any { it.status == UploadStatus.PENDING }
                ) {
                    if (uiState.isUploading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.file_uploading))
                    } else {
                        Icon(Icons.Default.CloudUpload, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.file_start_upload))
                    }
                }
            }
        }
    }
}

@Composable
private fun UploadTaskItem(
    task: UploadTask,
    isCurrentUploading: Boolean,
    onCancel: () -> Unit,
    onRemove: () -> Unit,
    onRetry: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 状态图标
                when (task.status) {
                    UploadStatus.PENDING -> {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = stringResource(R.string.file_status_pending),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    UploadStatus.UPLOADING -> {
                        CircularProgressIndicator(
                            progress = { if (task.bytesTotal > 0) task.bytesUploaded.toFloat() / task.bytesTotal else 0f },
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    }
                    UploadStatus.COMPLETED -> {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = stringResource(R.string.file_status_completed),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    UploadStatus.FAILED -> {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = stringResource(R.string.file_status_failed),
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                    UploadStatus.CANCELLED -> {
                        Icon(
                            imageVector = Icons.Default.Cancel,
                            contentDescription = stringResource(R.string.file_status_cancelled),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = task.fileName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = formatSize(task.bytesTotal),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        when (task.status) {
                            UploadStatus.UPLOADING -> {
                                val progress = if (task.bytesTotal > 0)
                                    task.bytesUploaded * 100 / task.bytesTotal else 0
                                Text(
                                    text = "$progress%",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            UploadStatus.COMPLETED -> {
                                Text(
                                    text = stringResource(R.string.file_status_completed),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            UploadStatus.FAILED -> {
                                Text(
                                    text = task.error ?: stringResource(R.string.file_upload_failed),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                            UploadStatus.CANCELLED -> {
                                Text(
                                    text = stringResource(R.string.file_upload_cancelled),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            else -> {}
                        }
                    }
                }

                // 操作按钮
                when (task.status) {
                    UploadStatus.UPLOADING -> {
                        IconButton(onClick = onCancel) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = stringResource(R.string.common_cancel),
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                    UploadStatus.FAILED, UploadStatus.CANCELLED -> {
                        Row {
                            IconButton(onClick = onRetry) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = stringResource(R.string.common_retry),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                            IconButton(onClick = onRemove) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = stringResource(R.string.common_remove),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    UploadStatus.PENDING -> {
                        IconButton(onClick = onRemove) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = stringResource(R.string.common_remove),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    UploadStatus.COMPLETED -> {
                        // 已完成不显示操作按钮
                    }
                }
            }

            // 进度条
            if (task.status == UploadStatus.UPLOADING && task.bytesTotal > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { task.bytesUploaded.toFloat() / task.bytesTotal },
                    modifier = Modifier.fillMaxWidth(),
                    strokeCap = StrokeCap.Butt,
                    gapSize = 0.dp,
                    drawStopIndicator = {})
            }
        }
    }
}
