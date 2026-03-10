package wang.zengye.dsm.ui.control_panel

import android.widget.Toast
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import wang.zengye.dsm.R
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import wang.zengye.dsm.util.formatSize

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExternalDevicesScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: ExternalDevicesViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showEjectDialog by remember { mutableStateOf<ExternalDevice?>(null) }

    // 处理事件
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is ExternalDevicesEvent.ShowError -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
                is ExternalDevicesEvent.EjectSuccess -> {
                    Toast.makeText(context, context.getString(R.string.external_eject), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.external_title)) },
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
                    IconButton(onClick = { viewModel.sendIntent(ExternalDevicesIntent.LoadDevices) }) {
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
                        text = state.error ?: stringResource(R.string.components_load_failed),
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { viewModel.sendIntent(ExternalDevicesIntent.LoadDevices) }) {
                        Text(stringResource(R.string.common_retry))
                    }
                }
            }

            state.devices.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Usb,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = stringResource(R.string.external_no_devices),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.external_connect_hint),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // USB 设备
                    val usbDevices = state.devices.filter { it.isUsb }
                    if (usbDevices.isNotEmpty()) {
                        item {
                            Text(
                                text = stringResource(R.string.external_usb_devices),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        items(usbDevices, key = { it.id }) { device ->
                            DeviceCard(
                                device = device,
                                onEject = { showEjectDialog = device }
                            )
                        }
                    }

                    // eSATA 设备
                    val esataDevices = state.devices.filter { !it.isUsb }
                    if (esataDevices.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = stringResource(R.string.external_esata_devices),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        items(esataDevices, key = { it.id }) { device ->
                            DeviceCard(
                                device = device,
                                onEject = { showEjectDialog = device }
                            )
                        }
                    }
                }
            }
        }
    }

    // 弹出确认对话框
    showEjectDialog?.let { device ->
        AlertDialog(
            onDismissRequest = { showEjectDialog = null },
            title = { Text(stringResource(R.string.external_eject_title)) },
            text = { Text(stringResource(R.string.external_eject_confirm, device.name)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.sendIntent(ExternalDevicesIntent.EjectDevice(device.id))
                        showEjectDialog = null
                    }
                ) {
                    Text(stringResource(R.string.external_eject))
                }
            },
            dismissButton = {
                TextButton(onClick = { showEjectDialog = null }) {
                    Text(stringResource(R.string.common_cancel))
                }
            }
        )
    }
}

@Composable
private fun DeviceCard(
    device: ExternalDevice,
    onEject: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = when (device.status.lowercase()) {
                            "mounted", "ready" -> MaterialTheme.colorScheme.primaryContainer
                            "error" -> MaterialTheme.colorScheme.errorContainer
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        },
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Box(
                            modifier = Modifier.size(48.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (device.isUsb) Icons.Default.Usb else Icons.Default.Storage,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Column {
                        Text(
                            text = device.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        if (device.vendor.isNotEmpty() || device.model.isNotEmpty()) {
                            Text(
                                text = listOf(device.vendor, device.model).filter { it.isNotEmpty() }.joinToString(" "),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                
                Surface(
                    color = when (device.status.lowercase()) {
                        "mounted", "ready" -> MaterialTheme.colorScheme.primaryContainer
                        "error" -> MaterialTheme.colorScheme.errorContainer
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    },
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = when (device.status.lowercase()) {
                            "mounted" -> stringResource(R.string.external_status_mounted)
                            "ready" -> stringResource(R.string.external_status_ready)
                            "error" -> stringResource(R.string.external_status_error)
                            "unmounted" -> stringResource(R.string.external_status_unmounted)
                            else -> device.status
                        },
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
            
            // 设备详情
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.external_capacity),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formatSize(device.size),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                
                if (device.mountPoint.isNotEmpty()) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.external_mount_point),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = device.mountPoint,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
            
            // 操作按钮
            if (device.isEjectable) {
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    FilledTonalButton(onClick = onEject) {
                        Icon(Icons.Default.Eject, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource(R.string.external_eject))
                    }
                }
            }
        }
    }
}
