package wang.zengye.dsm.ui.control_panel

import android.widget.Toast
import androidx.compose.ui.graphics.Color

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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import wang.zengye.dsm.R
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TerminalScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: TerminalViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // 处理事件
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is TerminalEvent.ShowError -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
                is TerminalEvent.UpdateSuccess -> {
                    Toast.makeText(context, context.getString(R.string.common_save_success), Toast.LENGTH_SHORT).show()
                }
                is TerminalEvent.DisconnectSuccess -> {
                    Toast.makeText(context, context.getString(R.string.common_save_success), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.terminal_title_snmp)) },
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
                    IconButton(onClick = { viewModel.sendIntent(TerminalIntent.LoadSettings) }) {
                        Icon(Icons.Default.Refresh, contentDescription = stringResource(R.string.dashboard_refresh))
                    }
                }
            )
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
                        text = state.error ?: stringResource(R.string.dashboard_load_failed),
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { viewModel.sendIntent(TerminalIntent.LoadSettings) }) {
                        Text(stringResource(R.string.common_retry))
                    }
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // SSH状态卡片
                    item {
                        SshStatusCard(
                            setting = state.setting,
                            onEdit = { viewModel.sendIntent(TerminalIntent.ShowEditDialog) }
                        )
                    }

                    // SSH设置详情
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = stringResource(R.string.terminal_ssh_settings),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                SettingRow(stringResource(R.string.terminal_port), state.setting.port.toString())
                                SettingRow(stringResource(R.string.terminal_root_login), if (state.setting.allowRootLogin) stringResource(R.string.terminal_root_allowed) else stringResource(R.string.terminal_root_denied))
                                SettingRow(stringResource(R.string.terminal_ssh_key), if (state.setting.sshKeyEnabled) stringResource(R.string.terminal_enabled) else stringResource(R.string.terminal_disabled))
                                SettingRow(stringResource(R.string.terminal_max_connections), state.setting.maxConnections.toString())
                            }
                        }
                    }

                    // 活动会话
                    if (state.sessions.isNotEmpty()) {
                        item {
                            Text(
                                text = stringResource(R.string.terminal_active_sessions),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        items(state.sessions) { session ->
                            TerminalSessionCard(
                                session = session,
                                onDisconnect = { viewModel.sendIntent(TerminalIntent.DisconnectSession(session.id)) }
                            )
                        }
                    }

                    // 帮助信息
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = stringResource(R.string.terminal_connection_info),
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = stringResource(R.string.terminal_connection_hint),
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = stringResource(R.string.terminal_connection_command, state.setting.port),
                                    style = MaterialTheme.typography.bodySmall,
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                                )
                            }
                        }
                    }
                }
            }
        }

        // 编辑对话框
        if (state.showEditDialog) {
            EditTerminalDialog(
                currentSetting = state.setting,
                onDismiss = { viewModel.sendIntent(TerminalIntent.HideEditDialog) },
                onSave = { viewModel.sendIntent(TerminalIntent.UpdateSettings(it)) }
            )
        }
    }
}
