[根目录](../../../../../../../../../CLAUDE.md) > app > src > main > java > wang > zengye > dsm > **navigation**

---

# Navigation 模块

## 模块职责

Navigation 模块负责应用的导航管理，使用 Jetpack Navigation Compose 实现类型安全的导航。主要职责包括：
- 定义所有路由（Route）
- 管理导航图（NavGraph）
- 处理导航参数传递
- 管理底部导航栏

## 入口与启动

### 核心文件
- **Navigation.kt**: 主导航图定义，包含所有路由和导航逻辑
- **DashboardNavigation.kt**: 仪表盘相关导航
- **FileNavigation.kt**: 文件管理相关导航
- **DownloadNavigation.kt**: 下载站相关导航
- **PhotosNavigation.kt**: 照片管理相关导航
- **DockerNavigation.kt**: Docker 相关导航
- **ControlPanelNavigation.kt**: 控制面板相关导航
- **SystemNavigation.kt**: 系统相关导航
- **SettingNavigation.kt**: 设置相关导航
- **PlayerNavigation.kt**: 播放器相关导航
- **NavUtils.kt**: 导航工具函数

## 对外接口

### 路由定义 (DsmRoute)

使用 Kotlinx Serialization 实现类型安全的路由：

```kotlin
object DsmRoute {
    // 登录相关
    @Serializable data object Login
    @Serializable data object Accounts

    // 主界面
    @Serializable data object Home

    // 底部导航 Tab
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
    @Serializable data object SmartTest
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
```

### 底部导航 (BottomNavItem)

```kotlin
enum class BottomNavItem(
    val route: Any,
    val titleResId: Int,
    val icon: ImageVector
) {
    Dashboard(DsmRoute.Dashboard, R.string.dashboard_console, Icons.Filled.Dashboard),
    File(DsmRoute.FileManager, R.string.file_files, Icons.Filled.Folder),
    Download(DsmRoute.Download, R.string.dashboard_download, Icons.Filled.Download),
    Setting(DsmRoute.Setting, R.string.setting_title, Icons.Filled.Settings)
}
```

### 主导航图 (DSMNavHost)

```kotlin
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
        composable<DsmRoute.Login> { ... }

        // 账户管理
        composable<DsmRoute.Accounts> { ... }

        // 主界面（包含底部导航）
        composable<DsmRoute.Home> { ... }

        // 其他路由...
    }
}
```

## 关键依赖与配置

### 依赖
- **Navigation Compose**: 2.9.7
- **Kotlinx Serialization**: 1.7.1

### 导航参数传递

使用类型安全的参数传递：

```kotlin
// 导航到详情页
navController.navigate(DsmRoute.FileDetail(path = "/home/documents"))

// 接收参数
composable<DsmRoute.FileDetail> { backStackEntry ->
    val args = backStackEntry.toRoute<DsmRoute.FileDetail>()
    FileDetailScreen(path = args.path)
}
```

### 导航选项

```kotlin
// 清空返回栈
navController.navigate(DsmRoute.Home) {
    popUpTo(0) { inclusive = true }
}

// 单例模式（避免重复创建）
navController.navigate(DsmRoute.Dashboard) {
    launchSingleTop = true
}

// 恢复状态
navController.navigate(DsmRoute.FileManager) {
    restoreState = true
}
```

## 数据模型

### 导航参数类型
- **String**: 路径、ID、名称等
- **Int**: 索引、数量等
- **Boolean**: 标志位
- **可选参数**: 使用默认值（如 `path: String = "/"`）

### URL 编码
对于包含特殊字符的参数（如文件路径），需要进行 URL 编码：
```kotlin
val encodedPath = URLEncoder.encode(path, "UTF-8")
navController.navigate(DsmRoute.FileDetail(path = encodedPath))
```

## 测试与质量

### 导航测试
- 测试路由跳转是否正确
- 测试参数传递是否正确
- 测试返回栈管理是否正确

### 测试示例
```kotlin
@Test
fun testNavigationToFileDetail() {
    val navController = TestNavHostController(ApplicationProvider.getApplicationContext())
    navController.navigate(DsmRoute.FileDetail(path = "/home"))

    assertThat(navController.currentBackStackEntry?.destination?.route)
        .isEqualTo(DsmRoute.FileDetail::class.qualifiedName)
}
```

## 常见问题 (FAQ)

### Q: 如何添加新路由？
1. 在 `DsmRoute` 对象中定义新路由（使用 `@Serializable` 注解）
2. 在对应的 `*Navigation.kt` 文件中添加 `composable` 路由
3. 在需要导航的地方调用 `navController.navigate(DsmRoute.NewRoute(...))`

### Q: 如何处理深层嵌套导航？
使用嵌套导航图：
```kotlin
navigation<DsmRoute.FileManager>(startDestination = DsmRoute.FileDetail::class) {
    composable<DsmRoute.FileDetail> { ... }
    composable<DsmRoute.FileSearch> { ... }
}
```

### Q: 如何处理返回结果？
使用 `SavedStateHandle` 传递结果：
```kotlin
// 设置结果
navController.previousBackStackEntry?.savedStateHandle?.set("result", value)
navController.popBackStack()

// 获取结果
val result = navController.currentBackStackEntry?.savedStateHandle?.get<String>("result")
```

### Q: 如何处理底部导航栏的状态保存？
在 `MainScreen` 中使用 `rememberSaveable` 保存当前选中的 Tab：
```kotlin
var selectedTab by rememberSaveable { mutableStateOf(BottomNavItem.Dashboard) }
```

## 相关文件清单

### 核心文件
- `Navigation.kt` - 主导航图定义
- `NavUtils.kt` - 导航工具函数

### 功能模块导航
- `DashboardNavigation.kt` - 仪表盘导航
- `FileNavigation.kt` - 文件管理导航
- `DownloadNavigation.kt` - 下载站导航
- `PhotosNavigation.kt` - 照片管理导航
- `DockerNavigation.kt` - Docker 导航
- `ControlPanelNavigation.kt` - 控制面板导航
- `SystemNavigation.kt` - 系统导航
- `SettingNavigation.kt` - 设置导航
- `PlayerNavigation.kt` - 播放器导航

---

## 变更记录 (Changelog)

### 2026-03-05 18:07:29
- 初始化模块文档
- 完成模块结构分析

---

**最后更新**: 2026-03-05 18:07:29
