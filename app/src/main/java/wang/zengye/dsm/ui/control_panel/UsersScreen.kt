package wang.zengye.dsm.ui.control_panel

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import wang.zengye.dsm.R
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UsersScreen(
    onNavigateBack: () -> Unit = {},
    onNavigateToUserDetail: (String) -> Unit = {},
    viewModel: UsersViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // 创建用户对话框
    var showCreateDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var selectedUser by remember { mutableStateOf<UserInfo?>(null) }

    // 处理 Event
    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is UsersEvent.ShowError -> {
                    snackbarHostState.showSnackbar(event.message)
                }
                is UsersEvent.CreateUserSuccess -> {
                    showCreateDialog = false
                    snackbarHostState.showSnackbar("用户创建成功")
                }
                is UsersEvent.UpdateUserSuccess -> {
                    showEditDialog = false
                    selectedUser = null
                    snackbarHostState.showSnackbar("用户更新成功")
                }
                is UsersEvent.DeleteUserSuccess -> {
                    showDeleteDialog = false
                    selectedUser = null
                    snackbarHostState.showSnackbar("用户删除成功")
                }
            }
        }
    }

    // 创建用户对话框
    if (showCreateDialog) {
        CreateUserDialog(
            onDismiss = { showCreateDialog = false },
            onConfirm = { username, password, description, email ->
                viewModel.sendIntent(
                    UsersIntent.CreateUser(
                        username = username,
                        password = password,
                        description = description,
                        email = email
                    )
                )
            },
            isLoading = uiState.isOperating
        )
    }

    // 编辑用户对话框
    if (showEditDialog && selectedUser != null) {
        EditUserDialog(
            user = selectedUser!!,
            onDismiss = {
                showEditDialog = false
                selectedUser = null
            },
            onConfirm = { username, description, email, newPassword ->
                viewModel.sendIntent(
                    UsersIntent.UpdateUser(
                        username = username,
                        description = description,
                        email = email,
                        newPassword = newPassword.ifEmpty { null }
                    )
                )
            },
            isLoading = uiState.isOperating
        )
    }

    // 删除确认对话框
    if (showDeleteDialog && selectedUser != null) {
        AlertDialog(
            onDismissRequest = {
                showDeleteDialog = false
                selectedUser = null
            },
            title = { Text(stringResource(R.string.users_delete_title)) },
            text = { Text(stringResource(R.string.users_delete_message, selectedUser?.name ?: "")) },
            confirmButton = {
                TextButton(
                    onClick = {
                        selectedUser?.let { user ->
                            viewModel.sendIntent(UsersIntent.DeleteUser(user.name))
                        }
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(stringResource(R.string.common_delete))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        selectedUser = null
                    }
                ) {
                    Text(stringResource(R.string.common_cancel))
                }
            }
        )
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.users_title)) },
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
                    IconButton(onClick = { viewModel.sendIntent(UsersIntent.LoadUsers) }) {
                        Icon(Icons.Default.Refresh, contentDescription = stringResource(R.string.dashboard_refresh))
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true }
            ) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.users_add_user))
            }
        }
    ) { paddingValues ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            uiState.error != null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = uiState.error ?: stringResource(R.string.dashboard_load_failed),
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { viewModel.sendIntent(UsersIntent.LoadUsers) }) {
                        Text(stringResource(R.string.common_retry))
                    }
                }
            }

            uiState.users.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text(stringResource(R.string.users_no_users_display))
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(uiState.users, key = { it.name }) { user ->
                        val onEditUser = remember(user) {
                            {
                                selectedUser = user
                                showEditDialog = true
                            }
                        }
                        val onDeleteUser = remember(user) {
                            {
                                selectedUser = user
                                showDeleteDialog = true
                            }
                        }
                        UserListItem(
                            user = user,
                            onEdit = onEditUser,
                            onDelete = onDeleteUser,
                            onClick = { onNavigateToUserDetail(user.name) }
                        )
                    }
                }
            }
        }
    }
}