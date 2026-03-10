package wang.zengye.dsm.ui.file

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import wang.zengye.dsm.R
import androidx.hilt.navigation.compose.hiltViewModel
import wang.zengye.dsm.ui.components.EmptyState
import wang.zengye.dsm.ui.components.ErrorState
import wang.zengye.dsm.ui.components.LoadingState

/**
 * 收藏夹管理页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoriteScreen(
    onNavigateBack: () -> Unit = {},
    onFavoriteClick: (String) -> Unit = {},
    viewModel: FavoriteViewModel = hiltViewModel()
) {
    val uiState by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // 处理事件
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is FavoriteEvent.ShowError -> {
                    snackbarHostState.showSnackbar(event.message)
                }
                is FavoriteEvent.RenameSuccess -> {
                    snackbarHostState.showSnackbar("重命名成功")
                }
                is FavoriteEvent.DeleteSuccess -> {
                    snackbarHostState.showSnackbar("取消收藏成功")
                }
            }
        }
    }

    // 重命名对话框
    if (uiState.showRenameDialog && uiState.selectedItem != null) {
        RenameDialog(
            currentName = uiState.newName,
            onNameChange = { viewModel.sendIntent(FavoriteIntent.UpdateNewName(it)) },
            onConfirm = { viewModel.sendIntent(FavoriteIntent.RenameFavorite) },
            onDismiss = { viewModel.sendIntent(FavoriteIntent.HideDialogs) }
        )
    }

    // 删除确认对话框
    if (uiState.showDeleteDialog && uiState.selectedItem != null) {
        DeleteConfirmDialog(
            itemName = uiState.selectedItem!!.name,
            onConfirm = { viewModel.sendIntent(FavoriteIntent.DeleteFavorite) },
            onDismiss = { viewModel.sendIntent(FavoriteIntent.HideDialogs) }
        )
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.file_favorites_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.common_back))
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.sendIntent(FavoriteIntent.LoadFavorites) }) {
                        Icon(Icons.Default.Refresh, contentDescription = stringResource(R.string.dashboard_refresh))
                    }
                }
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
                    onRetry = { viewModel.sendIntent(FavoriteIntent.LoadFavorites) },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
            }
            
            uiState.favorites.isEmpty() -> {
                EmptyState(
                    message = stringResource(R.string.file_no_favorites),
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
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(
                        items = uiState.favorites,
                        key = { it.path }
                    ) { favorite ->
                        FavoriteListItem(
                            favorite = favorite,
                            onClick = {
                                if (favorite.status == "broken") {
                                    // 文件不存在，不处理
                                } else {
                                    onFavoriteClick(favorite.path)
                                }
                            },
                            onRename = { viewModel.sendIntent(FavoriteIntent.ShowRenameDialog(favorite)) },
                            onDelete = { viewModel.sendIntent(FavoriteIntent.ShowDeleteDialog(favorite)) }
                        )
                    }
                }
            }
        }
    }
}

/**
 * 收藏列表项
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FavoriteListItem(
    favorite: FavoriteItem,
    onClick: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    
    ListItem(
        headlineContent = {
            Text(
                text = favorite.name,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = if (favorite.status == "broken") 
                    MaterialTheme.colorScheme.error 
                else 
                    MaterialTheme.colorScheme.onSurface
            )
        },
        supportingContent = {
            Text(
                text = favorite.path,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        leadingContent = {
            Icon(
                imageVector = if (favorite.status == "broken") 
                    Icons.Default.FolderOff 
                else 
                    Icons.Default.Folder,
                contentDescription = null,
                tint = if (favorite.status == "broken") 
                    MaterialTheme.colorScheme.error 
                else 
                    MaterialTheme.colorScheme.primary
            )
        },
        trailingContent = {
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = stringResource(R.string.file_more_options))
                }
                
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.file_rename)) },
                        onClick = {
                            showMenu = false
                            onRename()
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Edit, contentDescription = null)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.file_cancel_favorite)) },
                        onClick = {
                            showMenu = false
                            onDelete()
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Delete, contentDescription = null)
                        },
                        colors = MenuDefaults.itemColors(
                            textColor = MaterialTheme.colorScheme.error
                        )
                    )
                }
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (favorite.status == "broken") {
                    Modifier
                } else {
                    Modifier.clickable(onClick = onClick)
                }
            )
    )
}

/**
 * 重命名对话框
 */
@Composable
private fun RenameDialog(
    currentName: String,
    onNameChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.file_rename_title)) },
        text = {
            OutlinedTextField(
                value = currentName,
                onValueChange = onNameChange,
                label = { Text(stringResource(R.string.file_new_name_label)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(R.string.common_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.common_cancel))
            }
        }
    )
}

/**
 * 删除确认对话框
 */
@Composable
private fun DeleteConfirmDialog(
    itemName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.file_cancel_favorite)) },
        text = { Text(stringResource(R.string.file_cancel_favorite_confirm, itemName)) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(R.string.common_confirm), color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.common_cancel))
            }
        }
    )
}
