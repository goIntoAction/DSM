package wang.zengye.dsm.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import wang.zengye.dsm.ui.file.FileScreen

// 用于存储文件夹选择回调
object FolderSelectCallback {
    private var callback: ((String) -> Unit)? = null
    private var currentExcludePath: String = ""
    
    fun setCallback(excludePath: String, onSelected: (String) -> Unit) {
        currentExcludePath = excludePath
        callback = onSelected
    }
    
    fun getExcludePath(): String = currentExcludePath
    
    fun invokeAndClear(path: String) {
        callback?.invoke(path)
        callback = null
        currentExcludePath = ""
    }
    
    fun clear() {
        callback = null
        currentExcludePath = ""
    }
}

fun NavGraphBuilder.fileNavGraph(navController: NavHostController) {
    // 文件管理
    composable<DsmRoute.FileManager>(
        enterTransition = { tabEnterTransition },
        exitTransition = { tabExitTransition },
        popEnterTransition = { tabEnterTransition },
        popExitTransition = { tabExitTransition }
    ) {
        FileScreen(
            onNavigateBack = {},
            onNavigateToDetail = { path ->
                navController.navigate(DsmRoute.FileDetail(path = path))
            },
            onNavigateToSearch = { currentPath ->
                navController.navigate(DsmRoute.FileSearch(path = currentPath))
            },
            onNavigateToFavorites = {
                navController.navigate(DsmRoute.Favorite)
            },
            onNavigateToUpload = { currentPath ->
                navController.navigate(DsmRoute.FileUpload(path = currentPath))
            },
            onNavigateToShareManager = {
                navController.navigate(DsmRoute.ShareManager)
            },
            onNavigateToVideoPlayer = { url, title ->
                navController.navigate(DsmRoute.VideoPlayer(url = url, title = title))
            },
            onNavigateToAudioPlayer = { url, title ->
                navController.navigate(DsmRoute.AudioPlayer(url = url, title = title))
            },
            onNavigateToImageViewer = { path ->
                navController.navigate(DsmRoute.FileImageViewer(path = path))
            },
            onNavigateToPdfViewer = { url, title ->
                navController.navigate(DsmRoute.PdfViewer(url = url, title = title))
            },
            onNavigateToTextEditor = { url, title ->
                navController.navigate(DsmRoute.TextEditor(url = url, title = title))
            },
            onNavigateToDownloadManager = {
                navController.navigate(DsmRoute.DownloadManager)
            },
            onNavigateToSelectFolder = { excludePath, onSelected ->
                FolderSelectCallback.setCallback(excludePath, onSelected)
                navController.navigate(DsmRoute.SelectFolder(excludePath = excludePath))
            }
        )
    }
    // 文件图片查看器 - 使用底部滑入的模态动画
    composable<DsmRoute.FileImageViewer>(
        enterTransition = { slideInFromBottom },
        exitTransition = { slideOutToBottom },
        popEnterTransition = { slideInFromBottom },
        popExitTransition = { slideOutToBottom }
    ) { backStackEntry ->
        val route = backStackEntry.toRoute<DsmRoute.FileImageViewer>()
        wang.zengye.dsm.ui.file.FileImageViewerScreen(
            initialPath = route.path,
            onNavigateBack = { navController.popBackStack() },
            onNavigateToDetail = { detailPath ->
                navController.navigate(DsmRoute.FileDetail(path = detailPath))
            }
        )
    }

    // 文件搜索
    composable<DsmRoute.FileSearch> { backStackEntry ->
        val route = backStackEntry.toRoute<DsmRoute.FileSearch>()
        wang.zengye.dsm.ui.file.FileSearchScreen(
            initialPath = route.path,
            onNavigateBack = { navController.popBackStack() },
            onFileClick = { filePath ->
                navController.navigate(DsmRoute.FileDetail(path = filePath))
            }
        )
    }

    // 收藏夹
    composable<DsmRoute.Favorite> {
        wang.zengye.dsm.ui.file.FavoriteScreen(
            onNavigateBack = { navController.popBackStack() },
            onFavoriteClick = { path ->
                navController.navigate(DsmRoute.FileDetail(path = path))
            }
        )
    }

    // 文件上传
    composable<DsmRoute.FileUpload> { backStackEntry ->
        val route = backStackEntry.toRoute<DsmRoute.FileUpload>()
        wang.zengye.dsm.ui.file.FileUploadScreen(
            targetPath = route.path,
            onNavigateBack = { navController.popBackStack() }
        )
    }

    // 分享管理
    composable<DsmRoute.ShareManager> {
        wang.zengye.dsm.ui.file.ShareManagerScreen(
            onNavigateBack = { navController.popBackStack() }
        )
    }

    // 文件详情
    composable<DsmRoute.FileDetail> { backStackEntry ->
        val route = backStackEntry.toRoute<DsmRoute.FileDetail>()
        wang.zengye.dsm.ui.file.FileDetailScreen(
            filePath = route.path,
            onNavigateBack = { navController.popBackStack() },
            onNavigateToVideoPlayer = { url, title ->
                navController.navigate(DsmRoute.VideoPlayer(url = url, title = title))
            },
            onNavigateToAudioPlayer = { url, title ->
                navController.navigate(DsmRoute.AudioPlayer(url = url, title = title))
            }
        )
    }

    // 下载管理
    composable<DsmRoute.DownloadManager> {
        wang.zengye.dsm.ui.file.DownloadManagerScreen(
            onNavigateBack = { navController.popBackStack() }
        )
    }

    // 文件夹选择器
    composable<DsmRoute.SelectFolder> { backStackEntry ->
        val route = backStackEntry.toRoute<DsmRoute.SelectFolder>()
        wang.zengye.dsm.ui.file.SelectFolderScreen(
            excludePath = route.excludePath,
            onFolderSelected = { path ->
                FolderSelectCallback.invokeAndClear(path)
                navController.popBackStack()
            },
            onNavigateBack = {
                FolderSelectCallback.clear()
                navController.popBackStack()
            }
        )
    }
}
