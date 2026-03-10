package wang.zengye.dsm.ui.docker

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import wang.zengye.dsm.R

/**
 * 网络卡片
 */
@Composable
internal fun NetworkCard(
    network: DockerNetwork,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }

    // 删除确认对话框
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.docker_network_delete_title)) },
            text = { Text(stringResource(R.string.docker_network_delete_confirm, network.name)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onDelete()
                    }
                ) {
                    Text(stringResource(R.string.common_delete), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(R.string.common_cancel))
                }
            }
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // 标题行
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Hub,
                        contentDescription = null,
                        tint = when (network.driver) {
                            "bridge" -> MaterialTheme.colorScheme.primary
                            "host" -> MaterialTheme.colorScheme.tertiary
                            "none" -> MaterialTheme.colorScheme.onSurfaceVariant
                            else -> MaterialTheme.colorScheme.secondary
                        }
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = network.name,
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = network.id.take(12),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = network.driver,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                // 更多操作
                IconButton(onClick = { showDeleteDialog = true }) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = stringResource(R.string.common_delete),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }

            // 详细信息
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                if (network.subnet.isNotEmpty()) {
                    Column {
                        Text(
                            text = stringResource(R.string.docker_network_subnet),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = network.subnet,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                if (network.gateway.isNotEmpty()) {
                    Column {
                        Text(
                            text = stringResource(R.string.docker_network_gateway),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = network.gateway,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                Column {
                    Text(
                        text = stringResource(R.string.docker_network_scope),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = network.scope,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            // 标签
            if (network.internal || network.attachable || network.ipv6Enabled) {
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (network.internal) {
                        AssistChip(
                            onClick = {},
                            label = { Text(stringResource(R.string.docker_network_internal), style = MaterialTheme.typography.labelSmall) },
                            modifier = Modifier.height(24.dp)
                        )
                    }
                    if (network.attachable) {
                        AssistChip(
                            onClick = {},
                            label = { Text(stringResource(R.string.docker_network_attachable), style = MaterialTheme.typography.labelSmall) },
                            modifier = Modifier.height(24.dp)
                        )
                    }
                    if (network.ipv6Enabled) {
                        AssistChip(
                            onClick = {},
                            label = { Text(stringResource(R.string.docker_network_ipv6), style = MaterialTheme.typography.labelSmall) },
                            modifier = Modifier.height(24.dp)
                        )
                    }
                }
            }

            // 连接的容器
            if (network.containers.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.docker_network_connected_containers_label, network.containers.size),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    IconButton(
                        onClick = { expanded = !expanded },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = if (expanded) stringResource(R.string.file_collapse) else stringResource(R.string.file_expand)
                        )
                    }
                }

                if (expanded) {
                    Spacer(modifier = Modifier.height(8.dp))

                    network.containers.forEach { container ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = container.name,
                                style = MaterialTheme.typography.bodySmall
                            )
                            if (container.macAddress.isNotEmpty()) {
                                Text(
                                    text = container.macAddress,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * 创建网络对话框
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CreateNetworkDialog(
    isCreating: Boolean,
    error: String?,
    onDismiss: () -> Unit,
    onCreate: (name: String, driver: String, subnet: String, gateway: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var driver by remember { mutableStateOf("bridge") }
    var subnet by remember { mutableStateOf("") }
    var gateway by remember { mutableStateOf("") }
    var showDriverDropdown by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.docker_create_network)) },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.docker_network_name_required)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 驱动选择
                ExposedDropdownMenuBox(
                    expanded = showDriverDropdown,
                    onExpandedChange = { showDriverDropdown = it }
                ) {
                    OutlinedTextField(
                        value = driver,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.docker_network_driver_label)) },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = showDriverDropdown)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = showDriverDropdown,
                        onDismissRequest = { showDriverDropdown = false }
                    ) {
                        listOf("bridge", "host", "overlay", "macvlan", "none").forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    driver = option
                                    showDriverDropdown = false
                                }
                            )
                        }
                    }
                }

                if (driver == "bridge" || driver == "overlay" || driver == "macvlan") {
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = subnet,
                        onValueChange = { subnet = it },
                        label = { Text(stringResource(R.string.docker_network_subnet_label)) },
                        placeholder = { Text(stringResource(R.string.docker_network_subnet_placeholder)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = gateway,
                        onValueChange = { gateway = it },
                        label = { Text(stringResource(R.string.docker_network_gateway_label)) },
                        placeholder = { Text(stringResource(R.string.docker_network_gateway_placeholder)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                if (error != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = error,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onCreate(name, driver, subnet, gateway) },
                enabled = !isCreating && name.isNotEmpty()
            ) {
                if (isCreating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(stringResource(R.string.common_create))
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isCreating) {
                Text(stringResource(R.string.common_cancel))
            }
        }
    )
}
