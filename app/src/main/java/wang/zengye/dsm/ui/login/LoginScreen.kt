package wang.zengye.dsm.ui.login

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import wang.zengye.dsm.R
import wang.zengye.dsm.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToAccounts: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val uiState by viewModel.state.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current

    // 收集事件
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is LoginEvent.LoginSuccess -> onLoginSuccess()
                is LoginEvent.ShowError -> { /* 错误已在状态中处理 */ }
                is LoginEvent.RequireOtp -> { /* OTP 需求已在状态中处理 */ }
                is LoginEvent.DeviceUnauthorized -> { /* 设备未授权已在状态中处理 */ }
            }
        }
    }

    // 自动登录：当有保存的凭据时自动尝试登录
    LaunchedEffect(uiState.host, uiState.account, uiState.password) {
        viewModel.sendIntent(LoginIntent.AutoLogin)
    }

    Scaffold(
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
                TopAppBar(
                    title = {},
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        scrolledContainerColor = Color.Transparent
                    ),
                    actions = {
                        IconButton(onClick = onNavigateToAccounts) {
                            Icon(
                                imageVector = Icons.Outlined.AccountCircle,
                                contentDescription = stringResource(R.string.login_account_management)
                            )
                        }
                    }
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
            ) {
                // 顶部品牌区域
                BrandSection(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                )

                // 登录表单区域
                LoginFormSection(
                    uiState = uiState,
                    onHostChange = { viewModel.sendIntent(LoginIntent.SetHost(it)) },
                    onAccountChange = { viewModel.sendIntent(LoginIntent.SetAccount(it)) },
                    onPasswordChange = { viewModel.sendIntent(LoginIntent.SetPassword(it)) },
                    onOtpChange = { viewModel.sendIntent(LoginIntent.SetOtpCode(it)) },
                    onRememberDeviceChange = { viewModel.sendIntent(LoginIntent.SetRememberDevice(it)) },
                    onCheckSslChange = { viewModel.sendIntent(LoginIntent.SetCheckSsl(!it)) },
                    onLogin = { viewModel.sendIntent(LoginIntent.Login) },
                    focusManager = focusManager,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.PageHorizontal)
                        .padding(top = Spacing.Large)
                )

                // 底部帮助信息
                HelpSection(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Spacing.Large)
                )
            }
        }
}

/**
 * 品牌区域
 */
@Composable
private fun BrandSection(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val launcherBitmap = remember {
        BitmapFactory.decodeResource(context.resources, R.mipmap.ic_launcher_round)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(top = Spacing.ExtraLarge),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Logo 图标
        if (launcherBitmap != null) {
            Image(
                bitmap = launcherBitmap.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier.size(96.dp)
            )
        }

        Spacer(modifier = Modifier.height(Spacing.Large))

        // 应用名称
        Text(
            text = stringResource(R.string.login_app_title),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(Spacing.Small))

        // 副标题
        Text(
            text = stringResource(R.string.login_subtitle),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * 登录表单区域
 */
@Composable
private fun LoginFormSection(
    uiState: LoginUiState,
    onHostChange: (String) -> Unit,
    onAccountChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onOtpChange: (String) -> Unit,
    onRememberDeviceChange: (Boolean) -> Unit,
    onCheckSslChange: (Boolean) -> Unit,
    onLogin: () -> Unit,
    focusManager: androidx.compose.ui.focus.FocusManager,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 表单卡片
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = AppShapes.Card
        ) {
            Column(
                modifier = Modifier.padding(Spacing.CardPadding)
            ) {
                // 服务器地址
                TextField(
                    value = uiState.host,
                    onValueChange = onHostChange,
                    label = { Text(stringResource(R.string.login_server_address)) },
                    placeholder = { Text(stringResource(R.string.login_server_address_hint)) },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Dns,
                            contentDescription = null
                        )
                    },
                    isError = uiState.hostError != null,
                    supportingText = uiState.hostError?.let { { Text(it) } },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Uri,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    ),
                    shape = RoundedCornerShape(CornerRadius.Medium)
                )

                Spacer(modifier = Modifier.height(Spacing.Standard))

                // 用户名
                TextField(
                    value = uiState.account,
                    onValueChange = onAccountChange,
                    label = { Text(stringResource(R.string.login_username)) },
                    placeholder = { Text(stringResource(R.string.login_username_hint)) },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Person,
                            contentDescription = null
                        )
                    },
                    isError = uiState.accountError != null,
                    supportingText = uiState.accountError?.let { { Text(it) } },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    ),
                    shape = RoundedCornerShape(CornerRadius.Medium)
                )

                Spacer(modifier = Modifier.height(Spacing.Standard))

                // 密码
                var passwordVisible by remember { mutableStateOf(false) }

                TextField(
                    value = uiState.password,
                    onValueChange = onPasswordChange,
                    label = { Text(stringResource(R.string.login_password_label)) },
                    placeholder = { Text(stringResource(R.string.login_password_hint)) },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Lock,
                            contentDescription = null
                        )
                    },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                contentDescription = if (passwordVisible) stringResource(R.string.login_hide_password) else stringResource(R.string.login_show_password)
                            )
                        }
                    },
                    singleLine = true,
                    isError = uiState.passwordError != null,
                    supportingText = uiState.passwordError?.let { { Text(it) } },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = if (uiState.requireOtp) ImeAction.Next else ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) },
                        onDone = { onLogin() }
                    ),
                    shape = RoundedCornerShape(CornerRadius.Medium)
                )

                // OTP 验证码（如果需要）
                if (uiState.requireOtp) {
                    Spacer(modifier = Modifier.height(Spacing.Standard))

                    TextField(
                        value = uiState.otpCode,
                        onValueChange = onOtpChange,
                        label = { Text(stringResource(R.string.login_otp_label)) },
                        placeholder = { Text(stringResource(R.string.login_otp_hint)) },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.VerifiedUser,
                                contentDescription = null
                            )
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = { onLogin() }
                        ),
                        shape = RoundedCornerShape(CornerRadius.Medium)
                    )

                    // 记住设备选项
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = Spacing.Small),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = uiState.rememberDevice,
                            onCheckedChange = onRememberDeviceChange
                        )
                        Spacer(modifier = Modifier.width(Spacing.Small))
                        Text(
                            text = stringResource(R.string.login_remember_device),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(Spacing.Small))

                // SSL 证书验证选项
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = !uiState.checkSsl,
                        onCheckedChange = onCheckSslChange
                    )
                    Spacer(modifier = Modifier.width(Spacing.Small))
                    Text(
                        text = stringResource(R.string.login_ignore_ssl_error),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        // 错误提示
        uiState.error?.let { error ->
            Spacer(modifier = Modifier.height(Spacing.Standard))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.Small),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Error,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(Spacing.MediumSmall))
                Text(
                    text = error,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }

        Spacer(modifier = Modifier.height(Spacing.Large))

        // 登录按钮
        Button(
            onClick = onLogin,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            enabled = !uiState.isLoading,
            shape = RoundedCornerShape(CornerRadius.Huge)
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(Spacing.Small))
                Text(stringResource(R.string.login_logging_in))
            } else {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Login,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(Spacing.Small))
                Text(
                    text = stringResource(R.string.login_button),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

/**
 * 帮助信息区域
 */
@Composable
private fun HelpSection(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.login_help_title),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(Spacing.Small))

        Text(
            text = stringResource(R.string.login_help_formats),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(Spacing.Small))

        Surface(
            shape = RoundedCornerShape(CornerRadius.Small),
            color = MaterialTheme.colorScheme.surfaceContainerHigh
        ) {
            Text(
                text = stringResource(R.string.login_help_default_protocol),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = Spacing.Standard, vertical = Spacing.Small)
            )
        }
    }
}
