package wang.zengye.dsm.ui.file

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import wang.zengye.dsm.R
import androidx.hilt.navigation.compose.hiltViewModel
import wang.zengye.dsm.ui.components.EmptyState
import wang.zengye.dsm.ui.components.ErrorState
import java.text.SimpleDateFormat
import java.util.*

/**
 * 文件搜索页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileSearchScreen(
    initialPath: String = "/",
    onNavigateBack: () -> Unit = {},
    onFileClick: (String) -> Unit = {},
    viewModel: FileSearchViewModel = hiltViewModel()
) {
    val uiState by viewModel.state.collectAsStateWithLifecycle()
    
    // 收集事件
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is FileSearchEvent.ShowError -> {
                    // 错误已在状态中处理
                }
                is FileSearchEvent.SearchCompleted -> {
                    // 搜索完成
                }
            }
        }
    }
    
    LaunchedEffect(initialPath) {
        viewModel.sendIntent(FileSearchIntent.SetSearchPath(initialPath))
    }
    
    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.file_search_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.common_back))
                    }
                },
                actions = {
                    if (uiState.isSearching) {
                        IconButton(onClick = { viewModel.sendIntent(FileSearchIntent.CancelSearch) }) {
                            Icon(Icons.Default.Close, contentDescription = stringResource(R.string.file_cancel_search))
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 搜索输入区域
            SearchInputSection(
                searchQuery = uiState.searchQuery,
                searchPath = uiState.searchPath,
                isRecursive = uiState.isRecursive,
                searchContent = uiState.searchContent,
                isSearching = uiState.isSearching,
                onQueryChange = { viewModel.sendIntent(FileSearchIntent.SetSearchQuery(it)) },
                onRecursiveChange = { viewModel.sendIntent(FileSearchIntent.SetRecursive(it)) },
                onSearchContentChange = { viewModel.sendIntent(FileSearchIntent.SetSearchContent(it)) },
                onSearch = { viewModel.sendIntent(FileSearchIntent.StartSearch) },
                onCancel = { viewModel.sendIntent(FileSearchIntent.CancelSearch) }
            )
            
            HorizontalDivider()
            
            // 搜索结果区域
            when {
                uiState.isSearching -> {
                    // 搜索中状态
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            progress = { uiState.searchProgress },
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = stringResource(R.string.file_search_progress_percent, ((uiState.searchProgress * 100).toInt())),
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.file_search_found_results, uiState.searchResults.size),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                uiState.error != null -> {
                    ErrorState(
                        message = uiState.error ?: stringResource(R.string.file_search_failed),
                        onRetry = { viewModel.sendIntent(FileSearchIntent.StartSearch) },
                        modifier = Modifier.fillMaxSize()
                    )
                }
                
                uiState.searchResults.isEmpty() && uiState.searchQuery.isEmpty() -> {
                    EmptyState(
                        message = stringResource(R.string.file_search_hint),
                        modifier = Modifier.fillMaxSize()
                    )
                }
                
                uiState.searchResults.isEmpty() -> {
                    EmptyState(
                        message = stringResource(R.string.file_search_no_results),
                        modifier = Modifier.fillMaxSize()
                    )
                }
                
                else -> {
                    // 显示搜索结果
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        item {
                            Text(
                                text = stringResource(R.string.file_search_result_count, uiState.searchResults.size),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                        
                        items(
                            items = uiState.searchResults,
                            key = { it.path }
                        ) { file ->
                            SearchResultItem(
                                file = file,
                                onClick = { onFileClick(file.path) }
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * 搜索输入区域
 */
@Composable
internal fun SearchInputSection(
    searchQuery: String,
    searchPath: String,
    isRecursive: Boolean,
    searchContent: Boolean,
    isSearching: Boolean,
    onQueryChange: (String) -> Unit,
    onRecursiveChange: (Boolean) -> Unit,
    onSearchContentChange: (Boolean) -> Unit,
    onSearch: () -> Unit,
    onCancel: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // 搜索关键字输入框
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onQueryChange,
            label = { Text(stringResource(R.string.file_search_keyword)) },
            placeholder = { Text(stringResource(R.string.file_search_placeholder)) },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = null)
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onQueryChange("") }) {
                        Icon(Icons.Default.Clear, contentDescription = stringResource(R.string.file_search_clear))
                    }
                }
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Search
            ),
            keyboardActions = KeyboardActions(
                onSearch = { onSearch() }
            ),
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // 搜索路径
        OutlinedTextField(
            value = searchPath,
            onValueChange = { },
            label = { Text(stringResource(R.string.file_search_location)) },
            leadingIcon = {
                Icon(Icons.Default.Folder, contentDescription = null)
            },
            enabled = false,
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // 选项行
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 递归搜索
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = isRecursive,
                    onCheckedChange = onRecursiveChange
                )
                Text(
                    text = stringResource(R.string.file_search_include_subdir),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            // 搜索内容
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = searchContent,
                    onCheckedChange = onSearchContentChange
                )
                Text(
                    text = stringResource(R.string.file_search_content),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 搜索按钮
        Button(
            onClick = onSearch,
            enabled = searchQuery.isNotEmpty() && !isSearching,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isSearching) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.file_searching))
            } else {
                Icon(Icons.Default.Search, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.file_start_search))
            }
        }
    }
}

/**
 * 搜索结果项
 */
@Composable
internal fun SearchResultItem(
    file: FileItem,
    onClick: () -> Unit
) {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    
    ListItem(
        headlineContent = {
            Text(
                text = file.name,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        supportingContent = {
            Column {
                Text(
                    text = file.path,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (!file.isDir) {
                        Text(
                            text = formatFileSize(file.size),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = dateFormat.format(Date(file.modified * 1000)),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        leadingContent = {
            Icon(
                imageVector = if (file.isDir) Icons.Default.Folder else Icons.Default.InsertDriveFile,
                contentDescription = null,
                tint = if (file.isDir) 
                    MaterialTheme.colorScheme.primary 
                else 
                    MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        modifier = Modifier.clickable(onClick = onClick)
    )
}

/**
 * 格式化文件大小
 */
internal fun formatFileSize(size: Long): String {
    return when {
        size < 1024 -> "$size B"
        size < 1024 * 1024 -> String.format("%.1f KB", size / 1024.0)
        size < 1024 * 1024 * 1024 -> String.format("%.1f MB", size / (1024.0 * 1024))
        else -> String.format("%.1f GB", size / (1024.0 * 1024 * 1024))
    }
}