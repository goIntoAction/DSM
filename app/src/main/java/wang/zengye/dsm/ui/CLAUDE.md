[根目录](../../../../../../../../../CLAUDE.md) > app > src > main > java > wang > zengye > dsm > **ui**

---

# UI 模块

## 模块职责

UI 模块是应用的表现层，负责所有用户界面的渲染和交互。采用 MVI（Model-View-Intent）架构模式，使用 Jetpack Compose 构建声明式 UI。主要职责包括：
- 界面渲染（Composable 函数）
- 用户交互处理（Intent）
- 状态管理（ViewModel + State）
- 单次事件处理（Event）
- 导航协调

## 入口与启动

### 主界面
- **MainActivity**: 应用入口，设置 Compose 主题，初始化导航
- **MainScreen**: 主界面容器，包含底部导航栏

### 启动流程
```
MainActivity.onCreate()
  → DSMTheme 主题包裹
  → DSMNavHost 导航图
  → LoginScreen (默认起始页)
  → 登录成功后 → MainScreen (包含底部导航)
```

## 对外接口

### 功能模块列表

#### 核心功能
- **dashboard**: 仪表盘（系统监控、资源使用、存储、网络）
- **file**: 文件管理（浏览、上传、下载、搜索、收藏、分享）
- **download**: 下载站（任务管理、BT/磁力链接、Peer 列表）
- **photos**: 照片管理（相册列表、照片预览）
- **login**: 登录与账户管理

#### 系统管理
- **docker**: Docker 容器管理（容器列表、镜像、网络）
- **control_panel**: 控制面板（用户、组、共享文件夹、网络、存储、证书、终端、防火墙、DDNS、安全扫描、媒体索引）
- **resource_monitor**: 性能监控（历史数据、实时监控）
- **taskmanager**: 任务管理器（进程列表）
- **logcenter**: 日志中心
- **iscsi**: iSCSI 管理
- **virtual_machine**: 虚拟机管理
- **update**: 系统更新

#### 设置与工具
- **settings**: 应用设置（偏好设置、关于、反馈、开源许可、备份、手势密码、OTP 绑定）
- **components**: 通用 UI 组件（PDF 查看器、文本编辑器、视频播放器、音频播放器、WebView）
- **theme**: 主题定义（颜色、字体、形状）
- **base**: 基础类（BaseViewModel）
- **main**: 主界面容器

### MVI 架构规范

每个功能模块包含 4 个核心文件：

1. **Screen.kt**: Composable UI
```kotlin
@Composable
fun FileScreen(
    viewModel: FileViewModel = hiltViewModel(),
    onNavigateToDetail: (String) -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.handleIntent(FileIntent.LoadFiles("/"))
    }

    // UI 渲染
}
```

2. **ViewModel.kt**: 状态管理
```kotlin
@HiltViewModel
class FileViewModel @Inject constructor(
    private val repository: FileRepository
) : BaseViewModel<FileIntent, FileState, FileEvent>() {

    override fun handleIntent(intent: FileIntent) {
        when (intent) {
            is FileIntent.LoadFiles -> loadFiles(intent.path)
        }
    }
}
```

3. **Intent.kt**: 用户意图
```kotlin
sealed interface FileIntent {
    data class LoadFiles(val path: String) : FileIntent
    data class DeleteFile(val path: String) : FileIntent
}
```

4. **Event.kt**: 单次事件
```kotlin
sealed interface FileEvent {
    data class ShowToast(val message: String) : FileEvent
    data class NavigateToDetail(val path: String) : FileEvent
}
```

## 关键依赖与配置

### Compose 依赖
- **Compose BOM**: 2026.02.01
- **Material3**: 最新版本
- **Navigation Compose**: 2.9.7
- **Lifecycle Compose**: 2.10.0

### 第三方 UI 库
- **Coil**: 图片加载（支持 GIF）
- **Telephoto**: 可缩放图片查看器
- **Vico**: 图表库（性能监控）
- **MPV Android**: 视频播放器
- **Seeker**: 进度条库
- **ConstraintLayout Compose**: 约束布局

### Hilt 集成
所有 ViewModel 使用 `@HiltViewModel` 注解，通过 `hiltViewModel()` 获取实例。

## 数据模型

### State 定义
每个 ViewModel 定义自己的 State 数据类：
```kotlin
data class FileState(
    val files: List<FileItem> = emptyList(),
    val currentPath: String = "/",
    val isLoading: Boolean = false,
    val error: String? = null
)
```

### 状态更新
通过 `BaseViewModel.updateState` 方法更新：
```kotlin
updateState { copy(isLoading = true) }
```

### 事件发送
通过 `BaseViewModel.sendEvent` 方法发送单次事件：
```kotlin
sendEvent(FileEvent.ShowToast("删除成功"))
```

## 测试与质量

### 单元测试
- `ui/login/LoginViewModelTest.kt`: 登录 ViewModel 测试
- `ui/file/FileViewModelTest.kt`: 文件 ViewModel 测试
- `ui/dashboard/DashboardViewModelTest.kt`: 仪表盘 ViewModel 测试
- `ui/base/BaseViewModelTest.kt`: 基础 ViewModel 测试

### 测试策略
- 使用 Turbine 测试 StateFlow 和 SharedFlow
- 使用 MockK 模拟 Repository
- 使用 Truth 进行断言
- 测试 Intent 处理、状态更新、Event 发送

### UI 测试
- 使用 Compose UI Test 进行 UI 测试
- 测试用户交互流程

## 常见问题 (FAQ)

### Q: 如何添加新的功能模块？
1. 在 `ui` 目录下创建新模块目录（如 `ui/newfeature`）
2. 创建 4 个核心文件：`Screen.kt`, `ViewModel.kt`, `Intent.kt`, `Event.kt`
3. 在 `navigation/Navigation.kt` 中添加路由定义
4. 在导航图中添加 `composable` 路由

### Q: 如何处理导航？
使用类型安全的导航：
```kotlin
// 定义路由
@Serializable data class FileDetail(val path: String)

// 导航
navController.navigate(DsmRoute.FileDetail(path = "/home"))

// 接收参数
composable<DsmRoute.FileDetail> { backStackEntry ->
    val args = backStackEntry.toRoute<DsmRoute.FileDetail>()
    FileDetailScreen(path = args.path)
}
```

### Q: 如何处理单次事件（如 Toast、导航）？
在 Screen 中使用 `LaunchedEffect` 监听 Event：
```kotlin
LaunchedEffect(Unit) {
    viewModel.event.collect { event ->
        when (event) {
            is FileEvent.ShowToast -> {
                // 显示 Toast
            }
            is FileEvent.NavigateToDetail -> {
                navController.navigate(DsmRoute.FileDetail(event.path))
            }
        }
    }
}
```

### Q: 如何处理加载状态？
在 State 中定义 `isLoading` 字段，在 UI 中根据状态显示加载指示器：
```kotlin
if (state.isLoading) {
    CircularProgressIndicator()
}
```

## 相关文件清单

### 核心文件
- `MainActivity.kt` - 应用入口
- `main/MainScreen.kt` - 主界面容器
- `base/BaseViewModel.kt` - ViewModel 基类
- `theme/Theme.kt` - 主题定义

### 功能模块（每个模块包含 Screen/ViewModel/Intent/Event）
- `dashboard/*` - 仪表盘
- `file/*` - 文件管理
- `download/*` - 下载站
- `photos/*` - 照片管理
- `docker/*` - Docker 管理
- `control_panel/*` - 控制面板
- `resource_monitor/*` - 性能监控
- `taskmanager/*` - 任务管理器
- `logcenter/*` - 日志中心
- `iscsi/*` - iSCSI 管理
- `virtual_machine/*` - 虚拟机管理
- `update/*` - 系统更新
- `login/*` - 登录
- `settings/*` - 设置

### 通用组件
- `components/PdfViewer.kt` - PDF 查看器
- `components/*` - 其他通用组件

---

## 变更记录 (Changelog)

### 2026-03-05 18:07:29
- 初始化模块文档
- 完成模块结构分析
- 识别 306 个 Kotlin 文件

---

**最后更新**: 2026-03-05 18:07:29
