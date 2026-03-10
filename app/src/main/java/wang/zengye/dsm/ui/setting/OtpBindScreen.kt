package wang.zengye.dsm.ui.setting

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.res.stringResource
import wang.zengye.dsm.R

/**
 * OTP绑定页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OtpBindScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: OtpBindViewModel = hiltViewModel()
) {
    val uiState by viewModel.state.collectAsStateWithLifecycle()

    // 收集事件
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is OtpBindEvent.ShowError -> { /* 错误已在状态中处理 */ }
                is OtpBindEvent.ShowSuccess -> { /* 成功已在状态中处理 */ }
                is OtpBindEvent.OtpEnabled -> { /* OTP已启用 */ }
                is OtpBindEvent.OtpDisabled -> { /* OTP已禁用 */ }
            }
        }
    }

    // 禁用OTP对话框
    if (uiState.showDisableDialog) {
        DisableOtpDialog(
            isLoading = uiState.isSaving,
            onDismiss = { viewModel.sendIntent(OtpBindIntent.HideDisableDialog) },
            onConfirm = { code -> viewModel.sendIntent(OtpBindIntent.DisableOtp(code)) }
        )
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.otp_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.common_back))
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
            // 消息提示
            LaunchedEffect(uiState.error, uiState.success) {
                // 自动清除消息
            }

            uiState.error?.let { error ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Error,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { viewModel.sendIntent(OtpBindIntent.ClearMessages) }) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = stringResource(R.string.common_close),
                                tint = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
            }

            uiState.success?.let { success ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = success,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { viewModel.sendIntent(OtpBindIntent.ClearMessages) }) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = stringResource(R.string.common_close),
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }

            when (uiState.currentStep) {
                0 -> OtpInitialStep(
                    isOtpEnabled = uiState.isOtpEnabled,
                    onEnable = { viewModel.sendIntent(OtpBindIntent.SetEmailAndGetQrCode("")) },
                    onDisable = { viewModel.sendIntent(OtpBindIntent.ShowDisableDialog) }
                )
                1 -> OtpEmailStep(
                    isLoading = uiState.isLoading,
                    onSubmit = { email -> viewModel.sendIntent(OtpBindIntent.SetEmailAndGetQrCode(email)) }
                )
                2 -> OtpQrCodeStep(
                    qrCodeUrl = uiState.qrCodeUrl,
                    secret = uiState.secret,
                    account = uiState.account,
                    onContinue = { /* 进入下一步 */ },
                    onVerify = { viewModel.sendIntent(OtpBindIntent.SetVerificationCode(it)) }
                )
                4 -> OtpCompleteStep(
                    onDone = { viewModel.sendIntent(OtpBindIntent.Reset) }
                )
            }
        }
    }
}