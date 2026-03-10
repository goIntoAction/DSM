package wang.zengye.dsm.ui.setting

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import wang.zengye.dsm.R
import wang.zengye.dsm.ui.components.*
import wang.zengye.dsm.ui.components.ElevatedCard
import wang.zengye.dsm.ui.theme.*
import wang.zengye.dsm.util.SettingsManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingScreen(
    onNavigateToAbout: () -> Unit,
    onNavigateToFeedback: () -> Unit,
    onNavigateToPreferences: () -> Unit = {},
    onNavigateToDocker: () -> Unit = {},
    onNavigateToPackages: () -> Unit = {},
    onNavigateToVirtualMachine: () -> Unit = {},
    onNavigateToSmartTest: () -> Unit = {},
    onNavigateToBackup: () -> Unit = {},
    onNavigateToOpenSource: () -> Unit = {},
    onLogout: () -> Unit,
    viewModel: SettingViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val darkMode by SettingsManager.darkMode.collectAsState(initial = DarkMode.SYSTEM)

    // 处理事件
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is SettingEvent.LogoutSuccess -> {
                    onLogout()
                }
            }
        }
    }
    
    Scaffold(
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = stringResource(R.string.setting_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Medium
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = Color.Transparent
                ),
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // 外观设置
            SectionHeader(title = stringResource(R.string.setting_section_appearance))
            
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.PageHorizontal)
            ) {
                Column {
                    // 深色模式
                    var showThemeDialog by remember { mutableStateOf(false) }
                    
                    SettingsRow(
                        icon = Icons.Outlined.DarkMode,
                        title = stringResource(R.string.setting_dark_mode),
                        onClick = { showThemeDialog = true }
                    ) {
                        Text(
                            text = when (darkMode) {
                                DarkMode.LIGHT -> stringResource(R.string.setting_dark_mode_light)
                                DarkMode.DARK -> stringResource(R.string.setting_dark_mode_dark)
                                DarkMode.SYSTEM -> stringResource(R.string.setting_dark_mode_system)
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Icon(
                            imageVector = Icons.Filled.ChevronRight,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    }
                    
                    if (showThemeDialog) {
                        AlertDialog(
                            onDismissRequest = { showThemeDialog = false },
                            icon = {
                                Icon(
                                    imageVector = Icons.Outlined.DarkMode,
                                    contentDescription = null
                                )
                            },
                            title = { 
                                Text(
                                    text = stringResource(R.string.setting_choose_theme),
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Medium
                                )
                            },
                            text = {
                                Column {
                                    DarkMode.entries.forEach { mode ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    viewModel.sendIntent(SettingIntent.SetDarkMode(mode))
                                                    showThemeDialog = false
                                                }
                                                .padding(vertical = Spacing.Standard),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                                    RadioButton(
                                                        selected = darkMode == mode,
                                                        onClick = {
                                                            viewModel.sendIntent(SettingIntent.SetDarkMode(mode))
                                                            showThemeDialog = false
                                                        }
                                                    )
                                            Spacer(modifier = Modifier.width(Spacing.Standard))
                                            Text(
                                                text = when (mode) {
                                                    DarkMode.LIGHT -> stringResource(R.string.setting_dark_mode_light)
                                                    DarkMode.DARK -> stringResource(R.string.setting_dark_mode_dark)
                                                    DarkMode.SYSTEM -> stringResource(R.string.setting_dark_mode_system)
                                                },
                                                style = MaterialTheme.typography.bodyLarge
                                            )
                                        }
                                    }
                                }
                            },
                            confirmButton = {
                                TextButton(onClick = { showThemeDialog = false }) {
                                    Text(stringResource(R.string.common_cancel))
                                }
                            },
                            shape = AppShapes.Dialog
                        )
                    }
                    
                    HorizontalDivider(
                        modifier = Modifier.padding(start = Spacing.PageHorizontal),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                    
                    // 震动反馈
                    SettingsRow(
                        icon = Icons.Outlined.Vibration,
                        title = stringResource(R.string.setting_vibration_feedback)
                    ) {
                        Switch(
                            checked = state.vibrateOn,
                            onCheckedChange = { viewModel.sendIntent(SettingIntent.SetVibrateOn(it)) }
                        )
                    }
                }
            }

            // 首选项
            SectionHeader(title = stringResource(R.string.setting_section_preferences))

            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.PageHorizontal)
            ) {
                SettingsRow(
                    icon = Icons.Outlined.Settings,
                    title = stringResource(R.string.setting_preferences),
                    onClick = onNavigateToPreferences
                ) {
                    ListItemTrailingArrow()
                }
            }

            Spacer(modifier = Modifier.height(Spacing.CardSpacing))

            // 下载设置
            SectionHeader(title = stringResource(R.string.setting_section_download))
            
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.PageHorizontal)
            ) {
                SettingsRow(
                    icon = Icons.Outlined.Wifi,
                    title = stringResource(R.string.setting_wifi_only_download)
                ) {
                    Switch(
                        checked = state.downloadWifiOnly,
                        onCheckedChange = { viewModel.sendIntent(SettingIntent.SetDownloadWifiOnly(it)) }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(Spacing.CardSpacing))
            
            // 安全设置
            SectionHeader(title = stringResource(R.string.setting_section_security))
            
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.PageHorizontal)
            ) {
                Column {
                    SettingsRow(
                        icon = Icons.Outlined.VerifiedUser,
                        title = stringResource(R.string.setting_ssl_verification)
                    ) {
                        Switch(
                            checked = state.checkSsl,
                            onCheckedChange = { viewModel.sendIntent(SettingIntent.SetCheckSsl(it)) }
                        )
                    }
                    
                    HorizontalDivider(
                        modifier = Modifier.padding(start = Spacing.PageHorizontal),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                    
                    SettingsRow(
                        icon = Icons.Outlined.Fingerprint,
                        title = stringResource(R.string.setting_launch_auth)
                    ) {
                        Switch(
                            checked = state.launchAuth,
                            onCheckedChange = { viewModel.sendIntent(SettingIntent.SetLaunchAuth(it)) }
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(Spacing.CardSpacing))
            
            // 高级功能
            SectionHeader(title = stringResource(R.string.setting_section_advanced))
            
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.PageHorizontal)
            ) {
                Column {
                    SettingsItemRow(
                        icon = Icons.Outlined.Dns,
                        title = "Docker",
                        subtitle = stringResource(R.string.setting_docker_subtitle),
                        onClick = onNavigateToDocker
                    )
                    
                    HorizontalDivider(
                        modifier = Modifier.padding(start = Spacing.PageHorizontal + 40.dp + Spacing.Standard),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                    
                    SettingsItemRow(
                        icon = Icons.Outlined.Extension,
                        title = stringResource(R.string.setting_packages),
                        subtitle = stringResource(R.string.setting_packages_subtitle),
                        onClick = onNavigateToPackages
                    )
                    
                    HorizontalDivider(
                        modifier = Modifier.padding(start = Spacing.PageHorizontal + 40.dp + Spacing.Standard),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                    
                    SettingsItemRow(
                        icon = Icons.Outlined.Computer,
                        title = stringResource(R.string.setting_virtual_machine),
                        subtitle = "Virtual Machine Manager",
                        onClick = onNavigateToVirtualMachine
                    )
                    
                    HorizontalDivider(
                        modifier = Modifier.padding(start = Spacing.PageHorizontal + 40.dp + Spacing.Standard),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                    
                    SettingsItemRow(
                        icon = Icons.Outlined.HealthAndSafety,
                        title = stringResource(R.string.setting_smart_test),
                        subtitle = stringResource(R.string.setting_smart_test_subtitle),
                        onClick = onNavigateToSmartTest
                    )
                    
                    HorizontalDivider(
                        modifier = Modifier.padding(start = Spacing.PageHorizontal + 40.dp + Spacing.Standard),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                    
                    SettingsItemRow(
                        icon = Icons.Outlined.Backup,
                        title = stringResource(R.string.setting_backup),
                        subtitle = stringResource(R.string.setting_backup_subtitle),
                        onClick = onNavigateToBackup
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(Spacing.CardSpacing))
            
            // 其他
            SectionHeader(title = stringResource(R.string.setting_section_other))
            
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.PageHorizontal)
            ) {
                Column {
                    SettingsItemRow(
                        icon = Icons.Outlined.Info,
                        title = stringResource(R.string.setting_about),
                        subtitle = stringResource(R.string.setting_version, state.appVersion),
                        onClick = onNavigateToAbout
                    )
                    
                    HorizontalDivider(
                        modifier = Modifier.padding(start = Spacing.PageHorizontal + 40.dp + Spacing.Standard),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                    
                    SettingsItemRow(
                        icon = Icons.Outlined.Feedback,
                        title = stringResource(R.string.setting_feedback),
                        subtitle = stringResource(R.string.setting_feedback_subtitle),
                        onClick = onNavigateToFeedback
                    )
                    
                    HorizontalDivider(
                        modifier = Modifier.padding(start = Spacing.PageHorizontal + 40.dp + Spacing.Standard),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                    
                    SettingsItemRow(
                        icon = Icons.Outlined.Code,
                        title = stringResource(R.string.open_source_title),
                        subtitle = stringResource(R.string.open_source_desc),
                        onClick = onNavigateToOpenSource
                    )
                }
            }

            Spacer(modifier = Modifier.height(Spacing.Large))

            // 退出登录按钮
            OutlinedButton(
                onClick = { viewModel.sendIntent(SettingIntent.Logout) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.PageHorizontal),
                shape = RoundedCornerShape(CornerRadius.Large),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
                )
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Logout,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(Spacing.MediumSmall))
                Text(
                    text = stringResource(R.string.setting_logout),
                    style = MaterialTheme.typography.labelLarge
                )
            }
            
            Spacer(modifier = Modifier.height(Spacing.ExtraLarge))
        }
    }
}

@Composable
private fun SettingsRow(
    icon: ImageVector,
    title: String,
    onClick: (() -> Unit)? = null,
    trailing: @Composable () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (onClick != null) {
                    Modifier.clickable(onClick = onClick)
                } else {
                    Modifier
                }
            )
            .padding(horizontal = Spacing.CardPadding, vertical = Spacing.Medium),
        verticalAlignment = Alignment.CenterVertically
    ) {
        LeadingIcon(icon = icon)
        Spacer(modifier = Modifier.width(Spacing.Standard))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
        trailing()
    }
}

@Composable
private fun SettingsItemRow(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    titleColor: Color = MaterialTheme.colorScheme.onSurface,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = Spacing.CardPadding, vertical = Spacing.Medium),
        verticalAlignment = Alignment.CenterVertically
    ) {
        LeadingIcon(
            icon = icon,
            containerColor = if (titleColor == MaterialTheme.colorScheme.error) {
                MaterialTheme.colorScheme.errorContainer
            } else {
                MaterialTheme.colorScheme.primaryContainer
            },
            contentColor = if (titleColor == MaterialTheme.colorScheme.error) {
                MaterialTheme.colorScheme.onErrorContainer
            } else {
                MaterialTheme.colorScheme.onPrimaryContainer
            }
        )
        Spacer(modifier = Modifier.width(Spacing.Standard))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = titleColor
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        ListItemTrailingArrow()
    }
}