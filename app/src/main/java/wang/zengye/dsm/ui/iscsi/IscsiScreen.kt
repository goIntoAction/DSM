package wang.zengye.dsm.ui.iscsi

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.flow.collectLatest
import wang.zengye.dsm.R
import wang.zengye.dsm.ui.components.*
import wang.zengye.dsm.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IscsiScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: IscsiViewModel = hiltViewModel()
) {
    val uiState by viewModel.state.collectAsStateWithLifecycle()
    val tabs = listOf(stringResource(R.string.iscsi_tab_lun), stringResource(R.string.iscsi_tab_target))
    val snackbarHostState = remember { SnackbarHostState() }

    // 处理事件
    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is IscsiEvent.ShowError -> {
                    snackbarHostState.showSnackbar(
                        message = event.message,
                        duration = SnackbarDuration.Short
                    )
                }
                is IscsiEvent.ShowSuccess -> {
                    snackbarHostState.showSnackbar(
                        message = event.message,
                        duration = SnackbarDuration.Short
                    )
                }
            }
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.iscsi_title), fontWeight = FontWeight.Medium) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.common_back))
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.sendIntent(IscsiIntent.LoadData) }, enabled = !uiState.isLoading) {
                        Icon(Icons.Outlined.Refresh, contentDescription = stringResource(R.string.common_refresh))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = Color.Transparent
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (uiState.currentTab == 0) {
                        viewModel.sendIntent(IscsiIntent.ShowCreateLunDialog(true))
                    } else {
                        viewModel.sendIntent(IscsiIntent.ShowCreateTargetDialog(true))
                    }
                },
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.common_create))
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues)
        ) {
            SecondaryScrollableTabRow(
                selectedTabIndex = uiState.currentTab,
                containerColor = Color.Transparent,
                edgePadding = Spacing.PageHorizontal
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = uiState.currentTab == index,
                        onClick = { viewModel.sendIntent(IscsiIntent.SetTab(index)) },
                        text = { Text(title, fontWeight = if (uiState.currentTab == index) FontWeight.Medium else FontWeight.Normal) },
                        selectedContentColor = MaterialTheme.colorScheme.primary,
                        unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            when {
                uiState.isLoading -> LoadingState(
                    message = stringResource(R.string.iscsi_loading),
                    modifier = Modifier.fillMaxSize()
                )
                uiState.error != null && uiState.luns.isEmpty() && uiState.targets.isEmpty() -> ErrorState(
                    message = uiState.error ?: stringResource(R.string.iscsi_load_failed),
                    onRetry = { viewModel.sendIntent(IscsiIntent.LoadData) },
                    modifier = Modifier.fillMaxSize()
                )
                else -> when (uiState.currentTab) {
                    0 -> LunList(
                        luns = uiState.luns,
                        onDelete = { lunId ->
                            viewModel.sendIntent(IscsiIntent.ShowDeleteConfirmDialog("lun", lunId, true))
                        }
                    )
                    1 -> TargetList(
                        targets = uiState.targets,
                        onToggleEnabled = { targetId, enabled ->
                            viewModel.sendIntent(IscsiIntent.SetTargetEnabled(targetId, enabled))
                        },
                        onDelete = { targetId ->
                            viewModel.sendIntent(IscsiIntent.ShowDeleteConfirmDialog("target", targetId, true))
                        }
                    )
                }
            }
        }

        // 创建 LUN 对话框
        if (uiState.showCreateLunDialog) {
            CreateLunDialog(
                storagePools = uiState.storagePools,
                onDismiss = { viewModel.sendIntent(IscsiIntent.ShowCreateLunDialog(false)) },
                onCreate = { name, location, size, thinProvision ->
                    viewModel.sendIntent(IscsiIntent.CreateLun(name, location, size, thinProvision))
                }
            )
        }

        // 创建 Target 对话框
        if (uiState.showCreateTargetDialog) {
            CreateTargetDialog(
                luns = uiState.luns,
                onDismiss = { viewModel.sendIntent(IscsiIntent.ShowCreateTargetDialog(false)) },
                onCreate = { name, iqn, mappedLunIds ->
                    viewModel.sendIntent(IscsiIntent.CreateTarget(name, iqn, mappedLunIds))
                }
            )
        }

        // 删除确认对话框
        if (uiState.showDeleteConfirmDialog) {
            DeleteConfirmDialog(
                type = uiState.deleteTargetType,
                onDismiss = { viewModel.sendIntent(IscsiIntent.ShowDeleteConfirmDialog("", 0, false)) },
                onConfirm = {
                    if (uiState.deleteTargetType == "lun") {
                        viewModel.sendIntent(IscsiIntent.DeleteLun(uiState.deleteTargetId))
                    } else {
                        viewModel.sendIntent(IscsiIntent.DeleteTarget(uiState.deleteTargetId))
                    }
                }
            )
        }

        // 操作中遮罩
        if (uiState.isOperating) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }
}

@Composable
private fun LunList(
    luns: List<LunInfo>,
    onDelete: (Int) -> Unit
) {
    if (luns.isEmpty()) {
        EmptyState(
            message = stringResource(R.string.iscsi_no_lun),
            icon = Icons.Outlined.Storage,
            modifier = Modifier.fillMaxSize()
        )
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(luns, key = { it.id }) { lun ->
                LunCard(lun, onDelete)
            }
        }
    }
}

@Composable
private fun LunCard(lun: LunInfo, onDelete: (Int) -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Storage,
                    contentDescription = null,
                    tint = when (lun.status) {
                        "normal" -> MaterialTheme.colorScheme.primary
                        "degraded" -> MaterialTheme.colorScheme.tertiary
                        else -> MaterialTheme.colorScheme.error
                    }
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(lun.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Text(
                        text = if (lun.isThin) stringResource(R.string.iscsi_lun_thin) else stringResource(R.string.iscsi_lun_thick),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                StatusChip(lun.status)

                Spacer(modifier = Modifier.width(8.dp))

                // 删除按钮
                IconButton(onClick = { onDelete(lun.id) }) {
                    Icon(
                        Icons.Outlined.Delete,
                        contentDescription = stringResource(R.string.common_delete),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            if (lun.location.isNotEmpty()) {
                InfoRow(stringResource(R.string.iscsi_lun_location), lun.location)
            }
            InfoRow(stringResource(R.string.iscsi_lun_size), formatSize(lun.size))
            if (lun.usedSize > 0) {
                InfoRow(stringResource(R.string.iscsi_lun_used), formatSize(lun.usedSize))
                val usagePercent = if (lun.size > 0) (lun.usedSize * 100f / lun.size) else 0f
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { usagePercent / 100f },
                    modifier = Modifier.fillMaxWidth(),
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }
        }
    }
}

@Composable
private fun TargetList(
    targets: List<TargetInfo>,
    onToggleEnabled: (Int, Boolean) -> Unit,
    onDelete: (Int) -> Unit
) {
    if (targets.isEmpty()) {
        EmptyState(
            message = stringResource(R.string.iscsi_no_target),
            icon = Icons.Outlined.Dns,
            modifier = Modifier.fillMaxSize()
        )
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(targets, key = { it.id }) { target ->
                TargetCard(target, onToggleEnabled, onDelete)
            }
        }
    }
}

@Composable
private fun TargetCard(
    target: TargetInfo,
    onToggleEnabled: (Int, Boolean) -> Unit,
    onDelete: (Int) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Dns,
                    contentDescription = null,
                    tint = if (target.enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(target.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Text(
                        text = target.iqn,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }

                // 启用/禁用开关
                Switch(
                    checked = target.enabled,
                    onCheckedChange = { onToggleEnabled(target.id, it) }
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (target.connectedSessions > 0) {
                    AssistChip(
                        onClick = {},
                        label = { Text(stringResource(R.string.iscsi_target_connected)) },
                        leadingIcon = { Icon(Icons.Default.Link, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    )
                }
                Text(
                    text = "${stringResource(R.string.iscsi_target_sessions)}: ${target.connectedSessions}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${stringResource(R.string.iscsi_target_luns)}: ${target.mappedLunIds.size}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 删除按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = { onDelete(target.id) },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(Icons.Outlined.Delete, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(stringResource(R.string.common_delete))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateLunDialog(
    storagePools: List<StoragePoolInfo>,
    onDismiss: () -> Unit,
    onCreate: (name: String, location: String, size: Long, thinProvision: Boolean) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedPool by remember { mutableStateOf<StoragePoolInfo?>(null) }
    var sizeGB by remember { mutableStateOf("") }
    var thinProvision by remember { mutableStateOf(true) }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.iscsi_lun_create_title)) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.iscsi_lun_name)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = selectedPool?.name ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.iscsi_lun_location)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        storagePools.forEach { pool ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(pool.name)
                                        Text(
                                            "${formatSize(pool.sizeFree)} / ${formatSize(pool.sizeTotal)}",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                },
                                onClick = {
                                    selectedPool = pool
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = sizeGB,
                    onValueChange = { sizeGB = it.filter { c -> c.isDigit() } },
                    label = { Text(stringResource(R.string.iscsi_lun_size_gb)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = thinProvision,
                        onCheckedChange = { thinProvision = it }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.iscsi_lun_thin))
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val sizeBytes = sizeGB.toLongOrNull()?.times(1024 * 1024 * 1024) ?: 0L
                    if (name.isNotBlank() && selectedPool != null && sizeBytes > 0) {
                        onCreate(name, selectedPool!!.name, sizeBytes, thinProvision)
                    }
                },
                enabled = name.isNotBlank() && selectedPool != null && sizeGB.isNotBlank()
            ) {
                Text(stringResource(R.string.common_create))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.common_cancel))
            }
        }
    )
}

@Composable
private fun CreateTargetDialog(
    luns: List<LunInfo>,
    onDismiss: () -> Unit,
    onCreate: (name: String, iqn: String?, mappedLunIds: List<Int>) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var iqn by remember { mutableStateOf("") }
    val selectedLunIds = remember { mutableStateListOf<Int>() }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.iscsi_target_create_title)) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.iscsi_target_name)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = iqn,
                    onValueChange = { iqn = it },
                    label = { Text(stringResource(R.string.iscsi_target_iqn) + " (${stringResource(R.string.common_optional)})") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    supportingText = { Text(stringResource(R.string.iscsi_target_iqn_hint)) }
                )

                if (luns.isNotEmpty()) {
                    Text(
                        text = stringResource(R.string.iscsi_target_select_luns),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    luns.forEach { lun ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = selectedLunIds.contains(lun.id),
                                onCheckedChange = { checked ->
                                    if (checked) {
                                        selectedLunIds.add(lun.id)
                                    } else {
                                        selectedLunIds.remove(lun.id)
                                    }
                                }
                            )
                            Text(lun.name)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onCreate(name, iqn.ifBlank { null }, selectedLunIds.toList())
                },
                enabled = name.isNotBlank()
            ) {
                Text(stringResource(R.string.common_create))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.common_cancel))
            }
        }
    )
}

@Composable
private fun DeleteConfirmDialog(
    type: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val title = if (type == "lun") stringResource(R.string.iscsi_lun_delete_title) else stringResource(R.string.iscsi_target_delete_title)
    val message = if (type == "lun") stringResource(R.string.iscsi_lun_delete_confirm) else stringResource(R.string.iscsi_target_delete_confirm)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text(stringResource(R.string.common_delete))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.common_cancel))
            }
        }
    )
}

@Composable
private fun StatusChip(status: String) {
    val (text, color) = when (status) {
        "normal" -> stringResource(R.string.iscsi_lun_status_normal) to MaterialTheme.colorScheme.primary
        "degraded" -> stringResource(R.string.iscsi_lun_status_degraded) to MaterialTheme.colorScheme.tertiary
        "crashed" -> stringResource(R.string.iscsi_lun_status_crashed) to MaterialTheme.colorScheme.error
        else -> stringResource(R.string.iscsi_lun_status_unknown) to MaterialTheme.colorScheme.onSurfaceVariant
    }
    Surface(
        color = color.copy(alpha = 0.12f),
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color
        )
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
    }
}

private fun formatSize(bytes: Long): String {
    if (bytes <= 0) return "0 B"
    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    val digitGroups = (Math.log10(bytes.toDouble()) / Math.log10(1024.0)).toInt()
    val idx = digitGroups.coerceAtMost(units.size - 1)
    return "%.1f %s".format(bytes / Math.pow(1024.0, idx.toDouble()), units[idx])
}