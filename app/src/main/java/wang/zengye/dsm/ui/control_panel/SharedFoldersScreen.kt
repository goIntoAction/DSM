package wang.zengye.dsm.ui.control_panel

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import wang.zengye.dsm.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SharedFoldersScreen(
    onNavigateBack: () -> Unit = {},
    onNavigateToAddShareFolder: (String?) -> Unit = {},
    viewModel: SharedFoldersViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // 处理事件
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is SharedFoldersEvent.ShowError -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
                is SharedFoldersEvent.DeleteSuccess -> {
                    Toast.makeText(context, context.getString(R.string.shared_folders_deleted), Toast.LENGTH_SHORT).show()
                }
                is SharedFoldersEvent.CleanRecycleBinSuccess -> {
                    Toast.makeText(context, context.getString(R.string.shared_folders_recycle_bin_started), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // 删除确认对话框
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showRecycleBinDialog by remember { mutableStateOf(false) }
    var selectedShare by remember { mutableStateOf<ShareInfo?>(null) }
    var showShareDetail by remember { mutableStateOf(false) }

    // 删除确认对话框
    if (showDeleteDialog && selectedShare != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false; selectedShare = null },
            title = { Text(stringResource(R.string.shared_folders_delete_title)) },
            text = {
                Column {
                    Text(stringResource(R.string.shared_folders_delete_confirm, selectedShare?.name ?: ""))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.shared_folders_delete_warning),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        selectedShare?.let { share ->
                            viewModel.sendIntent(SharedFoldersIntent.DeleteShare(share.name))
                        }
                        showDeleteDialog = false
                        selectedShare = null
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(stringResource(R.string.common_delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false; selectedShare = null }) {
                    Text(stringResource(R.string.common_cancel))
                }
            }
        )
    }

    // 清空回收站确认对话框
    if (showRecycleBinDialog && selectedShare != null) {
        AlertDialog(
            onDismissRequest = { showRecycleBinDialog = false; selectedShare = null },
            title = { Text(stringResource(R.string.shared_folders_clean_recycle_bin)) },
            text = { Text(stringResource(R.string.shared_folders_clean_recycle_confirm, selectedShare?.name ?: "")) },
            confirmButton = {
                TextButton(
                    onClick = {
                        selectedShare?.let { share ->
                            viewModel.sendIntent(SharedFoldersIntent.CleanRecycleBin(share.name))
                        }
                        showRecycleBinDialog = false
                        selectedShare = null
                    }
                ) {
                    Text(stringResource(R.string.shared_folders_clean))
                }
            },
            dismissButton = {
                TextButton(onClick = { showRecycleBinDialog = false; selectedShare = null }) {
                    Text(stringResource(R.string.common_cancel))
                }
            }
        )
    }

    // 详情底部抽屉
    if (showShareDetail && selectedShare != null) {
        ModalBottomSheet(
            onDismissRequest = { showShareDetail = false; selectedShare = null }
        ) {
            ShareDetailSheet(
                share = selectedShare!!,
                onDelete = {
                    showShareDetail = false
                    showDeleteDialog = true
                },
                onCleanRecycleBin = {
                    showShareDetail = false
                    showRecycleBinDialog = true
                }
            )
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.shared_folders_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.common_back))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = Color.Transparent
                ),
                actions = {
                    IconButton(onClick = { viewModel.sendIntent(SharedFoldersIntent.LoadShares) }) {
                        Icon(Icons.Default.Refresh, contentDescription = stringResource(R.string.dashboard_refresh))
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onNavigateToAddShareFolder(null) }
            ) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.share_add_title))
            }
        }
    ) { paddingValues ->
        when {
            state.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            state.error != null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = state.error ?: stringResource(R.string.components_load_failed),
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { viewModel.sendIntent(SharedFoldersIntent.LoadShares) }) {
                        Text(stringResource(R.string.common_retry))
                    }
                }
            }

            state.shares.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text(stringResource(R.string.shared_folders_empty))
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(state.shares, key = { it.name }) { share ->
                        ShareListItem(
                            share = share,
                            onClick = {
                                selectedShare = share
                                showShareDetail = true
                            }
                        )
                    }
                }
            }
        }
    }
}
