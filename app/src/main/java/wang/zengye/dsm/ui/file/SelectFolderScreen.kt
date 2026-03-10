package wang.zengye.dsm.ui.file

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import wang.zengye.dsm.R
import wang.zengye.dsm.ui.components.*
import wang.zengye.dsm.ui.theme.*
import wang.zengye.dsm.util.formatSize

/**
 * 文件夹选择器页面
 * 用于批量复制/移动操作时选择目标文件夹
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectFolderScreen(
    excludePath: String = "",  // 排除的路径（不能选择自己或子目录）
    onFolderSelected: (String) -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: FileViewModel = hiltViewModel()
) {
    val uiState by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    
    // 如果是从选择模式进入，先退出选择模式
    LaunchedEffect(Unit) {
        if (uiState.isSelectionMode) {
            viewModel.sendIntent(FileIntent.ExitSelectionMode)
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = stringResource(R.string.file_select_folder),
                        style = MaterialTheme.typography.titleMedium,
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
                    containerColor = Color.Transparent
                )
            )
        }
    ) { paddingValues ->
        // 预计算 stringResource 值
        val currentFolderText = stringResource(R.string.file_current_folder)
        val selectFolderText = stringResource(R.string.file_select_folder)
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 当前路径面包屑
            PathBreadcrumb(
                currentPath = uiState.currentPath,
                onPathClick = { path -> viewModel.sendIntent(FileIntent.NavigateTo(path)) },
                modifier = Modifier.padding(horizontal = Spacing.PageHorizontal, vertical = Spacing.Small)
            )
            
            when {
                uiState.isLoading -> {
                    LoadingState(
                        message = stringResource(R.string.file_loading_files),
                        modifier = Modifier.fillMaxSize()
                    )
                }
                
                uiState.error != null -> {
                    ErrorState(
                        message = uiState.error ?: stringResource(R.string.dashboard_load_failed),
                        onRetry = { viewModel.sendIntent(FileIntent.LoadFiles(uiState.currentPath)) },
                        modifier = Modifier.fillMaxSize()
                    )
                }
                
                uiState.files.isEmpty() -> {
                    EmptyState(
                        message = stringResource(R.string.file_folder_empty),
                        icon = Icons.Outlined.Folder,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = Spacing.Small)
                    ) {
                        // 当前文件夹选项（选择当前位置）
                        item {
                            SelectFolderItem(
                                name = currentFolderText,
                                isCurrentLocation = true,
                                isDisabled = uiState.currentPath == excludePath || 
                                    (excludePath.isNotEmpty() && excludePath.startsWith(uiState.currentPath)),
                                onClick = {
                                    onFolderSelected(uiState.currentPath)
                                }
                            )
                        }
                        
                        // 只显示文件夹
                        items(
                            items = uiState.files.filter { it.isDir },
                            key = { it.path }
                        ) { folder ->
                            val isDisabled = folder.path == excludePath || 
                                (excludePath.isNotEmpty() && excludePath.startsWith(folder.path))
                            
                            SelectFolderItem(
                                name = folder.name,
                                isCurrentLocation = false,
                                isDisabled = isDisabled,
                                onClick = {
                                    if (!isDisabled) {
                                        viewModel.sendIntent(FileIntent.NavigateTo(folder.path))
                                    }
                                }
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
}

@Composable
private fun SelectFolderItem(
    name: String,
    isCurrentLocation: Boolean,
    isDisabled: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !isDisabled, onClick = onClick),
        color = if (isCurrentLocation) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) else Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.PageHorizontal, vertical = Spacing.Medium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 文件夹图标
            Surface(
                shape = CircleShape,
                color = if (isCurrentLocation) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) 
                        else MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                modifier = Modifier.size(44.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isCurrentLocation) Icons.Filled.Folder else Icons.Filled.Folder,
                        contentDescription = null,
                        tint = if (isDisabled) MaterialTheme.colorScheme.onSurfaceVariant 
                               else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(Spacing.Standard))
            
            // 文件夹名称
            Text(
                text = name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (isCurrentLocation) FontWeight.Medium else FontWeight.Normal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
                color = if (isDisabled) MaterialTheme.colorScheme.onSurfaceVariant 
                        else MaterialTheme.colorScheme.onSurface
            )
            
            // 箭头
            if (!isCurrentLocation) {
                Icon(
                    imageVector = Icons.Filled.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.size(20.dp)
                )
            } else {
                // 选择按钮
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun PathBreadcrumb(
    currentPath: String,
    onPathClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val segments = currentPath.split("/").filter { it.isNotEmpty() }
    
    Row(
        modifier = modifier,
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
            
            AssistChip(
                onClick = { onPathClick(path) },
                label = {
                    Text(
                        segment,
                        style = MaterialTheme.typography.labelMedium,
                        maxLines = 1
                    )
                },
                shape = RoundedCornerShape(CornerRadius.Full),
                modifier = Modifier.height(28.dp)
            )
        }
    }
}
