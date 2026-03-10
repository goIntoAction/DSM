package wang.zengye.dsm.ui.file

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch
import wang.zengye.dsm.R
import wang.zengye.dsm.data.api.DsmApiHelper
import wang.zengye.dsm.ui.components.*
import wang.zengye.dsm.ui.theme.*
import wang.zengye.dsm.util.formatSize
import java.text.SimpleDateFormat
import java.util.*

// 支持的文件类型定义
object FileTypes {
    val IMAGES = listOf("jpg", "jpeg", "png", "gif", "bmp", "webp", "heic", "heif", "svg", "ico")
    val VIDEOS = listOf("mp4", "mkv", "avi", "mov", "wmv", "flv", "webm", "ts", "m4v", "rmvb", "rm", "3gp", "f4v", "vob")
    val AUDIOS = listOf("mp3", "wav", "flac", "aac", "m4a", "ogg", "wma", "ape", "alac", "opus")
    val PDF = listOf("pdf")
    val TEXT = listOf("txt", "log", "md", "json", "xml", "html", "css", "js", "ts", "py", "java", "kt", "cpp", "c", "h", "swift", "go", "rs", "sh", "yaml", "yml", "ini", "conf", "properties")
    
    fun isImage(ext: String) = ext in IMAGES
    fun isVideo(ext: String) = ext in VIDEOS
    fun isAudio(ext: String) = ext in AUDIOS
    fun isPdf(ext: String) = ext in PDF
    fun isText(ext: String) = ext in TEXT
    fun isSupported(ext: String) = isImage(ext) || isVideo(ext) || isAudio(ext) || isPdf(ext) || isText(ext)
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun FileScreen(
    onNavigateBack: () -> Unit = {},
    onNavigateToDetail: (String) -> Unit = {},
    onNavigateToSearch: (String) -> Unit = {},
    onNavigateToFavorites: () -> Unit = {},
    onNavigateToUpload: (String) -> Unit = {},
    onNavigateToShareManager: () -> Unit = {},
    onNavigateToVideoPlayer: (String, String) -> Unit = { _, _ -> },
    onNavigateToAudioPlayer: (String, String) -> Unit = { _, _ -> },
    onNavigateToImageViewer: (String) -> Unit = { _ -> },
    onNavigateToPdfViewer: (String, String) -> Unit = { _, _ -> },
    onNavigateToTextEditor: (String, String) -> Unit = { _, _ -> },
    onNavigateToDownloadManager: () -> Unit = {},
    onNavigateToSelectFolder: (String, (String) -> Unit) -> Unit = { _, _ -> },
    viewModel: FileViewModel = hiltViewModel(),
    downloadManagerViewModel: DownloadManagerViewModel = hiltViewModel()
) {
    val uiState by viewModel.state.collectAsStateWithLifecycle()
    var showMenu by remember { mutableStateOf(false) }
    var showDownloadDialog by remember { mutableStateOf<Pair<String, String>?>(null) } // (path, name)
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // 批量操作相关状态
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var showCompressDialog by remember { mutableStateOf(false) }
    var compressFileName by remember { mutableStateOf("") }
    var pendingOperation by remember { mutableStateOf<String?>(null) } // "copy" or "move"

    // 处理 Event
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is FileEvent.ShowError -> {
                    snackbarHostState.showSnackbar(event.message)
                }
                is FileEvent.ShowSuccess -> {
                    snackbarHostState.showSnackbar(event.message)
                }
                is FileEvent.OperationSuccess -> {
                    // 操作成功后重新加载文件列表
                    viewModel.sendIntent(FileIntent.LoadFiles(uiState.currentPath))
                }
                is FileEvent.ShareLinkCreated -> {
                    snackbarHostState.showSnackbar("分享链接: ${event.url}")
                }
                // 导航事件
                is FileEvent.NavigateToImageViewer -> onNavigateToImageViewer(event.path)
                is FileEvent.NavigateToVideoPlayer -> onNavigateToVideoPlayer(event.url, event.name)
                is FileEvent.NavigateToAudioPlayer -> onNavigateToAudioPlayer(event.url, event.name)
                is FileEvent.NavigateToPdfViewer -> onNavigateToPdfViewer(event.url, event.name)
                is FileEvent.NavigateToTextEditor -> onNavigateToTextEditor(event.url, event.name)
                is FileEvent.ShowDownloadDialog -> {
                    showDownloadDialog = Pair(event.path, event.name)
                }
            }
        }
    }
    
    // 处理 DownloadManagerViewModel 的事件
    LaunchedEffect(Unit) {
        downloadManagerViewModel.events.collect { event ->
            when (event) {
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
                    // 权限检查通过，跳转到下载管理页面
                    onNavigateToDownloadManager()
                }
                is DownloadManagerEvent.NeedSetDownloadDirectory -> {
                    // 没有设置下载目录，跳转到下载管理页面让用户设置
                    onNavigateToDownloadManager()
                }
                is DownloadManagerEvent.ShowMessage -> {
                    snackbarHostState.showSnackbar(event.message)
                }
                is DownloadManagerEvent.ShowError -> {
                    snackbarHostState.showSnackbar(event.message)
                }
            }
        }
    }
    
    // 长按菜单状态
    var selectedFile by remember { mutableStateOf<FileItem?>(null) }
    var showFileMenu by remember { mutableStateOf(false) }
    
    // 使用 remember 缓存回调函数
    val onNavigateBackClick = remember(uiState.currentPath, onNavigateBack, viewModel) {
        {
            if (uiState.currentPath == "/") {
                onNavigateBack()
            } else {
                viewModel.sendIntent(FileIntent.NavigateUp)
            }
        }
    }

    val onSortClick = remember(viewModel) {
        { viewModel.sendIntent(FileIntent.SortFiles("name")) }
    }

    val onSearchClick = remember(onNavigateToSearch, uiState.currentPath) {
        { onNavigateToSearch(uiState.currentPath) }
    }
    
    val onFavoritesClick = remember(onNavigateToFavorites) {
        {
            showMenu = false
            onNavigateToFavorites()
        }
    }
    
    val onShareManagerClick = remember(onNavigateToShareManager) {
        {
            showMenu = false
            onNavigateToShareManager()
        }
    }
    
    val onUploadClick = remember(onNavigateToUpload, uiState.currentPath) {
        { onNavigateToUpload(uiState.currentPath) }
    }
    
    val onDismissFileMenu = remember {
        {
            showFileMenu = false
            selectedFile = null
        }
    }

    val onDismissDownloadDialog = remember {
        { showDownloadDialog = null }
    }

    val onDownloadConfirm: () -> Unit = {
        val downloadInfo = showDownloadDialog
        showDownloadDialog = null
        if (downloadInfo != null) {
            val (path, name) = downloadInfo
            downloadManagerViewModel.sendIntent(DownloadManagerIntent.StartDownload(path, name))
            // 不再立即跳转，由 ViewModel 检查权限后发送 NavigateToDownloadManager 事件触发跳转
        }
    }
    
    val onFileDetailClick = remember(onNavigateToDetail) {
        { file: FileItem ->
            onNavigateToDetail(file.path)
            showFileMenu = false
            selectedFile = null
        }
    }
    
    val onFileDownloadClick = { file: FileItem ->
        downloadManagerViewModel.sendIntent(DownloadManagerIntent.StartDownload(file.path, file.name))
        showFileMenu = false
        selectedFile = null
        // 不再立即跳转，由 ViewModel 检查权限后发送 NavigateToDownloadManager 事件触发跳转
    }
    
    val onFileOpenClick = remember(onNavigateToImageViewer, onNavigateToVideoPlayer, onNavigateToAudioPlayer, onNavigateToPdfViewer, onNavigateToTextEditor) {
        { file: FileItem ->
            val ext = file.name.substringAfterLast(".").lowercase()
            when {
                FileTypes.isImage(ext) -> {
                    onNavigateToImageViewer(file.path)
                }
                FileTypes.isVideo(ext) -> {
                    onNavigateToVideoPlayer(DsmApiHelper.getDownloadUrl(file.path), file.name)
                }
                FileTypes.isAudio(ext) -> {
                    onNavigateToAudioPlayer(DsmApiHelper.getDownloadUrl(file.path), file.name)
                }
                FileTypes.isPdf(ext) -> {
                    onNavigateToPdfViewer(DsmApiHelper.getDownloadUrl(file.path), file.name)
                }
                FileTypes.isText(ext) -> {
                    onNavigateToTextEditor(DsmApiHelper.getDownloadUrl(file.path), file.name)
                }
            }
            showFileMenu = false
            selectedFile = null
        }
    }
    
    val onShowMenuChange = remember {
        { value: Boolean -> showMenu = value }
    }
    
    val onRetryLoadFiles = remember(viewModel, uiState.currentPath) {
        { viewModel.sendIntent(FileIntent.LoadFiles(uiState.currentPath)) }
    }

    // 拦截系统返回按钮
    BackHandler(enabled = uiState.currentPath != "/") {
        viewModel.sendIntent(FileIntent.NavigateUp)
    }
    
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    
    // 使用 remember 一次遍历完成所有媒体文件分类
    val (imageFiles, videoFiles, audioFiles) = remember(uiState.files) {
        val images = mutableListOf<FileImageItem>()
        val videos = mutableListOf<FileMediaItem>()
        val audios = mutableListOf<FileMediaItem>()

        uiState.files.forEach { file ->
            if (!file.isDir) {
                val ext = file.name.substringAfterLast(".").lowercase()
                when {
                    FileTypes.isImage(ext) -> images.add(FileImageItem(file.path, file.name))
                    FileTypes.isVideo(ext) -> videos.add(FileMediaItem(file.path, file.name))
                    FileTypes.isAudio(ext) -> audios.add(FileMediaItem(file.path, file.name))
                }
            }
        }
        Triple(images.toList(), videos.toList(), audios.toList())
    }
    
    // 更新媒体列表管理器
    LaunchedEffect(imageFiles, videoFiles, audioFiles) {
        MediaListManager.setImageList(imageFiles)
        MediaListManager.setVideoList(videoFiles)
        MediaListManager.setAudioList(audioFiles)
    }
    
    // 下载确认对话框
    if (showDownloadDialog != null) {
        AlertDialog(
            onDismissRequest = onDismissDownloadDialog,
            title = { Text(stringResource(R.string.file_download_file)) },
            text = { Text(stringResource(R.string.file_download_unsupported_preview)) },
            confirmButton = {
                TextButton(onClick = onDownloadConfirm) {
                    Text(stringResource(R.string.file_download))
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissDownloadDialog) {
                    Text(stringResource(R.string.common_cancel))
                }
            },
            shape = RoundedCornerShape(CornerRadius.Large)
        )
    }
    
    // 批量删除确认对话框
    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text(stringResource(R.string.file_delete_confirm_title)) },
            text = { Text(stringResource(R.string.file_delete_confirm_message, uiState.selectedItems.size)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.sendIntent(FileIntent.Delete(uiState.selectedItems.toList()))
                        showDeleteConfirmDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(stringResource(R.string.common_delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text(stringResource(R.string.common_cancel))
                }
            },
            shape = RoundedCornerShape(CornerRadius.Large)
        )
    }
    
    // 压缩对话框
    if (showCompressDialog) {
        AlertDialog(
            onDismissRequest = { showCompressDialog = false },
            title = { Text(stringResource(R.string.file_compress_title)) },
            text = {
                OutlinedTextField(
                    value = compressFileName,
                    onValueChange = { compressFileName = it },
                    label = { Text(stringResource(R.string.file_compress_filename)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (compressFileName.isNotBlank()) {
                            viewModel.sendIntent(
                                FileIntent.Compress(
                                    uiState.selectedItems.toList(),
                                    "${uiState.currentPath}/$compressFileName"
                                )
                            )
                            showCompressDialog = false
                        }
                    }
                ) {
                    Text(stringResource(R.string.common_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showCompressDialog = false }) {
                    Text(stringResource(R.string.common_cancel))
                }
            },
            shape = RoundedCornerShape(CornerRadius.Large)
        )
    }
    
    // 文件长按菜单 - ModalBottomSheet
    if (showFileMenu && selectedFile != null) {
        ModalBottomSheet(
            onDismissRequest = onDismissFileMenu,
            shape = RoundedCornerShape(topStart = CornerRadius.ExtraLarge, topEnd = CornerRadius.ExtraLarge)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = Spacing.Large)
            ) {
                // 文件名标题
                Text(
                    text = selectedFile?.name ?: "",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = Spacing.PageHorizontal, vertical = Spacing.Medium),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                // 详情 - 始终显示
                NavigationDrawerItem(
                    icon = { Icon(Icons.Outlined.Info, contentDescription = null) },
                    label = { Text(stringResource(R.string.common_details)) },
                    selected = false,
                    onClick = { selectedFile?.let { onFileDetailClick(it) } },
                    modifier = Modifier.padding(horizontal = Spacing.Medium)
                )
                
                // 下载 - 始终显示
                NavigationDrawerItem(
                    icon = { Icon(Icons.Outlined.Download, contentDescription = null) },
                    label = { Text(stringResource(R.string.file_download)) },
                    selected = false,
                    onClick = { selectedFile?.let { onFileDownloadClick(it) } },
                    modifier = Modifier.padding(horizontal = Spacing.Medium)
                )
                
                // 打开 - 仅支持的文件类型显示
                selectedFile?.let { file ->
                    if (!file.isDir) {
                        val ext = file.name.substringAfterLast(".").lowercase()
                        if (FileTypes.isSupported(ext)) {
                            NavigationDrawerItem(
                                icon = { Icon(Icons.AutoMirrored.Outlined.OpenInNew, contentDescription = null) },
                                label = { Text(stringResource(R.string.file_open)) },
                                selected = false,
                                onClick = { onFileOpenClick(file) },
                                modifier = Modifier.padding(horizontal = Spacing.Medium)
                            )
                        }
                    }
                }
            }
        }
    }
    
    Scaffold(
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            if (uiState.isSelectionMode) {
                // 选择模式 TopAppBar
                TopAppBar(
                    title = { 
                        Text(
                            text = stringResource(R.string.file_selected_count, uiState.selectedItems.size),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium
                        ) 
                    },
                    navigationIcon = {
                        IconButton(onClick = { viewModel.sendIntent(FileIntent.ExitSelectionMode) }) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = stringResource(R.string.common_cancel)
                            )
                        }
                    },
                    actions = {
                        TextButton(onClick = { viewModel.sendIntent(FileIntent.ToggleSelectAll) }) {
                            Text(
                                text = if (uiState.selectedItems.size == uiState.files.size && uiState.files.isNotEmpty()) {
                                    stringResource(R.string.file_deselect_all)
                                } else {
                                    stringResource(R.string.file_select_all)
                                }
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            } else {
                // 正常模式 TopAppBar
                TopAppBar(
                    title = { },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBackClick) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.common_back)
                            )
                        }
                    },
                    actions = {
                        // 多选模式开关
                        IconButton(onClick = { 
                            if (uiState.isSelectionMode) {
                                viewModel.sendIntent(FileIntent.ExitSelectionMode)
                            } else {
                                viewModel.sendIntent(FileIntent.EnterSelectionMode)
                            }
                        }) {
                            Icon(
                                imageVector = if (uiState.isSelectionMode) Icons.Outlined.Close else Icons.Outlined.CheckBoxOutlineBlank,
                                contentDescription = stringResource(R.string.file_multi_select)
                            )
                        }
                        // 搜索
                        IconButton(onClick = onSearchClick) {
                            Icon(
                                imageVector = Icons.Outlined.Search,
                                contentDescription = stringResource(R.string.dashboard_search)
                            )
                        }
                        // 排序
                        IconButton(onClick = onSortClick) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Sort,
                                contentDescription = stringResource(R.string.file_sort)
                            )
                        }
                        // 刷新
                        IconButton(onClick = onRetryLoadFiles) {
                            Icon(
                                imageVector = Icons.Outlined.Refresh,
                                contentDescription = stringResource(R.string.common_refresh)
                            )
                        }
                        // 更多菜单
                        IconButton(onClick = { showMenu = true }) {
                            Icon(
                                imageVector = Icons.Outlined.MoreVert,
                                contentDescription = stringResource(R.string.file_more)
                            )
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false },
                            shape = RoundedCornerShape(CornerRadius.Medium)
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.file_favorites)) },
                                onClick = onFavoritesClick,
                                leadingIcon = { 
                                    Icon(
                                        imageVector = Icons.Outlined.Star,
                                        contentDescription = null
                                    ) 
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.file_share_manager)) },
                                onClick = onShareManagerClick,
                                leadingIcon = { 
                                    Icon(
                                        imageVector = Icons.Outlined.Share,
                                        contentDescription = null
                                    ) 
                                }
                            )
                        }
                    },
                    scrollBehavior = scrollBehavior,
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        scrolledContainerColor = Color.Transparent
                    )
                )
            }
        },
        floatingActionButton = {
            if (!uiState.isSelectionMode) {
                ExtendedFloatingActionButton(
                    onClick = onUploadClick,
                    icon = { 
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = null
                        ) 
                    },
                    text = { Text(stringResource(R.string.file_upload)) },
                    shape = RoundedCornerShape(CornerRadius.Large),
                    elevation = FloatingActionButtonDefaults.elevation(
                        defaultElevation = Elevation.Fab
                    )
                )
            }
        },
        bottomBar = {
            if (uiState.isSelectionMode && uiState.selectedItems.isNotEmpty()) {
                BatchOperationBar(
                    selectedCount = uiState.selectedItems.size,
                    onCopy = {
                        pendingOperation = "copy"
                        onNavigateToSelectFolder(uiState.currentPath) { destPath ->
                            viewModel.sendIntent(FileIntent.Copy(uiState.selectedItems.toList(), destPath))
                            pendingOperation = null
                        }
                    },
                    onMove = {
                        pendingOperation = "move"
                        onNavigateToSelectFolder(uiState.currentPath) { destPath ->
                            viewModel.sendIntent(FileIntent.Move(uiState.selectedItems.toList(), destPath))
                            pendingOperation = null
                        }
                    },
                    onDelete = {
                        showDeleteConfirmDialog = true
                    },
                    onCompress = {
                        compressFileName = "archive.zip"
                        showCompressDialog = true
                    }
                )
            }
        }
    ) { paddingValues ->
        when {
            uiState.isLoading -> {
                LoadingState(
                    message = stringResource(R.string.file_loading_files),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
            }
            
            uiState.error != null -> {
                ErrorState(
                    message = uiState.error ?: stringResource(R.string.dashboard_load_failed),
                    onRetry = onRetryLoadFiles,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
            }
            
            uiState.files.isEmpty() -> {
                EmptyState(
                    message = stringResource(R.string.file_folder_empty),
                    icon = Icons.Outlined.FolderOpen,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
            }
            
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .nestedScroll(scrollBehavior.nestedScrollConnection),
                    contentPadding = PaddingValues(vertical = Spacing.Small)
                ) {
                    // 路径面包屑
                    if (uiState.currentPath != "/") {
                        item {
                            PathBreadcrumb(
                                currentPath = uiState.currentPath,
                                onPathClick = { path -> viewModel.sendIntent(FileIntent.NavigateTo(path)) }
                            )
                        }
                    }
                    
                    items(
                        items = uiState.files,
                        key = { it.path }
                    ) { file ->
                        val isSelected = uiState.selectedItems.contains(file.path)

                        // 简化后的点击回调，将逻辑委托给 ViewModel
                        val fileOnClick = remember(viewModel) {
                            { viewModel.sendIntent(FileIntent.HandleFileClick(file.path, file.name, file.isDir)) }
                        }
                        val fileOnLongClick = remember(file, viewModel) {
                            {
                                // 长按只弹出菜单，不进入选择模式
                                selectedFile = file
                                showFileMenu = true
                            }
                        }
                        FileListItem(
                            fileName = file.name,
                            isDirectory = file.isDir,
                            fileSize = file.size,
                            modifiedTime = file.modified,
                            isSelected = isSelected,
                            isSelectionMode = uiState.isSelectionMode,
                            onClick = fileOnClick,
                            onLongClick = fileOnLongClick
                        )
                    }
                    
                    // 底部间距
                    item {
                        Spacer(modifier = Modifier.height(Spacing.ExtraLarge))
                    }
                }
            }
        }
    }
}

/**
 * 路径面包屑导航
 */
@Composable
private fun PathBreadcrumb(
    currentPath: String,
    onPathClick: (String) -> Unit
) {
    val segments = currentPath.split("/").filter { it.isNotEmpty() }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = Spacing.PageHorizontal, vertical = Spacing.Small),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 根目录
        AssistChip(
            onClick = { onPathClick("/") },
            label = { Text("/", style = MaterialTheme.typography.labelMedium) },
            shape = RoundedCornerShape(CornerRadius.Full),
            modifier = Modifier.height(28.dp)
        )

        segments.forEachIndexed { index, segment ->
            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )

            val path = "/" + segments.take(index + 1).joinToString("/")
            val isLast = index == segments.lastIndex

            AssistChip(
                onClick = { if (!isLast) onPathClick(path) },
                label = {
                    Text(
                        text = segment,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (isLast) FontWeight.Medium else FontWeight.Normal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                shape = RoundedCornerShape(CornerRadius.Full),
                modifier = Modifier.height(28.dp),
                colors = if (isLast) {
                    AssistChipDefaults.assistChipColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        labelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                } else {
                    AssistChipDefaults.assistChipColors()
                }
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun FileListItem(
    fileName: String,
    isDirectory: Boolean,
    fileSize: Long,
    modifiedTime: Long,
    isSelected: Boolean = false,
    isSelectionMode: Boolean = false,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = if (isSelectionMode) null else onLongClick
            ),
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) else Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = Spacing.PageHorizontal,
                    vertical = Spacing.Medium
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 选择模式下显示复选框
            if (isSelectionMode) {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = null,
                    modifier = Modifier.padding(end = Spacing.Small)
                )
            }
            
            // 文件图标
            val ext = fileName.substringAfterLast(".").lowercase()
            val iconColor = when {
                isDirectory -> MaterialTheme.colorScheme.primary
                FileTypes.isImage(ext) -> MaterialTheme.colorScheme.tertiary
                FileTypes.isVideo(ext) -> MaterialTheme.colorScheme.error
                FileTypes.isAudio(ext) -> MaterialTheme.colorScheme.secondary
                FileTypes.isPdf(ext) -> MaterialTheme.colorScheme.error
                FileTypes.isText(ext) -> MaterialTheme.colorScheme.primary
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            }
            
            Surface(
                shape = CircleShape,
                color = iconColor.copy(alpha = 0.12f),
                modifier = Modifier.size(44.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when {
                            isDirectory -> Icons.Filled.Folder
                            else -> getFileIcon(fileName)
                        },
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(Spacing.Standard))
            
            // 文件信息
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = fileName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(2.dp))
                
                Text(
                    text = if (isDirectory) {
                        stringResource(R.string.file_folder)
                    } else {
                        "${formatSize(fileSize)} · ${formatTime(modifiedTime)}"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // 箭头
            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

private fun getFileIcon(fileName: String): ImageVector {
    val ext = fileName.substringAfterLast(".").lowercase()
    return when {
        FileTypes.isImage(ext) -> Icons.Filled.Image
        FileTypes.isVideo(ext) -> Icons.Filled.VideoFile
        FileTypes.isAudio(ext) -> Icons.Filled.AudioFile
        FileTypes.isPdf(ext) -> Icons.Filled.PictureAsPdf
        ext in listOf("doc", "docx") -> Icons.Filled.Description
        ext in listOf("xls", "xlsx") -> Icons.Filled.TableChart
        ext in listOf("ppt", "pptx") -> Icons.Filled.Slideshow
        ext in listOf("zip", "rar", "7z", "tar", "gz") -> Icons.Filled.FolderZip
        ext == "apk" -> Icons.Filled.Android
        FileTypes.isText(ext) -> Icons.Filled.Description
        else -> Icons.AutoMirrored.Filled.InsertDriveFile
    }
}


private fun formatTime(timestamp: Long): String {
    if (timestamp == 0L) return "-"
    val format = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    return format.format(Date(timestamp * 1000))
}

/**
 * 批量操作栏
 */
@Composable
private fun BatchOperationBar(
    selectedCount: Int,
    onCopy: () -> Unit,
    onMove: () -> Unit,
    onDelete: () -> Unit,
    onCompress: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp,
        shadowElevation = 3.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.PageHorizontal, vertical = Spacing.Medium)
                .navigationBarsPadding(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // 复制
            BatchActionButton(
                icon = Icons.Outlined.ContentCopy,
                label = stringResource(R.string.file_copy),
                onClick = onCopy
            )
            // 移动
            BatchActionButton(
                icon = Icons.Outlined.DriveFileMove,
                label = stringResource(R.string.file_move),
                onClick = onMove
            )
            // 压缩
            BatchActionButton(
                icon = Icons.Outlined.FolderZip,
                label = stringResource(R.string.file_compress),
                onClick = onCompress
            )
            // 删除
            BatchActionButton(
                icon = Icons.Outlined.Delete,
                label = stringResource(R.string.common_delete),
                onClick = onDelete,
                tint = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
private fun BatchActionButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    tint: Color = MaterialTheme.colorScheme.primary
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = tint,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = tint
        )
    }
}