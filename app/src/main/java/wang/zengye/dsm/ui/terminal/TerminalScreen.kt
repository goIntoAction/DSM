package wang.zengye.dsm.ui.terminal

import android.graphics.Typeface
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.imeAnimationTarget
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.connectbot.terminal.Terminal
import wang.zengye.dsm.R
import wang.zengye.dsm.terminal.ModifierLevel
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
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF1E1E1E))
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
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

                    // 底部快捷键栏 - 仅在连接成功后显示
                    if (isConnected) {
                        TerminalExtraKeysBar(
                            modifierManager = viewModel.bridge.modifierManager,
                            onSendKey = { key ->
                                viewModel.bridge.sendString(key)
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
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

/**
 * 终端底部快捷键栏 - 可展开面板
 * 默认显示核心按键，展开显示更多按键
 */
@Composable
private fun TerminalExtraKeysBar(
    modifierManager: wang.zengye.dsm.terminal.TerminalModifierManager,
    onSendKey: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // 展开状态
    var isExpanded by remember { mutableStateOf(false) }

    // 监听修饰键状态
    var modifierState by remember { mutableStateOf(modifierManager.getModifierState()) }

    // 定期同步状态
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(50)
            modifierState = modifierManager.getModifierState()
        }
    }

    // 按键颜色定义
    val keyBackgroundColor = Color(0xFF2D2D2D)
    val keyActiveColor = Color(0xFF4A6FA5)
    val keyLockedColor = Color(0xFF3D5A80)
    val keyTextColor = Color(0xFFE0E0E0)
    val keyActiveTextColor = Color.White
    val underlineColor = Color(0xFF4FC3F7)
    val dividerColor = Color(0xFF3A3A3A)

    // Ctrl 键状态
    val ctrlState = modifierState.ctrlState
    val isCtrlActive = ctrlState != ModifierLevel.OFF
    val isCtrlLocked = ctrlState == ModifierLevel.LOCKED

    // Shift 键状态
    val shiftState = modifierState.shiftState
    val isShiftActive = shiftState != ModifierLevel.OFF
    val isShiftLocked = shiftState == ModifierLevel.LOCKED

    // Alt 键状态
    val altState = modifierState.altState
    val isAltActive = altState != ModifierLevel.OFF
    val isAltLocked = altState == ModifierLevel.LOCKED

    Column(
        modifier = modifier.background(Color(0xFF1A1A1A))
    ) {
        // 主行按键
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // ESC 键
            ExtraKeyButton(
                text = "ESC",
                backgroundColor = keyBackgroundColor,
                textColor = keyTextColor,
                onClick = { onSendKey("\u001b") }
            )

            // Ctrl 键 - 特殊逻辑
            ExtraKeyButton(
                text = "CTRL",
                backgroundColor = when {
                    isCtrlLocked -> keyLockedColor
                    isCtrlActive -> keyActiveColor
                    else -> keyBackgroundColor
                },
                textColor = if (isCtrlActive) keyActiveTextColor else keyTextColor,
                showUnderline = isCtrlLocked,
                underlineColor = underlineColor,
                onClick = {
                    when (ctrlState) {
                        ModifierLevel.OFF -> modifierManager.toggleCtrl(sticky = false)
                        ModifierLevel.TRANSIENT -> modifierManager.toggleCtrl(sticky = true)
                        ModifierLevel.LOCKED -> modifierManager.toggleCtrl(sticky = false)
                    }
                }
            )

            // Alt 键
            ExtraKeyButton(
                text = "ALT",
                backgroundColor = when {
                    isAltLocked -> keyLockedColor
                    isAltActive -> keyActiveColor
                    else -> keyBackgroundColor
                },
                textColor = if (isAltActive) keyActiveTextColor else keyTextColor,
                showUnderline = isAltLocked,
                underlineColor = underlineColor,
                onClick = {
                    when (altState) {
                        ModifierLevel.OFF -> modifierManager.toggleAlt(sticky = false)
                        ModifierLevel.TRANSIENT -> modifierManager.toggleAlt(sticky = true)
                        ModifierLevel.LOCKED -> modifierManager.toggleAlt(sticky = false)
                    }
                }
            )

            Spacer(Modifier.weight(1f, fill = true))

            // 方向键组 - 单行水平布局
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                ExtraKeyButton(
                    text = "←",
                    backgroundColor = keyBackgroundColor,
                    textColor = keyTextColor,
                    onClick = { onSendKey("\u001b[D") },
                    fixedHeight = 36.dp,
                    fontSize = 14.sp
                )
                ExtraKeyButton(
                    text = "↑",
                    backgroundColor = keyBackgroundColor,
                    textColor = keyTextColor,
                    onClick = { onSendKey("\u001b[A") },
                    fixedHeight = 36.dp,
                    fontSize = 14.sp
                )
                ExtraKeyButton(
                    text = "↓",
                    backgroundColor = keyBackgroundColor,
                    textColor = keyTextColor,
                    onClick = { onSendKey("\u001b[B") },
                    fixedHeight = 36.dp,
                    fontSize = 14.sp
                )
                ExtraKeyButton(
                    text = "→",
                    backgroundColor = keyBackgroundColor,
                    textColor = keyTextColor,
                    onClick = { onSendKey("\u001b[C") },
                    fixedHeight = 36.dp,
                    fontSize = 14.sp
                )
            }

            Spacer(Modifier.weight(1f, fill = true))

            // 展开按钮
            ExtraKeyButton(
                text = if (isExpanded) "▼" else "▲",
                backgroundColor = if (isExpanded) keyActiveColor else keyBackgroundColor,
                textColor = if (isExpanded) keyActiveTextColor else keyTextColor,
                onClick = { isExpanded = !isExpanded }
            )
        }

        // 展开后的额外按键行
        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically(
                animationSpec = tween(200)
            ) + fadeIn(
                animationSpec = tween(200)
            ),
            exit = shrinkVertically(
                animationSpec = tween(200)
            ) + fadeOut(
                animationSpec = tween(200)
            )
        ) {
            Column {
                // 分隔线
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .padding(horizontal = 8.dp)
                        .background(dividerColor)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Tab
                    ExtraKeyButton(
                        text = "TAB",
                        backgroundColor = keyBackgroundColor,
                        textColor = keyTextColor,
                        onClick = { onSendKey("\t") }
                    )

                    // Shift 键
                    ExtraKeyButton(
                        text = "SHIFT",
                        backgroundColor = when {
                            isShiftLocked -> keyLockedColor
                            isShiftActive -> keyActiveColor
                            else -> keyBackgroundColor
                        },
                        textColor = if (isShiftActive) keyActiveTextColor else keyTextColor,
                        showUnderline = isShiftLocked,
                        underlineColor = underlineColor,
                        onClick = {
                            when (shiftState) {
                                ModifierLevel.OFF -> modifierManager.toggleShift(sticky = false)
                                ModifierLevel.TRANSIENT -> modifierManager.toggleShift(sticky = true)
                                ModifierLevel.LOCKED -> modifierManager.toggleShift(sticky = false)
                            }
                        }
                    )

                    // 分隔符
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(24.dp)
                            .background(dividerColor)
                    )

                    // Home
                    ExtraKeyButton(
                        text = "HOME",
                        backgroundColor = keyBackgroundColor,
                        textColor = keyTextColor,
                        onClick = { onSendKey("\u001b[H") },
                        fontSize = 10.sp
                    )

                    // End
                    ExtraKeyButton(
                        text = "END",
                        backgroundColor = keyBackgroundColor,
                        textColor = keyTextColor,
                        onClick = { onSendKey("\u001b[F") },
                        fontSize = 10.sp
                    )

                    // 分隔符
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(24.dp)
                            .background(dividerColor)
                    )

                    // PgUp
                    ExtraKeyButton(
                        text = "PGUP",
                        backgroundColor = keyBackgroundColor,
                        textColor = keyTextColor,
                        onClick = { onSendKey("\u001b[5~") },
                        fontSize = 10.sp
                    )

                    // PgDn
                    ExtraKeyButton(
                        text = "PGDN",
                        backgroundColor = keyBackgroundColor,
                        textColor = keyTextColor,
                        onClick = { onSendKey("\u001b[6~") },
                        fontSize = 10.sp
                    )

                    // 分隔符
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(24.dp)
                            .background(dividerColor)
                    )

                    // Ins
                    ExtraKeyButton(
                        text = "INS",
                        backgroundColor = keyBackgroundColor,
                        textColor = keyTextColor,
                        onClick = { onSendKey("\u001b[2~") },
                        fontSize = 10.sp
                    )

                    // Del
                    ExtraKeyButton(
                        text = "DEL",
                        backgroundColor = keyBackgroundColor,
                        textColor = keyTextColor,
                        onClick = { onSendKey("\u001b[3~") },
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}

/**
 * 单个快捷键按钮
 */
@Composable
private fun ExtraKeyButton(
    text: String,
    backgroundColor: Color,
    textColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    showUnderline: Boolean = false,
    underlineColor: Color = Color.Cyan,
    fontSize: androidx.compose.ui.unit.TextUnit = 12.sp,
    fixedHeight: androidx.compose.ui.unit.Dp = 36.dp
) {
    Box(
        modifier = modifier
            .height(fixedHeight)
            .width(IntrinsicSize.Min)
            .background(backgroundColor, RoundedCornerShape(6.dp))
            .then(
                if (showUnderline) {
                    Modifier.drawBehind {
                        val strokeWidth = 2.dp.toPx()
                        val y = size.height - strokeWidth - 4.dp.toPx()
                        drawLine(
                            color = underlineColor,
                            start = Offset(6.dp.toPx(), y),
                            end = Offset(size.width - 6.dp.toPx(), y),
                            strokeWidth = strokeWidth
                        )
                    }
                } else Modifier
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = fontSize,
            textAlign = TextAlign.Center
        )
    }
}
