package wang.zengye.dsm.ui.control_panel

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.collectLatest
import wang.zengye.dsm.R
import wang.zengye.dsm.ui.components.EmptyState
import wang.zengye.dsm.ui.components.ErrorState
import wang.zengye.dsm.ui.components.LoadingState
import wang.zengye.dsm.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserDetailScreen(
    username: String,
    onNavigateBack: () -> Unit,
    viewModel: UserDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf(stringResource(R.string.common_info), stringResource(R.string.user_groups))
    var showSaveDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    
    // 在 Composable 上下文中获取字符串资源
    val saveSuccessMessage = stringResource(R.string.common_save_success)

    LaunchedEffect(username) {
        viewModel.sendIntent(UserDetailIntent.LoadUserDetail(username))
    }

    // 处理 Event
    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is UserDetailEvent.ShowError -> {
                    snackbarHostState.showSnackbar(event.message)
                }
                is UserDetailEvent.SaveSuccess -> {
                    snackbarHostState.showSnackbar(saveSuccessMessage)
                    showSaveDialog = false
                }
            }
        }
    }

    // 保存确认对话框
    if (showSaveDialog) {
        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            title = { Text(stringResource(R.string.user_save_changes)) },
            text = {
                Column {
                    if (uiState.newPassword.isNotEmpty() || uiState.confirmPassword.isNotEmpty()) {
                        if (uiState.newPassword != uiState.confirmPassword) {
                            Text(
                                text = stringResource(R.string.user_password_mismatch),
                                color = MaterialTheme.colorScheme.error
                            )
                        } else {
                            Text(stringResource(R.string.user_password_will_update))
                        }
                    } else {
                        Text(stringResource(R.string.user_confirm_save))
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if ((uiState.newPassword.isNotEmpty() || uiState.confirmPassword.isNotEmpty()) && uiState.newPassword != uiState.confirmPassword) {
                            return@TextButton
                        }
                        viewModel.sendIntent(UserDetailIntent.Save(username))
                    },
                    enabled = !uiState.isSaving
                ) {
                    if (uiState.isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(stringResource(R.string.common_save))
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showSaveDialog = false }) {
                    Text(stringResource(R.string.common_cancel))
                }
            }
        )
    }

    Scaffold(
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = username,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
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
                    IconButton(
                        onClick = { showSaveDialog = true },
                        enabled = !uiState.isSaving
                    ) {
                        Icon(Icons.Filled.Save, contentDescription = stringResource(R.string.common_save))
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
                    message = uiState.error ?: stringResource(R.string.common_load_failed),
                    onRetry = { viewModel.sendIntent(UserDetailIntent.LoadUserDetail(username)) },
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
                    // Tab 栏
                    TabRow(
                        selectedTabIndex = selectedTabIndex,
                        containerColor = Color.Transparent
                    ) {
                        tabs.forEachIndexed { index, title ->
                            Tab(
                                selected = selectedTabIndex == index,
                                onClick = { selectedTabIndex = index },
                                text = { Text(title) }
                            )
                        }
                    }

                    // Tab 内容
                    when (selectedTabIndex) {
                        0 -> UserInfoTab(
                            uiState = uiState,
                            onNewPasswordChange = { viewModel.sendIntent(UserDetailIntent.UpdateNewPassword(it)) },
                            onConfirmPasswordChange = { viewModel.sendIntent(UserDetailIntent.UpdateConfirmPassword(it)) },
                            onDescriptionChange = { viewModel.sendIntent(UserDetailIntent.UpdateDescription(it)) },
                            onEmailChange = { viewModel.sendIntent(UserDetailIntent.UpdateEmail(it)) },
                            onToggleCannotChangePassword = { viewModel.sendIntent(UserDetailIntent.ToggleCannotChangePassword) },
                            onTogglePasswordNeverExpire = { viewModel.sendIntent(UserDetailIntent.TogglePasswordNeverExpire) }
                        )
                        1 -> UserGroupsTab(
                            uiState = uiState,
                            onToggleGroup = { viewModel.sendIntent(UserDetailIntent.ToggleGroup(it)) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun UserInfoTab(
    uiState: UserDetailUiState,
    onNewPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onToggleCannotChangePassword: () -> Unit,
    onTogglePasswordNeverExpire: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(Spacing.PageHorizontal, Spacing.Medium),
        verticalArrangement = Arrangement.spacedBy(Spacing.Medium)
    ) {
        // 基本信息
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = AppShapes.Card,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.cardSurface
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Spacing.CardPadding)
                ) {
                    Text(
                        text = stringResource(R.string.docker_basic_info),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(Spacing.Small))

                    // 用户名（只读）
                    OutlinedTextField(
                        value = uiState.username,
                        onValueChange = {},
                        label = { Text(stringResource(R.string.user_username)) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = false,
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(Spacing.Medium))

                    // 描述
                    OutlinedTextField(
                        value = uiState.description,
                        onValueChange = onDescriptionChange,
                        label = { Text(stringResource(R.string.user_description)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(Spacing.Medium))

                    // 邮箱
                    OutlinedTextField(
                        value = uiState.email,
                        onValueChange = onEmailChange,
                        label = { Text(stringResource(R.string.user_email)) },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        singleLine = true
                    )
                }
            }
        }

        // 密码修改
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = AppShapes.Card,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.cardSurface
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Spacing.CardPadding)
                ) {
                    Text(
                        text = stringResource(R.string.users_modify_password_section),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(Spacing.Small))

                    var passwordVisible by remember { mutableStateOf(false) }

                    OutlinedTextField(
                        value = uiState.newPassword,
                        onValueChange = onNewPasswordChange,
                        label = { Text(stringResource(R.string.user_new_password)) },
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                    contentDescription = null
                                )
                            }
                        },
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(Spacing.Medium))

                    OutlinedTextField(
                        value = uiState.confirmPassword,
                        onValueChange = onConfirmPasswordChange,
                        label = { Text(stringResource(R.string.user_confirm_password)) },
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        isError = uiState.newPassword.isNotEmpty() && uiState.confirmPassword.isNotEmpty() && uiState.newPassword != uiState.confirmPassword,
                        supportingText = {
                            if (uiState.newPassword.isNotEmpty() && uiState.confirmPassword.isNotEmpty() && uiState.newPassword != uiState.confirmPassword) {
                                Text(stringResource(R.string.user_password_mismatch))
                            }
                        },
                        singleLine = true
                    )
                }
            }
        }

        // 密码策略
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = AppShapes.Card,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.cardSurface
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Spacing.CardPadding)
                ) {
                    Text(
                        text = stringResource(R.string.users_password_policy),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(Spacing.Small))

                    // 不允许修改密码
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = Spacing.Small),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = uiState.cannotChangePassword,
                            onCheckedChange = { onToggleCannotChangePassword() }
                        )
                        Spacer(modifier = Modifier.width(Spacing.Small))
                        Text(stringResource(R.string.user_cannot_change_password))
                    }

                    // 密码永不过期
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = Spacing.Small),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = uiState.passwordNeverExpire,
                            onCheckedChange = { onTogglePasswordNeverExpire() }
                        )
                        Spacer(modifier = Modifier.width(Spacing.Small))
                        Text(stringResource(R.string.user_password_always_valid))
                    }
                }
            }
        }

        // 账户状态
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = AppShapes.Card,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.cardSurface
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Spacing.CardPadding)
                ) {
                    Text(
                        text = stringResource(R.string.users_account_status),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(Spacing.Small))

                    val statusText = if (uiState.expired == "normal") stringResource(R.string.users_status_normal) else stringResource(R.string.users_status_disabled)
                    val statusColor = if (uiState.expired == "normal") {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.error
                    }

                    Surface(
                        color = statusColor.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(CornerRadius.Full)
                    ) {
                        Text(
                            text = statusText,
                            style = MaterialTheme.typography.labelLarge,
                            color = statusColor,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun UserGroupsTab(
    uiState: UserDetailUiState,
    onToggleGroup: (String) -> Unit
) {
    when {
        uiState.groupsLoading -> {
            LoadingState(
                message = stringResource(R.string.common_loading),
                modifier = Modifier.fillMaxSize()
            )
        }

        uiState.groups.isEmpty() -> {
            EmptyState(
                message = stringResource(R.string.user_no_groups),
                modifier = Modifier.fillMaxSize()
            )
        }

        else -> {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(Spacing.PageHorizontal, Spacing.Medium),
                verticalArrangement = Arrangement.spacedBy(Spacing.MediumSmall)
            ) {
                items(uiState.groups, key = { it.name }) { group ->
                    GroupItem(
                        group = group,
                        onToggle = { onToggleGroup(group.name) }
                    )
                }
            }
        }
    }
}

@Composable
private fun GroupItem(
    group: UserGroupInfo,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = AppShapes.Card,
        colors = CardDefaults.cardColors(
            containerColor = if (group.isMember) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            } else {
                MaterialTheme.cardSurface
            }
        ),
        onClick = onToggle
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.CardPadding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = group.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                if (group.description.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = group.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (group.isMember) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
