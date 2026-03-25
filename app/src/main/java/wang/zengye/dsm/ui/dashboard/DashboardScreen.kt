package wang.zengye.dsm.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.res.stringResource
import wang.zengye.dsm.R
import wang.zengye.dsm.ui.components.ErrorState
import wang.zengye.dsm.ui.components.LoadingState
import wang.zengye.dsm.ui.components.SectionHeader
import wang.zengye.dsm.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigateToSystemInfo: () -> Unit = {},
    onNavigateToSearch: () -> Unit = {},
    onNavigateToRoute: (String) -> Unit = {},
    onNavigateToTerminal: () -> Unit = {},
    onNavigateToDownloads: () -> Unit = {},
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.state.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current

    // 注册/注销 LifecycleObserver（退后台停止刷新，回到前台恢复刷新）
    DisposableEffect(lifecycleOwner) {
        lifecycleOwner.lifecycle.addObserver(viewModel)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(viewModel)
        }
    }

    // 关机/重启对话框状态
    var showPowerDialog by remember { mutableStateOf(false) }
    var powerAction by remember { mutableStateOf("") }

    // 电源操作确认对话框 - MD3风格
    if (showPowerDialog) {
        PowerConfirmDialog(
            powerAction = powerAction,
            onConfirm = {
                showPowerDialog = false
                if (powerAction == "shutdown") viewModel.sendIntent(DashboardIntent.Shutdown) else viewModel.sendIntent(DashboardIntent.Reboot)
            },
            onDismiss = { showPowerDialog = false }
        )
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.dashboard_console),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Medium
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = Color.Transparent
                ),
                actions = {
                    // 搜索按钮
                    IconButton(onClick = onNavigateToSearch) {
                        Icon(
                            imageVector = Icons.Outlined.Search,
                            contentDescription = stringResource(R.string.dashboard_search)
                        )
                    }
                    // 刷新按钮
                    IconButton(
                        onClick = { viewModel.sendIntent(DashboardIntent.Refresh) },
                        enabled = !uiState.isRefreshing
                    ) {
                        if (uiState.isRefreshing) {
                            CircularProgressIndicator(
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.primary
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Outlined.Refresh,
                                contentDescription = stringResource(R.string.dashboard_refresh)
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            LoadingState(
                message = stringResource(R.string.dashboard_loading_system_info),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            )
        } else if (uiState.error != null) {
            ErrorState(
                message = uiState.error ?: stringResource(R.string.dashboard_load_failed),
                onRetry = { viewModel.sendIntent(DashboardIntent.LoadData) },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
            ) {
                // 内容区域
                Column(
                    modifier = Modifier.padding(horizontal = Spacing.PageHorizontal),
                    verticalArrangement = Arrangement.spacedBy(Spacing.CardSpacing)
                ) {
                    // 问候语
                    GreetingSection(
                        model = uiState.systemInfo.model
                    )

                    // 系统信息卡片
                    SystemInfoCard(
                        systemInfo = uiState.systemInfo,
                        onClick = onNavigateToSystemInfo
                    )

                    // 资源使用情况 - 使用响应式布局
                    ResourceUsageSection(
                        cpuUsage = uiState.utilization.cpuUsage,
                        cpuHistory = uiState.utilization.cpuHistory,
                        memoryUsage = uiState.utilization.memoryUsage,
                        memoryHistory = uiState.utilization.memoryHistory,
                        memoryUsed = uiState.utilization.memoryUsed,
                        memoryTotal = uiState.utilization.memoryTotal
                    )

                    // 应用启动器
                    if (uiState.installedApps.isNotEmpty()) {
                        SectionHeader(title = stringResource(R.string.dashboard_apps))

                        AppLauncherGrid(
                            installedApps = uiState.installedApps,
                            onNavigate = onNavigateToRoute
                        )
                    }

                    // 存储空间
                    if (uiState.volumes.isNotEmpty()) {
                        SectionHeader(
                            title = stringResource(R.string.dashboard_storage)
                        )

                        uiState.volumes.forEach { volume ->
                            VolumeCard(volume = volume)
                            Spacer(modifier = Modifier.height(Spacing.CardSpacing))
                        }
                    }

                    // 磁盘信息
                    if (uiState.disks.isNotEmpty()) {
                        SectionHeader(title = stringResource(R.string.dashboard_disk_status))

                        uiState.disks.forEach { disk ->
                            DiskCard(disk = disk)
                            Spacer(modifier = Modifier.height(Spacing.CardSpacing))
                        }
                    }

                    // 网络信息
                    if (uiState.networks.isNotEmpty()) {
                        SectionHeader(title = stringResource(R.string.dashboard_network))

                        uiState.networks.forEachIndexed { index, network ->
                            key(index) {
                                NetworkCard(network = network)
                            }
                            Spacer(modifier = Modifier.height(Spacing.CardSpacing))
                        }
                    }

                    // 快捷操作（终端、关机/重启）
                    SectionHeader(title = stringResource(R.string.dashboard_quick_actions))

                    QuickActionsRow(
                        onDownloads = onNavigateToDownloads,
                        onTerminal = onNavigateToTerminal,
                        onShutdown = { powerAction = "shutdown"; showPowerDialog = true },
                        onReboot = { powerAction = "reboot"; showPowerDialog = true }
                    )
                }

                // 底部间距
                Spacer(modifier = Modifier.height(Spacing.ExtraLarge))
            }
        }
    }
}
