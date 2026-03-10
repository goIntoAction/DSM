package wang.zengye.dsm.ui.packages

import android.content.Intent
import android.net.Uri
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import wang.zengye.dsm.R
import wang.zengye.dsm.data.api.DsmApiHelper
import wang.zengye.dsm.data.model.PackageInfo
import wang.zengye.dsm.ui.components.EmptyState
import wang.zengye.dsm.ui.components.ErrorState
import wang.zengye.dsm.ui.components.LoadingState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PackagesScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: PackagesViewModel = hiltViewModel()
) {
    val uiState by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    // 处理 Event
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is PackagesEvent.ShowError -> {
                    snackbarHostState.showSnackbar(event.message)
                }
                is PackagesEvent.ShowSuccess -> {
                    snackbarHostState.showSnackbar(event.message)
                }
                is PackagesEvent.InstallSuccess -> {
                    snackbarHostState.showSnackbar("安装成功")
                }
                is PackagesEvent.UninstallSuccess -> {
                    snackbarHostState.showSnackbar("卸载成功")
                }
            }
        }
    }

    // 卸载确认对话框
    var showUninstallDialog by remember { mutableStateOf(false) }
    var selectedPackage by remember { mutableStateOf<PackageInfo?>(null) }
    var removePackageData by remember { mutableStateOf(false) }

    // Tab配置
    val tabs = listOf("已安装", "全部套件", "社群")

    val onRefreshPackages = remember(viewModel) {
        { viewModel.sendIntent(PackagesIntent.Refresh) }
    }

    val onSetFilterAll = remember(viewModel) {
        { viewModel.sendIntent(PackagesIntent.SetFilter("all")) }
    }

    val onSetFilterRunning = remember(viewModel) {
        { viewModel.sendIntent(PackagesIntent.SetFilter("running")) }
    }

    val onSetFilterStopped = remember(viewModel) {
        { viewModel.sendIntent(PackagesIntent.SetFilter("stopped")) }
    }

    val onDismissUninstallDialog = remember {
        {
            showUninstallDialog = false
            selectedPackage = null
            removePackageData = false
            Unit
        }
    }

    val onConfirmUninstall = remember(viewModel) {
        {
            selectedPackage?.let { pkg ->
                viewModel.sendIntent(PackagesIntent.UninstallPackage(
                    packageId = pkg.id,
                    removeData = removePackageData
                ))
                showUninstallDialog = false
                selectedPackage = null
                removePackageData = false
            }
            Unit
        }
    }

    // 卸载确认对话框
    if (showUninstallDialog && selectedPackage != null) {
        AlertDialog(
            onDismissRequest = onDismissUninstallDialog,
            title = { Text(stringResource(R.string.packages_uninstall_title)) },
            text = {
                Column {
                    Text(stringResource(R.string.packages_uninstall_confirm, selectedPackage?.displayName ?: ""))
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = removePackageData,
                            onCheckedChange = { removePackageData = it }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.packages_remove_data),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    if (removePackageData) {
                        Text(
                            text = stringResource(R.string.packages_remove_data_warning),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = onConfirmUninstall,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(stringResource(R.string.packages_uninstall))
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissUninstallDialog) {
                    Text(stringResource(R.string.common_cancel))
                }
            }
        )
    }

    // 操作消息 Snackbar
    LaunchedEffect(uiState.operationMessage) {
        uiState.operationMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.sendIntent(PackagesIntent.ClearOperationMessage)
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            Column {
                TopAppBar(
                    title = { Text(stringResource(R.string.packages_title)) },
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
                        IconButton(onClick = onRefreshPackages) {
                            Icon(Icons.Filled.Refresh, contentDescription = stringResource(R.string.common_refresh))
                        }
                    }
                )
                // TabBar
                TabRow(
                    selectedTabIndex = uiState.selectedTab,
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = uiState.selectedTab == index,
                            onClick = { viewModel.sendIntent(PackagesIntent.SelectTab(index)) },
                            text = { Text(title) }
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        val currentTabData = uiState.currentTabData
        val isInstalledTab = uiState.selectedTab == 0

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 筛选标签（仅在已安装Tab显示）
            if (isInstalledTab) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = uiState.filter == "all",
                        onClick = onSetFilterAll,
                        label = { Text(stringResource(R.string.packages_filter_all_count, currentTabData.packages.size)) }
                    )
                    FilterChip(
                        selected = uiState.filter == "running",
                        onClick = onSetFilterRunning,
                        label = { Text(stringResource(R.string.packages_filter_running)) }
                    )
                    FilterChip(
                        selected = uiState.filter == "stopped",
                        onClick = onSetFilterStopped,
                        label = { Text(stringResource(R.string.packages_filter_stopped)) }
                    )
                }
            }

            when {
                currentTabData.isLoading -> {
                    LoadingState(
                        message = stringResource(R.string.common_loading),
                        modifier = Modifier.fillMaxSize()
                    )
                }

                currentTabData.error != null -> {
                    ErrorState(
                        message = currentTabData.error ?: stringResource(R.string.common_load_failed),
                        onRetry = onRefreshPackages,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                currentTabData.packages.isEmpty() -> {
                    EmptyState(
                        message = stringResource(R.string.packages_no_packages_desc),
                        modifier = Modifier.fillMaxSize()
                    )
                }

                else -> {
                    val filteredPackages = remember(uiState.filter, currentTabData.packages) {
                        viewModel.getFilteredPackages()
                    }

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        items(filteredPackages, key = { it.id }) { pkg ->
                            val onStartPkg = remember(viewModel, pkg) {
                                { viewModel.sendIntent(PackagesIntent.StartPackage(pkg.id)) }
                            }
                            val onStopPkg = remember(viewModel, pkg) {
                                { viewModel.sendIntent(PackagesIntent.StopPackage(pkg.id)) }
                            }
                            val onOpenPkg = remember(context, pkg) {
                                {
                                    val url = DsmApiHelper.getPackageUrl(pkg.name)
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                    context.startActivity(intent)
                                }
                            }
                            val onUninstallPkg = remember(pkg) {
                                {
                                    selectedPackage = pkg
                                    showUninstallDialog = true
                                }
                            }
                            val onInstallPkg = remember(viewModel, pkg) {
                                {
                                    viewModel.sendIntent(PackagesIntent.InstallPackage(pkg.name))
                                }
                            }
                            PackageItem(
                                packageInfo = pkg,
                                isOperating = uiState.operatingPackageId == pkg.id,
                                onStart = onStartPkg,
                                onStop = onStopPkg,
                                onOpen = onOpenPkg,
                                onUninstall = onUninstallPkg,
                                onInstall = onInstallPkg
                            )
                        }
                    }
                }
            }
        }
    }
}
