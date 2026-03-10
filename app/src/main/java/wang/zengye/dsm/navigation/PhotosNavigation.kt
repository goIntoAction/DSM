package wang.zengye.dsm.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import wang.zengye.dsm.ui.photos.PhotosScreen

fun NavGraphBuilder.photosNavGraph(navController: NavHostController) {
    // 照片管理
    composable<DsmRoute.Photos>(
        enterTransition = { tabEnterTransition },
        exitTransition = { tabExitTransition },
        popEnterTransition = { tabEnterTransition },
        popExitTransition = { tabExitTransition }
    ) {
        PhotosScreen(
            onNavigateBack = { navController.popBackStack() },
            onNavigateToAlbum = { albumId, albumName ->
                navController.navigate(DsmRoute.AlbumDetail(albumId = albumId.toString(), albumName = albumName))
            },
            onNavigateToPhoto = { photoId, _ ->
                navController.navigate(DsmRoute.PhotoPreview(photoId = photoId.toString()))
            },
            onNavigateToVideoPlayer = { url, title ->
                navController.navigate(DsmRoute.VideoPlayer(url = url, title = title))
            }
        )
    }

    // 照片预览 - 使用底部滑入的模态动画
    composable<DsmRoute.PhotoPreview>(
        enterTransition = { slideInFromBottom },
        exitTransition = { slideOutToBottom },
        popEnterTransition = { slideInFromBottom },
        popExitTransition = { slideOutToBottom }
    ) { backStackEntry ->
        val route = backStackEntry.toRoute<DsmRoute.PhotoPreview>()
        wang.zengye.dsm.ui.photos.PhotoPreviewScreen(
            photoId = route.photoId.toLongOrNull() ?: 0,
            onNavigateBack = { navController.popBackStack() },
            onNavigateToVideoPlayer = { url, title ->
                navController.navigate(DsmRoute.VideoPlayer(url = url, title = title))
            }
        )
    }

    // 相册详情 - 使用共享元素风格的动画
    composable<DsmRoute.AlbumDetail>(
        enterTransition = { sharedElementEnter },
        exitTransition = { sharedElementExit },
        popEnterTransition = { sharedElementEnter },
        popExitTransition = { sharedElementExit }
    ) { backStackEntry ->
        val route = backStackEntry.toRoute<DsmRoute.AlbumDetail>()
        wang.zengye.dsm.ui.photos.AlbumDetailScreen(
            albumId = route.albumId.toLongOrNull() ?: 0,
            albumName = route.albumName,
            onNavigateBack = { navController.popBackStack() },
            onNavigateToPhoto = { photoId, _ ->
                navController.navigate(DsmRoute.PhotoPreview(photoId = photoId.toString()))
            }
        )
    }
}
