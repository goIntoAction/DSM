package wang.zengye.dsm.ui.control_panel

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import wang.zengye.dsm.R
import wang.zengye.dsm.ui.components.ErrorState
import wang.zengye.dsm.ui.components.LoadingState
import wang.zengye.dsm.ui.theme.*
import wang.zengye.dsm.util.formatSize

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddShareFolderScreen(
    shareName: String? = null, // 如果传入则表示编辑模式
    onNavigateBack: () -> Unit,
    viewModel: AddShareFolderViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showVolumePicker by remember { mutableStateOf(false) }
    var showQuotaUnitPicker by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    // 处理事件
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is AddShareFolderEvent.ShowError -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
                is AddShareFolderEvent.SaveSuccess -> {
                    Toast.makeText(context, context.getString(R.string.common_save_success), Toast.LENGTH_SHORT).show()
                    onNavigateBack()
                }
            }
        }
    }

    LaunchedEffect(shareName) {
        viewModel.sendIntent(AddShareFolderIntent.LoadVolumes)
        if (shareName != null) {
            viewModel.sendIntent(AddShareFolderIntent.LoadShareDetail(shareName))
        }
    }

    // 选择存储卷对话框
    if (showVolumePicker) {
        VolumePickerDialog(
            volumes = state.volumes,
            selectedIndex = state.selectedVolumeIndex,
            onSelect = { index ->
                viewModel.sendIntent(AddShareFolderIntent.SelectVolume(index))
                showVolumePicker = false
            },
            onDismiss = { showVolumePicker = false }
        )
    }

    // 选择配额单位对话框
    if (showQuotaUnitPicker) {
        QuotaUnitPickerDialog(
            units = AddShareFolderViewModel.QUOTA_UNITS,
            selectedIndex = state.quotaUnitIndex,
            onSelect = { index ->
                viewModel.sendIntent(AddShareFolderIntent.UpdateQuotaUnit(index))
                showQuotaUnitPicker = false
            },
            onDismiss = { showQuotaUnitPicker = false }
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
                        text = stringResource(
                            if (state.isEdit) R.string.share_edit_title else R.string.share_add_title
                        )
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
                )
            )
        }
    ) { paddingValues ->
        when {
            state.isLoading -> {
                LoadingState(
                    message = stringResource(R.string.common_loading),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
            }

            state.error != null && state.volumes.isEmpty() -> {
                ErrorState(
                    message = state.error ?: stringResource(R.string.common_load_failed),
                    onRetry = { viewModel.sendIntent(AddShareFolderIntent.LoadVolumes) },
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
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentPadding = PaddingValues(Spacing.PageHorizontal, Spacing.Medium),
                        verticalArrangement = Arrangement.spacedBy(Spacing.Medium)
                    ) {
                        // 名称
                        item {
                            OutlinedTextField(
                                value = state.name,
                                onValueChange = { viewModel.sendIntent(AddShareFolderIntent.UpdateName(it)) },
                                label = { Text(stringResource(R.string.share_name)) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                        }

                        // 描述
                        item {
                            OutlinedTextField(
                                value = state.description,
                                onValueChange = { viewModel.sendIntent(AddShareFolderIntent.UpdateDescription(it)) },
                                label = { Text(stringResource(R.string.share_description)) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                        }

                        // 所在位置
                        item {
                            val volume = state.volumes.getOrNull(state.selectedVolumeIndex)
                            OutlinedTextField(
                                value = volume?.let { v ->
                                    stringResource(
                                        R.string.share_location_format,
                                        v.displayName,
                                        formatSize(v.sizeFreeByte),
                                        v.fsType
                                    )
                                } ?: "",
                                onValueChange = {},
                                label = { Text(stringResource(R.string.share_location)) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showVolumePicker = true },
                                enabled = false,
                                singleLine = true,
                                trailingIcon = {
                                    IconButton(onClick = { showVolumePicker = true }) {
                                        Icon(Icons.Filled.ArrowDropDown, contentDescription = null)
                                    }
                                }
                            )
                        }

                        // 隐藏选项
                        item {
                            SettingSwitchItem(
                                title = stringResource(R.string.share_hidden),
                                checked = state.hidden,
                                onCheckedChange = { viewModel.sendIntent(AddShareFolderIntent.ToggleHidden) }
                            )
                        }

                        item {
                            SettingSwitchItem(
                                title = stringResource(R.string.share_hide_unreadable),
                                checked = state.hideUnreadable,
                                onCheckedChange = { viewModel.sendIntent(AddShareFolderIntent.ToggleHideUnreadable) }
                            )
                        }

                        // 回收站
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(Spacing.Medium)
                            ) {
                                SettingSwitchItem(
                                    title = stringResource(R.string.share_enable_recycle_bin),
                                    checked = state.enableRecycleBin,
                                    onCheckedChange = { viewModel.sendIntent(AddShareFolderIntent.ToggleRecycleBin) },
                                    modifier = Modifier.weight(1f)
                                )
                                SettingSwitchItem(
                                    title = stringResource(R.string.share_recycle_bin_admin_only),
                                    checked = state.recycleBinAdminOnly,
                                    enabled = state.enableRecycleBin,
                                    onCheckedChange = { viewModel.sendIntent(AddShareFolderIntent.ToggleRecycleBinAdminOnly) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        // 加密
                        item {
                            SettingSwitchItem(
                                title = stringResource(R.string.share_encryption),
                                checked = state.encryption,
                                onCheckedChange = { viewModel.sendIntent(AddShareFolderIntent.ToggleEncryption) }
                            )
                        }

                        // 加密密钥
                        if (state.encryption) {
                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(Spacing.Medium)
                                ) {
                                    OutlinedTextField(
                                        value = state.password,
                                        onValueChange = { viewModel.sendIntent(AddShareFolderIntent.UpdatePassword(it)) },
                                        label = { Text(stringResource(R.string.share_encryption_key)) },
                                        modifier = Modifier.weight(1f),
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
                                    OutlinedTextField(
                                        value = state.confirmPassword,
                                        onValueChange = { viewModel.sendIntent(AddShareFolderIntent.UpdateConfirmPassword(it)) },
                                        label = { Text(stringResource(R.string.share_confirm_key)) },
                                        modifier = Modifier.weight(1f),
                                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                        isError = state.password.isNotEmpty() && state.confirmPassword.isNotEmpty() && state.password != state.confirmPassword,
                                        singleLine = true
                                    )
                                }
                            }
                        }

                        // Btrfs 特性
                        val isBtrfs = state.volumes.getOrNull(state.selectedVolumeIndex)?.fsType == "btrfs"
                        
                        item {
                            SettingSwitchItem(
                                title = stringResource(R.string.share_enable_cow),
                                subtitle = if (!isBtrfs) stringResource(R.string.share_btrfs_only) else null,
                                checked = state.enableShareCow,
                                enabled = isBtrfs,
                                onCheckedChange = { viewModel.sendIntent(AddShareFolderIntent.ToggleShareCow) }
                            )
                        }

                        item {
                            SettingSwitchItem(
                                title = stringResource(R.string.share_enable_compress),
                                subtitle = if (!isBtrfs) stringResource(R.string.share_btrfs_only) else null,
                                checked = state.enableShareCompress,
                                enabled = isBtrfs,
                                onCheckedChange = { viewModel.sendIntent(AddShareFolderIntent.ToggleShareCompress) }
                            )
                        }

                        item {
                            val subtitle = if (state.shareQuotaUsed != null) {
                                stringResource(R.string.share_quota_used, formatSize(state.shareQuotaUsed!! * 1024 * 1024))
                            } else if (!isBtrfs) {
                                stringResource(R.string.share_btrfs_only)
                            } else null
                            
                            SettingSwitchItem(
                                title = stringResource(R.string.share_enable_quota),
                                subtitle = subtitle,
                                checked = state.enableShareQuota,
                                enabled = isBtrfs,
                                onCheckedChange = { viewModel.sendIntent(AddShareFolderIntent.ToggleShareQuota) }
                            )
                        }

                        // 配额设置
                        if (state.enableShareQuota) {
                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(Spacing.Medium),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedTextField(
                                        value = state.shareQuota,
                                        onValueChange = { viewModel.sendIntent(AddShareFolderIntent.UpdateShareQuota(it)) },
                                        label = { Text(stringResource(R.string.share_quota)) },
                                        modifier = Modifier.weight(2f),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        singleLine = true
                                    )
                                    OutlinedTextField(
                                        value = AddShareFolderViewModel.QUOTA_UNITS[state.quotaUnitIndex],
                                        onValueChange = {},
                                        label = { Text(stringResource(R.string.share_quota_unit)) },
                                        modifier = Modifier.weight(1f),
                                        enabled = false,
                                        trailingIcon = {
                                            IconButton(onClick = { showQuotaUnitPicker = true }) {
                                                Icon(Icons.Filled.ArrowDropDown, contentDescription = null)
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // 保存按钮
                    Button(
                        onClick = { viewModel.sendIntent(AddShareFolderIntent.Save) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Spacing.PageHorizontal),
                        enabled = !state.isSaving
                    ) {
                        if (state.isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text(stringResource(R.string.common_save))
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingSwitchItem(
    title: String,
    subtitle: String? = null,
    checked: Boolean,
    enabled: Boolean = true,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = AppShapes.Card,
        colors = CardDefaults.cardColors(
            containerColor = if (!enabled) {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            } else if (checked) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            } else {
                MaterialTheme.cardSurface
            }
        ),
        onClick = { if (enabled) onCheckedChange(!checked) }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.CardPadding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Switch(
                checked = checked,
                onCheckedChange = { if (enabled) onCheckedChange(it) },
                enabled = enabled
            )
        }
    }
}

@Composable
private fun VolumePickerDialog(
    volumes: List<VolumeInfo>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.share_select_volume)) },
        text = {
            LazyColumn {
                items(volumes.indices.toList()) { index ->
                    val volume = volumes[index]
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (index == selectedIndex) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.surfaceContainerHigh
                            }
                        ),
                        onClick = { onSelect(index) }
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = stringResource(
                                    R.string.share_location_format,
                                    volume.displayName,
                                    formatSize(volume.sizeFreeByte),
                                    volume.fsType
                                ),
                                style = MaterialTheme.typography.bodyLarge
                            )
                            if (volume.description.isNotEmpty()) {
                                Text(
                                    text = volume.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.common_cancel))
            }
        }
    )
}

@Composable
private fun QuotaUnitPickerDialog(
    units: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.share_quota_unit)) },
        text = {
            Column {
                units.forEachIndexed { index, unit ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(index) }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = index == selectedIndex,
                            onClick = { onSelect(index) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = unit, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.common_cancel))
            }
        }
    )
}
