package wang.zengye.dsm.ui.download

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
import androidx.hilt.navigation.compose.hiltViewModel
import wang.zengye.dsm.R
import wang.zengye.dsm.ui.components.EmptyState
import wang.zengye.dsm.ui.components.ErrorState
import wang.zengye.dsm.ui.components.LoadingState

/**
 * BT文件选择页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BtFileSelectScreen(
    taskId: String,
    onNavigateBack: () -> Unit = {},
    viewModel: BtFileSelectViewModel = hiltViewModel()
) {
    val uiState by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(taskId) {
        viewModel.sendIntent(BtFileSelectIntent.LoadFiles(taskId))
    }

    // 收集 Event 处理副作用
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is BtFileSelectEvent.SaveSuccess -> onNavigateBack()
                is BtFileSelectEvent.Error -> { /* 错误已通过 State 展示 */ }
            }
        }
    }

    val allSelected = uiState.files.isNotEmpty() && uiState.files.all { it.isSelected }
    val someSelected = uiState.files.any { it.isSelected } && !allSelected

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.download_select_files)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.common_back))
                    }
                },
                actions = {
                    // 全选/取消全选按钮
                    IconButton(onClick = { viewModel.sendIntent(BtFileSelectIntent.ToggleSelectAll) }) {
                        Icon(
                            imageVector = when {
                                allSelected -> Icons.Default.CheckBox
                                someSelected -> Icons.Default.IndeterminateCheckBox
                                else -> Icons.Default.CheckBoxOutlineBlank
                            },
                            contentDescription = stringResource(R.string.download_select_all)
                        )
                    }
                }
            )
        },
        bottomBar = {
            // 底部操作栏
            Surface(
                modifier = Modifier.fillMaxWidth(),
                tonalElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    // 统计信息
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = stringResource(R.string.download_selected_count, uiState.selectedCount, uiState.files.size),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "${formatFileSize(uiState.selectedSize)} / ${formatFileSize(uiState.totalSize)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 保存按钮
                    Button(
                        onClick = { viewModel.sendIntent(BtFileSelectIntent.SaveSelection(taskId)) },
                        enabled = uiState.selectedCount > 0 && !uiState.isSaving,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (uiState.isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(R.string.download_saving))
                        } else {
                            Icon(Icons.Default.Check, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(R.string.download_save_selection))
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        when {
            uiState.isLoading -> {
                LoadingState(
                    message = stringResource(R.string.download_loading_files),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
            }

            uiState.error != null && uiState.files.isEmpty() -> {
                ErrorState(
                    message = uiState.error ?: stringResource(R.string.components_load_failed),
                    onRetry = { viewModel.sendIntent(BtFileSelectIntent.LoadFiles(taskId)) },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
            }

            uiState.files.isEmpty() -> {
                EmptyState(
                    message = stringResource(R.string.download_no_selectable_files),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
            }

            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    // 错误提示
                    if (uiState.error != null) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            )
                        ) {
                            Text(
                                text = uiState.error!!,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }

                    // 文件列表
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        items(
                            items = uiState.files,
                            key = { it.index }
                        ) { file ->
                            BtFileItem(
                                file = file,
                                onToggle = { viewModel.sendIntent(BtFileSelectIntent.ToggleFileSelection(file.index)) }
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * BT文件项
 */
@Composable
private fun BtFileItem(
    file: BtFileItem,
    onToggle: () -> Unit
) {
    ListItem(
        headlineContent = {
            Text(
                text = file.fileName,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyLarge,
                color = if (file.isSelected)
                    MaterialTheme.colorScheme.onSurface
                else
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        },
        supportingContent = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = formatFileSize(file.size),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (file.priority != "normal") {
                    AssistChip(
                        onClick = {},
                        label = {
                            Text(
                                when (file.priority) {
                                    "high" -> stringResource(R.string.download_priority_high)
                                    "low" -> stringResource(R.string.download_priority_low)
                                    else -> file.priority
                                },
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        modifier = Modifier.height(24.dp)
                    )
                }
            }
        },
        leadingContent = {
            Checkbox(
                checked = file.isSelected,
                onCheckedChange = { onToggle() }
            )
        },
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (file.isSelected) {
                    Modifier
                } else {
                    Modifier.clickable(onClick = onToggle)
                }
            )
    )
}

/**
 * 格式化文件大小
 */
private fun formatFileSize(size: Long): String {
    return when {
        size < 1024 -> "$size B"
        size < 1024 * 1024 -> String.format("%.1f KB", size / 1024.0)
        size < 1024 * 1024 * 1024 -> String.format("%.1f MB", size / (1024.0 * 1024))
        else -> String.format("%.1f GB", size / (1024.0 * 1024 * 1024))
    }
}
