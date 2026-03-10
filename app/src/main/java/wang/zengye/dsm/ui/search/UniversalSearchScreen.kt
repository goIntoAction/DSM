package wang.zengye.dsm.ui.search

import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.res.stringResource
import wang.zengye.dsm.R
import wang.zengye.dsm.ui.components.EmptyState
import wang.zengye.dsm.ui.components.LoadingState
import wang.zengye.dsm.util.formatSize
import java.text.SimpleDateFormat
import java.util.*

/**
 * 全局搜索页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UniversalSearchScreen(
    onNavigateBack: () -> Unit = {},
    onNavigateToFile: (String) -> Unit = {},
    onNavigateToPhoto: (Long) -> Unit = {},
    viewModel: UniversalSearchViewModel = hiltViewModel()
) {
    val uiState by viewModel.state.collectAsStateWithLifecycle()

    // 收集事件
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is UniversalSearchEvent.ShowError -> { /* 错误已在状态中处理 */ }
                is UniversalSearchEvent.SearchCompleted -> { /* 搜索完成 */ }
            }
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.search_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.common_back))
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
            // 搜索输入框
            SearchBar(
                query = uiState.query,
                isSearching = uiState.isSearching,
                onQueryChange = { viewModel.sendIntent(UniversalSearchIntent.SetQuery(it)) },
                onClear = { viewModel.sendIntent(UniversalSearchIntent.Clear) },
                modifier = Modifier.padding(16.dp)
            )

            // 搜索范围选择
            SearchScopeChips(
                currentScope = uiState.searchScope,
                onScopeChange = { viewModel.sendIntent(UniversalSearchIntent.SetScope(it)) },
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // 搜索结果
            when {
                uiState.isSearching -> {
                    LoadingState(
                        message = stringResource(R.string.search_searching),
                        modifier = Modifier.fillMaxSize()
                    )
                }

                uiState.error != null -> {
                    EmptyState(
                        message = uiState.error ?: stringResource(R.string.search_failed),
                        modifier = Modifier.fillMaxSize()
                    )
                }

                uiState.query.isEmpty() -> {
                    EmptyState(
                        message = stringResource(R.string.search_hint),
                        modifier = Modifier.fillMaxSize()
                    )
                }

                uiState.results.isEmpty() -> {
                    EmptyState(
                        message = stringResource(R.string.search_no_match),
                        modifier = Modifier.fillMaxSize()
                    )
                }

                else -> {
                    // 结果统计
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = stringResource(R.string.search_result_count, uiState.results.size),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // 结果列表
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        items(uiState.results, key = { it.id }) { item ->
                            SearchResultItem(
                                item = item,
                                onClick = {
                                    when (item.type) {
                                        SearchResultType.FILE -> onNavigateToFile(item.path)
                                        SearchResultType.PHOTO -> onNavigateToPhoto(item.id.toLongOrNull() ?: 0)
                                        else -> {}
                                    }
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
private fun SearchBar(
    query: String,
    isSearching: Boolean,
    onQueryChange: (String) -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier.fillMaxWidth(),
        placeholder = { Text(stringResource(R.string.search_placeholder)) },
        leadingIcon = {
            if (isSearching) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Icon(Icons.Default.Search, contentDescription = stringResource(R.string.search_title))
            }
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = onClear) {
                    Icon(Icons.Default.Clear, contentDescription = stringResource(R.string.file_search_clear))
                }
            }
        },
        singleLine = true
    )
}

@Composable
private fun SearchScopeChips(
    currentScope: SearchScope,
    onScopeChange: (SearchScope) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            selected = currentScope == SearchScope.ALL,
            onClick = { onScopeChange(SearchScope.ALL) },
            label = { Text(stringResource(R.string.search_scope_all)) }
        )
        FilterChip(
            selected = currentScope == SearchScope.FILES,
            onClick = { onScopeChange(SearchScope.FILES) },
            label = { Text(stringResource(R.string.search_scope_files)) }
        )
        FilterChip(
            selected = currentScope == SearchScope.PHOTOS,
            onClick = { onScopeChange(SearchScope.PHOTOS) },
            label = { Text(stringResource(R.string.search_scope_photos)) }
        )
    }
}

@Composable
private fun SearchResultItem(
    item: SearchResultItem,
    onClick: () -> Unit
) {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

    ListItem(
        headlineContent = {
            Text(
                text = item.name,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontWeight = FontWeight.Medium
            )
        },
        supportingContent = {
            Column {
                Text(
                    text = item.path,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (!item.isDir && item.size > 0) {
                        Text(
                            text = formatSize(item.size),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (item.modified > 0) {
                        Text(
                            text = dateFormat.format(Date(item.modified * 1000)),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        },
        leadingContent = {
            Icon(
                imageVector = when {
                    item.isDir -> Icons.Default.Folder
                    item.type == SearchResultType.PHOTO -> Icons.Default.Photo
                    item.type == SearchResultType.ALBUM -> Icons.Default.PhotoAlbum
                    else -> Icons.Default.InsertDriveFile
                },
                contentDescription = null,
                tint = if (item.isDir)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        modifier = Modifier.clickable(onClick = onClick)
    )
}