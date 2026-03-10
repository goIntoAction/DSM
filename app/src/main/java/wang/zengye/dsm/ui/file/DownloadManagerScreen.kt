package wang.zengye.dsm.ui.file

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch
import wang.zengye.dsm.R
import wang.zengye.dsm.data.model.AppDownloadStatus
import wang.zengye.dsm.data.model.AppDownloadTask
import wang.zengye.dsm.ui.theme.CornerRadius
import wang.zengye.dsm.ui.theme.Spacing
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadManagerScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: DownloadManagerViewModel = hiltViewModel()
) {
    val uiState by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Debug: log UI state
    LaunchedEffect(uiState) {
        android.util.Log.d("DMScreen", "uiState: tasks=${uiState.tasks.size}, hasPermission=${uiState.hasDirectoryPermission}, isLoading=${uiState.isLoading}, uri=${uiState.currentDirectoryUri}")
    }
    var showClearConfirmDialog by remember { mutableStateOf(false) }
    var showDirectoryDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // 处理事件
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is DownloadManagerEvent.ShowError -> {
                    snackbarHostState.showSnackbar(event.message)
                }
                is DownloadManagerEvent.ShowMessage -> {
                    snackbarHostState.showSnackbar(event.message)
                }
                is DownloadManagerEvent.NeedSetDownloadDirectory -> {
                    // 没有设置下载目录，弹出目录选择器
                    showDirectoryDialog = true
                }
                is DownloadManagerEvent.ShowNotificationPermissionDeniedWarning -> {
                    // 用户拒绝了通知权限，提示可能的风险
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            message = context.getString(R.string.notification_permission_denied_warning),
                            duration = SnackbarDuration.Long
                        )
                    }
                }
                is DownloadManagerEvent.NavigateToDownloadManager -> {
                    // 已在下载管理页面，无需处理
                }
            }
        }
    }

    // SAF 目录选择器
    val directoryPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        uri?.let {
            viewModel.sendIntent(DownloadManagerIntent.SetDownloadDirectory(it))
        }
    }

    // 首次使用提示选择目录（只在确认没有权限时弹框）
    LaunchedEffect(uiState.hasDirectoryPermission, uiState.currentDirectoryUri) {
        // 只在确认没有权限且没有目录时才弹框
        if (!uiState.hasDirectoryPermission && uiState.currentDirectoryUri == null) {
            // 检查是否有任何持久化的 URI 权限
            val hasAnyPermission = context.contentResolver.persistedUriPermissions.isNotEmpty()
            if (!hasAnyPermission) {
                showDirectoryDialog = true
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.download_manager_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.common_back)
                        )
                    }
                },
                actions = {
                    // 选择下载目录按钮
                    IconButton(onClick = { directoryPickerLauncher.launch(null) }) {
                        Icon(
                            imageVector = Icons.Default.Folder,
                            contentDescription = "选择下载目录"
                        )
                    }
                    // 清除已完成按钮
                    if (uiState.tasks.any { it.isDeletable }) {
                        IconButton(onClick = { showClearConfirmDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.DeleteSweep,
                                contentDescription = stringResource(R.string.download_clear_completed)
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        android.util.Log.d("DMScreen", "Scaffold: isLoading=${uiState.isLoading}, hasPermission=${uiState.hasDirectoryPermission}, tasks.isEmpty=${uiState.tasks.isEmpty()}")

        if (uiState.isLoading) {
            android.util.Log.d("DMScreen", "Showing loading")
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (!uiState.hasDirectoryPermission) {
            android.util.Log.d("DMScreen", "Showing no permission prompt")
            // 未设置下载目录
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(Spacing.Medium)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.FolderOff,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    Text(
                        text = "请先设置下载目录",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Button(
                        onClick = { directoryPickerLauncher.launch(null) }
                    ) {
                        Icon(Icons.Default.Folder, null)
                        Spacer(Modifier.width(Spacing.Small))
                        Text("选择下载目录")
                    }
                }
            }
        } else if (uiState.tasks.isEmpty()) {
            android.util.Log.d("DMScreen", "Showing empty state")
            // 空状态
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(Spacing.Medium)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.CloudDownload,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    Text(
                        text = stringResource(R.string.download_empty),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            android.util.Log.d("DMScreen", "Showing task list: ${uiState.tasks.size} tasks")
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(Spacing.PageHorizontal, Spacing.Medium),
                verticalArrangement = Arrangement.spacedBy(Spacing.Medium)
            ) {
                // 显示当前下载目录
                item {
                    DirectoryInfoCard(
                        directoryUri = uiState.currentDirectoryUri,
                        onChangeDirectory = { directoryPickerLauncher.launch(null) }
                    )
                }

                // 下载任务列表
                items(
                    items = uiState.tasks,
                    key = { it.id }
                ) { task ->
                    AppDownloadTaskItem(
                        task = task,
                        onDelete = { viewModel.sendIntent(DownloadManagerIntent.DeleteTask(task)) },
                        onOpenFile = {
                            viewModel.getFileUri(task)?.let { uri ->
                                val intent = Intent(Intent.ACTION_VIEW).apply {
                                    setDataAndType(uri, context.contentResolver.getType(uri))
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                context.startActivity(Intent.createChooser(intent, "打开文件"))
                            }
                        },
                        onRetry = { viewModel.sendIntent(DownloadManagerIntent.RetryDownload(task)) },
                        onCancel = { viewModel.sendIntent(DownloadManagerIntent.CancelDownload(task.id)) }
                    )
                }
            }
        }
    }

    // 清除确认对话框
    if (showClearConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showClearConfirmDialog = false },
            title = { Text(stringResource(R.string.download_clear_completed)) },
            text = { Text(stringResource(R.string.download_clear_confirm_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.sendIntent(DownloadManagerIntent.ClearCompletedTasks)
                        showClearConfirmDialog = false
                    }
                ) {
                    Text(stringResource(R.string.common_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirmDialog = false }) {
                    Text(stringResource(R.string.common_cancel))
                }
            }
        )
    }

    // 首次使用目录设置对话框
    if (showDirectoryDialog) {
        AlertDialog(
            onDismissRequest = { showDirectoryDialog = false },
            title = { Text("设置下载目录") },
            text = {
                Text("为了使用文件下载功能，请选择一个用于保存下载文件的目录。")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDirectoryDialog = false
                        directoryPickerLauncher.launch(null)
                    }
                ) {
                    Text("选择目录")
                }
            }
        )
    }
}

@Composable
private fun DirectoryInfoCard(
    directoryUri: String?,
    onChangeDirectory: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(CornerRadius.Medium),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.Medium),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.Small)
            ) {
                Icon(
                    imageVector = Icons.Default.Folder,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Column {
                    Text(
                        text = "下载目录",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                    )
                    Text(
                        text = directoryUri?.let { Uri.parse(it).lastPathSegment } ?: "未设置",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            TextButton(onClick = onChangeDirectory) {
                Text("更改", style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

@Composable
private fun AppDownloadTaskItem(
    task: AppDownloadTask,
    onDelete: () -> Unit,
    onOpenFile: () -> Unit,
    onRetry: () -> Unit,
    onCancel: () -> Unit
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }
    val animatedProgress by animateFloatAsState(
        targetValue = task.progress / 100f,
        label = "progress"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(CornerRadius.Medium)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.Medium)
        ) {
            // 文件名和状态
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = task.fileName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(Spacing.Small))
                AppDownloadStatusChip(status = task.status)
            }

            Spacer(modifier = Modifier.height(Spacing.Small))

            // 进度条（仅下载中显示）
            if (task.status == AppDownloadStatus.RUNNING || task.status == AppDownloadStatus.PENDING) {
                val progressColor = when {
                    task.progress > 90 -> MaterialTheme.colorScheme.error
                    task.progress > 70 -> MaterialTheme.colorScheme.tertiary
                    else -> MaterialTheme.colorScheme.primary
                }
                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = progressColor,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    strokeCap = StrokeCap.Round,
                    gapSize = 0.dp,
                    drawStopIndicator = {}
                )
                Spacer(modifier = Modifier.height(Spacing.ExtraSmall))
            }

            // 大小信息
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${task.downloadedSizeFormatted} / ${task.totalSizeFormatted}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontFamily = FontFamily.Monospace
                )
                if (task.status == AppDownloadStatus.RUNNING) {
                    Text(
                        text = "${task.progress}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // 时间信息
            val dateFormat = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault())
            Text(
                text = when (task.status) {
                    AppDownloadStatus.COMPLETED -> "${stringResource(R.string.download_completed)}: ${task.finishTime?.let { dateFormat.format(Date(it)) } ?: ""}"
                    AppDownloadStatus.FAILED -> "${stringResource(R.string.download_failed)}: ${task.errorMessage ?: stringResource(R.string.download_unknown_error)}"
                    AppDownloadStatus.CANCELLED -> "已取消"
                    else -> "${stringResource(R.string.download_started)}: ${dateFormat.format(Date(task.createTime))}"
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )

            // 操作按钮
            Spacer(modifier = Modifier.height(Spacing.Small))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                when (task.status) {
                    AppDownloadStatus.COMPLETED -> {
                        TextButton(onClick = onOpenFile) {
                            Icon(
                                imageVector = Icons.Default.OpenInNew,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(Spacing.ExtraSmall))
                            Text(stringResource(R.string.download_open))
                        }
                    }
                    AppDownloadStatus.FAILED -> {
                        TextButton(onClick = onRetry) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(Spacing.ExtraSmall))
                            Text(stringResource(R.string.download_retry))
                        }
                    }
                    AppDownloadStatus.RUNNING, AppDownloadStatus.PENDING -> {
                        TextButton(onClick = onCancel) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(Spacing.ExtraSmall))
                            Text(stringResource(R.string.download_cancel))
                        }
                    }
                    else -> {}
                }

                // 删除按钮（已完成、失败或取消的）
                if (task.isDeletable) {
                    TextButton(onClick = { showDeleteConfirm = true }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(Spacing.ExtraSmall))
                        Text(stringResource(R.string.download_delete))
                    }
                }
            }
        }
    }

    // 删除确认对话框
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text(stringResource(R.string.download_delete)) },
            text = { Text(stringResource(R.string.download_delete_confirm_message, task.fileName)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showDeleteConfirm = false
                    }
                ) {
                    Text(stringResource(R.string.common_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text(stringResource(R.string.common_cancel))
                }
            }
        )
    }
}

@Composable
private fun AppDownloadStatusChip(status: AppDownloadStatus) {
    val (text, containerColor, contentColor) = when (status) {
        AppDownloadStatus.PENDING -> Triple(
            "等待中",
            MaterialTheme.colorScheme.secondaryContainer,
            MaterialTheme.colorScheme.onSecondaryContainer
        )
        AppDownloadStatus.RUNNING -> Triple(
            "下载中",
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.onPrimaryContainer
        )
        AppDownloadStatus.PAUSED -> Triple(
            "已暂停",
            MaterialTheme.colorScheme.tertiaryContainer,
            MaterialTheme.colorScheme.onTertiaryContainer
        )
        AppDownloadStatus.COMPLETED -> Triple(
            stringResource(R.string.file_download_status_successful),
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.onPrimaryContainer
        )
        AppDownloadStatus.FAILED -> Triple(
            stringResource(R.string.file_download_status_failed),
            MaterialTheme.colorScheme.errorContainer,
            MaterialTheme.colorScheme.onErrorContainer
        )
        AppDownloadStatus.CANCELLED -> Triple(
            "已取消",
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    Surface(
        shape = RoundedCornerShape(CornerRadius.Small),
        color = containerColor
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = contentColor,
            modifier = Modifier.padding(horizontal = Spacing.Small, vertical = Spacing.ExtraSmall)
        )
    }
}
