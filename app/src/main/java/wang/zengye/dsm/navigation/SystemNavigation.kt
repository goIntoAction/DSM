package wang.zengye.dsm.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.toRoute

fun NavGraphBuilder.systemNavGraph(navController: NavHostController) {
    // 性能监控
    composable<DsmRoute.Performance> {
        wang.zengye.dsm.ui.performance.PerformanceScreen(
            onNavigateBack = { navController.popBackStack() },
            onNavigateToHistory = { navController.navigate(DsmRoute.PerformanceHistory) },
            onNavigateToProcessManager = { navController.navigate(DsmRoute.ProcessManager) }
        )
    }

    // 性能历史
    composable<DsmRoute.PerformanceHistory> {
        wang.zengye.dsm.ui.resource_monitor.PerformanceHistoryScreen(
            onNavigateBack = { navController.popBackStack() }
        )
    }

    // 进程管理
    composable<DsmRoute.ProcessManager> {
        wang.zengye.dsm.ui.resource_monitor.ProcessManagerScreen(
            onNavigateBack = { navController.popBackStack() }
        )
    }

    // 任务管理
    composable<DsmRoute.TaskManager> {
        wang.zengye.dsm.ui.taskmanager.TaskManagerScreen(
            onBack = { navController.popBackStack() }
        )
    }

    // 通知中心
    composable<DsmRoute.Notifications> {
        wang.zengye.dsm.ui.control_panel.NotificationsScreen(
            onNavigateBack = { navController.popBackStack() }
        )
    }

    // 套件中心
    composable<DsmRoute.Packages> {
        wang.zengye.dsm.ui.packages.PackagesScreen(
            onNavigateBack = { navController.popBackStack() }
        )
    }
    // 虚拟机
    composable<DsmRoute.VirtualMachine> {
        wang.zengye.dsm.ui.virtual_machine.VirtualMachineScreen(
            onNavigateBack = { navController.popBackStack() }
        )
    }

    // SMART 测试
    composable<DsmRoute.SmartTest> { backStackEntry ->
        val route = backStackEntry.toRoute<DsmRoute.SmartTest>()
        wang.zengye.dsm.ui.smart.SmartTestScreen(
            diskId = route.diskId,
            onBack = { navController.popBackStack() }
        )
    }

    // 存储管理
    composable<DsmRoute.Storage> {
        wang.zengye.dsm.ui.control_panel.StorageScreen(
            onNavigateBack = { navController.popBackStack() },
            onNavigateToSmartTest = { diskId ->
                navController.navigate(DsmRoute.SmartTest(diskId = diskId))
            }
        )
    }

    // 日志中心
    composable<DsmRoute.Logs> {
        wang.zengye.dsm.ui.logcenter.LogCenterScreen(
            onBack = { navController.popBackStack() }
        )
    }

    // DDNS
    composable<DsmRoute.Ddns> {
        wang.zengye.dsm.ui.control_panel.DdnsScreen(
            onNavigateBack = { navController.popBackStack() }
        )
    }

    // 安全扫描
    composable<DsmRoute.SecurityScan> {
        wang.zengye.dsm.ui.control_panel.SecurityScanScreen(
            onNavigateBack = { navController.popBackStack() }
        )
    }

    // 媒体索引
    composable<DsmRoute.MediaIndex> {
        wang.zengye.dsm.ui.control_panel.MediaIndexScreen(
            onNavigateBack = { navController.popBackStack() }
        )
    }

    // SSH终端 - 底部滑入模态动画
    composable<DsmRoute.Terminal>(
        enterTransition = { slideInFromBottom },
        exitTransition = { slideOutToBottom },
        popEnterTransition = { slideInFromBottom },
        popExitTransition = { slideOutToBottom }
    ) {
        wang.zengye.dsm.ui.terminal.TerminalScreen(
            onNavigateBack = { navController.popBackStack() }
        )
    }
}
