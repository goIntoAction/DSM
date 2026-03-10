package wang.zengye.dsm.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import wang.zengye.dsm.R
import wang.zengye.dsm.util.SettingsManager
import wang.zengye.dsm.ui.theme.DarkMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreferencesScreen(
    onBack: () -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    var darkMode by remember { mutableStateOf(DarkMode.SYSTEM) }
    var launchAuth by remember { mutableStateOf(false) }
    var refreshDuration by remember { mutableIntStateOf(10) }
    var downloadWifiOnly by remember { mutableStateOf(true) }
    var checkSsl by remember { mutableStateOf(true) }
    var vibrateOn by remember { mutableStateOf(true) }

    // 加载设置
    LaunchedEffect(Unit) {
        scope.launch {
            darkMode = SettingsManager.darkMode.first()
            launchAuth = SettingsManager.launchAuth.first()
            refreshDuration = SettingsManager.refreshDuration.first()
            downloadWifiOnly = SettingsManager.downloadWifiOnly.first()
            checkSsl = SettingsManager.checkSsl.first()
            vibrateOn = SettingsManager.vibrateOn.first()
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.preferences_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.common_back))
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 外观设置
            item {
                Text(
                    text = stringResource(R.string.preferences_section_appearance),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.preferences_dark_mode),
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = when (darkMode) {
                                    DarkMode.LIGHT -> stringResource(R.string.preferences_dark_mode_light)
                                    DarkMode.DARK -> stringResource(R.string.preferences_dark_mode_dark)
                                    DarkMode.SYSTEM -> stringResource(R.string.preferences_dark_mode_system)
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        var expanded by remember { mutableStateOf(false) }
                        Box {
                            TextButton(onClick = { expanded = true }) {
                                Text(stringResource(R.string.preferences_select))
                                Icon(Icons.Filled.ArrowDropDown, contentDescription = null)
                            }
                            DropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.preferences_dark_mode_light)) },
                                    onClick = {
                                        scope.launch { SettingsManager.setDarkMode(DarkMode.LIGHT) }
                                        darkMode = DarkMode.LIGHT
                                        expanded = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.preferences_dark_mode_dark)) },
                                    onClick = {
                                        scope.launch { SettingsManager.setDarkMode(DarkMode.DARK) }
                                        darkMode = DarkMode.DARK
                                        expanded = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.preferences_dark_mode_system)) },
                                    onClick = {
                                        scope.launch { SettingsManager.setDarkMode(DarkMode.SYSTEM) }
                                        darkMode = DarkMode.SYSTEM
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // 安全设置
            item {
                Text(
                    text = stringResource(R.string.preferences_section_security),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.preferences_launch_auth),
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = stringResource(R.string.preferences_launch_auth_subtitle),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = launchAuth,
                            onCheckedChange = { value ->
                                scope.launch { SettingsManager.setLaunchAuth(value) }
                                launchAuth = value
                            }
                        )
                    }
                }
            }

            // 同步设置
            item {
                Text(
                    text = stringResource(R.string.preferences_section_sync),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = stringResource(R.string.preferences_refresh_interval),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Text(
                                    text = stringResource(R.string.preferences_refresh_seconds, refreshDuration),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = {
                                        if (refreshDuration > 5) {
                                            refreshDuration -= 5
                                            scope.launch { SettingsManager.setRefreshDuration(refreshDuration) }
                                        }
                                    }
                                ) {
                                    Icon(Icons.Filled.Remove, contentDescription = stringResource(R.string.preferences_decrease))
                                }
                                Text("$refreshDuration")
                                IconButton(
                                    onClick = {
                                        if (refreshDuration < 60) {
                                            refreshDuration += 5
                                            scope.launch { SettingsManager.setRefreshDuration(refreshDuration) }
                                        }
                                    }
                                ) {
                                    Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.preferences_increase))
                                }
                            }
                        }
                    }
                }
            }

            // 下载设置
            item {
                Text(
                    text = stringResource(R.string.preferences_section_download),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = stringResource(R.string.preferences_wifi_only_download),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Text(
                                    text = stringResource(R.string.preferences_wifi_only_subtitle),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = downloadWifiOnly,
                                onCheckedChange = { value ->
                                    scope.launch { SettingsManager.setDownloadWifiOnly(value) }
                                    downloadWifiOnly = value
                                }
                            )
                        }
                    }
                }
            }

            // 连接设置
            item {
                Text(
                    text = stringResource(R.string.preferences_section_connection),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = stringResource(R.string.preferences_verify_ssl),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Text(
                                    text = stringResource(R.string.preferences_verify_ssl_subtitle),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = checkSsl,
                                onCheckedChange = { value ->
                                    scope.launch { SettingsManager.setCheckSsl(value) }
                                    checkSsl = value
                                }
                            )
                        }
                    }
                }
            }

            // 通知设置
            item {
                Text(
                    text = stringResource(R.string.preferences_section_notification),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = stringResource(R.string.preferences_vibration_alert),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Text(
                                    text = stringResource(R.string.preferences_vibration_subtitle),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = vibrateOn,
                                onCheckedChange = { value ->
                                    scope.launch { SettingsManager.setVibrateOn(value) }
                                    vibrateOn = value
                                }
                            )
                        }
                    }
                }
            }

            // 关于
            item {
                Text(
                    text = stringResource(R.string.preferences_section_about),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = stringResource(R.string.common_version),
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = "1.0.0",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = stringResource(R.string.common_build),
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = "Debug",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}
