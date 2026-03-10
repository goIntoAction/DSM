package wang.zengye.dsm.ui.control_panel
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import android.widget.Toast
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
fun PowerScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: PowerViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showShutdownDialog by remember { mutableStateOf(false) }
    var showRebootDialog by remember { mutableStateOf(false) }

    // 处理事件
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is PowerEvent.ShowError -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
                is PowerEvent.ShutdownSuccess -> {
                    Toast.makeText(context, context.getString(R.string.power_shutdown_type), Toast.LENGTH_SHORT).show()
                }
                is PowerEvent.RebootSuccess -> {
                    Toast.makeText(context, context.getString(R.string.power_confirm_reboot), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.power_title)) },
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
                    IconButton(onClick = { viewModel.sendIntent(PowerIntent.LoadPowerSettings) }) {
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
                    Button(onClick = { viewModel.sendIntent(PowerIntent.LoadPowerSettings) }) {
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
                    // 电源控制
                    item {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = stringResource(R.string.power_control),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = { showRebootDialog = true },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Default.RestartAlt, contentDescription = null)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(stringResource(R.string.common_restart))
                                    }

                                    OutlinedButton(
                                        onClick = { showShutdownDialog = true },
                                        modifier = Modifier.weight(1f),
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            contentColor = MaterialTheme.colorScheme.error
                                        )
                                    ) {
                                        Icon(Icons.Default.PowerSettingsNew, contentDescription = null)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(stringResource(R.string.power_shutdown_type))
                                    }
                                }
                            }
                        }
                    }

                    // 电源选项
                    item {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = stringResource(R.string.power_options),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(stringResource(R.string.power_auto_power_on))
                                        Text(
                                            text = stringResource(R.string.power_auto_power_on_desc),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Switch(
                                        checked = state.autoPowerOn,
                                        onCheckedChange = { viewModel.sendIntent(PowerIntent.SetAutoPowerOn(it)) }
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(stringResource(R.string.power_beep_on_alert_label))
                                        Text(
                                            text = stringResource(R.string.power_beep_on_alert_desc),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Switch(
                                        checked = state.beepOnAlert,
                                        onCheckedChange = { viewModel.sendIntent(PowerIntent.SetBeepOnAlert(it)) }
                                    )
                                }
                            }
                        }
                    }

                    // UPS 信息
                    if (state.upsInfo != null) {
                        item {
                            Card(modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = stringResource(R.string.power_ups_title),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )

                                    Spacer(modifier = Modifier.height(16.dp))

                                    val ups = state.upsInfo!!

                                    if (!ups.connected) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                Icons.Default.OfflineBolt,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = stringResource(R.string.power_ups_not_connected_device),
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    } else {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(stringResource(R.string.power_ups_model))
                                            Text(ups.model.ifEmpty { stringResource(R.string.common_unknown) })
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(stringResource(R.string.power_ups_status))
                                            Surface(
                                                color = when (ups.status.lowercase()) {
                                                    "online" -> MaterialTheme.colorScheme.primaryContainer
                                                    "onbattery" -> MaterialTheme.colorScheme.tertiaryContainer
                                                    else -> MaterialTheme.colorScheme.surfaceVariant
                                                },
                                                shape = MaterialTheme.shapes.extraSmall
                                            ) {
                                                Text(
                                                    text = when (ups.status.lowercase()) {
                                                        "online" -> stringResource(R.string.power_ups_online)
                                                        "onbattery" -> stringResource(R.string.power_ups_on_battery)
                                                        else -> ups.status
                                                    },
                                                    style = MaterialTheme.typography.labelSmall,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        // 电池电量
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(stringResource(R.string.power_ups_battery))
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                LinearProgressIndicator(
                                                    progress = { ups.batteryCharge / 100f },
                                                    modifier = Modifier.width(80.dp),
                                                    color = when {
                                                        ups.batteryCharge < 20 -> MaterialTheme.colorScheme.error
                                                        ups.batteryCharge < 50 -> MaterialTheme.colorScheme.tertiary
                                                        else -> MaterialTheme.colorScheme.primary
                                                    },
                                                    strokeCap = StrokeCap.Butt,
                                                    gapSize = 0.dp,
                                                    drawStopIndicator = {}
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text("${ups.batteryCharge}%")
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        // 负载
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(stringResource(R.string.power_ups_load))
                                            Text("${ups.load}%")
                                        }

                                        if (ups.runtime > 0) {
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(stringResource(R.string.power_ups_estimated_runtime))
                                                Text(stringResource(R.string.power_ups_runtime_format, ups.runtime))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 电源计划
                    if (state.schedules.isNotEmpty()) {
                        item {
                            Text(
                                text = stringResource(R.string.power_schedule),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        items(state.schedules, key = { it.id }) { schedule ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (schedule.enabled) {
                                        MaterialTheme.colorScheme.surface
                                    } else {
                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                    }
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = if (schedule.type == "boot") Icons.Default.PowerSettingsNew else Icons.Default.PowerOff,
                                            contentDescription = null,
                                            tint = if (schedule.enabled) {
                                                MaterialTheme.colorScheme.primary
                                            } else {
                                                MaterialTheme.colorScheme.onSurfaceVariant
                                            }
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = if (schedule.type == "boot") stringResource(R.string.power_schedule_boot) else stringResource(R.string.power_schedule_shutdown),
                                                fontWeight = FontWeight.Medium
                                            )
                                            Text(
                                                text = String.format("%02d:%02d", schedule.hour, schedule.minute),
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    Surface(
                                        color = if (schedule.enabled) {
                                            MaterialTheme.colorScheme.primaryContainer
                                        } else {
                                            MaterialTheme.colorScheme.surfaceVariant
                                        },
                                        shape = MaterialTheme.shapes.small
                                    ) {
                                        Text(
                                            text = if (schedule.enabled) stringResource(R.string.common_enabled) else stringResource(R.string.common_disabled),
                                            style = MaterialTheme.typography.labelSmall,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // 关机确认对话框
    if (showShutdownDialog) {
        AlertDialog(
            onDismissRequest = { showShutdownDialog = false },
            title = { Text(stringResource(R.string.power_confirm_shutdown)) },
            text = { Text(stringResource(R.string.power_confirm_shutdown_message_full)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.sendIntent(PowerIntent.Shutdown)
                        showShutdownDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(stringResource(R.string.power_shutdown_type))
                }
            },
            dismissButton = {
                TextButton(onClick = { showShutdownDialog = false }) {
                    Text(stringResource(R.string.common_cancel))
                }
            }
        )
    }

    // 重启确认对话框
    if (showRebootDialog) {
        AlertDialog(
            onDismissRequest = { showRebootDialog = false },
            title = { Text(stringResource(R.string.power_confirm_reboot)) },
            text = { Text(stringResource(R.string.power_confirm_reboot_message)) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.sendIntent(PowerIntent.Reboot)
                    showRebootDialog = false
                }) {
                    Text(stringResource(R.string.common_restart))
                }
            },
            dismissButton = {
                TextButton(onClick = { showRebootDialog = false }) {
                    Text(stringResource(R.string.common_cancel))
                }
            }
        )
    }
}
