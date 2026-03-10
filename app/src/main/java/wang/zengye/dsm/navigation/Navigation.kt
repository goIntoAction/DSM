package wang.zengye.dsm.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import kotlinx.serialization.Serializable
import wang.zengye.dsm.R
import wang.zengye.dsm.ui.login.AccountsScreen
import wang.zengye.dsm.ui.login.LoginScreen
import wang.zengye.dsm.ui.performance.PerformanceScreen

/**
 * 类型安全路由定义
 */
object DsmRoute {
    // 登录相关
    @Serializable data object Login
    @Serializable data object Accounts

    // 主界面 - 底部导航容器
    @Serializable data object Home

    // 底部导航 Tab - 全部在同一层级
    @Serializable data object Dashboard
    @Serializable data object FileManager
    @Serializable data object Download
    @Serializable data object Setting

    // 文件管理
    @Serializable data class FileDetail(val path: String)
    @Serializable data class FileSearch(val path: String = "/")
    @Serializable data object Favorite
    @Serializable data class FileUpload(val path: String = "/")
    @Serializable data object ShareManager
    @Serializable data class FileImageViewer(val path: String)
    @Serializable data object DownloadManager
    @Serializable data class SelectFolder(val excludePath: String = "")

    // 照片管理
    @Serializable data object Photos
    @Serializable data class AlbumDetail(val albumId: String, val albumName: String)
    @Serializable data class PhotoPreview(val photoId: String)

    // 下载站
    @Serializable data object AddDownloadTask
    @Serializable data class DownloadTaskDetail(val taskId: String)
    @Serializable data class PeerList(val taskId: String)
    @Serializable data class TrackerManager(val taskId: String)
    @Serializable data class BtFileSelect(val taskId: String)

    // Docker
    @Serializable data object Docker
    @Serializable data object DockerImages
    @Serializable data object DockerNetworks
    @Serializable data class DockerContainerDetail(val name: String)

    // 控制面板
    @Serializable data object ControlPanel
    @Serializable data class ControlPanelDetail(val itemId: String)
    @Serializable data class AddShareFolder(val shareName: String? = null)
    @Serializable data class UserDetail(val username: String)

    // 系统
    @Serializable data object SystemInfo
    @Serializable data object GlobalSearch
    @Serializable data object Performance
    @Serializable data object PerformanceHistory
    @Serializable data object ProcessManager
    @Serializable data object TaskManager
    @Serializable data object Notifications
    @Serializable data object Packages
    @Serializable data object VirtualMachine
    @Serializable data class SmartTest(val diskId: String = "")
    @Serializable data object Storage
    @Serializable data object Logs
    @Serializable data object Ddns
    @Serializable data object SecurityScan
    @Serializable data object MediaIndex
    @Serializable data object Terminal

    // 设置
    @Serializable data object About
    @Serializable data object Feedback
    @Serializable data object Preferences
    @Serializable data object Backup
    @Serializable data object GesturePassword
    @Serializable data object OpenSource
    @Serializable data object OtpBind

    // 播放器
    @Serializable data class VideoPlayer(val url: String, val title: String = "")
    @Serializable data class AudioPlayer(val url: String, val title: String = "")
    @Serializable data class PdfViewer(val url: String, val title: String = "")
    @Serializable data class TextEditor(val url: String, val title: String = "")
    @Serializable data class AppWebView(val title: String, val url: String)
}

/**
 * 底部导航项
 */
enum class BottomNavItem(
    val route: Any,
    val titleResId: Int,
    val icon: ImageVector
) {
    Dashboard(
        route = DsmRoute.Dashboard,
        titleResId = R.string.dashboard_console,
        icon = Icons.Filled.Dashboard
    ),
    File(
        route = DsmRoute.FileManager,
        titleResId = R.string.file_files,
        icon = Icons.Filled.Folder
    ),
    Download(
        route = DsmRoute.Download,
        titleResId = R.string.dashboard_download,
        icon = Icons.Filled.Download
    ),
    Setting(
        route = DsmRoute.Setting,
        titleResId = R.string.setting_title,
        icon = Icons.Filled.Settings
    );

    companion object {
        val all: List<BottomNavItem> = entries
    }
}

/**
 * 主导航图
 */
@Composable
fun DSMNavHost(
    navController: NavHostController,
    startDestination: Any = DsmRoute.Login,
    onAuthRequired: (() -> Unit)? = null
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // 登录
        composable<DsmRoute.Login> {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(DsmRoute.Home) {
                        popUpTo<DsmRoute.Login> { inclusive = true }
                    }
                },
                onNavigateToAccounts = {
                    navController.navigate(DsmRoute.Accounts)
                }
            )
        }

        // 账户管理
        composable<DsmRoute.Accounts> {
            AccountsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onAccountSelected = {
                    navController.popBackStack()
                }
            )
        }

        // 主界面（包含底部导航）
        composable<DsmRoute.Home> {
            wang.zengye.dsm.ui.main.MainScreen(
                onLogout = {
                    navController.navigate(DsmRoute.Login) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // 性能监控
        composable<DsmRoute.Performance> {
            PerformanceScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
