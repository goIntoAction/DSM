package wang.zengye.dsm.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import wang.zengye.dsm.ui.control_panel.*
import wang.zengye.dsm.ui.iscsi.IscsiScreen
import wang.zengye.dsm.ui.update.UpdateScreen

fun NavGraphBuilder.controlPanelNavGraph(navController: NavHostController) {
    // 控制面板主页
    composable<DsmRoute.ControlPanel> {
        ControlPanelScreen(
            onNavigateBack = { navController.popBackStack() },
            onNavigateToItem = { itemId ->
                navController.navigate(DsmRoute.ControlPanelDetail(itemId = itemId))
            }
        )
    }

    // 控制面板子页面
    composable<DsmRoute.ControlPanelDetail> { backStackEntry ->
        val route = backStackEntry.toRoute<DsmRoute.ControlPanelDetail>()
        ControlPanelDetailRoute(
            itemId = route.itemId,
            onNavigateBack = { navController.popBackStack() },
            onNavigateToUserDetail = { username ->
                navController.navigate(DsmRoute.UserDetail(username = username))
            },
            onNavigateToAddShareFolder = { shareName ->
                navController.navigate(DsmRoute.AddShareFolder(shareName = shareName))
            },
            onNavigateToSmartTest = { diskId ->
                navController.navigate(DsmRoute.SmartTest(diskId = diskId))
            }
        )
    }

    // 用户详情
    composable<DsmRoute.UserDetail> { backStackEntry ->
        val route = backStackEntry.toRoute<DsmRoute.UserDetail>()
        UserDetailScreen(
            username = route.username,
            onNavigateBack = { navController.popBackStack() }
        )
    }

    // 添加/编辑共享文件夹
    composable<DsmRoute.AddShareFolder> { backStackEntry ->
        val route = backStackEntry.toRoute<DsmRoute.AddShareFolder>()
        AddShareFolderScreen(
            shareName = route.shareName,
            onNavigateBack = { navController.popBackStack() }
        )
    }
}

/**
 * 控制面板详情页面路由
 */
@Composable
private fun ControlPanelDetailRoute(
    itemId: String,
    onNavigateBack: () -> Unit,
    onNavigateToUserDetail: (String) -> Unit = {},
    onNavigateToAddShareFolder: (String?) -> Unit = {},
    onNavigateToSmartTest: (String) -> Unit = {}
) {
    when (itemId) {
        "info" -> SystemInfoScreen(onNavigateBack = onNavigateBack)
        "storage" -> StorageScreen(
            onNavigateBack = onNavigateBack,
            onNavigateToSmartTest = onNavigateToSmartTest
        )
        "network" -> NetworkScreen(onNavigateBack = onNavigateBack)
        "terminal" -> TerminalScreen(onNavigateBack = onNavigateBack)
        "power" -> PowerScreen(onNavigateBack = onNavigateBack)
        "external" -> ExternalDevicesScreen(onNavigateBack = onNavigateBack)
        "users" -> UsersScreen(
            onNavigateBack = onNavigateBack,
            onNavigateToUserDetail = onNavigateToUserDetail
        )
        "groups" -> GroupsScreen(onNavigateBack = onNavigateBack)
        "shares" -> SharedFoldersScreen(
            onNavigateBack = onNavigateBack,
            onNavigateToAddShareFolder = onNavigateToAddShareFolder
        )
        "file_service" -> FileServiceScreen(onNavigateBack = onNavigateBack)
        "security" -> SecurityScanScreen(onNavigateBack = onNavigateBack)
        "firewall" -> FirewallScreen(onNavigateBack = onNavigateBack)
        "certificate" -> CertificateScreen(onNavigateBack = onNavigateBack)
        "tasks" -> TaskSchedulerScreen(onNavigateBack = onNavigateBack)
        "ddns" -> DdnsScreen(onNavigateBack = onNavigateBack)
        "logs" -> wang.zengye.dsm.ui.logcenter.LogCenterScreen(onBack = onNavigateBack)
        "notifications" -> NotificationsScreen(onNavigateBack = onNavigateBack)
        "update" -> UpdateScreen(onNavigateBack = onNavigateBack)
        "iscsi" -> IscsiScreen(onNavigateBack = onNavigateBack)
        else -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("未找到页面: $itemId")
            }
        }
    }
}
