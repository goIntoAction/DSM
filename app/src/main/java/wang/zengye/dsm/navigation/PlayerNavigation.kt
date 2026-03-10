package wang.zengye.dsm.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.toRoute

fun NavGraphBuilder.playerNavGraph(navController: NavHostController) {
    // 视频播放器 - 底部滑入模态动画
    composable<DsmRoute.VideoPlayer>(
        enterTransition = { slideInFromBottom },
        exitTransition = { slideOutToBottom },
        popEnterTransition = { slideInFromBottom },
        popExitTransition = { slideOutToBottom }
    ) { backStackEntry ->
        val route = backStackEntry.toRoute<DsmRoute.VideoPlayer>()
        wang.zengye.dsm.ui.player.VideoPlayerScreen(
            videoUrl = route.url,
            videoTitle = route.title,
            onBack = { navController.popBackStack() }
        )
    }

    // 音频播放器 - 底部滑入模态动画
    composable<DsmRoute.AudioPlayer>(
        enterTransition = { slideInFromBottom },
        exitTransition = { slideOutToBottom },
        popEnterTransition = { slideInFromBottom },
        popExitTransition = { slideOutToBottom }
    ) { backStackEntry ->
        val route = backStackEntry.toRoute<DsmRoute.AudioPlayer>()
        wang.zengye.dsm.ui.player.AudioPlayerScreen(
            audioUrl = route.url,
            audioTitle = route.title,
            onBack = { navController.popBackStack() }
        )
    }

    // PDF 查看器 - 底部滑入模态动画
    composable<DsmRoute.PdfViewer>(
        enterTransition = { slideInFromBottom },
        exitTransition = { slideOutToBottom },
        popEnterTransition = { slideInFromBottom },
        popExitTransition = { slideOutToBottom }
    ) { backStackEntry ->
        val route = backStackEntry.toRoute<DsmRoute.PdfViewer>()
        wang.zengye.dsm.ui.components.PdfViewerScreen(
            pdfUrl = route.url,
            pdfTitle = route.title,
            onBack = { navController.popBackStack() }
        )
    }

    // 文本编辑器 - 底部滑入模态动画
    composable<DsmRoute.TextEditor>(
        enterTransition = { slideInFromBottom },
        exitTransition = { slideOutToBottom },
        popEnterTransition = { slideInFromBottom },
        popExitTransition = { slideOutToBottom }
    ) { backStackEntry ->
        val route = backStackEntry.toRoute<DsmRoute.TextEditor>()
        wang.zengye.dsm.ui.components.TextEditorScreen(
            filePath = route.url,
            onBack = { navController.popBackStack() }
        )
    }

    // 应用 WebView - 底部滑入模态动画
    composable<DsmRoute.AppWebView>(
        enterTransition = { slideInFromBottom },
        exitTransition = { slideOutToBottom },
        popEnterTransition = { slideInFromBottom },
        popExitTransition = { slideOutToBottom }
    ) { backStackEntry ->
        val route = backStackEntry.toRoute<DsmRoute.AppWebView>()
        wang.zengye.dsm.ui.components.WebViewScreen(
            url = route.url,
            title = route.title,
            onBack = { navController.popBackStack() }
        )
    }
}