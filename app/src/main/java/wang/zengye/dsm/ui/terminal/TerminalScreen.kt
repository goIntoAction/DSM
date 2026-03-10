package wang.zengye.dsm.ui.terminal

import android.graphics.Typeface
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.imeAnimationTarget
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import org.connectbot.terminal.Terminal
import wang.zengye.dsm.R
import wang.zengye.dsm.terminal.SshTerminalBridge

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TerminalScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: TerminalViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val typeface = remember {
        try {
            Typeface.createFromAsset(context.assets, "fonts/JetBrainsMono-Regular.ttf")
        } catch (e: Exception) {
            Typeface.MONOSPACE
        }
    }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val state = uiState.connectionState
    val isConnected = state is SshTerminalBridge.State.Connected
    val isConnecting = state is SshTerminalBridge.State.Connecting
    val isReconnecting = uiState.isReconnecting

    // 与 ConnectBot 一致：键盘和焦点状态提升到顶层，不随 when 分支销毁重建
    val termFocusRequester = remember { FocusRequester() }
    var showSoftKeyboard by remember { mutableStateOf(false) }
    // 终端是否曾经显示过（一旦连接过就保持 Terminal 存在）
    var terminalEverShown by remember { mutableStateOf(false) }
    val showTerminal = isConnected || isConnecting || isReconnecting || terminalEverShown

    // 与 ConnectBot 一致：session open 时弹起键盘
    LaunchedEffect(isConnected) {
        if (isConnected) {
            terminalEverShown = true
            showSoftKeyboard = true
        }
    }

    // 与 ConnectBot 一致：通过系统 IME 可见性同步状态
    // 使用 WindowInsets 检测系统 IME 的真实可见性
    val imeInsets = WindowInsets.ime
    val density = LocalDensity.current
    val imeHeight = with(density) { imeInsets.getBottom(density).toDp() }
    val systemImeVisible = imeHeight > 0.dp
    var hasImeBeenVisible by remember { mutableStateOf(false) }

    // 同步系统 IME 状态到我们的状态变量
    // 只在 IME 曾经可见后才同步隐藏，防止初始化时取消键盘
    LaunchedEffect(systemImeVisible) {
        if (systemImeVisible) {
            hasImeBeenVisible = true
        }
        // 当系统 IME 被用户手动收起（如按返回键）时，同步状态
        if (hasImeBeenVisible && !systemImeVisible && showSoftKeyboard) {
            showSoftKeyboard = false
        }
    }

    // 连接对话框
    if (uiState.showConnectionDialog) {
        ConnectionDialog(
            savedCredentials = uiState.savedCredentials,
            onDismiss = { viewModel.hideConnectionDialog() },
            onConnect = { host, port, username, password ->
                viewModel.connect(host, port, username, password)
            }
        )
    }

    // 断开确认对话框
    if (uiState.showDisconnectDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.hideDisconnectDialog() },
            title = { Text(stringResource(R.string.terminal_disconnect)) },
            text = { Text(stringResource(R.string.terminal_confirm_disconnect)) },
            confirmButton = {
                TextButton(onClick = { viewModel.disconnect() }) {
                    Text(
                        stringResource(R.string.terminal_disconnect),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.hideDisconnectDialog() }) {
                    Text(stringResource(R.string.common_cancel))
                }
            }
        )
    }

    Scaffold(
        contentWindowInsets = ScaffoldDefaults.contentWindowInsets
            .union(WindowInsets.imeAnimationTarget),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(stringResource(R.string.terminal_ssh_terminal))
                        if (state is SshTerminalBridge.State.Connected) {
                            Text(
                                text = "${state.username}@${state.host}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.common_back)
                        )
                    }
                },
                actions = {
                    if (isConnected) {
                        // Ctrl+C
                        IconButton(onClick = {
                            viewModel.bridge.sendString("\u0003")
                        }) {
                            Icon(Icons.Default.Cancel, contentDescription = "Ctrl+C")
                        }
                        // 断开
                        IconButton(onClick = { viewModel.showDisconnectDialog() }) {
                            Icon(
                                Icons.Default.LinkOff,
                                contentDescription = stringResource(R.string.terminal_disconnect)
                            )
                        }
                    }
                    // 新建连接
                    IconButton(onClick = { viewModel.showConnectionDialog() }) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = stringResource(R.string.terminal_new_connection)
                        )
                    }
                },
                colors = if (isConnected) {
                    TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                } else {
                    TopAppBarDefaults.topAppBarColors()
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (showTerminal) {
                // 与 ConnectBot 一致：Terminal 始终存在，不随状态切换销毁重建
                Box(modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF1E1E1E))
                ) {
                    Terminal(
                        terminalEmulator = viewModel.bridge.terminalEmulator,
                        modifier = Modifier.fillMaxSize(),
                        typeface = typeface,
                        keyboardEnabled = true,
                        showSoftKeyboard = showSoftKeyboard,
                        focusRequester = termFocusRequester,
                        modifierManager = viewModel.bridge.modifierManager,
                        onTerminalTap = {
                            showSoftKeyboard = true
                        },
                        onImeVisibilityChanged = { visible ->
                            // 仅用于追踪 IME 状态，不在这里修改 showSoftKeyboard
                            // showSoftKeyboard 的同步由 WindowInsets.ime 的 LaunchedEffect 处理
                            if (visible) hasImeBeenVisible = true
                        }
                    )

                    // 连接中 / 重连中 overlay
                    if (isConnecting || isReconnecting) {
                        Surface(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(24.dp),
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                            shape = MaterialTheme.shapes.medium,
                            tonalElevation = 4.dp
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp
                                )
                                Spacer(Modifier.width(12.dp))
                                Text(
                                    if (isReconnecting) stringResource(R.string.terminal_reconnecting)
                                    else stringResource(R.string.terminal_connecting)
                                )
                            }
                        }
                    }
                }
            }

            // 错误状态 overlay
            if (state is SshTerminalBridge.State.Error) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.ErrorOutline,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        state.message,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(Modifier.height(24.dp))
                    Button(onClick = { viewModel.showConnectionDialog() }) {
                        Text(stringResource(R.string.terminal_new_connection))
                    }
                }
            }

            // 未连接初始页面（Idle 且从未连接过）
            if (!showTerminal && state !is SshTerminalBridge.State.Error) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Terminal,
                        contentDescription = null,
                        modifier = Modifier.size(80.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = stringResource(R.string.terminal_ssh_terminal),
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.terminal_connect_to_dsm),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(24.dp))
                    Button(onClick = { viewModel.showConnectionDialog() }) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.terminal_new_connection))
                    }
                }
            }
        }
    }
}

@Composable
private fun ConnectionDialog(
    savedCredentials: SavedCredentials,
    onDismiss: () -> Unit,
    onConnect: (host: String, port: Int, username: String, password: String) -> Unit
) {
    var host by remember { mutableStateOf(savedCredentials.host) }
    var port by remember { mutableStateOf("22") }
    var username by remember { mutableStateOf(savedCredentials.username) }
    var password by remember { mutableStateOf(savedCredentials.password) }
    var showPassword by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.terminal_ssh_connection)) },
        text = {
            Column {
                OutlinedTextField(
                    value = host,
                    onValueChange = { host = it },
                    label = { Text(stringResource(R.string.terminal_host_address)) },
                    placeholder = { Text(stringResource(R.string.terminal_host_placeholder)) },
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.Dns, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = port,
                    onValueChange = { if (it.all { c -> c.isDigit() }) port = it },
                    label = { Text(stringResource(R.string.terminal_port)) },
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.SettingsEthernet, contentDescription = null) },
                    modifier = Modifier.width(140.dp)
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text(stringResource(R.string.terminal_user)) },
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text(stringResource(R.string.common_password)) },
                    singleLine = true,
                    visualTransformation = if (showPassword) VisualTransformation.None
                    else PasswordVisualTransformation(),
                    leadingIcon = { Icon(Icons.Default.Key, contentDescription = null) },
                    trailingIcon = {
                        IconButton(onClick = { showPassword = !showPassword }) {
                            Icon(
                                if (showPassword) Icons.Default.VisibilityOff
                                else Icons.Default.Visibility,
                                contentDescription = null
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConnect(host, port.toIntOrNull() ?: 22, username, password)
                },
                enabled = host.isNotEmpty() && username.isNotEmpty() && password.isNotEmpty()
            ) {
                Text(stringResource(R.string.terminal_connect))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.common_cancel))
            }
        }
    )
}
