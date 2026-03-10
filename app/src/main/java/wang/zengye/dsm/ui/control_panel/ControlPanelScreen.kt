package wang.zengye.dsm.ui.control_panel

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import wang.zengye.dsm.R
import wang.zengye.dsm.ui.components.*
import wang.zengye.dsm.ui.theme.*

/**
 * 控制面板菜单项
 */
data class ControlPanelItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val badge: String? = null
)

/**
 * 控制面板分组
 */
data class ControlPanelGroup(
    val title: String,
    val items: List<ControlPanelItem>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ControlPanelScreen(
    onNavigateBack: () -> Unit = {},
    onNavigateToItem: (String) -> Unit = {}
) {
    val groups = listOf(
        ControlPanelGroup(
            title = stringResource(R.string.control_panel_group_system),
            items = listOf(
                ControlPanelItem("info", stringResource(R.string.control_panel_info_center), stringResource(R.string.control_panel_info_center_desc), Icons.Outlined.Info),
                ControlPanelItem("storage", stringResource(R.string.control_panel_storage), stringResource(R.string.control_panel_storage_desc), Icons.Outlined.Storage),
                ControlPanelItem("iscsi", stringResource(R.string.iscsi_title), stringResource(R.string.iscsi_desc), Icons.Outlined.Dns),
                ControlPanelItem("network", stringResource(R.string.control_panel_network), stringResource(R.string.control_panel_network_desc), Icons.Outlined.NetworkCheck),
                ControlPanelItem("terminal", stringResource(R.string.control_panel_terminal), stringResource(R.string.control_panel_terminal_desc), Icons.Outlined.Terminal),
                ControlPanelItem("power", stringResource(R.string.control_panel_power), stringResource(R.string.control_panel_power_desc), Icons.Outlined.PowerSettingsNew),
                ControlPanelItem("external", stringResource(R.string.control_panel_external), stringResource(R.string.control_panel_external_desc), Icons.Outlined.Usb),
                ControlPanelItem("update", stringResource(R.string.update_title), stringResource(R.string.update_desc), Icons.Outlined.SystemUpdate)
            )
        ),
        ControlPanelGroup(
            title = stringResource(R.string.control_panel_group_permission),
            items = listOf(
                ControlPanelItem("users", stringResource(R.string.control_panel_users), stringResource(R.string.control_panel_users_desc), Icons.Outlined.People),
                ControlPanelItem("groups", stringResource(R.string.control_panel_groups), stringResource(R.string.control_panel_groups_desc), Icons.Outlined.Group),
                ControlPanelItem("shares", stringResource(R.string.control_panel_shares), stringResource(R.string.control_panel_shares_desc), Icons.Outlined.FolderShared)
            )
        ),
        ControlPanelGroup(
            title = stringResource(R.string.control_panel_group_file_service),
            items = listOf(
                ControlPanelItem("file_service", stringResource(R.string.control_panel_file_service), stringResource(R.string.control_panel_file_service_desc), Icons.Outlined.FolderShared)
            )
        ),
        ControlPanelGroup(
            title = stringResource(R.string.control_panel_group_security),
            items = listOf(
                ControlPanelItem("security", stringResource(R.string.control_panel_security_advisor), stringResource(R.string.control_panel_security_advisor_desc), Icons.Outlined.Security),
                ControlPanelItem("firewall", stringResource(R.string.control_panel_firewall), stringResource(R.string.control_panel_firewall_desc), Icons.Outlined.FireExtinguisher),
                ControlPanelItem("certificate", stringResource(R.string.control_panel_certificate), stringResource(R.string.control_panel_certificate_desc_short), Icons.Outlined.Verified)
            )
        ),
        ControlPanelGroup(
            title = stringResource(R.string.control_panel_group_service),
            items = listOf(
                ControlPanelItem("tasks", stringResource(R.string.control_panel_task_scheduler), stringResource(R.string.control_panel_task_scheduler_desc_short), Icons.Outlined.Schedule),
                ControlPanelItem("ddns", stringResource(R.string.control_panel_ddns), stringResource(R.string.control_panel_ddns_desc_short), Icons.Outlined.Dns),
                ControlPanelItem("logs", stringResource(R.string.control_panel_log_center), stringResource(R.string.control_panel_log_center_desc), Icons.Outlined.Article),
                ControlPanelItem("notifications", stringResource(R.string.control_panel_notification_center), stringResource(R.string.control_panel_notification_desc_short), Icons.Outlined.Notifications)
            )
        )
    )
    
    Scaffold(
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = stringResource(R.string.control_panel_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Medium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.common_back)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = Color.Transparent
                ),
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(vertical = Spacing.Small)
        ) {
            groups.forEachIndexed { groupIndex, group ->
                // 分组标题
                item {
                    SectionHeader(title = group.title)
                }
                
                // 分组卡片
                item {
                    ElevatedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = Spacing.PageHorizontal),
                        shape = AppShapes.Card
                    ) {
                        Column {
                            group.items.forEachIndexed { index, item ->
                                val itemOnClick = remember(item, onNavigateToItem) {
                                    { onNavigateToItem(item.id) }
                                }
                                ControlPanelItemRow(
                                    item = item,
                                    onClick = itemOnClick
                                )
                                
                                if (index < group.items.size - 1) {
                                    HorizontalDivider(
                                        modifier = Modifier.padding(start = Spacing.PageHorizontal + 40.dp + Spacing.Standard),
                                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                    )
                                }
                            }
                        }
                    }
                }
                
                // 分组间距
                item {
                    Spacer(modifier = Modifier.height(Spacing.CardSpacing))
                }
            }
        }
    }
}

@Composable
private fun ControlPanelItemRow(
    item: ControlPanelItem,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = Spacing.CardPadding, vertical = Spacing.Medium),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 图标
        Surface(
            modifier = Modifier.size(40.dp),
            shape = RoundedCornerShape(CornerRadius.Medium),
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(Dimensions.IconStandard)
                )
            }
        }
        
        Spacer(modifier = Modifier.width(Spacing.Standard))
        
        // 文本内容
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = item.subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        // 徽章和箭头
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (item.badge != null) {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = RoundedCornerShape(CornerRadius.Small)
                ) {
                    Text(
                        text = item.badge,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                Spacer(modifier = Modifier.width(Spacing.Small))
            }
            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
        }
    }
}