package wang.zengye.dsm.ui.filestation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import wang.zengye.dsm.R
import wang.zengye.dsm.ui.components.EmptyState
import wang.zengye.dsm.ui.components.ErrorState
import wang.zengye.dsm.ui.components.LoadingState

/**
 * 远程文件夹管理页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemoteFolderScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: RemoteFolderViewModel = hiltViewModel()
) {
    val uiState by viewModel.state.collectAsStateWithLifecycle()

    // 断开连接确认对话框
    if (uiState.showDisconnectDialog && uiState.selectedFolder != null) {
        AlertDialog(
            onDismissRequest = { viewModel.sendIntent(RemoteFolderIntent.HideDisconnectDialog) },
            title = { Text(stringResource(R.string.remote_folder_confirm_disconnect)) },
            text = { Text(stringResource(R.string.remote_folder_confirm_disconnect_msg, uiState.selectedFolder!!.name)) },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.sendIntent(RemoteFolderIntent.Disconnect(uiState.selectedFolder!!)) }
                ) {
                    Text(stringResource(R.string.remote_folder_disconnect), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.sendIntent(RemoteFolderIntent.HideDisconnectDialog) }) {
                    Text(stringResource(R.string.common_cancel))
                }
            }
        )
    }

    // 添加远程文件夹对话框
    if (uiState.showAddDialog) {
        AddRemoteFolderDialog(
            server = uiState.newFolderServer,
            path = uiState.newFolderPath,
            username = uiState.newFolderUsername,
            password = uiState.newFolderPassword,
            onServerChange = { viewModel.sendIntent(RemoteFolderIntent.SetNewFolderServer(it)) },
            onPathChange = { viewModel.sendIntent(RemoteFolderIntent.SetNewFolderPath(it)) },
            onUsernameChange = { viewModel.sendIntent(RemoteFolderIntent.SetNewFolderUsername(it)) },
            onPasswordChange = { viewModel.sendIntent(RemoteFolderIntent.SetNewFolderPassword(it)) },
            onDismiss = { viewModel.sendIntent(RemoteFolderIntent.HideAddDialog) },
            onConfirm = { viewModel.sendIntent(RemoteFolderIntent.AddRemoteFolder) }
        )
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.remote_folder_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.common_back))
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.sendIntent(RemoteFolderIntent.ShowAddDialog) }) {
                        Icon(Icons.Default.Add, contentDescription = stringResource(R.string.common_add))
                    }
                    IconButton(onClick = { viewModel.sendIntent(RemoteFolderIntent.LoadRemoteFolders) }) {
                        Icon(Icons.Default.Refresh, contentDescription = stringResource(R.string.common_retry))
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
            // Tab选择
            TabRow(selectedTabIndex = uiState.selectedTab) {
                Tab(
                    selected = uiState.selectedTab == 0,
                    onClick = { viewModel.sendIntent(RemoteFolderIntent.SetTab(0)) },
                    text = { Text("SMB/CIFS") }
                )
                Tab(
                    selected = uiState.selectedTab == 1,
                    onClick = { viewModel.sendIntent(RemoteFolderIntent.SetTab(1)) },
                    text = { Text("NFS") }
                )
            }
            when {
                uiState.isLoading -> {
                    LoadingState(
                        message = stringResource(R.string.remote_folder_loading),
                        modifier = Modifier.fillMaxSize()
                    )
                }

                uiState.error != null && uiState.smbFolders.isEmpty() && uiState.remoteLinks.isEmpty() -> {
                    ErrorState(
                        message = uiState.error ?: stringResource(R.string.remote_folder_load_failed),
                        onRetry = { viewModel.sendIntent(RemoteFolderIntent.LoadRemoteFolders) },
                        modifier = Modifier.fillMaxSize()
                    )
                }

                else -> {
                    val folders = if (uiState.selectedTab == 0) uiState.smbFolders else uiState.remoteLinks

                    if (folders.isEmpty()) {
                        EmptyState(
                            message = if (uiState.selectedTab == 0) stringResource(R.string.remote_folder_no_smb) else stringResource(R.string.remote_folder_no_nfs),
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(
                                items = folders,
                                key = { it.id }
                            ) { folder ->
                                RemoteFolderCard(
                                    folder = folder,
                                    onDisconnect = { viewModel.sendIntent(RemoteFolderIntent.ShowDisconnectDialog(folder)) },
                                    onUnmount = { viewModel.sendIntent(RemoteFolderIntent.UnmountFolder(folder)) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
