package wang.zengye.dsm.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import wang.zengye.dsm.ui.control_panel.SystemInfoScreen
import wang.zengye.dsm.ui.dashboard.DashboardScreen

fun NavGraphBuilder.dashboardNavGraph(navController: NavHostController) {
    // 控制台
    composable<DsmRoute.Dashboard>(
        enterTransition = { tabEnterTransition },
        exitTransition = { tabExitTransition },
        popEnterTransition = { tabEnterTransition },
        popExitTransition = { tabExitTransition }
    ) {
        DashboardScreen(
            onNavigateToSystemInfo = {
                navController.navigate(DsmRoute.SystemInfo)
            },
            onNavigateToSearch = {
                navController.navigate(DsmRoute.GlobalSearch)
            },
            onNavigateToRoute = { route ->
                if (route.startsWith("webview:")) {
                    val parts = route.removePrefix("webview:").split(":", limit = 2)
                    val title = parts.getOrElse(0) { "" }
                    val url = parts.getOrElse(1) { "" }
                    navController.navigate(DsmRoute.AppWebView(title = title, url = url))
                } else {
                    // 处理简单字符串路由到类型安全路由的映射
                    navigateByStringRoute(navController, route)
                }
            },
            onNavigateToTerminal = {
                navController.navigate(DsmRoute.Terminal)
            },
            onNavigateToDownloads = {
                navController.navigate(DsmRoute.DownloadManager)
            }
        )
    }

    // 全局搜索
    composable<DsmRoute.GlobalSearch> {
        wang.zengye.dsm.ui.search.UniversalSearchScreen(
            onNavigateBack = { navController.popBackStack() },
            onNavigateToFile = { path ->
                navController.navigate(DsmRoute.FileDetail(path = path))
            }
        )
    }

    // 系统信息
    composable<DsmRoute.SystemInfo> {
        SystemInfoScreen(
            onNavigateBack = { navController.popBackStack() }
        )
    }
}

/**
 * Dashboard 中 onNavigateToRoute 传入的字符串路由映射到类型安全路由
 */
private fun navigateByStringRoute(navController: NavHostController, route: String) {
    val destination: Any = when (route) {
        "control_panel" -> DsmRoute.ControlPanel
        "docker" -> DsmRoute.Docker
        "performance" -> DsmRoute.Performance
        "task_manager" -> DsmRoute.TaskManager
        "packages" -> DsmRoute.Packages
        "virtual_machine" -> DsmRoute.VirtualMachine
        "smart_test" -> DsmRoute.SmartTest
        "storage" -> DsmRoute.Storage
        "logs" -> DsmRoute.Logs
        "notifications" -> DsmRoute.Notifications
        "security_scan" -> DsmRoute.SecurityScan
        "ddns" -> DsmRoute.Ddns
        "media_index" -> DsmRoute.MediaIndex
        "backup" -> DsmRoute.Backup
        "photos" -> DsmRoute.Photos
        "download" -> DsmRoute.Download
        "file" -> DsmRoute.FileManager
        else -> return // 未知路由，忽略
    }
    navController.navigate(destination)
}
