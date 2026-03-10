package wang.zengye.dsm.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import wang.zengye.dsm.ui.download.DownloadScreen

fun NavGraphBuilder.downloadNavGraph(navController: NavHostController) {
    // 下载
    composable<DsmRoute.Download>(
        enterTransition = { tabEnterTransition },
        exitTransition = { tabExitTransition },
        popEnterTransition = { tabEnterTransition },
        popExitTransition = { tabExitTransition }
    ) {
        DownloadScreen(
            onNavigateToPeerList = { taskId ->
                navController.navigate(DsmRoute.PeerList(taskId = taskId))
            },
            onNavigateToTrackerManager = { taskId ->
                navController.navigate(DsmRoute.TrackerManager(taskId = taskId))
            },
            onNavigateToAddTask = {
                navController.navigate(DsmRoute.AddDownloadTask)
            },
            onNavigateToTaskDetail = { taskId ->
                navController.navigate(DsmRoute.DownloadTaskDetail(taskId = taskId))
            }
        )
    }

    // 下载任务详情
    composable<DsmRoute.DownloadTaskDetail> { backStackEntry ->
        val route = backStackEntry.toRoute<DsmRoute.DownloadTaskDetail>()
        wang.zengye.dsm.ui.download.DownloadTaskDetailScreen(
            taskId = route.taskId,
            onNavigateBack = { navController.popBackStack() },
            onNavigateToTrackerManager = { tId ->
                navController.navigate(DsmRoute.TrackerManager(taskId = tId))
            },
            onNavigateToPeerList = { tId ->
                navController.navigate(DsmRoute.PeerList(taskId = tId))
            }
        )
    }
    // 新建下载任务
    composable<DsmRoute.AddDownloadTask> {
        wang.zengye.dsm.ui.download.AddDownloadTaskScreen(
            onNavigateBack = { navController.popBackStack() },
            onTaskCreated = { navController.popBackStack() },
            onNavigateToBtFileSelect = { taskId, _ ->
                navController.navigate(DsmRoute.BtFileSelect(taskId = taskId))
            }
        )
    }

    // BT 文件选择
    composable<DsmRoute.BtFileSelect> { backStackEntry ->
        val route = backStackEntry.toRoute<DsmRoute.BtFileSelect>()
        wang.zengye.dsm.ui.download.BtFileSelectScreen(
            taskId = route.taskId,
            onNavigateBack = {
                navController.popBackStack<DsmRoute.AddDownloadTask>(inclusive = true)
            }
        )
    }

    // Peer 列表
    composable<DsmRoute.PeerList> { backStackEntry ->
        val route = backStackEntry.toRoute<DsmRoute.PeerList>()
        wang.zengye.dsm.ui.download.PeerListScreen(
            taskId = route.taskId,
            onNavigateBack = { navController.popBackStack() }
        )
    }

    // Tracker 管理
    composable<DsmRoute.TrackerManager> { backStackEntry ->
        val route = backStackEntry.toRoute<DsmRoute.TrackerManager>()
        wang.zengye.dsm.ui.download.TrackerManagerScreen(
            taskId = route.taskId,
            onNavigateBack = { navController.popBackStack() }
        )
    }
}
