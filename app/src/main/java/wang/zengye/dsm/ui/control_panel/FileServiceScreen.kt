package wang.zengye.dsm.ui.control_panel

import android.widget.Toast
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import wang.zengye.dsm.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileServiceScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: FileServiceViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("SMB/AFP/NFS", "FTP/SFTP")

    // 处理事件
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is FileServiceEvent.ShowError -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
                is FileServiceEvent.ToggleSuccess -> {
                    Toast.makeText(context, context.getString(R.string.common_save_success), Toast.LENGTH_SHORT).show()
                }
                is FileServiceEvent.SaveSuccess -> {
                    Toast.makeText(context, context.getString(R.string.common_save_success), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.file_service_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.common_back))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                actions = {
                    IconButton(onClick = { viewModel.sendIntent(FileServiceIntent.LoadServices) }) {
                        Icon(Icons.Default.Refresh, contentDescription = stringResource(R.string.dashboard_refresh))
                    }
                }
            )
        },
        bottomBar = {
            BottomAppBar(
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Spacer(modifier = Modifier.weight(1f))
                Button(
                    onClick = { viewModel.sendIntent(FileServiceIntent.SaveAll) },
                    enabled = !state.isSaving
                ) {
                    if (state.isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(stringResource(R.string.common_save))
                }
                Spacer(modifier = Modifier.width(16.dp))
            }
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
                    Button(onClick = { viewModel.sendIntent(FileServiceIntent.LoadServices) }) {
                        Text(stringResource(R.string.common_retry))
                    }
                }
            }

            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    // Tab 栏
                    TabRow(
                        selectedTabIndex = selectedTabIndex,
                        containerColor = MaterialTheme.colorScheme.surface
                    ) {
                        tabs.forEachIndexed { index, title ->
                            Tab(
                                selected = selectedTabIndex == index,
                                onClick = { selectedTabIndex = index },
                                text = { Text(title) }
                            )
                        }
                    }

                    // Tab 内容
                    when (selectedTabIndex) {
                        0 -> SmbAfpNfsTab(state, viewModel)
                        1 -> FtpSftpTab(state, viewModel)
                    }
                }
            }
        }
    }
}

@Composable
private fun SmbAfpNfsTab(
    state: FileServiceState,
    viewModel: FileServiceViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // SMB 配置
        ServiceCard(title = "SMB") {
            ConfigSwitch(
                title = stringResource(R.string.smb_enable),
                checked = state.smb.enableSamba,
                onCheckedChange = { viewModel.sendIntent(FileServiceIntent.UpdateSmbEnabled(it)) }
            )
            
            if (state.smb.enableSamba) {
                Spacer(modifier = Modifier.height(12.dp))
                
                ConfigTextField(
                    title = stringResource(R.string.smb_workgroup),
                    value = state.smb.workgroup,
                    onValueChange = { viewModel.sendIntent(FileServiceIntent.UpdateSmbWorkgroup(it)) }
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                ConfigSwitch(
                    title = stringResource(R.string.smb_disable_shadow_copy),
                    checked = state.smb.disableShadowCopy,
                    onCheckedChange = { viewModel.sendIntent(FileServiceIntent.UpdateSmbShadowCopy(it)) }
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                ConfigSwitch(
                    title = stringResource(R.string.smb_transfer_log),
                    checked = state.syslogClient.cifs,
                    onCheckedChange = { viewModel.sendIntent(FileServiceIntent.UpdateSmbTransferLog(it)) }
                )
            }
        }

        // AFP 配置
        ServiceCard(title = "AFP") {
            ConfigSwitch(
                title = stringResource(R.string.afp_enable),
                checked = state.afp.enableAfp,
                onCheckedChange = { viewModel.sendIntent(FileServiceIntent.UpdateAfpEnabled(it)) }
            )
            
            if (state.afp.enableAfp) {
                Spacer(modifier = Modifier.height(12.dp))
                
                ConfigSwitch(
                    title = stringResource(R.string.afp_transfer_log),
                    checked = state.syslogClient.afp,
                    onCheckedChange = { viewModel.sendIntent(FileServiceIntent.UpdateAfpTransferLog(it)) }
                )
            }
        }

        // NFS 配置
        ServiceCard(title = "NFS") {
            ConfigSwitch(
                title = stringResource(R.string.nfs_enable),
                checked = state.nfs.enableNfs,
                onCheckedChange = { viewModel.sendIntent(FileServiceIntent.UpdateNfsEnabled(it)) }
            )
            
            if (state.nfs.enableNfs) {
                Spacer(modifier = Modifier.height(12.dp))
                
                ConfigSwitch(
                    title = stringResource(R.string.nfs_v4_enable),
                    checked = state.nfs.enableNfsV4,
                    onCheckedChange = { viewModel.sendIntent(FileServiceIntent.UpdateNfsV4Enabled(it)) }
                )
                
                if (state.nfs.enableNfsV4) {
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    ConfigTextField(
                        title = stringResource(R.string.nfs_v4_domain),
                        value = state.nfs.nfsV4Domain,
                        onValueChange = { viewModel.sendIntent(FileServiceIntent.UpdateNfsV4Domain(it)) }
                    )
                }
            }
        }
    }
}

@Composable
private fun FtpSftpTab(
    state: FileServiceState,
    viewModel: FileServiceViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // FTP/FTPS 配置
        ServiceCard(title = "FTP/FTPS") {
            ConfigSwitch(
                title = stringResource(R.string.ftp_enable),
                checked = state.ftp.enableFtp,
                onCheckedChange = { viewModel.sendIntent(FileServiceIntent.UpdateFtpEnabled(it)) }
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            ConfigSwitch(
                title = stringResource(R.string.ftps_enable),
                checked = state.ftp.enableFtps,
                onCheckedChange = { viewModel.sendIntent(FileServiceIntent.UpdateFtpsEnabled(it)) }
            )
            
            if (state.ftp.enableFtps) {
                Spacer(modifier = Modifier.height(12.dp))
                
                ConfigTextField(
                    title = stringResource(R.string.ftp_timeout),
                    value = state.ftp.timeout.toString(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    onValueChange = { 
                        it.toIntOrNull()?.let { timeout -> 
                            viewModel.sendIntent(FileServiceIntent.UpdateFtpTimeout(timeout.coerceIn(1, 7200)))
                        }
                    }
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                ConfigTextField(
                    title = stringResource(R.string.ftp_port),
                    value = state.ftp.portnum.toString(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    onValueChange = { 
                        it.toIntOrNull()?.let { port -> 
                            viewModel.sendIntent(FileServiceIntent.UpdateFtpPort(port.coerceIn(1, 65535)))
                        }
                    }
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                ConfigSwitch(
                    title = stringResource(R.string.ftp_fxp),
                    checked = state.ftp.enableFxp,
                    onCheckedChange = { viewModel.sendIntent(FileServiceIntent.UpdateFtpFxp(it)) }
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                ConfigSwitch(
                    title = stringResource(R.string.ftp_fips),
                    checked = state.ftp.enableFips,
                    onCheckedChange = { viewModel.sendIntent(FileServiceIntent.UpdateFtpFips(it)) }
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                ConfigSwitch(
                    title = stringResource(R.string.ftp_ascii),
                    checked = state.ftp.enableAscii,
                    onCheckedChange = { viewModel.sendIntent(FileServiceIntent.UpdateFtpAscii(it)) }
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Utf8ModeSelector(
                    selectedMode = state.ftp.utf8Mode,
                    onModeSelected = { viewModel.sendIntent(FileServiceIntent.UpdateFtpUtf8Mode(it)) }
                )
            }
        }

        // SFTP 配置
        ServiceCard(title = "SFTP") {
            ConfigSwitch(
                title = stringResource(R.string.sftp_enable),
                checked = state.sftp.enable,
                onCheckedChange = { viewModel.sendIntent(FileServiceIntent.UpdateSftpEnabled(it)) }
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            ConfigTextField(
                title = stringResource(R.string.sftp_port),
                value = state.sftp.portnum.toString(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                onValueChange = { 
                    it.toIntOrNull()?.let { port -> 
                        viewModel.sendIntent(FileServiceIntent.UpdateSftpPort(port.coerceIn(1, 65535)))
                    }
                }
            )
        }
    }
}

@Composable
private fun ServiceCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            content()
        }
    }
}

@Composable
private fun ConfigSwitch(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
private fun ConfigTextField(
    title: String,
    value: String,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(title) },
        keyboardOptions = keyboardOptions,
        modifier = Modifier.fillMaxWidth(),
        singleLine = true
    )
}

@Composable
private fun Utf8ModeSelector(
    selectedMode: Int,
    onModeSelected: (Int) -> Unit
) {
    val modes = listOf(
        0 to stringResource(R.string.utf8_disabled),
        1 to stringResource(R.string.utf8_auto),
        2 to stringResource(R.string.utf8_forced)
    )
    
    Column {
        Text(
            text = stringResource(R.string.ftp_utf8),
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(modifier = Modifier.height(8.dp))
        modes.forEach { (mode, label) ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = selectedMode == mode,
                    onClick = { onModeSelected(mode) }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = label)
            }
        }
    }
}